package org.silver.shop.impl.system.manual;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.loader.custom.Return;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.AccessTokenService;
import org.silver.shop.api.system.manual.MpayService;
import org.silver.shop.api.system.tenant.WalletLogService;
import org.silver.shop.config.YmMallConfig;
import org.silver.shop.dao.system.manual.MorderDao;
import org.silver.shop.dao.system.manual.MpayDao;
import org.silver.shop.impl.system.commerce.GoodsRecordServiceImpl;
import org.silver.shop.impl.system.tenant.MerchantFeeServiceImpl;
import org.silver.shop.impl.system.tenant.MerchantWalletServiceImpl;
import org.silver.shop.model.system.commerce.StockContent;
import org.silver.shop.model.system.manual.Appkey;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.manual.MorderSub;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantFeeContent;
import org.silver.shop.model.system.tenant.MerchantRecordInfo;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.shop.model.system.tenant.MerchantWalletLog;
import org.silver.shop.model.system.tenant.ProxyWalletContent;
import org.silver.shop.util.BufferUtils;
import org.silver.shop.util.InvokeTaskUtils;
import org.silver.shop.util.MerchantUtils;
import org.silver.shop.util.RedisInfoUtils;
import org.silver.util.CompressUtils;
import org.silver.util.IdcardValidator;
import org.silver.util.MD5;
import org.silver.util.PhoneUtils;
import org.silver.util.RandomUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.SortUtil;
import org.silver.util.StringEmptyUtils;
import org.silver.util.StringUtil;
import org.silver.util.YmHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

@Service(interfaceClass = MpayService.class)
public class MpayServiceImpl implements MpayService {
	@Resource
	private MpayDao mpayDao;
	@Resource
	private MorderDao morderDao;
	@Autowired
	private AccessTokenService accessTokenService;
	@Autowired
	private GoodsRecordServiceImpl goodsRecordServiceImpl;
	@Autowired
	private MerchantWalletServiceImpl merchantWalletServiceImpl;
	@Autowired
	private WalletLogService walletLogService;
	@Autowired
	private BufferUtils bufferUtils;
	@Autowired
	private MerchantUtils merchantUtils;
	@Autowired
	private InvokeTaskUtils invokeTaskUtils;
	@Autowired
	private MerchantFeeServiceImpl merchantFeeServiceImpl;

	private static Logger logger = LogManager.getLogger(MpayServiceImpl.class);

	// 进出境标志I-进，E-出
	private static final String IEFLAG = "I";

	// 币制默认为人民币
	private static final String CURRCODE = "142";
	/**
	 * 错误标识
	 */
	private static final String ERROR = "error";
	/**
	 * 商户Id
	 */
	private static final String MERCHANT_ID = "merchantId";
	/**
	 * 下划线版本的订单Id
	 */
	private static final String ORDER_ID = "order_id";

	/**
	 * 更新钱包日志
	 * 
	 * @param type
	 *            1-支付单,2-订单
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param serialNo
	 *            流水号
	 * @param proxyId
	 *            代理商Id
	 * @param payAmount
	 *            推送单总金额
	 * @param proxyParentName
	 *            代理商名称
	 * @return Map
	 */
	public Map<String, Object> updateWallet(int type, String merchantId, String merchantName, String serialNo,
			String proxyId, double payAmount, String proxyParentName) {
		if (type == 1
				|| type == 2 && StringEmptyUtils.isNotEmpty(merchantId) && StringEmptyUtils.isNotEmpty(merchantName)
						&& StringEmptyUtils.isNotEmpty(serialNo) && StringEmptyUtils.isNotEmpty(proxyId)
						&& payAmount > 0 && StringEmptyUtils.isNotEmpty(proxyParentName)) {
			Map<String, Object> merchantWalletMap = saveMerchantWalletLog(type, merchantId, merchantName, proxyId,
					proxyParentName, serialNo, payAmount);
			if (!"1".equals(merchantWalletMap.get(BaseCode.STATUS.toString()) + "")) {
				return merchantWalletMap;
			}
			double serviceFee = Double.parseDouble(merchantWalletMap.get("serviceFee") + "");
			return saveProxyWalletLog(type, merchantId, merchantName, proxyId, proxyParentName, serialNo, serviceFee);
		} else {
			return ReturnInfoUtils.errorInfo("更新钱包信息,参数出错,请核实信息!");
		}
	}

	/**
	 * 保存代理商钱包
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param proxyId
	 *            代理商
	 * @param proxyParentName
	 *            代理商名称
	 * @param treadeNo
	 *            交易编号
	 * @param serviceFee
	 *            佣金(平台费用千分之二)
	 * @return Map
	 */
	private Map<String, Object> saveProxyWalletLog(int type, String merchantId, String merchantName, String proxyId,
			String proxyParentName, String serialNo, double serviceFee) {
		Map<String, Object> reMap2 = merchantWalletServiceImpl.checkWallet(3, proxyId, proxyParentName);
		if (!"1".equals(reMap2.get(BaseCode.STATUS.toString()))) {
			return reMap2;
		}
		ProxyWalletContent proxyWallet = (ProxyWalletContent) reMap2.get(BaseCode.DATAS.toString());
		double balance = proxyWallet.getBalance();
		proxyWallet.setBalance(balance + serviceFee);
		if (!morderDao.update(proxyWallet)) {
			return ReturnInfoUtils.errorInfo("钱包更新余额失败!");
		}
		JSONObject param = new JSONObject();
		param.put(MERCHANT_ID, merchantId);
		param.put("merchantName", merchantName);
		param.put("proxyId", proxyId);
		param.put("proxyName", proxyParentName);
		// 钱包交易日志流水名称
		if (type == 1) {
			param.put("entPayNo", serialNo);
			param.put("entPayName", "推送支付单服务费");
		} else if (type == 2) {
			param.put("entOrderNo", serialNo);
			param.put("entPayName", "推送订单服务费");
		}
		param.put("payAmount", serviceFee);
		param.put("oldBalance", balance);
		// 分类1-佣金、2-充值、3-提现、4-缴费
		param.put("type", 1);
		Map<String, Object> logMap = walletLogService.addWalletLog(3, param);
		if (!"1".equals(logMap.get(BaseCode.STATUS.toString()))) {
			return ReturnInfoUtils.errorInfo("保存代理商钱包日志记录失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 添加商户钱包日志
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param proxyId
	 *            代理商Id
	 * @param proxyParentName
	 *            代理商名称
	 * @param serialNo
	 *            流水号
	 * @param payAmount
	 *            推送单金额
	 * @return Map
	 */
	private Map<String, Object> saveMerchantWalletLog(int type, String merchantId, String merchantName, String proxyId,
			String proxyParentName, String serialNo, double payAmount) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> reMap = merchantWalletServiceImpl.checkWallet(1, merchantId, merchantName);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "创建钱包失败!");
			return statusMap;
		}
		MerchantWalletContent merchantWallet = (MerchantWalletContent) reMap.get(BaseCode.DATAS.toString());
		double merchantBalance = merchantWallet.getBalance();
		// 平台服务费
		double serviceFee = payAmount * 0.002;
		merchantWallet.setBalance(merchantBalance - serviceFee);
		if (!morderDao.update(merchantWallet)) {
			statusMap.clear();
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.LOSS_SESSION.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "钱包更新余额失败!");
			return statusMap;
		}
		// 当更新钱包余额后,进行商户钱包日志记录
		JSONObject param = new JSONObject();
		param.put(MERCHANT_ID, merchantId);
		param.put("merchantName", merchantName);
		if (type == 1) {
			// 钱包交易日志流水名称
			param.put("entPayNo", serialNo);
			param.put("entPayName", "推送支付单服务费");
		} else if (type == 2) {
			param.put("entOrderNo", serialNo);
			param.put("entPayName", "推送订单服务费");
		}
		param.put("payAmount", serviceFee);
		param.put("oldBalance", merchantBalance);
		param.put("proxyId", proxyId);
		param.put("proxyName", proxyParentName);
		// 分类:1-购物、2-充值、3-提现、4-缴费、5-代理商佣金
		param.put("type", 5);
		Map<String, Object> reWalletLogMap = walletLogService.addWalletLog(2, param);
		if (!"1".equals(reWalletLogMap.get(BaseCode.STATUS.toString()))) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getMsg());
			statusMap.put(BaseCode.MSG.toString(), "保存商户钱包日志,服务器繁忙!");
			return statusMap;
		}
		statusMap.clear();
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put("serviceFee", serviceFee);
		return statusMap;
	}

	@Override
	public Object sendMorderRecord(String merchantId, Map<String, Object> customsMap, String orderNoPack,
			String proxyParentId, String merchantName, String proxyParentName) {

		// 总参数
		Map<String, Object> params = new HashMap<>();
		List<Map<String, Object>> errorList = new ArrayList<>();
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(orderNoPack);
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("订单Id信息包格式错误！");
		}
		if (jsonList == null || jsonList.isEmpty()) {
			return ReturnInfoUtils.errorInfo("订单Id信息包不能为空!");
		}
		int eport = Integer.parseInt(customsMap.get("eport") + "");
		String ciqOrgCode = customsMap.get("ciqOrgCode") + "";
		String customsCode = customsMap.get("customsCode") + "";
		// 校验前台传递口岸、海关、智检编码
		Map<String, Object> reCustomsMap = goodsRecordServiceImpl.checkCustomsPort(eport, customsCode, ciqOrgCode);
		if (!"1".equals(reCustomsMap.get(BaseCode.STATUS.toString()))) {
			return reCustomsMap;
		}
		// 获取商户在对应口岸的备案信息
		Map<String, Object> merchantRecordMap = merchantUtils.getMerchantRecordInfo(merchantId, eport);
		if (!"1".equals(merchantRecordMap.get(BaseCode.STATUS.toString()))) {
			return merchantRecordMap;
		}
		MerchantRecordInfo merchantRecordInfo = (MerchantRecordInfo) merchantRecordMap.get(BaseCode.DATAS.toString());
		customsMap.put("ebEntNo", merchantRecordInfo.getEbEntNo());
		customsMap.put("ebEntName", merchantRecordInfo.getEbEntName());
		customsMap.put("ebpEntNo", merchantRecordInfo.getEbpEntNo());
		customsMap.put("ebpEntName", merchantRecordInfo.getEbpEntName());
		// 获取商户信息
		Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(merchantId);
		if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
			return reMerchantMap;
		}
		Merchant merchant = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
		int thirdPartyFlag = merchant.getThirdPartyFlag();
		// 当商户标识为第三方平台商户时,则使用第三方appkey
		if (thirdPartyFlag == 2) {
			Map<String, Object> reAppkeyMap = merchantUtils.getMerchantAppkey(merchantId);
			if (!"1".equals(reAppkeyMap.get(BaseCode.STATUS.toString()))) {
				return reAppkeyMap;
			}
			Appkey appkey = (Appkey) reAppkeyMap.get(BaseCode.DATAS.toString());
			// 打包至海关备案信息Map中
			customsMap.put("appkey", appkey.getApp_key());
			customsMap.put("appSecret", appkey.getApp_secret());
		} else {
			// 当不是第三方时则使用银盟商城appkey
			customsMap.put("appkey", YmMallConfig.APPKEY);
			customsMap.put("appSecret", YmMallConfig.APPSECRET);
		}
		// 请求获取tok
		Map<String, Object> reTokMap = accessTokenService.getRedisToks(customsMap.get("appkey") + "",
				customsMap.get("appSecret") + "");
		if (!"1".equals(reTokMap.get(BaseCode.STATUS.toString()))) {
			return ReturnInfoUtils.errorInfo(reTokMap.get("errMsg") + "");
		}
		String tok = reTokMap.get(BaseCode.DATAS.toString()) + "";
		//
		String merchantFeeId = customsMap.get("merchantFeeId") + "";
		Map<String, Object> reCheckMap = computingCostsManualOrder(jsonList, merchantId, merchantName, merchantFeeId);
		if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
			return reCheckMap;
		}
		//
		// 总数
		int totalCount = jsonList.size();
		params.put(MERCHANT_ID, merchantId);
		params.put("merchantName", merchantName);
		params.put("tok", tok);
		// 获取流水号
		String serialNo = "orderRecord_" + SerialNoUtils.getSerialNo("orderRecord");
		params.put("serialNo", serialNo);
		Map<String, Object> reMap = invokeTaskUtils.commonInvokeTask(2, totalCount, jsonList, errorList, customsMap,
				params);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		Map<String, Object> statusMap = new HashMap<>();
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), "执行成功,开始推送订单备案.......");
		statusMap.put("serialNo", serialNo);
		return statusMap;
	}

	/**
	 * 计算商户钱包余额是否足够推送此次手工订单
	 * 
	 * @param jsonList
	 *            订单Id
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param merchantFeeId
	 *            商户口岸费率流水Id
	 * 
	 * @return Map
	 */
	private Map<String, Object> computingCostsManualOrder(JSONArray jsonList, String merchantId, String merchantName,
			String merchantFeeId) {
		if (jsonList == null) {
			return ReturnInfoUtils.errorInfo("订单Id信息集合不能为空!");
		}
		Map<String, Object> reCheckMap = beforePushingCheckManualOrder(
				JSONArray.toList(jsonList, new HashMap<>(), new JsonConfig()));
		if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
			return reCheckMap;
		}
		List<Object> newList = (List<Object>) reCheckMap.get(BaseCode.DATAS.toString());
		// 初始化平台服务费
		double fee;
		// 查询商户钱包余额是否有足够的钱
		Map<String, Object> reMap = merchantWalletServiceImpl.checkWallet(1, merchantId, merchantName);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		MerchantWalletContent merchantWallet = (MerchantWalletContent) reMap.get(BaseCode.DATAS.toString());
		// 统计未发起过备案的手工订单实际支付金额的总额
		double totalAmountPaid = morderDao.statisticalManualOrderAmount(newList);
		// 当小于0时,代表查询数据库信息错误
		if (totalAmountPaid < 0) {
			return ReturnInfoUtils.errorInfo("查询手工订单总金额失败,服务器繁忙!");
		} else if (totalAmountPaid > 0) {// 当查询出来手工订单总金额大于0时,进行平台服务费计算
			// 当商户口岸费率Id不为空时获取商户当前口岸的平台服务费率
			if (StringEmptyUtils.isNotEmpty(merchantFeeId)) {
				Map<String, Object> reFeeMap = merchantFeeServiceImpl.getMerchantFeeInfo(merchantFeeId);
				if (!"1".equals(reFeeMap.get(BaseCode.STATUS.toString()))) {
					return reFeeMap;
				}
				MerchantFeeContent merchantFee = (MerchantFeeContent) reFeeMap.get(BaseCode.DATAS.toString());
				fee = merchantFee.getPlatformFee();
			} else {
				fee = 0.001;
			}
			// 钱包余额
			double balance = merchantWallet.getBalance();
			// 平台服务费
			double serviceFee = totalAmountPaid * fee;
			// 商户钱包扣钱进代理商钱包
			Map<String, Object> reWalletDeductionMap = merchantWalletDeduction(merchantWallet, balance, serviceFee);
			if (!"1".equals(reWalletDeductionMap.get(BaseCode.STATUS.toString()))) {
				return reWalletDeductionMap;
			}
			// 保存钱包流水log
			Map<String, Object> reSaveWalletMap = saveWalletLog(merchantId, balance, serviceFee);
			if (!"1".equals(reSaveWalletMap.get(BaseCode.STATUS.toString()))) {
				return reSaveWalletMap;
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	private Map<String, Object> merchantWalletDeduction(MerchantWalletContent merchantWallet, double balance,
			double serviceFee) {
		if (merchantWallet == null) {
			return ReturnInfoUtils.errorInfo("商户钱包扣费时,请求参数不能为空!");
		}
		// 扣除服务费后余额
		double surplus = balance - serviceFee;
		if (surplus < 0) {
			return ReturnInfoUtils.errorInfo("推送订单失败,余额不足,请先充值后再进行操作!");
		}
		merchantWallet.setBalance(surplus);
		merchantWallet.setUpdateDate(new Date());
		merchantWallet.setUpdateBy("system");
		if (!morderDao.update(merchantWallet)) {
			return ReturnInfoUtils.errorInfo("推送订单失败,钱包扣费失败,请重试!");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 推送订单前进行订单校验,保证扣费订单都未校验通过的订单信息
	 * 
	 * @param list
	 * @return
	 */
	private Map<String, Object> beforePushingCheckManualOrder(List<Map<String, Object>> list) {
		if (list == null) {
			return ReturnInfoUtils.errorInfo("推送订单扣费前校验订单商品信息失败,请求参数不能为空!");
		}
		List<Morder> reList = morderDao.findByPropertyIn(list);
		List<Object> newOrderIdList = new ArrayList<>();
		if (reList != null && !reList.isEmpty()) {
			for (int i = 0; i < reList.size(); i++) {
				Morder order = reList.get(i);
				Map<String, Object> reCheckOrderMap = checkManualOrderInfo(order);
				if ("1".equals(reCheckOrderMap.get(BaseCode.STATUS.toString()))) {
					Map<String, Object> item = new HashMap<>();
					item.put("orderNo", order.getOrder_id());
					newOrderIdList.add(item);
				} 
			}
		} else {
			return ReturnInfoUtils.errorInfo("推送订单前校验订单数据失败!");
		}
		return ReturnInfoUtils.successDataInfo(newOrderIdList);
	}

	/**
	 * 商户钱包扣费并且记录钱包日志
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param balance
	 *            余额
	 * @param serviceFee
	 *            平台服务费
	 * @return Map
	 */
	private Map<String, Object> saveWalletLog(String merchantId, double balance, double serviceFee) {
		if (StringEmptyUtils.isEmpty(merchantId)) {
			return ReturnInfoUtils.errorInfo("钱包扣费商户Id不能为空");
		}
		Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(merchantId);
		if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
			return reMerchantMap;
		}
		Merchant merchant = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
		MerchantWalletLog walletLog = new MerchantWalletLog();
		walletLog.setEntPayName("订单推送服务费");
		walletLog.setMerchantId(merchant.getMerchantId());
		walletLog.setMerchantName(merchant.getMerchantName());
		walletLog.setBeforeChangingBalance(balance);
		walletLog.setPayAmount(serviceFee);
		walletLog.setAfterChangeBalance(balance - serviceFee);
		// 分类1-购物、2-充值、3-提现、4-缴费、5-支付代理商佣金
		walletLog.setType(5);
		// walletLog.setNote("推送[" + orderCount + "]单,订单申报平台服务费!");
		walletLog.setCreateBy("system");
		walletLog.setCreateDate(new Date());
		// 状态：1-交易成功、2-交易失败、3-交易关闭
		walletLog.setStatus(1);
		// 代理商暂定获取商户的代理商信息
		walletLog.setProxyId(merchant.getAgentParentId());
		walletLog.setProxyName(merchant.getAgentParentName());
		if (!morderDao.add(walletLog)) {
			return ReturnInfoUtils.errorInfo("保存商户钱包日志失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 准备开始推送订单备案
	 * 
	 * @param dataList
	 *            订单信息
	 * @param errorList
	 *            错误信息
	 * @param customsMap
	 *            海关信息
	 */
	public final void startSendOrderRecord(JSONArray dataList, List<Map<String, Object>> errorList,
			Map<String, Object> customsMap, Map<String, Object> paramsMap) {
		String merchantId = paramsMap.get(MERCHANT_ID) + "";
		String tok = paramsMap.get("tok") + "";
		paramsMap.put("name", "orderRecord");
		// 累计金额
		for (int i = 0; i < dataList.size(); i++) {
			try {
				Map<String, Object> orderMap = (Map<String, Object>) dataList.get(i);
				String orderNo = orderMap.get("orderNo") + "";
				Map<String, Object> param = new HashMap<>();
				param.put("merchant_no", merchantId);
				param.put(ORDER_ID, orderNo);
				List<Morder> orderList = morderDao.findByProperty(Morder.class, param, 1, 1);
				param.clear();
				param.put(ORDER_ID, orderNo);
				param.put("deleteFlag", 0);
				List<MorderSub> orderSubList = morderDao.findByProperty(MorderSub.class, param, 0, 0);
				if (orderList == null || orderSubList == null) {
					String msg = "订单号[" + orderNo + "]查询订单信息失败,服务器繁忙!";
					RedisInfoUtils.commonErrorInfo(msg, errorList, ERROR, paramsMap);
					continue;
				} else {
					Morder order = orderList.get(0);
					Map<String, Object> reCheckMap = checkManualOrderInfo(order);
					if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
						RedisInfoUtils.commonErrorInfo(reCheckMap.get(BaseCode.MSG.toString()) + "", errorList, ERROR,
								paramsMap);
						continue;
					}
					Map<String, Object> reOrderMap = sendOrder(customsMap, orderSubList, tok, order);
					if (!"1".equals(reOrderMap.get(BaseCode.STATUS.toString()) + "")) {
						StringBuilder msg = new StringBuilder("订单号[" + orderNo + "]-->");
						Map<String, Object> reUpdateMap = updateOrderErrorStatus(orderNo);
						if (!"1".equals(reUpdateMap.get(BaseCode.STATUS.toString()))) {
							msg.append(reUpdateMap.get(BaseCode.MSG.toString()) + "");
						} else {
							msg.append(reOrderMap.get(BaseCode.MSG.toString()) + "");
						}
						RedisInfoUtils.commonErrorInfo(msg.toString(), errorList, ERROR, paramsMap);
						continue;
					} else {
						String reOrderMessageID = reOrderMap.get("messageID") + "";
						// 更新服务器返回订单Id
						Map<String, Object> reUpdateMap = updateOrderInfo(orderNo, reOrderMessageID, customsMap);
						if (!"1".equals(reUpdateMap.get(BaseCode.STATUS.toString()) + "")) {
							RedisInfoUtils.commonErrorInfo(reUpdateMap.get(BaseCode.MSG.toString()) + "", errorList,
									ERROR, paramsMap);
							continue;
						}
					}
				}
				Thread.sleep(200);
			} catch (Exception e) {
				logger.error("--------推送订单信息错误-------", e);
			} finally {
				bufferUtils.writeRedis(errorList, paramsMap);
			}
		}
		bufferUtils.writeCompletedRedis(errorList, paramsMap);
	}

	/**
	 * 推送手工订单信息时校验数据
	 * 
	 * @param order
	 *            手工订单信息
	 * @return Map
	 */
	private Map<String, Object> checkManualOrderInfo(Morder order) {
		if (order == null) {
			return ReturnInfoUtils.errorInfo("推送订单时,校验订单数据,订单参数不能为空!");
		}
		if (order.getFCY() > 2000) {
			return ReturnInfoUtils.errorInfo("订单号[" + order.getOrder_id() + "]推送失败,订单商品总金额超过2000,请核对订单信息!");
		}
		if (!checkProvincesCityAreaCode(order)) {
			return ReturnInfoUtils.errorInfo("订单号[" + order.getOrder_id() + "]推送失败,订单收货人省市区编码不能为空,请核对订单信息!");
		}
		if (!PhoneUtils.isPhone(order.getRecipientTel().trim())) {
			return ReturnInfoUtils.errorInfo("订单号[" + order.getOrder_id() + "]推送失败,收货人手机号码格式不正确,请核对订单信息!");
		}
		if (!PhoneUtils.isPhone(order.getOrderDocTel().trim())) {
			return ReturnInfoUtils.errorInfo("订单号[" + order.getOrder_id() + "]推送失败,下单人手机号码格式不正确,请核对订单信息!");
		}
		if (!IdcardValidator.validate18Idcard(order.getOrderDocId().trim())) {
			return ReturnInfoUtils.errorInfo("订单号[" + order.getOrder_id() + "]推送失败,下单人身份证号码实名认证失败,请核对订单信息!");
		}
		// if (!IdcardValidator.validate18Idcard(order.getRecipientID())) {
		// return ReturnInfoUtils.errorInfo("订单号[" + order.getOrder_id() +
		// "]推送失败,收货人身份证号码实名认证失败,请核对订单信息!");
		// }
		String recipientName = order.getRecipientName().trim();
		if (!StringUtil.isChinese(recipientName) || recipientName.contains("先生") || recipientName.contains("女士")
				|| recipientName.contains("小姐")) {
			return ReturnInfoUtils.errorInfo("订单号[" + order.getOrder_id() + "]推送失败,收货人姓名错误,请核对订单信息!");
		}
		String orderDocName = order.getOrderDocName().trim();
		if (!StringUtil.isChinese(orderDocName) || orderDocName.contains("先生") || orderDocName.contains("女士")
				|| orderDocName.contains("小姐")) {
			return ReturnInfoUtils.errorInfo("订单号[" + order.getOrder_id() + "]推送失败,订单人姓名错误,请核对订单信息!");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 更新订单推送错误状态
	 * 
	 * @param orderNo
	 *            订单编号
	 * @return Map
	 */
	private Map<String, Object> updateOrderErrorStatus(String orderNo) {
		Map<String, Object> params = new HashMap<>();
		params.put(ORDER_ID, orderNo);
		List<Morder> reList = morderDao.findByProperty(Morder.class, params, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("推送订单备案接收失败后,更新订单接收状态时查询订单信息失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			for (int i = 0; i < reList.size(); i++) {
				Morder order = reList.get(i);
				// 订单接收状态： 0-未发起,1-已发起,2-接收成功,3-接收失败
				order.setStatus(3);
				order.setUpdate_date(new Date());
				if (!morderDao.update(order)) {
					return ReturnInfoUtils.errorInfo("推送订单备案接收失败后,更新订单接收状态失败,服务器繁忙!");
				}
			}
			return ReturnInfoUtils.successInfo();
		} else {
			return ReturnInfoUtils.errorInfo("推送订单备案接收失败后,更新订单接收状态时,找不到订单信息!");
		}
	}

	/**
	 * 校验手工订单省市区编码是否为空
	 * 
	 * @param order
	 *            手工订单信息
	 * @return boolean
	 */
	private boolean checkProvincesCityAreaCode(Morder order) {
		if (StringEmptyUtils.isNotEmpty(order.getRecipientAreaCode())) {
			return true;
		}
		if (StringEmptyUtils.isNotEmpty(order.getRecipientCityCode())) {
			return true;
		}
		return StringEmptyUtils.isNotEmpty(order.getRecipientProvincesCode());
	}

	/**
	 * 更新订单返回信息、并且将订单状态修改为备案中
	 * 
	 * @param orderNo
	 *            订单编号
	 * @param reOrderMessageID
	 *            服务器返回流水Id
	 * @param customsMap
	 *            海关口岸信息包
	 * @return Map
	 */
	private Map<String, Object> updateOrderInfo(String orderNo, String reOrderMessageID,
			Map<String, Object> customsMap) {
		String eport = customsMap.get("eport") + "";
		String ciqOrgCode = customsMap.get("ciqOrgCode") + "";
		String customsCode = customsMap.get("customsCode") + "";
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put(ORDER_ID, orderNo);
		List<Morder> reList = morderDao.findByProperty(Morder.class, paramMap, 0, 0);
		if (reList != null && !reList.isEmpty()) {
			for (int i = 0; i < reList.size(); i++) {
				Morder order = reList.get(i);
				order.setOrder_serial_no(reOrderMessageID);
				// 备案状态：1-未备案,2-备案中,3-备案成功、4-备案失败
				order.setOrder_record_status(2);
				// 订单接收状态： 0-未发起,1-已发起,2-接收成功,3-接收失败
				order.setStatus(2);
				order.setUpdate_date(new Date());
				if (StringEmptyUtils.isNotEmpty(eport) && StringEmptyUtils.isNotEmpty(ciqOrgCode)
						&& StringEmptyUtils.isNotEmpty(customsCode)) {
					order.setEport(eport);
					order.setCiqOrgCode(ciqOrgCode);
					order.setCustomsCode(customsCode);
				}
				if (!morderDao.update(order)) {
					return ReturnInfoUtils.errorInfo("更新服务器返回messageID错误!");
				}
			}
			return ReturnInfoUtils.successInfo();
		} else {
			return ReturnInfoUtils.errorInfo("更新支付订单返回messageID错误,服务器繁忙！");
		}
	}

	/**
	 * 发起手工订单备案
	 * 
	 * @param customsMap
	 *            海关信息
	 * @param orderSubList
	 *            备案商品信息List
	 * @param tok
	 *            服务端tok
	 * @param order
	 *            订单信息
	 * @return
	 */
	private final Map<String, Object> sendOrder(Map<String, Object> customsMap, List<MorderSub> orderSubList,
			String tok, Morder order) {
		String timestamp = String.valueOf(System.currentTimeMillis());
		List<JSONObject> goodsList = new ArrayList<>();
		List<JSONObject> orderJsonList = new ArrayList<>();
		Map<String, Object> orderMap = new HashMap<>();
		JSONObject goodsJson = null;
		JSONObject orderJson = new JSONObject();
		// 排序商品seq
		SortUtil.sortList(orderSubList, "seqNo", "ASC");
		String ebEntNo = "";
		String ebEntName = "";
		// 电子口岸16位编码
		String dzkaNo = "";

		for (int i = 0; i < orderSubList.size(); i++) {
			goodsJson = new JSONObject();
			MorderSub goodsInfo = orderSubList.get(i);
			goodsJson.element("Seq", i + 1);
			goodsJson.element("EntGoodsNo", goodsInfo.getEntGoodsNo());
			goodsJson.element("CIQGoodsNo", goodsInfo.getCIQGoodsNo());
			goodsJson.element("CusGoodsNo", goodsInfo.getCusGoodsNo());
			goodsJson.element("HSCode", goodsInfo.getHSCode());
			goodsJson.element("GoodsName", goodsInfo.getGoodsName());
			goodsJson.element("GoodsStyle", goodsInfo.getGoodsStyle());
			goodsJson.element("GoodsDescribe", "");
			goodsJson.element("OriginCountry", goodsInfo.getOriginCountry());
			goodsJson.element("BarCode", goodsInfo.getBarCode());
			goodsJson.element("Brand", goodsInfo.getBrand());
			goodsJson.element("Qty", goodsInfo.getQty());
			goodsJson.element("Unit", goodsInfo.getUnit());
			goodsJson.element("Price", goodsInfo.getPrice());
			goodsJson.element("Total", goodsInfo.getTotal());
			goodsJson.element("CurrCode", "142");
			goodsJson.element("Notes", "");
			// 企邦商品业务字段
			String jsonGoods = goodsInfo.getSpareParams();
			if (StringEmptyUtils.isNotEmpty(jsonGoods)) {
				JSONObject params = JSONObject.fromObject(jsonGoods);
				// 企邦专属字段
				goodsJson.element("marCode", params.get("marCode"));
				goodsJson.element("sku", params.get("SKU"));
				// 电商企业编号
				ebEntNo = params.get("ebEntNo") + "";
				// 电商企业名称
				ebEntName = params.get("ebEntName") + "";
				// 电子口岸(16)编码
				dzkaNo = params.get("DZKNNo") + "";
			}
			goodsList.add(goodsJson);
		}
		orderJson.element("orderGoodsList", goodsList);
		orderJson.element("EntOrderNo", order.getOrder_id());
		orderJson.element("OrderStatus", 1);
		orderJson.element("PayStatus", 0);
		orderJson.element("OrderGoodTotal", order.getFCY());
		orderJson.element("OrderGoodTotalCurr", order.getFcode());
		orderJson.element("Freight", 0);
		orderJson.element("Tax", order.getTax());
		orderJson.element("OtherPayment", 0);
		orderJson.element("OtherPayNotes", "");
		orderJson.element("OtherCharges", 0);
		orderJson.element("ActualAmountPaid", order.getActualAmountPaid());
		orderJson.element("RecipientName", order.getRecipientName());
		orderJson.element("RecipientAddr", order.getRecipientAddr());
		orderJson.element("RecipientTel", order.getRecipientTel());
		orderJson.element("RecipientCountry", "142");
		orderJson.element("RecipientProvincesCode", order.getRecipientProvincesCode());
		orderJson.element("RecipientCityCode", order.getRecipientCityCode());
		orderJson.element("RecipientAreaCode", order.getRecipientAreaCode());
		orderJson.element("OrderDocAcount", order.getOrderDocAcount());
		orderJson.element("OrderDocName", order.getOrderDocName());
		orderJson.element("OrderDocType", order.getOrderDocType());
		orderJson.element("OrderDocId", order.getOrderDocId());
		orderJson.element("OrderDocTel", order.getOrderDocTel());
		orderJson.element("OrderDate", order.getOrderDate());
		orderJson.element("entPayNo", order.getTrade_no());
		orderJson.element("waybill", order.getWaybill());
		addQBOrderInfo(order, orderJson);
		orderJsonList.add(orderJson);
		// 客戶端签名
		String clientsign = "";
		// YM APPKEY = "4a5de70025a7425dabeef6e8ea752976";
		String appkey = customsMap.get("appkey") + "";
		try {
			clientsign = MD5
					.getMD5((appkey + tok + orderJsonList.toString() + YmMallConfig.MANUALORDERNOTIFYURL + timestamp)
							.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			logger.error("------推送订单失败,MD5加密客户端签名失败-----", e);
			return ReturnInfoUtils.errorInfo("推送订单失败,MD5加密客户端签名失败!");
		}
		// 0:商品备案 1:订单推送 2:支付单推送
		orderMap.put("type", 1);
		int eport = Integer.parseInt(customsMap.get("eport") + "");
		// 1:广州电子口岸(目前只支持BC业务) 2:南沙智检(支持BBC业务)
		// 1-特殊监管区域BBC保税进口;2-保税仓库BBC保税进口;3-BC直购进口
		int businessType = eport == 1 ? 3 : 2;
		orderMap.put("businessType", businessType);
		orderMap.put("ieFlag", IEFLAG);
		orderMap.put("currCode", CURRCODE);
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置时间格式
		String inputDate = sdf.format(date);
		// 商品发起备案(录入)日期
		orderMap.put("inputDate", inputDate);
		// 1:广州电子口岸(目前只支持BC业务) 2:南沙智检(支持BBC业务)
		orderMap.put("eport", eport);
		if (StringEmptyUtils.isNotEmpty(ebEntNo) && StringEmptyUtils.isNotEmpty(ebEntName)) {
			// 电商企业编号
			orderMap.put("ebEntNo", ebEntNo);
			// 电商企业名称
			orderMap.put("ebEntName", ebEntName);
		} else {
			String ebEntNo2 = eport == 1 ? "C010000000537118" : "1509007917";
			orderMap.put("ebEntNo", ebEntNo2);
			// 电商企业名称
			orderMap.put("ebEntName", "广州银盟信息科技有限公司");
		}
		orderMap.put("ciqOrgCode", customsMap.get("ciqOrgCode"));
		orderMap.put("customsCode", customsMap.get("customsCode"));
		orderMap.put("appkey", appkey);
		orderMap.put("clientsign", clientsign);
		orderMap.put("timestamp", timestamp);
		orderMap.put("datas", orderJsonList.toString());
		orderMap.put("notifyurl", YmMallConfig.MANUALORDERNOTIFYURL);
		orderMap.put("note", "");
		// 报文类型
		String opType = customsMap.get("opType") + "";
		if (StringEmptyUtils.isNotEmpty(opType)) {
			// A-新增；M-修改；D-
			orderMap.put("opType", opType);
		} else {
			// 当前台不传参数时默认
			orderMap.put("opType", "A");
		}
		// 是否像海关发送
		// orderMap.put("uploadOrNot", false);
		// 发起订单备案
		String resultStr = YmHttpUtil.HttpPost("https://ym.191ec.com/silver-web/Eport/Report", orderMap);
		// 当端口号为2(智检时)再往电子口岸多发送一次
		if (eport == 2) {
			// 1:广州电子口岸(目前只支持BC业务) 2:南沙智检(支持BBC业务)
			orderMap.put("eport", 1);
			if (StringEmptyUtils.isNotEmpty(dzkaNo) && StringEmptyUtils.isNotEmpty(ebEntName)) {
				// 电商企业编号
				orderMap.put("ebEntNo", dzkaNo);
				// 电商企业名称
				orderMap.put("ebEntName", ebEntName);
			} else {
				orderMap.put("ebEntNo", "C010000000537118");
				// 电商企业名称
				orderMap.put("ebEntName", "广州银盟信息科技有限公司");
			}
			System.out.println("-----------------第二次向电子口岸发送------------");
			// 检验检疫机构代码
			orderMap.put("ciqOrgCode", "443400");
			resultStr = YmHttpUtil.HttpPost("https://ym.191ec.com/silver-web/Eport/Report", orderMap);
		}
		if (StringEmptyUtils.isNotEmpty(resultStr)) {
			return JSONObject.fromObject(resultStr);
		} else {
			return ReturnInfoUtils.errorInfo("服务器接收订单信息失败,请重试！");
		}
	}

	/**
	 * 根据业务需求添加企邦订单专属字段
	 * 
	 * @param order
	 *            订单信息
	 * @param orderJson
	 *            订单集合
	 */
	private void addQBOrderInfo(Morder order, JSONObject orderJson) {
		// 企邦字段
		String orderSpare = order.getSpareParams();
		if (StringEmptyUtils.isNotEmpty(orderSpare)) {
			JSONObject params = JSONObject.fromObject(orderSpare);
			// 企邦快递承运商
			orderJson.element("tmsServiceCode", params.getString("ehsEntName"));
		}
	}

	@Override
	public Map<String, Object> updateOrderRecordInfo(Map<String, Object> datasMap) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置时间格式
		String defaultDate = sdf.format(date); // 格式化当前时间
		Map<String, Object> orMap = new HashMap<>();
		orMap.put("order_serial_no", datasMap.get("messageID") + "");
		orMap.put(ORDER_ID, datasMap.get("entOrderNo") + "");
		String reMsg = datasMap.get("msg") + "";
		List<Morder> reList = morderDao.findByPropertyOr2(Morder.class, orMap, 1, 1);
		if (reList != null && !reList.isEmpty()) {
			Morder order = reList.get(0);
			String orderId = order.getOrder_id();
			String merchantId = order.getMerchant_no();
			String status = datasMap.get("status") + "";
			String note = order.getOrder_re_note();
			if (StringEmptyUtils.isEmpty(note)) {
				note = "";
			}
			order.setOrder_re_note(note + defaultDate + ":" + reMsg + ";");
			if ("1".equals(status)) {
				// 已经返回过一次备案成功后
				if (note.contains("新增申报成功") && order.getOrder_record_status() == 3) {
					System.out.println("------重复订单回执拦截------");
					return ReturnInfoUtils.successInfo();
				}
				//
				findOrderGoodsInfo(orderId, merchantId);
				order.setOrder_record_status(3);
			} else {
				// 备案失败
				if (reMsg.contains("旧报文数据") || reMsg.contains("订单数据已存在") || reMsg.contains("重复申报")) {
					return updateOldOrderInfo(order);
				}
				order.setOrder_record_status(4);
			}
			return updateOrderRecordInfo(order);
		} else {
			return ReturnInfoUtils.errorInfo("根据订单Id与msgId未找到数据,请核对信息!");
		}
	}

	private Map<String, Object> updateOldOrderInfo(Morder order) {
		if (order.getOrder_record_status() == 4) {
			System.out.println("-------旧报文备案失败修改为成功--");
			order.setOrder_record_status(3);
			return updateOrderRecordInfo(order);
		}
		return ReturnInfoUtils.successInfo();
	}

	private Map<String, Object> updateOrderRecordInfo(Morder order) {
		order.setUpdate_date(new Date());
		if (!morderDao.update(order)) {
			return ReturnInfoUtils.errorInfo("异步更新订单备案信息错误!");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 根据商户Id与订单Id查询到订单下所有商品信息
	 * 
	 * @param orderId
	 *            订单Id
	 * @param merchantId
	 *            商户Id
	 */
	private void findOrderGoodsInfo(String orderId, String merchantId) {
		Map<String, Object> params = new HashMap<>();
		params.put(ORDER_ID, orderId);
		params.put("merchant_no", merchantId);
		List<MorderSub> reGoodsList = morderDao.findByProperty(MorderSub.class, params, 0, 0);
		if (reGoodsList != null && !reGoodsList.isEmpty()) {
			for (MorderSub goods : reGoodsList) {
				String entGoodsNo = goods.getEntGoodsNo();
				String spareParams = goods.getSpareParams();
				// 当是第三方商户提供的商品信息时
				if (StringEmptyUtils.isNotEmpty(spareParams)) {
					JSONObject json = JSONObject.fromObject(spareParams);
					String marCode = json.get("marCode") + "";
					entGoodsNo = entGoodsNo + "_" + marCode;
				}
				// 订单商品数量
				int count = goods.getQty();
				updateStockDoneCount(entGoodsNo, count, merchantId);
			}
		} else {
			System.out.println("---------订单商品未找到----------");
		}
	}

	/**
	 * 根据商户Id与商品自编号，查询出来的订单商品数量更新库存售卖数量
	 * 
	 * @param entGoodsNo
	 *            商品自编号
	 * @param count
	 *            数量
	 * @param merchantId
	 *            商户Id
	 */
	private void updateStockDoneCount(String entGoodsNo, int count, String merchantId) {
		Map<String, Object> params = new HashMap<>();
		params.put("entGoodsNo", entGoodsNo);
		params.put(MERCHANT_ID, merchantId);
		List<StockContent> reStockList = morderDao.findByProperty(StockContent.class, params, 1, 1);
		if (reStockList != null && !reStockList.isEmpty()) {
			for (StockContent stock : reStockList) {
				int oldDoneCount = stock.getDoneCount();
				int oldReadIngCount = stock.getReadingCount();
				stock.setDoneCount(oldDoneCount + count);
				stock.setReadingCount(oldReadIngCount + RandomUtils.getRandom(1));
				stock.setUpdateBy("system");
				stock.setUpdateDate(new Date());
				morderDao.update(stock);
			}
		}

	}

	/**
	 * 订单备案成功后,进行订单平台服务费扣款
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param orderId
	 *            订单Id
	 * @param orderTotalAmount
	 *            订单总金额
	 * @return Map
	 */
	private Map<String, Object> orderToll(String merchantId, String orderId, double orderTotalAmount) {
		if (StringEmptyUtils.isEmpty(merchantId) || StringEmptyUtils.isEmpty(orderId)
				|| StringEmptyUtils.isEmpty(orderTotalAmount)) {
			return ReturnInfoUtils.errorInfo("清算订单服务费,请求参数不能为空！");
		}
		Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(merchantId);
		if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
			return reMerchantMap;
		}
		Merchant merchant = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());

		// 2018-05-17新增根据商户口岸费率计费
		// Map<String, Object> reMap = deductionPlatformFee(merchant, orderId,
		// orderTotalAmount, ciqOrgCode, customsCode);

		Map<String, Object> reUpdateWalletMap = updateWallet(2, merchant.getMerchantId(), merchant.getMerchantName(),
				orderId, merchant.getAgentParentId(), orderTotalAmount, merchant.getAgentParentName());
		if (!"1".equals(reUpdateWalletMap.get(BaseCode.STATUS.toString()))) {
			return reUpdateWalletMap;
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> downOrderExcelByDateSerialNo(String merchantId, String merchantName, String filePath,
			String date, String serialNo) {
		Table reList = morderDao.getOrderAndOrderGoodsInfo(merchantId, date, Integer.parseInt(serialNo));
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器异常!");
		} else if (!reList.getRows().isEmpty()) {
			String dataStr = Transform.tableToJson(reList).getJSONArray("rows") + "";
			try {
				dataStr = CompressUtils.compress(dataStr);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return ReturnInfoUtils.successDataInfo(dataStr, 0);
		} else {
			return ReturnInfoUtils.errorInfo("未找到订单数据！");
		}
	}

}

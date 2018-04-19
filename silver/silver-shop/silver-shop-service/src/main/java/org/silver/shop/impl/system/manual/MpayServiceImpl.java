package org.silver.shop.impl.system.manual;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPOutputStream;

import javax.annotation.Resource;

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
import org.silver.shop.impl.system.tenant.MerchantWalletServiceImpl;
import org.silver.shop.model.system.commerce.StockContent;
import org.silver.shop.model.system.manual.Appkey;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.manual.MorderSub;
import org.silver.shop.model.system.manual.Mpay;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantRecordInfo;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.shop.model.system.tenant.ProxyWalletContent;
import org.silver.shop.task.OrderRecordTask;
import org.silver.shop.util.BufferUtils;
import org.silver.shop.util.InvokeTaskUtils;
import org.silver.shop.util.MerchantUtils;
import org.silver.shop.util.RedisInfoUtils;
import org.silver.util.CalculateCpuUtils;
import org.silver.util.CompressUtils;
import org.silver.util.DateUtil;
import org.silver.util.JedisUtil;
import org.silver.util.MD5;
import org.silver.util.RandomUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.SerializeUtil;
import org.silver.util.SortUtil;
import org.silver.util.SplitListUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.util.TaskUtils;
import org.silver.util.YmHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.StringUtil;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

@Service(interfaceClass = MpayService.class)
public class MpayServiceImpl implements MpayService {
	// 进出境标志I-进，E-出
	private static final String IEFLAG = "I";

	// 币制默认为人民币
	private static final String CURRCODE = "142";
	/**
	 * 错误标识
	 */
	private static final String ERROR = "error";

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
	private InvokeTaskUtils invokeTaskUtils;
	@Autowired
	private MerchantUtils merchantUtils;

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
		param.put("merchantId", merchantId);
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
		param.put("merchantId", merchantId);
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
			return ReturnInfoUtils.errorInfo("订单编号错误,请核实！");
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
		Map<String, Object> reCheckMap = computingCostsManualOrder(jsonList, merchantId, merchantName);
		if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
			return reCheckMap;
		}

		// 请求获取tok
		Map<String, Object> reTokMap = accessTokenService.getRedisToks(customsMap.get("appkey") + "",
				customsMap.get("appSecret") + "");
		if (!"1".equals(reTokMap.get(BaseCode.STATUS.toString()))) {
			return ReturnInfoUtils.errorInfo(reTokMap.get("errMsg") + "");
		}
		String tok = reTokMap.get(BaseCode.DATAS.toString()) + "";
		// 获取流水号
		String serialNo = "orderRecord_" + SerialNoUtils.getSerialNo("orderRecord");
		// 总数
		int totalCount = jsonList.size();

		params.put("merchantId", merchantId);
		params.put("merchantName", merchantName);
		params.put("tok", tok);
		params.put("serialNo", serialNo);
		//
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
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @return Map
	 */
	private Map<String, Object> computingCostsManualOrder(JSONArray jsonList, String merchantId, String merchantName) {
		// 查询商户钱包余额是否有足够的钱
		Map<String, Object> reMap = merchantWalletServiceImpl.checkWallet(1, merchantId, merchantName);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return ReturnInfoUtils.errorInfo("创建钱包失败!");
		}
		MerchantWalletContent merchantWallet = (MerchantWalletContent) reMap.get(BaseCode.DATAS.toString());
		double merchantBalance = merchantWallet.getBalance();
		double totalAmountPaid = morderDao
				.statisticalManualOrderAmount(JSONArray.toList(jsonList, new HashMap<>(), new JsonConfig()));
		if (totalAmountPaid < 0) {
			return ReturnInfoUtils.errorInfo("查询手工订单总金额失败,服务器繁忙!");
		}
		// 平台服务费
		double serviceFee = totalAmountPaid * 0.001;
		if ((merchantBalance - serviceFee) < 0) {
			return ReturnInfoUtils.errorInfo("推送订单失败,余额不足,请先充值后再进行操作!");
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
		String merchantId = paramsMap.get("merchantId") + "";
		String tok = paramsMap.get("tok") + "";
		paramsMap.put("name", "orderRecord");

		// 累计金额
		for (int i = 0; i < dataList.size(); i++) {
			try {
				Map<String, Object> orderMap = (Map<String, Object>) dataList.get(i);
				String orderNo = orderMap.get("orderNo") + "";
				Map<String, Object> param = new HashMap<>();
				param.put("merchant_no", merchantId);
				param.put("order_id", orderNo);
				List<Morder> orderList = morderDao.findByProperty(Morder.class, param, 1, 1);
				param.clear();
				param.put("order_id", orderNo);
				param.put("deleteFlag", 0);
				List<MorderSub> orderSubList = morderDao.findByProperty(MorderSub.class, param, 0, 0);
				if (orderList == null || orderSubList == null) {
					String msg = "订单号[" + orderNo + "]查询订单信息失败,服务器繁忙!";
					RedisInfoUtils.commonErrorInfo(msg, errorList, ERROR, paramsMap);
					continue;
				} else {
					Morder order = orderList.get(0);
					if (order.getFCY() > 2000) {
						String msg = "订单号[" + order.getOrder_id() + "]推送失败,订单商品总金额超过2000,请核对订单信息!";
						RedisInfoUtils.commonErrorInfo(msg, errorList, ERROR, paramsMap);
						continue;
					}
					if (!checkProvincesCityAreaCode(order)) {
						String msg = "订单号[" + order.getOrder_id() + "]推送失败,订单收货人省市区编码不能为空,请核对订单信息!";
						RedisInfoUtils.commonErrorInfo(msg, errorList, ERROR, paramsMap);
						continue;
					}

					Map<String, Object> reOrderMap = sendOrder(customsMap, orderSubList, tok, order);
					if (!"1".equals(reOrderMap.get(BaseCode.STATUS.toString()) + "")) {
						String msg = "订单号[" + orderNo + "]-->" + reOrderMap.get(BaseCode.MSG.toString()) + "";
						RedisInfoUtils.commonErrorInfo(msg, errorList, ERROR, paramsMap);
						continue;
					} else {
						// 当订单状态为未备案时才进行订单服务费计算
						if (order.getOrder_record_status() == 1) {
							Map<String, Object> reOrderTollMap = orderToll(merchantId, order.getOrder_id(),
									order.getFCY());
							if (!"1".equals(reOrderTollMap.get(BaseCode.STATUS.toString()))) {
								String msg = reOrderTollMap.get(BaseCode.MSG.toString()) + "";
								RedisInfoUtils.commonErrorInfo(msg, errorList, ERROR, paramsMap);
								continue;
							}
						}
						String reOrderMessageID = reOrderMap.get("messageID") + "";
						// 更新服务器返回订单Id
						Map<String, Object> reOrderMap2 = updateOrderInfo(orderNo, reOrderMessageID);
						if (!"1".equals(reOrderMap2.get(BaseCode.STATUS.toString()) + "")) {
							String msg = reOrderMap2.get(BaseCode.MSG.toString()) + "";
							RedisInfoUtils.commonErrorInfo(msg, errorList, ERROR, paramsMap);
							continue;
						}
					}
				}
				Thread.sleep(200);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				bufferUtils.writeRedis(errorList, paramsMap);
			}
		}
		bufferUtils.writeCompletedRedis(errorList, paramsMap);
	}

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
	 * @return Map
	 */
	private Map<String, Object> updateOrderInfo(String orderNo, String reOrderMessageID) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("order_id", orderNo);
		List<Morder> reList = morderDao.findByProperty(Morder.class, paramMap, 0, 0);
		if (reList != null && !reList.isEmpty()) {
			for (int i = 0; i < reList.size(); i++) {
				Morder order = reList.get(i);
				order.setOrder_serial_no(reOrderMessageID);
				// 备案状态：1-未备案,2-备案中,3-备案成功、4-备案失败
				order.setOrder_record_status(2);
				order.setUpdate_date(new Date());
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
	 * 发起订单备案
	 * 
	 * @param customsMap
	 * 
	 * @param orderSubList
	 *            备案商品信息List
	 * @param tok
	 * 
	 * @param order
	 *            订单信息
	 * @return
	 */
	private final Map<String, Object> sendOrder(Map<String, Object> customsMap, List<MorderSub> orderSubList,
			String tok, Morder order) {
		String timestamp = String.valueOf(System.currentTimeMillis());
		Map<String, Object> statusMap = new HashMap<>();
		List<JSONObject> goodsList = new ArrayList<>();
		List<JSONObject> orderJsonList = new ArrayList<>();
		Map<String, Object> orderMap = new HashMap<>();
		JSONObject goodsJson = null;
		JSONObject orderJson = new JSONObject();
		// 排序商品seq
		SortUtil.sortList(orderSubList, "seqNo", "ASC");
		String ebEntNo = "";
		String ebEntName = "";
		String DZKANo = "";

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
				DZKANo = params.get("DZKNNo") + "";
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
		orderJson.element("OrderDate", order.getCreate_date());
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
			e.printStackTrace();
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
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
		// 商品发起备案(录入)日期
		String inputDate = sdf.format(date);
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
		// orderMap.put("opType", "M");
		// 是否像海关发送
		// orderMap.put("uploadOrNot", false);
		// 发起订单备案
		// String resultStr =
		// YmHttpUtil.HttpPost("http://192.168.1.120:8080/silver-web/Eport/Report",
		// orderMap);
		String resultStr = YmHttpUtil.HttpPost("https://ym.191ec.com/silver-web/Eport/Report", orderMap);
		// 当端口号为2(智检时)再往电子口岸多发送一次
		if (eport == 2) {
			// 1:广州电子口岸(目前只支持BC业务) 2:南沙智检(支持BBC业务)
			orderMap.put("eport", 1);
			if (StringEmptyUtils.isNotEmpty(DZKANo) && StringEmptyUtils.isNotEmpty(ebEntName)) {
				// 电商企业编号
				orderMap.put("ebEntNo", DZKANo);
				// 电商企业名称
				orderMap.put("ebEntName", ebEntName);
			} else {
				orderMap.put("ebEntNo", "C010000000537118");
				// 电商企业名称
				orderMap.put("ebEntName", "广州银盟信息科技有限公司");
			}
			System.out.println("-----------------第二次向电子口岸发送------------");
			// String resultStr2 =
			// YmHttpUtil.HttpPost("http://192.168.1.120:8080/silver-web/Eport/Report",
			// orderMap);

			String resultStr2 = YmHttpUtil.HttpPost("https://ym.191ec.com/silver-web/Eport/Report", orderMap);
			if (StringEmptyUtils.isNotEmpty(resultStr2)) {
				return JSONObject.fromObject(resultStr2);
			} else {
				return ReturnInfoUtils.errorInfo("第二次推送订单信息接收失败,请重试！");
			}
		}
		if (StringUtil.isNotEmpty(resultStr)) {
			return JSONObject.fromObject(resultStr);
		} else {
			return ReturnInfoUtils.errorInfo("服务器接受订单信息失败,请重试！");
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
		orMap.put("order_id", datasMap.get("entOrderNo") + "");
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
					System.out.println("------重复申报成功拦截------");
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
		params.put("order_id", orderId);
		params.put("merchant_no", merchantId);
		List<MorderSub> reGoodsList = morderDao.findByProperty(MorderSub.class, params, 0, 0);
		if (reGoodsList != null && !reGoodsList.isEmpty()) {
			for (MorderSub goods : reGoodsList) {
				System.out.println("--------遍历所有订单下的商品-----");
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
		params.put("merchantId", merchantId);
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
	 * @param price
	 *            订单总金额
	 * @return Map
	 */
	private Map<String, Object> orderToll(String merchantId, String orderId, double price) {
		if (StringEmptyUtils.isEmpty(merchantId) || StringEmptyUtils.isEmpty(orderId)
				|| StringEmptyUtils.isEmpty(price)) {
			return ReturnInfoUtils.errorInfo("清算订单服务费,请求参数不能为空！");
		}
		Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(merchantId);
		if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
			return reMerchantMap;
		}
		Merchant merchant = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
		// 商户钱包扣钱进代理商钱包
		Map<String, Object> reUpdateWalletMap = updateWallet(2, merchant.getMerchantId(), merchant.getMerchantName(),
				orderId, merchant.getAgentParentId(), price, merchant.getAgentParentName());
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

package org.silver.shop.impl.system.manual;

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

import javax.annotation.Resource;

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
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.manual.MorderSub;
import org.silver.shop.model.system.manual.Mpay;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantRecordInfo;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.shop.model.system.tenant.ProxyWalletContent;
import org.silver.shop.task.OrderRecordTask;
import org.silver.shop.util.BufferUtils;
import org.silver.shop.util.RedisInfoUtils;
import org.silver.util.DateUtil;
import org.silver.util.MD5;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
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

@Service(interfaceClass = MpayService.class)
public class MpayServiceImpl implements MpayService {
	// 进出境标志I-进，E-出
	private static final String IEFLAG = "I";

	// 币制默认为人民币
	private static final String CURRCODE = "142";
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
	/**
	 * 查询商户钱包余额是否有足够的钱
	 * 
	 * @param type
	 *            1-支付,2-订单
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param treadeNo
	 *            交易流水号
	 * @param payAmount
	 *            金额
	 * @return Map
	 */
	public Map<String, Object> checkWallet(int type, String merchantId, String merchantName, String serialNo,
			double payAmount) {
		Map<String, Object> statusMap = new HashMap<>();
		// 查询商户钱包余额是否有足够的钱
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
		if (merchantBalance - serviceFee < 0) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.UNKNOWN.getStatus());
			if (type == 1) {
				statusMap.put(BaseCode.MSG.toString(), "支付号[" + serialNo + "]推送失败,钱包余额不足,请续费后重试!");
			} else if (type == 2) {
				statusMap.put(BaseCode.MSG.toString(), "订单[" + serialNo + "]推送失败,钱包余额不足,请续费后重试!");
			}
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return statusMap;
	}

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
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> reMap2 = merchantWalletServiceImpl.checkWallet(3, proxyId, proxyParentName);
		if (!"1".equals(reMap2.get(BaseCode.STATUS.toString()))) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "创建钱包失败!");
			return statusMap;
		}
		ProxyWalletContent proxyWallet = (ProxyWalletContent) reMap2.get(BaseCode.DATAS.toString());
		double balance = proxyWallet.getBalance();
		proxyWallet.setBalance(balance + serviceFee);
		if (!morderDao.update(proxyWallet)) {
			statusMap.clear();
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.LOSS_SESSION.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "钱包更新余额失败!");
			return statusMap;
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
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getMsg());
			statusMap.put(BaseCode.MSG.toString(), "保存代理商钱包日志记录失败,服务器繁忙!");
			return statusMap;
		}
		statusMap.clear();
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;

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

	/**
	 * 根据商户Id及口岸获取商户对应的备案信息
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param eport
	 *            口岸
	 * @return Map
	 */
	private final Map<String, Object> getMerchantRecordInfo(String merchantId, int eport) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> param = new HashMap<>();
		param.put("merchantId", merchantId);
		param.put("customsPort", eport);
		List<MerchantRecordInfo> recordList = morderDao.findByProperty(MerchantRecordInfo.class, param, 1, 1);
		if (recordList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getStatus());
		} else if (!recordList.isEmpty()) {
			MerchantRecordInfo entity = recordList.get(0);
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), entity);
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getStatus());
		}
		return statusMap;
	}

	@Override
	public Object sendMorderRecord(String merchantId, Map<String, Object> customsMap, String orderNoPack,
			String proxyParentId, String merchantName, String proxyParentName) {
		Map<String, Object> statusMap = new HashMap<>();
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
		Map<String, Object> merchantRecordMap = getMerchantRecordInfo(merchantId, eport);
		if (!"1".equals(merchantRecordMap.get(BaseCode.STATUS.toString()))) {
			return merchantRecordMap;
		}
		MerchantRecordInfo merchantRecordInfo = (MerchantRecordInfo) merchantRecordMap.get(BaseCode.DATAS.toString());
		customsMap.put("ebEntNo", merchantRecordInfo.getEbEntNo());
		customsMap.put("ebEntName", merchantRecordInfo.getEbEntName());
		customsMap.put("ebpEntNo", merchantRecordInfo.getEbpEntNo());
		customsMap.put("ebpEntName", merchantRecordInfo.getEbpEntName());

		// 请求获取tok
		Map<String, Object> tokMap = accessTokenService.getAccessToken();
		if (!"1".equals(tokMap.get(BaseCode.STATUS.toString()))) {
			return tokMap;
		}
		String tok = tokMap.get(BaseCode.DATAS.toString()) + "";
		// 获取流水号
		String serialNo = "orderRecord_" + SerialNoUtils.getSerialNo("orderRecord");
		// 总数
		int totalCount = jsonList.size();
		ExecutorService threadPool = Executors.newCachedThreadPool();
		// 获取当前计算机CPU线程个数
		int cpuCount = Runtime.getRuntime().availableProcessors();
		if (totalCount < cpuCount) {
			OrderRecordTask threadTask = new OrderRecordTask(jsonList, merchantId, merchantName, errorList, customsMap,
					tok, totalCount, serialNo, this);
			threadPool.submit(threadTask);
		} else {
			// 分批处理
			Map<String, Object> reMap = SplitListUtils.batchList(jsonList, cpuCount);
			if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
				return reMap;
			}
			//
			List dataList = (List) reMap.get(BaseCode.DATAS.toString());
			for (int i = 0; i < dataList.size(); i++) {
				List newList = (List) dataList.get(i);
				OrderRecordTask threadTask = new OrderRecordTask(JSONArray.fromObject(newList), merchantId,
						merchantName, errorList, customsMap, tok, totalCount, serialNo, this);
				threadPool.submit(threadTask);
			}
		}
		// TaskUtils.invokeTask(totalCount, jsonList, merchantId, merchantName,
		// errorList, reCustomsMap, tok, serialNo, this);
		threadPool.shutdown();
		statusMap.put("status", 1);
		statusMap.put("msg", "执行成功,开始推送订单备案.......");
		statusMap.put("serialNo", serialNo);
		return statusMap;
	}

	/**
	 * 准备开始推送订单备案
	 * 
	 * @param dataList
	 *            订单信息
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param errorList
	 *            错误信息
	 * @param customsMap
	 *            海关信息
	 * @param tok
	 * @param totalCount
	 *            总行数
	 * @param serialNo
	 *            批次号
	 * @param threadPool
	 */
	public final void startSendOrderRecord(JSONArray dataList, String merchantId, String merchantName,
			List<Map<String, Object>> errorList, Map<String, Object> customsMap, String tok, int totalCount,
			String serialNo) {
		// 累计金额
		double cumulativeAmount = 0.0;
		for (int i = 0; i < dataList.size(); i++) {
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
				String msg = "订单号:[" + orderNo + "]订单查询失败,服务器繁忙!";
				RedisInfoUtils.commonErrorInfo(msg, errorList, totalCount, serialNo, "orderRecord",1);
				continue;
			} else {
				Morder order = orderList.get(0);
				if (order.getFCY() > 2000) {
					String msg = "订单号:[" + order.getOrder_id() + "]推送失败,订单商品总金额超过2000,请核对订单信息!";
					RedisInfoUtils.commonErrorInfo(msg, errorList, totalCount, serialNo, "orderRecord",1);
					continue;
				}
				if (!checkProvincesCityAreaCode(order)) {
					String msg = "订单号:[" + order.getOrder_id() + "]推送失败,订单收货人省市区编码不能为空,请核对订单信息!";
					RedisInfoUtils.commonErrorInfo(msg, errorList, totalCount, serialNo, "orderRecord",1);
					continue;
				}

				// 备案状态：1-未备案,2-备案中,3-备案成功、4-备案失败
				/*
				 * if (order.getOrder_record_status() == 3) { Map<String,
				 * Object> errMap = new HashMap<>();
				 * errMap.put(BaseCode.MSG.toString(), "[" + orderNo +
				 * "]订单已成功备案,无需再次发起!"); errorList.add(errMap);
				 * BufferUtils.writeRedis("1", errorList, (realRowCount - 1),
				 * serialNo, "order") continue; }
				 */

				// 订单商品总额
				double fcy = order.getFCY();
				// 将每个订单商品总额进行累加,计算当前金额下是否有足够的余额支付费用
				cumulativeAmount = fcy += cumulativeAmount;
				Map<String, Object> checkMap = checkWallet(2, merchantId, merchantName, orderNo, cumulativeAmount);
				if (!"1".equals(checkMap.get(BaseCode.STATUS.toString()))) {
					String msg = checkMap.get(BaseCode.MSG.toString()) + "";
					RedisInfoUtils.commonErrorInfo(msg, errorList, totalCount, serialNo, "orderRecord",1);
					continue;
				}
				Map<String, Object> reOrderMap = sendOrder(merchantId, customsMap, orderSubList, tok, order);
				System.out.println("->>>>>>>>>>>" + reOrderMap);
				if (!"1".equals(reOrderMap.get(BaseCode.STATUS.toString()) + "")) {
					String msg = "订单号:[" + orderNo + "]-->" + reOrderMap.get(BaseCode.MSG.toString()) + "";
					RedisInfoUtils.commonErrorInfo(msg, errorList, totalCount, serialNo, "orderRecord",1);
					continue;
				} else {
					String reOrderMessageID = reOrderMap.get("messageID") + "";
					// 更新服务器返回订单Id
					Map<String, Object> reOrderMap2 = updateOrderInfo(orderNo, reOrderMessageID);
					if (!"1".equals(reOrderMap2.get(BaseCode.STATUS.toString()) + "")) {
						String msg = reOrderMap2.get(BaseCode.MSG.toString()) + "";
						RedisInfoUtils.commonErrorInfo(msg, errorList, totalCount, serialNo, "orderRecord",1);
						continue;
					}
				}
			}
			bufferUtils.writeRedis(errorList, totalCount, serialNo, "orderRecord");
		}
		bufferUtils.writeCompletedRedis(errorList, totalCount, serialNo, "orderRecord", merchantId, merchantName);
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
	 * 更新订单返回信息及订单状态
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
			paramMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			paramMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			return paramMap;
		} else {
			return ReturnInfoUtils.errorInfo("更新支付订单返回messageID错误,服务器繁忙！");
		}
	}

	/**
	 * 发起订单备案
	 * 
	 * @param goodsRecordInfo
	 *            商品备案头信息实体类
	 * @param reGoodsRecordDetailList
	 *            备案商品信息List
	 * @param reOrderGoodsList
	 *            订单商品List
	 * @param tok
	 * @param orderRecordInfo
	 * @return
	 */
	private final Map<String, Object> sendOrder(String merchantId, Map<String, Object> recordMap,
			List<MorderSub> orderSubList, String tok, Morder order) {
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
		try {
			clientsign = MD5.getMD5((YmMallConfig.APPKEY + tok + orderJsonList.toString()
					+ YmMallConfig.MANUALORDERNOTIFYURL + timestamp).getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		}
		// 0:商品备案 1:订单推送 2:支付单推送
		orderMap.put("type", 1);
		int eport = Integer.parseInt(recordMap.get("eport") + "");
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
		orderMap.put("ciqOrgCode", recordMap.get("ciqOrgCode"));
		orderMap.put("customsCode", recordMap.get("customsCode"));
		orderMap.put("appkey", YmMallConfig.APPKEY);
		orderMap.put("clientsign", clientsign);
		orderMap.put("timestamp", timestamp);
		orderMap.put("datas", orderJsonList.toString());
		orderMap.put("notifyurl", YmMallConfig.MANUALORDERNOTIFYURL);
		orderMap.put("note", "");
		// 是否像海关发送
		orderMap.put("uploadOrNot", false);
		// 发起订单备案
		// String resultStr =
		// YmHttpUtil.HttpPost("http://192.168.1.120:8080/silver-web/Eport/Report",
		// orderMap);
		String resultStr = YmHttpUtil.HttpPost("http://ym.191ec.com/silver-web/Eport/Report", orderMap);
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
			String resultStr2 = YmHttpUtil.HttpPost("http://ym.191ec.com/silver-web/Eport/Report", orderMap);
			if (StringEmptyUtils.isNotEmpty(resultStr2)) {
				return JSONObject.fromObject(resultStr2);
			} else {
				return ReturnInfoUtils.errorInfo("服务器接受订单信息失败,请重试！");
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
		Map<String, Object> paramMap = new HashMap<>();
		Map<String, Object> orMap = new HashMap<>();
		orMap.put("order_serial_no", datasMap.get("messageID") + "");
		orMap.put("order_id", datasMap.get("entOrderNo") + "");
		String reMsg = datasMap.get("msg") + "";
		List<Morder> reList = morderDao.findByPropertyOr2(Morder.class, orMap, 0, 0);
		if (reList != null && !reList.isEmpty()) {
			Morder order = reList.get(0);
			paramMap.clear();
			paramMap.put("merchantId", order.getMerchant_no());
			List<Merchant> reMerchantList = morderDao.findByProperty(Merchant.class, paramMap, 1, 1);
			Merchant merchant = null;
			if (reMerchantList != null && !reMerchantList.isEmpty()) {
				merchant = reMerchantList.get(0);
			} else {
				return ReturnInfoUtils.errorInfo("查询商户信息失败,请核对信息!");
			}
			String status = datasMap.get("status") + "";
			String note = order.getOrder_re_note();
			if ("null".equals(note) || note == null) {
				note = "";
			}
			if ("1".equals(status)) {
				// 商户钱包扣钱进代理商钱包
				Map<String, Object> reUpdateWalletMap = updateWallet(2, merchant.getMerchantId(),
						merchant.getMerchantName(), order.getOrder_id(), merchant.getProxyParentId(), order.getFCY(),
						merchant.getProxyParentName());
				if (!"1".equals(reUpdateWalletMap.get(BaseCode.STATUS.toString()))) {
					return reUpdateWalletMap;
				}
				// 支付单备案状态修改为成功
				order.setOrder_record_status(3);
			} else {
				// 备案失败
				order.setOrder_record_status(4);
			}
			order.setOrder_re_note(note + defaultDate + ":" + reMsg + ";");
			order.setUpdate_date(new Date());
			if (!morderDao.update(order)) {
				return ReturnInfoUtils.errorInfo("异步更新订单备案信息错误!");
			}
			return ReturnInfoUtils.successInfo();
		} else {
			return ReturnInfoUtils.errorInfo("根据订单Id与msgId未找到数据,请核对信息!");
		}
	}

	@Override
	public Map<String, Object> downOrderExcelByDateSerialNo(String merchantId, String merchantName, String filePath,
			String date, String serialNo) {
		Map<String, Object> statusMap = new HashMap<>();
		Table reList = morderDao.getOrderAndOrderGoodsInfo(merchantId, date, Integer.parseInt(serialNo));
		if (reList != null && reList.getRows().size() > 0) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put("datas", Transform.tableToJson(reList).getJSONArray("rows"));
			return statusMap;
		}
		statusMap.put("status", -3);
		return statusMap;
	}

}

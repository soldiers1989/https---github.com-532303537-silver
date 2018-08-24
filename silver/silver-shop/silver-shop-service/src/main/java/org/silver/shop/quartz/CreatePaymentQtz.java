package org.silver.shop.quartz;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.common.BaseCode;
import org.silver.shop.api.system.AccessTokenService;
import org.silver.shop.api.system.cross.PaymentService;
import org.silver.shop.api.system.tenant.MemberWalletService;
import org.silver.shop.api.system.tenant.MerchantIdCardCostService;
import org.silver.shop.config.YmMallConfig;
import org.silver.shop.dao.system.commerce.OrderDao;
import org.silver.shop.model.common.base.IdCard;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.tenant.MerchantIdCardCostContent;
import org.silver.shop.task.WalletTransferTask;
import org.silver.util.DateUtil;
import org.silver.util.MD5;
import org.silver.util.MapSortUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.util.YmHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.sf.json.JSONObject;

/**
 * 定时任务,扫描商户自助申报的订单,生成支付单信息
 */
@Component
public class CreatePaymentQtz {
	/**
	 * 驼峰命名:商户Id
	 */
	private static final String MERCHANT_ID = "merchantId";
	/**
	 * 驼峰命名：订单Id
	 */
	private static final String ORDER_ID = "orderId";
	/**
	 * 口岸
	 */
	private static final String E_PORT = "eport";

	/**
	 * 检验检疫机构代码
	 */
	private static final String CIQ_ORG_CODE = "ciqOrgCode";
	/**
	 * 失败标识
	 */
	private static final String FAILURE = "failure";
	/**
	 * 主管海关代码
	 */
	private static final String CUSTOMS_CODE = "customsCode";
	/**
	 * 计数器
	 */
	private static AtomicInteger counter = new AtomicInteger(0);

	private static Logger logger = LogManager.getLogger(Object.class);

	@Autowired
	private OrderDao orderDao;
	@Autowired
	private PaymentService paymentService;
	@Autowired
	private MerchantIdCardCostService merchantIdCardCostService;
	@Autowired
	private AccessTokenService accessTokenService;
	@Autowired
	private MemberWalletService memberWalletService;

	/**
	 * 定时任务扫描商户自助申报的订单,未生成支付流水的订单信息
	 */
	public void createPaymentJob() {
		if (counter.get() % 50 == 0) {
			System.out.println("--生成支付单扫描--");
		}
		Map<String, Object> params = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.MONTH, -12);
		calendar.set(Calendar.HOUR_OF_DAY, 00);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		// 设置为24小时制
		params.put("startTime", calendar.getTime());
		calendar.setTime(new Date());
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		params.put("endTime", calendar.getTime());
		// 申报状态：1-未申报,2-申报中,3-申报成功、4-申报失败、10-申报中(待系统处理)
		params.put("order_record_status", 10);
		// 网关接收状态： 0-未发起,1-已发起,2-接收成功,3-接收失败
		params.put("status", 1);
		//
		params.put("tradeNoFlag", " IS NULL ");
		try {
			int page = 1;
			int size = 300;
			List<Morder> reOrderList = orderDao.findByPropertyLike(Morder.class, params, null, page, size);
			while (reOrderList != null && !reOrderList.isEmpty()) {
				if (page > 1) {
					reOrderList = orderDao.findByPropertyLike(Morder.class, params, null, page, size);
				}
				if (reOrderList != null && !reOrderList.isEmpty()) {
					for (Morder order : reOrderList) {
						// 保存支付单并且更新订单流水号
						if (!savePayment(order)) {
							logger.error(order.getOrder_id() + "--创建交易流水号失败--");
						}
					}
				}
				page++;
			}
			counter.getAndIncrement();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("--定时扫描待发起备案的订单信息错误--", e);
		}
	}

	/**
	 * 保存支付单信息实体
	 * 
	 * @param order
	 *            订单信息实体类
	 * @return
	 */
	private boolean savePayment(Morder order) {
		if (order == null) {
			return false;
		}
		Map<String, Object> checkInfoMap = new HashMap<>();
		checkInfoMap.put(ORDER_ID, order.getOrder_id());
		checkInfoMap.put("orderDocId", order.getOrderDocId());
		checkInfoMap.put("orderDocName", order.getOrderDocName());
		Map<String, Object> reCheckMap = paymentService.checkPaymentInfo(checkInfoMap);
		if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
			logger.error("系统扫描自助申报订单,创建支付单失败->" + reCheckMap.get(BaseCode.MSG.toString()));
			return updateOrder(order, reCheckMap.get(BaseCode.MSG.toString()) + "");
		}
		Map<String, Object> paymentMap = new HashMap<>();
		String merchantId = order.getMerchant_no();
		paymentMap.put(MERCHANT_ID, merchantId);
		int count = SerialNoUtils.getSerialNo("paymentId");
		String tradeNo = SerialNoUtils.createTradeNo("01O", (count + 1));
		paymentMap.put("tradeNo", tradeNo);
		paymentMap.put(ORDER_ID, order.getOrder_id());
		paymentMap.put("amount", order.getActualAmountPaid());
		paymentMap.put("orderDocName", order.getOrderDocName());
		paymentMap.put("orderDocId", order.getOrderDocId());
		paymentMap.put("orderDocTel", order.getOrderDocTel());
		paymentMap.put("orderDate", order.getOrderDate());
		paymentMap.put("createBy", order.getCreate_by());
		paymentMap.put(E_PORT, order.getEport());
		paymentMap.put(CIQ_ORG_CODE, order.getCiqOrgCode());
		paymentMap.put(CUSTOMS_CODE, order.getCustomsCode());
		paymentMap.put("thirdPartyId", order.getThirdPartyId());
		// 申报状态：1-待申报、2-申报中、3-申报成功、4-申报失败、10-申报中(待系统处理)
		paymentMap.put("pay_record_status", 10);
		int idcardCertifiedFlag = order.getIdcardCertifiedFlag();
		// 身份证实名认证标识：0-未实名、1-已实名、2-认证失败
		if (idcardCertifiedFlag == 0 || idcardCertifiedFlag == 2) {
			Map<String, Object> reIdCardMap = getIdCard(order.getOrderDocName(), order.getOrderDocId(),
					order.getMerchant_no());
			String status = reIdCardMap.get(BaseCode.STATUS.toString()) + "";
			if ("1".equals(status)) {
				// 添加订单交易流水号、作用于用户储备资金结算对应的流水号
				order.setTrade_no(tradeNo);
				startSubtasks(order);
				return paymentService.addEntity(paymentMap)
						&& paymentService.updateOrderPayNo(merchantId, order.getOrder_id(), tradeNo);
			} else {
				return updateCertifiedStatus(order);
			}
		} else {
			// 添加订单交易流水号、作用于用户储备资金结算对应的流水号
			order.setTrade_no(tradeNo);
			startSubtasks(order);
			return paymentService.addEntity(paymentMap)
					&& paymentService.updateOrderPayNo(merchantId, order.getOrder_id(), tradeNo);
		}
	}

	private void startSubtasks(Morder order) {
		// 创建一个生成钱包流水子任务
		String memberId = order.getOrderPayerId();
		// 暂时写死
		// String memberId = "Member_2017000025928";
		if (StringEmptyUtils.isNotEmpty(memberId)) {
			ExecutorService threadPool = Executors.newCachedThreadPool();
			WalletTransferTask walletTransferTask = new WalletTransferTask(memberId, order.getMerchant_no(),
					order.getTrade_no(), memberWalletService, order.getActualAmountPaid());
			threadPool.submit(walletTransferTask);
			threadPool.shutdown();
		}
	}

	/**
	 * 更新订单失败原因
	 * 
	 * @param order
	 *            订单信息实体
	 * @param msg
	 *            信息
	 * @return boolean
	 */
	private boolean updateOrder(Morder order, String msg) {
		if (order == null) {
			return false;
		}
		// 申报状态：1-未申报,2-申报中,3-申报成功、4-申报失败、10-申报中(待系统处理)
		order.setOrder_record_status(4);
		String note = order.getOrder_re_note();
		if (StringEmptyUtils.isEmpty(note)) {
			order.setOrder_re_note(DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss") + " " + msg);
		} else {
			order.setOrder_re_note(note + "#" + DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss") + " " + msg);
		}
		order.setUpdate_date(new Date());
		return orderDao.update(order);
	}

	/**
	 * 根据商户id、姓名、身份证号码、进行实名认证，先扫描本地实名数据库,查询是否已认证,本地没有认证时则直接像网关发起实名认证
	 * 
	 * @param idName
	 *            姓名
	 * @param idNumber
	 *            身份证号码
	 * @param merchantId
	 *            商户id
	 * @return Map
	 */
	public Map<String, Object> getIdCard(String idName, String idNumber, String merchantId) {
		if (StringEmptyUtils.isEmpty(idName) || StringEmptyUtils.isEmpty(idNumber)
				|| StringEmptyUtils.isEmpty(merchantId)) {
			return ReturnInfoUtils.errorInfo("保存实名信息失败,参数错误!");
		}
		Map<String, Object> params = new HashMap<>();
		params.put(MERCHANT_ID, merchantId);
		params.put("name", idName.trim());
		params.put("idNumber", idNumber.trim());
		List<IdCard> reIdCardList = orderDao.findByProperty(IdCard.class, params, 1, 1);
		if (reIdCardList == null) {
			return ReturnInfoUtils.errorInfo("查询身份证信息失败,服务器繁忙!");
		} else if (reIdCardList.isEmpty()) {
			return saveIdCardInfo(idName, idNumber, merchantId);
		} else {// 实名库已存在
			return updateIdcardInfo(reIdCardList.get(0));
		}
	}

	/**
	 * 保存身份证实名信息
	 * 
	 * @param idName
	 *            姓名
	 * @param idNumber
	 *            身份证号码
	 * @param merchantId
	 *            商户id
	 * @return Map
	 */
	private Map<String, Object> saveIdCardInfo(String idName, String idNumber, String merchantId) {
		if (StringEmptyUtils.isEmpty(idName) || StringEmptyUtils.isEmpty(idNumber)
				|| StringEmptyUtils.isEmpty(merchantId)) {
			return ReturnInfoUtils.errorInfo("身份证信息保存至实名库失败,参数不能为空!");
		}
		Map<String, Object> reCostMap = merchantIdCardCostService.getIdCardCostInfo(merchantId);
		if (!"1".equals(reCostMap.get(BaseCode.STATUS.toString()))) {
			return reCostMap;
		}
		MerchantIdCardCostContent merchantCost = (MerchantIdCardCostContent) reCostMap.get(BaseCode.DATAS.toString());
		String idCardVerifySwitch = merchantCost.getIdCardVerifySwitch();
		if ("on".equals(idCardVerifySwitch)) {// 当商户实名认证开启时
			Map<String, Object> reMap = sendIdCardCertification(idName, idNumber);
			String status = reMap.get(BaseCode.STATUS.toString()) + "";
			String msgId = reMap.get("messageID") + "";
			String msg = reMap.get("msg") + "";
			System.out.println("--实名认证--结果->" + reMap.toString());
			if ("1".equals(status)) {// 实名认证成功
				return addIdCardInfo(msgId, merchantId, merchantCost.getMerchantName(), idName, idNumber, "success",
						msg);
			} else if ("-1".equals(status)) {// 当向网关服务器发起身份证验证失败后
				Map<String, Object> reIdCardMap = addIdCardInfo(msgId, merchantId, merchantCost.getMerchantName(),
						idName, idNumber, FAILURE, msg);
				if (!"1".equals(reIdCardMap.get(BaseCode.STATUS.toString()))) {
					return reIdCardMap;
				}
				return reMap;
			} else {
				return addIdCardInfo(msgId, merchantId, merchantCost.getMerchantName(), idName, idNumber, "wait", msg);
			}
		} else {// 当商户实名认证关闭时
			return addIdCardInfo(null, merchantId, merchantCost.getMerchantName(), idName, idNumber, "wait", "");
		}
	}

	/**
	 * 更新实名认证后的订单实名认证状态与申报状态
	 * 
	 * @param order
	 */
	private boolean updateCertifiedStatus(Morder order) {
		if (order == null) {
			return false;
		}
		// 身份证实名认证标识：0-未实名、1-已实名、2-认证失败
		order.setIdcardCertifiedFlag(2);
		// 申报状态：1-未申报,2-申报中,3-申报成功、4-申报失败、10-申报中(待系统处理)
		order.setOrder_record_status(4);
		order.setUpdate_date(new Date());
		String oldNote = order.getOrder_re_note();
		if (StringEmptyUtils.isEmpty(oldNote)) {
			order.setOrder_re_note(DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss") + " 实名认证失败,请核对姓名与身份证号码！#");
		} else {
			order.setOrder_re_note(
					oldNote + "#" + DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss") + " 实名认证失败,请核对姓名与身份证号码！#");
		}
		return orderDao.update(order);
	}

	/**
	 * 更新已存在的身份证实名信息
	 * 
	 * @param idCard
	 *            身份证实名信息实体类
	 * @param idCardVerifySwitch
	 *            发起实名认证开关
	 * @return Map
	 */
	private Map<String, Object> updateIdcardInfo(IdCard idCard) {
		if (idCard == null) {
			return ReturnInfoUtils.errorInfo("更新身份证实名认证状态失败,参数错误!");
		}
		Map<String, Object> map = null;
		if ("success".equals(idCard.getStatus())) {
			return ReturnInfoUtils.successInfo();
		} else if ("wait".equals(idCard.getStatus())) {// 当实名数据库已存在,但并未向实名网关发起过验证时
			Map<String, Object> reCostMap = merchantIdCardCostService.getIdCardCostInfo(idCard.getMerchantId());
			if (!"1".equals(reCostMap.get(BaseCode.STATUS.toString()))) {
				return reCostMap;
			}
			MerchantIdCardCostContent merchantCost = (MerchantIdCardCostContent) reCostMap
					.get(BaseCode.DATAS.toString());
			String idCardVerifySwitch = merchantCost.getIdCardVerifySwitch();
			if ("on".equals(idCardVerifySwitch)) {// 当商户实名认证开启时
				Map<String, Object> reMap = sendIdCardCertification(idCard.getName(), idCard.getIdNumber());
				String status = reMap.get(BaseCode.STATUS.toString()) + "";
				String msgId = reMap.get("messageID") + "";
				String msg = reMap.get("msg") + "";
				if ("1".equals(status)) {// 实名认证成功
					return updateIdcardStatus(idCard, "success", msg, msgId);
				} else if ("-1".equals(status)) {// 当向网关服务器发起身份证验证失败后
					Map<String, Object> reIdcardMap = updateIdcardStatus(idCard, FAILURE, msg, msgId);
					if (!"1".equals(reIdcardMap.get(BaseCode.STATUS.toString()))) {
						return reIdcardMap;
					}
					//
					return reMap;
				}
				// 当认证服务器返回异常时,则返回成功结果,继续推送订单
				return ReturnInfoUtils.successInfo();
			} else {
				return ReturnInfoUtils.successInfo();
			}
		} else {
			map = new HashMap<>();
			map.put(BaseCode.STATUS.toString(), "-1");
			map.put(BaseCode.MSG.toString(), "实名认证不通过！");
			return map;
		}
	}

	/**
	 * 姓名与身份证号码(两要素)验证、发送身份证验证请求
	 * 
	 * @param idName
	 *            姓名
	 * @param idCard
	 *            身份证号码
	 * @return Map
	 */
	public Map<String, Object> sendIdCardCertification(String idName, String idCard) {
		if (StringEmptyUtils.isEmpty(idName) || StringEmptyUtils.isEmpty(idCard)) {
			return ReturnInfoUtils.errorInfo("发送身份证校验,请求参数不能为空!");
		}
		// 使用银盟商城app请求获取tok
		Map<String, Object> reTokMap = accessTokenService.getRedisToks(YmMallConfig.APPKEY, YmMallConfig.APPSECRET);
		if (!"1".equals(reTokMap.get(BaseCode.STATUS.toString()))) {
			return reTokMap;
		}
		String accessToken = reTokMap.get(BaseCode.DATAS.toString()) + "";
		Map<String, Object> params = new HashMap<>();
		params.put("version", "1.0");
		params.put("merchantNo", YmMallConfig.ID_CARD_CERTIFICATION_MERCHANT_NO);
		params.put("businessCode", "YS02");
		JSONObject bizContent = new JSONObject();
		bizContent.put("user_ID", idCard);
		bizContent.put("user_name", idName);
		params.put("bizContent", bizContent);
		params.put("timestamp", System.currentTimeMillis());
		params = new MapSortUtils().sortMap(params);
		String str2 = YmMallConfig.APPKEY + accessToken + params;
		String clientSign = null;
		try {
			clientSign = MD5.getMD5(str2.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return ReturnInfoUtils.errorInfo("加密签名错误!");
		}
		params.put("clientSign", clientSign);
		// String result =
		// YmHttpUtil.HttpPost("http://localhost:8080/silver-web/real/auth",
		// params);
		String result = YmHttpUtil.HttpPost("https://ym.191ec.com/silver-web/real/auth", params);
		if (StringEmptyUtils.isEmpty(result)) {
			return ReturnInfoUtils.errorInfo("验证身份证失败,网络异常!");
		} else {
			return JSONObject.fromObject(result);
		}
	}

	/**
	 * 保存身份证实名认证信息
	 * 
	 * @param msgId
	 *            实名认证流水Id
	 * @param merchantId
	 *            商户id
	 * @param merchantName
	 *            商户名称
	 * @param idName
	 *            姓名
	 * @param idNumber
	 *            身份证号码
	 * @param status
	 *            认证状态:success-成功;failure-失败;wait-待验证
	 * @param msg
	 *            认证消息
	 * @return Map key-datas 身份证实名实体
	 */
	public Map<String, Object> addIdCardInfo(String msgId, String merchantId, String merchantName, String idName,
			String idNumber, String status, String msg) {

		IdCard idCard = new IdCard();
		if (StringEmptyUtils.isNotEmpty(msgId)) {
			idCard.setCertifiedNo(msgId);
		}
		idCard.setMerchantId(merchantId);
		idCard.setMerchantName(merchantName);
		idCard.setName(idName);
		idCard.setIdNumber(idNumber);
		// 类型：1-未验证,2-手工验证,3-海关认证,4-第三方认证,5-错误
		idCard.setType(4);
		// 认证状态:success-成功;failure-失败;wait-待验证
		idCard.setStatus(status);
		if (StringEmptyUtils.isNotEmpty(msg)) {
			idCard.setNote(msg);
		}
		if ("success".equals(status)) {// 认证时间
			idCard.setCertifiedDate(new Date());
		}
		idCard.setCreateDate(new Date());
		if (!orderDao.add(idCard)) {
			return ReturnInfoUtils.errorInfo("保存失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successDataInfo(idCard);
	}

	/**
	 * 更新实名数据库中已有的身份证信息状态
	 * 
	 * @param idCard
	 *            身份证实名信息类
	 * @param status
	 *            认证状态:success-成功;failure-失败;wait-待验证
	 * @param msg
	 *            网关返回消息
	 * @param msgId
	 *            验证流水号
	 * @return Map
	 */
	private Map<String, Object> updateIdcardStatus(IdCard idCard, String status, String msg, String msgId) {
		if (idCard == null) {
			return ReturnInfoUtils.errorInfo("实名信息错误！");
		}
		if (StringEmptyUtils.isNotEmpty(msgId)) {
			idCard.setCertifiedNo(msgId);
		}
		idCard.setStatus(status);
		if ("success".equalsIgnoreCase(status)) {
			idCard.setCertifiedDate(new Date());
		}
		String oldNote = idCard.getNote();
		if (StringEmptyUtils.isNotEmpty(oldNote) && StringEmptyUtils.isNotEmpty(msg)) {
			idCard.setNote(oldNote + "#" + DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss") + " " + msg);
		} else if (StringEmptyUtils.isNotEmpty(msg)) {
			idCard.setNote(msg);
		}
		idCard.setUpdateDate(new Date());
		if (!orderDao.update(idCard)) {
			return ReturnInfoUtils.errorInfo("保存失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}
}

package org.silver.shop.quartz;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.common.BaseCode;
import org.silver.shop.api.system.AccessTokenService;
import org.silver.shop.api.system.manual.MpayService;
import org.silver.shop.api.system.tenant.MerchantIdCardCostService;
import org.silver.shop.config.YmMallConfig;
import org.silver.shop.dao.system.commerce.OrderDao;
import org.silver.shop.model.system.manual.Appkey;
import org.silver.shop.model.system.manual.ManualOrderResendContent;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.manual.MorderSub;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.util.MerchantUtils;
import org.silver.util.DateUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 定时任务,扫描商户自助申报的订单,进行订单申报
 */
public class PushOrderRecordQtz {
	/**
	 * 口岸
	 */
	private static final String E_PORT = "eport";

	/**
	 * 检验检疫机构代码
	 */
	private static final String CIQ_ORG_CODE = "ciqOrgCode";

	/**
	 * 主管海关代码
	 */
	private static final String CUSTOMS_CODE = "customsCode";
	/**
	 * 下划线命名:商户Id
	 */
	private static final String MERCHANT_NO = "merchant_no";
	/**
	 * 驼峰命名：手工订单重发唯一Id
	 */
	private static final String ORDER_RESEND_ID = "orderResendId";
	/**
	 * 失败标识
	 */
	private static final String FAILURE = "failure";
	/**
	 * 计数器
	 */
	private static AtomicInteger counter = new AtomicInteger(0);

	private static Logger logger = LogManager.getLogger(Object.class);

	@Autowired
	private OrderDao orderDao;
	@Autowired
	private MerchantUtils merchantUtils;
	@Autowired
	private MpayService mpayService;
	@Autowired
	private AccessTokenService accessTokenService;
	@Autowired
	private CreatePaymentQtz createPaymentQtz;

	public void pushOrderRecordJob() {
		if (counter.get() % 10 == 0) {
			System.out.println("---扫描自助申报订单---");
		}
		Map<String, Object> params = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		// 设置扫描开始时间点
		calendar.add(Calendar.MONTH, -3);
		calendar.set(Calendar.HOUR_OF_DAY, 00);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		// 设置扫描结束时间点
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
		// 支付流水标识
		params.put("tradeNoFlag", " IS NOT NULL ");
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
						Map<String, Object> reMap = startSendOrderRecord(order, null);
						if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
							logger.error("--定时扫描待发起备案的订单信息错误--" + reMap.toString());
						}
						// 线程休眠0.2秒,防止HTTP请求过快出错
						Thread.sleep(200);
					}
				}
				page++;
			}
			counter.getAndIncrement();
		} catch (Exception e) {
			logger.error("--定时扫描待发起备案的订单信息错误--", e);
		}
	}

	/**
	 * 准备开始发送手工订单申报
	 * 
	 * @param order
	 *            手工订单信息
	 * @param subParams
	 *            副参数,可传可不传，已有参数key-ORDER_RESEND_ID(重发订单唯一Id)
	 * @return Map
	 */
	private Map<String, Object> startSendOrderRecord(Morder order, Map<String, Object> subParams) {
		if (order == null) {
			return ReturnInfoUtils.errorInfo("--准备开始发送手工订单申报时-订单信息不能为null--");
		}
		String merchantId = order.getMerchant_no();
		Map<String, Object> params = new HashMap<>();
		params.put(MERCHANT_NO, merchantId);
		params.put("order_id", order.getOrder_id());
		List<MorderSub> reOrderGoodsList = orderDao.findByProperty(MorderSub.class, params, 0, 0);
		if (reOrderGoodsList == null || reOrderGoodsList.isEmpty()) {
			return ReturnInfoUtils.errorInfo(order.getOrder_id() + "--推送订单失败,订单商品信息不能为空!");
		}
		int idcardCertifiedFlag = order.getIdcardCertifiedFlag();
		// 身份证实名认证标识：0-未实名、1-已实名、2-认证失败
		if (idcardCertifiedFlag == 1) {
			return sendOrderRecord(order, reOrderGoodsList, subParams);
		} else {
			// 当订单尚未实名认证时,发起实名认证
			Map<String, Object> reIdCardMap = createPaymentQtz.getIdCard(order.getOrderDocName(), order.getOrderDocId(),
					merchantId);
			String status = reIdCardMap.get(BaseCode.STATUS.toString()) + "";
			if ("1".equals(status)) {
				if (!updateCertifiedSuccessStatus(order)) {
					return ReturnInfoUtils.errorInfo(order.getOrder_id() + "--推送订单失败,实名认证状态更新失败!");
				}
			} else {
				return updateCertifiedFailureStatus(order);
			}
			return ReturnInfoUtils.errorInfo(order.getOrder_id() + "--推送订单失败,订单尚未实名认证通过!");
		}
	}

	/**
	 * 更新订单认证失败状态
	 * @param order 订单信息实体类
	 * @return Map
	 */
	private Map<String, Object> updateCertifiedFailureStatus(Morder order) {
		if(order == null){
			return ReturnInfoUtils.errorInfo("更新订单认证失败状态错误,请求参数不能为null");
		}
		// 身份证实名认证标识：0-未实名、1-已实名、2-认证失败
		order.setIdcardCertifiedFlag(2);
		// 申报状态：1-未申报,2-申报中,3-申报成功、4-申报失败、10-申报中(待系统处理)
		order.setOrder_record_status(4);
		String oldNote = order.getOrder_re_note();
		if (StringEmptyUtils.isEmpty(oldNote)) {
			order.setOrder_re_note(DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss") + " 实名认证失败,请核对姓名与身份证号码!#");
		} else {
			order.setOrder_re_note(
					oldNote + "#" + DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss") + " 实名认证失败,请核对姓名与身份证号码!#");
		}
		if (orderDao.update(order)) {
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo(order.getOrder_id() + "--推送订单失败,订单实名认证失败,更新状态错误!");
	}

	/**
	 * 更新订单验证成功状态
	 * 
	 * @param order
	 *            订单实体信息
	 * @return boolean
	 */
	private boolean updateCertifiedSuccessStatus(Morder order) {
		if (order == null) {
			return false;
		}
		// 身份证实名认证标识：0-未实名、1-已实名、2-认证失败
		order.setIdcardCertifiedFlag(1);
		return orderDao.update(order);
	}

	/**
	 * 开始推送订单
	 * 
	 * @param order
	 *            手工订单实体信息类
	 * @param reOrderGoodsList
	 *            订单关联商品集合
	 * @param subParams
	 *            副参数,可传可不传，已有参数key-ORDER_RESEND_ID(重发订单唯一Id)
	 * @return Map
	 */
	private Map<String, Object> sendOrderRecord(Morder order, List<MorderSub> reOrderGoodsList,
			Map<String, Object> subParams) {
		String appkey = "";
		String appSecret = "";
		String merchantId = order.getMerchant_no();
		// 获取商户信息
		Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(merchantId);
		if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
			logger.error(reMerchantMap.get(BaseCode.MSG.toString()));
			return reMerchantMap;
		}
		Merchant merchant = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
		int thirdPartyFlag = merchant.getThirdPartyFlag();
		// 当商户标识为第三方平台商户时,则使用第三方appkey
		if (thirdPartyFlag == 2) {
			Map<String, Object> reAppkeyMap = merchantUtils.getMerchantAppkey(merchantId);
			if (!"1".equals(reAppkeyMap.get(BaseCode.STATUS.toString()))) {
				logger.error(reAppkeyMap.get(BaseCode.MSG.toString()));
				return reAppkeyMap;
			} else {
				Appkey appkeyInfo = (Appkey) reAppkeyMap.get(BaseCode.DATAS.toString());
				appkey = appkeyInfo.getApp_key();
				appSecret = appkeyInfo.getApp_secret();
			}
		} else {
			// 当不是第三方时则使用银盟商城appkey
			appkey = YmMallConfig.APPKEY;
			appSecret = YmMallConfig.APPSECRET;
		}
		// 请求获取tok
		Map<String, Object> reTokMap = accessTokenService.getRedisToks(appkey, appSecret);
		if (!"1".equals(reTokMap.get(BaseCode.STATUS.toString()))) {
			logger.error(reTokMap.get(BaseCode.MSG.toString()));
			return reTokMap;
		}
		String tok = reTokMap.get(BaseCode.DATAS.toString()) + "";
		Map<String, Object> customsMap = new HashMap<>();
		customsMap.put(E_PORT, order.getEport());
		customsMap.put(CIQ_ORG_CODE, order.getCiqOrgCode());
		customsMap.put(CUSTOMS_CODE, order.getCustomsCode());
		customsMap.put("appkey", appkey);
		Map<String, Object> reOrderMap = mpayService.sendOrder(customsMap, reOrderGoodsList, tok, order);
		if (!"1".equals(reOrderMap.get(BaseCode.STATUS.toString()) + "")) {
			// 当服务器接收失败时,将订单网络接收状态更新为失败
			Map<String, Object> reMap = mpayService.updateOrderErrorStatus(order.getOrder_id());
			if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
				return reMap;
			}
			// 添加至重发记录表中
			Map<String, Object> reOrderResendMap = addOrderResendInfo(order.getOrder_id(), order.getMerchant_no(),
					order.getCreate_by(), reOrderMap.get(BaseCode.MSG.toString()) + "", subParams);
			logger.error(order.getOrder_id() + "--订单推送失败后,增加至重发记录-->" + reOrderResendMap.get(BaseCode.MSG.toString()));
			// 当推送订单失败后,返回信息不能为成功,因此返回错误信息
			return ReturnInfoUtils.errorInfo(reOrderMap.get(BaseCode.MSG.toString()) + "");
		} else {
			String reOrderMessageID = reOrderMap.get("messageID") + "";
			return mpayService.updateOrderInfo(order.getOrder_id(), reOrderMessageID, customsMap);
		}
	}

	/**
	 * 添加手工订单推送失败,重发记录
	 * 
	 * @param orderId
	 *            订单Id
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param msg
	 *            信息
	 * @param subParams
	 * @return
	 */
	private Map<String, Object> addOrderResendInfo(String orderId, String merchantId, String merchantName, String msg,
			Map<String, Object> subParams) {
		if (StringEmptyUtils.isNotEmpty(orderId) && StringEmptyUtils.isNotEmpty(merchantId)
				&& StringEmptyUtils.isNotEmpty(merchantName)) {
			Map<String, Object> params = new HashMap<>();
			params.put("resendStatus", FAILURE);
			if (subParams != null && !subParams.isEmpty()) {
				params.put(ORDER_RESEND_ID, subParams.get(ORDER_RESEND_ID));
			}
			List<ManualOrderResendContent> orderList = orderDao.findByProperty(ManualOrderResendContent.class, params,
					0, 0);
			if (orderList != null && !orderList.isEmpty()) {
				return ReturnInfoUtils.errorInfo(subParams + "<--重发订单唯一标识已存在,无需重复添加");
			} else {
				ManualOrderResendContent orderResend = new ManualOrderResendContent();
				long id = orderDao.findLastId(ManualOrderResendContent.class);
				if (id < 0) {
					return ReturnInfoUtils.errorInfo("生成重推订单Id失败!");
				}
				orderResend.setOrderResendId(SerialNoUtils.getSerialNo("ORDER-RE-", id));
				orderResend.setMerchantId(merchantId);
				orderResend.setMerchantName(merchantName);
				orderResend.setOrderId(orderId);
				// 重发状态：success-成功，failure-失败
				orderResend.setResendStatus(FAILURE);
				orderResend.setResendCount(0);
				orderResend.setCreateBy("system");
				orderResend.setCreateDate(new Date());
				orderResend.setNote(DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss") + " " + msg);
				if (!orderDao.add(orderResend)) {
					return ReturnInfoUtils.errorInfo(orderId + "--保存订单重发记录失败--");
				}
				return ReturnInfoUtils.successInfo();
			}
		} else {
			return ReturnInfoUtils.errorInfo("--保存订单重发记录失败--请求参数不能为空--");
		}
	}

	/**
	 * 重发订单申报失败的手工订单
	 */
	public void resendPushOrderRecordJob() {
		int page = 1;
		int size = 300;
		Map<String, Object> params = new HashMap<>();
		params.put("resendStatus", FAILURE);
		List<ManualOrderResendContent> orderList = orderDao.getResendOrderInfo(ManualOrderResendContent.class, params,
				page, size);
		while (orderList != null && !orderList.isEmpty()) {
			if (page > 1) {
				params.put("resendStatus", FAILURE);
				orderList = orderDao.getResendOrderInfo(ManualOrderResendContent.class, params, page, size);
			}
			if (orderList != null && !orderList.isEmpty()) {
				Map<String, Object> params2 = new HashMap<>();
				for (ManualOrderResendContent orderResend : orderList) {
					params2.clear();
					params2.put("order_id", orderResend.getOrderId());
					List<Morder> reOrderGoodsList = orderDao.findByProperty(Morder.class, params2, 0, 0);
					if (reOrderGoodsList != null && !reOrderGoodsList.isEmpty()) {
						Map<String, Object> subParams = new HashMap<>();
						subParams.put(ORDER_RESEND_ID, orderResend.getOrderResendId());
						Map<String, Object> reMap = startSendOrderRecord(reOrderGoodsList.get(0), subParams);
						if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
							updateResendOrderCount(orderResend.getOrderResendId(),
									reMap.get(BaseCode.MSG.toString()) + "");
						} else {
							updateResendOrderSuccessStatus(orderResend.getOrderResendId());
						}
					}
				}
			}
			page++;
		}
	}

	/**
	 * 当重发成功后,更新状态
	 * 
	 * @param orderResendId
	 *            重发订单唯一Id
	 */
	private void updateResendOrderSuccessStatus(String orderResendId) {
		Map<String, Object> params = new HashMap<>();
		params.put(ORDER_RESEND_ID, orderResendId);
		List<ManualOrderResendContent> orderList = orderDao.findByProperty(ManualOrderResendContent.class, params, 0,
				0);
		if (orderList != null && !orderList.isEmpty()) {
			ManualOrderResendContent order = orderList.get(0);
			int count = order.getResendCount();
			count++;
			// 更新重发次数
			order.setResendCount(count);
			String note = order.getNote();
			if (StringEmptyUtils.isNotEmpty(note)) {
				order.setNote(note + "#" + "订单第" + count + "次重新申报成功!");
			} else {
				order.setNote("订单第" + count + "次重新申报成功!");
			}
			order.setResendStatus("success");
			order.setUpdateBy("system");
			order.setUpdateDate(new Date());
			if (!orderDao.update(order)) {
				logger.error(orderResendId + "--更新订单重推次数失败--");
			}
		}
	}

	/**
	 * 更新订单重推次数
	 * 
	 * @param orderResendId
	 *            订单重发唯一Id
	 * @param msg
	 *            错误信息
	 */
	private void updateResendOrderCount(String orderResendId, String msg) {
		Map<String, Object> params = new HashMap<>();
		params.put(ORDER_RESEND_ID, orderResendId);
		List<ManualOrderResendContent> orderList = orderDao.findByProperty(ManualOrderResendContent.class, params, 0,
				0);
		if (orderList != null && !orderList.isEmpty()) {
			ManualOrderResendContent order = orderList.get(0);
			int count = order.getResendCount();
			count++;
			// 更新重发次数
			order.setResendCount(count);
			String note = order.getNote();
			// 当手工订单推送10次全部失败后,将手工订单内的申报状态修改为申报失败
			if (count == 10) {
				updateManualOrderRecordStatus(order.getOrderId(), order.getMerchantId());
			}
			if (StringEmptyUtils.isNotEmpty(note)) {
				order.setNote(note + "#" + DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss") + " " + msg);
			} else {
				order.setNote(DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss") + " " + msg);
			}
			order.setUpdateBy("system");
			order.setUpdateDate(new Date());
			if (!orderDao.update(order)) {
				logger.error(orderResendId + "--更新订单重推次数失败--");
			}
		}
	}

	/**
	 * 将订单申报状态更新为申报失败
	 * 
	 * @param orderId
	 *            订单Id
	 * @param merchantId
	 *            商户Id
	 */
	private void updateManualOrderRecordStatus(String orderId, String merchantId) {
		Map<String, Object> params = new HashMap<>();
		params.put("order_id", orderId);
		params.put(MERCHANT_NO, merchantId);
		List<Morder> orderList = orderDao.findByProperty(Morder.class, params, 0, 0);
		if (orderList != null && !orderList.isEmpty()) {
			Morder order = orderList.get(0);
			// 申报状态：1-未申报,2-申报中,3-申报成功、4-申报失败、10-申报中(待系统处理)
			order.setOrder_record_status(4);
			if (!orderDao.update(order)) {
				logger.error(orderId + "--更新订单申报状态失败--");
			}
		}
	}

}

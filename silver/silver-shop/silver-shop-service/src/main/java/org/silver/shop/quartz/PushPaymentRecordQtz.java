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
import org.silver.shop.api.system.cross.PaymentService;
import org.silver.shop.api.system.cross.YsPayReceiveService;
import org.silver.shop.config.YmMallConfig;
import org.silver.shop.dao.system.commerce.OrderDao;
import org.silver.shop.dao.system.cross.PaymentDao;
import org.silver.shop.model.system.manual.Appkey;
import org.silver.shop.model.system.manual.ManualOrderResendContent;
import org.silver.shop.model.system.manual.ManualPaymentResendContent;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.manual.Mpay;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantRecordInfo;
import org.silver.shop.util.IdUtils;
import org.silver.shop.util.MerchantUtils;
import org.silver.util.DateUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 定时任务,扫描手工支付单中需要自助申报的手工支付单
 */
public class PushPaymentRecordQtz {
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
	 * 下划线命名:交易流水号
	 */
	private static final String TRADE_NO = "trade_no";
	/**
	 * 失败标识
	 */
	private static final String FAILURE = "failure";
	/**
	 * 支付单重发id唯一标识
	 */
	private static final String PAYMENT_RESEND_ID = "paymentResendId";

	private static Logger logger = LogManager.getLogger(Object.class);

	/**
	 * 计数器
	 */
	private static AtomicInteger counter = new AtomicInteger(0);

	@Autowired
	private PaymentDao paymentDao;
	@Autowired
	private MerchantUtils merchantUtils;
	@Autowired
	private PaymentService paymentService;
	@Autowired
	private AccessTokenService accessTokenService;
	@Autowired
	private YsPayReceiveService ysPayReceiveService;
	@Autowired
	private IdUtils idUtils;

	/**
	 * 扫描支付单备案
	 */
	public void pushPaymentRecordQtzJob() {
		if (counter.get() % 10 == 0) {
			System.out.println("--扫描需要自助申报的支付单--");
		}
		Map<String, Object> params = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, -7);
		calendar.set(Calendar.HOUR_OF_DAY, 00);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		params.put("startTime", calendar.getTime());
		calendar.setTime(new Date());
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		params.put("endTime", calendar.getTime());
		// 申报状态：1-待申报、2-申报中、3-申报成功、4-申报失败、10-申报中(待系统处理)
		params.put("pay_record_status", 10);
		// 网关接收状态： 0-未发起,1-接收成功,2-接收失败
		params.put("networkStatus", 0);
		try {
			int page = 1;
			int size = 300;
			List<Mpay> reMpayList = paymentDao.findByPropertyLike(Mpay.class, params, null, page, size);
			while (reMpayList != null && !reMpayList.isEmpty()) {
				if (page != 1) {
					reMpayList = paymentDao.findByPropertyLike(Mpay.class, params, null, page, size);
				}
				if (reMpayList != null && !reMpayList.isEmpty()) {
					for (Mpay pay : reMpayList) {
						sendPaymentRecord(pay.getMerchant_no(), pay.getTrade_no(), null);
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
	 * 发送支付单备案
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param tradeNo
	 *            交易流水号
	 * @param subParams
	 *            副参数,可传可不传，已有参数key-PAYMENT_RESEND_ID(重发支付单唯一Id)
	 * @return
	 */
	private Map<String, Object> sendPaymentRecord(String merchantId, String tradeNo, Map<String, Object> subParams) {
		String appkey = "";
		String appSecret = "";
		// 获取商户信息
		Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(merchantId);
		if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
			logger.error(reMerchantMap.get(BaseCode.MSG.toString()));
		}
		Merchant merchant = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
		int thirdPartyFlag = merchant.getThirdPartyFlag();
		// 当商户标识为第三方平台商户时,则使用第三方appkey
		if (thirdPartyFlag == 2) {
			Map<String, Object> reAppkeyMap = merchantUtils.getMerchantAppkey(merchantId);
			if (!"1".equals(reAppkeyMap.get(BaseCode.STATUS.toString()))) {
				logger.error(reAppkeyMap.get(BaseCode.MSG.toString()));
			} else {
				Appkey appkeyInfo = (Appkey) reAppkeyMap.get(BaseCode.DATAS.toString());
				appkey = appkeyInfo.getApp_key();
				appSecret = appkeyInfo.getApp_secret();
			}
		} else {
			// 当不是第三方电商平台时则使用银盟商城appkey
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

		Map<String, Object> params = new HashMap<>();
		params.put(MERCHANT_NO, merchantId);
		params.put(TRADE_NO, tradeNo);
		List<Mpay> payList = paymentDao.findByProperty(Mpay.class, params, 1, 1);
		if (payList == null || payList.isEmpty()) {
			logger.error(tradeNo + "--查询支付单信息失败--");
			return ReturnInfoUtils.errorInfo("--查询支付单信息失败--");
		} else {
			Map<String, Object> recordMap = new HashMap<>();
			Mpay payInfo = payList.get(0);
			Map<String, Object> paymentInfoMap = new HashMap<>();
			paymentInfoMap.put("EntPayNo", tradeNo);
			paymentInfoMap.put("PayStatus", payInfo.getPay_status());
			paymentInfoMap.put("PayAmount", payInfo.getPay_amount());
			paymentInfoMap.put("PayCurrCode", payInfo.getPay_currCode());
			paymentInfoMap.put("PayTime", payInfo.getPay_time());
			paymentInfoMap.put("PayerName", payInfo.getPayer_name());
			paymentInfoMap.put("PayerDocumentType", payInfo.getPayer_document_type());
			paymentInfoMap.put("PayerDocumentNumber", payInfo.getPayer_document_number());
			paymentInfoMap.put("PayerPhoneNumber", payInfo.getPayer_phone_number());
			paymentInfoMap.put("EntOrderNo", payInfo.getMorder_id());
			paymentInfoMap.put("Notes", payInfo.getRemarks());
			Map<String, Object> reMerchantMap2 = merchantUtils.getMerchantRecordInfo(merchantId,
					Integer.parseInt(payInfo.getEport()));
			if (!"1".equals(reMerchantMap2.get(BaseCode.STATUS.toString()))) {
				//
			}
			MerchantRecordInfo merchantRecord = (MerchantRecordInfo) reMerchantMap2.get(BaseCode.DATAS.toString());
			recordMap.put(E_PORT, payInfo.getEport());
			recordMap.put(CIQ_ORG_CODE, payInfo.getCiqOrgCode());
			recordMap.put(CUSTOMS_CODE, payInfo.getCustomsCode());
			recordMap.put("appkey", appkey);
			recordMap.put("ebpEntNo", merchantRecord.getEbpEntNo());
			recordMap.put("ebpEntName", merchantRecord.getEbpEntName());
			recordMap.put("ebEntNo", "aaa");
			recordMap.put("ebEntName", "bb");
			Map<String, Object> rePaymentMap = ysPayReceiveService.sendPayment(merchantId, paymentInfoMap, tok,
					recordMap, YmMallConfig.MANUAL_PAYMENT_NOTIFY_URL);
			if (!"1".equals(rePaymentMap.get(BaseCode.STATUS.toString()) + "")) {
				Map<String, Object> reMap = updatePaymentErrorStatus(payInfo);
				if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
					return reMap;
				}
				// 当网关接收失败时，添加至重发记录表中
				addPaymentResendInfo(tradeNo, payInfo.getMerchant_no(), payInfo.getCreate_by(),
						rePaymentMap.get(BaseCode.MSG.toString()) + "", subParams);
				logger.error(tradeNo + "<--接收失败--" + rePaymentMap.get(BaseCode.MSG.toString()));
				return rePaymentMap;
			} else {
				String rePayMessageID = rePaymentMap.get("messageID") + "";
				return paymentService.updatePaymentInfo(tradeNo, rePayMessageID, null);
			}
		}
	}

	/**
	 * 更新支付单网络失败状态
	 * 
	 * @param payInfo
	 *            手工改支付单实体类
	 * @return Map
	 */
	private Map<String, Object> updatePaymentErrorStatus(Mpay payInfo) {
		if (payInfo == null) {
			return ReturnInfoUtils.errorInfo("更新支付单失败状态错误,参数不能为null");
		}
		// 支付单推送至网关接收状态： 0-未发起,1-接收成功,2-接收失败
		payInfo.setNetworkStatus(2);
		if (!paymentDao.update(payInfo)) {
			return ReturnInfoUtils.errorInfo("更新支付单失败状态错误");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 添加支付单重发记录
	 * 
	 * @param tradeNo
	 *            交易流水号
	 * @param merchantId
	 *            商户id
	 * @param merchantName
	 *            商户名称
	 * @param msg
	 *            失败信息
	 * @param subParams
	 *            副参数,可传可不传，已有参数key-PAYMENT_RESEND_ID(重发支付单唯一Id)
	 * @return
	 */
	private Map<String, Object> addPaymentResendInfo(String tradeNo, String merchantId, String merchantName, String msg,
			Map<String, Object> subParams) {
		if (StringEmptyUtils.isNotEmpty(tradeNo) && StringEmptyUtils.isNotEmpty(merchantId)
				&& StringEmptyUtils.isNotEmpty(merchantName)) {
			Map<String, Object> params = new HashMap<>();
			if (subParams != null && !subParams.isEmpty()) {
				params.put(PAYMENT_RESEND_ID, subParams.get("PAYMENT_RESEND_ID"));
			}
			List<ManualPaymentResendContent> paymentList = paymentDao
					.getResendPaymentInfo(ManualPaymentResendContent.class, params, 0, 0);
			if (paymentList != null && !paymentList.isEmpty()) {
				return ReturnInfoUtils.errorInfo("重发记录已存在,无需重复添加");
			} else {
				ManualPaymentResendContent paymentRe = new ManualPaymentResendContent();
				Map<String, Object> reIdMap = idUtils.createId(ManualPaymentResendContent.class, "PAYMENT-RE-");
				if (!"1".equals(reIdMap.get(BaseCode.STATUS.toString()))) {
					return reIdMap;
				}
				paymentRe.setNote( DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss") + " " + msg);
				paymentRe.setPaymentResendId(reIdMap.get(BaseCode.DATAS.toString()) + "");
				paymentRe.setMerchantId(merchantId);
				paymentRe.setMerchantName(merchantName);
				paymentRe.setTradeNo(tradeNo);
				// 重发状态：success-成功，failure-失败
				paymentRe.setResendStatus("failure");
				paymentRe.setResendCount(0);
				paymentRe.setCreateBy("system");
				paymentRe.setCreateDate(new Date());
				if (!paymentDao.add(paymentRe)) {
					return ReturnInfoUtils.errorInfo("流水号[" + tradeNo + "]保存重发信息失败!");
				}
				return ReturnInfoUtils.successInfo();
			}
		} else {
			return ReturnInfoUtils.errorInfo("保存重发参数不能为空!");
		}
	}

	/**
	 * 扫描支付单推送网关失败的重发记录
	 */
	public void resendPaymentRecordJob() {
		int page = 1;
		int size = 300;
		Map<String, Object> params = new HashMap<>();
		Map<String, Object> subParams = null;
		params.put("resendStatus", FAILURE);
		List<ManualPaymentResendContent> paymentList = paymentDao.getResendPaymentInfo(ManualPaymentResendContent.class,
				params, page, size);
		while (paymentList != null && !paymentList.isEmpty()) {
			if (page > 1) {
				params.put("resendStatus", FAILURE);
				paymentList = paymentDao.getResendPaymentInfo(ManualPaymentResendContent.class, params, page, size);
			}
			if (paymentList != null && !paymentList.isEmpty()) {
				for (ManualPaymentResendContent paymentResend : paymentList) {
					subParams = new HashMap<>();
					subParams.put(PAYMENT_RESEND_ID, paymentResend.getPaymentResendId());
					Map<String, Object> reMap = sendPaymentRecord(paymentResend.getMerchantId(),
							paymentResend.getTradeNo(), subParams);
					if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
						updateResendPaymentCount(paymentResend.getPaymentResendId(),
								reMap.get(BaseCode.MSG.toString()) + "");
					} else {
						updateResendPaymentSuccessStatus(paymentResend.getPaymentResendId());
					}

				}
			}
			page++;
		}
	}

	/**
	 * 更新重发支付单成功状态
	 * @param paymentResendId 支付单重发唯一id
	 */
	private void updateResendPaymentSuccessStatus(String paymentResendId) {
		Map<String, Object> params = new HashMap<>();
		params.put(PAYMENT_RESEND_ID, paymentResendId);
		List<ManualPaymentResendContent> paymentList = paymentDao.findByProperty(ManualPaymentResendContent.class,
				params, 0, 0);
		if (paymentList != null && paymentList.isEmpty()) {
			ManualPaymentResendContent paymentRe = paymentList.get(0);
			int count = paymentRe.getResendCount();
			count++;
			// 更新重发次数
			paymentRe.setResendCount(count);
			String note = paymentRe.getNote();
			if (StringEmptyUtils.isNotEmpty(note)) {
				paymentRe.setNote(note + "#" + "订单第" + count + "次重新申报成功!");
			} else {
				paymentRe.setNote("订单第" + count + "次重新申报成功!");
			}
			paymentRe.setResendStatus("success");
			paymentRe.setUpdateBy("system");
			paymentRe.setUpdateDate(new Date());
			if (!paymentDao.update(paymentRe)) {
				logger.error(paymentResendId + "--成功后更新支付单重发记录失败--");
			}
		}
	}

	/**
	 * 更新支付单重发次数
	 * @param paymentResendId 支付单重发唯一id
	 * @param msg 信息
	 */
	private void updateResendPaymentCount(String paymentResendId, String msg) {
		Map<String, Object> params = new HashMap<>();
		params.put(PAYMENT_RESEND_ID, paymentResendId);
		List<ManualPaymentResendContent> paymentList = paymentDao.findByProperty(ManualPaymentResendContent.class,
				params, 0, 0);
		if (paymentList != null && paymentList.isEmpty()) {
			ManualPaymentResendContent paymentRe = paymentList.get(0);
			int count = paymentRe.getResendCount();
			count++;
			// 更新重发次数
			paymentRe.setResendCount(count);
			String note = paymentRe.getNote();
			if (StringEmptyUtils.isNotEmpty(note)) {
				paymentRe.setNote(note + "#" + DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss") + " " + msg);
			} else {
				paymentRe.setNote(DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss") + " " + msg);
			}
			paymentRe.setUpdateDate(new Date());
			if (!paymentDao.update(paymentRe)) {
				logger.error(paymentResendId + "--更新支付单重推次数失败--");
			}
		}
	}
}

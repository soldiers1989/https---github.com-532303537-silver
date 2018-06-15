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
import org.silver.shop.model.system.manual.Appkey;
import org.silver.shop.model.system.manual.Mpay;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantRecordInfo;
import org.silver.shop.util.MerchantUtils;
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

	private static Logger logger = LogManager.getLogger(Object.class);

	/**
	 * 计数器
	 */
	private static AtomicInteger counter = new AtomicInteger(0);
	
	@Autowired
	private OrderDao orderDao;
	@Autowired
	private MerchantUtils merchantUtils;
	@Autowired
	private PaymentService paymentService;
	@Autowired
	private AccessTokenService accessTokenService;
	@Autowired
	private YsPayReceiveService ysPayReceiveService;

	public void pushPaymentRecordQtzJob() {
		if(counter.get() % 10 == 0){
			System.out.println("--扫描需要自助申报的支付单--");
		}
		Map<String, Object> params = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, -7);
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
		// 申报状态：1-待申报、2-申报中、3-申报成功、4-申报失败、10-申报中(待系统处理)
		params.put("pay_record_status", 10);
		// 网关接收状态： 0-未发起,1-接收成功,2-接收失败
		//params.put("networkStatus", 0);
		try {
			int page = 1;
			int size = 300;
			List<Mpay> reMpayList = orderDao.findByPropertyLike(Mpay.class, params, null, page, size);
			while (reMpayList != null && !reMpayList.isEmpty()) {
				if (page != 1) {
					reMpayList = orderDao.findByPropertyLike(Mpay.class, params, null, page, size);
				}
				if (reMpayList != null && !reMpayList.isEmpty()) {
					for (Mpay pay : reMpayList) {
						sendPaymentRecord(pay.getMerchant_no(), pay.getTrade_no());
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

	private void sendPaymentRecord(String merchantId, String tradeNo) {
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
			// 当不是第三方时则使用银盟商城appkey
			appkey = YmMallConfig.APPKEY;
			appSecret = YmMallConfig.APPSECRET;
		}
		// 请求获取tok
		Map<String, Object> reTokMap = accessTokenService.getRedisToks(appkey, appSecret);
		if (!"1".equals(reTokMap.get(BaseCode.STATUS.toString()))) {
			logger.error(reTokMap.get(BaseCode.MSG.toString()));
		}
		String tok = reTokMap.get(BaseCode.DATAS.toString()) + "";
		
		Map<String, Object> params = new HashMap<>();
		params.put(MERCHANT_NO, merchantId);
		params.put(TRADE_NO, tradeNo);
		List<Mpay> payList = orderDao.findByProperty(Mpay.class, params, 1, 1);
		if (payList == null || payList.isEmpty()) {
			logger.error(tradeNo + "--查询支付单信息失败--");
		} else {
			Map<String, Object> recordMap = new HashMap<>();
			Mpay payInfo = payList.get(0);
			if(StringEmptyUtils.isNotEmpty(payInfo.getEport())){
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
				Map<String, Object> paymentMap = ysPayReceiveService.sendPayment(merchantId, paymentInfoMap, tok, recordMap,
						YmMallConfig.MANUALPAYMENTNOTIFYURL);
				if (!"1".equals(paymentMap.get(BaseCode.STATUS.toString()) + "")) {
					logger.error(tradeNo + "<--接收失败--"+paymentMap.get(BaseCode.MSG.toString()));
				} else {
					System.out.println("---支付单接收成功->");
					String rePayMessageID = paymentMap.get("messageID") + "";
					paymentService.updatePaymentInfo(tradeNo, rePayMessageID, null);
				}
			}
		}
	}

}

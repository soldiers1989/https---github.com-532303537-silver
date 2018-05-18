package org.silver.shop.impl.system.cross;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.plaf.synth.SynthSpinnerUI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.loader.custom.Return;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.AccessTokenService;
import org.silver.shop.api.system.cross.PaymentService;
import org.silver.shop.config.YmMallConfig;
import org.silver.shop.dao.system.cross.PaymentDao;
import org.silver.shop.impl.system.commerce.GoodsRecordServiceImpl;
import org.silver.shop.impl.system.manual.MpayServiceImpl;
import org.silver.shop.impl.system.tenant.MerchantWalletServiceImpl;
import org.silver.shop.model.common.base.Postal;
import org.silver.shop.model.system.cross.PaymentContent;
import org.silver.shop.model.system.manual.Appkey;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.manual.MorderSub;
import org.silver.shop.model.system.manual.Mpay;
import org.silver.shop.model.system.manual.PaymentCallBack;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantRecordInfo;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.shop.util.BufferUtils;
import org.silver.shop.util.InvokeTaskUtils;
import org.silver.shop.util.MerchantUtils;
import org.silver.shop.util.RedisInfoUtils;
import org.silver.shop.util.SearchUtils;
import org.silver.util.DateUtil;
import org.silver.util.IdcardValidator;
import org.silver.util.JedisUtil;
import org.silver.util.PhoneUtils;
import org.silver.util.RandomUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.SerializeUtil;
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

@Service(interfaceClass = PaymentService.class)
public class PaymentServiceImpl implements PaymentService {

	private static Logger logger = LogManager.getLogger(PaymentServiceImpl.class);
	@Autowired
	private PaymentDao paymentDao;
	@Autowired
	private YsPayReceiveServiceImpl ysPayReceiveServiceImpl;
	@Autowired
	private AccessTokenService accessTokenService;
	@Autowired
	private GoodsRecordServiceImpl goodsRecordServiceImpl;
	@Autowired
	private MpayServiceImpl mpayServiceImpl;

	@Autowired
	private BufferUtils bufferUtils;
	@Autowired
	private InvokeTaskUtils invokeTaskUtils;
	@Autowired
	private MerchantUtils merchantUtils;
	@Autowired
	private MerchantWalletServiceImpl merchantWalletServiceImpl;
	/**
	 * 错误标识
	 */
	private static final String ERROR = "error";
	/**
	 * appkey键
	 */
	private static final String APPKEY = "appkey";

	/**
	 * 商户Id
	 */
	private static final String MERCHANT_ID = "merchantId";
	
	/**
	 * 下划线版本商户Id
	 */
	private static final String MERCHANT_NO ="merchant_no";
	/**
	 * 口岸
	 */
	private static final String E_PORT ="eport";
	
	/**
	 * 检验检疫机构代码
	 */
	private static final String CIQ_ORG_CODE ="ciqOrgCode";
	
	/**
	 * 主管海关代码
	 */
	private static final String CUSTOMS_CODE = "customsCode";
	
	@Override
	public Map<String, Object> updatePaymentStatus(Map<String, Object> datasMap) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置时间格式
		String defaultDate = sdf.format(date); // 格式化当前时间
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("reSerialNo", datasMap.get("messageID") + "");
		String reMsg = datasMap.get("errMsg") + "";
		List<Object> reList = paymentDao.findByProperty(PaymentContent.class, paramMap, 1, 1);
		if (reList != null && reList.size() > 0) {
			PaymentContent payment = (PaymentContent) reList.get(0);
			String status = datasMap.get("status") + "";
			String note = payment.getReNote();
			if ("null".equals(note) || note == null) {
				note = "";
			}
			if ("1".equals(status)) {
				// 支付单备案状态修改为成功
				payment.setPayRecord(2);
			} else {
				payment.setPayRecord(3);
			}
			payment.setReNote(note + defaultDate + ":" + reMsg + ";");
			payment.setUpdateDate(date);
			if (!paymentDao.update(payment)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "异步更新支付单备案信息错误!");
				return paramMap;
			}
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
		}
		return statusMap;
	}

	@Override
	public Object sendMpayByRecord(String merchantId, Map<String, Object> recordMap, String tradeNoPack,
			String proxyParentId, String merchantName, String proxyParentName) {
		Map<String, Object> statusMap = new HashMap<>();
		List<Map<String, Object>> errorList = new ArrayList<>();
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(tradeNoPack);
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("支付流水号包格式错误,请核实信息！");
		}
		int eport = Integer.parseInt(recordMap.get(E_PORT) + "");
		String ciqOrgCode = recordMap.get(CIQ_ORG_CODE) + "";
		String customsCode = recordMap.get(CUSTOMS_CODE) + "";
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
		recordMap.put("ebEntNo", merchantRecordInfo.getEbEntNo());
		recordMap.put("ebEntName", merchantRecordInfo.getEbEntName());
		recordMap.put("ebpEntNo", merchantRecordInfo.getEbpEntNo());
		recordMap.put("ebpEntName", merchantRecordInfo.getEbpEntName());

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
			recordMap.put(APPKEY, appkey.getApp_key());
			recordMap.put("appSecret", appkey.getApp_secret());
		} else {
			// 当不是第三方时则使用银盟商城appkey
			recordMap.put(APPKEY, YmMallConfig.APPKEY);
			recordMap.put("appSecret", YmMallConfig.APPSECRET);
		}
		Map<String, Object> reCheckMap = computingCostsManualPayment(jsonList, merchantId, merchantName);
		if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
			return reCheckMap;
		}
		// 请求获取tok
		Map<String, Object> reTokMap = accessTokenService.getRedisToks(recordMap.get(APPKEY) + "",
				recordMap.get("appSecret") + "");
		if (!"1".equals(reTokMap.get(BaseCode.STATUS.toString()))) {
			return ReturnInfoUtils.errorInfo(reTokMap.get("errMsg") + "");
		}
		String tok = reTokMap.get(BaseCode.DATAS.toString()) + "";
		// 获取流水号
		String serialNo = "paymentRecord_" + SerialNoUtils.getSerialNo("paymentRecord");
		// 总数
		int totalCount = jsonList.size();
		Map<String, Object> params = new HashMap<>();
		params.put(MERCHANT_ID, merchantId);
		params.put("merchantName", merchantName);
		params.put("tok", tok);
		params.put("serialNo", serialNo);
		Map<String, Object> reMap = invokeTaskUtils.commonInvokeTask(3, totalCount, jsonList, errorList, recordMap,
				params);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		statusMap.clear();
		statusMap.put("status", 1);
		statusMap.put("msg", "执行成功,开始推送支付单备案.......");
		statusMap.put("serialNo", serialNo);
		return statusMap;
	}

	/**
	 * 计算商户余额是否有足够的钱支付本次推送支付单手续费
	 * 
	 * @param jsonList
	 *            支付流水号集合
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @return Map
	 */
	private Map<String, Object> computingCostsManualPayment(JSONArray jsonList, String merchantId,
			String merchantName) {
		// 查询商户钱包
		Map<String, Object> reMap = merchantWalletServiceImpl.checkWallet(1, merchantId, merchantName);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return ReturnInfoUtils.errorInfo("创建钱包失败!");
		}
		MerchantWalletContent merchantWallet = (MerchantWalletContent) reMap.get(BaseCode.DATAS.toString());
		double merchantBalance = merchantWallet.getBalance();
		// 所有支付单总金额
		double totalAmountPaid = paymentDao
				.statisticalManualPaymentAmount(JSONArray.toList(jsonList, new HashMap<>(), new JsonConfig()));
		if (totalAmountPaid < 0) {
			return ReturnInfoUtils.errorInfo("查询手工支付单总金额失败,服务器繁忙!");
		}
		// 支付单平台服务费
		double serviceFee = totalAmountPaid * 0.002;
		if (merchantBalance - serviceFee < 0) {
			return ReturnInfoUtils.errorInfo("推送支付单失败,余额不足,请先充值后再进行操作!");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 开始发起支付单推送
	 * 
	 * @param jsonList
	 * @param errorList
	 *            错误信息
	 * @param recordMap
	 *            商户备案信息
	 * @param paramsMap
	 *            缓存参数
	 */
	public final void startSendPaymentRecord(JSONArray jsonList, List<Map<String, Object>> errorList,
			Map<String, Object> recordMap, Map<String, Object> paramsMap) {
		String merchantId = paramsMap.get(MERCHANT_ID) + "";
		String tok = paramsMap.get("tok") + "";
		paramsMap.put("name", "paymentRecord");
		Map<String, Object> param = new HashMap<>();
		for (int i = 0; i < jsonList.size(); i++) {
			Map<String, Object> treadeMap = (Map<String, Object>) jsonList.get(i);
			String treadeNo = treadeMap.get("treadeNo") + "";
			try {
				param.clear();
				param.put(MERCHANT_NO, merchantId);
				param.put("trade_no", treadeNo);
				List<Mpay> payList = paymentDao.findByProperty(Mpay.class, param, 1, 1);
				if (payList != null && !payList.isEmpty()) {
					Mpay payInfo = payList.get(0);
					Map<String, Object> paymentInfoMap = new HashMap<>();
					paymentInfoMap.put("EntPayNo", payInfo.getTrade_no());
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
					Map<String, Object> paymentMap = ysPayReceiveServiceImpl.sendPayment(merchantId, paymentInfoMap,
							tok, recordMap, YmMallConfig.MANUALPAYMENTNOTIFYURL);
					if (!"1".equals(paymentMap.get(BaseCode.STATUS.toString()) + "")) {
						String msg = "支付流水号[" + treadeNo + "]-->" + paymentMap.get(BaseCode.MSG.toString());
						RedisInfoUtils.commonErrorInfo(msg, errorList, ERROR, paramsMap);
						continue;
					}
					// 当未备案时进行手续费清算
					if (payInfo.getPay_record_status() == 1) {
						// 支付单服务费清算
						Map<String, Object> rePaymentTollMap = paymentToll(merchantId, payInfo.getTrade_no(),
								payInfo.getPay_amount());
						if (!"1".equals(rePaymentTollMap.get(BaseCode.STATUS.toString()))) {
							String msg = "支付流水号[" + treadeNo + "]-->" + rePaymentTollMap.get(BaseCode.MSG.toString());
							RedisInfoUtils.commonErrorInfo(msg, errorList, ERROR, paramsMap);
							continue;
						}
					}
					String rePayMessageID = paymentMap.get("messageID") + "";
					// 更新服务器返回支付Id
					Map<String, Object> rePaymentMap2 = updatePaymentInfo(treadeNo, rePayMessageID, recordMap);
					if (!"1".equals(rePaymentMap2.get(BaseCode.STATUS.toString()) + "")) {
						String msg = "支付流水号[" + treadeNo + "]-->" + rePaymentMap2.get(BaseCode.MSG.toString());
						RedisInfoUtils.commonErrorInfo(msg, errorList, ERROR, paramsMap);
						continue;
					}
				} else {
					String msg = "支付流水号[" + treadeNo + "]查询支付单信息失败,请核实流水号!";
					RedisInfoUtils.commonErrorInfo(msg, errorList, ERROR, paramsMap);
					continue;
				}
				bufferUtils.writeRedis(errorList, paramsMap);
				Thread.sleep(200);
			} catch (Exception e) {
				logger.error(Thread.currentThread().getName() + "-->>>>", e);
				String msg = "[" + treadeNo + "]支付单推送失败,系統繁忙!";
				RedisInfoUtils.commonErrorInfo(msg, errorList, ERROR, paramsMap);
			}
		}
		bufferUtils.writeCompletedRedis(errorList, paramsMap);
	}

	/**
	 * 当支付单状态为未备案时,第一次推送后进行支付单服务费清算
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param tradeNo
	 *            交易流水号
	 * @param price
	 *            金额
	 * @return Map
	 */
	private Map<String, Object> paymentToll(String merchantId, String tradeNo, double price) {
		if (StringEmptyUtils.isEmpty(merchantId) || StringEmptyUtils.isEmpty(tradeNo)
				|| StringEmptyUtils.isEmpty(price)) {
			return ReturnInfoUtils.errorInfo("清算订单服务费,请求参数不能为空！");
		}
		Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(merchantId);
		if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
			return reMerchantMap;
		}
		Merchant merchant = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
		Map<String, Object> reUpdateWalletMap = mpayServiceImpl.updateWallet(1, merchantId, merchant.getMerchantName(),
				tradeNo, merchant.getAgentParentId(), price, merchant.getAgentParentName());
		if (!"1".equals(reUpdateWalletMap.get(BaseCode.STATUS.toString()))) {
			return reUpdateWalletMap;
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 更新支付单返回信息
	 * 
	 * @param entPayNo
	 *            交易流水号
	 * @param rePayMessageID
	 *            服务端返回的流水Id
	 * @param customsMap
	 *            前台传递的海关信息
	 * @return Map
	 */
	private Map<String, Object> updatePaymentInfo(String entPayNo, String rePayMessageID,
			Map<String, Object> customsMap) {
		String eport = customsMap.get(E_PORT) + "";
		String ciqOrgCode = customsMap.get(CIQ_ORG_CODE) + "";
		String customsCode = customsMap.get(CUSTOMS_CODE) + "";
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("trade_no", entPayNo);
		List<Mpay> reList = paymentDao.findByProperty(Mpay.class, paramMap, 0, 0);
		if (reList != null && !reList.isEmpty()) {
			for (int i = 0; i < reList.size(); i++) {
				Mpay payment = reList.get(i);
				payment.setPay_serial_no(rePayMessageID);
				// 备案状态：1-未备案,2-备案中,3-备案成功、4-备案失败
				payment.setPay_record_status(2);
				if (StringEmptyUtils.isNotEmpty(eport) && StringEmptyUtils.isNotEmpty(ciqOrgCode)
						&& StringEmptyUtils.isNotEmpty(customsCode)) {
					payment.setEport(eport);
					payment.setCiqOrgCode(ciqOrgCode);
					payment.setCustomsCode(customsCode);
				}
				payment.setUpdate_date(new Date());
				if (!paymentDao.update(payment)) {
					return ReturnInfoUtils.errorInfo("更新服务器返回messageID错误,服务器繁忙!");
				}
			}
			return ReturnInfoUtils.successInfo();
		} else {
			return ReturnInfoUtils.errorInfo("交易流水号[" + entPayNo + "]未找到支付单信息!");
		}
	}

	@Override
	public Map<String, Object> updatePayRecordInfo(Map<String, Object> datasMap) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置时间格式
		String defaultDate = sdf.format(date); // 格式化当前时间
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("pay_serial_no", datasMap.get("messageID") + "");
		String entPayNo = datasMap.get("entPayNo") + "";
		paramMap.put("trade_no", entPayNo);
		String reMsg = datasMap.get("msg") + "";
		List<Mpay> reList = paymentDao.findByPropertyOr2(Mpay.class, paramMap, 0, 0);
		if (reList != null && !reList.isEmpty()) {
			Mpay pay = reList.get(0);
			String status = datasMap.get("status") + "";
			String note = pay.getPay_re_note();
			if (StringEmptyUtils.isEmpty(note)) {
				note = "";
			}
			if ("1".equals(status)) {
				// 已经返回过一次备案成功后
				if (note.contains("保存成功") || note.contains("入库成功")
						|| note.contains("新增申报成功") && pay.getPay_record_status() == 3) {
					System.out.println("------重复支付单回执拦截成功------");
					return ReturnInfoUtils.successInfo();
				}
				// 支付单备案状态修改为成功
				pay.setPay_record_status(3);
			} else {
				// 备案失败
				if (reMsg.contains("旧报文数据") || reMsg.contains("支付数据已存在") || reMsg.contains("重复申报")) {
					return updateOldPaymentInfo(pay);
				}
				// 备案失败
				pay.setPay_record_status(4);
			}
			if (StringEmptyUtils.isNotEmpty(reMsg)) {
				pay.setPay_re_note(note + defaultDate + ":" + reMsg + ";");
			}
			Map<String, Object> reUpdateMap = updatePaymentRecordInfo(pay);
			if (!"1".equals(reUpdateMap.get(BaseCode.STATUS.toString()))) {
				System.out.println("--更新支付单信息失败--" + reUpdateMap.get(BaseCode.MSG.toString()));
			}
			return reThirdPartyPaymentInfo(pay, pay.getMerchant_no());
		} else {
			return ReturnInfoUtils.errorInfo("支付单[" + entPayNo + "]为找到对应信息,请核对信息!");
		}
	}

	private Map<String, Object> reThirdPartyPaymentInfo(Mpay pay, String merchantId) {
		if (StringEmptyUtils.isEmpty(merchantId) || pay == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(merchantId);
		if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
			return reMerchantMap;
		}
		Merchant merchant = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
		// 第三方标识：1-银盟(银盟商城平台),2-第三方商城平台
		int thirdPartyFlag = merchant.getThirdPartyFlag();
		if (thirdPartyFlag == 2) {
			System.out.println("---------返回第三方支付单信息----------");
			rePaymentInfo(pay);
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 返回给第三方支付单信息
	 * 
	 * @param pay
	 *            手工支付单实体类
	 * @return Map
	 */
	public void rePaymentInfo(Mpay pay) {
		if(pay !=null){
			Map<String, Object> item = new HashMap<>();
			JSONObject payment = new JSONObject();
			payment.element("thirdPartyId", pay.getThirdPartyId());
			payment.element("EntPayNo", pay.getTrade_no());
			payment.element("PayStatus", pay.getPay_status());
			payment.element("PayAmount", pay.getPay_amount());
			payment.element("PayCurrCode", pay.getPay_currCode());
			payment.element("PayTime", DateUtil.formatDate(pay.getPay_time(), "yyyyMMddhhmmss"));
			payment.element("PayerName", pay.getPayer_name());
			payment.element("PayerDocumentType", pay.getPayer_document_type());
			payment.element("PayerDocumentNumber", pay.getPayer_document_number());
			payment.element("PayerPhoneNumber", pay.getPayer_phone_number());
			payment.element("EntOrderNo", pay.getMorder_id());
			payment.element("Notes", pay.getRemarks());
			payment.element("payRecordNote", pay.getPay_re_note());
			payment.element("payRecordStatus", pay.getPay_record_status());
			payment.element(MERCHANT_ID, pay.getMerchant_no());
			payment.element(CIQ_ORG_CODE, pay.getCiqOrgCode());
			payment.element(CUSTOMS_CODE, pay.getCustomsCode());
			item.put("payment", payment.toString());
			String result = YmHttpUtil.HttpPost("https://ym.191ec.com/silver-web/Eport/getway-callback", item);
			// String result =
			// YmHttpUtil.HttpPost("http://192.168.1.104:8080/silver-web/Eport/getway-callback",
			// item);
			if (StringEmptyUtils.isNotEmpty(result) && result.replace("\n", "").equalsIgnoreCase("success")) {
				updateSuccessPaymentCallBack(pay);
			} else {
				// 当第三方接收支付单回执失败时,保存信息
				savePaymentCallBack(pay);
			}
		}
	}

	/**
	 * 更新成功的支付单第三方回调信息
	 * 
	 * @param Mpay 手工支付单实体类
	 */
	private void updateSuccessPaymentCallBack(Mpay pay) {
		Map<String, Object> params = new HashMap<>();
		params.put("thirdPartyId", pay.getThirdPartyId());
		params.put(MERCHANT_ID, pay.getMerchant_no());
		@SuppressWarnings("unchecked")
		List<PaymentCallBack> rePaymentCallBackList = paymentDao.findByProperty(PaymentCallBack.class, params, 0, 0);
		if (rePaymentCallBackList != null && !rePaymentCallBackList.isEmpty()) {
			Date date = new Date();
			PaymentCallBack paymentCallBack = rePaymentCallBackList.get(0);
			paymentCallBack.setResendStatus("SUCCESS");
			paymentCallBack.setUpdateBy("system");
			paymentCallBack.setUpdateDate(date);
			System.out.println(DateUtil.formatDate(date, "yyyy-MM-dd HH:mm:ss") + " 支付单重发第"
					+ paymentCallBack.getResendCount() + "次,接收成功!");
			paymentCallBack.setRemark(DateUtil.formatDate(date, "yyyy-MM-dd HH:mm:ss") + " 支付单重发第"
					+ (paymentCallBack.getResendCount() + 1) + "次,接收成功!");
			if (!paymentDao.update(paymentCallBack)) {
				logger.error("--异步回调第三方支付单成功后保存信息失败--");
			}
			pay.setResendStatus("SUCCESS");
			if (!paymentDao.update(pay)) {
				logger.error("--异步回调第三方支付单成功后更新支付单状态失败--");
			}
		}
	}

	/**
	 * 当第三方接收支付单回执失败时,保存进支付回调信息
	 * 
	 * @param pay
	 */
	private void savePaymentCallBack(Mpay pay) {
		
		Map<String, Object> params = new HashMap<>();
		params.put("thirdPartyId", pay.getThirdPartyId());
		params.put("tradeNo", pay.getTrade_no());
		params.put(MERCHANT_ID, pay.getMerchant_no());
		List<PaymentCallBack> rePaymentCallBackList = paymentDao.findByProperty(PaymentCallBack.class, params, 0, 0);
		if (rePaymentCallBackList != null && !rePaymentCallBackList.isEmpty()) {
			// 更新支付单回传记录中的回传计数器
			PaymentCallBack paymentCallBack = rePaymentCallBackList.get(0);
			int count = paymentCallBack.getResendCount();
			System.out.println(pay.getTrade_no() + "--支付单->第" + (count + 1) + "次重发接受失败");
			if (count == 9) {
				paymentCallBack.setRemark(DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss") + "_支付单重发第10次,接收失败!");
			}
			count++;
			paymentCallBack.setResendCount(count);
			paymentCallBack.setResendStatus("FALSE");
			if (!paymentDao.update(paymentCallBack)) {
				logger.error("--异步更新第三方支付单计数器失败--");
			}
		} else {
			PaymentCallBack paymentCallBack = new PaymentCallBack();
			paymentCallBack.setMerchantId(pay.getMerchant_no());
			paymentCallBack.setOrderId(pay.getMorder_id());
			paymentCallBack.setTradeNo(pay.getTrade_no());
			paymentCallBack.setThirdPartyId(pay.getThirdPartyId());
			paymentCallBack.setCreateBy("system");
			paymentCallBack.setCreateDate(new Date());
			paymentCallBack.setResendCount(0);
			paymentCallBack.setResendStatus("FALSE");
			if (!paymentDao.add(paymentCallBack)) {
				logger.error("--异步保存支付单第三方回调信息失败--");
			}
			pay.setResendStatus("FALSE");
			if (!paymentDao.update(pay)) {
				logger.error("--异步回调第三方支付单后更新支付单失败状态--");
			}
		}
	}

	/**
	 * 重复回执后更新旧手工支付单备案状态,如果为备案失败则修改为备案成功
	 * 
	 * @param pay
	 * @return
	 */
	private Map<String, Object> updateOldPaymentInfo(Mpay pay) {
		if (pay.getPay_record_status() == 4) {
			System.out.println("-------旧报文备案失败修改为成功--");
			pay.setPay_record_status(3);
			return updatePaymentRecordInfo(pay);
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 更新手工支付单状态
	 * 
	 * @param pay
	 * @return
	 */
	private Map<String, Object> updatePaymentRecordInfo(Mpay pay) {
		pay.setUpdate_date(new Date());
		if (!paymentDao.update(pay)) {
			return ReturnInfoUtils.errorInfo("异步更新订单备案信息错误!");
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Object getMpayRecordInfo(String merchantId, String merchantName, Map<String, Object> params, int page,
			int size) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> reDatasMap = SearchUtils.universalMPaymentSearch(params);
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		paramMap.put(MERCHANT_NO, merchantId);
		paramMap.put("del_flag", 0);
		List<Object> reList = paymentDao.findByPropertyLike(Mpay.class, paramMap, null, page, size);
		long tatolCount = paymentDao.findByPropertyLikeCount(Mpay.class, paramMap, null);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("服务器繁忙!");
		} else if (!reList.isEmpty()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), reList);
			statusMap.put(BaseCode.TOTALCOUNT.toString(), tatolCount);
			return statusMap;
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

	@Override
	public Map<String, Object> getMerchantPaymentReport(String merchantId, String merchantName, String startDate,
			String endDate) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramsMap = new HashMap<>();
		if (StringEmptyUtils.isNotEmpty(merchantId)) {
			paramsMap.put(MERCHANT_ID, merchantId);
		}
		if (StringEmptyUtils.isNotEmpty(merchantName)) {
			paramsMap.put("merchantName", merchantName);
		}
		paramsMap.put("startDate", startDate);
		paramsMap.put("endDate", endDate);
		Table reList = paymentDao.getPaymentReport(Morder.class, paramsMap, 0, 0);
		Table totalCount = paymentDao.getPaymentReport(Morder.class, paramsMap, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.getRows().isEmpty()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), Transform.tableToJson(reList).getJSONArray("rows"));
			statusMap.put(BaseCode.TOTALCOUNT.toString(), totalCount.getRows().size());
			return statusMap;
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

	@Override
	public final Map<String, Object> groupCreateMpay(List<String> orderIDs, List<Map<String, Object>> errorList,
			Map<String, Object> redisMap) {
		String merchantId = redisMap.get(MERCHANT_ID) + "";
		if (StringEmptyUtils.isNotEmpty(merchantId) && orderIDs != null && !orderIDs.isEmpty()) {
			//
			redisMap.put("name", "createPaymentId");
			for (String order_id : orderIDs) {
				Map<String, Object> params = new HashMap<>();
				params.put(MERCHANT_NO, merchantId);
				params.put("order_id", order_id);
				List<Morder> morder = paymentDao.findByProperty(Morder.class, params, 1, 1);
				if (morder != null && !morder.isEmpty()) {
					Morder order = morder.get(0);
					Map<String, Object> reCheckMap = checkPaymentInfo(order);
					if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
						RedisInfoUtils.commonErrorInfo(reCheckMap.get(BaseCode.MSG.toString()) + "", errorList, ERROR,
								redisMap);
						continue;
					}
					//
					Map<String, Object> paymentMap = new HashMap<>();
					paymentMap.put(MERCHANT_ID, merchantId);
					int count = SerialNoUtils.getRedisIdCount("paymentId");
					String tradeNo = createTradeNo("01O", (count + 1), new Date());
					paymentMap.put("tradeNo", tradeNo);
					paymentMap.put("orderId", order_id);
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
					if (addEntity(paymentMap) && updateOrderPayNo(merchantId, order_id, tradeNo)) {
						// 当创建完支付流水号之后
						bufferUtils.writeRedis(errorList, redisMap);
						continue;
					}
					RedisInfoUtils.commonErrorInfo("订单号[" + order_id + "]生成支付单失败,请稍后重试!", errorList, ERROR, redisMap);
					continue;
				}
				RedisInfoUtils.commonErrorInfo("订单号[" + order_id + "]不存在,请核对信息!", errorList, ERROR, redisMap);
			}
			bufferUtils.writeCompletedRedis(errorList, redisMap);
			return null;
		} else {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
	}

	/**
	 * 生成支付单时校验订单信息
	 * 
	 * @param order
	 *            订单信息实体类
	 * @return Map
	 */
	private Map<String, Object> checkPaymentInfo(Morder order) {
		Map<String, Object> params = new HashMap<>();
		String orderId = order.getOrder_id();
		params.put("morder_id", orderId);
		List<Mpay> mpayl = paymentDao.findByProperty(Mpay.class, params, 1, 1);
		if (mpayl != null && !mpayl.isEmpty()) {
			return ReturnInfoUtils.errorInfo("订单号[" + orderId + "]关联的支付单已经存在,无需重复生成!");
		}
		String orderDocId = order.getOrderDocId();
		if (!IdcardValidator.validate18Idcard(orderDocId)) {
			return ReturnInfoUtils.errorInfo("订单号[" + orderId + "]实名认证不通过,请核实身份证信息!");
		}
		if (!StringUtil.isContainChinese(order.getOrderDocName().replace("·", ""))) {
			return ReturnInfoUtils.errorInfo("订单号[" + orderId + "]下单人姓名错误,请核实信息!");
		}
		if (!StringUtil.isContainChinese(order.getRecipientName().replace("·", ""))) {
			return ReturnInfoUtils.errorInfo("订单号[" + orderId + "]收货人姓名错误,请核实信息!");
		}
		if (!PhoneUtils.isPhone(order.getRecipientTel())) {
			return ReturnInfoUtils.errorInfo("订单号[" + orderId + "]收件人电话错误,请核实信息!");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 保存支付单实体
	 * 
	 * @param paymentMap
	 *            支付单信息集合
	 * @return
	 */
	private boolean addEntity(Map<String, Object> paymentMap) {
		if (paymentMap == null || paymentMap.isEmpty()) {
			return false;
		}
		Mpay entity = new Mpay();
		entity.setMerchant_no(paymentMap.get(MERCHANT_ID) + "");
		entity.setTrade_no(paymentMap.get("tradeNo") + "");
		entity.setMorder_id(paymentMap.get("orderId") + "");
		entity.setPay_amount(Double.parseDouble(paymentMap.get("amount") + ""));
		entity.setPayer_name(paymentMap.get("orderDocName") + "");
		entity.setPayer_document_type("01");
		entity.setPayer_document_number(paymentMap.get("orderDocId") + "");
		entity.setPayer_phone_number(paymentMap.get("orderDocTel") + "");
		entity.setTrade_status("TRADE_SUCCESS");
		entity.setDel_flag(0);
		entity.setCreate_date(new Date());
		entity.setYear(DateUtil.formatDate(new Date(), "yyyy"));
		entity.setPay_status("D");
		entity.setPay_currCode("142");
		entity.setPay_record_status(1);
		String orderDate = paymentMap.get("orderDate") + "";
		Date payTime = DateUtil.randomPaymentDate(orderDate);
		entity.setPay_time(payTime);
		entity.setCreate_by(paymentMap.get("createBy") + "");
		// 口岸标识
		String eport = paymentMap.get(E_PORT) + "";
		// 国检检疫编码
		String ciqOrgCode = paymentMap.get(CIQ_ORG_CODE) + "";
		// 海关关区编码
		String customsCode = paymentMap.get(CUSTOMS_CODE) + "";
		if (StringEmptyUtils.isNotEmpty(eport) && StringEmptyUtils.isNotEmpty(ciqOrgCode)
				&& StringEmptyUtils.isNotEmpty(customsCode)) {
			entity.setEport(eport);
			entity.setCiqOrgCode(ciqOrgCode);
			entity.setCustomsCode(customsCode);
		}
		// 第三方业务Id
		String thirdPartyId = paymentMap.get("thirdPartyId") + "";
		if (StringEmptyUtils.isNotEmpty(thirdPartyId)) {
			entity.setThirdPartyId(thirdPartyId);
		}
		return paymentDao.add(entity);
	}

	/**
	 * 当生成的支付流水号更新到订单表中
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param order_id
	 *            订单Id
	 * @param trade_no
	 *            支付流水号
	 * @return boolean
	 */
	private boolean updateOrderPayNo(String merchantId, String order_id, String trade_no) {
		Map<String, Object> param = new HashMap<>();
		param.put(MERCHANT_NO, merchantId);
		param.put("order_id", order_id);
		List<Morder> reList = paymentDao.findByProperty(Morder.class, param, 1, 1);
		if (reList != null && !reList.isEmpty()) {
			Morder entity = reList.get(0);
			entity.setTrade_no(trade_no);
			entity.setUpdate_date(new Date());
			return paymentDao.update(entity);
		}
		return false;
	}

	/**
	 * 模拟银盛生成支付流水号
	 * 
	 * @param sign
	 *            日期标识
	 * @param id
	 *            自增Id数
	 * @param date
	 *            日期
	 * @return String
	 */
	private String createTradeNo(String sign, long id, Date date) {
		String dstr = DateUtil.formatDate(date, "yyMMdd");
		String nstr = id + "";
		// 获取或随机4位数
		int rstr = RandomUtils.getRandom(4);
		while (nstr.length() < 5) {
			nstr = "0" + nstr;
		}
		return sign + dstr + nstr + rstr;
	}

	@Override
	public Map<String, Object> splitStartPaymentId(List<String> orderIdList, String merchantId, String merchantName) {
		Map<String, Object> statusMap = new HashMap<>();
		String serialNo = "payment_" + SerialNoUtils.getSerialNo("payment");
		// 总数
		int totalCount = orderIdList.size();
		List<Map<String, Object>> errorList = new Vector();
		Map<String, Object> params = new HashMap<>();
		params.put(MERCHANT_ID, merchantId);
		params.put("merchantName", merchantName);
		params.put("tok", "tok");
		params.put("serialNo", serialNo);
		Map<String, Object> reMap = invokeTaskUtils.commonInvokeTask(1, totalCount, JSONArray.fromObject(orderIdList),
				errorList, null, params);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		statusMap.put("status", 1);
		statusMap.put("msg", "执行成功,正在生成支付流水号.......");
		statusMap.put("serialNo", serialNo);
		return statusMap;
	}

	@Override
	public Map<String, Object> managerGetMpayInfo(Map<String, Object> params, int page, int size) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> reDatasMap = SearchUtils.universalMPaymentSearch(params);
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		List<Mpay> reList = paymentDao.findByPropertyLike(Mpay.class, paramMap, null, page, size);
		long tatolCount = paymentDao.findByPropertyLikeCount(Mpay.class, paramMap, null);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("服务器繁忙!");
		} else if (!reList.isEmpty()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), reList);
			statusMap.put(BaseCode.TOTALCOUNT.toString(), tatolCount);
			return statusMap;
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

	@Override
	public Map<String, Object> managerEditMpayInfo(Map<String, Object> datasMap, String managerId, String managerName) {
		if (datasMap != null && !datasMap.isEmpty()) {
			Map<String, Object> params = new HashMap<>();
			String tradeNo = datasMap.get("trade_no") + "";
			params.put("trade_no", tradeNo);
			List<Mpay> mPayList = paymentDao.findByProperty(Mpay.class, params, 1, 1);
			if (mPayList != null && !mPayList.isEmpty()) {
				Mpay pay = mPayList.get(0);
				double payAmount = 0.0;
				try {
					payAmount = Double.parseDouble(datasMap.get("pay_amount") + "");
				} catch (Exception e) {
					e.printStackTrace();
					return ReturnInfoUtils.errorInfo("交易金额错误,请重新输入!");
				}
				pay.setPay_amount(payAmount);
				pay.setPayer_name(datasMap.get("payer_name") + "");
				pay.setPayer_document_number(datasMap.get("payer_document_number") + "");
				pay.setPayer_phone_number(datasMap.get("payer_phone_number") + "");
				pay.setDel_flag(Integer.parseInt(datasMap.get("del_flag") + ""));
				pay.setUpdate_by(managerName);
				pay.setUpdate_date(new Date());
				if (paymentDao.update(pay)) {
					return ReturnInfoUtils.successInfo();
				}
				return ReturnInfoUtils.errorInfo("更新支付单失败,服务器繁忙!");
			}
			return ReturnInfoUtils.errorInfo("支付单信息未找到,请核实信息!");
		}
		return ReturnInfoUtils.errorInfo("请求参数错误!");
	}

	@Override
	public Map<String, Object> getAgentPaymentReport(Map<String, Object> datasMap) {
		if (datasMap != null && !datasMap.isEmpty()) {
			Table reTable = paymentDao.getAgentPaymentReport(datasMap);
			if (reTable == null) {
				return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
			} else if (!reTable.getRows().isEmpty()) {
				return ReturnInfoUtils.successDataInfo(Transform.tableToJson(reTable).getJSONArray("rows"));
			} else {
				return ReturnInfoUtils.errorInfo("暂无数据");
			}
		}
		return ReturnInfoUtils.errorInfo("请求参数错误！");
	}

	@Override
	public Map<String, Object> managerHideMpayInfo(JSONArray jsonArray, String managerName) {
		if (jsonArray != null && !jsonArray.isEmpty()) {
			Map<String, Object> params = new HashMap<>();
			for (int i = 0; i < jsonArray.size(); i++) {
				String tradeNo = jsonArray.get(i) + "";
				params.put("trade_no", tradeNo);
				List<Mpay> reMpayList = paymentDao.findByProperty(Mpay.class, params, 0, 0);
				if (reMpayList == null) {
					return ReturnInfoUtils.errorInfo("支付流水号[" + tradeNo + "]查询失败,服务器繁忙!");
				} else if (!reMpayList.isEmpty()) {
					Mpay pay = reMpayList.get(0);
					pay.setDel_flag(1);
					pay.setUpdate_by(managerName);
					pay.setUpdate_date(new Date());
					if (!paymentDao.update(pay)) {
						return ReturnInfoUtils.errorInfo("支付流水号[" + tradeNo + "]修改状态失败,服务器繁忙!");
					}
				} else {
					return ReturnInfoUtils.errorInfo("支付流水号[" + tradeNo + "]未找到对应的支付单信息!");
				}
			}
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("请求参数错误!");
	}

	/**
	 * 根据支付流水号更新支付单中海关信息
	 * 
	 * @param datasMap
	 */
	public Map<String, Object> updatePaymentPortInfo(Map<String, Object> datasMap) {
		if (datasMap == null || datasMap.isEmpty()) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		String eport = datasMap.get(E_PORT) + "";
		String ciqOrgCode = datasMap.get(CIQ_ORG_CODE) + "";
		String customsCode = datasMap.get(CUSTOMS_CODE) + "";
		String tradeNo = datasMap.get("tradeNo") + "";
		Map<String, Object> params = new HashMap<>();
		params.put("trade_no", tradeNo);
		List<Mpay> reMpayList = paymentDao.findByProperty(Mpay.class, params, 1, 1);
		if (reMpayList == null) {
			return ReturnInfoUtils.errorInfo("查询支付单失败,服务器繁忙!");
		} else if (!reMpayList.isEmpty()) {
			Mpay pay = reMpayList.get(0);
			if (StringEmptyUtils.isNotEmpty(eport) && StringEmptyUtils.isNotEmpty(ciqOrgCode)
					&& StringEmptyUtils.isNotEmpty(customsCode)) {
				pay.setEport(eport);
				pay.setCiqOrgCode(ciqOrgCode);
				pay.setCustomsCode(customsCode);
			}
			pay.setUpdate_by("system");
			pay.setUpdate_date(new Date());
			if (!paymentDao.update(pay)) {
				return ReturnInfoUtils.errorInfo("更新支付单信息失败,服务器繁忙!");
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> checkPaymentPort(List<String> tradeNos, String merchantId) {
		if (tradeNos == null || StringEmptyUtils.isEmpty(merchantId)) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}

		List<Mpay> cacheList = new ArrayList<>();
		for (int i = 0; i < tradeNos.size(); i++) {
			String tradeNo = tradeNos.get(i);
			Map<String, Object> params = new HashMap<>();
			params.put("trade_no", tradeNo);
			params.put(MERCHANT_NO, merchantId);
			List<Mpay> reList = paymentDao.findByProperty(Mpay.class, params, 0, 0);
			if (reList == null) {
				return ReturnInfoUtils.errorInfo("查询支付单信息失败,服务器繁忙!");
			} else if (!reList.isEmpty()) {
				Mpay payment = reList.get(0);
				String eport = payment.getEport();
				String ciqOrgCode = payment.getCiqOrgCode();
				String customsCode = payment.getCustomsCode();
				if (StringEmptyUtils.isNotEmpty(eport) && StringEmptyUtils.isNotEmpty(ciqOrgCode)
						&& StringEmptyUtils.isNotEmpty(customsCode)) {
					cacheList.add(payment);
				}
				for (int c = 0; c < cacheList.size(); c++) {
					Mpay cachePayment = cacheList.get(c);
					String cacheEport = cachePayment.getEport();
					String cacheCiqOrgCode = cachePayment.getCiqOrgCode();
					String cacheCustomsCode = cachePayment.getCustomsCode();
					if (!eport.equals(cacheEport) && !ciqOrgCode.equals(cacheCiqOrgCode)
							&& !customsCode.equals(cacheCustomsCode)) {
						return ReturnInfoUtils.errorInfo("所选支付单为多个不同的口岸/海关关区/国检检疫机构信息,请重新选择!");
					}
				}
			} else {
				return ReturnInfoUtils.errorInfo("支付单流水号[" + tradeNo + "]未找到支付单信息,请重新选择!");
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> getThirdPartyInfo(Map<String, Object> datasMap) {
		Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(datasMap.get(MERCHANT_ID) + "");
		if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
			return reMerchantMap;
		}
		Merchant merchant = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
		String thirdPartyId = datasMap.get("thirdPartyId") + "";
		return thirdPartyPaymentInfo(merchant.getMerchantId(), thirdPartyId);
	}

	/**
	 * 第三方平获取支付单信息
	 * 
	 * @param json
	 *            查询参数
	 * @param merchantId
	 *            商户Id
	 * @param thirdPartyId
	 * @return Map
	 */
	private Map<String, Object> thirdPartyPaymentInfo(String merchantId, String thirdPartyId) {
		Map<String, Object> params = new HashMap<>();
		params.put(MERCHANT_NO, merchantId);
		params.put("thirdPartyId", thirdPartyId);
		List<Mpay> rePayList = paymentDao.findByProperty(Mpay.class, params, 1, 1);
		if (rePayList == null) {
			return ReturnInfoUtils.errorInfo("查询支付单信息失败,服务器繁忙!");
		} else if (!rePayList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(rePayList.get(0));
		} else {
			return ReturnInfoUtils.errorInfo("未找到对应的支付单信息!");
		}
	}
}

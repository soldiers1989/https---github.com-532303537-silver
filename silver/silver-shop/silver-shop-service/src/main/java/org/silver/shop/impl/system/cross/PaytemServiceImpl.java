package org.silver.shop.impl.system.cross;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.AccessTokenService;
import org.silver.shop.api.system.cross.PaymentService;
import org.silver.shop.api.system.tenant.WalletLogService;
import org.silver.shop.config.YmMallConfig;
import org.silver.shop.dao.system.cross.PaymentDao;
import org.silver.shop.impl.system.commerce.GoodsRecordServiceImpl;
import org.silver.shop.impl.system.manual.MpayServiceImpl;
import org.silver.shop.impl.system.tenant.MerchantWalletServiceImpl;
import org.silver.shop.model.system.cross.PaymentContent;
import org.silver.shop.model.system.manual.Mpay;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantRecordInfo;
import org.silver.shop.model.system.tenant.MerchantWalletLog;
import org.silver.util.DateUtil;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;

@Service(interfaceClass = PaymentService.class)
public class PaytemServiceImpl implements PaymentService {

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
			statusMap.put(BaseCode.MSG.toString(), "支付流水参数错误,请核实！");
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.toString());
			return statusMap;
		}
		int eport = Integer.parseInt(recordMap.get("eport") + "");
		String ciqOrgCode = recordMap.get("ciqOrgCode") + "";
		String customsCode = recordMap.get("customsCode") + "";
		// 校验前台传递口岸、海关、智检编码
		Map<String, Object> customsMap = goodsRecordServiceImpl.checkCustomsPort(eport, customsCode, ciqOrgCode);
		if (!"1".equals(customsMap.get(BaseCode.STATUS.toString()))) {
			return customsMap;
		}
		// 获取商户在对应口岸的备案信息
		Map<String, Object> merchantRecordMap = getMerchantRecordInfo(merchantId, eport);
		if (!"1".equals(merchantRecordMap.get(BaseCode.STATUS.toString()))) {
			return merchantRecordMap;
		}
		MerchantRecordInfo merchantRecordInfo = (MerchantRecordInfo) merchantRecordMap.get(BaseCode.DATAS.toString());
		recordMap.put("ebEntNo", merchantRecordInfo.getEbEntNo());
		recordMap.put("ebEntName", merchantRecordInfo.getEbEntName());
		recordMap.put("ebpEntNo", merchantRecordInfo.getEbpEntNo());
		recordMap.put("ebpEntName", merchantRecordInfo.getEbpEntName());
		// 请求获取tok
		Map<String, Object> tokMap = accessTokenService.getAccessToken();
		if (!"1".equals(tokMap.get(BaseCode.STATUS.toString()))) {
			return tokMap;
		}
		String tok = tokMap.get(BaseCode.DATAS.toString()) + "";
		Map<String, Object> param = new HashMap<>();
		double cumulativeAmount = 0.0;
		for (int i = 0; i < jsonList.size(); i++) {
			Map<String, Object> treadeMap = (Map<String, Object>) jsonList.get(i);
			String treadeNo = treadeMap.get("treadeNo") + "";
			param.clear();
			param.put("merchant_no", merchantId);
			param.put("trade_no", treadeNo);
			List<Mpay> payList = paymentDao.findByProperty(Mpay.class, param, 1, 1);
			if (payList != null && !payList.isEmpty()) {
				Mpay payInfo = payList.get(0);
			/*	if (payInfo.getPay_record_status() == 2) {
					Map<String, Object> errMap = new HashMap<>();
					errMap.put(BaseCode.MSG.toString(), "支付流水号[" + treadeNo + "]正在备案中无需再次发起!");
					errorList.add(errMap);
					continue;
				} else if (payInfo.getPay_record_status() == 3) {
					Map<String, Object> errMap = new HashMap<>();
					errMap.put(BaseCode.MSG.toString(), "支付流水号[" + treadeNo + "]已备案成功无需再次发起!");
					errorList.add(errMap);
					continue;
				}*/
				double payAmount = payInfo.getPay_amount();
				// 将每个支付单总额进行累加,计算当前金额下是否有足够的余额支付费用
				cumulativeAmount = payAmount += cumulativeAmount;
				Map<String, Object> checkMap = mpayServiceImpl.checkWallet(1, merchantId, merchantName, treadeNo,
						cumulativeAmount);
				if (!"1".equals(checkMap.get(BaseCode.STATUS.toString()))) {
					Map<String, Object> errMap = new HashMap<>();
					errMap.put(BaseCode.MSG.toString(), "支付流水号[" + treadeNo + "]推送失败,钱包余额不足!");
					errorList.add(errMap);
					continue;
				}
				Map<String, Object> paymentInfoMap = new HashMap<>();
				paymentInfoMap.put("EntPayNo", treadeNo);
				paymentInfoMap.put("PayStatus", payInfo.getPay_status());
				paymentInfoMap.put("PayAmount", payInfo.getPay_amount());
				paymentInfoMap.put("PayCurrCode", payInfo.getPay_currCode());
				paymentInfoMap.put("PayTime", payInfo.getCreate_date());
				paymentInfoMap.put("PayerName", payInfo.getPayer_name());
				paymentInfoMap.put("PayerDocumentType", payInfo.getPayer_document_type());
				paymentInfoMap.put("PayerDocumentNumber", payInfo.getPayer_document_number());
				paymentInfoMap.put("PayerPhoneNumber", payInfo.getPayer_phone_number());
				paymentInfoMap.put("EntOrderNo", payInfo.getMorder_id());
				paymentInfoMap.put("Notes", payInfo.getRemarks());
				Map<String, Object> paymentMap = ysPayReceiveServiceImpl.sendPayment(merchantId, paymentInfoMap, tok,
						recordMap, YmMallConfig.MANUALPAYMENTNOTIFYURL);

				/*
				 * Map<String, Object> reUpdateWalletMap =
				 * mpayServiceImpl.updateWallet(1, merchantId, merchantName,
				 * treadeNo, proxyParentId, payInfo.getPay_amount(),
				 * proxyParentName); if
				 * (!"1".equals(reUpdateWalletMap.get(BaseCode.STATUS.toString()
				 * ))) { Map<String,Object> errMap = new HashMap<>();
				 * errMap.put(BaseCode.MSG.toString(),
				 * "支付流水号["+treadeNo+"]推送失败----->>>"+reUpdateWalletMap.get(
				 * BaseCode.MSG.toString())); errorList.add(errMap); continue; }
				 */
				String rePayMessageID = paymentMap.get("messageID") + "";
				// 更新服务器返回支付Id
				Map<String, Object> rePaymentMap2 = updatePaymentInfo(treadeNo, rePayMessageID);
				if (!"1".equals(rePaymentMap2.get(BaseCode.STATUS.toString()) + "")) {
					Map<String, Object> errMap = new HashMap<>();
					errMap.put(BaseCode.MSG.toString(),
							"支付流水号[" + treadeNo + "]----->>>" + rePaymentMap2.get(BaseCode.MSG.toString()));
					errorList.add(errMap);
					continue;
				}
			} else {
				Map<String, Object> errorMap = new HashMap<>();
				errorMap.put(BaseCode.MSG.toString(), "[" + treadeNo + "]该支付流水号不存在,请核实！");
				errorList.add(errorMap);
				continue;
			}
		}
		statusMap.clear();
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		statusMap.put(BaseCode.ERROR.toString(), errorList);
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
		List<MerchantRecordInfo> recordList = paymentDao.findByProperty(MerchantRecordInfo.class, param, 1, 1);
		if (recordList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
		} else if (!recordList.isEmpty()) {
			MerchantRecordInfo entity = recordList.get(0);
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), entity);
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
		}
		return statusMap;
	}

	// 更新支付单返回信息
	private Map<String, Object> updatePaymentInfo(String entPayNo, String rePayMessageID) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("trade_no", entPayNo);
		List<Mpay> reList = paymentDao.findByProperty(Mpay.class, paramMap, 0, 0);
		if (reList != null && !reList.isEmpty()) {
			for (int i = 0; i < reList.size(); i++) {
				Mpay payment = reList.get(i);
				payment.setPay_serial_no(rePayMessageID);
				// 备案状态：1-未备案,2-备案中,3-备案成功、4-备案失败
				payment.setPay_record_status(2);
				payment.setUpdate_date(new Date());
				if (!paymentDao.update(payment)) {
					paramMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					paramMap.put(BaseCode.MSG.toString(), "更新服务器返回messageID错误!");
					return paramMap;
				}
			}
			paramMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			paramMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			return paramMap;
		} else {
			paramMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			paramMap.put(BaseCode.MSG.toString(), "更新支付订单返回messageID错误,服务器繁忙！");
			return paramMap;
		}
	}

	@Override
	public Map<String, Object> updatePayRecordInfo(Map<String, Object> datasMap) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置时间格式
		String defaultDate = sdf.format(date); // 格式化当前时间
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("pay_serial_no", datasMap.get("messageID") + "");
		paramMap.put("trade_no", datasMap.get("entPayNo") + "");
		String reMsg = datasMap.get("msg") + "";
		List<Mpay> reList = paymentDao.findByPropertyOr2(Mpay.class, paramMap, 0, 0);
		if (reList != null && reList.size() > 0) {
			Mpay pay = reList.get(0);
			paramMap.clear();
			paramMap.put("merchantId", pay.getMerchant_no());
			List<Merchant> reMerchantList = paymentDao.findByProperty(Merchant.class, paramMap, 1, 1);
			Merchant merchant = null;
			if (reMerchantList != null && !reMerchantList.isEmpty()) {
				merchant = reMerchantList.get(0);
			}
			String status = datasMap.get("status") + "";
			String note = pay.getPay_re_note();
			if ("null".equals(note) || note == null) {
				note = "";
			}
			if ("1".equals(status)) {
				// 支付单备案状态修改为成功
				pay.setPay_record_status(3);
				// 进行钱包扣款
				Map<String, Object> reUpdateWalletMap = mpayServiceImpl.updateWallet(1, merchant.getMerchantId(),
						merchant.getMerchantName(), pay.getTrade_no(), merchant.getProxyParentId(), pay.getPay_amount(),
						merchant.getProxyParentName());
				if (!"1".equals(reUpdateWalletMap.get(BaseCode.STATUS.toString()))) {
					return reUpdateWalletMap;
				}
			} else {
				// 备案失败
				pay.setPay_record_status(4);
			}
			pay.setPay_re_note(note + defaultDate + ":" + reMsg + ";");
			pay.setUpdate_date(new Date());
			if (!paymentDao.update(pay)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "异步更新订单备案信息错误!");
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
	public Object getMpayRecordInfo(String merchantId, String merchantName, Map<String, Object> params, int page,
			int size) {
		Map<String, Object> statusMap = new HashMap<>();
		params.put("merchant_no", merchantId);
		params.put("del_flag", 0);
		if (!"0".equals(params.get("pay_record_status") + "")) {
			params.put("pay_record_status", Integer.parseInt(params.get("pay_record_status") + ""));
		} else {
			params.remove("pay_record_status");
		}
		if(StringEmptyUtils.isNotEmpty(params.get("startTime")+"")){
			params.put("startTime", DateUtil.parseDate2(params.get("startTime")+""));
		}else if(StringEmptyUtils.isNotEmpty(params.get("endTime")+"")){
			params.put("endTime", DateUtil.parseDate2(params.get("endTime")+""));
		}
		List<Object> reList = paymentDao.findByPropertyLike(Mpay.class, params, null, page, size);
		long tatolCount = paymentDao.findByPropertyLikeCount(Mpay.class, params, null);
		if (reList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
		} else if (!reList.isEmpty()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), reList);
			statusMap.put(BaseCode.TOTALCOUNT.toString(), tatolCount);
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
		}
		return statusMap;
	}
}

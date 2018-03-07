package org.silver.shop.impl.system.cross;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.manual.Mpay;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantRecordInfo;
import org.silver.shop.model.system.tenant.MerchantWalletLog;
import org.silver.shop.task.GroupPaymentTask;
import org.silver.shop.task.PaymentRecordTask;
import org.silver.shop.util.BufferUtils;
import org.silver.shop.util.InvokeTaskUtils;
import org.silver.shop.util.MerchantUtils;
import org.silver.shop.util.RedisInfoUtils;
import org.silver.shop.util.SearchUtils;
import org.silver.util.CalculateCpuUtils;
import org.silver.util.DateUtil;
import org.silver.util.RandomUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.SplitListUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service(interfaceClass = PaymentService.class)
public class PaymentServiceImpl implements PaymentService {

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
		Map<String, Object> merchantRecordMap = merchantUtils.getMerchantRecordInfo(merchantId, eport);
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
		// 获取流水号
		String serialNo = "paymentRecord_" + SerialNoUtils.getSerialNo("paymentRecord");
		// 总数
		int totalCount = jsonList.size();
		Map<String, Object> params = new HashMap<>();
		params.put("merchantId", merchantId);
		params.put("merchantName", merchantName);
		params.put("tok", tok);
		params.put("serialNo", serialNo);
		Map<String, Object> reMap = invokeTaskUtils.commonInvokeTask(3, totalCount, jsonList, errorList, recordMap,
				params);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		// startSendPaymentRecord(jsonList, merchantId, merchantName, errorList,
		// recordMap, tok, totalCount, serialNo);
		statusMap.clear();
		statusMap.put("status", 1);
		statusMap.put("msg", "执行成功,开始推送支付单备案.......");
		statusMap.put("serialNo", serialNo);
		return statusMap;
	}

	/**
	 * 开始发起支付单推送
	 * 
	 * @param jsonList
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param errorList
	 *            错误信息
	 * @param recordMap
	 *            商户备案信息
	 * @param tok
	 * @param totalCount
	 *            总数
	 * @param serialNo
	 *            批次号
	 */
	public final void startSendPaymentRecord(JSONArray jsonList, String merchantId, String merchantName,
			List<Map<String, Object>> errorList, Map<String, Object> recordMap, String tok, int totalCount,
			String serialNo) {
		Map<String, Object> param = new HashMap<>();
		double cumulativeAmount = 0.0;
		for (int i = 0; i < jsonList.size(); i++) {
			Map<String, Object> treadeMap = (Map<String, Object>) jsonList.get(i);
			String treadeNo = treadeMap.get("treadeNo") + "";
			try {
				param.clear();
				param.put("merchant_no", merchantId);
				param.put("trade_no", treadeNo);
				List<Mpay> payList = paymentDao.findByProperty(Mpay.class, param, 1, 1);
				if (payList != null && !payList.isEmpty()) {
					Mpay payInfo = payList.get(0);
					/*
					 * if (payInfo.getPay_record_status() == 2) { Map<String,
					 * Object> errMap = new HashMap<>();
					 * errMap.put(BaseCode.MSG.toString(), "支付流水号[" + treadeNo +
					 * "]正在备案中无需再次发起!"); errorList.add(errMap); continue; } else
					 * if (payInfo.getPay_record_status() == 3) { Map<String,
					 * Object> errMap = new HashMap<>();
					 * errMap.put(BaseCode.MSG.toString(), "支付流水号[" + treadeNo +
					 * "]已备案成功无需再次发起!"); errorList.add(errMap); continue; }
					 */
					double payAmount = payInfo.getPay_amount();
					// 将每个支付单总额进行累加,计算当前金额下是否有足够的余额支付费用
					cumulativeAmount = payAmount += cumulativeAmount;
					Map<String, Object> checkMap = mpayServiceImpl.checkWallet(1, merchantId, merchantName, treadeNo,
							cumulativeAmount);
					if (!"1".equals(checkMap.get(BaseCode.STATUS.toString()))) {
						String msg = "支付流水号[" + treadeNo + "]推送失败,钱包余额不足!";
						RedisInfoUtils.commonErrorInfo(msg, errorList, totalCount, serialNo, "paymentRecord", 1);
						continue;
					}
					Map<String, Object> paymentInfoMap = new HashMap<>();
					paymentInfoMap.put("EntPayNo", payInfo.getTrade_no());
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
					Map<String, Object> paymentMap = ysPayReceiveServiceImpl.sendPayment(merchantId, paymentInfoMap,
							tok, recordMap, YmMallConfig.MANUALPAYMENTNOTIFYURL);
					if (!"1".equals(paymentMap.get(BaseCode.STATUS.toString()) + "")) {
						String msg = "支付流水号[" + treadeNo + "]-->" + paymentMap.get(BaseCode.MSG.toString());
						RedisInfoUtils.commonErrorInfo(msg, errorList, totalCount, serialNo, "paymentRecord", 1);
						continue;
					}
					String rePayMessageID = paymentMap.get("messageID") + "";
					// 更新服务器返回支付Id
					Map<String, Object> rePaymentMap2 = updatePaymentInfo(treadeNo, rePayMessageID);
					if (!"1".equals(rePaymentMap2.get(BaseCode.STATUS.toString()) + "")) {
						String msg = "支付流水号[" + treadeNo + "]-->" + rePaymentMap2.get(BaseCode.MSG.toString());
						RedisInfoUtils.commonErrorInfo(msg, errorList, totalCount, serialNo, "paymentRecord", 1);
						continue;
					}
				} else {
					String msg = "支付流水号[" + treadeNo + "]查询支付单信息失败,请核实流水号!";
					RedisInfoUtils.commonErrorInfo(msg, errorList, totalCount, serialNo, "paymentRecord", 1);
					continue;
				}
				bufferUtils.writeRedis(errorList, totalCount, serialNo, "paymentRecord");
			} catch (Exception e) {
				e.printStackTrace();
				String msg = "[" + treadeNo + "]支付单推送失败,系統繁忙!";
				RedisInfoUtils.commonErrorInfo(msg, errorList, totalCount, serialNo, "paymentRecord", 1);
			}
		}
		bufferUtils.writeCompletedRedis(errorList, totalCount, serialNo, "paymentRecord", merchantId, merchantName);
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
			paramMap.put(BaseCode.MSG.toString(), "更新支付单返回messageID错误,未找到支付流水号！");
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
				statusMap.put(BaseCode.MSG.toString(), "异步更新支付单信息错误!");
				return statusMap;
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
		Map<String, Object> reDatasMap = SearchUtils.universalSearch(params);
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		paramMap.put("merchant_no", merchantId);
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
	public Map<String, Object> getMerchantPaymentReport(String merchantId, String merchantName, int page, int size,
			String startDate, String endDate) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramsMap = new HashMap<>();
		if (page >= 0 && size >= 0) {
			if(StringEmptyUtils.isNotEmpty(merchantId)){
				paramsMap.put("merchantId", merchantId);
			}
			if(StringEmptyUtils.isNotEmpty(merchantName)){
				paramsMap.put("merchantName", merchantName);
			}
			paramsMap.put("startDate", startDate);
			paramsMap.put("endDate", endDate);
			Table reList = paymentDao.getPaymentReport(Morder.class, paramsMap, page, size);
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
		return ReturnInfoUtils.errorInfo("请求参数出错,请核实信息!");
	}

	@Override
	public final Map<String, Object> groupCreateMpay(String merchantId, List<String> orderIDs, String serialNo,
			int realRowCount, List<Map<String, Object>> errorList) {
		Map<String, Object> statusMap = new HashMap<>();
		String merchantName = "";
		if (merchantId != null && orderIDs != null && orderIDs.size() > 0) {
			for (String order_id : orderIDs) {
				Map<String, Object> params = new HashMap<>();
				params.put("merchant_no", merchantId);
				params.put("order_id", order_id);
				List<Morder> morder = paymentDao.findByProperty(Morder.class, params, 1, 1);
				if (morder != null && morder.size() > 0) {
					merchantName = morder.get(0).getCreate_by();
					params.clear();
					params.put("morder_id", order_id);
					List<Mpay> mpayl = paymentDao.findByProperty(Mpay.class, params, 1, 1);
					if (mpayl != null && !mpayl.isEmpty()) {
						String msg = "订单号[" + order_id + "]关联的支付单已经存在,无需重复生成!";
						RedisInfoUtils.commonErrorInfo(msg, errorList, realRowCount, serialNo, "createPaymentId", 1);
						continue;
					}
					//
					params.clear();
					int count = SerialNoUtils.getRedisIdCount("paymentId");
					String trade_no = createTradeNo("01O", (count + 1), new Date());
					String orderDate = morder.get(0).getOrderDate();
					Date pay_time = DateUtil.randomPaymentDate(orderDate);

					if (addEntity(merchantId, trade_no, order_id, morder.get(0).getActualAmountPaid(),
							morder.get(0).getOrderDocName(), morder.get(0).getOrderDocId(),
							morder.get(0).getOrderDocTel(), pay_time, morder.get(0).getCreate_by())
							&& updateOrderPayNo(merchantId, order_id, trade_no)) {
						// 当创建完支付流水号之后
						bufferUtils.writeRedis(errorList, realRowCount, serialNo, "createPaymentId");
						continue;
					}
					String msg = "订单号[" + order_id + "]生成支付单失败,请稍后重试!";
					RedisInfoUtils.commonErrorInfo(msg, errorList, realRowCount, serialNo, "createPaymentId", 1);
					continue;
				}
				String msg = "订单号[" + order_id + "]不存在,请核对信息!";
				RedisInfoUtils.commonErrorInfo(msg, errorList, realRowCount, serialNo, "createPaymentId", 1);
			}
			bufferUtils.writeCompletedRedis(errorList, realRowCount, serialNo, "createPaymentId", merchantId,
					merchantName);
			return null;
		} else {
			statusMap.put("status", -3);
			statusMap.put("msg", "非法请求");
			return statusMap;
		}
	}

	/**
	 * 保存支付单实体
	 * 
	 * @param merchant_no
	 * @param trade_no
	 * @param morder_id
	 * @param amount
	 * @param payer_name
	 * @param payer_document_number
	 * @param payer_phone_number
	 * @param pay_time
	 * @param merchantName
	 * @return
	 */
	private boolean addEntity(String merchant_no, String trade_no, String morder_id, double amount, String payer_name,
			String payer_document_number, String payer_phone_number, Date pay_time, String merchantName) {
		Mpay entity = new Mpay();
		entity.setMerchant_no(merchant_no);
		entity.setTrade_no(trade_no);
		entity.setMorder_id(morder_id);
		entity.setPay_amount(amount);
		entity.setPayer_name(payer_name);
		entity.setPayer_document_type("01");
		entity.setPayer_document_number(payer_document_number);
		entity.setPayer_phone_number(payer_phone_number);
		entity.setTrade_status("TRADE_SUCCESS");
		entity.setDel_flag(0);
		entity.setCreate_date(new Date());
		entity.setYear(DateUtil.formatDate(new Date(), "yyyy"));
		entity.setPay_status("D");
		entity.setPay_currCode("142");
		entity.setPay_record_status(1);
		entity.setPay_time(pay_time);
		entity.setCreate_by(merchantName);

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
		param.put("merchant_no", merchantId);
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
	 * @param id
	 * @param d
	 * @return
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
		params.put("merchantId", merchantId);
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
	public Map<String, Object> managerGetPaymentReport(int page, int size, String startDate, String endDate,
			 String merchantName) {
		return getMerchantPaymentReport(null, merchantName, page, size, startDate, endDate);
	}

	@Override
	public Map<String, Object> managerGetMpayInfo(Map<String, Object> params, int page, int size) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> reDatasMap = SearchUtils.universalSearch(params);
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
	//	paramMap.put("del_flag", 0);
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
	public Map<String, Object> managerEditMpayInfo(JSONObject json) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) {
		System.out.println("GAC_20180002015199758417456482".length());
	}
}

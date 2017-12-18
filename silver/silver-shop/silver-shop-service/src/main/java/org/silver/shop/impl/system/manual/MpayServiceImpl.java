package org.silver.shop.impl.system.manual;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import org.silver.shop.impl.system.cross.YsPayReceiveServiceImpl;
import org.silver.shop.impl.system.tenant.MerchantWalletServiceImpl;
import org.silver.shop.model.system.commerce.OrderRecordContent;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.manual.MorderSub;
import org.silver.shop.model.system.manual.Mpay;
import org.silver.shop.model.system.organization.Proxy;
import org.silver.shop.model.system.tenant.MerchantRecordInfo;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.shop.model.system.tenant.ProxyWalletContent;
import org.silver.util.MD5;
import org.silver.util.StringEmptyUtils;
import org.silver.util.YmHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.StringUtil;

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
	private YsPayReceiveServiceImpl ysPayReceiveServiceImpl;
	@Autowired
	private AccessTokenService accessTokenService;
	@Autowired
	private GoodsRecordServiceImpl goodsRecordServiceImpl;
	@Autowired
	private MerchantWalletServiceImpl merchantWalletServiceImpl;

	@Autowired
	private WalletLogService walletLogService;

	@Override
	public Map<String, Object> groupCreateMpay(String merchantId, List<String> orderIDs) {
		Map<String, Object> statusMap = new HashMap<>();
		List<Map<String, Object>> errorList = new ArrayList<>();
		if (merchantId != null && orderIDs != null && orderIDs.size() > 0) {
			for (String order_id : orderIDs) {
				Map<String, Object> params = new HashMap<>();
				params.put("merchant_no", merchantId);
				params.put("order_id", order_id);
				List<Morder> morder = morderDao.findByProperty(Morder.class, params, 1, 1);
				if (morder != null && morder.size() > 0) {
					params.clear();
					params.put("morder_id", order_id);
					List<Mpay> mpayl = mpayDao.findByProperty(params, 1, 1);
					if (mpayl != null && mpayl.size() > 0) {
						Map<String, Object> error = new HashMap<>();
						error.put("status", -4);
						error.put("msg", "订单【" + order_id + "】" + "关联的支付单已经存在，不需要重复生成");
						errorList.add(error);
						continue;
					}
					params.clear();
					long count = mpayDao.findByPropertyCount(params);
					if (count < 0) {
						Map<String, Object> error = new HashMap<>();
						error.put("status", -6);
						error.put("msg", "系统内部错误，生成支付失败");
						errorList.add(error);
						continue;
					}
					String trade_no = createTradeNo("01O", (count + 1), new Date());
					if (addEntity(merchantId, trade_no, order_id, morder.get(0).getActualAmountPaid(),
							morder.get(0).getOrderDocName(), morder.get(0).getOrderDocId(),
							morder.get(0).getOrderDocTel()) && updateOrderPayNo(merchantId, order_id, trade_no)) {
						// 当创建完支付流水号之后
						continue;
					}
					Map<String, Object> error = new HashMap<>();
					statusMap.put("status", -1);
					statusMap.put("msg", order_id + "生成支付单出错，请稍后重试");
					errorList.add(error);
					continue;
				}
				Map<String, Object> error = new HashMap<>();
				error.put("status", -2);
				error.put("msg", order_id + "不存在的订单信息");
				errorList.add(error);
				continue;
			}
		} else {
			statusMap.put("status", -3);
			statusMap.put("msg", "非法请求");
			return statusMap;
		}
		statusMap.put("status", 1);
		statusMap.put("msg", "支付单生成成功");
		statusMap.put(BaseCode.ERROR.toString(), errorList);
		return statusMap;
	}

	private String createTradeNo(String sign, long id, Date d) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		Random r = new Random();
		String dstr = sdf.format(d);
		String nstr = id + "";
		String rstr = r.nextInt(10000) + "";
		while (nstr.length() < 5) {
			nstr = "0" + nstr;
		}
		while (rstr.length() < 4) {
			rstr = "0" + rstr;
		}
		return sign + dstr + nstr + rstr;

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
		List<Morder> reList = morderDao.findByProperty(Morder.class, param, 1, 1);
		if (reList != null && !reList.isEmpty()) {
			Morder entity = reList.get(0);
			entity.setTrade_no(trade_no);
			entity.setUpdate_date(new Date());
			return morderDao.update(entity);
		}
		return false;
	}

	private boolean addEntity(String merchant_no, String trade_no, String morder_id, double amount, String payer_name,
			String payer_document_number, String payer_phone_number) {
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
		entity.setYear("year");
		entity.setPay_status("D");
		entity.setPay_currCode("142");
		return mpayDao.add(entity);

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
		for (int i = 0; i < jsonList.size(); i++) {
			Map<String, Object> treadeMap = (Map<String, Object>) jsonList.get(i);
			String treadeNo = treadeMap.get("treadeNo") + "";
			param.clear();
			param.put("merchant_no", merchantId);
			param.put("trade_no", treadeNo);
			List<Mpay> payList = morderDao.findByProperty(Mpay.class, param, 1, 1);
			if (payList != null && !payList.isEmpty()) {
				Mpay payInfo = payList.get(0);
				Map<String, Object> checkMap = checkWallet(1, merchantId, merchantName, treadeNo,
						payInfo.getPay_amount());
				if (!"1".equals(checkMap.get(BaseCode.STATUS.toString()))) {
					return checkMap;
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

				Map<String, Object> reUpdateWalletMap = updateWallet(1, merchantId, merchantName, treadeNo,
						proxyParentId, payInfo.getPay_amount(), proxyParentName);
				if (!"1".equals(reUpdateWalletMap.get(BaseCode.STATUS.toString()))) {
					return reUpdateWalletMap;
				}
				String rePayMessageID = paymentMap.get("messageID") + "";
				// 更新服务器返回支付Id
				Map<String, Object> rePaymentMap2 = updatePaymentInfo(treadeNo, rePayMessageID);
				if (!"1".equals(rePaymentMap2.get(BaseCode.STATUS.toString()) + "")) {
					return rePaymentMap2;
				}
			}
			Map<String, Object> errorMap = new HashMap<>();
			errorMap.put(BaseCode.MSG.toString(), "[" + treadeNo + "]该支付流水号不存在,请核实！");
			errorList.add(errorMap);
		}
		statusMap.clear();
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

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
	private Map<String, Object> checkWallet(int type, String merchantId, String merchantName, String serialNo,
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
	 * @param proxyParentName
	 *            代理商名称
	 * @param payAmount
	 *            推送单总金额
	 * @return Map
	 */
	private Map<String, Object> updateWallet(int type, String merchantId, String merchantName, String serialNo,
			String proxyId, double payAmount, String proxyParentName) {
		Map<String, Object> merchantWalletMap = saveMerchantWalletLog(type, merchantId, merchantName, proxyId,
				proxyParentName, serialNo, payAmount);
		if (!"1".equals(merchantWalletMap.get(BaseCode.STATUS.toString()) + "")) {
			return merchantWalletMap;
		}
		double serviceFee = Double.parseDouble(merchantWalletMap.get("serviceFee") + "");
		return saveProxyWalletLog(type, merchantId, merchantName, proxyId, proxyParentName, serialNo, serviceFee);
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
		proxyWallet.setBalance(serviceFee);
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
	 * @param proxyId 代理商Id
	 * @param proxyParentName 代理商名称
	 * @param serialNo 流水号
	 * @param payAmount 推送单金额
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

	// 更新支付单返回信息
	private Map<String, Object> updatePaymentInfo(String entPayNo, String rePayMessageID) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("trade_no", entPayNo);
		List<Mpay> reList = morderDao.findByProperty(Mpay.class, paramMap, 0, 0);
		if (reList != null && !reList.isEmpty()) {
			for (int i = 0; i < reList.size(); i++) {
				Mpay payment = reList.get(i);
				payment.setPay_serial_no(rePayMessageID);
				// 备案状态：1-未备案,2-备案中,3-备案成功、4-备案失败
				payment.setPay_record_status(2);
				payment.setUpdate_date(new Date());
				if (!morderDao.update(payment)) {
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
	public Object sendMorderRecord(String merchantId, Map<String, Object> recordMap, String orderNoPack,
			String proxyParentId, String merchantName, String proxyParentName) {
		Map<String, Object> statusMap = new HashMap<>();
		List<Map<String, Object>> errorList = new ArrayList<>();
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(orderNoPack);
		} catch (Exception e) {
			statusMap.put(BaseCode.MSG.toString(), "订单编号错误,请核实！");
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
		for (int i = 0; i < jsonList.size(); i++) {
			Map<String, Object> orderMap = (Map<String, Object>) jsonList.get(i);
			String orderNo = orderMap.get("orderNo") + "";
			Map<String, Object> param = new HashMap<>();
			param.put("merchant_no", merchantId);
			param.put("order_id", orderNo);
			List<Morder> orderList = morderDao.findByProperty(Morder.class, param, 1, 1);
			param.clear();
			param.put("order_id", orderNo);
			List<MorderSub> orderSubList = morderDao.findByProperty(MorderSub.class, param, 0, 0);
			if (orderList == null || orderSubList == null) {
				Map<String, Object> errMap = new HashMap<>();
				errMap.put(BaseCode.STATUS.toString(), "[" + orderNo + "]订单查询失败,服务器繁忙!");
				errorList.add(errMap);
			} else {
				Morder order = orderList.get(0);
				Map<String, Object> checkMap = checkWallet(2, merchantId, merchantName, orderNo, order.getFCY());
				if (!"1".equals(checkMap.get(BaseCode.STATUS.toString()))) {
					return checkMap;
				}
				Map<String, Object> reOrderMap = sendOrder(merchantId, recordMap, orderSubList, tok, order);
				Map<String, Object> reUpdateWalletMap = updateWallet(2, merchantId, merchantName, orderNo,
						proxyParentId, order.getFCY(), proxyParentName);
				if (!"1".equals(reUpdateWalletMap.get(BaseCode.STATUS.toString()))) {
					return reUpdateWalletMap;
				}
				String reOrderMessageID = reOrderMap.get("messageID") + "";
				// 更新服务器返回订单Id
				Map<String, Object> reOrderMap2 = updateOrderInfo(orderNo, reOrderMessageID);
				if (!"1".equals(reOrderMap2.get(BaseCode.STATUS.toString()) + "")) {
					return reOrderMap2;
				}
			}
		}
		statusMap.clear();
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

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
		for (int i = 0; i < orderSubList.size(); i++) {
			goodsJson = new JSONObject();
			MorderSub goodsInfo = orderSubList.get(i);
			goodsJson.element("Seq", goodsInfo.getSeq());
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
		orderJson.element("RecipientCityCode", order.getRecipientProvincesCode());
		orderJson.element("RecipientAreaCode", order.getRecipientProvincesCode());
		orderJson.element("OrderDocAcount", order.getOrderDocAcount());
		orderJson.element("OrderDocName", order.getOrderDocName());
		orderJson.element("OrderDocType", order.getOrderDocType());
		orderJson.element("OrderDocId", order.getOrderDocId());
		orderJson.element("OrderDocTel", order.getOrderDocTel());
		orderJson.element("OrderDate", order.getCreate_date());
		orderJson.element("entPayNo", order.getTrade_no());
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
		// 电商企业编号
		orderMap.put("ebEntNo", recordMap.get("ebEntNo"));
		// 电商企业名称
		orderMap.put("ebEntName", recordMap.get("ebEntName"));
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
		String resultStr = YmHttpUtil.HttpPost("http://ym.191ec.com/silver-web/Eport/Report", orderMap);
		// 当端口号为2(智检时)再往电子口岸多发送一次
		if (eport == 2) {
			Map<String, Object> paramsMap = new HashMap<>();
			paramsMap.put("merchantId", merchantId);
			paramsMap.put("customsPort", 1);
			List<Object> reMerchantList = morderDao.findByProperty(MerchantRecordInfo.class, paramsMap, 1, 1);
			MerchantRecordInfo merchantRecordInfo = (MerchantRecordInfo) reMerchantList.get(0);
			// 1:广州电子口岸(目前只支持BC业务) 2:南沙智检(支持BBC业务)
			orderMap.put("eport", 1);
			// 电商企业编号
			orderMap.put("ebEntNo", merchantRecordInfo.getEbEntNo());
			// 电商企业名称
			orderMap.put("ebEntName", merchantRecordInfo.getEbEntName());
			String resultStr2 = YmHttpUtil.HttpPost("http://ym.191ec.com/silver-web/Eport/Report", orderMap);
			if (StringEmptyUtils.isNotEmpty(resultStr2)) {
				JSONObject.fromObject(resultStr);
			} else {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "服务器接受信息失败,服务器繁忙！");
				return statusMap;
			}

		}
		if (StringUtil.isNotEmpty(resultStr)) {
			return JSONObject.fromObject(resultStr);
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "服务器接受信息失败,服务器繁忙！");
			return statusMap;
		}
	}

	@Override
	public Map<String, Object> updateOrderRecordInfo(Map<String, Object> datasMap) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置时间格式
		String defaultDate = sdf.format(date); // 格式化当前时间
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("order_serial_no", datasMap.get("messageID") + "");
		String reMsg = datasMap.get("errMsg") + "";
		List<Morder> reList = morderDao.findByProperty(Morder.class, paramMap, 0, 0);
		if (reList != null && reList.size() > 0) {
			Morder order = reList.get(0);
			String status = datasMap.get("status") + "";
			String note = order.getOrder_re_note();
			if ("null".equals(note) || note == null) {
				note = "";
			}
			if ("1".equals(status)) {
				// 支付单备案状态修改为成功
				order.setOrder_record_status(3);
			} else {
				// 备案失败
				order.setOrder_record_status(4);
			}
			order.setOrder_re_note(note + defaultDate + ":" + reMsg + ";");
			order.setUpdate_date(new Date());
			if (!morderDao.update(order)) {
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
	public Map<String, Object> updatePayRecordInfo(Map<String, Object> datasMap) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置时间格式
		String defaultDate = sdf.format(date); // 格式化当前时间
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("pay_serial_no", datasMap.get("messageID") + "");
		String reMsg = datasMap.get("errMsg") + "";
		List<Mpay> reList = morderDao.findByProperty(Mpay.class, paramMap, 0, 0);
		if (reList != null && reList.size() > 0) {
			Mpay pay = reList.get(0);
			String status = datasMap.get("status") + "";
			String note = pay.getPay_re_note();
			if ("null".equals(note) || note == null) {
				note = "";
			}
			if ("1".equals(status)) {
				// 支付单备案状态修改为成功
				pay.setPay_record_status(3);
			} else {
				// 备案失败
				pay.setPay_record_status(4);
			}
			pay.setPay_re_note(note + defaultDate + ":" + reMsg + ";");
			pay.setUpdate_date(new Date());
			if (!morderDao.update(pay)) {
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
}

package org.silver.shop.impl.system.cross;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.AccessTokenService;
import org.silver.shop.api.system.cross.YsPayReceiveService;
import org.silver.shop.api.system.log.MemberWalletLogService;
import org.silver.shop.api.system.log.MerchantWalletLogService;
import org.silver.shop.api.system.log.TradeReceiptLogService;
import org.silver.shop.api.system.organization.MemberService;
import org.silver.shop.api.system.tenant.MemberWalletService;
import org.silver.shop.api.system.tenant.MerchantWalletService;
import org.silver.shop.config.YmMallConfig;
import org.silver.shop.dao.system.cross.YsPayReceiveDao;
import org.silver.shop.model.system.commerce.GoodsRecord;
import org.silver.shop.model.system.commerce.GoodsRecordDetail;
import org.silver.shop.model.system.commerce.OrderContent;
import org.silver.shop.model.system.commerce.OrderGoodsContent;
import org.silver.shop.model.system.commerce.OrderRecordContent;
import org.silver.shop.model.system.commerce.OrderRecordGoodsContent;
import org.silver.shop.model.system.commerce.StockContent;
import org.silver.shop.model.system.cross.PaymentContent;
import org.silver.shop.model.system.log.AgentWalletLog;
import org.silver.shop.model.system.log.TradeReceiptLog;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.AgentWalletContent;
import org.silver.shop.model.system.tenant.MemberWalletContent;
import org.silver.shop.model.system.tenant.MerchantRecordInfo;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.shop.util.MerchantUtils;
import org.silver.shop.util.WalletUtils;
import org.silver.util.DateUtil;
import org.silver.util.DoubleOperationUtil;
import org.silver.util.MD5;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SendMsg;
import org.silver.util.SerialNoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.util.YmHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.StringUtil;
import net.sf.json.JSONObject;

@Service(interfaceClass = YsPayReceiveService.class)
public class YsPayReceiveServiceImpl implements YsPayReceiveService {

	protected static final Logger logger = LogManager.getLogger();
	/**
	 * 进出境标志I-进，E-出
	 */
	private static final String IEFLAG = "I";

	/**
	 * 币制默认为人民币
	 */
	private static final String CURR_CODE = "142";

	@Autowired
	private YsPayReceiveDao ysPayReceiveDao;
	@Autowired
	private AccessTokenService accessTokenService;
	@Autowired
	private MerchantWalletLogService merchantWalletLogService;
	@Autowired
	private MemberService memberService;
	@Autowired
	private MerchantUtils merchantUtils;
	@Autowired
	private WalletUtils walletUtils;
	@Autowired
	private MemberWalletService memberWalletService;
	@Autowired
	private TradeReceiptLogService tradeReceiptLogService;
	@Autowired
	private MemberWalletLogService memberWalletLogService;
	@Autowired
	private MerchantWalletService merchantWalletService;

	@Override
	public Map<String, Object> ysPayReceive(Map<String, Object> datasMap) {
		Map<String, Object> params = new HashMap<>();
		Member memberInfo = null;
		String reEntOrderNo = datasMap.get("out_trade_no") + "";
		// 根据订单ID查询订单是否存在
		params.put("entOrderNo", reEntOrderNo);
		List<OrderContent> orderList = ysPayReceiveDao.findByProperty(OrderContent.class, params, 0, 0);
		List<OrderGoodsContent> orderGoodsList = ysPayReceiveDao.findByProperty(OrderGoodsContent.class, params, 0, 0);
		if (orderList == null || orderGoodsList == null) {
			return ReturnInfoUtils.errorInfo("查询订单失败,服务器繁忙!");
		} else if (!orderList.isEmpty() && !orderGoodsList.isEmpty()) {
			try {
				OrderContent orderInfo = orderList.get(0);
				String merchantId = orderInfo.getMerchantId();
				Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(merchantId);
				if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
					return reMerchantMap;
				}
				Merchant merchantInfo = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
				// 根据用户ID查询用户是否存在
				Map<String, Object> reMemberMap = memberService.getMemberInfo(orderInfo.getMemberId());
				if (!"1".equals(reMemberMap.get(BaseCode.STATUS.toString()))) {
					return reMemberMap;
				}
				memberInfo = (Member) reMemberMap.get(BaseCode.DATAS.toString());
				// 保存支付单信息
				Map<String, Object> rePaymentMap = addPaymentInfo(orderList, datasMap, memberInfo);
				if (!"1".equals(rePaymentMap.get(BaseCode.STATUS.toString()))) {
					System.out.println("--保存支付单信息-->" + rePaymentMap.get(BaseCode.MSG.toString()));
					return rePaymentMap;
				}
				PaymentContent paymentInfo = (PaymentContent) rePaymentMap.get(BaseCode.DATAS.toString());
				SendMsg.sendMsg(merchantInfo.getMerchantPhone(), "【广州银盟】您有一个订单需要处理,订单号：" + reEntOrderNo);
				// 商户钱包资金更新
				Map<String, Object> reWalletMap = updateWalletFunds(orderInfo, paymentInfo.getPayAmount());
				if (!"1".equals(reWalletMap.get(BaseCode.STATUS.toString()))) {
					return reWalletMap;
				}
				Map<String, Object> paymentInfoMap = new HashMap<>();
				String entPayNo = paymentInfo.getEntPayNo();
				paymentInfoMap.put("EntPayNo", entPayNo);
				paymentInfoMap.put("PayStatus", paymentInfo.getPayStatus());
				paymentInfoMap.put("PayAmount", paymentInfo.getPayAmount());
				paymentInfoMap.put("PayCurrCode", paymentInfo.getPayCurrCode());
				paymentInfoMap.put("PayTime", paymentInfo.getPayTime());
				paymentInfoMap.put("PayerName", paymentInfo.getPayerName());
				paymentInfoMap.put("PayerDocumentType", paymentInfo.getPayerDocumentType());
				paymentInfoMap.put("PayerDocumentNumber", paymentInfo.getPayerDocumentNumber());
				paymentInfoMap.put("PayerPhoneNumber", paymentInfo.getPayerPhoneNumber());
				paymentInfoMap.put("EntOrderNo", paymentInfo.getEntOrderNo());
				paymentInfoMap.put("Notes", paymentInfo.getEntPayNo());
				// 保存备案订单信息
				Map<String, Object> reOrderRecordMap = addOrderRecordInfo(orderInfo, datasMap, memberInfo, orderList,
						orderGoodsList);
				if (!"1".equals(reOrderRecordMap.get(BaseCode.STATUS.toString()))) {
					return reOrderRecordMap;
				}
				// 获取返回的订单备案实体实体
				OrderRecordContent orderRecordInfo = (OrderRecordContent) reOrderRecordMap
						.get(BaseCode.DATAS.toString());
				// 获取订单Id
				String orderId = orderInfo.getEntOrderNo();
				// 获取订单商品信息及备案头与备案商品信息
				Map<String, Object> reMap = findOrderAndGoodsRecordInfo(orderId);
				if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
					return reMap;
				}
				// 商品备案头信息
				GoodsRecord goodsRecordInfo = (GoodsRecord) reMap.get("goodsRecordInfo");
				// 商品备案详情
				List<Object> reGoodsRecordDetailList = (List<Object>) reMap.get("reGoodsRecordDetailList");
				// 订单商品
				List<Object> reOrderGoodsList = (List<Object>) reMap.get("reOrderGoodsList");
				// 更新订单状态及修改库存上架数量
				Map<String, Object> reStockMap = updataOrderAndStockStatus(reOrderGoodsList);
				if (!reStockMap.get(BaseCode.STATUS.toString()).equals("1")) {
					return reStockMap;
				}
				// 保存订单关联商品信息
				Map<String, Object> reOrderRecordGoodsMap = addOrderRecordGoodsInfo(reGoodsRecordDetailList,
						reOrderGoodsList);
				if (!reOrderRecordGoodsMap.get(BaseCode.STATUS.toString()).equals("1")) {
					return reOrderRecordGoodsMap;
				}
				// 请求获取tok
				// 4a5de70025a7425dabeef6e8ea752976
				Map<String, Object> reTokMap = accessTokenService.getRedisToks(YmMallConfig.APPKEY,
						YmMallConfig.APPSECRET);
				if (!"1".equals(reTokMap.get(BaseCode.STATUS.toString()) + "")) {
					return reTokMap;
				}

				String tok = reTokMap.get(BaseCode.DATAS.toString()) + "";

				Map<String, Object> recordMap = new HashMap<>();
				recordMap.put("ebpEntNo", goodsRecordInfo.getEbpEntNo());
				recordMap.put("ebpEntName", goodsRecordInfo.getEbpEntName());
				// 电商企业编号
				recordMap.put("ebEntNo", goodsRecordInfo.getEbEntNo());
				// 电商企业名称
				recordMap.put("ebEntName", goodsRecordInfo.getEbEntName());
				recordMap.put("ciqOrgCode", goodsRecordInfo.getCiqOrgCode());
				recordMap.put("customsCode", goodsRecordInfo.getCustomsCode());
				recordMap.put("eport", goodsRecordInfo.getCustomsPort());
				// 4a5de70025a7425dabeef6e8ea752976
				recordMap.put("appkey", YmMallConfig.APPKEY);
				// 发送支付单备案
				Map<String, Object> rePayMap = sendPayment(merchantId, paymentInfoMap, tok, recordMap,
						YmMallConfig.PAYMENT_NOTIFY_URL);
				if (!"1".equals(rePayMap.get(BaseCode.STATUS.toString()) + "")) {
					return rePayMap;
				}
				String rePayMessageID = rePayMap.get("messageID") + "";
				// 更新服务器返回支付Id
				Map<String, Object> rePaymentMap2 = updatePaymentInfo(entPayNo, rePayMessageID);
				if (!"1".equals(rePaymentMap2.get(BaseCode.STATUS.toString()) + "")) {
					return rePaymentMap2;
				}
				// 发送订单备案
				Map<String, Object> reOrderMap = sendOrder(goodsRecordInfo, reGoodsRecordDetailList, reOrderGoodsList,
						tok, orderRecordInfo);
				if (!"1".equals(reOrderMap.get(BaseCode.STATUS.toString()) + "")) {
					return reOrderMap;
				}
				String reOrderMessageID = reOrderMap.get("messageID") + "";
				// 更新服务器返回订单Id
				return updateOrderInfo(orderRecordInfo, reOrderMessageID);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(Thread.currentThread().getName() + "--支付成功回调-->>", e);
			}
		}
		return ReturnInfoUtils.errorInfo("查询订单不存在,参数不正确!");
	}

	/**
	 * 支付成功回调后,更新商户钱包货款资金
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param goodsName
	 *            商品名称
	 * @param payAmount
	 *            交易金额
	 * @param memberId
	 *            用户Id
	 * @param entOrderNo
	 *            订单编号
	 * @param sourceFlag
	 *            来源标识：1-银盟商城、2-第三方推广
	 * @return Map
	 */
	private Map<String, Object> updateWalletFunds(OrderContent orderInfo, Double payAmount) {
		if (orderInfo == null) {
			return ReturnInfoUtils.errorInfo("更新钱包错误，订单信息不能为null");
		}
		// 获取商户钱包信息
		Map<String, Object> reMerchantWalletMap = walletUtils.checkWallet(1, orderInfo.getMerchantId(), null);
		if (!"1".equals(reMerchantWalletMap.get(BaseCode.STATUS.toString()))) {
			return reMerchantWalletMap;
		}
		MerchantWalletContent wallet = (MerchantWalletContent) reMerchantWalletMap.get(BaseCode.DATAS.toString());
		Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(wallet.getMerchantId());
		if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
			return reMerchantMap;
		}
		Merchant merchant = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
		double profit = merchant.getMerchantProfit();
		// 平台抽取佣金
		double commission = 0;
		//
		double cash = 0;
		// 原钱包资金
		double oldCash = wallet.getCash();
		// 来源标识：1-银盟商城、2-第三方推广
		if (orderInfo.getSourceFlag() == 2) {
			// 暂定手续费(佣金)九分之一
			commission = DoubleOperationUtil.div(payAmount, 9);
		} else {
			commission = DoubleOperationUtil.mul(payAmount, profit);
		}
		// 扣除平台佣金后商户所得资金
		cash = DoubleOperationUtil.sub(payAmount, commission);
		Map<String, Object> reWalletMap = updateWallet(wallet, cash);
		if (!"1".equals(reWalletMap.get(BaseCode.STATUS.toString()))) {
			return ReturnInfoUtils.errorInfo("交易金额存入商户钱包失败,服务器繁忙!");
		}
		Map<String, Object> datas = new HashMap<>();
		datas.put("merchantId", orderInfo.getMerchantId());
		datas.put("walletId", wallet.getWalletId());
		datas.put("merchantName", wallet.getMerchantName());
		datas.put("serialName", "购物");
		datas.put("balance", oldCash);
		datas.put("amount", cash);
		// 类型:1-佣金、2-充值、3-提现、4-缴费、5-购物、6-线上
		datas.put("type", 6);
		datas.put("flag", "in");
		// 获取用户钱包信息
		Map<String, Object> reMemeberWalletMap = walletUtils.checkWallet(2, orderInfo.getMemberId(), null);
		if (!"1".equals(reMemeberWalletMap.get(BaseCode.STATUS.toString()))) {
			return reMemeberWalletMap;
		}
		MemberWalletContent memberWallet = (MemberWalletContent) reMemeberWalletMap.get(BaseCode.DATAS.toString());
		datas.put("targetWalletId", memberWallet.getWalletId());
		datas.put("targetName", memberWallet.getMemberName());
		// 状态：success-交易成功、fail-交易失败
		datas.put("status", "success");
		datas.put("note", "订单号[" + orderInfo.getEntOrderNo() + "]用户购买 " + "购物" + " 支付了:" + payAmount + "元,商城平台抽取["
				+ commission + "]元佣金,剩余[" + cash + "]元存入钱包可用资金!");
		// 添加商户钱包流水日志
		Map<String, Object> reWalletLogMap = merchantWalletLogService.addWalletLog(datas);
		if (!"1".equals(reWalletLogMap.get(BaseCode.STATUS.toString()))) {
			return reWalletLogMap;
		}
		// 银盟总代理收款
		return updateAgentWallet(commission, "购物", payAmount, orderInfo.getEntOrderNo(), wallet.getWalletId(),
				wallet.getMerchantName());
	}

	/**
	 * 总代理商收取商户缴纳的佣金
	 * 
	 * @param commission
	 *            佣金
	 * @param goodsName
	 *            商品名称
	 * @param payAmount
	 *            实际支付金额
	 * @param entOrderNo
	 *            订单编号
	 * @param merchantWalletId
	 *            商户钱包Id
	 * @param merchantName
	 *            商户名称
	 * @return Map
	 */
	private Map<String, Object> updateAgentWallet(double commission, String goodsName, Double payAmount,
			String entOrderNo, String merchantWalletId, String merchantName) {
		Map<String, Object> reAgentWalletMap = walletUtils.checkWallet(3, "AgentId_00001", "");
		if (!"1".equals(reAgentWalletMap.get(BaseCode.STATUS.toString()))) {
			return reAgentWalletMap;
		}
		AgentWalletContent wallet = (AgentWalletContent) reAgentWalletMap.get(BaseCode.DATAS.toString());
		double oldCash = wallet.getCash();
		wallet.setCash(oldCash + commission);
		if (!ysPayReceiveDao.update(wallet)) {
			return ReturnInfoUtils.errorInfo("代理商收款失败!");
		}
		// 添加钱包日志
		AgentWalletLog walletLog = new AgentWalletLog();
		walletLog.setAgentWalletId(wallet.getWalletId());
		walletLog.setAgentName(wallet.getAgentName());
		int serial = SerialNoUtils.getSerialNo("logs");
		if (serial < 0) {
			return ReturnInfoUtils.errorInfo("查询流水号自增Id失败,服务器繁忙!");
		}
		walletLog.setSerialNo(SerialNoUtils.getSerialNo("L", serial));
		walletLog.setSerialName("商户支付平台佣金");
		walletLog.setBeforeChangingBalance(oldCash);
		walletLog.setAmount(commission);
		walletLog.setAfterChangeBalance(oldCash + commission);
		// 类型:1-佣金、2-充值、3-提现、4-缴费、5-购物
		walletLog.setType(1);
		// 状态：success-交易成功、fail-交易失败
		walletLog.setStatus("success");
		// 进出帐标识：in-进账,out-出账
		walletLog.setFlag("in");
		walletLog.setNote(
				"订单号[" + entOrderNo + "]用户购买 " + goodsName + " 支付了:" + payAmount + "元,商城平台抽取[" + commission + "]元佣金！");
		walletLog.setTargetWalletId(merchantWalletId);
		walletLog.setTargetName(merchantName);
		walletLog.setCreateDate(new Date());
		walletLog.setCreateBy("system");
		if (!ysPayReceiveDao.add(walletLog)) {
			return ReturnInfoUtils.errorInfo("保存代理商钱包日志失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}

	// 保存支付单信息
	private final Map<String, Object> addPaymentInfo(List orderList, Map<String, Object> datasMap, Member memberInfo) {
		Date date = new Date();
		Map<String, Object> paramMap = new HashMap<>();
		OrderContent orderInfo = (OrderContent) orderList.get(0);
		PaymentContent paymentInfo = new PaymentContent();
		String entPayNo = datasMap.get("trade_no") + "";
		paramMap.put("entPayNo", entPayNo);
		List<PaymentContent> rePayList = ysPayReceiveDao.findByProperty(PaymentContent.class, paramMap, 1, 1);
		if (rePayList == null) {
			return ReturnInfoUtils.errorInfo("查询支付订单是否存在时错误,服务器繁忙!");
		} else if (!rePayList.isEmpty()) {
			return ReturnInfoUtils.errorInfo("支付交易编号[" + entPayNo + "]已接收成功,无需重复保存!");
		} else {
			String memberId = orderInfo.getMemberId();
			String memberName = orderInfo.getMemberName();
			paymentInfo.setMemberId(memberId);
			paymentInfo.setMemberName(memberName);
			// 支付金额
			double payAmount = Double.parseDouble(datasMap.get("total_amount") + "");
			paymentInfo.setPayAmount(payAmount);
			// 支付流水号
			paymentInfo.setEntPayNo(entPayNo);
			paymentInfo.setPayStatus("D");
			// 默认为142-人名币
			paymentInfo.setPayCurrCode("142");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date reInvoiceDate = null;
			try {
				reInvoiceDate = sdf.parse(datasMap.get("notify_time") + "");
			} catch (ParseException e) {
				e.printStackTrace();
			}
			paymentInfo.setPayTime(reInvoiceDate);
			paymentInfo.setPayerName(memberInfo.getMemberIdCardName());
			// 支付人证件类型01:身份证02:护照04:其他
			paymentInfo.setPayerDocumentType(01);
			paymentInfo.setPayerDocumentNumber(memberInfo.getMemberIdCard());
			paymentInfo.setPayerPhoneNumber(memberInfo.getMemberTel());
			// 订单编号
			String entOrderNo = orderInfo.getEntOrderNo();
			paymentInfo.setEntOrderNo(entOrderNo);
			paymentInfo.setPayRecord(1);
			paymentInfo.setPayFalg(0);
			paymentInfo.setCreateBy(memberName);
			paymentInfo.setCreateDate(date);
			// 删除标识:0-未删除,1-已删除
			paymentInfo.setDeleteFlag(0);
			if (!ysPayReceiveDao.add(paymentInfo)) {
				return ReturnInfoUtils.errorInfo("保存支付单信息失败,服务器繁忙!");
			}
			return ReturnInfoUtils.successDataInfo(paymentInfo);
		}
	}

	/**
	 * 将支付金额存入到商户钱包中
	 * 
	 * @param wallet
	 *            钱包实体
	 * @param cash
	 *            入账现金
	 * @return Map
	 */
	private Map<String, Object> updateWallet(MerchantWalletContent wallet, double cash) {
		double oldCash = wallet.getCash();
		wallet.setCash(oldCash + cash);
		// 将支付金额存入到商户钱包中
		if (!ysPayReceiveDao.update(wallet)) {
			return ReturnInfoUtils.errorInfo("商户现金入账失败!");
		}
		return ReturnInfoUtils.successInfo();
	}

	// 保存订单备案信息
	private final Map<String, Object> addOrderRecordInfo(OrderContent orderInfo, Map<String, Object> datasMap,
			Member memberInfo, List orderList, List orderGoodsList) {
		Date date = new Date();
		Map<String, Object> paramMap = new HashMap<>();
		String entOrderNo = orderInfo.getEntOrderNo();
		paramMap.put("entOrderNo", entOrderNo);
		List<OrderRecordContent> reOrderList = ysPayReceiveDao.findByProperty(OrderRecordContent.class, paramMap, 1, 1);
		if (reOrderList == null) {
			return ReturnInfoUtils.errorInfo("订单号[" + entOrderNo + "]查询失败，服务器繁忙！");
		} else if (!reOrderList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reOrderList.get(0));
		} else {
			OrderRecordContent orderRecordInfo = new OrderRecordContent();
			orderRecordInfo.setMerchantId(orderInfo.getMerchantId());
			orderRecordInfo.setMerchantName(orderInfo.getMerchantName());
			orderRecordInfo.setMemberId(orderInfo.getMemberId());
			orderRecordInfo.setMemberName(orderInfo.getMemberName());
			orderRecordInfo.setEntOrderNo(orderInfo.getEntOrderNo());
			// 电子订单状态0-订单确认,1-订单完成,2-订单取消
			orderRecordInfo.setOrderStatus(1);
			orderRecordInfo.setPayStatus(0);
			// 商城订单总价
			double orderTotal = 0;
			// 订单商品总额
			double orderGoodTotal = 0;
			// 税费
			double tax = 0;
			orderInfo.getOrderTotalPrice();
			// 计算多个(商城基本)订单信息商品总价
			for (int i = 0; i < orderList.size(); i++) {
				OrderContent order = (OrderContent) orderList.get(i);
				orderTotal += order.getOrderTotalPrice();
			}
			// 根据订单金额减掉商品总价格得出(总)订单备案信息中税费
			for (int y = 0; y < orderGoodsList.size(); y++) {
				OrderGoodsContent orderGoodsInfo = (OrderGoodsContent) orderGoodsList.get(y);
				double goodstotalPrice = orderGoodsInfo.getGoodsTotalPrice();
				orderGoodTotal += goodstotalPrice;
				tax = orderTotal - goodstotalPrice;
			}
			orderRecordInfo.setOrderGoodTotal(orderGoodTotal);
			// 人民币
			orderRecordInfo.setOrderGoodTotalCurr("142");
			// 暂时填0
			orderRecordInfo.setFreight(0.0);
			orderRecordInfo.setTax(tax);
			orderRecordInfo.setOtherPayment(0.0);
			// 抵付说明抵付情况说明。如果填写抵付金额时，此项必填。
			orderRecordInfo.setOtherPayNotes("");
			orderRecordInfo.setOtherCharges(0.0);
			double actualAmountPaid = Double.parseDouble(datasMap.get("total_amount") + "");
			orderRecordInfo.setActualAmountPaid(actualAmountPaid);
			orderRecordInfo.setRecipientName(orderInfo.getRecipientName());
			orderRecordInfo.setRecipientCardId(orderInfo.getRecipientCardId());
			orderRecordInfo.setRecipientTel(orderInfo.getRecipientTel());
			// 将详细地址拼接省市区+详细地址
			String recipientAddr = orderInfo.getRecProvincesName() + orderInfo.getRecCityName()
					+ orderInfo.getRecAreaName() + orderInfo.getRecipientAddr();
			orderRecordInfo.setRecipientAddr(recipientAddr);
			orderRecordInfo.setRecipientCountry(orderInfo.getRecipientCountryCode());
			orderRecordInfo.setRecipientProvincesCode(orderInfo.getRecProvincesCode());
			orderRecordInfo.setRecipientCityCode(orderInfo.getRecCityCode());
			orderRecordInfo.setRecipientAreaCode(orderInfo.getRecAreaCode());
			orderRecordInfo.setOrderDocAcount(memberInfo.getMemberName());
			orderRecordInfo.setOrderDocName(memberInfo.getMemberIdCardName());
			// 01:身份证、02:护照、04:其他
			orderRecordInfo.setOrderDocType(01);
			orderRecordInfo.setOrderDocId(memberInfo.getMemberIdCard());
			orderRecordInfo.setOrderDocTel(memberInfo.getMemberTel());
			// 以下为可空字段,暂时都为空
			orderRecordInfo.setBatchNumbers("");
			orderRecordInfo.setInvoiceType(0);
			orderRecordInfo.setInvoiceNo("");
			orderRecordInfo.setInvoiceTitle("");
			orderRecordInfo.setInvoiceIdentifyID("");
			orderRecordInfo.setInvoiceDesc("");
			orderRecordInfo.setInvoiceAmount("");
			orderRecordInfo.setInvoiceDate(null);
			orderRecordInfo.setNotes("");
			// ----分割线------
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date reInvoiceDate = null;
			try {
				reInvoiceDate = sdf.parse(datasMap.get("notify_time") + "");
			} catch (ParseException e) {
				e.printStackTrace();
			}
			orderRecordInfo.setPayTime(reInvoiceDate);
			orderRecordInfo.setEntPayNo(datasMap.get("trade_no") + "");
			// 订单备案状态：1-备案中，2-备案成功，3-备案失败
			// 申报状态：1-未申报,2-申报中,3-申报成功、4-申报失败、10-申报中(待系统处理)
			orderRecordInfo.setOrderRecordStatus(2);
			orderRecordInfo.setCreateBy(orderInfo.getMemberName());
			orderRecordInfo.setCreateDate(date);
			orderRecordInfo.setDeleteFlag(0);
			orderRecordInfo.setOrderSerialNo("");
			// 订单录入系统类型：online-商城真实下单(线上)、offline-线下导入
			orderRecordInfo.setOrderSourceType("online");
			// 订单交易状态：1-待付款、2-已付款,待商家处理、3-待揽件、4-快件运输中、5-快件已签收、200-交易成功、400-交易关闭
			orderRecordInfo.setOrderTradingStatus(2);
			if (!ysPayReceiveDao.add(orderRecordInfo)) {
				return ReturnInfoUtils.errorInfo("保存备案订单信息失败,服务器繁忙!");
			}
			return ReturnInfoUtils.successDataInfo(orderRecordInfo);
		}

	}

	// 保存订单备案商品信息
	private Map<String, Object> addOrderRecordGoodsInfo(List<Object> reGoodsRecordDetailList,
			List<Object> reOrderGoodsList) {
		Map<String, Object> paramMap = new HashMap<>();
		String entOrderNo = "";
		Date date = new Date();
		OrderGoodsContent orderInfo = null;
		for (int y = 0; y < reOrderGoodsList.size(); y++) {
			orderInfo = (OrderGoodsContent) reOrderGoodsList.get(y);
			entOrderNo = orderInfo.getEntOrderNo();
			// 根据商品备案编号,保存订单商品数量及商品单价
			paramMap.put(orderInfo.getEntGoodsNo(), orderInfo.getGoodsCount() + "#" + orderInfo.getGoodsPrice());
		}
		Map<String, Object> paramMap2 = new HashMap<>();
		paramMap2.put("entOrderNo", entOrderNo);
		List<Object> reOrderRecordGoodsList = ysPayReceiveDao.findByProperty(OrderRecordGoodsContent.class, paramMap2,
				0, 0);
		if (reOrderRecordGoodsList != null && !reOrderRecordGoodsList.isEmpty()) {
			return ReturnInfoUtils.successInfo();
		} else {
			for (int i = 0; i < reGoodsRecordDetailList.size(); i++) {
				OrderRecordGoodsContent orderRecordGoods = new OrderRecordGoodsContent();
				GoodsRecordDetail goodsRecordDetail = (GoodsRecordDetail) reGoodsRecordDetailList.get(i);
				String count = paramMap.get(goodsRecordDetail.getEntGoodsNo()) + "";
				orderRecordGoods.setEntOrderNo(entOrderNo);
				orderRecordGoods.setSeq(goodsRecordDetail.getSeq());
				orderRecordGoods.setEntGoodsNo(goodsRecordDetail.getEntGoodsNo());
				orderRecordGoods.setCiqGoodsNo(goodsRecordDetail.getCiqGoodsNo());
				orderRecordGoods.setCusGoodsNo(goodsRecordDetail.getCusGoodsNo());
				orderRecordGoods.setHsCode(goodsRecordDetail.getHsCode());
				orderRecordGoods.setShelfGName(goodsRecordDetail.getShelfGName());
				orderRecordGoods.setGoodsName(goodsRecordDetail.getGoodsName());
				orderRecordGoods.setGoodsStyle(goodsRecordDetail.getGoodsStyle());
				orderRecordGoods.setGoodsDescribe("");
				orderRecordGoods.setOriginCountry(goodsRecordDetail.getOriginCountry());
				orderRecordGoods.setBarCode(goodsRecordDetail.getBarCode());
				orderRecordGoods.setBrand(goodsRecordDetail.getBrand());
				String[] strs = count.split("[#]");
				// 截取拼接在#之前的商品数量
				int goodsCount = Integer.parseInt(strs[0]);
				// 截取拼接在#之后的商品单价
				double price = Double.parseDouble(strs[1]);
				orderRecordGoods.setQty(goodsCount);
				orderRecordGoods.setUnit(goodsRecordDetail.getgUnit());
				orderRecordGoods.setPrice(price);
				orderRecordGoods.setTotal(goodsCount * price);
				orderRecordGoods.setCurrCode("142");
				orderRecordGoods.setNotes("");
				orderRecordGoods.setDeleteFlag(0);
				orderRecordGoods.setCreateDate(date);
				if (!ysPayReceiveDao.add(orderRecordGoods)) {
					return ReturnInfoUtils.errorInfo("保存备案订单商品信息失败,服务器繁忙!");
				}
			}
			return ReturnInfoUtils.successInfo();
		}

	}

	// 查询订单及备案头与备案商品信息
	private Map<String, Object> findOrderAndGoodsRecordInfo(String orderId) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramsMap = new HashMap<>();
		// 根据订单ID查询订单商品信息
		paramsMap.put("entOrderNo", orderId);
		List<Object> reOrderGoodsList = ysPayReceiveDao.findByProperty(OrderGoodsContent.class, paramsMap, 0, 0);
		List<Object> list = new ArrayList<>();
		if (reOrderGoodsList != null && reOrderGoodsList.size() > 0) {
			for (int i = 0; i < reOrderGoodsList.size(); i++) {
				paramsMap.clear();
				OrderGoodsContent orderGoodsInfo = (OrderGoodsContent) reOrderGoodsList.get(i);
				// 根据商品备案ID查询备案商品信息
				String entGoodsNo = orderGoodsInfo.getEntGoodsNo();
				paramsMap.put("entGoodsNo", entGoodsNo);
				List<Object> reGoodsRecordDetailList = ysPayReceiveDao.findByProperty(GoodsRecordDetail.class,
						paramsMap, 0, 0);
				list.addAll(reGoodsRecordDetailList);
				paramsMap.clear();
				if (reGoodsRecordDetailList != null && reGoodsRecordDetailList.size() > 0) {
					GoodsRecordDetail goods = (GoodsRecordDetail) reGoodsRecordDetailList.get(0);
					String goodsSerialNo = goods.getGoodsSerialNo();
					paramsMap.put("goodsSerialNo", goodsSerialNo);
					// 根据商品备案流水ID查询备案头信息
					List<Object> reGoodsRecordInfoList = ysPayReceiveDao.findByProperty(GoodsRecord.class, paramsMap, 0,
							0);
					if (reGoodsRecordInfoList != null && reGoodsRecordInfoList.size() > 0) {
						GoodsRecord goodsRecordInfo = (GoodsRecord) reGoodsRecordInfoList.get(0);
						// 商品备案头信息实体类
						statusMap.put("goodsRecordInfo", goodsRecordInfo);
					}
				} else {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "根据商品备案信息查询对应的备案头信息失败,服务器繁忙!");
					return statusMap;
				}
			}
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "查询订单商品对应的商品备案信息失败,服务器繁忙!");
			return statusMap;
		}
		// 订单商品List
		statusMap.put("reOrderGoodsList", reOrderGoodsList);
		// 备案商品信息List
		statusMap.put("reGoodsRecordDetailList", list);
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return statusMap;
	}

	@Override
	public Map<String, Object> sendPayment(String merchantId, Map<String, Object> paymentInfoMap, String tok,
			Map<String, Object> recordMap, String notifyurl) {
		String timestamp = String.valueOf(System.currentTimeMillis());
		Map<String, Object> statusMap = new HashMap<>();
		List<JSONObject> paymentList = new ArrayList<>();
		Map<String, Object> paymentMap = new HashMap<>();
		if (paymentInfoMap != null && !paymentInfoMap.isEmpty()) {
			JSONObject json = new JSONObject();
			json.element("EntPayNo", paymentInfoMap.get("EntPayNo"));
			json.element("PayStatus", paymentInfoMap.get("PayStatus"));
			json.element("PayAmount", paymentInfoMap.get("PayAmount"));
			json.element("PayCurrCode", paymentInfoMap.get("PayCurrCode"));
			json.element("PayTime", paymentInfoMap.get("PayTime"));
			json.element("PayerName", paymentInfoMap.get("PayerName"));
			json.element("PayerDocumentType", paymentInfoMap.get("PayerDocumentType"));
			json.element("PayerDocumentNumber", paymentInfoMap.get("PayerDocumentNumber"));
			json.element("PayerPhoneNumber", paymentInfoMap.get("PayerPhoneNumber"));
			json.element("EntOrderNo", paymentInfoMap.get("EntOrderNo"));
			json.element("EBPEntNo", recordMap.get("ebpEntNo"));
			json.element("EBPEntName", recordMap.get("ebpEntName"));
			json.element("Notes", paymentInfoMap.get("Notes"));
			paymentList.add(json);
			// 客戶端签名
			String clientsign = "";
			// YM APPKEY = "4a5de70025a7425dabeef6e8ea752976";
			String appkey = recordMap.get("appkey") + "";
			try {
				clientsign = MD5
						.getMD5((appkey + tok + paymentList.toString() + notifyurl + timestamp).getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
				return statusMap;
			}
			int eport = Integer.parseInt(recordMap.get("eport") + "");
			// 0:商品备案 1:订单推送 2:支付单推送
			paymentMap.put("type", 2);
			paymentMap.put("eport", eport);

			// 1:广州电子口岸(目前只支持BC业务) 2:南沙智检(支持BBC业务)
			// 1-特殊监管区域BBC保税进口;2-保税仓库BBC保税进口;3-BC直购进口
			int businessType = eport == 1 ? 3 : 2;
			paymentMap.put("businessType", businessType);
			paymentMap.put("ieFlag", IEFLAG);
			paymentMap.put("currCode", CURR_CODE);
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置时间格式
			paymentMap.put("appkey", appkey);
			// 商品发起备案(录入)日期
			String inputDate = sdf.format(date);
			paymentMap.put("inputDate", inputDate);
			// 电商企业编号
			paymentMap.put("ebEntNo", recordMap.get("ebEntNo"));
			// 电商企业名称
			paymentMap.put("ebEntName", recordMap.get("ebEntName"));
			paymentMap.put("ciqOrgCode", recordMap.get("ciqOrgCode"));
			paymentMap.put("customsCode", recordMap.get("customsCode"));
			paymentMap.put("clientsign", clientsign.trim());
			paymentMap.put("timestamp", timestamp);
			paymentMap.put("datas", paymentList.toString());
			paymentMap.put("notifyurl", notifyurl);
			paymentMap.put("note", "");
			// 报文类型
			String opType = recordMap.get("opType") + "";
			if (StringEmptyUtils.isNotEmpty(opType)) {
				// A-新增；M-修改；D-
				paymentMap.put("opType", opType);
			} else {
				// 当前台不传参数时默认
				paymentMap.put("opType", "A");
			}
			// 是否向海关发送
			// paymentMap.put("uploadOrNot", false);
			String resultStr = YmHttpUtil.HttpPost(YmMallConfig.REPORT_URL, paymentMap);
			// 当端口号为2(智检时)再往电子口岸多发送一次
			if (eport == 2 || "443400".equals(recordMap.get("ciqOrgCode"))) {
				List<JSONObject> paymentList2 = new ArrayList<>();
				if (eport == 1) {
					Map<String, Object> reMerchantMap = merchantUtils.getMerchantRecordInfo(merchantId, 2);
					if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
						return reMerchantMap;
					}
					MerchantRecordInfo merchantRecord = (MerchantRecordInfo) reMerchantMap
							.get(BaseCode.DATAS.toString());
					paymentMap.put("eport", 2);
					// 检验检疫机构代码
					paymentMap.put("businessType", 3);
					// 检验检疫机构代码
					paymentMap.put("ciqOrgCode", "443400");
					JSONObject json2 = new JSONObject();
					json2.element("EntPayNo", paymentInfoMap.get("EntPayNo"));
					json2.element("PayStatus", paymentInfoMap.get("PayStatus"));
					json2.element("PayAmount", paymentInfoMap.get("PayAmount"));
					json2.element("PayCurrCode", paymentInfoMap.get("PayCurrCode"));
					json2.element("PayTime", paymentInfoMap.get("PayTime"));
					json2.element("PayerName", paymentInfoMap.get("PayerName"));
					json2.element("PayerDocumentType", paymentInfoMap.get("PayerDocumentType"));
					json2.element("PayerDocumentNumber", paymentInfoMap.get("PayerDocumentNumber"));
					json2.element("PayerPhoneNumber", paymentInfoMap.get("PayerPhoneNumber"));
					json2.element("EntOrderNo", paymentInfoMap.get("EntOrderNo"));
					json2.element("Notes", paymentInfoMap.get("Notes"));
					// 电商平台企业编码
					json2.element("EBPEntNo", merchantRecord.getEbpEntNo());
					// 电商平台企业名称
					json2.element("EBPEntName", merchantRecord.getEbpEntName());
					paymentList2.add(json2);
					// 电商企业编号
					paymentMap.put("ebEntNo", merchantRecord.getEbEntNo());
					// 电商企业名称
					paymentMap.put("ebEntName", merchantRecord.getEbEntName());
				} else if (eport == 2) {
					Map<String, Object> reMerchantMap = merchantUtils.getMerchantRecordInfo(merchantId, 1);
					if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
						return reMerchantMap;
					}
					MerchantRecordInfo merchantRecord = (MerchantRecordInfo) reMerchantMap
							.get(BaseCode.DATAS.toString());
					// 检验检疫机构代码
					paymentMap.put("ciqOrgCode", "443400");
					// 1:广州电子口岸(目前只支持BC业务) 2:南沙智检(支持BBC业务)
					paymentMap.put("eport", 1);
					JSONObject json2 = new JSONObject();
					json2.element("EntPayNo", paymentInfoMap.get("EntPayNo"));
					json2.element("PayStatus", paymentInfoMap.get("PayStatus"));
					json2.element("PayAmount", paymentInfoMap.get("PayAmount"));
					json2.element("PayCurrCode", paymentInfoMap.get("PayCurrCode"));
					json2.element("PayTime", paymentInfoMap.get("PayTime"));
					json2.element("PayerName", paymentInfoMap.get("PayerName"));
					json2.element("PayerDocumentType", paymentInfoMap.get("PayerDocumentType"));
					json2.element("PayerDocumentNumber", paymentInfoMap.get("PayerDocumentNumber"));
					json2.element("PayerPhoneNumber", paymentInfoMap.get("PayerPhoneNumber"));
					json2.element("EntOrderNo", paymentInfoMap.get("EntOrderNo"));
					json2.element("Notes", paymentInfoMap.get("Notes"));
					// 电商平台企业编码
					json2.element("EBPEntNo", merchantRecord.getEbpEntNo());
					// 电商平台企业名称
					json2.element("EBPEntName", merchantRecord.getEbpEntName());
					paymentList2.add(json2);
					// 电商企业编号
					paymentMap.put("ebEntNo", merchantRecord.getEbEntNo());
					// 电商企业名称
					paymentMap.put("ebEntName", merchantRecord.getEbEntName());
				}
				paymentMap.put("datas", paymentList2.toString());
				try {
					clientsign = MD5
							.getMD5((appkey + tok + paymentList2.toString() + notifyurl + timestamp).getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
					return statusMap;
				}
				paymentMap.put("clientsign", clientsign.trim());
				System.out.println("------第二次发起支付单推送-------");
				resultStr = YmHttpUtil.HttpPost(YmMallConfig.REPORT_URL, paymentMap);
			}
			if (StringUtil.isNotEmpty(resultStr)) {
				return JSONObject.fromObject(resultStr);
			} else {
				return ReturnInfoUtils.errorInfo("服务器接受支付信息失败,服务器繁忙！");
			}
		}
		return ReturnInfoUtils.errorInfo("支付参数错误！");
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
	private final Map<String, Object> sendOrder(GoodsRecord goodsRecordInfo, List<Object> reGoodsRecordDetailList,
			List<Object> reOrderGoodsList, String tok, OrderRecordContent orderRecordInfo) {
		String timestamp = String.valueOf(System.currentTimeMillis());
		int eport = goodsRecordInfo.getCustomsPort();
		// 电商企业编号
		String ebEntNo = "";
		String ebEntName = "";
		// 电子口岸(16)编码
		String dzkaNo = "";
		Map<String, Object> statusMap = new HashMap<>();
		List<JSONObject> goodsList = new ArrayList<>();
		List<JSONObject> orderJsonList = new ArrayList<>();
		Map<String, Object> orderMap = new HashMap<>();
		JSONObject goodsJson = null;
		JSONObject orderJson = new JSONObject();
		Map<String, Object> goodsMap = new HashMap<>();
		for (int y = 0; y < reOrderGoodsList.size(); y++) {
			OrderGoodsContent orderInfo = (OrderGoodsContent) reOrderGoodsList.get(y);
			// 根据商品备案编号,保存订单商品数量及商品单价
			goodsMap.put(orderInfo.getEntGoodsNo(), orderInfo.getGoodsCount() + "#" + orderInfo.getGoodsPrice());
		}
		for (int i = 0; i < reGoodsRecordDetailList.size(); i++) {
			goodsJson = new JSONObject();
			GoodsRecordDetail goodsRecordDetail = (GoodsRecordDetail) reGoodsRecordDetailList.get(i);
			String str = goodsMap.get(goodsRecordDetail.getEntGoodsNo()) + "";
			String[] strs = str.split("#");
			// 截取拼接在#之前的商品数量
			int goodsCount = Integer.parseInt(strs[0]);
			// 截取拼接在#之后的商品单价
			double price = Double.parseDouble(strs[1]);
			goodsJson.element("Seq", i + 1);
			String entGoodsNo = goodsRecordDetail.getEntGoodsNo();
			if (entGoodsNo.contains("_")) {
				String[] s = entGoodsNo.split("_");
				goodsJson.element("EntGoodsNo", s[0]);
			} else {
				goodsJson.element("EntGoodsNo", entGoodsNo);
			}
			goodsJson.element("CIQGoodsNo", goodsRecordDetail.getCiqGoodsNo());
			goodsJson.element("CusGoodsNo", goodsRecordDetail.getCusGoodsNo());
			goodsJson.element("HSCode", goodsRecordDetail.getHsCode());
			goodsJson.element("GoodsName", goodsRecordDetail.getGoodsName());
			goodsJson.element("GoodsStyle", goodsRecordDetail.getGoodsStyle());
			goodsJson.element("GoodsDescribe", "");
			goodsJson.element("OriginCountry", goodsRecordDetail.getOriginCountry());
			goodsJson.element("BarCode", goodsRecordDetail.getBarCode());
			goodsJson.element("Brand", goodsRecordDetail.getBrand());
			goodsJson.element("Qty", goodsCount);
			goodsJson.element("Unit", goodsRecordDetail.getgUnit());
			goodsJson.element("Price", goodsRecordDetail.getRegPrice());
			goodsJson.element("Total", goodsCount * price);
			goodsJson.element("CurrCode", "142");
			goodsJson.element("Notes", "");
			// 当口岸为1(电子口岸时)电商企业编码则直接用电子口岸对应的16位编码,如果不是则使用智检电商企业备案编码
			ebEntNo = eport == 1 ? goodsRecordDetail.getDZKNNo() : goodsRecordDetail.getEbEntNo();
			dzkaNo = goodsRecordDetail.getDZKNNo();
			// 电商企业名称
			ebEntName = goodsRecordDetail.getEbEntName();
			String jsonGoods = goodsRecordDetail.getSpareParams();
			if (StringEmptyUtils.isNotEmpty(jsonGoods)) {
				JSONObject params = JSONObject.fromObject(jsonGoods);
				// 企邦专属字段
				goodsJson.element("marCode", params.get("orderDocAcount"));
				goodsJson.element("sku", params.get("SKU"));
			}
			goodsList.add(goodsJson);
		}
		orderJson.element("orderGoodsList", goodsList);
		orderJson.element("EntOrderNo", orderRecordInfo.getEntOrderNo());
		orderJson.element("OrderStatus", orderRecordInfo.getOrderStatus());
		orderJson.element("PayStatus", orderRecordInfo.getPayStatus());
		orderJson.element("OrderGoodTotal", orderRecordInfo.getOrderGoodTotal());
		orderJson.element("OrderGoodTotalCurr", orderRecordInfo.getOrderGoodTotalCurr());
		orderJson.element("Freight", orderRecordInfo.getFreight());
		orderJson.element("Tax", orderRecordInfo.getTax());
		orderJson.element("OtherPayment", orderRecordInfo.getOtherPayment());
		orderJson.element("OtherPayNotes", orderRecordInfo.getOtherPayNotes());
		orderJson.element("OtherCharges", orderRecordInfo.getOtherCharges());
		orderJson.element("ActualAmountPaid", orderRecordInfo.getActualAmountPaid());
		orderJson.element("RecipientName", orderRecordInfo.getRecipientName());
		orderJson.element("RecipientAddr", orderRecordInfo.getRecipientAddr());
		orderJson.element("RecipientTel", orderRecordInfo.getRecipientTel());
		orderJson.element("RecipientCountry", orderRecordInfo.getRecipientCountry());
		orderJson.element("RecipientProvincesCode", orderRecordInfo.getRecipientProvincesCode());
		orderJson.element("RecipientCityCode", orderRecordInfo.getRecipientCityCode());
		orderJson.element("RecipientAreaCode", orderRecordInfo.getRecipientAreaCode());
		orderJson.element("OrderDocAcount", orderRecordInfo.getOrderDocAcount());
		orderJson.element("OrderDocName", orderRecordInfo.getOrderDocName());
		orderJson.element("OrderDocType", orderRecordInfo.getOrderDocType());
		orderJson.element("OrderDocId", orderRecordInfo.getOrderDocId());
		orderJson.element("OrderDocTel", orderRecordInfo.getOrderDocTel());
		orderJson.element("OrderDate", orderRecordInfo.getCreateDate());
		orderJson.element("entPayNo", orderRecordInfo.getEntPayNo());
		orderJsonList.add(orderJson);
		// 客戶端签名
		String clientsign = "";
		try {
			clientsign = MD5.getMD5(
					(YmMallConfig.APPKEY + tok + orderJsonList.toString() + YmMallConfig.ORDER_NOTIFY_URL + timestamp)
							.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		}
		// 0:商品备案 1:订单推送 2:支付单推送
		orderMap.put("type", 1);

		// 1-特殊监管区域BBC保税进口;2-保税仓库BBC保税进口;3-BC直购进口
		String businessType = "";
		if (goodsRecordInfo.getCustomsPort() == 1) {
			businessType = "3";
		} else if (goodsRecordInfo.getCustomsPort() == 2) {
			businessType = "2";
		}
		orderMap.put("businessType", Integer.valueOf(businessType));
		orderMap.put("ieFlag", IEFLAG);
		orderMap.put("currCode", CURR_CODE);
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置时间格式
		// 商品发起备案(录入)日期
		String inputDate = sdf.format(date);
		orderMap.put("inputDate", inputDate);
		// 1:广州电子口岸(目前只支持BC业务) 2:南沙智检(支持BBC业务)
		orderMap.put("eport", goodsRecordInfo.getCustomsPort());
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
		// 国检代码
		orderMap.put("ciqOrgCode", goodsRecordInfo.getCiqOrgCode());
		// 海关代码
		orderMap.put("customsCode", goodsRecordInfo.getCustomsCode());
		orderMap.put("appkey", YmMallConfig.APPKEY);
		orderMap.put("clientsign", clientsign);
		orderMap.put("timestamp", timestamp);
		orderMap.put("datas", orderJsonList.toString());
		orderMap.put("notifyurl", YmMallConfig.ORDER_NOTIFY_URL);
		orderMap.put("note", "");
		// 是否像海关发送
		// orderMap.put("uploadOrNot", false);

		// 发起订单备案
		String resultStr = YmHttpUtil.HttpPost(YmMallConfig.REPORT_URL, orderMap);
		// 当端口号为2(智检时)再往电子口岸多发送一次
		if (goodsRecordInfo.getCustomsPort() == 2) {
			// 1:广州电子口岸(目前只支持BC业务) 2:南沙智检(支持BBC业务)
			orderMap.put("eport", 1);
			if (StringEmptyUtils.isNotEmpty(dzkaNo) && StringEmptyUtils.isNotEmpty(ebEntName)) {
				// 电商企业编号
				orderMap.put("ebEntNo", dzkaNo);
				// 电商企业名称
				orderMap.put("ebEntName", ebEntName);
			} else {
				orderMap.put("ebEntNo", "C010000000537118");
				// 电商企业名称
				orderMap.put("ebEntName", "广州银盟信息科技有限公司");
			}
			String resultStr2 = YmHttpUtil.HttpPost(YmMallConfig.REPORT_URL, orderMap);
			if (StringEmptyUtils.isNotEmpty(resultStr2)) {
				return JSONObject.fromObject(resultStr);
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

	// 更新支付单返回信息
	private Map<String, Object> updatePaymentInfo(String entPayNo, String rePayMessageID) {
		Date date = new Date();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("entPayNo", entPayNo);
		List<Object> reList = ysPayReceiveDao.findByProperty(PaymentContent.class, paramMap, 0, 0);
		paramMap.clear();
		if (reList != null && reList.size() > 0) {
			for (int i = 0; i < reList.size(); i++) {
				PaymentContent payment = (PaymentContent) reList.get(i);
				payment.setReSerialNo(rePayMessageID);
				payment.setUpdateBy("system");
				payment.setUpdateDate(date);
				if (!ysPayReceiveDao.update(payment)) {
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

	// 更新订单返回信息
	private Map<String, Object> updateOrderInfo(OrderRecordContent orderRecordInfo, String reOrderMessageID) {
		Date date = new Date();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("entOrderNo", orderRecordInfo.getEntOrderNo());
		List<Object> reList = ysPayReceiveDao.findByProperty(OrderRecordContent.class, paramMap, 0, 0);
		paramMap.clear();
		if (reList != null && reList.size() > 0) {
			for (int i = 0; i < reList.size(); i++) {
				OrderRecordContent order = (OrderRecordContent) reList.get(i);
				order.setReOrderSerialNo(reOrderMessageID);
				order.setUpdateBy("system");
				order.setUpdateDate(date);
				if (!ysPayReceiveDao.update(order)) {
					paramMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					paramMap.put(BaseCode.MSG.toString(), "更新服务器返回订单messageID错误!");
					return paramMap;
				}
			}
			return ReturnInfoUtils.successInfo();
		} else {
			paramMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			paramMap.put(BaseCode.MSG.toString(), "更新订单返回messageID错误,服务器繁忙！");
			return paramMap;
		}
	}

	// 更新订单状态及修改库存上架数量
	private Map<String, Object> updataOrderAndStockStatus(List<Object> reOrderGoodsList) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		Date date = new Date();
		String orderId = "";
		for (int i = 0; i < reOrderGoodsList.size(); i++) {
			OrderGoodsContent orderGoodsInfo = (OrderGoodsContent) reOrderGoodsList.get(i);
			orderId = orderGoodsInfo.getOrderId();
			String entGoodsNo = orderGoodsInfo.getEntGoodsNo();
			int goodsCount = Integer.parseInt(Long.toString(orderGoodsInfo.getGoodsCount()));
			paramMap.clear();
			paramMap.put("entGoodsNo", entGoodsNo);
			List<Object> reStockList = ysPayReceiveDao.findByProperty(StockContent.class, paramMap, 1, 1);
			StockContent stockInfo = (StockContent) reStockList.get(0);
			int paymentCount = stockInfo.getPaymentCount();
			// 更新待支付数量
			stockInfo.setPaymentCount(paymentCount - goodsCount);
			int oaudCount = stockInfo.getPaidCount();
			// 更新已支付数量
			stockInfo.setPaidCount(oaudCount + goodsCount);
			stockInfo.setUpdateDate(date);
			stockInfo.setUpdateBy("system");
			if (!ysPayReceiveDao.update(stockInfo)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "修改待库存中待支付数量错误,服务器繁忙！");
				return statusMap;
			}
		}
		paramMap.clear();
		paramMap.put("orderId", orderId);
		List<Object> reOrderList = ysPayReceiveDao.findByProperty(OrderContent.class, paramMap, 1, 1);
		if (reOrderList != null && reOrderList.size() > 0) {
			OrderContent orderInfo = (OrderContent) reOrderList.get(0);
			// 将订单状态修改为已支付
			orderInfo.setStatus(2);
			orderInfo.setUpdateDate(date);
			orderInfo.setUpdateBy("system");
			if (!ysPayReceiveDao.update(orderInfo)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "修改订单状态错误,服务器繁忙！");
				return statusMap;
			}
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "准备修改订单状态,查询订单状态错误,服务器繁忙！");
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

	@Override
	public Map<String, Object> balancePayReceive(Map<String, Object> datasMap) {
		Date date = new Date();
		// 生成支付交易编号
		int count = SerialNoUtils.getSerialNo("paymentId");
		String tradeNo = SerialNoUtils.createTradeNo("01O", (count + 1));
		datasMap.put("trade_no", tradeNo);
		datasMap.put("notify_time", DateUtil.formatTime(date));
		return ysPayReceive(datasMap);
	}

	@Override
	public Map<String, Object> walletRechargeReceive(Map datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("参数不能为空!");
		}
		try {
			// 添加交易日志
			Map<String, Object> rePayMap = addPaymentLog(datasMap);

			if (!"1".equals(rePayMap.get(BaseCode.STATUS.toString()))) {
				return rePayMap;
			}
			TradeReceiptLog paymentReceiptLog = (TradeReceiptLog) rePayMap.get(BaseCode.DATAS.toString());
			// 钱包金额更新
			return chooseWallet(paymentReceiptLog);
		} catch (Exception e) {
			logger.error("--钱包更新失败--", e);
			return ReturnInfoUtils.errorInfo("未知错误!");
		}
	}

	/**
	 * 判断不同的Id类型，添加钱包日志记录
	 * 
	 * @param paymentReceiptLog
	 *            交易日志实体类
	 * @return
	 */
	private Map<String, Object> chooseWallet(TradeReceiptLog paymentReceiptLog) {
		String userId = paymentReceiptLog.getUserId();
		String[] strA = userId.split("_");
		// 截取Id前比自定义的名称
		String id = strA[0];
		// 判断Id类型
		if (id.contains("MerchantId")) {// 商户
			return updateMerchantWallet(paymentReceiptLog);
		} else if (id.contains("Member")) {// 用户

		} else if (id.contains("AgentId")) {// 代理商

		}
		return ReturnInfoUtils.errorInfo("未找到对应钱包类型信息!");
	}

	/**
	 * 更新商户钱包余额
	 * 
	 * @param paymentReceiptLog
	 *            交易日志记录
	 * @return Map
	 */
	private Map<String, Object> updateMerchantWallet(TradeReceiptLog receiptLog) {
		System.out.println("--------更新商户钱包余额--");
		Map<String, Object> reMap = merchantWalletService.balanceOperating(receiptLog.getUserId(),
				receiptLog.getAmount(), "add");
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		Map<String,Object> reWalletMap = walletUtils.checkWallet(1, receiptLog.getUserId(), "");
		if(!"1".equals(reWalletMap.get(BaseCode.STATUS.toString()))){
			return reWalletMap;
		}
		MerchantWalletContent wallet = (MerchantWalletContent) reWalletMap.get(BaseCode.DATAS.toString());
		Map<String, Object> datas = new HashMap<>();
		datas.put("walletId", wallet.getWalletId());
		datas.put("merchantName", wallet.getMerchantName());
		//
		datas.put("serialName", "钱包充值");
		//充值金额
		double amount = receiptLog.getAmount();
		datas.put("balance", DoubleOperationUtil.sub(wallet.getBalance(), amount));
		datas.put("amount", receiptLog.getAmount());
		//
		datas.put("type", 2);
		//
		datas.put("flag", "in");
		// 由于商户充值是像银盛发起故而没有目标钱包Id
		datas.put("targetWalletId", "000000");
		datas.put("targetName", "银盛");
		datas.put("merchantId", receiptLog.getUserId());
		datas.put("serialNo", receiptLog.getTradeNo());
		datas.put("status", "success");
		// 添加日志
		return merchantWalletLogService.addWalletLog(datas);
	}

	/**
	 * 添加系统交易日志记录
	 * 
	 * @param datasMap
	 *            银盛返回参数
	 * @return Map
	 */
	private Map<String, Object> addPaymentLog(Map datasMap) {
		// 发起交易的订单号
		String orderId = datasMap.get("out_trade_no") + "";
		// 返回时间
		String reTime = datasMap.get("notify_time") + "";
		// 交易流水号
		String tradeNo = datasMap.get("trade_no") + "";
		// 交易金额
		String totalAmount = datasMap.get("total_amount") + "";
		Map<String, Object> params = new HashMap<>();
		params.put("orderId", orderId);
		List<TradeReceiptLog> reList = ysPayReceiveDao.findByProperty(TradeReceiptLog.class, params, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询支付日志失败!");
		} else if (!reList.isEmpty()) {
			TradeReceiptLog log = reList.get(0);
			String status = log.getTradingStatus();
			// 当流水号已返回成功后,防止银盛重复返回!
			if ("success".equals(status)) {
				return ReturnInfoUtils.errorInfo("交易流水[" + tradeNo + "]已经支付成功!");
			}
			if (Double.parseDouble(totalAmount) != log.getAmount()) {
				logger.error("--银盛支付回调金额错误--发起金额:" + log.getAmount() + ";回调金额:" + totalAmount);
			}
			log.setTradeNo(tradeNo);
			log.setNotifyTime(DateUtil.parseDate(reTime, "yyyy-MM-dd hh:mm:ss"));
			// 状态：success(交易成功)、failure(交易失败)、process(处理中)
			log.setTradingStatus("success");
			log.setUpdateDate(new Date());
			if (!ysPayReceiveDao.update(log)) {
				logger.error("--银盛支付回调--更新记录失败");
			}
			return ReturnInfoUtils.successDataInfo(log);
		} else {
			return ReturnInfoUtils.errorInfo("订单号[" + orderId + "]未查询到交易日志记录!");
		}
	}

	@Override
	public Map<String, Object> dfReceive(Map params) {
		if (params == null || params.isEmpty()) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		// 发起代付交易的订单号
		String orderId = params.get("out_batch_no") + "";
		// 返回时间
		String reTime = params.get("notify_time") + "";
		// 交易金额
		double amount = Double.parseDouble(params.get("total_amount") + "");
		// 交易状态
		String tradeStatus = params.get("trade_status") + "";
		// 交易描述
		String tradeStatusDescription = params.get("trade_status_description") + "";
		Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("orderId", orderId);
		Map<String, Object> reUpdateMap = updateTradeLogStatus(orderId, amount, tradeStatus, reTime,
				tradeStatusDescription);
		if (!"1".equals(reUpdateMap.get(BaseCode.STATUS.toString()))) {
			return reUpdateMap;
		}
		TradeReceiptLog log = (TradeReceiptLog) reUpdateMap.get(BaseCode.DATAS.toString());
		return updateMerchantWalletCash(log);
	}

	/**
	 * 更新商户钱包现金
	 * 
	 * @param log
	 * @return
	 */
	private Map<String, Object> updateMerchantWalletCash(TradeReceiptLog log) {
		System.out.println("--清算商户钱包现金--");
		Map<String, Object> reWalletMap = walletUtils.checkWallet(1, log.getUserId(), "");
		if (!"1".equals(reWalletMap.get(BaseCode.STATUS.toString()))) {
			return reWalletMap;
		}
		MerchantWalletContent wallet = (MerchantWalletContent) reWalletMap.get(BaseCode.DATAS.toString());
		double oldCash = wallet.getCash();
		wallet.setCash(oldCash - log.getAmount());
		if (!ysPayReceiveDao.update(wallet)) {
			return ReturnInfoUtils.errorInfo("商户钱包加款失败!");
		}
		Map<String, Object> datas = new HashMap<>();
		datas.put("walletId", wallet.getWalletId());
		datas.put("merchantName", wallet.getMerchantName());
		// 交易名称
		datas.put("serialName", "资金清算");
		datas.put("balance", oldCash);
		datas.put("amount", log.getAmount());
		// 类型:1-佣金、2-充值、3-提现、4-缴费、5-购物
		datas.put("type", 3);
		// 进出帐标识：in-进账,out-出账
		datas.put("flag", "out");
		// 由于清算是像银盛发起故而没有目标钱包Id
		datas.put("targetWalletId", "000000");
		datas.put("targetName", "银盛");
		datas.put("merchantId", log.getUserId());
		datas.put("serialNo", log.getTradeNo());
		datas.put("status", "success");
		// 添加日志
		return merchantWalletLogService.addWalletLog(datas);
	}

	private Map<String, Object> updatePaymentReceiptLog(TradeReceiptLog log) {
		if (log != null) {
			if (!ysPayReceiveDao.update(log)) {
				return ReturnInfoUtils.errorInfo("更新交易日志失败,服务器繁忙!");
			}
			return ReturnInfoUtils.successInfo();
		} else {
			return ReturnInfoUtils.errorInfo("更新交易日志失败,交易日志不能为空!");
		}
	}

	private void setRemarkInfo(TradeReceiptLog log, String oldRemark, String tradeStatusDescription) {
		if (StringEmptyUtils.isNotEmpty(oldRemark)) {
			log.setRemark(
					oldRemark + "#" + DateUtil.formatDate(new Date(), "yyyy-MM-dd hh:mm:ss") + tradeStatusDescription);
		} else {
			log.setRemark(DateUtil.formatDate(new Date(), "yyyy-MM-dd hh:mm:ss") + tradeStatusDescription);
		}
	}

	@Override
	public Map<String, Object> memberWithdraw(Map datasMap) {
		if (datasMap == null || datasMap.isEmpty()) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		// 发起代付交易的订单号
		String orderId = datasMap.get("out_batch_no") + "";
		// 返回时间
		String reTime = datasMap.get("notify_time") + "";
		// 交易金额
		double amount = Double.parseDouble(datasMap.get("total_amount") + "");
		// 交易状态
		String tradeStatus = datasMap.get("trade_status") + "";
		// 交易描述
		String tradeStatusDescription = datasMap.get("trade_status_description") + "";
		Map<String, Object> reUpdateMap = updateTradeLogStatus(orderId, amount, tradeStatus, reTime,
				tradeStatusDescription);
		if (!"1".equals(reUpdateMap.get(BaseCode.STATUS.toString()))) {
			return reUpdateMap;
		}
		TradeReceiptLog log = (TradeReceiptLog) reUpdateMap.get(BaseCode.DATAS.toString());
		Map<String, Object> reRollMap = rollbackAmount(orderId, amount, tradeStatus);
		if (!"1".equals(reRollMap.get(BaseCode.STATUS.toString()))) {
			return reRollMap;
		}
		String status = "";
		if ("BATCH_TRADE_FAILURE".equals(tradeStatus)) {
			status = "failure";
		} else if ("BATCH_TRADE_SUCCESS".equals(tradeStatus)) {
			status = "success";
		}
		return saveWalletLog(log.getUserId(), orderId, amount, status);
	}

	private Map<String, Object> saveWalletLog(String userId, String orderId, double amount, String status) {
		Map<String, Object> reWalletMap = walletUtils.checkWallet(2, userId, null);
		if (!"1".equals(reWalletMap.get(BaseCode.STATUS.toString()))) {
			logger.error("--用户钱包查询失败-->" + reWalletMap.toString());
			return null;
		}
		MemberWalletContent wallet = (MemberWalletContent) reWalletMap.get(BaseCode.DATAS.toString());
		Map<String, Object> params = new HashMap<>();
		params.put("memberWalletId", wallet.getWalletId());
		params.put("memberName", wallet.getMemberName());
		// 交易流水号
		params.put("serialNo", orderId);
		// 交易名称
		params.put("serialName", "提现");
		params.put("beforeChangingBalance", DoubleOperationUtil.add(wallet.getReserveAmount(), amount));
		params.put("amount", amount);
		params.put("afterChangeBalance", wallet.getReserveAmount());
		// 类型:1-佣金、2-充值、3-提现、4-缴费、5-购物
		params.put("type", 3);
		// 交易状态
		params.put("status", status);
		// 进出帐标识
		params.put("flag", "out");
		params.put("targetWalletId", "000000");
		params.put("targetName", "银盟");
		// 用户钱包日志记录
		return memberWalletLogService.addWalletLog(params);
	}

	/**
	 * 根据代付状态，回滚金额
	 * 
	 * @param orderId
	 * @param amount
	 * @param tradeStatus
	 * @return
	 */
	private Map<String, Object> rollbackAmount(String orderId, double amount, String tradeStatus) {
		Map<String, Object> params = new HashMap<>();
		params.put("orderId", orderId);
		Map<String, Object> reMap = getTradeReceiptLog(params, 1, 1);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		TradeReceiptLog log = (TradeReceiptLog) reMap.get(BaseCode.DATAS.toString());
		String sourceType = log.getSourceType();
		if ("reserveAmount".equals(sourceType)) {
			Map<String, Object> reMap1 = memberWalletService.freezingFundsOperating(log.getUserId(), amount, "sub");
			if (!"1".equals(reMap1.get(BaseCode.STATUS.toString()))) {
				return reMap1;
			}
			if ("BATCH_TRADE_FAILURE".equals(tradeStatus)) {
				return memberWalletService.reserveAmountOperating(log.getUserId(), amount, "add");
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 更新交易日志状态
	 * 
	 * @param orderId
	 *            订单id
	 * @param totalAmount
	 *            交易金额
	 * @param tradeStatus
	 *            回调的交易状态
	 * @param reTime
	 *            回调时间
	 * @param tradeStatusDescription
	 *            交易描述
	 * @return Map
	 */
	private Map<String, Object> updateTradeLogStatus(String orderId, double totalAmount, String tradeStatus,
			String reTime, String tradeStatusDescription) {
		Map<String, Object> params = new HashMap<>();
		params.put("orderId", orderId);
		Map<String, Object> reMap = getTradeReceiptLog(params, 1, 1);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		TradeReceiptLog log = (TradeReceiptLog) reMap.get(BaseCode.DATAS.toString());
		String status = log.getTradingStatus();
		// 当流水号已返回成功后,防止银盛重复返回!
		if ("success".equals(status)) {
			return ReturnInfoUtils.errorInfo("[" + orderId + "]已经支付成功!");
		}
		if (totalAmount != log.getAmount()) {
			logger.error("--银盛支付回调金额错误--发起金额:" + log.getAmount() + ";回调金额:" + totalAmount);
		}
		// 代付失败
		if ("BATCH_TRADE_FAILURE".equals(tradeStatus)) {
			// 状态：success(交易成功)、failure(交易失败)、process(处理中)
			log.setTradingStatus("failure");
			log.setNotifyTime(DateUtil.parseDate(reTime, "yyyy-MM-dd hh:mm:ss"));
			String oldRemark = log.getRemark();
			setRemarkInfo(log, oldRemark, tradeStatusDescription);
		} else if ("BATCH_TRADE_SUCCESS".equals(tradeStatus)) {
			// 状态：success(交易成功)、failure(交易失败)、process(处理中)
			log.setTradingStatus("success");
			log.setNotifyTime(DateUtil.parseDate(reTime, "yyyy-MM-dd hh:mm:ss"));
			String oldRemark = log.getRemark();
			setRemarkInfo(log, oldRemark, tradeStatusDescription);
		}
		return tradeReceiptLogService.updateLog(log);
	}

	/**
	 * 
	 * @param params
	 * @return
	 */
	private Map<String, Object> getTradeReceiptLog(Map<String, Object> params, int page, int size) {
		List<TradeReceiptLog> reList = ysPayReceiveDao.findByProperty(TradeReceiptLog.class, params, page, page);
		if (reList == null) {
			return ReturnInfoUtils.warnInfo();
		} else if (!reList.isEmpty()) {
			if (page == 1 && size == 1) {
				return ReturnInfoUtils.successDataInfo(reList.get(0));
			} else {
				return ReturnInfoUtils.successDataInfo(reList);
			}
		} else {
			return ReturnInfoUtils.noDatas();
		}
	}
}

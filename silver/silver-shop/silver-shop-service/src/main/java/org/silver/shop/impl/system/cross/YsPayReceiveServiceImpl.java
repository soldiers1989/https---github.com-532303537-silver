package org.silver.shop.impl.system.cross;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.AccessTokenService;
import org.silver.shop.api.system.cross.YsPayReceiveService;
import org.silver.shop.api.system.organization.MemberService;
import org.silver.shop.api.system.tenant.WalletLogService;
import org.silver.shop.config.YmMallConfig;
import org.silver.shop.dao.system.cross.YsPayReceiveDao;
import org.silver.shop.impl.system.tenant.MerchantWalletServiceImpl;
import org.silver.shop.model.system.commerce.GoodsRecord;
import org.silver.shop.model.system.commerce.GoodsRecordDetail;
import org.silver.shop.model.system.commerce.OrderContent;
import org.silver.shop.model.system.commerce.OrderGoodsContent;
import org.silver.shop.model.system.commerce.OrderRecordContent;
import org.silver.shop.model.system.commerce.OrderRecordGoodsContent;
import org.silver.shop.model.system.commerce.StockContent;
import org.silver.shop.model.system.cross.PaymentContent;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantRecordInfo;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.shop.util.MerchantUtils;
import org.silver.util.DateUtil;
import org.silver.util.JedisUtil;
import org.silver.util.MD5;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SendMsg;
import org.silver.util.SerialNoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.util.YmHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.StringUtil;

import net.sf.json.JSONObject;
import redis.clients.jedis.Jedis;

@Service(interfaceClass = YsPayReceiveService.class)
public class YsPayReceiveServiceImpl implements YsPayReceiveService {
	/**
	 * 进出境标志I-进，E-出
	 */
	private static final String IEFLAG = "I";

	/**
	 * 币制默认为人民币
	 */
	private static final String CURRCODE = "142";
	protected static final Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

	@Autowired
	private YsPayReceiveDao ysPayReceiveDao;

	@Autowired
	private AccessTokenService accessTokenService;

	@Autowired
	private MerchantWalletServiceImpl merchantWalletServiceImpl;

	@Autowired
	private WalletLogService walletLogService;

	@Autowired
	private MemberService memberService;
	@Autowired
	private MerchantUtils merchantUtils;

	@Override
	public Map<String, Object> ysPayReceive(Map<String, Object> datasMap) {
		Map<String, Object> params = new HashMap<>();
		Member memberInfo = null;
		String reEntOrderNo = datasMap.get("out_trade_no") + "";
		// 根据订单ID查询订单是否存在
		params.put("entOrderNo", reEntOrderNo);
		List<Object> orderList = ysPayReceiveDao.findByProperty(OrderContent.class, params, 0, 0);
		List<Object> orderGoodsList = ysPayReceiveDao.findByProperty(OrderGoodsContent.class, params, 0, 0);
		if (orderList == null || orderGoodsList == null) {
			return ReturnInfoUtils.errorInfo("查询订单失败,服务器繁忙!");
		} else if (!orderList.isEmpty() && !orderGoodsList.isEmpty()) {
			try {
				OrderContent orderInfo = (OrderContent) orderList.get(0);
				OrderGoodsContent orderGoodsContent = (OrderGoodsContent) orderGoodsList.get(0);
				String merchantId = orderGoodsContent.getMerchantId();
				Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(merchantId);
				if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
					return reMerchantMap;
				}
				Merchant merchantInfo = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
				SendMsg.sendMsg(merchantInfo.getMerchantPhone(), "【银盟信息科技有限公司】您有一个订单需要处理,订单号" + reEntOrderNo);

				// 根据用户ID查询用户是否存在
				Map<String, Object> reMemberMap = memberService.getMemberInfo(orderInfo.getMemberId());
				if (!"1".equals(reMemberMap.get(BaseCode.STATUS.toString()))) {
					return reMemberMap;
				}
				memberInfo = (Member) reMemberMap.get(BaseCode.DATAS.toString());
				// 保存支付单信息
				Map<String, Object> rePaymentMap = addPaymentInfo(orderList, datasMap, memberInfo, orderGoodsContent);
				if (!"1".equals(rePaymentMap.get(BaseCode.STATUS.toString()))) {
					return rePaymentMap;
				}
				// 获取返回的实体
				PaymentContent paymentInfo = (PaymentContent) rePaymentMap.get(BaseCode.DATAS.toString());
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
				//4a5de70025a7425dabeef6e8ea752976
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
				//4a5de70025a7425dabeef6e8ea752976
				recordMap.put("appkey", YmMallConfig.APPKEY);
				// 发送支付单备案
				Map<String, Object> rePayMap = sendPayment(merchantId, paymentInfoMap, tok, recordMap,
						YmMallConfig.PAYMENTNOTIFYURL);
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
				Map<String, Object> reOrderMap2 = updateOrderInfo(orderRecordInfo, reOrderMessageID);
				if (!"1".equals(reOrderMap2.get(BaseCode.STATUS.toString()) + "")) {
					return reOrderMap2;
				}
				return reOrderMap2;
			} catch (Exception e) {
				logger.error(Thread.currentThread().getName() + "--支付成功回调-->>", e);
			}
		}
		return ReturnInfoUtils.errorInfo("查询订单不存在,参数不正确!");
	}

	// 保存支付单信息
	private final Map<String, Object> addPaymentInfo(List<Object> orderList, Map<String, Object> datasMap,
			Member memberInfo, OrderGoodsContent orderGoodsContent) {
		Date date = new Date();
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		OrderContent orderInfo = (OrderContent) orderList.get(0);
		PaymentContent paymentInfo = new PaymentContent();
		String entPayNo = datasMap.get("trade_no") + "";
		paramMap.put("entPayNo", entPayNo);
		List<Object> rePayList = ysPayReceiveDao.findByProperty(PaymentContent.class, paramMap, 1, 1);
		if (rePayList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "查询支付订单是否存在时错误,请重试!");
			return statusMap;
		} else if (!rePayList.isEmpty()) {
			PaymentContent rePaymentInfo = (PaymentContent) rePayList.get(0);
			return ReturnInfoUtils.successDataInfo(rePaymentInfo);
		} else {
			// 获取订单商品名称
			String goodsName = orderGoodsContent.getGoodsName();
			String memberId = orderInfo.getMemberId();
			String memberName = orderInfo.getMemberName();
			String merchantId = orderInfo.getMerchantId();
			String merchantName = orderInfo.getMerchantName();
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
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "保存支付单信息失败,服务器繁忙!");
				return statusMap;
			}

			//
			Map<String, Object> reMap = merchantWalletServiceImpl.checkWallet(1, merchantId, merchantName);
			if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getMsg());
				statusMap.put(BaseCode.MSG.toString(), "创建钱包失败!");
				return statusMap;
			}
			MerchantWalletContent wallet = (MerchantWalletContent) reMap.get(BaseCode.DATAS.toString());
			// 原钱包余额
			double oldBalance = wallet.getBalance();
			Map<String, Object> reWalletMap = updateWallet(wallet, payAmount);
			if (!"1".equals(reWalletMap.get(BaseCode.STATUS.toString()))) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getMsg());
				statusMap.put(BaseCode.MSG.toString(), "交易金额存入商户钱包失败,服务器繁忙!");
				return statusMap;
			}
			JSONObject param = new JSONObject();
			param.put("merchantId", merchantId);
			param.put("merchantName", merchantName);
			param.put("memberId", memberId);
			param.put("memberName", memberName);
			param.put("entOrderNo", entOrderNo);
			param.put("entPayNo", entPayNo);
			// 钱包交易日志流水名称
			param.put("entPayName", goodsName);
			param.put("payAmount", payAmount);
			param.put("oldBalance", oldBalance);
			// 分类:1-购物、2-充值、3-提现、4-缴费
			param.put("type", 1);
			Map<String, Object> reWalletLogMap = walletLogService.addWalletLog(2, param);
			if (!"1".equals(reWalletLogMap.get(BaseCode.STATUS.toString()))) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getMsg());
				statusMap.put(BaseCode.MSG.toString(), "交易日志记录失败,服务器繁忙!");
				return statusMap;
			}
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), paymentInfo);
			return statusMap;
		}
	}

	/**
	 * 将支付金额存入到商户钱包中
	 * 
	 * @param wallet
	 *            钱包实体
	 * @param payAmount
	 *            交易金额
	 * @return Map
	 */
	private Map<String, Object> updateWallet(MerchantWalletContent wallet, double payAmount) {
		Map<String, Object> statusMap = new HashMap<>();
		double oldBalance = wallet.getBalance();
		// 将支付金额存入到商户钱包中
		wallet.setBalance(oldBalance + payAmount);
		if (!ysPayReceiveDao.update(wallet)) {
			statusMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getMsg());
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return statusMap;
	}

	// 保存订单备案信息
	private final Map<String, Object> addOrderRecordInfo(OrderContent orderInfo, Map<String, Object> datasMap,
			Member memberInfo, List<Object> orderList, List<Object> orderGoodsList) {
		Date date = new Date();
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		OrderRecordContent orderRecordInfo = new OrderRecordContent();
		String entOrderNo = orderInfo.getEntOrderNo();
		paramMap.put("entOrderNo", entOrderNo);
		List<Object> reOrderList = ysPayReceiveDao.findByProperty(OrderRecordContent.class, paramMap, 1, 1);
		if (reOrderList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "查询备案订单信息是否存在时错误,请重试!");
			return statusMap;
		} else if (!reOrderList.isEmpty()) {
			orderRecordInfo = (OrderRecordContent) reOrderList.get(0);
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), orderRecordInfo);
			return statusMap;
		} else {
			paramMap.clear();
			paramMap.put("entOrderNo", entOrderNo);
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
			//// 订单备案状态：1-备案中，2-备案成功，3-备案失败
			orderRecordInfo.setOrderRecordStatus(1);
			orderRecordInfo.setCreateBy(orderInfo.getMemberName());
			orderRecordInfo.setCreateDate(date);
			orderRecordInfo.setDeleteFlag(0);
			orderRecordInfo.setOrderSerialNo("");
			if (!ysPayReceiveDao.add(orderRecordInfo)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "保存备案订单信息失败,服务器繁忙!");
				return statusMap;
			}
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), orderRecordInfo);
			return statusMap;
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

	// 发起支付单备案
	public Map<String, Object> sendPayment(String merchantId, Map<String, Object> paymentInfoMap, String tok,
			Map<String, Object> recordMap, String notifyurl) {
		String timestamp = String.valueOf(System.currentTimeMillis());
		Map<String, Object> statusMap = new HashMap<>();
		List<JSONObject> paymentList = new ArrayList<>();
		Map<String, Object> paymentMap = new HashMap<>();
		if (StringEmptyUtils.isNotEmpty(tok)) {
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
			paymentMap.put("currCode", CURRCODE);
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
			// 是否像海关发送
			// paymentMap.put("uploadOrNot", false);
			// String resultStr =
			// YmHttpUtil.HttpPost("http://192.168.1.120:8080/silver-web/Eport/Report",
			// paymentMap);
			String resultStr = YmHttpUtil.HttpPost("https://ym.191ec.com/silver-web/Eport/Report", paymentMap);
			// 当端口号为2(智检时)再往电子口岸多发送一次
			if (eport == 2) {
				System.out.println("------第二次发起支付单推送------");
				Map<String, Object> paramsMap = new HashMap<>();
				paramsMap.put("merchantId", merchantId);
				paramsMap.put("customsPort", 1);
				List<Object> reMerchantList = ysPayReceiveDao.findByProperty(MerchantRecordInfo.class, paramsMap, 1, 1);
				MerchantRecordInfo merchantRecordInfo = (MerchantRecordInfo) reMerchantList.get(0);
				JSONObject json2 = new JSONObject();
				List<JSONObject> paymentList2 = new ArrayList<>();
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
				json2.element("EBPEntNo", merchantRecordInfo.getEbpEntNo());
				json2.element("EBPEntName", merchantRecordInfo.getEbpEntName());
				paymentList2.add(json2);
				// 1:广州电子口岸(目前只支持BC业务) 2:南沙智检(支持BBC业务)
				paymentMap.put("eport", 1);
				// 电商企业编号
				paymentMap.put("ebEntNo", merchantRecordInfo.getEbEntNo());
				// 电商企业名称
				paymentMap.put("ebEntName", merchantRecordInfo.getEbEntName());
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
				// String resultStr2 =
				// YmHttpUtil.HttpPost("http://192.168.1.120:8080/silver-web/Eport/Report",
				// paymentMap);
				String resultStr2 = YmHttpUtil.HttpPost("https://ym.191ec.com/silver-web/Eport/Report", paymentMap);
				if (StringEmptyUtils.isNotEmpty(resultStr2)) {
					return JSONObject.fromObject(resultStr2);
				} else {
					return ReturnInfoUtils.errorInfo("第二次推送订单信息接收失败,请重试！");
				}
			}
			if (StringUtil.isNotEmpty(resultStr)) {
				return JSONObject.fromObject(resultStr);
			} else {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "服务器接受支付信息失败,服务器繁忙！");
				return statusMap;
			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
		statusMap.put(BaseCode.MSG.toString(), "支付参数错误！");
		return statusMap;
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
		String DZKANo = "";
		Map<String, Object> statusMap = new HashMap<>();
		List<JSONObject> goodsList = new ArrayList<>();
		List<JSONObject> orderJsonList = new ArrayList<>();
		Map<String, Object> orderMap = new HashMap<>();
		JSONObject goodsJson = null;
		JSONObject orderJson = new JSONObject();
		Map<String, Object> goodsMap = new HashMap<>();
		for (int y = 0; y < reOrderGoodsList.size(); y++) {
			OrderGoodsContent orderGoodsInfo = (OrderGoodsContent) reOrderGoodsList.get(y);
			goodsMap.put(orderGoodsInfo.getGoodsName(),
					orderGoodsInfo.getGoodsCount() + "#" + orderGoodsInfo.getGoodsPrice());
		}
		for (int i = 0; i < reGoodsRecordDetailList.size(); i++) {
			goodsJson = new JSONObject();
			GoodsRecordDetail goodsRecordDetail = (GoodsRecordDetail) reGoodsRecordDetailList.get(i);
			String str = goodsMap.get(goodsRecordDetail.getGoodsName()) + "";
			String[] strs = str.split("#");
			// 截取拼接在#之前的商品数量
			int goodsCount = Integer.parseInt(strs[0]);
			// 截取拼接在#之后的商品单价
			double price = Double.parseDouble(strs[1]);
			goodsJson.element("Seq", i + 1);
			String entGoodsNo = goodsRecordDetail.getEntGoodsNo();
			if(entGoodsNo.contains("_")){
				String[]  s = entGoodsNo.split("_");
				goodsJson.element("EntGoodsNo", s[0]);
			}else{
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
			ebEntNo = eport == 1 ? goodsRecordDetail.getDZKNNo() : goodsRecordDetail.getEbEntNo();
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
					(YmMallConfig.APPKEY + tok + orderJsonList.toString() + YmMallConfig.ORDERNOTIFYURL + timestamp)
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
		orderMap.put("currCode", CURRCODE);
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
		orderMap.put("notifyurl", YmMallConfig.ORDERNOTIFYURL);
		orderMap.put("note", "");
		// 是否像海关发送
		// orderMap.put("uploadOrNot", false);

		// 发起订单备案
		String resultStr = YmHttpUtil.HttpPost("https://ym.191ec.com/silver-web/Eport/Report", orderMap);
		// 当端口号为2(智检时)再往电子口岸多发送一次
		if (goodsRecordInfo.getCustomsPort() == 2) {
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
			String resultStr2 = YmHttpUtil.HttpPost("https://ym.191ec.com/silver-web/Eport/Report", orderMap);
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
			paramMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			paramMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			return paramMap;
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
		Map<String, Object> rePayNoMap = createPayNo();
		if (!"1".equals(rePayNoMap.get(BaseCode.STATUS.toString()))) {
			return rePayNoMap;
		}
		String time = DateUtil.formatTime(date);
		String entPayNo = rePayNoMap.get(BaseCode.DATAS.toString()) + "";
		datasMap.put("trade_no", entPayNo);
		datasMap.put("notify_time", time);
		return ysPayReceive(datasMap);
	}

	/**
	 * 生成交易编号
	 * 
	 * @return Map
	 */
	private Map<String, Object> createPayNo() {
		Map<String, Object> statusMap = new HashMap<>();
		Calendar cl = Calendar.getInstance();
		int year = cl.get(Calendar.YEAR);
		String property = "entPayNo";
		String topStr = "PayNo_";
		long orderIdCount = ysPayReceiveDao.findSerialNoCount(PaymentContent.class, property, year);
		if (orderIdCount < 0) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else {
			String serialNo = SerialNoUtils.getSerialNo(topStr, orderIdCount);
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), serialNo);
			return statusMap;
		}
	}

	public static void main(String[] args) {
		// 4a5de70025a7425dabeef6e8ea752976
		// 4a5de70025a7425dabeef6e8ea752976;
		// 缓存中的键
		Jedis j = new Jedis("150.242.58.22", 6380);
		j.auth("jugg");

		String redisKey = "4a5de70025a7425dabeef6e8ea752976_accessToken";
		String redisTok = j.get(redisKey);
		System.out.println(redisTok);
	}
}

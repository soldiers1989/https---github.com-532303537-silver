package org.silver.shop.impl.system.cross;

import java.io.UnsupportedEncodingException;
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
import org.silver.shop.config.YmMallConfig;
import org.silver.shop.dao.system.cross.YsPayReceiveDao;
import org.silver.shop.model.system.commerce.GoodsRecord;
import org.silver.shop.model.system.commerce.GoodsRecordDetail;
import org.silver.shop.model.system.commerce.OrderContent;
import org.silver.shop.model.system.commerce.OrderGoodsContent;
import org.silver.shop.model.system.commerce.OrderRecordContent;
import org.silver.shop.model.system.commerce.OrderRecordGoodsContent;
import org.silver.shop.model.system.cross.PaymentContent;
import org.silver.shop.model.system.organization.Member;
import org.silver.util.MD5;
import org.silver.util.YmHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.StringUtil;

import net.sf.json.JSONObject;

@Service(interfaceClass = YsPayReceiveService.class)
public class YsPayReceiveServiceImpl implements YsPayReceiveService {
	// 进出境标志I-进，E-出
	private static final String IEFLAG = "I";

	// 币制默认为人民币
	private static final String CURRCODE = "142";
	protected static final Logger logger = LogManager.getLogger();

	@Autowired
	private YsPayReceiveDao ysPayReceiveDao;

	@Autowired
	private AccessTokenService accessTokenService;

	@Override
	public Map<String, Object> ysPayReceive(Map<String, Object> datasMap) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		Member memberInfo = null;
		String reEntOrderNo = datasMap.get("out_trade_no") + "";
		// 根据订单ID查询订单是否存在
		params.put("entOrderNo", reEntOrderNo);
		List<Object> orderList = ysPayReceiveDao.findByProperty(OrderContent.class, params, 0, 0);
		if (orderList != null && orderList.size() > 0) {
			OrderContent orderInfo = (OrderContent) orderList.get(0);
			params.clear();
			// 根据用户ID查询用户是否存在
			params.put("memberId", orderInfo.getMemberId());
			List<Object> memberList = ysPayReceiveDao.findByProperty(Member.class, params, 1, 1);
			if (memberList != null && memberList.size() > 0) {
				memberInfo = (Member) memberList.get(0);
				// 保存支付单信息
				Map<String, Object> rePaymentMap = addPaymentInfo(orderList, datasMap, memberInfo);
				if (!"1".equals(rePaymentMap.get(BaseCode.STATUS.toString()))) {
					return rePaymentMap;
				}
				// 获取返回的实体
				PaymentContent paymentInfo = (PaymentContent) rePaymentMap.get(BaseCode.DATAS.toString());
				// 保存备案订单信息
				Map<String, Object> reOrderRecordMap = addOrderRecordInfo(orderInfo, datasMap, memberInfo, orderList);
				if (!"1".equals(reOrderRecordMap.get(BaseCode.STATUS.toString()))) {
					return reOrderRecordMap;
				}
				// 获取返回的订单备案实体实体
				OrderRecordContent orderRecordInfo = (OrderRecordContent) reOrderRecordMap
						.get(BaseCode.DATAS.toString());

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
				// 保存订单关联商品信息
				Map<String, Object> reOrderRecordGoodsMap = addOrderRecordGoodsInfo(reGoodsRecordDetailList,
						reOrderGoodsList);
				if (!reOrderRecordGoodsMap.get(BaseCode.STATUS.toString()).equals("1")) {
					return reOrderRecordGoodsMap;
				}
				// 请求获取tok
				Map<String, Object> tokMap = accessTokenService.getAccessToken();
				if (!"1".equals(tokMap.get(BaseCode.STATUS.toString()))) {
					return tokMap;
				}
				String tok = tokMap.get(BaseCode.DATAS.toString()) + "";
				// 发送支付单备案
				Map<String, Object> rePayMap = sendPayment(paymentInfo, goodsRecordInfo, tok);
				if (!"1".equals(rePayMap.get(BaseCode.STATUS.toString()) + "")) {
					return rePayMap;
				}
				String rePayMessageID = rePayMap.get("messageID") + "";
				// 更新支付单返回id
				Map<String, Object> rePaymentMap2 = updatePaymentInfo(paymentInfo, rePayMessageID);
				if (!"1".equals(rePaymentMap2.get(BaseCode.STATUS.toString()) + "")) {
					return rePaymentMap2;
				}
				// 发送订单备案
				Map<String, Object> reOrderMap = sendOrder(goodsRecordInfo, reGoodsRecordDetailList, reOrderGoodsList,
						tok, orderList, orderRecordInfo);
				if (!"1".equals(reOrderMap.get(BaseCode.STATUS.toString()) + "")) {
					return reOrderMap;
				}
				String reOrderMessageID = rePayMap.get("messageID") + "";
				// 更新支付单返回id
				Map<String, Object> reOrderMap2 = updateOrderInfo(orderRecordInfo, reOrderMessageID);
				if (!"1".equals(reOrderMap2.get(BaseCode.STATUS.toString()) + "")) {
					return reOrderMap2;
				}
				return reOrderMap2;
			}
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "查询订单信息失败,参数不正确!");
			return statusMap;
		}
		return null;
	}

	// 保存支付单信息
	private final Map<String, Object> addPaymentInfo(List<Object> orderList, Map<String, Object> datasMap,
			Member memberInfo) {
		Date date = new Date();
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		OrderContent orderInfo = (OrderContent) orderList.get(0);
		PaymentContent paymentInfo = new PaymentContent();
		String entPayNp = datasMap.get("trade_no") + "";
		paramMap.put("entPayNo", entPayNp);
		List<Object> rePayList = ysPayReceiveDao.findByProperty(PaymentContent.class, paramMap, 1, 1);
		if (rePayList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "查询支付订单是否存在时错误,请重试!");
			return statusMap;
		} else if (rePayList != null && rePayList.size() > 0) {
			PaymentContent rePaymentInfo = (PaymentContent) rePayList.get(0);
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), rePaymentInfo);
			return statusMap;
		} else {
			paymentInfo.setMemberId(orderInfo.getMemberId());
			paymentInfo.setMemberName(orderInfo.getMemberName());
			paymentInfo.setPayAmount(Double.parseDouble(datasMap.get("total_amount") + ""));
			// 支付流水号
			paymentInfo.setEntPayNo(entPayNp);
			paymentInfo.setPayStatus("D");
			// 默认为142-人名币
			paymentInfo.setPayCurrCode("142");
			paymentInfo.setPayTime(date);
			paymentInfo.setPayerName(memberInfo.getMemberIdCardName());
			// 支付人证件类型01:身份证02:护照04:其他
			paymentInfo.setPayerDocumentType(01);
			paymentInfo.setPayerDocumentNumber(memberInfo.getMemberIdCard());
			paymentInfo.setPayerPhoneNumber(memberInfo.getMemberTel());
			// 订单编号
			paymentInfo.setEntOrderNo(orderInfo.getEntOrderNo());
			paymentInfo.setPayRecord(1);
			paymentInfo.setPayFalg(0);
			paymentInfo.setCreateBy(orderInfo.getMemberName());
			paymentInfo.setCreateDate(date);
			// 删除标识:0-未删除,1-已删除
			paymentInfo.setDeleteFlag(0);
			if (!ysPayReceiveDao.add(paymentInfo)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "保存支付单信息失败,服务器繁忙!");
				return statusMap;
			}
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), paymentInfo);
			return statusMap;
		}
	}

	// 保存订单备案信息
	private final Map<String, Object> addOrderRecordInfo(OrderContent orderInfo, Map<String, Object> datasMap,
			Member memberInfo, List<Object> orderList) {
		Date date = new Date();
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		OrderRecordContent orderRecordInfo = new OrderRecordContent();
		paramMap.put("entOrderNo", orderInfo.getEntOrderNo());
		List<Object> reOrderList = ysPayReceiveDao.findByProperty(OrderRecordContent.class, paramMap, 1, 1);
		if (reOrderList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "查询备案订单信息是否存在时错误,请重试!");
			return statusMap;
		} else if (reOrderList.size() > 0) {
			orderRecordInfo = (OrderRecordContent) reOrderList.get(0);
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), orderRecordInfo);
			return statusMap;
		} else {
			/*
			 * orderRecordInfo.setMerchantId(orderInfo.getMerchantId());
			 * orderRecordInfo.setMerchantName(orderInfo.getMerchantName());
			 * orderRecordInfo.setMemberId(orderInfo.getMemberId());
			 * orderRecordInfo.setMemberName(orderInfo.getMemberName());
			 */
			orderRecordInfo.setEntOrderNo(orderInfo.getEntOrderNo());
			// 电子订单状态0-订单确认,1-订单完成,2-订单取消
			orderRecordInfo.setOrderStatus(1);
			orderRecordInfo.setPayStatus(0);
			double totalPrice = 0;
			for (int i = 0; i < orderList.size(); i++) {
				OrderContent order = (OrderContent) orderList.get(i);
				totalPrice += order.getOrderTotalPrice();
			}
			orderRecordInfo.setOrderGoodTotal(totalPrice);
			// 人民币
			orderRecordInfo.setOrderGoodTotalCurr("142");
			// 暂时填0
			orderRecordInfo.setFreight(0.0);
			orderRecordInfo.setTax(0.0);
			orderRecordInfo.setOtherPayment(0.0);
			// 抵付说明抵付情况说明。如果填写抵付金额时，此项必填。
			orderRecordInfo.setOtherPayNotes("");
			orderRecordInfo.setOtherCharges(0.0);
			double actualAmountPaid = Double.parseDouble(datasMap.get("total_amount") + "");
			orderRecordInfo.setActualAmountPaid(actualAmountPaid);
			orderRecordInfo.setRecipientName(orderInfo.getRecipientName());
			orderRecordInfo.setRecipientCardId(orderInfo.getRecipientCardId());
			orderRecordInfo.setRecipientTel(orderInfo.getRecipientTel());
			orderRecordInfo.setRecipientAddr(orderInfo.getRecipientTel());
			orderRecordInfo.setRecipientCountry(orderInfo.getRecipientCountryCode());
			orderRecordInfo.setRecipientProvincesCode(orderInfo.getRecProvincesCode());
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
			orderRecordInfo.setInvoiceDate(reInvoiceDate);
			//// 订单备案状态：1-备案中，2-备案成功，3-备案失败
			orderRecordInfo.setOrderRecordStatus(1);
			orderRecordInfo.setCreateBy(orderInfo.getMemberName());
			orderRecordInfo.setCreateDate(date);
			orderRecordInfo.setDeleteFlag(0);
			orderRecordInfo.setOrderSerialNo("aaaa");
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
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		Date date = new Date();
		OrderGoodsContent orderInfo = null;
		for (int y = 0; y < reOrderGoodsList.size(); y++) {
			orderInfo = (OrderGoodsContent) reOrderGoodsList.get(y);
			// 根据商品备案编号,保存订单商品数量及商品单价
			paramMap.put(orderInfo.getEntGoodsNo(), orderInfo.getGoodsCount() + "#" + orderInfo.getGoodsPrice());
		}
		Map<String, Object> paramMap2 = new HashMap<>();
		paramMap2.put("entOrderNo", orderInfo.getEntOrderNo());
		List<Object> reOrderRecordGoodsList = ysPayReceiveDao.findByProperty(OrderRecordGoodsContent.class, paramMap2,
				0, 0);
		if (reOrderRecordGoodsList != null && reOrderRecordGoodsList.size() > 0) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			return statusMap;
		} else {
			for (int i = 0; i < reGoodsRecordDetailList.size(); i++) {
				OrderRecordGoodsContent orderRecordGoods = new OrderRecordGoodsContent();
				GoodsRecordDetail goodsRecordDetail = (GoodsRecordDetail) reGoodsRecordDetailList.get(i);
				String count = paramMap.get(goodsRecordDetail.getEntGoodsNo()) + "";
				orderRecordGoods.setEntOrderNo(orderInfo.getEntOrderNo());
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
				orderRecordGoods.setPrice(goodsRecordDetail.getRegPrice());
				orderRecordGoods.setTotal(goodsCount * price);
				orderRecordGoods.setCurrCode("142");
				orderRecordGoods.setNotes("");
				orderRecordGoods.setDeleteFlag(0);
				orderRecordGoods.setCreateDate(date);
				if (!ysPayReceiveDao.add(orderRecordGoods)) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "保存备案订单商品信息失败,服务器繁忙!");
					return statusMap;
				}
			}
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			return statusMap;
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
	private Map<String, Object> sendPayment(PaymentContent paymentInfo, GoodsRecord goodsRecordInfo, String tok) {
		String timestamp = String.valueOf(System.currentTimeMillis());
		Map<String, Object> statusMap = new HashMap<>();
		List<JSONObject> paymentList = new ArrayList<>();
		Map<String, Object> paymentMap = new HashMap<>();
		JSONObject json = new JSONObject();
		json.element("EntPayNo", paymentInfo.getEntPayNo());
		json.element("PayStatus", paymentInfo.getPayStatus());
		json.element("PayAmount", paymentInfo.getPayAmount());
		json.element("PayCurrCode", paymentInfo.getPayCurrCode());
		json.element("PayTime", paymentInfo.getPayTime());
		json.element("PayerName", paymentInfo.getPayerName());
		json.element("PayerDocumentType", paymentInfo.getPayerDocumentType());
		json.element("PayerDocumentNumber", paymentInfo.getPayerDocumentNumber());
		json.element("PayerPhoneNumber", paymentInfo.getPayerPhoneNumber());
		json.element("EntOrderNo", paymentInfo.getEntOrderNo());
		json.element("EBPEntNo", goodsRecordInfo.getEbpEntNo());
		json.element("EBPEntName", goodsRecordInfo.getEbpEntName());
		json.element("Notes", paymentInfo.getEntPayNo());
		paymentList.add(json);
		// 客戶端签名
		String clientsign = "";
		try {
			clientsign = MD5.getMD5(
					(YmMallConfig.APPKEY + tok + paymentList.toString() + YmMallConfig.PAYMENTNOTIFYURL + timestamp)
							.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		}
		// 0:商品备案 1:订单推送 2:支付单推送
		paymentMap.put("type", 2);
		paymentMap.put("eport", goodsRecordInfo.getCustomsPort());
		// 1:广州电子口岸(目前只支持BC业务) 2:南沙智检(支持BBC业务)
		// 1-特殊监管区域BBC保税进口;2-保税仓库BBC保税进口;3-BC直购进口
		String businessType = "";
		if (goodsRecordInfo.getCustomsPort() == 1) {
			businessType = "3";
		} else if (goodsRecordInfo.getCustomsPort() == 2) {
			businessType = "2";
		}
		paymentMap.put("businessType", Integer.valueOf(businessType));
		paymentMap.put("ieFlag", IEFLAG);
		paymentMap.put("currCode", CURRCODE);
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置时间格式
		// 商品发起备案(录入)日期
		String inputDate = sdf.format(date);
		paymentMap.put("inputDate", inputDate);
		// 电商企业编号
		paymentMap.put("ebEntNo", goodsRecordInfo.getEbEntNo());
		// 电商企业名称
		paymentMap.put("ebEntName", goodsRecordInfo.getEbEntName());
		paymentMap.put("appkey", YmMallConfig.APPKEY.trim());
		paymentMap.put("ciqOrgCode", goodsRecordInfo.getCiqOrgCode());
		paymentMap.put("customsCode", goodsRecordInfo.getCustomsCode());
		paymentMap.put("clientsign", clientsign.trim());
		paymentMap.put("timestamp", timestamp);
		paymentMap.put("datas", paymentList.toString());
		paymentMap.put("notifyurl", YmMallConfig.PAYMENTNOTIFYURL);
		paymentMap.put("note", "");
		// 商城支付单备案流水号
		// String resultStr =
		// YmHttpUtil.HttpPost("http://ym.191ec.com/silver-web/Eport/Report",
		// paymentMap);
		String resultStr = YmHttpUtil.HttpPost("http://192.168.1.120:8080/silver-web/Eport/Report", paymentMap);
		if (StringUtil.isNotEmpty(resultStr)) {
			return JSONObject.fromObject(resultStr);
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "服务器接受支付信息失败,服务器繁忙！");
			return statusMap;
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
	 * @param orderList
	 * @param orderRecordInfo
	 * @return
	 */
	private final Map<String, Object> sendOrder(GoodsRecord goodsRecordInfo, List<Object> reGoodsRecordDetailList,
			List<Object> reOrderGoodsList, String tok, List<Object> orderList, OrderRecordContent orderRecordInfo) {
		String timestamp = String.valueOf(System.currentTimeMillis());
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
			String[] strs = str.split("[#]");
			// 截取拼接在#之前的商品数量
			int goodsCount = Integer.parseInt(strs[0]);
			// 截取拼接在#之后的商品单价
			double price = Double.parseDouble(strs[1]);
			goodsJson.element("Seq", i + 1);
			goodsJson.element("EntGoodsNo", goodsRecordDetail.getEntGoodsNo());
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
		orderJson.element("OrderDocAcount", orderRecordInfo.getOrderDocAcount());
		orderJson.element("OrderDocName", orderRecordInfo.getOrderDocName());
		orderJson.element("OrderDocType", orderRecordInfo.getOrderDocType());
		orderJson.element("OrderDocId", orderRecordInfo.getOrderDocId());
		orderJson.element("OrderDocTel", orderRecordInfo.getOrderDocTel());
		orderJson.element("OrderDate", orderRecordInfo.getCreateDate());
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
		orderMap.put("eport", goodsRecordInfo.getCustomsPort());
		// 1:广州电子口岸(目前只支持BC业务) 2:南沙智检(支持BBC业务)
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
		// 电商企业编号
		orderMap.put("ebEntNo", goodsRecordInfo.getEbEntNo());
		// 电商企业名称
		orderMap.put("ebEntName", goodsRecordInfo.getEbEntName());
		orderMap.put("appkey", YmMallConfig.APPKEY);
		orderMap.put("ciqOrgCode", goodsRecordInfo.getCiqOrgCode());
		orderMap.put("customsCode", goodsRecordInfo.getCustomsCode());
		orderMap.put("clientsign", clientsign);
		orderMap.put("timestamp", timestamp);
		orderMap.put("datas", orderJsonList.toString());
		orderMap.put("notifyurl", YmMallConfig.ORDERNOTIFYURL);
		orderMap.put("note", "");
		String resultStr = YmHttpUtil.HttpPost("http://192.168.1.120:8080/silver-web/Eport/Report", orderMap);
		// 商城商品备案流水号
		// String resultStr =
		// YmHttpUtil.HttpPost("http://ym.191ec.com/silver-web/Eport/Report",
		// orderMap);
		if (StringUtil.isNotEmpty(resultStr)) {
			return JSONObject.fromObject(resultStr);
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "服务器接受信息失败,服务器繁忙！");
			return statusMap;
		}
	}

	//更新支付单返回信息
	private Map<String, Object> updatePaymentInfo(PaymentContent paymentInfo, String rePayMessageID) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("entPayNo", paymentInfo.getEntPayNo());
		List<Object> reList = ysPayReceiveDao.findByProperty(PaymentContent.class, paramMap, 0, 0);
		paramMap.clear();
		if (reList != null && reList.size() > 0) {
			for (int i = 0; i < reList.size(); i++) {
				PaymentContent payment = (PaymentContent) reList.get(i);
				payment.setReSerialNo(rePayMessageID);
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

	//更新订单返回信息
	private Map<String, Object> updateOrderInfo(OrderRecordContent orderRecordInfo, String reOrderMessageID) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("entOrderNo", orderRecordInfo.getEntOrderNo());
		List<Object> reList = ysPayReceiveDao.findByProperty(OrderRecordContent.class, paramMap, 0, 0);
		paramMap.clear();
		if (reList != null && reList.size() > 0) {
			for (int i = 0; i < reList.size(); i++) {
				OrderRecordContent order = (OrderRecordContent) reList.get(i);
				order.setReOrderSerialNo(reOrderMessageID);
				if (!ysPayReceiveDao.update(order)) {
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
			paramMap.put(BaseCode.MSG.toString(), "更新订单返回messageID错误,服务器繁忙！");
			return paramMap;
		}
	}

}

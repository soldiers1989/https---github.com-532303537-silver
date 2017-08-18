package org.silver.sys.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.silver.common.GZEportCode;
import org.silver.sys.api.GZEportService;
import org.silver.sys.util.CheckDatasUtil;
import org.silver.util.DateUtil;
import org.silver.util.FtpUtils;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
@Service(interfaceClass=GZEportService.class)
public class GZEportServiceImpl implements GZEportService {

	private final static Log logger = LogFactory.getLog(GZEportServiceImpl.class);

	@Override
	public void requestAnalysis(Object records, String type, String eport) {
		logger.info("=================开始处理接收的数据===============================");
		if ("0".equals(eport)) {// 电子口岸
			if ("0".equals(type)) {// 商品备案
				//goodsRecord(records);
			}
			if ("1".equals(type)) {// 订单备案

			}
			if ("2".equals(type)) {// 支付单备案

			}
		} else if ("1".equals(eport)) {// 智检

		}

	}

	@Override
	public Map<String, Object> goodsRecord(String opType,String ieFlag,String businessType,Object records) {
		logger.info("=================开始处理接收的records===============================");
		Map<String, Object> checkMap = new HashMap<String, Object>();
		JSONArray jList = JSONArray.fromObject(records);
		List<String> noNullKeys = new ArrayList<>();
		
	//	noNullKeys.add("CustomsCode");//海关区域代码
	//	noNullKeys.add("CIQOrgCode");//检验检疫机构
		noNullKeys.add("Seq");
		noNullKeys.add("EntGoodsNo");
		noNullKeys.add("ShelfGName");
		noNullKeys.add("NcadCode");
		noNullKeys.add("HSCode");
		noNullKeys.add("GoodsName");
		noNullKeys.add("GoodsStyle");
		noNullKeys.add("Brand");
		noNullKeys.add("GUnit");
		noNullKeys.add("StdUnit");
		noNullKeys.add("RegPrice");
		noNullKeys.add("GiftFlag");
		noNullKeys.add("OriginCountry");
		noNullKeys.add("Quality");
		noNullKeys.add("Manufactory");
		noNullKeys.add("NetWt");
		noNullKeys.add("GrossWt");
		checkMap = CheckDatasUtil.checkData(jList, noNullKeys);
		if ((int) checkMap.get("status") != 1) {
			return checkMap;
		}
		JSONArray list = JSONArray.fromObject(checkMap.get("datas"));
		//JSONObject record = JSONObject.fromObject(list.get(0));
		try {
			checkMap = createHead(list, "", opType,  businessType, ieFlag);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			checkMap.put("status", -1);
			checkMap.put("msg", "上传报文失败，请重试");
			return checkMap;
		} catch (IOException e) {
			e.printStackTrace();
			checkMap.put("status", -2);
			checkMap.put("msg", "上传报文失败，请重试");
		}
		return checkMap;
	}

	@Override
	public Map<String, Object> createHead(JSONArray list, String path, String opType, String businessType, String ieFlag) throws FileNotFoundException, IOException {
		logger.info("=================开始处理接收的JSonList生成XML文件===============================");
	
		Map<String, Object> statusMap = new HashMap<String, Object>();
		statusMap.put("status", 1);
		statusMap.put("msg", "报文发送成功 ");
		Element root = new Element("InternationalTrade");
		Document Doc = new Document(root);
		String time = DateUtil.getDate("yyyyMMddHHmmss");
		String remitSerialNumber = DateUtil.getDate("yyyyMMddHHmmss") + (int) (Math.random() * 9000 + 10000);// 自动生成交易编码：当前时间+五位随机码
		String strName = "KJ881101_YINMENG_" + remitSerialNumber;
		Element elements = new Element("Head");
		elements.addContent(new Element("MessageID").setText(strName));
		elements.addContent(new Element("MessageType").setText(GZEportCode.MESSAGE_TYPE_GOOD));
		elements.addContent(new Element("Sender").setText(GZEportCode.SENDER));
		elements.addContent(new Element("Receiver").setText(GZEportCode.RECEIVER));
		elements.addContent(new Element("SendTime").setText(time));
		elements.addContent(new Element("FunctionCode").setText(GZEportCode.FUNCTION_CODE_CIQ));
		elements.addContent(new Element("SignerInfo").setText(""));
		elements.addContent(new Element("Version").setText(GZEportCode.VERSION));
		// 创建节点 ;
		Element declaration = new Element("Declaration");
		Element goodsRegHead = new Element("GoodsRegHead");
		goodsRegHead.addContent(new Element("DeclEntNo").setText(GZEportCode.DECL_ENT_NO));
		goodsRegHead.addContent(new Element("DeclEntName").setText(GZEportCode.DECL_ENT_NAME));
		goodsRegHead.addContent(new Element("EBEntNo").setText(GZEportCode.EB_ENT_NO));
		goodsRegHead.addContent(new Element("EBEntName").setText(GZEportCode.EB_ENT_NAME));
		goodsRegHead.addContent(new Element("OpType").setText(opType));
		goodsRegHead.addContent(new Element("CustomsCode").setText("442100"));
		goodsRegHead.addContent(new Element("CIQOrgCode").setText("5208"));
		goodsRegHead.addContent(new Element("EBPEntNo").setText(GZEportCode.EBP_ENT_NO));
		goodsRegHead.addContent(new Element("EBPEntName").setText(GZEportCode.EBP_ENT_NAME));
		goodsRegHead.addContent(new Element("CurrCode").setText(GZEportCode.CURR_CODE));
		goodsRegHead.addContent(new Element("BusinessType").setText(businessType));
		goodsRegHead.addContent(new Element("InputDate").setText(time));
		goodsRegHead.addContent(new Element("DeclTime").setText(time));
		goodsRegHead.addContent(new Element("IeFlag").setText(ieFlag));
		goodsRegHead.addContent(new Element("Notes").setText(""));
		Element goodsRegList = new Element("GoodsRegList");
		Element goodsContent = new Element("GoodsContent");
		for (int i = 0; i < list.size(); i++) {
			JSONObject map = JSONObject.fromObject(list.get(i));
			goodsContent.addContent(new Element("Seq").setText(map.get("Seq") + ""));
			goodsContent.addContent(new Element("EntGoodsNo").setText(map.get("EntGoodsNo") + ""));
			//goodsContent.addContent(new Element("EPortGoodsNo").setText(""));
			//goodsContent.addContent(new Element("CIQGoodsNo").setText(""));
			//goodsContent.addContent(new Element("CusGoodsNo").setText(""));
			goodsContent.addContent(new Element("EmsNo").setText(map.get("EmsNo")+""));
			goodsContent.addContent(new Element("ItemNo").setText(map.get("ItemNo")+""));
			goodsContent.addContent(new Element("ShelfGName").setText(map.get("ShelfGName") + ""));
			goodsContent.addContent(new Element("NcadCode").setText(map.get("NcadCode") + ""));
			goodsContent.addContent(new Element("HSCode").setText(map.get("HSCode") + ""));
			goodsContent.addContent(new Element("BarCode").setText(""));
			goodsContent.addContent(new Element("GoodsName").setText(map.get("GoodsName") + ""));
			goodsContent.addContent(new Element("GoodsStyle").setText(map.get("GoodsStyle") + ""));
			goodsContent.addContent(new Element("Brand").setText(map.get("Brand") + ""));
			goodsContent.addContent(new Element("GUnit").setText(map.get("GUnit") + ""));
			goodsContent.addContent(new Element("StdUnit").setText(map.get("StdUnit") + ""));
			goodsContent.addContent(new Element("SecUnit").setText(""));
			goodsContent.addContent(new Element("RegPrice").setText(map.get("RegPrice") + ""));
			goodsContent.addContent(new Element("GiftFlag").setText(map.get("GiftFlag") + ""));
			goodsContent.addContent(new Element("OriginCountry").setText(map.get("OriginCountry") + ""));
			goodsContent.addContent(new Element("Quality").setText(map.get("Quality") + ""));
			goodsContent.addContent(new Element("QualityCertify").setText(""));
			goodsContent.addContent(new Element("Manufactory").setText(map.get("Manufactory") + ""));
			goodsContent.addContent(new Element("NetWt").setText(map.get("NetWt") + ""));
			goodsContent.addContent(new Element("GrossWt").setText(map.get("GrossWt") + ""));
			goodsContent.addContent(new Element("Notes").setText(""));
		}
		declaration.addContent(goodsRegHead);
		goodsRegList.addContent(goodsContent);
		declaration.addContent(goodsRegList);
		// 给父节点root添加子节点;
		root.addContent(elements);
		root.addContent(declaration);
		Format format = Format.getPrettyFormat();
		XMLOutputter XMLOut = new XMLOutputter(format);
		// 获取 user.xml根路径
		String xmlpath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		System.out.println(xmlpath);
		// xml输出路径
		String uploadPath = xmlpath + strName + ".xml";
		logger.info("-----------生成XML报文路径：============" + uploadPath);
		try {
			XMLOut.output(Doc, new FileOutputStream(uploadPath));
			File file1 = new File(uploadPath);
			FtpUtils.upload(file1, "/in");
		} catch (Exception e) {
			e.printStackTrace();
			statusMap.put("status", -1);
			statusMap.put("msg", "报文发送异常，请重试 ");
		}

		return statusMap;
	}

	@Override
	public Map<String, Object> orderRecord(Object records,String opType,String ieFlag,String internetDomainName) {
		//System.out.println("=================开始处理接收的records===============================");
		Map<String, Object> checkMap = new HashMap<String, Object>();
		JSONArray jList = JSONArray.fromObject(records);
		// 将必填的字段添加到list
		List<String> noNullKeys = new ArrayList<>();

		// noNullKeys.add("InternetDomainName");//电商平台互联网域名
		noNullKeys.add("EntOrderNo");//企业电子订单编号
		noNullKeys.add("OrderStatus");//电子订单状态		
		noNullKeys.add("PayStatus");//支付状态
		noNullKeys.add("OrderGoodTotal");//订单商品总额
		noNullKeys.add("OrderGoodTotalCurr");//订单商品总额币制
		noNullKeys.add("Freight");//订单运费
		noNullKeys.add("Tax");//税款
		noNullKeys.add("OtherPayment");//抵付金额
		noNullKeys.add("ActualAmountPaid");//实际支付金额
		noNullKeys.add("RecipientName");//收货人名称
		noNullKeys.add("RecipientAddr");//收货人地址
		noNullKeys.add("RecipientTel");//收货人电话
		noNullKeys.add("RecipientCountry");//收货人所在国
		noNullKeys.add("RecipientProvincesCode");//收货人行政区代码
		noNullKeys.add("OrderDocAcount");//下单人账户
		noNullKeys.add("OrderDocName");//下单人姓名
		noNullKeys.add("OrderDocType");//下单人证件类型
		noNullKeys.add("OrderDocId");//下单人证件号
		noNullKeys.add("OrderDocTel");//下单人电话
		noNullKeys.add("OrderDate");//订单日期
		noNullKeys.add("EHSEntNo");//物流企业代码
		noNullKeys.add("EHSEntName");//物流企业名称
		noNullKeys.add("WaybillNo");//电子运单编号
		noNullKeys.add("PayEntNo");//支付企业代码
		noNullKeys.add("PayEntName");//支付企业名称
		noNullKeys.add("PayNo");//支付交易编号
		// 验证必填数据
		checkMap = CheckDatasUtil.checkData(jList, noNullKeys);
		if ((int) checkMap.get("status") != 1) {
			return checkMap;
		}
		// JSONArray 订单数据
		JSONArray list = JSONArray.fromObject(checkMap.get("datas"));
		JSONObject record = JSONObject.fromObject(list.get(0));
		JSONArray goodsList = JSONArray.fromObject(record.get("orderGoodsList"));
		noNullKeys.add("Seq");//商品序号
		noNullKeys.add("EntGoodsNo");//企业商品自编号
		noNullKeys.add("CIQGoodsNo");//检验检疫商品备案编号
		noNullKeys.add("CusGoodsNo");//海关正式备案编号 
		noNullKeys.add("GoodsName");//企业商品品名
		noNullKeys.add("GoodsStyle");//规格型号
		noNullKeys.add("OriginCountry");//原产国
		noNullKeys.add("Qty");//数量
		noNullKeys.add("Unit");//计量单位
		noNullKeys.add("Price");//单价
		noNullKeys.add("Total");//总价
		noNullKeys.add("CurrCode");//币制
	    noNullKeys.add("HSCode");//海关商品分类编号，BBC业务必填，BC业务可空
		CheckDatasUtil.checkData(goodsList, noNullKeys);
		if ((int) checkMap.get("status") != 1) {
			return checkMap;
		}
		return createOrder(list, "", opType, ieFlag,internetDomainName);
	}

	@Override
	public Map<String, Object> createOrder(JSONArray list, String path, String opType, String ieFlag,String internetDomainName)
			{
		Map<String, Object> statusMap = new HashMap<String, Object>();
		statusMap.put("status", 1);
		statusMap.put("msg", "报文发送成功 ");
		Element root = new Element("InternationalTrade");
		Document Doc = new Document(root);
		String time = DateUtil.getDate("yyyyMMddHHmmss");
		String remitSerialNumber = DateUtil.getDate("yyyyMMddHHmmss") + (int) (Math.random() * 9000 + 10000);// 自动生成交易编码：当前时间+五位随机码
		String strName = "KJ881111_YINMENG_" + remitSerialNumber;
		Element elements = new Element("Head");
		elements.addContent(new Element("MessageID").setText(strName));
		elements.addContent(new Element("MessageType").setText(GZEportCode.MESSAGE_TYPE_GOOD));
		elements.addContent(new Element("Sender").setText(GZEportCode.SENDER));
		elements.addContent(new Element("Receiver").setText(GZEportCode.RECEIVER));
		elements.addContent(new Element("SendTime").setText(time));
		elements.addContent(new Element("FunctionCode").setText(GZEportCode.FUNCTION_CODE_CIQ));
		elements.addContent(new Element("SignerInfo").setText(" "));
		elements.addContent(new Element("Version").setText(GZEportCode.VERSION));
		// 创建节点 OrderHead;
		Element declaration = new Element("Declaration");
		Element orderHead = new Element("OrderHead");
		declaration.addContent(orderHead);
		orderHead.addContent(new Element("DeclEntNo").setText(GZEportCode.DECL_ENT_NO));
		orderHead.addContent(new Element("DeclEntName").setText(GZEportCode.DECL_ENT_NAME));
		orderHead.addContent(new Element("EBEntNo").setText(GZEportCode.EB_ENT_NO));
		orderHead.addContent(new Element("EBEntName").setText(GZEportCode.EB_ENT_NAME));
		orderHead.addContent(new Element("EBPEntNo").setText(GZEportCode.EBP_ENT_NO));
		orderHead.addContent(new Element("EBPEntName").setText(GZEportCode.EBP_ENT_NAME));
		orderHead.addContent(new Element("InternetDomainName").setText(internetDomainName));
		orderHead.addContent(new Element("DeclTime").setText(time));
		orderHead.addContent(new Element("OpType").setText(opType));
		orderHead.addContent(new Element("IeFlag").setText(ieFlag));
		orderHead.addContent(new Element("CustomsCode").setText("442100"));//海关
		orderHead.addContent(new Element("CIQOrgCode").setText("5208"));//检疫局
        //订单信息  可循环
		Element orderList = new Element("OrderList");
		Element orderContent = new Element("OrderContent");
		Element orderDetail = new Element("OrderDetail");
		declaration.addContent(orderList);
		orderList.addContent(orderContent);
		for (int i = 0; i < list.size(); i++) {
			JSONObject orderObj = JSONObject.fromObject(list.get(i));
			orderDetail.addContent(new Element("EntOrderNo").setText(orderObj.get("EntOrderNo")+""));
			orderDetail.addContent(new Element("OrderStatus").setText(orderObj.get("OrderStatus")+""));
			orderDetail.addContent(new Element("PayStatus").setText(orderObj.get("PayStatus")+""));
			orderDetail.addContent(new Element("OrderGoodTotal").setText(orderObj.get("OrderGoodTotal")+""));
			orderDetail.addContent(new Element("OrderGoodTotalCurr").setText(orderObj.get("OrderGoodTotalCurr")+""));
			orderDetail.addContent(new Element("Freight").setText(orderObj.get("Freight")+""));
			orderDetail.addContent(new Element("Tax").setText(orderObj.get("Tax")+""));
			orderDetail.addContent(new Element("OtherPayment").setText(orderObj.get("OtherPayment")+""));
			orderDetail.addContent(new Element("OtherPayNotes").setText(orderObj.get("OtherPayNotes")+""));
			orderDetail.addContent(new Element("OtherCharges").setText("0"));
			orderDetail.addContent(new Element("ActualAmountPaid").setText(orderObj.get("ActualAmountPaid")+""));
			orderDetail.addContent(new Element("RecipientName").setText(orderObj.get("RecipientName")+""));
			orderDetail.addContent(new Element("RecipientAddr").setText(orderObj.get("RecipientAddr")+""));
			orderDetail.addContent(new Element("RecipientTel").setText(orderObj.get("RecipientTel")+""));
			orderDetail.addContent(new Element("RecipientCountry").setText(orderObj.get("RecipientCountry")+""));
			orderDetail.addContent(new Element("RecipientProvincesCode").setText(orderObj.get("RecipientProvincesCode")+""));
			orderDetail.addContent(new Element("OrderDocAcount").setText(orderObj.get("OrderDocAcount")+""));
			orderDetail.addContent(new Element("OrderDocName").setText(orderObj.get("OrderDocName")+""));
			orderDetail.addContent(new Element("OrderDocType").setText(orderObj.get("OrderDocType")+""));
			orderDetail.addContent(new Element("OrderDocId").setText(orderObj.get("OrderDocId")+""));
			orderDetail.addContent(new Element("OrderDocTel").setText(orderObj.get("OrderDocTel")+""));
			orderDetail.addContent(new Element("OrderDate").setText(orderObj.get("OrderDate")+""));
			orderDetail.addContent(new Element("Notes").setText(orderObj.get("Notes")+""));
			orderContent.addContent(orderDetail);
			//订单的商品信息  可循环
			Element goodsList = new Element("GoodsList");
			Element orderGoodsList = new Element("OrderGoodsList");
			JSONArray goodList = JSONArray.fromObject(orderObj.get("orderGoodsList"));
			for (int j = 0; j < goodList.size(); j++) {
				JSONObject goods = JSONObject.fromObject(goodList.get(i));
				orderGoodsList.addContent(new Element("Seq").setText(goods.get("Seq") + ""));
				orderGoodsList.addContent(new Element("EntGoodsNo").setText(goods.get("EntGoodsNo") + ""));
				orderGoodsList.addContent(new Element("CIQGoodsNo").setText(goods.get("CIQGoodsNo") + ""));
				orderGoodsList.addContent(new Element("CusGoodsNo").setText(goods.get("CusGoodsNo") + ""));
				orderGoodsList.addContent(new Element("HSCode").setText(goods.get("HSCode") + ""));
				orderGoodsList.addContent(new Element("GoodsName").setText(goods.get("GoodsName") + ""));
				orderGoodsList.addContent(new Element("GoodsStyle").setText(goods.get("GoodsStyle") + ""));
				orderGoodsList.addContent(new Element("GoodsDescribe").setText(goods.get("GoodsDescribe") + ""));
				orderGoodsList.addContent(new Element("OriginCountry").setText(goods.get("OriginCountry") + ""));
				orderGoodsList.addContent(new Element("BarCode").setText(goods.get("BarCode") + ""));
				orderGoodsList.addContent(new Element("Brand").setText(goods.get("Brand") + ""));
				orderGoodsList.addContent(new Element("Qty").setText(goods.get("Qty") + ""));
				orderGoodsList.addContent(new Element("Unit").setText(goods.get("Unit") + ""));
				orderGoodsList.addContent(new Element("Price").setText(goods.get("Price") + ""));
				orderGoodsList.addContent(new Element("Total").setText(goods.get("Total") + ""));
				orderGoodsList.addContent(new Element("CurrCode").setText(goods.get("CurrCode") + ""));
				orderGoodsList.addContent(new Element("Notes").setText(goods.get("Notes") + ""));
			}
			orderDetail.addContent(goodsList);
			goodsList.addContent(orderGoodsList);
			//关联运单表
			Element orderWaybillRel = new Element("OrderWaybillRel");
			orderWaybillRel.addContent(new Element("EHSEntNo").setText(orderObj.get("EHSEntNo")+""));
			orderWaybillRel.addContent(new Element("EHSEntName").setText(orderObj.get("EHSEntName")+""));
			orderWaybillRel.addContent(new Element("WaybillNo").setText(orderObj.get("WaybillNo")+""));
			orderWaybillRel.addContent(new Element("Notes").setText(orderObj.get("Notes")+""));
			orderContent.addContent(orderWaybillRel);
			//关联支付表
			Element orderPaymentRel = new Element("OrderPaymentRel");
			orderPaymentRel.addContent(new Element("PayEntNo").setText(orderObj.get("PayEntNo")+""));
			orderPaymentRel.addContent(new Element("PayEntName").setText(orderObj.get("PayEntName")+""));
			orderPaymentRel.addContent(new Element("PayNo").setText(orderObj.get("PayNo")+""));
			orderPaymentRel.addContent(new Element("Notes").setText(orderObj.get("Notes")+""));
			orderContent.addContent(orderPaymentRel);
		}
		//给父节点root添加子节点;
		root.addContent(elements);
		root.addContent(declaration);
		// 格式化
		Format format = Format.getPrettyFormat();
		XMLOutputter XMLOut = new XMLOutputter(format);
		// 输出 user.xml 文件；
		String outPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		String uploadPath =""+ outPath + strName + ".xml";
		try {
			XMLOut.output(Doc, new FileOutputStream(uploadPath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			statusMap.put("status", -1);
			statusMap.put("msg", "上送报文失败，请重试 ");
		} catch (IOException e) {
			e.printStackTrace();
			statusMap.put("status", -2);
			statusMap.put("msg", "上送报文出错，请重试 ");
		}
		return statusMap;
	}

	@Override
	public Map<String, Object> payRecord(Object records, String opType) {
	//	System.out.println("=================开始处理接收的支付records===============================");
		Map<String, Object> checkMap = new HashMap<String, Object>();
		JSONArray jList = JSONArray.fromObject(records);
		List<String> noNullKeys = new ArrayList<>();
		noNullKeys.add("EntPayNo");
		noNullKeys.add("PayStatus");
		noNullKeys.add("PayAmount");
		noNullKeys.add("PayCurrCode");
		noNullKeys.add("PayTime");
		noNullKeys.add("PayerName");
		noNullKeys.add("PayerDocumentType");
		noNullKeys.add("PayerDocumentNumber");
		noNullKeys.add("EntOrderNo");
		noNullKeys.add("EBPEntNo");
		noNullKeys.add("EBPEntName");
		checkMap = CheckDatasUtil.checkData(jList, noNullKeys);
		if ((int) checkMap.get("status") != 1) {
			return checkMap;
		}
		JSONArray list = JSONArray.fromObject(checkMap.get("datas"));
		checkMap = createPay(list, "",opType);
		return checkMap;
	}

	@Override
	public Map<String, Object> createPay(JSONArray list, String path, String opType) {
		System.out.println("=================开始处理接收的JSonList生成XML文件===============================");
		Map<String, Object> statusMap = new HashMap<String, Object>();
		statusMap.put("status", 1);
		statusMap.put("msg", "报文发送成功 ");
		Element root = new Element("InternationalTrade");
		Document Doc = new Document(root);
		String time = DateUtil.getDate("yyyyMMddHHmmss");
		String remitSerialNumber = DateUtil.getDate("yyyyMMddHHmmss") + (int) (Math.random() * 9000 + 10000);// 自动生成交易编码：当前时间+五位随机码
		String strName = "KJ881112_YINMENG_" + remitSerialNumber;
		Element elements = new Element("Head");
		elements.addContent(new Element("MessageID").setText(strName));
		elements.addContent(new Element("MessageType").setText(GZEportCode.MESSAGE_TYPE_GOOD));
		elements.addContent(new Element("Sender").setText(GZEportCode.SENDER));
		elements.addContent(new Element("Receiver").setText(GZEportCode.RECEIVER));
		elements.addContent(new Element("SendTime").setText(time));
		elements.addContent(new Element("FunctionCode").setText(GZEportCode.FUNCTION_CODE_CIQ));
		elements.addContent(new Element("SignerInfo").setText(" "));
		elements.addContent(new Element("Version").setText(GZEportCode.VERSION));
		//支付信息报文头
		Element declaration = new Element("Declaration");
		Element paymentHead = new Element("PaymentHead");
		paymentHead.addContent(new Element("DeclEntNo").setText(GZEportCode.DECL_ENT_NO));
		paymentHead.addContent(new Element("DeclEntName").setText(GZEportCode.DECL_ENT_NAME));
		paymentHead.addContent(new Element("PayEntNo").setText("C100085134"));//支付企业备案号
		paymentHead.addContent(new Element("PayEntName").setText("银盛支付"));
		paymentHead.addContent(new Element("DeclTime").setText(time));
		paymentHead.addContent(new Element("OpType").setText(opType));
		paymentHead.addContent(new Element("CustomsCode").setText(""));
		paymentHead.addContent(new Element("CIQOrgCode").setText(""));
		declaration.addContent(paymentHead);
		//支付信息
		Element paymentList = new Element("PaymentList");
		Element paymentDetail = new Element("PaymentDetail");
		for (int i = 0; i < list.size(); i++) {
			JSONObject pay = JSONObject.fromObject(list.get(i));
			paymentDetail.addContent(new Element("EntPayNo").setText(pay.get("EntPayNo")+""));
			paymentDetail.addContent(new Element("PayStatus").setText(pay.get("PayStatus")+""));
			paymentDetail.addContent(new Element("PayAmount").setText(pay.get("PayAmount")+""));
			paymentDetail.addContent(new Element("PayCurrCode").setText(pay.get("PayCurrCode")+""));
			paymentDetail.addContent(new Element("PayTime").setText(pay.get("PayTime")+""));
			paymentDetail.addContent(new Element("PayerName").setText(pay.get("PayerName")+""));
			paymentDetail.addContent(new Element("PayerDocumentType").setText(pay.get("PayerDocumentType")+""));
			paymentDetail.addContent(new Element("PayerDocumentNumber").setText(pay.get("PayerDocumentNumber")+""));
			paymentDetail.addContent(new Element("PayerPhoneNumber").setText(pay.get("PayerPhoneNumber")+""));
			paymentDetail.addContent(new Element("EntOrderNo").setText(pay.get("EntOrderNo")+""));
			paymentDetail.addContent(new Element("EBPEntNo").setText(pay.get("EBPEntNo")+""));
			paymentDetail.addContent(new Element("EBPEntName").setText(pay.get("EBPEntName")+""));
			paymentDetail.addContent(new Element("Notes").setText(pay.get("Notes")+""));
		}
		paymentList.addContent(paymentDetail);
		declaration.addContent(paymentList);
		root.addContent(elements);
		root.addContent(declaration);
		Format format = Format.getPrettyFormat();
		XMLOutputter XMLOut = new XMLOutputter(format);
		String outPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		String uploadPath = "" + outPath + strName + ".xml";
		try {
			XMLOut.output(Doc, new FileOutputStream(uploadPath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			statusMap.put("status", -4);
			statusMap.put("msg", "上送报文失败，请重试");
			return statusMap;
		} catch (IOException e) {
			e.printStackTrace();
			statusMap.put("status", -5);
			statusMap.put("msg", "上送报文出错，请重试");
			return statusMap;
		}
		return statusMap;
	}
}

package org.silver.pay.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.silver.common.GZEportCode;
import org.silver.pay.api.GZPayEportService;
import org.silver.pay.dao.PaymentDetailDao;
import org.silver.pay.dao.PaymentHeadDao;
import org.silver.pay.model.PaymentDetail;
import org.silver.pay.model.PaymentHead;
import org.silver.pay.util.CheckDatasUtil;
import org.silver.util.DateUtil;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service(interfaceClass = GZPayEportService.class)
public class GZPayEportServiceImpl implements GZPayEportService {

	@Resource
	private PaymentHeadDao paymentHeadDao;
	@Resource
	private PaymentDetailDao paymentDetailDao;
	
	@Override
	public Map<String, Object> payRecord(Object records,String opType,String customsCode,String ciqOrgCode,String tenantNo,String notifyurl) {
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
		checkMap = createPay(list, opType, customsCode, ciqOrgCode,tenantNo,notifyurl);
		return checkMap;
	}

	private PaymentHead savePaymentHead(String messageID,String time,Date now,String opType,String customsCode,String ciqOrgCode,String tenantNo,String notifyurl){
		PaymentHead paymentHead = new PaymentHead();
		paymentHead.setDeclEntNo(GZEportCode.PAY_ENT_NO);// 申报企业编号
		paymentHead.setDeclEntName(GZEportCode.PAY_ENT_NAME);// 申报企业名称
		paymentHead.setPayEntNo(GZEportCode.PAY_ENT_NO);//
		paymentHead.setPayEntName(GZEportCode.PAY_ENT_NAME);//
		paymentHead.setDeclTime(time);// 申报时间
		paymentHead.setOpType(opType);// 操作方式
		paymentHead.setCustomsCode(customsCode);// 主管海关代码
		paymentHead.setCIQOrgCode(ciqOrgCode);// 检验检疫机构代码
		paymentHead.setOrgMessageID(messageID);
		paymentHead.setUrl(notifyurl);
		paymentHead.setTenantNo(tenantNo);
		paymentHead.setEport(1);
		paymentHead.setCreate_date(now);
		paymentHead.setDel_flag(0);
		if(paymentHeadDao.add(paymentHead)){
			return paymentHead;
		}
		return null;
	}
	
	private List<PaymentDetail> savePaymentDetail(JSONArray list,String messageID,Date now){
		List<PaymentDetail> paymentDetailList = new ArrayList<>();
		PaymentDetail paymentDetail = null;
		for (int i = 0; i < list.size(); i++) {
			paymentDetail = new PaymentDetail();
			JSONObject map = JSONObject.fromObject(list.get(i));
			paymentDetail = (PaymentDetail) jsonChangeToEntity(map, paymentDetail);
			paymentDetail.setOrgMessageID(messageID);
			paymentDetail.setDel_flag(0);
			paymentDetail.setCreate_date(now);
			if (paymentDetailDao.add(paymentDetail)) {
				paymentDetailList.add(paymentDetail);
			} else {
				return null;
			}
		}
		return paymentDetailList;
	}
	/**
	 * json转数据表实体
	 * @param json
	 * @param obj
	 * @return
	 */
	public static Object jsonChangeToEntity(JSONObject json, Object obj) {
		String key = "";
		String type = "";
		Object value = null;
		for (Field field : obj.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			try {
				key = field.getName();
				if (json.get(field.getName()) != null) {
					Method method = obj.getClass().getDeclaredMethod("set" + key, field.getType());
					type = field.getType().getSimpleName();
					value = json.get(key);
					if (type.equals("int") || type.equals("Integer")) {
						value = Integer.parseInt(value + "");
					} else if (type.equals("long") || type.equals("Long")) {
						value = Long.parseLong(value + "");
					} else if (type.equals("double") || type.equals("Double")) {
						value = Double.parseDouble(value + "");
					}
					method.invoke(obj, value);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return obj;
	}
	
	@Override
	public Map<String, Object> createPay(JSONArray list,String opType,String customsCode,String ciqOrgCode,String tenantNo,String notifyurl) {
		System.out.println("=================开始处理接收的JSonList生成XML文件===============================");
		Map<String, Object> statusMap = new HashMap<String, Object>();
		List<PaymentDetail> paymentList =new ArrayList<>();
		Date now = new Date();
		String time = DateUtil.getDate("yyyyMMddHHmmss");
		String remitSerialNumber = DateUtil.getDate("yyyyMMddHHmmss") + (int) (Math.random() * 9000 + 10000);// 自动生成交易编码：当前时间+五位随机码
		String messageID = "KJ881112_YINMENG_" + remitSerialNumber;
		//生成支付报文头
		PaymentHead paymentHead=savePaymentHead(messageID,time,now,opType,customsCode,ciqOrgCode,tenantNo,notifyurl);
		if(paymentHead!=null){//存储支付信息
			paymentList =savePaymentDetail(list, messageID, now);
		}
		if(paymentHead!=null&& paymentList.size()>0){//生成XML报文
			statusMap=convertPaymentRecordChangeToXML(paymentHead, paymentList);
		}
		statusMap.put("messageID", messageID);
		return statusMap;
	}
	
	public Map<String, Object> convertPaymentRecordChangeToXML(PaymentHead payHead,List<PaymentDetail> payList) {
		System.out.println("=================开始处理接收的JSonList生成XML文件===============================");
		Map<String, Object> statusMap = new HashMap<String, Object>();
		String messageID=payHead.getOrgMessageID();
		Element root = new Element("InternationalTrade");
		Document Doc = new Document(root);
		Element elements = new Element("Head");
		elements.addContent(new Element("MessageID").setText(messageID));
		elements.addContent(new Element("MessageType").setText(GZEportCode.MESSAGE_TYPE_PAY));
		elements.addContent(new Element("Sender").setText(GZEportCode.SENDER_PAY));
		elements.addContent(new Element("Receiver").setText(GZEportCode.RECEIVER));
		elements.addContent(new Element("SendTime").setText(payHead.getDeclTime()));
		elements.addContent(new Element("FunctionCode").setText(GZEportCode.FUNCTION_CODE_CIQ));
		elements.addContent(new Element("SignerInfo").setText(" "));
		elements.addContent(new Element("Version").setText(GZEportCode.VERSION));
		// 支付信息报文头
		Element declaration = new Element("Declaration");
		List slist = new ArrayList<>();
		slist.add("serialVersionUID");
		slist.add("id");
		slist.add("count");
		slist.add("eport");
		slist.add("status");
		slist.add("OrgMessageID");
		slist.add("del_flag");
		slist.add("create_date");
		Element paymentHead = entityChangeToXmlElement(payHead, "PaymentHead", slist);
		declaration.addContent(paymentHead);
		// 支付信息
		Element paymentList = new Element("PaymentList");
		for (int i = 0; i < payList.size(); i++) {
			PaymentDetail payDetail =payList.get(i);
			List elist = new ArrayList<>();
			elist.add("serialVersionUID");
			elist.add("id");
			elist.add("OrgMessageID");
			elist.add("count");
			elist.add("status");
			elist.add("del_flag");
			elist.add("create_date");
			Element paymentDetail = entityChangeToXmlElement(payDetail, "PaymentDetail", elist);
			paymentList.addContent(paymentDetail);
		}
		declaration.addContent(paymentList);
		root.addContent(elements);
		root.addContent(declaration);
		Format format = Format.getPrettyFormat();
		XMLOutputter XMLOut = new XMLOutputter(format);
		String outPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		String fileName = messageID + ".xml";
		String ePath = outPath+"gz_pay\\"+DateUtil.getDate("yyyyMMdd");
		File uploadFile = new File(ePath); //
		if (!uploadFile.exists() || uploadFile == null) { //
			uploadFile.mkdirs();
		}
		ePath = uploadFile.getPath() + "\\" + fileName;
		System.out.println(("生成的路径为： " + ePath));
		if (createLocalXMLFile(Doc, ePath)) {
			statusMap.put("status", 1);
			statusMap.put("msg", "受理成功");
			statusMap.put("path", outPath);
			return statusMap;
		}
		statusMap.put("status", -1);
		statusMap.put("msg", "本地存储失败");
		return statusMap;
	}

	/**
	 * 生成本地xml文件
	 * 
	 * @param doc
	 * @param savePath
	 * @return
	 */
	private boolean createLocalXMLFile(Document doc, String savePath) {
		Format format = Format.getPrettyFormat();
		XMLOutputter XMLOut = new XMLOutputter(format);
		try {
			XMLOut.output(doc, new FileOutputStream(savePath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 数据实体转XML报文节点
	 * 
	 * @param obj
	 *            数据实体
	 * @param elementName
	 *            节点
	 * @param untiList
	 *            实体不需要转节点的元素List
	 * @return
	 */
	private Element entityChangeToXmlElement(Object obj, String elementName, List<String> untiList) {
		Element elements = new Element(elementName);
		for (Field field : obj.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			try {
				if (!untiList.contains(field.getName())) {
					if (field.get(obj) != null && !(field.get(obj) + "").trim().equals("")
							&& !(field.get(obj) + "").trim().equals("null")) {
						elements.addContent(new Element(field.getName()).setText(field.get(obj) + ""));
					}
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return elements;
	}
	
	
	
}

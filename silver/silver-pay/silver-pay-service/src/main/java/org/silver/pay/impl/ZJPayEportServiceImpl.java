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
import org.silver.common.NSEportCode;
import org.silver.pay.api.ZJPayEportService;
import org.silver.pay.dao.PaymentDetailDao;
import org.silver.pay.dao.PaymentHeadDao;
import org.silver.pay.model.PaymentDetail;
import org.silver.pay.model.PaymentHead;
import org.silver.pay.util.FtpUtil;
import org.silver.util.DateUtil;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service(interfaceClass = ZJPayEportService.class)
public class ZJPayEportServiceImpl implements ZJPayEportService {
	@Resource
	private PaymentHeadDao paymentHeadDao;
	@Resource
	private PaymentDetailDao paymentDetailDao;
	
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
		paymentHead.setTenantNo(tenantNo);
		paymentHead.setUrl(notifyurl);
		paymentHead.setEport(2);
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

	@Override
	public Map<String, Object> zjCreatePayRecord(Object obj, String path, String opType,String customsCode,String ciqOrgCode,String tenantNo,String notifyurl) {
		Map<String, Object> statusMap = new HashMap<String, Object>();
		String time = DateUtil.getDate("yyyyMMddHHmmss");
		JSONArray jList = JSONArray.fromObject(obj);
		List<PaymentDetail> paymentList =new ArrayList<>();
		Date now = new Date();
		String remitSerialNumber = DateUtil.getDate("yyyyMMddHHmmssSSS") + (int) (Math.random() * 9000 + 1000);// 自动生成交易编码：当前时间+四位随机码
		String messageID = "YINMENG_" + remitSerialNumber;
		PaymentHead paymentHead=savePaymentHead(messageID, time, now, opType, customsCode, ciqOrgCode,tenantNo,notifyurl);
		if(paymentHead!=null){
			paymentList =savePaymentDetail(jList, messageID, now);
		}
		if(paymentHead!=null&& paymentList.size()>0){//生成XML报文
			statusMap=createPayRecordChangeToXML(paymentHead, paymentList);
		}
		statusMap.put("messageID", messageID);
		return statusMap;
	}
	
	public Map<String, Object> createPayRecordChangeToXML(PaymentHead paymentHead,List<PaymentDetail> list) {
		Map<String, Object> statusMap = new HashMap<String, Object>();
		Element root = new Element("ROOT");
		// 头部文件
		Element Head = new Element("Head");
		Head.addContent(new Element("MessageID").setText(paymentHead.getOrgMessageID()));
		Head.addContent(new Element("MessageType").setText(NSEportCode.MESSAGE_TYPE_PAY));
		Head.addContent(new Element("Sender").setText(NSEportCode.ENT_RECORD_NO));
		Head.addContent(new Element("Receiver").setText(NSEportCode.RECEIVER));
		Head.addContent(new Element("SendTime").setText(paymentHead.getDeclTime()));
		Head.addContent(new Element("FunctionCode").setText(""));
		Head.addContent(new Element("Version").setText("1.0"));

		Element Body = new Element("Body");
		Element SwbPayment = new Element("SwbPayment");
		PaymentDetail paymentDetail =null;
		for (int i = 0; i < list.size(); i++) {
			paymentDetail = list.get(i);
			Element Record = new Element("Record");
			Record.addContent(new Element("Payno").setText(paymentDetail.getEntPayNo()));//支付编号
			Record.addContent(new Element("Ciqbcode").setText(NSEportCode.CIQ_CODE));//国检组织机构代码
			Record.addContent(new Element("PaypComcode").setText(NSEportCode.ENT_RECORD_NO));//支付企业备案号
			Record.addContent(new Element("CbepComcode").setText("1509007917"));//跨境电商平台企业备案号
			Record.addContent(new Element("Enordercode").setText(paymentDetail.getEntOrderNo()));//电子交易订单号
			Record.addContent(new Element("PayorName").setText(paymentDetail.getPayerName()));//付款人
			Record.addContent(new Element("PayorAccount").setText(""));//付款账号
			Record.addContent(new Element("ShipperName").setText(""));//收款人
			Record.addContent(new Element("OrderFcy").setText(String.valueOf(paymentDetail.getPayAmount())));//订单金额
			Record.addContent(new Element("OrderFcode").setText("CNY"));//订单币种
			Record.addContent(new Element("ReceiveNo").setText(paymentDetail.getPayerDocumentNumber()));//付款人证件号
			Record.addContent(new Element("PayFcy").setText(String.valueOf(paymentDetail.getPayAmount())));//支付金额
			Record.addContent(new Element("PayFcode").setText("CNY"));//支付币种
			Record.addContent(new Element("PayDate").setText(paymentDetail.getPayTime()));//支付日期
			Record.addContent(new Element("Shippercompcode").setText(NSEportCode.ENT_RECORD_NO));//申报企业备案号
			Record.addContent(new Element("DrDate").setText(paymentHead.getDeclTime()));
			SwbPayment.addContent(Record);
		}
		Body.addContent(SwbPayment);
		root.addContent(Head);
		root.addContent(Body);
		// 根节点添加到文档中；
		Document Doc = new Document(root);

		// 格式化
		Format format = Format.getPrettyFormat();
		XMLOutputter XMLOut = new XMLOutputter(format);
		// System.out.println(XMLOut.outputString(Doc));
		// 输出 user.xml 文件；
		String fileName = "661107_" + paymentHead.getDeclTime() + "001.xml";
		System.out.println("生成的文件名为：" + fileName);
		String str=Thread.currentThread().getContextClassLoader().getResource("").getPath();
		String ePath = str+"zj_pay\\"+DateUtil.getDate("yyyyMMdd");
		File uploadFile = new File(ePath); //
		if (!uploadFile.exists() || uploadFile == null) { //
			uploadFile.mkdirs();
		}
		ePath = uploadFile.getPath() + "\\" + fileName;
		System.out.println("生成的路径为：+" + ePath);
		File file1 = new File(ePath);
		if(createLocalXMLFile(Doc, ePath)){
			statusMap.put("status", 1);
			statusMap.put("msg", "受理成功 ");
			statusMap.put("path", ePath);
			return statusMap;
		}
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

	  /**
     * 上送本地文件至ftp服务器
     * @param filePath  读取本地文件的路径
     * @param url
     * @param port
     * @param username
     * @param password
     * @param routePath  文件存储到FTP的路径
     * @return
     */
	private boolean uploadXMLFile(String filePath,String url,int port,String username,String password,String routePath) {
		try {
			File file = new File(filePath);
			FtpUtil.upload(url,port,username,password,routePath,file);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}

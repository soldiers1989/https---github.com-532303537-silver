package org.silver.pay.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.silver.common.NSEportCode;
import org.silver.pay.api.ZJPayEportService;
import org.silver.pay.util.FtpUtil;
import org.silver.util.DateUtil;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service(interfaceClass = ZJPayEportService.class)
public class ZJPayEportServiceImpl implements ZJPayEportService {

	@Override
	public Map<String, Object> zjCreatePayRecord(Object obj, String path, String opType) {
		Map<String, Object> statusMap = new HashMap<String, Object>();
		statusMap.put("status", 1);
		statusMap.put("msg", "报文发送成功 ");
		JSONObject record = null;
		JSONArray list = JSONArray.fromObject(obj);
		String time = DateUtil.getDate("yyyyMMddHHmmss");
		String remitSerialNumber = DateUtil.getDate("yyyyMMddHHmmssSSS") + (int) (Math.random() * 9000 + 1000);// 自动生成交易编码：当前时间+四位随机码
		Element root = new Element("ROOT");
		// 头部文件
		Element Head = new Element("Head");
		Head.addContent(new Element("MessageID").setText("YINMENG_" + remitSerialNumber));
		Head.addContent(new Element("MessageType").setText(NSEportCode.MESSAGE_TYPE_PAY));
		Head.addContent(new Element("Sender").setText(NSEportCode.ENT_RECORD_NO));
		Head.addContent(new Element("Receiver").setText(NSEportCode.RECEIVER));
		Head.addContent(new Element("SendTime").setText(time));
		Head.addContent(new Element("FunctionCode").setText(""));
		Head.addContent(new Element("Version").setText("1.0"));

		Element Body = new Element("Body");
		Element SwbPayment = new Element("SwbPayment");
		for (int i = 0; i < list.size(); i++) {
			record = list.getJSONObject(i);
			Element Record = new Element("Record");
			Record.addContent(new Element("Payno").setText(record.getString("EntPayNo")));//支付编号
			Record.addContent(new Element("Ciqbcode").setText(NSEportCode.CIQ_CODE));//国检组织机构代码
			Record.addContent(new Element("PaypComcode").setText(NSEportCode.ENT_RECORD_NO));//支付企业备案号
			Record.addContent(new Element("CbepComcode").setText("1509007917"));//跨境电商平台企业备案号
			Record.addContent(new Element("Enordercode").setText(record.getString("EntOrderNo")));//电子交易订单号
			Record.addContent(new Element("PayorName").setText(record.getString("OrderDocName")));//付款人
			Record.addContent(new Element("PayorAccount").setText(""));//付款账号
			Record.addContent(new Element("ShipperName").setText(""));//收款人
			Record.addContent(new Element("OrderFcy").setText(record.getString("OrderGoodTotal")));//订单金额
			Record.addContent(new Element("OrderFcode").setText("CNY"));//订单币种
			Record.addContent(new Element("ReceiveNo").setText(record.getString("OrderDocId")));//付款人证件号
			Record.addContent(new Element("PayFcy").setText(record.getString("ActualAmountPaid")));//支付金额
			Record.addContent(new Element("PayFcode").setText("CNY"));//支付币种
			Record.addContent(new Element("PayDate").setText(record.getString("PayTime")));//支付日期
			Record.addContent(new Element("Shippercompcode").setText(NSEportCode.ENT_RECORD_NO));//申报企业备案号
			Record.addContent(new Element("DrDate").setText(time));
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
		String fileName = "661107_" + time + "001.xml";
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
			statusMap.put("msg", "存储成功 ");
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

package org.silver.sys.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.silver.sys.api.NSEportService;
import org.silver.util.DateUtil;
import org.silver.util.FtpUtils;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONObject;

@Service(interfaceClass = NSEportService.class)
public class NSEportServiceImpl implements NSEportService {
	public static final String Sender = "1509007917";// 银盟企业备案号

	@Override
	public Map<String, Object> createEportXML(int type, Object jlist) {
		Element root = new Element("Root");
		String time = DateUtil.getDate("yyyyMMddHHmmss");
		System.out.println(time+"------->");
		String remitSerialNumber = DateUtil.getDate("yyyyMMddHHmmssSSS") + (int) (Math.random() * 9000 + 1000);// 自动生成交易编码：当前时间+四位随机码
		// 头部文件
		Element Head = new Element("Head");
		Head.addContent(new Element("MessageID").setText("YINMENG_" + remitSerialNumber));
		Head.addContent(new Element("MessageType").setText("661105"));
		Head.addContent(new Element("Sender").setText("1500003643"));
		Head.addContent(new Element("Receiver").setText("ICIP"));
		Head.addContent(new Element("SendTime").setText(time));
		Head.addContent(new Element("FunctionCode").setText(""));
		Head.addContent(new Element("Version").setText("1.0"));

		Element Body = new Element("Body");
		Element GOODSRECORD = new Element("GOODSRECORD");
		Element Record = new Element("Record");
		Record.addContent(new Element("CargoBcode").setText("YMSP_17060614674069"));
		Record.addContent(new Element("Ciqbcode").setText("000069"));// 南沙本局
		Record.addContent(new Element("CbeComcode").setText("1500003643"));
		Record.addContent(new Element("Remark").setText(""));
		Record.addContent(new Element("Editccode").setText("1500003643"));
		Record.addContent(new Element("OperType").setText("A"));

		Element CARGOLIST = new Element("CARGOLIST");
		// 循环放商品信息
		List<Map<String, Object>> list = (List<Map<String, Object>>) jlist;
		for (int i = 0; i < list.size(); i++) {
			Map<String, Object> map = list.get(i);
			System.out.println("=============================================");
			System.out.println(JSONObject.fromObject(map));
			System.out.println("=============================================");
			Element Records = new Element("Record");// 循环节点放商品信息
			Records.addContent(new Element("Gcode").setText("Entc-201611052138"));
			Records.addContent(new Element("Gname").setText(map.get("ShelfGName") + ""));
			Records.addContent(new Element("Spec").setText(map.get("Spec") + ""));
			Records.addContent(new Element("Hscode").setText("1901101000"));
			Records.addContent(new Element("Unit").setText(map.get("Unit") + ""));
			Records.addContent(new Element("GoodsBarcode").setText(map.get("GoodsBarcode") + ""));
			Records.addContent(new Element("GoodsDesc").setText(map.get("GoodsDesc") + ""));
			Records.addContent(new Element("Remark").setText(map.get("Remark") + ""));
			Records.addContent(new Element("ComName").setText(map.get("ComName") + ""));
			Records.addContent(new Element("Manufactureraddr").setText(map.get("Manufactureraddr") + ""));// 食品类必填
			Records.addContent(new Element("Brand").setText(map.get("Brand") + ""));
			Records.addContent(new Element("AssemCountry").setText(map.get("AssemCountry") + ""));
			Records.addContent(new Element("Ingredient").setText(map.get("Ingredient") + ""));
			Records.addContent(new Element("Additiveflag").setText(map.get("Additiveflag") + ""));
			Records.addContent(new Element("Poisonflag").setText(map.get("Poisonflag") + ""));
			CARGOLIST.addContent(Records);// 将循环完的商品信息放入父节点
		}
		Record.addContent(CARGOLIST);
		GOODSRECORD.addContent(Record);
		Body.addContent(GOODSRECORD);
		root.addContent(Head);
		root.addContent(Body);
		// 根节点添加到文档中；
		Document Doc = new Document(root);
		// logger.info("=================开始生成XML==============================");
		Format format = Format.getPrettyFormat();
		XMLOutputter XMLOut = new XMLOutputter(format);
		String fileName = "661105_" + time + "001.xml";
		String path = "D://work/";
		File uploadFile = new File(path); //
		if (!uploadFile.exists() || uploadFile == null) { //
			uploadFile.mkdirs();
		}
		path = uploadFile.getPath() + "\\" + fileName;
		System.out.println(("生成的路径为：+" + path));
		try {
			XMLOut.output(Doc, new FileOutputStream(path));
			pushEportData(path,"/4200.IMPBA.SWBCARGOBACK.REPORT/in");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Map<String, Object> saveEportData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> pushEportData(String localPath,String serverWorkPath) {
		//上传到南沙海关的FTP中
		File file1 = new File(localPath);
		try {
			FtpUtils.upload(file1,serverWorkPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}

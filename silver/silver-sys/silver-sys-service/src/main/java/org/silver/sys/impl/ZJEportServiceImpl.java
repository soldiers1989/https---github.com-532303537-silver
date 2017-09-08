package org.silver.sys.impl;

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
import org.silver.common.NSFtpConfig;
import org.silver.sys.api.ZJEportService;
import org.silver.sys.util.FtpUtil;
import org.silver.util.DateUtil;
import org.silver.util.FtpUtils;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service(interfaceClass = ZJEportService.class)
public class ZJEportServiceImpl implements ZJEportService {

	@Override
	public Map<String, Object> zjCreateGoodsRecord(Object obj, String path, String opType, String businessType,
			String ieFlag) {
		Map<String, Object> statusMap = new HashMap<String, Object>();
		statusMap.put("status", 1);
		statusMap.put("msg", "报文发送成功 ");
		Element root = new Element("Root");
		String time = DateUtil.getDate("yyyyMMddHHmmss");
		System.out.println(time + "------->");
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
		Record.addContent(new Element("OperType").setText(opType));

		Element CARGOLIST = new Element("CARGOLIST");
		JSONArray list = JSONArray.fromObject(obj);
		// 循环放商品信息
		for (int i = 0; i < list.size(); i++) {
			JSONObject map = JSONObject.fromObject(list.get(i));
			Element Records = new Element("Record");// 循环节点放商品信息
			Records.addContent(new Element("Gcode").setText(map.get("EntGoodsNo") + ""));// 商品货号
			Records.addContent(new Element("Gname").setText(map.get("ShelfGName") + ""));// 商品名称
			Records.addContent(new Element("Spec").setText(map.get("GoodsStyle") + ""));// 规格型号
			Records.addContent(new Element("Hscode").setText(map.get("HSCode") + ""));
			Records.addContent(new Element("Unit").setText(map.get("GUnit") + ""));// 计量单位
			Records.addContent(new Element("GoodsBarcode").setText(map.get("BarCode") + ""));// 商品条形码
			Records.addContent(new Element("GoodsDesc").setText(map.get("Quality") + ""));// 商品描述
			Records.addContent(new Element("Remark").setText(map.get("Notes") + ""));// 备注
			Records.addContent(new Element("ComName").setText(map.get("Manufactory") + ""));// 生产厂家
			Records.addContent(new Element("Manufactureraddr").setText(map.get("Manufactory") + ""));// 食品类必填
			Records.addContent(new Element("Brand").setText(map.get("Brand") + ""));// 品牌
			Records.addContent(new Element("AssemCountry").setText(map.get("OriginCountry") + ""));// 原产国
			if (map.get("Ingredient") != null && !"".equals(map.get("Ingredient").toString().trim())) {
				Records.addContent(new Element("Ingredient").setText(map.get("Ingredient") + ""));// 成分
			} else {
				Records.addContent(new Element("Ingredient").setText("无"));
			}
			if (map.get("Additiveflag") != null && !"".equals(map.get("Additiveflag").toString().trim())) {
				Records.addContent(new Element("Additiveflag").setText(map.get("Additiveflag") + ""));// 超范围使用食品添加剂为空时默认“无”
			} else {
				Records.addContent(new Element("Additiveflag").setText("无"));
			}
			if (map.get("Poisonflag") != null && !"".equals(map.get("Poisonflag").toString().trim())) {
				Records.addContent(new Element("Poisonflag").setText(map.get("Poisonflag") + ""));// 含有毒害物质为空时默认“无”
			} else {
				Records.addContent(new Element("Poisonflag").setText("无"));
			}
			CARGOLIST.addContent(Records);// 将循环完的商品信息放入父节点
		}
		Record.addContent(CARGOLIST);
		GOODSRECORD.addContent(Record);
		Body.addContent(GOODSRECORD);
		root.addContent(Head);
		root.addContent(Body);
		// 根节点添加到文档中；
		Document Doc = new Document(root);
		Format format = Format.getPrettyFormat();
		XMLOutputter XMLOut = new XMLOutputter(format);
		String fileName = "661105_" + time + "001.xml";
		String str=Thread.currentThread().getContextClassLoader().getResource("").getPath();
		String ePath = str+"goods\\"+DateUtil.getDate("yyyyMMdd");
		File uploadFile = new File(ePath); //
		if (!uploadFile.exists() || uploadFile == null) { //
			uploadFile.mkdirs();
		}
		ePath = uploadFile.getPath() + "\\" + fileName;
		System.out.println(("生成的路径为： " + ePath));
		File file1 = new File(ePath);
		if(createLocalXMLFile(Doc, ePath)){
			statusMap.put("status", 1);
			statusMap.put("msg", "存储成功 ");
			statusMap.put("path", ePath);
			return statusMap;
		}
	    //上传文件
		try {
			FtpUtil.upload(NSFtpConfig.FTP_ID, NSFtpConfig.FTP_PORT,
					NSFtpConfig.FTP_USER_NAME_YM,
					NSFtpConfig.FTP_PASS_WORD_YM, 
					NSFtpConfig.FTP_GOODS_ROUTE_IN,
					file1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return statusMap;
	}

	@Override
	public Map<String, Object> zjCreateOrderRecord(Object obj, String path, String opType, String ieFlag) {
		Map<String, Object> statusMap = new HashMap<String, Object>();
		statusMap.put("status", 1);
		statusMap.put("msg", "报文发送成功 ");
		JSONObject record = null;
		String seq = "";
		JSONArray list = JSONArray.fromObject(obj);
		for (int i = 0; i < list.size(); i++) {
			System.out.println("=================开始处理接收的JSonList生成XML文件===============================");
			Element root = new Element("ROOT");
			String time = DateUtil.getDate("yyyyMMddHHmmss");
			String remitSerialNumber = DateUtil.getDate("yyyyMMddHHmmssSSS") + (int) (Math.random() * 9000 + 1000);// 自动生成交易编码：当前时间+四位随机码
			// 头部文件
			Element Head = new Element("Head");
			Head.addContent(new Element("MessageID").setText("YINMENG_" + remitSerialNumber));
			Head.addContent(new Element("MessageType").setText("661101"));
			Head.addContent(new Element("Sender").setText("1509007917"));
			Head.addContent(new Element("Receiver").setText("ICIP"));
			Head.addContent(new Element("SendTime").setText(time));
			Head.addContent(new Element("FunctionCode").setText(""));
			Head.addContent(new Element("Version").setText("1.0"));

			record = list.getJSONObject(i);

			Element Body = new Element("Body");
			Element swbebtrade = new Element("swbebtrade");
			Element Record = new Element("Record");
			Record.addContent(new Element("EntInsideNo").setText(record.getString("EntOrderNo")));//电子交易订单号
			Record.addContent(new Element("Ciqbcode").setText("000069"));//国检组织机构代码
			Record.addContent(new Element("CbeComcode").setText("1500004809"));//跨境电商企业备案号
			Record.addContent(new Element("CbepComcode").setText("1509007917"));//跨境电商平台企业备案号
			Record.addContent(new Element("OrderStatus").setText("S"));//订单状态
			Record.addContent(new Element("ReceiveName").setText(record.getString("RecipientName")));//收件人姓名
			Record.addContent(new Element("ReceiveAddr").setText(record.getString("RecipientAddr")));//收件人地址
			Record.addContent(new Element("ReceiveNo").setText(record.getString("OrderDocId")));//收件人证件号
			Record.addContent(new Element("ReceivePhone").setText(record.getString("RecipientTel")));//收件人电话
			Record.addContent(new Element("FCY").setText(record.getString("OrderGoodTotal")));//总货款
			Record.addContent(new Element("Fcode").setText("CNY"));//币种
			Record.addContent(new Element("Editccode").setText("1509007917"));//代发企业的企业备案号
			Record.addContent(new Element("DrDate").setText(time));//下单日期
			Element swbebtradeg = new Element("swbebtradeg");
			// 获取商品List
			JSONArray goodsList = record.getJSONArray("orderGoodsList");
			JSONObject goods = null;
			for (int j = 0; j < goodsList.size(); j++) {
				goods = goodsList.getJSONObject(j);
				Element Record1 = new Element("Record");
				Record1.addContent(new Element("EntGoodsNo").setText(String.valueOf(j+1)));//商品序号
				Record1.addContent(new Element("Gcode").setText(goods.get("BarCode") + ""));//商品货号
				Record1.addContent(new Element("Hscode").setText(goods.get("HSCode") + ""));//HS编码(海关编码)
				Record1.addContent(new Element("CiqGoodsNo").setText(goods.get("CIQGoodsNo") + ""));//商品备案号
				Record1.addContent(new Element("CopGName").setText(goods.get("GoodsName") + ""));//商品名称
				Record1.addContent(new Element("Brand").setText(goods.get("Brand") + ""));//品牌
				Record1.addContent(new Element("Spec").setText(goods.get("GoodsStyle") + ""));//规格型号
				Record1.addContent(new Element("Origin").setText(goods.get("OriginCountry") + ""));//产地
				Record1.addContent(new Element("Qty").setText(goods.get("Qty") + ""));//商品数/重量
				Record1.addContent(new Element("QtyUnit").setText(goods.get("Unit") + ""));//计量单位
				Record1.addContent(new Element("DecPrice").setText(goods.get("Price") + ""));//商品单价
				Record1.addContent(new Element("DecTotal").setText(goods.get("Total") + ""));//商品总价
				Record1.addContent(new Element("SellWebSite").setText("http://www.mall.191ec.com"));//销售网址
				Record1.addContent(new Element("Nots").setText(""));
				swbebtradeg.addContent(Record1);
			}
			Record.addContent(swbebtradeg);
			swbebtrade.addContent(Record);
			Body.addContent(swbebtrade);
			root.addContent(Head);
			root.addContent(Body);
			// 根节点添加到文档中；
			Document Doc = new Document(root);
			// 格式化
			Format format = Format.getPrettyFormat();
			XMLOutputter XMLOut = new XMLOutputter(format);
			Integer ii = i;
			if (ii.toString().length() == 1) {
				seq = "00" + ii;
			} else if (ii.toString().length() == 2) {
				seq = "0" + ii;
			} else if (ii.toString().length() == 3) {
				seq = ii.toString();
			} else {
				System.out.println("数据太多，请分次发送");
			}
			String fileName = "661101_" + time + seq + ".xml";
			System.out.println("生成的文件名为：" + fileName);
			String str=Thread.currentThread().getContextClassLoader().getResource("").getPath();
			String ePath = str+"order\\"+DateUtil.getDate("yyyyMMdd");
			File uploadFile = new File(ePath); //
			if (!uploadFile.exists() || uploadFile == null) { //
				uploadFile.mkdirs();
			}
			ePath = uploadFile.getPath() + "\\" + fileName;
			System.out.println(("生成的路径为： " + ePath));
			File file1 = new File(ePath);
			if(createLocalXMLFile(Doc, ePath)){
				statusMap.put("status", 1);
				statusMap.put("msg", "存储成功 ");
				statusMap.put("path", ePath);
				return statusMap;
			}
			
		}

		return statusMap;
	}
	/**
	 * 上送本地文件至ftp服务器
	 * 
	 * @param filePath
	 * @return
	 */
	private boolean uploadXMLFile(String filePath) {
		try {
			// XMLOut.output(Doc, new FileOutputStream(uploadPath));
			File file1 = new File(filePath);
			FtpUtils.upload(file1, "/in");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
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

}

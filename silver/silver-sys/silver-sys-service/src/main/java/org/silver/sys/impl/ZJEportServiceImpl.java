package org.silver.sys.impl;

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
import org.silver.common.NSFtpConfig;
import org.silver.sys.api.ZJEportService;
import org.silver.sys.dao.GoodsInfoDao;
import org.silver.sys.dao.GoodsRecordDao;
import org.silver.sys.dao.OrderGoodsDao;
import org.silver.sys.dao.OrderHeadDao;
import org.silver.sys.dao.OrderRecordDao;
import org.silver.sys.model.goods.GoodsInfo;
import org.silver.sys.model.goods.GoodsRecord;
import org.silver.sys.model.order.OrderGoods;
import org.silver.sys.model.order.OrderHead;
import org.silver.sys.model.order.OrderRecord;
import org.silver.sys.util.FtpUtil;
import org.silver.util.DateUtil;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service(interfaceClass = ZJEportService.class)
public class ZJEportServiceImpl implements ZJEportService {
	@Resource
	private GoodsRecordDao goodsRecordDao;
	@Resource
	private GoodsInfoDao goodsInfoDao;
	@Resource
	private OrderHeadDao orderHeadDao;
	@Resource
	private OrderRecordDao orderRecordDao;
	@Resource
	private OrderGoodsDao orderGoodsDao;
	
	private GoodsRecord saveRecord(String messageID, String time, Date now, String opType, String businessType,
			String ieFlag, String ebEntNo, String ebEntName, String currCode, String customsCode, String ciqOrgCode,
			String ebpentNo, String ebpentName) {
		GoodsRecord goodsRecord = new GoodsRecord();
		goodsRecord.setDeclEntNo(NSEportCode.ENT_RECORD_CODE);// 申报企业编号
		goodsRecord.setDeclEntName(GZEportCode.DECL_ENT_NAME);// 申报企业名称
		goodsRecord.setEBEntNo(ebEntNo);// 电商企业编号
		goodsRecord.setEBEntName(ebEntName);// 电商企业名称
		goodsRecord.setOpType(opType);// 操作方式
		goodsRecord.setCustomsCode(customsCode);// 主管海关代码
		goodsRecord.setCIQOrgCode(ciqOrgCode);// 检验检疫机构代码
		goodsRecord.setEBPEntNo(ebpentNo);// 电商平台企业编号
		goodsRecord.setEBPEntName(ebpentName);// 电商平台名称
		goodsRecord.setCurrCode(currCode);// 币制
		goodsRecord.setBusinessType(businessType);// 跨境业务类型
		goodsRecord.setInputDate(time);// 录入日期
		goodsRecord.setDeclTime(time);// 备案申请时间
		goodsRecord.setIeFlag(ieFlag);// 进出口标识
		goodsRecord.setOrgMessageID(messageID);
		goodsRecord.setEport(2);//口岸    1  电子口岸 2 智检
		goodsRecord.setCiqStatus("0");
		goodsRecord.setCusStatus("0");
		goodsRecord.setStatus(0);
		goodsRecord.setCount(0);
		goodsRecord.setCreate_date(now);
		goodsRecord.setDel_flag(0);
		if (goodsRecordDao.add(goodsRecord)) {
			return goodsRecord;
		}
		return null;
	}
	/**
	 * 备案商品实体存储
	 * 
	 * @param list
	 * @param messageID
	 * @return
	 */
	private List<GoodsInfo> saveGoods(JSONArray list, String messageID) {
		List<GoodsInfo> goodsList = new ArrayList<>();
		GoodsInfo goodsInfo = null;
		for (int i = 0; i < list.size(); i++) {
			goodsInfo = new GoodsInfo();
			JSONObject map = JSONObject.fromObject(list.get(i));
			goodsInfo = (GoodsInfo) jsonChangeToEntity(map, goodsInfo);
			goodsInfo.setOrgMessageID(messageID);
			if (goodsInfoDao.add(goodsInfo)) {
				goodsList.add(goodsInfo);
			} else {
				return null;
			}
		}
		return goodsList;
	}

	@Override
	public Map<String, Object> zjCreateGoodsRecord(Object obj, String path, String opType, String businessType,
			String ieFlag,String ebEntNo,String ebEntName ) {
		Map<String, Object> statusMap = new HashMap<String, Object>();
		String time = DateUtil.getDate("yyyyMMddHHmmss");
		String remitSerialNumber = DateUtil.getDate("yyyyMMddHHmmssSSS") + (int) (Math.random() * 9000 + 1000);// 自动生成交易编码：当前时间+四位随机码
		String messageID ="YINMENG_"+remitSerialNumber;
		Date now = new Date();
		System.out.println(time + "------->");
		List<GoodsInfo> goodsLilt =new ArrayList<>();
		// 5165 南沙保税   443400 南沙局本部
		GoodsRecord goodsRecord=saveRecord(messageID, time, now, opType, businessType, ieFlag, ebEntNo, ebEntName, 
				                "142", "5165", "443400", GZEportCode.DECL_ENT_NO, GZEportCode.DECL_ENT_NAME);
		if(goodsRecord!=null){
			JSONArray list = JSONArray.fromObject(obj);
			goodsLilt = saveGoods(list, messageID);
		}
		if(goodsRecord!=null&&goodsLilt.size()>0){
			statusMap=zjCreateGoodsRecordXML(goodsRecord,goodsLilt, opType);
		}
	
		return statusMap;
	}
	
	public Map<String, Object> zjCreateGoodsRecordXML(GoodsRecord goodsRecord,List<GoodsInfo> list ,String opType) {
		Map<String, Object> statusMap = new HashMap<String, Object>();
		Element root = new Element("Root");
		// 头部文件
		Element Head = new Element("Head");
		Head.addContent(new Element("MessageID").setText(goodsRecord.getOrgMessageID()));
		Head.addContent(new Element("MessageType").setText(NSEportCode.MESSAGE_TYPE_GOOD));
		Head.addContent(new Element("Sender").setText(NSEportCode.ENT_RECORD_CODE));
		Head.addContent(new Element("Receiver").setText(NSEportCode.RECEIVER));
		Head.addContent(new Element("SendTime").setText(goodsRecord.getDeclTime()));
		Head.addContent(new Element("FunctionCode").setText(""));
		Head.addContent(new Element("Version").setText("1.0"));

		Element Body = new Element("Body");
		Element GOODSRECORD = new Element("GOODSRECORD");
		Element Record = new Element("Record");
		Record.addContent(new Element("CargoBcode").setText(goodsRecord.getOrgMessageID()));//商品申请编号
		Record.addContent(new Element("Ciqbcode").setText(NSEportCode.CIQ_CODE));// 南沙本局
		Record.addContent(new Element("CbeComcode").setText(NSEportCode.ENT_RECORD_CODE));//电商企业备案号
		Record.addContent(new Element("Remark").setText(""));
		Record.addContent(new Element("Editccode").setText(NSEportCode.ENT_RECORD_CODE));//制单企业备案
		Record.addContent(new Element("OperType").setText(opType));

		Element CARGOLIST = new Element("CARGOLIST");
		
		// 循环放商品信息
		for (int i = 0; i < list.size(); i++) {
			GoodsInfo map = list.get(i);
			Element Records = new Element("Record");// 循环节点放商品信息
			Records.addContent(new Element("Gcode").setText(map.getEntGoodsNo()));// 商品货号
			Records.addContent(new Element("Gname").setText(map.getShelfGName()));// 商品名称
			Records.addContent(new Element("Spec").setText(map.getGoodsStyle()));// 规格型号
			Records.addContent(new Element("Hscode").setText(map.getHSCode()));
			Records.addContent(new Element("Unit").setText(map.getGUnit()));// 计量单位
			Records.addContent(new Element("GoodsBarcode").setText(map.getBarCode()));// 商品条形码
			Records.addContent(new Element("GoodsDesc").setText(map.getQuality()));// 商品描述
			Records.addContent(new Element("Remark").setText(map.getNotes()));// 备注
			Records.addContent(new Element("ComName").setText(map.getManufactory()));// 生产厂家
			Records.addContent(new Element("Manufactureraddr").setText(map.getManufactory()));// 食品类必填
			Records.addContent(new Element("Brand").setText(map.getBrand()));// 品牌
			Records.addContent(new Element("AssemCountry").setText(map.getOriginCountry()));// 原产国
		
			Records.addContent(new Element("Ingredient").setText("无"));
		    Records.addContent(new Element("Additiveflag").setText("无"));
			Records.addContent(new Element("Poisonflag").setText("无"));
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
		String fileName = "661105_" + goodsRecord.getDeclTime() + "001.xml";
		String str=Thread.currentThread().getContextClassLoader().getResource("").getPath();
		String ePath = str+"zj_goods\\"+DateUtil.getDate("yyyyMMdd");
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
		System.out.println("ePath=="+ePath);
	    //上传文件
//		try {
//			if(uploadXMLFile(ePath,NSFtpConfig.FTP_ID, NSFtpConfig.FTP_PORT,
//					NSFtpConfig.FTP_USER_NAME_YM, NSFtpConfig.FTP_PASS_WORD_YM, 
//					NSFtpConfig.FTP_GOODS_ROUTE_IN)){
//				statusMap.put("status", 1);
//				statusMap.put("msg", "上传文件成功 ");
//				return statusMap;
//			}
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		return statusMap;
	}
	private OrderHead saveOrderHead(String messageID, String time, Date now, String opType, String ieFlag,
			String ebEntNo, String ebEntName, String customsCode, String ciqOrgCode, String ebpentNo, String ebpentName,
			String internetDomainName) {
		OrderHead orderHead = new OrderHead();
		orderHead.setDeclEntNo(GZEportCode.DECL_ENT_NO);// 申报企业编号
		orderHead.setDeclEntName(GZEportCode.DECL_ENT_NAME);// 申报企业名称
		orderHead.setEBEntNo(ebEntNo);// 电商企业编号
		orderHead.setEBEntName(ebEntName);// 电商企业名称
		orderHead.setEBPEntNo(ebpentNo);// 电商平台企业编号
		orderHead.setEBPEntName(ebpentName);// 电商平台名称
		orderHead.setInternetDomainName(internetDomainName);// 电商平台域名
		orderHead.setDeclTime(time);// 申报时间
		orderHead.setOpType(opType);// 操作方式
		orderHead.setIeFlag(ieFlag);// 进出口标识
		orderHead.setCustomsCode(customsCode);// 主管海关代码
		orderHead.setCIQOrgCode(ciqOrgCode);// 检验检疫机构代码
		orderHead.setOrgMessageID(messageID);
		orderHead.setEport(1);//口岸   1 电子口岸 2 智检
		orderHead.setCreate_date(now);
		orderHead.setDel_flag(0);
		if (orderHeadDao.add(orderHead)) {
			return orderHead;
		}
		return null;
	}
	private OrderRecord saveOrderRecord(JSONObject orderObj, String messageID, Date now) {
		OrderRecord orderRecord = new OrderRecord();
		orderRecord = (OrderRecord) jsonChangeToEntity(orderObj, orderRecord);
		orderRecord.setOrgMessageID(messageID);
		orderRecord.setCreate_date(now);
		orderRecord.setDel_flag(0);
		if (orderRecordDao.add(orderRecord)) {
			return orderRecord;
		}
		return null;
	}

	private OrderGoods saveOrderGoods(JSONObject json, String messageID, String entOrderNo) {
		OrderGoods orderGoods = new OrderGoods();
		orderGoods = (OrderGoods) jsonChangeToEntity(json, orderGoods);
		orderGoods.setOrgMessageID(messageID);
		orderGoods.setEntOrderNo(entOrderNo);
		if (orderGoodsDao.add(orderGoods)) {
			return orderGoods;
		}
		return null;
	}

	@Override
	public Map<String, Object> zjCreateOrderRecord(Object obj, String path, String opType, String ieFlag,String ebEntNo,String ebEntName,String ebpentNo,String ebpentName,String internetDomainName) {
		Map<String, Object> statusMap = new HashMap<String, Object>();
		String time = DateUtil.getDate("yyyyMMddHHmmss");
		String remitSerialNumber = DateUtil.getDate("yyyyMMddHHmmssSSS") + (int) (Math.random() * 9000 + 1000);// 自动生成交易编码：当前时间+四位随机码
		String messageID ="YINMENG_"+remitSerialNumber;
		Date now = new Date();
		OrderHead orderHead=saveOrderHead(messageID, time, now, opType, ieFlag, ebEntNo, ebEntName, "5165", "443400", ebpentNo, ebpentName, internetDomainName);
		JSONArray list = JSONArray.fromObject(obj);
		List<OrderRecord> orderRecordList = new ArrayList<>();
		List<OrderGoods> orderGoodsLists = new ArrayList<>();
		String entOrderNo = "";
		if (orderHead != null) {
			for (int i = 0; i < list.size(); i++) {
				// 获取订单信息
				JSONObject orderObj = JSONObject.fromObject(list.get(i));
				// 保存订单信息
				OrderRecord orderRecord = saveOrderRecord(orderObj, messageID, now);
				// 获取订单编号
				entOrderNo = orderObj.get("EntOrderNo") + "";
				orderRecordList.add(orderRecord);
				if (orderRecord != null) {
					// 获取订单中的商品信息集
					JSONArray goodList = JSONArray.fromObject(orderObj.get("orderGoodsList"));
					for (int j = 0; j < goodList.size(); j++) {
						// 获取各个商品的详细信息
						JSONObject goods = JSONObject.fromObject(goodList.get(j));
						// 保存订单商品详细信息
						OrderGoods orderGoods = saveOrderGoods(goods, messageID, entOrderNo);
						orderGoodsLists.add(orderGoods);
					}
				}
			}
		}
		
		if (orderHead != null && orderRecordList.size() > 0 && orderGoodsLists.size() > 0) {
			statusMap = zjCreateOrderRecordXML(orderHead, orderRecordList, orderGoodsLists);
		}


		return statusMap;
	}

	public Map<String, Object> zjCreateOrderRecordXML(OrderHead orderHead,
			List<OrderRecord> orderRecordList, List<OrderGoods> orderGoodsLists) {
		Map<String, Object> statusMap = new HashMap<String, Object>();
		System.out.println("=================开始处理接收的JSonList生成XML文件===============================");
		for (int i = 0; i < orderRecordList.size(); i++) {
			OrderRecord order = orderRecordList.get(i);
			Element root = new Element("ROOT");
			String remitSerialNumber = DateUtil.getDate("yyyyMMddHHmmssSSS") + (int) (Math.random() * 9000 + 1000);// 自动生成交易编码：当前时间+四位随机码
			// 头部文件
			Element Head = new Element("Head");
			Head.addContent(new Element("MessageID").setText(order.getOrgMessageID()));
			Head.addContent(new Element("MessageType").setText(NSEportCode.MESSAGE_TYPE_ORDER));
			Head.addContent(new Element("Sender").setText(NSEportCode.ENT_RECORD_CODE));
			Head.addContent(new Element("Receiver").setText(NSEportCode.RECEIVER));
			Head.addContent(new Element("SendTime").setText(orderHead.getDeclTime()));
			Head.addContent(new Element("FunctionCode").setText(""));
			Head.addContent(new Element("Version").setText("1.0"));

			Element Body = new Element("Body");
			Element swbebtrade = new Element("swbebtrade");
			Element Record = new Element("Record");
			Record.addContent(new Element("EntInsideNo").setText(order.getEntOrderNo()));// 电子交易订单号
			Record.addContent(new Element("Ciqbcode").setText(NSEportCode.CIQ_CODE));// 国检组织机构代码
			Record.addContent(new Element("CbeComcode").setText("1500004809"));// 跨境电商企业备案号
			Record.addContent(new Element("CbepComcode").setText(NSEportCode.ENT_RECORD_CODE));// 跨境电商平台企业备案号
			Record.addContent(new Element("OrderStatus").setText("S"));// 订单状态
			Record.addContent(new Element("ReceiveName").setText(order.getRecipientName()));// 收件人姓名
			Record.addContent(new Element("ReceiveAddr").setText(order.getRecipientAddr()));// 收件人地址
			Record.addContent(new Element("ReceiveNo").setText(order.getOrderDocId()));// 收件人证件号
			Record.addContent(new Element("ReceivePhone").setText(order.getRecipientTel()));// 收件人电话
			Record.addContent(new Element("FCY").setText(String.valueOf(order.getOrderGoodTotal())));// 总货款
			Record.addContent(new Element("Fcode").setText("CNY"));// 币种
			Record.addContent(new Element("Editccode").setText(NSEportCode.ENT_RECORD_CODE));// 代发企业的企业备案号
			Record.addContent(new Element("DrDate").setText(order.getOrderDate()));// 下单日期
			Element swbebtradeg = new Element("swbebtradeg");
			// 获取商品List
			String entOrderNo = order.getEntOrderNo();
			for (int j = 0; j < orderGoodsLists.size(); j++) {
				OrderGoods goods = orderGoodsLists.get(j);
				String gentOrderNo = goods.getEntOrderNo();
				if (gentOrderNo.equals(entOrderNo)) {
					Element Record1 = new Element("Record");
					Record1.addContent(new Element("EntGoodsNo").setText(String.valueOf(goods.getSeq())));// 商品序号
					Record1.addContent(new Element("Gcode").setText(goods.getBarCode()));// 商品货号
					Record1.addContent(new Element("Hscode").setText(goods.getHSCode()));// HS编码(海关编码)
					Record1.addContent(new Element("CiqGoodsNo").setText(goods.getCIQGoodsNo()));// 商品备案号
					Record1.addContent(new Element("CopGName").setText(goods.getGoodsName()));// 商品名称
					Record1.addContent(new Element("Brand").setText(goods.getBrand()));// 品牌
					Record1.addContent(new Element("Spec").setText(goods.getGoodsStyle()));// 规格型号
					Record1.addContent(new Element("Origin").setText(goods.getOriginCountry()));// 产地
					Record1.addContent(new Element("Qty").setText(String.valueOf(goods.getQty())));// 商品数
					Record1.addContent(new Element("QtyUnit").setText(goods.getUnit()));// 计量单位
					Record1.addContent(new Element("DecPrice").setText(goods.getPrice() + ""));// 商品单价
					Record1.addContent(new Element("DecTotal").setText(goods.getTotal() + ""));// 商品总价
					Record1.addContent(new Element("SellWebSite").setText("http://www.mall.191ec.com"));// 销售网址
					Record1.addContent(new Element("Nots").setText(""));
					swbebtradeg.addContent(Record1);
				}
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

			String fileName = "661101_" + orderHead.getDeclTime()  + "001.xml";
			System.out.println("生成的文件名为：" + fileName);
			String str = Thread.currentThread().getContextClassLoader().getResource("").getPath();
			String ePath = str + "zj_order\\" + DateUtil.getDate("yyyyMMdd");
			File uploadFile = new File(ePath); //
			if (!uploadFile.exists() || uploadFile == null) { //
				uploadFile.mkdirs();
			}
			ePath = uploadFile.getPath() + "\\" + fileName;
			System.out.println(("生成的路径为： " + ePath));
			File file1 = new File(ePath);
			if (createLocalXMLFile(Doc, ePath)) {
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
	 * 
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
	

	public static void main(String[] args) {
		GZEportServiceImpl gz = new GZEportServiceImpl();
		String time = DateUtil.getDate("yyyyMMddHHmmss");
		System.out.println(time + "------->");
		String remitSerialNumber = DateUtil.getDate("yyyyMMddHHmmssSSS") + (int) (Math.random() * 9000 + 1000);// 自动生成交易编码：当前时间+四位随机码
		String messageID ="YINMENG_"+remitSerialNumber;
		Date now = new Date();
		List<GoodsInfo> goodsLilt =new ArrayList<>();
		// 5165 南沙保税   443400 南沙局本部
		GoodsRecord goodsRecord=gz.saveRecord(messageID, time, now, "c", "3", "I", "ASDFADF", "ADFADF", 
				                "142", "5165", "443400", GZEportCode.DECL_ENT_NO, GZEportCode.DECL_ENT_NAME,"");
		System.out.println(goodsRecord);
	}
}

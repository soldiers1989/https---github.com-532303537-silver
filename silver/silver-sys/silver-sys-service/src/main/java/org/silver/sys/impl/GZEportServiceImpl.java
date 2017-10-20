package org.silver.sys.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.silver.common.GZEportCode;
import org.silver.sys.api.GZEportService;
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
import org.silver.sys.util.CheckDatasUtil;
import org.silver.sys.util.FtpUtil;
import org.silver.util.DateUtil;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service(interfaceClass = GZEportService.class)
public class GZEportServiceImpl implements GZEportService {

	private final static Log logger = LogFactory.getLog(GZEportServiceImpl.class);
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

	@Override
	public void requestAnalysis(Object records, String type, String eport) {
		logger.info("=================开始处理接收的数据===============================");

	}

	@Override
	public Map<String, Object> goodsRecord(String opType, String ieFlag, String businessType, Object records,
			String ebEntNo, String ebEntName, String currCode, String customsCode, String ciqOrgCode, String ebpentNo,
			String ebpentName,String appkey,String notifyurl) {
		logger.info("=================开始处理接收的records===============================");
		Map<String, Object> checkMap = new HashMap<String, Object>();
		JSONArray jList = JSONArray.fromObject(records);
		List<String> noNullKeys = new ArrayList<>();
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
		try {
			checkMap = createHead(list, "", opType, businessType, ieFlag, ebEntNo, ebEntName, currCode, customsCode,
					ciqOrgCode, ebpentNo, ebpentName ,appkey,notifyurl);
			//FtpUtil.upload(url, port, username, password, remotePath, new File(checkMap.get("path")+""));
			
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

	// 生成唯一报文messageID
	private String createRemitSerialNumber(String topSign, String time) {
		return topSign + time + (int) (Math.random() * 9000 + 10000);// 自动生成交易编码：当前时间+五位随机码
	}

	/**
	 * 订单实体保存
	 * 
	 * @param messageID
	 * @param time
	 * @param now
	 * @param opType
	 * @param businessType
	 * @param ieFlag
	 * @param ebEntNo
	 * @param ebEntName
	 * @param currCode
	 * @param customsCode
	 * @param ciqOrgCode
	 * @param ebpentNo
	 * @param ebpentName
	 * @return
	 */
	public GoodsRecord saveRecord(String messageID, String time, Date now, String opType, String businessType,
			String ieFlag, String ebEntNo, String ebEntName, String currCode, String customsCode, String ciqOrgCode,
			String ebpentNo, String ebpentName,String appkey,String notifyurl) {
		GoodsRecord goodsRecord = new GoodsRecord();
		goodsRecord.setDeclEntNo(GZEportCode.DECL_ENT_NO);// 申报企业编号
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
		goodsRecord.setTenantNo(appkey);
		goodsRecord.setUrl(notifyurl);//回调URL
		goodsRecord.setEport(1);//口岸    1  电子口岸 2 智检
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
	public Map<String, Object> createHead(JSONArray list, String path, String opType, String businessType,
			String ieFlag, String ebEntNo, String ebEntName, String currCode, String customsCode, String ciqOrgCode,
			String ebpentNo, String ebpentName,String appkey,String notifyurl) throws FileNotFoundException, IOException {
		Map<String, Object> statusMap = new HashMap<String, Object>();
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String time = sdf.format(now);
		String messageID = createRemitSerialNumber("KJ881101_YINMENG_", time);
		// 保存商品备案头，并返回
		GoodsRecord goodsRecord = saveRecord(messageID, time, now, opType, businessType, ieFlag, ebEntNo, ebEntName,
				currCode, customsCode, ciqOrgCode, ebpentNo, ebpentName,appkey,notifyurl);
		if (goodsRecord != null) {
			// 保存商品备案的多中商品，并返回商品List
			List<GoodsInfo> goodsInfoList = saveGoods(list, messageID);
			if (goodsInfoList != null && goodsInfoList.size() > 0) {
				// 封装商品备案报文
				statusMap = convertGoodsRecordIntoXML(goodsRecord, goodsInfoList);
			//	String path2=statusMap.get("path")+"";
			//	statusMap.put("filePath", path2);
//				FtpUtil.upload(url, port, username, password, remotePath, new File(path2));
				
			}
		}
		statusMap.put("messageID", messageID);
		return statusMap;
	}

	/**
	 * 商品备案数据转成XML报文格式
	 * 
	 * @param goodsRecord
	 *            商品备案头部信息
	 * @param list
	 *            商品详细信息List
	 * @return
	 */
	public Map<String, Object> convertGoodsRecordIntoXML(GoodsRecord goodsRecord, List<GoodsInfo> list) {
		System.out.println("开始生成报文");
		Map<String, Object> statusMap = new HashMap<String, Object>();
		Element root = new Element("InternationalTrade");
		Document Doc = new Document(root);
		Element elements = new Element("Head");
		elements.addContent(new Element("MessageID").setText(goodsRecord.getOrgMessageID()));
		elements.addContent(new Element("MessageType").setText(GZEportCode.MESSAGE_TYPE_GOOD));
		elements.addContent(new Element("Sender").setText(GZEportCode.SENDER));
		elements.addContent(new Element("Receiver").setText(GZEportCode.RECEIVER));
		elements.addContent(new Element("SendTime").setText(goodsRecord.getDeclTime()));
		elements.addContent(new Element("FunctionCode").setText(GZEportCode.FUNCTION_CODE_BOTH));
		elements.addContent(new Element("SignerInfo").setText(""));
		elements.addContent(new Element("Version").setText(GZEportCode.VERSION));
		// 创建节点 ;
		Element declaration = new Element("Declaration");
		List slist = new ArrayList<>();
		slist.add("serialVersionUID");
		slist.add("id");
		slist.add("OrgMessageID");
		slist.add("ciqStatus");
		slist.add("cusStatus");
		slist.add("status");
		slist.add("count");
		slist.add("del_flag");
		slist.add("create_date");
		slist.add("eport");
		slist.add("app_key");
		Element goodsRegHead = entityChangeToXmlElement(goodsRecord, "GoodsRegHead", slist);
		Element goodsRegList = new Element("GoodsRegList");
		GoodsInfo goodsInfo = null;
		for (int i = 0; i < list.size(); i++) {
			goodsInfo = list.get(i);
			List elist = new ArrayList<>();
			elist.add("serialVersionUID");
			elist.add("id");
			elist.add("OrgMessageID");
			Element goodsContent = entityChangeToXmlElement(goodsInfo, "GoodsContent", elist);
			goodsRegList.addContent(goodsContent);
		}
		declaration.addContent(goodsRegHead);
		declaration.addContent(goodsRegList);
		// 给父节点root添加子节点;
		root.addContent(elements);
		root.addContent(declaration);
		String outPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		String ePath = outPath+"gz_goods\\"+DateUtil.getDate("yyyyMMdd");
		String fileName = goodsRecord.getOrgMessageID() + ".xml";
		File uploadFile = new File(ePath); //
		if (!uploadFile.exists() || uploadFile == null) { //
			uploadFile.mkdirs();
		}
		ePath = uploadFile.getPath() + "\\" + fileName;
		if (createLocalXMLFile(Doc, ePath)) {
			System.out.println("====="+ePath);
			statusMap.put("status", 1);
			statusMap.put("msg", "本地存储成功");
			statusMap.put("path", ePath);
			return statusMap;
		}
		statusMap.put("status", -1);
		statusMap.put("msg", "本地存储失败");
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

	@Override
	public Map<String, Object> orderRecord(Object records, String opType, String ieFlag, String internetDomainName,
			String ebpentNo, String ebpentName, String ebEntNo, String ebEntName, String customsCode,
			String ciqOrgCode,String appkey,String notifyurl) {
		System.out.println("开始验证数据");
		Map<String, Object> checkMap = new HashMap<String, Object>();
		JSONArray jList = JSONArray.fromObject(records);
		// 将必填的字段添加到list
		List<String> noNullKeys = new ArrayList<>();
		noNullKeys.add("EntOrderNo");// 企业电子订单编号
		noNullKeys.add("OrderStatus");// 电子订单状态
		noNullKeys.add("PayStatus");// 支付状态
		noNullKeys.add("OrderGoodTotal");// 订单商品总额
		noNullKeys.add("OrderGoodTotalCurr");// 订单商品总额币制
		noNullKeys.add("Freight");// 订单运费
		noNullKeys.add("Tax");// 税款
		noNullKeys.add("OtherPayment");// 抵付金额
		noNullKeys.add("ActualAmountPaid");// 实际支付金额
		noNullKeys.add("RecipientName");// 收货人名称
		noNullKeys.add("RecipientAddr");// 收货人地址
		noNullKeys.add("RecipientTel");// 收货人电话
		noNullKeys.add("RecipientCountry");// 收货人所在国
		noNullKeys.add("RecipientProvincesCode");// 收货人行政区代码
		noNullKeys.add("OrderDocAcount");// 下单人账户
		noNullKeys.add("OrderDocName");// 下单人姓名
		noNullKeys.add("OrderDocType");// 下单人证件类型
		noNullKeys.add("OrderDocId");// 下单人证件号
		noNullKeys.add("OrderDocTel");// 下单人电话
		noNullKeys.add("OrderDate");// 订单日期
		// 验证必填数据
		checkMap = CheckDatasUtil.checkData(jList, noNullKeys);
		if ((int) checkMap.get("status") != 1) {
			return checkMap;
		}
		// JSONArray 订单数据
		JSONArray list = JSONArray.fromObject(checkMap.get("datas"));
		JSONObject record = JSONObject.fromObject(list.get(0));
		JSONArray goodsList = JSONArray.fromObject(record.get("orderGoodsList"));
		noNullKeys.add("Seq");// 商品序号
		noNullKeys.add("EntGoodsNo");// 企业商品自编号
		noNullKeys.add("CIQGoodsNo");// 检验检疫商品备案编号
		noNullKeys.add("CusGoodsNo");// 海关正式备案编号
		noNullKeys.add("GoodsName");// 企业商品品名
		noNullKeys.add("GoodsStyle");// 规格型号
		noNullKeys.add("OriginCountry");// 原产国
		noNullKeys.add("Qty");// 数量
		noNullKeys.add("Unit");// 计量单位
		noNullKeys.add("Price");// 单价
		noNullKeys.add("Total");// 总价
		noNullKeys.add("CurrCode");// 币制
		noNullKeys.add("HSCode");// 海关商品分类编号，BBC业务必填，BC业务可空
		CheckDatasUtil.checkData(goodsList, noNullKeys);
		if ((int) checkMap.get("status") != 1) {
			return checkMap;
		}
		return createOrder(list, "", opType, ieFlag, internetDomainName, ebpentNo, ebpentName, ebEntNo, ebEntName,
				customsCode, ciqOrgCode,appkey,notifyurl);
	}

	private OrderHead saveOrderHead(String messageID, String time, Date now, String opType, String ieFlag,
			String ebEntNo, String ebEntName, String customsCode, String ciqOrgCode, String ebpentNo, String ebpentName,
			String internetDomainName,String appkey,String notifyurl) {
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
		orderHead.setApp_key(appkey);
		orderHead.setUrl(notifyurl);
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
	public Map<String, Object> createOrder(JSONArray list, String path, String opType, String ieFlag,
			String internetDomainName, String ebpentNo, String ebpentName, String ebEntNo, String ebEntName,
			String customsCode, String ciqOrgCode,String appkey,String notifyurl) {
		System.out.println("开始存储数据");
		Map<String, Object> statusMap = new HashMap<String, Object>();
		List<OrderRecord> orderRecordList = new ArrayList<>();
		List<OrderGoods> orderGoodsLists = new ArrayList<>();
		statusMap.put("status", 1);
		statusMap.put("msg", "报文发送成功 ");
		String time = DateUtil.getDate("yyyyMMddHHmmss");
		String remitSerialNumber = DateUtil.getDate("yyyyMMddHHmmss") + (int) (Math.random() * 9000 + 10000);// 自动生成交易编码：当前时间+五位随机码
		String messageID = "KJ881111_YINMENG_" + remitSerialNumber;
		Date now = new Date();
		OrderHead orderHeadEnt = saveOrderHead(messageID, time, now, opType, ieFlag, ebEntNo, ebEntName, customsCode,
				ciqOrgCode, ebpentNo, ebpentName, internetDomainName,appkey,notifyurl);
		String entOrderNo = "";
		if (orderHeadEnt != null) {
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
		if (orderHeadEnt != null && orderRecordList.size() > 0 && orderGoodsLists.size() > 0) {
			statusMap = convertOrderRecordIntoXML(orderHeadEnt, orderRecordList, orderGoodsLists);
		}
		return statusMap;
	}

	public Map<String, Object> convertOrderRecordIntoXML(OrderHead orderHeadEnt,
			List<OrderRecord> orderRecordList, List<OrderGoods> orderGoodsLists) {
		Map<String, Object> statusMap = new HashMap<String, Object>();
		Element root = new Element("InternationalTrade");
		Document Doc = new Document(root);
		Element elements = new Element("Head");
		elements.addContent(new Element("MessageID").setText(orderHeadEnt.getOrgMessageID()));
		elements.addContent(new Element("MessageType").setText(GZEportCode.MESSAGE_TYPE_GOOD));
		elements.addContent(new Element("Sender").setText(GZEportCode.SENDER));
		elements.addContent(new Element("Receiver").setText(GZEportCode.RECEIVER));
		elements.addContent(new Element("SendTime").setText(orderHeadEnt.getDeclTime()));
		elements.addContent(new Element("FunctionCode").setText(GZEportCode.FUNCTION_CODE_CIQ));
		elements.addContent(new Element("SignerInfo").setText(" "));
		elements.addContent(new Element("Version").setText(GZEportCode.VERSION));
		// 创建节点 OrderHead;
		Element declaration = new Element("Declaration");
		List slist = new ArrayList<>();
		slist.add("serialVersionUID");
		slist.add("id");
		slist.add("OrgMessageID");
		slist.add("del_flag");
		slist.add("create_date");
		slist.add("eport");
		slist.add("status");
		slist.add("count");
		slist.add("app_key");
		Element orderHead = entityChangeToXmlElement(orderHeadEnt, "OrderHead", slist);
		declaration.addContent(orderHead);
		// 订单信息 可循环
		Element orderList = new Element("OrderList");
		Element orderContent = new Element("OrderContent");
		declaration.addContent(orderList);
		orderList.addContent(orderContent);
		for (int i = 0; i < orderRecordList.size(); i++) {
			OrderRecord order = orderRecordList.get(i);
			List elist = new ArrayList<>();
			elist.add("serialVersionUID");
			elist.add("id");
			elist.add("OrgMessageID");
			elist.add("count");
			elist.add("del_flag");
			elist.add("create_date");
			Element orderDetail = entityChangeToXmlElement(order, "OrderDetail", elist);
			String entOrderNo = order.getEntOrderNo();
			orderContent.addContent(orderDetail);
			// 订单的商品信息 可循环
			Element goodsList = new Element("GoodsList");
			for (int j = 0; j < orderGoodsLists.size(); j++) {
				OrderGoods goods = orderGoodsLists.get(j);
				String gentOrderNo = goods.getEntOrderNo();
				if (gentOrderNo.equals(entOrderNo)) {
					List zlist = new ArrayList<>();
					zlist.add("serialVersionUID");
					zlist.add("id");
					zlist.add("OrgMessageID");
					zlist.add("EntOrderNo");
					Element orderGoodsList = entityChangeToXmlElement(goods, "OrderGoodsList", zlist);
					goodsList.addContent(orderGoodsList);
				}
			}
			orderDetail.addContent(goodsList);
		}
		// 给父节点root添加子节点;
		root.addContent(elements);
		root.addContent(declaration);
		// 格式化
		Format format = Format.getPrettyFormat();
		XMLOutputter XMLOut = new XMLOutputter(format);
		// 输出 user.xml 文件；
		String outPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		String ePath = outPath+"gz_order\\"+DateUtil.getDate("yyyyMMdd");
		String fileName = orderHeadEnt.getOrgMessageID() + ".xml";
		File uploadFile = new File(ePath); //
		if (!uploadFile.exists() || uploadFile == null) { //
			uploadFile.mkdirs();
		}
		ePath = uploadFile.getPath() + "\\" + fileName;
		if (createLocalXMLFile(Doc, ePath)) {
			statusMap.put("status", 1);
			statusMap.put("msg", "本地存储成功");
			statusMap.put("path", ePath);
			return statusMap;
		}
		statusMap.put("status", -1);
		statusMap.put("msg", "本地存储失败");
		return statusMap;
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
}

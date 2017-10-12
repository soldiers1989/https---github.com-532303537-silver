package org.silver.sys.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.codec.binary.Base64;
import org.silver.sys.api.QBLogisticsService;
import org.silver.sys.component.ChooseDatasourceHandler;
import org.silver.sys.dao.AreaDao;
import org.silver.sys.dao.SessionFactory;
import org.silver.util.DateUtil;
import org.silver.util.MD5;
import org.silver.util.YmHttpUtil;

import com.justep.baas.data.Table;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class QBLogisticsServiceImpl implements QBLogisticsService {

	String key = "316A721244p44";
	@Resource
	private AreaDao areaDao = new AreaDao();
	@Override
	public String pushOrderToQB(String store_code, String order_code, String n_kos, String receiver_info,
			String sender_info, JSONArray list, int package_count, String order_ename, String order_phone,
			String order_cardno, String freight, String tax) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String itemListStr = "", xml = "", md5Str = "";
		String head = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
		String body = "<store_code>" + store_code + "</store_code>" + "<order_code>" + order_code + "</order_code>"
				+ "<order_type>201</order_type>" + // Integer(11) 操作子类型 001 进口BC接口  002 出口BC 201销售单进口BBC 601采购单进口BBC
				"<order_source>191ec</order_source>" + // Integer(11) 订单来源双方约定
				"<order_create_time>" + df.format(new Date()) + "</order_create_time>" + "<v_ieflag>I</v_ieflag>"
				+ "<v_transport_code>1</v_transport_code>" + // 运输方式代码 可空
				"<v_package_typecode>1</v_package_typecode>" + // 打包方式代码 可空
				"<v_qy_state>142</v_qy_state>" + // 起运国/运抵国代码 可空
				"<n_kos>" + n_kos + "</n_kos>" + // 毛重 必填
				"<v_traf_name></v_traf_name>" + "<tms_service_code>UC</tms_service_code>" + // 物流公司编码
				"<receiver_info>" + "NA^^^广东省^^^广州市^^^南沙区^^^环市大道南漾滨路珠江湾A5-2303^^^钟妙桃^^^15918686478^^^NA"
				+ "</receiver_info>" + "<sender_info>"
				+ "511400^^^广东省^^^广州市^^^南沙区^^^港荣三街3号402^^^广州市启邦国际物流有限公司^^^13928820023^^^0755-33079718"
				+ "</sender_info>";

		String bottom = "<package_count>" + package_count + "</package_count>" + // 包裹数量
				"<order_ename>" + order_ename + "</order_ename>" + // 订单人姓名（和支付人姓名一致）
				"<order_phone>" + order_phone + "</order_phone>" + // 订单人电话
				"<order_cardno>" + order_cardno + "</order_cardno>" + // 订单人身份证号
				"<freight>" + freight + "</freight>" + // 运费
				"<tax>" + tax + "</tax>";// 税费
		JSONObject record = null;
		Double price = 0.00;
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				record = JSONObject.fromObject(list.get(i));
				price = Double.valueOf(record.getString("Price"));
				itemListStr += "<order_item>" + "<v_goods_regist_no>" + record.getString("CIQGoodsNo")
						+ "</v_goods_regist_no>" + // 商品备案号
						"<order_item_id>" + record.getString("Seq") + "</order_item_id>" + // 订单商品
						// "<order_item_id>"+(i+1)+"</order_item_id>"+//订单商品 ID
						// 推送给启邦的商品顺序需要跟推送海关的订单报文中的商品顺序一致
						"<item_id>" + record.get("HSCode") + "" + "</item_id>" + // 商品HS编码
						// "<item_id>"+"19011000"+"</item_id>"+//商品HS编码
						"<item_name>" + record.getString("GoodsName") + "</item_name>" + // 商品名称
						"<item_code>" + record.getString("BarCode") + "</item_code>" + // 商品货号
						// "<item_code>"+"024196"+"</item_code>"+//商品货号
						"<inventory_type>" + 1 + "</inventory_type>" + // 库存类型 1
						"<item_quantity>" + record.getString("Qty") + "</item_quantity>" + // 商品数量
						"<item_price>" + (int) (price * 100) + "</item_price>" + // 销售价格
						// "<item_price>"+"3400"+"</item_price>"+// 销售价格 分为单位
						"<item_version>" + 1 + "</item_version>" + // 商品版本
						"<cus_code>" + "XJGL" + "</cus_code>" + // 商家代码
						"<sku_code>" + record.getString("SKU") + "</sku_code>" + // 填启邦提供的商品备案信息中的客户SKU
						"<item_spec>" + record.getString("GoodsStyle") + "</item_spec>" + // 规格型号
						"</order_item>";
			}
		}
		xml = head + "<request>" + body + "<order_item_list>" + itemListStr + "</order_item_list>" + bottom
				+ "</request>";
		md5Str = "";
		try {
			md5Str = MD5.getMD5((xml + key).getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("sign_type", "MD5");
		params.put("notify_type", "COSCO_STOCK_OUT_ORDER");
		params.put("input_charset", "UTF-8");
		params.put("sign", new String(Base64.encodeBase64(md5Str.getBytes())));
		params.put("content", xml);
		System.out.println("xml=====>" + xml);
		// str=YmHttpUtil.HttpPost("http://wmstest02.keypon.cn:51234/exgw/wms",params);//测试环境
		String str = YmHttpUtil.HttpPost("http://exgw01.keypon.cn:51234/exgw/wms", params);// 生产环境
		return str;
	}

	@Override
	public String stockInquiryToQB(String cus_code, String pt_code, String sku_code, String goods_code) {
		String xml = "", md5Str = "";
		String head = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
		String body = "";
		if (null != cus_code && !"".equals(cus_code.trim())) {
			body += "<cus_code>" + cus_code + "</cus_code>";// 商家编号(必填)
			if (null != pt_code && !"".equals(pt_code.trim())) {
				body += "<pt_code>" + pt_code + "</pt_code>";// 平台代码
			}
			if (null != sku_code && !"".equals(sku_code.trim())) {
				body += "<sku_code>" + sku_code + "</sku_code>";// SKU代码
			}
			if (goods_code != null && !"".equals(goods_code.trim())) {
				body += "<goods_code>" + goods_code + "</goods_code>";// 货号
			}

		}
		xml = head + "<request>" + body + "</request>";
		md5Str = "";
		try {
			md5Str = MD5.getMD5((xml + key).getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("sign_type", "MD5");
		params.put("notify_type", "COSCO_STOCKNUM_INFO_OUT");
		params.put("input_charset", "UTF-8");
		 params.put("sign", new String(Base64.encodeBase64(md5Str.getBytes())));
		params.put("content", xml);
		System.out.println("xml=====>" + xml);
		String str = YmHttpUtil.HttpPost("http://wmstest02.keypon.cn:51234/exgw/wms", params);// 测试
		// str=YmHttpUtil.HttpPost("http://exgw01.keypon.cn:51234/exgw/wms",params);//生产
		return str;
	}

	@Override
	public String waybillQueryToQB(String order_code, String ordermark, String order_type, String trading_orderno,
			String Order_Source) {
		String xml = "", md5Str = "";
		String head = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
		String body = "<order_code>" + order_code + "</order_code>" + // 订单编码(必填)
				"<ordermark>" + ordermark + "</ordermark>" + // 业务类型 1,BBC 2,BC
				"<order_type>" + order_type + "</order_type>" + //订单子类型：001,一般进口；B002,BC备货模式；B003,BC转运模式；B004,BC直邮模式；
				"<trading_orderno>" + trading_orderno + "</trading_orderno>" + // 交易订单号
				"<Order_Source>" + Order_Source + "</Order_Source>";// 订单来源(必填)平台编号(启邦与电商双方定义的)

		xml = head + "<request>" + body + "</request>";
		md5Str = "";
		try {
			md5Str = MD5.getMD5((xml + key).getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("sign_type", "MD5");
		params.put("notify_type", "QB_QUERY_WAYBILLNO");
		params.put("input_charset", "UTF-8");
		params.put("sign", new String(Base64.encodeBase64(md5Str.getBytes())));
		params.put("content", xml);
		System.out.println("xml=====>" + xml);
		// str=YmHttpUtil.HttpPost("http://wmstest02.keypon.cn:51234/exgw/wms",params);//测试
		String str = YmHttpUtil.HttpPost("http://exgw01.keypon.cn:51234/exgw/wms", params);// 正式
		return str;
	}

	@Override
	public String orderStatusQueryToQB(String orders_code) {
		String xml = "", md5Str = "";
		String time = DateUtil.getDate("yyyyMMddHHmmssSSS");
		String head = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
		String body = "<orders_code>" + orders_code + "</orders_code>";// 订单编码(必填)
		xml = head + "<request>" + body + "</request>";
		md5Str = "";
		try {
			md5Str = MD5.getMD5((xml + key).getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("sign_type", "MD5");
		params.put("notify_type", "COSCO_ORDERSTATUS_INFO_OUT");
		params.put("input_charset", "UTF-8");
		params.put("sign", new String(Base64.encodeBase64(md5Str.getBytes())));
		params.put("content", xml);
		System.out.println("xml=====>" + xml);
		// String
		// str=YmHttpUtil.HttpPost("http://wmstest02.keypon.cn:51234/exgw/wms",params);//测试环境
		// 测试订单号：00000000100019890754
		String str = YmHttpUtil.HttpPost("http://183.62.139.124:22220/ds_bz_web/gateway", params);
		return str;
	}

	private boolean createLocalXMLFile(String xml, String time) {
		String str = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		String ePath = str + "/qborder/" + DateUtil.getDate("yyyyMMdd");
		String fileName = ePath + "qb_" + time + ".xml";
		try {
			File file = new File(fileName);
			FileOutputStream fop = new FileOutputStream(file);
			if (!file.exists()) {
				file.createNewFile();
			}
			byte[] contentInBytes = xml.getBytes();
			fop.write(contentInBytes);
			fop.flush();
			fop.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public String queryZipCode(String areaCode,String addr,String name,String tel,String phone){
		String str="";
		ChooseDatasourceHandler.hibernateDaoImpl.setSession(SessionFactory.getSession());
		Table t=areaDao.findRecordByAreaCode(areaCode);
		String zip=t.getRows().get(0).getValue("zip")+"";
		String provinceName=t.getRows().get(0).getValue("provinceName")+"";
		String cityName= t.getRows().get(0).getValue("cityName")+"";
		String areaName=t.getRows().get(0).getValue("areaName")+"";
		if(tel==null||tel.trim().length()==0){
			tel="NA";
		}
		if(phone==null||phone.trim().length()==0){
			phone="NA";
		}
		str =zip+"^^^"+provinceName+"^^^"+cityName+"^^^"+areaName+"^^^"+addr+"^^^"+name+"^^^"+tel+"^^^"+phone;
		return str;
	}
	public static void main(String[] args) {
		QBLogisticsServiceImpl qb = new QBLogisticsServiceImpl();
		//Map<String,Object> zipMap=qb.queryZipCode("440115");
//		for (Object v : zipMap.values()) {
//			System.out.println("value= " + v);
//	    }
		String s=qb.queryZipCode("440105", "佳都商务大厦", "张旭", "18102538226","");
		System.out.println(s);
	}
	
}

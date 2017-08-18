package org.silver.sys.service;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.silver.util.MD5;
import org.silver.util.YmHttpUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class QBPushOrderService {
	
	/**
	 * 
	 * @param store_code 仓储编号
	 * @param order_code  订单编号
	 * @param n_kos  毛重
	 * @param receiver_info  接件人地址联系电话 --- 拼接见文档
	 * @param sender_info    发件人信息 --- 拼接见文档
	 * @param list   需推送的商品清单   详见开发文档 
	 * @param package_count  包裹数 
	 * @param order_ename  下单人姓名
	 * @param order_phone 下单人电话
	 * @param order_cardno 下单人身份证号
	 * @param freight  运费
	 * @param tax 税费
	 * @return
	 */
	 public String pushOrderToQB(String store_code,String order_code,String n_kos,String receiver_info,String sender_info,JSONArray list,int package_count,String order_ename,String order_phone,String order_cardno,String freight,String tax){
	    	String itemListStr="",xml="",md5Str="";
	    	String head="<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	    	String body="<store_code>"+store_code+"</store_code>"+
	    			"<order_code>"+order_code+"</order_code>"+
	    			"<order_type>201</order_type>"+//Integer(11) 操作子类型 001 进口BC接口 002 出口BC 201销售单进口BBC 601采购单进口BBC
	    			"<order_source>ym-mall</order_source>"+//Integer(11) 订单来源双方约定
	    			"<order_create_time>2016-12-29 17:52:56</order_create_time>"+
	    			"<v_ieflag>I</v_ieflag>"+
	    			"<v_transport_code>1</v_transport_code>"+//运输方式代码 可空
	    			"<v_package_typecode>1</v_package_typecode>"+//打包方式代码  可空
	    			"<v_qy_state>142</v_qy_state>"+//起运国/运抵国代码 可空
	    			"<n_kos>"+n_kos+"</n_kos>"+//毛重 必填
	    			"<v_traf_name></v_traf_name>"+
	    			"<tms_service_code>UC</tms_service_code>"+//物流公司编码
	    			"<receiver_info>"+receiver_info+"</receiver_info>"+
	    			"<sender_info>"+sender_info+"</sender_info>"+
	    			"<package_count>"+package_count+"</package_count>"+//包裹数量
	    			"<order_ename>"+order_ename+"</order_ename>"+//订单人姓名
	    			"<order_phone>"+order_phone+"</order_phone>"+//订单人电话
	    			"<order_cardno>"+order_cardno+"</order_cardno>"+//订单人身份证号
	    			"<freight>"+freight+"</freight>"+//运费
	    			"<tax>"+tax+"</tax>";//税费
	    	
	    	 JSONObject record=null;
	    			if(list!=null&&list.size()>0){
	    				for(int i=0;i<list.size();i++){
	    					record =JSONObject.fromObject(list.get(i));
	    					itemListStr+="<order_item_list>"+
	    	    			"<order_item>"+
	    	    			"<v_goods_regist_no>10088752</v_goods_regist_no>"+//商品备案号
	    	    			"<order_item_id>1</order_item_id>"+//订单商品 ID 推送给启邦的商品顺序需要跟推送海关的订单报文中的商品顺序一致
	    	    			"<item_id>2202900099</item_id>"+//商品HS编码
	    	    			"<item_name>新西兰Vitalise维他命能量饮料250ml</item_name>"+//商品名称
	    	    			"<item_code>022174</item_code>"+//商品货号
	    	    			"<inventory_type>1</inventory_type>"+//库存类型  1 可销售库存
	    	    			"<item_quantity>4</item_quantity>"+//商品数量
	    	    			"<item_price>400.0000</item_price>"+// 销售价格 分为单位
	    	    			"<item_version>0</item_version>"+//商品版本
	    	    			"<cus_code>HYLD</cus_code>"+//商家代码
	    	    			"<sku_code>HYLD000058</sku_code>"+//填启邦提供的商品备案信息中的客户SKU
	    	    			"<item_spec>250ml/瓶</item_spec>"+//规格型号
	    	    			"</order_item>"+
	    	    			"</order_item_list>";
	    				}
	    			}
	    	
	        xml=head+"<request>"+body+itemListStr+"</request>";
	        md5Str="";
	        try {
	        	md5Str=MD5.getMD5(xml.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
	        Map<String,Object> params = new HashMap<String,Object>();
	        params.put("sign_type", "MD5");
	        params.put("notify_type", "COSCO_STOCK_OUT_ORDER");
	       	params.put("input_charset", "UTF-8");
	       	params.put("sign", new String (Base64.encodeBase64(md5Str.getBytes())));
	       	params.put("content", xml);
			return YmHttpUtil.HttpPost("http://wmstest02.keypon.cn:51234/exgw/wms",params);
	    	
	    }
}

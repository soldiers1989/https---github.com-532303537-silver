package org.silver.sys.api;

import net.sf.json.JSONArray;

/**
 * 启邦物流接口
 * @author zhangxin
 *
 */
public interface QBLogisticsService {
	/**
	 * 订单推送（启邦）
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
	public String pushOrderToQB(String store_code,String order_code,String n_kos,String receiver_info,String sender_info,JSONArray list,int package_count,String order_ename,String order_phone,String order_cardno,String freight,String tax);
	
	 /**
	  * 库存查询接口
	  * @param cus_code  商家编号      YMKJ
	  * @param pt_code   平台代码      191ec
	  * @param sku_code  SKU代码    YM000002
	  * @param goods_code  货号
	  * @return
	  */
	public String stockInquiryToQB(String cus_code,String pt_code,String sku_code,String goods_code);
	
	 /**
	  * 运单查询
	  * @param order_code  订单编码
	  * @param ordermark   业务类型
	  * @param order_type  订单子类型
	  * @param trading_orderno 交易订单号
	  * @param Order_Source 订单来源
	  * @return
	  */
	public String waybillQueryToQB(String order_code,String ordermark,String order_type,String trading_orderno,String Order_Source);
	
	 /**
	  * 订单状态查询
	  * @param orders_code  订单编号
	  * @return
	  */
	public String orderStatusQueryToQB(String orders_code);
}

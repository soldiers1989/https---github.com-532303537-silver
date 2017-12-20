package org.silver.shop.config;

/**
 * 银盟商城配置文件 
 */
public class YmMallConfig {
	public static final String  TOPSTR = "http://192.168.1.116:8080";
	public static final String  YM = "http://ym.191ec.com";
	//
	public static final String  APPKEY = "4a5de70025a7425dabeef6e8ea752976";
	//
	public static final String APPSECRET ="NeMs1DFG8xFARwZeSlRZwlT22ayY5oIbkgZg1uCziQ3LfSgqcPN4qGydAt7s3jMW";
	/**
	 * 商品备案异步回调URL
	 */
	public static final String GOODSRECORDNOTIFYURL = YM+"/silver-web-shop/goodsRecord/reNotifyMsg";
	/**
	 * 订单备案异步回调URL
	 */
	public static final String ORDERNOTIFYURL = YM+"/silver-web-shop/order/reNotifyMsg";
	
	/**
	 * 支付单备案异步回调URL
	 */
	public static final String PAYMENTNOTIFYURL = YM+"/silver-web-shop/payment/reNotifyMsg";
	
	/**
	 * 手工订单备案异步回调URL
	 */
	public static final String MANUALORDERNOTIFYURL = YM+"/silver-web-shop/manual/reOrderNotifyMsg";
	
	/**
	 * 手工支付单备案异步回调URL
	 */
	public static final String MANUALPAYMENTNOTIFYURL = YM+"/silver-web-shop/payment/rePayNotifyMsg";
}

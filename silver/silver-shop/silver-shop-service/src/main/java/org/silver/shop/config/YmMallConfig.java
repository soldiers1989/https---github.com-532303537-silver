package org.silver.shop.config;

/**
 * 银盟商城url
 */
public class YmMallConfig {
	//public static final String TOPSTR = "http://192.168.1.116:8080";
	/**
	 * 银盟商城域名抬头
	 */
	public static final String YM = "https://ym.191ec.com";
	/**
	 * 银盟商城对接接入网关key
	 */
	public static final String APPKEY = "4a5de70025a7425dabeef6e8ea752976";
	/**
	 * 银盟商城对接接入网关的密钥
	 */
	public static final String APPSECRET = "NeMs1DFG8xFARwZeSlRZwlT22ayY5oIbkgZg1uCziQ3LfSgqcPN4qGydAt7s3jMW";
	/**
	 * 商品备案异步回调URL
	 */
	public static final String GOODS_RECORD_NOTIFY_URL = YM + "/silver-web-shop/goodsRecord/reNotifyMsg";
	/**
	 * 商城真实订单备案异步回调URL
	 */
	public static final String ORDER_NOTIFY_URL = YM + "/silver-web-shop/order/reNotifyMsg";

	/**
	 * 支付单备案异步回调URL
	 */
	public static final String PAYMENT_NOTIFY_URL = YM + "/silver-web-shop/payment/reNotifyMsg";

	/**
	 * 手工订单备案异步回调URL
	 */
	public static final String MANUAL_ORDER_NOTIFY_URL = YM + "/silver-web-shop/manual/reOrderNotifyMsg";

	/**
	 * 手工支付单备案异步回调URL
	 */
	public static final String MANUAL_PAYMENT_NOTIFY_URL = YM + "/silver-web-shop/payment/rePayNotifyMsg";

	/**
	 * 银盟报关网关URL
	 */
	public static final String REPORT_URL = "https://ym.191ec.com/silver-web/Eport/Report";
	
	/**
	 * 银盟实名认证网关商户号
	 */
	public static final String ID_CARD_CERTIFICATION_MERCHANT_NO = "YM20170000015078659178651922";
	
	/**
	 * 第三方电商订单、支付单回传url
	 */
	public static final String THIRD_PARTY_NOTIFY_URL = "https://ym.191ec.com/silver-web/Eport/getway-callback";
	
	/**
	 * 实名认证url
	 */
	public static final String REAL_URL = "https://ym.191ec.com/silver-web/real/auth";
	
}

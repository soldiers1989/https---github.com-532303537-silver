package org.silver.shop.config;

/**
 *	分账(银盟1119账号)的配置文件 
 */
public class FenZhangConfig {
	// 合作商家私钥pkcs12证书路径
	//public static final String PATH_PARTER_PKCS12 = "/shanghu_test.pfx";测试
	public static final String PATH_PARTER_PKCS12 = "/yinmeng1119.pfx";

	// 合作商家私钥pkcs12证书密码
	//public static final String PASSWORD_PARTNER_PKCS12 = "123456";测试
	public static final String PASSWORD_PARTNER_PKCS12 = "yinmeng1";

	// 银盛公钥pkcs12证书路径
	public static final String PATH_YSEPAY_PUBLIC_CERT = "/businessgate.cer";

	// rsa算法名
	public static final String RSA_ALGORITHM = "SHA1WithRSA";

	// 签名算法
	public static final String SIGN_ALGORITHM = "RSA";

	// 使用商户自己的私钥签名请求时，采用的字符编码
	public static final String DEFAULT_CHARSET = "utf-8";

	// 银盛分配给商家的商户号
	//public static final String PLATFORM_PARTNER_NO = "shanghu_test";测试
	public static final String PLATFORM_PARTNER_NO = "yinmeng1119";

	//商户名称
	//public static final String PLATFORM_PARTNER_NAME = "银盛支付商户测试公司";测试
	public static final String PLATFORM_PARTNER_NAME = "广州银盟信息科技有限公司";

	// 银盛分配给商家的商户号版本
	public static final String VERSION = "3.0";

	public static final String YSEPAY_GATEWAY_URL_DF = "https://mertest.ysepay.com/openapi_dsf_gateway/gateway.do";

	// 银盛支付接入网关url

	//public static final String YSEPAY_GATEWAY_URL = "https://mertest.ysepay.com/openapi_gateway/gateway.do"; // 商户测试环境订单支付

	// public static final String YSEPAY_GATEWAY_URL =
	// "https://mertest.ysepay.com/openapi_dsf_gateway/gateway.do";
	// 商户环境代付交易
	public static final String YSEPAY_GATEWAY_URL ="https://openapi.ysepay.com/gateway.do"; //生产
	// https://openapi.ysepay.com/gateway.do
	// public static final String YSEPAY_GATEWAY_URL =
	// "https://10.211.52.49:8443/openapi_gateway/gateway.do";

}

package org.silver.common;

/**
 * 用于商城存放redis缓存key
 */
public class RedisKey {

	/**
	 * 用户修改登陆密码前,验证用户身份,发送手机验证码--key
	 */
	public static final String SHOP_KEY_MEMBER_UPDATE_LOGIN_PASSWORD_CODE = "SHOP_KEY_MEMBER_UPDATE_LOGIN_PASSWORD_CODE_";

	/**
	 * 商城所有口岸-海关-国检信息--key
	 */
	public static final String SHOP_KEY_ALL_PORT_CUSTOMS_LIST = "SHOP_KEY_ALL_PORT_CUSTOMS_LIST";

	/**
	 * 商城省-市-区三级联动封装好的MAP--key
	 */
	public static final String SHOP_KEY_PROVINCE_CITY_AREA_POSTAL_MAP = "SHOP_KEY_PROVINCE_CITY_AREA_POSTAL_MAP";

	/**
	 * 第三方商城推广订单下单时,发送手机验证码--key
	 */
	public static final String SHOP_KEY_THIRD_PROMOTE_BUSINESS_CAPTCHA_CODE = "SHOP_KEY_THIRD_PROMOTE_BUSINESS_CAPTCHA_CODE_";

	/**
	 * 商城用户注册时发送手机验证码--key
	 */
	public static final String SHOP_KEY_MEMBER_REGISTER_CODE = "SHOP_KEY_MEMBER_REGISTER_CODE_";

	/**
	 * 商城国家-key
	 */
	public static final String SHOP_KEY_COUNTRY_LIST = "SHOP_KEY_COUNTRY_LIST";

	/**
	 * 商城计量单位-key
	 */
	public static final String SHOP_KEY_METERING_LIST = "SHOP_KEY_METERING_LIST";

	/**
	 * 商城海关关区信息-key
	 */
	public static final String SHOP_KEY_GAC_LIST = "SHOP_KEY_GAC_LIST";

	/**
	 * 商城国检检疫机构信息-key
	 */
	public static final String SHOP_KEY_CCIQ_LIST = "SHOP_KEY_CCIQ_LIST";

	/**
	 * 商城商品类型信息-key
	 */
	public static final String SHOP_KEY_GOODS_CATEGORY_MAP = "SHOP_KEY_GOODS_CATEGORY_MAP";
	
	/**
	 * 用户修改支付密码时,验证用户身份,发送手机验证码--key
	 */
	public static final String SHOP_KEY_MEMBER_UPDATE_PAYMENT_PASSWORD_CODE = "SHOP_KEY_MEMBER_UPDATE_PAYMENT_PASSWORD_CODE_";
	
	/**
	 * 用户重置登录密码时,验证用户身份,发送手机验证码--key
	 */
	public static final String SHOP_KEY_MEMBER_RESET_LOGIN_PASSWORD_CODE = "SHOP_KEY_MEMBER_RESET_LOGIN_PASSWORD_CODE_";
	
	/**
	 * 用户首次设置支付密码,验证用户身份,发送手机验证码--key
	 */
	public static final String SHOP_KEY_MEMBER_SET_PAYMENT_PASSWORD_CODE = "SHOP_KEY_MEMBER_SET_PAYMENT_PASSWORD_CODE_";
	
	/**
	 * 用户更换手机号码,发送手机验证码--key
	 */
	public static final String SHOP_KEY_MEMBER_UPDATE_PHONE_CODE = "SHOP_KEY_MEMBER_UPDATE_PHONE_CODE_";
	
	/**
	 * 用户重置支付密码,发送手机验证码--key
	 */
	public static final String SHOP_KEY_MEMBER_RESET_PAYMENT_PASSWORD_CODE = "SHOP_KEY_MEMBER_RESET_PAYMENT_PASSWORD_CODE_";
	
	/**
	 * 商户重置登录密码,发送手机验证码--key
	 */
	public static final String SHOP_KEY_MERCHANT_RESET_LOGIN_PASSWORD_CODE = "SHOP_KEY_MERCHANT_RESET_LOGIN_PASSWORD_CODE_";
	
	
}

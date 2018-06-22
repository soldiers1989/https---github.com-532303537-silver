package org.silver.common;

/**
 *	用于系统存放redis缓存key 
 */
public class RedisKey {

	/**
	 * 用户修改登陆密码前,验证用户身份,发送手机验证码--key
	 */
	public static final String SHOP_KEY_MEMBER_UPDATE_LOGIN_PASSWORD_CODE_ = "SHOP_KEY_MEMBER_UPDATE_LOGIN_PASSWORD_CODE_";
	
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
	public static final String SHOP_KEY_THIRD_PROMOTE_BUSINESS_CAPTCHA_CODE_ = "SHOP_KEY_THIRD_PROMOTE_BUSINESS_CAPTCHA_CODE_";
	
}

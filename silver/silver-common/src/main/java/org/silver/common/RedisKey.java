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
	
}

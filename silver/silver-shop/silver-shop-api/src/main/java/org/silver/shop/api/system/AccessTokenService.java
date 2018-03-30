package org.silver.shop.api.system;

import java.util.Map;

/**
 * 获取AccessToken
 *
 */
public interface AccessTokenService {
	/**
	 * 向服务器请求获取AccessToken
	 * @param appkey 银盟平台接入分配的key
	 * @param appSecret 银盟平台接入分配的appSecret
	 * @return Map
	 */
	public Map<String, Object> getAccessToken(String appkey,String appSecret);
	
	/**
	 * 获取缓存中的tok
	 * @return
	 */
	public Map<String,Object> getRedisToks(String appkey,String appSecret);
}

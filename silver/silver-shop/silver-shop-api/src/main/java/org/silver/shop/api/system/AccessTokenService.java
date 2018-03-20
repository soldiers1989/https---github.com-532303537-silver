package org.silver.shop.api.system;

import java.util.Map;

/**
 * 获取AccessToken
 *
 */
public interface AccessTokenService {
	/**
	 * 向服务器请求获取AccessToken
	 * @return String
	 */
	public Map<String, Object> getAccessToken();
	
	/**
	 * 获取缓存中的tok
	 * @return
	 */
	public Map<String,Object> getRedisToks();
}

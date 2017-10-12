package org.silver.shop.api.system;

import java.util.Map;

/**
 * 获取AccessToken
 *
 */
public interface AccessTokenService {
	/**
	 * 获取AccessToken
	 * @return String
	 */
	public Map<String, Object> getAccessToken();
}

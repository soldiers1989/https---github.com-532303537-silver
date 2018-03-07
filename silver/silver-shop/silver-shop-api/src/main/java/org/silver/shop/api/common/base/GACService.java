package org.silver.shop.api.common.base;

import java.util.Map;

public interface GACService {

	/**
	 * 获取所有海关关区信息
	 * @return Map
	 */
	public Map<String, Object> getGACInfo();

}

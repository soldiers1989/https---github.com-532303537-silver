package org.silver.shop.api.system.tenant;

import java.util.Map;

public interface MerchantBusinessService {

	/**
	 * 添加商户业务信息
	 * @param managerName 管理员名称
	 * @param datasMap 参数
	 * @return Map
	 */
	public Map<String,Object> addInfo(String managerName, Map<String, Object> datasMap);

	/**
	 * 查询商户业务信息
	 * @param datasMap 搜索参数
	 * @return Map
	 */
	public Map<String, Object> getInfo(Map<String, Object> datasMap);

}

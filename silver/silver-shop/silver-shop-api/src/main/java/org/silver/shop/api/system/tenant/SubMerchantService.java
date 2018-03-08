package org.silver.shop.api.system.tenant;

import java.util.Map;

import net.sf.json.JSONObject;

public interface SubMerchantService {

	/**
	 * 添加子商户信息
	 * @param datasMap 参数
	 * @param managerName 管理员名称
	 * @param managerId 管理员Id
	 * @return Map 
	 */
	public Map<String, Object> addSubMerchantInfo(Map<String,Object> datasMap, String managerId, String managerName);

	/**
	 * 管理员查询子商户信息
	 * @return
	 */
	public Map<String,Object> getSubMerchantInfo();

	/**
	 * 管理员修改子商户信息
	 * @param datasMap 参数
	 * @param managerId 管理员Id
	 * @param managerName 管理员名称
	 * @return Map
	 */
	public Object editSubMerchantInfo(Map<String, Object> datasMap, String managerId, String managerName);

}

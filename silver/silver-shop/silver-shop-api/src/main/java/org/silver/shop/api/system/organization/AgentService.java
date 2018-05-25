package org.silver.shop.api.system.organization;

import java.util.List;
import java.util.Map;

public interface AgentService {

	/**
	 * 添加代理商基本信息
	 * @param params 参数
	 * @param managerName 管理员名称
	 * @param managerId  管理员Id
	 * @return Map
	 */
	public Map<String, Object> addAgentBaseInfo(Map<String, Object> params, String managerId, String managerName);

	/**
	 * 根据登陆账号查询代理商
	 * @param account 名称
	 * @return List
	 */
	public List<Object> findAngetBy(String account);

	/**
	 * 获取所有代理商信息
	 * @param datasMap 参数
	 * @param size 
	 * @param page 
	 * @return Map
	 */
	public Map<String, Object> getAllAgentInfo(Map<String, Object> datasMap, int page, int size);

	/**
	 * 添加代理商下子商户
	 * @param params
	 * @return Map
	 */
	public Map<String,Object> setAgentSubMerchant(Map<String, Object> datasMap);

	/**
	 * 查询代理商下所有子商户信息
	 * @param agentId 代理商Id
	 * @param agentName 代理商名称
	 * @return Map
	 */
	public Map<String, Object> getSubMerchantInfo(String agentId, String agentName);

}

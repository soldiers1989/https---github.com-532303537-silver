package org.silver.shop.api.system;

import java.util.Map;

public interface AuthorityService {

	/**
	 * 管理员添加权限信息
	 * @param datasMap
	 * @return
	 */
	public Map<String, Object> addAuthorityInfo(Map<String, Object> datasMap);

	/**
	 * 获取所有权限信息
	 * @return
	 */
	public Map<String, Object> getAuthorityInfo();

	/**
	 * 管理员设置角色权限
	 * @param roleName 
	 * @param managerName 
	 * @return
	 */
	public Map<String, Object> setAuthorityInfo(Map<String, Object> datasMap);

	/**
	 * 管理员针对对应的用户查询权限信息
	 * @param userId 用户Id
	 * @param groupName 分组名称
	 * @return Map
	 */
	public Map<String, Object> getUserAuthorityInfo(String userId, String groupName);

	/**
	 * 管理员根据权限流水Id修改信息
	 * @param datasMap 权限信息
	 * @return Map
	 */
	public Map<String, Object> editAuthorityInfo(Map<String, Object> datasMap);

	/**
	 * 管理员删除权限信息
	 * @param authorityId 权限信息流水Id
	 * @param managerName 
	 * @return Map
	 */
	public Map<String, Object> deleteAuthorityInfo(String authorityId, String managerName);

}

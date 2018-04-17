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
	public Map<String, Object> setRoleAuthority(Map<String, Object> datasMap);

	/**
	 * 管理员针对对应的用户组查询权限信息
	 * @param groupName 用户组的名称
	 * @return Map
	 */
	public Map<String, Object> getAuthorityGroupInfo(String groupName);

}

package org.silver.shop.dao.system;

import org.silver.shop.dao.BaseDao;

import com.justep.baas.data.Table;

public interface AuthorityDao extends BaseDao{

	/**
	 * 根据用户Id与用户组的名称查询当前用户下所有权限信息
	 * @param userId 用户Id
	 * @param groupName 用户组名称
	 * @return Table
	 */
	public Table getAuthorityGroupInfo(String userId, String groupName);

	/**
	 * 根据组的名称查询组下所拥有权限信息
	 * @param groupName
	 * @return
	 */
	public Table getAuthorityGroupInfo(String groupName);

	/**
	 * 更新已存在的用户权限选中标识
	 * @param roleId 
	 * @return
	 */
	public boolean updateAuthorityCheckFlag(String roleId);

}

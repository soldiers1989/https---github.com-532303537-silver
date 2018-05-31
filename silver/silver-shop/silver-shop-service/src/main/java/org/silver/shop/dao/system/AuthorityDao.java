package org.silver.shop.dao.system;

import org.silver.shop.dao.BaseDao;

import com.justep.baas.data.Table;

public interface AuthorityDao extends BaseDao{

	/**
	 * 
	 * @param userId
	 * @param groupName
	 * @return
	 */
	public Table getAuthorityGroupInfo(String userId, String groupName);

}

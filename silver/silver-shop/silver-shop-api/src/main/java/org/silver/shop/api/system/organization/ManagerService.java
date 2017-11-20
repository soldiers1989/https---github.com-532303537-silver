package org.silver.shop.api.system.organization;

import java.util.List;
import java.util.Map;

public interface ManagerService {

	
	public List<Object> findManagerBy(String account);

	/**
	 * 管理员查询用户信息
	 * @return
	 */
	public Map<String, Object> findAllmemberInfo();

}

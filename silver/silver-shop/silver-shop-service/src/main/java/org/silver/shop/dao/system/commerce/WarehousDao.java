package org.silver.shop.dao.system.commerce;

import java.util.List;
import java.util.Map;

public interface WarehousDao {
	/**
	 * 根据实体,及Map键值对查询数据 key=(表中列名称),value=(查询参数)
	 * @param entity
	 * @param params
	 * @param page
	 * @param size
	 */
	public List<Object> findByProperty(Class entity, Map params, int page, int size);
}

package org.silver.shop.dao.common.base;

import java.util.List;

public interface CustomsPortDao {

	/**
	 * 添加实体
	 * 
	 * @param entity
	 * @return
	 */
	public boolean add(Object entity);

	/**
	 * 查询实体下全部
	 * @return
	 */
	public List<Object> findAll(Class entity , int page , int size);

}

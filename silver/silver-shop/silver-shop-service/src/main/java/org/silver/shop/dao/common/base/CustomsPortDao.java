package org.silver.shop.dao.common.base;

import java.util.List;
import java.util.Map;

public interface CustomsPortDao {
	
	/**
	 * 根据实体,及Map键值对查询数据 key=(表中列名称),value=(查询参数)
	 * 
	 * @param entity
	 * @param params
	 * @param page
	 * @param size
	 */
	public List<Object> findByProperty(Class entity, Map params, int page, int size);

	/**
	 * 保存实体
	 * 
	 * @param entity
	 * @return
	 */
	public boolean add(Object entity);

	/**
	 * 根据实体查询所有数据
	 * @param entity
	 * @param page
	 * @param size
	 * @return
	 */
	public List<Object> findAll(Class entity, int page, int size);
	
	/**
	 * 根据实体更新数据
	 * @param entity
	 * @return
	 */
	public boolean update(Object entity);

}

package org.silver.shop.dao;

import java.util.List;
import java.util.Map;

/**
 * description：共用Dao方法
 */
public interface BaseDao<T> {
	
	/**
	 * 根据实体查询所有数据(带分页查询)
	 * 
	 * @param <T>
	 * @param entity
	 * @param page
	 * @param size
	 * @return list
	 */
	public List<Object> findAll(Class<T> entity, int page, int size);

	
	/**
	 * 根据实体、列(名)、值查询数据
	 * @param entity 实体名
	 * @param params 属性
	 * @param page 
	 * @param size 
	 * @return List
	 */
	public List<Object> findByProperty(Class<T> entity,Map<String, Object> params, int page, int size);
}

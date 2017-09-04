package org.silver.shop.dao;

import java.util.List;

/**
 *	description：共用Dao方法
 */
public interface BaseDao  {
	/**
	 * 根据实体查询所有数据
	 * @param <T>
	 * @param entity
	 * @return list
	 */
	public <T> List<Object> findAll(Class<T> entity);
	
	/**
	 * 根据实体查询所有数据(带分页查询)
	 * @param <T>
	 * @param entity
	 * @param page
	 * @param size
	 * @return list
	 */
	public  <T> List<Object> findAllPage(Class<T> entity,int page, int size) ;
	
	
}

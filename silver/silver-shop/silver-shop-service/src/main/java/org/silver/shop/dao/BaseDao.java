package org.silver.shop.dao;

import java.util.List;
import java.util.Map;

import org.silver.shop.model.system.organization.Member;

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
	public List<Object> findByProperty(Class entity,Map<String, Object> params, int page, int size);


	/**
	 * 将实体类实例化
	 * @param entity
	 * @return
	 */
	public boolean add(Object entity);


	/**
	 * 根据实体类删除信息
	 * @param entity
	 * @return
	 */
	public boolean delete(Object entity);


	/**
	 * 根据实体类更新数据库信息
	 * @param entity
	 * @return
	 */
	public boolean update(Object entity);


	/**
	 * 带说明
	 * @param id
	 * @return
	 */
	public Member findMailboxbyId(long id);

	/**
	 * 统计数据库表中数据数量
	 * @param entity
	 * @return
	 */
	public Long findAllCount(Class entity);

	/**
	 * 查询数据库表中最后一条记录的自增ID
	 * @param entity
	 * @return
	 */
	public Long findLastId(Class entity);

}

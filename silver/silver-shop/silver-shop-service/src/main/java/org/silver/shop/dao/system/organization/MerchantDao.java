package org.silver.shop.dao.system.organization;

import java.util.List;
import java.util.Map;

import org.silver.shop.dao.BaseDao;

public interface MerchantDao extends BaseDao {

	/**
	 * 查询数据库表中最后一条记录的自增ID
	 * 
	 * @return long
	 */
	public long findLastId();

	/**
	 * 保存商户信息
	 * 
	 * @param entity
	 * @return
	 */
	public boolean add(Object entity);

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
	 * 根据实体更新数据
	 * @param entity
	 */
	public boolean update(Object entity);

}

package org.silver.shop.dao.system.tenant;

import java.util.List;
import java.util.Map;

import org.silver.shop.dao.BaseDao;
import org.silver.shop.dao.BaseDaoImpl;

import com.justep.baas.data.Table;

public interface MerchantWalletDao<E> extends BaseDao{

	/**
	 * 根据实体,及Map键值对查询数据 key=(表中列名称),value=(查询参数)
	 * 
	 * @param entity
	 *            实体类Class
	 * @param params
	 *            查询参数
	 * @param page
	 *            页数
	 * @param size
	 *            数据条数
	 */
	public List<Object> findByProperty(Class entity, Map params, int page, int size);

	/**
	 * 根据年份查询当前年份下的流水号总数
	 * 
	 * @param entity
	 *            实体类Class
	 * @param property
	 *            查询表中列销属性名
	 * @param year
	 *            年份
	 * @return long
	 */
	public long findSerialNoCount(Class entity, String property, int year);

	/**
	 * 将实体类实例化
	 * 
	 * @param entity
	 *            实体类名
	 * @return
	 */
	public boolean add(Object entity);

	/**
	 * 根据实体类删除信息
	 * 
	 * @param entity
	 *            实体类名
	 * @return
	 */
	public boolean delete(Object entity);

	/**
	 * 根据实体类更新数据库信息
	 * 
	 * @param entity
	 *            实体类名
	 * @return
	 */
	public boolean update(Object entity);
	
	/**
	 * 
	 * @param params
	 * @param page
	 * @param size
	 * @return
	 */
	public Table getApplication(Map<String, Object> params, int page, int size);
}

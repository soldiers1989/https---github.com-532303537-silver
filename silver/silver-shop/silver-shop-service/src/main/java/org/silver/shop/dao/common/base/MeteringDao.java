package org.silver.shop.dao.common.base;

import java.util.List;
import java.util.Map;

public interface MeteringDao {
	

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
	 * @return String
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
	 * 模糊查询总数
	 * 
	 * @param entity
	 *            类
	 * @param params
	 *            查询参数
	 * @return
	 */
	public long findByPropertyCount(Class entity, Map  params);
	
	/**
	 * 根据实体、列(名)、值模糊查询数据
	 * 
	 * @param entity
	 *            实体名
	 * @param params 主参数
	 * @param blurryMap 模糊查询参数
	 * @param page 页数
	 * @param size 数目
	 * @return List 
	 */
	public List<Object> findByPropertyLike(Class entity, Map params,Map blurryMap ,int page, int size);

	
	/**
	 * 模糊查询总数
	 * @param entity 类
	 * @param params 主参数
	 * @param blurryMap 模糊查询参数 
	 * @return
	 */
	long findByPropertyLikeCount(Class entity, Map params,Map blurryMap);
}

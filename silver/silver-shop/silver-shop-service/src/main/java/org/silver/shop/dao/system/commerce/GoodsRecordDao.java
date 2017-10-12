package org.silver.shop.dao.system.commerce;

import java.util.List;
import java.util.Map;

import org.silver.shop.model.system.commerce.GoodsContent;

public interface GoodsRecordDao {

	/**
	 * 查询商户商品基本信息,并倒序
	 * 
	 * @param params
	 *            查询参数
	 * @param descParam
	 *            倒序参数
	 * @param page
	 *            页数
	 * @param size
	 *            数据条数
	 * @return List
	 */
	public List findGoodsBaseInfo(Map<String, Object> params, String descParam, int page, int size);

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
	 * 查询数据库表中最后一条记录的自增ID
	 * 
	 * @return Long
	 */
	public Long findLastId();

	/**
	 * 根据实体、列(名)、值查询数据 倒序
	 * 
	 * @param entity
	 *            实体名
	 * @param params
	 *            属性
	 * @param descParams
	 *            倒序参数
	 * @param page
	 *            页数
	 * @param size
	 *            数据条数
	 * @return List
	 */
	public List<Object> findPropertyDesc(Class entity, Map<String, Object> params, String descParams, int page,
			int size);

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
}

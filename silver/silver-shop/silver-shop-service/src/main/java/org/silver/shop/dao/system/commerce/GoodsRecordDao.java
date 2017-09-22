package org.silver.shop.dao.system.commerce;

import java.util.List;
import java.util.Map;

public interface GoodsRecordDao {

	/**
	 * 查询商户商品基本信息,并倒序
	 * @param params 查询参数
	 * @param descParam  倒序参数
	 * @param page 页数 
	 * @param size 数据条数
	 * @return List
	 */
	public List findGoodsBaseInfo(Map<String, Object> params, String descParam, int page, int size);
	
	/**
	 * 根据实体,及Map键值对查询数据 key=(表中列名称),value=(查询参数)
	 * @param entity
	 * @param params
	 * @param page
	 * @param size
	 */
	public List<Object> findByProperty(Class entity, Map params, int page, int size);
	
	/**
	 * 查询数据库表中最后一条记录的自增ID
	 * 
	 * @return
	 */
	public Long findLastId();
}

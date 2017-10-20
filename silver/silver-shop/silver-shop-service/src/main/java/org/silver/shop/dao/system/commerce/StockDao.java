package org.silver.shop.dao.system.commerce;

import java.util.List;
import java.util.Map;

import com.justep.baas.data.Table;

public interface StockDao {
	/**
	 * 根据实体,及Map键值对查询数据 key=(表中列名称),value=(查询参数)
	 * @param entity
	 * @param params
	 * @param page
	 * @param size
	 */
	public Table getWarehousGoodsInfo(String merchantId ,String warehouseCode, int page, int size);
	
	
	/**
	 * 根据实体,及Map键值对查询数据 key=(表中列名称),value=(查询参数)
	 * @param entity
	 * @param params
	 * @param page
	 * @param size
	 */
	public List<Object> findByProperty(Class entity, Map params, int page, int size);
	
	/**
	 * 将实体类实例化
	 * 
	 * @param entity
	 *            实体类名
	 * @return
	 */
	public boolean add(Object entity);
	
	
	/**
	 * 根据实体类更新数据库信息
	 * 
	 * @param entity
	 *            实体类名
	 * @return
	 */
	public boolean update(Object entity);
}
package org.silver.shop.dao.common.base;

import java.util.List;
import java.util.Map;

import com.justep.baas.data.Table;

public interface EPortDao {
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
	 * @param entity
	 * @return
	 */
	public boolean add(Object entity);


	/**
	 * 将省市口岸三级联动,汇成一张表数据
	 * @return
	 */
	public Table findProvinceCityEport();
}

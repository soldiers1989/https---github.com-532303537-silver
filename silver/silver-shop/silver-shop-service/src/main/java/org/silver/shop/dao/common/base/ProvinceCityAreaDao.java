package org.silver.shop.dao.common.base;


import org.silver.shop.dao.BaseDao;

import com.justep.baas.data.Table;

public interface ProvinceCityAreaDao extends BaseDao{

	/**
	 * 将省市区关联成一张表数据查询
	 * @return
	 */
	public Table findAllProvinceCityArea();

	/**
	 * 将省市区邮编关联成一张表数据查询
	 * @return
	 */
	public Table findAllProvinceCityArePostal();

}

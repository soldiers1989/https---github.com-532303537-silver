package org.silver.shop.dao.common.base;


import com.justep.baas.data.Table;

public interface ProvinceCityAreaDao {

	/**
	 * 将省市区关联成一张表数据查询
	 * @return
	 */
	public Table findAllProvinceCityArea();

}

package org.silver.shop.dao.common.base;

import java.util.List;

public interface ProvinceCityAreaDao {

	/**
	 * 将省市区关联成一张表数据查询
	 * @return
	 */
	public <T> List<T> findAllCountry();
}

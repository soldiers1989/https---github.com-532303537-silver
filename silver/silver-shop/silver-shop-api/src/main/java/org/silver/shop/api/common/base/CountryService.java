package org.silver.shop.api.common.base;

import java.util.List;

import org.silver.shop.model.common.base.Country;

public interface CountryService {

	/**
	 * 查询所有国家
	 * @return
	 */
	public List<Object> findAllCountry();

}

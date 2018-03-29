package org.silver.shop.api.common.base;

import java.util.Map;

public interface CountryService {

	/**
	 * 查询所有国家
	 * @return
	 */
	public Map<String,Object> findAllCountry();

}

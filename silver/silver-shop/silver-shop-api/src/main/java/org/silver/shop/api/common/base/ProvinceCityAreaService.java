package org.silver.shop.api.common.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public interface ProvinceCityAreaService {

	/**
	 * 获取省市区三级联动
	 * @return
	 */
	public Map<String, HashMap<String, Object>> getProvinceCityArea();
	
}

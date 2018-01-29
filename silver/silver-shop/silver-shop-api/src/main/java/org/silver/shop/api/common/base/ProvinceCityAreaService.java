package org.silver.shop.api.common.base;

import java.util.Map;


public interface ProvinceCityAreaService {

	/**
	 * 获取省市区三级联动
	 * @return
	 */
	public Map<String, Object> getProvinceCityArea();


	/**
	 * 不封装对应三级联动
	 * @return
	 */
	public Map<String, Object> getProvinceCityArea2();

	
	/**
	 * 
	 * @param strArr
	 * @return
	 */
	public Object editProvinceCityAreaInfo(String[] strArr);
	
}

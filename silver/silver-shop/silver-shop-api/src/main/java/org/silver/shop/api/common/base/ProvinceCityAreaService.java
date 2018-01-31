package org.silver.shop.api.common.base;

import java.util.Map;

public interface ProvinceCityAreaService {

	/**
	 * 获取省市区三级联动
	 * 
	 * @return
	 */
	public Map<String, Object> getProvinceCityArea();

	/**
	 * 不封装对应三级联动
	 * 
	 * @return
	 */
	public Map<String, Object> getProvinceCityArea2();

	/**
	 * 修改省市区
	 * 
	 * @param flag
	 * @param json
	 * @param strArr
	 * @return
	 */
	public Map<String,Object> editProvinceCityAreaInfo(Map<String,Object> params, int flag);

}

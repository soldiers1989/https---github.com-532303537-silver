package org.silver.shop.api.common.base;

import java.util.Map;


public interface ProvinceCityAreaService {

	/**
	 * 获取省市区三级联动
	 * @return
	 */
	public Map<String, Object> getProvinceCityArea();

	/**
	 * 获取全国省份
	 * @return
	 */
	public Map<String, Object> getProvince();

	/**
	 * 获取全国城市
	 * @return
	 */
	public Map<String, Object> getCity();

	/**
	 * 获取全国区域
	 * @return
	 */
	public Map<String, Object> getArea();

	/**
	 * 不封装对应三级联动
	 * @return
	 */
	public Map<String, Object> getProvinceCityArea2();
	
}

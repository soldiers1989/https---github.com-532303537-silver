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
	 * 查询省市区封装成Map集合key=区域编码,value=省份编码+省份名称#城市编码_城市名称#区域编码—_区域名称
	 * 
	 * @return Map
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

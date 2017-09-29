package org.silver.shop.api.common.base;

import java.util.List;
import java.util.Map;

public interface EPortService {

	/**
	 * 查询口岸名称是否重复
	 * @param customsPortName
	 * @return List
	 */
	public List<Object> checkEportName(String customsPortName);

	
	/**
	 * 添加口岸
	 * @param customsPort 口岸编码
	 * @param customsPortName 口岸名称
	 * @param cityCode 关联城市编码
	 * @return 
	 */
	public Map<String, Object> addEPort(String customsPort, String customsPortName, String cityCode);

	
	/**
	 *	查询省市口岸,并进行响应等级封装
	 */
	public List<Object> findEPort();
	
}

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
	 * @param provinceName 
	 * @param provinceCode 
	 * @param cityName 
	 * @return 
	 */
	public Map<String, Object> addEPort(String customsPort, String customsPortName, String cityCode, String cityName, String provinceCode, String provinceName);

	
	/**
	 *	查询所有省市口岸
	 */
	public Map<String, Object> findAllEPort();

	
	
	/**
	 * 根据口岸自增ID,修改信息
	 * @param id
	 * @param provinceName 
	 * @param provinceCode 
	 * @param cityName 
	 * @param cityCode 
	 * @param customsPortName 
	 * @param customsPort 
	 * @return 
	 */
	public Map<String, Object> editEPotInfo(long id, String customsPort, String customsPortName, String cityCode, String cityName, String provinceCode, String provinceName);
	
}

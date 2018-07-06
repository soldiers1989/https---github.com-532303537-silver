package org.silver.shop.api.system.tenant;

import java.util.Map;

public interface MerchantIdCardCostService {

	/**
	 * 根据商户id查询商户身份证实名认证费率信息
	 * @param merchantId 商户id
	 * @return Map
	 */
	public Map<String,Object> getIdCardCostInfo(String merchantId);

	/**
	 * 获取商户口岸费率信息
	 * @param merchantId 商户id
	 * @return map
	 */
	public Map<String, Object> getInfo(String merchantId);

	/**
	 * 添加商户实名认证手续费信息
	 * @param datasMap 
	 * @return
	 */
	public Object addInfo(Map<String, Object> datasMap);
}

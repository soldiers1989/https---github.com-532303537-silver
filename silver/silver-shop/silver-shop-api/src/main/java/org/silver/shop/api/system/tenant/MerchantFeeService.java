package org.silver.shop.api.system.tenant;

import java.util.Map;

public interface MerchantFeeService {

	/**
	 * 管理员添加商户已开通口岸及平台服务费
	 * @param params
	 * @return Map
	 */
	public Map<String,Object> addMerchantServiceFee(Map<String, Object> params);
	
	
}

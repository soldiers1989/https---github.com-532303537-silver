package org.silver.shop.api.system.commerce;

import java.util.Map;

public interface WarehousService {

	/**
	 * 获取商户仓库
	 * @param merchantId
	 * @return
	 */
	public Map<String,Object> getWarehousInfo(String merchantId);


}

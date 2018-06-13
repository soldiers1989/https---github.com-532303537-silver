package org.silver.shop.api.system.log;

import java.util.Map;

public interface MerchantWalletLogService {
	
	/**
	 * 添加商户钱包日志
	 * 
	 * @param datasMap
	 *            参数
	 * @return Map
	 */
	public Map<String, Object> addWalletLog(Map<String, Object> datasMap);
}

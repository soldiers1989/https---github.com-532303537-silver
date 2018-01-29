package org.silver.shop.api.system.tenant;

import java.util.Map;

public interface ManagerWalletService {

	/**
	 * 获取所有商户钱包信息
	 * 
	 * @param page
	 * @param size
	 * @param dataMap
	 * @return
	 */
	public Map<String, Object> getMerchantWalletInfo(int page, int size, Map<String, Object> dataMap);

	/**
	 * 管理员更新商户钱包余额
	 * @param merchantId
	 * @param managerName
	 * @param amount
	 * @return
	 */
	public Map<String,Object> updateMerchantWalletAmount(String merchantId, String managerName, double amount);

}

package org.silver.shop.api.system.tenant;

import java.util.Map;

public interface MerchantWalletService {

	/**
	 * 商户钱包充值
	 * @param merchantId
	 * @param merchantName
	 * @param money
	 * @return
	 */
	public Map<String, Object> walletRecharge(String merchantId, String merchantName, Double money);

	/**
	 * 商户获取钱包信息
	 * @param merchantName 
	 * @param merchantId 
	 * @return 
	 */
	public Map<String, Object> getMerchantWallet(String merchantId, String merchantName);

}

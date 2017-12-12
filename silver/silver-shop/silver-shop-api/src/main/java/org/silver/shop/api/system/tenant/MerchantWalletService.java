package org.silver.shop.api.system.tenant;

import java.util.Map;

public interface MerchantWalletService {

	/**
	 * 商户钱包充值
	 * @param merchantId 商户Id
	 * @param merchantName 商户名称
	 * @param money 充值金额
	 * @return Map
	 */
	public Map<String, Object> walletRecharge(String merchantId, String merchantName, Double money);

	/**
	 * 商户获取钱包信息
	 * @param merchantId 商户Id
	 * @param merchantName 商户名称
	 * @return Map
	 */
	public Map<String, Object> getMerchantWallet(String merchantId, String merchantName);

	/**
	 * 商户获取钱包日志
	 * @param merchantId 商户Id
	 * @param merchantName 商户名称
	 * @param type 
	 * @param size 
	 * @param page 
	 * @return
	 */
	public Map<String, Object> getMerchantWalletLog(String merchantId, String merchantName, int type, int page, int size);

}

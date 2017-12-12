package org.silver.shop.aop.system.tenant;

import java.util.Map;


public interface MerchantWalletAop {

	/**
	 * 商户钱包充值Aop拦截
	 * @param merchantId
	 * @param merchantName
	 * @param money
	 * @return
	 */
	public Map<String, Object> walletRecharge(String merchantId, String merchantName, Double money);
}

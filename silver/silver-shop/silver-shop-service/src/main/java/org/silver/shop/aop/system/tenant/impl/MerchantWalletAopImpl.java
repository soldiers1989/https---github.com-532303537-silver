package org.silver.shop.aop.system.tenant.impl;

import java.util.Map;

import org.silver.shop.aop.system.tenant.MerchantWalletAop;
import org.silver.shop.api.system.tenant.MerchantWalletService;
import org.springframework.beans.factory.annotation.Autowired;


public class MerchantWalletAopImpl implements MerchantWalletAop{

	@Autowired
	private MerchantWalletService merchantWalletService;
	
	@Override
	public Map<String, Object> walletRecharge(String merchantId, String merchantName, Double money) {
		return merchantWalletService.walletRecharge(merchantId, merchantName, money);
	}
	
}

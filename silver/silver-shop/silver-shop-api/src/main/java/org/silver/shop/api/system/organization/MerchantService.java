package org.silver.shop.api.system.organization;

import java.util.Map;

public interface MerchantService {
	
	/**
	 * 商户登录
	 * @param account
	 * @param loginPassword
	 * @return
	 */
	public Map<String,Object> finMerchantBy(String account, String loginPassword);
	
	public Map<String,Object> merchantRegister();
}

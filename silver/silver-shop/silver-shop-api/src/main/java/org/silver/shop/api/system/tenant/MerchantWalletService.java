package org.silver.shop.api.system.tenant;

import java.util.Map;

import org.silver.shop.model.system.tenant.MerchantWalletContent;

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
	 * @param timeLimit 
	 * @return
	 */
	public Map<String, Object> getMerchantWalletLog(String merchantId, String merchantName, int type, int page, int size, int timeLimit);

	/**
	 * 商户钱包扣款
	 * 
	 * @param merchantWallet
	 *            商户钱包实体类
	 * @param balance
	 *            商户原钱包余额
	 * @param serviceFee
	 *            手续费(平台服务费)
	 * @return Map
	 */
	public Map<String, Object> walletDeduction(MerchantWalletContent merchantWallet, double balance,
			double serviceFee);
	

}

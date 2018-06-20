package org.silver.shop.api.system.tenant;

import java.util.Map;

import org.silver.shop.model.system.tenant.MerchantWalletContent;

public interface MerchantWalletService {

	/**
	 * 商户获取钱包信息
	 * @param merchantId 商户Id
	 * @param merchantName 商户名称
	 * @return Map
	 */
	public Map<String, Object> getMerchantWallet(String merchantId, String merchantName);


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

	/**
	 * 添加用户钱包日志
	 * @param orderId 订单Id
	 * @param amount 交易金额
	 * @param merchantName 商户名称
	 * @param merchantId 商户Id
	 * 
	 */
	public void addWalletRechargeLog(String merchantId, String merchantName, double amount, String orderId);
	

}

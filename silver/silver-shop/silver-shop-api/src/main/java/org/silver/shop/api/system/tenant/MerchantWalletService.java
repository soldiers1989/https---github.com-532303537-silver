package org.silver.shop.api.system.tenant;

import java.util.Map;

import org.silver.shop.model.system.organization.Manager;
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
	/**
	 * 商户线下加款申请
	 * @param datasMap 
	 * @return Map
	 */
	public Map<String, Object> merchantApplication(Map<String, Object> datasMap);
	/**
	 * 查询线下加款信息
	 * @param datasMap
	 * @return
	 */
	public Map<String, Object> getOfflineRechargeInfo(Map<String, Object> datasMap,int page,int size);


	/**
	 * 临时管理员分账
	 * @param orderId 订单id
	 * @param amount 交易金额
	 * @param managerInfo 管理员信息实体类
	 * @return
	 */
	public Map<String, Object> fenZhang(String orderId, double amount, Manager managerInfo);


	/**
	 * 商户钱包冻结资金的扣款
	 * @param merchantWallet 商户钱包实体类
	 * @param amount 金额
	 * @return Map
	 */
	public Map<String, Object> freezingFundFeduction(MerchantWalletContent merchantWallet,  double amount);


	/**
	 * 根据转移金额对商户钱包余额进行扣款，并将金额转移至冻结资金
	 * @param merchantWallet 商户钱包实体
	 * @param amount 转移金额
	 * @return Map
	 */
	public Map<String,Object> balanceTransferFreezingFunds(MerchantWalletContent merchantWallet, double amount);


	/**
	 * 根据商户id查询钱包信息后，进行对应的货款操作
	 * 
	 * @param merchantId
	 *            商户id
	 * @param amount
	 *            金额
	 * @param type
	 *            类型：add-加款、sub-扣款
	 * @return Map
	 */
	public Map<String, Object> reserveAmountOperating(String merchantId, double amount, String type);


	/**
	 * 根据商户id生成钱包校验码
	 * @param merchantId 商户id
 	 * @return
	 */
	public Map<String, Object> generateSign(String merchantId);


	/**
	 * 根据商户id查询钱包信息后，进行对应的余额操作
	 * @param merchantId 商户id
	 * @param amount 金额
	 * @param type 类型：add-加款、sub-扣款
	 * @return Map
	 */
	public Map<String, Object> balanceOperating(String merchantId, double amount, String type);


	/**
	 * 商户余额清算
	 * @param merchantId 商户id
	 * @param amount 金额
	 * @return Map
	 */
	public Map<String, Object> balanceDeduction(String merchantId, Double amount);
	
	/**
	 * 根据商户id查询钱包信息后，进行冻结资金对应的操作
	 * @param merchantId 商户id
	 * @param amount 金额
	 * @param type 类型：add-加款、sub-扣款
	 * @return Map
	 */
	public Map<String, Object> freezingFundsOperating(String merchantId, double amount, String type) ;
}

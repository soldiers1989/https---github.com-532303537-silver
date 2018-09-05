package org.silver.shop.api.system.tenant;

import java.util.Map;

import org.silver.shop.model.system.organization.Member;

public interface MemberWalletService {

	/**
	 * 将用户钱包的储备资金扣款至对应的商户钱包中
	 * @param memberId	用户Id
	 * @param merchantId 商户Id
	 * @param tradeNo 交易流水号
	 * @param amount 交易金额
	 */
	public void reserveAmountTransfer(String memberId, String merchantId, String tradeNo, Double amount);
	
	/**
	 * 添加交易日志信息
	 * @param memberInfo 用户信息
	 * @param serialNo 交易订单号
	 * @param amount 交易订单金额
	 * @return Map
	 */
	public Map<String, Object> addPayReceipt(Member memberInfo, double amount, String serialNo);

	/**
	 *	根据用户id、获取钱包信息 
	 * @param memberId 用户id
	 * @return Map
	 */
	public Map<String, Object> getInfo(String memberId);
	
	/**
	 * 用户调用银盛支付充值货款，成功后回调
	 * @param datasMap
	 * @return
	 */
	public Map<String, String> memberRechargeReceive(Map datasMap);

	/**
	 * 根据用户id,校验钱包货款金额
	 * @param memberId 用户id
	 * @param amount 金额
	 * @return Map
	 */
	public Map<String, Object> checkReserveAmount(String memberId, double amount);

	/**
	 * 根据用户id，生成用户钱包校验码
	 * @param memberId 
	 * @return 
	 */
	public Map<String,Object> generateSign(String memberId);

	/**
	 * 储备资金转移至冻结金额
	 * @param memberId
	 * @param amount
	 * @return
	 */
	public Map<String, Object> reserveAmountDeduction(String memberId, double amount);

	public Object tmpAddAmount(String memberId, double amount);

	/**
	 * 校验用户交易密码
	 * @param memberId 用户id
	 * @param payPassword 交易密码
	 * @return Map
	 */
	public Map<String,Object> checkPayPassword(String memberId, String payPassword);
	
	/**
	 * 根据用户id查询钱包信息后，进行对应的冻结资金操作
	 * 
	 * @param memberId
	 *            用户id
	 * @param amount
	 *            金额
	 * @param type
	 *            类型：add-加款、sub-扣款
	 * @return Map
	 */
	public Map<String, Object> freezingFundsOperating(String memberId, double amount, String type);

	
	/**
	 * 根据用户id查询钱包信息后，进行对应的货款操作
	 * 
	 * @param memberId
	 *            用户id
	 * @param amount
	 *            金额
	 * @param type
	 *            类型：add-加款、sub-扣款
	 * @return Map
	 */
	public Map<String, Object> reserveAmountOperating(String memberId, double amount, String type);

}

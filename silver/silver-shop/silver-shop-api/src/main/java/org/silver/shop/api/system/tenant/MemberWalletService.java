package org.silver.shop.api.system.tenant;

public interface MemberWalletService {

	/**
	 * 将用户钱包的储备资金扣款至对应的商户钱包中
	 * @param memberId	用户Id
	 * @param merchantId 商户Id
	 * @param tradeNo 交易流水号
	 * @param amount 交易金额
	 */
	public void reserveAmountTransfer(String memberId, String merchantId, String tradeNo, Double amount);

}

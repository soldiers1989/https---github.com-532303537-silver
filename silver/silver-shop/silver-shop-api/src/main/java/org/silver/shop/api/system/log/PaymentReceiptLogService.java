package org.silver.shop.api.system.log;

import java.util.Map;

public interface PaymentReceiptLogService {

	/**
	 * 针对商户发起国的交易，添加交易记录
	 * @param merchantId 商户Id
	 * @param amount 交易金额
	 * @param orderId 交易订单Id
	 * @param operator 操作人
	 * @param type 类型：recharge(充值)、transfer(转账)、withdraw(提现)
	 * @return Map
	 */
	public Map<String, Object> addMerchantLog(String merchantId, double amount, String orderId, String operator, String type);

}

package org.silver.shop.api.system.log;

import java.util.Map;

public interface PaymentReceiptLogService {

	/**
	 * 根据商户Id,添加商户的交易记录
	 * @param merchantId 商户Id
	 * @param amount 交易金额
	 * @param orderId 交易订单Id
	 * @param operator 操作人
	 * @return Map
	 */
	public Map<String, Object> addPaymentReceiptLog(String merchantId, double amount, String orderId, String operator);

}

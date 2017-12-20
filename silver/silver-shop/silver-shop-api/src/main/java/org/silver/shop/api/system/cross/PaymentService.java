package org.silver.shop.api.system.cross;

import java.util.Map;

public interface PaymentService {

	public Map<String,Object> updatePaymentStatus(Map<String, Object> datasMap);

	/**
	 * 根据支付单Id发起支付单备案
	 * @param merchantId 
	 * @param merchantId
	 * @param proxyParentId 
	 * @param merchantName 
	 * @param proxyParentName 
	 * @param tradeIDs
	 * @return
	 */
	public Object sendMpayByRecord(String merchantId, Map<String,Object> recordMap, String tradeNoPack, String proxyParentId, String merchantName, String proxyParentName);
	
	/**
	 * 异步回调支付单备案信息
	 * @param datasMap
	 * @return
	 */
	public Map<String, Object> updatePayRecordInfo(Map<String, Object> datasMap);

	/**
	 * 获取商户支付单信息
	 * @param merchantId
	 * @param merchantName
	 * @param params 
	 * @return
	 */
	public Object getMpayRecordInfo(String merchantId, String merchantName, Map<String, Object> params,int page,int size);
}

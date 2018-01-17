package org.silver.shop.api.system.cross;

import java.util.List;
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
	 * @param merchantId 商户Id
	 * @param merchantName 商户名称
	 * @param params 
	 * @return
	 */
	public Object getMpayRecordInfo(String merchantId, String merchantName, Map<String, Object> params,int page,int size);

	/**
	 * 获取商户支付单报表
	 * @param merchantId 商户Id
	 * @param merchantName 商户名
	 * @param page 页数
	 * @param size 每页数目
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @return Map
	 */
	public Map<String, Object> getMerchantPaymentReport(String merchantId, String merchantName, int page, int size,
			String startDate, String endDate);
	
	/**
	 * 根据订单id 批量生成相应支付单
	 * 
	 * @param merchant_no 商户Id
	 * @param orderIDs 订单Id
	 * @param serialNo 流水号
	 * @param realRowCount 总数
	 * @return Map
	 */
	public Map<String, Object> groupCreateMpay(String merchant_no, List<String> orderIDs, String serialNo,int realRowCount);
}

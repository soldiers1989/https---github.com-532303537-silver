package org.silver.shop.api.system.manual;

import java.util.List;
import java.util.Map;

public interface MpayService {

	/**
	 * 根据订单id 列 批量生成相应支付单
	 * @param merchant_no
	 * @param orderIDs
	 * @return
	 */
	public Map<String, Object> groupCreateMpay(String merchant_no, List<String> orderIDs);

	

	/**
	 * 根据订单Id发起订单备案
	 * @param merchantId
	 * @param recordMap
	 * @param orderNoPack
	 * @param proxyParentName 
	 * @param merchantName 
	 * @param proxyParentId 
	 * @return
	 */
	public Object sendMorderRecord(String merchantId, Map<String, Object> recordMap, String orderNoPack, String proxyParentId, String merchantName, String proxyParentName);

	/**
	 * 异步回调订单备案信息
	 * @param datasMap
	 * @return
	 */
	public Map<String, Object> updateOrderRecordInfo(Map<String, Object> datasMap);

	
	
	

}

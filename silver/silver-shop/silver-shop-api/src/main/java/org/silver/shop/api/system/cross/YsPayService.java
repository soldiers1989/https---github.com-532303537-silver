package org.silver.shop.api.system.cross;

import java.util.Map;


public interface YsPayService {

	/**
	 * 检查商户订单信息
	 * @param memberId
	 * @param orderId
	 * @return
	 */
	public Map<String,Object> checkOrderInfo(String memberId, String orderId);

}

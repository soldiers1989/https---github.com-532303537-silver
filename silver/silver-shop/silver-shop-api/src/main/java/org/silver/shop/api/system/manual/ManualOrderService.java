package org.silver.shop.api.system.manual;

import java.util.Map;

public interface ManualOrderService {

	/**
	 * 更新手工订单信息
	 * @param datasMap 修改订单参数
	 * @return Map
	 */
	public Map<String, Object> updateManualOrderInfo(Map<String, Object> datasMap);

}

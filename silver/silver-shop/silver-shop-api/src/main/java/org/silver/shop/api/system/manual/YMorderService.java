package org.silver.shop.api.system.manual;

import java.util.Map;

public interface YMorderService {
	

	/**
	 * 订单支付结果处理
	 * @param order_id订单号
	 * @param trade_no交易流水
	 * @param trade_status 状态 （TRADE_SUCCESS  成功）
	 * @return
	 */
	Map<String, Object> doCallBack(String order_id, String trade_no, String trade_status);
}

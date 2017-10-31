package org.silver.shop.api.system.cross;

import java.util.Map;


public interface YsPayReceiveService {

	/**
	 * 支付回调后,处理支付单,订单的存储与发起
	 * @param datasMap 支付回调信息
	 * @return
	 */
	public Map<String,Object> ysPayReceive(Map<String, Object> datasMap);

}

package org.silver.shop.api.system.cross;

import java.util.Map;


public interface YsPayReceiveService {

	/**
	 * 银盛支付回调,处理支付单,订单的存储与发起
	 * @param datasMap 支付回调信息
	 * @return Map	
	 */
	public Map<String,Object> ysPayReceive(Map<String, Object> datasMap) ;

	/**
	 * 银盟用户钱包支付,处理订单
	 * @param datasMap 支付信息
	 * @return Map
	 */
	public Map<String,Object> balancePayReceive(Map<String,Object> datasMap);
}

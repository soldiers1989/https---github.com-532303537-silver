package org.silver.shop.api.system.tenant;

import java.util.Map;

public interface AgentWalletService {

	/**
	 * 获取代理商钱包日志
	 * @param proxyUUid 代理商Id
	 * @param proxyName 代理商名称
	 * @param type (查询时间范围 1-三个月内,2-一年内)
	 * @param page 页数
	 * @param size 数目
	 * @return Map
	 */
	public Map<String, Object> getProxyWalletLog(String proxyUUid, String proxyName, int type, int page, int size);

	/**
	 * 获取代理商钱包信息
	 * @param agentId 代理商Id
	 * @param agentName 代理商名称
	 * @return Map
	 */
	public Map<String, Object> getAgentWalletInfo(String agentId, String agentName);
}

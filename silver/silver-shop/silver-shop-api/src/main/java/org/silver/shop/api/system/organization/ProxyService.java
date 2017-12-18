package org.silver.shop.api.system.organization;

import java.util.List;
import java.util.Map;


public interface ProxyService<T> {
	
	/**
	 * 查询代理商
	 * @param account 代理商名称
	 * @return list
	 */
	public List<T> findProxyBy(String account);

	/**
	 * 获取代理商下商户信息
	 * @param proxyUUid 代理商Id
	 * @return Map
	 */
	public Map<String, Object> getProxyMerchantInfo(String proxyUUid);

	
}

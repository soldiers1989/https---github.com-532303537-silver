package org.silver.shop.api.system.log;

import java.util.Map;

public interface MemberWalletLogService {

	/**
	 * 添加用户钱包流水日志
	 * @param params 参数
	 * @return Map
	 */
	public Map<String,Object> addWalletLog(Map<String, Object> params);

}

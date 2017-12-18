package org.silver.shop.api.system.tenant;

import java.util.Map;

import net.sf.json.JSONObject;

public interface WalletLogService {

	/**
	 * 添加钱包日志
	 * @param type	1-用户,2-商户,3-代理商
	 * @param params 参数
	 * @return Map
	 */
	public Map<String, Object> addWalletLog(int type, JSONObject params);
}

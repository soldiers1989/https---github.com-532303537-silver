package org.silver.shop.api.system.tenant;

import java.util.Map;

import net.sf.json.JSONObject;

public interface SubMerchantService {

	/**
	 * 添加子商户信息
	 * @param json 参数
	 * @return Map
	 */
	public Map<String, Object> addSubMerchantInfo(JSONObject json);

}

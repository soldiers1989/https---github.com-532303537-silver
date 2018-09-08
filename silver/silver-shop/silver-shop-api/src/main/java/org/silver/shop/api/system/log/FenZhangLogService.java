package org.silver.shop.api.system.log;

import java.util.Map;

public interface FenZhangLogService {

	/**
	 * 保存分账日志
	 * @return
	 */
	public Map<String, Object> saveFenZhangLog(Map<String,Object> datasMap );

}

package org.silver.shop.api.system.log;

import java.util.List;
import java.util.Map;

public interface ErrorLogsService {

	/**
	 * 添加错误日志
	 * 
	 * @param errorList
	 *            错误List
	 * @param totalCount
	 *            总数
	 * @param serialNo
	 *            批次号
	 * @param merchantName
	 * @param merchantId
	 * @param action
	 * @return Map
	 */
	public Map<String, Object> addErrorLogs(List<Map<String, Object>> errorList, int totalCount, String serialNo,
			String merchantId, String merchantName, String action);

}

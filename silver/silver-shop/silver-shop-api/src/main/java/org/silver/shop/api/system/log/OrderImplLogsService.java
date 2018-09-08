package org.silver.shop.api.system.log;

import java.util.List;
import java.util.Map;

public interface OrderImplLogsService {

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
	 *            商户名称
	 * @param merchantId
	 *            商户Id
	 * @param action
	 *            动作名称
	 * @return Map
	 */
	public Map<String, Object> addErrorLogs(List<Map<String, Object>> errorList, int totalCount, String serialNo,
			String merchantId, String merchantName, String action);

	/**
	 * 商户获取日志信息
	 * 
	 * @param page
	 *            页数
	 * @param size
	 *            数目
	 * @return Map
	 */
	public Object merchantGetErrorLogs(Map<String, Object> params, int page, int size);

}

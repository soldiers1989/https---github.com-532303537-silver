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

	/**
	 * 商户获取日志信息
	 * @param type 类型1-错误,2-警告订单超额....待续
	 * @param startDate 开始日期
	 * @param endDate 结束日期
	 * @param page 页数
	 * @param size 数目
	 * @param serialNo 批次号
	 * @param action 操作名称
	 * @param blurryStr 
	 * @return Map
	 */
	public Object merchantGetErrorLogs(Map<String,Object> params,int page,int size);

}

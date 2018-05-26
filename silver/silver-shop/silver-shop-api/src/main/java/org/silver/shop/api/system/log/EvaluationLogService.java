package org.silver.shop.api.system.log;

import java.util.Map;

public interface EvaluationLogService {

	/**
	 * 获取评论日志信息
	 * @param datasMap 
	 * @param page 页数
	 * @param size 数目
	 * @return 
	 */
	public Map<String, Object> getlogsInfo(Map<String, Object> datasMap, int page, int size);

	public Map<String, Object> tempLogs();

}

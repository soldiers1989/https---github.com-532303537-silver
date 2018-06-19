package org.silver.shop.api.system.cross;

import java.util.Map;

public interface ReportsService {

	/**
	 * 查询综合报表信息详情
	 * @param params 查询参数
	 * @return
	 */
	public Map<String, Object> getSynthesisReportDetails(Map<String, Object> params);

}

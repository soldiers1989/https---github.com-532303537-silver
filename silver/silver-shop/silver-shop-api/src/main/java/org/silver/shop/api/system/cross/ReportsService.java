package org.silver.shop.api.system.cross;

import java.util.Map;

public interface ReportsService {

	/**
	 * 查询综合报表信息详情
	 * @param params 查询参数
	 * @return
	 */
	public Map<String, Object> getSynthesisReportDetails(Map<String, Object> params);

	/**
	 * 查询身份证实名认证报表详情
	 * @param params
	 * @return
	 */
	public Map<String, Object> getIdCardCertification(Map<String, Object> params);

}

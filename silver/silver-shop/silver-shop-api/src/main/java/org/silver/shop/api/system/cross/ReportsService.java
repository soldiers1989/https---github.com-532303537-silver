package org.silver.shop.api.system.cross;

import java.util.Map;

import com.alibaba.fastjson.JSONArray;

public interface ReportsService {

	/**
	 * 查询综合报表信息详情
	 * @param datasMap 查询参数
	 * @return
	 */
	public Map<String, Object> getSynthesisReportDetails(Map<String, Object> datasMap);

	/**
	 * 查询身份证实名认证报表详情
	 * @param params
	 * @return
	 */
	public Map<String, Object> getIdCardCertification(Map<String, Object> params);

	/**
	 * 根据查询出的订单报表详情进行对应的身份证实名认证报表数据合并返回
	 * @param jsonArray
	 * @return
	 */
	public Map<String, Object> oldGetSynthesisReport(JSONArray jsonArray);

	/**
	 * 临时创建报表数据
	 * @param merchantId 商户id
	 * @return Map
	 */
	public Map<String, Object> tmpCreate(String merchantId);

	/**
	 * 查询已生成的综合报表数据
	 * @param datasMap 查询参数
	 * @return Map
	 */
	public Map<String, Object> getSynthesisReportInfo(Map<String, Object> datasMap);

}

package org.silver.shop.dao.system.cross;

import java.util.List;
import java.util.Map;

import org.silver.shop.dao.BaseDao;
import org.silver.shop.model.system.log.SynthesisReportLog;

import com.justep.baas.data.Table;

public interface ReportsDao extends BaseDao{
	
	/**
	 * 根据日期、商户id查询对应的身份证实名认证报表数据
	 * @param params
	 * @return
	 */
	public Table getIdCardDetails(Map<String, Object> params);

	/**
	 * 获取身份证认证报表详情
	 * @param params 
	 * @return
	 */
	public Table getIdCardCertificationDetails(Map<String, Object> params);

	/**
	 * 查询订单报表详情
	 * @param datasMap 
	 * @return Table
	 */
	public Table getOrderReportDetail(Map<String, Object> datasMap);

	/**
	 * 根据制定的月份查询历史报表中的最后一条数据
	 * @param date 日期格式：yyyy-MM
	 * @param merchantId 商户id
	 * @param merchantId2 
	 */
	public List<SynthesisReportLog> findByMonth(String monthFirstDate, String strDayBefore, String merchantId);

	/**
	 * 根据支付单统计报表
	 * @param datasMap
	 * @return
	 */
	public Table getPaymentReportDetails(Map<String, Object> datasMap);

	
	/**
	 * 查询每日报表详情
	 * @param params 查询条件参数
	 * @param backCoverFee 封底手续费
	 * @param fee 平台服务费率
	 * @return Table
	 */
	public Table getDailyReportDetails(Map<String, Object> params, double fee, double backCoverFee);

	
	/**
	 * 自由参数查询报表信息
	 * @param datasMap
	 * @return 
	 */
	public Table getReport(Map<String, Object> datasMap);

	public List<SynthesisReportLog> getReportInfo(Map<String, Object> params, int page, int size);
	
}

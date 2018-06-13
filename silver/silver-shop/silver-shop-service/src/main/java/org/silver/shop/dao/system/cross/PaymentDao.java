package org.silver.shop.dao.system.cross;


import java.util.List;
import java.util.Map;

import org.silver.shop.dao.BaseDao;
import org.silver.shop.model.system.manual.Morder;

import com.justep.baas.data.Table;

public interface PaymentDao  extends BaseDao{

	/**
	 * 查询商户每日支付单报表
	 * @param class1
	 * @param paramsMap
	 * @param page
	 * @param size
	 * @return
	 */
	public Table getPaymentReport(Class<Morder> class1, Map<String, Object> paramsMap, int page, int size);

	/**
	 * 查询统计手工支付单总金额
	 * @param list 支付单Id集合
	 * @return double 支付单总金额
	 */
	public double statisticalManualPaymentAmount(List<Object> list);

	/**
	 * 代理商查询旗下所有商户支付单报表信息
	 * @param datasMap
	 * @return
	 */
	public Table getAgentPaymentReport(Map<String, Object> datasMap);
	
	
	/**
	 * 查询支付单回调失败的支付单信息
	 * @param page
	 * @param size
	 * @return Table
	 */
	public List getFailPaymentInfo(Class entity, Map<String, Object> params, int page, int size);

	/**
	 * 当支付单实际支付金额不足100提升至100,后统计支付单金额
	 * @param itemList
	 * @return
	 */
	public double backCoverStatisticalManualPaymentAmount(List<Object> itemList);

	/**
	 * 根据订单Id,统计订单总金额
	 * @param orderIdList 
	 */
	public double statisticsManualOrderAmount(List<String> orderIdList);

	/**
	 * 查询手工支付单信息
	 * @param params 查询参数
	 * @return Table
	 */
	public Table getPaymentReportInfo(Map<String, Object> params);

	/**
	 * 查询手工支付单详情
	 * @param params 查询参数
	 * @return
	 */
	public Table getPaymentReportDetails(Map<String, Object> params);
}

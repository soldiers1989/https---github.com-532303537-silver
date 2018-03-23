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
	 * @return double 总金额
	 */
	public double statisticalManualPaymentAmount(List<Object> list);

	/**
	 * 代理商查询旗下所有商户支付单报表信息
	 * @param datasMap
	 * @return
	 */
	public Table getAgentPaymentReport(Map<String, Object> datasMap);
	
}

package org.silver.shop.dao.system.cross;


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
	
}

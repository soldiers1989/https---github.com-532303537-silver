package org.silver.shop.dao.system.cross.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.cross.PaymentDao;
import org.silver.shop.model.system.manual.Morder;
import org.silver.util.StringEmptyUtils;
import org.springframework.stereotype.Repository;

import com.justep.baas.data.DataUtils;
import com.justep.baas.data.Table;

@Repository("paymentDao")
public class PaymentDaoImpl extends BaseDaoImpl implements PaymentDao {

	@Override
	public Table getPaymentReport(Class<Morder> class1, Map<String, Object> paramsMap, int page, int size) {
		Session session = null;
		try {
			List<Object> sqlParams = new ArrayList<>();
			sqlParams.add(paramsMap.get("merchantId") + "");
			String startDate = paramsMap.get("startDate") + "";
			String endDate = paramsMap.get("endDate") + "";
			String sql = "SELECT count(t1.trade_no) AS tradeCount,sum(t1.pay_amount) AS totalAmount,(sum(t1.pay_amount) * 0.002) AS price,DATE_FORMAT(t1.create_date, '%Y-%m-%d') as date FROM 	ym_shop_manual_mpay t1 "
					+ " WHERE  t1.merchant_no = ? AND t1.pay_record_status = '3' ";
			if (StringEmptyUtils.isNotEmpty(startDate)) {
				sql += " AND DATE_FORMAT(t1.create_date, '%m-%d-%Y') >= DATE_FORMAT( ? ,'%Y-%m-%d') ";
				sqlParams.add(startDate);
			}
			if (StringEmptyUtils.isNotEmpty(endDate)) {
				sql += " AND DATE_FORMAT(t1.create_date, '%m-%d-%Y') <= DATE_FORMAT( ? ,'%Y-%m-%d') ";
				sqlParams.add(endDate);
			}
			sql += " GROUP BY DATE_FORMAT(t1.create_date, '%Y-%m-%d') ";
			session = getSession();
			Table t = null;
			if (page > 0 && size > 0) {
				page = page - 1;
				t = DataUtils.queryData(session.connection(), sql, sqlParams, null, page * size, size);
			} else {
				t = DataUtils.queryData(session.connection(), sql, sqlParams, null, null, null);
			}
			session.connection().close();
			session.close();
			return t;
		} catch (Exception re) {
			re.printStackTrace();
			return null;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}
	
}

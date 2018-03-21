package org.silver.shop.dao.system.cross.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
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
			String startDate = paramsMap.get("startDate") + "";
			String endDate = paramsMap.get("endDate") + "";
			String sql = "SELECT count(t1.trade_no) AS tradeCount,sum(t1.pay_amount) AS totalAmount,(sum(t1.pay_amount) * 0.002) AS price,DATE_FORMAT(t1.create_date, '%Y-%m-%d') as date FROM 	ym_shop_manual_mpay t1 "
					+ " WHERE (t1.pay_record_status = '3' OR t1.pay_record_status = '2') AND t1.del_flag = '0' ";
			String merchantId = paramsMap.get("merchantId") + "";	
			if(StringEmptyUtils.isNotEmpty(merchantId)){
				sql += " AND t1.merchant_no = ? ";
				sqlParams.add(merchantId);
			}
			String merchantName = paramsMap.get("merchantName") + "";	
			if(StringEmptyUtils.isNotEmpty(merchantName)){
				sql += " AND t1.create_by = ? ";
				sqlParams.add(merchantName);
			}
			if (StringEmptyUtils.isNotEmpty(startDate)) {
				sql += " AND DATE_FORMAT(t1.create_date, '%Y-%m-%d') >= DATE_FORMAT( ? ,'%Y-%m-%d') ";
				sqlParams.add(startDate);
			}
			if (StringEmptyUtils.isNotEmpty(endDate)) {
				sql += " AND DATE_FORMAT(t1.create_date, '%Y-%m-%d') <= DATE_FORMAT( ? ,'%Y-%m-%d') ";
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

	@Override
	public double statisticalManualPaymentAmount(List<Object> list) {
		Session session = null;
		try {
			StringBuilder sbSQL = new StringBuilder(
					" SELECT SUM(t1.pay_amount)   FROM ym_shop_manual_mpay t1 WHERE trade_no IN ( ");
			for (int i = 0; i < list.size(); i++) {
				sbSQL.append(" ? , ");
			}
			//删除结尾的逗号
			sbSQL.deleteCharAt(sbSQL.length() - 2);
			sbSQL.append(" ) ");
			session = getSession();
			Query query = session.createSQLQuery(sbSQL.toString());
			for (int i = 0; i < list.size(); i++) {
				Map<String, Object> treadeMap = (Map<String, Object>) list.get(i);
				query.setString(i, treadeMap.get("treadeNo") + "");
			}
			List resources = query.list();
			session.close();
			return (double) resources.get(0);
		} catch (Exception re) {
			re.printStackTrace();
			return -1;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}
	
}

package org.silver.shop.dao.system.commerce.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.commerce.OrderDao;
import org.silver.shop.model.system.manual.Morder;
import org.silver.util.StringEmptyUtils;
import org.springframework.stereotype.Repository;

import com.github.pagehelper.StringUtil;
import com.justep.baas.data.DataUtils;
import com.justep.baas.data.Table;

@Repository("orderDao")
public class OrderDaoImpl extends BaseDaoImpl implements OrderDao {

	@Override
	public Table getMerchantOrderInfo(String merchantId, int page, int size) {
		Session session = null;
		try {
			String sql = "SELECT t1.*, t2.orderId,t2.entOrderNo,t2.freight,t2.consolidatedTax,t2.orderTotalPrice,t2.recipientName,t2.recipientCardId,t2.recipientTel,t2.recipientAddr,t2.recipientCountryCode,t2.recProvincesCode,t2.recCityCode, t2.recAreaCode "
					+ "FROM ym_shop_order_goods_content	 t1 LEFT JOIN ym_shop_order_content t2 ON t1.orderId = t2.orderId "
					+ "and t1.merchantId = ?";
			List<Object> sqlParams = new ArrayList<>();	
			sqlParams.add(merchantId);
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
			// log.error("查询数据出错！", re);
			return null;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public List<Object> searchTimOutOrder(Class entity, Map params, String time) {
		Session session = null;
		String entName = entity.getSimpleName();
		try {
			session = getSession();
			StringBuilder hql = new StringBuilder();
			hql.append("from " + entName + " model ");
			List<Object> list = new ArrayList<>();
			if (params != null && params.size() > 0) {
				hql.append(" where ");
				String property;
				Iterator<String> is = params.keySet().iterator();
				while (is.hasNext()) {
					property = is.next();
					hql.append("model." + property + " = " + " ? " + " and ");
					list.add(params.get(property));
				}
			}
			if (StringUtil.isNotEmpty(time) && !"null".equals(time)) {
				hql.append("model.createDate" + " <= '" + time + "' and ");
			}
			hql.append(" 1=1 ");
			Query query = session.createQuery(hql.toString());
			if (!list.isEmpty()) {
				for (int i = 0; i < list.size(); i++) {
					query.setParameter(i, list.get(i));
				}
			}
			List<Object> results = query.list();
			session.close();
			return results;
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
	public Table getOrderDailyReport(Class class1, Map paramsMap, int page, int size) {
		Session session = null;
		try {
			List<Object> sqlParams = new ArrayList<>();
			String startDate = paramsMap.get("startDate") + "";
			String endDate = paramsMap.get("endDate") + "";
			String sql = "SELECT count(t1.order_id) AS orderCount, sum(t1.FCY) as total,(sum(t1.FCY) * 0.001)  AS price,DATE_FORMAT(t1.create_date, '%Y-%m-%d') AS date FROM ym_shop_manual_morder t1 "
					+ "WHERE (t1.order_record_status = '3' OR t1.order_record_status = '2')  AND t1.del_flag = '0' ";
			String merchantId = paramsMap.get("merchantId") + "";
			if (StringEmptyUtils.isNotEmpty(merchantId)) {
				sql += " AND t1.merchant_no = ? ";
				sqlParams.add(merchantId);
			}
			String merchantName = paramsMap.get("merchantName") + "";
			if (StringEmptyUtils.isNotEmpty(merchantName)) {
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
	public boolean managerDeleteTestOrder() {
		Session session = null;
		try {
			session = getSession();
			Transaction tra = session.beginTransaction();
			String sql = "DELETE t1,t2 FROM ym_shop_manual_morder t1 LEFT JOIN ym_shop_manual_morder_sub t2 ON t1.merchant_no =t2.merchant_no "
					+ "WHERE t1.merchant_no = 'MerchantId_00047' AND t1.create_by = '测试账号'";
			Query q = session.createQuery(sql);
			q.executeUpdate();
			session.flush();
			session.close();
			tra.commit();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}

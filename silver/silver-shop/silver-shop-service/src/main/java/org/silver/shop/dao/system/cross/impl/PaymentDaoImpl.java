package org.silver.shop.dao.system.cross.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.cross.PaymentDao;
import org.silver.shop.model.system.manual.ManualOrderResendContent;
import org.silver.shop.model.system.manual.ManualPaymentResendContent;
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
			String sql = "SELECT DATE_FORMAT(t1.create_date, '%Y-%m-%d') AS date,COUNT(t1.trade_no) as paymentCount,SUM(t1.pay_amount) AS amount,(SUM(t1.pay_amount) * 0.002	) AS Fee,t2.merchantId,t2.merchantName,t2.agentParentId,t2.agentParentName FROM	ym_shop_manual_mpay t1"
					+ " LEFT JOIN ym_shop_merchant t2 ON t1.merchant_no = t2.merchantId WHERE 	(t1.pay_record_status = 3 or t1.pay_record_status = 2 AND t1.del_flag = 0 )  ";
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
			sql += " GROUP BY DATE_FORMAT(t1.create_date, '%Y-%m-%d'),t2.merchantName  ";
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
					" SELECT SUM(t1.pay_amount)  AS count FROM ym_shop_manual_mpay t1 WHERE t1.networkStatus = 0 AND trade_no IN ( ");
			for (int i = 0; i < list.size(); i++) {
				sbSQL.append(" ? , ");
			}
			// 删除结尾的逗号
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
			if (resources != null && !resources.isEmpty() && StringEmptyUtils.isNotEmpty(resources.get(0))) {
				return (double) resources.get(0);
			}
			return 0;
		} catch (Exception re) {
			re.printStackTrace();
			return -1;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public Table getAgentPaymentReport(Map<String, Object> datasMap) {
		Session session = null;
		try {
			List<Object> sqlParams = new ArrayList<>();
			String startDate = datasMap.get("startDate") + "";
			String endDate = datasMap.get("endDate") + "";

			String sql = "SELECT DATE_FORMAT(t1.create_date, '%Y-%m-%d') AS date,COUNT(t1.trade_no) as paymentCount,SUM(t1.pay_amount) AS amount,(SUM(t1.pay_amount) * 0.002	) AS Fee,t2.merchantId,t2.merchantName,t2.agentParentId,t2.agentParentName FROM	ym_shop_manual_mpay t1 "
					+ " LEFT JOIN ym_shop_merchant t2 ON t1.merchant_no = t2.merchantId WHERE 	(t1.pay_record_status = 3 or t1.pay_record_status = 2 AND t1.del_flag = 0 )";
			String merchantId = datasMap.get("merchantId") + "";
			if (StringEmptyUtils.isNotEmpty(merchantId)) {
				sql += " AND t2.merchantId = ? ";
				sqlParams.add(merchantId);
			}
			String agentParentId = datasMap.get("agentParentId") + "";
			if (StringEmptyUtils.isNotEmpty(agentParentId)) {
				sql += " AND t2.agentParentId = ? ";
				sqlParams.add(merchantId);
			}
			if (StringEmptyUtils.isNotEmpty(startDate)) {
				sql += " AND DATE_FORMAT(t1.create_date, '%Y-%m-%d') >= DATE_FORMAT( ? ,'%Y-%m-%d') ";
				sqlParams.add(startDate);
			}
			if (StringEmptyUtils.isNotEmpty(endDate)) {
				sql += " AND DATE_FORMAT(t1.create_date, '%Y-%m-%d') <= DATE_FORMAT( ? ,'%Y-%m-%d') ";
				sqlParams.add(endDate);
			}
			sql += " GROUP BY DATE_FORMAT(t1.create_date, '%Y-%m-%d'),t2.merchantName ";
			session = getSession();
			Table t = DataUtils.queryData(session.connection(), sql, sqlParams, null, null, null);
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
	public List<Object> getReplyThirdPartyFailInfo(Class entity, Map<String, Object> params, int page, int size) {
		Session session = null;
		String entName = entity.getSimpleName();
		try {
			session = getSession();
			String hql = " FROM " + entName + " model ";
			List<Object> list = new ArrayList<>();
			if (params != null && params.size() > 0) {
				hql += "where ";
				String property;
				Iterator<String> is = params.keySet().iterator();
				while (is.hasNext()) {
					property = is.next();
					hql = hql + "model." + property + "=" + " ? " + " AND ";
					list.add(params.get(property));
				}
				hql += " model.resendCount < 10 ORDER BY id DESC";
			}
			Query query = session.createQuery(hql);
			if (!list.isEmpty()) {
				for (int i = 0; i < list.size(); i++) {
					query.setParameter(i, list.get(i));
				}
			}
			if (page > 0 && size > 0) {
				query.setFirstResult((page - 1) * size).setMaxResults(size);
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
	public double backCoverStatisticalManualPaymentAmount(List<Object> itemList) {
		Session session = null;
		try {
			StringBuilder sbSQL = new StringBuilder(
					" SELECT SUM(CASE WHEN t1.pay_amount < 100 THEN 100 ELSE t1.pay_amount END ) AS amount FROM ym_shop_manual_mpay t1 WHERE t1.networkStatus = 0 AND trade_no IN ( ");
			for (int i = 0; i < itemList.size(); i++) {
				sbSQL.append(" ? , ");
			}
			// 删除结尾的逗号
			sbSQL.deleteCharAt(sbSQL.length() - 2);
			sbSQL.append(" ) ");
			session = getSession();
			Query query = session.createSQLQuery(sbSQL.toString());
			for (int i = 0; i < itemList.size(); i++) {
				Map<String, Object> treadeMap = (Map<String, Object>) itemList.get(i);
				query.setString(i, treadeMap.get("treadeNo") + "");
			}
			List resources = query.list();
			session.close();
			if (resources != null && !resources.isEmpty() && StringEmptyUtils.isNotEmpty(resources.get(0))) {
				return (double) resources.get(0);
			}
			return 0;
		} catch (Exception re) {
			re.printStackTrace();
			return -1;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public double statisticsManualOrderAmount(List<String> orderIdList) {
		Session session = null;
		try {
			StringBuilder sbSQL = new StringBuilder(
					" SELECT SUM(t1.ActualAmountPaid) FROM ym_shop_manual_morder t1 WHERE t1.order_id IN ( ");
			for (int i = 0; i < orderIdList.size(); i++) {
				sbSQL.append(" ? , ");
			}
			// 删除结尾的逗号
			sbSQL.deleteCharAt(sbSQL.length() - 2);
			sbSQL.append(" ) ");
			session = getSession();
			Query query = session.createSQLQuery(sbSQL.toString());
			for (int i = 0; i < orderIdList.size(); i++) {
				query.setString(i, orderIdList.get(i));
			}
			List resources = query.list();
			session.close();
			if (resources != null && !resources.isEmpty() && StringEmptyUtils.isNotEmpty(resources.get(0))) {
				return (double) resources.get(0);
			}
			return 0;
		} catch (Exception re) {
			re.printStackTrace();
			return -1;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public Table getPaymentReportInfo(Map<String, Object> params) {
		Session session = null;
		try {
			List<Object> sqlParams = new ArrayList<>();
			String startDate = params.get("startDate") + "";
			String endDate = params.get("endDate") + "";
			String merchantId = params.get("merchantId") + "";
			StringBuilder sql = new StringBuilder(
					"SELECT	COUNT(t1.trade_no) AS count,SUM(t1.pay_amount) AS amount,DATE_FORMAT(t1.create_date, '%Y-%m-%d') AS date FROM ym_shop_manual_mpay t1 "
							+ "WHERE t1.networkStatus !=0  ");
			if (StringEmptyUtils.isNotEmpty(merchantId)) {
				sql.append(" AND t1.merchant_no = ? ");
				sqlParams.add(merchantId);
			}
			if (StringEmptyUtils.isNotEmpty(startDate)) {
				appendStartDate(sql);
				sqlParams.add(startDate);
			}
			if (StringEmptyUtils.isNotEmpty(endDate)) {
				appendEndDate(sql);
				sqlParams.add(endDate);
			}
			sql.append(" GROUP BY DATE_FORMAT(t1.create_date, '%Y-%m-%d') ");
			session = getSession();
			Table t = null;
			t = DataUtils.queryData(session.connection(), sql.toString(), sqlParams, null, null, null);
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

	/**
	 * 拼接查询条件中,通用的日期开始时间
	 * 
	 * @param sql
	 */
	private void appendStartDate(StringBuilder sql) {
		sql.append(" AND DATE_FORMAT(t1.create_date, '%Y-%m-%d') >= DATE_FORMAT( ? ,'%Y-%m-%d') ");
	}

	/**
	 * 拼接查询条件中,通用的日期结束时间
	 * 
	 * @param sql
	 */
	private void appendEndDate(StringBuilder sql) {
		sql.append(" AND DATE_FORMAT(t1.create_date, '%Y-%m-%d') <= DATE_FORMAT( ? ,'%Y-%m-%d') ");
	}

	@Override
	public Table getPaymentReportDetails(Map<String, Object> params) {
		Session session = null;
		try {
			List<Object> sqlParams = new ArrayList<>();
			StringBuilder sql = new StringBuilder(
					" SELECT t1.merchant_no,t1.create_by AS merchantName,DATE_FORMAT(t1.create_date, '%Y-%m-%d') AS date,COUNT(t1.trade_no) AS totalCount,SUM(t1.pay_amount) AS amount,	t2.platformFee,COUNT(case when t1.pay_amount < 100 then t1.pay_amount end) AS backCoverCount,SUM(case when t1.pay_amount >= 100 then t1.pay_amount  ELSE 0 end)  AS normalAmount,t1.customsCode FROM ym_shop_manual_mpay t1 "
							+ " LEFT JOIN ym_shop_merchant_fee_content t2 ON (t1.merchant_no = t2.merchantId AND t2.type = 'paymentRecord'  AND t1.customsCode = t2.customsCode ) WHERE t1.networkStatus != 0 AND t1.del_flag = 0 AND t1.pay_record_status != 1 ");
			String merchantId = params.get("merchantId") + "";
			if (StringEmptyUtils.isNotEmpty(merchantId)) {
				sql.append(" AND t1.merchant_no = ? ");
				sqlParams.add(merchantId);
			}
			String startDate = params.get("startDate") + "";
			if (StringEmptyUtils.isNotEmpty(startDate)) {
				appendStartDate(sql);
				sqlParams.add(startDate);
			}
			String endDate = params.get("endDate") + "";
			if (StringEmptyUtils.isNotEmpty(endDate)) {
				appendEndDate(sql);
				sqlParams.add(endDate);
			}
			sql.append(" GROUP BY DATE_FORMAT(t1.create_date, '%Y-%m-%d'), t1.create_by  ");
			session = getSession();
			Table t = DataUtils.queryData(session.connection(), sql.toString(), sqlParams, null, null, null);
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
	public List<ManualPaymentResendContent> getResendPaymentInfo(Class<ManualPaymentResendContent> entity,
			Map<String, Object> params, int page, int size) {
		Session session = null;
		String entName = entity.getSimpleName();
		try {
			session = getSession();
			String hql = " FROM " + entName + " model ";
			List<Object> list = new ArrayList<>();
			if (params != null && params.size() > 0) {
				hql += " WHERE ";
				String property;
				Iterator<String> is = params.keySet().iterator();
				while (is.hasNext()) {
					property = is.next();
					hql = hql + "model." + property + " = " + " ? " + " and ";
					list.add(params.get(property));
				}
				hql += " model.resendCount < 10 Order By id DESC";
			}
			Query query = session.createQuery(hql);
			if (!list.isEmpty()) {
				for (int i = 0; i < list.size(); i++) {
					query.setParameter(i, list.get(i));
				}
			}
			if (page > 0 && size > 0) {
				query.setFirstResult((page - 1) * size).setMaxResults(size);
			}
			List results = query.list();
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

	
}

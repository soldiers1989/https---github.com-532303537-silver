package org.silver.shop.dao.system.cross.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.cross.ReportsDao;
import org.silver.shop.model.system.log.SynthesisReportLog;
import org.silver.util.DateUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.stereotype.Repository;

import com.justep.baas.data.DataUtils;
import com.justep.baas.data.Table;

@Repository("reportsDao")
public class ReportsDaoImpl extends BaseDaoImpl implements ReportsDao {

	@Override
	public Table getIdCardDetails(Map<String, Object> params) {
		Session session = null;
		try {
			List<Object> sqlParams = new ArrayList<>();
			StringBuilder sql = new StringBuilder(
					"SELECT	DATE_FORMAT(t1.createDate, '%Y-%m-%d') AS date,	t1.merchantName,count(*) AS idCardTotalCount,t2.platformCost as idCardCost,count(t1.tollFlag = 1 OR NULL) AS idCardTollCount,count(t1.tollFlag = 2 OR NULL) AS idCardFreeCount FROM 	ym_shop_sys_id_card_certification_log t1 "
							+ " LEFT JOIN ym_shop_merchant_idcard_cost_content t2 ON t1.merchantId = t2.merchantId  WHERE  1=1 ");
			String merchantId = params.get("merchantId") + "";
			if (StringEmptyUtils.isNotEmpty(merchantId)) {
				sql.append(" AND t1.merchantId = ? ");
				sqlParams.add(merchantId);
			}
			String date = params.get("date") + "";
			if (StringEmptyUtils.isNotEmpty(date)) {
				sql.append(" AND DATE_FORMAT(t1.createDate, '%Y-%m-%d') = DATE_FORMAT( ? ,'%Y-%m-%d') ");
				sqlParams.add(date);
			}
			sql.append(
					" GROUP BY t1.merchantName,DATE_FORMAT(t1.createDate, '%Y-%m-%d')  ORDER BY DATE_FORMAT(t1.createDate, '%Y-%m-%d') DESC");
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
	public Table getIdCardCertificationDetails(Map<String, Object> params) {
		Session session = null;
		try {
			List<Object> sqlParams = new ArrayList<>();
			StringBuilder sql = new StringBuilder(
					"SELECT	DATE_FORMAT(t1.createDate, '%Y-%m-%d') AS date,	t1.merchantName,count(*) AS idCardTotalCount,t2.platformCost as idCardCost,count(t1.tollFlag = 1 OR NULL) AS tollFlag1,count(t1.tollFlag = 2 OR NULL) AS tollFlag2 FROM 	ym_shop_sys_id_card_certification_log t1 "
							+ " LEFT JOIN ym_shop_merchant_idcard_cost_content t2 ON t1.merchantId = t2.merchantId  WHERE  1=1 ");
			String merchantId = params.get("merchantId") + "";
			if (StringEmptyUtils.isNotEmpty(merchantId)) {
				sql.append(" AND t1.merchantId = ? ");
				sqlParams.add(merchantId);
			}
			String startDate = params.get("startDate") + "";
			String endDate = params.get("endDate") + "";
			if (StringEmptyUtils.isNotEmpty(startDate)) {
				sql.append(" AND DATE_FORMAT(t1.createDate, '%Y-%m-%d') >= DATE_FORMAT( ? ,'%Y-%m-%d') ");
				sqlParams.add(startDate);
			}
			if (StringEmptyUtils.isNotEmpty(endDate)) {
				sql.append(" AND DATE_FORMAT(t1.createDate, '%Y-%m-%d') <= DATE_FORMAT( ? ,'%Y-%m-%d') ");
				sqlParams.add(endDate);
			}
			sql.append(
					" GROUP BY t1.merchantName,DATE_FORMAT(t1.createDate, '%Y-%m-%d')  ORDER BY DATE_FORMAT(t1.createDate, '%Y-%m-%d') DESC");
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
	public Table getOrderReportDetail(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return null;
		}
		Session session = null;
		try {
			List<Object> sqlParams = new ArrayList<>();
			StringBuilder sql = new StringBuilder(
					" SELECT t1.merchant_no,t1.create_by AS merchantName,DATE_FORMAT(t1.create_date, '%Y-%m-%d') AS date,COUNT(t1.trade_no) AS totalCount,SUM(t1.pay_amount) AS amount,	t2.platformFee,COUNT(case when t1.pay_amount < 100 then t1.pay_amount end) AS backCoverCount,SUM(case when t1.pay_amount >= 100 then t1.pay_amount  ELSE 0 end)  AS normalAmount,t1.customsCode FROM ym_shop_manual_mpay t1 "
							+ " LEFT JOIN ym_shop_merchant_fee_content t2 ON (t1.merchant_no = t2.merchantId AND t2.type = 'paymentRecord'  AND t1.customsCode = t2.customsCode ) WHERE t1.networkStatus != 0 AND t1.del_flag = 0 AND t1.pay_record_status != 1 ");
			String merchantId = datasMap.get("merchantId") + "";
			if (StringEmptyUtils.isNotEmpty(merchantId)) {
				sql.append(" AND t1.merchantId = ? ");
				sqlParams.add(merchantId);
			}
			String startDate = datasMap.get("date") + "";
			if (StringEmptyUtils.isNotEmpty(startDate)) {
				sql.append(" AND DATE_FORMAT(t1.create_date, '%Y-%m-%d') = DATE_FORMAT( ? ,'%Y-%m-%d') ");
				sqlParams.add(startDate);
			}

			sql.append(" GROUP BY DATE_FORMAT(t1.create_date, '%Y-%m-%d'), t1.create_by");
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
	public List<SynthesisReportLog> findByMonth(String monthFirstDate, String strDayBefore, String merchantId) {
		Session session = null;
		try {
			List<Object> list = new ArrayList<>();
			StringBuilder sbSQL = new StringBuilder(" FROM SynthesisReportLog MODEL WHERE  ");

			if (StringEmptyUtils.isNotEmpty(monthFirstDate)) {
				sbSQL.append(" MODEL.date >= ? AND ");
				list.add(DateUtil.parseDate(monthFirstDate, "yyyy-MM-dd HH:mm:ss"));
			}
			if (StringEmptyUtils.isNotEmpty(strDayBefore)) {
				sbSQL.append(" MODEL.date <= ? AND ");
				list.add(DateUtil.parseDate(strDayBefore, "yyyy-MM-dd HH:mm:ss"));
			}
			if (StringEmptyUtils.isNotEmpty(merchantId)) {
				sbSQL.append(" MODEL.merchantId = ? AND ");
				list.add(merchantId);
			}
			sbSQL.append(" 1=1  ORDER BY createDate DESC ");
			session = getSession();
			Query query = session.createQuery(sbSQL.toString());
			// Query query = session.createSQLQuery(sbSQL.toString());
			for (int i = 0; i < list.size(); i++) {
				query.setParameter(i, list.get(i));
			}
			List resources = query.list();
			session.close();
			return resources;
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
	public Table getPaymentReportDetails(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return null;
		}
		Session session = null;
		try {
			List<Object> sqlParams = new ArrayList<>();
			StringBuilder sql = new StringBuilder(
					" SELECT t1.merchant_no,t1.create_by AS merchantName,DATE_FORMAT(t1.create_date, '%Y-%m-%d') AS date,COUNT(t1.trade_no) AS totalCount,SUM(t1.pay_amount) AS amount,COUNT(case when t1.pay_amount < 100 then t1.pay_amount end) AS backCoverCount,SUM(case when t1.pay_amount >= 100 then t1.pay_amount  ELSE 0 end)  AS normalAmount,t1.customsCode FROM ym_shop_manual_mpay t1 "
							+ "  WHERE t1.networkStatus != 0 AND t1.del_flag = 0 AND t1.pay_record_status != 1 ");
			String merchantId = datasMap.get("merchantId") + "";
			if (StringEmptyUtils.isNotEmpty(merchantId)) {
				sql.append(" AND t1.merchant_no = ? ");
				sqlParams.add(merchantId);
			}
			if (StringEmptyUtils.isNotEmpty(datasMap.get("startDate"))) {
				appendStartDate(sql);
				sqlParams.add(datasMap.get("startDate"));
			}
			if (StringEmptyUtils.isNotEmpty(datasMap.get("endDate"))) {
				appendEndDate(sql);
				sqlParams.add(datasMap.get("endDate"));
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
	public Table getDailyReportDetails(Map<String, Object> params, double fee, double backCoverFee) {
		if (params == null) {
			return null;
		}
		Session session = null;
		try {
			List<Object> sqlParams = new ArrayList<>();
			StringBuilder sql = new StringBuilder(
					" SELECT t1.merchant_no,t1.create_by AS merchantName,DATE_FORMAT(t1.create_date, '%Y-%m-%d') AS date,COUNT(t1.trade_no) AS totalCount,SUM(t1.pay_amount) AS amount,"
					+ "	COUNT( CASE WHEN (t1.pay_amount * "+fee+") < "+backCoverFee+" THEN 1 END) AS backCoverCount,"
					+ " SUM(CASE WHEN (t1.pay_amount *  "+fee+") >= "+backCoverFee+" THEN	t1.pay_amount	ELSE 0	END)  AS normalAmount,t1.customsCode FROM ym_shop_manual_mpay t1 "
					+ "  WHERE t1.networkStatus != 0 AND t1.del_flag = 0 AND t1.pay_record_status != 1 ");
			String merchantId = params.get("merchantId") + "";
			if (StringEmptyUtils.isNotEmpty(merchantId)) {
				sql.append(" AND t1.merchant_no = ? ");
				sqlParams.add(merchantId);
			}
			if (StringEmptyUtils.isNotEmpty(params.get("startDate"))) {
				appendStartDate(sql);
				sqlParams.add(params.get("startDate"));
			}
			if (StringEmptyUtils.isNotEmpty(params.get("endDate"))) {
				appendEndDate(sql);
				sqlParams.add(params.get("endDate"));
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
	public Table getReport(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return null;
		}
		Session session = null;
		try {
			List<Object> sqlParams = new ArrayList<>();
			StringBuilder sql = new StringBuilder(
					" SELECT * FROM ym_shop_sys_synthesis_report_log t1 WHERE  1=1 ");
			String merchantId = datasMap.get("merchantId") + "";
			if (StringEmptyUtils.isNotEmpty(merchantId)) {
				sql.append(" AND t1.merchant_no = ? ");
				sqlParams.add(merchantId);
			}
			if (StringEmptyUtils.isNotEmpty(datasMap.get("startDate"))) {
				sql.append(" AND t1.date >= ? ");
				sqlParams.add(datasMap.get("startDate"));
			}
			if (StringEmptyUtils.isNotEmpty(datasMap.get("endDate"))) {
				sql.append(" AND t1.date <= ? ");
				sqlParams.add(datasMap.get("endDate"));
			}
			sql.append(" ORDER BY t1.date ASC   ");
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
	public List<SynthesisReportLog> getReportInfo(Map<String, Object> params, int page, int size) {
		Session session = null;
		String entName = SynthesisReportLog.class.getSimpleName();
		try {
			session = getSession();
			StringBuilder sbHql = new StringBuilder(" FROM " + entName + " MODEL WHERE 1=1 ");
			List<Object> sqlParams = new ArrayList<>();
			if(params !=null && !params.isEmpty()){
				String merchantId = params.get("merchantId") + "";
				if (StringEmptyUtils.isNotEmpty(merchantId)) {
					sbHql.append(" AND MODEL.merchantId = ? ");
					sqlParams.add(merchantId);
				}
				if (StringEmptyUtils.isNotEmpty(params.get("startDate"))) {
					sbHql.append(" AND MODEL.date >= ? ");
					sqlParams.add(params.get("startDate"));
				}
				if (StringEmptyUtils.isNotEmpty(params.get("endDate"))) {
					sbHql.append(" AND MODEL.date <= ? ");
					sqlParams.add(params.get("endDate"));
				}
			}
			sbHql.append(" ORDER BY MODEL.date ASC ");
			Query query = session.createQuery(sbHql.toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				query.setParameter(i, sqlParams.get(i));
			}
			if (page > 0 && size > 0) {
				query.setFirstResult((page - 1) * size).setMaxResults(size);
			}
			List<SynthesisReportLog> results = query.list();
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

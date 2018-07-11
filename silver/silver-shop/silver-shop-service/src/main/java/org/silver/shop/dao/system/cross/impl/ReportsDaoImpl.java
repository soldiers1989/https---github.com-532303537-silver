package org.silver.shop.dao.system.cross.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.cross.ReportsDao;
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
					"SELECT	DATE_FORMAT(t1.createDate, '%Y-%m-%d') AS date,	t1.merchantName,count(*) AS idCardTotalCount,t2.platformCost as idCardCost,count(t1.tollFlag = 1 OR NULL) AS tollFlag1,count(t1.tollFlag = 2 OR NULL) AS tollFlag2 FROM 	ym_shop_sys_id_card_certification_log t1 "
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
			sql.append(" GROUP BY t1.merchantName,DATE_FORMAT(t1.createDate, '%Y-%m-%d')  ORDER BY DATE_FORMAT(t1.createDate, '%Y-%m-%d') DESC");
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
			sql.append(" GROUP BY t1.merchantName,DATE_FORMAT(t1.createDate, '%Y-%m-%d')  ORDER BY DATE_FORMAT(t1.createDate, '%Y-%m-%d') DESC");
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
}

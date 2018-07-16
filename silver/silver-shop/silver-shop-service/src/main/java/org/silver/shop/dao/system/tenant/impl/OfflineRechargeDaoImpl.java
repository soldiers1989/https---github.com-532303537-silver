package org.silver.shop.dao.system.tenant.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.tenant.OfflineRechargeDao;
import org.silver.util.StringEmptyUtils;
import org.springframework.stereotype.Repository;

import com.justep.baas.data.DataUtils;
import com.justep.baas.data.Table;

@Repository
public class OfflineRechargeDaoImpl extends BaseDaoImpl implements OfflineRechargeDao{

	@Override
	public Table getApplication(Map<String, Object> params, int page, int size) {
		Session session = null;
		try {
			List<Object> sqlParams = new ArrayList<>();
			StringBuilder sql = new StringBuilder(
					"SELECT t1.offlineRechargeId,t1.applicant,t1.remittanceAccount,t1.remittanceName,DATE_FORMAT(t1.remittanceDate,'%Y-%m-%d'),t1.remittanceAmount,t2.currentNodeName,t2.reviewerFlag FROM ym_shop_sys_offline_recharge_content t1 "
					+ " LEFT JOIN ym_shop_sys_offline_recharge_log t2 ON t1.offlineRechargeId = t2.offlineRechargeId where 1 = 1 ");
			session = getSession();
			//
			String reviewerType = params.get("reviewerType")+"";
			if(StringEmptyUtils.isNotEmpty(reviewerType)){
				sql.append(" t1.reviewerType = ? ");
				sqlParams.add(reviewerType);
			}
			String merchantId = params.get("merchantId")+"";
			if(StringEmptyUtils.isNotEmpty(merchantId)){
				sql.append(" t1.applicantId = ? ");
				sqlParams.add(merchantId);
			}
			Table t = null;
			if (page > 0 && size > 0) {
				page = page - 1;
				t = DataUtils.queryData(session.connection(), sql.toString(), sqlParams, null, page * size, size);
			} else {
				t = DataUtils.queryData(session.connection(), sql.toString(), sqlParams, null, null, null);
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

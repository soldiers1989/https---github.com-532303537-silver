package org.silver.shop.dao.system.tenant.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.tenant.MerchantWalletDao;
import org.silver.util.StringEmptyUtils;
import org.springframework.stereotype.Repository;

import com.justep.baas.data.DataUtils;
import com.justep.baas.data.Table;

@Repository("merchantWalletDao")
public class MerchantWalletDaoImpl extends BaseDaoImpl implements MerchantWalletDao {

	@Override
	public Table getApplication(Map params, int page, int size) {
		Session session = null;
		try {
			List<Object> sqlParams = new ArrayList<>();
			StringBuilder sbSql = new StringBuilder(
					"SELECT t1.offlineRechargeId,t1.applicant,t1.remittanceAccount,t1.remittanceName,DATE_FORMAT(t1.remittanceDate,'%Y-%m-%d') as remittanceDate ,t1.remittanceAmount,t2.currentNodeName,t2.reviewerFlag,DATE_FORMAT(t1.createDate, '%Y-%m-%d %T') AS date FROM ym_shop_sys_offline_recharge_content t1 "
					+ " LEFT JOIN ym_shop_sys_offline_recharge_log t2 ON t1.offlineRechargeId = t2.offlineRechargeId ");
			session = getSession();
			sbSql.append(" WHERE ");
			//
			String reviewerType = params.get("reviewerType")+"";
			if(StringEmptyUtils.isNotEmpty(reviewerType)){
				sbSql.append(" t1.reviewerType = ? AND ");
				sqlParams.add(reviewerType);
			}
			String merchantId = params.get("merchantId")+"";
			if(StringEmptyUtils.isNotEmpty(merchantId)){
				sbSql.append(" t1.applicantId = ? AND ");
				sqlParams.add(merchantId);
			}
			String reviewerFlag = params.get("reviewerFlag")+"";
			if(StringEmptyUtils.isNotEmpty(reviewerFlag)){
				sbSql.append(" t2.reviewerFlag = ? AND ");
				sqlParams.add(merchantId);
			}
			String startDate = params.get("startDate")+"";
			String endDate = params.get("endDate")+"";
			if(StringEmptyUtils.isNotEmpty(startDate)){
				sbSql.append(" t1.createDate " + " >= " + " ? " + " AND ");
			} 
			if(StringEmptyUtils.isNotEmpty(endDate)){
				sbSql.append(" t1.createDate " + " <= " + " ? " + " AND ");
			}
			sbSql.append(" 1=1  ORDER BY t1.createDate DESC ");
			Table t = null;
			if (page > 0 && size > 0) {
				page = page - 1;
				t = DataUtils.queryData(session.connection(), sbSql.toString(), sqlParams, null, page * size, size);
			} else {
				t = DataUtils.queryData(session.connection(), sbSql.toString(), sqlParams, null, null, null);
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

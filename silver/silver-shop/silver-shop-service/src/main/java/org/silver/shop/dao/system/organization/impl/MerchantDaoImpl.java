package org.silver.shop.dao.system.organization.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.organization.MerchantDao;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.util.StringEmptyUtils;
import org.springframework.stereotype.Repository;

import com.justep.baas.data.DataUtils;
import com.justep.baas.data.Table;

@Repository("merchantDao")
public class MerchantDaoImpl extends BaseDaoImpl implements MerchantDao {

	@Override
	public long findLastId() {
		return this.findLastId(Merchant.class);
	}

	@Override
	public Table getRelatedMemberFunds(String merchantId, String memberId, int page, int size) {
		Session session = null;
		try {
			String sql = "SELECT  m.*,t3.memberIdCard,t3.memberIdCardName FROM (SELECT t2.memberId,t2.memberName,t2.reserveAmount FROM 	ym_shop_merchant_related_member_content t1 LEFT JOIN ym_shop_member_wallet_content t2 ON t1.memberId = t2.memberId WHERE t1.merchantId = ? )m  "
					+ "LEFT JOIN ym_shop_member t3 on m.memberId = t3.memberId";
			List<Object> sqlParams = new ArrayList<>();
			sqlParams.add(merchantId);
			if (StringEmptyUtils.isNotEmpty(memberId)) {
				sql += "AND t3.memberIdCardName = ? ";
				sqlParams.add(memberId);
			}
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
}

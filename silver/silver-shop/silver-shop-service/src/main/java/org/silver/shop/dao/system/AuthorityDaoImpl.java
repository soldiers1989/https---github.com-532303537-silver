package org.silver.shop.dao.system;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.silver.shop.dao.BaseDaoImpl;
import org.springframework.stereotype.Repository;

import com.justep.baas.data.DataUtils;
import com.justep.baas.data.Table;

@Repository("authorityDao")
public class AuthorityDaoImpl extends BaseDaoImpl implements AuthorityDao {

	@Override
	public Table getAuthorityGroupInfo(String userId, String groupName) {
		Session session = null;
		try {
			String queryString = " SELECT m.* ,t3.checkFlag ,t3.userId FROM (SELECT t1.firstName,t1.secondName,t1.thirdName,t2.status,t2.authorityId  FROM ym_shop_sys_authority t1 INNER JOIN ym_shop_sys_authority_group t2 ON (t1.id = t2.authorityId AND t2.status = 1 AND t2.groupName = ?) ) m "
					+ " LEFT JOIN ym_shop_sys_authority_user t3 ON t3.authorityId = m.authorityId WHERE t3.userId = ? OR t3.userId IS NULL ";
			List<Object> sqlParams = new ArrayList<>();
			sqlParams.add(groupName);
			sqlParams.add(userId);
			session = getSession();
			java.sql.Connection conn = session.connection();
			Table t = DataUtils.queryData(conn, queryString, sqlParams, null, null, null);
			session.close();
			// Transform.tableToJson(l);
			return t;
		} catch (RuntimeException re) {
			re.printStackTrace();
			return null;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

}

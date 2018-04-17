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
	public Table getAuthorityGroupInfo(String groupName) {
		Session session = null;
		try {
			String queryString = "SELECT t1.firstName,t1.firstCode,t1.secondCode,t1.secondName,t1.thirdCode,t1.thirdName,t2.id AS groupId,t1.groupName,t2.status  FROM ym_shop_sys_authority t1 "
					+ " RIGHT JOIN ym_shop_sys_authority_group t2 ON t1.id = t2.authorityId WHERE t2.groupName = ?";
			List<Object> sqlParams = new ArrayList<>();
			sqlParams.add(groupName);
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

package org.silver.shop.dao.system;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
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
			String queryString = " SELECT m.* ,t3.checkFlag ,t3.userId FROM (SELECT t1.firstName,t1.firstCode,t1.secondName,t1.secondCode,t1.thirdName,t1.thirdCode,t2.status,t2.authorityId  FROM ym_shop_sys_authority t1 INNER JOIN ym_shop_sys_authority_group t2 ON (t1.authorityId = t2.authorityId AND t2.status = 1 AND t2.groupName = ?) ) m "
					+ " LEFT JOIN ym_shop_sys_authority_user t3 ON (t3.authorityId = m.authorityId AND t3.userId = ?  )";
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

	@Override
	public Table getAuthorityGroupInfo(String groupName) {
		Session session = null;
		try {
			String queryString = "SELECT t1.authorityId ,t1.firstName,t1.firstCode,t1.secondName,t1.secondCode,t1.thirdName,t1.thirdCode FROM ym_shop_sys_authority t1"
					+ "LEFT JOIN ym_shop_sys_authority_group t2 ON (t2.authorityId = t1.authorityId AND t2.groupName ='manager')";
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

	@Override
	public boolean updateAuthorityCheckFlag(String roleId) {
		Session session = null;
		try {
			session = getSession();
			session.beginTransaction();
			Query query = session.createQuery("UPDATE AuthorityUser t1 SET t1.checkFlag = 'false' WHERE t1.userId = ?");
			query.setParameter(0, roleId);
			query.executeUpdate();
			session.getTransaction().commit();
			return true;
		} catch (RuntimeException re) {
			re.printStackTrace();
			return false;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

}

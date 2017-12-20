package org.silver.shop.dao.common.base.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.common.base.ProvinceCityAreaDao;
import org.springframework.stereotype.Repository;

import com.justep.baas.data.DataUtils;
import com.justep.baas.data.Table;

/**
 * 
 */
@Repository("provinceCityAreaDao")
public class ProvinceCityAreaDaoImpl extends BaseDaoImpl implements ProvinceCityAreaDao {

	@Override
	public Table findAllProvinceCityArea() {
		Connection c = null;
		Session session = null;
		try {
			String sql = "SELECT m.*,t3.provinceName,t3.provinceCode  FROM(SELECT t1.areaCode,t1.areaName,t2.cityCode,t2.cityName,t2.provinceCode as Pcode FROM ym_shop_base_area t1 LEFT JOIN ym_shop_base_city t2 ON (t1.cityCode = t2.cityCode)) m "
					+ " RIGHT JOIN ym_shop_base_province t3 on(m.Pcode=t3.provinceCode) ";
			session = getSession();
			// ConnectionProvider cp = ((SessionFactoryImplementor)
			// session.getSessionFactory()).getConnectionProvider();
			// c = cp.getConnection();
			Table t = DataUtils.queryData(session.connection(), sql, null, null, null, null);
			session.connection().close();
			session.close();
			return t;
		} catch (Exception re) {
			re.printStackTrace();
			// log.error("查询数据出错！", re);
			return null;
		} finally {
			if (session != null && session.isOpen()) {
				if (c != null) {
					try {
						c.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				session.close();
			}
		}
	}

	@Override
	public Table findAllProvinceCityArePostal() {
		Connection c = null;
		Session session = null;
		try {
			String sql = "SELECT n.* ,t4.postalCode FROM (SELECT m.*, t3.areaCode,t3.areaName FROM ( SELECT t1.provinceCode,t1.provinceName,t2.cityCode,t2.cityName "
					+ " FROM ym_shop_base_province t1 LEFT JOIN ym_shop_base_city t2 ON (t1.provinceCode = t2.provinceCode) ) m RIGHT JOIN ym_shop_base_area t3 ON (m.cityCode = t3.cityCode)) n "
					+ " LEFT JOIN ym_shop_base_postal t4 ON (n.areaCode = t4.areaCode)";
			session = getSession();

			Table t = DataUtils.queryData(session.connection(), sql, null, null, null, null);
			session.connection().close();
			session.close();
			return t;
		} catch (Exception re) {
			re.printStackTrace();
			// log.error("查询数据出错！", re);
			return null;
		} finally {
			if (session != null && session.isOpen()) {
				if (c != null) {
					try {
						c.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				session.close();
			}
		}
	}
}

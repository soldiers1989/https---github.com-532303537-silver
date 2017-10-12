package org.silver.shop.dao.common.base.impl;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;
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
		Session session = null;
		try {
			String sql = "SELECT m.*,t3.provinceName,t3.provinceCode  FROM(SELECT t1.areaCode,t1.areaName,t2.cityCode,t2.cityName,t2.provinceCode as Pcode FROM ym_shop_base_area t1 LEFT JOIN ym_shop_base_city t2 ON (t1.cityCode = t2.cityCode)) m "
					+ " RIGHT JOIN ym_shop_base_province t3 on(m.Pcode=t3.provinceCode) ";
			session = getSession();
			ConnectionProvider cp = ((SessionFactoryImplementor) session.getSessionFactory()).getConnectionProvider();
			Connection c = cp.getConnection();
			Table t = DataUtils.queryData(c, sql, null, null, null, null);
			c.close();
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
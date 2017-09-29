package org.silver.shop.dao.common.base.impl;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.common.base.EPortDao;
import org.silver.shop.model.common.base.EPort;
import org.springframework.stereotype.Repository;

import com.justep.baas.data.DataUtils;
import com.justep.baas.data.Table;

@Repository("ePortDao")
public class EPortDaoImpl extends BaseDaoImpl implements EPortDao {

	@Override
	public List<Object> findByProperty(Class entity, Map params, int page, int size) {
		return super.findByProperty(entity, params, page, size);
	}

	@Override
	public boolean add(Object entity) {
		return super.add(entity);
	}

	@Override
	public Table findProvinceCityEport() {
		Session session = null;
		try {
			String sql = "SELECT m.*,t1.provinceName from "
					+ "(SELECT t1.customsPort,t1.customsPortName,t1.cityCode,t2.cityName,t2.provinceCode from ym_shop_sys_eport t1  LEFT JOIN ym_shop_base_city t2 on t1.cityCode = t2.cityCode) m"
					+ " left JOIN ym_shop_base_province t1 ON m.provinceCode = t1.provinceCode";
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

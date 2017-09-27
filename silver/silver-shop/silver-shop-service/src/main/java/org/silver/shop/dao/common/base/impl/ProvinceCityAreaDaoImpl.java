package org.silver.shop.dao.common.base.impl;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.common.base.ProvinceCityAreaDao;
import org.springframework.stereotype.Repository;

/**
 * 
 */
@Repository("provinceCityAreaDao")
public class ProvinceCityAreaDaoImpl extends BaseDaoImpl implements ProvinceCityAreaDao {

	@Override
	public List<Object> findAllCountry() {
		Session session = null;
		try {
			String sql = "SELECT m.*,t3.provinceName FROM(SELECT t1.areaCode,t1.areaName,t2.cityCode,t2.cityName,t2.provinceCode FROM ym_shop_base_area t1 LEFT JOIN ym_shop_base_city t2 ON (t1.cityCode = t2.cityCode)) m "
					+ " LEFT JOIN ym_shop_base_province t3 on(m.provinceCode=t3.provinceCode) ";
			session = getSession();
			Query query = session.createSQLQuery(sql);

			List<Object> list = query.list();
			session.close();
			return list;
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

package org.silver.shop.dao.system.manual.impl;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.manual.MorderDao;
import org.silver.util.DateUtil;
import org.springframework.stereotype.Repository;

import com.justep.baas.data.DataUtils;
import com.justep.baas.data.Table;

@Repository("morderDao")
public class MorderDaoImpl<T> extends BaseDaoImpl<T>  implements MorderDao {

	@Override
	public Table getOrderAndOrderGoodsInfo(String merchantId,String date,int serialNo) {
		Connection c = null;
		Session session = null;
		try {
			String sql = "SELECT * FROM ym_shop_manual_morder t1 LEFT JOIN ym_shop_manual_morder_sub t2 ON t1.order_id =t2.order_id "
					+ "WHERE t1.create_date >= ?  AND t1.serial = ? AND t1.merchant_no = ?";
			//t1.create_date >= ?  AND
			session = getSession();
			List<Object> sqlParams = new ArrayList<>();
			sqlParams.add(date+" 00:00:00");
			sqlParams.add(serialNo);
			sqlParams.add(merchantId);
			Table t = DataUtils.queryData(session.connection(), sql, sqlParams, null, null, null);
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

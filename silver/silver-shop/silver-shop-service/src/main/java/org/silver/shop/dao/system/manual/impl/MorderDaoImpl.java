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
public class MorderDaoImpl<T> extends BaseDaoImpl<T> implements MorderDao {

	@Override
	public Table getOrderAndOrderGoodsInfo(String merchantId, String date, int serialNo) {
		Connection c = null;
		Session session = null;
		try {
			String sql = "SELECT * FROM ym_shop_manual_morder t1 LEFT JOIN ym_shop_manual_morder_sub t2 ON t1.order_id =t2.order_id "
					+ "WHERE t1.create_date >= ?  AND t1.serial = ? AND t1.merchant_no = ? ORDER BY t2.seqNo";
			// t1.create_date >= ? AND
			session = getSession();
			List<Object> sqlParams = new ArrayList<>();
			sqlParams.add(date + " 00:00:00");
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

	@Override
	public Table getMOrderAndMGoodsInfo(String merchantId, String startDate, String endDate, int page, int size) {
		Session session = null;
		try {
			String sql = "SELECT * from ym_shop_manual_morder t1 LEFT JOIN ym_shop_manual_morder_sub t2 ON (t1.order_id = t2.order_id AND t2.deleteFlag = 0) "
					+ " WHERE  t1.order_record_status = 3 AND t1.del_flag = 0  AND t1.merchant_no = ?  and t1.create_date >= ? AND t1.create_date <= ? "
					+ " GROUP BY t2.EntGoodsNo";
			List<Object> sqlParams = new ArrayList<>();
			sqlParams.add(merchantId);
			sqlParams.add(startDate + " 00:00:00");
			sqlParams.add(endDate + " 23:59:59");
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

	@Override
	public long getMOrderAndMGoodsInfoCount(String merchantId, String startDate, String endDate, int page, int size) {
		Session session = null;
		try {
			String sql = "SELECT	COUNT(m.EntGoodsNo) AS count FROM 	"
					+ "( SELECT t2.order_id,t2.EntGoodsNo	FROM ym_shop_manual_morder t1	LEFT JOIN ym_shop_manual_morder_sub t2 ON (	t1.order_id = t2.order_id AND t2.deleteFlag = 0	 )	"
					+ " WHERE   t1.del_flag = 0 AND t1.order_record_status = 3  AND t1.merchant_no = ?	AND t1.create_date >= ?		AND t1.create_date <= ?	GROUP BY	t2.EntGoodsNo	"
					+ ") m LEFT JOIN ym_shop_goods_record_detail t3 ON m.EntGoodsNo = t3.entGoodsNo "
					+ "WHERE 	t3.entGoodsNo IS NULL";
			List<Object> sqlParams = new ArrayList<>();
			sqlParams.add(merchantId);
			sqlParams.add(startDate + " 00:00:00");
			sqlParams.add(endDate + " 23:59:59");
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
			// 将链表查询出的数据进行转换
			return Long.parseLong(t.getRows().get(0).getValue("count") + "");
		} catch (Exception re) {
			re.printStackTrace();
			// log.error("查询数据出错！", re);
			return -1;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}
}

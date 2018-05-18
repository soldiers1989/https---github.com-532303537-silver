package org.silver.shop.dao.system.manual.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.manual.MorderDao;
import org.silver.shop.model.system.commerce.OrderRecordContent;
import org.silver.shop.model.system.manual.Morder;
import org.silver.util.DateUtil;
import org.silver.util.StringEmptyUtils;
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
			String sql = "SELECT t1.order_id,t1.trade_no,t1.merchant_no,t1.Fcode,t1.FCY,t1.Tax,t1.ActualAmountPaid,t1.RecipientName,t1.RecipientAddr,t1.RecipientID,t1.RecipientTel,t1.RecipientProvincesCode,t1.OrderDocAcount,t1.OrderDocAcount,t1.OrderDocName,t1.OrderDocType,t1.OrderDocId,t1.OrderDocTel,t1.OrderDate,t1.waybill,t1.senderName,t1.senderCountry,t1.senderAreaCode,t1.senderAddress,t1.senderTel,t1.RecipientCityCode,t1.RecipientAreaCode,t1.postal,t1.RecipientProvincesName,t1.RecipientCityName,t1.RecipientAreaName,t1.customsCode,t2.EntGoodsNo,t2.CIQGoodsNo,t2.CusGoodsNo,t2.HSCode,t2.GoodsName,t2.GoodsStyle,t2.OriginCountry,t2.BarCode,t2.Brand,t2.Qty,t2.Unit,t2.Price,t2.Total,t2.netWt,t2.grossWt,t2.firstLegalCount,t2.secondLegalCount,t2.stdUnit,t2.numOfPackages,t2.packageType,t2.transportModel,t2.seqNo "
					+ " FROM ym_shop_manual_morder t1 LEFT JOIN ym_shop_manual_morder_sub t2 ON t1.order_id =t2.order_id "
					+ " WHERE DATE_FORMAT(t1.create_date ,'%Y-%m-%d') = DATE_FORMAT(? ,'%Y-%m-%d') AND t1.serial = ?  AND t1.merchant_no = ? ORDER BY t2.seqNo";
			//
			session = getSession();
			List<Object> sqlParams = new ArrayList<>();
			sqlParams.add(date);
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

	@Override
	public double statisticalManualOrderAmount(List<Object> itemList) {
		// 当没有订单Id集合则直接返回0
		if (itemList == null || itemList.isEmpty()) {
			return 0;
		}
		Session session = null;
		try {
			StringBuilder sbSQL = new StringBuilder(
					" SELECT SUM(t1.ActualAmountPaid) AS ActualAmountPaid FROM ym_shop_manual_morder t1 WHERE t1.status = 0 AND t1.order_id IN ( ");
			for (int i = 0; i < itemList.size(); i++) {
				sbSQL.append(" ? , ");
			}
			// 删除结尾的逗号
			sbSQL.deleteCharAt(sbSQL.length() - 2);
			sbSQL.append(" ) ");
			session = getSession();
			Query query = session.createSQLQuery(sbSQL.toString());
			for (int i = 0; i < itemList.size(); i++) {
				Map<String, Object> orderMap = (Map<String, Object>) itemList.get(i);
				query.setString(i, orderMap.get("orderNo") + "");
			}
			List resources = query.list();
			session.close();
			if (resources != null && !resources.isEmpty() && StringEmptyUtils.isNotEmpty(resources.get(0))) {
				return (double) resources.get(0);
			}
			return 0;
		} catch (Exception re) {
			re.printStackTrace();
			return -1;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public Table getIdCardCount(String orderDocId, String startDate, String endDate) {
		Session session = null;
		try {
			String sql = "SELECT DocName,idCard,count FROM (SELECT	t1.OrderDocName AS DocName,	t1.OrderDocId AS idCard,COUNT(t1.OrderDocId) AS count	FROM ym_shop_manual_morder t1 WHERE	t1.create_date >= ? AND t1.create_date <= ?	AND t1.OrderDocId = ? GROUP BY t1.OrderDocId ) m "
					+ " WHERE count >= 3 ORDER BY count DESC";
			List<Object> sqlParams = new ArrayList<>();
			sqlParams.add(startDate + " 00:00:00");
			sqlParams.add(endDate + " 23:59:59");
			sqlParams.add(orderDocId);
			session = getSession();
			Table t = null;
			t = DataUtils.queryData(session.connection(), sql, sqlParams, null, null, null);
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
	public Table getPhoneCount(String recipientTel, String startDate, String endDate) {
		Session session = null;
		try {
			String sql = "SELECT DocName,phone,count FROM ( SELECT t1.OrderDocName AS DocName,t1.RecipientTel AS phone,	COUNT(t1.RecipientTel) AS count FROM ym_shop_manual_morder t1 WHERE t1.create_date >= ? AND t1.create_date <= ? AND t1.RecipientTel = ?	GROUP BY  t1.RecipientTel ) m "
					+ " WHERE count >= 3 ORDER BY 	count DESC";
			List<Object> sqlParams = new ArrayList<>();
			sqlParams.add(startDate + " 00:00:00");
			sqlParams.add(endDate + " 23:59:59");
			sqlParams.add(recipientTel);
			session = getSession();
			Table t = null;
			t = DataUtils.queryData(session.connection(), sql, sqlParams, null, null, null);
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
	public List<Morder> findByPropertyIn(List<Map<String,Object>> itemList) {
		Session session = null;
		try {
			StringBuilder sbSQL = new StringBuilder(" SELECT * FROM ym_shop_manual_morder t1 WHERE  t1.order_id IN ( ");
			for (int i = 0; i < itemList.size(); i++) {
				sbSQL.append(" ? , ");
			}
			// 删除结尾的逗号
			sbSQL.deleteCharAt(sbSQL.length() - 2);
			sbSQL.append(" ) ");
			session = getSession();
			Query query = session.createSQLQuery(sbSQL.toString());
			for (int i = 0; i < itemList.size(); i++) {
				Map<String, Object> orderMap =  itemList.get(i);
				query.setString(i, orderMap.get("orderNo") + "");
			}
			// session.close();
			return setEntityInfo(query.list());
		} catch (Exception re) {
			re.printStackTrace();
			return null;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	/**
	 * 遍历查询的结果集,将所有结果集set到对应的实体中
	 * 
	 * @param cources
	 * @return List
	 */
	private List<Morder> setEntityInfo(List<Morder> cources) {
		Iterator i = cources.iterator();
		List<Morder> orderList = new ArrayList<>();
		Morder order = null;
		while (i.hasNext()) {
			order = new Morder();
			Object[] obj = (Object[]) i.next();
			for (int j = 0; j < obj.length; j++) {
				order.setOrder_id(obj[1].toString());
				order.setFCY(Double.parseDouble(obj[5].toString()));
				order.setActualAmountPaid(Double.parseDouble(obj[7].toString()));
				order.setRecipientName(obj[8].toString());
				order.setRecipientID(obj[10].toString());
				order.setRecipientTel(obj[11].toString());
				order.setRecipientProvincesCode(obj[12].toString());
				order.setOrderDocName(obj[14].toString());
				order.setOrderDocId(obj[16].toString());
				order.setOrderDocTel(obj[17].toString());
				order.setStatus(Integer.parseInt(obj[21].toString()));
				order.setOrder_record_status(Integer.parseInt(obj[34].toString()));
			}
			orderList.add(order);
		}
		return orderList;
	}

}

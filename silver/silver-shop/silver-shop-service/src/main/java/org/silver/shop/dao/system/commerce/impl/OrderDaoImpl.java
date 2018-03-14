package org.silver.shop.dao.system.commerce.impl;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.commerce.OrderDao;
import org.silver.shop.model.system.commerce.OrderRecordContent;
import org.silver.util.DateUtil;
import org.silver.util.StringEmptyUtils;
import org.springframework.stereotype.Repository;

import com.github.pagehelper.StringUtil;
import com.justep.baas.data.DataUtils;
import com.justep.baas.data.Table;

@Repository("orderDao")
public class OrderDaoImpl extends BaseDaoImpl implements OrderDao {

	@Override
	public Table getMerchantOrderInfo(String merchantId, int page, int size) {
		Session session = null;
		try {
			String sql = "SELECT t1.*, t2.orderId,t2.entOrderNo,t2.freight,t2.consolidatedTax,t2.orderTotalPrice,t2.recipientName,t2.recipientCardId,t2.recipientTel,t2.recipientAddr,t2.recipientCountryCode,t2.recProvincesCode,t2.recCityCode, t2.recAreaCode "
					+ "FROM ym_shop_order_goods_content	 t1 LEFT JOIN ym_shop_order_content t2 ON t1.orderId = t2.orderId "
					+ "and t1.merchantId = ?";
			List<Object> sqlParams = new ArrayList<>();
			sqlParams.add(merchantId);
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
	public List<Object> searchTimOutOrder(Class entity, Map params, String time) {
		Session session = null;
		String entName = entity.getSimpleName();
		try {
			session = getSession();
			StringBuilder hql = new StringBuilder();
			hql.append("from " + entName + " model ");
			List<Object> list = new ArrayList<>();
			if (params != null && params.size() > 0) {
				hql.append(" where ");
				String property;
				Iterator<String> is = params.keySet().iterator();
				while (is.hasNext()) {
					property = is.next();
					hql.append("model." + property + " = " + " ? " + " and ");
					list.add(params.get(property));
				}
			}
			if (StringUtil.isNotEmpty(time) && !"null".equals(time)) {
				hql.append("model.createDate" + " <= '" + time + "' and ");
			}
			hql.append(" 1=1 ");
			Query query = session.createQuery(hql.toString());
			if (!list.isEmpty()) {
				for (int i = 0; i < list.size(); i++) {
					query.setParameter(i, list.get(i));
				}
			}
			List<Object> results = query.list();
			session.close();
			return results;
		} catch (Exception re) {
			re.printStackTrace();
			return null;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public Table getOrderDailyReport(Class class1, Map paramsMap, int page, int size) {
		Session session = null;
		try {
			List<Object> sqlParams = new ArrayList<>();
			String startDate = paramsMap.get("startDate") + "";
			String endDate = paramsMap.get("endDate") + "";
			String sql = "SELECT count(t1.order_id) AS orderCount, sum(t1.FCY) as total,(sum(t1.FCY) * 0.001)  AS price,DATE_FORMAT(t1.create_date, '%Y-%m-%d') AS date FROM ym_shop_manual_morder t1 "
					+ "WHERE (t1.order_record_status = '3' OR t1.order_record_status = '2')  AND t1.del_flag = '0' ";
			String merchantId = paramsMap.get("merchantId") + "";
			if (StringEmptyUtils.isNotEmpty(merchantId)) {
				sql += " AND t1.merchant_no = ? ";
				sqlParams.add(merchantId);
			}
			String merchantName = paramsMap.get("merchantName") + "";
			if (StringEmptyUtils.isNotEmpty(merchantName)) {
				sql += " AND t1.create_by = ? ";
				sqlParams.add(merchantName);
			}
			if (StringEmptyUtils.isNotEmpty(startDate)) {
				sql += " AND DATE_FORMAT(t1.create_date, '%Y-%m-%d') >= DATE_FORMAT( ? ,'%Y-%m-%d') ";
				sqlParams.add(startDate);
			}
			if (StringEmptyUtils.isNotEmpty(endDate)) {
				sql += " AND DATE_FORMAT(t1.create_date, '%Y-%m-%d') <= DATE_FORMAT( ? ,'%Y-%m-%d') ";
				sqlParams.add(endDate);
			}
			sql += " GROUP BY DATE_FORMAT(t1.create_date, '%Y-%m-%d') ";
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
			return null;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public boolean managerDeleteTestOrder() {
		Session session = null;
		try {
			session = getSession();
			Transaction tra = session.beginTransaction();
			String sql = "DELETE t1,t2 FROM ym_shop_manual_morder t1 LEFT JOIN ym_shop_manual_morder_sub t2 ON t1.merchant_no =t2.merchant_no "
					+ "WHERE t1.merchant_no = 'MerchantId_00047' AND t1.create_by = '测试账号'";
			Query q = session.createQuery(sql);
			q.executeUpdate();
			session.flush();
			session.close();
			tra.commit();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public List<OrderRecordContent> merchantuUnionOrderInfo(Class entity, Map<String, Object> params,
			Map<String, Object> viceParams, int page, int size) {
		Session session = null;
		try {
			session = getSession();
			StringBuilder sbSQL = new StringBuilder();
			List<Object> list = new ArrayList<>();
			//
			unionShopOrderInfo(sbSQL, params, list);
			// 并集
			sbSQL.append(" UNION  ");
			//
			unionMorderInfo(sbSQL, viceParams, list, page, size);
			Query query = session.createSQLQuery(sbSQL.toString());
			if (!list.isEmpty()) {
				for (int i = 0; i < list.size(); i++) {
					query.setParameter(i, list.get(i));
				}
			}
			List<OrderRecordContent> cources = query.list();
			session.close();
			return setEntityInfo(cources);
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
	 * 并集查询手工订单与商城订单信息,拼接前半部分商城自用订单信息表
	 * 
	 * @param sbSQL
	 *            sql字符串语句
	 * @param params
	 *            参数
	 * @param list
	 */
	private void unionShopOrderInfo(StringBuilder sbSQL, Map<String, Object> params, List<Object> list) {
		sbSQL.append(
				" SELECT entOrderNo, entPayNo,orderGoodTotal,tax,actualAmountPaid,recipientName,recipientAddr ,recipientTel,orderDocAcount,orderDocName,orderDocId,orderDocTel,createDate,merchantId,merchantName,deleteFlag from ym_shop_order_record_content ");
		if (params != null && params.size() > 0) {
			sbSQL.append(" WHERE ");
			String property;
			Iterator<String> is = params.keySet().iterator();
			while (is.hasNext()) {
				property = is.next();
				if ("startDate".equals(property)) {// 驼峰写法实体
					sbSQL.append(" createDate " + " >= " + "? " + " and ");
				} else if ("endDate".equals(property)) {// 驼峰写法实体
					sbSQL.append(" createDate " + " <= " + "?" + " and ");
				} else {
					sbSQL.append(property + " = " + "?" + " and ");
				}
				list.add(params.get(property));
			}
			sbSQL.append(" 1 = 1");
		}
	}

	/**
	 * 遍历查询的结果集,将所有结果集set到对应的实体中
	 * 
	 * @param cources
	 * @return List
	 */
	private List<OrderRecordContent> setEntityInfo(List<OrderRecordContent> cources) {
		Iterator i = cources.iterator();
		List<OrderRecordContent> orderList = new ArrayList<>();
		OrderRecordContent order = null;
		while (i.hasNext()) {
			order = new OrderRecordContent();
			Object[] obj = (Object[]) i.next();
			for (int j = 0; j < obj.length; j++) {
				order.setEntOrderNo(obj[0].toString());
				order.setEntPayNo(obj[1].toString());
				order.setOrderGoodTotal(Double.parseDouble(obj[2].toString()));
				order.setTax(Double.parseDouble(obj[3].toString()));
				order.setActualAmountPaid(Double.parseDouble(obj[4].toString()));
				order.setRecipientName(obj[5].toString());
				order.setRecipientAddr(obj[6].toString());
				order.setRecipientTel(obj[7].toString());
				order.setOrderDocAcount(obj[8].toString());
				order.setOrderDocName(obj[9].toString());
				order.setOrderDocId(obj[10].toString());
				order.setOrderDocTel(obj[11].toString());
				order.setCreateDate(DateUtil.parseDate2(new String((byte[]) obj[12])));
				order.setMerchantId(obj[13].toString());
				order.setMerchantName(obj[14].toString());
				order.setDeleteFlag(Integer.parseInt(obj[15].toString()));
			}
			orderList.add(order);
		}
		return orderList;
	}

	/**
	 * 并集查询手工订单与商城订单信息,拼接后半段部分手工订单信息表
	 * 
	 * @param sbSQL
	 *            sql字符串语句
	 * @param params
	 *            参数
	 * @param list
	 *            参数
	 * @param page
	 *            页数
	 * @param size
	 *            数目
	 */
	private void unionMorderInfo(StringBuilder sbSQL, Map<String, Object> viceParams, List<Object> list, int page,
			int size) {
		sbSQL.append(
				" SELECT order_id,trade_no ,FCY,Tax,ActualAmountPaid,RecipientName,RecipientAddr,RecipientTel,OrderDocAcount,OrderDocName,OrderDocId,OrderDocTel,DATE_FORMAT(OrderDate,'%Y-%m-%d %H:%i:%s'),merchant_no,create_by,del_flag from ym_shop_manual_morder ");
		if (viceParams != null && viceParams.size() > 0) {
			sbSQL.append(" WHERE ");
			String property;
			Iterator<String> is = viceParams.keySet().iterator();
			while (is.hasNext()) {
				property = is.next();
				if ("startDate".equals(property)) {// 驼峰写法实体
					sbSQL.append(" OrderDate " + " >= " + "? " + " and ");
				} else if ("endDate".equals(property)) {// 驼峰写法实体
					sbSQL.append(" OrderDate " + " <= " + "?" + " and ");
				} else {
					sbSQL.append(property + " = " + "?" + " and ");
				}
				list.add(viceParams.get(property));
			}
			sbSQL.append(" 1 = 1 ");
			// 查询分页
			sbSQL.append(" ORDER BY createDate DESC LIMIT " + (page - 1) + " , " + size);
		}
	}

	@Override
	public long merchantuUnionOrderCount(Class<OrderRecordContent> class1, Map<String, Object> paramMap,
			Map<String, Object> viceParams) {
		Session session = null;
		try {
			session = getSession();
			StringBuilder sbSQL = new StringBuilder();
			List<Object> list = new ArrayList<>();
			sbSQL.append("  SELECT COUNT(m.entOrderNo) AS count FROM(");
			//
			unionShopOrderCount(sbSQL, paramMap, list);
			// 并集
			sbSQL.append(" UNION  ");
			//
			unionMorderCount(sbSQL, viceParams, list);
			sbSQL.append(" ) m  ");
			Query query = session.createSQLQuery(sbSQL.toString());
			if (!list.isEmpty()) {
				for (int i = 0; i < list.size(); i++) {
					query.setParameter(i, list.get(i));
				}
			}
			BigInteger bi = (BigInteger) query.uniqueResult();
			session.close();
			return bi.longValue();
		} catch (Exception re) {
			re.printStackTrace();
			return -1;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	/**
	 * 并集查询手工订单与商城订单信息统计总数,后半段拼接手工订单语句
	 * @param sbSQL
	 * @param viceParams
	 * @param list
	 */
	private void unionMorderCount(StringBuilder sbSQL, Map<String, Object> viceParams, List<Object> list) {
		sbSQL.append(" SELECT order_id  from ym_shop_manual_morder ");
		if (viceParams != null && viceParams.size() > 0) {
			sbSQL.append(" WHERE ");
			String property;
			Iterator<String> is = viceParams.keySet().iterator();
			while (is.hasNext()) {
				property = is.next();
				if ("startDate".equals(property)) {// 驼峰写法实体
					sbSQL.append(" OrderDate " + " >= " + "? " + " and ");
				} else if ("endDate".equals(property)) {// 驼峰写法实体
					sbSQL.append(" OrderDate " + " <= " + "?" + " and ");
				} else {
					sbSQL.append(property + " = " + "?" + " and ");
				}
				list.add(viceParams.get(property));
			}
			sbSQL.append(" 1 = 1 ");
		}

	}

	/**
	 * 并集查询手工订单与商城订单信息统计总数,前半段查询商城语句
	 * @param sbSQL sql语句
	 * @param params 参数
	 * @param list 参数集合
	 */
	private void unionShopOrderCount(StringBuilder sbSQL, Map<String, Object> params, List<Object> list) {
		sbSQL.append(" SELECT entOrderNo  from ym_shop_order_record_content  ");
		if (params != null && params.size() > 0) {
			sbSQL.append(" WHERE ");
			String property;
			Iterator<String> is = params.keySet().iterator();
			while (is.hasNext()) {
				property = is.next();
				if ("startDate".equals(property)) {// 驼峰写法实体
					sbSQL.append(" createDate " + " >= " + "? " + " and ");
				} else if ("endDate".equals(property)) {// 驼峰写法实体
					sbSQL.append(" createDate " + " <= " + "?" + " and ");
				} else {
					sbSQL.append(property + " = " + "?" + " and ");
				}
				list.add(params.get(property));
			}
			sbSQL.append(" 1 = 1");
		}

	}
}

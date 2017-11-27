package org.silver.shop.dao.system.commerce.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.commerce.GoodsRecordDao;
import org.silver.shop.model.system.commerce.GoodsContent;
import org.silver.shop.model.system.commerce.GoodsRecord;
import org.springframework.stereotype.Repository;

import com.justep.baas.data.DataUtils;
import com.justep.baas.data.Table;

@Repository("goodsRecordDao")
public class GoodsRecordDaoImpl extends BaseDaoImpl<Object> implements GoodsRecordDao {

	@Override
	public List findGoodsBaseInfo(Map<String, Object> params, String descParam, int page, int size) {
		return super.findByPropertyDesc(GoodsContent.class, params, descParam, page, size);
	}

	@Override
	public Long findLastId() {
		return super.findLastId(GoodsRecord.class);
	}

	@Override
	public List<Object> findPropertyDesc(Class entity, Map<String, Object> params, String descParams, int page,
			int size) {
		return super.findByPropertyDesc(entity, params, descParams, page, size);
	}

	@Override
	public boolean updateGoodsRecordStatus(String tableName, String merchantIdColumnName, String merchantId,
			String goodsSerialNo, int status) {
		boolean flag = false;
		Session session = null;
		try {
			String sql = "update " + tableName + " t1 set t1.status =? where t1." + merchantIdColumnName
					+ " = ? and t1.goodsSerialNo = ?";
			session = getSession();
			Query query = session.createSQLQuery(sql);
			query.setInteger(0, status);
			query.setString(1, merchantId.trim());
			query.setString(2, goodsSerialNo.trim());
			int reStatus = query.executeUpdate();
			session.close();
			if (reStatus > 0) {
				return true;
			}
			return flag;
		} catch (Exception re) {
			re.printStackTrace();
			return flag;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public Table findByRecordInfo(String merchantId, int page, int size) {
		Session session = null;
		try {
			String queryString = "SELECT t1.customsPort, t1.customsPortName, t1.customsCode, t1.customsName, t1.ciqOrgCode, t1.ciqOrgName, t1.status as acceptFlag, t2.* FROM ym_shop_goods_record t1 RIGHT JOIN ym_shop_goods_record_detail t2 ON t1.goodsSerialNo = t2.goodsSerialNo "
					+ " where  t2.deleteFlag = 0 AND t2.goodsMerchantId = ? ORDER BY t2.createDate DESC";
			List<Object> sqlParams = new ArrayList<>();
			sqlParams.add(merchantId);
			session = getSession();
			java.sql.Connection conn = session.connection();
			Table l = null;
			if (page > 0 && size > 0) {
				page = page - 1;
				l = DataUtils.queryData(conn, queryString, sqlParams, null, page * size, size);
			} else {
				l = DataUtils.queryData(conn, queryString, sqlParams, null, null, null);
			}
			session.close();
			// Transform.tableToJson(l);
			return l;
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
	public Table findByRecordInfoLike(Class entity, Map<String, Object> params, Map blurryMap, int page,
			int size) {
		Session session = null;
		try {
			String queryString = "SELECT t1.customsPort, t1.customsPortName, t1.customsCode, t1.customsName, t1.ciqOrgCode, t1.ciqOrgName, t1.status as acceptFlag, t2.* FROM ym_shop_goods_record t1 RIGHT JOIN ym_shop_goods_record_detail t2 ON t1.goodsSerialNo = t2.goodsSerialNo ";
			queryString += " where ";
			List<Object> sqlParams = new ArrayList<>();
			if (params != null && params.size() > 0) {
				String property;
				Iterator<String> is = params.keySet().iterator();
				while (is.hasNext()) {
					property = is.next();
					if ("startDate".equals(property)) {
						queryString = queryString + "t2.createDate " + " > " + "?" + " and ";
					} else if ("endDate".equals(property)) {
						queryString = queryString + "t2.createDate " + " < " + "?" + " and ";
					} else {
						queryString = queryString + "t2." + property + " = " + "?" + " and ";
					}
					sqlParams.add(params.get(property));
				}
			}
			if (blurryMap != null && !blurryMap.isEmpty()) {
				String property;
				Iterator<String> is = blurryMap.keySet().iterator();
				while (is.hasNext()) {
					property = is.next();
					queryString = queryString + "t2." + property + " LIKE " + "?" + " and ";
					sqlParams.add(blurryMap.get(property));
				}
			}
			queryString += " 1=1 Order By id DESC";
			session = getSession();
			java.sql.Connection conn = session.connection();
			Table l = null;
			if (page > 0 && size > 0) {
				page = page - 1;
				l = DataUtils.queryData(conn, queryString, sqlParams, null, page * size, size);
			} else {
				l = DataUtils.queryData(conn, queryString, sqlParams, null, null, null);
			}
			session.close();
			// Transform.tableToJson(l);
			return l;
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

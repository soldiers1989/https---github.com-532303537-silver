package org.silver.shop.dao.system.commerce.impl;

import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.commerce.GoodsRecordDao;
import org.silver.shop.model.system.commerce.GoodsContent;
import org.silver.shop.model.system.commerce.GoodsRecord;
import org.springframework.stereotype.Repository;

@Repository("goodsRecordDao")
public class GoodsRecordDaoImpl extends BaseDaoImpl<Object> implements GoodsRecordDao {

	@Override
	public List findGoodsBaseInfo(Map<String, Object> params, String descParam, int page, int size) {
		return super.findByPropertyDesc(GoodsContent.class, params, descParam, page, size);
	}

	@Override
	public List<Object> findByProperty(Class entity, Map params, int page, int size) {
		return super.findByProperty(entity, params, page, size);
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
	public long findSerialNoCount(Class entity, String property, int year) {
		return super.findSerialNoCount(entity, property, year);
	}

	@Override
	public boolean add(Object entity) {
		return super.add(entity);
	}

	@Override
	public boolean update(Object entity) {
		return super.update(entity);
	}

	@Override
	public boolean delete(Object entity) {
		return super.delete(entity);
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
				return  true;
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

}

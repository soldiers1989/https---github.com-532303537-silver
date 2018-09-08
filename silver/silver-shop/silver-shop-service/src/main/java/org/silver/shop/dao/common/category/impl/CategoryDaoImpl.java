package org.silver.shop.dao.common.category.impl;

import java.util.Date;

import org.hibernate.Query;
import org.hibernate.Session;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.common.category.CategoryDao;
import org.springframework.stereotype.Repository;

import com.justep.baas.data.DataUtils;
import com.justep.baas.data.Table;

@Repository("categoryDao")
public class CategoryDaoImpl<E> extends BaseDaoImpl<E> implements CategoryDao<E> {

	@Override
	public Table findAllCategory() {
		Session session = null;
		try {
			String sql = "SELECT m.*, t3.id AS thirdId, t3.goodsThirdTypeName, t3.vat, t3.tariff, t3.consumptionTax, t3.consolidatedTax,t3.serialNo AS thirdNo FROM "
					+ "( SELECT t1.id, t1.firstTypeName, t1.serialNo AS firstNo,t2.id AS secId,t2.goodsSecondTypeName,t2.serialNo AS secondeNo FROM	ym_shop_goods_first_type t1	LEFT JOIN ym_shop_goods_second_type t2 ON (t1.id = t2.firstTypeId) ORDER BY	t1.id) m"
					+ " LEFT JOIN ym_shop_goods_third_type t3 ON (m.secId = t3.secondTypeId) "
					+ " ORDER BY m.firstNo,m.secondeNo,t3.serialNo ";
			session = getSession();
			Table t = DataUtils.queryData(session.connection(), sql, null, null, null, null);
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
	public Table searchSecondCategory() {
		Session session = null;
		try {
			String sql = "SELECT t1.id AS secId,t1.firstTypeId,t1.goodsSecondTypeName,t1.serialNo AS secondNo,t2.id AS thirdId,t2.goodsThirdTypeName,t2.vat,t2.tariff,t2.consolidatedTax,t2.consumptionTax ,t2.serialNo AS thirdNo FROM ym_shop_goods_second_type t1 "
					+ "LEFT JOIN ym_shop_goods_third_type t2 ON t1.id = t2.secondTypeId ORDER BY t1.serialNo,t2.serialNo ";
			session = getSession();
			Table t = DataUtils.queryData(session.connection(), sql, null, null, null, null);
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
	public boolean updateGoodsRecordDetailSecondCategory(int firstId, String firstTypeName, long secondId,
			String goodsSecondTypeName, String managerName) {
		Session session = null;
		try {
			String sql = "update ym_shop_goods_record_detail  SET  spareGoodsFirstTypeId = ?  , spareGoodsFirstTypeName = ?   , spareGoodsSecondTypeName = ? , updateBy = ? , updateDate = ?"
					+ " WHERE spareGoodsSecondTypeId = ? ";
			session = getSession();
			Query query = session.createSQLQuery(sql);
			query.setInteger(0, firstId);
			query.setString(1, firstTypeName.trim());
			query.setString(2, goodsSecondTypeName.trim());
			query.setString(3, managerName.trim());
			query.setDate(4, new Date());
			query.setLong(5, secondId);
			int reStatus = query.executeUpdate();
			session.close();
			if (reStatus >= 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception re) {
			re.printStackTrace();
			return false;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public boolean updateGoodsBaseInfoSecondCategory(int firstId, String firstTypeName, long secondId,
			String goodsSecondTypeName, String managerName) {
		Session session = null;
		try {
			String sql = "update ym_shop_goods_content  SET  goodsFirstTypeId = ?  , goodsFirstTypeName = ?   , goodsSecondTypeName = ? , updateBy = ? , updateDate = ?"
					+ " WHERE goodsSecondTypeId = ? ";
			session = getSession();
			Query query = session.createSQLQuery(sql);
			query.setInteger(0, firstId);
			query.setString(1, firstTypeName.trim());
			query.setString(2, goodsSecondTypeName.trim());
			query.setString(3, managerName.trim());
			query.setDate(4, new Date());
			query.setLong(5, secondId);
			int reStatus = query.executeUpdate();
			session.close();
			if (reStatus >= 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception re) {
			re.printStackTrace();
			return false;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

	}
}

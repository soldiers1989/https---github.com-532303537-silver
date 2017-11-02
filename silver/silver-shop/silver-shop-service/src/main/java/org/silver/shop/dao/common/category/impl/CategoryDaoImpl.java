package org.silver.shop.dao.common.category.impl;


import org.hibernate.Session;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.common.category.CategoryDao;
import org.springframework.stereotype.Repository;

import com.justep.baas.data.DataUtils;
import com.justep.baas.data.Table;

@Repository("categoryDao")
public class CategoryDaoImpl extends BaseDaoImpl<Object> implements CategoryDao {
	
	@Override
	public Table findAllCategory() {
		Session session = null;
		try {
			String sql = "SELECT m.*,t3.id as thirdId,t3.goodsThirdTypeName ,t3.vat,t3.consumptionTax,t3.consolidatedTax from "
					+ "(SELECT t1.id,t1.firstTypeName,t2.id as secId,t2.goodsSecondTypeName from ym_shop_goods_first_type t1 LEFT JOIN ym_shop_goods_second_type t2 ON (t1.id= t2.firstTypeId)) m  "
					+ "LEFT JOIN ym_shop_goods_third_type t3 ON (m.secId = t3.secondTypeId)";
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
}

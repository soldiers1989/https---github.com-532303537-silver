package org.silver.shop.dao.system.commerce.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.commerce.MerchantCounterDao;
import org.silver.util.StringEmptyUtils;
import org.springframework.stereotype.Repository;

import com.justep.baas.data.DataUtils;
import com.justep.baas.data.Table;

@Repository("merchantCounterDao")
public class MerchantCounterDaoImpl extends BaseDaoImpl implements MerchantCounterDao {

	@Override
	public Table getEnteringTheCabinetGoods(String merchantId, Map<String, Object> datasMap, int page, int size) {
		Session session = null;
		try {
			List<Object> sqlParams = new ArrayList<>();
			StringBuilder sql = new StringBuilder(
					"SELECT m.* FROM(SELECT t1.entGoodsNo,t1.goodsName,t1.sellDate,t2.spareGoodsImage ,t1.regPrice FROM ym_shop_stock_content t1 LEFT JOIN ym_shop_goods_record_detail t2 ON t1.entGoodsNo = t2.entGoodsNo WHERE  t1.sellFlag =1 ");
			if (StringEmptyUtils.isNotEmpty(merchantId)) {
				sql.append(" AND t1.merchantId = ? ");
				sqlParams.add(merchantId);
			}
			sql.append(") m  ");
			sql.append(
					"LEFT JOIN ym_shop_counter_goods_content t3 ON m.entGoodsNo = t3.entGoodsNo WHERE t3.entGoodsNo IS NULL ");
			if (datasMap != null && !datasMap.isEmpty()) {
				String entGoodsNo = datasMap.get("entGoodsNo") + "";
				String goodsName = datasMap.get("goodsName") + "";
				if (StringEmptyUtils.isNotEmpty(entGoodsNo)) {
					sql.append("AND  m.entGoodsNo = ? ");
					sqlParams.add(entGoodsNo);
				}
				if (StringEmptyUtils.isNotEmpty(goodsName)) {
					sql.append("AND  m.goodsName Like ? ");
					sqlParams.add("%" + goodsName + "%");
				}
			}
			session = getSession();
			java.sql.Connection conn = session.connection();
			Table table = null;
			if (page > 0 && size > 0) {
				page = page - 1;
				table = DataUtils.queryData(conn, sql.toString(), sqlParams, null, page * size, size);
			} else {
				table = DataUtils.queryData(conn, sql.toString(), sqlParams, null, null, null);
			}
			session.close();
			// Transform.tableToJson(l);
			return table;
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

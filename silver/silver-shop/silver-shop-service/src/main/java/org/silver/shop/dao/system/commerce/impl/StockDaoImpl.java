package org.silver.shop.dao.system.commerce.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.commerce.StockDao;
import org.springframework.stereotype.Repository;

import com.justep.baas.data.DataUtils;
import com.justep.baas.data.Table;

@Repository("stockDao")
public class StockDaoImpl extends BaseDaoImpl<Object> implements StockDao {

	@Override
	public Table getWarehousGoodsInfo(String merchantId, String warehouseCode, int page, int size) {
		Session session = null;
		try {
			String queryString = "SELECT m.*,t3.entGoodsNo from (SELECT t1.customsCode,t2.goodsName,t2.entGoodsNo,t2.brand,t2.gUnit,t2.goodsStyle,t2.goodsDetailId,t2.regPrice FROM ym_shop_goods_record t1 LEFT JOIN ym_shop_goods_record_detail t2 ON t1.goodsSerialNo = t2.goodsSerialNo "
					+ "WHERE t1.customsCode = ? AND t1.merchantId = ? AND t1.deleteFlag = 0 AND t2. STATUS = 2 AND ( t2.recordFlag = 1 OR t2.recordFlag = 2)) m "
					+ " LEFT JOIN ym_shop_stock_content t3 ON (m.entGoodsNo = t3.entGoodsNo) WHERE t3.entGoodsNo IS NULL";
			List<Object> sqlParams = new ArrayList<>();
			sqlParams.add(warehouseCode);
			sqlParams.add(merchantId);
			session = getSession();
			Table l = null;
			if (page > 0 && size > 0) {
				page = page - 1;
				l = DataUtils.queryData(session.connection(), queryString, sqlParams, null, page * size, size);
			} else {
				l = DataUtils.queryData(session.connection(), queryString, sqlParams, null, null, null);
			}
			session.close();
			return l;
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

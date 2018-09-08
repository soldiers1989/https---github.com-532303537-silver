package org.silver.shop.dao.system.commerce.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.commerce.GoodsContentDao;
import org.silver.shop.model.system.commerce.GoodsContent;
import org.silver.util.StringEmptyUtils;
import org.springframework.stereotype.Repository;

import com.justep.baas.data.DataUtils;
import com.justep.baas.data.Table;

@Repository("goodsContentDao")
public class GoodsContentDaoImpl extends BaseDaoImpl implements GoodsContentDao {

	@Override
	public Long findLastId() {
		return super.findLastId(GoodsContent.class);
	}

	@Override
	public Table getAlreadyRecordGoodsBaseInfo(Map<String, Object> datasMap, int page, int size) {
		Session session = null;
		StringBuilder queryString = new StringBuilder();
		Connection conn = null;
		try {
			queryString
					.append("SELECT t1.*,t2.sellCount,t2.regPrice as sellPrice,t2.marketPrice,t2.doneCount AS doneCount from ym_shop_goods_record_detail t1  LEFT JOIN ym_shop_stock_content t2 "
							+ "	 on t1.entGoodsNo = t2.entGoodsNo WHERE t2.sellFlag = 1 AND t1.status =2 ");
			List<Object> sqlParams = new ArrayList<>();
			if(datasMap !=null){
				appendSort(datasMap, queryString, sqlParams);
			}
			session = getSession();
			Table l = null;
			if (page > 0 && size > 0) {
				page = page - 1;
				l = DataUtils.queryData(session.connection(), queryString.toString(), sqlParams, null, page * size,
						size);
			} else {
				l = DataUtils.queryData(session.connection(), queryString.toString(), sqlParams, null, null, null);
			}
			session.connection().close();
			session.close();
			// Transform.tableToJson(l);
			return l;
		} catch (Exception re) {
			re.printStackTrace();
			return null;
		} finally {
			if (session != null && session.isOpen()) {
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				session.close();
			}
		}
	}

	/**
	 * 拼接sql条件过滤字段
	 * 
	 * @param datasMap
	 *            参数
	 * @param queryString
	 *            sql语句
	 * @param sqlParams
	 *            参数
	 */
	private void appendSort(Map<String, Object> datasMap, StringBuilder queryString, List<Object> sqlParams) {
		String goodsName = datasMap.get("goodsName") + "";
		if (StringEmptyUtils.isNotEmpty(goodsName)) {
			sqlParams.add("%" + goodsName + "%");
			queryString.append(" and t1.spareGoodsName LIKE  ? ");
		}
		if (StringEmptyUtils.isNotEmpty(datasMap.get("thirdType"))) {
			int thirdType = Integer.parseInt(datasMap.get("thirdType") + "");
			sqlParams.add(thirdType);
			queryString.append("  and t1.spareGoodsThirdTypeId = ? ");
		} else if (StringEmptyUtils.isNotEmpty(datasMap.get("secondType"))) {
			int secondType = Integer.parseInt(datasMap.get("secondType") + "");
			sqlParams.add(secondType);
			queryString.append("  and t1.spareGoodsSecondTypeId = ? ");
		} else if (StringEmptyUtils.isNotEmpty(datasMap.get("firstType"))) {
			int firstType = Integer.parseInt(datasMap.get("firstType") + "");
			sqlParams.add(firstType);
			queryString.append(" and t1.spareGoodsFirstTypeId = ? ");
		}
		if (StringEmptyUtils.isNotEmpty(datasMap.get("sortType"))) {
			String orderBy = "";
			switch (datasMap.get("sortType") + "") {
			case "doneCount":
				orderBy = datasMap.get("orderBy") + "";
				if("ASC".equals(orderBy)){
					queryString.append(" ORDER BY doneCount ASC ");
				}else if("DESC".equals(orderBy)){
					queryString.append(" ORDER BY doneCount DESC ");
				}
				break;
			case "sellPrice":
				orderBy = datasMap.get("orderBy") + "";
				if("ASC".equals(orderBy)){
					queryString.append(" ORDER BY sellPrice ASC ");
				}else if("DESC".equals(orderBy)){
					queryString.append(" ORDER BY sellPrice DESC ");
				}
				break;
			default:
				break;
			}
		}
	}

}

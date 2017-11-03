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
import org.springframework.stereotype.Repository;

import com.justep.baas.data.DataUtils;
import com.justep.baas.data.Table;

@Repository("goodsContentDao")
public class GoodsContentDaoImpl<T> extends BaseDaoImpl<T> implements GoodsContentDao {

	@Override
	public Long findLastId() {
		return super.findLastId(GoodsContent.class);
	}

	@Override
	public boolean add(Object entity) {
		return super.add(entity);
	}

	@Override
	public List<Object> findByProperty(Class entity, Map params, int page, int size) {
		return super.findByProperty(entity, params, page, size);
	}

	@Override
	public boolean update(GoodsContent entity) {
		return super.update(entity);
	}

	@Override
	public List<Object> findBlurryProperty(Class entity, Map params, String startTime, String endTime, int page,
			int size) {
		return super.findBlurryProperty(entity, params, startTime, endTime, page, size);
	}
	
	@Override
	public long findSerialNoCount(Class entity,String property,int year){
		return super.findSerialNoCount(entity,  property,year);
	}

	@Override
	public Table getAlreadyRecordGoodsBaseInfo(int firstType, int  secndType,int thirdType,int page,int size) {
		Session session = null;
		String queryString = null;
		Connection conn = null;
		try {
			queryString = "SELECT t1.*,t2.sellCount,t2.regPrice from ym_shop_goods_content t1  LEFT JOIN ym_shop_stock_content t2 "
					+ "on t1.goodsId = t2.goodsId WHERE t2.sellFlag = 1  ";			
			List<Object> sqlParams = new ArrayList<>();
			if(firstType >0 && secndType > 0 && thirdType > 0){
				sqlParams.add(firstType);
				sqlParams.add(secndType);
				sqlParams.add(thirdType);
				queryString = queryString  +" and t1.goodsFirstTypeId = ?  and t1.goodsSecondTypeId = ? and t1.goodsThirdTypeId = ? ";
			}else if(firstType >0 && secndType > 0){
				sqlParams.add(firstType);
				sqlParams.add(secndType);
				queryString = queryString  +" and t1.goodsFirstTypeId = ? and t1.goodsSecondTypeId = ? ";
			}else if(firstType > 0){
				sqlParams.add(firstType);
				queryString = queryString  +" and t1.goodsFirstTypeId = ? ";
			}
			session = getSession();
		//	ConnectionProvider cp = ((SessionFactoryImplementor) session.getSessionFactory()).getConnectionProvider();
		//	 conn = cp.getConnection();
			Table l = null;
			if (page > 0 && size > 0) {
				page = page - 1;
				l = DataUtils.queryData(session.connection(), queryString, sqlParams, null, page * size, size);
			} else {
				l = DataUtils.queryData(session.connection(), queryString, sqlParams, null, null, null);
			}
			session.connection().close();
			session.close();
			// Transform.tableToJson(l);
			return l;
		} catch (Exception  re) {
			re.printStackTrace();
			return null;
		} finally {
			if (session != null && session.isOpen()) {
				if(conn!=null){
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
	
	public long findByPropertyCount(Class entity,Map params){		
		return super.findByPropertyCount(entity, params);
	}
	
}

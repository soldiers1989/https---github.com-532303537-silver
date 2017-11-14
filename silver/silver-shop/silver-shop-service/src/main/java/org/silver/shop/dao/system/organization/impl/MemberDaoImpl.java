package org.silver.shop.dao.system.organization.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.organization.MemberDao;
import org.silver.shop.model.system.organization.Merchant;
import org.springframework.stereotype.Repository;

import com.justep.baas.data.DataUtils;
import com.justep.baas.data.Table;

@Repository("memberDao")
public class MemberDaoImpl<T> extends BaseDaoImpl<T> implements MemberDao {
	@Override
	public long findLastId() { 
		return this.findLastId(Merchant.class);
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
	public boolean update(Object entity) {
		return super.update(entity);
	}

	@Override
	public long findSerialNoCount(Class entity, String property, int year) {
		return super.findSerialNoCount(entity, property, year);
	}

	@Override
	public boolean delete(Object entity) {
		return super.delete(entity);
	}

	@Override
	public long findByPropertyCount(Class entity, Map params) {
		return super.findByPropertyCount(entity, params);
	}

	@Override
	public Table findOrderInfo(String memberId, int page, int size) {
		Session session = null;
		try {
			String sql = "SELECT t2.orderId,t2.goodsId,t2.goodsName,t2.goodsImage,t2.goodsPrice,t2.goodsCount,t2.goodsTotalPrice,	t2.merchantId,t2.merchantName,t2.memberId,t2.memberName,t1.status,t2.createDate "
					+ "FROM ym_shop_order_content t1 LEFT JOIN ym_shop_order_goods_content t2 "
					+ "ON t1.orderId = t2.orderId " + "WHERE t2.deleteFlag = 0 " 
					+ "AND t1.memberId = ? ";
			List<Object> sqlParams = new ArrayList<>();
			sqlParams.add(memberId);
			session = getSession();
			//ConnectionProvider cp = ((SessionFactoryImplementor) session.getSessionFactory()).getConnectionProvider();
			//Connection c = cp.getConnection();
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
}

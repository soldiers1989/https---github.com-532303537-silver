package org.silver.shop.dao.system.commerce.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.hibernate.Session;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.commerce.OrderDao;
import org.silver.shop.model.system.organization.Member;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.ResponseBody;

import com.justep.baas.data.DataUtils;
import com.justep.baas.data.Table;

@Repository("orderDao")
public class OrderDaoImpl extends BaseDaoImpl implements OrderDao {

	@Override
	public List<Object> findAll(Class entity, int page, int size) {
		return super.findAll(entity, page, size);
	}

	@Override
	public List findByProperty(Class entity, Map params, int page, int size) {
		return super.findByProperty(entity, params, page, size);
	}

	@Override
	public boolean add(Object entity) {
		return super.add(entity);
	}

	@Override
	public boolean delete(Object entity) {
		return super.delete(entity);
	}

	@Override
	public boolean update(Object entity) {
		return super.update(entity);
	}

	@Override
	public Long findAllCount(Class entity) {
		return super.findAllCount(entity);
	}

	@Override
	public long findLastId(Class entity) {
		return super.findLastId(entity);
	}

	@Override
	public List<Object> findBlurryProperty(Class entity, Map params, String startTime, String endTime, int page,
			int size) {
		return super.findBlurryProperty(entity, params, startTime, endTime, page, size);
	}

	@Override
	public List<Object> findByPropertyDesc(Class entity, Map params, String descParams, int page, int size) {
		return super.findByPropertyDesc(entity, params, descParams, page, size);
	}

	@Override
	public long findSerialNoCount(Class entity, String property, int year) {
		return super.findSerialNoCount(entity, property, year);
	}

	@Override
	public long findByPropertyCount(Class entity, Map params) {
		return super.findByPropertyCount(entity, params);
	}

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

}

package org.silver.shop.dao.system.commerce.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.commerce.OrderDao;
import org.silver.shop.model.system.organization.Member;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.ResponseBody;

@Repository("orderDao")
public class OrderDaoImpl extends BaseDaoImpl  implements OrderDao {

	@Override
	public List<Object> findAll(Class entity, int page, int size) {
		return super.findAll(entity, page, size);
	}

	@Override
	public List<Object> findByProperty(Class entity, Map params, int page, int size) {
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
	public List<Object> findBlurryProperty(Class entity, Map params, String startTime, String endTime,
			int page, int size) {
		return super.findBlurryProperty(entity, params, startTime, endTime, page, size);
	}

	@Override
	public List<Object> findByPropertyDesc(Class entity, Map params, String descParams, int page,
			int size) {
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

}

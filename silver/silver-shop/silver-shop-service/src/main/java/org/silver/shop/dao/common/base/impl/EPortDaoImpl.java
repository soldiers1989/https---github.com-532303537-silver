package org.silver.shop.dao.common.base.impl;

import java.util.List;
import java.util.Map;

import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.common.base.EPortDao;
import org.springframework.stereotype.Repository;


@Repository("ePortDao")
public class EPortDaoImpl extends BaseDaoImpl implements EPortDao {

	@Override
	public List<Object> findByProperty(Class entity, Map params, int page, int size) {
		return super.findByProperty(entity, params, page, size);
	}

	@Override
	public boolean add(Object entity) {
		return super.add(entity);
	}

	@Override
	public List<Object> findAll(Class entity,int page,int size) {
		return super.findAll(entity, page, size);
	}

	@Override
	public boolean update(Object entity){
		return super.update(entity);
	}
}

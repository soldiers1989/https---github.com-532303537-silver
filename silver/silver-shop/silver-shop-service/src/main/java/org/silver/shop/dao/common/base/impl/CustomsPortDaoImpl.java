package org.silver.shop.dao.common.base.impl;

import java.util.List;

import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.common.base.CustomsPortDao;
import org.springframework.stereotype.Repository;

@Repository("customsPortDao")
public class CustomsPortDaoImpl extends BaseDaoImpl implements CustomsPortDao{

	@Override
	public boolean add(Object entity){
		return super.add(entity);
	}

	@Override
	public List<Object> findAll(Class entity,int page,int size) {
		return super.findAll(entity, page, size);
	}
}

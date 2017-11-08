package org.silver.shop.dao.system.commerce.impl;

import java.util.List;
import java.util.Map;

import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.commerce.WarehousDao;
import org.springframework.stereotype.Repository;

@Repository("warehousDao")
public class WarehousDaoImpl extends BaseDaoImpl<Object> implements WarehousDao {
	@Override
	public List<Object> findByProperty(Class entity, Map params, int page, int size) {
		return super.findByProperty(entity, params, page, size);
	}
	
	@Override
	public boolean add(Object entity) {
		return super.add(entity);
	}

	@Override
	public boolean update(Object entity) {
		return super.update(entity);
	}
	
	@Override 
	public long findByPropertyCount(Class entity,Map params) {
		return super.findByPropertyCount(entity,params);
	}
}

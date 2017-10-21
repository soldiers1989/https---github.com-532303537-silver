package org.silver.shop.dao.system.organization.impl;

import java.util.List;
import java.util.Map;

import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.organization.MemberDao;
import org.silver.shop.model.system.organization.Merchant;
import org.springframework.stereotype.Repository;

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
	public long findSerialNoCount(Class entity,String property,int year){
		return super.findSerialNoCount(entity,  property,year);
	}
	
	@Override
	public boolean delete(Object entity){
		return super.delete(entity);
	}
}

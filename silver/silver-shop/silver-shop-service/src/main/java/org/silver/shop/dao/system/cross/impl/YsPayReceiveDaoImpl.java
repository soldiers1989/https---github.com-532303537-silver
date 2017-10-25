package org.silver.shop.dao.system.cross.impl;

import java.util.List;
import java.util.Map;


import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.cross.YsPayReceiveDao;
import org.silver.shop.model.system.organization.Member;
import org.springframework.stereotype.Repository;

@Repository("ysPayReceiveDao")
public class YsPayReceiveDaoImpl extends BaseDaoImpl implements YsPayReceiveDao {

	@Override
	public List<Object> findAll(Class entity, int page, int size) {
		return null;
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
	public Member findMailboxbyId(long id) {
		return null;
	}

	@Override
	public Long findAllCount(Class entity) {
		return null;
	}

	@Override
	public long findLastId(Class entity) {
		return 0;
	}

	@Override
	public List<Object> findBlurryProperty(Class entity, Map params, String startTime, String endTime,
			int page, int size) {
		return null;
	}

	@Override
	public List<Object> findByPropertyDesc(Class entity, Map params, String descParams, int page,
			int size) {
		return null;
	}

	@Override
	public long findSerialNoCount(Class entity, String property, int year) {
		return 0;
	}

	@Override
	public long findByPropertyCount(Class entity, Map params) {
		return 0;
	}

}

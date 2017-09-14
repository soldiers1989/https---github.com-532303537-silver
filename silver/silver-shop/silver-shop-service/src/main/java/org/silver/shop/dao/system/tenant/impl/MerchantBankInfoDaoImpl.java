package org.silver.shop.dao.system.tenant.impl;

import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.tenant.MerchantBankInfoDao;
import org.silver.shop.model.system.tenant.MerchantBankInfo;
import org.springframework.stereotype.Repository;

@Repository("merchantBankInfoDao")
public class MerchantBankInfoDaoImpl<T> extends BaseDaoImpl<T> implements MerchantBankInfoDao {

	@Override
	public boolean add(MerchantBankInfo entity) {
		return super.add(entity);
	}

	@Override
	public List<Object> findByProperty(Class entity, Map params, int page, int size) {
		return super.findByProperty(entity, params, page, size);
	}


	@Override
	public boolean update(Class entity) {
		return super.update(entity);
	}

	@Override
	public boolean delete(Class entity) {
		return super.delete(entity);
	}

}

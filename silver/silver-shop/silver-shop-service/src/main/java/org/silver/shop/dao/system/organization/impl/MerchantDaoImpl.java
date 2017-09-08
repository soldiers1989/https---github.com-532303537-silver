package org.silver.shop.dao.system.organization.impl;

import java.util.List;
import java.util.Map;

import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.organization.MerchantDao;
import org.silver.shop.model.system.organization.Merchant;
import org.springframework.stereotype.Repository;

@Repository("merchantDao")
public class MerchantDaoImpl<T> extends BaseDaoImpl<T> implements MerchantDao {

	@Override
	public List<Object> checkMerchantName(Class entity, Map params, int page, int size) {
		return this.findByProperty(Merchant.class, params, page, size);
	}

	@Override
	public Long findLastId() {
		return this.findLastId(Merchant.class);
	}

	@Override
	public boolean saveMerchantContent(Object entity) {
		return this.add(entity);
	}

	@Override
	public boolean savenMerchantRecordInfo(Object entity) {
		return this.add(entity);
	}

	
}

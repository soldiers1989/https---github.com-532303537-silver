package org.silver.shop.dao.system.organization.impl;


import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.organization.ManagerDao;
import org.silver.shop.model.system.organization.Merchant;
import org.springframework.stereotype.Repository;

@Repository("managerDao")
public class ManagerDaoImpl extends BaseDaoImpl implements ManagerDao {

	@Override
	public long findLastId() {
		return this.findLastId(Merchant.class);
	}
}

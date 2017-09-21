package org.silver.shop.dao.common.base.impl;

import java.util.List;

import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.common.base.MeteringDao;
import org.silver.shop.model.common.base.Metering;
import org.springframework.stereotype.Repository;

@Repository("meteringDao")
public class MeterDaoImpl extends BaseDaoImpl<Object> implements MeteringDao {

	@Override
	public List findMetering() {
		return super.findAll(Metering.class, 0, 0);
	}

}

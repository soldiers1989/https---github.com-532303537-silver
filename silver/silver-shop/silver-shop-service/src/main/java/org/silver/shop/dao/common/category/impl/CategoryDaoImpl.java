package org.silver.shop.dao.common.category.impl;

import java.util.List;

import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.common.category.CategoryDao;
import org.silver.shop.model.common.category.GoodsFirstType;
import org.silver.shop.model.common.category.GoodsSecondType;
import org.silver.shop.model.common.category.GoodsThirdType;
import org.springframework.stereotype.Repository;

@Repository("categoryDao")
public class CategoryDaoImpl extends BaseDaoImpl<Object> implements CategoryDao {
	
	@Override
	public List<Object> findAllfirstType() {
		return super.findAll(GoodsFirstType.class,0,0);
	}

	@Override
	public List<Object> findAllSecondType() {
		return super.findAll(GoodsSecondType.class,0,0);
	}

	@Override
	public List<Object> findAllThirdType() {
		return super.findAll(GoodsThirdType.class,0,0);
	}
}

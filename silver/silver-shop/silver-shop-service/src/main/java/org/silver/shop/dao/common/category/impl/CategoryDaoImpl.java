package org.silver.shop.dao.common.category.impl;

import java.util.List;

import org.silver.shop.component.ChooseDatasourceHandler;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.SessionFactory;
import org.silver.shop.dao.common.category.CategoryDao;
import org.silver.shop.model.common.category.GoodsFirstType;
import org.silver.shop.model.common.category.GoodsSecondType;
import org.silver.shop.model.common.category.GoodsThirdType;
import org.springframework.stereotype.Repository;

@Repository("categoryDao")
public class CategoryDaoImpl extends BaseDaoImpl implements CategoryDao {

	
	
	@Override
	public List<Object> findAllfirstType() {
		return this.findAll(GoodsFirstType.class);
	}

	@Override
	public List<Object> findAllSecondType() {
		return this.findAll(GoodsSecondType.class);
	}

	@Override
	public List<Object> findAllThirdType() {
		return this.findAll(GoodsThirdType.class);
	}
	
	public static void main(String[] args) {
		ChooseDatasourceHandler.hibernateDaoImpl.setSession(SessionFactory.getSession());
		CategoryDaoImpl cdao = new CategoryDaoImpl();
		/*System.out.println(cdao.findAllCount());*/
		System.out.println(cdao.findAll(GoodsFirstType.class).size());
		
	}
}

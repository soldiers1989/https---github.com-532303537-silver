package org.silver.shop.dao.system.commerce.impl;

import java.util.List;
import java.util.Map;

import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.commerce.GoodsContentDao;
import org.silver.shop.model.system.commerce.GoodsContent;
import org.springframework.stereotype.Repository;

@Repository("goodsContentDao")
public class GoodsContentDaoImpl<T> extends BaseDaoImpl<T> implements GoodsContentDao {

	@Override
	public Long findLastId() {
		return super.findLastId(GoodsContent.class);
	}

	@Override
	public boolean add(GoodsContent goods) {
		return super.add(goods);
	}

	@Override
	public List<Object> findByProperty(Class entity, Map params,  int page, int size) {
		return super.findByProperty(entity, params,  page, size);
	}

	@Override
	public boolean update(GoodsContent entity) {
		return super.update(entity);
	}
}

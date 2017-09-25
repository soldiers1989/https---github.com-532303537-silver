package org.silver.shop.dao.system.commerce.impl;

import java.util.List;
import java.util.Map;

import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.commerce.GoodsRecordDao;
import org.silver.shop.model.system.commerce.GoodsContent;
import org.silver.shop.model.system.commerce.GoodsRecordContent;
import org.springframework.stereotype.Repository;

@Repository("goodsRecordDao")
public class GoodsRecordDaoImpl extends BaseDaoImpl<Object> implements GoodsRecordDao {

	@Override
	public List findGoodsBaseInfo(Map<String, Object> params, String descParam, int page, int size) {
		return super.findByPropertyDesc(GoodsContent.class, params, descParam, page, size);
	}

	@Override
	public List<Object> findByProperty(Class entity, Map params, int page, int size) {
		return super.findByProperty(entity, params, page, size);
	}

	@Override
	public Long findLastId() {
		return super.findLastId(GoodsRecordContent.class);
	}
	
	@Override
	public List<Object> findPropertyDesc(Class entity, Map<String, Object> params, String descParams, int page,
			int size){
		return super.findByPropertyDesc(entity, params, descParams, page, size) ;
	}
	
	@Override
	public String findGoodsYearLastId(Class entity, int year) {
		return super.findGoodsYearLastId(entity, year);
	}
}

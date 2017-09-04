package org.silver.shop.dao.common.category;


import java.util.List;

import org.silver.shop.dao.BaseDao;

public interface CategoryDao extends BaseDao{
	
	/**
	 * 查询商品所有第一类型
	 * @return list
	 */
	public List<Object> findAllfirstType();
	
	/**
	 * 查询商品所有第二类型
	 * @return list
	 */
	public List<Object> findAllSecondType();
	
	/**
	 *  查询商品所有第三类型
	 * @return list
	 */
	public List<Object> findAllThirdType();
	
	
}

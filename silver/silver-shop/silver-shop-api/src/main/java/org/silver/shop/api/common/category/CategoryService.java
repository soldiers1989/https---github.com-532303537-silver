package org.silver.shop.api.common.category;

import java.util.List;


public interface CategoryService {
	
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

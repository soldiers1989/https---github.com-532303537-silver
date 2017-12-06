package org.silver.shop.dao.common.category;



import org.silver.shop.dao.BaseDao;

import com.justep.baas.data.Table;

public interface CategoryDao<T> extends BaseDao<T>{
	
	/**
	 * 查询商品所有类型
	 * @return list
	 */
	public Table findAllCategory();
	
	
}

package org.silver.shop.api.common.category;

import java.util.HashMap;
import java.util.Map;


public interface CategoryService {
	/**
	 * 查询所有商品类型,并进行对应的级联封装到Map
	 * @return
	 */
	public Map<String, HashMap<String, Object>> findGoodsType();
}

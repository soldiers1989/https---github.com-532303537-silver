package org.silver.shop.dao.system.commerce;


import org.silver.shop.dao.BaseDao;

import com.justep.baas.data.Table;

public interface StockDao  extends BaseDao{
	/**
	 * 根据实体,及Map键值对查询数据 key=(表中列名称),value=(查询参数)
	 * @param entity
	 * @param params
	 * @param page
	 * @param size
	 */
	public Table getWarehousGoodsInfo(String merchantId ,String warehouseCode, int page, int size);

	
}

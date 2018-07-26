package org.silver.shop.dao.system.commerce;


import org.silver.shop.dao.BaseDao;

import com.justep.baas.data.Table;

public interface StockDao  extends BaseDao{
	/**
	 * 查询商户仓库下没用入库的已备案商品信息
	 * @param page
	 * @param size
	 * @param entGoodsNo 商品自编号
	 * @param merchantId 商户id
	 * @param warehouseCode 仓库编码
	 */
	public Table getWarehousGoodsInfo(String merchantId ,String warehouseCode, int page, int size, String entGoodsNo);

	
}

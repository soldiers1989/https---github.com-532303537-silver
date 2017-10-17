package org.silver.shop.api.system.commerce;

import java.util.Map;

public interface StockService {

	/**
	 * 查询已备案的备案商品详情
	 * @param merchantId
	 * @param warehouseCode 
	 * @param page
	 * @param size
	 * @return
	 */
	public Map<String,Object> searchAlreadyRecordGoodsDetails(String merchantId, String warehouseCode, int page, int size);

	
	/**
	 * 添加库存数量
	 * @param merchantId
	 * @param merchantName
	 * @param warehousCode
	 * @param warehousName
	 * @param goodsId
	 */
	public Map<String,Object> addGoodsStockCount(String merchantId, String merchantName, String warehousCode, String warehousName,
			String goodsInfoPack);

}

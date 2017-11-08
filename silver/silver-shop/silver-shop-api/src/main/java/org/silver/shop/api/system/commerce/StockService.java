package org.silver.shop.api.system.commerce;

import java.util.Map;

public interface StockService {

	/**
	 * 查询已备案的备案商品详情
	 * @param merchantId 商户ID
	 * @param warehouseCode 仓库编码
	 * @param page 页面
	 * @param size 数目
	 * @return
	 */
	public Map<String,Object> searchAlreadyRecordGoodsDetails(String merchantId, String warehouseCode, int page, int size);

	
	/**
	 * 添加库存数量
	 * @param merchantId 商户ID
	 * @param merchantName 商户名称
	 * @param warehousCode 仓库编号
	 * @param warehousName 仓库名称
	 * @param goodsId 商品ID
	 */
	public Map<String,Object> addGoodsStockCount(String merchantId, String merchantName, String warehouseCode, String warehousName,
			String goodsInfoPack);


	/**
	 * 添加商品上架数量
	 * @param merchantId 商户ID
	 * @param merchantName 商户名称
	 * @param goodsId 商品ID
	 * @param sellCount 上架数量
	 */
	public Map<String,Object> addGoodsSellCount(String merchantId, String merchantName, String goodsId, int sellCount);


	/**
	 * 商户查询所有商品库存信息
	 * @param merchantId 商户ID
	 * @param merchantName 商户名称
	 * @param page 页数
	 * @param size 数目
	 * @return Map
	 */
	public Map<String, Object> getGoodsStockInfo(String merchantId, String merchantName, int page, int size,String warehouseCode);


	/**
	 * 商户批量与单个商品上/下架状态修改
	 * @param merchantId 商户ID
	 * @param merchantName 商户名称
	 * @param goodsInfoPack 商品信息包
	 * @param type 1-上架,2-下架
	 * @return Map
	 */
	public Map<String, Object> setGoodsSellAndStopSelling(String merchantId, String merchantName, String goodsInfoPack, int type);


	/**
	 * 商户批量与单个商品入库与上架
	 * @param merchantId 商户ID
	 * @param merchantName 商户名称
	 * @param goodsInfoPack 商品信息包
	 * @param type 类型：1-库存,2-上架
	 * @return
	 */
	public Map<String, Object> setGoodsStorageAndSellCount(String merchantId, String merchantName, String goodsInfoPack,
			int type);

}

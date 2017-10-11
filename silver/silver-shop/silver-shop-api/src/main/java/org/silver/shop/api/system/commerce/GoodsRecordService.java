package org.silver.shop.api.system.commerce;

import java.util.List;
import java.util.Map;

public interface GoodsRecordService {
	
	/**
	 * 根据商户名查询所有商品基本信息
	 * @param merchantName 商户名称
	 * @param page 页数
	 * @param size 数据条数
	 * @return List
	 */
	public List findGoodsBaseInfo(String merchantName, int page, int size);

	/**
	 * 根据商户名称及商户ID、商品名称查询商户下商品备案信息,如未备案商品则查询商品基本信息
	 * @param merchantName 商户名称
	 * @param goodsIdPack 商品信息包
	 * @return 
	 */
	public Map<String, Object> getGoodsRecordInfo(String merchantName, String goodsIdPack);

	/**
	 * 
	 * @param merchantName 商户名称
	 * @param merchantId 
	 * @param ciqOrgCode 
	 * @param customsCode 
	 * @param eport 
	 * @param goodsIdPack 商品信息包
	 * @return
	 */
	public Map<String, Object> merchantSendGoodsRecord(String merchantName, String merchantId, String recordGoodsInfoPack, String eport, String customsCode, String ciqOrgCode);
}

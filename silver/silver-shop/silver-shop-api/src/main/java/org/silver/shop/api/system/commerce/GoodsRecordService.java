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
	public List<Object> findGoodsBaseInfo(String merchantName, int page, int size);

	/**
	 * 根据商户名称及商户ID、商品名称查询商户下商品备案信息,如未备案商品则查询商品基本信息
	 * @param merchantName 商户名称
	 * @param goodsIdPack 商品信息包
	 * @return 
	 */
	public Map<String, Object> getGoodsRecordInfo(String merchantName, String goodsIdPack);

	/**
	 * 商户发起商品备案
	 * @param merchantName 商户名称
	 * @param merchantId 商户ID
	 * @param eport 口岸
	 * @param customsCode 检验检疫机构代码
	 * @param ciqOrgCode 主管海关代码
	 * @param recordGoodsInfoPack 备案商品信息
	 * @return Map
	 */
	public Map<String, Object> merchantSendGoodsRecord(String merchantName, String merchantId, String recordGoodsInfoPack, String eport, String customsCode, String ciqOrgCode);
}

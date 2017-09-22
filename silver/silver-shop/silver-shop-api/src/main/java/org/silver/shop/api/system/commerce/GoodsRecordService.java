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
	 * 
	 * @param merchantName
	 * @param goodsIdPack
	 * @return 
	 */
	public Map<String, Object> getGoodsRecordInfo(String merchantName, String goodsIdPack);
	
	
}

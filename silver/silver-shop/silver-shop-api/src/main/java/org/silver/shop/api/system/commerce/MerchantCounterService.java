package org.silver.shop.api.system.commerce;

import java.util.Map;

import org.silver.shop.model.system.organization.Merchant;

public interface MerchantCounterService {

	/**
	 * 商户查看商品专柜信息
	 * @param datasMap 查询参数
	 * @param page 页数 
	 * @param size 数目
	 * @return Map
	 */
	public Map<String, Object> getInfo(Map<String, Object> datasMap, int page, int size);

	/**
	 * 查询专柜下所有商品信息
	 * @param merchantId 商户id
	 * @param datasMap 查询参数
	 * @param page 页数
	 * @param size 数目
	 * @return Map
	 */
	public Map<String,Object> getGoodsInfo(String merchantId, Map<String, Object> datasMap, int page, int size);

	/**
	 * 商户添加专柜信息
	 * @param merchantInfo 商户信息实体类
	 * @param datasMap 专柜信息
	 * @return Map
	 */
	public Map<String, Object> addCounterInfo(Merchant merchantInfo, Map<String, Object> datasMap);

	/**
	 * 商户向专柜添加商品信息
	 * @param merchantInfo 商户信息
	 * @param datasMap 添加信息
	 * @return Map
	 */
	public Map<String, Object> addGoodsInfo(Merchant merchantInfo, Map<String, Object> datasMap);

	/**
	 * 根据专柜id查询专柜信息
	 * @param counterId 专柜id
	 * @param size 数目
	 * @param page 
	 * @return Map
	 */
	public Map<String, Object> counterInfo(String counterId);


}

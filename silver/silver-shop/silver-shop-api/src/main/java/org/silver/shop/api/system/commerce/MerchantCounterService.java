package org.silver.shop.api.system.commerce;

import java.util.List;
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
	 * @param imglist 图片集合
	 * @return Map
	 */
	public Map<String, Object> addCounterInfo(Merchant merchantInfo, Map<String, Object> datasMap, List<Object> imglist);

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

	/**
	 * 设置专柜商品是否允许分销
	 * @param datasMap
	 * @return
	 */
	public Map<String, Object> updatePopularizeFlag(Map<String, Object> datasMap);

	
	/**
	 * 根据商户id查询商户能加入专柜的商品信息
	 * @param merchantId 商户id
	 * @param datasMap 查询参数
	 * @param size 
	 * @param page 
	 * @return Map
	 */
	public Map<String, Object> getEnteringTheCabinetGoods(String merchantId, Map<String, Object> datasMap, int page, int size);


}

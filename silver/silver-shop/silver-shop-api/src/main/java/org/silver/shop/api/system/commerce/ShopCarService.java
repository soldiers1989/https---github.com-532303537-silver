package org.silver.shop.api.system.commerce;

import java.util.Map;

public interface ShopCarService {
	/**
	 * 用户添加商品至购物车
	 * 
	 * @param memberId
	 *            用户ID
	 * @param memberName
	 *            用户名称
	 * @param entGoodsNo
	 *            商品备案Id
	 * @param count
	 *            数量
	 */
	public Map<String, Object> addGoodsToShopCar(String memberId, String memberName, String entGoodsNo, int count);

	/**
	 * 用户查询购物车
	 * 
	 * @param memberId
	 *            用户ID
	 * @param memberName
	 *            用户名称
	 * @return
	 */
	public Map<String, Object> getGoodsToShopCartInfo(String memberId, String memberName);

	/**
	 * 用户删除购物车信息
	 * 
	 * @param entGoodsNo 商品自编号
	 * @param memberId 用户Id
	 * @param memberName 用户名称
	 * @return Map
	 */
	public Map<String, Object> deleteShopCartGoodsInfo(String entGoodsNo, String memberId, String memberName);

	/**
	 * 用户修改购物车选中标识与商品数量
	 * @param memberId
	 * @param memberName
	 * @param goodsInfo
	 * @return
	 */
	public Map<String, Object> editShopCarGoodsInfo(String memberId, String memberName, String goodsInfo);

	/**
	 * 临时接口
	 * @return 
	 */
	public Object temporaryUpdate();

}

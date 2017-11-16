package org.silver.shop.api.system.commerce;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface GoodsContentService {

	/**
	 * 添加商品基本信息
	 * @param date 
	 * @param goodsYear 
	 * @param imgList 
	 * @param params 
	 * @param merchantName 
	 * @param merchantId 
	 * @return
	 */
	public Map<String, Object> addGoodsBaseInfo(String merchantId, String merchantName, Map<String, Object> params, List<Object> imgList, int goodsYear, Date date);

	/**
	 * 创建自编商品ID 获取商品基本信息的自增ID后,不足5位则前面补0
	 * 
	 * @return
	 */
	public Map<String, Object> createGoodsId();

	/**
	 * 商户模糊查询商品基本信息
	 * 
	 * @param goodsId
	 * @param merchantName
	 *            商户名
	 * @param goodsName
	 *            商品名
	 * @param starTime
	 *            开始时间
	 * @param endTime
	 *            结束时间
	 * @param ymYear
	 *            商品年份
	 * @param size
	 * @param page
	 */
	public Map<String, Object> blurryFindGoodsInfo(String goodsId, String merchantName, String goodsName, String startTime,
			String endTime, String ymYear, int page, int size,String merchantId);

	/**
	 * 商户修改商品基本信息
	 * 
	 * @param goodsId 
	 * @param goodsName
	 * @param goodsFirstType
	 * @param goodsSecondType
	 * @param goodsThirdType
	 * @param imgList
	 * @param goodsDetail
	 * @param goodsBrand
	 * @param goodsStyle
	 * @param goodsUnit
	 * @param goodsRegPrice
	 * @param goodsOriginCountry
	 * @param goodsBarCode
	 * @param merchantName
	 * @return
	 */
	public boolean editGoodsBaseInfo(String goodsId, String goodsName, String goodsFirstType, String goodsSecondType,
			String goodsThirdType, List<Object> imgList, String goodsDetail, String goodsBrand, String goodsStyle,
			String goodsUnit, String goodsRegPrice, String goodsOriginCountry, String goodsBarCode,
			String merchantName);

	/**
	 * 删除商品基本信息
	 * 
	 * @param goodsId
	 *            商品ID
	 * @param merchantName
	 *            商户名称
	 * @return boolean
	 */
	public boolean deleteBaseInfo(String merchantName, String goodsId);

	/**
	 * 根据商品ID,查询商户下已备案的商品基本信息
	 * @param merchantId 商户ID
	 * @return 
	 */
	public Map<String,Object> getShowGoodsBaseInfo(int firstType, int  secndType,int thirdType,int page,int size);

	/**
	 * 根据商品ID单独查询商品基本信息
	 * @param goodsId
	 * @return
	 */
	public Map<String, Object> goodsContentService(String goodsId);

	/**
	 * 商城根据商品类型搜索商品
	 * @param firstType
	 * @param secndType
	 * @param thirdType
	 * @param page 
	 * @param size
	 * @return
	 */
	public Map<String, Object> getCategoryGoods(Integer firstType, Integer secndType, Integer thirdType, int page, int size);

	/**
	 * 根据商品名搜索商品
	 * @param goodsName
	 * @param page
	 * @param size
	 * @return
	 */
	public Map<String, Object> searchGoodsInfo(String goodsName, int page, int size);

}

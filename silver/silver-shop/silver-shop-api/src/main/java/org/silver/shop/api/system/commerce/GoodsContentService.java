package org.silver.shop.api.system.commerce;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface GoodsContentService {

	/**
	 * 添加商品基本信息
	 * @param goodsId
	 * @param merchantName
	 * @param goodsName
	 * @param imgList
	 * @param goodsFirstType
	 * @param goodsSecondType
	 * @param goodsThirdType
	 * @param goodsDetail
	 * @param goodsBrand
	 * @param goodsStyle
	 * @param goodsUnit
	 * @param goodsRegPrice
	 * @param goodsOriginCountry
	 * @param goodsBarCode
	 * @param year
	 * @param date
	 * @return
	 */
	public boolean addGoodsBaseInfo(String goodsId, String merchantName, String goodsName, List imgList, String goodsFirstType, String goodsSecondType, String goodsThirdType, String goodsDetail, String goodsBrand, String goodsStyle, String goodsUnit, String goodsRegPrice, String goodsOriginCountry, String goodsBarCode, int year, Date date);

	

	/**
	 * 获取商品基本信息的自增ID后,不足5位则前面补0
	 * 
	 * @return
	 */
	public Map<String, Object> findGoodsId();

}

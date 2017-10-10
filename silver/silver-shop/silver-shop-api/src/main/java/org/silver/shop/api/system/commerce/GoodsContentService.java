package org.silver.shop.api.system.commerce;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface GoodsContentService {

	/**
	 * 添加商品基本信息
	 * 
	 * @param goodsId 商品ID
	 * @param goodsName 商品名称
	 * @param merchantId 商户ID
	 * @param merchantName 商户名称
	 * @param imgList 图片地址List
	 * @param goodsFirstType 商品第一类型ID
	 * @param goodsSecondType 商品第二类型ID
	 * @param goodsThirdType 商品第三类型ID
	 * @param goodsDetail 商品详情
	 * @param goodsBrand 商品品牌
	 * @param goodsStyle 规格
	 * @param goodsUnit 申报计量单位
	 * @param goodsRegPrice 商品单价
	 * @param goodsOriginCountry 原产国
	 * @param goodsBarCode 商品条形码
	 * @param year 年份
	 * @param date 日期
	 * @return
	 */
	public boolean addGoodsBaseInfo(String goodsId, String merchantName, String goodsName, List imgList,
			String goodsFirstType, String goodsSecondType, String goodsThirdType, String goodsDetail, String goodsBrand,
			String goodsStyle, String goodsUnit, String goodsRegPrice, String goodsOriginCountry, String goodsBarCode,
			int year, Date date,String merchantId);

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
	public List<Object> blurryFindGoodsInfo(String goodsId, String merchantName, String goodsName, String startTime,
			String endTime, String ymYear, int page, int size);

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

}

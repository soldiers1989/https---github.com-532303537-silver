package org.silver.shop.api.system.commerce;

import java.util.Map;

public interface GoodsRecordService {

	/**
	 * 根据商户名称及商户ID、商品名称查询商户下商品备案信息,如未备案商品则查询商品基本信息
	 * 
	 * @param merchantName
	 *            商户名称
	 * @param goodsIdPack
	 *            商品信息包
	 * @return
	 */
	public Map<String, Object> getGoodsRecordInfo(String merchantName, String goodsIdPack);

	/**
	 * 商戶发起商品备案
	 * 
	 * @param merchantName
	 *            商户名称
	 * @param merchantId
	 *            商户ID
	 * @param customsPort
	 *            口岸编码
	 * @param customsCode
	 *            海关代码
	 * @param ciqOrgCode
	 *            国检代码
	 * @param recordGoodsInfoPack
	 *            备案商品信息
	 * @return
	 */
	public Map<String, Object> merchantSendGoodsRecord(String merchantName, String merchantId, String customsPort,
			String customsCode, String ciqOrgCode, String recordGoodsInfoPack);

	/**
	 * 查询所有商品备案信息
	 * 
	 * @param goodsId
	 *            商品ID
	 * @param merchantId
	 *            商户ID
	 * @param page
	 *            页数
	 * @param size
	 *            数目
	 * @return
	 */
	public Map<String, Object> findAllGoodsRecordInfo(String merchantId, String goodsId, int page, int size);

	/**
	 * 异步回调,更新商品备案状态
	 * @param datasMap
	 */
	public Map<String,Object> updateGoodsRecordInfo(Map<String, Object> datasMap);

	/**
	 * 
	 * @param merchantId
	 * @param merchantName
	 * @param entGoodsNo
	 * @return
	 */
	public Map<String, Object> getMerchantGoodsRecordDetail(String merchantId, String merchantName, String entGoodsNo);

	/**
	 * 商户修改备案商品中的商品基本信息
	 * @param merchantId 商户Id
	 * @param merchantName 商户名称
	 * @param paramMap 参数
	 * @param type 
	 * @return Map
	 */
	public Map<String, Object> editMerchantRecordGoodsDetailInfo(String merchantId, String merchantName,
			Map<String, Object> paramMap, int type);

	/**
	 * 商户添加已备案商品信息
	 * @param merchantId 商户Id
	 * @param merchantName 商户名称
	 * @param paramMap 参数
	 * @return
	 */
	public Map<String, Object> merchantAddAlreadyRecordGoodsInfo(String merchantId, String merchantName,
			Map<String, Object> paramMap);
}

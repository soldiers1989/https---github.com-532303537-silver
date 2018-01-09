package org.silver.shop.api.system.manual;

import java.util.Map;

import net.sf.json.JSONObject;

public interface MorderService {
	public boolean saveRecord(String merchant_no, String[] head, int body_length, String[][] body);

	public Map<String, Object> pageFindRecords(Map<String, Object> params,int page,int size);

	public Map<String, Object> createNew(String merchant_no, String OrderDate, String order_id, Double FCY, Double Tax,
			Double ActualAmountPaid, String RecipientName, String RecipientID, String RecipientTel,
			String RecipientProvincesCode, String RecipientAddr, String OrderDocAcount, String OrderDocName,
			String OrderDocId, String OrderDocTel, String autoMSHR, String autoMXDR);

	/**
	 * 创建订单商品
	 * 
	 * @param params
	 * @return
	 */
	public Map<String, Object> createNewSub(JSONObject goodsInfo);

	public Map<String, Object> deleteByOrderId(String marchant_no, String order_id);

	/**
	 * 生成国宗物流订单
	 * 
	 * @param senderTel
	 * @param senderAddress
	 * @param senderAreaCode
	 * @param senderCountry
	 * @param senderName
	 * @param provinceCode
	 * @param cityCode
	 * @param areaCode
	 * @param postal 
	 * @param areaName 
	 * @param cityName 
	 * @param provinceName 
	 * @param orderId 
	 * @param goodsInfo 
	 * @param seqNo 
	 * @return
	 */
	public Map<String, Object> guoCreateNew(String merchant_no, String waybill, int serial, String dateSign,
			String OrderDate, Double FCY, Double Tax, Double ActualAmountPaid, String RecipientName, String RecipientID,
			String RecipientTel, String RecipientProvincesCode, String RecipientAddr, String OrderDocAcount,
			String OrderDocName, String OrderDocId, String OrderDocTel, String senderName, String senderCountry,
			String senderAreaCode, String senderAddress, String senderTel, String areaCode, String cityCode,
			String provinceCode, String postal, String provinceName, String cityName, String areaName, String orderId, JSONObject goodsInfo);

	/**
	 * 根据商品编号及名称检查商品是否真实存在
	 * 
	 * @param entGoodsNo
	 *            商品自编号
	 * @param goodsName
	 *            商品名称
	 * @return Map 商品List
	 */
	public Map<String, Object> checkEntGoodsNo(String entGoodsNo, String goodsName);

	/**
	 * 批量导入企邦订单表
	 * 
	 * @param merchantId
	 * @param item
	 * @return
	 */
	public Map<String, Object> createQBOrder(String merchantId, Map<String, Object> item);

	/**
	 * 批量创建企邦订单商品
	 * 
	 * @param merchantId
	 * @param item
	 * @return
	 */
	public Map<String, Object> createQBOrderSub(String merchantId, Map<String, Object> item);
}

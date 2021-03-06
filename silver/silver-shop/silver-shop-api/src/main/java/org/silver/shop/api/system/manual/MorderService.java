package org.silver.shop.api.system.manual;

import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public interface MorderService {
	public boolean saveRecord(String merchant_no, String[] head, int body_length, String[][] body);

	public Map<String, Object> pageFindRecords(Map<String, Object> params, int page, int size);

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

	/**
	 * 删除订单信息
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param orderIdPack
	 *            订单Id(JSON格式)
	 * @return Map
	 */
	public Map<String, Object> deleteByOrderId(String merchantId, String merchantName, String orderIdPack);

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
			String provinceCode, String postal, String provinceName, String cityName, String areaName, String orderId,
			JSONObject goodsInfo);

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
	 *            商户Id
	 * @param item
	 *            订单信息
	 * @param merchantName
	 *            商户名称
	 * @return Map
	 */
	public Map<String, Object> createQBOrder(String merchantId, Map<String, Object> item, String merchantName);

	/**
	 * 批量创建企邦订单商品
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param item
	 *            商品信息
	 * @param merchantName
	 *            商户名称
	 * @return
	 */
	public Map<String, Object> createQBOrderSub(String merchantId, Map<String, Object> item, String merchantName);

	/**
	 * 删除订单关联的商品信息
	 * 
	 * @param id
	 *            操作人Id
	 * @param name
	 *            操作人名称
	 * @param idPack
	 *            Id信息包
	 * @return Map
	 */
	public Map<String, Object> deleteOrderGoodsInfo(String id, String name, String idPack);

	/**
	 * 商户修改手工(导入)的订单
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param strArr
	 *            修改参数
	 * @param flag
	 *            修改标识 1-订单,2-订单商品,3-订单与商品一起修改
	 * @return Map
	 */
	public Map<String, Object> editMorderInfo(String merchantId, String merchantName, String[] strArr, int flag);

	/**
	 * 添加手工订单对应的商品信息
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param strArr
	 *            商品信息
	 * @return Map
	 */
	public Map<String, Object> addOrderGoodsInfo(String merchantId, String merchantName, String[] strArr);

	/**
	 * 临时接口,由于修改之前的手工订单导入的创建人为商户Id,修改为商户名称
	 * 
	 * @return
	 */
	public Map<String, Object> updateOldCreateBy();

	/**
	 * 临时接口,由于修改之前的支付单信息生成时创建人为空,修改为商户名称
	 * 
	 * @return
	 */
	public Map<String, Object> updateOldPaymentCreateBy();

	/**
	 * 商户根据时间段查询已备案成功的订单信息中的商品信息,添加至备案商品信息中
	 * 
	 * @param startTime
	 *            开始时间
	 * @param endTime
	 *            结束时间
	 * @param merchantName
	 *            商户名称
	 * @param merchantId
	 *            商户Id
	 * @param customsMap
	 * @return
	 */
	public Map<String, Object> updateManualOrderGoodsInfo(String startTime, String endTime, String merchantId,
			String merchantName, Map<String, Object> customsMap);

	/**
	 * (预处理)校验国宗物流订单
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
	public Map<String, Object> checkGZOrder(String waybill, int serial, String dateSign, String OrderDate, Double FCY,
			Double Tax, Double ActualAmountPaid, String RecipientName, String RecipientID, String RecipientTel,
			String RecipientProvincesCode, String RecipientAddr, String OrderDocAcount, String OrderDocName,
			String OrderDocId, String OrderDocTel, String senderName, String senderCountry, String senderAreaCode,
			String senderAddress, String senderTel, String areaCode, String cityCode, String provinceCode,
			String postal, String provinceName, String cityName, String areaName, String orderId, JSONObject goodsInfo);

	/**
	 * (预处理)导入企邦订单表
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param item
	 *            订单信息
	 * @param merchantName
	 *            商户名称
	 * @return Map
	 */
	public Map<String, Object> checkQBOrder(String merchantId, Map<String, Object> item, String merchantName);

	/**
	 * 管理员删除手工订单信息
	 * 
	 * @param json
	 * @param managerName
	 * @param managerId
	 * @param note
	 * @param managerName
	 * @param managerId
	 * @return Map
	 */
	public Map<String, Object> managerDeleteMorderDatas(JSONArray json, String note, String managerId,
			String managerName);

	/**
	 * 校验订单人身份证出现次数
	 * 
	 * @param orderDocId
	 *            下单人身份证号码
	 * @return Map
	 */
	public Map<String, Object> checkIdCardCount(String orderDocId);

	/**
	 * 校验收货人电话出现次数
	 * 
	 * @param recipientTel
	 *            收货人电话
	 * @return Map
	 */
	public Map<String, Object> checkRecipientTel(String recipientTel);

	public Object testLogs();

}

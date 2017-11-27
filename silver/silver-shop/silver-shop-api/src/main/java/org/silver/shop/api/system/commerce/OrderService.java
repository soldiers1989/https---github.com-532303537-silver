package org.silver.shop.api.system.commerce;

import java.util.Map;

public interface OrderService {

	/**
	 * 创建订单
	 * @param memberName 用户名
	 * @param memberId 用户ID
	 * @param goodsInfoPack 商品信息包
	 * @param type 类型1-余额支付,2-跳转至银盛
	 * @param totalPrice 总价格
	 * @return
	 */
	public Map<String, Object> createOrderInfo(String memberName, String memberId,String goodsInfoPack,int type,String recipientId);

	/**
	 * 异步回调接口,处理备案网关返回信息
	 * @param datasMap
	 * @return
	 */
	public Map<String, Object> updateOrderRecordInfo(Map<String, Object> datasMap);

	/**
	 * 商户查看备案订单信息
	 * @param merchantId 商户Id
	 * @param merchantName 商户名称
	 * @return 
	 */
	public Map<String, Object> getMerchantOrderRecordInfo(String merchantId, String merchantName,int page,int size);

	/**
	 * 检查订单商品是否都属于一个海关口岸
	 * @param orderGoodsInfoPack
	 * @return
	 */
	public Map<String, Object> checkOrderGoodsCustoms(String orderGoodsInfoPack);
	
	/**
	 * 用户查询订单信息
	 * @param memberId
	 * @param memberName
	 * @return
	 */
	public Map<String, Object> getMemberOrderInfo(String memberId, String memberName,int page,int size);

	/**
	 * 商户查看订单详情
	 * @param merchantId 商户ID
	 * @param merchantName 商户名称
	 * @param entOrderNo 订单id
	 * @return
	 */
	public Map<String, Object> getMerchantOrderDetail(String merchantId, String merchantName, String entOrderNo);

	/**
	 * 用户查看订单详情
	 * @param entOrderNo 
	 * @param memberId
	 * @param memberName
	 * @return
	 */
	public Map<String, Object> getMemberOrderDetail(String memberId, String memberName,String entOrderNo);

	/**
	 * 
	 * @param merchantId
	 * @param merchantName
	 * @param param
	 * @param page
	 * @param size
	 * @return
	 */
	public Map<String, Object> searchMerchantOrderInfo(String merchantId, String merchantName,
			Map<String, Object> datasMap, int page, int size);

}

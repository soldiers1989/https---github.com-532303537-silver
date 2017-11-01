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
	 * 商户查看所有订单信息
	 * @param merchantId 商户Id
	 * @param merchantName 商户名称
	 * @return 
	 */
	public Map<String, Object> getMerchantOrderInfo(String merchantId, String merchantName,int page,int size);

}

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
	public Map<String, Object> createOrderInfo(String memberName, String memberId,String goodsInfoPack,int type,double totalPrice);

	public Map<String, Object> updateOrderRecordInfo(Map<String, Object> datasMap);

}

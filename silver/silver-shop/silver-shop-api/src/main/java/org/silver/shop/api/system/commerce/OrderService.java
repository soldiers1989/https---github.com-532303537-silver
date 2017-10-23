package org.silver.shop.api.system.commerce;

import java.util.Map;

public interface OrderService {

	public Map<String, Object> createOrderInfo(String memberName, String memberId,String goodsInfoPack,int type,double totalPrice);

}

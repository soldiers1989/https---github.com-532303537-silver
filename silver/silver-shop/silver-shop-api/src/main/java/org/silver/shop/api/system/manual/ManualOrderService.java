package org.silver.shop.api.system.manual;

import java.util.List;
import java.util.Map;

import org.silver.shop.model.system.organization.Merchant;

public interface ManualOrderService {

	/**
	 * 更新手工订单信息
	 * 
	 * @param datasMap
	 *            修改参数
	 * @return
	 */
	public Map<String, Object> updateManualOrderInfo(Map<String, Object> datasMap);

	/**
	 * 更新手工订单上坡信息
	 * 
	 * @param merchantInfo
	 *            商户信息实体
	 * @param datasMap
	 *            修改参数
	 * @return Map
	 */
	public Map<String, Object> updateManualOrderGoodsInfo(Merchant merchantInfo, Map<String, Object> datasMap);

	/**
	 * 商户选择对应的订单后，将订单信息推送至关联的物流企业
	 * @param merchantInfo
	 * @param orderList
	 * @return
	 */
	public Map<String, Object> sendMsgToLogistics(Merchant merchantInfo, List<String> orderList);

	/**
	 * 商户根据订单获取订单运单号
	 * @param merchantInfo 商户信息
	 * @param orderId 订单id
	 * @return Map
	 */
	public Map<String, Object> getWaybillNumber(Merchant merchantInfo, String orderId);

}

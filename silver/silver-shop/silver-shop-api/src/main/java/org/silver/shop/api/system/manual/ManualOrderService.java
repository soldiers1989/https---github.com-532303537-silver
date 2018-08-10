package org.silver.shop.api.system.manual;

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

}

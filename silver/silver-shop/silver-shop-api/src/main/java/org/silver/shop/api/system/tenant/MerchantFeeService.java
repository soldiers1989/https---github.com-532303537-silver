package org.silver.shop.api.system.tenant;

import java.util.Map;

public interface MerchantFeeService {

	/**
	 * 管理员添加商户已开通口岸及平台服务费
	 * @param params
	 * @return Map
	 */
	public Map<String,Object> addMerchantServiceFee(Map<String, Object> params);

	/**
	 * 获取商户口岸及服务费信息
	 * @param merchantId 商户Id
	 * @param type 类型：goodsRecord-商品备案、orderRecord-订单备案、paymentRecord-支付单备案
	 * @return Map
	 */
	public Map<String, Object> getMerchantServiceFee(String merchantId, String type);

	/**
	 * 修改商户口岸及服务费信息
	 * @param params 参数
	 * @return Map
	 */
	public Map<String,Object> editMerchantServiceFee(Map<String, Object> params);

	/**
	 * 管理员获取指定商户口岸服务费信息
	 * @param merchantId 商户Id
	 * @return Map
	 */
	public Map<String,Object> getServiceFee(String merchantId);
	
	
}

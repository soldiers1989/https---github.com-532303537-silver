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
	 * @param datasMap 查询参数
	 * @param size 
	 * @param page 
	 * @return Map
	 */
	public Map<String,Object> getServiceFee(Map<String, Object> datasMap, int page, int size);
	
	/**
	 * 根据流水Id查询商户口岸费率信息
	 * 
	 * @param merchantFeeId
	 *            口岸费率流水Id
	 * @return Map
	 */
	public Map<String, Object> getMerchantFeeInfo(String merchantFeeId);

	/**
	 * 根据商户Id，查询商户自助申报订单时进行口岸费率统计总和
	 * @param merchantId 商户Id
	 * @return Map
	 */
	public Map<String, Object> getCustomsFee(String merchantId);
}

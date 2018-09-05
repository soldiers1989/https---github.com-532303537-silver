package org.silver.shop.api.system.cross;

import java.util.Map;


public interface YsPayReceiveService {

	/**
	 * 银盛支付回调,处理支付单,订单的存储与发起
	 * @param datasMap 支付回调信息
	 * @return Map	
	 */
	public Map<String,Object> ysPayReceive(Map<String, Object> datasMap) ;

	/**
	 * 银盟用户钱包支付,处理订单
	 * @param datasMap 支付信息
	 * @return Map
	 */
	public Map<String,Object> balancePayReceive(Map<String,Object> datasMap);
	
	/**
	 * 发起支付单备案
	 * @param merchantId 商户Id 
	 * @param paymentInfoMap 支付单信息
	 * @param tok 
	 * @param recordMap 备案信息 appkey、eport-口岸代码、ciqOrgCode(检验检疫机构代码)、customsCode(海关代码)、opType(报文类型可空)、
	 * @param notifyurl 回调的url
 	 * @return Map
	 */
	public Map<String, Object> sendPayment(String merchantId, Map<String, Object> paymentInfoMap, String tok,
			Map<String, Object> recordMap, String notifyurl);

	/**
	 * 钱包充值成功后回调信息处理
	 * @param datasMap
	 * @return
	 */
	public Map<String, Object> walletRechargeReceive(Map datasMap);

	/**
	 * 管理员针对商户进行资金结算后,银盛代付回调处理
	 * @param params
	 * @return
	 */
	public Map<String, Object> dfReceive(Map params);

	/**
	 * 用户发起提现操作后，银盛回调操作
	 * @param datasMap 
	 * @return
	 */
	public Map<String, Object> memberWithdraw(Map datasMap);

	
}

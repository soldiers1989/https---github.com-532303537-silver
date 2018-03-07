package org.silver.shop.api.system.cross;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

public interface PaymentService {

	public Map<String,Object> updatePaymentStatus(Map<String, Object> datasMap);

	/**
	 * 根据支付单Id发起支付单备案
	 * @param merchantId 商户Id
	 * @param recordMap 备案包
	 * @param tradeNoPack 交易包
	 * @param proxyParentId 代理商Id
	 * @param merchantName 商户名称
	 * @param proxyParentName 
	 * @return 
	 */
	public Object sendMpayByRecord(String merchantId, Map<String,Object> recordMap, String tradeNoPack, String proxyParentId, String merchantName, String proxyParentName);
	
	/**
	 * 异步回调支付单备案信息
	 * @param datasMap
	 * @return
	 */
	public Map<String, Object> updatePayRecordInfo(Map<String, Object> datasMap);

	/**
	 * 获取商户支付单信息
	 * @param merchantId 商户Id
	 * @param merchantName 商户名称
	 * @param params 
	 * @return
	 */
	public Object getMpayRecordInfo(String merchantId, String merchantName, Map<String, Object> params,int page,int size);

	/**
	 * 获取商户支付单报表
	 * @param merchantId 商户Id
	 * @param merchantName 商户名
	 * @param page 页数
	 * @param size 每页数目
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @return Map
	 */
	public Map<String, Object> getMerchantPaymentReport(String merchantId, String merchantName, int page, int size,
			String startDate, String endDate);
	
	/**
	 * 根据订单id 批量生成相应支付单
	 * 
	 * @param merchant_no 商户Id
	 * @param orderIDs 订单Id
	 * @param serialNo 流水号
	 * @param realRowCount 总数
	 * @param errorList 错误集合 
	 * @return Map
	 */
	public Map<String, Object> groupCreateMpay(String merchant_no, List<String> orderIDs, String serialNo,int realRowCount, List<Map<String, Object>> errorList);
	
	/**
	 * 分批启动多线程创建支付单流水
	 * @param orderIdList 订单Id集合
	 * @param merchantId 商户Id
	 * @param merchantName 商户名称
	 * @return Map
	 */
	public Map<String,Object> splitStartPaymentId(List<String> orderIdList, String merchantId, String merchantName);

	/**
	 * 管理员查询支付单报表信息
	 * @param page
	 * @param size
	 * @param startDate
	 * @param endDate
	 * @param merchantName
	 * @return
	 */
	public Map<String, Object> managerGetPaymentReport(int page, int size, String startDate, String endDate,
			 String merchantName);

	/**
	 * 管理员查询所有商户手工支付单信息
	 * @param params 参数
	 * @param page 页数
	 * @param size 数目
	 * @return Map
	 */
	public Map<String, Object> managerGetMpayInfo(Map<String, Object> params, int page, int size);

	/**
	 * 管理员修改商户手工支付单信息
	 * @param json
	 * @return Map
	 */
	public Map<String, Object> managerEditMpayInfo(JSONObject json);
}

package org.silver.shop.api.system.cross;

import java.util.List;
import java.util.Map;

import org.silver.shop.model.system.manual.Mpay;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public interface PaymentService {

	public Map<String, Object> updatePaymentStatus(Map<String, Object> datasMap);

	/**
	 * 根据支付单Id发起支付单备案
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param recordMap
	 *            备案包
	 * @param tradeNoPack
	 *            交易包
	 * @param proxyParentId
	 *            代理商Id
	 * @param merchantName
	 *            商户名称
	 * @param proxyParentName
	 * @return
	 */
	public Object sendMpayByRecord(String merchantId, Map<String, Object> recordMap, String tradeNoPack,
			String proxyParentId, String merchantName, String proxyParentName);

	/**
	 * 异步回调支付单备案信息
	 * 
	 * @param datasMap
	 * @return
	 */
	public Map<String, Object> updatePayRecordInfo(Map<String, Object> datasMap);

	/**
	 * 获取商户支付单信息
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param params
	 * @return
	 */
	public Object getMpayRecordInfo(String merchantId, String merchantName, Map<String, Object> params, int page,
			int size);

	/**
	 * 获取商户支付单报表
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名
	 * @param startDate
	 *            开始时间
	 * @param endDate
	 *            结束时间
	 * @return Map
	 */
	public Map<String, Object> getMerchantPaymentReport(String merchantId, String merchantName, String startDate,
			String endDate);

	/**
	 * 根据订单id 批量生成相应支付单
	 * 
	 * @param orderIDs
	 *            订单Id集合
	 * @param errorList
	 *            错误集合
	 * @param redisMap
	 *            缓存参数
	 * @return Map
	 */
	public Map<String, Object> groupCreateMpay(List<String> orderIDs, List<Map<String, Object>> errorList,
			Map<String, Object> redisMap);

	/**
	 * 分批启动多线程创建支付单流水
	 * 
	 * @param orderIdList
	 *            订单Id集合
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param memberId 
	 * @return Map
	 */
	public Map<String, Object> splitStartPaymentId(List<String> orderIdList, String merchantId, String merchantName, String memberId);

	/**
	 * 管理员查询所有商户手工支付单信息
	 * 
	 * @param params
	 *            参数
	 * @param page
	 *            页数
	 * @param size
	 *            数目
	 * @return Map
	 */
	public Map<String, Object> managerGetMpayInfo(Map<String, Object> params, int page, int size);

	/**
	 * 管理员修改商户手工支付单信息
	 * 
	 * @param datasMap
	 *            参数
	 * @param managerId
	 *            管理员Id
	 * @param managerName
	 *            管理员名称
	 * @return Map
	 */
	public Map<String, Object> managerEditMpayInfo(Map<String, Object> datasMap, String managerId, String managerName);

	/**
	 * 代理商获取所有支付单报表信息
	 * 
	 * @param datasMap
	 * @return Map
	 */
	public Map<String, Object> getAgentPaymentReport(Map<String, Object> datasMap);

	/**
	 * 管理员隐藏(对于商户=删除)手工支付单信息
	 * 
	 * @param jsonArray
	 * @param managerName
	 * @return
	 */
	public Map<String, Object> managerHideMpayInfo(JSONArray jsonArray, String managerName);

	/**
	 * 根据支付流水号 校验订单是否归属同一口岸
	 * @param tradeNos 支付流水号集合 
	 * @param merchantId 商户Id
	 * @return Map
	 */
	public Map<String,Object> checkPaymentPort(List<String> tradeNos, String merchantId);

	/**
	 * 第三方商城平台传递订单信息入口
	 * @param datasMap
	 * @return
	 */
	public Map<String,Object> getThirdPartyInfo(Map<String, Object> datasMap);

	/**
	 * 开始发送手工支付单备案
	 * @param dataList 支付单流水Id集合
	 * @param errorList 错误信息集合
	 * @param customsMap 海关口岸信息
	 * @param paramsMap 缓存参数
	 */
	public void startSendPaymentRecord(JSONArray dataList, List<Map<String, Object>> errorList,
			Map<String, Object> customsMap, Map<String, Object> paramsMap);

	/**
	 * 将支付单返回给第三方电商平台
	 * @param mpay 手工支付单实体类
	 */
	public void rePaymentInfo(Mpay mpay);

	/**
	 * 管理员平台查询支付单报表信息
	 * @param params 查询参数
	 * @return Map
	 */
	public Map<String, Object> managerGetPaymentReportInfo(Map<String, Object> params);

	/**
	 * 管理员查询平台支付单报表详情
	 * @param params 查询参数
	 * @return Map
	 */
	public Map<String, Object> managerGetPaymentReportDetails(Map<String, Object> params);
}

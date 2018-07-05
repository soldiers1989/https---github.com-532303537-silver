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
	public Map<String, Object> splitStartPaymentId(List<String> orderIdList, String merchantId, String merchantName,
			String memberId);

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
	 * 
	 * @param tradeNos
	 *            支付流水号集合
	 * @param merchantId
	 *            商户Id
	 * @return Map
	 */
	public Map<String, Object> checkPaymentPort(List<String> tradeNos, String merchantId);

	/**
	 * 第三方商城平台传递订单信息入口
	 * 
	 * @param datasMap
	 * @return
	 */
	public Map<String, Object> getThirdPartyInfo(Map<String, Object> datasMap);

	/**
	 * 开始发送手工支付单备案
	 * 
	 * @param dataList
	 *            支付单流水Id集合
	 * @param errorList
	 *            错误信息集合
	 * @param customsMap
	 *            海关口岸信息
	 * @param paramsMap
	 *            缓存参数
	 */
	public void startSendPaymentRecord(JSONArray dataList, List<Map<String, Object>> errorList,
			Map<String, Object> customsMap, Map<String, Object> paramsMap);

	/**
	 * 将支付单返回给第三方电商平台
	 * 
	 * @param mpay
	 *            手工支付单实体类
	 */
	public void rePaymentInfo(Mpay mpay);

	/**
	 * 管理员平台查询支付单报表信息
	 * 
	 * @param params
	 *            查询参数
	 * @return Map
	 */
	public Map<String, Object> managerGetPaymentReportInfo(Map<String, Object> params);

	/**
	 * 管理员查询平台支付单报表详情
	 * 
	 * @param params
	 *            查询参数
	 * @return Map
	 */
	public Map<String, Object> managerGetPaymentReportDetails(Map<String, Object> params);

	/**
	 * 保存支付单实体
	 * 
	 * @param paymentMap
	 *            支付单信息集合
	 * @return
	 */
	public boolean addEntity(Map<String, Object> paymentMap);

	/**
	 * 当生成的支付流水号更新到订单表中,并将实名认证状态更新为已实名
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param orderId
	 *            订单Id
	 * @param tradeNo
	 *            支付流水号
	 * @return boolean
	 */
	public boolean updateOrderPayNo(String merchantId, String orderId, String tradeNo);

	/**
	 * 当网关接收支付单信息成功后,同步更新支付单返回信息
	 * 
	 * @param entPayNo
	 *            交易流水号
	 * @param rePayMessageID
	 *            服务端返回的流水Id
	 * @param customsMap
	 *            海关信息，参数为：eport(口岸代码)、ciqOrgCode(国检检疫机构代码)、customsCode(海关代码)
	 * @return Map
	 */
	public Map<String, Object> updatePaymentInfo(String entPayNo, String rePayMessageID,
			Map<String, Object> customsMap);

	/**
	 * 当通用网关接收支付单失败后,同步更新支付单中网络状态
	 * 
	 * @param treadeNo
	 *            支付单流水号
	 * @return Map
	 */
	public Map<String, Object> updatePaymentFailureStatus(String treadeNo);
	/**
	 * 生成支付单时校验订单信息
	 * 
	 * @param infoMap
	 *            参数为：orderId(订单Id)、orderDocId(下单人身份证号码)、orderDocName(下单人姓名)
	 * @return Map
	 */
	public Map<String, Object> checkPaymentInfo(Map<String, Object> infoMap);

	/**
	 * 管理员移除手工支付单至历史记录表中
	 * @param json
	 * @param note
	 * @param managerName
	 * @return
	 */
	public Map<String, Object> managerDeleteMpay(JSONArray json, String note, String managerName);
}

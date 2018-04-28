package org.silver.shop.api.system.commerce;

import java.util.Map;

public interface OrderService {

	/**
	 * 创建订单
	 * 
	 * @param memberName
	 *            用户名
	 * @param memberId
	 *            用户ID
	 * @param goodsInfoPack
	 *            商品信息包
	 * @param type
	 *            类型1-余额支付,2-跳转至银盛
	 * @param totalPrice
	 *            总价格
	 * @return
	 */
	public Map<String, Object> createOrderInfo(String memberName, String memberId, String goodsInfoPack, int type,
			String recipientId);

	/**
	 * 异步回调接口,处理备案网关返回信息
	 * 
	 * @param datasMap
	 * @return
	 */
	public Map<String, Object> updateOrderRecordInfo(Map<String, Object> datasMap);

	/**
	 * 商户查看备案订单信息
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @return
	 */
	public Map<String, Object> getMerchantOrderRecordInfo(String merchantId, String merchantName, int page, int size);

	/**
	 * 检查订单商品是否都属于一个海关口岸
	 * 
	 * @param orderGoodsInfoPack
	 * @return
	 */
	public Map<String, Object> checkOrderGoodsCustoms(String orderGoodsInfoPack);

	/**
	 * 用户查询订单信息
	 * 
	 * @param memberId
	 * @param memberName
	 * @return
	 */
	public Map<String, Object> getMemberOrderInfo(String memberId, String memberName, int page, int size);

	/**
	 * 商户查看订单详情
	 * 
	 * @param merchantId
	 *            商户ID
	 * @param merchantName
	 *            商户名称
	 * @param entOrderNo
	 *            订单id
	 * @return
	 */
	public Map<String, Object> getMerchantOrderDetail(String merchantId, String merchantName, String entOrderNo);

	/**
	 * 用户查看订单详情
	 * 
	 * @param entOrderNo
	 * @param memberId
	 * @param memberName
	 * @return
	 */
	public Map<String, Object> getMemberOrderDetail(String memberId, String memberName, String entOrderNo);

	/**
	 * 根据指定信息搜索商户订单信息
	 * 
	 * @param merchantId
	 * @param merchantName
	 * @param param
	 * @param page
	 * @param size
	 * @return
	 */
	public Map<String, Object> searchMerchantOrderInfo(String merchantId, String merchantName,
			Map<String, Object> datasMap, int page, int size);

	/**
	 * 获取商户每日订单报表
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param page
	 *            页数
	 * @param size
	 *            数目
	 * @param startDate
	 *            开始日期
	 * @param endDate
	 *            结束日期
	 * @return Map
	 */
	public Map<String, Object> getMerchantOrderDailyReport(String merchantId, String merchantName, String startDate,
			String endDate);

	/**
	 * 银盟电子订单下单
	 * 
	 * @param merchantCusNo
	 *            商户号
	 * @param outTradeNo
	 *            商户订单批次号 所属商户下唯一标识
	 * @param amount
	 *            交易金额
	 * @param notifyUrl
	 *            异步通知url
	 * @param extraCommonParam
	 *            额外参数 可空
	 * @param clientSign
	 *            客户端签名
	 * @param timestamp
	 *            时间戳
	 * 
	 * @return 返回下单的必须参数map
	 */
	Map<String, Object> doBusiness(String merchantCusNo, String outTradeNo, String amount, String notifyUrl,
			String extraCommonParam, String clientSign, String timestamp);

	/**
	 * 管理员查询所有手动订单信息
	 * 
	 * @param size
	 * @param page
	 * @param params
	 * @return
	 */
	public Map<String, Object> getManualOrderInfo(Map<String, Object> params, int page, int size);


	/**
	 * 用户删除商城下单信息
	 * 
	 * @param entOrderNo
	 *            订单Id
	 * @param memberName
	 *            用户名称
	 * @return Map
	 */
	public Map<String, Object> memberDeleteOrderInfo(String entOrderNo, String memberName);

	/**
	 * 代理商查询订单报表信息
	 * 
	 * @param datasMap
	 *            参数
	 * @return Map
	 */
	public Map<String, Object> getAgentOrderReport(Map<String, Object> datasMap);

	/**
	 * 第三方商城平台传递订单信息接口
	 * 
	 * @param datasMap
	 *            参数
	 * @return Map
	 */
	public Map<String, Object> thirdPartyBusiness(Map<String, Object> datasMap);

	/**
	 * 管理员获取已移除到历史记录(删除)表中的订单及订单商品信息
	 * 
	 * @param datasMap
	 *            参数
	 * @param page
	 *            页数
	 * @param size
	 *            数目
	 * @return Map
	 */
	public Map<String, Object> getAlreadyDelOrderInfo(Map<String, Object> datasMap, int page, int size);

	/**
	 * 第三方获取订单信息
	 * 
	 * @param datasMap
	 * @return
	 */
	public Object getThirdPartyInfo(Map<String, Object> datasMap);
}

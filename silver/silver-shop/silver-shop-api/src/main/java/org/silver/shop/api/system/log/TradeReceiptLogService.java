package org.silver.shop.api.system.log;

import java.util.Map;

import org.silver.shop.model.system.log.TradeReceiptLog;

public interface TradeReceiptLogService {

	/**
	 * 针对商户发起国的交易，添加交易记录
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param amount
	 *            交易金额
	 * @param orderId
	 *            交易订单Id
	 * @param operator
	 *            操作人
	 * @param type
	 *            类型：recharge(充值)、transfer(转账)、withdraw(提现)
	 * @return Map
	 */
	public Map<String, Object> addMerchantLog(String merchantId, double amount, String orderId, String operator,
			String type);

	/**
	 * 添加交易记录
	 * @param datasMap
	 * <li>userId(用户id)</li>
	 * <li>userName(用户名称)</li>
	 * <li>orderId(订单Id)</li>
	 * <li>type( 类型：recharge-充值、transfer-转账、withdraw-提现)</li>
	 * <li>status(状态：success-交易成功、failure-交易失败、process-处理中)</li>
	 * <li>amount(金额)</li>
	 * <li>sourceType(金额来源)</li>
	 * @return Map
	 */
	public Map<String, Object> addLog(Map<String, Object> datasMap);

	/**
	 * 
	 * @param entity
	 * @return
	 */
	public Map<String, Object> updateLog(TradeReceiptLog entity);

	/**
	 * 查询交易日志记录
	 * @param datasMap 查询参数
	 * @param page 
	 * @param size 
	 * @return
	 */
	public Map<String, Object> getInfo(Map<String, Object> datasMap, int page, int size);

}

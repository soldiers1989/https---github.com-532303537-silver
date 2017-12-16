package org.silver.shop.api.system.manual;

import java.util.Map;

public interface YMorderService {
	/**
	 * 银盟电子订单下单
	 * @param merchant_no 商户号
	 * @param out_trade_no 商户订单批次号 所属商户下唯一标识
	 * @param amount  交易金额
	 * @param notify_url 异步通知url
	 * @param extra_common_param 额外参数  可空
	 * @param client_sign  客户端签名
	 * @param timestamp  时间戳
	 * 
	 * @return 返回下单的必须参数map
	 */
	Map<String, Object> doBusiness(String merchant_no, String out_trade_no, String amount, String notify_url,
			String extra_common_param, String client_sign, String timestamp);

	/**
	 * 订单支付结果处理
	 * @param order_id订单号
	 * @param trade_no交易流水
	 * @param trade_status 状态 （TRADE_SUCCESS  成功）
	 * @return
	 */
	Map<String, Object> doCallBack(String order_id, String trade_no, String trade_status);
}

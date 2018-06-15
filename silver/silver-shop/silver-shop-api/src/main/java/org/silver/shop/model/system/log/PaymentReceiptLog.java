package org.silver.shop.model.system.log;

import java.util.Date;

/**
 *	记录所有钱包充值、提现、转账、等交易记录 
 */
public class PaymentReceiptLog {
	
	private long id;
	private String userId;//用户Id
	private String userName;//用户名称
	private String orderId;//交易订单Id
	private String tradeNo;//交易流水
	private double beforeChangingBalance;// 变更之前余额
	private double amount;// 金额
	private double afterChangeBalance;// 变更之后余额
	private String type;//类型：recharge(充值)、transfer(转账)、withdraw(提现)
	private String tradingStatus;//状态：success(交易成功)、failure(交易失败)、process(处理中)
	private String networkStatus;//网络状态：success(接收成功)、failure(接收失败)
	private String remark;//
	private String createBy;// 创建人
	private Date createDate;// 创建日期
	private String updateBy;// 创建人
	private Date updateDate;// 创建日期
}

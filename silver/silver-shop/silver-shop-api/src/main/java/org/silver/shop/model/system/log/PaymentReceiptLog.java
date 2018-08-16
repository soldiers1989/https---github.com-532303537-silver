package org.silver.shop.model.system.log;

import java.io.Serializable;
import java.util.Date;

/**
 * 记录所有钱包充值、提现、转账、等交易记录
 */
public class PaymentReceiptLog implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6645278599739724815L;
	private long id;
	private String userId;// 用户Id
	private String userName;// 用户名称
	private String orderId;// 交易订单Id
	private String tradeNo;// 交易流水
	private double beforeChangingBalance;// 变更之前余额
	private double amount;// 金额
	private double afterChangeBalance;// 变更之后余额
	private String type;// 类型：recharge(充值)、transfer(转账)、withdraw(提现)
	private String tradingStatus;// 交易状态：success(交易成功)、failure(交易失败)、process(处理中)
	private Date notifyTime;// 银盛支付回调通知时间
	private String remark;//
	private String createBy;// 创建人
	private Date createDate;// 创建日期
	private String updateBy;// 更新人
	private Date updateDate;// 更新日期

	public long getId() {
		return id;
	}

	public String getUserId() {
		return userId;
	}

	public String getUserName() {
		return userName;
	}

	public String getOrderId() {
		return orderId;
	}

	public String getTradeNo() {
		return tradeNo;
	}

	public double getBeforeChangingBalance() {
		return beforeChangingBalance;
	}

	public double getAmount() {
		return amount;
	}

	public double getAfterChangeBalance() {
		return afterChangeBalance;
	}

	public String getType() {
		return type;
	}

	public String getTradingStatus() {
		return tradingStatus;
	}

	public String getRemark() {
		return remark;
	}

	public String getCreateBy() {
		return createBy;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public String getUpdateBy() {
		return updateBy;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}

	public void setBeforeChangingBalance(double beforeChangingBalance) {
		this.beforeChangingBalance = beforeChangingBalance;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public void setAfterChangeBalance(double afterChangeBalance) {
		this.afterChangeBalance = afterChangeBalance;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setTradingStatus(String tradingStatus) {
		this.tradingStatus = tradingStatus;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public Date getNotifyTime() {
		return notifyTime;
	}

	public void setNotifyTime(Date notifyTime) {
		this.notifyTime = notifyTime;
	}

}

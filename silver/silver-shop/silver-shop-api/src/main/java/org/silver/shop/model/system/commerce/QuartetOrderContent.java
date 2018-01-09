package org.silver.shop.model.system.commerce;

import java.io.Serializable;
import java.util.Date;

/**
 *  第四方订单(接入信息)实体类
 *
 */
public class QuartetOrderContent implements Serializable{
	
	private long id;//
	private String orderId;// 商户订单批次号 （所属商户下的唯一标识 最大30位长度）
	private String merchantCusNo;//商户第三方自编号
	private int type;// 订单类型
	private String content;// 订单描述
	private String tradeNo;// 交易流水号 (支付成功后有值)
	private double amount;// 订单金额
	private int orderStatus;// 订单状态 0生成 1已付款 2支付失败 3订单已过期
	private String extraCommonParam;// 补充字段 (可空)
	private String notifyUrl;// 异步通知URL
	private int delFlag;// 0正常 1删除
	private Date delDate;//删除日期
	private String delBy;// 删除人
	private Date createDate;
	private String createBy;
	private Date updateDate;
	private String updateBy;
	private String remarks;
	public long getId() {
		return id;
	}
	public String getOrderId() {
		return orderId;
	}
	public String getMerchantCusNo() {
		return merchantCusNo;
	}
	public int getType() {
		return type;
	}
	public String getContent() {
		return content;
	}
	public String getTradeNo() {
		return tradeNo;
	}
	public double getAmount() {
		return amount;
	}
	public int getOrderStatus() {
		return orderStatus;
	}
	public String getExtraCommonParam() {
		return extraCommonParam;
	}
	public String getNotifyUrl() {
		return notifyUrl;
	}
	public int getDelFlag() {
		return delFlag;
	}
	public Date getDelDate() {
		return delDate;
	}
	public String getDelBy() {
		return delBy;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public String getCreateBy() {
		return createBy;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public String getUpdateBy() {
		return updateBy;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setId(long id) {
		this.id = id;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public void setMerchantCusNo(String merchantCusNo) {
		this.merchantCusNo = merchantCusNo;
	}
	public void setType(int type) {
		this.type = type;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public void setOrderStatus(int orderStatus) {
		this.orderStatus = orderStatus;
	}
	public void setExtraCommonParam(String extraCommonParam) {
		this.extraCommonParam = extraCommonParam;
	}
	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}
	public void setDelFlag(int delFlag) {
		this.delFlag = delFlag;
	}
	public void setDelDate(Date delDate) {
		this.delDate = delDate;
	}
	public void setDelBy(String delBy) {
		this.delBy = delBy;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	
	
}

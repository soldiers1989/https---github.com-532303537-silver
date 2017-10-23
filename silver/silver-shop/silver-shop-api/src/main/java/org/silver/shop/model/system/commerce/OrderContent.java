package org.silver.shop.model.system.commerce;

import java.io.Serializable;
import java.util.Date;

/**
 * 商城自用基本订单信息 
 */
public class OrderContent implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2524545940525643500L;
	private long id;
	private String merchantId;//商户ID
	private String merchantName;//商户名称
	private String memberId;//用户ID
	private String memberName;//用户名称
	private String orderId;//订单ID
	private String goodsId;//商品ID
	private String goodsName;//商品名称
	private int count;//数量
	private double regPrice;//单价
	private double totalPrice;//商品总价
	private double freight;//运费
	private double consolidatedTax;//综合税
	private double orderTotalPrice;//订单总金额 
	private int status;//订单状态：1-待付款,2-已付款;3-商户待处理;4-订单超时;
	private String createBy;// 创建人
	private Date createDate;// 创建时间
	private String updateBy;// 更新人
	private Date updateDate;// 更新时间
	private int deleteFlag;// 删除标识:0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除时间
	
	private String receiptId;//收货人地址ID
	private String reMark;//
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public String getMemberId() {
		return memberId;
	}
	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}
	public String getMemberName() {
		return memberName;
	}
	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getGoodsId() {
		return goodsId;
	}
	public void setGoodsId(String goodsId) {
		this.goodsId = goodsId;
	}
	public String getGoodsName() {
		return goodsName;
	}
	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}
	
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public double getRegPrice() {
		return regPrice;
	}
	public void setRegPrice(double regPrice) {
		this.regPrice = regPrice;
	}
	public double getTotalPrice() {
		return totalPrice;
	}
	public void setTotalPrice(double totalPrice) {
		this.totalPrice = totalPrice;
	}
	public double getFreight() {
		return freight;
	}
	public void setFreight(double freight) {
		this.freight = freight;
	}
	public double getConsolidatedTax() {
		return consolidatedTax;
	}
	public void setConsolidatedTax(double consolidatedTax) {
		this.consolidatedTax = consolidatedTax;
	}
	public double getOrderTotalPrice() {
		return orderTotalPrice;
	}
	public void setOrderTotalPrice(double orderTotalPrice) {
		this.orderTotalPrice = orderTotalPrice;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getCreateBy() {
		return createBy;
	}
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public String getUpdateBy() {
		return updateBy;
	}
	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	public int getDeleteFlag() {
		return deleteFlag;
	}
	public void setDeleteFlag(int deleteFlag) {
		this.deleteFlag = deleteFlag;
	}
	public String getDeleteBy() {
		return deleteBy;
	}
	public void setDeleteBy(String deleteBy) {
		this.deleteBy = deleteBy;
	}
	public Date getDeleteDate() {
		return deleteDate;
	}
	public void setDeleteDate(Date deleteDate) {
		this.deleteDate = deleteDate;
	}
	public String getReceiptId() {
		return receiptId;
	}
	public void setReceiptId(String receiptId) {
		this.receiptId = receiptId;
	}
	public String getReMark() {
		return reMark;
	}
	public void setReMark(String reMark) {
		this.reMark = reMark;
	}
	
	
}

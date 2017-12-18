package org.silver.shop.model.system.tenant;

import java.io.Serializable;
import java.util.Date;

/**
 * 记录商户钱包信息
 *
 */
public class MerchantWalletLog implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3923473504903642825L;
	private long id ; 
	private String entPayNo;// 支付交易编号
	private String entPayName;// 支付交易名称
	private String entOrderNo;//交易订单编号
	private String memberId;//用户Id
	private String memberName;//用户名称
	private String merchantId;// 商户Id
	private String merchantName;// 商户名称
	private double beforeChangingBalance;//变更之前钱包余额
	private double afterChangeBalance;//变更之后钱包余额
	private double payAmount;//交易金额
	private int type; //分类1-购物、2-充值、3-提现、4-缴费、5-支付代理商佣金
	private String note; //操作说明
	private String createBy;//创建人
	private Date createDate;// 创建日期
	private int status;//状态：1-交易成功、2-交易失败、3-交易关闭
	
	private String proxyId ; //代理商Id
	private String proxyName ; //代理商Id
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getEntPayNo() {
		return entPayNo;
	}
	public void setEntPayNo(String entPayNo) {
		this.entPayNo = entPayNo;
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
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public String getEntPayName() {
		return entPayName;
	}
	public void setEntPayName(String entPayName) {
		this.entPayName = entPayName;
	}
	public String getCreateBy() {
		return createBy;
	}
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public double getBeforeChangingBalance() {
		return beforeChangingBalance;
	}
	public void setBeforeChangingBalance(double beforeChangingBalance) {
		this.beforeChangingBalance = beforeChangingBalance;
	}
	public double getPayAmount() {
		return payAmount;
	}
	public void setPayAmount(double payAmount) {
		this.payAmount = payAmount;
	}
	public double getAfterChangeBalance() {
		return afterChangeBalance;
	}
	public void setAfterChangeBalance(double afterChangeBalance) {
		this.afterChangeBalance = afterChangeBalance;
	}
	public String getEntOrderNo() {
		return entOrderNo;
	}
	public void setEntOrderNo(String entOrderNo) {
		this.entOrderNo = entOrderNo;
	}
	public String getProxyId() {
		return proxyId;
	}
	public String getProxyName() {
		return proxyName;
	}
	public void setProxyId(String proxyId) {
		this.proxyId = proxyId;
	}
	public void setProxyName(String proxyName) {
		this.proxyName = proxyName;
	}
	
}

package org.silver.shop.model.system.tenant;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 *
 */
public class ProxyWalletLog implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3923473504903642825L;
	private long id ; 
	private String proxyId ; //代理商Id
	private String proxyName ; //代理商Id
	private String merchantId;// 商户Id
	private String merchantName;// 商户名称
	private String entPayNo;// 支付交易编号
	private String serialName;// 交易名称
	private String entOrderNo;//交易订单编号
	private double beforeChangingBalance;//变更之前钱包余额
	private double afterChangeBalance;//变更之后钱包余额
	private double amount;//金额
	private int type; //分类1-佣金、2-充值、3-提现、4-缴费
	private String note; //操作说明
	private String createBy;//创建人
	private Date createDate;// 创建日期
	private int status;//状态：1-交易成功、2-交易失败、3-交易关闭
	public long getId() {
		return id;
	}
	public String getProxyId() {
		return proxyId;
	}
	public String getProxyName() {
		return proxyName;
	}
	public String getEntPayNo() {
		return entPayNo;
	}
	public String getSerialName() {
		return serialName;
	}
	public String getEntOrderNo() {
		return entOrderNo;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public double getBeforeChangingBalance() {
		return beforeChangingBalance;
	}
	public double getAfterChangeBalance() {
		return afterChangeBalance;
	}
	public double getAmount() {
		return amount;
	}
	public int getType() {
		return type;
	}
	public String getNote() {
		return note;
	}
	public String getCreateBy() {
		return createBy;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public int getStatus() {
		return status;
	}
	public void setId(long id) {
		this.id = id;
	}
	public void setProxyId(String proxyId) {
		this.proxyId = proxyId;
	}
	public void setProxyName(String proxyName) {
		this.proxyName = proxyName;
	}
	public void setEntPayNo(String entPayNo) {
		this.entPayNo = entPayNo;
	}
	public void setSerialName(String serialName) {
		this.serialName = serialName;
	}
	public void setEntOrderNo(String entOrderNo) {
		this.entOrderNo = entOrderNo;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public void setBeforeChangingBalance(double beforeChangingBalance) {
		this.beforeChangingBalance = beforeChangingBalance;
	}
	public void setAfterChangeBalance(double afterChangeBalance) {
		this.afterChangeBalance = afterChangeBalance;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public void setType(int type) {
		this.type = type;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
	
}

package org.silver.sys.model.pures;

import java.util.Date;

public class PuresRecord {

	private static final long serialVersionUID = 1L;
	private long id;
	private String walletNo;   //原始报文编号
	private String tenantNo;   //相关的商户编号
	private String serialNo;   //流水号
	private String acceptanceNo;//支付企业受理号 
	private double amount;     //总金额
	private double balance;    //可用余额
	private double frozenFund; //冻结金额
	private int type;    //业务类型  1 充值  2 提现
	private double money;//业务金额
	private int delFlag;//0正常   1删除
	private Date createDate; //创建时间
	private String createBy; //创建人
	private Date updateDate; //更新时间
	private String updateBy;//更新人
	private String remarks;//备注
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getWalletNo() {
		return walletNo;
	}
	public void setWalletNo(String walletNo) {
		this.walletNo = walletNo;
	}
	public String getTenantNo() {
		return tenantNo;
	}
	public void setTenantNo(String tenantNo) {
		this.tenantNo = tenantNo;
	}
	public String getSerialNo() {
		return serialNo;
	}
	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}
	public String getAcceptanceNo() {
		return acceptanceNo;
	}
	public void setAcceptanceNo(String acceptanceNo) {
		this.acceptanceNo = acceptanceNo;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public double getBalance() {
		return balance;
	}
	public void setBalance(double balance) {
		this.balance = balance;
	}
	public double getFrozenFund() {
		return frozenFund;
	}
	public void setFrozenFund(double frozenFund) {
		this.frozenFund = frozenFund;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public double getMoney() {
		return money;
	}
	public void setMoney(double money) {
		this.money = money;
	}
	public int getDelFlag() {
		return delFlag;
	}
	public void setDelFlag(int delFlag) {
		this.delFlag = delFlag;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public String getCreateBy() {
		return createBy;
	}
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	public String getUpdateBy() {
		return updateBy;
	}
	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	
}

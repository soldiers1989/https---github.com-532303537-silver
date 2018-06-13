package org.silver.shop.model.system.log;

import java.io.Serializable;
import java.util.Date;

/**
 * 记录用户钱包信息
 *
 */
public class MemberWalletLog implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3923473504903642825L;
	private long id;
	private String memberWalletId;// 钱包Id
	private String memberName;// 钱包归属商户名称
	private String serialNo;// 流水编号
	private String serialName;// 交易名称
	private double beforeChangingBalance;// 变更之前钱包余额
	private double amount;// 金额
	private double afterChangeBalance;// 变更之后钱包余额
	private int type; // 类型:1-佣金、2-充值、3-提现、4-缴费、5-购物
	private String status;// 状态：success-交易成功、fail-交易失败
	private String flag;// 进出帐标识：in-进账,out-出账
	private String note; // 操作说明
	private String targetWalletId;// 目标(来源)钱包Id
	private String targetName;// 目标(来源)名称
	private String createBy;// 创建人
	private Date createDate;// 创建日期
	private String remark;//
	public long getId() {
		return id;
	}
	public String getSerialNo() {
		return serialNo;
	}
	public String getSerialName() {
		return serialName;
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
	public int getType() {
		return type;
	}
	public String getStatus() {
		return status;
	}
	public String getFlag() {
		return flag;
	}
	public String getNote() {
		return note;
	}
	public String getTargetWalletId() {
		return targetWalletId;
	}
	public String getTargetName() {
		return targetName;
	}
	public String getCreateBy() {
		return createBy;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public String getRemark() {
		return remark;
	}
	public void setId(long id) {
		this.id = id;
	}
	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}
	public void setSerialName(String serialName) {
		this.serialName = serialName;
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
	public void setType(int type) {
		this.type = type;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public void setTargetWalletId(String targetWalletId) {
		this.targetWalletId = targetWalletId;
	}
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getMemberWalletId() {
		return memberWalletId;
	}
	public String getMemberName() {
		return memberName;
	}
	public void setMemberWalletId(String memberWalletId) {
		this.memberWalletId = memberWalletId;
	}
	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}
	
	
	
}

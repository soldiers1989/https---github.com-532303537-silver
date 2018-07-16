package org.silver.shop.model.system.tenant;

import java.io.Serializable;
import java.util.Date;

/**
 * 线下钱包充值
 */
public class OfflineRechargeContent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1253632415395223661L;

	private long id;
	private String offlineRechargeId;// 线下充值申请流水id
	private String applicantId;// 申请人id
	private String applicant;// 申请人名称
	private String beneficiaryAccount;// 收款人账号
	private String beneficiaryName;// 收款人名称
	private String beneficiaryBank;// 收款人开户行
	private double remittanceAmount;// 汇款金额
	private String remittanceAccount;// 汇款人账号
	private String remittanceName;// 汇款人名称
	private String remittanceBank;// 汇款人开户行
	private Date remittanceDate;// 汇款时间
	private String remittanceReceipt;// 汇款回执图片
	private String reviewerType;//审核类型：firstTrial-运营初审、financialAudit-财务审核、end-结束
	
	private String createBy;// 创建人
	private Date createDate;// 创建时间
	private String updateBy;// 更新人
	private Date updateDate;// 更新时间
	private int deleteFlag;// 删除标识:0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除时间

	public long getId() {
		return id;
	}

	public String getOfflineRechargeId() {
		return offlineRechargeId;
	}

	public String getBeneficiaryAccount() {
		return beneficiaryAccount;
	}

	public String getBeneficiaryName() {
		return beneficiaryName;
	}

	public String getBeneficiaryBank() {
		return beneficiaryBank;
	}

	public double getRemittanceAmount() {
		return remittanceAmount;
	}

	public String getRemittanceAccount() {
		return remittanceAccount;
	}

	public String getRemittanceName() {
		return remittanceName;
	}

	public String getRemittanceBank() {
		return remittanceBank;
	}

	public Date getRemittanceDate() {
		return remittanceDate;
	}

	public String getRemittanceReceipt() {
		return remittanceReceipt;
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

	public int getDeleteFlag() {
		return deleteFlag;
	}

	public String getDeleteBy() {
		return deleteBy;
	}

	public Date getDeleteDate() {
		return deleteDate;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setOfflineRechargeId(String offlineRechargeId) {
		this.offlineRechargeId = offlineRechargeId;
	}

	public void setBeneficiaryAccount(String beneficiaryAccount) {
		this.beneficiaryAccount = beneficiaryAccount;
	}

	public void setBeneficiaryName(String beneficiaryName) {
		this.beneficiaryName = beneficiaryName;
	}

	public void setBeneficiaryBank(String beneficiaryBank) {
		this.beneficiaryBank = beneficiaryBank;
	}

	public void setRemittanceAmount(double remittanceAmount) {
		this.remittanceAmount = remittanceAmount;
	}

	public void setRemittanceAccount(String remittanceAccount) {
		this.remittanceAccount = remittanceAccount;
	}

	public void setRemittanceName(String remittanceName) {
		this.remittanceName = remittanceName;
	}

	public void setRemittanceBank(String remittanceBank) {
		this.remittanceBank = remittanceBank;
	}

	public void setRemittanceDate(Date remittanceDate) {
		this.remittanceDate = remittanceDate;
	}

	public void setRemittanceReceipt(String remittanceReceipt) {
		this.remittanceReceipt = remittanceReceipt;
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

	public void setDeleteFlag(int deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	public void setDeleteBy(String deleteBy) {
		this.deleteBy = deleteBy;
	}

	public void setDeleteDate(Date deleteDate) {
		this.deleteDate = deleteDate;
	}

	public String getApplicantId() {
		return applicantId;
	}

	public String getApplicant() {
		return applicant;
	}

	public void setApplicantId(String applicantId) {
		this.applicantId = applicantId;
	}

	public void setApplicant(String applicant) {
		this.applicant = applicant;
	}

	public String getReviewerType() {
		return reviewerType;
	}

	public void setReviewerType(String reviewerType) {
		this.reviewerType = reviewerType;
	}

}

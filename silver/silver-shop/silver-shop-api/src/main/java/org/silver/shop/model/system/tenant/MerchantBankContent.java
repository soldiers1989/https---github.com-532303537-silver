package org.silver.shop.model.system.tenant;

import java.io.Serializable;
import java.util.Date;

/**
 * 商户银行信息实体类
 */
public class MerchantBankContent implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long id;
	private String merchantId;// 商户ID
	private String merchantName;//商户名称
	private String bankProvince;// 开户行省份 广东省
	private String bankCity;// 开户行市 广州市
	private String bankName;// 开户行名称
	private String bankAccountNo;// 银行卡号
	private String bankAccountName;// 银行卡账户名称
	private String bankAccountType;// 银行卡账户类型 私人(personal) 对公(corporate)
	private String bankCardType;// 银行卡类别 借记卡(debit) 信用卡(credit) 单位结算卡(unit)
	private int defaultFlag;// 默认表示：1-默认选中,2-备用
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
	public String getMerchantId() {
		return merchantId;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public String getBankProvince() {
		return bankProvince;
	}
	public String getBankCity() {
		return bankCity;
	}
	public String getBankName() {
		return bankName;
	}
	public String getBankAccountName() {
		return bankAccountName;
	}
	public String getBankAccountType() {
		return bankAccountType;
	}
	public String getBankCardType() {
		return bankCardType;
	}
	public int getDefaultFlag() {
		return defaultFlag;
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
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public void setBankProvince(String bankProvince) {
		this.bankProvince = bankProvince;
	}
	public void setBankCity(String bankCity) {
		this.bankCity = bankCity;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	
	public void setBankAccountName(String bankAccountName) {
		this.bankAccountName = bankAccountName;
	}
	public void setBankAccountType(String bankAccountType) {
		this.bankAccountType = bankAccountType;
	}
	public void setBankCardType(String bankCardType) {
		this.bankCardType = bankCardType;
	}
	public void setDefaultFlag(int defaultFlag) {
		this.defaultFlag = defaultFlag;
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
	public String getBankAccountNo() {
		return bankAccountNo;
	}
	public void setBankAccountNo(String bankAccountNo) {
		this.bankAccountNo = bankAccountNo;
	}
	
	

}

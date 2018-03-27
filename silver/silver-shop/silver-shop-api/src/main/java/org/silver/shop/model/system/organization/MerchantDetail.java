package org.silver.shop.model.system.organization;

import java.io.Serializable;
import java.util.Date;

/**
 * 商户详细资料信息实体类
 */
public class MerchantDetail implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5244929470205954226L;
	private long id ;//
	private String merchantId;// 商户ID
	private String merchantBusinessLicenseLink;// 企业营业执照(图片地址)
	private String merchantCustomsregistrationCodeLink;// 海关注册代码(图片地址)
	private String merchantOrganizationCodeLink;// 组织机构代码(图片地址)
	private String merchantChecktheRegistrationCodeLink;// 报检注册代码(图片地址)
	private String merchantTaxRegistrationCertificateLink;// 税务登记证(图片地址)
	private String merchantSpecificIndustryLicenseLink;// 特定行业经营许可证(图片地址)
	private String merchantCustomsregistrationCode;// 海关注册编码
	private String merchantOrganizationCode;// 组织机构编码
	private String merchantChecktheRegistrationCode;// 报检注册编码
	private String createBy;// 创建人
	private Date createDate;// 创建日期
	private String updateBy;// 更新人
	private Date updateDate;// 更新日期
	private int deleteFlag;// 删除标识:0-未删除,1-已删除
	
	public String getMerchantBusinessLicenseLink() {
		return merchantBusinessLicenseLink;
	}

	public void setMerchantBusinessLicenseLink(String merchantBusinessLicenseLink) {
		this.merchantBusinessLicenseLink = merchantBusinessLicenseLink;
	}

	public String getMerchantCustomsregistrationCodeLink() {
		return merchantCustomsregistrationCodeLink;
	}

	public void setMerchantCustomsregistrationCodeLink(String merchantCustomsregistrationCodeLink) {
		this.merchantCustomsregistrationCodeLink = merchantCustomsregistrationCodeLink;
	}

	public String getMerchantOrganizationCodeLink() {
		return merchantOrganizationCodeLink;
	}

	public void setMerchantOrganizationCodeLink(String merchantOrganizationCodeLink) {
		this.merchantOrganizationCodeLink = merchantOrganizationCodeLink;
	}

	public String getMerchantChecktheRegistrationCodeLink() {
		return merchantChecktheRegistrationCodeLink;
	}

	public void setMerchantChecktheRegistrationCodeLink(String merchantChecktheRegistrationCodeLink) {
		this.merchantChecktheRegistrationCodeLink = merchantChecktheRegistrationCodeLink;
	}

	public String getMerchantTaxRegistrationCertificateLink() {
		return merchantTaxRegistrationCertificateLink;
	}

	public void setMerchantTaxRegistrationCertificateLink(String merchantTaxRegistrationCertificateLink) {
		this.merchantTaxRegistrationCertificateLink = merchantTaxRegistrationCertificateLink;
	}

	public String getMerchantSpecificIndustryLicenseLink() {
		return merchantSpecificIndustryLicenseLink;
	}

	public void setMerchantSpecificIndustryLicenseLink(String merchantSpecificIndustryLicenseLink) {
		this.merchantSpecificIndustryLicenseLink = merchantSpecificIndustryLicenseLink;
	}

	public String getMerchantCustomsregistrationCode() {
		return merchantCustomsregistrationCode;
	}

	public void setMerchantCustomsregistrationCode(String merchantCustomsregistrationCode) {
		this.merchantCustomsregistrationCode = merchantCustomsregistrationCode;
	}

	public String getMerchantOrganizationCode() {
		return merchantOrganizationCode;
	}

	public void setMerchantOrganizationCode(String merchantOrganizationCode) {
		this.merchantOrganizationCode = merchantOrganizationCode;
	}

	public String getMerchantChecktheRegistrationCode() {
		return merchantChecktheRegistrationCode;
	}

	public void setMerchantChecktheRegistrationCode(String merchantChecktheRegistrationCode) {
		this.merchantChecktheRegistrationCode = merchantChecktheRegistrationCode;
	}

	public String getMerchantId() {
		return merchantId;
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

	public void setId(long id) {
		this.id = id;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
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

	public long getId() {
		return id;
	}
	
}

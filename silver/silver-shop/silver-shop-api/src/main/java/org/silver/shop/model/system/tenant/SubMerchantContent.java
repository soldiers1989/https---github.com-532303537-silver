package org.silver.shop.model.system.tenant;

import java.io.Serializable;
import java.util.Date;

/**
 * 商家下的子商户实体,用于存放商城所有商户的子商户信息字典
 *
 */
public class SubMerchantContent implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8363332962759413626L;
	private long id;
	private String merchantId;//商户Id
	private String merchantName;// 商户名称
	private String companyName;//公司名称
	private String customsRecordCode;//(海关备案号)电商企业海关备案号----电子口岸(16位编号)
	private String ciqRecoreCode;//(电商企业国检备案号)南沙智检备案号(BC业务可以不填)
	private String marCode;//(名称来源自启邦)商家平台唯一标识
	private String sku;//(名称来源自启邦)
	
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
	public String getCompanyName() {
		return companyName;
	}
	public String getCustomsRecordCode() {
		return customsRecordCode;
	}
	public String getCiqRecoreCode() {
		return ciqRecoreCode;
	}
	public String getMarCode() {
		return marCode;
	}
	public String getSku() {
		return sku;
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
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public void setCustomsRecordCode(String customsRecordCode) {
		this.customsRecordCode = customsRecordCode;
	}
	public void setCiqRecoreCode(String ciqRecoreCode) {
		this.ciqRecoreCode = ciqRecoreCode;
	}
	public void setMarCode(String marCode) {
		this.marCode = marCode;
	}
	public void setSku(String sku) {
		this.sku = sku;
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
	
	
	
}

package org.silver.shop.model.system.tenant;

import java.io.Serializable;
import java.util.Date;

public class MerchantIdCardCostContent implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6127391163195377830L;
	private long id;
	private String idCardCostNo;//身份证实名费用流水Id
	private String merchantId;// 商户Id
	private String merchantName;// 商户Id
	private double platformCost;// 身份证实名手续费,暂定每笔
	private String idCardVerifySwitch;//商户是否需要身份证认证开关:on-开；off-关
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
	public String getIdCardCostNo() {
		return idCardCostNo;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public double getPlatformCost() {
		return platformCost;
	}
	public String getIdCardVerifySwitch() {
		return idCardVerifySwitch;
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
	public void setIdCardCostNo(String idCardCostNo) {
		this.idCardCostNo = idCardCostNo;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public void setPlatformCost(double platformCost) {
		this.platformCost = platformCost;
	}
	public void setIdCardVerifySwitch(String idCardVerifySwitch) {
		this.idCardVerifySwitch = idCardVerifySwitch;
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

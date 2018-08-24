package org.silver.shop.model.system.tenant;

import java.io.Serializable;
import java.util.Date;

/**
 *	 
 */
public class MerchantBusinessContent implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5799356622634257868L;
	private long id;
	private String merchantId;// 商户ID
	private String merchantName;// 商户名称
	private String businessType;// 支付业务类型:all-全部,online-线上支付、offline-线下支付
	private String pushType;// 推送类型：all-全部推送、orderRecord-订单推送、paymentRecord-支付单推送、goodsRecord-商品备案推送
	private String idCardVerifySwitch;// 实名认证开关:on-开；off-关
	private String thirdPartyReType;//第三方返回类型：all-全部、order-订单返回、payment-支付单返回
	private String createBy;// 创建人
	private Date createDate;// 创建日期
	private String updateBy;// 更新人
	private Date updateDate;// 更新日期
	private int deleteFlag;// 删除标识:0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除日期

	public long getId() {
		return id;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public String getPushType() {
		return pushType;
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

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public void setPushType(String pushType) {
		this.pushType = pushType;
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

	public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}
	public String getThirdPartyReType() {
		return thirdPartyReType;
	}

	public void setThirdPartyReType(String thirdPartyReType) {
		this.thirdPartyReType = thirdPartyReType;
	}
}

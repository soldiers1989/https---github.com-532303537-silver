package org.silver.shop.model.system.organization;

import java.io.Serializable;
import java.util.Date;

/**
 * 商户信息实体类
 */
public class Merchant implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long id;
	private String merchantId;// 商户ID
	private String merchantName;// 商户名称(兼登陆账号)
	private String merchantCusNo;// 商户第三方编号
	private String loginPassword;// 商户登录密码
	private String merchantAvatar;// 商户头像
	private String merchantPhone;// 商户手机号码
	private String merchantQQ;// 商户QQ
	private String merchantEmail;// 商户邮箱
	private String merchantIdCard;// 商户法人证件号
	private String merchantAddress;// 商户地址
	private String merchantTransport;// 商户商品运输方式
	private int merchantLevel;// 商户等级
	private long merchantProfit;// 平台服务费(原分润),按0.0X算
	private String merchantStatus;// 商户状态：1-启用，2-禁用，3-审核
	private String createBy;// 创建人
	private Date createDate;// 创建日期
	private String updateBy;// 更新人
	private Date updateDate;// 更新日期
	private int deleteFlag;// 删除标识:0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除日期
	private int merchantRealName;// 商户实名表示1-未实名,2-已实名
	private String merchantIdCardName; // 商户法人代表名称

	private String agentParentId ; //代理商Id
	private String agentParentName ;// 代理商名称
	private String companyName;//公司名称
	private String marCode;//(名称来源自启邦)商户在银盟商城平台唯一标识
	private String sku;//(名称来源自启邦)
	private int thirdPartyFlag;//第三方标识：1-银盟(银盟商城平台),2-第三方商城平台
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getMerchantAvatar() {
		return merchantAvatar;
	}

	public void setMerchantAvatar(String merchantAvatar) {
		this.merchantAvatar = merchantAvatar;
	}

	public String getMerchantCusNo() {
		return merchantCusNo;
	}

	public void setMerchantCusNo(String merchantCusNo) {
		this.merchantCusNo = merchantCusNo;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public String getLoginPassword() {
		return loginPassword;
	}

	public void setLoginPassword(String loginPassword) {
		this.loginPassword = loginPassword;
	}

	public String getMerchantPhone() {
		return merchantPhone;
	}

	public void setMerchantPhone(String merchantPhone) {
		this.merchantPhone = merchantPhone;
	}

	public String getMerchantQQ() {
		return merchantQQ;
	}

	public void setMerchantQQ(String merchantQQ) {
		this.merchantQQ = merchantQQ;
	}

	public String getMerchantEmail() {
		return merchantEmail;
	}

	public void setMerchantEmail(String merchantEmail) {
		this.merchantEmail = merchantEmail;
	}

	public String getMerchantIdCard() {
		return merchantIdCard;
	}

	public void setMerchantIdCard(String merchantIdCard) {
		this.merchantIdCard = merchantIdCard;
	}


	public String getMerchantAddress() {
		return merchantAddress;
	}

	public void setMerchantAddress(String merchantAddress) {
		this.merchantAddress = merchantAddress;
	}

	public String getMerchantTransport() {
		return merchantTransport;
	}

	public void setMerchantTransport(String merchantTransport) {
		this.merchantTransport = merchantTransport;
	}

	
	public int getMerchantLevel() {
		return merchantLevel;
	}

	public void setMerchantLevel(int merchantLevel) {
		this.merchantLevel = merchantLevel;
	}

	public long getMerchantProfit() {
		return merchantProfit;
	}

	public void setMerchantProfit(long merchantProfit) {
		this.merchantProfit = merchantProfit;
	}

	public String getMerchantStatus() {
		return merchantStatus;
	}

	public void setMerchantStatus(String merchantStatus) {
		this.merchantStatus = merchantStatus;
	}

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getUpdateBy() {
		return updateBy;
	}

	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	

	public int getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(int deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	public String getDeleteBy() {
		return deleteBy;
	}

	public void setDeleteBy(String deleteBy) {
		this.deleteBy = deleteBy;
	}

	public Date getDeleteDate() {
		return deleteDate;
	}

	public void setDeleteDate(Date deleteDate) {
		this.deleteDate = deleteDate;
	}

	public int getMerchantRealName() {
		return merchantRealName;
	}

	public void setMerchantRealName(int merchantRealName) {
		this.merchantRealName = merchantRealName;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getMerchantIdCardName() {
		return merchantIdCardName;
	}

	public void setMerchantIdCardName(String merchantIdCardName) {
		this.merchantIdCardName = merchantIdCardName;
	}

	public String getAgentParentId() {
		return agentParentId;
	}

	public String getAgentParentName() {
		return agentParentName;
	}

	public void setAgentParentId(String agentParentId) {
		this.agentParentId = agentParentId;
	}

	public void setAgentParentName(String agentParentName) {
		this.agentParentName = agentParentName;
	}

	public String getCompanyName() {
		return companyName;
	}

	public String getMarCode() {
		return marCode;
	}

	public String getSku() {
		return sku;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public void setMarCode(String marCode) {
		this.marCode = marCode;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public int getThirdPartyFlag() {
		return thirdPartyFlag;
	}

	public void setThirdPartyFlag(int thirdPartyFlag) {
		this.thirdPartyFlag = thirdPartyFlag;
	}

}

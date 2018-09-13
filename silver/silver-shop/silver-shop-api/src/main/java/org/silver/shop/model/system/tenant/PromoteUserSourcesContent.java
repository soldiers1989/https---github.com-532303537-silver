package org.silver.shop.model.system.tenant;

import java.io.Serializable;
import java.util.Date;

/**
 * vip商城推广用户来源
 */
public class PromoteUserSourcesContent implements Serializable {

	private long id;//
	private String merchantId;// 
	private String merchantName;//
	private String expadndMerchantCode;// 拓展商code
	private String expadndMerchantName;// 拓展商名称
	private String memberId;// 用户ID
	private String memberName;// 用户名称
	private String memberIdCardName;// 用户身份证姓名
	private String memberIdCard;// 用户身份号码
	private String memberTel;// 手机号码
	private Date expiredDate;//过期时间
	
	
	private String createBy;// 创建人
	private Date createDate;// 创建时间
	private String updateBy;// 更新人
	private Date updateDate;// 更新时间
	private int deleteFlag;// 删除标识0-未删除,1-已删除
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
	public String getExpadndMerchantCode() {
		return expadndMerchantCode;
	}
	public String getExpadndMerchantName() {
		return expadndMerchantName;
	}
	public String getMemberId() {
		return memberId;
	}
	public String getMemberName() {
		return memberName;
	}
	public String getMemberIdCardName() {
		return memberIdCardName;
	}
	public String getMemberIdCard() {
		return memberIdCard;
	}
	public String getMemberTel() {
		return memberTel;
	}
	public Date getExpiredDate() {
		return expiredDate;
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
	public void setExpadndMerchantCode(String expadndMerchantCode) {
		this.expadndMerchantCode = expadndMerchantCode;
	}
	public void setExpadndMerchantName(String expadndMerchantName) {
		this.expadndMerchantName = expadndMerchantName;
	}
	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}
	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}
	public void setMemberIdCardName(String memberIdCardName) {
		this.memberIdCardName = memberIdCardName;
	}
	public void setMemberIdCard(String memberIdCard) {
		this.memberIdCard = memberIdCard;
	}
	public void setMemberTel(String memberTel) {
		this.memberTel = memberTel;
	}
	public void setExpiredDate(Date expiredDate) {
		this.expiredDate = expiredDate;
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

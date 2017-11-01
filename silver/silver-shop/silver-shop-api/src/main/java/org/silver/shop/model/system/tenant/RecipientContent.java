package org.silver.shop.model.system.tenant;

import java.io.Serializable;
import java.util.Date;

/**
 * 收货地址信息实体类
 */
public class RecipientContent implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long id;
	private String recipientId;//收货地址ID (RCPT=Recipient缩写)
	private String memberId;//用户ID
	private String memberName;//用户名称
	private String recipientName;//收货人姓名
	private String recipientCardId;//收货人身份证号码
	private String recipientTel;//收货人电话
	private String recipientCountryName;// 收货人所在国-国家名称
	private String recipientCountryCode;// 收货人所在国-国家代码
	private String recProvincesName;//收货人省份名称
	private String recProvincesCode;//收货人省份代码
	private String recCityName;//收货人城市名称
	private String recCityCode;//收货人城市代码
	private String recAreaName;//收货人市区名称
	private String recAreaCode;//收货人城市区代码
	private String recipientAddr;//收货人详细地址
	private  String notes;//备注
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
	public void setId(long id) {
		this.id = id;
	}
	
	public String getMemberId() {
		return memberId;
	}
	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}
	public String getMemberName() {
		return memberName;
	}
	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}
	public String getRecipientName() {
		return recipientName;
	}
	public void setRecipientName(String recipientName) {
		this.recipientName = recipientName;
	}
	public String getRecipientCardId() {
		return recipientCardId;
	}
	public void setRecipientCardId(String recipientCardId) {
		this.recipientCardId = recipientCardId;
	}
	public String getRecipientTel() {
		return recipientTel;
	}
	public void setRecipientTel(String recipientTel) {
		this.recipientTel = recipientTel;
	}
	
	public String getRecProvincesCode() {
		return recProvincesCode;
	}
	public void setRecProvincesCode(String recProvincesCode) {
		this.recProvincesCode = recProvincesCode;
	}
	public String getRecCityCode() {
		return recCityCode;
	}
	public void setRecCityCode(String recCityCode) {
		this.recCityCode = recCityCode;
	}
	
	public String getRecipientAddr() {
		return recipientAddr;
	}
	public void setRecipientAddr(String recipientAddr) {
		this.recipientAddr = recipientAddr;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
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
	public String getRecipientId() {
		return recipientId;
	}
	public void setRecipientId(String recipientId) {
		this.recipientId = recipientId;
	}
	public String getRecipientCountryCode() {
		return recipientCountryCode;
	}
	public void setRecipientCountryCode(String recipientCountryCode) {
		this.recipientCountryCode = recipientCountryCode;
	}
	public String getRecAreaCode() {
		return recAreaCode;
	}
	public void setRecAreaCode(String recAreaCode) {
		this.recAreaCode = recAreaCode;
	}
	public String getRecipientCountryName() {
		return recipientCountryName;
	}
	public void setRecipientCountryName(String recipientCountryName) {
		this.recipientCountryName = recipientCountryName;
	}
	public String getRecProvincesName() {
		return recProvincesName;
	}
	public void setRecProvincesName(String recProvincesName) {
		this.recProvincesName = recProvincesName;
	}
	public String getRecCityName() {
		return recCityName;
	}
	public void setRecCityName(String recCityName) {
		this.recCityName = recCityName;
	}
	public String getRecAreaName() {
		return recAreaName;
	}
	public void setRecAreaName(String recAreaName) {
		this.recAreaName = recAreaName;
	}
	
	
}

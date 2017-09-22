package org.silver.shop.model.system.tenant;

import java.io.Serializable;
import java.util.Date;

/**
 * 收货地址信息实体类
 */
public class ReceiptInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long id;
	private String userId;//用户ID
	private String recipientName;//收货人姓名
	private String recipientCardId;//收货人身份证号码
	private String recipientTel;//收货人电话
	private String recipientCountry;// 收货人所在国-国家代码
	private String recProvincesCode;//收货人省份代码
	private String recCityCode;//收货人城市代码
	private String recArea;//收货人城市区代码
	private String recipientAddr;//收货人详细地址
	private  String notes;//
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
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
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
	public String getRecipientCountry() {
		return recipientCountry;
	}
	public void setRecipientCountry(String recipientCountry) {
		this.recipientCountry = recipientCountry;
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
	public String getRecArea() {
		return recArea;
	}
	public void setRecArea(String recArea) {
		this.recArea = recArea;
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
	
	
}

package org.silver.shop.model.system.log;

import java.io.Serializable;
import java.util.Date;

/**
 * 身份证实名认证记录
 */
public class IdCardCertificationLog implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5519473355522953311L;
	private long id;
	private String orderId;// 订单id
	private String merchantId;// 商户id
	private String merchantName;// 商户名称
	private String name;// 姓名
	private String idNumber;// 身份证号码
	private double fee;// 实名认证费用
	private int tollFlag;// 收费标识:1-收费、2-不收费
	private String note;// 备注说明
	private String createBy;// 创建人
	private Date createDate;// 创建时间
	private String updateBy;// 更新人
	private Date updateDate;// 更新时间
	private int deleteFlag;// 删除标识:0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除时间
	private String remark;//

	public long getId() {
		return id;
	}

	public String getOrderId() {
		return orderId;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public String getName() {
		return name;
	}

	public String getIdNumber() {
		return idNumber;
	}

	public double getFee() {
		return fee;
	}

	public String getNote() {
		return note;
	}

	public String getCreateBy() {
		return createBy;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setIdNumber(String idNumber) {
		this.idNumber = idNumber;
	}

	public void setFee(double fee) {
		this.fee = fee;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public int getTollFlag() {
		return tollFlag;
	}

	public void setTollFlag(int tollFlag) {
		this.tollFlag = tollFlag;
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

	public String getRemark() {
		return remark;
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

	public void setRemark(String remark) {
		this.remark = remark;
	}

}

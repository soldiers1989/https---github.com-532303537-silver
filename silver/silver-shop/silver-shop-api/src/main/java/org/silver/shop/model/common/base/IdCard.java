package org.silver.shop.model.common.base;

import java.io.Serializable;
import java.util.Date;

/**
 * 身份证实体类
 */
public class IdCard implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7378169124374297011L;
	private long id;
	private String certifiedNo;// 认证流水号
	private String merchantId;// 商户Id
	private String merchantName;// 商户名称
	private String name;// 姓名
	private String idNumber;// 身份证号码
	private int type; // 类型：1-未验证,2-手工验证,3-海关认证,4-第三方认证,5-错误
	private String status;// 认证状态:success-成功;failure-失败;wait-待验证
	private String note;// 备注说明
	private String remrak;// 备用字段
	private Date certifiedDate;// 认证成功时间
	private String createBy;//
	private Date createDate;//
	private String updateBy;//
	private Date updateDate;//
	private int deleteFlag;//
	private String deleteBy;//
	private Date deleteDate;//

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getIdNumber() {
		return idNumber;
	}

	public int getType() {
		return type;
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

	public String getDeleteBy() {
		return deleteBy;
	}

	public Date getDeleteDate() {
		return deleteDate;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setIdNumber(String idNumber) {
		this.idNumber = idNumber;
	}

	public void setType(int type) {
		this.type = type;
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

	public void setDeleteBy(String deleteBy) {
		this.deleteBy = deleteBy;
	}

	public void setDeleteDate(Date deleteDate) {
		this.deleteDate = deleteDate;
	}

	public int getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(int deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	public String getCertifiedNo() {
		return certifiedNo;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public String getStatus() {
		return status;
	}

	public String getNote() {
		return note;
	}

	public String getRemrak() {
		return remrak;
	}

	public Date getCertifiedDate() {
		return certifiedDate;
	}

	public void setCertifiedNo(String certifiedNo) {
		this.certifiedNo = certifiedNo;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public void setRemrak(String remrak) {
		this.remrak = remrak;
	}

	public void setCertifiedDate(Date certifiedDate) {
		this.certifiedDate = certifiedDate;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

}

package org.silver.shop.model.system.manual;

import java.io.Serializable;
import java.util.Date;

/**
 * 手工支付单重推记录实体信息类
 */
public class ManualPaymentResendContent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8268179938406418403L;
	private long id;
	private String paymentResendId;//支付单重发唯一Id
	private String merchantId;// 商户Id
	private String merchantName;//商户名称
	private String tradeNo;// 支付流水号
	private String resendStatus;// 重发状态：success-成功，failure-失败
	private int resendCount;// 重发网关次数
	private String note;//
	private Date createDate; // 创建时间
	private String createBy; // 创建人
	private Date updateDate; // 更新时间
	private String updateBy;// 更新人
	private int deleteFlag;// 删除标识:0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除时间
	private String remark;//
	public long getId() {
		return id;
	}
	public String getPaymentResendId() {
		return paymentResendId;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public String getTradeNo() {
		return tradeNo;
	}
	public String getResendStatus() {
		return resendStatus;
	}
	public int getResendCount() {
		return resendCount;
	}
	public String getNote() {
		return note;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public String getCreateBy() {
		return createBy;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public String getUpdateBy() {
		return updateBy;
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
	public void setId(long id) {
		this.id = id;
	}
	public void setPaymentResendId(String paymentResendId) {
		this.paymentResendId = paymentResendId;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}
	public void setResendStatus(String resendStatus) {
		this.resendStatus = resendStatus;
	}
	public void setResendCount(int resendCount) {
		this.resendCount = resendCount;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
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

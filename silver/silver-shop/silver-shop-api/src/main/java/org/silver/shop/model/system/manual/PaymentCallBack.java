package org.silver.shop.model.system.manual;

import java.io.Serializable;
import java.util.Date;

/**
 * 手工支付单第三方回调实体类
 */
public class PaymentCallBack implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -391180442577887112L;
	private long id;
	private String merchantId;// 商户Id
	private String tradeNo;// 交易流水 系统唯一
	private String orderId;// 关联的手工订单 id
	private String thirdPartyId;// 第三方支付单唯一标识
	private String resendStatus;// 重发状态：success-成功，failure-失败
	private int resendCount;// 第三方回调重发次数
	private String remark;// 备注说明字段
	private Date createDate; // 创建时间
	private String createBy; // 创建人
	private Date updateDate; // 更新时间
	private String updateBy;// 更新人
	private int deleteFlag;// 删除标识:0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除时间

	public long getId() {
		return id;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public String getTradeNo() {
		return tradeNo;
	}

	public String getOrderId() {
		return orderId;
	}

	public String getThirdPartyId() {
		return thirdPartyId;
	}

	public String getResendStatus() {
		return resendStatus;
	}

	public int getResendCount() {
		return resendCount;
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

	public void setId(long id) {
		this.id = id;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public void setThirdPartyId(String thirdPartyId) {
		this.thirdPartyId = thirdPartyId;
	}

	public void setResendStatus(String resendStatus) {
		this.resendStatus = resendStatus;
	}

	public void setResendCount(int resendCount) {
		this.resendCount = resendCount;
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

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}

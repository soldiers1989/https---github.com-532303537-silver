package org.silver.shop.model.system.log;

import java.io.Serializable;
import java.util.Date;

/**
 * 记录分账日志
 */
public class FenZhangLog implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7225321552831838321L;
	private long id;
	private String serialNo;// 分账流水号
	private String orderId;// 订单id
	private String tradeNo;// 交易流水
	private String ysPartnerNo;// 银盛分账账号
	private double originalAmount;// 分账前订单原始金额
	private double platformFee;// 平台抽成(服务费)
	private double masterReceiptAmount;//主商户收款金额
	private String masterMerchantId;//主商户id
	private String masterMerchantName;// 主商户名称
	private String divPartnerParams;//推广商参数格式JSON串---参与分账的推广商账号与金额
	private String tradingStatus;// 交易状态：success(交易成功)、failure(交易失败)、process(处理中)
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
	public String getSerialNo() {
		return serialNo;
	}
	public String getOrderId() {
		return orderId;
	}
	public String getTradeNo() {
		return tradeNo;
	}
	public String getYsPartnerNo() {
		return ysPartnerNo;
	}
	public double getOriginalAmount() {
		return originalAmount;
	}
	public double getPlatformFee() {
		return platformFee;
	}
	public double getMasterReceiptAmount() {
		return masterReceiptAmount;
	}
	public String getMasterMerchantId() {
		return masterMerchantId;
	}
	public String getMasterMerchantName() {
		return masterMerchantName;
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
	public void setId(long id) {
		this.id = id;
	}
	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}
	public void setYsPartnerNo(String ysPartnerNo) {
		this.ysPartnerNo = ysPartnerNo;
	}
	public void setOriginalAmount(double originalAmount) {
		this.originalAmount = originalAmount;
	}
	public void setPlatformFee(double platformFee) {
		this.platformFee = platformFee;
	}
	public void setMasterReceiptAmount(double masterReceiptAmount) {
		this.masterReceiptAmount = masterReceiptAmount;
	}
	public void setMasterMerchantId(String masterMerchantId) {
		this.masterMerchantId = masterMerchantId;
	}
	public void setMasterMerchantName(String masterMerchantName) {
		this.masterMerchantName = masterMerchantName;
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
	public String getDivPartnerParams() {
		return divPartnerParams;
	}
	public void setDivPartnerParams(String divPartnerParams) {
		this.divPartnerParams = divPartnerParams;
	}
	public String getTradingStatus() {
		return tradingStatus;
	}
	public void setTradingStatus(String tradingStatus) {
		this.tradingStatus = tradingStatus;
	}
	
	
	
}

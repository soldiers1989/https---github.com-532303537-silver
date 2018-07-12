package org.silver.shop.model.system.log;

import java.io.Serializable;
import java.util.Date;

/**
 *	库存审核日志实体类 
 */
public class StockReviewLog implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8914234646104037976L;
	private long id;
	private String entGoodsNo;//商品自编号
	private String merchantId;//商户id
	private String merchantName;//商户名称
	private String reviewerId;//审核人id
	private String reviewerName;//审核人名称
	private String operationName;//操作名称
	private int reviewerFlag;//审核标识：1-待审核，2-审核通过；3-审核不通过
	private Date createDate;//创建时间
	private Date reviewDate;//审核时间
	private String note;//说明
	private String remark;//备用
	public long getId() {
		return id;
	}
	public String getEntGoodsNo() {
		return entGoodsNo;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public String getReviewerId() {
		return reviewerId;
	}
	public String getReviewerName() {
		return reviewerName;
	}
	public String getOperationName() {
		return operationName;
	}
	public int getReviewerFlag() {
		return reviewerFlag;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public Date getReviewDate() {
		return reviewDate;
	}
	public String getNote() {
		return note;
	}
	public String getRemark() {
		return remark;
	}
	public void setId(long id) {
		this.id = id;
	}
	public void setEntGoodsNo(String entGoodsNo) {
		this.entGoodsNo = entGoodsNo;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public void setReviewerId(String reviewerId) {
		this.reviewerId = reviewerId;
	}
	public void setReviewerName(String reviewerName) {
		this.reviewerName = reviewerName;
	}
	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}
	public void setReviewerFlag(int reviewerFlag) {
		this.reviewerFlag = reviewerFlag;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public void setReviewDate(Date reviewDate) {
		this.reviewDate = reviewDate;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	
}

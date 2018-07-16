package org.silver.shop.model.system.log;

import java.io.Serializable;
import java.util.Date;

public class OfflineRechargeLog implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7863715186357322159L;
	private long id;//
	private String offlineRechargeId;//线下充值申请流水id
	private String reviewerId;// 审核人id
	private String reviewerName;// 审核人名称
	private String currentNodeName;// 当前节点名称
	private String previousNodeName;// 上一个节点名称
	private int reviewerFlag;// 审核标识：1-待审核、2-通过、3-不通过
	private Date reviewDate;// 审核时间
	private String note;// 说明
	private String remark;// 备用字段
	private String createBy;// 创建人
	private Date createDate;// 创建时间
	
	public long getId() {
		return id;
	}
	public String getOfflineRechargeId() {
		return offlineRechargeId;
	}
	public String getReviewerId() {
		return reviewerId;
	}
	public String getReviewerName() {
		return reviewerName;
	}
	public String getCurrentNodeName() {
		return currentNodeName;
	}
	public String getPreviousNodeName() {
		return previousNodeName;
	}
	public int getReviewerFlag() {
		return reviewerFlag;
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
	public String getCreateBy() {
		return createBy;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setId(long id) {
		this.id = id;
	}
	public void setOfflineRechargeId(String offlineRechargeId) {
		this.offlineRechargeId = offlineRechargeId;
	}
	public void setReviewerId(String reviewerId) {
		this.reviewerId = reviewerId;
	}
	public void setReviewerName(String reviewerName) {
		this.reviewerName = reviewerName;
	}
	public void setCurrentNodeName(String currentNodeName) {
		this.currentNodeName = currentNodeName;
	}
	public void setPreviousNodeName(String previousNodeName) {
		this.previousNodeName = previousNodeName;
	}
	public void setReviewerFlag(int reviewerFlag) {
		this.reviewerFlag = reviewerFlag;
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
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
}

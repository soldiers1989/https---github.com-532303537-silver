package org.silver.shop.model.system.log;

import java.io.Serializable;
import java.util.Date;

/**
 * 商品审核记录
 */
public class GoodsReviewerLog  implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9006524003035368127L;
	private long id;//
	private String entGoodsNo;//商品自编号
	private String reviewerId;// 审核人id
	private String reviewerName;// 审核人名称	
	private int reviewerFlag;// 审核标识：1-审核中、2-通过、3-不通过
	private Date reviewDate;// 审核时间
	private String note;// 说明
	private String remark;// 备用字段
	private String createBy;// 创建人
	private Date createDate;// 创建时间
	private int deleteFlag;// 删除标识:0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除时间
	public long getId() {
		return id;
	}
	public String getReviewerId() {
		return reviewerId;
	}
	public String getReviewerName() {
		return reviewerName;
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
	public void setReviewerId(String reviewerId) {
		this.reviewerId = reviewerId;
	}
	public void setReviewerName(String reviewerName) {
		this.reviewerName = reviewerName;
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
	public void setDeleteFlag(int deleteFlag) {
		this.deleteFlag = deleteFlag;
	}
	public void setDeleteBy(String deleteBy) {
		this.deleteBy = deleteBy;
	}
	public void setDeleteDate(Date deleteDate) {
		this.deleteDate = deleteDate;
	}
	public String getEntGoodsNo() {
		return entGoodsNo;
	}
	public void setEntGoodsNo(String entGoodsNo) {
		this.entGoodsNo = entGoodsNo;
	}
	
	
}

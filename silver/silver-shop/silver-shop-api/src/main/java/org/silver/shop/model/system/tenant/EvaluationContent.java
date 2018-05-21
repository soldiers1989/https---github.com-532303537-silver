package org.silver.shop.model.system.tenant;

import java.io.Serializable;
import java.util.Date;

/**
 * 商品评价实体类
 */
public class EvaluationContent implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3308263777484318737L;
	private long id;
	private String goodsId;//商品Id
	private String goodsName;//商品名称
	private String memberId;//用户Id
	private String memberName;//用户名称
	private double level;//
	private String content;//内容
	private String createBy;// 创建人
	private Date createDate;// 创建时间
	private String updateBy;// 更新人
	private Date updateDate;// 更新时间
	private int deleteFlag;// 删除标识:0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除时间
	private String reply;//商户回复评论
	private int sensitiveFlag;//是否包含敏感字眼标识:0-未识别,1-不包含,2-包含
	private String merchantId;//商户Id
	private String merchantName;//商户名称
	public long getId() {
		return id;
	}
	public String getGoodsName() {
		return goodsName;
	}
	public String getMemberName() {
		return memberName;
	}
	public String getContent() {
		return content;
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
	public void setId(long id) {
		this.id = id;
	}
	
	public String getGoodsId() {
		return goodsId;
	}
	public void setGoodsId(String goodsId) {
		this.goodsId = goodsId;
	}
	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}
	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}
	public void setContent(String content) {
		this.content = content;
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
	public double getLevel() {
		return level;
	}
	public void setLevel(double level) {
		this.level = level;
	}
	public String getMemberId() {
		return memberId;
	}
	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}
	public String getReply() {
		return reply;
	}
	public int getSensitiveFlag() {
		return sensitiveFlag;
	}
	public void setReply(String reply) {
		this.reply = reply;
	}
	public void setSensitiveFlag(int sensitiveFlag) {
		this.sensitiveFlag = sensitiveFlag;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
}

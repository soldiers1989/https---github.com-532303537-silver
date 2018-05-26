package org.silver.shop.model.system.log;

import java.io.Serializable;
import java.util.Date;

/**
 * 评论日志
 */
public class EvaluationLog implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1546847414211474403L;
	private long id;
	private String goodsId;//商品Id
	private String goodsName;//商品名称
	private String memberId;//用户Id
	private String memberName;//用户名称
	private double level;//
	private String content;//内容
	private String reply;//商户回复评论
	private int sensitiveFlag;//是否包含敏感字眼标识:0-未识别,1-不包含,2-包含
	private String type;//日志类型:1-用户商品评论,2-商户回复评论,3-管理员删除评论信息
	private String merchantId;//商户Id
	private String merchantName;//商户名称
	private String ipAddresses;//ip地址
	private String createBy;// 创建人
	private Date createDate;// 创建时间
	public long getId() {
		return id;
	}
	public String getGoodsId() {
		return goodsId;
	}
	public String getGoodsName() {
		return goodsName;
	}
	public String getMemberId() {
		return memberId;
	}
	public String getMemberName() {
		return memberName;
	}
	public double getLevel() {
		return level;
	}
	public String getContent() {
		return content;
	}
	public String getReply() {
		return reply;
	}
	public int getSensitiveFlag() {
		return sensitiveFlag;
	}
	public String getType() {
		return type;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public String getIpAddresses() {
		return ipAddresses;
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
	public void setGoodsId(String goodsId) {
		this.goodsId = goodsId;
	}
	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}
	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}
	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}
	public void setLevel(double level) {
		this.level = level;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public void setReply(String reply) {
		this.reply = reply;
	}
	public void setSensitiveFlag(int sensitiveFlag) {
		this.sensitiveFlag = sensitiveFlag;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public void setIpAddresses(String ipAddresses) {
		this.ipAddresses = ipAddresses;
	}
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
	
}

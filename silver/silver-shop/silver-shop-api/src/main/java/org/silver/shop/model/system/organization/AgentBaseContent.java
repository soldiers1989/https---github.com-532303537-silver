package org.silver.shop.model.system.organization;

import java.io.Serializable;
import java.util.Date;

/**
 * 代理商基本信息实体类 
 */
public class AgentBaseContent implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8476676838165490614L;
	private long id;
	private String agentId;// 代理商编号
	private String agentName;// 代理商名称
	private String loginPassword;// 代理商登录密码
	private String agentStatus;// 代理商状态：1：注销 2：正常
	private String agentOrg;// 所属机构
	private String agentNature;// 代理商性质
	private String agentLevel;// 代理商等级
	private double goodsRecordCommissionRate;//商品备案佣金率
	private double orderCommissionRate;//订单佣金率
	private double paymentCommissionRate;//支付单佣金率
	private String createBy;// 创建人
	private Date createDate;// 创建时间
	private String updateBy;// 更新人
	private Date updateDate;// 更新时间
	private int deleteFlag;// 删除标识0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除日期
	public long getId() {
		return id;
	}
	public String getAgentName() {
		return agentName;
	}
	public String getLoginPassword() {
		return loginPassword;
	}
	public String getAgentStatus() {
		return agentStatus;
	}
	public String getAgentOrg() {
		return agentOrg;
	}
	public String getAgentNature() {
		return agentNature;
	}
	public String getAgentLevel() {
		return agentLevel;
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
	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}
	public void setLoginPassword(String loginPassword) {
		this.loginPassword = loginPassword;
	}
	public void setAgentStatus(String agentStatus) {
		this.agentStatus = agentStatus;
	}
	public void setAgentOrg(String agentOrg) {
		this.agentOrg = agentOrg;
	}
	public void setAgentNature(String agentNature) {
		this.agentNature = agentNature;
	}
	public void setAgentLevel(String agentLevel) {
		this.agentLevel = agentLevel;
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
	public String getAgentId() {
		return agentId;
	}
	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}
	public double getGoodsRecordCommissionRate() {
		return goodsRecordCommissionRate;
	}
	public double getOrderCommissionRate() {
		return orderCommissionRate;
	}
	public double getPaymentCommissionRate() {
		return paymentCommissionRate;
	}
	public void setGoodsRecordCommissionRate(double goodsRecordCommissionRate) {
		this.goodsRecordCommissionRate = goodsRecordCommissionRate;
	}
	public void setOrderCommissionRate(double orderCommissionRate) {
		this.orderCommissionRate = orderCommissionRate;
	}
	public void setPaymentCommissionRate(double paymentCommissionRate) {
		this.paymentCommissionRate = paymentCommissionRate;
	}
}

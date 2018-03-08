package org.silver.shop.model.system.organization;

import java.util.Date;

/**
 * 代理商详情实体类
 */
public class AgentDetailContent {
	private long id;
	private String agentParentId;//代理商父类关联ID
	private String agentPrivinceCode;//代理商所属省份编码
	private String agentPrivinceName;//代理商所属省份名称
	private String cityCode;//代理商所属城市编码
	private String cityName;//代理商所属城市名称
	private String agentTaxNo;//代理商税务编号
	private String agentIndustryNo;//代理商工商注册号
	private String agentOrgAdd;//代理商注册地址
	private String agentLegalName;//代理商法人代表名称
	private String agentLegalCardType;//代理商法人证件类型
	private String agentLegalCardNo;//代理商法人证件号
	private Date agentAgreementStart;//协议签订日期
	private Date agentAgreementEnd;//协议签订到期日期
	private long agentRegMoney;//注册资本
	private long agentSafeguardMoney;//保障金
	private String agentBussinessAdd;//营业地址
	private String agentLinkPhone;//联系电话
	private String agentAccountCheckName;//对账单收件人
	private String agentAccountCheckAdd;//对账单地址
	private String agentAccountCheckMail;//对账单邮箱
	private String createBy;// 创建人
	private Date createDate;// 创建时间
	private String updateBy;// 更新人
	private Date updateDate;// 更新时间
	public long getId() {
		return id;
	}
	public String getAgentParentId() {
		return agentParentId;
	}
	public String getAgentPrivinceCode() {
		return agentPrivinceCode;
	}
	public String getAgentPrivinceName() {
		return agentPrivinceName;
	}
	public String getCityCode() {
		return cityCode;
	}
	public String getCityName() {
		return cityName;
	}
	public String getAgentTaxNo() {
		return agentTaxNo;
	}
	public String getAgentIndustryNo() {
		return agentIndustryNo;
	}
	public String getAgentOrgAdd() {
		return agentOrgAdd;
	}
	public String getAgentLegalName() {
		return agentLegalName;
	}
	public String getAgentLegalCardType() {
		return agentLegalCardType;
	}
	public String getAgentLegalCardNo() {
		return agentLegalCardNo;
	}
	public Date getAgentAgreementStart() {
		return agentAgreementStart;
	}
	public Date getAgentAgreementEnd() {
		return agentAgreementEnd;
	}
	public long getAgentRegMoney() {
		return agentRegMoney;
	}
	public long getAgentSafeguardMoney() {
		return agentSafeguardMoney;
	}
	public String getAgentBussinessAdd() {
		return agentBussinessAdd;
	}
	public String getAgentLinkPhone() {
		return agentLinkPhone;
	}
	public String getAgentAccountCheckName() {
		return agentAccountCheckName;
	}
	public String getAgentAccountCheckAdd() {
		return agentAccountCheckAdd;
	}
	public String getAgentAccountCheckMail() {
		return agentAccountCheckMail;
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
	public void setId(long id) {
		this.id = id;
	}
	public void setAgentParentId(String agentParentId) {
		this.agentParentId = agentParentId;
	}
	public void setAgentPrivinceCode(String agentPrivinceCode) {
		this.agentPrivinceCode = agentPrivinceCode;
	}
	public void setAgentPrivinceName(String agentPrivinceName) {
		this.agentPrivinceName = agentPrivinceName;
	}
	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public void setAgentTaxNo(String agentTaxNo) {
		this.agentTaxNo = agentTaxNo;
	}
	public void setAgentIndustryNo(String agentIndustryNo) {
		this.agentIndustryNo = agentIndustryNo;
	}
	public void setAgentOrgAdd(String agentOrgAdd) {
		this.agentOrgAdd = agentOrgAdd;
	}
	public void setAgentLegalName(String agentLegalName) {
		this.agentLegalName = agentLegalName;
	}
	public void setAgentLegalCardType(String agentLegalCardType) {
		this.agentLegalCardType = agentLegalCardType;
	}
	public void setAgentLegalCardNo(String agentLegalCardNo) {
		this.agentLegalCardNo = agentLegalCardNo;
	}
	public void setAgentAgreementStart(Date agentAgreementStart) {
		this.agentAgreementStart = agentAgreementStart;
	}
	public void setAgentAgreementEnd(Date agentAgreementEnd) {
		this.agentAgreementEnd = agentAgreementEnd;
	}
	public void setAgentRegMoney(long agentRegMoney) {
		this.agentRegMoney = agentRegMoney;
	}
	public void setAgentSafeguardMoney(long agentSafeguardMoney) {
		this.agentSafeguardMoney = agentSafeguardMoney;
	}
	public void setAgentBussinessAdd(String agentBussinessAdd) {
		this.agentBussinessAdd = agentBussinessAdd;
	}
	public void setAgentLinkPhone(String agentLinkPhone) {
		this.agentLinkPhone = agentLinkPhone;
	}
	public void setAgentAccountCheckName(String agentAccountCheckName) {
		this.agentAccountCheckName = agentAccountCheckName;
	}
	public void setAgentAccountCheckAdd(String agentAccountCheckAdd) {
		this.agentAccountCheckAdd = agentAccountCheckAdd;
	}
	public void setAgentAccountCheckMail(String agentAccountCheckMail) {
		this.agentAccountCheckMail = agentAccountCheckMail;
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
	
	
}

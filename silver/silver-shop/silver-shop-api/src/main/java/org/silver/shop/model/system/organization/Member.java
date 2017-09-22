package org.silver.shop.model.system.organization;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户实体类，消费者
 */
public class Member implements Serializable {
	/**
		 * 
		 */
	private static final long serialVersionUID = 1L;
	private long id;
	private String memberId;//用户ID
	private String memberName;//用户名
	private String loginPass;//用户登录密码
	private String memberTel;//用户手机号码
	private String memberMail;//用户邮箱
	private String memberIdCardName;//用户身份证名
	private String memberIdCard;//用户身份号码
	private int memberStatus;//用户状态1-审核2-启用3-禁用
	private int memberRealName;//用户实名1-未实名,2-已实名
	private String createBy;//创建人
	private Date createDate;//创建时间
	private String updateBy;//更新人
	private Date updateDate;//更新时间
	private int deleteFlag;//删除标识0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除日期
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getMemberId() {
		return memberId;
	}
	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}
	public String getMemberName() {
		return memberName;
	}
	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}
	public String getLoginPass() {
		return loginPass;
	}
	public void setLoginPass(String loginPass) {
		this.loginPass = loginPass;
	}
	public String getMemberTel() {
		return memberTel;
	}
	public void setMemberTel(String memberTel) {
		this.memberTel = memberTel;
	}
	public String getMemberMail() {
		return memberMail;
	}
	public void setMemberMail(String memberMail) {
		this.memberMail = memberMail;
	}
	public String getMemberIdCardName() {
		return memberIdCardName;
	}
	public void setMemberIdCardName(String memberIdCardName) {
		this.memberIdCardName = memberIdCardName;
	}
	public String getMemberIdCard() {
		return memberIdCard;
	}
	public void setMemberIdCard(String memberIdCard) {
		this.memberIdCard = memberIdCard;
	}
	public int getMemberStatus() {
		return memberStatus;
	}
	public void setMemberStatus(int memberStatus) {
		this.memberStatus = memberStatus;
	}
	public int getMemberRealName() {
		return memberRealName;
	}
	public void setMemberRealName(int memberRealName) {
		this.memberRealName = memberRealName;
	}
	public String getCreateBy() {
		return createBy;
	}
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public String getUpdateBy() {
		return updateBy;
	}
	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	
	public int getDeleteFlag() {
		return deleteFlag;
	}
	public void setDeleteFlag(int deleteFlag) {
		this.deleteFlag = deleteFlag;
	}
	public String getDeleteBy() {
		return deleteBy;
	}
	public void setDeleteBy(String deleteBy) {
		this.deleteBy = deleteBy;
	}
	public Date getDeleteDate() {
		return deleteDate;
	}
	public void setDeleteDate(Date deleteDate) {
		this.deleteDate = deleteDate;
	}
	
	
}

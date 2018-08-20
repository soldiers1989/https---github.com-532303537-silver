package org.silver.shop.model.system.organization;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户实体类，消费者
 */
public class Member implements Serializable {

	private static final long serialVersionUID = 1L;
	private long id;
	private String memberId;// 用户ID
	private String memberName;// 用户名称
	private String loginName;// 用于登录名称
	private String nickname;// 昵称
	private String loginPass;// 登录密码
	private String memberTel;// 手机号码
	private String memberMail;// 邮箱
	private String memberIdCardName;// 用户身份证名
	private String memberIdCard;// 用户身份号码
	private int memberStatus;// 状态：1-审核、2-启用、3-禁用
	private int realNameFlag;// 用户实名标识：1-未实名、2-已实名
	private String paymentPassword;// 用户支付密码
	private String remark;//
	private String createBy;// 创建人
	private Date createDate;// 创建时间
	private String updateBy;// 更新人
	private Date updateDate;// 更新时间
	private int deleteFlag;// 删除标识0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除日期
	private int memberFlag;// 会员注册标识：1-真实用户、2-批量用户

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

	public String getPaymentPassword() {
		return paymentPassword;
	}

	public void setPaymentPassword(String paymentPassword) {
		this.paymentPassword = paymentPassword;
	}

	public int getRealNameFlag() {
		return realNameFlag;
	}

	public void setRealNameFlag(int realNameFlag) {
		this.realNameFlag = realNameFlag;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public int getMemberFlag() {
		return memberFlag;
	}

	public void setMemberFlag(int memberFlag) {
		this.memberFlag = memberFlag;
	}

}

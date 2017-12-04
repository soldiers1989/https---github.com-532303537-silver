package org.silver.shop.model.system.tenant;

import java.io.Serializable;
import java.util.Date;


/**
 * 用户钱包实体类 
 */
public class MemberWalletContent implements Serializable{
	public MemberWalletContent(){
		
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 2695074747360406903L;
	private long id;
	private String walletId;//用户钱包Id
	private String memberId;//用户Id
	private String memberName;//用户名称
	private Double balance;//余额
	private String createBy;// 创建人
	private Date createDate;// 创建日期
	private String updateBy;// 更新人
	private Date updateDate;// 更新日期
	
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
	public Double getBalance() {
		return balance;
	}
	public void setBalance(Double balance) {
		this.balance = balance;
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
	public String getWalletId() {
		return walletId;
	}
	public void setWalletId(String walletId) {
		this.walletId = walletId;
	}
	
	//Builder(建造者模式)
	public static class Builder {
		private long id;
		private String walletId = null;// 钱包Id
		private String memberId = null;// 用户Id
		private String memberName = null;// 用户名称
		private double balance = 0.0;// 余额
		private String createBy = null;// 创建人
		private Date createDate = null;// 创建日期
		private String updateBy = null;// 更新人
		private Date updateDate = null;// 更新日期

		public Builder(String walletId) {
			this.walletId = walletId;
		}

		public Builder memberId(String memberId) {
			this.memberId = memberId;
			return this;
		}

		public Builder memberName(String memberName) {
			this.memberName = memberName;
			return this;
		}

		public Builder balance(double balance) {
			this.balance = balance;
			return this;
		}

		public Builder createBy(String createBy) {
			this.createBy = createBy;
			return this;
		}

		public Builder createDate(Date createDate) {
			this.createDate = createDate;
			return this;
		}

		public Builder updateBy(String updateBy) {
			this.updateBy = updateBy;
			return this;
		}

		public Builder updateDate(Date updateDate) {
			this.updateDate = updateDate;
			return this;
		}
		
		public MemberWalletContent build(){
			return new MemberWalletContent(this);
		}
	}

	private MemberWalletContent(Builder b) {
		id = b.id;
		walletId = b.walletId;
		memberId = b.memberId;
		memberName = b.memberName;
		balance = b.balance;
		createBy = b.createBy;
		createDate = b.createDate;
		updateBy = b.updateBy;
		updateDate = b.updateDate;
	}
	
}

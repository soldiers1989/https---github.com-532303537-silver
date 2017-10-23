package org.silver.shop.model.system.tenant;

import java.io.Serializable;

/**
 * 用户钱包实体类 
 */
public class MemberWalletContent implements Serializable{
	private long id;
	private String memberId;
	private String memberName;
	private Double balance;//余额
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
	
	 
	
	
	
}

package org.silver.shop.model.system;

import java.io.Serializable;

public class AuthorityGroup implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5349646896844916822L;

	private Long id;//
	
	private String groupName;// 组 merchant,manager,member
	
	private Long authorityId;//关联权限Id
	
	private String status;//状态1-启用,2-禁用
	
	public Long getId() {
		return id;
	}

	public String getGroupName() {
		return groupName;
	}

	public Long getAuthorityId() {
		return authorityId;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public void setAuthorityId(Long authorityId) {
		this.authorityId = authorityId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}

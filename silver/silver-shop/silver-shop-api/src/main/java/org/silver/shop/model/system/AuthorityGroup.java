package org.silver.shop.model.system;

import java.io.Serializable;

/**
 * 权限关联组实体类
 */
public class AuthorityGroup implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5349646896844916822L;

	private Long id;//
	
	private String groupName;// 组 merchant,manager,member
	
	private String authorityId;//关联权限Id
	
	private String status;//状态1-启用,2-禁用
	
	public Long getId() {
		return id;
	}

	public String getGroupName() {
		return groupName;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAuthorityId() {
		return authorityId;
	}

	public void setAuthorityId(String authorityId) {
		this.authorityId = authorityId;
	}
	
	
}

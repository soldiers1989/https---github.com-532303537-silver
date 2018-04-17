package org.silver.shop.model.system;

import java.io.Serializable;
import java.util.Date;

/**
 * 角色管理表
 */
public class AuthorityRole implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5688859813715680687L;
	// 流水号
	private Long id;
	// 角色Id
	private String roleId;
	// 角色名称
	private String roleName;
	// 对应权限Id
	private Long authorityId;
	// 权限代码
	private String authorityCode;
	private String status;// 状态1-启用(授权),2-禁用()
	private String createBy;// 创建人
	private Date createDate;// 创建日期
	private String updateBy;// 更新人
	private Date updateDate;// 更新日期

	public Long getId() {
		return id;
	}

	public String getRoleId() {
		return roleId;
	}

	public String getRoleName() {
		return roleName;
	}

	public String getStatus() {
		return status;
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

	public void setId(Long id) {
		this.id = id;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public Long getAuthorityId() {
		return authorityId;
	}

	public String getAuthorityCode() {
		return authorityCode;
	}

	public void setAuthorityId(Long authorityId) {
		this.authorityId = authorityId;
	}

	public void setAuthorityCode(String authorityCode) {
		this.authorityCode = authorityCode;
	}

}

package org.silver.shop.model.system;

import java.io.Serializable;
import java.util.Date;

public class MenuRoleInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -24416735647849364L;
	//流水号
	private Long id;
	//角色Id
	private String roleId;
	//角色名称
	private String roleName;
	//角色类别
	private String roleType;
	//对应菜单Id
	private Long menuId;
	
	private String createBy;// 创建人
	private Date createDate;// 创建日期
	private String updateBy;// 更新人
	private Date updateDate;// 更新日期
	private int deleteFlag;//删除标识0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除日期
	public String getRoleId() {
		return roleId;
	}
	public String getRoleName() {
		return roleName;
	}
	public String getRoleType() {
		return roleType;
	}
	public Long getMenuId() {
		return menuId;
	}
	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	public void setRoleType(String roleType) {
		this.roleType = roleType;
	}
	public void setMenuId(Long menuId) {
		this.menuId = menuId;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
}

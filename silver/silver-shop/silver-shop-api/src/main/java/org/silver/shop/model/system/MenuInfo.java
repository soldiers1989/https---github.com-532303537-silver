package org.silver.shop.model.system;

import org.apache.commons.lang.StringUtils;

public class MenuInfo {
	
	private Long id;
	private String menuName; //菜单名称
	private String menuPath; //菜单路径
	private String menuLevel; //菜单等级
	private String menuParent; //父菜单
	private String status;//状态
	private int menuOrder;//
	private String state;
	
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getMenuName() {
		return menuName;
	}
	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}
	public String getMenuPath() {
		return menuPath;
	}
	public void setMenuPath(String menuPath) {
		this.menuPath = menuPath;
	}
	public String getMenuLevel() {
		return menuLevel;
	}
	public void setMenuLevel(String menuLevel) {
		this.menuLevel = menuLevel;
	}
	public String getMenuParent() {
		return StringUtils.isEmpty(menuParent)?"0":menuParent;
	}
	public void setMenuParent(String menuParent) {
		this.menuParent = menuParent;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getMenuOrder() {
		return menuOrder;
	}
	public void setMenuOrder(int menuOrder) {
		this.menuOrder = menuOrder;
	}

}

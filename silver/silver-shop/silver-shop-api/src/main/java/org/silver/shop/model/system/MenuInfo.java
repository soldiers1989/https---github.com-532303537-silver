package org.silver.shop.model.system;

import java.io.Serializable;
import java.util.Date;

/**
 * 商城后台菜单实体类
 */
public class MenuInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 9107594158406088296L;
	private Long id;
	private String menuName; // 菜单名称
	private String menuLevel; // 菜单等级
	private String menuParent; // 父菜单
	private String status;// 状态1-启用,2-禁用
	private String createBy;// 创建人
	private Date createDate;// 创建日期
	private String updateBy;// 更新人
	private Date updateDate;// 更新日期
	private int deleteFlag;//删除标识0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除日期
	
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


	public String getMenuLevel() {
		return menuLevel;
	}

	public void setMenuLevel(String menuLevel) {
		this.menuLevel = menuLevel;
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

	public String getMenuParent() {
		return menuParent;
	}


}

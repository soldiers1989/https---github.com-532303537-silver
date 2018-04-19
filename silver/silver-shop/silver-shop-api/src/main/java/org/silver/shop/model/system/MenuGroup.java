package org.silver.shop.model.system;

import java.io.Serializable;

/**
 * 菜单关联组实体类
 */
public class MenuGroup implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5349646896844916822L;

	private Long id;//
	
	private String groupName;// 组 merchant,manager,member
	
	private Long menuId;//关联菜单Id
	
	private String status;//状态1-启用,2-禁用
}

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
	private String firstName;// 第一级权限名称 order
	private String firstCode;// 第一级权限代码
	private String secondName;// 第二级权限名称
	private String secondCode;// 第二级权代码
	private String thirdName;// 第三级权限名称
	private String thirdCode;// 第三级权限代码
	private String groupName;//分组名称
	private String createBy;// 创建人
	private Date createDate;// 创建日期
	private String updateBy;// 更新人
	private Date updateDate;// 更新日期
	private int deleteFlag;//删除标识0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除日期
	
	
}

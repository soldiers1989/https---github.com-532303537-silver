package org.silver.shop.model.system;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户权限信息实体类
 */
public class AuthorityUser implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5688859813715680687L;
	// 流水号
	private Long id;
	// 用户Id
	private String userId;
	// 用户名称
	private String userName;
	// 对应权限Id
	private Long authorityId;
	// 权限代码
	private String authorityCode;
	private String status;// 状态1-启用(授权),2-禁用
	private String createBy;// 创建人
	private Date createDate;// 创建日期
	private String updateBy;// 更新人
	private Date updateDate;// 更新日期

	public Long getId() {
		return id;
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

	public String getUserId() {
		return userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

}

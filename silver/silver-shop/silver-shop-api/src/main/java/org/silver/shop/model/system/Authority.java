package org.silver.shop.model.system;

import java.io.Serializable;
import java.util.Date;

/**
 * 权限字典实体类
 */
public class Authority implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4373242077661617116L;
	private Long id;
	private String authorityId;//权限流水Id
	private String firstName;// 第一级权限名称 order
	private String firstCode;// 第一级权限代码
	private String secondName;// 第二级权限名称
	private String secondCode;// 第二级权代码
	private String thirdName;// 第三级权限名称
	private String thirdCode;// 第三级权限代码
	private String groupName;//分组名称
	private String status;//状态:1-正常,2-禁用
	private String createBy;// 创建人
	private Date createDate;// 创建日期
	private String updateBy;// 更新人
	private Date updateDate;// 更新日期
	private int deleteFlag;// 删除标识0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除日期
	private String remark;//
	public Long getId() {
		return id;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getSecondName() {
		return secondName;
	}

	public String getThirdName() {
		return thirdName;
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

	public int getDeleteFlag() {
		return deleteFlag;
	}

	public String getDeleteBy() {
		return deleteBy;
	}

	public Date getDeleteDate() {
		return deleteDate;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setSecondName(String secondName) {
		this.secondName = secondName;
	}

	public void setThirdName(String thirdName) {
		this.thirdName = thirdName;
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

	public void setDeleteFlag(int deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	public void setDeleteBy(String deleteBy) {
		this.deleteBy = deleteBy;
	}

	public void setDeleteDate(Date deleteDate) {
		this.deleteDate = deleteDate;
	}

	public String getFirstCode() {
		return firstCode;
	}

	public String getSecondCode() {
		return secondCode;
	}

	public String getThirdCode() {
		return thirdCode;
	}

	public void setFirstCode(String firstCode) {
		this.firstCode = firstCode;
	}

	public void setSecondCode(String secondCode) {
		this.secondCode = secondCode;
	}

	public void setThirdCode(String thirdCode) {
		this.thirdCode = thirdCode;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getStatus() {
		return status;
	}

	public String getRemark() {
		return remark;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getAuthorityId() {
		return authorityId;
	}

	public void setAuthorityId(String authorityId) {
		this.authorityId = authorityId;
	}

}

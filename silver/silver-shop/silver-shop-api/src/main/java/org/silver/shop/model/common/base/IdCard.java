package org.silver.shop.model.common.base;

import java.io.Serializable;
import java.util.Date;

/**
 * 身份证实体类
 */
public class IdCard implements Serializable{
	private long id ;
	private String name;//姓名
	private String idNumber;//身份证号码
	private int type ; //类型：1-未验证,2-手工验证,3-海关认证,4-第三方认证,5-错误
	
	private String createBy;//
	private Date createDate;//
	private String updateBy;//
	private Date updateDate;//
	private int deleteFlag;//
	private String deleteBy;//
	private Date deleteDate;//
	public long getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getIdNumber() {
		return idNumber;
	}
	public int getType() {
		return type;
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
	public String getDeleteBy() {
		return deleteBy;
	}
	public Date getDeleteDate() {
		return deleteDate;
	}
	public void setId(long id) {
		this.id = id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setIdNumber(String idNumber) {
		this.idNumber = idNumber;
	}
	public void setType(int type) {
		this.type = type;
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
	public void setDeleteBy(String deleteBy) {
		this.deleteBy = deleteBy;
	}
	public void setDeleteDate(Date deleteDate) {
		this.deleteDate = deleteDate;
	}
	public int getDeleteFlag() {
		return deleteFlag;
	}
	public void setDeleteFlag(int deleteFlag) {
		this.deleteFlag = deleteFlag;
	}
	
}

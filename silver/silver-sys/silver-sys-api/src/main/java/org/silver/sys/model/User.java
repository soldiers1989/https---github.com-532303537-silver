package org.silver.sys.model;

import java.io.Serializable;
import java.util.Date;
/**
 * 用户登录表
 * @author zhangxin  2017/9/6
 *
 */
public class User implements Serializable {


	private static final long serialVersionUID = 1L;

	private Long id;  //流水号ID
	private String account; //账号
	private String password; //密码
	private String description;  //描述
	private String userType;//登录账户类别：1 管理员（admin）  2 电商平台  3 物流企业  4电商 5运营人员 
	
	private int del_flag;//0正常   1删除
	private Date create_date; //创建时间
	private String create_by; //创建人
	private Date update_date; //更新时间
	private String update_by;//更新人
	private String remarks;//备注
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
	public int getDel_flag() {
		return del_flag;
	}
	public void setDel_flag(int del_flag) {
		this.del_flag = del_flag;
	}
	public Date getCreate_date() {
		return create_date;
	}
	public void setCreate_date(Date create_date) {
		this.create_date = create_date;
	}
	public String getCreate_by() {
		return create_by;
	}
	public void setCreate_by(String create_by) {
		this.create_by = create_by;
	}
	public Date getUpdate_date() {
		return update_date;
	}
	public void setUpdate_date(Date update_date) {
		this.update_date = update_date;
	}
	public String getUpdate_by() {
		return update_by;
	}
	public void setUpdate_by(String update_by) {
		this.update_by = update_by;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
}

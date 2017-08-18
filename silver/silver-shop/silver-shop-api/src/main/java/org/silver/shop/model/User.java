package org.silver.shop.model;

import java.io.Serializable;
import java.util.Date;

//普通用户，消费者
public class User implements Serializable{
/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
private long id;
private String user_id;
private String user_name;
private String login_pass;
private String address;
private String mobile;
private String personal_ID;
private Date create_date;
private String create_by;
private Date update_date;
private String update_by;
private int del_flag;
private String remarks;
public long getId() {
	return id;
}
public void setId(long id) {
	this.id = id;
}
public String getUser_id() {
	return user_id;
}
public void setUser_id(String user_id) {
	this.user_id = user_id;
}
public String getUser_name() {
	return user_name;
}
public void setUser_name(String user_name) {
	this.user_name = user_name;
}
public String getLogin_pass() {
	return login_pass;
}
public void setLogin_pass(String login_pass) {
	this.login_pass = login_pass;
}
public String getAddress() {
	return address;
}
public void setAddress(String address) {
	this.address = address;
}
public String getMobile() {
	return mobile;
}
public void setMobile(String mobile) {
	this.mobile = mobile;
}
public String getPersonal_ID() {
	return personal_ID;
}
public void setPersonal_ID(String personal_ID) {
	this.personal_ID = personal_ID;
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
public int getDel_flag() {
	return del_flag;
}
public void setDel_flag(int del_flag) {
	this.del_flag = del_flag;
}
public String getRemarks() {
	return remarks;
}
public void setRemarks(String remarks) {
	this.remarks = remarks;
}

}

package org.silver.shop.model.system.manual;

import java.io.Serializable;
import java.util.Date;

public class Appkey implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8656470389536641103L;
 
	private long id;
	private String app_key;//appKey  关联的各个电商平台企业备案号
	private String app_secret;//appSecret
	private String app_name;//应用名
	private String user_name;//申请人姓名
	private String user_mobile;//联系电话
	private String user_id;//身份证
	private int del_flag;//0正常   1删除
	private Date create_date; //创建时间
	private String create_by; //创建人
	private Date update_date; //更新时间
	private String update_by;//更新人
	private String merchant_Id;//商户Id
	private String merchant_name;//商户名称
	private String remarks;//备注
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getApp_key() {
		return app_key;
	}
	public void setApp_key(String app_key) {
		this.app_key = app_key;
	}
	public String getApp_secret() {
		return app_secret;
	}
	public void setApp_secret(String app_secret) {
		this.app_secret = app_secret;
	}
	public String getApp_name() {
		return app_name;
	}
	public void setApp_name(String app_name) {
		this.app_name = app_name;
	}
	public String getUser_name() {
		return user_name;
	}
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
	public String getUser_mobile() {
		return user_mobile;
	}
	public void setUser_mobile(String user_mobile) {
		this.user_mobile = user_mobile;
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
	public String getUser_id() {
		return user_id;
	}
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	public String getMerchant_name() {
		return merchant_name;
	}
	public void setMerchant_name(String merchant_name) {
		this.merchant_name = merchant_name;
	}
	public String getMerchant_Id() {
		return merchant_Id;
	}
	public void setMerchant_Id(String merchant_Id) {
		this.merchant_Id = merchant_Id;
	}
	
	
	
}

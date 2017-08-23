package org.silver.sys.model;

import java.io.Serializable;
import java.util.Date;

//电商平台各个口岸的电商平台备案号
public class EBPentRecord  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long id;
	private String app_key;// 关联的开发key
	private int eport;// 在哪个口岸的备案号 1黄埔 2 南沙 （详细参考已对接口岸字典）
	private String EBPEntNo;// 电商平台企业备案号
	private String EBPEntName;// 电商平台企业名称
	private String internetDomainName;// 电商平台互联网域名
	private int del_flag;// 0正常 1删除
	private Date create_date; // 创建时间
	private String create_by; // 创建人
	private Date update_date; // 更新时间
	private String update_by;// 更新人
	private String remarks;// 备注
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
	public int getEport() {
		return eport;
	}
	public void setEport(int eport) {
		this.eport = eport;
	}
	public String getEBPEntNo() {
		return EBPEntNo;
	}
	public void setEBPEntNo(String eBPEntNo) {
		EBPEntNo = eBPEntNo;
	}
	public String getEBPEntName() {
		return EBPEntName;
	}
	public void setEBPEntName(String eBPEntName) {
		EBPEntName = eBPEntName;
	}
	public String getInternetDomainName() {
		return internetDomainName;
	}
	public void setInternetDomainName(String internetDomainName) {
		this.internetDomainName = internetDomainName;
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
	
	
}

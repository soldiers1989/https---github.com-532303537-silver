package org.silver.shop.model.system.manual;

import java.io.Serializable;
import java.util.Date;

//手工录入用户信息存储表
public class Muser implements Serializable {
	/**
		 * 
		 */
	private static final long serialVersionUID = 1219021505732575144L;
	private long id;
	private String muser_sys_no;// 系统分配唯一编号
	private String merchant_no;// 所属商户 (录入者)
	private String muser_name;// 姓名
	private String muser_tel;// 电话
	private String muser_addr;// 地址
	private String muser_cer_type;// 证件类型 暂固定位 01身份证
	private String muser_ID;// 证件id 暂国定为身份证id
	private String bank_type;// 银行类型
	private String bank_card_no;// 银行卡号
	private String adm_area_code;// 行政区域代码
	private int del_flag;
	private Date create_date;
	private String create_by;
	private Date update_date;
	private String update_by;
	private String remarks;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getMuser_sys_no() {
		return muser_sys_no;
	}

	public void setMuser_sys_no(String muser_sys_no) {
		this.muser_sys_no = muser_sys_no;
	}

	public String getMerchant_no() {
		return merchant_no;
	}

	public void setMerchant_no(String merchant_no) {
		this.merchant_no = merchant_no;
	}

	public String getMuser_name() {
		return muser_name;
	}

	public void setMuser_name(String muser_name) {
		this.muser_name = muser_name;
	}

	public String getMuser_tel() {
		return muser_tel;
	}

	public void setMuser_tel(String muser_tel) {
		this.muser_tel = muser_tel;
	}

	public String getMuser_addr() {
		return muser_addr;
	}

	public void setMuser_addr(String muser_addr) {
		this.muser_addr = muser_addr;
	}

	public String getMuser_cer_type() {
		return muser_cer_type;
	}

	public void setMuser_cer_type(String muser_cer_type) {
		this.muser_cer_type = muser_cer_type;
	}

	public String getMuser_ID() {
		return muser_ID;
	}

	public void setMuser_ID(String muser_ID) {
		this.muser_ID = muser_ID;
	}

	public String getBank_type() {
		return bank_type;
	}

	public void setBank_type(String bank_type) {
		this.bank_type = bank_type;
	}

	public String getBank_card_no() {
		return bank_card_no;
	}

	public void setBank_card_no(String bank_card_no) {
		this.bank_card_no = bank_card_no;
	}

	public String getAdm_area_code() {
		return adm_area_code;
	}

	public void setAdm_area_code(String adm_area_code) {
		this.adm_area_code = adm_area_code;
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

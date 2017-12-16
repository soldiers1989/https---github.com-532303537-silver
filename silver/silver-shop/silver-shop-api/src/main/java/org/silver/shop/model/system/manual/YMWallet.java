package org.silver.shop.model.system.manual;

import java.io.Serializable;
import java.util.Date;

//商户电子钱包
public class YMWallet implements Serializable{
  /**
	 * 
	 */
	private static final long serialVersionUID = 859297120364202632L;
  private long id;
  private String sys_wallet_no;//钱包唯一标识
  private String merchant_no;//所属商户id
  private double total_fund;//总资产
  private double frozen_balance;//冻结金额
  private double available_balance;//可用余额
  private String check_sign;//校验码
  private int status;//0 正常使用    1 用户停用    2 管理员冻结    3 存在异常，系统冻结
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
public String getSys_wallet_no() {
	return sys_wallet_no;
}
public void setSys_wallet_no(String sys_wallet_no) {
	this.sys_wallet_no = sys_wallet_no;
}
public String getMerchant_no() {
	return merchant_no;
}
public void setMerchant_no(String merchant_no) {
	this.merchant_no = merchant_no;
}

public double getTotal_fund() {
	return total_fund;
}
public void setTotal_fund(double total_fund) {
	this.total_fund = total_fund;
}
public double getFrozen_balance() {
	return frozen_balance;
}
public void setFrozen_balance(double frozen_balance) {
	this.frozen_balance = frozen_balance;
}
public double getAvailable_balance() {
	return available_balance;
}
public void setAvailable_balance(double available_balance) {
	this.available_balance = available_balance;
}

public String getCheck_sign() {
	return check_sign;
}
public void setCheck_sign(String check_sign) {
	this.check_sign = check_sign;
}
public int getStatus() {
	return status;
}
public void setStatus(int status) {
	this.status = status;
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

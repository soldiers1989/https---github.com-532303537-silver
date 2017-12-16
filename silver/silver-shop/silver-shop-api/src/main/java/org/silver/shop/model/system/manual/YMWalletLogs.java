package org.silver.shop.model.system.manual;

import java.io.Serializable;
import java.util.Date;

//电子钱包日志
public class YMWalletLogs implements Serializable{
/**
	 * 
	 */
	private static final long serialVersionUID = 6387042007108598275L;
private long id;
private String sys_wallet_no;
private String merchant_no;
private String trade_no;
private double before_trade;
private double after_trade;
private double amount;
private Date create_date;
private String create_by;
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
public String getTrade_no() {
	return trade_no;
}
public void setTrade_no(String trade_no) {
	this.trade_no = trade_no;
}


public double getBefore_trade() {
	return before_trade;
}
public void setBefore_trade(double before_trade) {
	this.before_trade = before_trade;
}
public double getAfter_trade() {
	return after_trade;
}
public void setAfter_trade(double after_trade) {
	this.after_trade = after_trade;
}
public double getAmount() {
	return amount;
}
public void setAmount(double amount) {
	this.amount = amount;
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
public String getRemarks() {
	return remarks;
}
public void setRemarks(String remarks) {
	this.remarks = remarks;
}

}

package org.silver.shop.model.common.base;

import java.io.Serializable;

/**
 * 	币制实体类
 */
public class Currency implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long id;
	private String currencyCode;//币制编码
	private String currencySymb;//币制字母简称
	private String currencyName;//币制中文名称
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getCurrencyCode() {
		return currencyCode;
	}
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}
	public String getCurrencySymb() {
		return currencySymb;
	}
	public void setCurrencySymb(String currencySymb) {
		this.currencySymb = currencySymb;
	}
	public String getCurrencyName() {
		return currencyName;
	}
	public void setCurrencyName(String currencyName) {
		this.currencyName = currencyName;
	}
	
	
}

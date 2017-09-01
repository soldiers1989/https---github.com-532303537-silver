package org.silver.shop.model.common.base;

import java.io.Serializable;

/**
 *	国家实体类 
 */
public class Country implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long id;
	private String countryCode; //国家编码
	private String isoE;//国家简称
	private String countryName;//国家全称
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getCountryCode() {
		return countryCode;
	}
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
	public String getIsoE() {
		return isoE;
	}
	public void setIsoE(String isoE) {
		this.isoE = isoE;
	}
	public String getCountryName() {
		return countryName;
	}
	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}
	
}

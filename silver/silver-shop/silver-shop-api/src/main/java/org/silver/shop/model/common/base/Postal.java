package org.silver.shop.model.common.base;

import java.io.Serializable;

/**
 * 全国邮政编码
 */
public class Postal  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7160107599252560181L;
	private long id;
	private String areaCode;//区域编码
	private String postalCode;//邮政编码
	private String phoneAreaCode;//电话区域编码
	
	public long getId() {
		return id;
	}
	public String getAreaCode() {
		return areaCode;
	}
	public String getPostalCode() {
		return postalCode;
	}
	public String getPhoneAreaCode() {
		return phoneAreaCode;
	}
	public void setId(long id) {
		this.id = id;
	}
	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	public void setPhoneAreaCode(String phoneAreaCode) {
		this.phoneAreaCode = phoneAreaCode;
	}
	
	
}

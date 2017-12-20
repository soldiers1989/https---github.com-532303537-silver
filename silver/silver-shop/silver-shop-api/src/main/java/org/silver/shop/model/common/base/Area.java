package org.silver.shop.model.common.base;

import java.io.Serializable;

/**
 *	城市区域实体类 
 */
public class Area implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long id;
	private String areaCode;//区域编码
	private String areaName;//区域名称
	private String cityCode;//关联上级城市编码
	private String postalName;//区域邮编编码
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getAreaCode() {
		return areaCode;
	}
	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}
	public String getAreaName() {
		return areaName;
	}
	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}
	public String getCityCode() {
		return cityCode;
	}
	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}
	public String getPostalName() {
		return postalName;
	}
	public void setPostalName(String postalName) {
		this.postalName = postalName;
	}
	
	
}

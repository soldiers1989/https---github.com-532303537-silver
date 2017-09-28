package org.silver.shop.model.common.base;

import java.io.Serializable;

/**
 * 口岸
 */
public class EPort implements Serializable{

	private long id ;
	private String customsPort;//海关口岸代码
	private String customsPortName;//海关口岸名称
	private String cityCode;//关联市级代码
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getCustomsPort() {
		return customsPort;
	}
	public void setCustomsPort(String customsPort) {
		this.customsPort = customsPort;
	}
	public String getCustomsPortName() {
		return customsPortName;
	}
	public void setCustomsPortName(String customsPortName) {
		this.customsPortName = customsPortName;
	}
	public String getCityCode() {
		return cityCode;
	}
	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}
	
	
	
}

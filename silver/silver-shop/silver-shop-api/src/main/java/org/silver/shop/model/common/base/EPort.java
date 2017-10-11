package org.silver.shop.model.common.base;

import java.io.Serializable;

/**
 * 系统口岸实体
 */
public class EPort implements Serializable{

	private long id ;
	private String customsPort;//海关口岸代码
	private String customsPortName;//海关口岸名称
	private String cityCode;//市级代码
	private String cityName;//市级中文名
	private String provinceCode;//省份代码
	private String provinceName;//省份中文名
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
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public String getProvinceCode() {
		return provinceCode;
	}
	public void setProvinceCode(String provinceCode) {
		this.provinceCode = provinceCode;
	}
	public String getProvinceName() {
		return provinceName;
	}
	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}
	
	
	
}

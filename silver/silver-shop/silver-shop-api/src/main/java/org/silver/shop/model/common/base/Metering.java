package org.silver.shop.model.common.base;

import java.io.Serializable;

/**
 * 计量单位实体类
 */
public class Metering implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long id;
	private String meteringCode;//计量单位代码
	private String meteringName;//计量单位中文名称
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getMeteringCode() {
		return meteringCode;
	}
	public void setMeteringCode(String meteringCode) {
		this.meteringCode = meteringCode;
	}
	public String getMeteringName() {
		return meteringName;
	}
	public void setMeteringName(String meteringName) {
		this.meteringName = meteringName;
	}
	
}

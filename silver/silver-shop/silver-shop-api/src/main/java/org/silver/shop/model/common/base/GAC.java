package org.silver.shop.model.common.base;

import java.io.Serializable;

/**
 * 海关总署
 * General Administration of Customs
 */
public class GAC implements Serializable {
	private long id;
	private String code;
	private String GACName;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getGACName() {
		return GACName;
	}
	public void setGACName(String gACName) {
		GACName = gACName;
	}
	
	
}

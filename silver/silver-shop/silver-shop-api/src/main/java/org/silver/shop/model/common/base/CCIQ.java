package org.silver.shop.model.common.base;

import java.io.Serializable;

/**
 *	国家出入境检验检疫局:简称国检机构
 */
public class CCIQ implements Serializable{

	private long id;
	private String code;//代码
	private String CCIQName;//中文名称
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
	public String getCCIQName() {
		return CCIQName;
	}
	public void setCCIQName(String cCIQName) {
		CCIQName = cCIQName;
	}

	
}


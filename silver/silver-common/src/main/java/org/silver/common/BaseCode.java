package org.silver.common;



/**
 * 常用Map返回key
 */
public enum BaseCode {
	/**
	 * 状态
	 */
	STATUS("status"),
	/**
	 * 信息
	 */
	MSG("msg"),
	/**
	 * 总数
	 */
	TOTALCOUNT("totalCount"),
	/**
	 * 错误
	 */
	ERROR("error"),
	/**
	 * 数据
	 */
	DATAS("datas");

	private String baseCode;

	private BaseCode(String baseCode) {
		this.baseCode = baseCode;
	}

	public String getBaseCode() {
		return this.baseCode;
	}

	@Override
	public String toString() {
		return this.baseCode.toString();
	}
}

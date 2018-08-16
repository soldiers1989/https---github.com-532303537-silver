package org.silver.common;

public enum StatusCode {
	/**
	 * 操作成功
	 */
	SUCCESS("1", "操作成功"),
	/**
	 * 暂无数据
	 */
	NO_DATAS("-1", "暂无数据"),
	/**
	 * 数据格式有误
	 */
	FORMAT_ERR("-2", "数据格式有误"), 
	/**
	 * 参数不正确
	 */
	NOTICE("-3", "参数不正确"),
	/**
	 * 没有操作权限
	 */
	PERMISSION_DENIED("-4","没有操作权限"),
	/**
	 * 服务器繁忙
	 */
    WARN("-5","服务器繁忙"),
    /**
     * 登录已过期
     */
	LOSS_SESSION("-6","登录已过期"),

	/**
	 * 未知错误
	 */
	UNKNOWN("-7","未知错误");
	
	
	private String StatusCode;
	private String Msg;

	private StatusCode(String statusCode, String msg) {
		this.StatusCode = statusCode;
		this.Msg = msg;
	}

	public String getStatus() {
		return this.StatusCode;

	}

	public String getMsg() {
		return this.Msg;

	}

	  @Override
	   public String toString() {
	        return this.StatusCode+":"+this.Msg;
	   }
	
}

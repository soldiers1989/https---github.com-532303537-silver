package org.silver.common;

public enum LoginType {
	/**
	 * 用户
	 */
	MEMBER("Member"),  
	/**
	 * 管理员
	 */
	MANAGER("Manager") ,
	/**
	 * 商户
	 */
	MERCHANT("Merchant"),
	/**
	 * （临时旧数据）代理
	 */
	PROXY("Proxy"),
	/**
	 * 代理商
	 */
	AGENT("Agent"),
	/**
	 * 商户的info
	 */
	MERCHANT_INFO(MERCHANT+"_info"),
	/**
	 * 管理员info
	 */
	MANAGER_INFO(MANAGER+"_info"),
	/**
	 * 用户info
	 */
	MEMBER_INFO(MEMBER+"_info"),
	/**
	 * （临时旧数据）代理info
	 */
	PROXY_INFO(PROXY+"_info"),
	/**
	 * 代理商info
	 */
	AGENT_INFO(AGENT+"_info")
	;
	
    private String type;

    private LoginType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type.toString();
    }
    
}

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
	 * 商户的info
	 */
	MERCHANTINFO(MERCHANT+"_info"),
	/**
	 * 管理员的info
	 */
	MANAGERINFO(MANAGER+"_info"),
	/**
	 * 用户的info
	 */
	MEMBERINFO(MEMBER+"_info");
	
    private String type;

    private LoginType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type.toString();
    }
    
}

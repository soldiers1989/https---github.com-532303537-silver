package org.silver.common;

public enum LoginType {
	USER("User"),  ADMIN("Admin") ,MERCHANT("Merchant");

    private String type;

    private LoginType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type.toString();
    }
}

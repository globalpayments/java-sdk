package com.global.api.entities.enums;

public enum AuthenticationSource implements IStringConstant {
    Browser("BROWSER"),
    StoredRecurring("STORED_RECURRING"),
    MobileSDK("MOBILE_SDK"),
    MerchantInitiated("MERCHANT_INITIATED");

    String value;
    AuthenticationSource(String value) { this.value = value; }
    public String getValue() { return value; }
    public byte[] getBytes() { return value.getBytes(); }
}

package com.global.api.entities.enums;

public enum WhitelistStatus implements IStringConstant {
    Whitelisted("WHITELISTED"),
    NotWhitelisted("NOT_WHITELISTED");

    String value;
    WhitelistStatus(String value) { this.value = value; }
    public String getValue() { return value; }
    public byte[] getBytes() { return value.getBytes(); }
}

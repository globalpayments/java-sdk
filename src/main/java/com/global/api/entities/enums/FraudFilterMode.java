package com.global.api.entities.enums;

public enum FraudFilterMode implements IStringConstant {
    None("NONE"),
    Off("OFF"),
    Passive("PASSIVE"),
    Active("ACTIVE");

    String value;
    FraudFilterMode(String value) {this.value = value;}
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}

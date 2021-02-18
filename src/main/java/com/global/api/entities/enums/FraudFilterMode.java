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

    public static FraudFilterMode fromString(String value) {
        for (FraudFilterMode mode : FraudFilterMode.values()) {
            if (mode.getValue().equalsIgnoreCase(value)) {
                return mode;
            }
        }
        return null;
    }
}
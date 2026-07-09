package com.global.api.entities.enums;

public enum RestrictedTokenType implements IStringConstant {
    YES("YES"),
    NO("NO");

    private final String value;

    RestrictedTokenType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public byte[] getBytes() {
        return value.getBytes();
    }
}

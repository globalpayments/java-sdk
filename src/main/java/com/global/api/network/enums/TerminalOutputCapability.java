package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum TerminalOutputCapability implements IStringConstant {
    Unknown("0"),
    None("1"),
    Printing("2"),
    Display("3"),
    Printing_Display("4"),
    Coupon_Printing("9");

    private final String value;
    TerminalOutputCapability(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}

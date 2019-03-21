package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum DE48_AddressUsage implements IStringConstant {
    Business("0"),
    Home("1"),
    Fax("2"),
    Cellular("3"),
    Billing("4"),
    Shipping("5"),
    CustomerServiceCenter("6"),
    Daytime("7"),
    Evening("8"),
    PreviousHome("9"),
    Merchant("A");

    private final String value;
    DE48_AddressUsage(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}

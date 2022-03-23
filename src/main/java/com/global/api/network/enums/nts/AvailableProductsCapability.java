package com.global.api.network.enums.nts;

import com.global.api.entities.enums.IStringConstant;

public enum AvailableProductsCapability implements IStringConstant {
    DeviceIsNotAvailableProductsCapable("0"),
    DeviceIsAvailableProductsCapable("4");
    private final String value;
    AvailableProductsCapability(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}

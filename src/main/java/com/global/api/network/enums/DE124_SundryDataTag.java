package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum DE124_SundryDataTag implements IStringConstant {
    ClientSuppliedData("00"),
    PiggyBack_CollectTransaction("01"),
    PiggyBack_AuthCaptureData("02");

    private final String value;
    DE124_SundryDataTag(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}

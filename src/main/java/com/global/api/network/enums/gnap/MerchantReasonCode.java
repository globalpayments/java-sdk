package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum MerchantReasonCode implements IStringConstant {
    IncrementalAuth("N"),
    Resubmission("S"),
    DelayedChares("D"),
    ReAuth("A"),
    NoShow("X");

    String value;
    MerchantReasonCode(String Value){this.value=Value;}

    @Override
    public byte[] getBytes() {
        return this.value.getBytes();
    }

    @Override
    public String getValue() {
        return this.value;
    }

}

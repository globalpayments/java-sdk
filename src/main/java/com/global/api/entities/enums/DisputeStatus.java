package com.global.api.entities.enums;

import com.global.api.entities.enums.IStringConstant;

public enum DisputeStatus implements IStringConstant {
    UnderReview("UNDER_REVIEW"),
    WithMerchant("WITH_MERCHANT"),
    Closed("CLOSED");

    String value;
    DisputeStatus(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}

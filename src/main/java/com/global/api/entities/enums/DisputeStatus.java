package com.global.api.entities.enums;

public enum DisputeStatus implements IStringConstant {
    UnderReview("UNDER_REVIEW"),
    WithMerchant("WITH_MERCHANT"),
    Closed("CLOSED"),
    Funded("FUNDED");       //Only for Settlement disputes

    String value;
    DisputeStatus(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}

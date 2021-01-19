package com.global.api.entities.enums;

public enum TransactionStatus implements IStringConstant {

    Initiated("INITIATED"),
    Authenticated("AUTHENTICATED"),
    Pending("PENDING"),
    Declined("DECLINED"),
    Preauthorized("PREAUTHORIZED"),
    Captured("CAPTURED"),
    Batched("BATCHED"),
    Reversed("REVERSED"),
    Funded("FUNDED"),
    Rejected("REJECTED");

    private String value;

    TransactionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public byte[] getBytes() {
        return this.value.getBytes();
    }

}
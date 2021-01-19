package com.global.api.entities.enums;

import com.global.api.entities.enums.IStringConstant;

public enum DepositSortProperty implements IStringConstant {
    TimeCreated("TIME_CREATED"),
    Status("STATUS"),
    Type("TYPE"),
    DepositId("DEPOSIT_ID");

    String value;
    DepositSortProperty(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}

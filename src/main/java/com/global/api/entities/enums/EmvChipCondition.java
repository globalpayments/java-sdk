package com.global.api.entities.enums;

public enum EmvChipCondition implements IStringConstant {
    ChipFailPreviousSuccess("CHIP_FAILED_PREV_SUCCESS"),
    ChipFailPreviousFail("CHIP_FAILED_PREV_FAILED");

    private String value;
    EmvChipCondition(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}

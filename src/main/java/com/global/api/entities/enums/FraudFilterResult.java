package com.global.api.entities.enums;

public enum FraudFilterResult implements IStringConstant {
    None("NONE"),
    HOLD("PENDING_REVIEW"),
    PASS("ACCEPTED"),
    BLOCK("REJECTED"),
    NOT_EXECUTED("NOT_EXECUTED"),
    ERROR("ERROR"),
    RELEASE_SUCCESSFUL("RELEASE_SUCCESSFULL"),
    HOLD_SUCCESSFUL("HOLD_SUCCESSFULL");

    String value;
    FraudFilterResult(String value) {this.value = value;}
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }

    public static FraudFilterResult fromString(String value) {
        for (FraudFilterResult mode : FraudFilterResult.values()) {
            if (mode.getValue().equalsIgnoreCase(value)) {
                return mode;
            }
        }
        return null;
    }
}
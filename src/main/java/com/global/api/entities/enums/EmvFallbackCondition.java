package com.global.api.entities.enums;

public enum EmvFallbackCondition implements IStringConstant {
    ChipReadFailure("ICC_TERMINAL_ERROR"),
    NoCandidateList("NO_CANDIDATE_LIST");

    private String value;
    EmvFallbackCondition(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}

package com.global.api.entities.enums;

public enum DecoupledFlowRequest implements IStringConstant {
    DecoupledPreferred("DECOUPLED_PREFERRED"),
    DoNotUseDecoupled("DO_NOT_USE_DECOUPLED");

    String value;
    DecoupledFlowRequest(String value) { this.value = value; }
    public String getValue() { return value; }
    public byte[] getBytes() { return value.getBytes(); }
}

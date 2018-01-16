package com.global.api.entities.enums;

public enum ReversalReasonCode implements IStringConstant {
    CustomerCancellation("CUSTOMERCANCELLATION"),
    TerminalError("TERMINALERROR"),
    Timeout("TIMEOUT");

    String value;
    ReversalReasonCode(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}

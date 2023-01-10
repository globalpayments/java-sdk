package com.global.api.entities.enums;

public enum PendingParameterIndicator implements IStringConstant {
    PDLWaiting("P"),
    NoPDL("O");

    String value;
    PendingParameterIndicator(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }

    }

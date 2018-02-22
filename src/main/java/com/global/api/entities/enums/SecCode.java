package com.global.api.entities.enums;

public enum SecCode implements IStringConstant {
    Ppd("PPD"),
    Ccd("CCD"),
    Pop("POP"),
    Web("WEB"),
    Tel("TEL"),
    Ebronze("EBRONZE");

    String value;
    SecCode(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}

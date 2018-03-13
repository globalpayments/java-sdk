package com.global.api.entities.enums;

public enum HppVersion implements IStringConstant {
    Version2("2");

    String value;
    HppVersion(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}

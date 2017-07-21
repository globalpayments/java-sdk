package com.global.api.entities.enums;

public enum AliasAction implements IStringConstant {
    Add("ADD"),
    Create("CREATE"),
    Delete("DELETE");

    String value;
    AliasAction(String value) {
        this.value = value;
    }
    public String getValue() { return value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}

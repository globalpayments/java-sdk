package com.global.api.entities.enums;

public enum StorageMode implements IStringConstant {
    ALWAYS("ALWAYS"),
    ON_SUCCESS("ON_SUCCESS"),
    PROMPT("PROMPT"),
    OFF("OFF");

    String value;
    StorageMode(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}

package com.global.api.entities.enums;

public enum EntryMethod implements IStringConstant {
    Manual("manual"),
    Swipe("swipe"),
    Proximity("proximity");

    String value;
    EntryMethod(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}

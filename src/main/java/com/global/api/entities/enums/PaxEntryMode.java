package com.global.api.entities.enums;

public enum PaxEntryMode implements IStringConstant {
    Manual("0"),
    Swipe("1"),
    Contactless("2"),
    Scanner("3"),
    Chip("4"),
    ChipFallBackSwipe("5");

    String value;
    PaxEntryMode(String value) { this.value = value; }
    public byte[] getBytes() { return value.getBytes(); }
    public String getValue() { return value; }
}
package com.global.api.entities.enums;

public enum PrestigiousPropertyLimit implements IStringConstant {
    Limit_500("LIMIT_500"),
    Limit_1000("LIMIT_1000"),
    Limit_1500("LIMIT_1500"),
    NotParticipating("NOT_PARTICIPATING");

    String value;
    PrestigiousPropertyLimit(String value) {
        this.value = value;
    }
    public String getValue() { return value; }
    public byte[] getBytes() { return value.getBytes(); }
}

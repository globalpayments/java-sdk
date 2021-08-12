package com.global.api.entities.enums;

public enum CvnPresenceIndicator implements IStringConstant  {
    Present("1"),
    Illegible("2"),
    NotOnCard("3"),
    NotRequested("4");

    private String value;
    CvnPresenceIndicator(String value){
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return value.getBytes(); }
}
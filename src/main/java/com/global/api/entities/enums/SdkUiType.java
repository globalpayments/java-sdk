package com.global.api.entities.enums;

public enum SdkUiType implements IStringConstant {
    Text("TEXT"),
    SingleSelect("SINGLE_SELECT"),
    MultiSelect("MULTI_SELECT"),
    OOB("OOB"),
    HTML_Other("HTML_OTHER");

    String value;
    SdkUiType(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}

package com.global.api.utils;

public class TlvData {
    private String tag;
    private String length;
    private String value;
    private String description;

    public String getTag() {
        return tag;
    }
    public String getLength() {
        return length;
    }
    public String getValue() {
        return value;
    }
    public String getBinaryValue() {
        StringBuilder sb = new StringBuilder();
        for(byte b: StringUtils.bytesFromHex(value)) {
            sb.append(Integer.toBinaryString((b & 0xFF) + 0x100).substring(1));
        }
        return sb.toString();
    }
    public String getDescription() {
        return description;
    }

    public String getFullValue() {
        return String.format("%s%s%s", tag, length, value);
    }

    public TlvData(String tag, String length, String value) {
        this(tag, length, value, null);
    }
    public TlvData(String tag, String length, String value, String description) {
        this.tag = tag;
        this.length = length;
        this.value = value;
        this.description = description;
    }
}

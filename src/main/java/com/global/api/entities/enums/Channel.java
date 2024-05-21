package com.global.api.entities.enums;

public enum Channel implements IStringConstant {
    Ecom("ECOM"),
    CardPresent("CP"),
    CardNotPresent("CNP");

    private String value;

    Channel(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public byte[] getBytes() {
        return this.value.getBytes();
    }

    public static Channel fromString(String value) {
        for (Channel currentEnum : Channel.values()) {
            if (currentEnum.getValue().equalsIgnoreCase(value)) {
                return currentEnum;
            }
        }
        return null;
    }
}
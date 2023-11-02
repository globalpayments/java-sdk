package com.global.api.entities.enums;

public enum BlockCardType {

    CONSUMER_CREDIT("consumercredit"),
    CONSUMER_DEBIT("consumerdebit"),
    COMMERCIAL_DEBIT("commercialdebit"),
    COMMERCIAL_CREDIT("commercialcredit");

    private final String value;

    BlockCardType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static BlockCardType fromValue(String value) {
        for (BlockCardType blockCardType : BlockCardType.values()) {
            if (blockCardType.getValue().equals(value)) {
                return blockCardType;
            }
        }
        return null;
    }

}

package com.global.api.terminals.diamond.enums;

public enum CardSource {
    CONTACTLESS("B"),
    MANUAL("M"),
    MAGSTRIPE("C"),
    ICC("P"),
    UNKNOWN("?");

    private String value;

    CardSource(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CardSource fromValue(String value) {
        for (CardSource cardSource : CardSource.values()) {
            if (cardSource.getValue().equals(value)) {
                return cardSource;
            }
        }
        return null;
    }
}

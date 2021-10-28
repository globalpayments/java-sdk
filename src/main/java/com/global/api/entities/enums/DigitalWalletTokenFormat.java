package com.global.api.entities.enums;

public enum DigitalWalletTokenFormat {
    CARD_NUMBER ("CARD_NUMBER"),
    CARD_TOKEN("CARD_TOKEN");

    private String value;

    DigitalWalletTokenFormat(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
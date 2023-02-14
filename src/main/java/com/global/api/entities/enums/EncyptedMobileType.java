package com.global.api.entities.enums;

public enum EncyptedMobileType {
    APPLE_PAY("APPLEPAY"),
    GOOGLE_PAY("PAY_BY_GOOGLE"),
    CLICK_TO_PAY("CLICK_TO_PAY");

    private String value;

    EncyptedMobileType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}

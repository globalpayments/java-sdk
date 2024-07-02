package com.global.api.entities.enums;

public enum MasterCardCITMITIndicator {
    CARDHOLDER_INITIATED_CREDENTIAL_ON_FILE("C101"),
    CARDHOLDER_INITIATED_STANDING_ORDER("C102"),
    CARDHOLDER_INITIATED_SUBSCRIPTION("C103"),
    MERCHANT_INITIATED_UNSCHEDULED_CREDENTIAL_ON_FILE("M101"),
    MERCHANT_INITIATED_STANDING_ORDER("M102"),
    MERCHANT_INITIATED_SUBSCRIPTION("M103"),
    MERCHANT_INITIATED_DELAYED_CHARGE("M206"),
    MERCHANT_INITIATED_NO_SHOW_CHARGE("M207"),
    MERCHANT_INITIATED_RESUBMISSION("M208");

    private String value;

    MasterCardCITMITIndicator(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public byte[] getBytes() {
        return this.value.getBytes();
    }

}

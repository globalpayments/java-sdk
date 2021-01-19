package com.global.api.entities.enums;

import com.global.api.entities.enums.IStringConstant;

public enum DisputeStage implements IStringConstant {
    Retrieval("RETRIEVAL"),
    Chargeback("CHARGEBACK"),
    Reversal("REVERSAL"),
    SecondChargeback("SECOND_CHARGEBACK"),
    PreArbitration("PRE_ARBITRATION"),
    Arbitration("ARBITRATION"),
    PreCompliance("PRE_COMPLIANCE"),
    Compliance("COMPLIANCE"),
    Goodfaith("GOODFAITH");

    String value;
    DisputeStage(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}

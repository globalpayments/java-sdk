package com.global.api.entities.enums;

public enum ProPayAccountStatus implements IStringConstant{
    ReadyToProcess("ReadyToProcess"),
    FraudAccount("FraudAccount"),
    RiskwiseDeclined("RiskwiseDeclined"),
    Hold("Hold"),
    Canceled("Canceled"),
    FraudVictim("FraudVictim"),
    ClosedEula("ClosedEula"),
    ClosedExcessiveChargeback("ClosedExcessiveChargeback");

    String value;
    ProPayAccountStatus(String value) {
        this.value = value;
    }
    public String getValue() { return value; }
    public byte[] getBytes() { return value.getBytes(); }
}

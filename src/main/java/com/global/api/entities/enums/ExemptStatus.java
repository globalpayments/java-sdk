package com.global.api.entities.enums;

public enum ExemptStatus implements IStringConstant {
    LowValue("LOW_VALUE"),
    TransactionRiskAnalysis("TRANSACTION_RISK_ANALYSIS"),
    TrustedMerchant("TRUSTED_MERCHANT"),
    SecureCorporatePayment("SECURE_CORPORATE_PAYMENT"),
    ScaDelegation("SCA_DELEGATION");

    String value;
    ExemptStatus(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}

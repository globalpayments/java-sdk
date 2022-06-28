package com.global.api.network.enums.gnap;

public enum TransactionStatusIndicator {
    NormalRequest("0"),
    MerchantAuthorization("1"),
    PreAuthorizationRequest("4"),
    StandIn("5"),
    AddressVerificationRequest("6"),
    AccountStatusInquiryService("8");

    String value;
    TransactionStatusIndicator(String value){this.value=value;}
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}

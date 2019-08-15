package com.global.api.entities.enums;

public enum CustomerAuthenticationMethod implements IStringConstant {
    NotAuthenticated("NOT_AUTHENTICATED"),
    MerchantSystem("MERCHANT_SYSTEM_AUTHENTICATION"),
    FederatedId("FEDERATED_ID_AUTHENTICATION"),
    IssuerCredential("ISSUER_CREDENTIAL_AUTHENTICATION"),
    thirdParty("THIRD_PARTY_AUTHENTICATION"),
    FIDO("FIDO_AUTHENTICATION");

    String value;
    CustomerAuthenticationMethod(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}

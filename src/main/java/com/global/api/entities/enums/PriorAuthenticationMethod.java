package com.global.api.entities.enums;

public enum PriorAuthenticationMethod implements IStringConstant {
    FrictionlessAuthentication("FRICTIONLESS_AUTHENTICATION"),
    ChallengeOccurred("CHALLENGE_OCCURRED"),
    AvsVerified("AVS_VERIFIED"),
    OtherMethod("OTHER_ISSUER_METHOD");

    String value;
    PriorAuthenticationMethod(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}

package com.global.api.network.entities;

import com.global.api.entities.enums.DebitAuthorizerCode;
import com.global.api.network.enums.AuthorizerCode;
import com.global.api.network.enums.FallbackCode;
import com.global.api.utils.ReverseStringEnumMap;

public class NtsData {
    private FallbackCode fallbackCode;
    private AuthorizerCode authorizerCode;
    private DebitAuthorizerCode debitAuthorizerCode;

    public FallbackCode getFallbackCode() {
        return fallbackCode;
    }
    private void setFallbackCode(FallbackCode fallbackCode) {
        this.fallbackCode = fallbackCode;
    }
    public AuthorizerCode getAuthorizerCode() {
        return authorizerCode;
    }
    private void setAuthorizerCode(AuthorizerCode authorizerCode) {
        this.authorizerCode = authorizerCode;
    }
    public DebitAuthorizerCode getDebitAuthorizerCode() {
        return debitAuthorizerCode;
    }
    private void setDebitAuthorizerCode(DebitAuthorizerCode debitAuthorizerCode) {
        this.debitAuthorizerCode = debitAuthorizerCode;
    }

    public NtsData() {
        this(FallbackCode.None, AuthorizerCode.Interchange_Authorized, DebitAuthorizerCode.NonPinDebitCard);
    }
    public NtsData(FallbackCode fallbackCode, AuthorizerCode authorizerCode) {
        this(fallbackCode, authorizerCode, DebitAuthorizerCode.NonPinDebitCard);
    }
    public NtsData(FallbackCode fallbackCode, AuthorizerCode authorizerCode, String debitAuthorizerCode) {
        this.fallbackCode = fallbackCode;
        this.authorizerCode = authorizerCode;
        this.debitAuthorizerCode = ReverseStringEnumMap.parse(debitAuthorizerCode, DebitAuthorizerCode.class);
        if(this.debitAuthorizerCode == null) {
            this.debitAuthorizerCode = DebitAuthorizerCode.NonPinDebitCard;
        }
    }
    public NtsData(FallbackCode fallbackCode, AuthorizerCode authorizerCode, DebitAuthorizerCode debitAuthorizerCode) {
        this.fallbackCode = fallbackCode;
        this.authorizerCode = authorizerCode;
        this.debitAuthorizerCode = debitAuthorizerCode;
    }

    public String toString() {
        return fallbackCode.getValue()
                .concat(authorizerCode.getValue())
                .concat(debitAuthorizerCode.getValue());
    }

    public static NtsData fromString(String data) {
        if(data == null) {
            return null;
        }

        NtsData rvalue = new NtsData();

        String fallbackStr = data.substring(0, 2);
        String authorizorCodeStr = data.substring(2, 3);
        String debitAuthorizorCode = data.substring(3);

        rvalue.setFallbackCode(ReverseStringEnumMap.parse(fallbackStr, FallbackCode.class));
        rvalue.setAuthorizerCode(ReverseStringEnumMap.parse(authorizorCodeStr, AuthorizerCode.class));
        rvalue.setDebitAuthorizerCode(ReverseStringEnumMap.parse(debitAuthorizorCode, DebitAuthorizerCode.class));

        return rvalue;
    }
    public static NtsData interchangeAuthorized() {
        return new NtsData(FallbackCode.None, AuthorizerCode.Interchange_Authorized);
    }
    public static NtsData hostAuthorized(FallbackCode fallbackCode) {
        return new NtsData(fallbackCode, AuthorizerCode.Host_Authorized);
    }
    public static NtsData voiceAuthorized() {
        return voiceAuthorized(DebitAuthorizerCode.NonPinDebitCard);
    }
    public static NtsData voiceAuthorized(DebitAuthorizerCode debitAuthorizer) {
        return new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized, debitAuthorizer);
    }
}

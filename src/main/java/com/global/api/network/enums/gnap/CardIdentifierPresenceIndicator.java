package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum CardIdentifierPresenceIndicator implements IStringConstant {

    CVDORCIDValueBypassed("0"),
    CVDORCIDValueIsPresent("1"),
    CVDORCIDValueOnTheCardButIllegible("2"),
    CardholderStatesCardHasNoCVDORCIDImprint("9");

    String value;

    CardIdentifierPresenceIndicator(String value){this.value = value;}


    public byte[] getBytes() {
        return this.value.getBytes();
    }

    public String getValue() {
        return this.value;
    }
}


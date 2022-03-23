package com.global.api.network.enums.nts;

import com.global.api.entities.enums.IStringConstant;

public enum EMVPDLKeyStatus implements IStringConstant {
    Active("A"),
    Expired("E"),
    Revoked("R");

    final String value;

    EMVPDLKeyStatus(String value){
        this.value = value;
    }

    @Override
    public byte[] getBytes() {
        return value.getBytes();
    }

    @Override
    public String getValue() {
        return value;
    }
}

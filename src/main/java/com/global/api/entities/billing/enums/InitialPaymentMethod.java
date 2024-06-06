package com.global.api.entities.billing.enums;

import com.global.api.entities.enums.IStringConstant;

public enum InitialPaymentMethod implements IStringConstant {
    UNASSIGNED("Unassigned"),
    CARD("Card"),
    OTHER("Other");

    String value;
    InitialPaymentMethod(String value) {
        this.value = value;
    }

    @Override
    public byte[] getBytes() {
        return this.value.getBytes();
    }

    @Override
    public String getValue() {
        return value;
    }
}

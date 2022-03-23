package com.global.api.network.enums;

import com.global.api.entities.enums.INumericConstant;

public enum ServiceCode implements INumericConstant {
    Self(1),
    Full(2),
    Other(3);

    private final Integer value;

    ServiceCode(Integer value) { this.value = value; }

    @Override
    public int getValue() {
        return value;
    }
}

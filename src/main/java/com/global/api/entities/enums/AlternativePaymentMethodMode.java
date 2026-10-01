package com.global.api.entities.enums;

public enum AlternativePaymentMethodMode implements IStringConstant {
    LEVEL_ZERO("level_zero");

    private final String value;

    AlternativePaymentMethodMode(String value) {
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

    public static AlternativePaymentMethodMode fromValue(String value) {
        for (AlternativePaymentMethodMode mode : AlternativePaymentMethodMode.values()) {
            if (mode.getValue().equals(value)) {
                return mode;
            }
        }
        return null;
    }
}


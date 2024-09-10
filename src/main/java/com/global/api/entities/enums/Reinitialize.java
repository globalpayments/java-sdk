package com.global.api.entities.enums;

public enum Reinitialize {
    ReinitializeApplication(1),
    NoReinitializeApplication(0);

    private final int value;

    Reinitialize(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

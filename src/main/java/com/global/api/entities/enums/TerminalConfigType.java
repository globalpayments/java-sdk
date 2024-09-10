package com.global.api.entities.enums;

import java.util.Arrays;

public enum TerminalConfigType {
    ContactTerminalConfiguration(1),
    ContactCardDataConfiguration(2),
    ContactCAPK(3),
    ContactlessTerminalConfiguration(4),
    ContactlessCardDataConfiguration(5),
    ContactlessCAPK(6),
    AIDList(7),
    ModifyAIDs(8);

    private final int value;

    TerminalConfigType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TerminalConfigType getByValue(String value) {
        return Arrays.stream(values())
                .filter(val -> String.valueOf(val.value).equals(value))
                .findFirst()
                .orElse(null);
    }

}
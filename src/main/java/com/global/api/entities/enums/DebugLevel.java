package com.global.api.entities.enums;

public enum DebugLevel {

    NOLOGS(0),
    ERROR(1),
    WARNING(2),
    FLOW(4),
    MESSAGE(8),
    DATA(16),
    PACKETS(32),
    PIA(64),
    PERF(128);

    private final int value;

    private DebugLevel(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DebugLevel fromValue(int value) {
        for (DebugLevel level : DebugLevel.values()) {
            if (level.value == value) {
                return level;
            }
        }
        throw new IllegalArgumentException("No DebugLevel Found: " + value);
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
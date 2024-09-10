package com.global.api.entities.enums;

public enum DebugLogsOutput {

    CONSOLE(0),
    FILE(1);
    private final int value;

    private DebugLogsOutput(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DebugLogsOutput fromValue(int value) {
        for (DebugLogsOutput logsOutput : DebugLogsOutput.values()) {
            if (logsOutput.value == value) {
                return logsOutput;
            }
        }
        throw new IllegalArgumentException("No DebugLevel Found: " + value);
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}

package com.global.api.entities.enums;

import lombok.Getter;

@Getter
public enum LogFile {

    DEBUGLOG1(1, "Debuglog1.log"),
    DEBUGLOG2(2, "Debuglog2.log"),
    DEBUGLOG3(3, "Debuglog3.log");

    private final int value;
    private final String description;

    private LogFile(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static LogFile fromValue(int value) {
        for (LogFile logFile : LogFile.values()) {
            if (logFile.value == value) {
                return logFile;
            }
        }
        throw new IllegalArgumentException("No DebugLevel Found: " + value);
    }
}
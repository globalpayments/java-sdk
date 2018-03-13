package com.global.api.entities.exceptions;

public class UnsupportedConnectionModeException extends ConfigurationException {
    public UnsupportedConnectionModeException() {
        this("Your chosen connection method is not supported by this device.");
    }
    public UnsupportedConnectionModeException(String message) {
        super(message);
    }
}

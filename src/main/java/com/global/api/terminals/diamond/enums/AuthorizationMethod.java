package com.global.api.terminals.diamond.enums;

public enum AuthorizationMethod {
    PIN("A"),
    SIGNATURE("@"),
    PIN_AND_SIGNATURE("B"),
    NO_AUTH_METHOD("?");

    private String value;

    AuthorizationMethod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AuthorizationMethod fromValue(String value) {
        for (AuthorizationMethod authorizationMethod : AuthorizationMethod.values()) {
            if (authorizationMethod.getValue().equals(value)) {
                return authorizationMethod;
            }
        }
        return null;
    }
}

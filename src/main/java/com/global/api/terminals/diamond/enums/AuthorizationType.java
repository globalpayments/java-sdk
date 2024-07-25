package com.global.api.terminals.diamond.enums;

public enum AuthorizationType {
    ONLINE("1"),
    OFFLINE("3"),
    REFERRAL ("4");

    private String value;

    AuthorizationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AuthorizationType fromValue(String value) {
        for (AuthorizationType authorizationType : AuthorizationType.values()) {
            if (authorizationType.getValue().equals(value)) {
                return authorizationType;
            }
        }
        return null;
    }
}

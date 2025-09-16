package com.global.api.entities.enums;

import java.util.HashMap;

public enum VoidReason implements IStringConstant, IMappedConstant {

    PostAuth_UserDeclined("POST_AUTH_USER_DECLINE", new HashMap<Target, String>() {{
        put(Target.Transit, "POST_AUTH_USER_DECLINE");
    }}),
    DeviceTimeout("DEVICE_TIMEOUT", new HashMap<Target, String>() {{
        put(Target.Transit, "DEVICE_TIMEOUT");
    }}),
    DeviceUnavailable("DEVICE_UNAVAILABLE", new HashMap<Target, String>() {{
        put(Target.Transit, "DEVICE_UNAVAILABLE");
    }}),
    PartialReversal("PARTIAL_REVERSAL", new HashMap<Target, String>() {{
        put(Target.Transit, "PARTIAL_REVERSAL");
    }}),
    TornTransactions("TORN_TRANSACTIONS", new HashMap<Target, String>() {{
        put(Target.Transit, "TORN_TRANSACTIONS");
    }}),
    PostAuth_ChipDecline("POST_AUTH_CHIP_DECLINE", new HashMap<Target, String>() {{
        put(Target.Transit, "POST_AUTH_CHIP_DECLINE");
    }});

    private final String value;
    private final HashMap<Target, String> transitValue;

    VoidReason(String value, HashMap<Target, String> transitValue) {
        this.value = value;
        this.transitValue = transitValue;
    }

    public String getValue() {
        return value;
    }

    public String getValue(Target target) {
        if (transitValue.containsKey(target)) {
            return transitValue.get(target);
        }
        return null;
    }

    public byte[] getBytes() {
        return value.getBytes();
    }
}
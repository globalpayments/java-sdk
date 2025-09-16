package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;
import com.global.api.entities.enums.IMappedConstant;
import com.global.api.entities.enums.Target;

import java.util.HashMap;

public enum OperatingEnvironment implements IStringConstant, IMappedConstant {
    NoTerminalUsed("0", new HashMap<Target, String>() {{
        put(Target.Transit, "NO_TERMINAL");
    }}),
    OnPremises_CardAcceptor_Attended("1", new HashMap<Target, String>() {{
        put(Target.Transit, "ON_MERCHANT_PREMISES_ATTENDED");
    }}),
    OnPremises_CardAcceptor_Unattended("2", new HashMap<Target, String>() {{
        put(Target.Transit, "ON_MERCHANT_PREMISES_UNATTENDED");
    }}),
    OffPremises_CardAcceptor_Attended("3", new HashMap<Target, String>() {{
        put(Target.Transit, "OFF_MERCHANT_PREMISES_ATTENDED");
    }}),
    OffPremises_CardAcceptor_Unattended("4", new HashMap<Target, String>() {{
        put(Target.Transit, "OFF_MERCHANT_PREMISES_UNATTENDED");
    }}),
    OnPremises_CardHolder_Unattended("5", new HashMap<Target, String>() {{
        put(Target.Transit, "ON_CUSTOMER_PREMISES_UNATTENDED");
    }}),
    OnPremises_CardAcceptor_Unattended_Mobile("9", new HashMap<Target, String>() {{
        put(Target.Transit, "ON_MERCHANT_PREMISES_MPOS");
    }}),
    Internet_With_SSL("S", new HashMap<Target, String>() {{
        put(Target.Transit, "ELECTRONIC_COMMERCE_TRANSACTION");
    }}),
    Deprecated("T", new HashMap<Target, String>() {{
        put(Target.Transit, "ELECTRONIC_COMMERCE_TRANSACTION");
    }}),
    Attended("attended", new HashMap<Target, String>() {{
        put(Target.Transit, "ON_MERCHANT_PREMISES_ATTENDED");
    }}),
    UnattendedAfd("unattendedAfd", new HashMap<Target, String>() {{
        put(Target.Transit, "ON_MERCHANT_PREMISES_UNATTENDED");
    }}),
    UnattendedCat("unattendedCat", new HashMap<Target, String>() {{
        put(Target.Transit, "ON_MERCHANT_PREMISES_CUSTOMER_POS");
    }}),
    UnattendedOffPremise("unattendedOffPremise", new HashMap<Target, String>() {{
        put(Target.Transit, "OFF_MERCHANT_PREMISES_CUSTOMER_POS");
    }});

    private final String value;
    private final HashMap<Target, String> transitValue;
    
    OperatingEnvironment(String value, HashMap<Target, String> transitValue) {
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

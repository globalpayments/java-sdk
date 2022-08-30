package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum OperatingEnvironment implements IStringConstant {
    NoTerminalUsed("0"),
    OnPremises_CardAcceptor_Attended("1"),
    OnPremises_CardAcceptor_Unattended("2"),
    OffPremises_CardAcceptor_Attended("3"),
    OffPremises_CardAcceptor_Unattended("4"),
    OnPremises_CardHolder_Unattended("5"),
    OnPremises_CardAcceptor_Unattended_Mobile("9"),
    Internet_With_SSL("S"),
    Deprecated("T"),
    Attended("attended"),
    UnattendedAfd("unattendedAfd"),
    UnattendedCat("unattendedCat"),
    UnattendedOffPremise("unattendedOffPremise");

    private final String value;
    OperatingEnvironment(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}

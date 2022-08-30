package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum DE22_CardHolderPresence implements IStringConstant {
    CardHolder_Present("0"),
    CardHolder_NotPresent("1"),
    CardHolder_NotPresent_MailOrder("2"),
    CardHolder_NotPresent_Telephone("3"),
    CardHolder_NotPresent_StandingAuth("4"),
    CardHolder_NotPresent_RecurringBilling("9"),
    CardHolder_NotPresent_Internet("S"),
    Secure3D_Authenticated("T"),
    Secure3D_AuthenticationAttempted("U"),
    Secure3D_AuthenticationFailed_Amexonly("V"),
    InApp_Using_DiscoverCard("W");

    private final String value;
    DE22_CardHolderPresence(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}


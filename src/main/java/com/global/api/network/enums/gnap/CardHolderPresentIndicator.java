package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum CardHolderPresentIndicator implements IStringConstant {
    CardHolderIsPresent("0"),
    UnspecifiedReason("1"),
    MailOrFaxOrder("2"),
    TelephoneOrAutomatedResponseUnitOrder("3"),
    StandingOrderOrRecurringPaymentTransaction("4"),
    ElectronicOrder("5");

    String value;
    CardHolderPresentIndicator(String value){this.value=value;}
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }

}

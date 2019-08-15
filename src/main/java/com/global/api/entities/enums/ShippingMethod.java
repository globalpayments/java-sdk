package com.global.api.entities.enums;

public enum ShippingMethod implements IStringConstant {
    BillingAddress("BILLING_ADDRESS"),
    VerifiedAddress("ANOTHER_VERIFIED_ADDRESS"),
    UnverifiedAddress("UNVERIFIED_ADDRESS"),
    ShipToStore("SHIP_TO_STORE"),
    DigitalGoods("DIGITAL_GOODS"),
    TravelOrEventTickets("TRAVEL_AND_EVENT_TICKETS"),
    Other("OTHER");

    String value;
    ShippingMethod(String value) { this.value = value; }
    public String getValue() { return value; }
    public byte[] getBytes() { return value.getBytes(); }
}

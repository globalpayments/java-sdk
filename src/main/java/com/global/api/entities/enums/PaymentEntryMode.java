package com.global.api.entities.enums;

import com.global.api.entities.enums.IStringConstant;

public enum PaymentEntryMode implements IStringConstant {
    Chip("CHIP"),
    ContactlessChip("CONTACTLESS_CHIP"),
    ContactlessSwipe("CONTACTLESS_SWIPE"),
    Ecom("ECOM"),
    InApp("IN_APP"),
    Manual("MANUAL"),
    Moto("MOTO"),
    Swipe("SWIPE");

    String value;
    PaymentEntryMode(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}

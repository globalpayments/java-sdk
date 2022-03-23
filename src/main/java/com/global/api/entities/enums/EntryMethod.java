package com.global.api.entities.enums;

public enum EntryMethod implements IStringConstant {
    Manual("manual"),
    Swipe("swipe"),
    Proximity("proximity"),
	ContactlessEMV("contactlessEMV"),
	ContactEMV("contactEMV"),
	ContactlessRFID("contactlessRFID"),
	QrCode("QrCode"),
	ContactlessRfidRingTechnology("contactlessRfidRingTechnology"),
    BarCode("3"),
    ManualDriverLicense("4"),
	NoTrackData("G"),
    ECommerce("ecommerce"),
    SecureEcommerce("secureEcommerce"),
    CardOnFileEcommerce("cardOnFileEcommerce");

    String value;
    EntryMethod(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}

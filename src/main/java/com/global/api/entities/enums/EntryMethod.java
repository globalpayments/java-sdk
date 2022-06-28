package com.global.api.entities.enums;

public enum EntryMethod implements IStringConstant {
    Manual("manual"),
    Swipe("swipe"),
    Proximity("proximity"),
    Unspecified("00"),
    MagneticStripeAndMSRFallback("02"),
    EMVIntegratedChipCard("05"),
    EmvContactlessCard("07"),
    CredentialOnFile("10"),
    TechnicalFallback("80"),
    ProximityVisaPayWaveMsdORPayPassMagORAmexExpressPay("91"),
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

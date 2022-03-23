package com.global.api.entities.enums;

public enum NTSEntryMethod implements  IStringConstant{
    ManualAttended("1"),
    MagneticStripeWithoutTrackDataAttended("0"),
    MagneticStripeWithoutTrackDataUnattended("2"),
    MagneticStripeTrack1DataAttended("5"),
    MagneticStripeTrack1DataUnattendedAfd("6"),
    MagneticStripeTrack2DataAttended("7"),
    MagneticStripeTrack2DataUnattendedAfd("8"),
    MagneticStripeTrack1DataUnattendedCat("H"),
    MagneticStripeTrack2DataUnattendedCat("J"),

    ContactEmvTrack2DataAttended("A"),
    ContactEmvTrack2DataUnattendedCat("C"),
    ContactEmvTrack2DataUnattendedAfd("E"),
    ContactEmvNoTrackDataAttended("L"),
    ContactEmvNoTrackDataUnattendedCat("N"),
    ContactEmvNoTrackDataUnattended("P"),

    ContactlessEmvTrack2DataAttended("B"),
    ContactlessEmvTrack2DataUnattendedCat("D"),
    ContactlessEmvTrack2DataUnattendedAfd("F"),
    ContactlessEmvNoTrackDataAttended("M"),
    ContactlessEmvNoTrackDataUnattendedCat("O"),
    ContactlessEmvNoTrackDataUnattended("Q"),

    ContactlessRfidTrack1DataUnattendedCat("I"),
    ContactlessRfidTrack1DataAttended("S"),
    ContactlessRfidTrack1DataUnattened("T"),
    ContactlessRfidTrack2DataAttended("U"),
    ContactlessRfidTrack2DataUnattendedCat("K"),
    ContactlessRfidTrack2DataUnattended("V"),

    QrCodeTrack2Data("R"),
    QrCodeTrack2DataAfd("Y"),
    QrCodeTrack2DataCat("Z"),

    ContactlessRfidRingTechnologyTrack1Data("W"),
    ContactlessRfidRingTechnologyTrack2Data("X"),
    BarCode("3"),
    ECommerceNoTrackDataAttended("a"),
    ECommerceNoTrackDataUnattendedAfd("b"),
    ECommerceNoTrackDataUnattendedCat("c"),
    ECommerceNoTrackDataUnattended("d"),
    SecureEcommerceNoTrackDataAttended("e"),
    SecureEcommerceNoTrackDataUnattendedAfd("f"),
    SecureEcommerceNoTrackDataUnattendedCat("g"),
    SecureEcommerceNoTrackDataUnattended("h"),
    CardOnFileEcommerceNoTrackDataAttended("i"),
    CardOnFileEcommerceNoTrackDataUnattendedAfd("j"),
    CardOnFileEcommerceNoTrackDataUnattendedCat("k"),
    CardOnFileECommerceNoTrackDataUnattended("l");

    String value;
    NTSEntryMethod(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }

}

package com.global.api.entities.enums;

public enum DebitAuthorizerCode implements IStringConstant {
    NonPinDebitCard("00"),
    StarSoutheast("02"),
    PULSE("03"),
    StarCentral("04"),
    StarNortheast("06"),
    Culiance("07"),
    NYCE("08"),
    AFFN_Network("10"),
    Interlink("12"),
    StarWest("13"),
    Maestro("16"),
    ACCEL("21"),
    INMAR_And_INMAR_eWIC("30"),
    Quest_JPM_EBT("33"),
    Conduent_EBT("34"),
    AlaskaOption("35"),
    Shazam("36"),
    VisaReadyLink("38"),
    NationalPaymentCard("42"),
    RevolutionMoney("43"),
    Visa_PIN_POS("44"),
    UnknownAuthorizer("59");

    String value;
    DebitAuthorizerCode(String value) {
        this.value = value;
    }
    public String getValue() {
        return this.value;
    }
    public byte[] getBytes() {
        return this.value.getBytes();
    }
}

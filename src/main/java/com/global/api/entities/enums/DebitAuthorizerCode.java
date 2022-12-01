package com.global.api.entities.enums;

public enum DebitAuthorizerCode implements IStringConstant {
    NonPinDebitCard("00"),
    StarAndStarPINless("02"),
    PulseAndPulsePINless("03"),
    StarCentral("04"),
    StarNortheast("06"),
    CulianceAndCuliancePINless("07"),
    NYCE("08"),
    AFFN_Network("10"),
    Interlink("12"),
    StarWest("13"),
    Maestro("16"),
    ACCEL("21"),
    INMAR_And_INMAR_eWIC("30"),
    AlaskaOption("35"),
    Solutran_EBT_And_Solutran_eWIC("32"),
    QuestEBTAndQuesteWIC("33"),
    Conduent_EBT_And_Conduent_eWIC("34"),
    Shazam("36"),
    ReadyLink("38"),
    Zipline("42"),
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

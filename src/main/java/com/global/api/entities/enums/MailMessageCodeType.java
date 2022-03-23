package com.global.api.entities.enums;

public enum MailMessageCodeType implements IStringConstant {
    Miscellaneous("0"),
    TankReading("1"),
    FuelNeeded("2"),
    FSC("3"),
    FuelSalesVolume("4"),
    VSATBroadbandMai("5"),
    CarWash("6"),
    Loyalty("7"),
    TerminalConfigurationMail("8"),
    BillOfLading("9");

    String value;
    MailMessageCodeType(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
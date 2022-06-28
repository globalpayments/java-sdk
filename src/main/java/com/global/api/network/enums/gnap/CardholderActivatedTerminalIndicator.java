package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum CardholderActivatedTerminalIndicator implements IStringConstant {
    NotACATTransaction("0"),
    AutomatedDispensingMachineWithPIN("1"),
    SelfServiceTerminal("2"),
    LimitedAmountTerminal("3"),
    InFlightCommerce("4"),
    Reserved("5"),
    ECOMM("6"),
    TransponderTransaction("7"),
    ReservedForFutureUse("8"),
    MPOSTransaction("9");
    String value;
    CardholderActivatedTerminalIndicator(String value){this.value=value;}
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}

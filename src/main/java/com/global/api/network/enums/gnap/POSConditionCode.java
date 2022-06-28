package com.global.api.network.enums.gnap;


import com.global.api.entities.enums.IStringConstant;

public enum POSConditionCode implements IStringConstant {
    StandAloneTerminal("00"),
    ElectronicCashRegisterInterfaceIntegrated("04"),
    PreAuthCompletionRequest("06"),
    UnattendedTerminalUnableToRetainCard("27");

    String value;
    POSConditionCode(String value){this.value=value;}

    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }


    }

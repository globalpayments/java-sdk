package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum PINCapability implements IStringConstant {
    PINEntryCapable("1"),
    NoPINEntryCapability("2"),
    TraditionalTerminalIsNotPresent("3");

    String value;
    PINCapability(String value) { this.value=value;}
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }

    }

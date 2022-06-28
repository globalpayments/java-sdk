package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum TerminalType implements IStringConstant {
    IntegratedSolutions("IS"),
    StandAloneOrSemiIntegratedSolutions("TT");

    String value;
    TerminalType(String value) { this.value=value;}
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}

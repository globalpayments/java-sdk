package com.global.api.network.enums.nts;

import com.global.api.entities.enums.IStringConstant;

public enum ServicingHostName implements IStringConstant {
    EMVPDLDownloadResponse(" "),
    Lenexa("0"),
    Reno("1"),
    Columbus("2"),
    Lenexa1("8");

    final String value;

    ServicingHostName(String value){
        this.value = value;
    }
    @Override
    public byte[] getBytes() {
        return value.getBytes();
    }

    @Override
    public String getValue() {
        return value;
    }
}

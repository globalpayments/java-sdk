package com.global.api.network.enums.nts;

import com.global.api.entities.enums.IStringConstant;

public enum PDLParameterType implements IStringConstant {
    RequestMagnumPdl("04"),
    MagnumPDLConfirm("05"),
    RequestEMVPDL("06"),
    EMVPDLConfirm("07");

    final private String value;

    PDLParameterType(String value){
        this.value = value;
    }
    @Override
    public byte[] getBytes() {
        return this.value.getBytes();
    }

    @Override
    public String getValue() {
        return this.value;
    }
}

package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum DraftCaptureFlag implements IStringConstant {
    DraftCapture("1"),
    NonDraftCapture("0");

    String value;

    DraftCaptureFlag(String value){ this.value=value;}

    @Override
    public byte[] getBytes() {
        return value.getBytes();
    }

    public String getValue(){ return this.value;}
}

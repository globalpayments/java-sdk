package com.global.api.network.enums.nts;

import com.global.api.entities.enums.IStringConstant;

public enum PDLEndOfTableFlag implements IStringConstant {
    EndOfTable("Y"),
    NotEndOfTable("N"),
    DownloadConfirmation("C");

    final private String value;

    PDLEndOfTableFlag(String value){
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

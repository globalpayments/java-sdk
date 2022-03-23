package com.global.api.network.enums.nts;

import com.global.api.entities.enums.IStringConstant;

public enum PendingRequestIndicator implements IStringConstant {
    EmvPdlDownloadResponse(" "),
    NoPendingMessage("0"),
    MailPending("1"),
    ParameterDataLoadPending("2"),
    EmvPdlPending("3"),
    MultipleMessagePending("4");

    final String value;

    PendingRequestIndicator(String value){
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

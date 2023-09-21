package com.global.api.terminals.genius.enums;

import com.global.api.entities.enums.IStringConstant;

import java.nio.charset.StandardCharsets;

public enum TransactionIdType implements IStringConstant {
    CLIENT_TRANSACTION_ID("CLIENT_TRANSACTION_ID"),
    GATEWAY_TRANSACTION_ID("GATEWAY_TRANSACTION_ID");

    final String value;

    TransactionIdType(String value){
        this.value = value;
    }

    @Override
    public byte[] getBytes() {
        return this.value.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getValue() {
        return this.value;
    }
}

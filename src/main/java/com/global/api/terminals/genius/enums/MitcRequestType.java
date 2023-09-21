package com.global.api.terminals.genius.enums;

import com.global.api.entities.enums.IStringConstant;

public enum MitcRequestType implements IStringConstant {
    CARD_PRESENT_SALE("CARD_PRESENT_SALE"),
    CARD_PRESENT_REFUND("CARD_PRESENT_REFUND"),
    REPORT_SALE_GATEWAY_ID("REPORT_SALE_GATEWAY_ID"),
    REPORT_SALE_CLIENT_ID("REPORT_SALE_CLIENT_ID"),
    REPORT_REFUND_GATEWAY_ID("REPORT_REFUND_GATEWAY_ID"),
    REPORT_REFUND_CLIENT_ID("REPORT_REFUND_CLIENT_ID"),
    REFUND_BY_CLIENT_ID("REFUND_BY_CLIENT_ID"),
    VOID_CREDIT_SALE("VOID_CREDIT_SALE"),
    VOID_DEBIT_SALE("VOID_DEBIT_SALE"),
    VOID_REFUND("VOID_REFUND");


    private final String value;

    MitcRequestType(String value){
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

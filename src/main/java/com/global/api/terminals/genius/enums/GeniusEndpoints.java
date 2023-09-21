package com.global.api.terminals.genius.enums;

import com.global.api.entities.enums.IStringConstant;
import com.global.api.terminals.genius.request.GeniusMitcRequest;

public enum GeniusEndpoints implements IStringConstant {
    CARD_PRESENT_SALE("/cardpresent/sales",GeniusMitcRequest.HttpMethod.POST),
    CARD_PRESENT_REFUND("/cardpresent/returns",GeniusMitcRequest.HttpMethod.POST),
    REPORT_SALE_CLIENT_ID("/card/sales/reference_id/%s",GeniusMitcRequest.HttpMethod.GET),
    REPORT_REFUND_CLIENT_ID("/card/returns/reference_id/%s",GeniusMitcRequest.HttpMethod.GET),
    REFUND_BY_CLIENT_ID("/creditsales/reference_id/%s/creditreturns",GeniusMitcRequest.HttpMethod.POST),
    VOID_CREDIT_SALE("/creditsales/reference_id/%s/voids",GeniusMitcRequest.HttpMethod.PUT),
    VOID_DEBIT_SALE("/debitsales/reference_id/%s/voids",GeniusMitcRequest.HttpMethod.PUT),
    VOID_REFUND("/creditreturns/reference_id/%s/voids",GeniusMitcRequest.HttpMethod.PUT);

    final String endpoint;
    final GeniusMitcRequest.HttpMethod method;

    GeniusEndpoints(String endpoint,  GeniusMitcRequest.HttpMethod method){
        this.endpoint = endpoint;
        this.method = method;
    }
    @Override
    public byte[] getBytes() {
        return this.endpoint.getBytes();
    }

    @Override
    public String getValue() {
        return this.endpoint;
    }

    public GeniusMitcRequest.HttpMethod getMethod() {
        return this.method;
    }
}

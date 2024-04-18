package com.global.api.gateways.bill_pay.requests;

import com.global.api.utils.ElementTree;

public class MakeQuickPayBlindPaymentReturnTokenRequest extends MakeQuickPayBlindPaymentRequest{
    public MakeQuickPayBlindPaymentReturnTokenRequest(ElementTree et) {
        super(et);
    }

    @Override
    protected String getMethodElementTagName() {
        return "bil:MakeQuickPayBlindPaymentReturnToken";
    }

}

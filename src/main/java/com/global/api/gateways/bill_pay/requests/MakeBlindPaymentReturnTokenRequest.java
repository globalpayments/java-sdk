package com.global.api.gateways.bill_pay.requests;

import com.global.api.utils.ElementTree;

public class MakeBlindPaymentReturnTokenRequest extends MakeBlindPaymentRequest {
    public MakeBlindPaymentReturnTokenRequest(ElementTree et) {
        super(et);
    }

    @Override
    protected String getMethodElementTagName() {
        return "bil:MakeBlindPaymentReturnToken";
    }

    @Override
    protected String getRequestElementTagName() {
        return "bil:MakePaymentReturnTokenRequest";
    }
}

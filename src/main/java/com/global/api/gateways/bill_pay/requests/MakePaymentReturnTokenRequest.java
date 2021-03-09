package com.global.api.gateways.bill_pay.requests;

import com.global.api.utils.ElementTree;

public class MakePaymentReturnTokenRequest extends MakeBlindPaymentRequest {
    public MakePaymentReturnTokenRequest(ElementTree et) {
        super(et);
    }

    @Override
    protected String getMethodElementTagName() {
        return "bil:MakePaymentReturnToken";
    }

    @Override
    protected String getRequestElementTagName() {
        return "bil:MakePaymentReturnTokenRequest";
    }
}

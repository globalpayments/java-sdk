package com.global.api.gateways.bill_pay.requests;

import com.global.api.utils.ElementTree;

public class MakePaymentRequest extends MakeBlindPaymentRequest {
    public MakePaymentRequest(ElementTree et) {
        super(et);
    }

    @Override
    protected String getMethodElementTagName() {
        return "bil:MakePayment";
    }

    @Override
    protected String getRequestElementTagName() {
        return "bil:MakeE3PaymentRequest";
    }
}

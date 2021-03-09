package com.global.api.gateways.bill_pay.requests;

import com.global.api.entities.billing.Credentials;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;

public class UpdateTokenRequest extends BillPayRequestBase {
    public UpdateTokenRequest(ElementTree et) {
        super(et);
    }

    public String build(Element envelope, CreditCardData card, Credentials credentials) {
        Element body = et.subElement(envelope, "soapenv:Body");
        Element methodElement = et.subElement(body, "bil:UpdateToken");
        Element requestElement = et.subElement(methodElement, "bil:UpdateTokenRequest");

        buildCredentials(requestElement, credentials);

        et.subElement(requestElement, "bdms:ExpirationMonth", card.getExpMonth());
        et.subElement(requestElement, "bdms:ExpirationYear", card.getExpYear());
        et.subElement(requestElement, "bdms:Token", card.getToken());

        return et.toString(envelope);
    }
}

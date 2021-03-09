package com.global.api.gateways.bill_pay.requests;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.billing.Credentials;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;

public class GetTokenRequest extends BillPayRequestBase {
    public GetTokenRequest(ElementTree et) {
        super(et);
    }

    public String build(Element envelope, AuthorizationBuilder builder, Credentials credentials) throws UnsupportedTransactionException {
        Element body = et.subElement(envelope, "soapenv:Body");
        Element methodElement = et.subElement(body, "bil:GetToken");
        Element requestElement = et.subElement(methodElement, "bil:GetTokenRequest");
        CreditCardData card = (CreditCardData) builder.getPaymentMethod();

        buildCredentials(requestElement, credentials);

        Element accountHolderDataElement = et.subElement(requestElement, "bdms:AccountHolderData");
        if (builder.getBillingAddress() != null) {
            et.subElement(accountHolderDataElement, "pos:Zip", builder.getBillingAddress().getPostalCode());
        }
        et.subElement(requestElement, "bdms:AccountNumber", card.getNumber());
        // PLACEHOLDER ClearTrackData
        // PLACEHOLDER E3KTB
        // PLACEHOLDER e3TrackData
        // PLACEHOLDER e3TrackType
        et.subElement(requestElement, "bdms:ExpirationMonth", card.getExpMonth());
        et.subElement(requestElement, "bdms:ExpirationYear", card.getExpYear());
        et.subElement(requestElement, "bdms:PaymentMethod", getPaymentMethodType(card.getPaymentMethodType()));

        return et.toString(envelope);
    }
}

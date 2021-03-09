package com.global.api.gateways.bill_pay.requests;

import com.global.api.builders.BillingBuilder;
import com.global.api.entities.billing.Credentials;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.eCheck;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;

public class GetConvenienceFeeRequest extends BillPayRequestBase {
    public GetConvenienceFeeRequest(ElementTree et) {
        super(et);
    }

    public String build(Element envelope, BillingBuilder builder, Credentials credentials) {
        Element body = et.subElement(envelope, "soapenv:Body");
        Element methodElement = et.subElement(body, "bil:GetConvenienceFee");
        Element requestElement = et.subElement(methodElement, "bil:GetConvenienceFeeRequest");

        String accountNumber = null;
        String routingNumber = null;
        String paymentMethod = null;

        if (builder.getPaymentMethod() instanceof eCheck) {
            eCheck check = (eCheck) builder.getPaymentMethod();
            routingNumber = check.getRoutingNumber();
            paymentMethod = "ACH";
        } else if (builder.getPaymentMethod() instanceof CreditCardData) {
            CreditCardData credit = (CreditCardData) builder.getPaymentMethod();
            accountNumber = credit.getNumber();
        }

        buildCredentials(requestElement, credentials);

        et.subElement(requestElement, "bdms:BaseAmount", builder.getAmount());
        
        if (accountNumber != null) {
            et.subElement(requestElement, "bdms:CardNumber", accountNumber);
        }
        
        et.subElement(requestElement, "bdms:CardProcessingMethod", getCardProcessingMethod(builder.getPaymentMethod().getPaymentMethodType()));
        
        if (paymentMethod != null) {
            et.subElement(requestElement, "bdms:PaymentMethod", paymentMethod);
        }

        et.subElement(requestElement, "bdms:RoutingNumber", routingNumber);
        return et.toString(envelope);
    }
}

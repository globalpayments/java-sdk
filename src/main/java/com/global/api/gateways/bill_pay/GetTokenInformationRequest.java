package com.global.api.gateways.bill_pay;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.billing.Credentials;
import com.global.api.gateways.bill_pay.requests.BillPayRequestBase;
import com.global.api.paymentMethods.ITokenizable;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;

public class GetTokenInformationRequest extends BillPayRequestBase {
    public GetTokenInformationRequest(ElementTree et) {
        super(et);
    }

    public String build(Element envelope, AuthorizationBuilder builder, Credentials credentials) {
        Element body = et.subElement(envelope,"soapenv:Body");
        Element methodElement = et.subElement(body,"bil:GetTokenInformation");
        Element requestElement =  et.subElement(methodElement,"bil:request");
        ITokenizable tokenizablePayment = (ITokenizable)builder.getPaymentMethod();

        buildCredentials(requestElement, credentials);

        et.subElement(requestElement,"bdms:Token", tokenizablePayment.getToken());

        return et.toString(envelope);
    }
}


package com.global.api.gateways.bill_pay.requests;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.billing.Credentials;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.paymentMethods.eCheck;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;
import com.global.api.utils.StringUtils;

public class GetAchTokenRequest extends BillPayRequestBase {
    public GetAchTokenRequest(ElementTree et) {
        super(et);
    }

    public String build(Element envelope, AuthorizationBuilder builder, Credentials credentials) throws UnsupportedTransactionException {
        Element body = et.subElement(envelope, "soapenv:Body");
        Element methodElement = et.subElement(body, "bil:GetToken");
        Element requestElement = et.subElement(methodElement, "bil:GetTokenRequest");
        eCheck ach = (eCheck) builder.getPaymentMethod();

        buildCredentials(requestElement, credentials);

        et.subElement(requestElement, "bdms:ACHAccountType", getDepositType(ach.getCheckType()));
        et.subElement(requestElement, "bdms:ACHDepositType", getACHAccountType(ach.getAccountType()));
        et.subElement(requestElement, "bdms:ACHStandardEntryClass", ach.getSecCode());

        Element accountHolderDataElement = et.subElement(requestElement, "bdms:AccountHolderData");
        if (!StringUtils.isNullOrEmpty(ach.getCheckHolderName())) {
            String[] parts = ach.getCheckHolderName().split(" ");
            et.subElement(accountHolderDataElement, "pos:LastName", parts[parts.length - 1]);
        }

        et.subElement(requestElement, "bdms:AccountNumber", ach.getAccountNumber());
        et.subElement(requestElement, "bdms:PaymentMethod", getPaymentMethodType(ach.getPaymentMethodType()));
        et.subElement(requestElement, "bdms:RoutingNumber", ach.getRoutingNumber());

        return et.toString(envelope);
    }
}

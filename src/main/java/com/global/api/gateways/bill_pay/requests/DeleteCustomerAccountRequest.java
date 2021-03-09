package com.global.api.gateways.bill_pay.requests;

import com.global.api.entities.billing.Credentials;
import com.global.api.paymentMethods.RecurringPaymentMethod;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;

public class DeleteCustomerAccountRequest extends BillPayRequestBase {
    public DeleteCustomerAccountRequest(ElementTree et) {
        super(et);
    }

    public String build(Element envelope, Credentials credentials, RecurringPaymentMethod paymentMethod) {
        Element body = et.subElement(envelope, "soapenv:Body");
        Element methodElement = et.subElement(body, "bil:DeleteCustomerAccount");
        Element requestElement = et.subElement(methodElement, "bil:DeleteCustomerAccountRequest");

        buildCredentials(requestElement, credentials);

        et.subElement(requestElement, "bdms:CustomerAccountNameToDelete", paymentMethod.getId());
        et.subElement(requestElement, "bdms:MerchantCustomerID", paymentMethod.getCustomerKey());

        return et.toString(envelope);
    }
}

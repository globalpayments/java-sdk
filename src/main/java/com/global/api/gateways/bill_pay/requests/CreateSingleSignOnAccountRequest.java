package com.global.api.gateways.bill_pay.requests;

import com.global.api.entities.Customer;
import com.global.api.entities.billing.Credentials;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;

public class CreateSingleSignOnAccountRequest extends BillPayRequestBase {
    public CreateSingleSignOnAccountRequest(ElementTree et) {
        super(et);
    }

    public String build(Element envelope, Credentials credentials, Customer customer) {
        Element body = et.subElement(envelope, "soapenv:Body");
        Element methodElement = et.subElement(body, "bil:CreateSingleSignOnAccount");
        Element requestElement = et.subElement(methodElement, "bil:request");

        buildCredentials(requestElement, credentials);

        Element customerElement = et.subElement(requestElement, "bdms:Customer");

        buildCustomer(customerElement, customer);
        return et.toString(envelope);
    }
}

package com.global.api.gateways.bill_pay.requests;

import com.global.api.entities.Customer;
import com.global.api.entities.billing.Credentials;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;

public class UpdateSingleSignOnAccountRequest extends BillPayRequestBase {
    public UpdateSingleSignOnAccountRequest(ElementTree et) {
        super(et);
    }

    public String build(Element envelope, Credentials credentials, Customer customer) {
        Element body = et.subElement(envelope, "soapenv:Body");
        Element methodElement = et.subElement(body, "bil:UpdateSingleSignOnAccount");
        Element requestElement = et.subElement(methodElement, "bil:request");
        
        buildCredentials(requestElement, credentials);
        
        Element customerElement = et.subElement(requestElement, "bdms:Customer");
        buildCustomer(customerElement, customer);

        et.subElement(requestElement, "bdms:MerchantCustomerIDToUpdate", customer.getId());

        return et.toString(envelope);
    }
}

package com.global.api.gateways.bill_pay.requests;

import com.global.api.entities.billing.Credentials;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;

public class ClearLoadedBillsRequest extends BillPayRequestBase {
    public ClearLoadedBillsRequest(ElementTree et) {
        super(et);
    }

    public String build(Element envelope, Credentials credentials) {
        Element body = et.subElement(envelope, "soapenv:Body");
        Element methodElement = et.subElement(body, "bil:ClearLoadedBills");
        Element requestElement = et.subElement(methodElement, "bil:ClearLoadedBillsRequest");

        buildCredentials(requestElement, credentials);
        return et.toString(envelope);
    }
}

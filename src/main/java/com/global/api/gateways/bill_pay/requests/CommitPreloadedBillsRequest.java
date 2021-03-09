package com.global.api.gateways.bill_pay.requests;

import com.global.api.entities.billing.Credentials;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;

public class CommitPreloadedBillsRequest extends BillPayRequestBase {
    public CommitPreloadedBillsRequest(ElementTree et) {
        super(et);
    }

    public String build(Element envelope, Credentials credentials) {
        Element body = et.subElement(envelope, "soapenv:Body");
        Element methodElement = et.subElement(body, "bil:CommitPreloadedBills");
        Element requestElement = et.subElement(methodElement, "bil:CommitPreloadedBillsRequest");

        buildCredentials(requestElement, credentials);
        return et.toString(envelope);
    }
}

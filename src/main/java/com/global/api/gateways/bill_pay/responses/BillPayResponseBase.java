package com.global.api.gateways.bill_pay.responses;

import java.util.HashMap;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.gateways.bill_pay.IBillPayResponse;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;

public abstract class BillPayResponseBase<T> implements IBillPayResponse<T> {
    protected Element response;
    protected String responseTagName;

    public IBillPayResponse<T> withResponseTagName(String tagName) {
        this.responseTagName = tagName;
        return this;
    }

    public IBillPayResponse<T> withResponse(String response) throws ApiException {
        HashMap<String, String> namespaces = new HashMap<>();

        namespaces.put("s", "http://schemas.xmlsoap.org/soap/envelope/");
        namespaces.put("", "https://test.heartlandpaymentservices.net/BillingDataManagement/v3/BillingDataManagementService");
        namespaces.put("a", "http://schemas.datacontract.org/2004/07/BDMS.NewModel");
        namespaces.put("i", "http://www.w3.org/2001/XMLSchema-instance");

        this.response = ElementTree.parse(response, namespaces).get(responseTagName);
        return this;
    }

    protected String getFirstResponseCode(Element response) {
        Element message = response.get("a:Messages");
        return message.getString("a:Code");
    }

    protected String getFirstResponseMessage(Element response) {
        Element message = response.get("a:Messages");
        return message.getString("a:MessageDescription");
    }
}

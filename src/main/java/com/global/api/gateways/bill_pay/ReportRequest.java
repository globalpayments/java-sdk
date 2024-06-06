package com.global.api.gateways.bill_pay;

import com.global.api.builders.ReportBuilder;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.billing.Credentials;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.gateways.bill_pay.requests.GetTransactionByOrderIDRequest;
import com.global.api.gateways.bill_pay.responses.TransactionByOrderIDRequestResponse;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;

public class ReportRequest<T> extends GatewayRequestBase {
    protected static final String GET_TRANSACTION_BY_ORDER_ID = "GetTransactionByOrderID";
    protected static final String GET_TRANSACTION_BY_ORDER_ID_RESPONSE = "GetTransactionByOrderIDResponse";
    public ReportRequest(Credentials credentials, String serviceUrl, int timeout){
        this.credentials = credentials;
        this.serviceUrl = serviceUrl;
        this.timeout = timeout;
    }

    public T execute(ReportBuilder<T> builder) throws ApiException{

        ElementTree et = new ElementTree();
        Element envelope = createSOAPEnvelope(et, GET_TRANSACTION_BY_ORDER_ID);
        String request = new GetTransactionByOrderIDRequest(et)
                .build(envelope,builder,credentials);

        String response = doTransaction(request);

        TransactionSummary result = new TransactionByOrderIDRequestResponse()
                .withResponseTagName(GET_TRANSACTION_BY_ORDER_ID_RESPONSE)
                .withResponse(response)
                .map();

    return (T) result;
    }
}

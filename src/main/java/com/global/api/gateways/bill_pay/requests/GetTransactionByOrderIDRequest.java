package com.global.api.gateways.bill_pay.requests;

import com.global.api.builders.ReportBuilder;
import com.global.api.builders.TransactionReportBuilder;
import com.global.api.entities.billing.Credentials;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;

public class GetTransactionByOrderIDRequest extends BillPayRequestBase {
    protected static final String SOAPENV_BODY = "soapenv:Body";
    protected static final String BIL_GET_TRANSACTION_BY_ORDER_ID = "bil:GetTransactionByOrderID";
    protected static final String BIL_GET_TRANSACTION_BY_ORDER_ID_REQUEST = "bil:GetTransactionByOrderIDRequest";
    protected static final String BDMS_ORDER_ID = "bdms:OrderID";
    protected static final String BUILDER_EXCEPTION = "This method only support TransactionReportBuilder.";
    public GetTransactionByOrderIDRequest(ElementTree et) {
        super(et);
    }

    public String build(Element envelope, ReportBuilder builder, Credentials credentials) throws BuilderException {
        if (builder instanceof TransactionReportBuilder) {
            Element body = et.subElement(envelope, SOAPENV_BODY);
            Element methodElement = et.subElement(body, BIL_GET_TRANSACTION_BY_ORDER_ID);
            Element requestElement = et.subElement(methodElement, BIL_GET_TRANSACTION_BY_ORDER_ID_REQUEST);

            buildCredentials(requestElement, credentials);

            et.subElement(requestElement,BDMS_ORDER_ID,((TransactionReportBuilder<?>) builder).getTransactionId());

            return et.toString(envelope);
        }
        else
            throw new BuilderException(BUILDER_EXCEPTION);
    }
}

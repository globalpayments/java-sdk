package com.global.api.gateways.bill_pay.requests;

import java.math.BigDecimal;

import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.billing.Credentials;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;

public class ReversePaymentRequest extends BillPayRequestBase {
    public ReversePaymentRequest(ElementTree et) {
        super(et);
    }

    public String build(Element envelope, ManagementBuilder builder, Credentials credentials) throws UnsupportedTransactionException, BuilderException {
        Element body = et.subElement(envelope, "soapenv:Body");
        Element methodElement = et.subElement(body, "bil:ReversePayment");
        Element requestElement = et.subElement(methodElement, "bil:ReversePaymentRequest");

        validateReversal(builder);
        buildCredentials(requestElement, credentials);

        if (builder.getAmount().compareTo(new BigDecimal(0)) > 0) {
            et.subElement(requestElement, "bdms:BaseAmountToRefund", builder.getAmount());
        }

        Element billsToReverse = et.subElement(requestElement, "bdms:BillsToReverse");

        if (builder.getBills() != null && !builder.getBills().isEmpty()) {
            buildBillTransactions(billsToReverse, builder.getBills(), "bdms:ReversalBillTransaction", "bdms:AmountToReverse");
        }

        et.subElement(requestElement, "bdms:EndUserBrowserType", browserType);
        et.subElement(requestElement, "bdms:EndUserIPAddress", builder.getCustomerIpAddress());
        et.subElement(requestElement, "bdms:ExpectedFeeAmountToRefund", builder.getConvenienceAmount());
        et.subElement(requestElement, "bdms:OrderIDOfReversal", builder.getOrderId());
        // PLACEHOLDER ReversalReason
        et.subElement(requestElement, "bdms:Transaction_ID", ((TransactionReference) builder.getPaymentMethod()).getTransactionId());

        return et.toString(envelope);
    }
}

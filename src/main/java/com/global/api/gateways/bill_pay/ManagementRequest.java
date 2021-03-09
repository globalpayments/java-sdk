package com.global.api.gateways.bill_pay;

import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.Transaction;
import com.global.api.entities.billing.Credentials;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.gateways.bill_pay.requests.ReversePaymentRequest;
import com.global.api.gateways.bill_pay.requests.UpdateTokenRequest;
import com.global.api.gateways.bill_pay.responses.ReversalResponse;
import com.global.api.gateways.bill_pay.responses.UpdateTokenResponse;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;

public class ManagementRequest extends GatewayRequestBase {
    public ManagementRequest(Credentials credentials, String serviceUrl, int timeout) {
        this.credentials = credentials;
        this.serviceUrl = serviceUrl;
        this.timeout = timeout;
    }

    public Transaction execute(ManagementBuilder builder, boolean isBillDataHosted) throws ApiException {
        switch (builder.getTransactionType()) {
            case Refund:
            case Reversal:
            case Void:
                return reversePayment(builder);
            case TokenUpdate:
                if (builder.getPaymentMethod() instanceof CreditCardData) {
                    return updateToken((CreditCardData) builder.getPaymentMethod());
                }

                throw new UnsupportedTransactionException();
            default:
                throw new UnsupportedTransactionException();
        }
    }

    private Transaction reversePayment(ManagementBuilder builder) throws ApiException {
        ElementTree et = new ElementTree();
        Element envelope = createSOAPEnvelope(et, "ReversePayment");
        String request = new ReversePaymentRequest(et)
            .build(envelope, builder, credentials);

        String response = doTransaction(request);
        Transaction result = new ReversalResponse()
            .withResponseTagName("ReversePaymentResponse")
            .withResponse(response)
            .map();

        if (result.getResponseCode().equals("0")) {
            return result;
        }

        throw new GatewayException("There was an error attempting to reverse the payment", result.getResponseCode(), result.getResponseMessage());
    }

    private Transaction updateToken(CreditCardData card) throws ApiException {
        ElementTree et = new ElementTree();
        Element envelope = createSOAPEnvelope(et, "UpdateTokenExpirationDate");
        String request = new UpdateTokenRequest(et)
            .build(envelope, card, credentials);

        String response = doTransaction(request);
        Transaction result = new UpdateTokenResponse()
            .withResponseTagName("UpdateTokenExpirationDateResponse")
            .withResponse(response)
            .map();

        if (result.getResponseCode().equals("0")) {
            return result;
        }

        throw new GatewayException("There was an error attempting to the token expiry information", result.getResponseCode(), result.getResponseMessage());
    }
}

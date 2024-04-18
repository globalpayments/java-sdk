package com.global.api.gateways.bill_pay;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.Transaction;
import com.global.api.entities.billing.Credentials;
import com.global.api.entities.enums.PaymentMethodUsageMode;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.gateways.bill_pay.requests.*;
import com.global.api.gateways.bill_pay.responses.TokenInformationRequestResponse;
import com.global.api.gateways.bill_pay.responses.TokenRequestResponse;
import com.global.api.gateways.bill_pay.responses.TransactionResponse;
import com.global.api.paymentMethods.ITokenizable;
import com.global.api.paymentMethods.eCheck;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;
import com.global.api.utils.StringUtils;

public class AuthorizationRequest extends GatewayRequestBase {
    private static final String GENERIC_PAYMENT_EXCEPTION_MESSAGE = "An error occurred attempting to make the payment";
    private static final String MAKE_QUICK_PAY_BLIND_PAYMENT= "MakeQuickPayBlindPayment";
    private static final String MAKE_QUICK_PAY_BLIND_PAYMENT_RESPONSE= "MakeQuickPayBlindPaymentResponse";
    private static final String MAKE_QUICK_PAY_BLIND_PAYMENT_RETURN_TOKEN= "MakeQuickPayBlindPaymentReturnToken";
    private static final String MAKE_QUICK_PAY_BLIND_PAYMENT_RETURN_TOKEN_RESPONSE= "MakeQuickPayBlindPaymentReturnTokenResponse";
    private static final String ZERO= "0";
    public AuthorizationRequest(Credentials credentials, String serviceUrl, int timeout) {
        this.credentials = credentials;
        this.serviceUrl = serviceUrl;
        this.timeout = timeout;
    }

    public Transaction execute(AuthorizationBuilder builder, boolean isBillDataHosted)
            throws ApiException {
        switch (builder.getTransactionType()) {
            case Sale:
                if (isBillDataHosted) {
                    if (builder.isRequestMultiUseToken()) {
                        return makePaymentReturnToken(builder);
                    }

                    return makePayment(builder);
                }

                if (builder.isRequestMultiUseToken()) {
                    return ((builder.getPaymentMethodUsageMode() != null && builder.getPaymentMethodUsageMode().equals(PaymentMethodUsageMode.SINGLE)) ? makeQuickPayBlindPaymentReturnToken(builder) : makeBlindPaymentReturnToken(builder));
                }

                return ((builder.getPaymentMethodUsageMode() != null && builder.getPaymentMethodUsageMode().equals(PaymentMethodUsageMode.SINGLE)) ? makeQuickPayBlindPayment(builder) : makeBlindPayment(builder));
            case Verify:
                if (!builder.isRequestMultiUseToken()) {
                    throw new UnsupportedTransactionException();
                }

                if (builder.getPaymentMethod() instanceof eCheck) {
                    return getAchToken(builder);
                }

                return getToken(builder);
            case GetTokenInfo:
                return getTokenInformation(builder);
            default:
                throw new UnsupportedTransactionException();
        }
    }

    private Transaction makePaymentReturnToken(AuthorizationBuilder builder) throws ApiException {
        ElementTree et = new ElementTree();
        Element envelope = createSOAPEnvelope(et, "MakePaymentReturnToken");
        String request = new MakePaymentReturnTokenRequest(et)
            .build(envelope, builder, credentials);

        String response = doTransaction(request);
        Transaction result = new TransactionResponse()
            .withResponseTagName("MakePaymentReturnTokenResponse")
            .withResponse(response)
            .map();

        if (result.getResponseCode().equals("0")) {
            return result;
        }

        throw new GatewayException(GENERIC_PAYMENT_EXCEPTION_MESSAGE, result.getResponseCode(), result.getResponseMessage());
    }

    private Transaction makeBlindPaymentReturnToken(AuthorizationBuilder builder) throws ApiException {
        ElementTree et = new ElementTree();
        Element envelope = createSOAPEnvelope(et, "MakeBlindPaymentReturnToken");
        String request = new MakeBlindPaymentReturnTokenRequest(et)
            .build(envelope, builder, credentials);
        String response = doTransaction(request);
        Transaction result = new TransactionResponse()
            .withResponseTagName("MakeBlindPaymentReturnTokenResponse")
            .withResponse(response)
            .map();

        if (result.getResponseCode().equals("0")) {
            return result;
        }

        throw new GatewayException(GENERIC_PAYMENT_EXCEPTION_MESSAGE, result.getResponseCode(), result.getResponseMessage());
    }

    private Transaction makeBlindPayment(AuthorizationBuilder builder) throws ApiException {
        ElementTree et = new ElementTree();
        Element envelope = createSOAPEnvelope(et, "MakeBlindPayment");
        String request = new MakeBlindPaymentRequest(et)
            .build(envelope, builder, credentials);
        String response = doTransaction(request);
        Transaction result = new TransactionResponse()
            .withResponseTagName("MakeBlindPaymentResponse")
            .withResponse(response)
            .map();

        if (result.getResponseCode().equals("0")) {
            return result;
        }

        throw new GatewayException(GENERIC_PAYMENT_EXCEPTION_MESSAGE, result.getResponseCode(), result.getResponseMessage());
    }

    private Transaction makePayment(AuthorizationBuilder builder) throws ApiException {
        ElementTree et = new ElementTree();
        Element envelope = createSOAPEnvelope(et, "MakePayment");
        String request = new MakePaymentRequest(et)
            .build(envelope, builder, credentials);
        String response = doTransaction(request);
        Transaction result = new TransactionResponse()
            .withResponseTagName("MakePaymentResponse")
            .withResponse(response)
            .map();

        if (result.getResponseCode().equals("0")) {
            return result;
        }

        throw new GatewayException(GENERIC_PAYMENT_EXCEPTION_MESSAGE, result.getResponseCode(), result.getResponseMessage());
    }

    private Transaction getToken(AuthorizationBuilder builder) throws ApiException {
        ElementTree et = new ElementTree();
        Element envelope = createSOAPEnvelope(et, "GetToken");
        String request = new GetTokenRequest(et)
            .build(envelope, builder, credentials);
        String response = doTransaction(request);
        Transaction result = new TokenRequestResponse()
            .withResponseTagName("GetTokenResponse")
            .withResponse(response)
            .map();

        if (result.getResponseCode().equals("0")) {
            return result;
        }

        throw new GatewayException("An error occurred attempting to create the token", result.getResponseCode(), result.getResponseMessage());
    }

    private Transaction getAchToken(AuthorizationBuilder builder) throws ApiException {
        ElementTree et = new ElementTree();
        Element envelope = createSOAPEnvelope(et, "GetToken");
        String request = new GetAchTokenRequest(et)
            .build(envelope, builder, credentials);
        String response = doTransaction(request);
        Transaction result = new TokenRequestResponse()
            .withResponseTagName("GetTokenResponse")
            .withResponse(response)
            .map();

        if (result.getResponseCode().equals("0")) {
            return result;
        }

        throw new GatewayException("An error occurred attempting to create the token", result.getResponseCode(), result.getResponseMessage());
    }

    private Transaction getTokenInformation(AuthorizationBuilder builder) throws ApiException {
        String request;
        ElementTree et = new ElementTree();
        Element envelope = createSOAPEnvelope(et,"GetTokenInformation");
        ITokenizable tokenizablePayment;
        if (builder.getPaymentMethod() instanceof ITokenizable) {
            tokenizablePayment = (ITokenizable) builder.getPaymentMethod();
            if (StringUtils.isNullOrEmpty(tokenizablePayment.getToken())) {
                throw new BuilderException("Payment method has not been tokenized");
            }
            request = new GetTokenInformationRequest(et)
                    .build(envelope, builder, credentials);
        }
        else {
            throw new BuilderException("Token Information is currently only retrievable for Credit and eCheck payment methods.");
        }

        String response = doTransaction(request);
        Transaction result = new TokenInformationRequestResponse()
                .withResponseTagName("GetTokenInformationResponse")
                .withResponse(response)
                .map();

        if(result.getResponseCode().equals("0")) {
            return result;
        }
        throw  new GatewayException("message: " + "An error occurred attempting to retrieve token information. ResponseCode: " + result.getResponseCode() + " responseMessage: " + result.getResponseMessage() );
    }

    private Transaction makeQuickPayBlindPayment(AuthorizationBuilder builder) throws ApiException {
        ElementTree et = new ElementTree();
        Element envelope = createSOAPEnvelope(et, MAKE_QUICK_PAY_BLIND_PAYMENT);
        String request = new MakeQuickPayBlindPaymentRequest(et)
                .build(envelope, builder, credentials);
        String response = doTransaction(request);
        Transaction result = new TransactionResponse()
                .withResponseTagName(MAKE_QUICK_PAY_BLIND_PAYMENT_RESPONSE)
                .withResponse(response)
                .map();

        if (result.getResponseCode().equals(ZERO)) {
            return result;
        }
        throw new GatewayException(GENERIC_PAYMENT_EXCEPTION_MESSAGE, result.getResponseCode(), result.getResponseMessage());
    }

    private Transaction makeQuickPayBlindPaymentReturnToken(AuthorizationBuilder builder) throws ApiException {
        ElementTree et = new ElementTree();
        Element envelope = createSOAPEnvelope(et, MAKE_QUICK_PAY_BLIND_PAYMENT_RETURN_TOKEN);
        String request = new MakeQuickPayBlindPaymentReturnTokenRequest(et)
                .build(envelope, builder, credentials);
        String response = doTransaction(request);
        Transaction result = new TransactionResponse()
                .withResponseTagName(MAKE_QUICK_PAY_BLIND_PAYMENT_RETURN_TOKEN_RESPONSE)
                .withResponse(response)
                .map();

        if (result.getResponseCode().equals(ZERO)) {
            return result;
        }
        throw new GatewayException(GENERIC_PAYMENT_EXCEPTION_MESSAGE, result.getResponseCode(), result.getResponseMessage());
    }
}

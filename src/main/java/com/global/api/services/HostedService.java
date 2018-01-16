package com.global.api.services;

import com.global.api.ServicesConfig;
import com.global.api.ServicesContainer;
import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.utils.GenerationUtils;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.JsonEncoders;

import java.math.BigDecimal;
import java.util.HashMap;

public class HostedService {
    ServicesConfig _config;

    public HostedService(ServicesConfig config) throws ApiException {
        _config = config;
        ServicesContainer.configure(config);
    }

    public AuthorizationBuilder authorize() {
        return authorize(null);
    }
    public AuthorizationBuilder authorize(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Auth).withAmount(amount);
    }

    public AuthorizationBuilder charge() {
        return charge(null);
    }
    public AuthorizationBuilder charge(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Sale).withAmount(amount);
    }

    public AuthorizationBuilder verify() {
        return verify(null);
    }
    public AuthorizationBuilder verify(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Verify).withAmount(amount);
    }

    public Transaction parseResponse(String json) throws ApiException {
        return parseResponse(json, false);
    }
    public Transaction parseResponse(String json, boolean encoded) throws ApiException {
        JsonDoc response = JsonDoc.parse(json, encoded ? JsonEncoders.base64Encoder() : null);

        String timestamp = response.getString("TIMESTAMP");
        String merchantId = response.getString("MERCHANT_ID");
        String orderId = response.getString("ORDER_ID");
        String result = response.getString("RESULT");
        String message = response.getString("MESSAGE");
        String transactionId = response.getString("PASREF");
        String authCode = response.getString("AUTHCODE");

        String sha1Hash = response.getString("SHA1HASH");
        String hash = GenerationUtils.generateHash(_config.getSharedSecret(), timestamp, merchantId, orderId, result, message, transactionId, authCode);
        if (!hash.equals(sha1Hash))
            throw new ApiException("Incorrect hash. Please check your code and the Developers Documentation.");

        HashMap<String, String> rvalues = new HashMap<String, String>();
        for(String key: response.getKeys()) {
            String value = response.getString(key);
            if(value != null)
                rvalues.put(key, value);
        }

        TransactionReference ref = new TransactionReference();
        ref.setAuthCode(authCode);
        ref.setOrderId(orderId);
        ref.setPaymentMethodType(PaymentMethodType.Credit);
        ref.setTransactionId(transactionId);

        Transaction trans = new Transaction();
        trans.setAuthorizedAmount(response.getDecimal("AMOUNT"));
        trans.setCvnResponseCode(response.getString("CVNRESULT"));
        trans.setResponseCode(result);
        trans.setResponseMessage(message);
        trans.setAvsResponseCode(response.getString("AVSPOSTCODERESULT"));
        trans.setTransactionReference(ref);
        trans.setResponseValues(rvalues);

        return trans;
    }
}

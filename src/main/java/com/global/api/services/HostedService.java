package com.global.api.services;

import com.global.api.ServicesContainer;
import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.AlternativePaymentResponse;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.serviceConfigs.GpEcomConfig;
import com.global.api.utils.GenerationUtils;
import com.global.api.utils.IRequestEncoder;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.JsonEncoders;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HostedService {
    GpEcomConfig _config;

    public HostedService(GpEcomConfig config) throws ApiException {
        _config = config;
        ServicesContainer.configureService(config); // Configure the default Service
    }

    public HostedService(GpEcomConfig config, String configName) throws ApiException {
        _config = config;
        ServicesContainer.configureService(config, configName); // Configure a new service with the given configName
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
        return parseResponse(json, false, "default");
    }

    public Transaction parseResponse(String json, boolean encoded) throws ApiException {
        return parseResponse(json, encoded, "default");
    }

    public Transaction parseResponse(String json, boolean encoded, String configName) throws ApiException {
        JsonDoc response = JsonDoc.parse(json, encoded ? JsonEncoders.base64Encoder() : null);
        String merchantResponseUrl = response.getStringOrNull("MERCHANT_RESPONSE_URL");
        if (merchantResponseUrl == null) {
            response = mapTransactionStatusResponse(response, encoded ? JsonEncoders.base64Encoder() : null);
        }

        String timestamp = response.getString("TIMESTAMP");
        String merchantId = response.getString("MERCHANT_ID");
        String orderId = response.getString("ORDER_ID");
        String result = response.getString("RESULT");
        String message = response.getString("MESSAGE");
        String transactionId = response.getString("PASREF");
        String authCode = response.getStringOrNull("AUTHCODE");
        if (authCode == null) {
            authCode = "";
        }
        String paymentMethod = response.getStringOrNull("PAYMENTMETHOD");
        if (paymentMethod == null) {
            paymentMethod = "";
        }

        String sha1Hash = response.getString("SHA1HASH");
        String hash = GenerationUtils.generateHash(
                _config.getSharedSecret(),
                timestamp,
                merchantId,
                orderId,
                result,
                message,
                transactionId,
                merchantResponseUrl != null ? authCode : paymentMethod);

        if (!MessageDigest.isEqual(hash.getBytes(), sha1Hash.getBytes()))
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
        trans.setAutoSettleFlag(response.getString("AUTO_SETTLE_FLAG"));
        trans.setTimestamp(timestamp);

        if (response.getStringOrNull("PAYMENTMETHOD") != null) {
            AlternativePaymentResponse apm = new AlternativePaymentResponse();
            apm.setCountry(response.getStringOrNull("COUNTRY"));
            apm.setProviderName(response.getStringOrNull("PAYMENTMETHOD"));
            apm.setPaymentStatus(response.getStringOrNull("TRANSACTION_STATUS"));
            apm.setReasonCode(response.getStringOrNull("PAYMENT_PURPOSE"));
            apm.setAccountHolderName(response.getStringOrNull("ACCOUNT_HOLDER_NAME"));
            trans.setAlternativePaymentResponse(apm);
        }

        trans.setResponseValues(rvalues);

        return trans;
    }

    private JsonDoc mapTransactionStatusResponse(JsonDoc response, IRequestEncoder iRequestEncoder) {

        JsonDoc newResponse = new JsonDoc(iRequestEncoder);

        newResponse.set("ACCOUNT_HOLDER_NAME", response.getStringOrNull("accountholdername"));
        newResponse.set("ACCOUNT_NUMBER", response.getStringOrNull("accountnumber"));
        newResponse.set("TIMESTAMP", response.getStringOrNull("timestamp"));
        newResponse.set("MERCHANT_ID", response.getStringOrNull("merchantid"));
        newResponse.set("BANK_CODE", response.getStringOrNull("bankcode"));
        newResponse.set("BANK_NAME", response.getStringOrNull("bankname"));
        newResponse.set("HPP_CUSTOMER_BIC", response.getStringOrNull("bic"));
        newResponse.set("COUNTRY", response.getStringOrNull("country"));
        newResponse.set("HPP_CUSTOMER_EMAIL", response.getStringOrNull("customeremail"));
        newResponse.set("TRANSACTION_STATUS", response.getStringOrNull("fundsstatus"));
        newResponse.set("IBAN", response.getStringOrNull("iban"));
        newResponse.set("MESSAGE", response.getStringOrNull("message"));
        newResponse.set("ORDER_ID", response.getStringOrNull("orderid"));
        newResponse.set("PASREF", response.getStringOrNull("pasref"));
        newResponse.set("PAYMENTMETHOD", response.getStringOrNull("paymentmethod"));
        newResponse.set("PAYMENT_PURPOSE", response.getStringOrNull("paymentpurpose"));
        newResponse.set("RESULT", response.getStringOrNull("result"));
        newResponse.set("SHA1HASH", response.getStringOrNull("sha1hash"));

        return newResponse;
    }

}
package com.global.api.tests.realex.hpp;

import com.global.api.ServicesConfig;
import com.global.api.ServicesContainer;
import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.AddressType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GatewayConfig;
import com.global.api.utils.GenerationUtils;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.JsonEncoders;
import com.global.api.utils.StringUtils;

public class RealexHppClient {
    private String serviceUrl;
    private String sharedSecret;
    private CreditCardData card;

    public RealexHppClient(String url, String sharedSecret) {
        this.serviceUrl = url;
        this.sharedSecret = sharedSecret;
        
        this.card = new CreditCardData();
        card.setNumber("4111111111111111");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");
        card.setCardHolderName("James Mason");
    }

    public String sendRequest(String request) throws ApiException {
        // gather information
        JsonDoc json = JsonDoc.parse(request, JsonEncoders.base64Encoder());
        if(json == null)
            throw new ApiException("Failed to parse request.");
        
        String timestamp = json.getString("TIMESTAMP");
        String merchantId = json.getString("MERCHANT_ID");
        String account = json.getString("ACCOUNT");
        String orderId = json.getString("ORDER_ID");
        String amount = json.getString("AMOUNT");
        String currency = json.getString("CURRENCY");
        boolean autoSettle = json.getString("AUTO_SETTLE_FLAG").equals("1");
        String requestHash = json.getString("SHA1HASH");

        // check hash
        String newhash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, orderId, amount, currency);
        if (!newhash.equals(requestHash)) {
            throw new ApiException("Incorrect hash. Please check your code and the Developers Documentation.");
        }

        // configure the container
        GatewayConfig config = new GatewayConfig();
        config.setMerchantId(merchantId);
        config.setAccountId(account);
        config.setServiceUrl("https://api.sandbox.realexpayments.com/epage-remote.cgi");
        config.setSharedSecret(sharedSecret);
        
        ServicesContainer.configureService(config, "realexResponder");

        // gather additional information
        String shippingCode = json.getString("SHIPPING_CODE");
        String shippingCountry = json.getString("SHIPPING_CO");
        String billingCode = json.getString("BILLING_CODE");
        String billingCountry = json.getString("BILLING_CO");

        // build request
        AuthorizationBuilder gatewayRequest = null;
        if (amount == null) {
            boolean validate = json.getString("VALIDATE_CARD_ONLY").equals("1");
            if (validate)
                gatewayRequest = card.verify();
            else gatewayRequest = card.verify().withRequestMultiUseToken(true);
        }
        else {
            if (autoSettle)
                gatewayRequest = card.charge(StringUtils.toAmount(amount));
            else gatewayRequest = card.authorize(StringUtils.toAmount(amount));
        }

        Address billing = null;
        if(billingCode != null || billingCountry != null) {
            billing = new Address();
            billing.setPostalCode(billingCode);
            billing.setCountry(billingCountry);
        }
        
        Address shipping = null;
        if(shippingCode != null || shippingCountry != null) {
            shipping = new Address();
            shipping.setPostalCode(shippingCode);
            shipping.setCountry(shippingCountry);
        }

        gatewayRequest.withCurrency(currency).withOrderId(orderId).withTimestamp(timestamp);
        if(billing != null)
            gatewayRequest.withAddress(billing);
        if(shipping != null)
            gatewayRequest.withAddress(shipping, AddressType.Shipping);

        Transaction gatewayResponse = gatewayRequest.execute("realexResponder");
        if (gatewayResponse.getResponseCode().equals("00"))
            return convertResponse(json, gatewayResponse);
        else throw new ApiException(gatewayResponse.getResponseMessage());
    }
    
    private String convertResponse(JsonDoc request, Transaction trans) {
        String merchantId = request.getString("MERCHANT_ID");
        String account = request.getString("ACCOUNT");

        // begin building response
        JsonDoc response = new JsonDoc(JsonEncoders.base64Encoder());
        response.set("MERCHANT_ID", merchantId);
        response.set("ACCOUNT", request.getString("ACCOUNT"));
        response.set("ORDER_ID", trans.getOrderId());
        response.set("TIMESTAMP", trans.getTimestamp());
        response.set("RESULT", trans.getResponseCode());
        response.set("PASREF", trans.getTransactionId());
        response.set("AUTHCODE", trans.getAuthorizationCode());
        response.set("AVSPOSTCODERESULT", trans.getAvsResponseCode());
        response.set("CVNRESULT", trans.getCvnResponseCode());
        response.set("HPP_LANG", request.getString("HPP_LANG"));
        response.set("SHIPPING_CODE", request.getString("SHIPPING_CODE"));
        response.set("SHIPPING_CO", request.getString("SHIPPING_CO"));
        response.set("BILLING_CODE", request.getString("BILLING_CODE"));
        response.set("BILLING_CO", request.getString("BILLING_CO"));
        response.set("ECI", request.getString("ECI"));
        response.set("CAVV", request.getString("CAVV"));
        response.set("XID", request.getString("XID"));
        response.set("MERCHANT_RESPONSE_URL", request.getString("MERCHANT_RESPONSE_URL"));
        response.set("CARD_PAYMENT_BUTTON", request.getString("CARD_PAYMENT_BUTTON"));
        response.set("MESSAGE", trans.getResponseMessage());
        response.set("AMOUNT", StringUtils.toNumeric(trans.getAuthorizedAmount()));
        response.set("SHA1HASH", GenerationUtils.generateHash(sharedSecret, trans.getTimestamp(), merchantId, trans.getOrderId(), trans.getResponseCode(), trans.getResponseMessage(), trans.getTransactionId(), trans.getAuthorizationCode()));

        return response.toString();
    }
}

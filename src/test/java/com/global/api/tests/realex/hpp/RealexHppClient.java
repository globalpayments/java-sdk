package com.global.api.tests.realex.hpp;

import com.global.api.ServicesContainer;
import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.BankPaymentBuilder;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.gateways.GatewayResponse;
import com.global.api.paymentMethods.*;
import com.global.api.serviceConfigs.GpEcomConfig;
import com.global.api.utils.*;
import lombok.var;
import org.apache.http.HttpStatus;

import java.util.ArrayList;

public class RealexHppClient {
    private String serviceUrl;
    private String sharedSecret;
    private IPaymentMethod paymentMethod;
    private ShaHashType shaHashType;

    // TODO: Check why in .NET the url is not needed
    public RealexHppClient(String url, String sharedSecret) {
        this.serviceUrl = url;
        this.sharedSecret = sharedSecret;
        this.shaHashType = ShaHashType.SHA1;
    }

    public RealexHppClient(String url, String sharedSecret, ShaHashType shaHashType) {
        this.serviceUrl = url;
        this.sharedSecret = sharedSecret;
        this.shaHashType = shaHashType != null ? shaHashType : ShaHashType.SHA1;
    }

    public String sendRequest(String request) throws ApiException {
        // gather information
        IRequestEncoder encoder = request.contains("\"HPP_VERSION\":\"2\"") ? null : JsonEncoders.base64Encoder();
        JsonDoc json = JsonDoc.parse(request, encoder);

        if(json == null)
            throw new ApiException("Failed to parse request.");
        
        String timestamp = json.getString("TIMESTAMP");
        String merchantId = json.getString("MERCHANT_ID");
        String account = json.getString("ACCOUNT");
        String orderId = json.getString("ORDER_ID");
        String amount = json.getString("AMOUNT");
        String currency = json.getString("CURRENCY");
        boolean autoSettle = json.getString("AUTO_SETTLE_FLAG").equals("1");
        String description = json.getString("COMMENT1");
        String shaHashTagName = shaHashType + "HASH";
        String requestHash = json.getString(shaHashTagName);

        // gather additional information
        String shippingCode = json.getString("SHIPPING_CODE");
        String shippingCountry = json.getString("SHIPPING_CO");
        String billingCode = json.getString("BILLING_CODE");
        String billingCountry = json.getString("BILLING_CO");
        String fraudFilterMode = json.getString("HPP_FRAUDFILTER_MODE");

        ArrayList<String> hashParam = new ArrayList<>();
        hashParam.add(timestamp);
        hashParam.add(merchantId);
        hashParam.add(orderId);
        hashParam.add(amount);
        hashParam.add(currency);

        // create the card/APM/LPM/OB object
        if (json.has("PM_METHODS")) {

            String[] apmTypes = json.getString("PM_METHODS").split("\\|");
            String apmType = apmTypes[0];

            //OB
            if (apmTypesContains(apmTypes, HostedPaymentMethods.OB.toString())) {
                var card = new BankPayment();

                card.setSortCode(json.getString("HPP_OB_DST_ACCOUNT_SORT_CODE"));
                card.setAccountNumber(json.getString("HPP_OB_DST_ACCOUNT_NUMBER"));
                card.setAccountName(json.getString("HPP_OB_DST_ACCOUNT_NAME"));
                card.setBankPaymentType(BankPaymentType.valueOf(json.getString("HPP_OB_PAYMENT_SCHEME")));
                card.setIban(json.getString("HPP_OB_DST_ACCOUNT_IBAN"));
                card.setReturnUrl(json.getString("MERCHANT_RESPONSE_URL"));
                card.setStatusUpdateUrl(json.getString("HPP_TX_STATUS_URL"));

                paymentMethod = card;

                if (!StringUtils.isNullOrEmpty(card.getSortCode()))
                    hashParam.add(card.getSortCode());
                if (!StringUtils.isNullOrEmpty(card.getAccountNumber()))
                    hashParam.add(card.getAccountNumber());
                if (!StringUtils.isNullOrEmpty(card.getIban()))
                    hashParam.add(card.getIban());
            }
            else {
                AlternativePaymentMethod apm = new AlternativePaymentMethod(AlternativePaymentType.fromValue(apmType));
                apm.setReturnUrl(json.getString("MERCHANT_RESPONSE_URL"));
                apm.setStatusUpdateUrl(json.getString("HPP_TX_STATUS_URL"));

                if (apmType == AlternativePaymentType.PAYPAL.getValue()) {
                    //cancelUrl for Paypal example
                    apm.setCancelUrl("https://www.example.com/failure/cancelURL");
                }

                apm.setCountry(json.getString("HPP_CUSTOMER_COUNTRY"));
                apm.setAccountHolderName(
                        json.getString("HPP_CUSTOMER_FIRSTNAME") + " " + json.getString("HPP_CUSTOMER_LASTNAME"));

                paymentMethod = apm;
            }
        } else {
            CreditCardData card = new CreditCardData();
            card.setNumber("4006097467207025");
            card.setExpMonth(12);
            card.setExpYear(2025);
            card.setCvn("131");
            card.setCardHolderName("James Mason");

            paymentMethod = card;
        }

        // for stored card
        if (json.has("OFFER_SAVE_CARD")) {
            if (json.has("PAYER_REF")) {
                hashParam.add(json.getString("PAYER_REF"));
            }
            if (json.has("PMT_REF")) {
                hashParam.add(json.getString("PMT_REF"));
            }
        }

        if (json.has("HPP_FRAUDFILTER_MODE")) {
            hashParam.add(json.getString("HPP_FRAUDFILTER_MODE"));
        }

        // check hash
        String newhash = GenerationUtils.generateHash(sharedSecret, hashParam.toArray(new String[0]));
        if (!newhash.equals(requestHash)) {
            throw new ApiException("Incorrect hash. Please check your code and the Developers Documentation.");
        }

        // configure the container
        GpEcomConfig config = new GpEcomConfig();
        config.setMerchantId(merchantId);
        config.setAccountId(account);
        config.setServiceUrl("https://api.sandbox.realexpayments.com/epage-remote.cgi");
        config.setSharedSecret(sharedSecret);
        // Uncomment/Comment if you need to enable/disable logging the raw request/response
        // config.setEnableLogging(true);
        
        ServicesContainer.configureService(config, "realexResponder");

        // build request
        AuthorizationBuilder gatewayRequest = null;
        if (amount == null || amount.equals("000")) {
            boolean validate = json.getString("VALIDATE_CARD_ONLY").equals("1");
            if (validate) {
                gatewayRequest = ((CreditCardData) paymentMethod).verify();
            } else {
                gatewayRequest = ((CreditCardData) paymentMethod).verify().withRequestMultiUseToken(true);
            }
        } else {
            if (autoSettle) {
                if (paymentMethod instanceof CreditCardData) {
                    gatewayRequest = ((CreditCardData) paymentMethod).charge(StringUtils.toAmount(amount));
                }
                if (paymentMethod instanceof AlternativePaymentMethod) {
                    gatewayRequest = ((AlternativePaymentMethod) paymentMethod).charge(StringUtils.toAmount(amount));
                }
                if(paymentMethod instanceof BankPayment) {
                    var gatewayBankRequest =
                            addRemittanceRef(
                                    ((BankPayment) paymentMethod)
                                            .charge(StringUtils.toAmount(amount))
                                            .withCurrency(currency)
                                            .withDescription(description),
                                    json);

                    var gatewayResponse = gatewayBankRequest.execute();

                    if (gatewayResponse.getBankPaymentResponse().getPaymentStatus().equals("PAYMENT_INITIATED")) {
                        return buildResponse(HttpStatus.SC_OK, convertResponse(json, gatewayResponse)).getRawResponse();
                    } else {
                        return badRequest(gatewayResponse.getResponseMessage()).getRawResponse();
                    }
                }
            } else {
                gatewayRequest = ((CreditCardData) paymentMethod).authorize(StringUtils.toAmount(amount));
            }
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

        //handle fraud management
        if(fraudFilterMode != null) {
            gatewayRequest.withFraudFilter(FraudFilterMode.fromString(fraudFilterMode), getFraudFilterRules(json));
        }

        Transaction gatewayResponse = gatewayRequest.execute("realexResponder");
        if (gatewayResponse.getResponseCode().equals("00") || gatewayResponse.getResponseCode().equals("01") ) {
            return convertResponse(json, gatewayResponse);
        } else {
            throw new ApiException(gatewayResponse.getResponseMessage());
        }
    }

    private BankPaymentBuilder addRemittanceRef(BankPaymentBuilder gatewayRequest, JsonDoc json) {
        var REF_TYPE = json.getString("HPP_OB_REMITTANCE_REF_TYPE");
        var REF_VALUE = json.getString("HPP_OB_REMITTANCE_REF_VALUE");

        return
                gatewayRequest
                        .withRemittanceReference(RemittanceReferenceType.valueOf(REF_TYPE), REF_VALUE);
    }

    private FraudRuleCollection getFraudFilterRules(JsonDoc json) {
        FraudRuleCollection rules = new FraudRuleCollection();
        for(String hppKey : json.getKeys()) {
            if (hppKey.startsWith("HPP_FRAUDFILTER_RULE_")) {
                rules.addRule(hppKey.replace("HPP_FRAUDFILTER_RULE_", ""), FraudFilterMode.fromString(json.getString(hppKey)));
            }
        }
        return rules.getRules().isEmpty() ? null : rules;
    }

    private GatewayResponse buildResponse(int code, String reason) {
        GatewayResponse httpResponse = new GatewayResponse();

        httpResponse.setStatusCode(code);
        httpResponse.setRawResponse(reason);

        return httpResponse;
    }
    private GatewayResponse badRequest(String reason) {
        return buildResponse(HttpStatus.SC_BAD_REQUEST, reason);
    }

    private String convertBankResponse(JsonDoc request, Transaction trans) {
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
        response.set("DCC_INFO_REQUST", request.getString("DCC_INFO"));
        response.set("HPP_FRAUDFILTER_MODE", request.getString("HPP_FRAUDFILTER_MODE"));
        response.set("REDIRECT_URL", trans.getBankPaymentResponse().getRedirectUrl());

        if (trans.getAlternativePaymentResponse() != null) {
            AlternativePaymentResponse alternativePaymentResponse = trans.getAlternativePaymentResponse();
            response.set("HPP_CUSTOMER_FIRSTNAME", request.getString("HPP_CUSTOMER_FIRSTNAME"));
            response.set("HPP_CUSTOMER_LASTNAME", request.getString("HPP_CUSTOMER_LASTNAME"));
            response.set("HPP_CUSTOMER_COUNTRY", request.getString("HPP_CUSTOMER_COUNTRY"));
            response.set("PAYMENTMETHOD", alternativePaymentResponse.getProviderName());
            response.set("PAYMENTPURPOSE", alternativePaymentResponse.getPaymentPurpose());
            response.set("HPP_CUSTOMER_BANK_ACCOUNT", alternativePaymentResponse.getBankAccount());
        }

        response.set("TSS_INFO", request.getString("TSS_INFO"));

        return response.toString();
    }

    private String convertResponse(JsonDoc request, Transaction trans) {
        var merchantId = request.getString("MERCHANT_ID");
        var account = request.getString("ACCOUNT");

        // begin building response
        var response = new JsonDoc(JsonEncoders.base64Encoder());

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
        response.set("AMOUNT", trans.getAuthorizedAmount());
        response.set("SHA1HASH", GenerationUtils.generateHash(sharedSecret, trans.getTimestamp(), merchantId, trans.getOrderId(), trans.getResponseCode(), trans.getResponseMessage(), trans.getTransactionId(), trans.getAuthorizationCode()));
        response.set("DCC_INFO_REQUST", request.getString("DCC_INFO"));
        response.set("HPP_FRAUDFILTER_MODE", request.getString("HPP_FRAUDFILTER_MODE"));

        if (trans.getFraudResponse() != null) {
            response.set("HPP_FRAUDFILTER_RESULT", trans.getFraudResponse().getResult());

            for (FraudResponse.Rule fraudResponseRule : trans.getFraudResponse().getRules()) {
                response.set("HPP_FRAUDFILTER_RULE_" + fraudResponseRule.getId(), fraudResponseRule.getAction());
            }
        }

        if (trans.getAlternativePaymentResponse() != null) {
            AlternativePaymentResponse alternativePaymentResponse = trans.getAlternativePaymentResponse();
            response.set("HPP_CUSTOMER_FIRSTNAME", request.getString("HPP_CUSTOMER_FIRSTNAME"));
            response.set("HPP_CUSTOMER_LASTNAME", request.getString("HPP_CUSTOMER_LASTNAME"));
            response.set("HPP_CUSTOMER_COUNTRY", request.getString("HPP_CUSTOMER_COUNTRY"));
            response.set("PAYMENTMETHOD", alternativePaymentResponse.getProviderName());
            response.set("PAYMENTPURPOSE", alternativePaymentResponse.getPaymentPurpose());
            response.set("HPP_CUSTOMER_BANK_ACCOUNT", alternativePaymentResponse.getBankAccount());
        }

        return response.toString();
    }

    private boolean apmTypesContains(String[] apmTypes, String apmType) {
        for( String type : apmTypes) {
            if(apmType.equals(type)) {
                return true;
            }
        }
        return false;
    }

}

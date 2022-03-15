package com.global.api.gateways;

import com.global.api.builders.Secure3dBuilder;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.paymentMethods.ISecure3d;
import com.global.api.paymentMethods.RecurringPaymentMethod;
import com.global.api.utils.CardUtils;
import com.global.api.utils.GenerationUtils;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Gp3DSProvider extends RestGateway implements ISecure3dProvider {
    private String accountId;
    private String challengeNotificationUrl;
    private String merchantContactUrl;
    private String merchantId;
    private String methodNotificationUrl;
    private String sharedSecret;

    public Secure3dVersion getVersion() { return Secure3dVersion.TWO; }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
    public void setSharedSecret(String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }
    public void setChallengeNotificationUrl(String challengeNotificationUrl) {
        this.challengeNotificationUrl = challengeNotificationUrl;
    }
    public void setMerchantContactUrl(String merchantContactUrl) {
        this.merchantContactUrl = merchantContactUrl;
    }
    public void setMethodNotificationUrl(String methodNotificationUrl) {
        this.methodNotificationUrl = methodNotificationUrl;
    }

    public Transaction processSecure3d(Secure3dBuilder builder) throws ApiException {
        TransactionType transType = builder.getTransactionType();
        String timestamp = DateTime.now().toString("yyyy-MM-dd'T'hh:mm:ss.SSSSSS");
        IPaymentMethod paymentMethod = builder.getPaymentMethod();
        ISecure3d secure3d = (ISecure3d)paymentMethod;

        JsonDoc request = new JsonDoc();
        if(transType.equals(TransactionType.VerifyEnrolled)) {
            request.set("request_timestamp", timestamp)
                    .set("merchant_id", merchantId)
                    .set("account_id", accountId)
                    .set("method_notification_url", methodNotificationUrl);

            String hashValue = "";
            if(paymentMethod instanceof CreditCardData) {
                CreditCardData cardData = (CreditCardData)paymentMethod;
                request
                        .set("number", cardData.getNumber())
                        .set("scheme", mapCardScheme(CardUtils.getBaseCardType(cardData.getCardType()).toUpperCase()));
                hashValue = cardData.getNumber();
            }
            else if(paymentMethod instanceof RecurringPaymentMethod) {
                RecurringPaymentMethod storedCard = (RecurringPaymentMethod)paymentMethod;
                request.set("payer_reference", storedCard.getCustomerKey())
                        .set("payment_method_reference", storedCard.getKey());
                hashValue = storedCard.getCustomerKey();
            }

            String hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, hashValue);
            setAuthHeader(hash);

            String rawResponse = doTransaction("POST", "protocol-versions", request.toString());
            return mapResponse(rawResponse);
        }
        else  if(transType.equals(TransactionType.VerifySignature)) {
            String hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, builder.getServerTransactionId());
            setAuthHeader(hash);

            HashMap<String, String> queryValues = new HashMap<String, String>();
            queryValues.put("merchant_id", merchantId);
            queryValues.put("request_timestamp", timestamp);

            String rawResponse = doTransaction("GET", String.format("authentications/%s", builder.getServerTransactionId()), request.toString(), queryValues);
            return mapResponse(rawResponse);
        }
        else if(transType.equals(TransactionType.InitiateAuthentication)) {
            String orderId = builder.getOrderId();
            if(StringUtils.isNullOrEmpty(orderId)) {
                orderId = GenerationUtils.generateOrderId();
            }

            ThreeDSecure secureEcom = secure3d.getThreeDSecure();

            request.set("request_timestamp", timestamp)
                    .set("authentication_source", builder.getAuthenticationSource())
                    .set("authentication_request_type", builder.getAuthenticationRequestType())
                    .set("message_category", builder.getMessageCategory())
                    .set("message_version", "2.1.0")
                    .set("server_trans_id", secureEcom.getServerTransactionId())
                    .set("merchant_id", merchantId)
                    .set("account_id", accountId)
                    .set("challenge_notification_url", challengeNotificationUrl)
                    .set("challenge_request_indicator", builder.getChallengeRequestIndicator())
                    .set("method_url_completion", builder.getMethodUrlCompletion() != null ? builder.getMethodUrlCompletion().getValue() : "")
                    .set("merchant_contact_url", merchantContactUrl)
                    .set("merchant_initiated_request_type", builder.getMerchantInitiatedRequestType() != null ? builder.getMerchantInitiatedRequestType().getValue() : "")
                    .set("whitelist_status", builder.getWhitelistStatus() != null ? builder.getWhitelistStatus().toString() : "")
                    .set("decoupled_flow_request", builder.getDecoupledFlowRequest() != null ? builder.getDecoupledFlowRequest().toString() : "")
                    .set("decoupled_flow_timeout", builder.getDecoupledFlowTimeout())
                    .set("decoupled_notification_url", builder.getDecoupledNotificationUrl())
                    .set("enable_exemption_optimization", builder.isEnableExemptionOptimization());

            // card details
            String hashValue = "";
            JsonDoc cardDetailElement = request.subElement("card_detail");
            if(paymentMethod instanceof CreditCardData) {
                CreditCardData cardData = (CreditCardData)paymentMethod;
                hashValue = cardData.getNumber();

                cardDetailElement.set("number", cardData.getNumber())
                        .set("scheme", CardUtils.getBaseCardType(cardData.getCardType()).toUpperCase())
                        .set("expiry_month", cardData.getExpMonth().toString())
                        .set("expiry_year", cardData.getExpYear().toString().substring(2))
                        .set("full_name", cardData.getCardHolderName());

                if(!StringUtils.isNullOrEmpty(cardData.getCardHolderName())) {
                    String[] names = cardData.getCardHolderName().split("\\s+");
                    if(names.length >= 1) {
                        cardDetailElement.set("first_name", names[0]);
                    }
                    if(names.length >= 2) {
                        cardDetailElement.set("last_name", Arrays.stream(names).skip(1).collect(Collectors.joining(" ")));
                    }
                }
            }
            else if(paymentMethod instanceof RecurringPaymentMethod) {
                RecurringPaymentMethod storedCard = (RecurringPaymentMethod)paymentMethod;
                hashValue = storedCard.getCustomerKey();

                cardDetailElement.set("payer_reference", storedCard.getCustomerKey())
                        .set("payment_method_reference", storedCard.getKey());
            }

            // order details
            JsonDoc order = request.subElement("order")
                    .set("amount", builder.getAmount())
                    .set("currency", builder.getCurrency())
                    .set("id", orderId)
                    .set("address_match_indicator", builder.isAddressMatchIndicator() ? "true" : "false")
                    .set("date_time_created", builder.getOrderCreateDate(), "yyyy-MM-dd'T'hh:mm'Z'")
                    .set("gift_card_count", builder.getGiftCardCount())
                    .set("gift_card_currency", builder.getGiftCardCurrency())
                    .set("gift_card_amount", builder.getGiftCardAmount())
                    .set("delivery_email", builder.getDeliveryEmail())
                    .set("delivery_timeframe", builder.getDeliveryTimeframe())
                    .set("shipping_method", builder.getShippingMethod())
                    .set("shipping_name_matches_cardholder_name", builder.getShippingNameMatchesCardHolderName())
                    .set("preorder_indicator", builder.getPreOrderIndicator())
                    .set("reorder_indicator", builder.getReorderIndicator())
                    .set("transaction_type", builder.getOrderTransactionType())
                    .set("preorder_availability_date", builder.getPreOrderAvailabilityDate(), "yyyy-MM-dd")
            ;

            // shipping address
            Address shippingAddress = builder.getShippingAddress();
            if(shippingAddress != null) {
                order.subElement("shipping_address")
                        .set("line1", shippingAddress.getStreetAddress1())
                        .set("line2", shippingAddress.getStreetAddress2())
                        .set("line3", shippingAddress.getStreetAddress3())
                        .set("city", shippingAddress.getCity())
                        .set("postal_code", shippingAddress.getPostalCode())
                        .set("state", shippingAddress.getState())
                        .set("country", shippingAddress.getCountryCode());
            }

            // payer
            JsonDoc payer = request.subElement("payer");
            payer.set("email", builder.getCustomerEmail())
                    .set("id", builder.getCustomerAccountId())
                    .set("account_age", builder.getAccountAgeIndicator())
                    .set("account_creation_date", builder.getAccountCreateDate(), "yyyy-MM-dd")
                    .set("account_change_indicator", builder.getAccountChangeIndicator())
                    .set("account_change_date", builder.getAccountChangeDate(), "yyyy-MM-dd")
                    .set("account_password_change_indicator", builder.getPasswordChangeIndicator())
                    .set("account_password_change_date", builder.getPasswordChangeDate(), "yyyy-MM-dd")
                    .set("payment_account_age_indicator", builder.getPaymentAgeIndicator())
                    .set("payment_account_creation_date", builder.getPaymentAccountCreateDate(), "yyyy-MM-dd")
                    .set("purchase_count_last_6months", builder.getNumberOfPurchasesInLastSixMonths())
                    .set("transaction_count_last_24hours", builder.getNumberOfTransactionsInLast24Hours())
                    .set("transaction_count_last_year", builder.getNumberOfTransactionsInLastYear())
                    .set("provision_attempt_count_last_24hours", builder.getNumberOfAddCardAttemptsInLast24Hours())
                    .set("shipping_address_creation_indicator", builder.getShippingAddressUsageIndicator())
                    .set("shipping_address_creation_date", builder.getShippingAddressCreateDate(), "yyyy-MM-dd")
            ;

            // suspicious activity
            if(builder.getPreviousSuspiciousActivity() != null) {
                payer.set("suspicious_account_activity", builder.getPreviousSuspiciousActivity() ? "SUSPICIOUS_ACTIVITY" : "NO_SUSPICIOUS_ACTIVITY");
            }

            // home phone
            if(!StringUtils.isNullOrEmpty(builder.getHomeNumber())) {
                payer.subElement("home_phone")
                        .set("country_code", StringUtils.toNumeric(builder.getHomeCountryCode()))
                        .set("subscriber_number", StringUtils.toNumeric(builder.getHomeNumber()));
            }

            // work phone
            if(!StringUtils.isNullOrEmpty(builder.getWorkNumber())) {
                payer.subElement("work_phone")
                        .set("country_code", StringUtils.toNumeric(builder.getWorkCountryCode()))
                        .set("subscriber_number", StringUtils.toNumeric((builder.getWorkNumber())));
            }

            // payer login data
            if(builder.hasPayerLoginData()) {
                request.subElement("payer_login_data")
                        .set("authentication_data", builder.getCustomerAuthenticationData())
                        .set("authentication_timestamp", builder.getCustomerAuthenticationTimestamp(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                        .set("authentication_type", builder.getCustomerAuthenticationMethod());
            }

            // prior authentication data
            if(builder.hasPriorAuthenticationData()) {
                request.subElement("payer_prior_three_ds_authentication_data")
                        .set("authentication_method", builder.getPriorAuthenticationMethod())
                        .set("acs_transaction_id", builder.getPriorAuthenticationTransactionId())
                        .set("authentication_timestamp", builder.getPriorAuthenticationTimestamp(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                        .set("authentication_data", builder.getPriorAuthenticationData())
                ;
            }

            // recurring authorization data
            if(builder.hasRecurringAuthData()) {
                request.subElement("recurring_authorization_data")
                        .set("max_number_of_instalments", builder.getMaxNumberOfInstallments())
                        .set("frequency", builder.getRecurringAuthorizationFrequency())
                        .set("expiry_date", builder.getRecurringAuthorizationExpiryDate(), "yyyy-MM-dd");
            }

            // billing details
            Address billingAddress = builder.getBillingAddress();
            if(billingAddress != null) {
                payer.subElement("billing_address")
                        .set("line1", billingAddress.getStreetAddress1())
                        .set("line2", billingAddress.getStreetAddress2())
                        .set("line3", billingAddress.getStreetAddress3())
                        .set("city", billingAddress.getCity())
                        .set("postal_code", billingAddress.getPostalCode())
                        .set("state", billingAddress.getState())
                        .set("country", billingAddress.getCountryCode());
            }

            // mobile phone
            if(!StringUtils.isNullOrEmpty(builder.getMobileNumber())) {
                payer.subElement("mobile_phone")
                        .set("country_code", StringUtils.toNumeric(builder.getMobileCountryCode()))
                        .set("subscriber_number", StringUtils.toNumeric(builder.getMobileNumber()));
            }

            // browser_data
            BrowserData broswerData = builder.getBrowserData();
            if(broswerData != null) {
                request.subElement("browser_data")
                        .set("accept_header", broswerData.getAcceptHeader())
                        .set("color_depth", broswerData.getColorDepth())
                        .set("ip", broswerData.getIpAddress())
                        .set("java_enabled", broswerData.isJavaEnabled())
                        .set("javascript_enabled", broswerData.isJavaScriptEnabled())
                        .set("language", broswerData.getLanguage())
                        .set("screen_height", broswerData.getScreenHeight())
                        .set("screen_width", broswerData.getScreenWidth())
                        .set("challenge_window_size", broswerData.getChallengeWindowSize())
                        .set("timezone", broswerData.getTimezone())
                        .set("user_agent", broswerData.getUserAgent());
            }

            // mobile fields
            if(builder.hasMobileFields()) {
                JsonDoc sdkInformationElement = request.subElement("sdk_information")
                        .set("application_id", builder.getApplicationId())
                        .set("ephemeral_public_key", builder.getEphemeralPublicKey())
                        .set("maximum_timeout", StringUtils.padLeft(builder.getMaximumTimeout(), 2, '0'))
                        .set("reference_number", builder.getReferenceNumber())
                        .set("sdk_trans_id", builder.getSdkTransactionId())
                        .set("encoded_data", builder.getEncodedData())
                ;

                // device render options
                if(builder.getSdkInterface() != null || builder.getSdkUiTypes() != null) {
                    sdkInformationElement.subElement("device_render_options")
                            .set("sdk_interface", builder.getSdkInterface())
                            .set("sdk_ui_type", SdkUiType.getSdkUiTypes(builder.getSdkUiTypes(), Target.DEFAULT));
                }
            }

            String hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, hashValue, secureEcom.getServerTransactionId());
            setAuthHeader(hash);

            String rawResponse = doTransaction("POST", "authentications", request.toString());
            return mapResponse(rawResponse);
        }

        throw new ApiException(String.format("Unknown transaction type %s.", transType));
    }

    private void setAuthHeader(String value) {
        headers.put("Authorization", String.format("securehash %s", value));
        headers.put("X-GP-Version", "2.2.0");
    }

    private Transaction mapResponse(String rawResponse) {
        JsonDoc doc = JsonDoc.parse(rawResponse);

        ThreeDSecure secureEcom = new ThreeDSecure();

        // check enrolled
        secureEcom.setServerTransactionId(doc.getString("server_trans_id"));
        if(doc.has("enrolled")) {
            secureEcom.setEnrolled(doc.getBool("enrolled"));
        }
        secureEcom.setIssuerAcsUrl(doc.getString("method_url", "challenge_request_url"));

        // get authentication data
        secureEcom.setAcsTransactionId(doc.getString("acs_trans_id"));
        secureEcom.setDirectoryServerTransactionId(doc.getString("ds_trans_id"));
        secureEcom.setAuthenticationType(doc.getString("authentication_type"));
        secureEcom.setAuthenticationValue(doc.getString("authentication_value"));
        secureEcom.setEci(doc.getString("eci"));
        secureEcom.setStatus(doc.getString("status"));
        secureEcom.setStatusReason(doc.getString("status_reason"));
        secureEcom.setAuthenticationSource(doc.getString("authentication_source"));
        secureEcom.setMessageCategory(doc.getString("message_category"));
        secureEcom.setMessageVersion(doc.getString("message_version"));
        secureEcom.setAcsInfoIndicator(doc.getStringArrayList("acs_info_indicator"));
        secureEcom.setDecoupledResponseIndicator(doc.getString("decoupled_response_indicator"));
        secureEcom.setWhitelistStatus(doc.getString("whitelist_status"));
        secureEcom.setExemptReason(doc.getString("eos_reason"));
        if (ExemptReason.APPLY_EXEMPTION.name().equals(secureEcom.getExemptReason())) {
            secureEcom.setExemptStatus(ExemptStatus.TransactionRiskAnalysis);
        }

        // challenge mandated
        if(doc.has("challenge_mandated")) {
            secureEcom.setChallengeMandated(doc.getBool("challenge_mandated"));
        }

        // initiate authentication
        secureEcom.setCardHolderResponseInfo(doc.getString("cardholder_response_info"));

        // device_render_options
        if(doc.has("device_render_options")) {
            JsonDoc renderOptions = doc.get("device_render_options");
            secureEcom.setSdkInterface(renderOptions.getString("sdk_interface"));
            secureEcom.setSdkUiType(renderOptions.getString("sdk_ui_type"));
        }

        // message_extension
        if(doc.has("message_extension")) {
            secureEcom.setMessageExtensions(new ArrayList<>());
            for (JsonDoc messageExtension : doc.getEnumerator("message_extension")) {
                MessageExtension msgExtension =
                        new MessageExtension()
                                .setCriticalityIndicator(messageExtension.getString("criticality_indicator"))
                                .setMessageExtensionData(messageExtension.get("data").toString())
                                .setMessageExtensionId(messageExtension.getString("id"))
                                .setMessageExtensionName(messageExtension.getString("name"));

                secureEcom.getMessageExtensions().add(msgExtension);
            }
        }

        // versions
        secureEcom.setDirectoryServerEndVersion(doc.getString("ds_protocol_version_end"));
        secureEcom.setDirectoryServerStartVersion(doc.getString("ds_protocol_version_start"));
        secureEcom.setAcsEndVersion(doc.getString("acs_protocol_version_end"));
        secureEcom.setAcsStartVersion(doc.getString("acs_protocol_version_start"));

        // payer authentication request
        if(doc.has("method_data")) {
            JsonDoc methodData = doc.get("method_data");
            secureEcom.setPayerAuthenticationRequest(methodData.getString("encoded_method_data"));
        }
        else if(doc.has("encoded_creq")) {
                secureEcom.setPayerAuthenticationRequest(doc.getString("encoded_creq"));
        }

        Transaction response = new Transaction();
        response.setThreeDsecure(secureEcom);
        return response;
    }

    private String mapCardScheme(String cardType) {
        if(cardType.equals("MC")) {
            return "MASTERCARD";
        }
        else if (cardType.equals("DINERSCLUB")) {
            return "DINERS";
        }
        else return cardType;
    }

    protected String handleResponse(GatewayResponse response) throws GatewayException {
        if(response.getStatusCode() != 200 && response.getStatusCode() != 202 && response.getStatusCode() != 204) {
            JsonDoc parsed = JsonDoc.parse(response.getRawResponse());
            if(parsed.has("error")) {
                JsonDoc error = parsed.get("error");
                throw new GatewayException(String.format("Status code: %s - %s", response.getStatusCode(), error.getString("message")));
            }
            throw new GatewayException(String.format("Status code: %s - %s", response.getStatusCode(), parsed.getString("message")));
        }
        return response.getRawResponse();
    }
}

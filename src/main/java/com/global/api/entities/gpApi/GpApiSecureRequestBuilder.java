package com.global.api.entities.gpApi;

import com.global.api.builders.FraudBuilder;
import com.global.api.builders.Secure3dBuilder;
import com.global.api.builders.SecureBuilder;
import com.global.api.entities.enums.*;
import com.global.api.gateways.GpApiConnector;
import com.global.api.paymentMethods.*;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.utils.*;
import lombok.Getter;
import lombok.Setter;
import lombok.var;

import java.util.UUID;

import static com.global.api.gateways.GpApiConnector.getValueIfNotNull;

public class GpApiSecureRequestBuilder {

    @Getter @Setter private static Secure3dBuilder _3dBuilder;

    public static GpApiRequest buildRequest(FraudBuilder builder, GpApiConnector gateway) {

        var merchantUrl = !StringUtils.isNullOrEmpty(gateway.getGpApiConfig().getMerchantId()) ? GpApiRequest.MERCHANT_MANAGEMENT_ENDPOINT + "/" + gateway.getGpApiConfig().getMerchantId() : "";

        switch (builder.getTransactionType()) {
            case RiskAssess:
                var requestData =
                        new JsonDoc()
                                .set("account_name", gateway.getGpApiConfig().getAccessTokenInfo().getRiskAssessmentAccountName())
                                .set("account_id", gateway.getGpApiConfig().getAccessTokenInfo().getRiskAssessmentAccountID())
                                .set("reference", builder.getReferenceNumber() != null ? builder.getReferenceNumber() : java.util.UUID.randomUUID().toString())
                                .set("source", getValueIfNotNull(builder.getAuthenticationSource()))
                                .set("merchant_contact_url", gateway.getGpApiConfig().getMerchantContactUrl())
                                .set("order", SetOrderParam(builder))
                                .set("payment_method", SetPaymentMethodParam(builder, false))
                                .set("payer", SetPayerParam(builder))
                                .set("payer_prior_three_ds_authentication_data", SetPayerPrior3DSAuthenticationDataParam(builder))
                                .set("recurring_authorization_data", SetRecurringAuthorizationDataParam(builder))
                                .set("payer_login_data", SetPayerLoginDataParam(builder))
                                .set("browser_data", SetBrowserDataParam(builder));

                return
                        new GpApiRequest()
                                .setVerb(GpApiRequest.HttpMethod.Post)
                                .setEndpoint(merchantUrl + GpApiRequest.RISK_ASSESSMENTS)
                                .setRequestBody(requestData.toString());
            default:
                break;
        }

        return null;
    }

    private static GpApiRequest buildRequest(Secure3dBuilder builder, GpApiConnector gateway) {
        _3dBuilder = builder;
        var merchantUrl = !StringUtils.isNullOrEmpty(gateway.getGpApiConfig().getMerchantId()) ? GpApiRequest.MERCHANT_MANAGEMENT_ENDPOINT + "/" + gateway.getGpApiConfig().getMerchantId() : "";

        switch (builder.getTransactionType()) {
            case VerifyEnrolled:
                return
                        new GpApiRequest()
                                .setVerb(GpApiRequest.HttpMethod.Post)
                                .setEndpoint(merchantUrl + GpApiRequest.AUTHENTICATIONS_ENDPOINT)
                                .setRequestBody(VerifyEnrolled(gateway.getGpApiConfig()).toString());

            case InitiateAuthentication:
                return
                        new GpApiRequest()
                                .setVerb(GpApiRequest.HttpMethod.Post)
                                .setEndpoint(merchantUrl + GpApiRequest.AUTHENTICATIONS_ENDPOINT + "/" + builder.getServerTransactionId() + "/initiate")
                                .setRequestBody(InitiateAuthenticationData(gateway.getGpApiConfig()).toString());

            case VerifySignature:
                JsonDoc data = null;

                if (!StringUtils.isNullOrEmpty(builder.getPayerAuthenticationResponse())) {
                    data =
                            new JsonDoc()
                                    .set("three_ds",
                                            new JsonDoc()
                                                    .set("challenge_result_value", builder.getPayerAuthenticationResponse())
                    );
                }

                return
                        new GpApiRequest()
                                .setVerb(GpApiRequest.HttpMethod.Post)
                                .setEndpoint(merchantUrl + GpApiRequest.AUTHENTICATIONS_ENDPOINT + "/" + builder.getServerTransactionId() + "/result")
                                .setRequestBody(data.toString());

            default:
                break;
        }

        return null;
    }


    private static JsonDoc InitiateAuthenticationData(GpApiConfig config) {

        var threeDS =
                new JsonDoc()
                        .set("source", getValueIfNotNull(_3dBuilder.getAuthenticationSource()))
                        .set("preference", getValueIfNotNull(_3dBuilder.getChallengeRequestIndicator()))
                        .set("message_version", getValueIfNotNull(_3dBuilder.getMessageVersion()))
                        .set("message_category", getValueIfNotNull(_3dBuilder.getMessageCategory()));

        var data =
                new JsonDoc()
                        .set("three_ds", !threeDS.getKeys().isEmpty() ? threeDS : null)
                        .set("initator", EnumUtils.getMapping(Target.GP_API, _3dBuilder.getStoredCredential().getInitiator()))
                        .set("stored_credential", SetStoreCredentialParam())
                        .set("method_url_completion_status", getValueIfNotNull(_3dBuilder.getMethodUrlCompletion()))
                        .set("merchant_contact_url", config.getMerchantContactUrl())
                        .set("order", SetOrderParam(_3dBuilder)
                        .set("payment_method", SetPaymentMethodParam(_3dBuilder, false))
                        .set("payer", SetPayerParam(_3dBuilder))
                        .set("payer_prior_three_ds_authentication_data", SetPayerPrior3DSAuthenticationDataParam(_3dBuilder))
                        .set("recurring_authorization_data", SetRecurringAuthorizationDataParam(_3dBuilder))
                        .set("payer_login_data", SetPayerLoginDataParam(_3dBuilder))
                        .set("browser_data", _3dBuilder.getBrowserData() != null && _3dBuilder.getAuthenticationSource() != AuthenticationSource.MobileSDK ? SetBrowserDataParam(_3dBuilder) : null)
                        .set("mobile_data", _3dBuilder.getMobileData() != null && _3dBuilder.getAuthenticationSource() == AuthenticationSource.MobileSDK ? SetMobileDataParam() : null));


        var notifications =
                new JsonDoc()
                        .set("challenge_return_url", config.getChallengeNotificationUrl())
                        .set("three_ds_method_return_url", config.getMethodNotificationUrl())
                        .set("decoupled_notification_url", _3dBuilder.getDecoupledNotificationUrl());

        data.set("notifications", !notifications.getKeys().isEmpty() ? notifications : null);

        if (_3dBuilder.getDecoupledFlowRequest() != null) {
            data.set("decoupled_flow_request", _3dBuilder.getDecoupledFlowRequest() ? DecoupledFlowRequest.DECOUPLED_PREFERRED.toString() : DecoupledFlowRequest.DO_NOT_USE_DECOUPLED.toString());
        }

        data.set("decoupled_flow_timeout", _3dBuilder.getDecoupledFlowTimeout() != null ? _3dBuilder.getDecoupledFlowTimeout().toString() : null);

        return data;
    }

    private static JsonDoc SetMobileDataParam() {
        var mobileData =
                new JsonDoc()
                        .set("encoded_data", _3dBuilder.getMobileData().getEncodedData())
                        .set("application_reference", _3dBuilder.getMobileData().getApplicationReference())
                        .set("sdk_interface", _3dBuilder.getMobileData().getSdkInterface().toString())
                        .set("sdk_ui_type", _3dBuilder.getMobileData() != null && _3dBuilder.getMobileData().getSdkUiTypes().length > 0 ? ModifySdkUiTypes() : null)
                        .set("ephemeral_public_key", _3dBuilder.getMobileData().getEphemeralPublicKey())
                        .set("maximum_timeout", _3dBuilder.getMobileData().getMaximumTimeout())
                        .set("reference_number", _3dBuilder.getMobileData().getReferenceNumber())
                        .set("sdk_trans_reference", _3dBuilder.getMobileData().getSdkTransReference());

        return mobileData.getKeys() != null ? mobileData : null;
    }

    private static String[] ModifySdkUiTypes() {
        String[] result = new String[(int) _3dBuilder.getMobileData().getSdkUiTypes().length];

        for (int i = 0; i < _3dBuilder.getMobileData().getSdkUiTypes().length; i++) {
            result[i] = EnumUtils.getMapping(Target.GP_API, _3dBuilder.getMobileData().getSdkUiTypes()[i]);
        }

        return result;
    }

    private static JsonDoc SetBrowserDataParam(SecureBuilder builder) {
        var browserData = new JsonDoc();

        if (builder.getBrowserData() != null) {
            browserData
                    .set("accept_header", builder.getBrowserData().getAcceptHeader())
                    .set("color_depth", getValueIfNotNull(builder.getBrowserData().getColorDepth()))
                    .set("ip", builder.getBrowserData().getIpAddress())
                    .set("java_enabled", builder.getBrowserData().isJavaEnabled())
                    .set("javascript_enabled", builder.getBrowserData().isJavaScriptEnabled())
                    .set("language", builder.getBrowserData().getLanguage())
                    .set("screen_height",  builder.getBrowserData().getScreenHeight() != 0 ? builder.getBrowserData().getScreenHeight() : null)
                    .set("screen_width", builder.getBrowserData().getScreenWidth() != 0 ? builder.getBrowserData().getScreenWidth() : null)
                    .set("challenge_window_size", getValueIfNotNull(builder.getBrowserData().getChallengeWindowSize()))
                    .set("timezone", builder.getBrowserData().getTimezone())
                    .set("user_agent", builder.getBrowserData().getUserAgent());
        }

        return ! browserData.getKeys().isEmpty() ? browserData : null;
    }

    private static JsonDoc SetPayerLoginDataParam(SecureBuilder builder) {
        var payerLoginData =
                new JsonDoc()
                        .set("authentication_data", builder.getCustomerAuthenticationData())
                        .set("authentication_timestamp", builder.getCustomerAuthenticationTimestamp() != null ? builder.getCustomerAuthenticationTimestamp().toString("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") : null)
                        .set("authentication_type", getValueIfNotNull(builder.getCustomerAuthenticationMethod()));

        return !payerLoginData.getKeys().isEmpty() ? payerLoginData : null;
    }

    private static JsonDoc SetRecurringAuthorizationDataParam(SecureBuilder builder) {
        var recurringAuthorizationData = new JsonDoc();

        recurringAuthorizationData
                .set("max_number_of_instalments", builder.getMaxNumberOfInstallments())
                .set("frequency", builder.getRecurringAuthorizationFrequency())
                .set("expiry_date", builder.getRecurringAuthorizationExpiryDate() != null ? builder.getRecurringAuthorizationExpiryDate().toString("yyyy-MM-dd") : null);

        return !recurringAuthorizationData.getKeys().isEmpty() ? recurringAuthorizationData : null;
    }

    private static JsonDoc SetPayerPrior3DSAuthenticationDataParam(SecureBuilder builder) {
        var payerPrior3DSAuthenticationData =
                new JsonDoc()
                        .set("authentication_method", getValueIfNotNull(builder.getPriorAuthenticationMethod()))
                        .set("acs_transaction_reference", builder.getPriorAuthenticationTransactionId())
                        .set("authentication_timestamp", builder.getPriorAuthenticationTimestamp() != null ? builder.getPriorAuthenticationTimestamp().toString("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") : null)
                        .set("authentication_data", builder.getPriorAuthenticationData());

        return !payerPrior3DSAuthenticationData.getKeys().isEmpty() ? payerPrior3DSAuthenticationData : null;
    }

    private static JsonDoc SetPayerParam(SecureBuilder builder) {

        var homePhone =
                new JsonDoc()
                        .set("country_code", builder.getHomeCountryCode())
                        .set("subscriber_number", builder.getHomeNumber());

        var workPhone =
                new JsonDoc()
                        .set("country_code", builder.getWorkCountryCode())
                        .set("subscriber_number", builder.getWorkNumber());

        var mobilePhone =
                new JsonDoc()
                        .set("country_code", builder.getMobileCountryCode())
                        .set("subscriber_number", builder.getMobileNumber());

        var payer =
                new JsonDoc()
                        .set("reference", builder.getCustomerAccountId())
                        .set("account_age", getValueIfNotNull(builder.getAccountAgeIndicator()))
                        .set("account_creation_date", builder.getAccountCreateDate() != null ? builder.getAccountCreateDate().toString("yyyy-MM-dd") : null)
                        .set("account_change_date", builder.getAccountChangeDate() !=  null ? builder.getAccountChangeDate().toString("yyyy-MM-dd") : null)
                        .set("account_change_indicator", getValueIfNotNull(builder.getAccountChangeIndicator()))
                        .set("account_password_change_date", builder.getPasswordChangeDate() != null ? builder.getPasswordChangeDate().toString("yyyy-MM-dd") : null)
                        .set("account_password_change_indicator", getValueIfNotNull(builder.getPasswordChangeIndicator()))
                        .set("home_phone", homePhone.getKeys() != null ? homePhone : null)
                        .set("work_phone", workPhone.getKeys() != null ? workPhone : null)
                        .set("mobile_phone", mobilePhone.getKeys() != null ? mobilePhone : null)
                        .set("payment_account_creation_date", builder.getPaymentAccountCreateDate() != null ? builder.getPaymentAccountCreateDate().toString("yyyy-MM-dd") : null)
                        .set("payment_account_age_indicator", getValueIfNotNull(builder.getPaymentAgeIndicator()))
                        .set("suspicious_account_activity", builder.getPreviousSuspiciousActivity())
                        .set("purchases_last_6months_count", builder.getNumberOfPurchasesInLastSixMonths())
                        .set("transactions_last_24hours_count", builder.getNumberOfTransactionsInLast24Hours())
                        .set("transaction_last_year_count", builder.getNumberOfTransactionsInLastYear())
                        .set("provision_attempt_last_24hours_count", builder.getNumberOfAddCardAttemptsInLast24Hours())
                        .set("shipping_address_time_created_reference", builder.getShippingAddressCreateDate() != null ? builder.getShippingAddressCreateDate().toString("yyyy-MM-dd'T'HH:mm:ss") : null)
                        .set("shipping_address_creation_indicator", getValueIfNotNull(builder.getShippingAddressUsageIndicator()));

        if(builder.getBillingAddress() != null) {
            var billingAddress =
                    new JsonDoc()
                            .set("line1", builder.getBillingAddress().getStreetAddress1())
                            .set("line2", builder.getBillingAddress().getStreetAddress2())
                            .set("line3", builder.getBillingAddress().getStreetAddress3())
                            .set("city", builder.getBillingAddress().getCity())
                            .set("postal_code", builder.getBillingAddress().getPostalCode())
                            .set("state", builder.getBillingAddress().getState())
                            .set("country", CountryUtils.getNumericCodeByCountry(builder.getBillingAddress().getCountryCode()));

            payer.set("billing_address", billingAddress);
        }

        return !payer.getKeys().isEmpty() ? payer : null;

    }

    private static JsonDoc SetOrderParam(SecureBuilder builder)
    {
        var order =
                new JsonDoc()
                        .set("time_created_reference", builder.getOrderCreateDate() != null ? builder.getOrderCreateDate().toString("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") : null)
                        .set("amount", StringUtils.toNumeric(builder.getAmount()))
                        .set("currency", builder.getCurrency())
                        .set("reference", builder.getOrderId() != null ? builder.getOrderId() : UUID.randomUUID().toString())
                        .set("address_match_indicator", builder.getAddressMatchIndicator())
                        .set("gift_card_count", builder.getGiftCardCount())
                        .set("gift_card_currency", builder.getGiftCardCurrency())
                        .set("gift_card_amount", StringUtils.toNumeric(builder.getGiftCardAmount()))
                        .set("delivery_email", builder.getDeliveryEmail())
                        .set("delivery_timeframe", getValueIfNotNull(builder.getDeliveryTimeframe()))
                        .set("shipping_method", getValueIfNotNull(builder.getShippingMethod()))
                        .set("shipping_name_matches_cardholder_name", builder.getShippingNameMatchesCardHolderName())
                        .set("preorder_indicator", getValueIfNotNull(builder.getPreOrderIndicator()))
                        .set("preorder_availability_date", builder.getPreOrderAvailabilityDate() != null ? builder.getPreOrderAvailabilityDate().toString("yyyy-MM-dd") : null)
                        .set("reorder_indicator", getValueIfNotNull(builder.getReorderIndicator()))
                        .set("category", getValueIfNotNull(builder.getOrderTransactionType()));

        if (builder.getShippingAddress() != null) {
            var shippingAddress = new JsonDoc()
                    .set("line1", builder.getShippingAddress().getStreetAddress1())
                    .set("line2", builder.getShippingAddress().getStreetAddress2())
                    .set("line3", builder.getShippingAddress().getStreetAddress3())
                    .set("city", builder.getShippingAddress().getCity())
                    .set("postal_code", builder.getShippingAddress().getPostalCode())
                    .set("state", builder.getShippingAddress().getState())
                    .set("country", CountryUtils.getCountryCodeByCountry(builder.getShippingAddress().getCountryCode()));

            order.set("shipping_address", shippingAddress);
        }

        return !order.getKeys().isEmpty() ? order : null;
    }

    private static JsonDoc VerifyEnrolled(GpApiConfig config) {

        var notifications =
                new JsonDoc()
                        .set("challenge_return_url", config.getChallengeNotificationUrl())
                        .set("three_ds_method_return_url", config.getMethodNotificationUrl())
                        .set("decoupled_notification_url", _3dBuilder.getDecoupledNotificationUrl());

        var threeDS =
                new JsonDoc()
                        .set("account_name", config.getAccessTokenInfo().getTransactionProcessingAccountName())
                        .set("account_id", config.getAccessTokenInfo().getTransactionProcessingAccountID())
                        .set("channel",config.getChannel())
                        .set("country", config.getCountry())
                        .set("reference", _3dBuilder.getReferenceNumber() != null ? _3dBuilder.getReferenceNumber() : UUID.randomUUID().toString())
                        .set("amount", StringUtils.toNumeric(_3dBuilder.getAmount()))
                        .set("currency", _3dBuilder.getCurrency())
                        .set("preference", getValueIfNotNull(_3dBuilder.getChallengeRequestIndicator()))
                        .set("source", getValueIfNotNull(_3dBuilder.getAuthenticationSource()))
                        .set("payment_method", SetPaymentMethodParam(_3dBuilder, false))
                        .set("notifications", !notifications.getKeys().isEmpty() ? notifications : null)
                        .set("initator", EnumUtils.getMapping(Target.GP_API, _3dBuilder.getStoredCredential().getInitiator()))
                        .set("stored_credential", SetStoreCredentialParam());

        return threeDS;
    }

    private static JsonDoc SetPaymentMethodParam(SecureBuilder builder, boolean is3DSecure) {
        var paymentMethod = new JsonDoc();

        if (builder.getPaymentMethod() instanceof ITokenizable && !StringUtils.isNullOrEmpty(((ITokenizable) builder.getPaymentMethod()).getToken())) {
            paymentMethod.set("id", ((ITokenizable) builder.getPaymentMethod()).getToken());
        } else if (builder.getPaymentMethod() instanceof ICardData) {
            var cardData = (ICardData) builder.getPaymentMethod();

            var card = new JsonDoc()
                    .set("brand", cardData.getCardType().toUpperCase())
                    .set("number", cardData.getNumber())
                    .set("expiry_month", cardData.getExpMonth() != null ? StringUtils.padLeft(cardData.getExpMonth(), 2, '0') : null)
                    .set("expiry_year", cardData.getExpYear() != null ? StringUtils.padLeft(cardData.getExpYear().toString(), 4, '0').substring(2, 4) : null);

            paymentMethod
                    .set("card", card)
                    .set("name", !StringUtils.isNullOrEmpty(cardData.getCardHolderName()) ? cardData.getCardHolderName() : null);
        }

        return paymentMethod;
    }

    private static JsonDoc SetStoreCredentialParam() {
        var storedCredential = new JsonDoc()
                .set("model", EnumUtils.getMapping(Target.GP_API, _3dBuilder.getStoredCredential().getType()))
                .set("reason", EnumUtils.getMapping(Target.GP_API, _3dBuilder.getStoredCredential().getReason()))
                .set("sequence", EnumUtils.getMapping(Target.GP_API, _3dBuilder.getStoredCredential().getSequence()));

        return !storedCredential.getKeys().isEmpty() ? storedCredential : null;
    }

}
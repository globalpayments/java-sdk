package com.global.api.builders.requestbuilder.gpApi;

import com.global.api.builders.FraudBuilder;
import com.global.api.builders.Secure3dBuilder;
import com.global.api.builders.SecureBuilder;
import com.global.api.entities.Address;
import com.global.api.entities.BrowserData;
import com.global.api.entities.IRequestBuilder;
import com.global.api.entities.MobileData;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.entities.gpApi.GpApiRequest;
import com.global.api.utils.masking.ElementToMask;
import com.global.api.utils.masking.MaskValueUtil;
import com.global.api.gateways.GpApiConnector;
import com.global.api.paymentMethods.ICardData;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.paymentMethods.ITokenizable;
import com.global.api.utils.CountryUtils;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.var;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.global.api.gateways.GpApiConnector.getValueIfNotNull;
import static com.global.api.utils.StringUtils.isNullOrEmpty;

public class GpApiSecureRequestBuilder implements IRequestBuilder<Secure3dBuilder> {

    @Getter
    @Setter
    private static Secure3dBuilder _3dBuilder;
    private final Map<String, String> maskedData = new HashMap<>();

    public GpApiRequest buildRequest(FraudBuilder builder, GpApiConnector gateway) {

        var merchantUrl = !StringUtils.isNullOrEmpty(gateway.getGpApiConfig().getMerchantId()) ? GpApiRequest.MERCHANT_MANAGEMENT_ENDPOINT + "/" + gateway.getGpApiConfig().getMerchantId() : "";

        switch (builder.getTransactionType()) {
            case RiskAssess:
                IPaymentMethod builderPaymentMethod = builder.getPaymentMethod();
                var requestData =
                        new JsonDoc()
                                .set("account_name", gateway.getGpApiConfig().getAccessTokenInfo().getRiskAssessmentAccountName())
                                .set("account_id", gateway.getGpApiConfig().getAccessTokenInfo().getRiskAssessmentAccountID())
                                .set("reference", builder.getReferenceNumber() != null ? builder.getReferenceNumber() : java.util.UUID.randomUUID().toString())
                                .set("source", getValueIfNotNull(builder.getAuthenticationSource()))
                                .set("merchant_contact_url", gateway.getGpApiConfig().getMerchantContactUrl())
                                .set("order", SetOrderParam(builder))
                                .set("payment_method", setPaymentMethodParam(builderPaymentMethod))
                                .set("payer", SetPayerParam(builder))
                                .set("payer_prior_three_ds_authentication_data", SetPayerPrior3DSAuthenticationDataParam(builder))
                                .set("recurring_authorization_data", SetRecurringAuthorizationDataParam(builder))
                                .set("payer_login_data", SetPayerLoginDataParam(builder))
                                .set("browser_data", SetBrowserDataParam(builder));

                return (GpApiRequest)
                        new GpApiRequest()
                                .setVerb(GpApiRequest.HttpMethod.Post)
                                .setEndpoint(merchantUrl + GpApiRequest.RISK_ASSESSMENTS)
                                .setRequestBody(requestData.toString())
                                .setMaskedData(maskedData);
            default:
                break;
        }

        return null;
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
                    .set("screen_height", builder.getBrowserData().getScreenHeight() != 0 ? builder.getBrowserData().getScreenHeight() : null)
                    .set("screen_width", builder.getBrowserData().getScreenWidth() != 0 ? builder.getBrowserData().getScreenWidth() : null)
                    .set("challenge_window_size", getValueIfNotNull(builder.getBrowserData().getChallengeWindowSize()))
                    .set("timezone", builder.getBrowserData().getTimezone())
                    .set("user_agent", builder.getBrowserData().getUserAgent());
        }

        return !browserData.getKeys().isEmpty() ? browserData : null;
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

        String format = builder.getTransactionType().equals(TransactionType.RiskAssess)  ? "yyyy-MM-dd'T'HH:mm:ss" : "yyyy-MM-dd";

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
                        .set("account_change_date", builder.getAccountChangeDate() != null ? builder.getAccountChangeDate().toString("yyyy-MM-dd") : null)
                        .set("account_change_indicator", getValueIfNotNull(builder.getAccountChangeIndicator()))
                        .set("account_password_change_date", builder.getPasswordChangeDate() != null ? builder.getPasswordChangeDate().toString("yyyy-MM-dd") : null)
                        .set("account_password_change_indicator", getValueIfNotNull(builder.getPasswordChangeIndicator()))
                        .set("payment_account_creation_date", builder.getPaymentAccountCreateDate() != null ? builder.getPaymentAccountCreateDate().toString("yyyy-MM-dd") : null)
                        .set("payment_account_age_indicator", getValueIfNotNull(builder.getPaymentAgeIndicator()))
                        .set("suspicious_account_activity", builder.getSuspiciousAccountActivity() != null ? builder.getSuspiciousAccountActivity().toString() : "")
                        .set("purchases_last_6months_count", builder.getNumberOfPurchasesInLastSixMonths())
                        .set("transactions_last_24hours_count", builder.getNumberOfTransactionsInLast24Hours())
                        .set("transaction_last_year_count", builder.getNumberOfTransactionsInLastYear())
                        .set("provision_attempt_last_24hours_count", builder.getNumberOfAddCardAttemptsInLast24Hours())
                        .set("shipping_address_time_created_reference", builder.getShippingAddressCreateDate() != null ? builder.getShippingAddressCreateDate().toString(format) : null)
                        .set("email", builder.getCustomerEmail())
                        .set("shipping_address_creation_indicator", getValueIfNotNull(builder.getShippingAddressUsageIndicator()));
        if (!mobilePhone.getKeys().isEmpty()) {
            payer.set("mobile_phone", mobilePhone);
        }
        if (!homePhone.getKeys().isEmpty()) {
            payer.set("home_phone", homePhone);
        }
        if (!workPhone.getKeys().isEmpty()) {
            payer.set("work_phone", workPhone);
        }
        if (builder.getBillingAddress() != null) {
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

    private static JsonDoc SetOrderParam(SecureBuilder builder) {
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

    private JsonDoc setPaymentMethodParam(IPaymentMethod builderPaymentMethod) {
        var paymentMethod = new JsonDoc();

        if (builderPaymentMethod instanceof ITokenizable && !StringUtils.isNullOrEmpty(((ITokenizable) builderPaymentMethod).getToken())) {
            paymentMethod.set("id", ((ITokenizable) builderPaymentMethod).getToken());
        } else if (builderPaymentMethod instanceof ICardData) {
            var cardData = (ICardData) builderPaymentMethod;

            var card = new JsonDoc()
                    .set("brand", cardData.getCardType().toUpperCase())
                    .set("number", cardData.getNumber())
                    .set("expiry_month", cardData.getExpMonth() != null ? StringUtils.padLeft(cardData.getExpMonth(), 2, '0') : null)
                    .set("expiry_year", cardData.getExpYear() != null ? StringUtils.padLeft(cardData.getExpYear().toString(), 4, '0').substring(2, 4) : null);

            maskPaymentMethodSensitiveData(card);

            paymentMethod
                    .set("card", card)
                    .set("name", !StringUtils.isNullOrEmpty(cardData.getCardHolderName()) ? cardData.getCardHolderName() : null);

        }

        return paymentMethod;
    }

    @Override
    public GpApiRequest buildRequest(Secure3dBuilder builder, GpApiConnector gateway) throws ApiException {
        String format = builder.getTransactionType().equals(TransactionType.RiskAssess) ? "yyyy-MM-ddThh:mm:ss" : "yyyy-MM-dd";
        String merchantUrl = gateway.getMerchantUrl();
        TransactionType builderTransactionType = builder.getTransactionType();
        IPaymentMethod builderPaymentMethod = builder.getPaymentMethod();

        switch (builderTransactionType) {
            case VerifyEnrolled: {
                JsonDoc storedCredential = new JsonDoc();

                if (builder.getStoredCredential() != null) {
                    storedCredential
                            .set("model", getValueIfNotNull(builder.getStoredCredential().getType()))
                            .set("reason", getValueIfNotNull(builder.getStoredCredential().getReason()))
                            .set("sequence", getValueIfNotNull(builder.getStoredCredential().getSequence()));
                }

                JsonDoc paymentMethod = setPaymentMethodParam(builderPaymentMethod);

                JsonDoc notifications =
                        new JsonDoc()
                                .set("challenge_return_url", gateway.getGpApiConfig().getChallengeNotificationUrl())
                                .set("three_ds_method_return_url", gateway.getGpApiConfig().getMethodNotificationUrl())
                                .set("decoupled_notification_url", builder.getDecoupledNotificationUrl());

                JsonDoc data =
                        new JsonDoc()
                                .set("account_name", gateway.getGpApiConfig().getAccessTokenInfo().getTransactionProcessingAccountName())
                                .set("account_id", gateway.getGpApiConfig().getAccessTokenInfo().getTransactionProcessingAccountID())
                                .set("reference", isNullOrEmpty(builder.getReferenceNumber()) ? java.util.UUID.randomUUID().toString() : builder.getReferenceNumber())
                                .set("channel", gateway.getGpApiConfig().getChannel())
                                .set("amount", StringUtils.toNumeric(builder.getAmount()))
                                .set("currency", builder.getCurrency())
                                .set("country", gateway.getGpApiConfig().getCountry())
                                .set("preference", getValueIfNotNull(builder.getChallengeRequestIndicator()))
                                .set("source", getValueIfNotNull(builder.getAuthenticationSource()))
                                .set("initator", builder.getStoredCredential() != null ? getValueIfNotNull(builder.getStoredCredential().getInitiator()) : null)
                                .set("stored_credential", storedCredential.getKeys() != null ? storedCredential : null)
                                .set("payment_method", paymentMethod)
                                .set("notifications", !notifications.getKeys().isEmpty() ? notifications : null);

                return (GpApiRequest) new GpApiRequest()
                        .setVerb(GpApiRequest.HttpMethod.Post)
                        .setEndpoint(merchantUrl + GpApiRequest.AUTHENTICATIONS_ENDPOINT)
                        .setRequestBody(data.toString())
                        .setMaskedData(maskedData);
            }
            case InitiateAuthentication: {
                JsonDoc storedCredential = new JsonDoc();

                if (builder.getStoredCredential() != null) {
                    storedCredential
                            .set("model", getValueIfNotNull(builder.getStoredCredential().getType()))
                            .set("reason", getValueIfNotNull(builder.getStoredCredential().getReason()))
                            .set("sequence", getValueIfNotNull(builder.getStoredCredential().getSequence()));
                }

                JsonDoc paymentMethod = setPaymentMethodParam(builderPaymentMethod);

                JsonDoc notifications =
                        new JsonDoc()
                                .set("challenge_return_url", gateway.getChallengeNotificationUrl())
                                .set("three_ds_method_return_url", gateway.getMethodNotificationUrl())
                                .set("decoupled_notification_url", builder.getDecoupledNotificationUrl());

                JsonDoc order = setOrderParam(builder);

                JsonDoc payerPrior3DSAuthenticationData =
                        new JsonDoc()
                                .set("authentication_method", getValueIfNotNull(builder.getPriorAuthenticationMethod()))
                                .set("acs_transaction_reference", builder.getPriorAuthenticationTransactionId())
                                .set("authentication_timestamp", GpApiConnector.getDateTimeIfNotNull(builder.getPriorAuthenticationTimestamp()))
                                .set("authentication_data", builder.getPriorAuthenticationData());

                JsonDoc recurringAuthorizationData =
                        new JsonDoc()
                                .set("max_number_of_instalments", builder.getMaxNumberOfInstallments())
                                .set("frequency", builder.getRecurringAuthorizationFrequency())
                                .set("expiry_date", GpApiConnector.getDateIfNotNull(builder.getRecurringAuthorizationExpiryDate()));

                JsonDoc payerLoginData =
                        new JsonDoc()
                                .set("authentication_data", builder.getCustomerAuthenticationData())
                                .set("authentication_timestamp", GpApiConnector.getDateTimeIfNotNull(builder.getCustomerAuthenticationTimestamp()))
                                .set("authentication_type", getValueIfNotNull(builder.getCustomerAuthenticationMethod()));

                JsonDoc browserData = new JsonDoc();
                BrowserData builderBrowserData = builder.getBrowserData();
                if (builderBrowserData != null) {
                    browserData
                            .set("accept_header", builderBrowserData.getAcceptHeader())
                            .set("color_depth", getValueIfNotNull(builderBrowserData.getColorDepth()))
                            .set("ip", builderBrowserData.getIpAddress())
                            .set("java_enabled", builderBrowserData.isJavaEnabled())
                            .set("javascript_enabled", builderBrowserData.isJavaScriptEnabled())
                            .set("language", builderBrowserData.getLanguage())
                            .set("screen_height", builderBrowserData.getScreenHeight())
                            .set("screen_width", builderBrowserData.getScreenWidth())
                            .set("challenge_window_size", getValueIfNotNull(builderBrowserData.getChallengeWindowSize()))
                            .set("timezone", builderBrowserData.getTimezone())
                            .set("user_agent", builderBrowserData.getUserAgent());
                }

                JsonDoc mobileData = new JsonDoc();
                MobileData builderMobileData = builder.getMobileData();
                if (builderMobileData != null) {
                    mobileData
                            .set("encoded_data", builderMobileData.getEncodedData())
                            .set("application_reference", builderMobileData.getApplicationReference())
                            .set("sdk_interface", builderMobileData.getSdkInterface())
                            .set("sdk_ui_type", SdkUiType.getSdkUiTypes(builderMobileData.getSdkUiTypes(), Target.GP_API))
                            .set("ephemeral_public_key", builderMobileData.getEphemeralPublicKey()) // Maybe .toString() needed
                            .set("maximum_timeout", builderMobileData.getMaximumTimeout())
                            .set("reference_number", builderMobileData.getReferenceNumber())
                            .set("sdk_trans_reference", builderMobileData.getSdkTransReference());
                }

                JsonDoc threeDS =
                        new JsonDoc()
                                .set("source", getValueIfNotNull(builder.getAuthenticationSource()))
                                .set("preference", getValueIfNotNull(builder.getChallengeRequestIndicator()))
                                .set("message_version", getValueIfNotNull(builder.getMessageVersion()));

                JsonDoc data =
                        new JsonDoc()
                                .set("three_ds", !threeDS.getKeys().isEmpty() ? threeDS : null)
                                .set("initator", builder.getStoredCredential() != null ? getValueIfNotNull(builder.getStoredCredential().getInitiator()) : null)
                                .set("stored_credential", !storedCredential.getKeys().isEmpty() ? storedCredential : null)
                                .set("method_url_completion_status", builder.getMethodUrlCompletion() != null ? getValueIfNotNull(builder.getMethodUrlCompletion()) : null)
                                .set("payment_method", !paymentMethod.getKeys().isEmpty() ? paymentMethod : null)
                                .set("notifications", !notifications.getKeys().isEmpty() ? notifications : null)
                                .set("decoupled_flow_request", builder.getDecoupledFlowRequest() == Boolean.TRUE ? DecoupledFlowRequest.DECOUPLED_PREFERRED.toString() : null)
                                .set("decoupled_flow_timeout", builder.getDecoupledFlowTimeout() != null ? builder.getDecoupledFlowTimeout().toString() : null)
                                .set("order", !order.getKeys().isEmpty() ? order : null)
                                .set("payer", SetPayerParam(builder))
                                .set("payer_prior_three_ds_authentication_data", !payerPrior3DSAuthenticationData.getKeys().isEmpty() ? payerPrior3DSAuthenticationData : null)
                                .set("recurring_authorization_data", !recurringAuthorizationData.getKeys().isEmpty() ? recurringAuthorizationData : null)
                                .set("payer_login_data", !payerLoginData.getKeys().isEmpty() ? payerLoginData : null)
                                .set("browser_data", !browserData.getKeys().isEmpty() && builder.getAuthenticationSource() != AuthenticationSource.MobileSDK ? browserData : null)
                                .set("mobile_data", !mobileData.getKeys().isEmpty() && builder.getAuthenticationSource() == AuthenticationSource.MobileSDK ? mobileData : null)
                                .set("merchant_contact_url", gateway.getGpApiConfig().getMerchantContactUrl());

                return (GpApiRequest)
                        new GpApiRequest()
                                .setVerb(GpApiRequest.HttpMethod.Post)
                                .setEndpoint(merchantUrl + GpApiRequest.AUTHENTICATIONS_ENDPOINT + "/" + builder.getServerTransactionId() + "/initiate")
                                .setRequestBody(data.toString())
                                .setMaskedData(maskedData);
            }
            case VerifySignature: {
                JsonDoc data = new JsonDoc();
                if (!StringUtils.isNullOrEmpty(builder.getPayerAuthenticationResponse())) {
                    data = new JsonDoc()
                            .set("three_ds", new JsonDoc().set("challenge_result_value", builder.getPayerAuthenticationResponse()));
                }

                return (GpApiRequest)
                        new GpApiRequest()
                                .setVerb(GpApiRequest.HttpMethod.Post)
                                .setEndpoint(merchantUrl + GpApiRequest.AUTHENTICATIONS_ENDPOINT + "/" + builder.getServerTransactionId() + "/result")
                                .setRequestBody(data.toString())
                                .setMaskedData(maskedData);
            }
            case RiskAssess: {
                JsonDoc threeDS =
                        new JsonDoc()
                                .set("account_name", gateway.getGpApiConfig().getAccessTokenInfo().getTransactionProcessingAccountName())
                                .set("account_id", gateway.getGpApiConfig().getAccessTokenInfo().getTransactionProcessingAccountID())
                                .set("reference", builder.getReferenceNumber() != null ? builder.getReferenceNumber() : UUID.randomUUID().toString())
                                .set("source", getValueIfNotNull(builder.getAuthenticationSource()))
                                .set("merchant_contact_url", gateway.getGpApiConfig().getMerchantContactUrl())
                                .set("order", setOrderParam(builder));

                return (GpApiRequest)
                        new GpApiRequest()
                                .setVerb(GpApiRequest.HttpMethod.Post)
                                .setEndpoint(merchantUrl + GpApiRequest.RISK_ASSESSMENTS)
                                .setRequestBody(threeDS.toString())
                                .setMaskedData(maskedData);
            }
            default:
                throw new UnsupportedTransactionException();
        }
    }

    private static JsonDoc setOrderParam(Secure3dBuilder builder) {
        JsonDoc order =
                new JsonDoc()
                        .set("time_created_reference", builder.getOrderCreateDate().toString(GpApiConnector.DATE_TIME_DTF))
                        .set("amount", StringUtils.toNumeric(builder.getAmount()))
                        .set("currency", builder.getCurrency())
                        .set("reference", builder.getReferenceNumber())
                        .set("address_match_indicator", builder.isAddressMatchIndicator())
                        .set("gift_card_count", builder.getGiftCardCount())
                        .set("gift_card_currency", builder.getGiftCardCurrency())
                        .set("gift_card_amount", StringUtils.toNumeric(builder.getGiftCardAmount()))
                        .set("delivery_email", builder.getDeliveryEmail())
                        .set("delivery_timeframe", getValueIfNotNull(builder.getDeliveryTimeframe()))
                        .set("shipping_method", getValueIfNotNull(builder.getShippingMethod()))
                        .set("shipping_name_matches_cardholder_name", builder.getShippingNameMatchesCardHolderName())
                        .set("preorder_indicator", getValueIfNotNull(builder.getPreOrderIndicator()))
                        .set("preorder_availability_date", GpApiConnector.getDateTimeIfNotNull((builder.getPreOrderAvailabilityDate())))
                        .set("reorder_indicator", getValueIfNotNull(builder.getReorderIndicator()))
                        .set("category", getValueIfNotNull(builder.getMessageCategory()))
                        .set("transaction_type", getValueIfNotNull(builder.getOrderTransactionType()));

        Address builderShippingAddress = builder.getShippingAddress();
        if (builderShippingAddress != null) {
            JsonDoc shippingAddress =
                    new JsonDoc()
                            .set("line1", builderShippingAddress.getStreetAddress1())
                            .set("line2", builderShippingAddress.getStreetAddress2())
                            .set("line3", builderShippingAddress.getStreetAddress3())
                            .set("city", builderShippingAddress.getCity())
                            .set("postal_code", builderShippingAddress.getPostalCode())
                            .set("state", builderShippingAddress.getState())
                            .set("country", builderShippingAddress.getCountryCode());

            order.set("shipping_address", shippingAddress);
        }

        return order;
    }

    private void maskPaymentMethodSensitiveData(JsonDoc card) {
        maskedData.putAll(MaskValueUtil.hideValues(
                new ElementToMask("payment_method.card.expiry_month", card.getString("expiry_month")),
                new ElementToMask("payment_method.card.expiry_year", card.getString("expiry_year")),
                new ElementToMask("payment_method.card.number", card.getString("number"), 4, 6)
        ));
    }
}
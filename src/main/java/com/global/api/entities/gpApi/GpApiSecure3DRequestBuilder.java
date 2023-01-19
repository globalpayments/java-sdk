package com.global.api.entities.gpApi;

import com.global.api.builders.Secure3dBuilder;
import com.global.api.entities.Address;
import com.global.api.entities.BrowserData;
import com.global.api.entities.MobileData;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.gateways.GpApiConnector;
import com.global.api.paymentMethods.ICardData;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.paymentMethods.ITokenizable;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;

import static com.global.api.gateways.GpApiConnector.getValueIfNotNull;
import static com.global.api.utils.StringUtils.isNullOrEmpty;

public class GpApiSecure3DRequestBuilder {

    public static GpApiRequest buildRequest(Secure3dBuilder builder, GpApiConnector gateway) throws ApiException {
        String merchantUrl = gateway.getMerchantUrl();
        TransactionType builderTransactionType = builder.getTransactionType();
        IPaymentMethod builderPaymentMethod = builder.getPaymentMethod();

        switch (builderTransactionType) {
            case VerifyEnrolled: {
                JsonDoc storedCredential = new JsonDoc();

                if(builder.getStoredCredential() != null) {
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

                return new GpApiRequest()
                        .setVerb(GpApiRequest.HttpMethod.Post)
                        .setEndpoint(merchantUrl + "/authentications")
                        .setRequestBody(data.toString());
            }
            case InitiateAuthentication: {
                JsonDoc storedCredential = new JsonDoc();

                if(builder.getStoredCredential() != null) {
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

                JsonDoc homePhone =
                        new JsonDoc()
                                .set("country_code", builder.getHomeCountryCode())
                                .set("subscriber_number", builder.getHomeNumber());

                JsonDoc workPhone =
                        new JsonDoc()
                                .set("country_code", builder.getWorkCountryCode())
                                .set("subscriber_number", builder.getWorkNumber());

                JsonDoc payer =
                        new JsonDoc()
                                .set("reference", builder.getCustomerAccountId())     //TODO: Confirm
                                .set("account_age", builder.getAccountAgeIndicator())
                                .set("account_creation_date", GpApiConnector.getDateIfNotNull(builder.getAccountCreateDate()))
                                .set("account_change_date", GpApiConnector.getDateIfNotNull(builder.getAccountChangeDate()))
                                .set("account_change_indicator", getValueIfNotNull(builder.getAccountChangeIndicator()))
                                .set("account_password_change_date", GpApiConnector.getDateIfNotNull(builder.getPasswordChangeDate()))
                                .set("account_password_change_indicator", getValueIfNotNull(builder.getPasswordChangeIndicator()))
                                .set("home_phone", !homePhone.getKeys().isEmpty() ? homePhone : null)
                                .set("work_phone", !workPhone.getKeys().isEmpty() ? workPhone : null)
                                .set("payment_account_creation_date", GpApiConnector.getDateIfNotNull(builder.getPaymentAccountCreateDate()))
                                .set("payment_account_age_indicator", getValueIfNotNull(builder.getPaymentAgeIndicator()))
                                .set("suspicious_account_activity", builder.getPreviousSuspiciousActivity())
                                .set("purchases_last_6months_count", builder.getNumberOfPurchasesInLastSixMonths())
                                .set("transactions_last_24hours_count", builder.getNumberOfTransactionsInLast24Hours())
                                .set("transaction_last_year_count", builder.getNumberOfTransactionsInLastYear())
                                .set("provision_attempt_last_24hours_count", builder.getNumberOfAddCardAttemptsInLast24Hours())
                                .set("shipping_address_time_created_reference", GpApiConnector.getDateIfNotNull(builder.getShippingAddressCreateDate()))
                                .set("shipping_address_creation_indicator", getValueIfNotNull(builder.getShippingAddressUsageIndicator()));

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
                            .set("color_depth", builderBrowserData.getColorDepth().getValue())
                            .set("ip", builderBrowserData.getIpAddress())
                            .set("java_enabled", builderBrowserData.isJavaEnabled())
                            .set("javascript_enabled", builderBrowserData.isJavaScriptEnabled())
                            .set("language", builderBrowserData.getLanguage())
                            .set("screen_height", builderBrowserData.getScreenHeight())
                            .set("screen_width", builderBrowserData.getScreenWidth())
                            .set("challenge_window_size", builderBrowserData.getChallengeWindowSize().getValue())
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
                                .set("payer", !payer.getKeys().isEmpty() ? payer : null)
                                .set("payer_prior_three_ds_authentication_data", !payerPrior3DSAuthenticationData.getKeys().isEmpty() ? payerPrior3DSAuthenticationData : null)
                                .set("recurring_authorization_data", !recurringAuthorizationData.getKeys().isEmpty() ? recurringAuthorizationData : null)
                                .set("payer_login_data", !payerLoginData.getKeys().isEmpty() ? payerLoginData : null)
                                .set("browser_data", !browserData.getKeys().isEmpty() && builder.getAuthenticationSource() != AuthenticationSource.MobileSDK ? browserData : null)
                                .set("mobile_data", !mobileData.getKeys().isEmpty() && builder.getAuthenticationSource() == AuthenticationSource.MobileSDK ? mobileData : null)
                                .set("merchant_contact_url", gateway.getGpApiConfig().getMerchantContactUrl());

                return
                        new GpApiRequest()
                                .setVerb(GpApiRequest.HttpMethod.Post)
                                .setEndpoint(merchantUrl + "/authentications/" + builder.getServerTransactionId() + "/initiate")
                                .setRequestBody(data.toString());
            }
            case VerifySignature: {
                JsonDoc data = new JsonDoc();
                if (!StringUtils.isNullOrEmpty(builder.getPayerAuthenticationResponse())) {
                    data = new JsonDoc()
                            .set("three_ds", new JsonDoc().set("challenge_result_value", builder.getPayerAuthenticationResponse()));
                }

                return
                        new GpApiRequest()
                                .setVerb(GpApiRequest.HttpMethod.Post)
                                .setEndpoint(merchantUrl + "/authentications/" + builder.getServerTransactionId() + "/result")
                                .setRequestBody(data.toString());
            }
            default:
                throw new UnsupportedTransactionException();
        }
    }

    private static JsonDoc setPaymentMethodParam(IPaymentMethod builderPaymentMethod) {
        JsonDoc paymentMethod = new JsonDoc();

        if (builderPaymentMethod instanceof ITokenizable && !StringUtils.isNullOrEmpty(((ITokenizable) builderPaymentMethod).getToken())) {
            paymentMethod.set("id", ((ITokenizable) builderPaymentMethod).getToken());
        }
        else if (builderPaymentMethod instanceof ICardData) {
            ICardData cardData = (ICardData) builderPaymentMethod;
            JsonDoc card = new JsonDoc()
                    .set("number", cardData.getNumber())
                    .set("expiry_month", cardData.getExpMonth() != null ? StringUtils.padLeft(cardData.getExpMonth().toString(), 2, '0') : null)
                    .set("expiry_year", cardData.getExpYear() != null ? cardData.getExpYear().toString().substring(2, 4) : null);

            paymentMethod.set("card", card);
        }

        return paymentMethod;
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
                        .set("category", builder.getMessageCategory().getValue());

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
}
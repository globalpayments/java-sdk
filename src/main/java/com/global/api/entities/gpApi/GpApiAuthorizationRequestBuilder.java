package com.global.api.entities.gpApi;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.Address;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.ThreeDSecure;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.gateways.GpApiConnector;
import com.global.api.paymentMethods.*;
import com.global.api.utils.EnumUtils;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;

import java.util.UUID;

import static com.global.api.entities.enums.TransactionType.Refund;
import static com.global.api.utils.StringUtils.isNullOrEmpty;

public class GpApiAuthorizationRequestBuilder {


    public static GpApiRequest buildRequest(AuthorizationBuilder builder, GpApiConnector gateway) throws GatewayException {
        JsonDoc paymentMethod =
                new JsonDoc()
                        .set("entry_mode", getEntryMode(builder)); // [MOTO, ECOM, IN_APP, CHIP, SWIPE, MANUAL, CONTACTLESS_CHIP, CONTACTLESS_SWIPE]

        IPaymentMethod builderPaymentMethod = builder.getPaymentMethod();
        TransactionType builderTransactionType = builder.getTransactionType();
        Address builderBillingAddress = builder.getBillingAddress();

        // CardData
        if (builderPaymentMethod instanceof ICardData) {
            ICardData cardData = (ICardData) builderPaymentMethod;

            JsonDoc card = new JsonDoc();
            card.set("number", cardData.getNumber());
            card.set("expiry_month", cardData.getExpMonth() != null ? StringUtils.padLeft(cardData.getExpMonth().toString(), 2, '0') : null);
            card.set("expiry_year", cardData.getExpYear() != null ? cardData.getExpYear().toString().substring(2, 4) : null);
            //card.set("track", "");
            card.set("tag", builder.getTagData());
            card.set("cvv", cardData.getCvn());
            card.set("cvv_indicator", !cardData.getCvnPresenceIndicator().getValue().equals("0") ? getCvvIndicator(cardData.getCvnPresenceIndicator()) : null); // [ILLEGIBLE, NOT_PRESENT, PRESENT]
            card.set("avs_address", builderBillingAddress != null ? builderBillingAddress.getStreetAddress1() : "");
            card.set("avs_postal_code", builderBillingAddress != null ? builderBillingAddress.getPostalCode() : "");
            card.set("funding", builderPaymentMethod.getPaymentMethodType() == PaymentMethodType.Debit ? "DEBIT" : "CREDIT"); // [DEBIT, CREDIT]
            card.set("authcode", builder.getOfflineAuthCode());
            //card.set("brand_reference", "")

            card.set("chip_condition", builder.getEmvChipCondition()); // [PREV_SUCCESS, PREV_FAILED]

            paymentMethod.set("card", card);

            if (builderTransactionType == TransactionType.Tokenize) {
                JsonDoc tokenizationData = new JsonDoc();
                tokenizationData.set("account_name", gateway.getTokenizationAccountName());
                tokenizationData.set("reference", isNullOrEmpty(builder.getClientTransactionId()) ? java.util.UUID.randomUUID().toString() : builder.getClientTransactionId());
                tokenizationData.set("usage_mode", builder.getPaymentMethodUsageMode());
                tokenizationData.set("name", "");
                tokenizationData.set("card", card);

                return
                        new GpApiRequest()
                                .setVerb(GpApiRequest.HttpMethod.Post)
                                .setEndpoint("/payment-methods")
                                .setRequestBody(tokenizationData.toString());
            }
            else if (builderTransactionType == TransactionType.Verify) {
                if (builder.isRequestMultiUseToken() && StringUtils.isNullOrEmpty(((ITokenizable) builderPaymentMethod).getToken())) {
                    JsonDoc tokenizationData = new JsonDoc();
                    tokenizationData.set("account_name", gateway.getTokenizationAccountName());
                    tokenizationData.set("reference", isNullOrEmpty(builder.getClientTransactionId()) ? java.util.UUID.randomUUID().toString() : builder.getClientTransactionId());
                    tokenizationData.set("usage_mode", builder.getPaymentMethodUsageMode());
                    tokenizationData.set("name", "");
                    tokenizationData.set("card", card);

                    return
                            new GpApiRequest()
                                    .setVerb(GpApiRequest.HttpMethod.Post)
                                    .setEndpoint("/payment-methods")
                                    .setRequestBody(tokenizationData.toString());

                }
                else {
                    JsonDoc verificationData =
                            new JsonDoc()
                                    .set("account_name", gateway.getTransactionProcessingAccountName())
                                    .set("channel", gateway.getGpApiConfig().getChannel())
                                    .set("reference", isNullOrEmpty(builder.getClientTransactionId()) ? java.util.UUID.randomUUID().toString() : builder.getClientTransactionId())
                                    .set("currency", builder.getCurrency())
                                    .set("country", gateway.getGpApiConfig().getCountry())
                                    .set("payment_method", paymentMethod);

                    if (builderPaymentMethod instanceof ITokenizable && !StringUtils.isNullOrEmpty(((ITokenizable) builderPaymentMethod).getToken())) {
                        verificationData.remove("payment_method");
                        verificationData.set("payment_method",
                                new JsonDoc()
                                        .set("entry_mode", getEntryMode(builder))
                                        .set("id", ((ITokenizable) builderPaymentMethod).getToken())
                            );
                    }

                    return
                            new GpApiRequest()
                                .setVerb(GpApiRequest.HttpMethod.Post)
                                .setEndpoint("/verifications")
                                .setRequestBody(verificationData.toString());
                }
            }
        }

        // TrackData
        else if (builderPaymentMethod instanceof ITrackData) {
            ITrackData track = (ITrackData) builderPaymentMethod;

            JsonDoc card =
                    new JsonDoc()
                            .set("track", track.getValue())
                            .set("tag", builder.getTagData())
                            .set("avs_address", builderBillingAddress != null ? builderBillingAddress.getStreetAddress1() : "")
                            .set("avs_postal_code", builderBillingAddress != null ? builderBillingAddress.getPostalCode() : "")
                            .set("authcode", builder.getOfflineAuthCode());

            if (builderTransactionType == TransactionType.Verify) {
                paymentMethod.set("card", card);

                JsonDoc verificationData = new JsonDoc()
                        .set("account_name", gateway.getTransactionProcessingAccountName())
                        .set("channel", gateway.getGpApiConfig().getChannel())
                        .set("reference", isNullOrEmpty(builder.getClientTransactionId()) ? UUID.randomUUID().toString() : builder.getClientTransactionId())
                        .set("currency", builder.getCurrency())
                        .set("country", gateway.getGpApiConfig().getCountry())
                        .set("payment_method", paymentMethod);

                return
                        new GpApiRequest()
                            .setVerb(GpApiRequest.HttpMethod.Post)
                            .setEndpoint("/verifications")
                            .setRequestBody(verificationData.toString());
            }

            if (builderTransactionType == TransactionType.Sale || builderTransactionType == TransactionType.Refund) {
                if (StringUtils.isNullOrEmpty(track.getValue())) {
                    card.set("number", track.getPan());
                    card.set("expiry_month", track.getExpiry().substring(2, 4));
                    card.set("expiry_year", track.getExpiry().substring(0, 2));
                }
                if (StringUtils.isNullOrEmpty(builder.getTagData())) {
                    card.set("chip_condition", getChipCondition(builder.getEmvChipCondition())); // [PREV_SUCCESS, PREV_FAILED]
                }
            }

            if (builderTransactionType == TransactionType.Sale) {
                card.set("funding", builderPaymentMethod.getPaymentMethodType() == PaymentMethodType.Debit ? "DEBIT" : "CREDIT"); // [DEBIT, CREDIT]
            }

            paymentMethod.set("card", card);
        }

        // Payment Method Storage Mode
        if (builder.isRequestMultiUseToken()) {
            //TODO: there might be a typo: should be storage_mode
            paymentMethod.set("storage_mode", "ON_SUCCESS");
        }

        // Tokenized Payment Method
        if (builderPaymentMethod instanceof ITokenizable) {
            String token = ((ITokenizable) builderPaymentMethod).getToken();
            if (!StringUtils.isNullOrEmpty(token)) {
                paymentMethod.set("id", token);
            }
        }

        // Pin Block
        if (builderPaymentMethod instanceof IPinProtected) {
            paymentMethod.get("card").set("pin_block", ((IPinProtected) builderPaymentMethod).getPinBlock());
        }

        // Authentication
        if (builderPaymentMethod instanceof CreditCardData) {
            CreditCardData creditCardData = (CreditCardData) builderPaymentMethod;
            paymentMethod.set("name", creditCardData.getCardHolderName());

            ThreeDSecure secureEcom = creditCardData.getThreeDSecure();
            if (secureEcom != null) {
                JsonDoc authentication = new JsonDoc().set("id", secureEcom.getServerTransactionId());

                paymentMethod.set("authentication", authentication);
            }
        }

        // Encryption
        if (builderPaymentMethod instanceof IEncryptable) {
            IEncryptable encryptable = (IEncryptable) builderPaymentMethod;
            EncryptionData encryptionData = encryptable.getEncryptionData();

            if (encryptionData != null) {
                JsonDoc encryption =
                        new JsonDoc()
                                .set("version", encryptionData.getVersion());

                if (!StringUtils.isNullOrEmpty(encryptionData.getKtb())) {
                    encryption.set("method", "KTB");
                    encryption.set("info", encryptionData.getKtb());
                }
                else if (!StringUtils.isNullOrEmpty(encryptionData.getKsn())) {
                    encryption.set("method", "KSN");
                    encryption.set("info", encryptionData.getKsn());
                }

                if (encryption.has("info")) {
                    paymentMethod.set("encryption", encryption);
                }
            }
        }

        JsonDoc data = new JsonDoc()
                .set("account_name", gateway.getTransactionProcessingAccountName())
                .set("type", builderTransactionType == Refund ? "REFUND" : "SALE") // [SALE, REFUND]
                .set("channel", gateway.getGpApiConfig().getChannel()) // [CP, CNP]
                .set("capture_mode", getCaptureMode(builder)) // [AUTO, LATER, MULTIPLE]
                //.set("remaining_capture_count", "") // Pending Russell
                .set("authorization_mode", builder.isAllowPartialAuth() ? "PARTIAL" : null)
                .set("amount", StringUtils.toNumeric(builder.getAmount()))
                .set("currency", builder.getCurrency())
                .set("reference", isNullOrEmpty(builder.getClientTransactionId()) ? java.util.UUID.randomUUID().toString() : builder.getClientTransactionId())
                .set("description", builder.getDescription())
                //.set("order_reference", builder.getOrderId())
                .set("gratuity_amount", StringUtils.toNumeric(builder.getGratuity()))
                .set("cashback_amount", StringUtils.toNumeric(builder.getCashBackAmount()))
                .set("surcharge_amount", StringUtils.toNumeric(builder.getSurchargeAmount()))
                .set("convenience_amount", StringUtils.toNumeric(builder.getConvenienceAmount()))
                .set("country", gateway.getGpApiConfig().getCountry())
                //.set("language", language)
                .set("ip_address", builder.getCustomerIpAddress())
                //.set("site_reference", "") //
                .set("payment_method", paymentMethod);

        // Set Order Reference
        if (!StringUtils.isNullOrEmpty(builder.getOrderId())) {
            JsonDoc order =
                    new JsonDoc()
                            .set("reference", builder.getOrderId());

            data.set("order", order);
        }

        // Stored Credential
        if (builder.getStoredCredential() != null) {
            data.set("initiator", EnumUtils.getMapping(builder.getStoredCredential().getInitiator(), Target.GP_API));
            JsonDoc storedCredential =
                    new JsonDoc()
                            .set("model", EnumUtils.getMapping(builder.getStoredCredential().getType(), Target.GP_API))
                            .set("reason", EnumUtils.getMapping(builder.getStoredCredential().getReason(), Target.GP_API))
                            .set("sequence", EnumUtils.getMapping(builder.getStoredCredential().getSequence(), Target.GP_API));
            data.set("stored_credential", storedCredential);
        }

        return
                new GpApiRequest()
                        .setVerb(GpApiRequest.HttpMethod.Post)
                        .setEndpoint("/transactions")
                        .setRequestBody(data.toString());
    }

    private static String getEntryMode(AuthorizationBuilder builder) {
        IPaymentMethod builderPaymentMethod = builder.getPaymentMethod();
        if (builderPaymentMethod instanceof ICardData) {
            ICardData card = (ICardData) builderPaymentMethod;
            if (card.isReaderPresent()) {
                return card.isCardPresent() ? "MANUAL" : "IN_APP";
            }
            else {
                return card.isCardPresent() ? "MANUAL" : "ECOM";
            }
        }
        else if (builderPaymentMethod instanceof ITrackData) {
            ITrackData track = (ITrackData) builderPaymentMethod;
            if (builder.getTagData() != null) {
                return (track.getEntryMethod() == EntryMethod.Swipe) ? "CHIP" : "CONTACTLESS_CHIP";
            }
            else if (builder.hasEmvFallbackData()) {
                return "CONTACTLESS_SWIPE";
            }
            return "SWIPE";
        }
        return "ECOM";
    }

    private static String getCaptureMode(AuthorizationBuilder builder) {
        if (builder.isMultiCapture())
            return "MULTIPLE";
        else if (builder.getTransactionType() == TransactionType.Auth)
            return "LATER";

        return "AUTO";
    }

    private static String getCvvIndicator(CvnPresenceIndicator cvnPresenceIndicator) {
        switch (cvnPresenceIndicator) {
            case Present:
                return "PRESENT";
            case Illegible:
                return "ILLEGIBLE";
            default:
                return "NOT_PRESENT";
        }
    }

    private static String getChipCondition(EmvChipCondition emvChipCondition) {
        if (emvChipCondition == null) return "";
        switch (emvChipCondition) {
            case ChipFailPreviousSuccess:
                return "PREV_SUCCESS";
            case ChipFailPreviousFail:
                return "PREV_FAILED";
            default:
                return "UNKNOWN";
        }
    }

}
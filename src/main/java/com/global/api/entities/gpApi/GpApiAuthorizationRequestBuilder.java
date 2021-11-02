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
import static com.global.api.gateways.GpApiConnector.getValueIfNotNull;
import static com.global.api.utils.EnumUtils.mapDigitalWalletType;
import static com.global.api.utils.StringUtils.isNullOrEmpty;

public class GpApiAuthorizationRequestBuilder {

    public static GpApiRequest buildRequest(AuthorizationBuilder builder, GpApiConnector gateway) throws GatewayException {
        String merchantUrl = gateway.getMerchantUrl();
        JsonDoc paymentMethod =
                new JsonDoc()
                        .set("entry_mode", getEntryMode(builder, gateway.getGpApiConfig().getChannel())); // [MOTO, ECOM, IN_APP, CHIP, SWIPE, MANUAL, CONTACTLESS_CHIP, CONTACTLESS_SWIPE]

        IPaymentMethod builderPaymentMethod = builder.getPaymentMethod();
        TransactionType builderTransactionType = builder.getTransactionType();
        TransactionModifier builderTransactionModifier = builder.getTransactionModifier();
        Address builderBillingAddress = builder.getBillingAddress();

        if (builderPaymentMethod instanceof CreditCardData && (builderTransactionModifier == TransactionModifier.EncryptedMobile || builderTransactionModifier == TransactionModifier.DecryptedMobile))
        {
            JsonDoc digitalWallet = new JsonDoc();
            CreditCardData creditCardData = (CreditCardData) builderPaymentMethod;
            //Digital Wallet
            if (builderTransactionModifier == TransactionModifier.EncryptedMobile)
            {
                digitalWallet
                        .set("payment_token", JsonDoc.parse(creditCardData.getToken()));

            }
            else if (builderTransactionModifier == TransactionModifier.DecryptedMobile)
            {
                DigitalWalletTokenFormat tokenFormat = DigitalWalletTokenFormat.CARD_NUMBER;
                digitalWallet
                        .set("token", creditCardData.getToken())
                        .set("token_format", DigitalWalletTokenFormat.CARD_NUMBER.getValue())
                        .set("expiry_month", creditCardData.getExpMonth() != null ? StringUtils.padLeft(creditCardData.getExpMonth(), 2, '0') : null)
                        .set("expiry_year", creditCardData.getExpYear() != null ? StringUtils.padLeft(creditCardData.getExpYear(), 4, '0').substring(2, 4) : null)
                        .set("cryptogram", creditCardData.getCryptogram())
                        .set("eci", creditCardData.getEci());
            }
            digitalWallet.set("provider", mapDigitalWalletType(Target.GP_API, ((CreditCardData) builderPaymentMethod).getMobileType()));
            paymentMethod.set("digital_wallet", digitalWallet);
        } else {
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
                card.set("avs_address", builderBillingAddress != null ? builderBillingAddress.getStreetAddress1() : "");
                card.set("avs_postal_code", builderBillingAddress != null ? builderBillingAddress.getPostalCode() : "");
                card.set("authcode", builder.getOfflineAuthCode());
                //card.set("brand_reference", "")

                card.set("chip_condition", builder.getEmvChipCondition()); // [PREV_SUCCESS, PREV_FAILED]

                // Avoid setting transaction types requesting to: POST /payment-methods
                if (!(builderTransactionType == TransactionType.Tokenize || builderTransactionType == TransactionType.Verify)) {
                    card.set("cvv_indicator", !getValueIfNotNull(cardData.getCvnPresenceIndicator()).equals("0") ? getCvvIndicator(cardData.getCvnPresenceIndicator()) : null); // [ILLEGIBLE, NOT_PRESENT, PRESENT]
                    card.set("funding", builderPaymentMethod.getPaymentMethodType() == PaymentMethodType.Debit ? "DEBIT" : "CREDIT"); // [DEBIT, CREDIT]
                }

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
                                    .setEndpoint(merchantUrl + "/payment-methods")
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
                                        .setEndpoint(merchantUrl + "/payment-methods")
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
                                            //.set("entry_mode", getEntryMode(builder, Channel.valueOf(gateway.getGpApiConfig().getChannel())))
                                            .set("entry_mode", getEntryMode(builder, gateway.getGpApiConfig().getChannel()))
                                            .set("id", ((ITokenizable) builderPaymentMethod).getToken())
                            );
                        }

                        return
                                new GpApiRequest()
                                        .setVerb(GpApiRequest.HttpMethod.Post)
                                        .setEndpoint(merchantUrl + "/verifications")
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
                                    .setEndpoint(merchantUrl + "/verifications")
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

            // Tokenized Payment Method
            if (builderPaymentMethod instanceof ITokenizable) {
                String token = ((ITokenizable) builderPaymentMethod).getToken();
                if (!StringUtils.isNullOrEmpty(token)) {
                    paymentMethod.set("id", token);
                }
            }
        }
        // Payment Method Storage Mode
        if (builder.isRequestMultiUseToken()) {
            paymentMethod.set("storage_mode", "ON_SUCCESS");
        }

        // Pin Block
        if (builderPaymentMethod instanceof IPinProtected) {
            if (paymentMethod.get("card") != null) {
                paymentMethod.get("card").set("pin_block", ((IPinProtected) builderPaymentMethod).getPinBlock());
            }
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

        if(builderPaymentMethod instanceof EBT) {
            EBT ebt = (EBT) builderPaymentMethod;
            paymentMethod.set("name", ebt.getCardHolderName());
        }

        if (builderPaymentMethod instanceof eCheck) {
            eCheck check = (eCheck) builderPaymentMethod;
            paymentMethod.set("name", check.getCheckHolderName());

            JsonDoc bankTransfer =
                    new JsonDoc()
                            .set("account_number", check.getAccountNumber())
                            .set("account_type", (check.getAccountType() != null) ? EnumUtils.getMapping(Target.GP_API, check.getAccountType()) : null)
                            .set("check_reference", check.getCheckReference())
                            .set("sec_code", check.getSecCode())
                            .set("narrative", check.getMerchantNotes());

            JsonDoc bank =
                    new JsonDoc()
                            .set("code", check.getRoutingNumber())
                            .set("name", check.getBankName());

            if(check.getBankAddress() != null) {
                Address checkBankAddress = check.getBankAddress();
                JsonDoc address =
                        new JsonDoc()
                                .set("line_1", checkBankAddress.getStreetAddress1())
                                .set("line_2", checkBankAddress.getStreetAddress2())
                                .set("line_3", checkBankAddress.getStreetAddress3())
                                .set("city", checkBankAddress.getCity())
                                .set("postal_code", checkBankAddress.getPostalCode())
                                .set("state", checkBankAddress.getState())
                                .set("country", checkBankAddress.getCountryCode());

                bank.set("address", address);
            }

            bankTransfer.set("bank", bank);

            paymentMethod.set("bank_transfer", bankTransfer);

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

        if (builderPaymentMethod instanceof eCheck) {
            data.set("payer", setPayerInformation(builder));
        }

        // Set Order Reference
        if (!StringUtils.isNullOrEmpty(builder.getOrderId())) {
            JsonDoc order =
                    new JsonDoc()
                            .set("reference", builder.getOrderId());

            data.set("order", order);
        }

        // Stored Credential
        if (builder.getStoredCredential() != null) {
            data.set("initiator", EnumUtils.getMapping(Target.GP_API, builder.getStoredCredential().getInitiator()));
            JsonDoc storedCredential =
                    new JsonDoc()
                            .set("model", EnumUtils.getMapping(Target.GP_API, builder.getStoredCredential().getType()))
                            .set("reason", EnumUtils.getMapping(Target.GP_API, builder.getStoredCredential().getReason()))
                            .set("sequence", EnumUtils.getMapping(Target.GP_API, builder.getStoredCredential().getSequence()));
            data.set("stored_credential", storedCredential);
        }

        return
                new GpApiRequest()
                        .setVerb(GpApiRequest.HttpMethod.Post)
                        .setEndpoint(merchantUrl + "/transactions")
                        .setRequestBody(data.toString());
    }

    private static JsonDoc setPayerInformation(AuthorizationBuilder builder) {
        JsonDoc payer = new JsonDoc();
        payer.set("reference", builder.getCustomerId() != null ? builder.getCustomerId() : (builder.getCustomerData() != null ? builder.getCustomerData().getId() : null));

        if(builder.getPaymentMethod() instanceof eCheck) {
            JsonDoc billingAddress = new JsonDoc();

            Address builderBillingAddress = builder.getBillingAddress();

            if(builderBillingAddress != null) {
                billingAddress
                        .set("line_1", builderBillingAddress.getStreetAddress1())
                        .set("line_2", builderBillingAddress.getStreetAddress2())
                        .set("city", builderBillingAddress.getCity())
                        .set("postal_code", builderBillingAddress.getPostalCode())
                        .set("state", builderBillingAddress.getState())
                        .set("country", builderBillingAddress.getCountryCode());

                payer.set("billing_address", billingAddress);
            }

            if (builder.getCustomerData() != null) {
                payer.set("name", builder.getCustomerData().getFirstName() + " " + builder.getCustomerData().getLastName());
                payer.set("date_of_birth", builder.getCustomerData().getDateOfBirth());
                payer.set("landline_phone", builder.getCustomerData().getHomePhone());
                payer.set("mobile_phone", builder.getCustomerData().getMobilePhone());
            }
        }
        return payer;
    }

    private static String getEntryMode(AuthorizationBuilder builder, String channel) {
        IPaymentMethod builderPaymentMethod = builder.getPaymentMethod();

        if (channel.equals(Channel.CardPresent.getValue())) {
            if (builderPaymentMethod instanceof ITrackData) {
                ITrackData paymentMethod = (ITrackData) builderPaymentMethod;
                if (builder.getTagData() != null) {
                    if (paymentMethod.getEntryMethod() == EntryMethod.Proximity) {
                        return "CONTACTLESS_CHIP";
                    }
                    return "CHIP";
                }
                if (paymentMethod.getEntryMethod() == EntryMethod.Swipe) {
                    return "SWIPE";
                }
            }
            if (builderPaymentMethod instanceof ICardData && ((ICardData) builderPaymentMethod).isCardPresent()) {
                return "MANUAL";
            }
            return "SWIPE";
        }
        else {
            if (builderPaymentMethod instanceof ICardData) {
                ICardData paymentMethod = (ICardData) builderPaymentMethod;
                if (paymentMethod.isReaderPresent()) {
                    return "ECOM";
                }
                else {
                    if (paymentMethod.getEntryMethod() != null) {
                        switch (paymentMethod.getEntryMethod()) {
                            case Phone:
                                return "PHONE";
                            case Moto:
                                return "MOTO";
                            case Mail:
                                return "MAIL";
                            default:
                                break;
                        }
                    }
                }
            }
            return "ECOM";
        }
    }

    private static String getCaptureMode(AuthorizationBuilder builder) {
        if (builder.isMultiCapture()) {
            return "MULTIPLE";
        }
        else if (builder.getTransactionType() == TransactionType.Auth) {
            return "LATER";
        }
        return "AUTO";
    }

    private static String getCvvIndicator(CvnPresenceIndicator cvnPresenceIndicator) {
        if(cvnPresenceIndicator == null) return "";
        switch (cvnPresenceIndicator) {
            case Present:
                return "PRESENT";
            case Illegible:
                return "ILLEGIBLE";
            case NotOnCard:
                return "NOT_ON_CARD";
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
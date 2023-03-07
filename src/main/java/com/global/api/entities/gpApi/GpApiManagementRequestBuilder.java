package com.global.api.entities.gpApi;

import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.DccRateData;
import com.global.api.entities.DisputeDocument;
import com.global.api.entities.enums.PaymentMethodName;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.Target;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.gateways.GpApiConnector;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.paymentMethods.ITokenizable;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.utils.EnumUtils;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import lombok.Setter;
import lombok.var;

import java.util.ArrayList;
import java.util.HashMap;

public class GpApiManagementRequestBuilder {

    @Getter @Setter private static HashMap<String, ArrayList<String>> allowedActions;

    public static GpApiRequest buildRequest(ManagementBuilder builder, GpApiConnector gateway) throws GatewayException, BuilderException {
        JsonDoc data = new JsonDoc();

        TransactionType builderTransactionType = builder.getTransactionType();
        IPaymentMethod builderPaymentMethod = builder.getPaymentMethod();

        getAllowedActions();

        String merchantUrl = !StringUtils.isNullOrEmpty(gateway.getGpApiConfig().getMerchantId()) ? "/merchants/" + gateway.getGpApiConfig().getMerchantId() : "";

        if (builderPaymentMethod != null && builderPaymentMethod.getPaymentMethodType() == PaymentMethodType.BankPayment) {
            if (    allowedActions.get(PaymentMethodType.BankPayment.toString()) == null ||
                    !allowedActions.get(PaymentMethodType.BankPayment.toString()).contains(builder.getTransactionType().toString())) {
                throw new BuilderException("The " + builder.getTransactionType().toString() + " is not supported for " + PaymentMethodName.BankPayment);
            }
        }

        if (builderTransactionType == TransactionType.Capture) {
            data.set("amount", StringUtils.toNumeric(builder.getAmount()));
            data.set("gratuity_amount", StringUtils.toNumeric(builder.getGratuity()));
            data.set("currency_conversion", builder.getDccRateData() != null ? getDccId(builder.getDccRateData()) : null);

            return
                    new GpApiRequest()
                            .setVerb(GpApiRequest.HttpMethod.Post)
                            .setEndpoint(merchantUrl + "/transactions/" + builder.getTransactionId() + "/capture")
                            .setRequestBody(data.toString());

        }
        else if (builderTransactionType == TransactionType.Refund) {
            data.set("amount", StringUtils.toNumeric(builder.getAmount()));
            data.set("currency_conversion", builder.getDccRateData() != null ? getDccId(builder.getDccRateData()) : null);

            return
                    new GpApiRequest()
                            .setVerb(GpApiRequest.HttpMethod.Post)
                            .setEndpoint(merchantUrl + "/transactions/" + builder.getTransactionId() + "/refund")
                            .setRequestBody(data.toString());

        }
        else if (builderTransactionType == TransactionType.Reversal) {
            data.set("amount", StringUtils.toNumeric(builder.getAmount()));
            data.set("currency_conversion", builder.getDccRateData() != null ? getDccId(builder.getDccRateData()) : null);

            return
                    new GpApiRequest()
                            .setVerb(GpApiRequest.HttpMethod.Post)
                            .setEndpoint(merchantUrl + "/transactions/" + builder.getTransactionId() + "/reversal")
                            .setRequestBody(data.toString());

        }
        else if (builderTransactionType == TransactionType.TokenUpdate) {
            if (!(builderPaymentMethod instanceof CreditCardData)) {
                throw new GatewayException("Payment method doesn't support this action!");
            }

            CreditCardData cardData = (CreditCardData) builderPaymentMethod;

            JsonDoc card =
                    new JsonDoc()
                            .set("expiry_month", cardData.getExpMonth() != null ? StringUtils.padLeft(cardData.getExpMonth().toString(), 2, '0') : "")
                            .set("expiry_year", cardData.getExpYear() != null ? StringUtils.padLeft(cardData.getExpYear().toString(), 4, '0').substring(2, 4) : "")
                            .set("number", cardData.getNumber() != null ? cardData.getNumber() : null);

            data =
                    new JsonDoc()
                            .set("card", card)
                            .set("usage_mode", builder.getPaymentMethodUsageMode() != null ? builder.getPaymentMethodUsageMode() : null)
                            .set("name", cardData.getCardHolderName() != null ? cardData.getCardHolderName() : null);

            return
                    new GpApiRequest()
                            .setVerb(GpApiRequest.HttpMethod.Patch)
                            .setEndpoint(merchantUrl + "/payment-methods/" + ((ITokenizable) builderPaymentMethod).getToken())
                            .setRequestBody(data.toString());
        }
        else if (builderTransactionType == TransactionType.TokenDelete && builderPaymentMethod instanceof ITokenizable) {
            return
                    new GpApiRequest()
                        .setVerb(GpApiRequest.HttpMethod.Delete)
                        .setEndpoint(merchantUrl + "/payment-methods/" + ((ITokenizable) builderPaymentMethod).getToken());
        }
        else if (builderTransactionType == TransactionType.DisputeAcceptance) {
            return
                    new GpApiRequest()
                            .setVerb(GpApiRequest.HttpMethod.Post)
                            .setEndpoint(merchantUrl + "/disputes/" + builder.getDisputeId() + "/acceptance");
        }
        else if (builderTransactionType == TransactionType.DisputeChallenge) {
            JsonArray documentsJsonArray = new JsonArray();
            for(DisputeDocument document : builder.getDisputeDocuments()) {
                JsonObject innerJsonDoc = new JsonObject();

                if(document.getType() != null ) {
                    innerJsonDoc.add("type", new JsonPrimitive(document.getType()));
                }

                if (document.getBase64Content() != null) {
                    innerJsonDoc.add("b64_content", new JsonPrimitive(document.getBase64Content()));
                }

                documentsJsonArray.add(innerJsonDoc);
            }

            JsonObject disputeChallengeData = new JsonObject();
            disputeChallengeData.add("documents", documentsJsonArray);

            return
                    new GpApiRequest()
                            .setVerb(GpApiRequest.HttpMethod.Post)
                            .setEndpoint(merchantUrl + "/disputes/" + builder.getDisputeId() + "/challenge")
                            .setRequestBody(disputeChallengeData.toString());
        }
        else if (builderTransactionType == TransactionType.BatchClose) {
            return new GpApiRequest()
                    .setVerb(GpApiRequest.HttpMethod.Post)
                    .setEndpoint(merchantUrl + "/batches/" + builder.getBatchReference());
        }
        else if (builderTransactionType == TransactionType.Reauth) {
            data = new JsonDoc()
                            .set("amount", builder.getAmount());

            if (builderPaymentMethod.getPaymentMethodType() == PaymentMethodType.ACH) {
                data.set("description", builder.getDescription());

                if (builder.getBankTransferDetails() != null) {
                    var bankTransferDetails = builder.getBankTransferDetails();

                    var paymentMethod =
                            new JsonDoc()
                                    .set("narrative", bankTransferDetails.getMerchantNotes());

                    var bankTransfer =
                            new JsonDoc()
                                    .set("account_number", bankTransferDetails.getAccountNumber())
                                    .set("account_type", (bankTransferDetails.getAccountType() != null) ? EnumUtils.getMapping(Target.GP_API, bankTransferDetails.getAccountType()) : null)
                                    .set("check_reference", bankTransferDetails.getCheckReference());

                    var bank =
                            new JsonDoc()
                                    .set("code", bankTransferDetails.getRoutingNumber())
                                    .set("name", bankTransferDetails.getBankName());

                    bankTransfer.set("bank", bank);

                    paymentMethod.set("bank_transfer", bankTransfer);

                    data.set("payment_method", paymentMethod);
                }
            }

            return new GpApiRequest()
                    .setVerb(GpApiRequest.HttpMethod.Post)
                    .setEndpoint(merchantUrl + "/transactions/" + builder.getTransactionId() + "/reauthorization")
                    .setRequestBody(data.toString());
        }
        else if (builderTransactionType == TransactionType.Confirm) {
            if (builderPaymentMethod instanceof TransactionReference && builderPaymentMethod.getPaymentMethodType() == PaymentMethodType.APM) {
                var transactionReference = (com.global.api.paymentMethods.TransactionReference) builderPaymentMethod;
                var apmResponse = transactionReference.getAlternativePaymentResponse();
                var apm =
                        new JsonDoc()
                                .set("provider", apmResponse.getProviderName())
                                .set("provider_payer_reference", apmResponse.getProviderReference());

                var payment_method =
                        new JsonDoc()
                                .set("apm", apm);

                data
                        .set("payment_method", payment_method);

                return new GpApiRequest()
                        .setVerb(GpApiRequest.HttpMethod.Post)
                        .setEndpoint(merchantUrl + "/transactions/" + builder.getTransactionId() + "/confirmation")
                        .setRequestBody(data.toString());
            }
        }
        else if (builderTransactionType == TransactionType.Auth) {
            data.set("amount", builder.getAmount());

            if (builder.getLodgingData() != null) {
                var lodging = builder.getLodgingData();
                if (lodging.getItems() != null) {
                    var lodginItems = new ArrayList<HashMap<String, Object>>();

                    for (var item : lodging.getItems()) {
                        HashMap<String, Object> item2 = new HashMap<>();
                        item2.put("Types", item.getTypes());
                        item2.put("Reference", item.getReference());
                        item2.put("TotalAmount", item.getTotalAmount());
                        item2.put("paymentMethodProgramCodes", item.getPaymentMethodProgramCodes());

                        lodginItems.add(item2);
                    }

                    var lodgingData =
                            new JsonDoc()
                                    .set("booking_reference", lodging.getBookingReference())
                                    .set("duration_days", lodging.getStayDuration())
                                    .set("date_checked_in", lodging.getCheckInDate() != null ? lodging.getCheckInDate().toString("yyyy-MM-dd") : null)
                                    .set("date_checked_out", lodging.getCheckOutDate() != null ? lodging.getCheckOutDate().toString("yyyy-MM-dd") : null)
                                    .set("daily_rate_amount", StringUtils.toNumeric(lodging.getRate()))
                                    .set("charge_items", lodginItems);

                    data.set("lodging", lodgingData);
                }
            }

            return new GpApiRequest()
                    .setVerb(GpApiRequest.HttpMethod.Post)
                    .setEndpoint(merchantUrl + "/transactions/" + builder.getTransactionId() + "/incremental")
                    .setRequestBody(data.toString());
        }
        else if (builderTransactionType == TransactionType.Edit) {
            var card =
                    new JsonDoc()
                            .set("tag", builder.getTagData());

            var payment_method =
                    new JsonDoc()
                            .set("card", card);

            data
                    .set("amount",  StringUtils.toNumeric(builder.getAmount()))
                    .set("gratuity_amount",  StringUtils.toNumeric(builder.getGratuity()))
                    .set("payment_method",  payment_method);

            return new GpApiRequest()
                    .setVerb(GpApiRequest.HttpMethod.Post)
                    .setEndpoint(merchantUrl + "/transactions/" + builder.getTransactionId() + "/adjustment")
                    .setRequestBody(data.toString());
        }
        else if (builderTransactionType == TransactionType.PayLinkUpdate) {
            var payLinkData = builder.getPayLinkData();

            data =
                    new JsonDoc()
                            .set("usage_mode", payLinkData.getUsageMode() != null ? payLinkData.getUsageMode().getValue() : null)
                            .set("usage_limit", payLinkData.getUsageLimit() != null ? payLinkData.getUsageLimit() : null)
                            .set("name", payLinkData.getName() != null ? payLinkData.getName() : null)
                            .set("description", builder.getDescription() != null ? builder.getDescription() : null)
                            .set("type", payLinkData.getType() != null ? payLinkData.getType().toString() : null)
                            .set("status", payLinkData.getStatus() != null ? payLinkData.getStatus().toString() : null)
                            .set("shippable", payLinkData.isShippable() == Boolean.TRUE ? "YES" : "NO")
                            .set("shipping_amount", StringUtils.toNumeric(payLinkData.getShippingAmount()));

            var transactions =
                    new JsonDoc()
                            .set("amount", builder.getAmount() != null ? StringUtils.toNumeric(builder.getAmount()) : null);

            data
                    .set("transactions", transactions)
                    .set("expiration_date", payLinkData.getExpirationDate() != null ? payLinkData.getExpirationDate().toString("yyyy-MM-dd") : null)
                    .set("images", payLinkData.getImages() != null ? payLinkData.getImages().toString() : null);

            return new GpApiRequest()
                    .setVerb(GpApiRequest.HttpMethod.Patch)
                    .setEndpoint(merchantUrl + "/links/" + builder.getPaymentLinkId())
                    .setRequestBody(data.toString());

        } else if (builderTransactionType == TransactionType.Release || builderTransactionType == TransactionType.Hold) {
            var payload =
                    new JsonDoc()
                            .set("reason_code", builder.getReasonCode() != null ? EnumUtils.getMapping(Target.GP_API, builder.getReasonCode()) : null);

            var endpoint =
                    builderTransactionType == TransactionType.Release ?
                            "release" :
                            builderTransactionType == TransactionType.Hold ? "hold" : null;

            return
                    new GpApiRequest()
                            .setVerb(GpApiRequest.HttpMethod.Post)
                            .setEndpoint(merchantUrl + "/transactions/" + builder.getTransactionId() + "/" + endpoint)
                            .setRequestBody(payload.toString());
        }

        return null;
    }

    static JsonDoc getDccId(DccRateData dccRateData)
    {
        return
                new JsonDoc()
                        .set("id", dccRateData.getDccId());
    }

    private static void getAllowedActions() {
        if (allowedActions == null) {
            allowedActions = new HashMap<>();
            allowedActions.put(PaymentMethodType.BankPayment.toString(), null);
        }
    }
}

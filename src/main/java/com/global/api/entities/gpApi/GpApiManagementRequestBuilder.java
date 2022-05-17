package com.global.api.entities.gpApi;

import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.DccRateData;
import com.global.api.entities.DisputeDocument;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.Target;
import com.global.api.entities.enums.TransactionType;
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
import lombok.var;

public class GpApiManagementRequestBuilder {

    public static GpApiRequest buildRequest(ManagementBuilder builder, GpApiConnector gateway) throws GatewayException {
        String merchantUrl = gateway.getMerchantUrl();
        JsonDoc data = new JsonDoc();

        TransactionType builderTransactionType = builder.getTransactionType();
        IPaymentMethod builderPaymentMethod = builder.getPaymentMethod();

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

        return null;
    }

    static JsonDoc getDccId(DccRateData dccRateData)
    {
        return
                new JsonDoc()
                        .set("id", dccRateData.getDccId());
    }
}

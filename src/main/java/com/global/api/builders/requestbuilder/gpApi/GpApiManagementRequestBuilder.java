package com.global.api.builders.requestbuilder.gpApi;

import com.global.api.builders.ManagementBuilder;
import com.global.api.builders.TransactionBuilder;
import com.global.api.entities.DccRateData;
import com.global.api.entities.DisputeDocument;
import com.global.api.entities.IRequestBuilder;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.PaymentMethodName;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.Target;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.entities.gpApi.GpApiRequest;
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

public class GpApiManagementRequestBuilder implements IRequestBuilder<ManagementBuilder> {

    @Getter @Setter private static HashMap<String, ArrayList<String>> allowedActions;

    @Override
    public GpApiRequest buildRequest(ManagementBuilder builder, GpApiConnector gateway) throws GatewayException, BuilderException, UnsupportedTransactionException {
        JsonDoc data = new JsonDoc();

        TransactionType builderTransactionType = builder.getTransactionType();
        IPaymentMethod builderPaymentMethod = builder.getPaymentMethod();

        getAllowedActions();

        String merchantUrl = !StringUtils.isNullOrEmpty(gateway.getGpApiConfig().getMerchantId()) ? GpApiRequest.MERCHANT_MANAGEMENT_ENDPOINT + "/" + gateway.getGpApiConfig().getMerchantId() : "";

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

            return (GpApiRequest)
                    new GpApiRequest()
                            .setVerb(GpApiRequest.HttpMethod.Post)
                            .setEndpoint(merchantUrl + GpApiRequest.TRANSACTION_ENDPOINT + "/" + builder.getTransactionId() + "/capture")
                            .setRequestBody(data.toString());

        }
        else if (builderTransactionType == TransactionType.Refund) {
            data.set("amount", StringUtils.toNumeric(builder.getAmount()));
            data.set("currency_conversion", builder.getDccRateData() != null ? getDccId(builder.getDccRateData()) : null);

            return (GpApiRequest)
                    new GpApiRequest()
                            .setVerb(GpApiRequest.HttpMethod.Post)
                            .setEndpoint(merchantUrl + GpApiRequest.TRANSACTION_ENDPOINT + "/" + builder.getTransactionId() + "/refund")
                            .setRequestBody(data.toString());

        }
        else if (builderTransactionType == TransactionType.Reversal) {
            data.set("amount", StringUtils.toNumeric(builder.getAmount()));
            data.set("currency_conversion", builder.getDccRateData() != null ? getDccId(builder.getDccRateData()) : null);

            String endpoint = merchantUrl;
            if (builder.getPaymentMethod().getPaymentMethodType() == PaymentMethodType.AccountFunds) {
                if (null != builder.getFundsData()) {
                    String merchantId = builder.getFundsData().getMerchantId();
                    if (!StringUtils.isNullOrEmpty(merchantId)) {
                        endpoint = GpApiRequest.MERCHANT_MANAGEMENT_ENDPOINT + "/" + merchantId;
                    }
                }
                endpoint = endpoint + GpApiRequest.TRANSFER_ENDPOINT + "/" + builder.getTransactionId() + "/reversal";
            } else {
                endpoint  = endpoint + GpApiRequest.TRANSACTION_ENDPOINT + "/" + builder.getTransactionId() + "/reversal";
            }

            return (GpApiRequest)
                    new GpApiRequest()
                            .setVerb(GpApiRequest.HttpMethod.Post)
                            .setEndpoint(endpoint)
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

            return (GpApiRequest)
                    new GpApiRequest()
                            .setVerb(GpApiRequest.HttpMethod.Patch)
                            .setEndpoint(merchantUrl + GpApiRequest.PAYMENT_METHODS_ENDPOINT + "/" + ((ITokenizable) builderPaymentMethod).getToken())
                            .setRequestBody(data.toString());
        }
        else if (builderTransactionType == TransactionType.TokenDelete && builderPaymentMethod instanceof ITokenizable) {
            return (GpApiRequest)
                    new GpApiRequest()
                        .setVerb(GpApiRequest.HttpMethod.Delete)
                        .setEndpoint(merchantUrl + GpApiRequest.PAYMENT_METHODS_ENDPOINT + "/" + ((ITokenizable) builderPaymentMethod).getToken());
        }
        else if (builderTransactionType == TransactionType.DisputeAcceptance) {
            return (GpApiRequest)
                    new GpApiRequest()
                            .setVerb(GpApiRequest.HttpMethod.Post)
                            .setEndpoint(merchantUrl + GpApiRequest.DISPUTES_ENDPOINT + "/" + builder.getDisputeId() + "/acceptance");
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

            return  (GpApiRequest)
                    new GpApiRequest()
                            .setVerb(GpApiRequest.HttpMethod.Post)
                            .setEndpoint(merchantUrl + GpApiRequest.DISPUTES_ENDPOINT + "/" + builder.getDisputeId() + "/challenge")
                            .setRequestBody(disputeChallengeData.toString());
        }
        else if (builderTransactionType == TransactionType.BatchClose) {
            return  (GpApiRequest) new GpApiRequest()
                    .setVerb(GpApiRequest.HttpMethod.Post)
                    .setEndpoint(merchantUrl + GpApiRequest.BATCHES_ENDPOINT + "/" + builder.getBatchReference());
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

            return (GpApiRequest) new GpApiRequest()
                    .setVerb(GpApiRequest.HttpMethod.Post)
                    .setEndpoint(merchantUrl + GpApiRequest.TRANSACTION_ENDPOINT + "/" + builder.getTransactionId() + "/reauthorization")
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

                return  (GpApiRequest) new GpApiRequest()
                        .setVerb(GpApiRequest.HttpMethod.Post)
                        .setEndpoint(merchantUrl + GpApiRequest.TRANSACTION_ENDPOINT + "/" + builder.getTransactionId() + "/confirmation")
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

            return  (GpApiRequest) new GpApiRequest()
                    .setVerb(GpApiRequest.HttpMethod.Post)
                    .setEndpoint(merchantUrl + GpApiRequest.TRANSACTION_ENDPOINT + "/" + builder.getTransactionId() + "/incremental")
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

            return  (GpApiRequest) new GpApiRequest()
                    .setVerb(GpApiRequest.HttpMethod.Post)
                    .setEndpoint(merchantUrl + GpApiRequest.TRANSACTION_ENDPOINT + "/" + builder.getTransactionId() + "/adjustment")
                    .setRequestBody(data.toString());
        }
        else if (builderTransactionType == TransactionType.PayByLinkUpdate) {
            var payByLinkData = builder.getPayByLinkData();

            data =
                    new JsonDoc()
                            .set("usage_mode", payByLinkData.getUsageMode() != null ? payByLinkData.getUsageMode().getValue() : null)
                            .set("usage_limit", payByLinkData.getUsageLimit() != null ? payByLinkData.getUsageLimit() : null)
                            .set("name", payByLinkData.getName() != null ? payByLinkData.getName() : null)
                            .set("description", builder.getDescription() != null ? builder.getDescription() : null)
                            .set("type", payByLinkData.getType() != null ? payByLinkData.getType().toString() : null)
                            .set("status", payByLinkData.getStatus() != null ? payByLinkData.getStatus().toString() : null)
                            .set("shippable", payByLinkData.isShippable() == Boolean.TRUE ? "YES" : "NO")
                            .set("shipping_amount", StringUtils.toNumeric(payByLinkData.getShippingAmount()));

            var transactions =
                    new JsonDoc()
                            .set("amount", builder.getAmount() != null ? StringUtils.toNumeric(builder.getAmount()) : null);

            data
                    .set("transactions", transactions)
                    .set("expiration_date", payByLinkData.getExpirationDate() != null ? payByLinkData.getExpirationDate().toString("yyyy-MM-dd") : null)
                    .set("images", payByLinkData.getImages() != null ? payByLinkData.getImages().toString() : null);

            return  (GpApiRequest) new GpApiRequest()
                    .setVerb(GpApiRequest.HttpMethod.Patch)
                    .setEndpoint(merchantUrl + GpApiRequest.PAYBYLINK_ENDPOINT + "/" + builder.getPaymentLinkId())
                    .setRequestBody(data.toString());

        }
        else if (builderTransactionType == TransactionType.Release || builderTransactionType == TransactionType.Hold) {
            var payload =
                    new JsonDoc()
                            .set("reason_code", builder.getReasonCode() != null ? EnumUtils.getMapping(Target.GP_API, builder.getReasonCode()) : null);

            var endpoint =
                    builderTransactionType == TransactionType.Release ?
                            "release" :
                            builderTransactionType == TransactionType.Hold ? "hold" : null;

            return  (GpApiRequest)
                    new GpApiRequest()
                            .setVerb(GpApiRequest.HttpMethod.Post)
                            .setEndpoint(merchantUrl + GpApiRequest.TRANSACTION_ENDPOINT + "/" + builder.getTransactionId() + "/" + endpoint)
                            .setRequestBody(payload.toString());

        }

        //Transaction split
        else if (builderTransactionType == TransactionType.SplitFunds) {

            HashMap<String, Object> request = new HashMap<>();
            request.put("recipient_account_id", builder.getFundsData().getRecipientAccountId());
            request.put("reference", builder.getReference());
            request.put("description", builder.getDescription());
            request.put("amount", StringUtils.toNumeric(builder.getAmount()));

            ArrayList<HashMap<String, Object>> split = new ArrayList<>();
            split.add(request);

            JsonDoc transfer = new JsonDoc();
            transfer.set("transfers", split);

            String endpoint = merchantUrl;
            if (!StringUtils.isNullOrEmpty(builder.getFundsData().getMerchantId())) {
                endpoint = GpApiRequest.MERCHANT_MANAGEMENT_ENDPOINT + "/" + builder.getFundsData().getMerchantId();
            }

            return  (GpApiRequest)
                    new GpApiRequest()
                            .setVerb(GpApiRequest.HttpMethod.Post)
                            .setEndpoint(endpoint + GpApiRequest.TRANSACTION_ENDPOINT + "/" + builder.getTransactionId() + "/split")
                            .setRequestBody(transfer.toString());
        }

        return null;
    }

    @Override
    public boolean canProcess(Object builder) {
        return builder instanceof ManagementBuilder;
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

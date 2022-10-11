package com.global.api.entities.gpApi;

import com.global.api.builders.ReportBuilder;
import com.global.api.builders.TransactionReportBuilder;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.enums.Target;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.gateways.GpApiConnector;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.utils.EnumUtils;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;

import static com.global.api.gateways.GpApiConnector.getDateIfNotNull;
import static com.global.api.gateways.GpApiConnector.getValueIfNotNull;

public class GpApiReportRequestBuilder {

    public static GpApiRequest buildRequest(ReportBuilder builder, GpApiConnector gateway) throws GatewayException, UnsupportedTransactionException {
        String merchantUrl = gateway.getMerchantUrl();
        if (builder instanceof TransactionReportBuilder) {

            GpApiRequest request = new GpApiRequest();
            TransactionReportBuilder<TransactionSummary> trb = (TransactionReportBuilder<TransactionSummary>) builder;

            switch (builder.getReportType()) {

                case TransactionDetail:
                    return request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint(merchantUrl + "/transactions/" + trb.getTransactionId());

                case FindTransactionsPaged:
                    request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint(merchantUrl + "/transactions");

                    request.addQueryStringParam("page", String.valueOf(trb.getPage()));
                    request.addQueryStringParam("page_size", String.valueOf(trb.getPageSize()));
                    request.addQueryStringParam("order_by", getValueIfNotNull(trb.getTransactionOrderBy()));
                    request.addQueryStringParam("order", getValueIfNotNull(trb.getOrder()));
                    request.addQueryStringParam("id", trb.getTransactionId());
                    request.addQueryStringParam("type", getValueIfNotNull(trb.getSearchBuilder().getPaymentType()));
                    request.addQueryStringParam("channel", getValueIfNotNull(trb.getSearchBuilder().getChannel()));
                    request.addQueryStringParam("amount", StringUtils.toNumeric(trb.getSearchBuilder().getAmount()));
                    request.addQueryStringParam("currency", trb.getSearchBuilder().getCurrency());
                    request.addQueryStringParam("number_first6", trb.getSearchBuilder().getCardNumberFirstSix());
                    request.addQueryStringParam("number_last4", trb.getSearchBuilder().getCardNumberLastFour());
                    request.addQueryStringParam("token_first6", trb.getSearchBuilder().getTokenFirstSix());
                    request.addQueryStringParam("token_last4", trb.getSearchBuilder().getTokenLastFour());
                    request.addQueryStringParam("account_name", trb.getSearchBuilder().getAccountName());
                    request.addQueryStringParam("brand", trb.getSearchBuilder().getCardBrand());
                    request.addQueryStringParam("brand_reference", trb.getSearchBuilder().getBrandReference());
                    request.addQueryStringParam("authcode", trb.getSearchBuilder().getAuthCode());
                    request.addQueryStringParam("reference", trb.getSearchBuilder().getReferenceNumber());
                    request.addQueryStringParam("status", getValueIfNotNull(trb.getSearchBuilder().getTransactionStatus()));
                    request.addQueryStringParam("from_time_created", getDateIfNotNull(trb.getStartDate()));
                    request.addQueryStringParam("to_time_created", getDateIfNotNull(trb.getEndDate()));
                    request.addQueryStringParam("country", trb.getSearchBuilder().getCountry());
                    request.addQueryStringParam("batch_id", trb.getSearchBuilder().getBatchId());
                    request.addQueryStringParam("entry_mode", getValueIfNotNull(trb.getSearchBuilder().getPaymentEntryMode()));
                    request.addQueryStringParam("name", trb.getSearchBuilder().getName());
                    request.addQueryStringParam("payment_method", EnumUtils.getMapping(Target.GP_API, trb.getSearchBuilder().getPaymentMethodName()));

                    return request;

                case FindSettlementTransactionsPaged:
                    request = new GpApiRequest()
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint(merchantUrl + "/settlement/transactions");

                    request.addQueryStringParam("page", String.valueOf(trb.getPage()));
                    request.addQueryStringParam("page_size", String.valueOf(trb.getPageSize()));
                    request.addQueryStringParam("order_by", getValueIfNotNull(trb.getTransactionOrderBy()));
                    request.addQueryStringParam("order", getValueIfNotNull(trb.getOrder()));
                    request.addQueryStringParam("number_first6", trb.getSearchBuilder().getCardNumberFirstSix());
                    request.addQueryStringParam("number_last4", trb.getSearchBuilder().getCardNumberLastFour());
                    request.addQueryStringParam("deposit_status", getValueIfNotNull(trb.getSearchBuilder().getDepositStatus()));
                    request.addQueryStringParam("account_name", gateway.getGpApiConfig().getAccessTokenInfo().getDataAccountName());
                    request.addQueryStringParam("brand", trb.getSearchBuilder().getCardBrand());
                    request.addQueryStringParam("arn", trb.getSearchBuilder().getAquirerReferenceNumber());
                    request.addQueryStringParam("brand_reference", trb.getSearchBuilder().getBrandReference());
                    request.addQueryStringParam("authcode", trb.getSearchBuilder().getAuthCode());
                    request.addQueryStringParam("reference", trb.getSearchBuilder().getReferenceNumber());
                    request.addQueryStringParam("status", getValueIfNotNull(trb.getSearchBuilder().getTransactionStatus()));
                    request.addQueryStringParam("from_time_created", getDateIfNotNull(trb.getStartDate()));
                    request.addQueryStringParam("to_time_created", getDateIfNotNull(trb.getEndDate()));
                    request.addQueryStringParam("deposit_id", trb.getSearchBuilder().getDepositReference());
                    request.addQueryStringParam("from_deposit_time_created", getDateIfNotNull(trb.getSearchBuilder().getStartDepositDate()));
                    request.addQueryStringParam("to_deposit_time_created", getDateIfNotNull(trb.getSearchBuilder().getEndDepositDate()));
                    request.addQueryStringParam("from_batch_time_created", getDateIfNotNull(trb.getSearchBuilder().getStartBatchDate()));
                    request.addQueryStringParam("to_batch_time_created", getDateIfNotNull(trb.getSearchBuilder().getEndBatchDate()));
                    request.addQueryStringParam("system.mid", trb.getSearchBuilder().getMerchantId());
                    request.addQueryStringParam("system.hierarchy", trb.getSearchBuilder().getSystemHierarchy());

                    return request;

                case DepositDetail:
                    return request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint(merchantUrl + "/settlement/deposits/" + trb.getSearchBuilder().getDepositReference());

                case FindDepositsPaged:
                    request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint(merchantUrl + "/settlement/deposits");

                    request.addQueryStringParam("page", String.valueOf(trb.getPage()));
                    request.addQueryStringParam("page_size", String.valueOf(trb.getPageSize()));
                    request.addQueryStringParam("order_by", getValueIfNotNull(trb.getDepositOrderBy()));
                    request.addQueryStringParam("order", getValueIfNotNull(trb.getOrder()));
                    request.addQueryStringParam("account_name", gateway.getGpApiConfig().getAccessTokenInfo().getDataAccountName());
                    request.addQueryStringParam("from_time_created", getDateIfNotNull(trb.getStartDate()));
                    request.addQueryStringParam("to_time_created", getDateIfNotNull(trb.getEndDate()));
                    request.addQueryStringParam("id", trb.getSearchBuilder().getDepositReference());
                    request.addQueryStringParam("status", getValueIfNotNull(trb.getSearchBuilder().getDepositStatus()));
                    request.addQueryStringParam("amount", StringUtils.toNumeric(trb.getSearchBuilder().getAmount()));
                    request.addQueryStringParam("masked_account_number_last4", trb.getSearchBuilder().getAccountNumberLastFour());
                    request.addQueryStringParam("system.mid", trb.getSearchBuilder().getMerchantId());
                    request.addQueryStringParam("system.hierarchy", trb.getSearchBuilder().getSystemHierarchy());

                    return request;

                case DisputeDetail:
                    return request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint(merchantUrl + "/disputes/" + trb.getSearchBuilder().getDisputeId());

                case DocumentDisputeDetail:
                    return request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint(merchantUrl + "/disputes/" + trb.getSearchBuilder().getDisputeId() +
                                    "/documents/" + trb.getSearchBuilder().getDisputeDocumentId());

                case FindDisputesPaged:
                    request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint(merchantUrl + "/disputes");

                    request.addQueryStringParam("page", String.valueOf(trb.getPage()));
                    request.addQueryStringParam("page_size", String.valueOf(trb.getPageSize()));
                    request.addQueryStringParam("order_by", getValueIfNotNull(trb.getDisputeOrderBy()));
                    request.addQueryStringParam("order", getValueIfNotNull(trb.getOrder()));
                    request.addQueryStringParam("arn", trb.getSearchBuilder().getAquirerReferenceNumber());
                    request.addQueryStringParam("brand", trb.getSearchBuilder().getCardBrand());
                    request.addQueryStringParam("status", getValueIfNotNull(trb.getSearchBuilder().getDisputeStatus()));
                    request.addQueryStringParam("stage", getValueIfNotNull(trb.getSearchBuilder().getDisputeStage()));
                    request.addQueryStringParam("from_stage_time_created", getDateIfNotNull(trb.getSearchBuilder().getStartStageDate()));
                    request.addQueryStringParam("to_stage_time_created", getDateIfNotNull(trb.getSearchBuilder().getEndStageDate()));
                    request.addQueryStringParam("system.mid", trb.getSearchBuilder().getMerchantId());
                    request.addQueryStringParam("system.hierarchy", trb.getSearchBuilder().getSystemHierarchy());

                    return request;

                case SettlementDisputeDetail:
                    return request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint(merchantUrl + "/settlement/disputes/" + trb.getSearchBuilder().getSettlementDisputeId());

                case FindSettlementDisputesPaged:
                    request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint(merchantUrl + "/settlement/disputes");

                    request.addQueryStringParam("account_name", gateway.getGpApiConfig().getAccessTokenInfo().getDataAccountName());
                    request.addQueryStringParam("deposit_id", trb.getSearchBuilder().getDepositReference());
                    request.addQueryStringParam("page", String.valueOf(trb.getPage()));
                    request.addQueryStringParam("page_size", String.valueOf(trb.getPageSize()));
                    request.addQueryStringParam("order_by", getValueIfNotNull(trb.getDisputeOrderBy()));
                    request.addQueryStringParam("order", getValueIfNotNull(trb.getOrder()));
                    request.addQueryStringParam("arn", trb.getSearchBuilder().getAquirerReferenceNumber());
                    request.addQueryStringParam("brand", trb.getSearchBuilder().getCardBrand());
                    request.addQueryStringParam("STATUS", getValueIfNotNull(trb.getSearchBuilder().getDisputeStatus()));
                    request.addQueryStringParam("stage", getValueIfNotNull(trb.getSearchBuilder().getDisputeStage()));
                    request.addQueryStringParam("from_stage_time_created", getDateIfNotNull(trb.getSearchBuilder().getStartStageDate()));
                    request.addQueryStringParam("to_stage_time_created", getDateIfNotNull(trb.getSearchBuilder().getEndStageDate()));
                    request.addQueryStringParam("from_deposit_time_created", getDateIfNotNull(trb.getSearchBuilder().getStartDepositDate()));
                    request.addQueryStringParam("to_deposit_time_created", getDateIfNotNull(trb.getSearchBuilder().getEndDepositDate()));
                    request.addQueryStringParam("system.mid", trb.getSearchBuilder().getMerchantId());
                    request.addQueryStringParam("system.hierarchy", trb.getSearchBuilder().getSystemHierarchy());

                    return request;

                case StoredPaymentMethodDetail:
                    return
                            request
                                    .setVerb(GpApiRequest.HttpMethod.Get)
                                    .setEndpoint(merchantUrl + "/payment-methods/" + trb.getSearchBuilder().getStoredPaymentMethodId());

                case FindStoredPaymentMethodsPaged:

                    if (trb.getSearchBuilder().getPaymentMethod() instanceof CreditCardData) {

                        CreditCardData paymentMethod = (CreditCardData) trb.getSearchBuilder().getPaymentMethod();

                        JsonDoc card =
                                new JsonDoc()
                                        .set("number", paymentMethod.getNumber())
                                        .set("expiry_month", paymentMethod.getExpMonth() != null ? StringUtils.padLeft(paymentMethod.getExpMonth().toString(), 2, '0') : null)
                                        .set("expiry_year", paymentMethod.getExpYear() != null ? StringUtils.padLeft(paymentMethod.getExpYear().toString(), 4, '0').substring(2, 4) : null);

                        JsonDoc data =
                                new JsonDoc()
                                        .set("account_name", gateway.getGpApiConfig().getAccessTokenInfo().getTokenizationAccountName())
                                        .set("reference", trb.getSearchBuilder().getReferenceNumber())
                                        .set("card", card != null ? card : null);

                        request
                                .setVerb(GpApiRequest.HttpMethod.Post)
                                .setEndpoint(merchantUrl + "/payment-methods/" + "search")
                                .setRequestBody(data.toString());

                        return request;
                    }


                    request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint(merchantUrl + "/payment-methods");

                    request.addQueryStringParam("page", String.valueOf(trb.getPage()));
                    request.addQueryStringParam("page_size", String.valueOf(trb.getPageSize()));
                    request.addQueryStringParam("order_by", getValueIfNotNull(trb.getStoredPaymentMethodOrderBy()));
                    request.addQueryStringParam("order", getValueIfNotNull(trb.getOrder()));
                    request.addQueryStringParam("id", trb.getSearchBuilder().getStoredPaymentMethodId());
                    request.addQueryStringParam("number_last4", trb.getSearchBuilder().getCardNumberLastFour());
                    request.addQueryStringParam("reference", trb.getSearchBuilder().getReferenceNumber());
                    request.addQueryStringParam("status", getValueIfNotNull(trb.getSearchBuilder().getStoredPaymentMethodStatus()));
                    request.addQueryStringParam("from_time_created", getDateIfNotNull(trb.getSearchBuilder().getStartDate()));
                    request.addQueryStringParam("to_time_created", getDateIfNotNull(trb.getSearchBuilder().getEndDate()));
                    request.addQueryStringParam("from_time_last_updated", getDateIfNotNull(trb.getSearchBuilder().getStartLastUpdatedDate()));
                    request.addQueryStringParam("to_time_last_updated", getDateIfNotNull(trb.getSearchBuilder().getEndLastUpdatedDate()));

                    return request;

                case ActionDetail:
                    return request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint(merchantUrl + "/actions/" + trb.getSearchBuilder().getActionId());

                case FindActionsPaged:
                    request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint(merchantUrl + "/actions");

                    request.addQueryStringParam("page", String.valueOf(trb.getPage()));
                    request.addQueryStringParam("page_size", String.valueOf(trb.getPageSize()));
                    request.addQueryStringParam("order_by", getValueIfNotNull(trb.getActionOrderBy()));
                    request.addQueryStringParam("order", getValueIfNotNull(trb.getOrder()));
                    request.addQueryStringParam("id", trb.getSearchBuilder().getActionId());
                    request.addQueryStringParam("type", trb.getSearchBuilder().getActionType());
                    request.addQueryStringParam("resource", trb.getSearchBuilder().getResource());
                    request.addQueryStringParam("resource_status", trb.getSearchBuilder().getResourceStatus());
                    request.addQueryStringParam("resource_id", trb.getSearchBuilder().getResourceId());
                    request.addQueryStringParam("from_time_created", getDateIfNotNull(trb.getSearchBuilder().getStartDate()));
                    request.addQueryStringParam("to_time_created", getDateIfNotNull(trb.getSearchBuilder().getEndDate()));
                    request.addQueryStringParam("merchant_name", trb.getSearchBuilder().getMerchantName());
                    request.addQueryStringParam("account_name", trb.getSearchBuilder().getAccountName());
                    request.addQueryStringParam("app_name", trb.getSearchBuilder().getAppName());
                    request.addQueryStringParam("version", trb.getSearchBuilder().getVersion());
                    request.addQueryStringParam("response_code", trb.getSearchBuilder().getResponseCode());
                    request.addQueryStringParam("http_response_code", trb.getSearchBuilder().getHttpResponseCode());

                    return request;

                case PayLinkDetail:
                    return
                            request
                                    .setVerb(GpApiRequest.HttpMethod.Get)
                                    .setEndpoint(merchantUrl + "/links/" + trb.getSearchBuilder().getPayLinkId());

                case FindPayLinkPaged:
                    request.addQueryStringParam("from_time_created", getDateIfNotNull(trb.getSearchBuilder().getStartDate()));
                    request.addQueryStringParam("to_time_created", getDateIfNotNull(trb.getSearchBuilder().getEndDate()));
                    request.addQueryStringParam("order", trb.getOrder().getValue());
                    request.addQueryStringParam("order_by", EnumUtils.getMapping(Target.GP_API, trb.getActionOrderBy()));
                    request.addQueryStringParam("status", trb.getSearchBuilder().getPayLinkStatus());
                    request.addQueryStringParam("usage_mode", trb.getSearchBuilder().getPaymentMethodUsageMode());
                    request.addQueryStringParam("name", trb.getSearchBuilder().getDisplayName());
                    request.addQueryStringParam("amount", StringUtils.toNumeric(trb.getSearchBuilder().getAmount()));;
                    request.addQueryStringParam("description", trb.getSearchBuilder().getDescription());
                    request.addQueryStringParam("reference", trb.getSearchBuilder().getReferenceNumber());
                    request.addQueryStringParam("country", trb.getSearchBuilder().getCountry());
                    request.addQueryStringParam("currency", trb.getSearchBuilder().getCurrency());
                    request.addQueryStringParam("expiration_date", getDateIfNotNull(trb.getSearchBuilder().getExpirationDate()));

                    return
                            request
                                    .setVerb(GpApiRequest.HttpMethod.Get)
                                    .setEndpoint(merchantUrl + "/links");

                default:
                    throw new UnsupportedTransactionException();
            }
        }

        throw new UnsupportedTransactionException();

    }

}
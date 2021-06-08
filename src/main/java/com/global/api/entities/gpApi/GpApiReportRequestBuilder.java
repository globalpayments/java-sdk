package com.global.api.entities.gpApi;

import com.global.api.builders.ReportBuilder;
import com.global.api.builders.TransactionReportBuilder;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.gateways.GpApiConnector;
import com.global.api.utils.StringUtils;

import static com.global.api.gateways.GpApiConnector.getDateIfNotNull;
import static com.global.api.gateways.GpApiConnector.getValueIfNotNull;

public class GpApiReportRequestBuilder {

    public static GpApiRequest buildRequest(ReportBuilder builder, GpApiConnector gateway) throws GatewayException, UnsupportedTransactionException {
        if (builder instanceof TransactionReportBuilder) {

            GpApiRequest request = new GpApiRequest();
            TransactionReportBuilder<TransactionSummary> trb = (TransactionReportBuilder<TransactionSummary>) builder;

            switch (builder.getReportType()) {

                case TransactionDetail:
                    return request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint("/transactions/" + trb.getTransactionId());

                case FindTransactionsPaged:
                    request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint("/transactions");

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

                    return request;

                case FindSettlementTransactionsPaged:
                    request = new GpApiRequest()
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint("/settlement/transactions");

                    request.addQueryStringParam("page", String.valueOf(trb.getPage()));
                    request.addQueryStringParam("page_size", String.valueOf(trb.getPageSize()));
                    request.addQueryStringParam("order_by", getValueIfNotNull(trb.getTransactionOrderBy()));
                    request.addQueryStringParam("order", getValueIfNotNull(trb.getOrder()));
                    request.addQueryStringParam("number_first6", trb.getSearchBuilder().getCardNumberFirstSix());
                    request.addQueryStringParam("number_last4", trb.getSearchBuilder().getCardNumberLastFour());
                    request.addQueryStringParam("deposit_status", getValueIfNotNull(trb.getSearchBuilder().getDepositStatus()));
                    request.addQueryStringParam("account_name", gateway.getDataAccountName());
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
                            .setEndpoint("/settlement/deposits/" + trb.getSearchBuilder().getDepositReference());

                case FindDepositsPaged:
                    request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint("/settlement/deposits");

                    request.addQueryStringParam("page", String.valueOf(trb.getPage()));
                    request.addQueryStringParam("page_size", String.valueOf(trb.getPageSize()));
                    request.addQueryStringParam("order_by", getValueIfNotNull(trb.getDepositOrderBy()));
                    request.addQueryStringParam("order", getValueIfNotNull(trb.getOrder()));
                    request.addQueryStringParam("account_name", gateway.getDataAccountName());
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
                            .setEndpoint("/disputes/" + trb.getSearchBuilder().getDisputeId());

                case FindDisputesPaged:
                    request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint("/disputes");

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
                            .setEndpoint("/settlement/disputes/" + trb.getSearchBuilder().getSettlementDisputeId());

                case FindSettlementDisputesPaged:
                    request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint("/settlement/disputes");

                    request.addQueryStringParam("account_name", gateway.getDataAccountName());
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
                    request.addQueryStringParam("system.mid", trb.getSearchBuilder().getMerchantId());
                    request.addQueryStringParam("system.hierarchy", trb.getSearchBuilder().getSystemHierarchy());

                    return request;

                case StoredPaymentMethodDetail:
                    return
                            request
                                    .setVerb(GpApiRequest.HttpMethod.Get)
                                    .setEndpoint("/payment-methods/" + trb.getSearchBuilder().getStoredPaymentMethodId());

                case FindStoredPaymentMethodsPaged:
                    request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint("/payment-methods");

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
                    request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint("/actions/" + trb.getSearchBuilder().getActionId());
                    return request;

                case FindActionsPaged:
                    request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint("/actions");

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

                default:
                    throw new UnsupportedTransactionException();
            }
        }

        throw new UnsupportedTransactionException();

    }

}
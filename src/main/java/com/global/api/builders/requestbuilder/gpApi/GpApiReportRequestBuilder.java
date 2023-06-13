package com.global.api.builders.requestbuilder.gpApi;

import com.global.api.builders.ReportBuilder;
import com.global.api.builders.TransactionReportBuilder;
import com.global.api.builders.UserReportBuilder;
import com.global.api.entities.IRequestBuilder;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.enums.Target;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.entities.gpApi.GpApiRequest;
import com.global.api.gateways.GpApiConnector;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.utils.EnumUtils;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;
import lombok.var;

import static com.global.api.gateways.GpApiConnector.getDateIfNotNull;
import static com.global.api.gateways.GpApiConnector.getValueIfNotNull;

public class GpApiReportRequestBuilder implements IRequestBuilder<ReportBuilder> {

    @Override
    public GpApiRequest buildRequest(ReportBuilder builder, GpApiConnector gateway) throws GatewayException, UnsupportedTransactionException {
        String merchantUrl = gateway.getMerchantUrl();
        var request = new GpApiRequest();

        if (builder instanceof TransactionReportBuilder) {

            TransactionReportBuilder<TransactionSummary> trb = (TransactionReportBuilder<TransactionSummary>) builder;

            switch (builder.getReportType()) {

                case TransactionDetail:
                    return (GpApiRequest) request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint(merchantUrl + GpApiRequest.TRANSACTION_ENDPOINT + "/" + trb.getTransactionId());

                case FindTransactionsPaged:
                    request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint(merchantUrl + GpApiRequest.TRANSACTION_ENDPOINT);

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

                    if (trb.getSearchBuilder().getRiskAssessmentMode() != null) {
                        request.addQueryStringParam("risk_assessment_mode", trb.getSearchBuilder().getRiskAssessmentMode().getValue());
                    }
                    if (trb.getSearchBuilder().getRiskAssessmentResult() != null) {
                        request.addQueryStringParam("risk_assessment_result", trb.getSearchBuilder().getRiskAssessmentResult().getValue());
                    }
                    if (trb.getSearchBuilder().getRiskAssessmentReasonCode() != null) {
                        request.addQueryStringParam("risk_assessment_reason_code", EnumUtils.getMapping(Target.GP_API, trb.getSearchBuilder().getRiskAssessmentReasonCode()));
                    }

                    if (trb.getSearchBuilder().getPaymentProvider() != null) {
                        request.addQueryStringParam("provider", trb.getSearchBuilder().getPaymentProvider().toString());
                    }

                    return request;

                case FindSettlementTransactionsPaged:
                    request = (GpApiRequest) new GpApiRequest()
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint(merchantUrl + GpApiRequest.SETTLEMENT_TRANSACTIONS_ENDPOINT);

                    request.addQueryStringParam("page", String.valueOf(trb.getPage()));
                    request.addQueryStringParam("page_size", String.valueOf(trb.getPageSize()));
                    request.addQueryStringParam("order_by", getValueIfNotNull(trb.getTransactionOrderBy()));
                    request.addQueryStringParam("order", getValueIfNotNull(trb.getOrder()));
                    request.addQueryStringParam("number_first6", trb.getSearchBuilder().getCardNumberFirstSix());
                    request.addQueryStringParam("number_last4", trb.getSearchBuilder().getCardNumberLastFour());
                    request.addQueryStringParam("deposit_status", getValueIfNotNull(trb.getSearchBuilder().getDepositStatus()));
                    request.addQueryStringParam("account_name", gateway.getGpApiConfig().getAccessTokenInfo().getDataAccountName());
                    request.addQueryStringParam("account_id", gateway.getGpApiConfig().getAccessTokenInfo().getDataAccountID());
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
                    return (GpApiRequest) request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint(merchantUrl + GpApiRequest.DEPOSITS_ENDPOINT + "/" + trb.getSearchBuilder().getDepositReference());

                case FindDepositsPaged:
                    request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint(merchantUrl + GpApiRequest.DEPOSITS_ENDPOINT);

                    request.addQueryStringParam("page", String.valueOf(trb.getPage()));
                    request.addQueryStringParam("page_size", String.valueOf(trb.getPageSize()));
                    request.addQueryStringParam("order_by", getValueIfNotNull(trb.getDepositOrderBy()));
                    request.addQueryStringParam("order", getValueIfNotNull(trb.getOrder()));
                    request.addQueryStringParam("account_name", gateway.getGpApiConfig().getAccessTokenInfo().getDataAccountName());
                    request.addQueryStringParam("account_id", gateway.getGpApiConfig().getAccessTokenInfo().getDataAccountID());
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
                    return (GpApiRequest) request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint(merchantUrl + GpApiRequest.DISPUTES_ENDPOINT + "/" + trb.getSearchBuilder().getDisputeId());

                case DocumentDisputeDetail:
                    return (GpApiRequest) request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint(merchantUrl + GpApiRequest.DISPUTES_ENDPOINT + "/" + trb.getSearchBuilder().getDisputeId() +
                                    "/documents/" + trb.getSearchBuilder().getDisputeDocumentId());

                case FindDisputesPaged:
                    request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint(merchantUrl + GpApiRequest.DISPUTES_ENDPOINT);

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
                    return (GpApiRequest) request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint(merchantUrl + GpApiRequest.SETTLEMENT_DISPUTES_ENDPOINT + "/" + trb.getSearchBuilder().getSettlementDisputeId());

                case FindSettlementDisputesPaged:
                    request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint(merchantUrl + GpApiRequest.SETTLEMENT_DISPUTES_ENDPOINT);

                    request.addQueryStringParam("account_name", gateway.getGpApiConfig().getAccessTokenInfo().getDataAccountName());
                    request.addQueryStringParam("account_id", gateway.getGpApiConfig().getAccessTokenInfo().getDataAccountID());
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
                    return (GpApiRequest)
                            request
                                    .setVerb(GpApiRequest.HttpMethod.Get)
                                    .setEndpoint(merchantUrl + GpApiRequest.PAYMENT_METHODS_ENDPOINT + "/" + trb.getSearchBuilder().getStoredPaymentMethodId());

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
                                        .set("account_id", gateway.getGpApiConfig().getAccessTokenInfo().getTokenizationAccountID())
                                        .set("reference", trb.getSearchBuilder().getReferenceNumber())
                                        .set("card", card);

                        request
                                .setVerb(GpApiRequest.HttpMethod.Post)
                                .setEndpoint(merchantUrl + GpApiRequest.PAYMENT_METHODS_ENDPOINT + "/" + "search")
                                .setRequestBody(data.toString());

                        return request;
                    }


                    request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint(merchantUrl + GpApiRequest.PAYMENT_METHODS_ENDPOINT);

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
                    return (GpApiRequest) request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint(merchantUrl + GpApiRequest.ACTIONS_ENDPOINT + "/" + trb.getSearchBuilder().getActionId());

                case FindActionsPaged:
                    request
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint(merchantUrl + GpApiRequest.ACTIONS_ENDPOINT);

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
                    return (GpApiRequest)
                            request
                                    .setVerb(GpApiRequest.HttpMethod.Get)
                                    .setEndpoint(merchantUrl + GpApiRequest.PAYLINK_ENDPOINT + "/" + trb.getSearchBuilder().getPayLinkId());

                case FindPayLinkPaged:
                    request.addQueryStringParam("from_time_created", getDateIfNotNull(trb.getSearchBuilder().getStartDate()));
                    request.addQueryStringParam("to_time_created", getDateIfNotNull(trb.getSearchBuilder().getEndDate()));
                    request.addQueryStringParam("order", trb.getOrder().getValue());
                    request.addQueryStringParam("order_by", EnumUtils.getMapping(Target.GP_API, trb.getActionOrderBy()));
                    request.addQueryStringParam("status", trb.getSearchBuilder().getPayLinkStatus());
                    request.addQueryStringParam("usage_mode", trb.getSearchBuilder().getPaymentMethodUsageMode());
                    request.addQueryStringParam("name", trb.getSearchBuilder().getDisplayName());
                    request.addQueryStringParam("amount", StringUtils.toNumeric(trb.getSearchBuilder().getAmount()));
                    request.addQueryStringParam("description", trb.getSearchBuilder().getDescription());
                    request.addQueryStringParam("reference", trb.getSearchBuilder().getReferenceNumber());
                    request.addQueryStringParam("country", trb.getSearchBuilder().getCountry());
                    request.addQueryStringParam("currency", trb.getSearchBuilder().getCurrency());
                    request.addQueryStringParam("expiration_date", getDateIfNotNull(trb.getSearchBuilder().getExpirationDate()));

                    return (GpApiRequest)
                            request
                                    .setVerb(GpApiRequest.HttpMethod.Get)
                                    .setEndpoint(merchantUrl + GpApiRequest.PAYLINK_ENDPOINT);

                default:
                    throw new UnsupportedTransactionException();
            }
        }
        else if (builder instanceof UserReportBuilder) {

            UserReportBuilder<TransactionSummary> userTrb = (UserReportBuilder<TransactionSummary>) builder;

            switch (builder.getReportType()) {
                case FindMerchantsPaged:
                    request = (GpApiRequest)
                            new GpApiRequest()
                                    .setVerb(GpApiRequest.HttpMethod.Get)
                                    .setEndpoint(merchantUrl + GpApiRequest.MERCHANT_MANAGEMENT_ENDPOINT);

                    basicsParams(request, userTrb);
                    request.addQueryStringParam("order", userTrb.getOrder() != null ? userTrb.getOrder().getValue() : null);
                    request.addQueryStringParam("order_by", EnumUtils.getMapping(Target.GP_API, userTrb.getAccountOrderBy()));
                    request.addQueryStringParam("status", userTrb.getSearchBuilder().getMerchantStatus() != null ? userTrb.getSearchBuilder().getMerchantStatus().toString() : null);

                    return request;

                case FindAccountsPaged:
                    String endpoint = merchantUrl;
                    if (userTrb.getSearchBuilder() != null && !StringUtils.isNullOrEmpty(userTrb.getSearchBuilder().getMerchantId())) {
                        endpoint = GpApiRequest.MERCHANT_MANAGEMENT_ENDPOINT + "/" + userTrb.getSearchBuilder().getMerchantId();
                    }

                    request = (GpApiRequest)
                            new GpApiRequest()
                                    .setVerb(GpApiRequest.HttpMethod.Get)
                                    .setEndpoint(endpoint + GpApiRequest.ACCOUNTS_ENDPOINT);

                    basicsParams(request, userTrb);

                    request.addQueryStringParam("order", userTrb.getOrder() != null ? userTrb.getOrder().getValue() : null);
                    request.addQueryStringParam("order_by", userTrb.getAccountOrderBy() != null ? userTrb.getAccountOrderBy().toString() : null);
                    request.addQueryStringParam("from_time_created", getDateIfNotNull(userTrb.getSearchBuilder().getStartDate()));
                    request.addQueryStringParam("to_time_created", getDateIfNotNull(userTrb.getSearchBuilder().getEndDate()));
                    request.addQueryStringParam("status", userTrb.getSearchBuilder().getAccountStatus() != null ? userTrb.getSearchBuilder().getAccountStatus().toString() : null);
                    request.addQueryStringParam("name", userTrb.getSearchBuilder().getAccountName());
                    request.addQueryStringParam("id", userTrb.getSearchBuilder().getResourceId());

                    return request;

                case FindAccountDetail:
                    request = (GpApiRequest)
                            new GpApiRequest()
                                    .setVerb(GpApiRequest.HttpMethod.Get)
                                    .setEndpoint(merchantUrl + GpApiRequest.ACCOUNTS_ENDPOINT + "/" + builder.getSearchBuilder().getAccountId());

                    if (builder.getSearchBuilder().getAddress() != null) {
                        request = (GpApiRequest)
                                new GpApiRequest()
                                        .setVerb(GpApiRequest.HttpMethod.Get)
                                        .setEndpoint(merchantUrl + GpApiRequest.ACCOUNTS_ENDPOINT + "/" + builder.getSearchBuilder().getAccountId() + "/addresses");

                        request.addQueryStringParam("postal_code", userTrb.getSearchBuilder().getAddress().getPostalCode());
                        request.addQueryStringParam("line_1", userTrb.getSearchBuilder().getAddress().getStreetAddress1());
                        request.addQueryStringParam("line_2", userTrb.getSearchBuilder().getAddress().getStreetAddress2());
                    }

                    return request;

                default:
                    throw new UnsupportedTransactionException();
            }
        }

        throw new UnsupportedTransactionException();
    }

    @Override
    public boolean canProcess(Object builder) {
        return builder instanceof ReportBuilder;
    }

    private static void basicsParams(GpApiRequest request, UserReportBuilder userTrb) {
        request.addQueryStringParam("page", Integer.toString(userTrb.getPage()));
        request.addQueryStringParam("page_size", Integer.toString(userTrb.getPageSize()));
    }
}
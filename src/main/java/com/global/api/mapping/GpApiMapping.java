package com.global.api.mapping;

import com.global.api.entities.*;
import com.global.api.entities.enums.ReportType;
import com.global.api.entities.enums.Secure3dVersion;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.PagedResult;
import com.global.api.entities.reporting.*;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

import static com.global.api.gateways.GpApiConnector.parseGpApiDate;
import static com.global.api.gateways.GpApiConnector.parseGpApiDateTime;

public class GpApiMapping {

    private static final String BATCH_CLOSE = "CLOSE";
    private static final String PAYMENT_METHOD_CREATE = "PAYMENT_METHOD_CREATE";
    private static final String PAYMENT_METHOD_DETOKENIZE = "PAYMENT_METHOD_DETOKENIZE";
    private static final String PAYMENT_METHOD_EDIT = "PAYMENT_METHOD_EDIT";
    private static final String PAYMENT_METHOD_DELETE = "PAYMENT_METHOD_DELETE";

    public static Transaction mapResponse(String rawResponse) {
        Transaction transaction = new Transaction();

        if (!StringUtils.isNullOrEmpty(rawResponse)) {
            JsonDoc json = JsonDoc.parse(rawResponse);

            transaction.setResponseCode(json.get("action").getString("result_code"));

            String actionType = json.get("action").getString("type");

            switch (actionType) {
                case BATCH_CLOSE:
                    BatchSummary batchSummary = new BatchSummary();

                    batchSummary.setBatchReference(json.getString("id"));
                    batchSummary.setStatus(json.getString("status"));
                    batchSummary.setTotalAmount(json.getAmount("amount"));
                    batchSummary.setTransactionCount(json.getInt("transaction_count"));

                    transaction.setBatchSummary(batchSummary);

                    return transaction;

                case PAYMENT_METHOD_CREATE:
                case PAYMENT_METHOD_DETOKENIZE:
                case PAYMENT_METHOD_EDIT:
                case PAYMENT_METHOD_DELETE:
                    transaction.setToken(json.getString("id"));
                    transaction.setTimestamp(json.getString("time_created"));
                    transaction.setReferenceNumber(json.getString("reference"));

                    if (json.has("card")) {
                        JsonDoc card = json.get("card");

                        transaction.setCardType(card.getString("brand"));
                        transaction.setCardNumber(card.getString("number"));
                        transaction.setCardLast4(card.getString("masked_number_last4"));
                        transaction.setCardExpMonth(card.getInt("expiry_month"));
                        transaction.setCardExpYear(card.getInt("expiry_year"));
                    }

                    return transaction;

                default:
                    break;

            }

            transaction.setTransactionId(json.getString("id"));
            transaction.setBalanceAmount(json.getAmount("amount"));
            transaction.setTimestamp(json.getString("time_created"));
            transaction.setResponseMessage(json.getString("status"));
            transaction.setReferenceNumber(json.getString("reference"));
            transaction.setClientTransactionId(json.getString("reference"));

            BatchSummary batchSummary = new BatchSummary();
            batchSummary.setBatchReference(json.getString("batch_id"));
            transaction.setBatchSummary(batchSummary);

            if (json.has("payment_method")) {
                JsonDoc paymentMethod = json.get("payment_method");

                transaction.setToken(paymentMethod.getString("id"));
                transaction.setAuthorizationCode(paymentMethod.getString("result"));

                if (paymentMethod.has("card")) {
                    JsonDoc card = paymentMethod.get("card");

                    transaction.setCardType(card.getString("brand"));
                    transaction.setCardLast4(card.getString("masked_number_last4"));
                    transaction.setCvnResponseMessage(card.getString("cvv_result"));
                    transaction.setCardBrandTransactionId(card.getString("brand_reference"));
                    transaction.setAvsResponseCode(card.getString("avs_postal_code_result"));
                    transaction.setAvsAddressResponse(card.getString("avs_address_result"));
                    transaction.setAvsResponseMessage(card.getString("avs_action"));
                }

            }

        }

        return transaction;
    }

    public static TransactionSummary mapTransactionSummary(JsonDoc doc) throws GatewayException {
        TransactionSummary summary = new TransactionSummary();

        summary.setTransactionId(doc.getString("id"));
        summary.setDepositReference(doc.getString("deposit_id"));
        summary.setTransactionDate(parseGpApiDateTime(doc.getString("time_created")));
        summary.setDepositDate(parseGpApiDate(doc.getString("deposit_time_created")));
        summary.setTransactionStatus(doc.getString("status"));
        summary.setDepositStatus(doc.getString("deposit_status"));
        summary.setTransactionType(doc.getString("type"));
        summary.setChannel(doc.getString("channel"));
        summary.setAmount(doc.getAmount("amount"));
        summary.setCurrency(doc.getString("currency"));
        summary.setReferenceNumber(doc.getString("reference"));
        summary.setClientTransactionId(doc.getString("reference"));
        summary.setTransactionLocalDate(parseGpApiDateTime(doc.getString("time_created_reference")));
        summary.setBatchSequenceNumber(doc.getString("batch_id"));
        summary.setCountry(doc.getString("country"));
        summary.setOriginalTransactionId(doc.getString("parent_resource_id"));
        summary.setDepositReference(doc.getString("deposit_id"));
        summary.setDepositDate(parseGpApiDate(doc.getString("deposit_time_created")));

        if (doc.has("payment_method")) {
            final JsonDoc paymentMethod = doc.get("payment_method");

            summary.setGatewayResponseMessage(paymentMethod.getString("message"));
            summary.setEntryMode(paymentMethod.getString("entry_mode"));
            summary.setCardHolderName(paymentMethod.getString("name"));

            if (paymentMethod.has("card")) {
                final JsonDoc card = paymentMethod.get("card");

                summary.setCardType(card.getString("brand"));
                summary.setAuthCode(card.getString("authcode"));
                summary.setBrandReference(card.getString("brand_reference"));
                summary.setAcquirerReferenceNumber(card.getString("arn"));
                summary.setMaskedCardNumber(card.getString("masked_number_first6last4"));
            } else if (paymentMethod.has("digital_wallet")) {
                JsonDoc digitalWallet = paymentMethod.get("digital_wallet");

                summary.setCardType(digitalWallet.getString("brand"));
                summary.setAuthCode(digitalWallet.getString("authcode"));
                summary.setBrandReference(digitalWallet.getString("brand_reference"));
                summary.setMaskedCardNumber(digitalWallet.getString("masked_token_first6last4"));
            }
        }

        if (doc.has("system")) {
            JsonDoc system = doc.get("system");

            summary.setMerchantId(system.getString("mid"));
            summary.setMerchantHierarchy(system.getString("hierarchy"));
            summary.setMerchantName(system.getString("name"));
            summary.setMerchantDbaName(system.getString("dba"));
        }

        return summary;
    }

    public static <T> T mapReportResponse(String rawResponse, ReportType reportType) throws GatewayException {
        JsonDoc json = JsonDoc.parse(rawResponse);

        switch (reportType) {
            case TransactionDetail:
                return (T) mapTransactionSummary(json);

            case FindTransactionsPaged:
            case FindSettlementTransactionsPaged:
                return (T) mapTransactions(json);

            case DepositDetail:
                return (T) mapDepositSummary(json);

            case FindDepositsPaged:
                return (T) mapDeposits(json);

            case DisputeDetail:
                return (T) mapDisputeSummary(json);

            case SettlementDisputeDetail:
                return (T) mapSettlementDisputeSummary(json);

            case FindDisputesPaged:
                return (T) mapDisputes(json);

            case FindSettlementDisputesPaged:
                return (T) mapSettlementDisputes(json);

            case StoredPaymentMethodDetail:
                return (T) mapStoredPaymentMethodSummary(json);

            case FindStoredPaymentMethodsPaged:
                return (T) mapStoredPaymentMethods(json);

            case ActionDetail:
                return (T) mapActionSummary(json);

            case FindActionsPaged:
                return (T) mapActions(json);
            default:
                throw new NotImplementedException();
        }
    }

    private static <T> void setPagingInfo(PagedResult<T> result, JsonDoc json) {
        if (json.getInt("total_record_count") != null) {
            result.setTotalRecordCount(json.getInt("total_record_count"));
        } else if (json.getInt("total_count") != null) {
            result.setTotalRecordCount(json.getInt("total_count"));
        } else {
            result.setTotalRecordCount(json.getInt("current_page_size"));
        }

        JsonDoc paging = json.get("paging");
        if (paging != null) {
            result.setPageSize(paging.getInt("page_size") != null ? paging.getInt("page_size") : 0);
            result.setPage(paging.getInt("page") != null ? paging.getInt("page") : 0);
            result.setOrder(paging.getString("order"));
            result.setOrderBy(paging.getString("order_by"));
        }
    }

    public static DepositSummary mapDepositSummary(JsonDoc doc) throws GatewayException {
        DepositSummary summary = new DepositSummary();

        summary.setDepositId(doc.getString("id"));
        summary.setDepositDate(parseGpApiDate(doc.getString("time_created")));
        summary.setStatus(doc.getString("status"));
        summary.setType(doc.getString("funding_type"));
        summary.setAmount(doc.getAmount("amount"));
        summary.setCurrency(doc.getString("currency"));

        if (doc.has("system")) {
            JsonDoc system = doc.get("system");
            summary.setMerchantNumber(system.getString("mid"));
            summary.setMerchantHierarchy(system.getString("hierarchy"));
            summary.setMerchantName(system.getString("name"));
            summary.setMerchantDbaName(system.getString("dba"));
        }

        if (doc.has("sales")) {
            JsonDoc sales = doc.get("sales");
            summary.setSalesTotalCount(sales.getInt("count"));
            summary.setSalesTotalAmount(sales.getAmount("amount"));
        }

        if (doc.has("refunds")) {
            JsonDoc refunds = doc.get("refunds");
            summary.setRefundsTotalCount(refunds.getInt("count"));
            summary.setRefundsTotalAmount(refunds.getAmount("amount"));
        }

        if (doc.has("disputes")) {
            JsonDoc disputes = doc.get("disputes");

            if (disputes.has("chargebacks")) {
                JsonDoc chargebacks = disputes.get("chargebacks");

                summary.setChargebackTotalCount(chargebacks.getInt("count"));
                summary.setChargebackTotalAmount(chargebacks.getAmount("amount"));
            }

            if (disputes.has("reversals")) {
                JsonDoc reversals = disputes.get("reversals");

                summary.setAdjustmentTotalCount(reversals.getInt("count"));
                summary.setAdjustmentTotalAmount(reversals.getAmount("amount"));
            }
        }

        if (doc.has("fees")) {
            JsonDoc fees = doc.get("fees");

            summary.setFeesTotalAmount(fees.getAmount("amount"));
        }

        if (doc.has("bank_transfer")) {
            JsonDoc bankTransfer = doc.get("bank_transfer");

            summary.setAccountNumber(bankTransfer.getString("masked_account_number_last4"));
        }

        return summary;
    }

    public static DisputeSummary mapDisputeSummary(JsonDoc doc) throws GatewayException {
        DisputeSummary summary = new DisputeSummary();

        summary.setCaseId(doc.getString("id"));
        summary.setCaseIdTime(parseGpApiDate(doc.getString("time_created")));
        summary.setCaseStatus(doc.getString("status"));
        summary.setCaseStage(doc.getString("stage"));
        summary.setCaseAmount(doc.getAmount("amount"));
        summary.setCaseCurrency(doc.getString("currency"));

        summary.setReasonCode(doc.getString("reason_code"));
        summary.setReason(doc.getString("reason_description"));
        summary.setResult(doc.getString("result"));

        if (doc.has("system")) {
            JsonDoc system = doc.get("system");

            summary.setCaseMerchantId(system.getString("mid"));
            summary.setCaseTerminalId(system.getString("tid"));
            summary.setMerchantHierarchy(system.getString("hierarchy"));
            summary.setMerchantName(system.getString("name"));
            summary.setMerchantDbaName(system.getString("dba"));
        }

        summary.setLastAdjustmentAmount(doc.getAmount("last_adjustment_amount"));
        summary.setLastAdjustmentCurrency(doc.getString("last_adjustment_currency"));
        summary.setLastAdjustmentFunding(doc.getString("last_adjustment_funding"));

        if (doc.has("payment_method")) {
            JsonDoc paymentMethod = doc.get("payment_method");

            if (paymentMethod.has("card")) {
                JsonDoc card = paymentMethod.get("card");

                summary.setTransactionMaskedCardNumber(card.getString("number"));
                summary.setTransactionARN(card.getString("arn"));
                summary.setTransactionCardType(card.getString("brand"));
            }
        }

        String timeToRespondBy = doc.getString("time_to_respond_by");
        if (!StringUtils.isNullOrEmpty(timeToRespondBy)) {
            summary.setRespondByDate(parseGpApiDate(timeToRespondBy));
        }

        return summary;
    }

    public static DisputeSummary mapSettlementDisputeSummary(JsonDoc doc) throws GatewayException {
        DisputeSummary summary = mapDisputeSummary(doc);

        summary.setCaseIdTime(parseGpApiDate(doc.getString("stage_time_created")));
        summary.setDepositDate(doc.getDate("deposit_time_created", "yyyy-MM-dd"));
        summary.setDepositReference(doc.getString("deposit_id"));

        if (doc.has("transaction")) {
            JsonDoc transaction = doc.get("transaction");

            summary.setTransactionTime(parseGpApiDate(transaction.getString("time_created")));
            summary.setTransactionType(transaction.getString("type"));
            summary.setTransactionAmount(transaction.getAmount("amount"));
            summary.setTransactionCurrency(transaction.getString("currency"));
            summary.setTransactionReferenceNumber(transaction.getString("reference"));

            if (transaction.has("payment_method")) {
                JsonDoc paymentMethod = transaction.get("payment_method");

                if (paymentMethod.has("card")) {
                    JsonDoc card = paymentMethod.get("card");

                    summary.setTransactionMaskedCardNumber(card.getString("masked_number_first6last4"));
                    summary.setTransactionARN(card.getString("arn"));
                    summary.setTransactionCardType(card.getString("brand"));
                    summary.setTransactionAuthCode(card.getString("authcode"));
                }
            }
        }

        return summary;
    }

    private static Secure3dVersion parse3DSVersion(String messageVersion) {
        if (messageVersion.startsWith("1."))
            return Secure3dVersion.ONE;
        if (messageVersion.startsWith("2."))
            return Secure3dVersion.TWO;
        return Secure3dVersion.ANY;
    }

    public static Transaction map3DSecureData(String rawResponse) throws ApiException {
        if (!StringUtils.isNullOrEmpty(rawResponse)) {
            JsonDoc json = JsonDoc.parse(rawResponse);

            ThreeDSecure threeDSecure = new ThreeDSecure();
            threeDSecure.setServerTransactionId(
                    !StringUtils.isNullOrEmpty(json.getString("id")) ?
                            json.getString("id") :
                            !StringUtils.isNullOrEmpty(json.get("three_ds").getString("server_trans_ref")) ?
                                    json.get("three_ds").getString("server_trans_ref") :
                                    null);
            threeDSecure.setStatus(json.getString("status"));
            threeDSecure.setCurrency(json.getString("currency"));
            threeDSecure.setAmount(json.getAmount("amount"));

            if (json.has("three_ds")) {
                JsonDoc three_ds = json.get("three_ds");

                threeDSecure.setMessageVersion(three_ds.getString("message_version"));
                threeDSecure.setVersion(parse3DSVersion(three_ds.getString("message_version")));
                threeDSecure.setDirectoryServerStartVersion(three_ds.getString("ds_protocol_version_start"));
                threeDSecure.setDirectoryServerEndVersion(three_ds.getString("ds_protocol_version_end"));
                threeDSecure.setAcsStartVersion(three_ds.getString("acs_protocol_version_start"));
                threeDSecure.setAcsEndVersion(three_ds.getString("acs_protocol_version_end"));
                // In other SDKs, enrolled is simply a String.
                // In JAVA, enrolled was used in another connectors as boolean. So enrolledStatus was created as String for that purpose.
                threeDSecure.setEnrolledStatus(three_ds.getString("enrolled_status"));
                threeDSecure.setEci(!StringUtils.isNullOrEmpty(three_ds.getString("eci")) ? three_ds.getString("eci") : null);
                threeDSecure.setAcsInfoIndicator(three_ds.getStringArrayList("acs_info_indicator"));
                threeDSecure.setChallengeMandated(three_ds.getString("challenge_status").equals("MANDATED"));
                threeDSecure.setPayerAuthenticationRequest(
                        !StringUtils.isNullOrEmpty(three_ds.getString("acs_challenge_request_url")) && json.getString("status").equals("CHALLENGE_REQUIRED") ?
                                three_ds.getString("challenge_value") :
                                three_ds.get("method_data") != null ?
                                        (!StringUtils.isNullOrEmpty(three_ds.get("method_data").getString("encoded_method_data")) ? three_ds.get("method_data").getString("encoded_method_data") : null) :
                                        null
                );
                threeDSecure.setIssuerAcsUrl(
                        !StringUtils.isNullOrEmpty(three_ds.getString("acs_challenge_request_url")) && json.getString("status").equals("CHALLENGE_REQUIRED") ?
                                three_ds.getString("acs_challenge_request_url") :
                                three_ds.getString("method_url")
                );

                threeDSecure.setCurrency(json.getString("currency"));
                threeDSecure.setAmount(json.getAmount("amount"));
                threeDSecure.setAuthenticationValue(three_ds.getString("authentication_value"));
                threeDSecure.setDirectoryServerTransactionId(three_ds.getString("ds_trans_ref"));
                threeDSecure.setAcsTransactionId(three_ds.getString("acs_trans_ref"));
                threeDSecure.setStatusReason(three_ds.getString("status_reason"));
                threeDSecure.setMessageCategory(three_ds.getString("message_category"));
                threeDSecure.setMessageType(three_ds.getString("message_type"));
                threeDSecure.setSessionDataFieldName(three_ds.getString("session_data_field_name"));
                if (json.has("notifications")) {
                    threeDSecure.setChallengeReturnUrl(json.get("notifications").getString("challenge_return_url"));
                }
                threeDSecure.setLiabilityShift(three_ds.getString("liability_shift"));
                threeDSecure.setAuthenticationSource(three_ds.getString("authentication_source"));
                threeDSecure.setAuthenticationType(three_ds.getString("authentication_request_type"));
                threeDSecure.setWhitelistStatus(three_ds.getString("whitelist_status"));
                threeDSecure.setMessageExtensions(new ArrayList<MessageExtension>());

                List<JsonDoc> messageExtensions = three_ds.getEnumerator("message_extension");
                List<MessageExtension> msgExtensions = new ArrayList<>();

                if (messageExtensions != null) {
                    for (JsonDoc messageExtension : messageExtensions) {
                        MessageExtension msgExtension =
                                new MessageExtension()
                                        .setCriticalityIndicator(messageExtension.getString("criticality_indicator"))
                                        .setMessageExtensionData(messageExtension.get("data").toString())
                                        .setMessageExtensionId(messageExtension.getString("id"))
                                        .setMessageExtensionName(messageExtension.getString("name"));

                        msgExtensions.add(msgExtension);
                    }
                }
                threeDSecure.setMessageExtensions(msgExtensions);
            }

            Transaction transaction = new Transaction();
            transaction.setThreeDsecure(threeDSecure);

            return transaction;
        }

        return new Transaction();
    }

    public static StoredPaymentMethodSummary mapStoredPaymentMethodSummary(JsonDoc doc) throws GatewayException {
        StoredPaymentMethodSummary storedPaymentMethodSummary = new StoredPaymentMethodSummary();

        storedPaymentMethodSummary.setId(doc.getString("id"));
        storedPaymentMethodSummary.setTimeCreated(parseGpApiDate(doc.getString("time_created")));
        storedPaymentMethodSummary.setStatus(doc.getString("status"));
        storedPaymentMethodSummary.setReference(doc.getString("reference"));
        storedPaymentMethodSummary.setName(doc.getString("name"));

        if (doc.has("card")) {
            JsonDoc card = doc.get("card");

            storedPaymentMethodSummary.setCardLast4(card.getString("number_last4"));
            storedPaymentMethodSummary.setCardType(card.getString("brand"));
            storedPaymentMethodSummary.setCardExpMonth(card.getString("expiry_month"));
            storedPaymentMethodSummary.setCardExpYear(card.getString("expiry_year"));
        }

        return storedPaymentMethodSummary;
    }

    public static ActionSummary mapActionSummary(JsonDoc doc) throws GatewayException {
        ActionSummary actionSummary = new ActionSummary();

        actionSummary.setId(doc.getString("id"));
        actionSummary.setType(doc.getString("type"));
        actionSummary.setTimeCreated(parseGpApiDateTime(doc.getString("time_created")));
        actionSummary.setResource(doc.getString("resource"));
        actionSummary.setVersion(doc.getString("version"));
        actionSummary.setResourceId(doc.getString("resource_id"));
        actionSummary.setResourceStatus(doc.getString("resource_status"));
        actionSummary.setHttpResponseCode(doc.getString("http_response_code"));
        actionSummary.setResponseCode(doc.getString("response_code"));
        actionSummary.setAppId(doc.getString("app_id"));
        actionSummary.setAppName(doc.getString("app_name"));
        actionSummary.setAccountId(doc.getString("account_id"));
        actionSummary.setAccountName(doc.getString("account_name"));
        actionSummary.setMerchantName(doc.getString("merchant_name"));

        return actionSummary;
    }

    public static TransactionSummaryPaged mapTransactions(JsonDoc doc) throws GatewayException {
        TransactionSummaryPaged pagedResult = new TransactionSummaryPaged();
        setPagingInfo(pagedResult, doc);

        for (JsonDoc transaction : doc.getEnumerator("transactions")) {
            pagedResult.add(mapTransactionSummary(transaction));
        }

        return pagedResult;
    }

    public static DepositSummaryPaged mapDeposits(JsonDoc doc) throws GatewayException {
        DepositSummaryPaged pagedResult = new DepositSummaryPaged();
        setPagingInfo(pagedResult, doc);

        for (JsonDoc deposit : doc.getEnumerator("deposits")) {
            pagedResult.add(mapDepositSummary(deposit));
        }

        return pagedResult;
    }

    public static DisputeSummaryPaged mapDisputes(JsonDoc doc) throws GatewayException {
        DisputeSummaryPaged pagedResult = new DisputeSummaryPaged();
        setPagingInfo(pagedResult, doc);

        for (JsonDoc transaction : doc.getEnumerator("disputes")) {
            pagedResult.add(mapDisputeSummary(transaction));
        }

        return pagedResult;
    }

    public static DisputeSummaryPaged mapSettlementDisputes(JsonDoc doc) throws GatewayException {
        DisputeSummaryPaged pagedResult = new DisputeSummaryPaged();
        setPagingInfo(pagedResult, doc);

        for (JsonDoc transaction : doc.getEnumerator("disputes")) {
            pagedResult.add(mapSettlementDisputeSummary(transaction));
        }

        return pagedResult;
    }

    public static StoredPaymentMethodSummaryPaged mapStoredPaymentMethods(JsonDoc doc) throws GatewayException {
        StoredPaymentMethodSummaryPaged pagedResult = new StoredPaymentMethodSummaryPaged();
        setPagingInfo(pagedResult, doc);

        for (JsonDoc paymentMethod : doc.getEnumerator("payment_methods")) {
            pagedResult.add(mapStoredPaymentMethodSummary(paymentMethod));
        }

        return pagedResult;
    }

    public static ActionSummaryPaged mapActions(JsonDoc doc) throws GatewayException {
        ActionSummaryPaged pagedResult = new ActionSummaryPaged();
        setPagingInfo(pagedResult, doc);

        for (JsonDoc element : doc.getEnumerator("actions")) {
            pagedResult.add(mapActionSummary(element));
        }

        return pagedResult;
    }

}
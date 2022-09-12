package com.global.api.mapping;

import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.entities.gpApi.PagedResult;
import com.global.api.entities.reporting.*;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import static com.global.api.gateways.GpApiConnector.*;

public class GpApiMapping {

    private static final String BATCH_CLOSE = "CLOSE";
    private static final String PAYMENT_METHOD_CREATE = "PAYMENT_METHOD_CREATE";
    private static final String PAYMENT_METHOD_DETOKENIZE = "PAYMENT_METHOD_DETOKENIZE";
    private static final String PAYMENT_METHOD_EDIT = "PAYMENT_METHOD_EDIT";
    private static final String PAYMENT_METHOD_DELETE = "PAYMENT_METHOD_DELETE";
    private static final String LINK_CREATE = "LINK_CREATE";
    private static final String LINK_EDIT = "LINK_EDIT";

    public static Transaction mapResponse(String rawResponse) throws GatewayException {
        Transaction transaction = new Transaction();

        if (!StringUtils.isNullOrEmpty(rawResponse)) {
            JsonDoc json = JsonDoc.parse(rawResponse);

            transaction.setResponseCode(json.get("action").getString("result_code"));
            transaction.setTransactionId(json.getString("id"));
            transaction.setBalanceAmount(json.getAmount("amount"));
            transaction.setAuthorizedAmount(
                    json.getString("status").toUpperCase().equals(TransactionStatus.Preauthorized.getValue().toUpperCase()) &&
                            !StringUtils.isNullOrEmpty(json.getString("amount")) ? json.getAmount("amount") : null
            );
            transaction.setTimestamp(json.getString("time_created"));
            transaction.setResponseMessage(json.getString("status"));
            transaction.setReferenceNumber(json.getString("reference"));
            transaction.setClientTransactionId(json.getString("reference"));
            transaction.setMultiCapture("MULTIPLE".equals(json.getString("capture_mode")));

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
                    if (!StringUtils.isNullOrEmpty(json.getString("usage_mode"))) {
                        transaction.setTokenUsageMode(getPaymentMethodUsageMode(json));
                    }
                    transaction.setTimestamp(json.getString("time_created"));
                    transaction.setReferenceNumber(json.getString("reference"));

                    if (json.has("card")) {
                        JsonDoc card = json.get("card");

                        transaction.setCardType(card.getString("brand"));
                        transaction.setCardNumber(card.getString("number"));
                        transaction.setCardLast4(card.getString("masked_number_last4"));
                        if (!StringUtils.isNullOrEmpty(card.getString("expiry_month"))) {
                            transaction.setCardExpMonth(card.getInt("expiry_month"));
                        }
                        if (!StringUtils.isNullOrEmpty(card.getString("expiry_year"))) {
                            transaction.setCardExpYear(card.getInt("expiry_year"));
                        }
                    }

                    break;

                case LINK_CREATE:
                case LINK_EDIT:

                    PayLinkResponse payLinkResponse = mapPayLinkResponse(json);

                    if (json.has("transactions")) {
                        JsonDoc trn = json.get("transactions");
                        transaction.setBalanceAmount(trn.getString("amount") != null ? trn.getAmount("amount") : null);
                        payLinkResponse.setAllowedPaymentMethods(trn.getStringArrayList("allowed_payment_methods").toArray(new String[0]));
                    }

                    transaction.setPayLinkResponse(payLinkResponse);

                    break;

                default:
                    break;

            }

            BatchSummary batchSummary = new BatchSummary();
            batchSummary.setBatchReference(json.getString("batch_id"));
            transaction.setBatchSummary(batchSummary);

            if (json.has("payment_method")) {
                JsonDoc paymentMethod = json.get("payment_method");

                transaction.setFingerPrint(paymentMethod.getString("fingerprint"));
                transaction.setFingerPrintIndicator(paymentMethod.getString("fingerprint_presence_indicator"));

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
                    transaction.setPaymentMethodType(paymentMethod.has("bank_transfer") == false ? PaymentMethodType.ACH : transaction.getPaymentMethodType());
                    transaction.setMultiCapturePaymentCount(getIsMultiCapture(json));
                    transaction.setPaymentMethodType(getPaymentMethodType(json) != null ? getPaymentMethodType(json) : transaction.getPaymentMethodType());
                    transaction.setDccRateData(mapDccInfo(json));
                }
            }
        }

        return transaction;
    }

    private static Integer getIsMultiCapture(JsonDoc json) {
        if (!StringUtils.isNullOrEmpty(json.getString("capture_mode"))) {
            switch (json.getString("capture_mode")) {
                case "MULTIPLE":
                    return 1;
                default:
                    return null;
            }
        }
        return null;
    }

    private static PaymentMethodType getPaymentMethodType(JsonDoc json) {
        if (json.get("payment_method").has("bank_transfer")) {
            return PaymentMethodType.ACH;
        } else if (json.get("payment_method").has("apm")) {
            return PaymentMethodType.APM;
        }
        return null;
    }

    private static PaymentMethodUsageMode getPaymentMethodUsageMode(JsonDoc json) {
        if (json.has("usage_mode")) {
            return PaymentMethodUsageMode.valueOf(json.getString("usage_mode"));
        }
        return null;
    }

    public static Transaction MapResponseAPM(String rawResponse) throws GatewayException {
        AlternativePaymentResponse apm = new AlternativePaymentResponse();
        Transaction transaction = mapResponse(rawResponse);

        JsonDoc json = JsonDoc.parse(rawResponse);

        apm.setRedirectUrl(json.get("payment_method").getString("redirect_url"));

        JsonDoc paymentMethodApm = json.get("payment_method").get("apm");

        if(paymentMethodApm != null) {
            apm.setProviderName(paymentMethodApm.getString("provider"));
            apm.setAck(paymentMethodApm.getString("ack"));
            apm.setSessionToken(paymentMethodApm.getString("session_token"));
            apm.setCorrelationReference(paymentMethodApm.getString("correlation_reference"));
            apm.setVersionReference(paymentMethodApm.getString("version_reference"));
            apm.setBuildReference(paymentMethodApm.getString("build_reference"));
            apm.setTimeCreatedReference(paymentMethodApm.getDateTime("time_created_reference"));
            apm.setTransactionReference(paymentMethodApm.getString("transaction_reference"));
            apm.setSecureAccountReference(paymentMethodApm.getString("secure_account_reference"));
            apm.setReasonCode(paymentMethodApm.getString("reason_code"));
            apm.setPendingReason(paymentMethodApm.getString("pending_reason"));
            apm.setGrossAmount(paymentMethodApm.getAmount("gross_amount"));
            apm.setPaymentTimeReference(paymentMethodApm.getDateTime("payment_time_reference"));
            apm.setPaymentType(paymentMethodApm.getString("payment_type"));
            apm.setPaymentStatus(paymentMethodApm.getString("payment_status"));
            apm.setType(paymentMethodApm.getString("type"));
            apm.setProtectionEligibility(paymentMethodApm.getString("protection_eligibilty"));
            apm.setFeeAmount(paymentMethodApm.getAmount("fee_amount"));
        }

        JsonDoc authorization = json.get("payment_method").get("authorization");
        if (authorization != null) {
            apm.setAuthStatus(authorization.getString("status"));
            apm.setAuthAmount(authorization.getAmount("amount"));
            apm.setAuthAck(authorization.getString("ack"));
            apm.setAuthCorrelationReference(authorization.getString("correlation_reference"));
            apm.setAuthVersionReference(authorization.getString("version_reference"));
            apm.setAuthBuildReference(authorization.getString("build_reference"));
            apm.setAuthPendingReason(authorization.getString("pending_reason"));
            apm.setAuthProtectionEligibility(authorization.getString("protection_eligibilty"));
            apm.setAuthProtectionEligibilityType(authorization.getString("protection_eligibilty_type"));
            apm.setAuthReference(authorization.getString("reference"));
        }

        transaction.setAlternativePaymentResponse(apm);

        return transaction;
    }

    public static TransactionSummary mapTransactionSummary(JsonDoc doc) throws GatewayException {
        TransactionSummary summary = createTransactionSummary(doc);

        summary.setClientTransactionId(doc.getString("reference"));
        summary.setTransactionLocalDate(parseGpApiDateTime(doc.getString("time_created_reference")));
        summary.setBatchSequenceNumber(doc.getString("batch_id"));
        summary.setCountry(doc.getString("country"));
        summary.setOriginalTransactionId(doc.getString("parent_resource_id"));
        summary.setDepositReference(doc.getString("deposit_id"));
        summary.setDepositDate(parseGpApiDate(doc.getString("deposit_time_created")));
        summary.setDepositStatus(doc.getString("deposit_status"));

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

        if (doc.has("payment_method")) {
            JsonDoc paymentMethod = doc.get("payment_method");
            summary.setGatewayResponseMessage(paymentMethod.getString("message"));
            summary.setEntryMode(paymentMethod.getString("entry_mode"));
            summary.setCardHolderName(paymentMethod.getString("name"));

            if (paymentMethod.has("card")) {
                JsonDoc card = paymentMethod.get("card");
                summary.setCardType(card.getString("brand"));
                summary.setAuthCode(card.getString("authcode"));
                summary.setBrandReference(card.getString("brand_reference"));
                summary.setAcquirerReferenceNumber(card.getString("arn"));
                summary.setMaskedCardNumber(card.getString("masked_number_first6last4"));
                summary.setPaymentType(PaymentMethodName.Card.getValue(Target.GP_API));
            } else if (paymentMethod.has("digital_wallet")) {
                JsonDoc digitalWallet = paymentMethod.get("digital_wallet");
                summary.setMaskedCardNumber(digitalWallet.getString("masked_token_first6last4"));
                summary.setPaymentType(PaymentMethodName.DigitalWallet.getValue(Target.GP_API));
            } else if (paymentMethod.has("bank_transfer")) {
                JsonDoc bankTransfer = paymentMethod.get("bank_transfer");
                summary.setAccountNumberLast4(bankTransfer.getString("masked_account_number_last4"));
                summary.setAccountType(bankTransfer.getString("account_type"));
                summary.setPaymentType(PaymentMethodName.BankTransfer.getValue(Target.GP_API));
            } else if (paymentMethod.has("apm")) {
                JsonDoc apm = paymentMethod.get("apm");
                AlternativePaymentResponse alternativePaymentResponse = new AlternativePaymentResponse();
                alternativePaymentResponse.setRedirectUrl(apm.getString("redirect_url"));
                alternativePaymentResponse.setProviderName(apm.getString("provider"));
                alternativePaymentResponse.setProviderReference(apm.getString("provider_reference"));
                summary.setAlternativePaymentResponse(alternativePaymentResponse);
                summary.setPaymentType(PaymentMethodName.APM.getValue(Target.GP_API));
            }
        }

        return summary;
    }

    public static PayLinkSummary mapPayLinkSummary(JsonDoc doc) throws GatewayException {
        PayLinkSummary summary = new PayLinkSummary();

        summary.setId(doc.getString("id"));
        summary.setMerchantId(doc.getString("merchant_id"));
        summary.setMerchantName(doc.getString("merchant_name"));
        summary.setAccountId(doc.getString("account_id"));
        summary.setAccountName(doc.getString("account_name"));
        summary.setUrl(doc.getString("url"));
        summary.setStatus(PayLinkStatus.valueOf(doc.getString("status")));
        summary.setType(PayLinkType.valueOf(doc.getString("type").toUpperCase()));
        summary.setAllowedPaymentMethods(getAllowedPaymentMethods(doc));
        summary.setUsageMode(getPaymentMethodUsageMode(doc));
        summary.setUsageCount(doc.getString("usage_count"));
        summary.setReference(doc.getString("reference"));
        summary.setName(doc.getString("name"));
        summary.setDescription(doc.getString("description"));
        summary.setShippable(doc.getString("shippable"));
        summary.setViewedCount(doc.getString("viewed_count"));
        summary.setExpirationDate(doc.getDateTime("expiration_date"));
        summary.setImages(doc.getStringArrayList("images"));

        if (doc.has("transactions")) {
            List<TransactionSummary> transactionSummaryList = new ArrayList<>();
            for (JsonDoc transaction : doc.getEnumerator("transactions")) {
                transactionSummaryList.add(createTransactionSummary(transaction));
            }
            summary.setTransactions(transactionSummaryList);
        }

        return summary;
    }

    private static PayLinkResponse mapPayLinkResponse(JsonDoc doc) throws GatewayException {
        PayLinkResponse payLinkResponse =
                new PayLinkResponse()
                        .setId(doc.getString("id"))
                        .setAccountName(doc.getString("account_name"))
                        .setUrl(doc.getString("url"))
                        .setStatus(PayLinkStatus.valueOf(doc.getString("status")))
                        .setType(PayLinkType.valueOf(doc.getString("type")))
                        .setUsageMode(PaymentMethodUsageMode.valueOf(doc.getString("usage_mode")))
                        .setUsageLimit(doc.getInt("usage_limit"))
                        .setReference(doc.getString("reference"))
                        .setName(doc.getString("name"))
                        .setDescription(doc.getString("description"))
                        .setIsShippable(doc.getBool("shippable"))
                        .setViewedCount(doc.getString("viewed_count"))
                        .setExpirationDate(doc.getString("expiration_date") != null ? new DateTime(doc.getDate("expiration_date")) : null);

        return payLinkResponse;
    }

    private static TransactionSummary createTransactionSummary(JsonDoc doc) throws GatewayException {
        TransactionSummary transaction = new TransactionSummary();

        transaction.setTransactionId(doc.getString("id"));
        transaction.setTransactionDate(parseGpApiDateTime(doc.getString("time_created")));
        transaction.setTransactionStatus(doc.getString("status"));
        transaction.setTransactionType(doc.getString("type"));
        transaction.setChannel(doc.getString("channel"));
        transaction.setAmount(doc.getAmount("amount"));
        transaction.setCurrency(doc.getString("currency"));
        transaction.setReferenceNumber(doc.getString("reference"));

        return transaction;
    }

    private static List<PaymentMethodName> getAllowedPaymentMethods(JsonDoc doc) {
        List<PaymentMethodName> list = new ArrayList<>();
        for (String item : doc.getStringArrayList("allowed_payment_methods")) {
            for (PaymentMethodName paymentMethodName : PaymentMethodName.values()) {
                if (paymentMethodName.getValue(Target.GP_API).equals(item)) {
                    list.add(paymentMethodName);
                }
            }
        }

        return list;
    }

    public static <T> T mapReportResponse(String rawResponse, ReportType reportType) throws ApiException {
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

            case DocumentDisputeDetail:
                return (T) mapDisputeDocument(json) ;

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

            case PayLinkDetail:
                return (T) mapPayLinkSummary(json);

            case FindPayLinkPaged:
                return (T) mapPayLinks(json);

            default:
                throw new UnsupportedTransactionException();
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

    public static DisputeDocument mapDisputeDocument(JsonDoc doc)
    {
        DisputeDocument document = new DisputeDocument();
        document.setId(doc.getString("id"));
        document.setType(doc.get("action") != null ? doc.get("action").getString("type") : "");
        document.setBase64Content(doc.getString("b64_content"));

        return document;
    }

    public static DisputeSummary mapDisputeSummary(JsonDoc doc) throws GatewayException {
        DisputeSummary summary = new DisputeSummary();

        summary.setCaseId(doc.getString("id"));
        summary.setCaseIdTime(parseGpApiDateTime(doc.getString("time_created")));
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
            summary.setRespondByDate(parseGpApiDateTime(timeToRespondBy));
        }

        if (doc.has("documents")) {
            ArrayList<JsonDoc> documents = (ArrayList<JsonDoc>) doc.getEnumerator("documents");

            ArrayList disputeDocuments = new ArrayList<DisputeDocument>();
            for (JsonDoc document : documents) {
                if (document.getString("id") != null) {
                    DisputeDocument disputeDocument = new DisputeDocument();
                    disputeDocument.setId(document.getString("id"));
                    disputeDocument.setType(document.getString("type") != null ? document.getString("type") : null);

                    disputeDocuments.add(disputeDocument);
                }
            }

            summary.setDocuments(disputeDocuments);
        }

        return summary;
    }

    public static DisputeSummary mapSettlementDisputeSummary(JsonDoc doc) throws GatewayException {
        DisputeSummary summary = mapDisputeSummary(doc);

        summary.setCaseIdTime(parseGpApiDateTime(doc.getString("stage_time_created")));
        summary.setDepositDate(parseGpApiDate(doc.getString("deposit_time_created")));
        summary.setDepositReference(doc.getString("deposit_id"));

        if (doc.has("transaction")) {
            JsonDoc transaction = doc.get("transaction");

            summary.setTransactionTime(parseGpApiDateTime(transaction.getString("time_created")));
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

    public static DccRateData mapDccInfo(JsonDoc response) throws GatewayException {
        JsonDoc currencyConversion = response;

        if (!response.get("action").getString("type").equals("RATE_LOOKUP") &&
                response.get("currency_conversion") == null) {
            return null;
        }

        if (response.get("currency_conversion") != null) {
            currencyConversion = response.get("currency_conversion");
        }

        return
                new DccRateData()
                        .setCardHolderCurrency(currencyConversion.getString("payer_currency"))
                        .setCardHolderAmount(currencyConversion.getAmount("payer_amount"))
                        .setCardHolderRate(currencyConversion.getString("exchange_rate"))
                        .setMerchantCurrency(currencyConversion.getString("currency"))
                        .setMerchantAmount(currencyConversion.getAmount("amount"))
                        .setMarginRatePercentage(currencyConversion.getString("margin_rate_percentage"))
                        .setExchangeRateSourceName(currencyConversion.getString("exchange_rate_source"))
                        .setCommissionPercentage(currencyConversion.getString("commission_percentage"))
                        .setExchangeRateSourceTimestamp(currencyConversion.getDateTime("exchange_rate_time_created"))
                        .setDccId(currencyConversion.getString("id"));
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
            threeDSecure.setServerTransactionId(json.getString("id"));
            threeDSecure.setProviderServerTransRef(
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
                threeDSecure.setAcsReferenceNumber(three_ds.getString("acs_reference_number"));
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

                // Mobile data
                if (!StringUtils.isNullOrEmpty(json.getString("source")) && json.getString("source").equals("MOBILE_SDK")) {
                    if (three_ds.has("mobile_data")) {
                        JsonDoc mobile_data = three_ds.get("mobile_data");

                        threeDSecure.setPayerAuthenticationRequest(mobile_data.getString("acs_signed_content"));

                        if (mobile_data.has("acs_rendering_type")) {
                            JsonDoc acs_rendering_type = mobile_data.get("acs_rendering_type");
                            threeDSecure.setAcsInterface(acs_rendering_type.getString("acs_interface"));
                            threeDSecure.setAcsUiTemplate(acs_rendering_type.getString("acs_ui_template"));
                        }
                    }
                }

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
        storedPaymentMethodSummary.setTimeCreated(parseGpApiDateTime(doc.getString("time_created")));
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

    public static PayLinkSummaryPaged mapPayLinks(JsonDoc doc) throws GatewayException {
        PayLinkSummaryPaged pagedResult = new PayLinkSummaryPaged();
        setPagingInfo(pagedResult, doc);

        for (JsonDoc transaction : doc.getEnumerator("links")) {
            pagedResult.add(mapPayLinkSummary(transaction));
        }

        return pagedResult;
    }

}
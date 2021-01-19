package com.global.api.mapping;

import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.TransactionSummaryList;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.reporting.DepositSummary;
import com.global.api.entities.reporting.DepositSummaryList;
import com.global.api.entities.reporting.DisputeSummary;
import com.global.api.entities.reporting.DisputeSummaryList;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;

import static com.global.api.gateways.GpApiConnector.parseGpApiDate;
import static com.global.api.gateways.GpApiConnector.parseGpApiDateTime;

public class GpApiMapping {

    public static Transaction mapResponse(String rawResponse) {
        Transaction transaction = new Transaction();

        if (!StringUtils.isNullOrEmpty(rawResponse)) {
            JsonDoc json = JsonDoc.parse(rawResponse);

            transaction.setTransactionId(json.getString("id"));
            transaction.setBalanceAmount(json.getDecimal("amount"));
            transaction.setTimestamp(json.getString("time_created"));
            transaction.setResponseMessage(json.getString("status"));
            transaction.setReferenceNumber(json.getString("reference"));

            BatchSummary batchSummary = new BatchSummary();
            batchSummary.setSequenceNumber(json.getString("batch_id"));
            transaction.setBatchSummary(batchSummary);

            transaction.setResponseCode(json.get("action").getString(("result_code")));
            transaction.setToken(json.getString("id"));

            if (json.has("payment_method")) {
                JsonDoc paymentMethod = json.get("payment_method");
                transaction.setAuthorizationCode( paymentMethod.getString(("result")));
                if (paymentMethod.has("card")) {
                    JsonDoc card = paymentMethod.get("card");
                    transaction.setCardType(card.getString("brand"));
                    transaction.setCardLast4(card.getString("masked_number_last4"));
                    transaction.setCvnResponseMessage(card.getString("cvv_result"));
                }
            }

            if( json.has("card")) {
                JsonDoc card = json.get("card");
                transaction.setCardNumber(card.getString("number"));
                transaction.setCardType(card.getString("brand"));
                transaction.setCardExpMonth(card.getInt("expiry_month"));
                transaction.setCardExpYear(card.getInt("expiry_year"));
                transaction.setCardLast4(card.getString("number_last4"));
            }
        }

        return transaction;
    }

    public static TransactionSummary mapTransactionSummary(JsonDoc doc) throws GatewayException {
        TransactionSummary summary = new TransactionSummary();

        //TODO: Map all transaction properties
        summary.setTransactionId(doc.getString("id"));
        summary.setTransactionDate(parseGpApiDateTime(doc.getString("time_created")));
        summary.setTransactionStatus(doc.getString("status"));
        summary.setTransactionType(doc.getString("type"));
        summary.setChannel(doc.getString("channel"));
        summary.setAmount(doc.getDecimal("amount")); // TODO: Check if we have to transform the amount format
        summary.setCurrency(doc.getString("currency"));
        summary.setReferenceNumber(doc.getString("reference"));
        summary.setClientTransactionId(doc.getString("reference"));
        // ?? = DATE_FORMATTER.parseDateTime(doc.getString("time_created_reference"))
        summary.setBatchSequenceNumber(doc.getString("batch_id"));
        summary.setCountry(doc.getString("country"));
        // ?? = doc.getString("action_create_id")
        summary.setOriginalTransactionId(doc.getString("parent_resource_id"));

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
                summary.setAquirerReferenceNumber(card.getString("arn"));
                summary.setMaskedCardNumber(card.getString("masked_number_first6last4"));
            }
            else if(paymentMethod.has("digital_wallet")) {
                    JsonDoc digitalWallet = paymentMethod.get("digital_wallet");

                    summary.setCardType(digitalWallet.getString("brand"));
                    summary.setAuthCode(digitalWallet.getString("authcode"));
                    summary.setBrandReference(digitalWallet.getString("brand_reference"));
                    summary.setMaskedCardNumber(digitalWallet.getString("masked_token_first6last4"));
            }
        }

        return summary;
    }

    public static DepositSummary mapDepositSummary(JsonDoc doc) throws GatewayException {
        DepositSummary summary = new DepositSummary();

        summary.setDepositId(doc.getString("id"));
        summary.setDepositDate(parseGpApiDate(doc.getString("time_created")));
        summary.setStatus(doc.getString("status"));
        summary.setType(doc.getString("funding_type"));
        summary.setAmount(doc.getDecimal("amount"));
        summary.setCurrency(doc.getString("currency"));

        if(doc.has("system")) {
            JsonDoc system = doc.get("system");
            summary.setMerchantNumber(system.getString("mid"));
            summary.setMerchantHierarchy(system.getString("hierarchy"));
            summary.setMerchantName(system.getString ("name"));
            summary.setMerchantDbaName(system.getString("dba"));
        }

        if(doc.has("sales")) {
            JsonDoc sales = doc.get("sales");
            summary.setSalesTotalCount(sales.getInt("count"));
            summary.setSalesTotalAmount(sales.getDecimal("amount"));
        }

        if(doc.has("refunds")) {
            JsonDoc refunds = doc.get("refunds");
            summary.setRefundsTotalCount(refunds.getInt ("count"));
            summary.setRefundsTotalAmount(refunds.getDecimal ("amount"));
        }

        if(doc.has("disputes")) {
            JsonDoc disputes = doc.get("disputes");

            if(disputes.has("chargebacks")) {
                JsonDoc chargebacks = disputes.get("chargebacks");

                summary.setChargebackTotalCount(chargebacks.getInt("count"));
                summary.setChargebackTotalAmount(chargebacks.getDecimal("amount"));
            }

            if(disputes.has("reversals")) {
                JsonDoc reversals = disputes.get("reversals");

                summary.setAdjustmentTotalCount(reversals.getInt ("count"));
                summary.setAdjustmentTotalAmount(reversals.getDecimal ("amount"));
            }
        }

        if(doc.has("fees")) {
            JsonDoc fees = doc.get("fees");

            summary.setFeesTotalAmount(fees.getDecimal("amount"));
        }

        if(doc.has("bank_transfer")) {
            JsonDoc bankTransfer = doc.get("bank_transfer");

            summary.setAccountNumber(bankTransfer.getString("masked_account_number_last4"));
        }

        return summary;
    }

    public static DisputeSummary mapDisputeSummary(JsonDoc doc) throws GatewayException {
        DisputeSummary summary = new DisputeSummary();

        //TODO: Map dispute summary
        summary.setCaseId(doc.getString("id"));
        summary.setCaseIdTime(parseGpApiDate(doc.getString("time_created")));
        summary.setCaseStatus(doc.getString("status"));
        summary.setCaseStage(doc.getString("stage"));
        summary.setCaseAmount(doc.getDecimal("amount"));
        summary.setCaseCurrency(doc.getString("currency"));

        if(doc.has("system")) {
            JsonDoc system = doc.get("system");
            summary.setCaseMerchantId(system.getString("mid"));
            summary.setMerchantHierarchy(system.getString("hierarchy"));
        }

        if (doc.has("payment_method")) {
            JsonDoc paymentMethod = doc.get("payment_method");
            JsonDoc card = paymentMethod.get("card");
            if (card != null) {
                summary.setTransactionMaskedCardNumber(card.getString("number"));
                summary.setTransactionARN(card.getString("arn"));
                summary.setTransactionCardType(card.getString("brand"));
            }
        }
        else {
            if (doc.has("transaction")) {
                JsonDoc transaction = doc.get("transaction");
                if (transaction.has("payment_method")) {
                    JsonDoc transactionPaymentMethod = transaction.get("payment_method");
                    if (transactionPaymentMethod.has("card")) {
                        JsonDoc card = transactionPaymentMethod.get("card");
                        if (card != null) {
                            summary.setTransactionMaskedCardNumber(card.getString("masked_number_first6last4"));
                            summary.setTransactionARN(card.getString("arn"));
                            summary.setTransactionCardType(card.getString("brand"));
                        }
                    }
                }
            }
        }

        summary.setReasonCode(doc.getString("reason_code"));
        summary.setReason(doc.getString("reason_description"));
        summary.setRespondByDate(doc.getDate("time_to_respond_by"));
        summary.setResult(doc.getString("result"));
        summary.setLastAdjustmentAmount(doc.getDecimal("last_adjustment_amount"));
        summary.setLastAdjustmentCurrency(doc.getString("last_adjustment_currency"));
        summary.setLastAdjustmentFunding(doc.getString("last_adjustment_funding"));

        return summary;
    }

    public static TransactionSummaryList mapTransactions(JsonDoc doc) throws GatewayException {
        TransactionSummaryList transactionsList = new TransactionSummaryList();

        for (JsonDoc transaction : doc.getEnumerator("transactions")) {
            transactionsList.add(mapTransactionSummary(transaction));
        }

        return transactionsList;
    }

    public static DepositSummaryList mapDeposits(JsonDoc doc) throws GatewayException {
        DepositSummaryList depositSummaryList = new DepositSummaryList();

        for (JsonDoc deposit : doc.getEnumerator("deposits")) {
            depositSummaryList.add(mapDepositSummary(deposit));
        }

        return depositSummaryList;
    }

    public static DisputeSummaryList mapDisputes(JsonDoc doc) throws GatewayException {
        DisputeSummaryList disputeSummaryList = new DisputeSummaryList();

        for (JsonDoc transaction : doc.getEnumerator("disputes")) {
            disputeSummaryList.add(mapDisputeSummary(transaction));
        }

        return disputeSummaryList;
    }

}

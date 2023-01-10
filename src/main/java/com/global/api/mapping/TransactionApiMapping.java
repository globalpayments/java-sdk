package com.global.api.mapping;

import com.global.api.entities.Transaction;
import com.global.api.entities.TransactionSummary;
import com.global.api.utils.JsonDoc;

import java.math.BigDecimal;

public interface TransactionApiMapping {

    static Transaction mapResponse(String rawResponse) {
        Transaction transaction = new Transaction();
        TransactionSummary transactionSummary = new TransactionSummary();
        JsonDoc response = JsonDoc.parse(rawResponse);
        transaction.setResponseCode(response.getString("status"));
        transaction.setTransactionId(response.getString("creditsale_id", "creditreturn_id", "auth_id", "checksale_id", "checkrefund_id"));
        transaction.setClientTransactionId(response.getString("reference_id"));

        //Mapping check details
        if (response.get("check") != null) {
            transaction.setCheckNumber(response.get("check").getString("check_number"));
            transactionSummary.setMaskedCardNumber(response.get("check").getString("masked_account_number"));
            transaction.setBankNumber(response.get("check").getString("bank_number"));
            transaction.setBranchTransitNumber(response.get("check").getString("branch_transit_number"));
            transaction.setBsbNumber(response.get("check").getString("bsb_number"));
            transaction.setFinancialInstitutionNumber(response.get("check").getString("financial_institution_number"));
            transaction.setRoutingNumber(response.get("check").getString("routing_number"));
            transaction.setToken(response.get("check").getString("token"));
        }
        //Mapping receipt
        if (response.get("receipt") != null) {
            transaction.setCustomerReceipt(response.get("receipt").get("text").getString("customer_receipt"));
            transaction.setMerchantReceipt(response.get("receipt").get("text").getString("merchant_receipt"));
        }
        //Mapping Payment details
        if (response.get("payment") != null) {
            transactionSummary.setAmount(new BigDecimal(response.get("payment").getString("amount")));
            transaction.setOrigionalAmount(new BigDecimal(response.get("payment").getString("amount")));
            transactionSummary.setCurrency(response.get("payment").getString("currency_code"));
            transactionSummary.setInvoiceNumber(response.get("payment").getString("invoice_number"));
            transactionSummary.setCardType(response.get("payment").getString("type"));
            if (response.get("payment").getString("gratuity_amount") != null) {
                transactionSummary.setGratuityAmount(new BigDecimal(response.get("payment").getString("gratuity_amount")));
            }
            if (response.get("payment").get("fee") != null) {
                transaction.setCustomerFeeAmount(new BigDecimal(response.get("payment").get("fee").getString("customer_fee_amount")));
            }
            if (response.get("payment").get("purchase_order") != null) {
                transactionSummary.setPoNumber(response.get("payment").get("purchase_order").getString("po_number"));
                if (response.get("payment").get("purchase_order").getString("tax_amount") != null) {
                    transactionSummary.setTaxAmount(new BigDecimal(response.get("payment").get("purchase_order").getString("tax_amount")));
                }
            }
        }
        //Mapping Transaction details
        if (response.get("transaction") != null) {
            transactionSummary.setEntryMode(response.get("transaction").getString("entry_class"));
            transactionSummary.setLanguage(response.get("transaction").getString("language"));
            transactionSummary.setPaymentPurposeCode(response.get("transaction").getString("payment_purpose_code"));
            transactionSummary.setVerificationCode(response.get("transaction").getString("verification_code"));
            transactionSummary.setBatchSequenceNumber(response.get("transaction").getString("batch_number"));
            if (response.get("transaction").getString("batch_number") != null) {
                transactionSummary.setBatchAmount(new BigDecimal(response.get("transaction").getString("batch_number")));
            }
        }
        //Mapping Card details
        if (response.get("card") != null) {
            transactionSummary.setMaskedCardNumber(response.get("card").getString("masked_card_number"));
            transaction.setCardExpMonth(response.get("card").getInt("expiry_month"));
            transaction.setCardExpYear(response.get("card").getInt("expiry_year"));
            transaction.setToken(response.get("card").getString("token"));
            transaction.setCardType(response.get("card").getString("type"));
            if (response.get("card").getString("balance") != null) {
                transaction.setAvailableBalance(new BigDecimal(response.get("card").getString("balance")));
            }
        }

        transaction.setAuthorizationCode(response.getString("approval_code"));
        transaction.setResponseMessage(response.getString("processor_response"));
        transaction.setTransactionSummary(transactionSummary);
        return transaction;
    }
}

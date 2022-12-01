package com.global.api.mapping;

import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.PagedResult;
import com.global.api.entities.reporting.TransactionSummaryPaged;
import com.global.api.utils.EnumUtils;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Locale;

public class OpenBankingMapping {

    public static Transaction mapResponse(String rawResponse) {
        Transaction transaction = new Transaction();

        if (!StringUtils.isNullOrEmpty(rawResponse)) {
            JsonDoc json = new JsonDoc().parse(rawResponse);

            transaction.setTransactionId(json.getString("ob_trans_id"));
            transaction.setPaymentMethodType(PaymentMethodType.BankPayment);
            if(json.get("order") != null) {
                transaction.setOrderId(json.get("order").getString("id"));
            }

            transaction.setResponseMessage(json.getString("status"));

            BankPaymentResponse obResponse = new BankPaymentResponse();
            obResponse.setRedirectUrl(json.getString("redirect_url"));
            obResponse.setPaymentStatus(json.getString("status"));
            obResponse.setId(json.getString("ob_trans_id"));
            transaction.setBankPaymentResponse(obResponse);
        }

        return transaction;
    }

    public static <T> T mapReportResponse(String rawResponse, ReportType reportType) throws ApiException, InstantiationException, IllegalAccessException {
        JsonDoc json = new JsonDoc().parse(rawResponse);

        switch (reportType) {
            case FindBankPayment:
                return (T) mapTransactions(json);
            default:
                throw new NotImplementedException();
        }
    }

    public static TransactionSummaryPaged mapTransactions(JsonDoc doc) throws GatewayException {
        TransactionSummaryPaged pagedResult = new TransactionSummaryPaged();
        setPagingInfo(pagedResult, doc);

        for (JsonDoc transaction : doc.getEnumerator("payments")) {
            pagedResult.add(mapTransactionSummary(transaction));
        }

        return pagedResult;
    }

    private static <T> void setPagingInfo(PagedResult<T> result, JsonDoc json) {
        result.setTotalRecordCount(json.getInt("total_number_of_records"));
        result.setPageSize(json.getInt("max_page_size"));
        result.setPage(json.getInt("page_number"));
    }

    public static TransactionSummary mapTransactionSummary(JsonDoc response) throws GatewayException {
        TransactionSummary summary = new TransactionSummary();

        summary.setTransactionId(response.getString("ob_trans_id"));
        summary.setOrderId(response.getString("order_id"));
        summary.setAmount(response.getAmount("amount"));
        summary.setCurrency(response.getString("currency"));
        summary.setTransactionStatus(response.getString("status"));
        summary.setPaymentType(EnumUtils.getMapping(Target.Realex, PaymentMethodName.BankPayment));
        summary.setTransactionDate(response.getDateTime("created_on"));

        BankPaymentResponse bankPaymentData = new BankPaymentResponse();

        bankPaymentData.setId(response.getString("ob_trans_id"));
        bankPaymentData.setType(getBankPaymentType(response));
        bankPaymentData.setTokenRequestId(response.getString("token_request_id"));
        bankPaymentData.setIban(response.getString("dest_iban"));
        bankPaymentData.setAccountName(response.getString("dest_account_name"));
        bankPaymentData.setAccountNumber(response.getString("dest_account_number"));
        bankPaymentData.setSortCode(response.getString("dest_sort_code"));
        bankPaymentData.setPaymentStatus(response.getString("status"));

        summary.setBankPaymentResponse(bankPaymentData);

        return summary;
    }

    private static BankPaymentType getBankPaymentType(JsonDoc response) {
        if (response.has("payment_type")) {
            if (BankPaymentType.FASTERPAYMENTS.toString().toUpperCase().equalsIgnoreCase(response.getString("payment_type"))) {
                return BankPaymentType.FASTERPAYMENTS;
            } else if (BankPaymentType.SEPA.toString().toUpperCase().equalsIgnoreCase(response.getString("payment_type"))) {
                return BankPaymentType.SEPA;
            }
        }
        return null;
    }

}
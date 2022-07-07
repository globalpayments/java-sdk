package com.global.api.gateways;

import com.global.api.builders.*;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.entities.reporting.SearchCriteriaBuilder;
import com.global.api.mapping.OpenBankingMapping;
import com.global.api.paymentMethods.BankPayment;
import com.global.api.utils.GenerationUtils;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.var;
import org.apache.http.HttpStatus;
import java.text.SimpleDateFormat;
import java.util.HashMap;

public class OpenBankingProvider extends RestGateway implements IOpenBankingProvider, IReportingService {
    static final String DATE_TIME_PATTERN = "yyyyMMddHHmmss";
    static final SimpleDateFormat DATE_SDF = new SimpleDateFormat(DATE_TIME_PATTERN);

    @Getter @Setter private String merchantId;
    @Getter @Setter private String accountId;
    @Getter @Setter private String sharedSecret;

    @Override
    public boolean supportsHostedPayments() {
        return false;
    }

    @Getter @Setter private ShaHashType shaHashType;

    public OpenBankingProvider() {
        super();
        headers.put(org.apache.http.HttpHeaders.ACCEPT, "application/json");
    }

    public Transaction processOpenBanking(BankPaymentBuilder builder) throws GatewayException {
        String timestamp = builder.getTimestamp() != null ? builder.getTimestamp() : GenerationUtils.generateTimestamp();
        String orderId = builder.getOrderId() != null ? builder.getOrderId() : GenerationUtils.generateOrderId();
        // TODO: Check this convertion
        var amount = builder.getAmount() != null ? StringUtils.toNumeric(builder.getAmount()) : null;

        JsonDoc request = new JsonDoc();

        BankPayment paymentMethod = (BankPayment) builder.getPaymentMethod();

        switch (builder.getTransactionType()) {
            case Sale:
                var bankPaymentType = (paymentMethod != null && paymentMethod.getBankPaymentType() != null) ?
                        paymentMethod.getBankPaymentType() : getBankPaymentType(builder.getCurrency());

                String hash = GenerationUtils.generateHash(sharedSecret, shaHashType, timestamp, merchantId, orderId, amount.toString(), builder.getCurrency(),
                        !StringUtils.isNullOrEmpty(paymentMethod.getSortCode()) && BankPaymentType.FASTERPAYMENTS.equals(bankPaymentType) ?
                                paymentMethod.getSortCode() : "",
                        !StringUtils.isNullOrEmpty(paymentMethod.getAccountNumber()) && BankPaymentType.FASTERPAYMENTS.equals(bankPaymentType) ?
                                paymentMethod.getAccountNumber() : "",
                        !StringUtils.isNullOrEmpty(paymentMethod.getIban()) && BankPaymentType.SEPA.equals(bankPaymentType) ? paymentMethod.getIban() : "");

                setAuthorizationHeader(hash);

                request
                        .set("request_timestamp", timestamp)
                        .set("merchant_id", merchantId)
                        .set("account_id", accountId);

                JsonDoc order =
                        new JsonDoc()
                                .set("id", orderId)
                                .set("currency", builder.getCurrency())
                                .set("amount", amount.toString())
                                .set("description", builder.getDescription());

                JsonDoc destination =
                        new JsonDoc()
                                .set("account_number", BankPaymentType.FASTERPAYMENTS.equals(bankPaymentType) ? paymentMethod.getAccountNumber() : null)
                                .set("sort_code", BankPaymentType.FASTERPAYMENTS.equals(bankPaymentType) ? paymentMethod.getSortCode() : null)
                                .set("iban", BankPaymentType.SEPA.equals(bankPaymentType) ? paymentMethod.getIban() : null)
                                .set("name", paymentMethod.getAccountName());


                JsonDoc payment = new JsonDoc();

                JsonDoc remittance_reference =
                        new JsonDoc()
                                .set("type", builder.getRemittanceReferenceType() != null ? builder.getRemittanceReferenceType().toString() : null)
                                .set("value", builder.getRemittanceReferenceValue());

                payment
                        .set("scheme", bankPaymentType != null ? bankPaymentType.toString() : "")
                        .set("destination", destination);

                if(!remittance_reference.getKeys().isEmpty()) {
                    payment.set("remittance_reference", remittance_reference);
                }

                request
                        .set("order", order)
                        .set("payment", payment)
                        .set("return_url", paymentMethod.getReturnUrl())
                        .set("status_url", paymentMethod.getStatusUpdateUrl());

                break;
            default:
                break;
        }

        try {
            String rawResponse = doTransaction("POST", "/payments", request.toString());

            return OpenBankingMapping.mapResponse(rawResponse);
        } catch (GatewayException gatewayException) {
            throw gatewayException;
        }
    }

    public <T> T processReport(ReportBuilder<T> builder, Class<T> clazz) throws ApiException {
        HashMap<String, String> queryParams = new HashMap<>();
        String timestamp = GenerationUtils.generateTimestamp();

        switch (builder.getReportType()) {

            case FindBankPayment:

                if (builder instanceof  TransactionReportBuilder) {
                    var trb = (TransactionReportBuilder<T>) builder;
                    queryParams.put("accountId", !StringUtils.isNullOrEmpty(accountId) ? accountId : "");

                    SearchCriteriaBuilder<T> searchBuilder = trb.getSearchBuilder();

                    String _accountId = StringUtils.isNullOrEmpty(searchBuilder.getBankPaymentId()) ? accountId : "";

                    String hash = GenerationUtils.generateHash(sharedSecret, shaHashType, timestamp, merchantId, _accountId,
                            !StringUtils.isNullOrEmpty(searchBuilder.getBankPaymentId()) ? searchBuilder.getBankPaymentId() : "",
                            searchBuilder.getStartDate() != null ? DATE_SDF.format(searchBuilder.getStartDate()) : "",
                            searchBuilder.getEndDate() != null ? DATE_SDF.format(searchBuilder.getEndDate()) : "",
                            searchBuilder.getReturnPII() != null ? (searchBuilder.getReturnPII().booleanValue() ? "True" : "False") : "");

                    setAuthorizationHeader(hash);

                    queryParams.put("timestamp", timestamp);
                    queryParams.put("merchantId", merchantId);
                    if(!StringUtils.isNullOrEmpty(accountId)) {
                        queryParams.put("accountId", accountId);
                    }
                    var obTransId = !StringUtils.isNullOrEmpty(searchBuilder.getBankPaymentId()) ? searchBuilder.getBankPaymentId() : "";
                    if(!StringUtils.isNullOrEmpty(obTransId)) {
                        queryParams.put("obTransId", obTransId);
                    }
                    var startDate = searchBuilder.getStartDate() != null ? DATE_SDF.format(searchBuilder.getStartDate()) : "";
                    if(!StringUtils.isNullOrEmpty(startDate)) {
                        queryParams.put("startDateTime", startDate);
                    }
                    var endDate = searchBuilder.getEndDate() != null ? DATE_SDF.format(searchBuilder.getEndDate()) : "";
                    if(!StringUtils.isNullOrEmpty(endDate)) {
                        queryParams.put("endDateTime", endDate);
                    }
                    var transState = searchBuilder.getBankPaymentStatus() != null ? searchBuilder.getBankPaymentStatus().toString() : "";
                    if(!StringUtils.isNullOrEmpty(transState)) {
                        queryParams.put("transactionState", transState);
                    }
                    var returnPii = searchBuilder.getReturnPII() != null ? (searchBuilder.getReturnPII().booleanValue() ? "True" : "False") : "";
                    if(!StringUtils.isNullOrEmpty(returnPii)) {
                        queryParams.put("returnPii", returnPii);
                    }
                }
            break;

            default:
                break;
        }

        try {
            String response = doTransaction("GET", "/payments", null, queryParams);

            return OpenBankingMapping.mapReportResponse(response, builder.getReportType());
        } catch (GatewayException ex) {
            throw ex;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void setAuthorizationHeader(String value) {
        headers.put("Authorization", this.shaHashType + " " + value);
    }

    public static BankPaymentType getBankPaymentType(String currency) {
        switch (currency) {
            case "EUR":
                return BankPaymentType.SEPA;
            case "GBP":
                return BankPaymentType.FASTERPAYMENTS;
            default:
                return null;
        }
    }

    @Override
    protected String handleResponse(GatewayResponse response) throws GatewayException {
        if (response.getStatusCode() != HttpStatus.SC_CREATED && response.getStatusCode() != HttpStatus.SC_NO_CONTENT && response.getStatusCode() != HttpStatus.SC_OK) {
            var parsed = JsonDoc.parse(response.getRawResponse());
            var parsedError = parsed.get("error");
            var error = parsedError != null ? parsedError : parsed;

            throw new GatewayException("Status Code: " + response.getStatusCode() + " - " + error.getString("message"));
        }

        return response.getRawResponse();
    }

    public String serializeRequest(BankPaymentBuilder builder) throws UnsupportedTransactionException {
        throw new UnsupportedTransactionException();
    }

}
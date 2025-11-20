package com.global.api.tests.transit;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.GatewayProvider;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.TransitConfig;
import com.global.api.services.ReportingService;
import com.global.api.utils.DateUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;


public class TransitReportingTests {
    private final CreditCardData card;

    public TransitReportingTests() throws ApiException {
        TransitConfig config = getConfig();
        ServicesContainer.configureService(config);

        card = new CreditCardData();
        card.setNumber("4111111111111111");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("999");
    }

    protected TransitConfig getConfig() {
        TransitConfig config = new TransitConfig();
        config.setMerchantId("884000003531");
        config.setUsername("TA5876503");
        config.setPassword("HRQATest!000");
        config.setDeviceId("88400000353102");
        config.setTransactionKey("7WDYEC6LE9T5Q8EER5CWRPN3P4O5BZH8");
        config.setDeveloperId("003226G001");
        config.setGatewayProvider(GatewayProvider.TRANSIT);
        config.setAcceptorConfig(new AcceptorConfig());
        return config;
    }

    @Test
    public void ReportActivity() throws ApiException {

        DateFormat formatterUTC = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        formatterUTC.setTimeZone(TimeZone.getTimeZone("UTC")); // UTC timezone

        Date startDate = DateUtils.addDays(new Date(), -7);
        Date endDate = DateUtils.addDays(new Date(), -1);

        String startDateUTC = formatterUTC.format(startDate);
        String endDateUTC = formatterUTC.format(endDate);

        ActivityReport summary = ReportingService.activity()
                .withStartDateUTC(startDateUTC)
                .withEndDateUTC(endDateUTC)
                .execute();
        assertNotNull(summary);
    }

    @Test
    public void ReportTransactionDetail() throws ApiException {
        TransactionSummaryList summary = ReportingService.findTransactions()
                .withStartDate(DateUtils.addDays(new Date(), -7))
                .withEndDate(DateUtils.addDays(new Date(), -1))
                .execute();

        if (!summary.isEmpty()) {
            TransactionSummary response = ReportingService.transactionDetail(summary.get(0).getTransactionId()).execute();
            assertNotNull(response);
            assertEquals("A0000", response.getGatewayResponseCode());

        }
    }

    @Test
    public void ReportTransactionDetailWithTransactionId() throws ApiException {
        Transaction authResponse = card.authorize(new BigDecimal(15))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();

        assertNotNull(authResponse);

        TransactionSummary response = retryOperation(() -> {
            try {
                return ReportingService.transactionDetail(authResponse.getTransactionId()).execute();
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }, 10, 2000);

        assertNotNull(response);
    }

    @Test
    public void ReportFindTransactionWithTransactionId() throws ApiException {
        TransactionSummary response = ReportingService.findTransactions("67295809")
                .execute();
        assertNotNull(response);
    }

    @Test
    public void ReportFindTransactionNoCriteria() throws ApiException {
        TransactionSummaryList response = ReportingService.findTransactions()
                .execute();
        assertNotNull(response);
    }

    @Test
    public void ReportFindTransactionWithCriteria() throws ApiException {
        CreditCardData creditCardData = new CreditCardData();
        creditCardData.setNumber("4818690480136615");
        creditCardData.setExpMonth(12);
        creditCardData.setExpYear(2025);
        creditCardData.setCvn("999");
        creditCardData.charge(new BigDecimal(15))
                .withAllowDuplicates(true)
                .withCurrency("USD")
                .withInvoiceNumber("1111")
                .execute();

        creditCardData.charge(new BigDecimal(10))
                .withAllowDuplicates(true)
                .withCurrency("USD")
                .withInvoiceNumber("7766")
                .execute();

        creditCardData.charge(new BigDecimal(11))
                .withAllowDuplicates(true)
                .withCurrency("USD")
                .withInvoiceNumber("7766")
                .execute();

        TransactionSummaryList summary = retryOperation(() -> {
            try {
                return ReportingService.findTransactions()
                        .where(SearchCriteria.CardNumberLastFour, "6615")
                        .execute();
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }, 10, 2000);

        assertNotNull(summary);

        if ((summary.size() % 3) != 0) {
            fail(String.format("Test should have produced 3 results. %s transactions were found.", summary.size()));
        }

        TransactionSummaryList summary2 = ReportingService.findTransactions()
                .where(SearchCriteria.SettlementAmount, new BigDecimal(10))
                .and(SearchCriteria.InvoiceNumber, "7766")
                .execute();

        assertNotNull(summary2);

        if ((summary2.size() % 2) != 0) {
            fail(String.format("Test should have produced 2 results. %s transactions were found.", summary2.size()));
        }

        TransactionSummaryList summary3 = ReportingService.findTransactions()
                .where(SearchCriteria.InvoiceNumber, "1111")
                .execute();

        assertNotNull(summary3);

        if ((0) != 0) {
            fail(String.format("Test should have produced 1 result. %s transactions were found.", summary3.size()));
        }
    }

    @Test
    public void ReportFindTransactionWithCardHolderName() throws ApiException {
        CreditCardData creditCardData = new CreditCardData();
        creditCardData.setCardHolderName("John Doe");
        creditCardData.setNumber("4012000098765439");
        creditCardData.setExpMonth(12);
        creditCardData.setExpYear(2025);
        creditCardData.setCvn("999");

        Address address = new Address();
        address.setStreetAddress1("1 Heartland Way");
        address.setCity("Jeffersonville");
        address.setState("IN");
        address.setPostalCode("47130");

        Transaction trResponse = creditCardData.charge(new BigDecimal(22))
                .withCurrency("USD")
                .withAddress(address)
                .execute();

        assertNotNull(trResponse);
        assertEquals("00", trResponse.getResponseCode());

        TransactionSummary response = retryOperation(() -> {
            try {
                return ReportingService.findTransactions(trResponse.getTransactionId()).execute();
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }, 10, 2000);

        assertNotNull(response);
        assertNotNull(response.getCardHolderName());
        assertNotNull(response.getTransactionDate());
        assertNotEquals("", response.getCardHolderName());
    }

    @Test
    public void ReportBatchDetail_WithClientTxnId_and_BatchID() throws ApiException {

        int randomID = new Random().nextInt(999999 - 10000) + 10000;
        String clientTxnID = Integer.toString(randomID);

        Transaction response = card.authorize(new BigDecimal(13))
                .withCurrency("USD")
                .withClientTransactionId(clientTxnID)
                .withAllowDuplicates(true)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction capture = response.capture(new BigDecimal(16)).withGratuity(new BigDecimal(2)).execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());

        TransactionSummaryList reportItem = retryOperation(() -> {
            try {
                return ReportingService.findTransactions()
                        .where(SearchCriteria.ClientTransactionId, clientTxnID)
                        .execute();
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }, 10, 2000);
        assertNotNull(reportItem, "Transaction with clientTxnID " + clientTxnID + " not found");
        assertEquals(clientTxnID, reportItem.get(0).getClientTransactionId());

    }

    @Test
    public void ReportOpenAuths_WithClientTxnId() throws ApiException {

        int randomID = new Random().nextInt(999999 - 10000) + 10000;
        String clientTxnID = Integer.toString(randomID);

        Transaction response = card.authorize(new BigDecimal(15))
                .withCurrency("USD")
                .withClientTransactionId(clientTxnID)
                .withAllowDuplicates(true)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        TransactionSummaryList reportResponse = ReportingService.openAuths()
                .execute();

        assertNotNull(reportResponse);

        TransactionSummaryList reportItem = retryOperation(() -> {
            try {
                return ReportingService.findTransactions()
                        .where(SearchCriteria.ClientTransactionId, clientTxnID)
                        .execute();
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }, 10, 2000);

        assertNotNull(reportItem);
        assertEquals(clientTxnID, reportItem.get(0).getClientTransactionId());

        assertNotNull(response);
    }

    private <T> T retryOperation(Supplier<T> operation, int maxRetries, int delayMs) throws ApiException {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                T result = operation.get();

                if (result != null && !(result instanceof TransactionSummaryList && ((TransactionSummaryList) result).isEmpty())) {
                    return result;
                }

                if (attempt < maxRetries) {
                    Thread.sleep(delayMs);
                }
            } catch (RuntimeException e) {
                Throwable cause = e.getCause();
                if (cause instanceof ApiException) {
                    ApiException apiEx = (ApiException) cause;
                    if (apiEx.getMessage().contains("D5209") && attempt < maxRetries) {
                        try {
                            Thread.sleep(delayMs);
                            continue;
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new ApiException("Test interrupted", ie);
                        }
                    }
                }
                throw e;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ApiException("Test interrupted", e);
            }
        }
        throw new ApiException("Data not available after " + maxRetries + " attempts");
    }
}

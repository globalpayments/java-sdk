package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.TransactionSummaryList;
import com.global.api.entities.enums.SortDirection;
import com.global.api.entities.enums.TransactionSortProperty;
import com.global.api.entities.enums.TransactionStatus;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.reporting.DataServiceCriteria;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.DepositStatus;
import com.global.api.entities.enums.PaymentEntryMode;
import com.global.api.entities.enums.PaymentType;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.ReportingService;
import com.global.api.utils.DateUtils;
import com.global.api.utils.StringUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class GpApiReportingTransactionsTests {

    private static final String TRANSACTION_ID = "TRN_ImiKh03hpvpjJDPMLmCbpRMyv5v6Q7";

    public GpApiReportingTransactionsTests() throws ApiException {

        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        // Pablo Credentials
        config
                .setAppId("OWTP5ptQZKGj7EnvPt3uqO844XDBt8Oj")
                .setAppKey("qM31FmlFiyXRHGYh");

        // Nacho credentials
        //        config
        //                .setAppId("Uyq6PzRbkorv2D4RQGlldEtunEeGNZll")
        //                .setAppKey("QDsW1ETQKHX6Y4TA");

        config.setEnableLogging(true);

        ServicesContainer.configureService(config, "GpApiConfig");
    }

    // ================================================================================
    // Transactions
    // ================================================================================

    @Test
    public void reportTransactionDetail_By_Id() throws ApiException {
        TransactionSummary transaction =
                ReportingService
                        .transactionDetail(TRANSACTION_ID)
                        .execute("GpApiConfig");

        assertNotNull(transaction);
        assertEquals(TRANSACTION_ID, transaction.getTransactionId());
    }

    @Test
    public void reportTransactionDetail_WrongId() throws ApiException {
        String transactionId = UUID.randomUUID().toString();

        try {
            ReportingService
                    .transactionDetail(transactionId)
                    .execute("GpApiConfig");
        } catch (GatewayException ex) {
            assertEquals("40118", ex.getResponseText());
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("Status Code: 404 - Transactions " + transactionId + " not found at this /ucp/transactions/" + transactionId + "", ex.getMessage());
        }
    }

    @Test
    public void reportFindTransactions_By_StartDate() throws ApiException {
        Date startDate = DateUtils.addDays(new Date(), -15);

        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 25)
                        .where(SearchCriteria.StartDate, startDate)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions)
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), startDate));
    }

    @Test
    public void reportFindTransactions_By_StartDate_And_EndDate() throws ApiException {
        Date startDate = DateUtils.addDays(new Date(), -30);
        Date endDate = DateUtils.addDays(new Date(), -10);

        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions) {
            Date transactionDate = transactionSummary.getTransactionDate().toDate();
            assertTrue(DateUtils.isBeforeOrEquals(transactionDate, endDate));
            assertTrue(DateUtils.isAfterOrEquals(transactionDate, startDate));
        }
    }

    @Test
    public void reportFindTransactions_OrderBy_TimeCreated() throws ApiException {
        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .execute("GpApiConfig");

        assertNotNull(transactions);

        TransactionSummaryList transactionsAsc =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Ascending)
                        .withPaging(1, 10)
                        .execute("GpApiConfig");

        assertNotNull(transactionsAsc);

        assertNotSame(transactions, transactionsAsc);
    }

    //TODO - returning empty transaction list
    @Test
    public void reportFindTransactions_OrderBy_Status() throws ApiException {
        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.Status, SortDirection.Descending)
                        .withPaging(1, 10)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
    }

    @Test
    public void reportFindTransactions_OrderBy_Type() throws ApiException {
        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.Type, SortDirection.Descending)
                        .withPaging(1, 10)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
    }

    @Ignore // Although documentation allows order_by DEPOSIT_ID, the real endpoint does not.
    // TODO: Report error to GP-API team. Enable it when fixed.
    @Test
    public void reportFindTransactions_OrderBy_DepositsId() throws ApiException {
        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.DepositId, SortDirection.Descending)
                        .withPaging(1, 10)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
    }

    @Test
    public void compareResults_reportFindTransactions_OrderBy_TypeAndTimeCreated() throws ApiException {
        TransactionSummaryList transactionsOrderedByTimeCreated =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .execute("GpApiConfig");

        assertNotNull(transactionsOrderedByTimeCreated);

        TransactionSummaryList transactionsOrderedByType =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.Type, SortDirection.Descending)
                        .withPaging(1, 10)
                        .execute("GpApiConfig");

        assertNotNull(transactionsOrderedByType);

        assertNotSame(transactionsOrderedByTimeCreated, transactionsOrderedByType);
    }

    @Test
    public void reportFindTransactions_By_Id() throws ApiException {
        String transactionId = "TRN_ouFFiOnhcBFCV7uWeqWYRHJwOmB73V";

        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .withTransactionId(transactionId)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        assertEquals(1, transactions.size());
        assertEquals(transactionId, transactions.get(0).getTransactionId());
    }

    @Test
    public void reportFindTransactions_WrongId() throws ApiException {
        List<TransactionSummary> transactions =
                ReportingService
                        .findTransactions()
                        .withTransactionId("TRN_CQauJhxTXBvPGqIO66MJA3Rfk7V5PUa")
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        assertEquals(0, transactions.size());
    }

    @Test
    public void reportFindTransactions_By_Type() throws ApiException {
        PaymentType paymentType = PaymentType.Sale;
        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.PaymentType, paymentType)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions)
            assertEquals(paymentType.getValue(), transactionSummary.getTransactionType());

        PaymentType refundPaymentType = PaymentType.Refund;
        TransactionSummaryList transactionsRefunded =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.PaymentType, refundPaymentType)
                        .execute("GpApiConfig");

        assertNotNull(transactionsRefunded);
        for (TransactionSummary transactionSummary : transactionsRefunded)
            assertEquals(refundPaymentType.getValue(), transactionSummary.getTransactionType());

        assertNotSame(transactions, transactionsRefunded);
    }

    @Test
    public void reportFindTransactions_By_Amount_And_Currency_And_Country() throws ApiException {
        BigDecimal amount = new BigDecimal("1.12");
        String currency = "aud"; //This is case sensitive
        String country = "AU"; //This is case sensitive

        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.Amount, amount)
                        .and(DataServiceCriteria.Currency, currency)
                        .and(DataServiceCriteria.Country, country)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions) {
            assertEquals(StringUtils.toNumeric(amount), transactionSummary.getAmount().toString());
            assertEquals(currency, transactionSummary.getCurrency());
            assertEquals(country, transactionSummary.getCountry());
        }
    }

    @Test
    public void reportFindTransactions_By_WrongCurrency() throws ApiException {
        String currency = "aUd"; //This is case sensitive

        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.Currency, currency)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        assertEquals(0, transactions.size());
    }

    @Test
    public void reportFindTransactions_By_Channel() throws ApiException {
        Channel channel = Channel.CardNotPresent;

        TransactionSummaryList transactionsCNP =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.Channel, channel)
                        .execute("GpApiConfig");

        assertNotNull(transactionsCNP);
        for (TransactionSummary transactionSummary : transactionsCNP)
            assertEquals(channel.getValue(), transactionSummary.getChannel());

        Channel channelCP = Channel.CardPresent;
        TransactionSummaryList transactionsCP =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.Channel, channelCP)
                        .execute("GpApiConfig");

        assertNotNull(transactionsCP);
        for (TransactionSummary transactionSummary : transactionsCP)
            assertEquals(channelCP.getValue(), transactionSummary.getChannel());

        assertNotSame(transactionsCNP, transactionsCP);
    }

    @Test
    public void reportFindTransactions_By_AllStatus() throws ApiException {
        for (TransactionStatus transactionStatus : TransactionStatus.values()) {

            TransactionSummaryList transactions =
                    ReportingService
                            .findTransactions()
                            .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                            .withPaging(1, 10)
                            .where(SearchCriteria.TransactionStatus, transactionStatus)
                            .execute("GpApiConfig");

            assertNotNull(transactions);
            for (TransactionSummary transactionSummary : transactions)
                // GP-API returns SUCCESS_AUTHENTICATED instead of AUTHENTICATED
                // when searching by TransactionStatus = AUTHENTICATED
                // TODO: Report this particularity to GP-API team
                if (transactionStatus.equals(TransactionStatus.Authenticated)) {
                    assertEquals("SUCCESS_AUTHENTICATED", transactionSummary.getTransactionStatus());
                } else {
                    assertEquals(transactionStatus.getValue(), transactionSummary.getTransactionStatus());
                }
        }
    }

    @Test
    public void reportFindTransactions_By_Status() throws ApiException {
        TransactionStatus transactionStatus = TransactionStatus.Preauthorized;

        TransactionSummaryList transactionsInitiated =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.TransactionStatus, transactionStatus)
                        .execute("GpApiConfig");

        assertNotNull(transactionsInitiated);
        for (TransactionSummary transactionSummary : transactionsInitiated)
            assertEquals(transactionStatus.getValue(), transactionSummary.getTransactionStatus());

        TransactionStatus transactionStatusRejected = TransactionStatus.Rejected;

        TransactionSummaryList transactionsRejected =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.TransactionStatus, transactionStatusRejected)
                        .execute("GpApiConfig");

        assertNotNull(transactionsRejected);
        for (TransactionSummary transactionSummary : transactionsRejected)
            assertEquals(transactionStatusRejected.getValue(), transactionSummary.getTransactionStatus());

        assertNotSame(transactionsInitiated, transactionsRejected);
    }

    @Test
    public void reportFindTransactions_By_CardBrand_And_AuthCode() throws ApiException {
        String cardBrand = "VISA";
        String authCode = "12345";

        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.CardBrand, cardBrand)
                        .and(SearchCriteria.AuthCode, authCode)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions) {
            assertEquals(cardBrand, transactionSummary.getCardType());
            assertEquals(authCode, transactionSummary.getAuthCode());
        }
    }

    //Diners and JCB cards are not working
    @Test
    public void reportFindTransactions_By_CardBrand() throws ApiException {
        String[] cardBrands = {"VISA", "MASTERCARD", "AMEX", "DINERS", "DISCOVER", "JCB", "CUP"};
        String[] cardBrandsShort = {"VISA", "MC", "AMEX", "DINERS", "DISCOVER", "JCB", "CUP"};

        for (int index = 0; index < cardBrands.length; index++) {
            // Although documentation allows DINERS and JCB values, the real endpoint does not.
            // TODO: Report error to GP-API team. Enable it when fixed.
            if (("DINERS").equals(cardBrands[index]) || "JCB".equals(cardBrands[index]))
                continue;

            TransactionSummaryList transactions =
                    ReportingService
                            .findTransactions()
                            .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                            .withPaging(1, 10)
                            .where(SearchCriteria.CardBrand, cardBrands[index])
                            .execute("GpApiConfig");

            assertNotNull(transactions);
            for (TransactionSummary transactionSummary : transactions) {
                assertEquals(cardBrandsShort[index], transactionSummary.getCardType());
            }
        }
    }

    @Test
    public void reportFindTransactions_By_InvalidCardBrand() throws ApiException {
        String cardBrand = "MIT";

        try {
            ReportingService
                    .findTransactions()
                    .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                    .withPaging(1, 10)
                    .where(SearchCriteria.CardBrand, cardBrand)
                    .execute("GpApiConfig");

        } catch (GatewayException ex) {
            assertEquals("40097", ex.getResponseText());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("Status Code: 400 - Invalid Value provided in the input field - brand", ex.getMessage());
        }
    }

    @Test
    public void reportFindTransactions_By_Reference() throws ApiException {
        String referenceNumber = "98f64385-6fcd-4605-b0ae-c5675be681cf";

        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.ReferenceNumber, referenceNumber)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions)
            assertEquals(referenceNumber, transactionSummary.getReferenceNumber());
    }

    @Test
    public void reportFindTransactions_By_RandomReference() throws ApiException {
        String referenceNumber = UUID.randomUUID().toString();

        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.ReferenceNumber, referenceNumber)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        assertEquals(0, transactions.size());
    }

    @Test
    public void reportFindTransactions_By_BrandReference() throws ApiException {
        String brandReference = "300351293234459";

        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.BrandReference, brandReference)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions)
            assertEquals(brandReference, transactionSummary.getBrandReference());
    }

    @Test
    public void reportFindTransactions_By_WrongBrandReference() throws ApiException {
        String brandReference = "000000000000001";

        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.BrandReference, brandReference)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        assertEquals(0, transactions.size());
    }

    @Ignore
    // Although requests are done with &entry_mode set properly, the real endpoint returns transactions with other entry_modes.
    // TODO: Report error to GP-API team. Enable it when fixed.
    @Test
    public void reportFindTransactions_By_AllEntryModes() throws ApiException {
        for (PaymentEntryMode paymentEntryMode : PaymentEntryMode.values()) {

            TransactionSummaryList transactions =
                    ReportingService
                            .findTransactions()
                            .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                            .withPaging(1, 10)
                            .where(SearchCriteria.PaymentEntryMode, paymentEntryMode)
                            .execute("GpApiConfig");

            assertNotNull(transactions);
            for (TransactionSummary transactionSummary : transactions)
                assertEquals(paymentEntryMode.getValue(), transactionSummary.getEntryMode());
        }
    }

    @Test
    public void reportFindTransactions_By_EntryMode() throws ApiException {
        PaymentEntryMode paymentEntryMode = PaymentEntryMode.Ecom;

        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.PaymentEntryMode, paymentEntryMode)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions)
            assertEquals(paymentEntryMode.getValue(), transactionSummary.getEntryMode());


        PaymentEntryMode paymentEntryModeMoto = PaymentEntryMode.Moto;

        TransactionSummaryList transactionsMoto =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.PaymentEntryMode, paymentEntryModeMoto)
                        .execute("GpApiConfig");

        assertNotNull(transactionsMoto);
        for (TransactionSummary transactionSummary : transactionsMoto)
            assertEquals(paymentEntryModeMoto.getValue(), transactionSummary.getEntryMode());

        assertNotSame(transactions, transactionsMoto);
    }

    @Test
    public void reportFindTransactions_By_Number_First6_And_Number_Last4() throws ApiException {
        String number_first6 = "516730";
        String number_last4 = "5507";

        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.CardNumberFirstSix, number_first6)
                        .and(SearchCriteria.CardNumberLastFour, number_last4)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions) {
            assertTrue(transactionSummary.getMaskedCardNumber().startsWith(number_first6));
            assertTrue(transactionSummary.getMaskedCardNumber().endsWith(number_last4));
        }
    }

    @Test
    public void reportFindTransactions_By_Token_First6_And_Token_Last4() throws ApiException {
        String token_first6 = "516730";
        String token_last4 = "5507";

        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.TokenFirstSix, token_first6)
                        .and(SearchCriteria.TokenLastFour, token_last4)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions) {
            String maskedCardNumber = transactionSummary.getMaskedCardNumber();
            if (maskedCardNumber != null) {
                assertTrue(maskedCardNumber.startsWith(token_first6));
                assertTrue(maskedCardNumber.endsWith(token_last4));
            }
        }
    }

    @Test
    public void reportFindTransactions_By_BatchId() throws ApiException {
        String batchId = "BAT_875461";

        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.BatchId, batchId)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions)
            assertEquals(batchId, transactionSummary.getBatchSequenceNumber());
    }

    @Test
    public void reportFindTransactions_By_WrongBatchId() throws ApiException {
        String batchId = "BAT_000461";

        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.BatchId, batchId)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        assertEquals(0, transactions.size());
    }

    @Test
    public void reportFindTransactions_By_Name() throws ApiException {
        String name = "James Mason";

        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.Name, name)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions)
            assertEquals(name, transactionSummary.getCardHolderName());
    }

    @Test
    public void reportFindTransactions_By_RandomName() throws ApiException {
        String name = UUID.randomUUID().toString().replace("-", "");

        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.Name, name)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        assertEquals(0, transactions.size());
    }

    @Test
    public void reportFindTransactions_WithInvalid_AccountName() throws ApiException {
        try {
            ReportingService
                    .findTransactions()
                    .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                    .withPaging(1, 10)
                    .where(SearchCriteria.AccountName, "VISA")
                    .execute("GpApiConfig");

        } catch (GatewayException ex) {
            assertEquals("40003", ex.getResponseText());
            assertEquals("ACTION_NOT_AUTHORIZED", ex.getResponseCode());
            assertEquals("Status Code: 400 - Token does not match account_id or account_name in the request", ex.getMessage());
        }
    }

    @Ignore // Although documentation indicates from_time_created is required, the real endpoint returns results.
    // TODO: Report error to GP-API team. Enable it when fixed.
    @Test
    public void reportFindTransactions_Without_Mandatory_StartDate() throws ApiException {
        try {
            TransactionSummaryList summary =
                    ReportingService
                            .findTransactions()
                            .execute("GpApiConfig");

        } catch (GatewayException ex) {
            assertEquals("40008", ex.getResponseText());
            assertEquals("TRANSACTION_NOT_FOUND", ex.getResponseCode());
            assertEquals("Status Code: 404 - Transaction to action cannot be found", ex.getMessage());
        }
    }

    @Test
    public void reportFindTransactions_OrderBy_TimeCreated_Ascending() throws ApiException {
        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Ascending)
                        .withPaging(1, 25)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
    }

    @Test
    public void reportFindTransactions_OrderBy_TimeCreated_Descending() throws ApiException {
        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 25)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
    }

    @Test
    public void reportFindTransactions_OrderBy_Status_Ascending() throws ApiException {
        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.Status, SortDirection.Ascending)
                        .withPaging(1, 25)
                        .execute("GpApiConfig");
        ;
        assertNotNull(transactions);
    }

    @Test
    public void reportFindTransactions_OrderBy_Status_Descending() throws ApiException {
        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.Status, SortDirection.Descending)
                        .withPaging(1, 25)
                        .execute("GpApiConfig");
        ;
        assertNotNull(transactions);
    }

    @Test
    public void reportFindTransactions_OrderBy_Type_Ascending() throws ApiException {
        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.Type, SortDirection.Ascending)
                        .withPaging(1, 25)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
    }

    @Test
    public void reportFindTransactions_OrderBy_Type_Descending() throws ApiException {
        TransactionSummaryList transactions =
                ReportingService
                        .findTransactions()
                        .orderBy(TransactionSortProperty.Type, SortDirection.Descending)
                        .withPaging(1, 25)
                        .execute("GpApiConfig");
        ;
        assertNotNull(transactions);
    }

    // ================================================================================
    // Settlement Transactions
    // ================================================================================
    @Test
    public void reportFindSettlementTransactions_By_StartDate_And_EndDate() throws ApiException {
        Date startDate = DateUtils.addDays(new Date(), -30);
        Date endDate = DateUtils.addDays(new Date(), -10);

        TransactionSummaryList transactions =
                ReportingService
                        .findSettlementTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions) {
            Date transactionDate = transactionSummary.getTransactionDate().toDate();
            assertTrue(DateUtils.isBeforeOrEquals(transactionDate, endDate));
            assertTrue(DateUtils.isAfterOrEquals(transactionDate, startDate));
        }
    }

    @Test
    public void reportFindSettlementTransactions_OrderBy_TimeCreated() throws ApiException {
        TransactionSummaryList transactions =
                ReportingService
                        .findSettlementTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .execute("GpApiConfig");

        assertNotNull(transactions);

        TransactionSummaryList transactionsAsc =
                ReportingService
                        .findSettlementTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Ascending)
                        .withPaging(1, 10)
                        .execute("GpApiConfig");

        assertNotNull(transactionsAsc);

        assertNotSame(transactions, transactionsAsc);
    }

    @Test
    public void reportFindSettlementTransactions_OrderBy_Status() throws ApiException {
        TransactionSummaryList transactions =
                ReportingService
                        .findSettlementTransactions()
                        .orderBy(TransactionSortProperty.Status, SortDirection.Descending)
                        .withPaging(1, 10)
                        .execute("GpApiConfig");

        assertNotNull(transactions);

        TransactionSummaryList transactionsAsc =
                ReportingService
                        .findSettlementTransactions()
                        .orderBy(TransactionSortProperty.Status, SortDirection.Ascending)
                        .withPaging(1, 10)
                        .execute("GpApiConfig");

        assertNotNull(transactionsAsc);

        assertNotSame(transactions, transactionsAsc);
    }

    @Test
    public void reportFindSettlementTransactions_OrderBy_Type() throws ApiException {
        TransactionSummaryList transactions =
                ReportingService
                        .findSettlementTransactions()
                        .orderBy(TransactionSortProperty.Type, SortDirection.Descending)
                        .withPaging(1, 10)
                        .execute("GpApiConfig");

        assertNotNull(transactions);

        TransactionSummaryList transactionsAsc =
                ReportingService
                        .findSettlementTransactions()
                        .orderBy(TransactionSortProperty.Type, SortDirection.Ascending)
                        .withPaging(1, 10)
                        .execute("GpApiConfig");

        assertNotNull(transactionsAsc);

        assertNotSame(transactions, transactionsAsc);
    }

    @Test
    public void reportFindSettlementTransactions_OrderBy_DepositId() throws ApiException {
        TransactionSummaryList transactions =
                ReportingService
                        .findSettlementTransactions()
                        .orderBy(TransactionSortProperty.DepositId, SortDirection.Descending)
                        .withPaging(1, 10)
                        .execute("GpApiConfig");

        assertNotNull(transactions);

        TransactionSummaryList transactionsAsc =
                ReportingService
                        .findSettlementTransactions()
                        .orderBy(TransactionSortProperty.DepositId, SortDirection.Ascending)
                        .withPaging(1, 10)
                        .execute("GpApiConfig");

        assertNotNull(transactionsAsc);

        assertNotSame(transactions, transactionsAsc);
    }

    @Test
    public void compareResults_reportFindSettlementTransactions_OrderBy_TypeAndTimeCreated() throws ApiException {
        TransactionSummaryList transactionsOrderedByTimeCreated =
                ReportingService
                        .findSettlementTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .execute("GpApiConfig");

        assertNotNull(transactionsOrderedByTimeCreated);

        TransactionSummaryList transactionsOrderedByType =
                ReportingService
                        .findSettlementTransactions()
                        .orderBy(TransactionSortProperty.Type, SortDirection.Descending)
                        .withPaging(1, 10)
                        .execute("GpApiConfig");

        assertNotNull(transactionsOrderedByType);

        assertNotSame(transactionsOrderedByTimeCreated, transactionsOrderedByType);
    }

    @Test
    public void reportFindSettlementTransactions_By_Number_First6_And_Number_Last4() throws ApiException {
        String number_first6 = "516075";
        String number_last4 = "4234";

        TransactionSummaryList transactions =
                ReportingService
                        .findSettlementTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.CardNumberFirstSix, number_first6)
                        .and(SearchCriteria.CardNumberLastFour, number_last4)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions) {
            assertTrue(transactionSummary.getMaskedCardNumber().startsWith(number_first6));
            assertTrue(transactionSummary.getMaskedCardNumber().endsWith(number_last4));
        }
    }

    //TODO - check assert if it's working
    @Test
    public void reportFindSettlementTransactions_FilterBy_AllDepositStatus() throws ApiException {
        for (DepositStatus depositStatus : DepositStatus.values()) {

            TransactionSummaryList transactions =
                    ReportingService
                            .findSettlementTransactions()
                            .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                            .withPaging(1, 10)
                            .where(SearchCriteria.DepositStatus, depositStatus)
                            .execute("GpApiConfig");

            assertNotNull(transactions);
            for (TransactionSummary transactionSummary : transactions)
                assertEquals(depositStatus.name(), transactionSummary.getDepositType());
        }
    }

    @Test
    public void reportFindSettlementTransactions_By_CardBrand() throws ApiException {
        String[] cardBrands = {"VISA", "MASTERCARD", "AMEX", "DINERS", "DISCOVER", "JCB", "CUP"};

        for (int index = 0; index < cardBrands.length; index++) {
            TransactionSummaryList transactions =
                    ReportingService
                            .findSettlementTransactions()
                            .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                            .withPaging(1, 10)
                            .where(SearchCriteria.CardBrand, cardBrands[index])
                            .execute("GpApiConfig");

            assertNotNull(transactions);
            for (TransactionSummary transactionSummary : transactions) {
                assertEquals(cardBrands[index], transactionSummary.getCardType());
            }
        }
    }

    //TODO - no error raised. empty list is received instead
    @Test
    public void reportFindSettlementTransactions_By_InvalidCardBrand() throws ApiException {
        String cardBrand = "MIT";

        try {
            ReportingService
                    .findSettlementTransactions()
                    .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                    .withPaging(1, 10)
                    .where(SearchCriteria.CardBrand, cardBrand)
                    .execute("GpApiConfig");

        } catch (GatewayException ex) {
            assertEquals("40097", ex.getResponseText());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("Status Code: 400 - Invalid Value provided in the input field - brand", ex.getMessage());
        }
    }

    @Test
    public void reportFindSettlementTransactions_By_ARN() throws ApiException {
        String arn = "71400011203688701840077";

        TransactionSummaryList transactions =
                ReportingService
                        .findSettlementTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.AquirerReferenceNumber, arn)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions)
            assertEquals(transactionSummary.getAcquirerReferenceNumber(), arn);
    }

    @Test
    public void reportFindSettlementTransactions_By_WrongARN() throws ApiException {
        String arn = "714000";

        TransactionSummaryList transactions =
                ReportingService
                        .findSettlementTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.AquirerReferenceNumber, arn)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        assertEquals(0, transactions.size());
    }

    @Test
    public void reportFindSettlementTransactions_By_BrandReference() throws ApiException {
        String brandReference = "MCS18V1EG3201";

        TransactionSummaryList transactions =
                ReportingService
                        .findSettlementTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.BrandReference, brandReference)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        assertEquals(1, transactions.size());
        for (TransactionSummary transactionSummary : transactions)
            assertEquals(brandReference, transactionSummary.getBrandReference());
    }

    @Test
    public void reportFindSettlementTransactions_By_WrongBrandReference() throws ApiException {
        String brandReference = "000000000000001";

        TransactionSummaryList transactions =
                ReportingService
                        .findSettlementTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.BrandReference, brandReference)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        assertEquals(0, transactions.size());
    }

    @Test
    public void reportFindSettlementTransactions_By_CardBrand_And_AuthCode() throws ApiException {
        String cardBrand = "VISA";
        String authCode = "008481";

        TransactionSummaryList transactions =
                ReportingService
                        .findSettlementTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.CardBrand, cardBrand)
                        .and(SearchCriteria.AuthCode, authCode)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        assertEquals(1, transactions.size());
        for (TransactionSummary transactionSummary : transactions) {
            assertEquals(cardBrand, transactionSummary.getCardType());
            assertEquals(authCode, transactionSummary.getAuthCode());
        }
    }

    @Test
    public void reportFindSettlementTransactions_By_Reference() throws ApiException {
        String referenceNumber = "28012076405M";

        TransactionSummaryList transactions =
                ReportingService
                        .findSettlementTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.ReferenceNumber, referenceNumber)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions)
            assertEquals(referenceNumber, transactionSummary.getReferenceNumber());
    }

    @Test
    public void reportFindSettlementTransactions_By_RandomReference() throws ApiException {
        String referenceNumber = UUID.randomUUID().toString();

        TransactionSummaryList transactions =
                ReportingService
                        .findSettlementTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.ReferenceNumber, referenceNumber)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        assertEquals(0, transactions.size());
    }

    @Test
    public void reportFindSettlementTransactions_By_AllStatus() throws ApiException {
        for (TransactionStatus transactionStatus : TransactionStatus.values()) {

            TransactionSummaryList transactions =
                    ReportingService
                            .findSettlementTransactions()
                            .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                            .withPaging(1, 10)
                            .where(SearchCriteria.TransactionStatus, transactionStatus)
                            .execute("GpApiConfig");

            assertNotNull(transactions);
            for (TransactionSummary transactionSummary : transactions)
                assertEquals(transactionStatus.getValue(), transactionSummary.getTransactionStatus());
        }
    }

    @Test
    public void reportFindSettlementTransactions_By_DepositId() throws ApiException {
        String depositId = "DEP_2342423423";

        TransactionSummaryList transactions =
                ReportingService
                        .findSettlementTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.DepositId, depositId)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions)
            assertEquals(transactionSummary.getDepositReference(), depositId);
    }

    @Test
    public void reportFindSettlementTransactions_By_RandomDepositId() throws ApiException {
        String depositId = UUID.randomUUID().toString();

        TransactionSummaryList transactions =
                ReportingService
                        .findSettlementTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.DepositId, depositId)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        assertEquals(0, transactions.size());
    }

    @Test
    public void reportFindSettlementTransactions_By_FromDepositTimeCreated_And_ToDepositTimeCreated() throws ApiException {
        Date startDate = DateUtils.addDays(new Date(), -30);
        Date endDate = DateUtils.addDays(new Date(), -1);

        TransactionSummaryList transactions =
                ReportingService
                        .findSettlementTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartDepositDate, startDate)
                        .and(DataServiceCriteria.EndDepositDate, endDate)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions) {
            Date depositDate = transactionSummary.getDepositDate();
            assertTrue(DateUtils.isBeforeOrEquals(depositDate, endDate));
            assertTrue(DateUtils.isAfterOrEquals(depositDate, startDate));
        }
    }

    @Test
    public void reportFindSettlementTransactions_By_FromBatchTimeCreated_And_ToBatchTimeCreated() throws ApiException {
        Date startDate = DateUtils.addDays(new Date(), -30);
        Date endDate = DateUtils.addDays(new Date(), -1);

        TransactionSummaryList transactions =
                ReportingService
                        .findSettlementTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartBatchDate, startDate)
                        .and(DataServiceCriteria.EndBatchDate, endDate)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions) {
            Date depositDate = transactionSummary.getDepositDate();
            assertTrue(DateUtils.isBeforeOrEquals(depositDate, endDate));
            assertTrue(DateUtils.isAfterOrEquals(depositDate, startDate));
        }
    }

    @Test
    public void reportFindSettlementTransactions_By_SystemMid_And_SystemHierarchy() throws ApiException {
        String merchantId = "101023947262";
        String hierarchy = "055-70-024-011-019";

        TransactionSummaryList transactions =
                ReportingService
                        .findSettlementTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.MerchantId, merchantId)
                        .and(DataServiceCriteria.SystemHierarchy, hierarchy)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions) {
            assertEquals(transactionSummary.getMerchantId(), merchantId);
            assertEquals(transactionSummary.getMerchantHierarchy(), hierarchy);
        }
    }

    @Test
    public void reportFindSettlementTransactions_By_NonExistent_SystemMid() throws ApiException {
        String merchantId = "000023940000";

        TransactionSummaryList transactions =
                ReportingService
                        .findSettlementTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.MerchantId, merchantId)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        assertEquals(0, transactions.size());
    }

    //TODO - NPE exception not handled; check asserts
    @Test
    public void reportFindSettlementTransactions_By_Invalid_SystemMid() throws ApiException {
        String merchantId = UUID.randomUUID().toString().replace("-", "");

        try {
            ReportingService
                    .findSettlementTransactions()
                    .where(DataServiceCriteria.MerchantId, merchantId)
                    .execute("GpApiConfig");

        } catch (GatewayException ex) {
            assertEquals("40097", ex.getResponseText());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("Status Code: 400 - Invalid Value provided in the input field - brand", ex.getMessage());
        }
    }

    @Test
    public void reportFindSettlementTransactions_By_Random_SystemHierarchy() throws ApiException {
        String hierarchy = "000-00-024-000-000";

        TransactionSummaryList transactions =
                ReportingService
                        .findSettlementTransactions()
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.SystemHierarchy, hierarchy)
                        .execute("GpApiConfig");

        assertNotNull(transactions);
        assertEquals(0, transactions.size());
    }

}
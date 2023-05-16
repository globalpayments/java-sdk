package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.reporting.DataServiceCriteria;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.entities.reporting.TransactionSummaryPaged;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.ReportingService;
import com.global.api.utils.DateUtils;
import com.global.api.utils.StringUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import static com.global.api.entities.enums.Target.GP_API;
import static org.junit.Assert.*;

public class GpApiReportingTransactionsTest extends BaseGpApiReportingTest {

    public GpApiReportingTransactionsTest() throws ApiException {
        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId(APP_ID)
                .setAppKey(APP_KEY);
        config.setEnableLogging(true);

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);
    }

    @Test
    public void ReportTransactionDetail_By_Id() throws ApiException {
        TransactionSummary sampleTransactionSummary =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .execute(GP_API_CONFIG_NAME)
                        .getResults()
                        .get(0);

        TransactionSummary transaction =
                ReportingService
                        .transactionDetail(sampleTransactionSummary.getTransactionId())
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transaction);
        assertEquals(sampleTransactionSummary.getTransactionId(), transaction.getTransactionId());
    }

    @Test
    public void ReportTransactionDetail_WrongId() throws ApiException {
        String transactionId = UUID.randomUUID().toString();

        boolean exceptionCaught = false;
        try {
            ReportingService
                    .transactionDetail(transactionId)
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("40118", ex.getResponseText());
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("Status Code: 404 - Transactions " + transactionId + " not found at this /ucp/transactions/" + transactionId + "", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void ReportTransactionDetail_NullId() throws ApiException {
        boolean exceptionCaught = false;
        try {
            ReportingService
                    .transactionDetail(null)
                    .execute(GP_API_CONFIG_NAME);
        } catch (BuilderException ex) {
            exceptionCaught = true;
            assertEquals("transactionId cannot be null for this transaction type.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void ReportFindTransactionsPaged_By_StartDate() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));
    }

    @Test
    public void ReportFindTransactionsPaged_By_StartDate_And_EndDate() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .and(SearchCriteria.EndDate, REPORTING_LAST_MONTH_DATE)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults()) {
            Date transactionDate = transactionSummary.getTransactionDate().toDate();
            assertTrue(DateUtils.isBeforeOrEquals(transactionDate, REPORTING_END_DATE));
            assertTrue(DateUtils.isAfterOrEquals(transactionDate, REPORTING_LAST_MONTH_DATE));
        }
    }

    @Test
    public void ReportFindTransactionsPaged_By_SameDates_StartDate_And_EndDate() throws ApiException {
        Date startDate = DateUtils.atStartOfDay(new Date());
        Date endDate = DateUtils.atEndOfDay(new Date());

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults()) {
            Date transactionDate = transactionSummary.getTransactionDate().toDate();
            assertTrue(DateUtils.isAfterOrEquals(transactionDate, startDate));
            assertTrue(DateUtils.isBeforeOrEquals(transactionDate, endDate));
        }
    }

    @Test
    public void ReportFindTransactionsPaged_OrderBy_TimeCreated() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));

        TransactionSummaryPaged transactionsAsc =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Ascending)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactionsAsc);
        for (TransactionSummary transactionSummary : transactionsAsc.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));

        assertNotSame(transactions, transactionsAsc);
    }

    //TODO - returning empty transaction list
    @Test
    public void ReportFindTransactionsPaged_OrderBy_Status() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.Status, SortDirection.Descending)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
    }

    @Test
    public void ReportFindTransactionsPaged_OrderBy_Type() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.Type, SortDirection.Descending)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
    }

    @Ignore // Although documentation allows order_by DEPOSIT_ID, the real endpoint does not.
    // TODO: Report error to GP-API team. Enable it when fixed.
    @Test
    public void ReportFindTransactionsPaged_OrderBy_DepositsId() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.DepositId, SortDirection.Descending)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
    }

    @Test
    public void CompareResults_reportFindTransactionsPaged_OrderBy_TypeAndTimeCreated() throws ApiException {
        TransactionSummaryPaged transactionsOrderedByTimeCreated =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactionsOrderedByTimeCreated);

        TransactionSummaryPaged transactionsOrderedByType =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.Type, SortDirection.Descending)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactionsOrderedByType);
        assertNotSame(transactionsOrderedByTimeCreated, transactionsOrderedByType);
    }

    @Test
    public void ReportFindTransactionsPaged_By_Id() throws ApiException {
        String transactionId =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .execute(GP_API_CONFIG_NAME)
                        .getResults()
                        .get(0)
                        .getTransactionId();
        assertNotNull(transactionId);

        TransactionSummaryPaged result =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .withTransactionId(transactionId)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(result);
        assertEquals(1, result.getResults().size());
        assertEquals(transactionId, result.getResults().get(0).getTransactionId());
    }

    @Test
    public void ReportFindTransactionsPaged_WrongId() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .withTransactionId("TRN_CQauJhxTXBvPGqIO66MJA3Rfk7V5PUa")
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        assertEquals(0, transactions.getResults().size());
    }

    @Test
    public void ReportFindTransactionsPaged_By_Type() throws ApiException {
        PaymentType paymentType = PaymentType.Sale;
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.PaymentType, paymentType)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertEquals(paymentType.getValue(), transactionSummary.getTransactionType());

        PaymentType refundPaymentType = PaymentType.Refund;
        TransactionSummaryPaged transactionsRefunded =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.PaymentType, refundPaymentType)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactionsRefunded);
        for (TransactionSummary transactionSummary : transactionsRefunded.getResults())
            assertEquals(refundPaymentType.getValue(), transactionSummary.getTransactionType());
        assertNotSame(transactions, transactionsRefunded);
    }

    @Test
    public void ReportFindTransactionsPaged_By_Amount_And_Currency_And_Country() throws ApiException {
        BigDecimal amount = new BigDecimal("1.12");
        String currency = "aud"; //This is case-sensitive
        String country = "AU"; //This is case-sensitive

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(DataServiceCriteria.Amount, amount)
                        .and(DataServiceCriteria.Currency, currency)
                        .and(DataServiceCriteria.Country, country)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults()) {
            assertEquals(StringUtils.toNumeric(amount), transactionSummary.getAmount().toString());
            assertEquals(currency, transactionSummary.getCurrency());
            assertEquals(country, transactionSummary.getCountry());
        }
    }

    @Test
    public void ReportFindTransactionsPaged_By_WrongCurrency() throws ApiException {
        String currency = "AAA";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(DataServiceCriteria.Currency, currency)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        assertEquals(0, transactions.getResults().size());
    }

    @Test
    public void ReportFindTransactionsPaged_By_Channel() throws ApiException {
        Channel channel = Channel.CardNotPresent;

        TransactionSummaryPaged transactionsCNP =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.Channel, channel)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactionsCNP);
        for (TransactionSummary transactionSummary : transactionsCNP.getResults())
            assertEquals(channel.getValue(), transactionSummary.getChannel());

        Channel channelCP = Channel.CardPresent;
        TransactionSummaryPaged transactionsCP =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.Channel, channelCP)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactionsCP);
        for (TransactionSummary transactionSummary : transactionsCP.getResults())
            assertEquals(channelCP.getValue(), transactionSummary.getChannel());

        assertNotSame(transactionsCNP, transactionsCP);
    }

    @Ignore
    // TODO: Reported to GP-API team. Enable when fixed.
    // GP-API returns SUCCESS_AUTHENTICATED an NOT_AUTHENTICATED instead of just AUTHENTICATED
    // when searching by TransactionStatus = AUTHENTICATED
    @Test
    public void ReportFindTransactionsPaged_By_AllStatus() throws ApiException {
        for (TransactionStatus transactionStatus : TransactionStatus.values()) {
            TransactionSummaryPaged transactions =
                    ReportingService
                            .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                            .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                            .where(SearchCriteria.TransactionStatus, transactionStatus)
                            .execute(GP_API_CONFIG_NAME);
            assertNotNull(transactions);
            for (TransactionSummary transactionSummary : transactions.getResults())
                if (transactionStatus.equals(TransactionStatus.Authenticated)) {
                    assertEquals("SUCCESS_AUTHENTICATED", transactionSummary.getTransactionStatus());
                } else {
                    assertEquals(transactionStatus.getValue(), transactionSummary.getTransactionStatus());
                }
        }
    }

    @Test
    public void ReportFindTransactionsPaged_By_Status() throws ApiException {
        TransactionStatus transactionStatus = TransactionStatus.Preauthorized;

        TransactionSummaryPaged transactionsInitiated =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.TransactionStatus, transactionStatus)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactionsInitiated);
        for (TransactionSummary transactionSummary : transactionsInitiated.getResults())
            assertEquals(transactionStatus.getValue(), transactionSummary.getTransactionStatus());

        TransactionStatus transactionStatusRejected = TransactionStatus.Rejected;

        TransactionSummaryPaged transactionsRejected =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.TransactionStatus, transactionStatusRejected)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactionsRejected);
        for (TransactionSummary transactionSummary : transactionsRejected.getResults())
            assertEquals(transactionStatusRejected.getValue(), transactionSummary.getTransactionStatus());
        assertNotSame(transactionsInitiated, transactionsRejected);
    }

    @Test
    public void ReportFindTransactionsPaged_By_CardBrand_And_AuthCode() throws ApiException {
        String cardBrand = "VISA";
        String authCode = "12345";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.CardBrand, cardBrand)
                        .and(SearchCriteria.AuthCode, authCode)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults()) {
            assertEquals(cardBrand, transactionSummary.getCardType());
            assertEquals(authCode, transactionSummary.getAuthCode());
        }
    }

    //Diners and JCB cards are not working
    @Test
    public void ReportFindTransactionsPaged_By_CardBrand() throws ApiException {
        String[] cardBrands = {"VISA", "MASTERCARD", "AMEX", "DINERS", "DISCOVER", "JCB", "CUP"};

        for (String cardBrand : cardBrands) {
            // Although documentation allows DINERS and JCB values, the real endpoint does not.
            // TODO: Report error to GP-API team. Enable it when fixed.
            if (("DINERS").equals(cardBrand) || "JCB".equals(cardBrand))
                continue;

            TransactionSummaryPaged transactions =
                    ReportingService
                            .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                            .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                            .where(SearchCriteria.CardBrand, cardBrand)
                            .execute(GP_API_CONFIG_NAME);
            assertNotNull(transactions);
            for (TransactionSummary transactionSummary : transactions.getResults()) {
                assertEquals(cardBrand, transactionSummary.getCardType());
            }
        }
    }

    @Test
    public void ReportFindTransactionsPaged_By_InvalidCardBrand() throws ApiException {
        String cardBrand = "MIT";

        boolean exceptionCaught = false;
        try {
            ReportingService
                    .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                    .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                    .where(SearchCriteria.CardBrand, cardBrand)
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("40097", ex.getResponseText());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("Status Code: 400 - Invalid Value provided in the input field - brand", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void ReportFindTransactionsPaged_By_Reference() throws ApiException {
        String referenceNumber = "98f64385-6fcd-4605-b0ae-c5675be681cf";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.ReferenceNumber, referenceNumber)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertEquals(referenceNumber, transactionSummary.getReferenceNumber());
    }

    @Test
    public void ReportFindTransactionsPaged_By_RandomReference() throws ApiException {
        String referenceNumber = UUID.randomUUID().toString();

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.ReferenceNumber, referenceNumber)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        assertEquals(0, transactions.getResults().size());
    }

    @Test
    public void ReportFindTransactionsPaged_By_BrandReference() throws ApiException {
        String brandReference = "300351293234459";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.BrandReference, brandReference)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertEquals(brandReference, transactionSummary.getBrandReference());
    }

    @Test
    public void ReportFindTransactionsPaged_By_WrongBrandReference() throws ApiException {
        String brandReference = "000000000000001";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.BrandReference, brandReference)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        assertEquals(0, transactions.getResults().size());
    }

    @Ignore
    // Although requests are done with &entry_mode set properly, the real endpoint returns transactions with other entry_modes.
    // TODO: Report error to GP-API team. Enable it when fixed.
    @Test
    public void ReportFindTransactionsPaged_By_AllEntryModes() throws ApiException {
        for (PaymentEntryMode paymentEntryMode : PaymentEntryMode.values()) {

            TransactionSummaryPaged transactions =
                    ReportingService
                            .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                            .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                            .where(SearchCriteria.PaymentEntryMode, paymentEntryMode)
                            .execute(GP_API_CONFIG_NAME);
            assertNotNull(transactions);
            for (TransactionSummary transactionSummary : transactions.getResults())
                assertEquals(paymentEntryMode.getValue(), transactionSummary.getEntryMode());
        }
    }

    @Test
    public void ReportFindTransactionsPaged_By_EntryMode() throws ApiException {
        PaymentEntryMode paymentEntryMode = PaymentEntryMode.Ecom;

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.PaymentEntryMode, paymentEntryMode)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertEquals(paymentEntryMode.getValue(), transactionSummary.getEntryMode());

        PaymentEntryMode paymentEntryModeMoto = PaymentEntryMode.Moto;

        TransactionSummaryPaged transactionsMoto =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.PaymentEntryMode, paymentEntryModeMoto)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactionsMoto);
        for (TransactionSummary transactionSummary : transactionsMoto.getResults())
            assertEquals(paymentEntryModeMoto.getValue(), transactionSummary.getEntryMode());
        assertNotSame(transactions, transactionsMoto);
    }

    @Test
    public void ReportFindTransactionsPaged_By_Number_First6_And_Number_Last4() throws ApiException {
        String number_first6 = "543458";
        String number_last4 = "7652";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.CardNumberFirstSix, number_first6)
                        .and(SearchCriteria.CardNumberLastFour, number_last4)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults()) {
            assertTrue(transactionSummary.getMaskedCardNumber().startsWith(number_first6));
            assertTrue(transactionSummary.getMaskedCardNumber().endsWith(number_last4));
        }
    }

    @Test
    public void ReportFindTransactionsPaged_By_Token_First6_And_Token_Last4() throws ApiException {
        String token_first6 = "516730";
        String token_last4 = "5507";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.TokenFirstSix, token_first6)
                        .and(SearchCriteria.TokenLastFour, token_last4)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults()) {
            String maskedCardNumber = transactionSummary.getMaskedCardNumber();
            if (maskedCardNumber != null) {
                assertTrue(maskedCardNumber.startsWith(token_first6));
                assertTrue(maskedCardNumber.endsWith(token_last4));
            }
        }
    }

    @Test
    public void ReportFindTransactionsPaged_By_Token_First6_And_Token_Last4_WithPaymentMethod() throws ApiException {
        String token_first6 = "516730";
        String token_last4 = "5507";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.TokenFirstSix, token_first6)
                        .and(SearchCriteria.TokenLastFour, token_last4)
                        .and(SearchCriteria.PaymentMethodName, PaymentMethodName.DigitalWallet)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults()) {
            String maskedCardNumber = transactionSummary.getMaskedCardNumber();
            if (maskedCardNumber != null) {
                assertTrue(maskedCardNumber.startsWith(token_first6));
                assertTrue(maskedCardNumber.endsWith(token_last4));
            }
        }
    }

    @Test
    public void ReportFindTransactionsPaged_By_Token_First6_And_Token_Last4_WithWrongPaymentMethod() throws ApiException {
        String token_first6 = "516730";
        String token_last4 = "5507";

        boolean exceptionCaught = false;
        try {
            ReportingService.findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                    .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                    .where(SearchCriteria.TokenFirstSix, token_first6)
                    .and(SearchCriteria.TokenLastFour, token_last4)
                    .and(SearchCriteria.PaymentMethodName, PaymentMethodName.Card)
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("40043", ex.getResponseText());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals(
                    "Status Code: 400 - Request contains unexpected fields: payment_method",
                    ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void ReportFindTransactionsPaged_By_BatchId() throws ApiException {
        String batchId = "BAT_875461";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.BatchId, batchId)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertEquals(batchId, transactionSummary.getBatchSequenceNumber());
    }

    @Test
    public void ReportFindTransactionsPaged_By_WrongBatchId() throws ApiException {
        String batchId = "BAT_000461";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.BatchId, batchId)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        assertEquals(0, transactions.getResults().size());
    }

    @Test
    public void ReportFindTransactionsPaged_By_Name() throws ApiException {
        String name = "James Mason";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.Name, name)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertEquals(name, transactionSummary.getCardHolderName());
    }

    @Test
    public void ReportFindTransactionsPaged_By_RandomName() throws ApiException {
        String name = UUID.randomUUID().toString().replace("-", "");

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.Name, name)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        assertEquals(0, transactions.getResults().size());
    }

    @Test
    public void ReportFindTransactionsPaged_WithInvalid_AccountName() throws ApiException {
        boolean exceptionCaught = false;
        try {
            ReportingService
                    .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                    .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                    .where(SearchCriteria.AccountName, "VISA")
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("40003", ex.getResponseText());
            assertEquals("ACTION_NOT_AUTHORIZED", ex.getResponseCode());
            assertEquals("Status Code: 400 - Token does not match account_id or account_name in the request", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Ignore // Although documentation indicates from_time_created is required, the real endpoint returns results.
    // TODO: Report error to GP-API team. Enable it when fixed.
    @Test
    public void ReportFindTransactionsPaged_Without_Mandatory_StartDate() throws ApiException {
        boolean exceptionCaught = false;
        try {
            ReportingService
                    .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("40008", ex.getResponseText());
            assertEquals("TRANSACTION_NOT_FOUND", ex.getResponseCode());
            assertEquals("Status Code: 404 - Transaction to action cannot be found", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void ReportFindTransactionsPaged_By_PaymentMethod() throws ApiException {
        for (PaymentMethodName paymentMethodName : PaymentMethodName.values()) {
            TransactionSummaryPaged transactions =
                    ReportingService
                            .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                            .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                            .where(SearchCriteria.PaymentMethodName, paymentMethodName)
                            .execute(GP_API_CONFIG_NAME);
            assertNotNull(transactions);
        }
    }

    @Test
    public void CompareResults_ReportFindTransactionsPaged_OrderBy_TypeAndTimeCreated() throws ApiException {
        TransactionSummaryPaged resultByTimeCreated =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .and(SearchCriteria.EndDate, REPORTING_END_DATE)
                        .execute(GP_API_CONFIG_NAME);

        TransactionSummaryPaged resultByType =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.Type, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .and(SearchCriteria.EndDate, REPORTING_END_DATE)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(resultByTimeCreated.getResults());
        assertTrue(resultByTimeCreated.getResults().size() > 0);
        assertNotNull(resultByType.getResults());
        assertTrue(resultByType.getResults().size() > 0);
        assertNotEquals(resultByTimeCreated.getResults(), resultByType.getResults());
    }

    @Test
    public void ReportFindTransactionsPaged_OrderBy_TimeCreated_Ascending() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Ascending)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));
    }

    @Test
    public void ReportFindTransactionsPaged_OrderBy_TimeCreated_Descending() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));
    }

    @Test
    public void ReportFindTransactionsPaged_OrderBy_Id_Ascending() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.Id, SortDirection.Ascending)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));
    }

    @Test
    public void ReportFindTransactionsPaged_OrderBy_Id_Descending() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.Id, SortDirection.Descending)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));
    }

    @Test
    public void ReportFindTransactionsPaged_OrderBy_Type_Ascending() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.Type, SortDirection.Ascending)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));
    }

    @Test
    public void ReportFindTransactionsPaged_OrderBy_Type_Descending() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.Type, SortDirection.Descending)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));
    }

    // ================================================================================
    // Settlement Transactions
    // ================================================================================
    @Test
    public void ReportFindSettlementTransactionsPaged_By_StartDate_And_EndDate() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .and(SearchCriteria.EndDate, REPORTING_END_DATE)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults()) {
            Date transactionDate = transactionSummary.getTransactionDate().toDate();
            assertTrue(DateUtils.isBeforeOrEquals(transactionDate, REPORTING_END_DATE));
            assertTrue(DateUtils.isAfterOrEquals(transactionDate, REPORTING_START_DATE));
        }
    }

    @Test
    public void ReportFindSettlementTransactionsPaged_OrderBy_TimeCreated() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));

        TransactionSummaryPaged transactionsAsc =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Ascending)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactionsAsc);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));
        assertNotSame(transactions, transactionsAsc);
    }

    @Test
    public void ReportFindSettlementTransactionsPaged_OrderBy_Status() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.Status, SortDirection.Descending)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));

        TransactionSummaryPaged transactionsAsc =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.Status, SortDirection.Ascending)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactionsAsc);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));
        assertNotSame(transactions, transactionsAsc);
    }

    @Test
    public void ReportFindSettlementTransactionsPaged_OrderBy_Type() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.Type, SortDirection.Descending)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));

        TransactionSummaryPaged transactionsAsc =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.Type, SortDirection.Ascending)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactionsAsc);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));
        assertNotSame(transactions, transactionsAsc);
    }

    @Test
    public void ReportFindSettlementTransactionsPaged_OrderBy_DepositId() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.DepositId, SortDirection.Descending)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));

        TransactionSummaryPaged transactionsAsc =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.DepositId, SortDirection.Ascending)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactionsAsc);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));
        assertNotSame(transactions, transactionsAsc);
    }

    @Test
    public void CompareResults_ReportFindSettlementTransactionsPaged_OrderBy_TypeAndTimeCreated() throws ApiException {
        TransactionSummaryPaged transactionsOrderedByTimeCreated =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactionsOrderedByTimeCreated);
        for (TransactionSummary transactionSummary : transactionsOrderedByTimeCreated.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));

        TransactionSummaryPaged transactionsOrderedByType =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.Type, SortDirection.Descending)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactionsOrderedByType);
        for (TransactionSummary transactionSummary : transactionsOrderedByType.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));
        assertNotSame(transactionsOrderedByTimeCreated, transactionsOrderedByType);
    }

    @Test
    public void ReportFindSettlementTransactionsPaged_By_Number_First6_And_Number_Last4() throws ApiException {
        String number_first6 = "543458";
        String number_last4 = "7652";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.CardNumberFirstSix, number_first6)
                        .and(SearchCriteria.CardNumberLastFour, number_last4)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults()) {
            assertTrue(transactionSummary.getMaskedCardNumber().startsWith(number_first6));
            assertTrue(transactionSummary.getMaskedCardNumber().endsWith(number_last4));
        }
    }

    @Test
    public void ReportFindSettlementTransactionsPaged_FilterBy_AllDepositStatus() throws ApiException {
        for (DepositStatus depositStatus : DepositStatus.values()) {

            TransactionSummaryPaged transactions =
                    ReportingService
                            .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                            .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                            .where(SearchCriteria.DepositStatus, depositStatus)
                            .execute(GP_API_CONFIG_NAME);
            assertNotNull(transactions);
            for (TransactionSummary transactionSummary : transactions.getResults())
                assertEquals(depositStatus.getValue(GP_API), transactionSummary.getDepositStatus());
        }
    }

    @Test
    public void ReportFindSettlementTransactionsPaged_By_CardBrand() throws ApiException {
        String[] cardBrands = {"VISA", "MASTERCARD", "AMEX", "DINERS", "DISCOVER", "JCB", "CUP"};

        for (String cardBrand : cardBrands) {
            TransactionSummaryPaged transactions =
                    ReportingService
                            .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                            .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                            .where(SearchCriteria.CardBrand, cardBrand)
                            .execute(GP_API_CONFIG_NAME);
            assertNotNull(transactions);
            for (TransactionSummary transactionSummary : transactions.getResults()) {
                assertEquals(cardBrand, transactionSummary.getCardType());
            }
        }
    }

    @Test
    public void ReportFindSettlementTransactionsPaged_By_InvalidCardBrand() throws ApiException {
        String cardBrand = "MIT";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .and(SearchCriteria.CardBrand, cardBrand)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        assertEquals(0, transactions.getResults().size());
    }

    @Test
    public void ReportFindSettlementTransactionsPaged_By_ARN() throws ApiException {
        String arn = "74500010037624410827759";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.AquirerReferenceNumber, arn)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertEquals(transactionSummary.getAcquirerReferenceNumber(), arn);
    }

    @Test
    public void ReportFindSettlementTransactionsPaged_By_WrongARN() throws ApiException {
        String arn = "00000010037624410827527";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.AquirerReferenceNumber, arn)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        assertEquals(0, transactions.getResults().size());
    }

    @Test
    public void ReportFindSettlementTransactionsPaged_By_BrandReference() throws ApiException {
        String brandReference = "MCF1CZ5ME5405";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.BrandReference, brandReference)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        assertEquals(1, transactions.getResults().size());
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertEquals(brandReference, transactionSummary.getBrandReference());
    }

    @Test
    public void ReportFindSettlementTransactionsPaged_By_WrongBrandReference() throws ApiException {
        String brandReference = "000000000000001";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.BrandReference, brandReference)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        assertEquals(0, transactions.getResults().size());
    }

    @Test
    public void ReportFindSettlementTransactionsPaged_By_CardBrand_And_AuthCode() throws ApiException {
        String cardBrand = "MASTERCARD";
        String authCode = "028010";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.CardBrand, cardBrand)
                        .and(SearchCriteria.AuthCode, authCode)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        assertEquals(1, transactions.getResults().size());
        for (TransactionSummary transactionSummary : transactions.getResults()) {
            assertEquals(cardBrand, transactionSummary.getCardType());
            assertEquals(authCode, transactionSummary.getAuthCode());
        }
    }

    @Test
    public void ReportFindSettlementTransactionsPaged_By_Reference() throws ApiException {
        String referenceNumber = "50080513769";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.ReferenceNumber, referenceNumber)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertEquals(referenceNumber, transactionSummary.getReferenceNumber());
    }

    @Test
    public void ReportFindSettlementTransactionsPaged_By_RandomReference() throws ApiException {
        String referenceNumber = UUID.randomUUID().toString();

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.ReferenceNumber, referenceNumber)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        assertEquals(0, transactions.getResults().size());
    }

    @Test
    public void ReportFindSettlementTransactionsPaged_By_AllStatus() throws ApiException {
        for (TransactionStatus transactionStatus : TransactionStatus.values()) {

            TransactionSummaryPaged transactions =
                    ReportingService
                            .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                            .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                            .where(SearchCriteria.TransactionStatus, transactionStatus)
                            .execute(GP_API_CONFIG_NAME);

            assertNotNull(transactions);
            for (TransactionSummary transactionSummary : transactions.getResults())
                assertEquals(transactionStatus.getValue(), transactionSummary.getTransactionStatus());
        }
    }

    @Test
    public void ReportFindSettlementTransactionsPaged_By_DepositId() throws ApiException {
        String depositId = "DEP_2342423423";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withDepositReference(depositId)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertEquals(depositId, transactionSummary.getDepositReference());
    }

    @Test
    public void ReportFindSettlementTransactionsPaged_By_RandomDepositId() throws ApiException {
        String depositId = UUID.randomUUID().toString();

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withDepositReference(depositId)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        assertEquals(0, transactions.getResults().size());
    }

    @Test
    public void ReportFindSettlementTransactionsPaged_By_FromDepositTimeCreated_And_ToDepositTimeCreated() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(DataServiceCriteria.StartDepositDate, REPORTING_START_DATE)
                        .and(DataServiceCriteria.EndDepositDate, REPORTING_END_DATE)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults()) {
            Date depositDate = transactionSummary.getDepositDate();
            assertTrue(DateUtils.isBeforeOrEquals(depositDate, REPORTING_END_DATE));
            assertTrue(DateUtils.isAfterOrEquals(depositDate, REPORTING_START_DATE));
        }
    }

    @Test
    public void ReportFindSettlementTransactionsPaged_By_FromBatchTimeCreated_And_ToBatchTimeCreated() throws ApiException {
        Date startDate = DateUtils.addDays(new Date(), -90);
        Date startBatchDate = DateUtils.addDays(new Date(), -89);
        Date endBatchDate = DateUtils.addDays(new Date(), -1);

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(DataServiceCriteria.StartBatchDate, startBatchDate)
                        .and(DataServiceCriteria.EndBatchDate, endBatchDate)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults()) {
            Date transactionDate = transactionSummary.getTransactionDate().toDate();
            assertTrue(DateUtils.isAfterOrEquals(transactionDate, startBatchDate));
            assertTrue(DateUtils.isBeforeOrEquals(transactionDate, endBatchDate));
        }
    }

    @Test
    public void ReportFindSettlementTransactionsPaged_By_SystemMid_And_SystemHierarchy() throws ApiException {
        String merchantId = "101023947262";
        String hierarchy = "055-70-024-011-019";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(DataServiceCriteria.MerchantId, merchantId)
                        .and(DataServiceCriteria.SystemHierarchy, hierarchy)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults()) {
            assertEquals(merchantId, transactionSummary.getMerchantId());
            assertEquals(hierarchy, transactionSummary.getMerchantHierarchy());
        }
    }

    @Test
    public void ReportFindSettlementTransactionsPaged_By_NonExistent_SystemMerchantId() throws ApiException {
        String merchantId = String.valueOf(new Random().nextInt(999999999));

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(DataServiceCriteria.MerchantId, merchantId)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        assertEquals(0, transactions.getResults().size());
    }

    @Test
    public void ReportFindSettlementTransactionsPaged_By_Invalid_SystemMid() throws ApiException {
        boolean exceptionCaught = false;
        try {
            ReportingService
                    .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                    .where(DataServiceCriteria.MerchantId, UUID.randomUUID().toString())
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40090", ex.getResponseText());
            assertEquals("Status Code: 400 - system.mid value is invalid. Please check the format and data provided is correct", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void ReportFindSettlementTransactionsPaged_By_Random_SystemHierarchy() throws ApiException {
        String systemHierarchy = "000-00-024-000-000";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(DataServiceCriteria.SystemHierarchy, systemHierarchy)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(transactions);
        assertEquals(0, transactions.getResults().size());
    }

}
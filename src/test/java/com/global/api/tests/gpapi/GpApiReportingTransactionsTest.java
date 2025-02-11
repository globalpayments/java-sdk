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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import static com.global.api.entities.enums.Target.GP_API;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GpApiReportingTransactionsTest extends BaseGpApiReportingTest {

    public GpApiReportingTransactionsTest() throws ApiException {
        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, null);
        ServicesContainer.configureService(config);
    }

    @Test
    @Order(1)
    public void ReportTransactionDetail_By_Id() throws ApiException {
        TransactionSummary sampleTransactionSummary =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .execute()
                        .getResults()
                        .get(0);

        TransactionSummary transaction =
                ReportingService
                        .transactionDetail(sampleTransactionSummary.getTransactionId())
                        .execute();
        assertNotNull(transaction);
        assertEquals(sampleTransactionSummary.getTransactionId(), transaction.getTransactionId());
    }

    @Test
    @Order(2)
    public void ReportTransactionDetail_WrongId() throws ApiException {
        String transactionId = UUID.randomUUID().toString();

        boolean exceptionCaught = false;
        try {
            ReportingService
                    .transactionDetail(transactionId)
                    .execute();
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
    @Order(3)
    public void ReportTransactionDetail_NullId() throws ApiException {
        boolean exceptionCaught = false;
        try {
            ReportingService
                    .transactionDetail(null)
                    .execute();
        } catch (BuilderException ex) {
            exceptionCaught = true;
            assertEquals("transactionId cannot be null for this transaction type.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(4)
    public void ReportFindTransactionsPaged_By_StartDate() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));
    }

    @Test
    @Order(5)
    public void ReportFindTransactionsPaged_By_StartDate_And_EndDate() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .and(SearchCriteria.EndDate, REPORTING_LAST_MONTH_DATE)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults()) {
            Date transactionDate = transactionSummary.getTransactionDate().toDate();
            assertTrue(DateUtils.isBeforeOrEquals(transactionDate, REPORTING_END_DATE));
            assertTrue(DateUtils.isAfterOrEquals(transactionDate, REPORTING_LAST_MONTH_DATE));
        }
    }

    @Test
    @Order(6)
    public void ReportFindTransactionsPaged_By_SameDates_StartDate_And_EndDate() throws ApiException {
        Date startDate = DateUtils.atStartOfDay(new Date());
        Date endDate = DateUtils.atEndOfDay(new Date());

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults()) {
            Date transactionDate = transactionSummary.getTransactionDate().toDate();
            assertTrue(DateUtils.isAfterOrEquals(transactionDate, startDate));
            assertTrue(DateUtils.isBeforeOrEquals(transactionDate, endDate));
        }
    }

    @Test
    @Order(7)
    public void ReportFindTransactionsPaged_OrderBy_TimeCreated() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));

        TransactionSummaryPaged transactionsAsc =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Ascending)
                        .execute();
        assertNotNull(transactionsAsc);
        for (TransactionSummary transactionSummary : transactionsAsc.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));

        assertNotSame(transactions, transactionsAsc);
    }

    //TODO - returning empty transaction list
    @Test
    @Order(8)
    public void ReportFindTransactionsPaged_OrderBy_Status() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.Status, SortDirection.Descending)
                        .execute();
        assertNotNull(transactions);
    }

    @Test
    @Order(9)
    public void ReportFindTransactionsPaged_OrderBy_Type() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.Type, SortDirection.Descending)
                        .execute();
        assertNotNull(transactions);
    }

    @Disabled // Although documentation allows order_by DEPOSIT_ID, the real endpoint does not.
    // TODO: Report error to GP-API team. Enable it when fixed.
    @Test
    @Order(10)
    public void ReportFindTransactionsPaged_OrderBy_DepositsId() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.DepositId, SortDirection.Descending)
                        .execute();
        assertNotNull(transactions);
    }

    @Test
    @Order(11)
    public void CompareResults_reportFindTransactionsPaged_OrderBy_TypeAndTimeCreated() throws ApiException {
        TransactionSummaryPaged transactionsOrderedByTimeCreated =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .execute();
        assertNotNull(transactionsOrderedByTimeCreated);

        TransactionSummaryPaged transactionsOrderedByType =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.Type, SortDirection.Descending)
                        .execute();
        assertNotNull(transactionsOrderedByType);
        assertNotSame(transactionsOrderedByTimeCreated, transactionsOrderedByType);
    }

    @Test
    @Order(12)
    public void ReportFindTransactionsPaged_By_Id() throws ApiException {
        String transactionId =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .execute()
                        .getResults()
                        .get(0)
                        .getTransactionId();
        assertNotNull(transactionId);

        TransactionSummaryPaged result =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .withTransactionId(transactionId)
                        .execute();
        assertNotNull(result);
        assertEquals(1, result.getResults().size());
        assertEquals(transactionId, result.getResults().get(0).getTransactionId());
    }

    @Test
    @Order(13)
    public void ReportFindTransactionsPaged_WrongId() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .withTransactionId("TRN_CQauJhxTXBvPGqIO66MJA3Rfk7V5PUa")
                        .execute();
        assertNotNull(transactions);
        assertEquals(0, transactions.getResults().size());
    }

    @Test
    @Order(14)
    public void ReportFindTransactionsPaged_By_Type() throws ApiException {
        PaymentType paymentType = PaymentType.Sale;
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.PaymentType, paymentType)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertEquals(paymentType.getValue(), transactionSummary.getTransactionType());

        PaymentType refundPaymentType = PaymentType.Refund;
        TransactionSummaryPaged transactionsRefunded =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.PaymentType, refundPaymentType)
                        .execute();
        assertNotNull(transactionsRefunded);
        for (TransactionSummary transactionSummary : transactionsRefunded.getResults())
            assertEquals(refundPaymentType.getValue(), transactionSummary.getTransactionType());
        assertNotSame(transactions, transactionsRefunded);
    }

    @Test
    @Order(15)
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
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults()) {
            assertEquals(StringUtils.toNumeric(amount), transactionSummary.getAmount().toString());
            assertEquals(currency, transactionSummary.getCurrency());
            assertEquals(country, transactionSummary.getCountry());
        }
    }

    @Test
    @Order(16)
    public void ReportFindTransactionsPaged_By_WrongCurrency() throws ApiException {
        String currency = "AAA";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(DataServiceCriteria.Currency, currency)
                        .execute();
        assertNotNull(transactions);
        assertEquals(0, transactions.getResults().size());
    }

    @Test
    @Order(17)
    public void ReportFindTransactionsPaged_By_Channel() throws ApiException {
        Channel channel = Channel.CardNotPresent;

        TransactionSummaryPaged transactionsCNP =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.Channel, channel)
                        .execute();
        assertNotNull(transactionsCNP);
        for (TransactionSummary transactionSummary : transactionsCNP.getResults())
            assertEquals(channel.getValue(), transactionSummary.getChannel());

        Channel channelCP = Channel.CardPresent;
        TransactionSummaryPaged transactionsCP =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.Channel, channelCP)
                        .execute();
        assertNotNull(transactionsCP);
        for (TransactionSummary transactionSummary : transactionsCP.getResults())
            assertEquals(channelCP.getValue(), transactionSummary.getChannel());

        assertNotSame(transactionsCNP, transactionsCP);
    }

    @Disabled
    // TODO: Reported to GP-API team. Enable when fixed.
    // GP-API returns SUCCESS_AUTHENTICATED an NOT_AUTHENTICATED instead of just AUTHENTICATED
    // when searching by TransactionStatus = AUTHENTICATED
    @Test
    @Order(18)
    public void ReportFindTransactionsPaged_By_AllStatus() throws ApiException {
        for (TransactionStatus transactionStatus : TransactionStatus.values()) {
            TransactionSummaryPaged transactions =
                    ReportingService
                            .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                            .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                            .where(SearchCriteria.TransactionStatus, transactionStatus)
                            .execute();
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
    @Order(19)
    public void ReportFindTransactionsPaged_By_Status() throws ApiException {
        TransactionStatus transactionStatus = TransactionStatus.Preauthorized;

        TransactionSummaryPaged transactionsInitiated =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.TransactionStatus, transactionStatus)
                        .execute();
        assertNotNull(transactionsInitiated);
        for (TransactionSummary transactionSummary : transactionsInitiated.getResults())
            assertEquals(transactionStatus.getValue(), transactionSummary.getTransactionStatus());

        TransactionStatus transactionStatusRejected = TransactionStatus.Rejected;

        TransactionSummaryPaged transactionsRejected =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.TransactionStatus, transactionStatusRejected)
                        .execute();
        assertNotNull(transactionsRejected);
        for (TransactionSummary transactionSummary : transactionsRejected.getResults())
            assertEquals(transactionStatusRejected.getValue(), transactionSummary.getTransactionStatus());
        assertNotSame(transactionsInitiated, transactionsRejected);
    }

    @Test
    @Order(20)
    public void ReportFindTransactionsPaged_By_CardBrand_And_AuthCode() throws ApiException {
        String cardBrand = "VISA";
        String authCode = "123456";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.CardBrand, cardBrand)
                        .and(SearchCriteria.AuthCode, authCode)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults()) {
            assertEquals(cardBrand, transactionSummary.getCardType());
            assertTrue(transactionSummary.getAuthCode().contains(authCode));
        }
    }

    //Diners and JCB cards are not working
    @Test
    @Order(21)
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
                            .execute();
            assertNotNull(transactions);
            for (TransactionSummary transactionSummary : transactions.getResults()) {
                assertEquals(cardBrand, transactionSummary.getCardType());
            }
        }
    }

    @Test
    @Order(22)
    public void ReportFindTransactionsPaged_By_InvalidCardBrand() throws ApiException {
        String cardBrand = "MIT";

        boolean exceptionCaught = false;
        try {
            ReportingService
                    .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                    .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                    .where(SearchCriteria.CardBrand, cardBrand)
                    .execute();
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
    @Order(23)
    public void ReportFindTransactionsPaged_By_Reference() throws ApiException {
        String referenceNumber = "98f64385-6fcd-4605-b0ae-c5675be681cf";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.ReferenceNumber, referenceNumber)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertEquals(referenceNumber, transactionSummary.getReferenceNumber());
    }

    @Test
    @Order(24)
    public void ReportFindTransactionsPaged_By_RandomReference() throws ApiException {
        String referenceNumber = UUID.randomUUID().toString();

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.ReferenceNumber, referenceNumber)
                        .execute();
        assertNotNull(transactions);
        assertEquals(0, transactions.getResults().size());
    }

    @Test
    @Order(25)
    public void ReportFindTransactionsPaged_By_BrandReference() throws ApiException {
        String brandReference = "300351293234459";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.BrandReference, brandReference)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertEquals(brandReference, transactionSummary.getBrandReference());
    }

    @Test
    @Order(26)
    public void ReportFindTransactionsPaged_By_WrongBrandReference() throws ApiException {
        String brandReference = "000000000000001";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.BrandReference, brandReference)
                        .execute();
        assertNotNull(transactions);
        assertEquals(0, transactions.getResults().size());
    }

    @Disabled
    // Although requests are done with &entry_mode set properly, the real endpoint returns transactions with other entry_modes.
    // TODO: Report error to GP-API team. Enable it when fixed.
    @Test
    @Order(27)
    public void ReportFindTransactionsPaged_By_AllEntryModes() throws ApiException {
        for (PaymentEntryMode paymentEntryMode : PaymentEntryMode.values()) {

            TransactionSummaryPaged transactions =
                    ReportingService
                            .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                            .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                            .where(SearchCriteria.PaymentEntryMode, paymentEntryMode)
                            .execute();
            assertNotNull(transactions);
            for (TransactionSummary transactionSummary : transactions.getResults())
                assertEquals(paymentEntryMode.getValue(), transactionSummary.getEntryMode());
        }
    }

    @Test
    @Order(28)
    public void ReportFindTransactionsPaged_By_EntryMode() throws ApiException {
        PaymentEntryMode paymentEntryMode = PaymentEntryMode.Ecom;

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.PaymentEntryMode, paymentEntryMode)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertEquals(paymentEntryMode.getValue(), transactionSummary.getEntryMode());

        PaymentEntryMode paymentEntryModeMoto = PaymentEntryMode.Moto;

        TransactionSummaryPaged transactionsMoto =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.PaymentEntryMode, paymentEntryModeMoto)
                        .execute();
        assertNotNull(transactionsMoto);
        for (TransactionSummary transactionSummary : transactionsMoto.getResults())
            assertEquals(paymentEntryModeMoto.getValue(), transactionSummary.getEntryMode());
        assertNotSame(transactions, transactionsMoto);
    }

    @Test
    @Order(29)
    public void ReportFindTransactionsPaged_By_Number_First6_And_Number_Last4() throws ApiException {
        String number_first6 = "543458";
        String number_last4 = "7652";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.CardNumberFirstSix, number_first6)
                        .and(SearchCriteria.CardNumberLastFour, number_last4)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults()) {
            assertTrue(transactionSummary.getMaskedCardNumber().startsWith(number_first6));
            assertTrue(transactionSummary.getMaskedCardNumber().endsWith(number_last4));
        }
    }

    @Test
    @Order(30)
    public void ReportFindTransactionsPaged_By_Token_First6_And_Token_Last4() throws ApiException {
        String token_first6 = "516730";
        String token_last4 = "5507";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.TokenFirstSix, token_first6)
                        .and(SearchCriteria.TokenLastFour, token_last4)
                        .execute();
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
    @Order(31)
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
                        .execute();
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
    @Order(32)
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
                    .execute();
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
    @Order(33)
    public void ReportFindTransactionsPaged_By_BatchId() throws ApiException {
        String batchId = "BAT_875461";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.BatchId, batchId)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertEquals(batchId, transactionSummary.getBatchSequenceNumber());
    }

    @Test
    @Order(34)
    public void ReportFindTransactionsPaged_By_WrongBatchId() throws ApiException {
        String batchId = "BAT_000461";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.BatchId, batchId)
                        .execute();
        assertNotNull(transactions);
        assertEquals(0, transactions.getResults().size());
    }

    @Test
    @Order(35)
    public void ReportFindTransactionsPaged_By_Name() throws ApiException {
        String name = "James Mason";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.Name, name)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertEquals(name, transactionSummary.getCardHolderName());
    }

    @Test
    @Order(36)
    public void ReportFindTransactionsPaged_By_RandomName() throws ApiException {
        String name = UUID.randomUUID().toString().replace("-", "");

        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.Name, name)
                        .execute();
        assertNotNull(transactions);
        assertEquals(0, transactions.getResults().size());
    }

    @Test
    @Order(37)
    public void ReportFindTransactionsPaged_WithInvalid_AccountName() throws ApiException {
        boolean exceptionCaught = false;
        try {
            ReportingService
                    .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                    .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                    .where(SearchCriteria.AccountName, "VISA")
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("40003", ex.getResponseText());
            assertEquals("ACTION_NOT_AUTHORIZED", ex.getResponseCode());
            assertEquals("Status Code: 400 - Token does not match account_id or account_name in the request", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Disabled // Although documentation indicates from_time_created is required, the real endpoint returns results.
    // TODO: Report error to GP-API team. Enable it when fixed.
    @Test
    @Order(38)
    public void ReportFindTransactionsPaged_Without_Mandatory_StartDate() throws ApiException {
        boolean exceptionCaught = false;
        try {
            ReportingService
                    .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                    .execute();
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
    @Order(39)
    public void ReportFindTransactionsPaged_By_PaymentMethod() throws ApiException {
        for (PaymentMethodName paymentMethodName : PaymentMethodName.values()) {
            TransactionSummaryPaged transactions =
                    ReportingService
                            .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                            .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                            .where(SearchCriteria.PaymentMethodName, paymentMethodName)
                            .execute();
            assertNotNull(transactions);
        }
    }

    @Test
    @Order(40)
    public void CompareResults_ReportFindTransactionsPaged_OrderBy_TypeAndTimeCreated() throws ApiException {
        TransactionSummaryPaged resultByTimeCreated =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .and(SearchCriteria.EndDate, REPORTING_END_DATE)
                        .execute();

        TransactionSummaryPaged resultByType =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.Type, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .and(SearchCriteria.EndDate, REPORTING_END_DATE)
                        .execute();
        assertNotNull(resultByTimeCreated.getResults());
        assertFalse(resultByTimeCreated.getResults().isEmpty());
        assertNotNull(resultByType.getResults());
        assertFalse(resultByType.getResults().isEmpty());
        assertNotEquals(resultByTimeCreated.getResults(), resultByType.getResults());
    }

    @Test
    @Order(41)
    public void ReportFindTransactionsPaged_OrderBy_TimeCreated_Ascending() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Ascending)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));
    }

    @Test
    @Order(42)
    public void ReportFindTransactionsPaged_OrderBy_TimeCreated_Descending() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));
    }

    @Test
    @Order(43)
    public void ReportFindTransactionsPaged_OrderBy_Id_Ascending() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.Id, SortDirection.Ascending)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));
    }

    @Test
    @Order(44)
    public void ReportFindTransactionsPaged_OrderBy_Id_Descending() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.Id, SortDirection.Descending)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));
    }

    @Test
    @Order(45)
    public void ReportFindTransactionsPaged_OrderBy_Type_Ascending() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.Type, SortDirection.Ascending)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));
    }

    @Test
    @Order(46)
    public void ReportFindTransactionsPaged_OrderBy_Type_Descending() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.Type, SortDirection.Descending)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));
    }

    // ================================================================================
    // Settlement Transactions
    // ================================================================================
    @Test
    @Order(47)
    public void ReportFindSettlementTransactionsPaged_By_StartDate_And_EndDate() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .and(SearchCriteria.EndDate, REPORTING_END_DATE)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults()) {
            Date transactionDate = transactionSummary.getTransactionDate().toDate();
            assertTrue(DateUtils.isBeforeOrEquals(transactionDate, REPORTING_END_DATE));
            assertTrue(DateUtils.isAfterOrEquals(transactionDate, REPORTING_START_DATE));
        }
    }

    @Test
    @Order(48)
    public void ReportFindSettlementTransactionsPaged_OrderBy_TimeCreated() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));

        TransactionSummaryPaged transactionsAsc =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Ascending)
                        .execute();
        assertNotNull(transactionsAsc);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));
        assertNotSame(transactions, transactionsAsc);
    }

    @Test
    @Order(49)
    public void ReportFindSettlementTransactionsPaged_OrderBy_Status() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.Status, SortDirection.Descending)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));

        TransactionSummaryPaged transactionsAsc =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.Status, SortDirection.Ascending)
                        .execute();
        assertNotNull(transactionsAsc);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));
        assertNotSame(transactions, transactionsAsc);
    }

    @Test
    @Order(50)
    public void ReportFindSettlementTransactionsPaged_OrderBy_Type() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.Type, SortDirection.Descending)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));

        TransactionSummaryPaged transactionsAsc =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.Type, SortDirection.Ascending)
                        .execute();
        assertNotNull(transactionsAsc);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));
        assertNotSame(transactions, transactionsAsc);
    }

    @Test
    @Order(51)
    public void ReportFindSettlementTransactionsPaged_OrderBy_DepositId() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.DepositId, SortDirection.Descending)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));

        TransactionSummaryPaged transactionsAsc =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.DepositId, SortDirection.Ascending)
                        .execute();
        assertNotNull(transactionsAsc);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));
        assertNotSame(transactions, transactionsAsc);
    }

    @Test
    @Order(52)
    public void CompareResults_ReportFindSettlementTransactionsPaged_OrderBy_TypeAndTimeCreated() throws ApiException {
        TransactionSummaryPaged transactionsOrderedByTimeCreated =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .execute();
        assertNotNull(transactionsOrderedByTimeCreated);
        for (TransactionSummary transactionSummary : transactionsOrderedByTimeCreated.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));

        TransactionSummaryPaged transactionsOrderedByType =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.Type, SortDirection.Descending)
                        .execute();
        assertNotNull(transactionsOrderedByType);
        for (TransactionSummary transactionSummary : transactionsOrderedByType.getResults())
            assertTrue(DateUtils.isAfterOrEquals(transactionSummary.getTransactionDate().toDate(), REPORTING_START_DATE));
        assertNotSame(transactionsOrderedByTimeCreated, transactionsOrderedByType);
    }

    @Test
    @Order(53)
    public void ReportFindSettlementTransactionsPaged_By_Number_First6_And_Number_Last4() throws ApiException {
        String number_first6 = "543458";
        String number_last4 = "7652";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.CardNumberFirstSix, number_first6)
                        .and(SearchCriteria.CardNumberLastFour, number_last4)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults()) {
            assertTrue(transactionSummary.getMaskedCardNumber().startsWith(number_first6));
            assertTrue(transactionSummary.getMaskedCardNumber().endsWith(number_last4));
        }
    }

    @Test
    @Order(54)
    public void ReportFindSettlementTransactionsPaged_FilterBy_AllDepositStatus() throws ApiException {
        for (DepositStatus depositStatus : DepositStatus.values()) {

            TransactionSummaryPaged transactions =
                    ReportingService
                            .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                            .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                            .where(SearchCriteria.DepositStatus, depositStatus)
                            .execute();
            assertNotNull(transactions);
            for (TransactionSummary transactionSummary : transactions.getResults())
                assertEquals(depositStatus.getValue(GP_API), transactionSummary.getDepositStatus());
        }
    }

    @Test
    @Order(55)
    public void ReportFindSettlementTransactionsPaged_By_CardBrand() throws ApiException {
        String[] cardBrands = {"VISA", "MASTERCARD", "AMEX", "DINERS", "DISCOVER", "JCB", "CUP"};

        for (String cardBrand : cardBrands) {
            TransactionSummaryPaged transactions =
                    ReportingService
                            .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                            .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                            .where(SearchCriteria.CardBrand, cardBrand)
                            .execute();
            assertNotNull(transactions);
            for (TransactionSummary transactionSummary : transactions.getResults()) {
                assertEquals(cardBrand, transactionSummary.getCardType());
            }
        }
    }

    @Test
    @Order(56)
    public void ReportFindSettlementTransactionsPaged_By_InvalidCardBrand() throws ApiException {
        String cardBrand = "MIT";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .and(SearchCriteria.CardBrand, cardBrand)
                        .execute();
        assertNotNull(transactions);
        assertEquals(0, transactions.getResults().size());
    }

    @Test
    @Order(57)
    public void ReportFindSettlementTransactionsPaged_By_ARN() throws ApiException {
        String arn = "74500010037624410827759";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.AquirerReferenceNumber, arn)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertEquals(transactionSummary.getAcquirerReferenceNumber(), arn);
    }

    @Test
    @Order(58)
    public void ReportFindSettlementTransactionsPaged_By_WrongARN() throws ApiException {
        String arn = "00000010037624410827527";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.AquirerReferenceNumber, arn)
                        .execute();
        assertNotNull(transactions);
        assertEquals(0, transactions.getResults().size());
    }

    @Test
    @Order(59)
    public void ReportFindSettlementTransactionsPaged_By_BrandReference() throws ApiException {
        String brandReference = "MCF1CZ5ME5405";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.BrandReference, brandReference)
                        .execute();
        assertNotNull(transactions);
        assertEquals(1, transactions.getResults().size());
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertEquals(brandReference, transactionSummary.getBrandReference());
    }

    @Test
    @Order(60)
    public void ReportFindSettlementTransactionsPaged_By_WrongBrandReference() throws ApiException {
        String brandReference = "000000000000001";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.BrandReference, brandReference)
                        .execute();
        assertNotNull(transactions);
        assertEquals(0, transactions.getResults().size());
    }

    @Test
    @Order(61)
    public void ReportFindSettlementTransactionsPaged_By_CardBrand_And_AuthCode() throws ApiException {
        String cardBrand = "MASTERCARD";
        String authCode = "028010";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.CardBrand, cardBrand)
                        .and(SearchCriteria.AuthCode, authCode)
                        .execute();
        assertNotNull(transactions);
        assertEquals(1, transactions.getResults().size());
        for (TransactionSummary transactionSummary : transactions.getResults()) {
            assertEquals(cardBrand, transactionSummary.getCardType());
            assertEquals(authCode, transactionSummary.getAuthCode());
        }
    }

    @Test
    @Order(62)
    public void ReportFindSettlementTransactionsPaged_By_Reference() throws ApiException {
        String referenceNumber = "50080513769";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.ReferenceNumber, referenceNumber)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertEquals(referenceNumber, transactionSummary.getReferenceNumber());
    }

    @Test
    @Order(63)
    public void ReportFindSettlementTransactionsPaged_By_RandomReference() throws ApiException {
        String referenceNumber = UUID.randomUUID().toString();

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.ReferenceNumber, referenceNumber)
                        .execute();
        assertNotNull(transactions);
        assertEquals(0, transactions.getResults().size());
    }

    @Test
    @Order(64)
    public void ReportFindSettlementTransactionsPaged_By_AllStatus() throws ApiException {
        for (TransactionStatus transactionStatus : TransactionStatus.values()) {

            TransactionSummaryPaged transactions =
                    ReportingService
                            .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                            .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                            .where(SearchCriteria.TransactionStatus, transactionStatus)
                            .execute();

            assertNotNull(transactions);
            for (TransactionSummary transactionSummary : transactions.getResults())
                assertEquals(transactionStatus.getValue(), transactionSummary.getTransactionStatus());
        }
    }

    @Test
    @Order(65)
    public void ReportFindSettlementTransactionsPaged_By_DepositId() throws ApiException {
        String depositId = "DEP_2342423423";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withDepositReference(depositId)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults())
            assertEquals(depositId, transactionSummary.getDepositReference());
    }

    @Test
    @Order(66)
    public void ReportFindSettlementTransactionsPaged_By_RandomDepositId() throws ApiException {
        String depositId = UUID.randomUUID().toString();

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .withDepositReference(depositId)
                        .execute();
        assertNotNull(transactions);
        assertEquals(0, transactions.getResults().size());
    }

    @Test
    @Order(67)
    public void ReportFindSettlementTransactionsPaged_By_FromDepositTimeCreated_And_ToDepositTimeCreated() throws ApiException {
        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(DataServiceCriteria.StartDepositDate, REPORTING_START_DATE)
                        .and(DataServiceCriteria.EndDepositDate, REPORTING_END_DATE)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults()) {
            Date depositDate = transactionSummary.getDepositDate();
            assertTrue(DateUtils.isBeforeOrEquals(depositDate, REPORTING_END_DATE));
            assertTrue(DateUtils.isAfterOrEquals(depositDate, REPORTING_START_DATE));
        }
    }

    @Test
    @Order(68)
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
                        .execute();

        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults()) {
            Date transactionDate = transactionSummary.getTransactionDate().toDate();
            assertTrue(DateUtils.isAfterOrEquals(transactionDate, startBatchDate));
            assertTrue(DateUtils.isBeforeOrEquals(transactionDate, endBatchDate));
            assertNotNull(transactionSummary.getBatchCloseDate());
        }
    }

    @Test
    @Order(69)
    public void ReportFindSettlementTransactionsPaged_By_SystemMid_And_SystemHierarchy() throws ApiException {
        String merchantId = "101023947262";
        String hierarchy = "055-70-024-011-019";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(DataServiceCriteria.MerchantId, merchantId)
                        .and(DataServiceCriteria.SystemHierarchy, hierarchy)
                        .execute();
        assertNotNull(transactions);
        for (TransactionSummary transactionSummary : transactions.getResults()) {
            assertEquals(merchantId, transactionSummary.getMerchantId());
            assertEquals(hierarchy, transactionSummary.getMerchantHierarchy());
        }
    }

    @Test
    @Order(70)
    public void ReportFindSettlementTransactionsPaged_By_NonExistent_SystemMerchantId() throws ApiException {
        String merchantId = String.valueOf(new Random().nextInt(999999999));

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(DataServiceCriteria.MerchantId, merchantId)
                        .execute();
        assertNotNull(transactions);
        assertEquals(0, transactions.getResults().size());
    }

    @Test
    @Order(71)
    public void ReportFindSettlementTransactionsPaged_By_Invalid_SystemMid() throws ApiException {
        boolean exceptionCaught = false;
        try {
            ReportingService
                    .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                    .where(DataServiceCriteria.MerchantId, UUID.randomUUID().toString())
                    .execute();
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
    @Order(72)
    public void ReportFindSettlementTransactionsPaged_By_Random_SystemHierarchy() throws ApiException {
        String systemHierarchy = "000-00-024-000-000";

        TransactionSummaryPaged transactions =
                ReportingService
                        .findSettlementTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(DataServiceCriteria.SystemHierarchy, systemHierarchy)
                        .execute();
        assertNotNull(transactions);
        assertEquals(0, transactions.getResults().size());
    }

}

package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.entities.gpApi.entities.FundsAccountDetails;
import com.global.api.entities.reporting.*;
import com.global.api.paymentMethods.eCheck;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.ReportingService;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.Assert.*;

public class GpApiAchTest extends BaseGpApiTest {

    private eCheck eCheck;
    private Address address;
    private Customer customer;

    private final String CURRENCY = "USD";
    private final BigDecimal AMOUNT = new BigDecimal(10);
    private final long SLEEP_30_SECS_IN_MILLIS = 30000;


    public GpApiAchTest() throws ApiException {

        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        ServicesContainer.configureService(config);
    }

    @Before
    public void TestInitialize() {
        address = new Address();
        address.setStreetAddress1("Apartment 852");
        address.setStreetAddress2("Complex 741");
        address.setStreetAddress3("no");
        address.setCity("Chicago");
        address.setPostalCode("5001");
        address.setState("IL");
        address.setCountryCode("US");

        Address bankAddress = new Address();
        bankAddress.setStreetAddress1("12000 Smoketown Rd");
        bankAddress.setStreetAddress2("Apt 3B");
        bankAddress.setStreetAddress3("no");
        bankAddress.setCity("Mesa");
        bankAddress.setPostalCode("22192");
        bankAddress.setState("AZ");
        bankAddress.setCountryCode("US");

        eCheck = new eCheck();
        eCheck.setAccountNumber("1234567890");
        eCheck.setRoutingNumber("122000030");
        eCheck.setAccountType(AccountType.Savings);
        eCheck.setSecCode(SecCode.Web);
        eCheck.setCheckReference("123");
        eCheck.setMerchantNotes("123");
        eCheck.setBankName("First Union");
        eCheck.setCheckHolderName("Jane Doe");
        eCheck.setBankAddress(bankAddress);

        customer = new Customer();
        customer.setKey("e193c21a-ce64-4820-b5b6-8f46715de931");
        customer.setFirstName("James");
        customer.setLastName("Mason");
        customer.setDateOfBirth("1980-01-01");
        customer.setMobilePhone("+35312345678");
        customer.setHomePhone("+11234589");
    }

    @Test
    public void CheckSale() throws ApiException {
        Transaction response =
                eCheck
                        .charge(AMOUNT)
                        .withCurrency(CURRENCY)
                        .withAddress(address)
                        .withCustomer(customer)
                        .execute();

        assertResponse(response);
    }

    @Test
    public void CreditSaleThenSplit() throws ApiException, InterruptedException {

        GpApiConfig config = gpApiSetup(APP_ID_FOR_MERCHANT, APP_KEY_FOR_MERCHANT, Channel.CardNotPresent);
        ServicesContainer.configureService(config);

        MerchantSummaryPaged merchants = getMerchants();

        MerchantSummary merchantProcessing = merchants.getResults().get(0);
        String merchantId = merchantProcessing.getId();
        MerchantAccountSummary accountProcessing = getAccountByType(merchantId, MerchantAccountType.TRANSACTION_PROCESSING);

        config.setMerchantId(merchantId);
        config.setAccessTokenInfo(new AccessTokenInfo().setTransactionProcessingAccountID(accountProcessing.getId()));

        String merchantConfigName = "config_" + merchantId;
        ServicesContainer.configureService(config, merchantConfigName);

        Transaction transaction = eCheck
                .charge(AMOUNT)
                .withCurrency(CURRENCY)
                .withAddress(address)
                .withCustomer(customer)
                .execute(merchantConfigName);

        MerchantSummary merchantSplitting = null;
        for (MerchantSummary el : merchants.getResults()) {
            if (!el.getId().equals(merchantId)) {
                merchantSplitting = el;
                break;
            }
        }

        assertNotNull(merchantSplitting);
        assertNotNull(accountProcessing);

        MerchantAccountSummary accountRecipient = getAccountByType(merchantId, MerchantAccountType.FUND_MANAGEMENT);
        MerchantAccountSummary accountSplitting = getAccountByType(merchantSplitting.getId(), MerchantAccountType.FUND_MANAGEMENT);

        assertNotNull(accountRecipient);
        assertNotNull(accountSplitting);

        FundsData fundsData = new FundsData();
        fundsData.setRecipientAccountId(accountSplitting.getId());
        fundsData.setMerchantId(merchantId);

        Transaction splitResponse = transaction
                .split(new BigDecimal(8))
                .withFundsData(fundsData)
                .withReference("Split Identifier")
                .withDescription("Split Test")
                .execute();

        assertResponse(splitResponse, TransactionStatus.Captured);

        ServicesContainer.removeConfig(merchantConfigName);
        Thread.sleep(SLEEP_30_SECS_IN_MILLIS);
    }

    @Test
    public void CreditSaleThenSplitThenReverse_WithConfigMerchantId() throws ApiException, InterruptedException {

        GpApiConfig config = gpApiSetup(APP_ID_FOR_MERCHANT, APP_KEY_FOR_MERCHANT, Channel.CardNotPresent);
        ServicesContainer.configureService(config);

        MerchantSummaryPaged merchants = getMerchants();

        MerchantSummary merchantProcessing = merchants.getResults().get(0);
        String merchantId = merchantProcessing.getId();
        MerchantAccountSummary accountProcessing = getAccountByType(merchantId, MerchantAccountType.TRANSACTION_PROCESSING);

        config.setMerchantId(merchantId);
        config.setAccessTokenInfo(new AccessTokenInfo().setTransactionProcessingAccountID(accountProcessing.getId()));

        String merchantConfigName = "config_" + merchantId;
        ServicesContainer.configureService(config, merchantConfigName);

        Transaction transaction = eCheck
                .charge(AMOUNT)
                .withCurrency(CURRENCY)
                .withAddress(address)
                .withCustomer(customer)
                .execute(merchantConfigName);

        MerchantSummary merchantSplitting = null;
        for (MerchantSummary el : merchants.getResults()) {
            if (!el.getId().equals(merchantId)) {
                merchantSplitting = el;
                break;
            }
        }

        MerchantAccountSummary accountRecipient = getAccountByType(merchantId, MerchantAccountType.FUND_MANAGEMENT);
        MerchantAccountSummary accountSplitting = getAccountByType(merchantSplitting.getId(), MerchantAccountType.FUND_MANAGEMENT);

        assertNotNull(accountRecipient);
        assertNotNull(accountSplitting);

        FundsData fundsData = new FundsData();
        fundsData.setRecipientAccountId(accountSplitting.getId());
        fundsData.setMerchantId(merchantId);

        BigDecimal transferAmount = new BigDecimal(8);
        String transferReference = "Split Identifier";
        String transferDescription = "Split Test";

        Transaction splitResponse = transaction.split(transferAmount)
                .withFundsData(fundsData)
                .withReference(transferReference)
                .withDescription(transferDescription)
                .execute();

        assertResponse(splitResponse, TransactionStatus.Captured);

        assertNotNull(splitResponse.getTransferFundsAccountDetailsList());

        FundsAccountDetails transferFund = splitResponse.getTransferFundsAccountDetailsList().get(0);

        assertEquals("00", transferFund.getStatus());
        assertEquals(transferAmount.toString(), transferFund.getAmount());
        assertEquals(transferReference, transferFund.getReference());
        assertEquals(transferDescription, transferFund.getDescription());

        Transaction trfTransaction = Transaction.fromId(transferFund.getId(), PaymentMethodType.AccountFunds);

        Transaction reverse = trfTransaction.reverse().execute(merchantConfigName);

        assertResponse(reverse, TransactionStatus.Funded);

        ServicesContainer.removeConfig(merchantConfigName);
        Thread.sleep(SLEEP_30_SECS_IN_MILLIS);
    }

    @Test
    public void CreditSaleThenSplitThenReverse_WithFundsData() throws ApiException, InterruptedException {

        GpApiConfig config = gpApiSetup(APP_ID_FOR_MERCHANT, APP_KEY_FOR_MERCHANT, Channel.CardNotPresent);
        ServicesContainer.configureService(config);

        MerchantSummaryPaged merchants = getMerchants();

        MerchantSummary merchantProcessing = merchants.getResults().get(0);
        String merchantId = merchantProcessing.getId();
        MerchantAccountSummary accountProcessing = getAccountByType(merchantId, MerchantAccountType.TRANSACTION_PROCESSING);

        config.setMerchantId(merchantId);
        config.setAccessTokenInfo(new AccessTokenInfo().setTransactionProcessingAccountID(accountProcessing.getId()));

        String merchantConfigName = "config_" + merchantId;
        ServicesContainer.configureService(config, merchantConfigName);

        Transaction transaction = eCheck
                .charge(AMOUNT)
                .withCurrency(CURRENCY)
                .withAddress(address)
                .withCustomer(customer)
                .execute(merchantConfigName);

        MerchantSummary merchantSplitting = null;
        for (MerchantSummary el : merchants.getResults()) {
            if (!el.getId().equals(merchantId)) {
                merchantSplitting = el;
                break;
            }
        }

        MerchantAccountSummary accountRecipient = getAccountByType(merchantId, MerchantAccountType.FUND_MANAGEMENT);
        MerchantAccountSummary accountSplitting = getAccountByType(merchantSplitting.getId(), MerchantAccountType.FUND_MANAGEMENT);

        assertNotNull(accountRecipient);
        assertNotNull(accountSplitting);

        FundsData fundsData = new FundsData();
        fundsData.setRecipientAccountId(accountSplitting.getId());
        fundsData.setMerchantId(merchantId);

        BigDecimal transferAmount = new BigDecimal(8);
        String transferReference = "Split Identifier";
        String transferDescription = "Split Test";

        Transaction splitResponse = transaction.split(transferAmount)
                .withFundsData(fundsData)
                .withReference(transferReference)
                .withDescription(transferDescription)
                .execute();

        assertResponse(splitResponse, TransactionStatus.Captured);

        assertNotNull(splitResponse.getTransferFundsAccountDetailsList());

        FundsAccountDetails transferFund = splitResponse.getTransferFundsAccountDetailsList().get(0);

        assertEquals("00", transferFund.getStatus());
        assertEquals(transferAmount.toString(), transferFund.getAmount());
        assertEquals(transferReference, transferFund.getReference());
        assertEquals(transferDescription, transferFund.getDescription());

        Transaction trfTransaction = Transaction.fromId(transferFund.getId(), PaymentMethodType.AccountFunds);

        Transaction reverse = trfTransaction.reverse().execute();

        assertResponse(reverse, TransactionStatus.Funded);

        ServicesContainer.removeConfig(merchantConfigName);
        Thread.sleep(SLEEP_30_SECS_IN_MILLIS);
    }

    @Test
    public void CreditSaleThenSplit_WithoutFundsData() throws ApiException {

        GpApiConfig config = gpApiSetup(APP_ID_FOR_MERCHANT, APP_KEY_FOR_MERCHANT, Channel.CardNotPresent);
        ServicesContainer.configureService(config);

        Transaction transaction = Transaction.fromId(UUID.randomUUID().toString());

        boolean exceptionCaught = false;
        try {
            transaction.split(new BigDecimal(8))
                    .withReference("Split Identifier")
                    .withDescription("Split Test")
                    .execute();
        } catch (BuilderException ex) {
            exceptionCaught = true;
            assertEquals("fundsData cannot be null for this transaction type.", ex.getMessage());
        } finally {
            ServicesContainer.removeConfig();
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditSaleThenSplit_WithoutAmount() throws ApiException, InterruptedException {

        GpApiConfig config = gpApiSetup(APP_ID_FOR_MERCHANT, APP_KEY_FOR_MERCHANT, Channel.CardNotPresent);
        ServicesContainer.configureService(config);

        MerchantSummaryPaged merchants = getMerchants();

        MerchantSummary merchantProcessing = merchants.getResults().get(0);
        String merchantId = merchantProcessing.getId();
        MerchantAccountSummary accountProcessing = getAccountByType(merchantId, MerchantAccountType.TRANSACTION_PROCESSING);

        config.setMerchantId(merchantId);
        config.setAccessTokenInfo(new AccessTokenInfo().setTransactionProcessingAccountID(accountProcessing.getId()));

        String merchantConfigName = "config_" + merchantId;
        ServicesContainer.configureService(config, merchantConfigName);

        Transaction transaction = eCheck
                .charge(AMOUNT)
                .withCurrency(CURRENCY)
                .withAddress(address)
                .withCustomer(customer)
                .execute(merchantConfigName);

        MerchantSummary merchantSplitting = null;
        for (MerchantSummary el : merchants.getResults()) {
            if (!el.getId().equals(merchantId)) {
                merchantSplitting = el;
                break;
            }
        }

        MerchantAccountSummary accountRecipient = getAccountByType(merchantId, MerchantAccountType.FUND_MANAGEMENT);
        MerchantAccountSummary accountSplitting = getAccountByType(merchantSplitting.getId(), MerchantAccountType.FUND_MANAGEMENT);

        assertNotNull(accountRecipient);

        FundsData fundsData = new FundsData();
        fundsData.setRecipientAccountId(accountSplitting.getId());
        fundsData.setMerchantId(merchantId);

        boolean exceptionCaught = false;
        try {
            transaction
                    .split(null)
                    .withFundsData(fundsData)
                    .withReference("Split Identifier")
                    .withDescription("Split Test")
                    .execute();
        } catch (BuilderException ex) {
            exceptionCaught = true;
            assertEquals("amount cannot be null for this transaction type.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
            ServicesContainer.removeConfig(merchantConfigName);
            Thread.sleep(SLEEP_30_SECS_IN_MILLIS);
        }
    }

    @Test
    public void CreditSaleThenSplit_WithoutRecipientId() throws ApiException, InterruptedException {

        GpApiConfig config = gpApiSetup(APP_ID_FOR_MERCHANT, APP_KEY_FOR_MERCHANT, Channel.CardNotPresent);
        ServicesContainer.configureService(config);

        MerchantSummaryPaged merchants = getMerchants();

        MerchantSummary merchantProcessing = merchants.getResults().get(0);
        String merchantId = merchantProcessing.getId();
        MerchantAccountSummary accountProcessing = getAccountByType(merchantId, MerchantAccountType.TRANSACTION_PROCESSING);

        config.setMerchantId(merchantId);
        config.setAccessTokenInfo(new AccessTokenInfo().setTransactionProcessingAccountID(accountProcessing.getId()));

        String merchantConfigName = "config_" + merchantId;
        ServicesContainer.configureService(config, merchantConfigName);

        Transaction transaction = eCheck
                .charge(AMOUNT)
                .withCurrency(CURRENCY)
                .withAddress(address)
                .withCustomer(customer)
                .execute(merchantConfigName);

        MerchantAccountSummary accountRecipient = getAccountByType(merchantId, MerchantAccountType.FUND_MANAGEMENT);

        assertNotNull(accountRecipient);

        FundsData fundsData = new FundsData();
        fundsData.setMerchantId(merchantId);

        boolean exceptionCaught = false;
        try {
            transaction.split(new BigDecimal("0.01"))
                    .withFundsData(fundsData)
                    .withReference("Split Identifier")
                    .withDescription("Split Test")
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 403 - Transfers may only be initiated between accounts under the same partner program", ex.getMessage());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40041", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
            ServicesContainer.removeConfig(merchantConfigName);
            Thread.sleep(SLEEP_30_SECS_IN_MILLIS);
        }
    }

    @Test
    public void CheckRefund() throws ApiException {
        Transaction response =
                eCheck
                        .refund(AMOUNT)
                        .withCurrency(CURRENCY)
                        .withAddress(address)
                        .withCustomer(customer)
                        .execute();

        assertResponse(response);
    }

    @Test
    @Ignore("GP-API sandbox limitation")
    public void CheckRefundExistingSale() throws ApiException {
        BigDecimal amount = new BigDecimal("1.29");

        TransactionSummaryPaged response =
                ReportingService
                        .findTransactionsPaged(1, 10)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, LocalDate.now().minusDays(2).toDate())
                        .and(SearchCriteria.EndDate, LocalDate.now().minusDays(2).toDate())
                        .and(SearchCriteria.PaymentType, PaymentType.Sale)
                        .and(SearchCriteria.PaymentMethodName, PaymentMethodName.BankTransfer)
                        .and(DataServiceCriteria.Amount, amount)
                        .execute();

        assertNotNull(response);
        assertNotNull(response.getResults());
        TransactionSummary transactionSummary = response.getResults().get(0);
        assertNotNull(transactionSummary);
        assertEquals(amount, transactionSummary.getAmount());
        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionSummary.getTransactionId());
        transaction.setPaymentMethodType(PaymentMethodType.ACH);

        Transaction resp =
                transaction
                        .refund()
                        .withCurrency(CURRENCY)
                        .execute();

        assertResponse(resp);
    }

    @Test
    public void CheckReauthorize() throws ApiException {
        eCheck eCheckReauth = new eCheck();
        eCheckReauth.setSecCode(SecCode.Ppd);
        eCheckReauth.setAccountNumber("051904524");
        eCheckReauth.setRoutingNumber("123456780");

        BigDecimal amount = new BigDecimal("1.29");

        TransactionSummaryPaged response =
                ReportingService
                        .findTransactionsPaged(1, 10)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, LocalDate.now().minusYears(1).toDate())
                        .and(SearchCriteria.EndDate, LocalDate.now().minusDays(2).toDate())
                        .and(SearchCriteria.PaymentType, PaymentType.Sale)
                        .and(SearchCriteria.PaymentMethodName, PaymentMethodName.BankTransfer)
                        .and(DataServiceCriteria.Amount, amount)
                        .execute();

        assertNotNull(response);
        assertNotNull(response.getResults());
        assertFalse(response.getResults().isEmpty());

        TransactionSummary transactionSummary = response.getResults().get(0);
        assertNotNull(transactionSummary);
        assertEquals(amount, transactionSummary.getAmount());

        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionSummary.getTransactionId());
        transaction.setPaymentMethodType(PaymentMethodType.ACH);

        Transaction resp =
                transaction
                        .reauthorize()
                        .withDescription("Resubmitting " + transaction.getReferenceNumber())
                        .withBankTransferDetails(eCheckReauth)
                        .execute();

        assertResponse(resp);
    }

    private void assertResponse(Transaction response) {
        assertResponse(response, TransactionStatus.Captured);
    }

    private void assertResponse(Transaction response, TransactionStatus transactionStatus) {
        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(transactionStatus.getValue(), response.getResponseMessage());
    }

    private MerchantSummaryPaged getMerchants() throws ApiException {
        return
                new ReportingService()
                        .findMerchants(1, 10)
                        .orderBy(MerchantAccountsSortProperty.TIME_CREATED, SortDirection.Descending)
                        .where(SearchCriteria.MerchantStatus, MerchantAccountStatus.ACTIVE)
                        .execute();
    }

    private MerchantAccountSummary getAccountByType(String merchantSenderId, MerchantAccountType
            merchantAccountType) throws ApiException {
        MerchantAccountSummaryPaged response =
                ReportingService
                        .findAccounts(1, 10)
                        .orderBy(MerchantAccountsSortProperty.TIME_CREATED, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .and(DataServiceCriteria.MerchantId, merchantSenderId)
                        .and(SearchCriteria.AccountStatus, MerchantAccountStatus.ACTIVE)
                        .execute();

        for (MerchantAccountSummary el : response.getResults()) {
            if (el.getType() == merchantAccountType) {
                return el;
            }
        }

        return null;
    }
}
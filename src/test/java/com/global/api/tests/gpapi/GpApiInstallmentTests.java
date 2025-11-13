package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.StoredCredential;
import com.global.api.entities.Transaction;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.InstallmentData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.ReportingService;
import org.joda.time.LocalDate;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GpApiInstallmentTests extends BaseGpApiTest{
    private final InstallmentData installmentData;
    private final BigDecimal amount = new BigDecimal("2.02");
    public static final String APP_ID = "Vw9O4jOMqozC39Grx8q3oGAvqEjLcgGn";
    public static final String APP_KEY = "qgvDUwIhgT8QS2kp";
    public static GpApiConfig gpApiConfig;
    public static GpApiConfig gpApiConfigSandbox;
    private final StoredCredential storedCredentialData;
    protected static final int FIRST_PAGE = 1;
    protected static final int PAGE_SIZE = 10;
    protected static final Date REPORTING_START_DATE = LocalDate.now().minusMonths(6).toDate();
    private final String currency = "MXN";
    CreditCardData masterCard= new CreditCardData();
    CreditCardData visaCard= new CreditCardData();
    CreditCardData carnetCard= new CreditCardData();

    public GpApiInstallmentTests() throws ApiException {
        gpApiConfig = new GpApiConfig()
                .setAppId(APP_ID)
                .setAppKey(APP_KEY);

        gpApiConfig.setChannel(Channel.CardNotPresent);
        gpApiConfig.setServiceUrl("https://apis-sit.globalpay.com/ucp");
        gpApiConfig.setEnableLogging(true);
        gpApiConfig.setRequestLogger(new RequestConsoleLogger());
        gpApiConfig.setCountry("MX");

        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setTransactionProcessingAccountName("Portico_SIT_405352");
        accessTokenInfo.setRiskAssessmentAccountName("EOS_RiskAssessment");
        gpApiConfig.setAccessTokenInfo(accessTokenInfo);
        ServicesContainer.configureService(gpApiConfig);

        gpApiConfigSandbox = new GpApiConfig()
                .setAppId("4gPqnGBkppGYvoE5UX9EWQlotTxGUDbs")
                .setAppKey("FQyJA5VuEQfcji2M");

        gpApiConfigSandbox.setChannel(Channel.CardNotPresent);
        gpApiConfigSandbox.setServiceUrl("https://apis.sandbox.globalpay.com/ucp");
        gpApiConfigSandbox.setEnableLogging(true);
        gpApiConfigSandbox.setRequestLogger(new RequestConsoleLogger());
        gpApiConfigSandbox.setCountry("MX");

        AccessTokenInfo accessTokenInfoS = new AccessTokenInfo();
        accessTokenInfoS.setTransactionProcessingAccountName("transaction_processing");
        accessTokenInfoS.setRiskAssessmentAccountName("EOS_RiskAssessment");
        gpApiConfigSandbox.setAccessTokenInfo(accessTokenInfoS);
        ServicesContainer.configureService(gpApiConfigSandbox, "sandbox");

        installmentData = new InstallmentData();
        installmentData.setMode("INTEREST");
        installmentData.setProgram("SIP");
        installmentData.setCount("99");
        installmentData.setGracePeriodCount("30");

        storedCredentialData = new StoredCredential();
        storedCredentialData.setInitiator(StoredCredentialInitiator.CardHolder);
        storedCredentialData.setType(StoredCredentialType.Installment);
        storedCredentialData.setSequence(StoredCredentialSequence.Subsequent);
        storedCredentialData.setReason(StoredCredentialReason.Incremental);
        storedCredentialData.setContractReference("TestContractReference");

        masterCard.setNumber("5120350100064537");
        masterCard.setExpMonth(12);
        masterCard.setExpYear(2027);
        masterCard.setCvn("123");
        masterCard.setCardPresent(false);
        masterCard.setReaderPresent(false);

        visaCard.setNumber("4395840190010011");
        visaCard.setExpMonth(12);
        visaCard.setExpYear(2027);
        visaCard.setCvn("840");
        visaCard.setCardPresent(false);
        visaCard.setReaderPresent(false);

        carnetCard.setNumber("6363181868200169");
        carnetCard.setExpMonth(01);
        carnetCard.setExpYear(2030);
        carnetCard.setCvn("123");
        carnetCard.setCardPresent(false);
        carnetCard.setReaderPresent(false);
        carnetCard.setCardType("carnet");
    }

    @Test
    @Order(1)
    public void CreditSaleForInstallmentMC() throws ApiException {
        Transaction response =
                masterCard
                        .charge(amount)
                        .withCurrency(currency)
                        .withStoredCredential(storedCredentialData)
                        .withInstallmentData(installmentData)
                        .execute("sandbox");
        assertTransactionResponse(response, TransactionStatus.Captured);
        assertEquals(amount, response.getBalanceAmount());
    }

    @Test
    public void CreditSaleForInstallmentVisa() throws ApiException {
        Transaction response =
                visaCard
                        .charge(amount)
                        .withCurrency(currency)
                        .withStoredCredential(storedCredentialData)
                        .withInstallmentData(installmentData)
                        .execute("sandbox");
        assertTransactionResponse(response, TransactionStatus.Captured);
        assertEquals(amount, response.getBalanceAmount());

    }

    @Test
    public void CreditSaleWithoutInstallmentData() throws ApiException {
        Transaction response =
                visaCard
                        .charge(amount)
                        .withCurrency(currency)
                        .withStoredCredential(storedCredentialData)
                        .execute("sandbox");

        assertTransactionResponse(response, TransactionStatus.Captured);
        assertEquals(amount, response.getBalanceAmount());
        assertNull(response.getInstallmentData());

    }

    @Test
    public void ReportTransactionDetailForInstallmentByID() throws ApiException {
        Transaction response =
                masterCard
                        .charge(amount)
                        .withCurrency(currency)
                        .withStoredCredential(storedCredentialData)
                        .withInstallmentData(installmentData)
                        .execute();

        TransactionSummary transaction =
                ReportingService
                        .transactionDetail(response.getTransactionId())
                        .execute();
        assertNotNull(transaction);
        assertNotNull(transaction.getInstallmentData());
        assertEquals(installmentData.getProgram(),transaction.getInstallmentData().getProgram());
        assertEquals(installmentData.getMode(),transaction.getInstallmentData().getMode());
        assertEquals(installmentData.getCount(),transaction.getInstallmentData().getCount());
        assertEquals(installmentData.getGracePeriodCount(),transaction.getInstallmentData().getGracePeriodCount());
        assertEquals(response.getTransactionId(), transaction.getTransactionId());
    }

    @Test
    public void ReportTransactionsDetailForInstallment() throws ApiException {
        List<TransactionSummary> sampleTransactionSummary =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .execute()
                        .getResults();

        assertNotNull(sampleTransactionSummary);
        for (TransactionSummary transactionSummary : sampleTransactionSummary) {
            assertNotNull(transactionSummary.getInstallmentData());
        }
    }

    @Test
    public void TestForStatusInThreeDSecure() throws ApiException {
        TransactionSummary sampleTransactionSummary =
                ReportingService
                        .findTransactionsPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(SearchCriteria.TransactionStatus, TransactionStatus.Authenticated)
                        .execute()
                        .getResults()
                        .get(0);

        assertEquals("AUTHENTICATION_SUCCESSFUL", sampleTransactionSummary.getThreeDSecure().getStatus());
    }

    @Test
    public void ReportTransactionDetailWithoutInstallmentByID() throws ApiException {
        Transaction response =
                masterCard
                        .charge(amount)
                        .withCurrency(currency)
                        .withStoredCredential(storedCredentialData)
                        .execute();

        TransactionSummary transaction =
                ReportingService
                        .transactionDetail(response.getTransactionId())
                        .execute();
        assertNotNull(transaction);
        assertNotNull(transaction.getInstallmentData());
        assertEquals("",transaction.getInstallmentData().getProgram());
        assertEquals("",transaction.getInstallmentData().getMode());
        assertEquals("",transaction.getInstallmentData().getCount());
        assertEquals("",transaction.getInstallmentData().getGracePeriodCount());
        assertEquals(response.getTransactionId(), transaction.getTransactionId());
    }


    private void assertTransactionResponse(Transaction transaction, TransactionStatus transactionStatus) {
        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(transactionStatus.getValue(), transaction.getResponseMessage());
    }

}

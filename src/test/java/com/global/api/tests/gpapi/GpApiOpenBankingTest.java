package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.entities.reporting.TransactionSummaryPaged;
import com.global.api.paymentMethods.BankPayment;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.ReportingService;
import com.global.api.utils.DateUtils;
import com.global.api.utils.EnumUtils;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static com.global.api.entities.reporting.SearchCriteria.EndDate;
import static com.global.api.entities.reporting.SearchCriteria.StartDate;
import static org.junit.Assert.*;

public class GpApiOpenBankingTest extends BaseGpApiTest {
    private static final String currency = "GBP";
    private static final BigDecimal amount = new BigDecimal("10.99");

    public GpApiOpenBankingTest() throws ApiException {
        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId(APP_ID)
                .setAppKey(APP_KEY)
                .setChannel(Channel.CardNotPresent.getValue());

        config.setEnableLogging(true);

        ServicesContainer.configureService(config);
    }

    @Test
    public void FasterPaymentsCharge() throws ApiException, InterruptedException {
        BankPayment bankPayment = FasterPaymentsConfig();

        Transaction transaction =
                bankPayment
                        .charge(amount)
                        .withCurrency(currency)
                        .withRemittanceReference(RemittanceReferenceType.TEXT, "Nike Bounce Shoes")
                        .execute();

        AssertOpenBankingResponse(transaction);

        System.out.println(transaction.getBankPaymentResponse().getRedirectUrl());
        Thread.sleep(6000);

        TransactionSummary response =
                ReportingService
                        .transactionDetail(transaction.getTransactionId())
                        .execute();

        assertNotNull(response);
        assertEquals(transaction.getTransactionId(), response.getTransactionId());
        assertNotNull(response.getBankPaymentResponse().getSortCode());
        assertNull(response.getBankPaymentResponse().getIban());
        assertNotNull(response.getBankPaymentResponse().getAccountNumber());
    }

    @Test
    public void SEPACharge() throws ApiException, InterruptedException {
        BankPayment bankPayment = SepaConfig();

        Transaction transactionResponse =
                bankPayment
                        .charge(amount)
                        .withCurrency("EUR")
                        .withRemittanceReference(RemittanceReferenceType.TEXT, "Nike Bounce Shoes")
                        .execute();

        AssertOpenBankingResponse(transactionResponse);

        System.out.println(transactionResponse.getBankPaymentResponse().getRedirectUrl());

        Thread.sleep(6000);

        TransactionSummary getTransactionResponse =
                ReportingService
                        .transactionDetail(transactionResponse.getTransactionId())
                        .execute();

        assertNotNull(getTransactionResponse);
        assertEquals(transactionResponse.getTransactionId(), getTransactionResponse.getTransactionId());
        assertNotNull(getTransactionResponse.getBankPaymentResponse().getIban());
        assertNull(getTransactionResponse.getBankPaymentResponse().getSortCode());
        assertNull(getTransactionResponse.getBankPaymentResponse().getAccountNumber());
    }

    @Test
    public void ReportFindOBTransactionsByStartDateAndEndDate() throws ApiException {
        TransactionSummaryPaged response =
                ReportingService
                        .findTransactionsPaged(1, 10)
                        .orderBy(TransactionSortProperty.TimeCreated, null)
                        .where(StartDate, startDate)
                        .and(EndDate, endDate)
                        .and(SearchCriteria.PaymentProvider, PaymentProvider.OPEN_BANKING)
                        .execute();

        assertNotNull(response);

        for (TransactionSummary rs : response.getResults()) {
            assertEquals(EnumUtils.getMapping(Target.GP_API, PaymentMethodName.BankPayment), rs.getPaymentType().toUpperCase());
            assertTrue(DateUtils.isAfterOrEquals(endDate, rs.getTransactionDate().toDate()));
            assertTrue(DateUtils.isBeforeOrEquals(startDate, rs.getTransactionDate().toDate()));
        }
    }

    @Test
    public void FasterPaymentsChargeThenRefund() throws ApiException, InterruptedException {
        BankPayment bankPayment = FasterPaymentsConfig();

        Transaction transaction =
                bankPayment
                        .charge(amount)
                        .withCurrency(currency)
                        .withRemittanceReference(RemittanceReferenceType.TEXT, "Nike Bounce Shoes")
                        .execute();

        AssertOpenBankingResponse(transaction);

        Thread.sleep(3000);

        boolean exceptionCaught = false;
        try {
            transaction
                    .refund(amount)
                    .withCurrency(currency)
                    .execute();
        } catch (BuilderException ex) {
            exceptionCaught = true;
            assertEquals("The Refund is not supported for BankPayment", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void SepaChargeThenRefund() throws ApiException, InterruptedException {
        BankPayment bankPayment = SepaConfig();

        Transaction transaction =
                bankPayment
                        .charge(amount)
                        .withCurrency("EUR")
                        .withRemittanceReference(RemittanceReferenceType.TEXT, "Nike Bounce Shoes")
                        .execute();

        AssertOpenBankingResponse(transaction);

        Thread.sleep(3000);

        boolean exceptionCaught = false;
        try {
            transaction
                    .refund(amount)
                    .withCurrency("EUR")
                    .execute();
        } catch (BuilderException ex) {
            exceptionCaught = true;
            assertEquals("The Refund is not supported for BankPayment", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void FasterPaymentsMissingRemittanceReference() throws ApiException {
        BankPayment bankPayment = FasterPaymentsConfig();

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency(currency)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 502 - Unable to process your request due to an error with a system down stream.", ex.getMessage());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("50046", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void FasterPaymentsMissingRemittanceReferenceType() throws ApiException {
        BankPayment bankPayment = FasterPaymentsConfig();

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency(currency)
                    .withRemittanceReference(null, "Nike Bounce Shoes")
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 502 - Unable to process your request due to an error with a system down stream.", ex.getMessage());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("50046", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void FasterPaymentsMissingRemittanceReferenceValue() throws ApiException {
        BankPayment bankPayment = FasterPaymentsConfig();

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency(currency)
                    .withRemittanceReference(RemittanceReferenceType.TEXT, null)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 502 - Unable to process your request due to an error with a system down stream.", ex.getMessage());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("50046", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void FasterPaymentsMissingReturnUrl() throws ApiException {
        BankPayment bankPayment = FasterPaymentsConfig();
        bankPayment.setReturnUrl(null);

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency(currency)
                    .withRemittanceReference(RemittanceReferenceType.TEXT, "Nike Bounce Shoes")
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 502 - Unable to process your request due to an error with a system down stream.", ex.getMessage());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("50046", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void FasterPaymentsMissingStatusUrl() throws ApiException {
        BankPayment bankPayment = FasterPaymentsConfig();
        bankPayment.setStatusUpdateUrl(null);

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency(currency)
                    .withRemittanceReference(RemittanceReferenceType.TEXT, "Nike Bounce Shoes")
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 502 - Unable to process your request due to an error with a system down stream.", ex.getMessage());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("50046", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void FasterPaymentsMissingAccountNumber() throws ApiException {
        BankPayment bankPayment = FasterPaymentsConfig();
        bankPayment.setAccountNumber(null);

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency(currency)
                    .withRemittanceReference(RemittanceReferenceType.TEXT, "Nike Bounce Shoes")
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 502 - Unable to process your request due to an error with a system down stream.", ex.getMessage());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("50046", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void FasterPaymentsMissingAccountName() throws ApiException {
        BankPayment bankPayment = FasterPaymentsConfig();
        bankPayment.setAccountName(null);

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency(currency)
                    .withRemittanceReference(RemittanceReferenceType.TEXT, "Nike Bounce Shoes")
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields payment_method.bank_transfer.bank.name", ex.getMessage());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40005", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void FasterPaymentsMissingSortCode() throws ApiException {
        BankPayment bankPayment = FasterPaymentsConfig();
        bankPayment.setSortCode(null);

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency(currency)
                    .withRemittanceReference(RemittanceReferenceType.TEXT, "Nike Bounce Shoes")
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 502 - Unable to process your request due to an error with a system down stream.", ex.getMessage());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("50046", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void FasterPaymentsInvalidCurrency() throws ApiException {
        BankPayment bankPayment = FasterPaymentsConfig();

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency("EUR")
                    .withRemittanceReference(RemittanceReferenceType.TEXT, "Nike Bounce Shoes")
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 502 - Unable to process your request due to an error with a system down stream.", ex.getMessage());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("50046", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void SepaMissingIban() throws ApiException {
        BankPayment bankPayment = SepaConfig();
        bankPayment.setIban(null);

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency("EUR")
                    .withRemittanceReference(RemittanceReferenceType.TEXT, "Nike Bounce Shoes")
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 502 - Unable to process your request due to an error with a system down stream.", ex.getMessage());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("50046", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void SepaMissingAccountName() throws ApiException {
        BankPayment bankPayment = SepaConfig();
        bankPayment.setAccountName(null);

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency("EUR")
                    .withRemittanceReference(RemittanceReferenceType.TEXT, "Nike Bounce Shoes")
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields payment_method.bank_transfer.bank.name", ex.getMessage());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40005", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void SepaInvalidCurrency() throws ApiException {
        BankPayment bankPayment = SepaConfig();
        bankPayment.setIban(null);

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency(currency)
                    .withRemittanceReference(RemittanceReferenceType.TEXT, "Nike Bounce Shoes")
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 502 - Unable to process your request due to an error with a system down stream.", ex.getMessage());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("50046", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void SepaChargeCADCurrency() throws ApiException {
        BankPayment bankPayment = SepaConfig();
        bankPayment.setIban(null);

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency("CAD")
                    .withRemittanceReference(RemittanceReferenceType.TEXT, "Nike Bounce Shoes")
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 502 - Unable to process your request due to an error with a system down stream.", ex.getMessage());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("50046", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    private void AssertOpenBankingResponse(Transaction trn) {
        assertEquals(TransactionStatus.Initiated.toString().toUpperCase(), trn.getResponseMessage().toUpperCase());
        assertNotNull(trn.getTransactionId());
        assertNotNull(trn.getBankPaymentResponse().getRedirectUrl());
    }

    private BankPayment FasterPaymentsConfig() {
        BankPayment bankPayment = new BankPayment();

        bankPayment.setAccountNumber("99999999");
        bankPayment.setSortCode("407777");
        bankPayment.setAccountName("Minal");
        bankPayment.setCountries(Arrays.asList("GB", "IE"));
        bankPayment.setReturnUrl("https://7b8e82a17ac00346e91e984f42a2a5fb.m.pipedream.net");
        bankPayment.setStatusUpdateUrl("https://7b8e82a17ac00346e91e984f42a2a5fb.m.pipedream.net");

        return bankPayment;
    }

    private BankPayment SepaConfig() {
        BankPayment bankPayment = new BankPayment();
        bankPayment.setIban("GB33BUKB20201555555555");
        bankPayment.setAccountName("AccountName");
        bankPayment.setReturnUrl("https://7b8e82a17ac00346e91e984f42a2a5fb.m.pipedream.net");
        bankPayment.setStatusUpdateUrl("https://7b8e82a17ac00346e91e984f42a2a5fb.m.pipedream.net");

        return bankPayment;
    }

}
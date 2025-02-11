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

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Arrays;
import static com.global.api.entities.reporting.SearchCriteria.EndDate;
import static com.global.api.entities.reporting.SearchCriteria.StartDate;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GpApiOpenBankingTest extends BaseGpApiTest {
    private static final String currency = "GBP";
    private static final BigDecimal amount = new BigDecimal("10.99");

    public GpApiOpenBankingTest() throws ApiException {

        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        ServicesContainer.configureService(config);
    }

    @Test
    @Order(1)
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
        assertNotNull(response.getAccountNumberLast4());
        assertNotNull(response.getBankPaymentResponse().getSortCode());
        assertNotNull(response.getBankPaymentResponse().getAccountName());
        assertNull(response.getBankPaymentResponse().getIban());
    }

    @Test
    @Order(2)
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
        assertNotNull(getTransactionResponse.getBankPaymentResponse().getMaskedIbanLast4());
        assertNull(getTransactionResponse.getBankPaymentResponse().getSortCode());
        assertNull(getTransactionResponse.getBankPaymentResponse().getAccountNumber());
        assertNull(getTransactionResponse.getAccountNumberLast4());
    }

    @Test
    @Order(3)
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
            assertTrue(DateUtils.isAfterOrEquals(rs.getTransactionDate().toDate(), startDate));
            assertTrue(DateUtils.isBeforeOrEquals(rs.getTransactionDate().toDate(), endDate));
        }
    }

    @Test
    @Order(4)
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
    @Order(5)
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
    @Order(6)
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
    @Order(7)
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
    @Order(8)
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
    @Order(9)
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
            assertEquals("Status Code: 400 - return_url value is invalid. Please check the format and data provided is correct.", ex.getMessage());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40090", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(10)
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
            assertEquals("Status Code: 400 - status_url value is invalid. Please check the format and data provided is correct.", ex.getMessage());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40090", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(11)
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
    @Order(12)
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
            assertEquals("Status Code: 400 - payment_method.bank_transfer.bank.name value is invalid. Please check the format and data provided is correct.", ex.getMessage());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40090", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(13)
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
    @Order(14)
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
    @Order(15)
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
    @Order(16)
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
            assertEquals("Status Code: 400 - payment_method.bank_transfer.bank.name value is invalid. Please check the format and data provided is correct.", ex.getMessage());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40090", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(17)
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
    @Order(18)
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

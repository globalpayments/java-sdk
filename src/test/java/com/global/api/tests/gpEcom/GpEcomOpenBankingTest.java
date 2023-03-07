package com.global.api.tests.gpEcom;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.BankPaymentStatus;
import com.global.api.entities.enums.RemittanceReferenceType;
import com.global.api.entities.enums.ShaHashType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.entities.reporting.TransactionSummaryPaged;
import com.global.api.paymentMethods.BankPayment;
import com.global.api.serviceConfigs.GpEcomConfig;
import com.global.api.services.ReportingService;
import lombok.var;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static com.global.api.entities.enums.BankPaymentStatus.PAYMENT_INITIATED;
import static org.junit.Assert.*;

public class GpEcomOpenBankingTest {

    private final BigDecimal amount = new BigDecimal("7.8");
    private final String currency = "GBP";

    public GpEcomOpenBankingTest() throws ApiException {
        GpEcomConfig config = new GpEcomConfig();
        config.setMerchantId("openbankingsandbox");
        config.setSharedSecret("sharedsecret");
        config.setAccountId("internet");
        config.setEnableBankPayment(true);
        config.setShaHashType(ShaHashType.SHA512);
        config.setEnableLogging(true);

        ServicesContainer.configureService(config);
    }

    @Test
    public void OpenBanking_FasterPaymentsCharge() throws ApiException, InterruptedException {
        BankPayment bankPayment = fasterPaymentConfig();

        Transaction transaction =
                bankPayment
                        .charge(amount)
                        .withCurrency(currency)
                        .withRemittanceReference(RemittanceReferenceType.TEXT, "Nike Bounce Shoes")
                        .execute();

        assertTransactionResponse(transaction);

        Thread.sleep(2000);

        TransactionSummaryPaged detail =
                ReportingService
                        .bankPaymentDetail(transaction.getBankPaymentResponse().getId(), 1, 10)
                        .execute();

        assertNotNull(detail);
        // TODO: These fields are not being retrieved from the related endpoint. Enable when fixed.
        // assertNotNull(detail.getResults().get(0).getBankPaymentResponse().getSortCode());
        // assertNotNull(detail.getResults().get(0).getBankPaymentResponse().getAccountNumber());
        // assertNotNull(detail.getResults().get(0).getBankPaymentResponse().getAccountName());
        // assertNotNull(detail.getResults().get(0).getBankPaymentResponse().getIban());
    }

    @Test
    public void OpenBanking_FasterPaymentsCharge_AllSHATypes() throws ApiException, InterruptedException {
        for (ShaHashType shaHashType : ShaHashType.values()) {
            var config = new GpEcomConfig();
            config.setMerchantId("openbankingsandbox");
            config.setSharedSecret("sharedsecret");
            config.setAccountId("internet");
            config.setEnableBankPayment(true);
            config.setShaHashType(shaHashType);

            ServicesContainer.configureService(config, shaHashType.toString());

            var bankPayment = fasterPaymentConfig();

            var transaction =
                    bankPayment
                            .charge(amount)
                            .withCurrency(currency)
                            .withRemittanceReference(RemittanceReferenceType.TEXT, "Nike Bounce Shoes")
                            .execute(shaHashType.toString());

            assertTransactionResponse(transaction);

            Thread.sleep(2000);

            var detail =
                    ReportingService
                            .bankPaymentDetail(transaction.getBankPaymentResponse().getId(), 1, 10)
                            .execute(shaHashType.toString());

            assertNotNull(detail);
            assertNull(detail.getResults().get(0).getBankPaymentResponse().getSortCode());
            assertNull(detail.getResults().get(0).getBankPaymentResponse().getAccountNumber());
            assertNull(detail.getResults().get(0).getBankPaymentResponse().getAccountName());
            assertNull(detail.getResults().get(0).getBankPaymentResponse().getIban());
        }
    }

    @Test
    public void OpenBanking_SepaCharge() throws ApiException, InterruptedException {
        BankPayment bankPayment = SepaConfig();

        Transaction transaction =
                bankPayment
                        .charge(amount)
                        .withCurrency("EUR")
                        .withRemittanceReference(RemittanceReferenceType.TEXT, "Nike Bounce Shoes")
                        .execute();

        assertTransactionResponse(transaction);

        Thread.sleep(2000);

        TransactionSummaryPaged detail =
                ReportingService
                        .bankPaymentDetail(transaction.getBankPaymentResponse().getId(), 1, 10)
                        .execute();

        assertNotNull(detail);
        // TODO: These fields are not being retrieved from the related endpoint. Enable when fixed.
        // assertNotNull(detail.getResults().get(0).getBankPaymentResponse().getSortCode());
        // assertNotNull(detail.getResults().get(0).getBankPaymentResponse().getAccountNumber());
        // assertNotNull(detail.getResults().get(0).getBankPaymentResponse().getAccountName());
        // assertNotNull(detail.getResults().get(0).getBankPaymentResponse().getIban());
    }

    @Test
    public void OpenBanking_BankPaymentList() throws ApiException {
        TransactionSummaryPaged result =
                ReportingService
                        .findBankPaymentTransactions(1, 10)
                        .where(SearchCriteria.StartDate, LocalDate.now().plusDays(-5).toDate())
                        .and(SearchCriteria.EndDate, LocalDate.now().toDate())
                        .execute();

        assertNotNull(result);
        assertNotEquals(0, result.getResults().size());
    }

    @Test
    public void OpenBanking_BankPaymentList_EmptyList() throws ApiException {
        final BankPaymentStatus status = BankPaymentStatus.REQUEST_CONSUMER_CONSENT;

        var result =
                ReportingService
                        .findBankPaymentTransactions(1, 10)
                        .where(SearchCriteria.StartDate, LocalDate.now().plusDays(-5).toDate())
                        .and(SearchCriteria.EndDate, LocalDate.now().toDate())
                        .and(SearchCriteria.BankPaymentStatus, status)
                        .execute();

        assertNotNull(result);
        // TODO: 1 element is retrieved. Enable when fixed
        // assertEquals(0, result.getResults().size());
    }

    @Test
    public void OpenBanking_BankPaymentList_WithReturnPii() throws ApiException {
        var result =
                ReportingService
                        .findBankPaymentTransactions(1, 10)
                        .where(SearchCriteria.StartDate, LocalDate.now().plusDays(-5).toDate())
                        .and(SearchCriteria.EndDate, LocalDate.now().toDate())
                        .and(SearchCriteria.ReturnPII, true)
                        .execute();

        assertNotNull(result);
        assertNotEquals(0, result.getResults().size());

        for (var item : result.getResults()) {
            if (item.getTransactionType() != null) {
                continue;
            }

            switch (item.getBankPaymentResponse().getType().toString()) {
                case "FASTERPAYMENTS":
                    assertNotNull(item.getBankPaymentResponse().getSortCode());
                    assertNotNull(item.getBankPaymentResponse().getAccountNumber());
                    assertNotNull(item.getBankPaymentResponse().getAccountName());
                    break;
                case "SEPA":
                    assertNotNull(item.getBankPaymentResponse().getIban());
                    assertNotNull(item.getBankPaymentResponse().getAccountName());
            }
        }
    }

    @Test
    public void OpenBanking_GetBankPaymentById() throws ApiException {
        String obTransId = "7f2eHrHPkLxyq0zYXC";

        TransactionSummaryPaged detail =
                ReportingService
                        .bankPaymentDetail(obTransId, 1, 10)
                        .execute();

        assertNotNull(detail);
        assertEquals(1, detail.getResults().size());
    }

    @Test
    public void OpenBanking_GetBankPaymentById_RandomId() throws ApiException {
        String obTransId = UUID.randomUUID().toString().replace("-", "").substring(0, 18);

        TransactionSummaryPaged detail =
                ReportingService
                        .bankPaymentDetail(obTransId, 1, 10)
                        .execute();

        assertNotNull(detail);
        assertEquals(0, detail.getResults().size());
    }

    @Test
    public void OpenBanking_GetBankPaymentById_InvalidId() throws ApiException {
        String obTransId = UUID.randomUUID().toString().replace("-", "");

        boolean exceptionCaught = false;
        try {
            ReportingService
                    .bankPaymentDetail(obTransId, 1, 10)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertTrue(ex.getResponseText().contains("obTransId is invalid"));
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void OpenBanking_AllPaymentDetails() throws ApiException {
        BankPayment bankPayment = fasterPaymentConfig();
        bankPayment.setIban("123456");

        Transaction transaction =
                bankPayment
                        .charge(amount)
                        .withCurrency(currency)
                        .withRemittanceReference(RemittanceReferenceType.TEXT, "Nike Bounce Shoes")
                        .execute();

        assertTransactionResponse(transaction);
    }

    @Test
    public void OpenBanking_FasterPaymentsCharge_CADCurrency() throws ApiException {
        BankPayment bankPayment = fasterPaymentConfig();

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency("CAD")
                    .withRemittanceReference(RemittanceReferenceType.TEXT, "Nike Bounce Shoes")
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertTrue(ex.getResponseText().contains("Merchant currency is not enabled for Open Banking"));
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void OpenBanking_FasterPayments_MissingRemittanceReference() throws ApiException {
        BankPayment bankPayment = fasterPaymentConfig();

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency(currency)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertTrue(ex.getResponseText().contains("payment.remittance_reference cannot be null"));
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void OpenBanking_FasterPayments_MissingRemittanceReferenceValue() throws ApiException {
        BankPayment bankPayment = fasterPaymentConfig();

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency(currency)
                    .withRemittanceReference(RemittanceReferenceType.TEXT, null)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertTrue(ex.getResponseText().contains("remittance_reference.value cannot be blank or null"));
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void OpenBanking_FasterPayments_MissingRemittanceReferenceType() throws ApiException {
        BankPayment bankPayment = fasterPaymentConfig();

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency(currency)
                    .withRemittanceReference(null, "Nike Shoes")
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertTrue(ex.getResponseText().contains("remittance_reference.type cannot be blank or null"));
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void OpenBanking_FasterPayments_MissingReturnUrl() throws ApiException {
        BankPayment bankPayment = fasterPaymentConfig();
        bankPayment.setReturnUrl(null);

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency(currency)
                    .withRemittanceReference(RemittanceReferenceType.TEXT, "Nike Shoes")
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertTrue(ex.getResponseText().contains("return_url must not be null"));
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void OpenBanking_FasterPayments_MissingStatusUrl() throws ApiException {
        BankPayment bankPayment = fasterPaymentConfig();
        bankPayment.setStatusUpdateUrl(null);

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency(currency)
                    .withRemittanceReference(RemittanceReferenceType.TEXT, "Nike Shoes")
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertTrue(ex.getResponseText().contains("status_url must not be null"));
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void OpenBanking_FasterPayments_MissingAccountNumber() throws ApiException {
        BankPayment bankPayment = fasterPaymentConfig();
        bankPayment.setAccountNumber(null);

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency(currency)
                    .withRemittanceReference(RemittanceReferenceType.TEXT, "Nike Shoes")
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertTrue(ex.getResponseText().contains("Invalid Payment Scheme required fields"));
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void OpenBanking_FasterPayments_MissingSortCode() throws ApiException {
        BankPayment bankPayment = fasterPaymentConfig();
        bankPayment.setSortCode(null);

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency(currency)
                    .withRemittanceReference(RemittanceReferenceType.TEXT, "Nike Shoes")
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertTrue(ex.getResponseText().contains("Invalid Payment Scheme required fields"));
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void OpenBanking_FasterPayments_MissingAccountName() throws ApiException {
        BankPayment bankPayment = fasterPaymentConfig();
        bankPayment.setAccountName(null);

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency(currency)
                    .withRemittanceReference(RemittanceReferenceType.TEXT, "Nike Shoes")
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertTrue(ex.getResponseText().contains("payment.destination.name is invalid"));
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void OpenBanking_FasterPayments_InvalidCurrency() throws ApiException {
        BankPayment bankPayment = fasterPaymentConfig();

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency("EUR")
                    .withRemittanceReference(RemittanceReferenceType.PAN, "Nike Bounce Shoes")
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertTrue(ex.getResponseText().contains("Invalid Payment Scheme required fields"));
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void OpenBanking_Sepa_MissingIban() throws ApiException {
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
            assertTrue(ex.getResponseText().contains("Invalid Payment Scheme required fields"));
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void OpenBanking_Sepa_MissingName() throws ApiException {
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
            assertTrue(ex.getResponseText().contains("payment.destination.name is invalid"));
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void OpenBanking_Sepa_InvalidCurrency() throws ApiException {
        BankPayment bankPayment = SepaConfig();

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency(currency)
                    .withRemittanceReference(RemittanceReferenceType.TEXT, "Nike Bounce Shoes")
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertTrue(ex.getResponseText().contains("Invalid Payment Scheme required fields"));
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    private BankPayment fasterPaymentConfig() {
        return new BankPayment()
                .setAccountNumber("12345678")
                .setSortCode("406650")
                .setAccountName("GpEcom Testing")
                .setReturnUrl("https://7b8e82a17ac00346e91e984f42a2a5fb.m.pipedream.net")
                .setStatusUpdateUrl("https://7b8e82a17ac00346e91e984f42a2a5fb.m.pipedream.net");
    }

    private BankPayment SepaConfig() {
        return new BankPayment()
                .setIban("123456")
                .setAccountName("GpEcom Testing")
                .setReturnUrl("https://7b8e82a17ac00346e91e984f42a2a5fb.m.pipedream.net")
                .setStatusUpdateUrl("https://7b8e82a17ac00346e91e984f42a2a5fb.m.pipedream.net");
    }

    private void assertTransactionResponse(Transaction transaction) {
        assertNotNull(transaction);
        assertNotNull(transaction.getTransactionId());
        assertNotNull(transaction.getOrderId());
        assertNotNull(transaction.getBankPaymentResponse().getId());
        assertNotNull(transaction.getBankPaymentResponse().getRedirectUrl());
        assertEquals(PAYMENT_INITIATED.toString(), transaction.getResponseMessage());
    }

}

package com.global.api.tests.gpEcom;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.enums.BankPaymentStatus;
import com.global.api.entities.enums.BankPaymentType;
import com.global.api.entities.enums.RemittanceReferenceType;
import com.global.api.entities.enums.ShaHashType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.entities.reporting.TransactionSummaryPaged;
import com.global.api.paymentMethods.BankPayment;
import com.global.api.serviceConfigs.GpEcomConfig;
import com.global.api.services.ReportingService;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static com.global.api.entities.enums.BankPaymentStatus.PAYMENT_INITIATED;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class GpEcomOpenBankingTest {

    private final String currency = "GBP";

    private final BigDecimal amount = new BigDecimal("10.99");

    private final String remittanceReferenceValue = "Nike Bounce Shoes";

    private boolean runAuto = true;


    public GpEcomOpenBankingTest() throws ApiException {
        GpEcomConfig config = getConfig();
        ServicesContainer.configureService(config);
    }

    private GpEcomConfig getConfig() {
        GpEcomConfig config = new GpEcomConfig();
        config.setMerchantId("openbankingsandbox");
        config.setSharedSecret("sharedsecret");
        config.setAccountId("internet3");
        config.setShaHashType(ShaHashType.SHA512);
        config.setEnableLogging(true);
        return config;
    }

    @Test
    public void OpenBanking_FasterPaymentsCharge() throws ApiException, InterruptedException {

        BankPayment bankPayment = fasterPaymentConfig();

        Transaction transaction =
                bankPayment
                        .charge(amount)
                        .withCurrency(currency)
                        .withRemittanceReference(RemittanceReferenceType.TEXT, remittanceReferenceValue)
                        .execute();

        assertTransactionResponse(transaction);

        System.out.println();
        System.out.println(transaction.getBankPaymentResponse().getRedirectUrl());
        System.out.println();
        Thread.sleep(2000);

        TransactionSummaryPaged response = ReportingService.bankPaymentDetail(
                transaction.getBankPaymentResponse().getId(), 1, 10)
                .execute();

        assertNotNull(response);
        assertEquals(1, response.getTotalRecordCount());
        assertEquals(transaction.getBankPaymentResponse().getId(), response.getResults().get(0).getTransactionId());
//        assertNotNull(response.getResults().get(0).getBankPaymentResponse().getIban());
//        assertNotNull(response.getResults().get(0).getBankPaymentResponse().getSortCode());
//        assertNotNull(response.getResults().get(0).getBankPaymentResponse().getAccountNumber());
//        assertNotNull(response.getResults().get(0).getBankPaymentResponse().getAccountName());
//        assertNotNull(response.getResults().get(0).getBankPaymentResponse().getTokenRequestId());
//        assertNotNull(response.getResults().get(0).getOrderId());
//        assertNotNull(response.getResults().get(0).getBankPaymentResponse().getId());
        assertEquals(BankPaymentType.FASTERPAYMENTS, response.getResults().get(0).getBankPaymentResponse().getType());
        assertEquals(BankPaymentStatus.SUCCESS.name(), response.getResults().get(0).getBankPaymentResponse().getPaymentStatus());
    }

    /**
     * In order to be able to run the full flow for refund you need to set the "runAuto" property to false.
     * Open the redirect url printed in a browser and continue the flow.
     */
    @Test
    public void OpenBanking_FasterPaymentsRefund() throws ApiException, InterruptedException {

        BankPayment bankPayment = fasterPaymentConfig();

        Transaction transaction =
                bankPayment
                        .charge(amount)
                        .withCurrency(currency)
                        .withRemittanceReference(RemittanceReferenceType.TEXT, remittanceReferenceValue)
                        .execute();


        assertTransactionResponse(transaction);

        if (this.runAuto) {
            return;
        }

        System.out.println();
        System.out.println(transaction.getBankPaymentResponse().getRedirectUrl());
        System.out.println();
        Thread.sleep(2000);

        Transaction refund = transaction.refund(amount)
                .withCurrency(currency)
                .execute();

        assertEquals(BankPaymentStatus.INITIATION_PROCESSING, refund.getResponseMessage());
        assertNotNull(refund.getTransactionId());
        assertNotNull(refund.getClientTransactionId());
        assertNull(refund.getBankPaymentResponse().getRedirectUrl());

        TransactionSummaryPaged response = ReportingService.bankPaymentDetail(
                        transaction.getBankPaymentResponse().getId(), 1, 10)
                .execute();

        assertNotNull(response);
        assertEquals(1, response.getTotalRecordCount());
        assertEquals(transaction.getBankPaymentResponse().getId(), response.getResults().get(0).getTransactionId());
//        assertNotNull(response.getResults().get(0).getBankPaymentResponse().getIban());
//        assertNotNull(response.getResults().get(0).getBankPaymentResponse().getSortCode());
//        assertNotNull(response.getResults().get(0).getBankPaymentResponse().getAccountNumber());
//        assertNotNull(response.getResults().get(0).getBankPaymentResponse().getAccountName());
//        assertNotNull(response.getResults().get(0).getBankPaymentResponse().getTokenRequestId());
//        assertNotNull(response.getResults().get(0).getOrderId());
//        assertNotNull(response.getResults().get(0).getBankPaymentResponse().getId());
        assertEquals(BankPaymentType.FASTERPAYMENTS, response.getResults().get(0).getBankPaymentResponse().getType());
        assertEquals(BankPaymentStatus.SUCCESS.name(), response.getResults().get(0).getBankPaymentResponse().getPaymentStatus());
    }

    @Test
    public void OpenBanking_SepaCharge() throws ApiException, InterruptedException {

        BankPayment bankPayment = SepaConfig();

        Transaction transaction =
                bankPayment
                        .charge(amount)
                        .withCurrency("EUR")
                        .withRemittanceReference(RemittanceReferenceType.TEXT, remittanceReferenceValue)
                        .execute();

        assertTransactionResponse(transaction);

        System.out.println();
        System.out.println(transaction.getBankPaymentResponse().getRedirectUrl());
        System.out.println();
        Thread.sleep(2000);

        TransactionSummaryPaged response =
                ReportingService
                        .bankPaymentDetail(transaction.getBankPaymentResponse().getId(), 1, 10)
                        .execute();

        assertNotNull(response);
        assertEquals(1, response.getTotalRecordCount());
        assertEquals(transaction.getBankPaymentResponse().getId(), response.getResults().get(0).getTransactionId());
//        assertNotNull(response.getResults().get(0).getBankPaymentResponse().getIban());
//        assertNotNull(response.getResults().get(0).getBankPaymentResponse().getSortCode());
//        assertNotNull(response.getResults().get(0).getBankPaymentResponse().getAccountNumber());
//        assertNotNull(response.getResults().get(0).getBankPaymentResponse().getAccountName());
//        assertNotNull(response.getResults().get(0).getBankPaymentResponse().getTokenRequestId());
//        assertNotNull(response.getResults().get(0).getOrderId());
//        assertNotNull(response.getResults().get(0).getBankPaymentResponse().getId());
        assertEquals(BankPaymentType.SEPA, response.getResults().get(0).getBankPaymentResponse().getType());
        assertEquals(BankPaymentStatus.SUCCESS.name(), response.getResults().get(0).getBankPaymentResponse().getPaymentStatus());
    }

    /**
     * In order to be able to run the full flow for refund you need to set the "runAuto" property to false.
     * Open the redirect url printed in a browser and continue the flow.
     */
    @Test
    public void SEPARefund() throws ApiException, InterruptedException {
        BankPayment bankPayment = SepaConfig();

        Transaction trn =
                bankPayment
                        .charge(amount)
                        .withCurrency("EUR")
                        .withRemittanceReference(RemittanceReferenceType.TEXT, remittanceReferenceValue)
                        .execute();

        assertTransactionResponse(trn);

        if (this.runAuto) {
            return;
        }


        System.out.println();
        System.out.println(trn.getBankPaymentResponse().getRedirectUrl());
        System.out.println();
        Thread.sleep(2000);
        
        Transaction refund =
                trn
                        .refund(amount)
                        .withCurrency("EUR")
                        .execute();

        assertEquals(BankPaymentStatus.INITIATION_PROCESSING.toString(), refund.getResponseMessage());
        assertNotNull(refund.getTransactionId());
        assertNotNull(refund.getClientTransactionId());
        assertNotNull(refund.getBankPaymentResponse().getRedirectUrl());

        TransactionSummaryPaged response = ReportingService
                .bankPaymentDetail(trn.getBankPaymentResponse().getId(), 1, 1)
                .execute();

        assertNotNull(response);

        assertEquals(1, response.getTotalRecordCount());

        assertEquals(trn.getBankPaymentResponse().getId(), response.getResults().get(0).getTransactionId());

        assertNull(response.getResults().get(0).getBankPaymentResponse().getIban());
        assertNull(response.getResults().get(0).getBankPaymentResponse().getSortCode());
        assertNull(response.getResults().get(0).getBankPaymentResponse().getAccountNumber());
        assertNull(response.getResults().get(0).getBankPaymentResponse().getAccountName());
//        assertNotNull(response.getResults().get(0).getBankPaymentResponse().getTokenRequestId());
//        assertNotNull(response.getResults().get(0).getOrderId());
//        assertNotNull(response.getResults().get(0).getBankPaymentResponse().getId());
        assertEquals(BankPaymentType.SEPA, response.getResults().get(0).getBankPaymentResponse().getType());
        assertEquals(BankPaymentStatus.SUCCESS.name(), response.getResults().get(0).getBankPaymentResponse().getPaymentStatus());
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

        TransactionSummaryPaged result =
                ReportingService
                        .findBankPaymentTransactions(1, 10)
                        .where(SearchCriteria.StartDate, LocalDate.now().plusDays(-5).toDate())
                        .and(SearchCriteria.EndDate, LocalDate.now().toDate())
                        .and(SearchCriteria.BankPaymentStatus, status)
                        .execute();

        assertNotNull(result);
        assertEquals(0, result.getResults().size());
    }

    @Test
    public void OpenBanking_FasterPaymentsCharge_UsingRemittanceReferenceAsPAN() throws ApiException, InterruptedException {
        BankPayment bankPayment = fasterPaymentConfig();

        Transaction transaction =
                bankPayment
                        .charge(amount)
                        .withCurrency(currency)
                        .withRemittanceReference(RemittanceReferenceType.PAN, "4263970000005262")
                        .execute();

        assertTransactionResponse(transaction);

        System.out.println();
        System.out.println(transaction.getBankPaymentResponse().getRedirectUrl());
        System.out.println();
        Thread.sleep(2000);

        TransactionSummaryPaged detail =
                ReportingService
                        .bankPaymentDetail(transaction.getBankPaymentResponse().getId(), 1, 10)
                        .execute();

        assertNotNull(detail);
    }

    @Test
    public void OpenBanking_FasterPaymentsCharge_AllSHATypes() throws ApiException, InterruptedException {
        for (ShaHashType shaHashType : ShaHashType.values()) {
            GpEcomConfig config = new GpEcomConfig();
            config.setMerchantId("openbankingsandbox");
            config.setSharedSecret("sharedsecret");
            config.setAccountId("internet");
            config.setEnableBankPayment(true);
            config.setShaHashType(shaHashType);

            ServicesContainer.configureService(config, shaHashType.toString());

            BankPayment bankPayment = fasterPaymentConfig();

            Transaction transaction =
                    bankPayment
                            .charge(amount)
                            .withCurrency(currency)
                            .withRemittanceReference(RemittanceReferenceType.TEXT, remittanceReferenceValue)
                            .execute(shaHashType.toString());

            assertTransactionResponse(transaction);

            System.out.println();
            System.out.println(transaction.getBankPaymentResponse().getRedirectUrl());
            System.out.println();
            Thread.sleep(2000);

            TransactionSummaryPaged detail =
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
    public void OpenBanking_BankPaymentList_WithReturnPii() throws ApiException {
        TransactionSummaryPaged result =
                ReportingService
                        .findBankPaymentTransactions(1, 10)
                        .where(SearchCriteria.StartDate, LocalDate.now().plusDays(-29).toDate())
                        .and(SearchCriteria.EndDate, LocalDate.now().plusDays(-1).toDate())
                        .and(SearchCriteria.ReturnPII, true)
                        .execute();

        assertNotNull(result);
        assertNotEquals(0, result.getResults().size());

        for (TransactionSummary item : result.getResults()) {
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
                        .withRemittanceReference(RemittanceReferenceType.TEXT, remittanceReferenceValue)
                        .execute();

        assertTransactionResponse(transaction);
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
    public void OpenBanking_FasterPayments_MissingRemittanceReferenceType() throws ApiException {
        BankPayment bankPayment = fasterPaymentConfig();

        Transaction transaction = bankPayment
                .charge(amount)
                .withCurrency(currency)
                .withRemittanceReference(null, remittanceReferenceValue)
                .execute();
        assertTransactionResponse(transaction);
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
    public void OpenBanking_FasterPayments_MissingReturnUrl() throws ApiException {
        BankPayment bankPayment = fasterPaymentConfig();
        bankPayment.setReturnUrl(null);

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency(currency)
                    .withRemittanceReference(RemittanceReferenceType.TEXT, remittanceReferenceValue)
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
                    .withRemittanceReference(RemittanceReferenceType.TEXT, remittanceReferenceValue)
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
                    .withRemittanceReference(RemittanceReferenceType.TEXT, remittanceReferenceValue)
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
                    .withRemittanceReference(RemittanceReferenceType.PAN, remittanceReferenceValue)
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
                    .withRemittanceReference(RemittanceReferenceType.TEXT, remittanceReferenceValue)
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
                    .withRemittanceReference(RemittanceReferenceType.TEXT, remittanceReferenceValue)
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
                    .withRemittanceReference(RemittanceReferenceType.TEXT, remittanceReferenceValue)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertTrue(ex.getResponseText().contains("Invalid Payment Scheme required fields"));
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void OpenBanking_FasterPaymentsCharge_CADCurrency() throws ApiException {
        BankPayment bankPayment = fasterPaymentConfig();

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency("CAD")
                    .withRemittanceReference(RemittanceReferenceType.TEXT, remittanceReferenceValue)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertTrue(ex.getResponseText().contains("Invalid Payment Scheme required fields"));
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void OpenBanking_FasterPayments_RemittanceValueMoreThan18Chars() throws ApiException {
        BankPayment bankPayment = fasterPaymentConfig();

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency(currency)
                    .withRemittanceReference(RemittanceReferenceType.PAN, "Nike Bounce Shoes Like Lebron")
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertTrue(ex.getResponseText().contains("remittance_reference.value is of invalid length"));
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void OpenBanking_FasterPayments_RemittanceValueLessThan2Chars() throws ApiException {
        BankPayment bankPayment = fasterPaymentConfig();

        boolean exceptionCaught = false;
        try {
            bankPayment
                    .charge(amount)
                    .withCurrency(currency)
                    .withRemittanceReference(RemittanceReferenceType.PAN, "N")
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertTrue(ex.getResponseText().contains("remittance_reference.value is of invalid length"));
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    private BankPayment fasterPaymentConfig() {
        return new BankPayment()
                .setAccountNumber("12345678")
                .setSortCode("406650")
                .setAccountName("AccountName")
                .setReturnUrl("https://7b8e82a17ac00346e91e984f42a2a5fb.m.pipedream.net")
                .setStatusUpdateUrl("https://7b8e82a17ac00346e91e984f42a2a5fb.m.pipedream.net");
    }

    private BankPayment SepaConfig() {
        return new BankPayment()
                .setIban("123456")
                .setAccountName("AccountName")
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

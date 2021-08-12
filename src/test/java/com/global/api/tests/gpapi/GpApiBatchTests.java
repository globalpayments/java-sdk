package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.enums.TransactionStatus;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.BatchService;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class GpApiBatchTests extends BaseGpApiTest {

    private final CreditTrackData creditCard;
    private final BigDecimal amount = new BigDecimal("1.00");
    private final String CURRENCY = "USD";
    private final String TAG_DATA = "82021C008407A0000002771010950580000000009A031709289C01005F280201245F2A0201245F3401019F02060000000010009F03060000000000009F080200019F090200019F100706010A03A420009F1A0201249F26089CC473F4A4CE18D39F2701809F3303E0F8C89F34030100029F3501229F360200639F370435EFED379F410400000019";

    public GpApiBatchTests() throws ConfigurationException {
        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                // These credentials have permissions for executing BATCH
                .setAppId(APP_ID_FOR_BATCH)
                .setAppKey(APP_KEY_FOR_BATCH)
                .setChannel(Channel.CardPresent.getValue());

        config.setEnableLogging(true);

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);

        creditCard = new CreditTrackData();
        creditCard.setValue("%B4012002000060016^VI TEST CREDIT^251210118039000000000396?;4012002000060016=25121011803939600000?");
        creditCard.setEntryMethod(EntryMethod.Swipe);
    }

    @Test
    public void CloseBatch() throws ApiException, InterruptedException {
        Transaction chargeTransaction =
                creditCard
                        .charge(amount)
                        .withCurrency(CURRENCY)
                        .execute(GP_API_CONFIG_NAME);

        assertTransactionResponse(chargeTransaction, TransactionStatus.Captured);

        //TODO - remove when api fix polling issue
        Thread.sleep(1000);

        BatchSummary batchSummary = BatchService.closeBatch(chargeTransaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME);
        assertBatchCloseResponse(batchSummary, amount);
    }

    @Test
    public void CloseBatch_ChipTransaction() throws ApiException, InterruptedException {
        Transaction transaction =
                creditCard
                        .charge(amount)
                        .withCurrency(CURRENCY)
                        .withTagData(TAG_DATA)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        //TODO - remove when api fix polling issue
        Thread.sleep(1000);

        BatchSummary batchSummary = BatchService.closeBatch(transaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME);
        assertBatchCloseResponse(batchSummary, amount);
    }

    @Test
    public void CloseBatch_AuthAndCapture() throws ApiException, InterruptedException {
        Transaction authTransaction =
                creditCard
                        .authorize(amount)
                        .withCurrency(CURRENCY)
                        .execute(GP_API_CONFIG_NAME);

        assertTransactionResponse(authTransaction, TransactionStatus.Preauthorized);

        Transaction captureTransaction =
                authTransaction
                        .capture(amount)
                        .withCurrency(CURRENCY)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(captureTransaction, TransactionStatus.Captured);

        //TODO - remove when api fix polling issue
        Thread.sleep(1000);

        BatchSummary batchSummary = BatchService.closeBatch(captureTransaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME);
        assertBatchCloseResponse(batchSummary, amount);
    }

    @Test
    public void CloseBatch_ContactlessTransaction() throws ApiException, InterruptedException {
        DebitTrackData debitCard = new DebitTrackData();
        debitCard.setValue(";4024720012345671=18125025432198712345?");
        debitCard.setEntryMethod(EntryMethod.Proximity);
        debitCard.setPinBlock("AFEC374574FC90623D010000116001EE");

        Transaction transaction =
                debitCard
                        .charge(amount)
                        .withCurrency(CURRENCY)
                        .withTagData(TAG_DATA)
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);

        assertTransactionResponse(transaction, TransactionStatus.Captured);

        //TODO - remove when api fix polling issue
        Thread.sleep(1000);

        BatchSummary batchSummary = BatchService.closeBatch(transaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME);
        assertBatchCloseResponse(batchSummary, amount);
    }

    @Test
    public void CloseBatch_MultipleChargeCreditTrackData() throws ApiException, InterruptedException {
        Transaction firstTransaction =
                creditCard
                        .charge(new BigDecimal("1.25"))
                        .withCurrency(CURRENCY)
                        .execute(GP_API_CONFIG_NAME);

        assertTransactionResponse(firstTransaction, TransactionStatus.Captured);

        Transaction secondTransaction =
                creditCard
                        .charge(new BigDecimal("2.03"))
                        .withCurrency(CURRENCY)
                        .execute(GP_API_CONFIG_NAME);

        assertTransactionResponse(secondTransaction, TransactionStatus.Captured);

        //TODO - remove when api fix polling issue
        Thread.sleep(1000);

        BatchSummary batchSummary = BatchService.closeBatch(secondTransaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME);
        assertBatchCloseResponse(batchSummary, new BigDecimal("3.28"));
    }

    @Test
    public void CloseBatch_Refund_CreditTrackData() throws ApiException, InterruptedException {
        Transaction transaction =
                creditCard
                        .charge(amount)
                        .withCurrency(CURRENCY)
                        .execute(GP_API_CONFIG_NAME);

        assertTransactionResponse(transaction, TransactionStatus.Captured);

        Transaction refundTransaction =
                transaction
                        .refund()
                        .withCurrency(CURRENCY)
                        .execute(GP_API_CONFIG_NAME);

        assertTransactionResponse(refundTransaction, TransactionStatus.Captured);

        //TODO - remove when api fix polling issue
        Thread.sleep(1000);

        BatchSummary batchSummary = BatchService.closeBatch(refundTransaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME);
        assertBatchCloseResponse(batchSummary, new BigDecimal("0"));
    }

    @Test
    public void CloseBatch_DebitTrackData() throws ApiException, InterruptedException {
        DebitTrackData debitCard = new DebitTrackData();
        debitCard.setValue("%B4012002000060016^VI TEST CREDIT^251210118039000000000396?;4012002000060016=25121011803939600000?");
        debitCard.setPinBlock("32539F50C245A6A93D123412324000AA");
        debitCard.setEntryMethod(EntryMethod.Swipe);

        Transaction transaction =
                debitCard
                        .charge(amount)
                        .withCurrency(CURRENCY)
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);

        assertTransactionResponse(transaction, TransactionStatus.Captured);

        //TODO - remove when api fix polling issue
        Thread.sleep(1000);

        BatchSummary batchSummary = BatchService.closeBatch(transaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME);
        assertBatchCloseResponse(batchSummary, amount);
    }

    @Test
    public void CloseBatch_Reverse_DebitTrackData() throws ApiException, InterruptedException {
        DebitTrackData debitCard = new DebitTrackData();
        debitCard.setValue("%B4012002000060016^VI TEST CREDIT^251210118039000000000396?;4012002000060016=25121011803939600000?");
        debitCard.setPinBlock("32539F50C245A6A93D123412324000AA");
        debitCard.setEntryMethod(EntryMethod.Swipe);

        Transaction transaction =
                debitCard
                        .authorize(amount)
                        .withCurrency(CURRENCY)
                        .execute(GP_API_CONFIG_NAME);

        assertTransactionResponse(transaction, TransactionStatus.Preauthorized);

        Transaction reverseTransaction = transaction
                .reverse()
                .withCurrency(CURRENCY)
                .execute(GP_API_CONFIG_NAME);

        assertTransactionResponse(reverseTransaction, TransactionStatus.Reversed);

        //TODO - remove when api fix polling issue
        Thread.sleep(1000);

        boolean exceptionCaught = false;
        try {
            BatchService.closeBatch(transaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40223", ex.getResponseText());
            assertEquals("Status Code: 400 - Request expects the batch_id", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CloseBatch_WithCardNumberDetails() throws ApiException, InterruptedException {
        CreditCardData card = new CreditCardData();

        card.setNumber("4263970000005262");
        card.setExpMonth(05);
        card.setExpYear(2025);
        card.setCvn("123");

        Transaction chargeTransaction =
                card
                        .charge(amount)
                        .withCurrency(CURRENCY)
                        .execute(GP_API_CONFIG_NAME);

        assertTransactionResponse(chargeTransaction, TransactionStatus.Captured);

        //TODO - remove when api fix polling issue
        Thread.sleep(1000);

        BatchSummary batchSummary = BatchService.closeBatch(chargeTransaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME);
        assertBatchCloseResponse(batchSummary, amount);
    }

    @Test
    public void CloseBatch_WithCardNumberDetails_DeclinedTransaction() throws ApiException, InterruptedException {
        CreditCardData card = new CreditCardData();
        card.setNumber("4263970000005262");
        card.setExpMonth(05);
        card.setExpYear(2025);
        card.setCvn("852");

        Transaction chargeTransaction =
                card
                        .charge(amount)
                        .withCurrency(CURRENCY)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(chargeTransaction);
        assertEquals("DECLINED", chargeTransaction.getResponseCode());
        assertEquals(TransactionStatus.Declined.getValue(), chargeTransaction.getResponseMessage());

        //TODO - remove when api fix polling issue
        Thread.sleep(1000);

        boolean exceptionCaught = false;
        try {
            BatchService.closeBatch(chargeTransaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("INVALID_BATCH_ACTION", ex.getResponseCode());
            assertEquals("40017", ex.getResponseText());
            assertEquals("Status Code: 400 - 9,No transaction associated with batch", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Ignore
    //TODO - add idempotency key as header
    public void CloseBatch_WithIdempotency() throws ApiException {
        String idempotency = java.util.UUID.randomUUID().toString();

        Transaction transaction =
                creditCard
                        .charge(amount)
                        .withCurrency(CURRENCY)
                        .execute(GP_API_CONFIG_NAME);

        assertTransactionResponse(transaction, TransactionStatus.Captured);

        BatchSummary batchSummary = BatchService.closeBatch(transaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME);
        //.setIdempotency - TODO

        assertNotNull(batchSummary);
        assertEquals(CLOSED, batchSummary.getStatus());

        BatchService.closeBatch(transaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME);
    }

    @Test
    public void CloseBatch_WithClosedBatchReference() throws ApiException, InterruptedException {
        Transaction transaction =
                creditCard
                        .charge(amount)
                        .withCurrency(CURRENCY)
                        .execute(GP_API_CONFIG_NAME);

        assertTransactionResponse(transaction, TransactionStatus.Captured);

        //TODO - remove when api fix polling issue
        Thread.sleep(1000);

        BatchSummary batchSummary = BatchService.closeBatch(transaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME);
        assertBatchCloseResponse(batchSummary, amount);

        //TODO - remove when api fix polling issue
        Thread.sleep(1000);

        boolean exceptionCaught = false;
        try {
            BatchService.closeBatch(batchSummary.getBatchReference(), GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("INVALID_BATCH_ACTION", ex.getResponseCode());
            assertEquals("40014", ex.getResponseText());
            assertEquals("Status Code: 400 - 5,No current batch", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CloseBatch_Verify_MissingBatchId() throws ApiException {
        Transaction transaction =
                creditCard
                        .verify()
                        .withCurrency(CURRENCY)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(VERIFIED, transaction.getResponseMessage());

        boolean exceptionCaught = false;
        try {
            BatchService.closeBatch(transaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40223", ex.getResponseText());
            assertEquals("Status Code: 400 - Request expects the batch_id", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CloseBatch_CardNotPresentChannel() throws ApiException, InterruptedException {
        GpApiConfig gpApiConfig = new GpApiConfig();
        gpApiConfig.setAppId("P3LRVjtGRGxWQQJDE345mSkEh2KfdAyg");
        gpApiConfig.setAppKey("ockJr6pv6KFoGiZA");
        gpApiConfig.setChannel(Channel.CardNotPresent.getValue());

        gpApiConfig.setEnableLogging(true);

        ServicesContainer.configureService(gpApiConfig, GP_API_CONFIG_NAME);

        CreditCardData creditCardData = new CreditCardData();
        creditCardData.setNumber("5425230000004415");
        creditCardData.setExpMonth(05);
        creditCardData.setExpYear(2025);
        creditCardData.setCvn("852");

        Transaction transaction =
                creditCardData
                        .charge(amount)
                        .withCurrency(CURRENCY)
                        .execute(GP_API_CONFIG_NAME);

        assertTransactionResponse(transaction, TransactionStatus.Captured);

        //TODO - remove when api fix polling issue
        Thread.sleep(1000);

        boolean exceptionCaught = false;
        try {
            BatchService.closeBatch(transaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("UNAUTHORIZED_DOWNSTREAM", ex.getResponseCode());
            assertEquals("50002", ex.getResponseText());
            assertEquals("Status Code: 502 - -2,Authentication error—Verify and correct credentials", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CloseBatch_WithInvalidBatchReference() throws ApiException {
        String batchReference = java.util.UUID.randomUUID().toString().replace("-", "");

        boolean exceptionCaught = false;
        try {
            BatchService.closeBatch(batchReference, GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40118", ex.getResponseText());
            assertEquals("Status Code: 404 - Batch " + batchReference + " not found at this location.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CloseBatch_ActionNotAuthorized() throws ApiException, InterruptedException {
        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                // These credentials have NOT permissions for executing BATCH
                .setAppId("OWTP5ptQZKGj7EnvPt3uqO844XDBt8Oj")
                .setAppKey("qM31FmlFiyXRHGYh")
                .setChannel(Channel.CardPresent.getValue());

        config.setEnableLogging(true);

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);

        Transaction transaction =
                creditCard
                        .charge(amount)
                        .withCurrency("USD")
                        .execute(GP_API_CONFIG_NAME);

        assertTransactionResponse(transaction, TransactionStatus.Captured);

        Thread.sleep(1000);

        boolean exceptionCaught = false;
        try {
            BatchService
                    .closeBatch(transaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("ACTION_NOT_AUTHORIZED", ex.getResponseCode());
            assertEquals("40212", ex.getResponseText());
            assertEquals("Status Code: 403 - Permission not enabled to execute action", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    private static void assertBatchCloseResponse(BatchSummary batchSummary, BigDecimal amount) {
        assertNotNull(batchSummary);
        assertEquals(CLOSED, batchSummary.getStatus());
        assertTrue(batchSummary.getTransactionCount() >= 1);
        assertTrue(batchSummary.getTotalAmount().intValue() >= amount.intValue());
    }

    private void assertTransactionResponse(Transaction transaction, TransactionStatus transactionStatus) {
        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(transactionStatus.getValue(), transaction.getResponseMessage());
    }

}
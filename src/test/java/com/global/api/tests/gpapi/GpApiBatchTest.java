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
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.BatchService;
import lombok.SneakyThrows;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GpApiBatchTest extends BaseGpApiTest {

    private final CreditTrackData creditCard;
    private final BigDecimal amount = new BigDecimal("1.00");
    private final String currency = "USD";
    private final String TAG_DATA = "9F4005F000F0A0019F02060000000025009F03060000000000009F2608D90A06501B48564E82027C005F3401019F360200029F0702FF009F0802008C9F0902008C9F34030403029F2701809F0D05F0400088009F0E0508000000009F0F05F0400098005F280208409F390105FFC605DC4000A800FFC7050010000000FFC805DC4004F8009F3303E0B8C89F1A0208409F350122950500000080005F2A0208409A031409109B02E8009F21030811539C01009F37045EED3A8E4F07A00000000310109F0607A00000000310108407A00000000310109F100706010A03A400029F410400000001";

    public GpApiBatchTest() throws ConfigurationException {
        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardPresent);
        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);

        creditCard = new CreditTrackData();
        creditCard.setValue("%B4012002000060016^VI TEST CREDIT^251210118039000000000396?;4012002000060016=25121011803939600000?");
        creditCard.setEntryMethod(EntryMethod.Swipe);
    }

    @SneakyThrows
    @Test
    public void CloseBatch() {
        Transaction chargeTransaction =
                creditCard
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(chargeTransaction, TransactionStatus.Captured);

        //TODO - remove when api fix polling issue
        waitForGpApiReplication();

        BatchSummary batchSummary = BatchService.closeBatch(chargeTransaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME);
        assertBatchCloseResponse(batchSummary, amount);
    }

    @SneakyThrows
    @Test
    public void CloseBatch_ChipTransaction() {
        Transaction transaction =
                creditCard
                        .charge(amount)
                        .withCurrency(currency)
                        .withTagData(TAG_DATA)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        //TODO - remove when api fix polling issue
        waitForGpApiReplication();

        BatchSummary batchSummary = BatchService.closeBatch(transaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME);
        assertBatchCloseResponse(batchSummary, amount);
    }

    @SneakyThrows
    @Test
    public void CloseBatch_AuthAndCapture() {
        Transaction authTransaction =
                creditCard
                        .authorize(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(authTransaction, TransactionStatus.Preauthorized);

        Transaction captureTransaction =
                authTransaction
                        .capture(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(captureTransaction, TransactionStatus.Captured);

        //TODO - remove when api fix polling issue
        waitForGpApiReplication();

        BatchSummary batchSummary = BatchService.closeBatch(captureTransaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME);
        assertBatchCloseResponse(batchSummary, amount);
    }

    @SneakyThrows
    @Test
    public void CloseBatch_ContactlessTransaction() {
        DebitTrackData debitCard = new DebitTrackData();
        debitCard.setValue(";4024720012345671=30125025432198712345?");
        debitCard.setEntryMethod(EntryMethod.Proximity);
        debitCard.setPinBlock("AFEC374574FC90623D010000116001EE");

        Transaction transaction =
                debitCard
                        .charge(amount)
                        .withCurrency(currency)
                        .withTagData(TAG_DATA)
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        //TODO - remove when api fix polling issue
        waitForGpApiReplication();

        BatchSummary batchSummary = BatchService.closeBatch(transaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME);
        assertBatchCloseResponse(batchSummary, amount);
    }

    @SneakyThrows
    @Test
    public void CloseBatch_MultipleChargeCreditTrackData() {
        Transaction firstTransaction =
                creditCard
                        .charge(new BigDecimal("1.25"))
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(firstTransaction, TransactionStatus.Captured);

        Transaction secondTransaction =
                creditCard
                        .charge(new BigDecimal("2.03"))
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(secondTransaction, TransactionStatus.Captured);

        //TODO - remove when api fix polling issue
        waitForGpApiReplication();

        BatchSummary batchSummary = BatchService.closeBatch(secondTransaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME);
        assertBatchCloseResponse(batchSummary, new BigDecimal("3.28"));
    }

    @SneakyThrows
    @Test
    public void CloseBatch_Refund_CreditTrackData() {
        Transaction transaction =
                creditCard
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        Transaction refundTransaction =
                transaction
                        .refund()
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(refundTransaction, TransactionStatus.Captured);

        //TODO - remove when api fix polling issue
        waitForGpApiReplication();

        BatchSummary batchSummary = BatchService.closeBatch(refundTransaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME);
        assertBatchCloseResponse(batchSummary, new BigDecimal("0"));
    }

    @SneakyThrows
    @Test
    public void CloseBatch_DebitTrackData() {
        DebitTrackData debitCard = new DebitTrackData();
        debitCard.setValue("%B4012002000060016^VI TEST CREDIT^251210118039000000000396?;4012002000060016=25121011803939600000?");
        debitCard.setPinBlock("32539F50C245A6A93D123412324000AA");
        debitCard.setEntryMethod(EntryMethod.Swipe);

        Transaction transaction =
                debitCard
                        .charge(amount)
                        .withCurrency(currency)
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        //TODO - remove when api fix polling issue
        waitForGpApiReplication();

        BatchSummary batchSummary = BatchService.closeBatch(transaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME);
        assertBatchCloseResponse(batchSummary, amount);
    }

    @SneakyThrows
    @Test
    public void CloseBatch_Reverse_DebitTrackData() {
        DebitTrackData debitCard = new DebitTrackData();
        debitCard.setValue("%B4012002000060016^VI TEST CREDIT^251210118039000000000396?;4012002000060016=25121011803939600000?");
        debitCard.setPinBlock("32539F50C245A6A93D123412324000AA");
        debitCard.setEntryMethod(EntryMethod.Swipe);

        Transaction transaction =
                debitCard
                        .authorize(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Preauthorized);

        Transaction reverseTransaction = transaction
                .reverse()
                .withCurrency(currency)
                .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(reverseTransaction, TransactionStatus.Reversed);

        //TODO - remove when api fix polling issue
        waitForGpApiReplication();

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

    @SneakyThrows
    @Test
    public void CloseBatch_WithCardNumberDetails() {
        CreditCardData card = new CreditCardData();
        card.setNumber("4263970000005262");
        card.setExpMonth(expMonth);
        card.setExpYear(expYear);
        card.setCvn("123");
        card.setCardPresent(true);

        Transaction chargeTransaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(chargeTransaction, TransactionStatus.Captured);

        //TODO - remove when api fix polling issue
        waitForGpApiReplication();

        BatchSummary batchSummary = BatchService.closeBatch(chargeTransaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME);
        assertBatchCloseResponse(batchSummary, amount);
    }

    @Ignore // Now with a DECLINED transaction is allowed
    @SneakyThrows
    @Test
    public void CloseBatch_WithCardNumberDetails_DeclinedTransaction() {
        CreditCardData card = new CreditCardData();
        card.setNumber("38865000000705");
        card.setExpMonth(expMonth);
        card.setExpYear(expYear);
        card.setCvn("8512");
        card.setCardPresent(true);

        Transaction chargeTransaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);
        assertNotNull(chargeTransaction);
        assertEquals("DECLINED", chargeTransaction.getResponseCode());
        assertEquals(TransactionStatus.Declined.getValue(), chargeTransaction.getResponseMessage());

        //TODO - remove when api fix polling issue
        waitForGpApiReplication();

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
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        BatchSummary batchSummary = BatchService.closeBatch(transaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME);
        //.setIdempotency - TODO

        assertNotNull(batchSummary);
        assertEquals(CLOSED, batchSummary.getStatus());

        BatchService.closeBatch(transaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME);
    }

    @SneakyThrows
    @Test
    public void CloseBatch_WithClosedBatchReference() {
        Transaction transaction =
                creditCard
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        //TODO - remove when api fix polling issue
        waitForGpApiReplication();

        BatchSummary batchSummary = BatchService.closeBatch(transaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME);
        assertBatchCloseResponse(batchSummary, amount);

        //TODO - remove when api fix polling issue
        waitForGpApiReplication();

        boolean exceptionCaught = false;
        try {
            BatchService.closeBatch(batchSummary.getBatchReference(), GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 502 - Action failed unexpectedly. Please try again ", ex.getMessage());
            assertEquals("ACTION_FAILED", ex.getResponseCode());
            assertEquals("500010", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @SneakyThrows
    @Test
    public void CloseBatch_Verify_MissingBatchId() {
        Transaction transaction =
                creditCard
                        .verify()
                        .withCurrency(currency)
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

    @SneakyThrows
    @Test
    public void CloseBatch_CardNotPresentChannel() {
        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        ServicesContainer.configureService(config);

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME + "CNP");

        CreditCardData creditCardData = new CreditCardData();
        creditCardData.setNumber("5425230000004415");
        creditCardData.setExpMonth(expMonth);
        creditCardData.setExpYear(expYear);
        creditCardData.setCvn("852");

        Transaction transaction =
                creditCardData
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME + "CNP");
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        //TODO - remove when api fix polling issue
        waitForGpApiReplication();

        boolean exceptionCaught = false;
        try {
            BatchService.closeBatch(transaction.getBatchSummary().getBatchReference(), GP_API_CONFIG_NAME+ "CNP");
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Merchant configuration does not exist for the following combination: country - US, channel - CNP, currency - USD", ex.getMessage());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40041", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @SneakyThrows
    @Test
    public void CloseBatch_WithInvalidBatchReference() {
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

    @SneakyThrows
    @Test
    public void CloseBatch_ActionNotAuthorized() {
        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                // These credentials have NO permissions for executing BATCH
                .setAppId("OWTP5ptQZKGj7EnvPt3uqO844XDBt8Oj")
                .setAppKey("qM31FmlFiyXRHGYh")
                .setChannel(Channel.CardPresent);
        config.setEnableLogging(true);

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);

        Transaction transaction =
                creditCard
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        waitForGpApiReplication();

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
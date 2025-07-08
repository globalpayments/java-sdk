package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.TransactionStatus;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import static java.lang.Double.parseDouble;
import static java.math.BigDecimal.valueOf;

public class GpApiDccCardNotPresentTest extends BaseGpApiTest {

    static final String currency = "EUR";
    static final double amount = 15.11;
    private final CreditCardData card;

    public GpApiDccCardNotPresentTest() throws ApiException {
        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        config
                .setAccessTokenInfo(new AccessTokenInfo().setTransactionProcessingAccountName("dcc"))
                .setEnableLogging(true);

        ServicesContainer.configureService(config);

        card = new CreditCardData();
        card.setNumber("4006097467207025");
        card.setExpMonth(expMonth);
        card.setExpYear(expYear);
        card.setCardHolderName("James Mason");
    }

    @Test
    public void CreditGetDccInfo() throws ApiException {
        Transaction dccDetails =
                card
                        .getDccRate()
                        .withAmount(amount)
                        .withCurrency(currency)
                        .execute();
        BigDecimal expectedDccAmountValue = getDccAmount(dccDetails);
        assertDccInfoResponse(dccDetails, expectedDccAmountValue);

        waitForGpApiReplication();
        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withDccRateData(dccDetails.getDccRateData())
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Captured, expectedDccAmountValue);
    }

    @Test
    public void CreditDccRateAuthorize() throws ApiException {
        Transaction dccDetails =
                card
                        .getDccRate()
                        .withAmount(new BigDecimal(amount))
                        .withCurrency(currency)
                        .execute();
        BigDecimal expectedDccAmountValue = getDccAmount(dccDetails);
        assertDccInfoResponse(dccDetails, expectedDccAmountValue);

        waitForGpApiReplication();
        Transaction response =
                card
                        .authorize(new BigDecimal(amount))
                        .withCurrency(currency)
                        .withDccRateData(dccDetails.getDccRateData())
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Preauthorized, expectedDccAmountValue);
    }

    @Test
    public void CreditDccRateRefundStandalone() throws ApiException {
        Transaction dccDetails =
                card
                        .getDccRate()
                        .withAmount(amount)
                        .withCurrency(currency)
                        .execute();
        BigDecimal expectedDccAmountValue = getDccAmount(dccDetails);
        assertDccInfoResponse(dccDetails, expectedDccAmountValue);

        waitForGpApiReplication();
        Transaction refund =
                card
                        .refund(amount)
                        .withCurrency(currency)
                        .withDccRateData(dccDetails.getDccRateData())
                        .execute();
        assertTransactionResponse(refund, TransactionStatus.Captured, expectedDccAmountValue);
    }

    @Test
    public void CreditDccRateReversal() throws ApiException {
        Transaction dccDetails =
                card
                        .getDccRate()
                        .withAmount(amount)
                        .withCurrency(currency)
                        .execute();
        BigDecimal expectedDccAmountValue = getDccAmount(dccDetails);
        assertDccInfoResponse(dccDetails, expectedDccAmountValue);

        waitForGpApiReplication();
        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withDccRateData(dccDetails.getDccRateData())
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Captured, expectedDccAmountValue);

        Transaction reverse =
                transaction
                        .reverse(amount)
                        .execute();
        assertTransactionResponse(reverse, TransactionStatus.Reversed, expectedDccAmountValue);
    }

    @Test
    public void CreditDccRateRefund() throws ApiException {
        Transaction dccDetails =
                card
                        .getDccRate()
                        .withAmount(amount)
                        .withCurrency(currency)
                        .execute();
        BigDecimal expectedDccAmountValue = getDccAmount(dccDetails);
        assertDccInfoResponse(dccDetails, expectedDccAmountValue);

        waitForGpApiReplication();
        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withDccRateData(dccDetails.getDccRateData())
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Captured, expectedDccAmountValue);

        Transaction refund =
                transaction
                        .refund(amount)
                        .withCurrency(currency)
                        .withDccRateData(dccDetails.getDccRateData())
                        .execute();
        assertTransactionResponse(refund, TransactionStatus.Captured, expectedDccAmountValue);
    }

    @Test
    public void CreditDccRateAuthorizationThenCapture() throws ApiException {
        Transaction dccDetails =
                card
                        .getDccRate()
                        .withAmount(amount)
                        .withCurrency(currency)
                        .execute();
        BigDecimal expectedDccAmountValue = getDccAmount(dccDetails);
        assertDccInfoResponse(dccDetails, expectedDccAmountValue);

        waitForGpApiReplication();
        Transaction transaction =
                card
                        .authorize(new BigDecimal(amount))
                        .withCurrency(currency)
                        .withDccRateData(dccDetails.getDccRateData())
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Preauthorized, expectedDccAmountValue);

        Transaction capture =
                transaction
                        .capture()
                        .withDccRateData(dccDetails.getDccRateData())
                        .execute();
        assertTransactionResponse(capture, TransactionStatus.Captured, expectedDccAmountValue);
    }

    @Test
    public void CardTokenizationThenPayingWithToken() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();

        tokenizedCard.setToken(card.tokenize());

        Transaction dccDetails =
                tokenizedCard
                        .getDccRate()
                        .withAmount(amount)
                        .withCurrency(currency)
                        .execute();
        BigDecimal expectedDccAmountValue = getDccAmount(dccDetails);
        assertDccInfoResponse(dccDetails, expectedDccAmountValue);

        waitForGpApiReplication();
        Transaction response =
                tokenizedCard
                        .charge(amount)
                        .withCurrency(currency)
                        .withDccRateData(dccDetails.getDccRateData())
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured, expectedDccAmountValue);
    }

    @Test
    public void CreditGetDccInfo_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction dccDetails =
                card
                        .getDccRate()
                        .withAmount(amount)
                        .withCurrency(currency)
                        .withIdempotencyKey(idempotencyKey)
                        .execute();
        BigDecimal expectedDccAmountValue = getDccAmount(dccDetails);
        assertDccInfoResponse(dccDetails, expectedDccAmountValue);

        waitForGpApiReplication();
        boolean exceptionCaught = false;
        try {
            card
                    .getDccRate()
                    .withAmount(amount)
                    .withCurrency(currency)
                    .withIdempotencyKey(idempotencyKey)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("DUPLICATE_ACTION", ex.getResponseCode());
            assertEquals("40039", ex.getResponseText());
            assertTrue(ex.getMessage().startsWith("Status Code: 409 - Idempotency Key seen before: id="));
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditGetDccInfo_RateNotAvailable() throws ApiException {
        card.setNumber("4263970000005262");

        Transaction dccDetails =
                card
                        .getDccRate()
                        .withAmount(amount)
                        .withCurrency(currency)
                        .execute();

        assertNotNull(dccDetails);
        assertEquals(SUCCESS, dccDetails.getResponseCode());
        assertEquals("NOT_AVAILABLE", dccDetails.getResponseMessage());
        assertNotNull(dccDetails.getDccRateData());

        waitForGpApiReplication();
        Transaction transaction = card.charge(amount)
                .withCurrency(currency)
                .withDccRateData(dccDetails.getDccRateData())
                .execute();
        assertTransactionResponse(transaction, TransactionStatus.Captured, valueOf(amount));
    }

    @Test
    public void CreditGetDccInfo_InvalidCardNumber() throws ApiException {
        card.setNumber("4000000000005262");
        boolean exceptionCaught = false;
        try {
            card.getDccRate()
                    .withAmount(amount)
                    .withCurrency(currency)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40090", ex.getResponseText());
            assertEquals("Status Code: 400 - card.number value is invalid. Please check the format and data provided is correct.",
                    ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditGetDccInfo_WithoutAmount() throws ApiException {
        boolean exceptionCaught = false;
        try {
            card.getDccRate()
                    .withCurrency(currency)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40005", ex.getResponseText());
            assertEquals("Status Code: 400 - Request expects the following fields : amount",
                    ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditGetDccInfo_WithoutCurrency() throws ApiException {
        boolean exceptionCaught = false;
        try {
            card.getDccRate()
                    .withAmount(amount)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40005", ex.getResponseText());
            assertEquals("Status Code: 400 - Request expects the following fields : currency",
                    ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    private BigDecimal getDccAmount(Transaction dccDetails) {
        return (!dccDetails.getDccRateData().getCardHolderRate().isEmpty())
                ? valueOf(amount * parseDouble(dccDetails.getDccRateData().getCardHolderRate()))
                .setScale(2, RoundingMode.DOWN)
                : null;
    }

    private void assertDccInfoResponse(Transaction dccDetails, BigDecimal expectedDccAmountValue) {
        assertNotNull(dccDetails);
        assertEquals(SUCCESS, dccDetails.getResponseCode());
        assertEquals("AVAILABLE", dccDetails.getResponseMessage());
        assertNotNull(dccDetails.getDccRateData());
        assertEquals(expectedDccAmountValue, dccDetails.getDccRateData().getCardHolderAmount());
    }

    private void assertTransactionResponse(Transaction transaction, TransactionStatus transactionStatus, BigDecimal expectedDccAmountValue) {
        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(transactionStatus.getValue(), transaction.getResponseMessage());
        if (!transactionStatus.equals(TransactionStatus.Reversed))
            assertEquals(expectedDccAmountValue, transaction.getDccRateData().getCardHolderAmount());
    }

}

package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.ManualEntryMethod;
import com.global.api.entities.enums.TransactionStatus;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * APAC eCommerce endpoint connectivity tests for GP-API.
 * Visa and Mastercard cards are used to test various transaction types including sales, authorizations, captures, voids, reversals, refunds, and partial captures.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public class GpApiApacEcommTest extends BaseGpApiTest {

    private static final String APAC_APP_ID = "16Br1RfjChBrsFnWlu7NGIp9LKm2MWWFyGg3SU3UfEl3voA2";
    private static final String APAC_APP_KEY = "xV9wnRLmi8qPqvMZoxAH9S0RtoQlodCYuvCboVYUohW6DObtcrYL1uj4YOZilKyu";

    private static final BigDecimal AMOUNT = new BigDecimal("123.45");
    private static final BigDecimal PARTIAL_CAPTURE_AMOUNT = new BigDecimal("5.00");
    private static final BigDecimal GRATUITY_AMOUNT = new BigDecimal("1.00");

    private static final String VISA_CARD_NUMBER = "4263970000005262";
    private static final String MASTERCARD_CARD_NUMBER = "5425230000004415";

    private CreditCardData visaCard;
    private CreditCardData masterCard;
    private CreditCardData motoVisaCard;
    private CreditCardData motoMasterCard;

    public GpApiApacEcommTest() throws ApiException {
        GpApiConfig config = gpApiSetup(APAC_APP_ID, APAC_APP_KEY, Channel.CardNotPresent);
        config.setCountry("SG");
        ServicesContainer.configureService(config);
    }

    @BeforeEach
    public void setupCards() {
        visaCard = createCard(VISA_CARD_NUMBER, "John Smith", false);
        masterCard = createCard(MASTERCARD_CARD_NUMBER, "Jane Doe", false);

        motoVisaCard = createCard(VISA_CARD_NUMBER, "John Smith", true);
        motoMasterCard = createCard(MASTERCARD_CARD_NUMBER, "Jane Doe", true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"HKD", "MOP", "PHP", "MYR", "SGD"})
    public void CreditSale_Visa_Currencies(String currency) throws ApiException {
        Transaction response = visaCard.charge(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @ParameterizedTest
    @ValueSource(strings = {"HKD", "MOP", "PHP", "MYR", "SGD"})
    public void CreditSale_Mastercard_Currencies(String currency) throws ApiException {
        Transaction response = masterCard.charge(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @ParameterizedTest
    @ValueSource(strings = {"HKD", "MOP", "PHP", "MYR", "SGD"})
    public void CreditAuthorization_Visa_Param(String currency) throws ApiException {
        Transaction response = visaCard.authorize(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(response, TransactionStatus.Preauthorized);
    }


    @ParameterizedTest
    @ValueSource(strings = {"HKD", "MOP", "PHP", "MYR", "SGD"})
    public void CreditAuthorization_Mastercard_Param(String currency) throws ApiException {
        Transaction response = masterCard.authorize(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(response, TransactionStatus.Preauthorized);
    }

    @ParameterizedTest
    @ValueSource(strings = {"HKD", "MOP", "PHP", "MYR", "SGD"})
    public void CreditPreAuth_Visa_Param(String currency) throws ApiException {
        Transaction response = visaCard.authorize(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(response, TransactionStatus.Preauthorized);
    }

    @ParameterizedTest
    @ValueSource(strings = {"HKD", "MOP", "PHP", "MYR", "SGD"})
    public void CreditPreAuth_Mastercard_Param(String currency) throws ApiException {
        Transaction response = masterCard.authorize(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(response, TransactionStatus.Preauthorized);
    }

    @ParameterizedTest
    @ValueSource(strings = {"HKD", "MOP", "PHP", "MYR", "SGD"})
    public void CreditCapture_Visa_Param(String currency) throws ApiException {
        Transaction authResponse = visaCard.authorize(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(authResponse, TransactionStatus.Preauthorized);

        Transaction captureResponse = authResponse.capture(AMOUNT).execute();
        assertTransactionResponse(captureResponse, TransactionStatus.Captured);
    }

    @ParameterizedTest
    @ValueSource(strings = {"HKD", "MOP", "PHP", "MYR", "SGD"})
    public void CreditCapture_Mastercard_Param(String currency) throws ApiException {
        Transaction authResponse = masterCard.authorize(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(authResponse, TransactionStatus.Preauthorized);

        Transaction captureResponse = authResponse.capture(AMOUNT).execute();
        assertTransactionResponse(captureResponse, TransactionStatus.Captured);
    }

    @ParameterizedTest
    @ValueSource(strings = {"HKD", "MOP", "PHP", "MYR", "SGD"})
    public void CreditVoid_Mastercard_Param(String currency) throws ApiException {
        Transaction saleResponse = masterCard.charge(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(saleResponse, TransactionStatus.Captured);

        Transaction voidResponse = saleResponse.voidTransaction(AMOUNT).execute();
        assertTransactionResponse(voidResponse, TransactionStatus.Reversed);
    }

    @ParameterizedTest
    @ValueSource(strings = {"HKD", "MOP", "PHP", "MYR", "SGD"})
    public void CreditVoid_Visa_Param(String currency) throws ApiException {
        Transaction saleResponse = visaCard.charge(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(saleResponse, TransactionStatus.Captured);

        Transaction voidResponse = saleResponse.voidTransaction(AMOUNT).execute();
        assertTransactionResponse(voidResponse, TransactionStatus.Reversed);
    }

    @ParameterizedTest
    @ValueSource(strings = {"HKD", "MOP", "PHP", "MYR", "SGD"})
    public void CreditAuthReversal_Mastercard_Param(String currency) throws ApiException {
        Transaction authResponse = masterCard.authorize(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(authResponse, TransactionStatus.Preauthorized);

        Transaction reversalResponse = authResponse.reverse(AMOUNT).execute();
        assertTransactionResponse(reversalResponse, TransactionStatus.Reversed);
    }

    @ParameterizedTest
    @ValueSource(strings = {"HKD", "MOP", "PHP", "MYR", "SGD"})
    public void CreditAuthReversal_Visa_Param(String currency) throws ApiException {
        Transaction authResponse = visaCard.authorize(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(authResponse, TransactionStatus.Preauthorized);

        Transaction reversalResponse = authResponse.reverse(AMOUNT).execute();
        assertTransactionResponse(reversalResponse, TransactionStatus.Reversed);
    }

    @ParameterizedTest
    @ValueSource(strings = {"HKD", "MOP", "PHP", "MYR", "SGD"})
    public void CreditRefund_Visa_Param(String currency) throws ApiException {
        Transaction authResponse = visaCard.charge(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(authResponse, TransactionStatus.Captured);

        Transaction reversalResponse = authResponse.refund(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(reversalResponse, TransactionStatus.Captured);
    }

    @ParameterizedTest
    @ValueSource(strings = {"HKD", "MOP", "PHP", "MYR", "SGD"})
    public void CreditRefund_Mastercard_Param(String currency) throws ApiException {
        Transaction authResponse = masterCard.charge(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(authResponse, TransactionStatus.Captured);

        Transaction reversalResponse = authResponse.refund(AMOUNT)
                .withCurrency(currency)
                 .execute();
        assertTransactionResponse(reversalResponse, TransactionStatus.Captured);
    }

    @ParameterizedTest
    @ValueSource(strings = {"HKD", "MOP", "PHP", "MYR", "SGD"})
    public void CreditRefund_Visa_StandaloneRefund_Param(String currency) throws ApiException {
        Transaction refundResponse = visaCard.refund(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(refundResponse, TransactionStatus.Captured);
    }

    @ParameterizedTest
    @ValueSource(strings = {"HKD", "MOP", "PHP", "MYR", "SGD"})
    public void CreditRefund_Mastercard_StandaloneRefund_Param(String currency) throws ApiException {
        Transaction refundResponse = masterCard.refund(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(refundResponse, TransactionStatus.Captured);
    }

    @ParameterizedTest
    @ValueSource(strings = {"HKD", "MOP", "PHP", "MYR", "SGD"})
    public void CreditPartialCapture_Visa_Param(String currency) throws ApiException {
        Transaction authResponse = visaCard.authorize(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(authResponse, TransactionStatus.Preauthorized);

        Transaction captureResponse = authResponse.capture(PARTIAL_CAPTURE_AMOUNT)
                .withGratuity(GRATUITY_AMOUNT)
                .execute();
        assertTransactionResponse(captureResponse, TransactionStatus.Captured);
    }

    @ParameterizedTest
    @ValueSource(strings = {"HKD", "MOP", "PHP", "MYR", "SGD"})
    public void CreditPartialCapture_Mastercard_Param(String currency) throws ApiException {
        Transaction authResponse = masterCard.authorize(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(authResponse, TransactionStatus.Preauthorized);

        Transaction captureResponse = authResponse.capture(PARTIAL_CAPTURE_AMOUNT)
                .withGratuity(GRATUITY_AMOUNT)
                .execute();
        assertTransactionResponse(captureResponse, TransactionStatus.Captured);
    }

    @ParameterizedTest
    @ValueSource(strings = {"HKD", "MOP", "PHP", "MYR", "SGD"})
    public void CreditMoto_Visa_Sale_Param(String currency) throws ApiException {
        Transaction response = motoVisaCard.charge(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @ParameterizedTest
    @ValueSource(strings = {"HKD", "MOP", "PHP", "MYR", "SGD"})
    public void CreditMoto_Mastercard_Sale_Param(String currency) throws ApiException {
        Transaction response = motoMasterCard.charge(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @ParameterizedTest
    @ValueSource(strings = {"HKD", "MOP", "PHP", "MYR", "SGD"})
    public void CreditMoto_Visa_Authorization_Param(String currency) throws ApiException {
        Transaction response = motoVisaCard.authorize(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(response, TransactionStatus.Preauthorized);
    }

    @ParameterizedTest
    @ValueSource(strings = {"HKD", "MOP", "PHP", "MYR", "SGD"})
    public void CreditMoto_Mastercard_Authorization_Param(String currency) throws ApiException {
        Transaction response = motoMasterCard.authorize(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(response, TransactionStatus.Preauthorized);
    }

    private CreditCardData createCard(String cardNumber, String cardHolderName, boolean moto) {
        CreditCardData card = new CreditCardData();
        card.setNumber(cardNumber);
        card.setExpMonth(expMonth);
        card.setExpYear(expYear);
        card.setCvn("123");
        card.setCardHolderName(cardHolderName);
        if (moto) {
            card.setEntryMethod(ManualEntryMethod.Moto);
        }
        return card;
    }

    private void assertTransactionResponse(Transaction transaction, TransactionStatus transactionStatus) {
        assertNotNull(transaction);
        assertTrue(SUCCESS.equals(transaction.getResponseCode()) || "00".equals(transaction.getResponseCode()));
        assertEquals(transactionStatus.getValue(), transaction.getResponseMessage());
    }
}

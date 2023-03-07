package com.global.api.tests.certifications;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.CvnPresenceIndicator;
import com.global.api.entities.enums.TransactionStatus;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GpApiSdkCertificationTest {

    @Before
    public void setupGpApi() throws ApiException {
        GpApiConfig gpApiConfig = new GpApiConfig();
        gpApiConfig
                .setAppId("4gPqnGBkppGYvoE5UX9EWQlotTxGUDbs")
                .setAppKey("FQyJA5VuEQfcji2M")
                .setChannel(Channel.CardNotPresent.getValue());

        gpApiConfig.setEnableLogging(true);

        ServicesContainer.configureService(gpApiConfig, "GpApiConfig");
    }

    private CreditCardData creditCardData(String cardNumber, String cvn, String cardHolderName) {
        CreditCardData card = new CreditCardData();
        card.setNumber(cardNumber);
        card.setExpMonth(DateTime.now().getMonthOfYear());
        card.setExpYear(DateTime.now().getYear() + 1);
        card.setCvn(cvn);
        card.setCvnPresenceIndicator(CvnPresenceIndicator.Present);
        card.setCardHolderName(cardHolderName);

        return card;
    }

    // ================================================================================
    // Credit Card "SUCCESS"
    // ================================================================================
    @Test
    public void CreditCard_Visa_Success() throws ApiException {
        CreditCardData card = creditCardData("4263970000005262", "123", "John Doe");

        Transaction response =
                card
                        .charge(new BigDecimal("14.99"))
                        .withCurrency("USD")
                        .withDescription("CreditCard_Visa_Success")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("Card brand mismatch", "VISA", response.getCardType());
        assertEquals("00", response.getAuthorizationCode());
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void CreditCard_Mastercard_Success() throws ApiException {
        CreditCardData card = creditCardData("5425230000004415", "123", "John Smith");

        Transaction response =
                card
                        .charge(new BigDecimal("4.95"))
                        .withCurrency("USD")
                        .withDescription("CreditCard_Mastercard_Success")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("Card brand mismatch", "MASTERCARD", response.getCardType());
        assertEquals("00", response.getAuthorizationCode());
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void CreditCard_AmericanExpress_Success() throws ApiException {
        CreditCardData card = creditCardData("374101000000608", "1234", "Susan Jones");

        Transaction response =
                card
                        .charge(new BigDecimal("17.25"))
                        .withCurrency("USD")
                        .withDescription("CreditCard_AmericanExpress_Success")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("Card brand mismatch", "AMEX", response.getCardType());
        assertEquals("00", response.getAuthorizationCode());
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void CreditCard_DinersClub_Success() throws ApiException {
        CreditCardData card = creditCardData("36256000000725", "789", "Mark Green");

        Transaction response =
                card
                        .charge(new BigDecimal("5.15"))
                        .withCurrency("USD")
                        .withDescription("CreditCard_DinersClub_Success")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("Card brand mismatch", "DINERS", response.getCardType());
        assertEquals("00", response.getAuthorizationCode());
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void CreditCard_Discover_Success() throws ApiException {
        CreditCardData card = creditCardData("6011000000000087", "456", "Mark Green");

        Transaction response =
                card
                        .charge(new BigDecimal("2.14"))
                        .withCurrency("USD")
                        .withDescription("CreditCard_Discover_Success")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("Card brand mismatch", "DISCOVER", response.getCardType());
        assertEquals("00", response.getAuthorizationCode());
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void CreditCard_JCB_Success() throws ApiException {
        CreditCardData card = creditCardData("3566000000000000", "223", "Mark Greens");

        Transaction response =
                card
                        .charge(new BigDecimal("1.9"))
                        .withCurrency("USD")
                        .withDescription("CreditCard_JCB_Success")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("Card brand mismatch", "JCB", response.getCardType());
        assertEquals("00", response.getAuthorizationCode());
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    // ================================================================================
    // Credit Card Visa DECLINED
    // ================================================================================
    @Test
    public void CreditCard_Visa_Declined_101() throws ApiException {
        CreditCardData card = creditCardData("4000120000001154", "123", "John Doe");

        Transaction response =
                card
                        .charge(new BigDecimal("10.25"))
                        .withCurrency("USD")
                        .withDescription("CreditCard_Visa_Declined_101")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("Card brand mismatch", "VISA", response.getCardType());
        assertEquals("101", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    @Test
    public void CreditCard_Visa_Declined_102() throws ApiException {
        CreditCardData card = creditCardData("4000130000001724", "123", "Mark Smith");

        Transaction response =
                card
                        .charge(new BigDecimal("3.75"))
                        .withCurrency("USD")
                        .withDescription("CreditCard_Visa_Declined_102")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("Card brand mismatch", "VISA", response.getCardType());
        assertEquals("102", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    @Test
    public void CreditCard_Visa_Declined_103() throws ApiException {
        CreditCardData card = creditCardData("4000160000004147", "123", "Bob Smith");

        Transaction response =
                card
                        .charge(new BigDecimal("5.35"))
                        .withCurrency("USD")
                        .withDescription("CreditCard_Visa_Declined_103")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("Card brand mismatch", "VISA", response.getCardType());
        assertEquals("103", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    // ================================================================================
    // Credit Card Mastercard DECLINED
    // ================================================================================
    @Test
    public void CreditCard_Mastercard_Declined_101() throws ApiException {
        CreditCardData card = creditCardData("5114610000004778", "123", "Bob Howard");

        Transaction response =
                card
                        .charge(new BigDecimal("3.25"))
                        .withCurrency("USD")
                        .withDescription("CreditCard_Mastercard_Declined_101")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("Card brand mismatch", "MASTERCARD", response.getCardType());
        assertEquals("101", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    @Test
    public void CreditCard_Mastercard_Declined_102() throws ApiException {
        CreditCardData card = creditCardData("5114630000009791", "123", "Tom Grey");

        Transaction response =
                card
                        .charge(new BigDecimal("4.50"))
                        .withCurrency("USD")
                        .withDescription("CreditCard_Mastercard_Declined_102")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("Card brand mismatch", "MASTERCARD", response.getCardType());
        assertEquals("102", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    @Test
    public void CreditCard_Mastercard_Declined_103() throws ApiException {
        CreditCardData card = creditCardData("5121220000006921", "123", "Marie Curie");
        card.setCvnPresenceIndicator(CvnPresenceIndicator.Illegible);

        Transaction response =
                card
                        .charge(new BigDecimal("5.99"))
                        .withCurrency("USD")
                        .withDescription("CreditCard_Mastercard_Declined_103")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("Card brand mismatch", "MASTERCARD", response.getCardType());
        assertEquals("103", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    // ================================================================================
    // Credit Card American Express DECLINED
    // ================================================================================
    @Test
    public void CreditCard_AmericanExpress_Declined_101() throws ApiException {
        CreditCardData card = creditCardData("376525000000010", "1234", "John Doe");

        Transaction response =
                card
                        .charge(new BigDecimal("7.25"))
                        .withCurrency("USD")
                        .withDescription("CreditCard_AmericanExpress_Declined_101")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("Card brand mismatch", "AMEX", response.getCardType());
        assertEquals("101", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    @Test
    public void CreditCard_AmericanExpress_Declined_102() throws ApiException {
        CreditCardData card = creditCardData("375425000000907", "1234", "Mark Smith");

        Transaction response =
                card
                        .charge(new BigDecimal("9.75"))
                        .withCurrency("USD")
                        .withDescription("CreditCard_AmericanExpress_Declined_102")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("Card brand mismatch", "AMEX", response.getCardType());
        assertEquals("102", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    @Test
    public void CreditCard_AmericanExpress_Declined_103() throws ApiException {
        CreditCardData card = creditCardData("343452000000306", "1234", "Bob Smith");

        Transaction response =
                card
                        .charge(new BigDecimal("1.25"))
                        .withCurrency("USD")
                        .withDescription("CreditCard_AmericanExpress_Declined_103")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("Card brand mismatch", "AMEX", response.getCardType());
        assertEquals("103", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    // ================================================================================
    // Credit Card Diners Club DECLINED
    // ================================================================================
    @Test
    public void CreditCard_DinersClub_Declined_101() throws ApiException {
        CreditCardData card = creditCardData("36256000000998", "123", "John Smith");

        Transaction response =
                card
                        .charge(new BigDecimal("1.25"))
                        .withCurrency("USD")
                        .withDescription("CreditCard_DinersClub_Declined_101")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("Card brand mismatch", "DINERS", response.getCardType());
        assertEquals("101", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    @Test
    public void CreditCard_DinersClub_Declined_102() throws ApiException {
        CreditCardData card = creditCardData("36256000000634", "123", "John Smith");

        Transaction response =
                card
                        .charge(new BigDecimal("2.25"))
                        .withCurrency("USD")
                        .withDescription("CreditCard_DinersClub_Declined_102")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("Card brand mismatch", "DINERS", response.getCardType());
        assertEquals("102", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    @Ignore // TODO: Reported error to GP-API team. Enable it when fixed.
    @Test
    public void CreditCard_DinersClub_Declined_103() throws ApiException {
        CreditCardData card = creditCardData("38865000000705", "123", "John Smith");

        Transaction response =
                card
                        .charge(new BigDecimal("3.25"))
                        .withCurrency("USD")
                        .withDescription("CreditCard_DinersClub_Declined_103")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("Card brand mismatch", "DINERS", response.getCardType());
        assertEquals("103", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    // ================================================================================
    // Credit Card Discover DECLINED
    // ================================================================================
    @Test
    public void CreditCard_Discover_Declined_101() throws ApiException {
        CreditCardData card = creditCardData("6011000000001010", "123", "Rob Brown");

        Transaction response =
                card
                        .charge(new BigDecimal("1.25"))
                        .withCurrency("USD")
                        .withDescription("CreditCard_Discover_Declined_101")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("Card brand mismatch", "DISCOVER", response.getCardType());
        assertEquals("101", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    @Test
    public void CreditCard_Discover_Declined_102() throws ApiException {
        CreditCardData card = creditCardData("6011000000001028", "123", "Rob Brown");

        Transaction response =
                card
                        .charge(new BigDecimal("2.25"))
                        .withCurrency("USD")
                        .withDescription("CreditCard_Discover_Declined_102")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("Card brand mismatch", "DISCOVER", response.getCardType());
        assertEquals("102", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    @Test
    public void CreditCard_Discover_Declined_103() throws ApiException {
        CreditCardData card = creditCardData("6011000000001036", "123", "Rob Brown");

        Transaction response =
                card
                        .charge(new BigDecimal("3.25"))
                        .withCurrency("USD")
                        .withDescription("CreditCard_Discover_Declined_103")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("Card brand mismatch", "DISCOVER", response.getCardType());
        assertEquals("103", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    // ================================================================================
    // Credit Card JCB DECLINED
    // ================================================================================
    @Test
    public void CreditCard_JCB_Declined_101() throws ApiException {
        CreditCardData card = creditCardData("3566000000001016", "123", "Michael Smith");

        Transaction response =
                card
                        .charge(new BigDecimal("1.25"))
                        .withCurrency("USD")
                        .withDescription("CreditCard_JCB_Declined_101")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("Card brand mismatch", "JCB", response.getCardType());
        assertEquals("101", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    @Test
    public void CreditCard_JCB_Declined_102() throws ApiException {
        CreditCardData card = creditCardData("3566000000001024", "123", "Michael Smith");

        Transaction response =
                card
                        .charge(new BigDecimal("2.25"))
                        .withCurrency("USD")
                        .withDescription("CreditCard_JCB_Declined_102")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("Card brand mismatch", "JCB", response.getCardType());
        assertEquals("102", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    @Test
    public void CreditCard_JCB_Declined_103() throws ApiException {
        CreditCardData card = creditCardData("3566000000001032", "123", "Michael Smith");

        Transaction response =
                card
                        .charge(new BigDecimal("3.25"))
                        .withCurrency("USD")
                        .withDescription("CreditCard_JCB_Declined_103")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("Card brand mismatch", "JCB", response.getCardType());
        assertEquals("103", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    // ================================================================================
    // Credit Card Visa ERROR
    // ================================================================================
    @Test
    public void CreditCard_Visa_Processing_Error() throws ApiException {
        CreditCardData card = creditCardData("4009830000001985", "123", "Mark Spencer");

        try {
            card
                    .charge(new BigDecimal("3.99"))
                    .withCurrency("USD")
                    .withDescription("CreditCard_Visa_Processing_Error")
                    .execute("GpApiConfig");
        } catch (GatewayException ex) {
            assertEquals("50013", ex.getResponseText());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("Status Code: 502 - 200,eCom error—Developers are notified", ex.getMessage());
        }
    }

    @Test
    public void CreditCard_Visa_Processing_Error_Wrong_Currency() throws ApiException {
        CreditCardData card = creditCardData("4009830000001985", "123", "Mark Spencer");

        try {
            card
                    .charge(new BigDecimal("3.99"))
                    .withCurrency("XXX")
                    .withDescription("CreditCard_Visa_Processing_Error_Wrong_Currency")
                    .execute("GpApiConfig");
        } catch (GatewayException ex) {
            assertEquals("50024", ex.getResponseText());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("Status Code: 502 - currency card combination not allowed", ex.getMessage());
        }
    }

    // ================================================================================
    // Credit Card Mastercard ERROR
    // ================================================================================
    @Test
    public void CreditCard_Mastercard_Processing_Error() throws ApiException {
        CreditCardData card = creditCardData("5135020000005871", "123", "Tom Brown");

        try {
            card
                    .charge(new BigDecimal("2.16"))
                    .withCurrency("USD")
                    .withDescription("CreditCard_Mastercard_Processing_Error")
                    .execute("GpApiConfig");
        } catch (GatewayException ex) {
            assertEquals("50013", ex.getResponseText());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("Status Code: 502 - 200,eCom error—Developers are notified", ex.getMessage());
        }
    }

    // ================================================================================
    // Credit Card American Express ERROR
    // ================================================================================
    @Test
    public void CreditCard_AmericanExpress_Processing_Error() throws ApiException {
        CreditCardData card = creditCardData("372349000000852", "1234", "Tina White");

        try {
            card
                    .charge(new BigDecimal("4.02"))
                    .withCurrency("USD")
                    .withDescription("CreditCard_AmericanExpress_Processing_Error")
                    .execute("GpApiConfig");
        } catch (GatewayException ex) {
            assertEquals("50013", ex.getResponseText());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("Status Code: 502 - 200,eCom error—Developers are notified", ex.getMessage());
        }
    }

    // Credit Card Diners Club ERROR
    @Test
    public void CreditCard_DinersClub_Processing_Error() throws ApiException {
        CreditCardData card = creditCardData("30450000000985", "123", "Ashley Brown");

        try {
            card
                    .charge(new BigDecimal("5.99"))
                    .withCurrency("USD")
                    .withDescription("CreditCard_DinersClub_Processing_Error")
                    .execute("GpApiConfig");
        } catch (GatewayException ex) {
            assertEquals("50013", ex.getResponseText());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("Status Code: 502 - 200,eCom error—Developers are notified", ex.getMessage());
        }
    }

    // ================================================================================
    // Credit Card Discover ERROR
    // ================================================================================
    @Test
    public void CreditCard_Discover_Processing_Error() throws ApiException {
        CreditCardData card = creditCardData("6011000000002000", "123", "Mark Spencer");

        try {
            card
                    .charge(new BigDecimal("8.99"))
                    .withCurrency("USD")
                    .withDescription("CreditCard_Discover_Processing_Error")
                    .execute("GpApiConfig");
        } catch (GatewayException ex) {
            assertEquals("50013", ex.getResponseText());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("Status Code: 502 - 200,eCom error—Developers are notified", ex.getMessage());
        }
    }

    // ================================================================================
    // Credit Card JCB ERROR
    // ================================================================================
    @Test
    public void CreditCard_JCB_Processing_Error() throws ApiException {
        CreditCardData card = creditCardData("3566000000002006", "123", "Mark Spencer");

        try {
            card
                    .charge(new BigDecimal("4.99"))
                    .withCurrency("USD")
                    .withDescription("CreditCard_JCB_Processing_Error")
                    .execute("GpApiConfig");
        } catch (GatewayException ex) {
            assertEquals("50013", ex.getResponseText());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("Status Code: 502 - 200,eCom error—Developers are notified", ex.getMessage());
        }
    }

}

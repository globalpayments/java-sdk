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
import com.global.api.tests.gpapi.BaseGpApiTest;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class GpApiSdkCertificationTest extends BaseGpApiTest {

    private final String currency = "USD";
    private final String successAuthCode = "00";
    private final String successResponseCode = "SUCCESS";

    @Before
    public void setupGpApi() throws ApiException {
        GpApiConfig gpApiConfig = new GpApiConfig();
        gpApiConfig
                .setAppId(APP_ID)
                .setAppKey(APP_KEY)
                .setChannel(Channel.CardNotPresent);

        gpApiConfig.setEnableLogging(true);

        ServicesContainer.configureService(gpApiConfig);
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
                        .withCurrency(currency)
                        .withDescription("CreditCard_Visa_Success")
                        .execute();

        assertNotNull(response);
        assertEquals("VISA", response.getCardType());
        assertEquals(successAuthCode, response.getAuthorizationCode());
        assertEquals(successResponseCode, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void CreditCard_Mastercard_Success() throws ApiException {
        CreditCardData card = creditCardData("5425230000004415", "123", "John Smith");

        Transaction response =
                card
                        .charge(new BigDecimal("4.95"))
                        .withCurrency(currency)
                        .withDescription("CreditCard_Mastercard_Success")
                        .execute();

        assertNotNull(response);
        assertEquals("MASTERCARD", response.getCardType());
        assertEquals(successAuthCode, response.getAuthorizationCode());
        assertEquals(successResponseCode, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void CreditCard_AmericanExpress_Success() throws ApiException {
        CreditCardData card = creditCardData("374101000000608", "1234", "Susan Jones");

        Transaction response =
                card
                        .charge(new BigDecimal("17.25"))
                        .withCurrency(currency)
                        .withDescription("CreditCard_AmericanExpress_Success")
                        .execute();

        assertNotNull(response);
        assertEquals("AMEX", response.getCardType());
        assertEquals(successAuthCode, response.getAuthorizationCode());
        assertEquals(successResponseCode, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void CreditCard_DinersClub_Success() throws ApiException {
        CreditCardData card = creditCardData("36256000000725", "789", "Mark Green");

        Transaction response =
                card
                        .charge(new BigDecimal("5.15"))
                        .withCurrency(currency)
                        .withDescription("CreditCard_DinersClub_Success")
                        .execute();

        assertNotNull(response);
        assertEquals("DINERS", response.getCardType());
        assertEquals(successAuthCode, response.getAuthorizationCode());
        assertEquals(successResponseCode, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void CreditCard_Discover_Success() throws ApiException {
        CreditCardData card = creditCardData("6011000000000087", "456", "Mark Green");

        Transaction response =
                card
                        .charge(new BigDecimal("2.14"))
                        .withCurrency(currency)
                        .withDescription("CreditCard_Discover_Success")
                        .execute();

        assertNotNull(response);
        assertEquals("DISCOVER", response.getCardType());
        assertEquals(successAuthCode, response.getAuthorizationCode());
        assertEquals(successResponseCode, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void CreditCard_JCB_Success() throws ApiException {
        CreditCardData card = creditCardData("3566000000000000", "223", "Mark Greens");

        Transaction response =
                card
                        .charge(new BigDecimal("1.9"))
                        .withCurrency(currency)
                        .withDescription("CreditCard_JCB_Success")
                        .execute();

        assertNotNull(response);
        assertEquals("JCB", response.getCardType());
        assertEquals(successAuthCode, response.getAuthorizationCode());
        assertEquals(successResponseCode, response.getResponseCode());
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
                        .withCurrency(currency)
                        .withDescription("CreditCard_Visa_Declined_101")
                        .execute();

        assertNotNull(response);
        assertEquals("VISA", response.getCardType());
        assertEquals("101", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    @Test
    public void CreditCard_Visa_Declined_102() throws ApiException {
        CreditCardData card = creditCardData("4000130000001724", "123", "Mark Smith");

        Transaction response =
                card
                        .charge(new BigDecimal("3.75"))
                        .withCurrency(currency)
                        .withDescription("CreditCard_Visa_Declined_102")
                        .execute();

        assertNotNull(response);
        assertEquals("VISA", response.getCardType());
        assertEquals("102", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    @Test
    public void CreditCard_Visa_Declined_103() throws ApiException {
        CreditCardData card = creditCardData("4000160000004147", "123", "Bob Smith");

        Transaction response =
                card
                        .charge(new BigDecimal("5.35"))
                        .withCurrency(currency)
                        .withDescription("CreditCard_Visa_Declined_103")
                        .execute();

        assertNotNull(response);
        assertEquals("VISA", response.getCardType());
        assertEquals("103", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    @Test
    public void CreditCard_Visa_Declined_111() throws ApiException {
        CreditCardData card = creditCardData("4242420000000091", "123", "Bob Smith");

        Transaction response =
                card
                        .charge(new BigDecimal("5.35"))
                        .withCurrency(currency)
                        .withDescription("CreditCard_Visa_Declined_111")
                        .execute();

        assertNotNull(response);
        assertEquals("VISA", response.getCardType());
        assertEquals("111", response.getAuthorizationCode());
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
                        .withCurrency(currency)
                        .withDescription("CreditCard_Mastercard_Declined_101")
                        .execute();

        assertNotNull(response);
        assertEquals("MASTERCARD", response.getCardType());
        assertEquals("101", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    @Test
    public void CreditCard_Mastercard_Declined_102() throws ApiException {
        CreditCardData card = creditCardData("5114630000009791", "123", "Tom Grey");

        Transaction response =
                card
                        .charge(new BigDecimal("4.50"))
                        .withCurrency(currency)
                        .withDescription("CreditCard_Mastercard_Declined_102")
                        .execute();

        assertNotNull(response);
        assertEquals("MASTERCARD", response.getCardType());
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
                        .withCurrency(currency)
                        .withDescription("CreditCard_Mastercard_Declined_103")
                        .execute();

        assertNotNull(response);
        assertEquals("MASTERCARD", response.getCardType());
        assertEquals("103", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    @Test
    public void CreditCard_Mastercard_Declined_111() throws ApiException {
        CreditCardData card = creditCardData("5100000000000131", "123", "Marie Curie");

        Transaction response =
                card
                        .charge(new BigDecimal("5.99"))
                        .withCurrency(currency)
                        .withDescription("CreditCard_Mastercard_Declined_111")
                        .execute();

        assertNotNull(response);
        assertEquals("MASTERCARD", response.getCardType());
        assertEquals("111", response.getAuthorizationCode());
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
                        .withCurrency(currency)
                        .withDescription("CreditCard_AmericanExpress_Declined_101")
                        .execute();

        assertNotNull(response);
        assertEquals("AMEX", response.getCardType());
        assertEquals("101", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    @Test
    public void CreditCard_AmericanExpress_Declined_102() throws ApiException {
        CreditCardData card = creditCardData("375425000000907", "1234", "Mark Smith");

        Transaction response =
                card
                        .charge(new BigDecimal("9.75"))
                        .withCurrency(currency)
                        .withDescription("CreditCard_AmericanExpress_Declined_102")
                        .execute();

        assertNotNull(response);
        assertEquals("AMEX", response.getCardType());
        assertEquals("102", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    @Test
    public void CreditCard_AmericanExpress_Declined_103() throws ApiException {
        CreditCardData card = creditCardData("343452000000306", "1234", "Bob Smith");

        Transaction response =
                card
                        .charge(new BigDecimal("1.25"))
                        .withCurrency(currency)
                        .withDescription("CreditCard_AmericanExpress_Declined_103")
                        .execute();

        assertNotNull(response);
        assertEquals("AMEX", response.getCardType());
        assertEquals("103", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    @Test
    public void CreditCard_AmericanExpress_Declined_111() throws ApiException {
        CreditCardData card = creditCardData("374205502001004", "1234", "Bob Smith");

        Transaction response =
                card
                        .charge(new BigDecimal("1.25"))
                        .withCurrency(currency)
                        .withDescription("CreditCard_AmericanExpress_Declined_111")
                        .execute();

        assertNotNull(response);
        assertEquals("AMEX", response.getCardType());
        assertEquals("111", response.getAuthorizationCode());
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
                        .withCurrency(currency)
                        .withDescription("CreditCard_DinersClub_Declined_101")
                        .execute();

        assertNotNull(response);
        assertEquals("DINERS", response.getCardType());
        assertEquals("101", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    @Test
    public void CreditCard_DinersClub_Declined_102() throws ApiException {
        CreditCardData card = creditCardData("36256000000634", "123", "John Smith");

        Transaction response =
                card
                        .charge(new BigDecimal("2.25"))
                        .withCurrency(currency)
                        .withDescription("CreditCard_DinersClub_Declined_102")
                        .execute();

        assertNotNull(response);
        assertEquals("DINERS", response.getCardType());
        assertEquals("102", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    @Test
    public void CreditCard_DinersClub_Declined_103() throws ApiException {
        CreditCardData card = creditCardData("38865000000705", "123", "John Smith");

        Transaction response =
                card
                        .charge(new BigDecimal("3.25"))
                        .withCurrency(currency)
                        .withDescription("CreditCard_DinersClub_Declined_103")
                        .execute();

        assertNotNull(response);
        assertEquals("DINERS", response.getCardType());
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
                        .withCurrency(currency)
                        .withDescription("CreditCard_Discover_Declined_101")
                        .execute();

        assertNotNull(response);
        assertEquals("DISCOVER", response.getCardType());
        assertEquals("101", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    @Test
    public void CreditCard_Discover_Declined_102() throws ApiException {
        CreditCardData card = creditCardData("6011000000001028", "123", "Rob Brown");

        Transaction response =
                card
                        .charge(new BigDecimal("2.25"))
                        .withCurrency(currency)
                        .withDescription("CreditCard_Discover_Declined_102")
                        .execute();

        assertNotNull(response);
        assertEquals("DISCOVER", response.getCardType());
        assertEquals("102", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    @Test
    public void CreditCard_Discover_Declined_103() throws ApiException {
        CreditCardData card = creditCardData("6011000000001036", "123", "Rob Brown");

        Transaction response =
                card
                        .charge(new BigDecimal("3.25"))
                        .withCurrency(currency)
                        .withDescription("CreditCard_Discover_Declined_103")
                        .execute();

        assertNotNull(response);
        assertEquals("DISCOVER", response.getCardType());
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
                        .withCurrency(currency)
                        .withDescription("CreditCard_JCB_Declined_101")
                        .execute();

        assertNotNull(response);
        assertEquals("JCB", response.getCardType());
        assertEquals("101", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    @Test
    public void CreditCard_JCB_Declined_102() throws ApiException {
        CreditCardData card = creditCardData("3566000000001024", "123", "Michael Smith");

        Transaction response =
                card
                        .charge(new BigDecimal("2.25"))
                        .withCurrency(currency)
                        .withDescription("CreditCard_JCB_Declined_102")
                        .execute();

        assertNotNull(response);
        assertEquals("JCB", response.getCardType());
        assertEquals("102", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    @Test
    public void CreditCard_JCB_Declined_103() throws ApiException {
        CreditCardData card = creditCardData("3566000000001032", "123", "Michael Smith");

        Transaction response =
                card
                        .charge(new BigDecimal("3.25"))
                        .withCurrency(currency)
                        .withDescription("CreditCard_JCB_Declined_103")
                        .execute();

        assertNotNull(response);
        assertEquals("JCB", response.getCardType());
        assertEquals("103", response.getAuthorizationCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseCode());
    }

    // ================================================================================
    // Credit Card Visa ERROR
    // ================================================================================
    @Test
    public void CreditCard_Visa_Processing_Error() throws ApiException {
        CreditCardData card = creditCardData("4009830000001985", "123", "Mark Spencer");

        boolean errorFound = false;
        try {
            card
                    .charge(new BigDecimal("3.99"))
                    .withCurrency(currency)
                    .withDescription("CreditCard_Visa_Processing_Error")
                    .execute();
        } catch (GatewayException ex) {
            errorFound = true;
            assertEquals("Status Code: 502 - 200,eCom error—Developers are notified", ex.getMessage());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("50013", ex.getResponseText());
        } finally {
            assertTrue(errorFound);
        }
    }

    @Test
    public void CreditCard_Visa_Processing_Error_Wrong_Currency() throws ApiException {
        CreditCardData card = creditCardData("4009830000001985", "123", "Mark Spencer");

        boolean errorFound = false;
        try {
            card
                    .charge(new BigDecimal("3.99"))
                    .withCurrency("XXX")
                    .withDescription("CreditCard_Visa_Processing_Error_Wrong_Currency")
                    .execute();
        } catch (GatewayException ex) {
            errorFound = true;
            assertEquals("Status Code: 502 - currency card combination not allowed", ex.getMessage());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("50024", ex.getResponseText());
        } finally {
            assertTrue(errorFound);
        }
    }

    // ================================================================================
    // Credit Card Mastercard ERROR
    // ================================================================================
    @Test
    public void CreditCard_Mastercard_Processing_Error() throws ApiException {
        CreditCardData card = creditCardData("5135020000005871", "123", "Tom Brown");

        boolean errorFound = false;
        try {
            card
                    .charge(new BigDecimal("2.16"))
                    .withCurrency(currency)
                    .withDescription("CreditCard_Mastercard_Processing_Error")
                    .execute();
        } catch (GatewayException ex) {
            errorFound = true;
            assertEquals("Status Code: 502 - 200,eCom error—Developers are notified", ex.getMessage());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("50013", ex.getResponseText());
        } finally {
            assertTrue(errorFound);
        }
    }

    // ================================================================================
    // Credit Card American Express ERROR
    // ================================================================================
    @Test
    public void CreditCard_AmericanExpress_Processing_Error() throws ApiException {
        CreditCardData card = creditCardData("372349000000852", "1234", "Tina White");

        boolean errorFound = false;
        try {
            card
                    .charge(new BigDecimal("4.02"))
                    .withCurrency(currency)
                    .withDescription("CreditCard_AmericanExpress_Processing_Error")
                    .execute();
        } catch (GatewayException ex) {
            errorFound = true;
            assertEquals("Status Code: 502 - 200,eCom error—Developers are notified", ex.getMessage());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("50013", ex.getResponseText());
        } finally {
            assertTrue(errorFound);
        }
    }

    // Credit Card Diners Club ERROR
    @Test
    public void CreditCard_DinersClub_Processing_Error() throws ApiException {
        CreditCardData card = creditCardData("30450000000985", "123", "Ashley Brown");

        boolean errorFound = false;
        try {
            card
                    .charge(new BigDecimal("5.99"))
                    .withCurrency(currency)
                    .withDescription("CreditCard_DinersClub_Processing_Error")
                    .execute();
        } catch (GatewayException ex) {
            errorFound = true;
            assertEquals("Status Code: 502 - 200,eCom error—Developers are notified", ex.getMessage());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("50013", ex.getResponseText());
        } finally {
            assertTrue(errorFound);
        }
    }

    // ================================================================================
    // Credit Card Discover ERROR
    // ================================================================================
    @Test
    public void CreditCard_Discover_Processing_Error() throws ApiException {
        CreditCardData card = creditCardData("6011000000002000", "123", "Mark Spencer");

        boolean errorFound = false;
        try {
            card
                    .charge(new BigDecimal("8.99"))
                    .withCurrency(currency)
                    .withDescription("CreditCard_Discover_Processing_Error")
                    .execute();
        } catch (GatewayException ex) {
            errorFound = true;
            assertEquals("Status Code: 502 - 200,eCom error—Developers are notified", ex.getMessage());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("50013", ex.getResponseText());
        } finally {
            assertTrue(errorFound);
        }
    }

    // ================================================================================
    // Credit Card JCB ERROR
    // ================================================================================
    @Test
    public void CreditCard_JCB_Processing_Error() throws ApiException {
        CreditCardData card = creditCardData("3566000000002006", "123", "Mark Spencer");

        boolean errorFound = false;
        try {
            card
                    .charge(new BigDecimal("4.99"))
                    .withCurrency(currency)
                    .withDescription("CreditCard_JCB_Processing_Error")
                    .execute();
        } catch (GatewayException ex) {
            errorFound = true;
            assertEquals("Status Code: 502 - 200,eCom error—Developers are notified", ex.getMessage());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("50013", ex.getResponseText());
        } finally {
            assertTrue(errorFound);
        }
    }

    // ================================================================================
    // Credit Card UATP ERROR
    // ================================================================================
    @Test
    public void CreditCard_UATP_Transaction_Not_Supported_Error() throws ApiException {
        CreditCardData card = creditCardData("135400000007187", "123", "Tom Brown");

        boolean errorFound = false;
        try {
            card
                    .charge(new BigDecimal("2.16"))
                    .withCurrency(currency)
                    .withDescription("CreditCard_UATP_Processing_Error")
                    .execute();
        } catch (GatewayException ex) {
            errorFound = true;
            assertEquals("Status Code: 400 - Transaction not supported Please contact support ", ex.getMessage());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("50020", ex.getResponseText());
        } finally {
            assertTrue(errorFound);
        }
    }

}

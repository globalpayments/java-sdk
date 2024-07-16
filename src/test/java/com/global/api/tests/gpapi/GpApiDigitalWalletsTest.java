package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.MobilePaymentMethodType;
import com.global.api.entities.enums.TransactionModifier;
import com.global.api.entities.enums.TransactionStatus;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.utils.StringUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class GpApiDigitalWalletsTest extends BaseGpApiTest {

    private final CreditCardData card;
    private final String clickToPayToken = "8144735251653223601";
    private final String googlePayToken = "{\n" +
            "    \"signature\": \"MEQCICeHhPSrVhVghzBUwtQcBZq9ed1+oe8wcInjOG5TciXeAiBmw35LvnI0POMW5p+oxisD2ImtZ9HmyKcYIYUX4kINtw\\u003d\\u003d\",\n" +
            "    \"protocolVersion\": \"ECv1\",\n" +
            "    \"signedMessage\": \"{\\\"encryptedMessage\\\":\\\"E+gmqwjr4RZUMA0iQKJJS69k+kQirnRQGf4F0OglGXNcXnBCHDAmgKyjFxTS8l0pRkgG6f6qvgrTDPL6m+XxOIAQ9mh6yM2MerybNYaPUuVZl2MGBe1eckVg3oYYsuIOLkF6Qrq/+fpZhcXZPQbsAgx0dt1IDOE3XL245aIrxHED01gFLZQE2eizQIXVtbU4eJahysBjzDu9nUMhHKE3eAHW+ltuyFtB4jGTUKRJHM5x2YsXgyy5hzY9zjiPYc7uNNSCWQUUSYdVY0dzT9DUO5jvl0YhsZVvjHgSCRKEsBisDbbhs3pkUvCQnPZYSqflTl/EendCyUDq8PgtVSNYdyH+ByAe67kOMbiQ2LTXu8Nry0/UUvTdc8RdRR3aC1LpKf2KlEFiYdbYTi1IiGiK16cJHqrbOyjXXzqWp878jn2hSO3/ONAdshacHGrFWw58BHH9\\\",\\\"ephemeralPublicKey\\\":\\\"BJlb3xtHbQ/7thj/4D+eXls+fbjewUw1EWEc6diZOvkeIBPvUbbTCzHo448t6sEuffCZ9u+rqVWnA2p6djT+1h8\\\\u003d\\\",\\\"tag\\\":\\\"CSB0z2fhjaY5KjRnh1k0G5X2HiTxLTdXUYTiflKm7So\\\\u003d\\\"}\"\n" +
            "}";

    public GpApiDigitalWalletsTest() throws ApiException {
        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        ServicesContainer.configureService(config);

        card = new CreditCardData();
        card.setCardHolderName("James Mason");
    }

    @Test
    public void ClickToPayEncrypted() throws ApiException {
        card.setToken(clickToPayToken);
        card.setMobileType(MobilePaymentMethodType.CLICK_TO_PAY);

        Transaction response =
                card
                        .charge(10)
                        .withCurrency("EUR")
                        .withModifier(TransactionModifier.EncryptedMobile)
                        .withMaskedDataResponse(true)
                        .execute();

        assertNotNull(response);
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
        assertEquals(SUCCESS, response.getResponseCode());
        assertFalse(StringUtils.isNullOrEmpty(response.getTransactionId()));
        assertClickToPayPayerDetails(response);
    }

    @Test
    public void ClickToPayEncryptedChargeThenRefund() throws ApiException {
        card.setToken(clickToPayToken);
        card.setMobileType(MobilePaymentMethodType.CLICK_TO_PAY);

        Transaction response =
                card
                        .charge(10)
                        .withCurrency("EUR")
                        .withModifier(TransactionModifier.EncryptedMobile)
                        .withMaskedDataResponse(true)
                        .execute();

        assertNotNull(response);
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
        assertClickToPayPayerDetails(response);

        Transaction refund =
                response
                        .refund()
                        .withCurrency("EUR")
                        .withAllowDuplicates(true)
                        .execute();

        assertNotNull(refund);
        assertEquals(TransactionStatus.Captured.getValue(), refund.getResponseMessage());
        assertClickToPayPayerDetails(response);
    }

    @Test
    public void ClickToPayEncryptedChargeThenReverse() throws ApiException {
        card.setToken(clickToPayToken);
        card.setMobileType(MobilePaymentMethodType.CLICK_TO_PAY);

        Transaction response =
                card
                        .charge(10)
                        .withCurrency("EUR")
                        .withModifier(TransactionModifier.EncryptedMobile)
                        .withMaskedDataResponse(true)
                        .execute();

        assertNotNull(response);
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
        assertClickToPayPayerDetails(response);

        Transaction reverse =
                response
                        .reverse()
                        .withCurrency("EUR")
                        .withAllowDuplicates(true)
                        .execute();

        assertNotNull(reverse);
        assertEquals(TransactionStatus.Reversed.getValue(), reverse.getResponseMessage());
        assertClickToPayPayerDetails(response);
    }

    @Test
    public void ClickToPayEncryptedAuthorize() throws ApiException {
        card.setToken(clickToPayToken);
        card.setMobileType(MobilePaymentMethodType.CLICK_TO_PAY);

        boolean errorFound = false;
        try {
            card
                    .authorize(10)
                    .withCurrency("EUR")
                    .withModifier(TransactionModifier.EncryptedMobile)
                    .withMaskedDataResponse(true)
                    .execute();
        } catch (GatewayException ex) {
            errorFound = true;
            assertEquals("Status Code: 400 - capture_mode contains unexpected data.", ex.getMessage());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40213", ex.getResponseText());
        } finally {
            assertTrue(errorFound);
        }
    }

    @Test
    public void ClickToPayEncryptedRefundStandalone() throws ApiException {
        card.setToken(clickToPayToken);
        card.setMobileType(MobilePaymentMethodType.CLICK_TO_PAY);

        boolean errorFound = false;
        try {
            card
                    .refund(10)
                    .withCurrency("EUR")
                    .withModifier(TransactionModifier.EncryptedMobile)
                    .withMaskedDataResponse(true)
                    .execute();
        } catch (GatewayException ex) {
            errorFound = true;
            assertEquals("Status Code: 400 - Mandatory Fields missing [ request card number] See Developers Guide", ex.getMessage());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("50021", ex.getResponseText());
        } finally {
            assertTrue(errorFound);
        }
    }

    @Test
    @Ignore
    //You need a valid Apple Pay token that it is valid only for 60 sec
    public void PayWithApplePayEncrypted() throws ApiException {
        card.setToken("{\"version\":\"EC_v1\",\"data\":\"7RbOpd67lcBgPFDHwpVCtz1g9DSXIEz6h7XUhsWiVS3B5WpkpgJ3Mj/EKNuuHezpPmsJ6AZb72twHiR/6Ngs29X4jKcv3XvdrEgL+5S7dKZoU0sNN3y7UWBFFklUgF+FGv9Amvytoav0mV+Pfe0UWenyb8smF5fZDF5Ta8d30WPkBaPf6IpD2sOXHoVxgqvoQNkr6rQNoG3Tm+fHzOukNTsRGxi35OZvx4SgKxZvivMiH7xs4DKnRZMiKWl+4Zym48/UQ+F/+cwM/7rCY+r7BPlki6xE50IEl2/4PPl6wzhs1AkfqVJB79J0iNHL5/CMTFi/UgUFmIRMTrujVHerhqGnFyIJ6jutsS9H6TJ6+6M9OUzfG53XNolUxJ0Nox9MA9uQxozw2tTJt/Z0RBpbTU8jnTvN9s5053xP/Hxx9dg=\",\"signature\":\"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\",\"header\":{\"ephemeralPublicKey\":\"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEeyyM++BjGrlaodphlJUvfTx4tQwn5Ci9IGpAI3RvbYqEshGX5cdkl0j7yNEu913OgT99r/MU1wqHnXn4p7qosA==\",\"publicKeyHash\":\"rEYX/7PdO7F7xL7rH0LZVak/iXTrkeU89Ck7E9dGFO4=\",\"transactionId\":\"38bb5ca49bc54c70e6ff5996bd087f1cce27f0f84fca2f6e71871fc7a56d877e\"}}");
        card.setMobileType(MobilePaymentMethodType.APPLEPAY);

        Transaction transaction =
                card
                        .charge(new BigDecimal(10))
                        .withCurrency("EUR")
                        .withModifier(TransactionModifier.EncryptedMobile)
                        .execute();

        assertNotNull(transaction);
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertFalse(StringUtils.isNullOrEmpty(transaction.getTransactionId()));
    }

    @Test
    @Ignore
    // You need a valid Apple Pay token that it is valid only for 60 sec
    public void PayWithApplePayEncryptedChargedThenRefund() throws ApiException {
        card.setToken("{\"version\":\"EC_v1\",\"data\":\"u4gZ7Qvq9jF8DN13aWOB8FkQ4coB5nurVfuG7DOv2JAbswT8KrcMqcIK2KdtFOzncmrKEaB3w8oysY0ZKfYxLS2KrK0mnQhY3qAbmNb3HfJ433CfsO1hTSEZgCDNsWFLkcOql4Do2wk4IKVlEq105CjRGfNeYn0gkWjGh9T/yT85WYElMB216nKH3WoutefbEt/uoN2aKUSImaciFPy6qDwFbtX1pOQ8kT//n7MvUfl7aUR83MTgktpH9VEU19k6K+H6D8xvecAlXiYt4zNCiw2XkYKWR0cg+4GxBqTl1RI1DV0bU0ZR4Qyz2FAmadvXohC7qZnOrh0FX/4w7D5DSP+O4BF3uCst4XIRJsQaz9zyr1GncE3qsePV4Q0WRfxoARvzBF0MQhnNfbR9cBmqgAFCMPlk0Qv0UJg+rbwgYGQ=\",\"signature\":\"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\",\"header\":{\"ephemeralPublicKey\":\"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEuhKOdDaqF0EjVE0qN4Cb1+d+hdXkoy4LIKbGzymTbIiMBj6tU2OZIDGes21fgEbIsXn92nkuwpalEsAoGueVjg==\",\"publicKeyHash\":\"rEYX/7PdO7F7xL7rH0LZVak/iXTrkeU89Ck7E9dGFO4=\",\"transactionId\":\"e9cfc9cc29bf17945a53fea1d8f65265ee2962708a46d7696ffc90090ea364aa\"}}");
        card.setMobileType(MobilePaymentMethodType.APPLEPAY);

        Transaction transaction =
                card
                        .charge(10)
                        .withCurrency("USD")
                        .withModifier(TransactionModifier.EncryptedMobile)
                        .execute();

        assertNotNull(transaction);
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());
        assertEquals("Success", transaction.getResponseCode());
        assertFalse(StringUtils.isNullOrEmpty(transaction.getTransactionId()));

        Transaction refund =
                transaction
                        .refund()
                        .withCurrency("USD")
                        .execute();

        assertNotNull(refund);
        assertEquals("Success", refund.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), refund.getResponseMessage());
    }

    @Test
    @Ignore
    // You need a valid Apple Pay token that it is valid only for 60 sec
    public void PayWithApplePayEncryptedChargedThenReverse() throws ApiException {
        card.setToken("{\"version\":\"EC_v1\",\"data\":\"EXcXc7RAa3/fyG30V1xmTRZZDBOwQMmURCNsE4gutV6apykQlbR7uAJEqbpI4hURpKXUc+g5uGCx2Ugp4XPH+CvnYBdeNiy6dVW9aGqY3fJubm0m1ye9xVTBCZtNF3+DsccYu08ER8H2eweiOa/ugOdcfvCKc86QIWdvFuQHHGYQG6GZl8krQOYT81x1cMfmNqisjCoS29F9p+MxvH7kvxGPewJ2cPvTOSyZx4wKRcKv+pblR/sktRBdkJGloGxUnxtCGM3DUE+CAKoFftfZramvlOCVY+1dbXnqDdf5P3ROTAGoP3F21Sgw43eQvKktaJ+99CAXB5ITtChEnEPMj24CDsvD+DyjRYjLytYzrv5gHAcTnySHdyeRldiYTyRHMJtN1PwnhFl/tlLOpNKgTr4I9TIHDwJpp4ujczilKIc=\",\"signature\":\"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\",\"header\":{\"ephemeralPublicKey\":\"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEDt8/QYDs4S7NjtJmOgzU71QuryCjBSYgPfdcP+Wr0KPQn2poE1X6gAk+702JNJ0pA0yqX+8OC93CNrB2DQ55cA==\",\"publicKeyHash\":\"rEYX/7PdO7F7xL7rH0LZVak/iXTrkeU89Ck7E9dGFO4=\",\"transactionId\":\"bd4bfd340914027b54c5aec21993d3537133228311b57defc36f1a107acd5662\"}}");
        card.setMobileType(MobilePaymentMethodType.APPLEPAY);

        Transaction transaction =
                card
                        .charge(10)
                        .withCurrency("USD")
                        .withModifier(TransactionModifier.EncryptedMobile)
                        .execute();

        assertNotNull(transaction);
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());
        assertEquals("Success", transaction.getResponseCode());
        assertFalse(StringUtils.isNullOrEmpty(transaction.getTransactionId()));

        Transaction reverse =
                transaction
                        .reverse()
                        .withCurrency("USD")
                        .execute();

        assertNotNull(reverse);
        assertEquals("Success", reverse.getResponseCode());
        assertEquals(TransactionStatus.Reversed.getValue(), reverse.getResponseMessage());
    }

    @Test
    public void PayWithDecryptedFlow() throws ApiException {
        List<MobilePaymentMethodType> mobilePaymentMethodTypes = Arrays.asList(MobilePaymentMethodType.APPLEPAY, MobilePaymentMethodType.GOOGLEPAY);

        Address address = new Address();
        address.setPostalCode("WB3 A21");
        address.setStreetAddress1("Flat 456");

        for (MobilePaymentMethodType mobilePaymentMethodType : mobilePaymentMethodTypes) {
            card.setToken("5167300431085507");
            card.setMobileType(mobilePaymentMethodType);
            card.setExpMonth(expMonth);
            card.setExpYear(expYear);
            card.setCryptogram("234234234");

            Transaction transaction =
                    card
                            .charge(new BigDecimal(5))
                            .withCurrency("EUR")
                            .withAddress(address)
                            .withModifier(TransactionModifier.DecryptedMobile)
                            .execute();

            assertNotNull(transaction);
            assertEquals(SUCCESS, transaction.getResponseCode());
            assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());
            assertFalse(StringUtils.isNullOrEmpty(transaction.getTransactionId()));
            assertFalse(StringUtils.isNullOrEmpty(transaction.getAuthorizationCode()));
        }
    }

    @Test
    public void PayWithGooglePayEncrypted() throws ApiException {
        card.setToken(googlePayToken);
        card.setMobileType(MobilePaymentMethodType.GOOGLEPAY);

        Transaction transaction =
                card
                        .charge(new BigDecimal(10))
                        .withCurrency("EUR")
                        .withModifier(TransactionModifier.EncryptedMobile)
                        .execute();

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());
        assertFalse(StringUtils.isNullOrEmpty(transaction.getTransactionId()));
    }

    @Test
    public void GooglePayEncrypted_LinkedRefund() throws ApiException {
        card.setToken(googlePayToken);
        card.setMobileType(MobilePaymentMethodType.GOOGLEPAY);

        Transaction transaction =
                card
                        .charge(new BigDecimal(10))
                        .withCurrency("EUR")
                        .withModifier(TransactionModifier.EncryptedMobile)
                        .execute();

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());
        assertFalse(StringUtils.isNullOrEmpty(transaction.getTransactionId()));

        Transaction refund =
                transaction
                        .refund()
                        .withCurrency("EUR")
                        .execute();

        assertNotNull(refund);
        assertEquals(SUCCESS, refund.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), refund.getResponseMessage());
    }

    @Test
    public void GooglePayEncrypted_Reverse() throws ApiException {
        card.setToken(googlePayToken);
        card.setMobileType(MobilePaymentMethodType.GOOGLEPAY);

        Transaction transaction =
                card
                        .charge(new BigDecimal(10))
                        .withCurrency("EUR")
                        .withModifier(TransactionModifier.EncryptedMobile)
                        .execute();

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());
        assertFalse(StringUtils.isNullOrEmpty(transaction.getTransactionId()));

        Transaction reverse =
                transaction
                        .reverse()
                        .withCurrency("EUR")
                        .execute();

        assertNotNull(reverse);
        assertEquals(SUCCESS, reverse.getResponseCode());
        assertEquals(TransactionStatus.Reversed.getValue(), reverse.getResponseMessage());
    }

    private void assertClickToPayPayerDetails(Transaction response) {
        assertNotNull(response.getPayerDetails());
        assertNotNull(response.getPayerDetails().getEmail());
        assertNotNull(response.getPayerDetails().getBillingAddress());
        assertNotNull(response.getPayerDetails().getShippingAddress());
        assertNotNull(response.getPayerDetails().getFirstName());
        assertNotNull(response.getPayerDetails().getLastName());
    }
}
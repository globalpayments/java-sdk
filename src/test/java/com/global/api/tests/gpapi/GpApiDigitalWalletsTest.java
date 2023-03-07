package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.utils.StringUtils;
import lombok.var;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class GpApiDigitalWalletsTest extends BaseGpApiTest {

    private final CreditCardData card;

    public GpApiDigitalWalletsTest() throws ApiException {

        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId(APP_ID)
                .setAppKey(APP_KEY)
                .setChannel(Channel.CardNotPresent.getValue());

        config.setEnableLogging(true);

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);

        card = new CreditCardData();
        card.setCardHolderName("James Mason#");
    }

    @Test
    public void ClickToPayEncrypted() throws ApiException {
        card.setToken("9113329269393758302");
        card.setMobileType(MobilePaymentMethodType.CLICK_TO_PAY);

        var response =
                card
                        .charge(10)
                        .withCurrency("EUR")
                        .withModifier(TransactionModifier.EncryptedMobile)
                        .withMaskedDataResponse(true)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
        assertEquals(SUCCESS, response.getResponseCode());
        assertFalse(StringUtils.isNullOrEmpty(response.getTransactionId()));
    }

    @Test @Ignore
    //You need a valid Apple Pay token that it is valid only for 60 sec
    public void PayWithApplePayEncrypted() throws ApiException {
        card.setToken("{\"version\":\"EC_v1\",\"data\":\"7RbOpd67lcBgPFDHwpVCtz1g9DSXIEz6h7XUhsWiVS3B5WpkpgJ3Mj/EKNuuHezpPmsJ6AZb72twHiR/6Ngs29X4jKcv3XvdrEgL+5S7dKZoU0sNN3y7UWBFFklUgF+FGv9Amvytoav0mV+Pfe0UWenyb8smF5fZDF5Ta8d30WPkBaPf6IpD2sOXHoVxgqvoQNkr6rQNoG3Tm+fHzOukNTsRGxi35OZvx4SgKxZvivMiH7xs4DKnRZMiKWl+4Zym48/UQ+F/+cwM/7rCY+r7BPlki6xE50IEl2/4PPl6wzhs1AkfqVJB79J0iNHL5/CMTFi/UgUFmIRMTrujVHerhqGnFyIJ6jutsS9H6TJ6+6M9OUzfG53XNolUxJ0Nox9MA9uQxozw2tTJt/Z0RBpbTU8jnTvN9s5053xP/Hxx9dg=\",\"signature\":\"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\",\"header\":{\"ephemeralPublicKey\":\"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEeyyM++BjGrlaodphlJUvfTx4tQwn5Ci9IGpAI3RvbYqEshGX5cdkl0j7yNEu913OgT99r/MU1wqHnXn4p7qosA==\",\"publicKeyHash\":\"rEYX/7PdO7F7xL7rH0LZVak/iXTrkeU89Ck7E9dGFO4=\",\"transactionId\":\"38bb5ca49bc54c70e6ff5996bd087f1cce27f0f84fca2f6e71871fc7a56d877e\"}}");
        card.setMobileType(MobilePaymentMethodType.APPLEPAY);

        Transaction transaction =
                card
                    .charge(new BigDecimal(10))
                    .withCurrency("EUR")
                    .withModifier(TransactionModifier.EncryptedMobile)
                    .execute(GP_API_CONFIG_NAME);

        assertNotNull(transaction);
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertFalse(StringUtils.isNullOrEmpty(transaction.getTransactionId()));
    }

    @Test
    public void PayWithDecryptedFlow() throws ApiException {
        List<MobilePaymentMethodType> mobilePaymentMethodTypes = Arrays.asList(MobilePaymentMethodType.APPLEPAY, MobilePaymentMethodType.GOOGLEPAY);

        for(MobilePaymentMethodType mobilePaymentMethodType : mobilePaymentMethodTypes) {
            card.setToken("5167300431085507");
            card.setMobileType(mobilePaymentMethodType);
            card.setExpMonth(expMonth);
            card.setExpYear(expYear);
            card.setCryptogram("234234234");

            Transaction transaction =
                    card
                            .charge(new BigDecimal(5))
                            .withCurrency("EUR")
                            .withModifier(TransactionModifier.DecryptedMobile)
                            .execute(GP_API_CONFIG_NAME);

            assertNotNull(transaction);
            assertEquals(SUCCESS, transaction.getResponseCode());
            assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());
            assertFalse(StringUtils.isNullOrEmpty(transaction.getTransactionId()));
            assertFalse(StringUtils.isNullOrEmpty(transaction.getAuthorizationCode()));
        }
    }

    @Test
    public void PayWithGooglePayEncrypted() throws ApiException {
        card.setToken("{\n" +
                "  \"signature\": \"MEQCIG5NDTGsgPrltVuuJQ0NYJnLUm1mo8JOW8vfLvt+ZtW6AiAZXJgnszos88b06ujEKubOMQNIuFdOgU+6ArRDGlZPoQ==\",\n" +
                "  \"protocolVersion\": \"ECv1\",\n" +
                "  \"signedMessage\": \"{\\\"encryptedMessage\\\":\\\"t+8CD+SWUl7VflMnwOqJedp7ZK71qnKX2tabp7px4C3d97ki1eb8vySoZoD8wbp7B2FDZPVGPDtUSUxz67JfwJ04sncw4KyDQiyShlxxdOakK/bnn+ooGHZ3jWA97hXVjtf1YecOCppzQIVk7F4RZZNgfIImQr4nDX5nm4BLN+TGsI4/m542xix45h+5e7y/xtyY/GFm8YwEfHQFCNY5edOr7h4nanDi/k//oQQG+ChOCKHtcmv4LpwtR2W737t5bTj+5mxXWu2sdAx6EnQGFOnmMKbNd1phcw4fdAHGtmxkW2LDUamXq5hnY99ECcl+Iqe75S6d3vQ3Lidk3k2lVVRtXTkxtCGvBOUy8okp4hndQL0665YOOIgEPengKfv3CIgUcKahKPyRVekaybDUalYpvyBvX2xIq+ilOo703z7pdL+FkILe7PC7RrEbnKLLJEv6\\\",\\\"ephemeralPublicKey\\\":\\\"BMLc+beu32XtPnrQ6D2z7IuICWl0gg9XmnKEync31BZ7NAvCa0BXTpLkyii+dDMPahj6xUoI0m7YES2UncrVkS8\\\\u003d\\\",\\\"tag\\\":\\\"QlvO9HQqmHXnlew8IzEWqr1AFSNL2wSc+HEb2IAd4YE\\\\u003d\\\"}\"\n" +
                "}");

        card.setMobileType(MobilePaymentMethodType.GOOGLEPAY);

        Transaction transaction =
                card
                        .charge(new BigDecimal(10))
                        .withCurrency("EUR")
                        .withModifier(TransactionModifier.EncryptedMobile)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());
        assertFalse(StringUtils.isNullOrEmpty(transaction.getTransactionId()));
    }

@Test
    public void GooglePayEncrypted_LinkedRefund() throws ApiException {
        card.setToken("{\n" +
                "  \"signature\": \"MEQCIG5NDTGsgPrltVuuJQ0NYJnLUm1mo8JOW8vfLvt+ZtW6AiAZXJgnszos88b06ujEKubOMQNIuFdOgU+6ArRDGlZPoQ==\",\n" +
                "  \"protocolVersion\": \"ECv1\",\n" +
                "  \"signedMessage\": \"{\\\"encryptedMessage\\\":\\\"t+8CD+SWUl7VflMnwOqJedp7ZK71qnKX2tabp7px4C3d97ki1eb8vySoZoD8wbp7B2FDZPVGPDtUSUxz67JfwJ04sncw4KyDQiyShlxxdOakK/bnn+ooGHZ3jWA97hXVjtf1YecOCppzQIVk7F4RZZNgfIImQr4nDX5nm4BLN+TGsI4/m542xix45h+5e7y/xtyY/GFm8YwEfHQFCNY5edOr7h4nanDi/k//oQQG+ChOCKHtcmv4LpwtR2W737t5bTj+5mxXWu2sdAx6EnQGFOnmMKbNd1phcw4fdAHGtmxkW2LDUamXq5hnY99ECcl+Iqe75S6d3vQ3Lidk3k2lVVRtXTkxtCGvBOUy8okp4hndQL0665YOOIgEPengKfv3CIgUcKahKPyRVekaybDUalYpvyBvX2xIq+ilOo703z7pdL+FkILe7PC7RrEbnKLLJEv6\\\",\\\"ephemeralPublicKey\\\":\\\"BMLc+beu32XtPnrQ6D2z7IuICWl0gg9XmnKEync31BZ7NAvCa0BXTpLkyii+dDMPahj6xUoI0m7YES2UncrVkS8\\\\u003d\\\",\\\"tag\\\":\\\"QlvO9HQqmHXnlew8IzEWqr1AFSNL2wSc+HEb2IAd4YE\\\\u003d\\\"}\"\n" +
                "}");

        card.setMobileType(MobilePaymentMethodType.GOOGLEPAY);

        Transaction transaction =
                card
                        .charge(new BigDecimal(10))
                        .withCurrency("EUR")
                        .withModifier(TransactionModifier.EncryptedMobile)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());
        assertFalse(StringUtils.isNullOrEmpty(transaction.getTransactionId()));

        Transaction refund =
                transaction
                        .refund()
                        .withCurrency("EUR")
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(refund);
        assertEquals(SUCCESS, refund.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), refund.getResponseMessage());
    }

    @Test
    public void GooglePayEncrypted_Reverse() throws ApiException {
        card.setToken("{\n" +
                "  \"signature\": \"MEQCIG5NDTGsgPrltVuuJQ0NYJnLUm1mo8JOW8vfLvt+ZtW6AiAZXJgnszos88b06ujEKubOMQNIuFdOgU+6ArRDGlZPoQ==\",\n" +
                "  \"protocolVersion\": \"ECv1\",\n" +
                "  \"signedMessage\": \"{\\\"encryptedMessage\\\":\\\"t+8CD+SWUl7VflMnwOqJedp7ZK71qnKX2tabp7px4C3d97ki1eb8vySoZoD8wbp7B2FDZPVGPDtUSUxz67JfwJ04sncw4KyDQiyShlxxdOakK/bnn+ooGHZ3jWA97hXVjtf1YecOCppzQIVk7F4RZZNgfIImQr4nDX5nm4BLN+TGsI4/m542xix45h+5e7y/xtyY/GFm8YwEfHQFCNY5edOr7h4nanDi/k//oQQG+ChOCKHtcmv4LpwtR2W737t5bTj+5mxXWu2sdAx6EnQGFOnmMKbNd1phcw4fdAHGtmxkW2LDUamXq5hnY99ECcl+Iqe75S6d3vQ3Lidk3k2lVVRtXTkxtCGvBOUy8okp4hndQL0665YOOIgEPengKfv3CIgUcKahKPyRVekaybDUalYpvyBvX2xIq+ilOo703z7pdL+FkILe7PC7RrEbnKLLJEv6\\\",\\\"ephemeralPublicKey\\\":\\\"BMLc+beu32XtPnrQ6D2z7IuICWl0gg9XmnKEync31BZ7NAvCa0BXTpLkyii+dDMPahj6xUoI0m7YES2UncrVkS8\\\\u003d\\\",\\\"tag\\\":\\\"QlvO9HQqmHXnlew8IzEWqr1AFSNL2wSc+HEb2IAd4YE\\\\u003d\\\"}\"\n" +
                "}");

        card.setMobileType(MobilePaymentMethodType.GOOGLEPAY);

        Transaction transaction =
                card
                        .charge(new BigDecimal(10))
                        .withCurrency("EUR")
                        .withModifier(TransactionModifier.EncryptedMobile)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());
        assertFalse(StringUtils.isNullOrEmpty(transaction.getTransactionId()));

        Transaction refund =
                transaction
                        .reverse()
                        .withCurrency("EUR")
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(refund);
        assertEquals(SUCCESS, refund.getResponseCode());
        assertEquals(TransactionStatus.Reversed.getValue(), refund.getResponseMessage());
    }

}
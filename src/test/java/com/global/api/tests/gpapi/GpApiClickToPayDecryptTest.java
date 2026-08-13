package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.ThreeDSecure;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.utils.StringUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Click to Pay decrypt flow via GP-API /decrypt endpoint.
 * Prerequisites:
 * - Region: Europe
 * - Gateway: GP-API
 * - Environment: Sandbox (apis.sandbox.eu.globalpay.com)
 * - The CTP payment_token values below are sandbox tokens and expire quickly;
 * replace with fresh tokens when running end-to-end.
 */
public class GpApiClickToPayDecryptTest extends BaseGpApiTest {

    private static final String ACCOUNT_NAME = "GPECOM_Transaction_Processing_CNP";

    /**
     * Sample Visa Click to Pay JWT token (sandbox). Replace with a fresh token for live runs.
     */
    private static final String CTP_VISA_TOKEN =
            "eyJraWQiOiJKN00zRTE0V1pVUDhHRkxUQkUzODEzcm1ydC0tRGRFTTZGZEZORVdQakpjNVJfTHZrIiwiYWxnIjoiUlMyNTYiLCJqdGkiOiJOV000WlRGak9HSXRPREF4T1MwME56aGtMVGsyWldJdE0yTXhNR0l5TWpZMFpHTTUiLCJpYXQiOjE3NzU1NDg5ODJ9.eyJzcmNDb3JyZWxhdGlvbklkIjoiYWE4N2ZhNzYtYjBlYi00MTFlLWMzZTQtMTRkOGU1OGUyZjAxIiwic3JjaVRyYW5zYWN0aW9uSWQiOiI3MjYyOWQ2Ni0zYTBmLTRjMjUtOGU3Ny0wMzYxYzU1NDhkZGYiLCJtYXNrZWRDYXJkIjp7InNyY0RpZ2l0YWxDYXJkSWQiOiI5ZmFmYjM4NTA2MzQwY2YyMzE3MjFmZDgzNjBlYWQwMiIsInBhbkJpbiI6IjQzOTU4NCIsInBhbkxhc3RGb3VyIjoiMDExMCIsInRva2VuQmluUmFuZ2UiOiI0OTA2MjQ2OTciLCJwYXltZW50QWNjb3VudFJlZmVyZW5jZSI6IlYwMDEwMDEzMDI0MzI1NjcxNjE0MjU3NzQ0MTgwIiwidG9rZW5MYXN0Rm91ciI6IjAyMDAiLCJwYW5FeHBpcmF0aW9uTW9udGgiOiIxMCIsInBhbkV4cGlyYXRpb25ZZWFyIjoiMjAzMiIsImRpZ2l0YWxDYXJkRGF0YSI6eyJzdGF0dXMiOiJBQ1RJVkUiLCJkZXNjcmlwdG9yTmFtZSI6Ik9CTiIsImFydFVyaSI6Imh0dHBzOi8vc2FuZGJveC5hc3NldHMudmltcy52aXNhLmNvbS92aW1zL2NhcmRhcnQvNWFmMzczNGNjYTRlNDM5ZTk3ZjAwZTQ0NzRkODI0NDdfaW1hZ2VBQDJ4LnBuZyIsImFydEhlaWdodCI6MjEwLCJhcnRXaWR0aCI6MzM0fSwiZGF0ZW9mQ2FyZENyZWF0ZWQiOjE3NzI1MzcwNDI2ODMsImRhdGVvZkNhcmRMYXN0VXNlZCI6MTc3NTExMjQ0MDc4NywibWFza2VkQmlsbGluZ0FkZHJlc3MiOnsiYWRkcmVzc0lkIjoiZmI0NGQ1ZTUtNGJmMS00NWJhLWJhZDgtNWNkOTk2MzRmMDhmIiwiY291bnRyeUNvZGUiOiJHQiJ9LCJlbGlnaWJsZSI6ZmFsc2UsInBheW1lbnRDYXJkVHlwZSI6IkRFQklUIiwidG9rZW5JZCI6ImNjOTE4YTIxMDAxYmMwMTgxNWM2MTQ5MjQxMWJlMzAyIn0sIm1hc2tlZENvbnN1bWVyIjp7InNyY0NvbnN1bWVySWQiOiJDRkpHcXdxNDZEaDZZaEVRdXA2YzM4d2ZQVENGSmlXYXhHS3NUNmpTZWJNPSIsImZpcnN0TmFtZSI6IlQqKioqKiIsImxhc3ROYW1lIjoiVioqKioqIiwiZnVsbE5hbWUiOiJUKioqKiogVioqKioqIiwiZW1haWxBZGRyZXNzIjoidmxhKipAZ2xvYmFscGF5LmNvbSIsIm1vYmlsZU51bWJlciI6eyJjb3VudHJ5Q29kZSI6IjQwIiwicGhvbmVOdW1iZXIiOiIqKioqKioqKjU2NTQifSwiY291bnRyeUNvZGUiOiJHQiIsImxhbmd1YWdlQ29kZSI6ImVuLUdCIiwic3RhdHVzIjoiQUNUSVZFIn0sImFzc3VyYW5jZURhdGEiOnsiZWNpIjoiMDcifSwiaXNHdWVzdENoZWNrb3V0IjpmYWxzZSwiaXNOZXdVc2VyIjpmYWxzZX0.PLKjFxIbA1mR0rEqELHmsNMhOv7P-ocTS4BskuIdwpL6q3lSpfeBymQ3U1p6oUdSbk1q0qoaThX-s845P9cDugl8K0r79Ng3huMUGfgXL25opdWKRUrIciS0y13hgUjyBku44_pvZuoAQ1ua0F1y6maKBia6_T0bFTKKQVLUBuZzIe_viL3i2m388M95chAVrSCum5XBFG46XysAox1L7FNm2I_UvE0QEmWvewVzwjd4BHfCVhGzCfr2mLURHHJYKvAEyT7WLYkCq4VpvKKkm4O-DouKE358OJtBiXYma7jlGc2IyWV0-gf2VV6m07h6o4TGYcORP9OMIg6GrEOaew";

    private static final String DPA_REFERENCE = "08f56394-4599-af88-ff38-1a64db7c6502";

    public GpApiClickToPayDecryptTest() throws ConfigurationException {
        GpApiConfig config = new GpApiConfig()
                .setAppId(EU_HPP_APP_ID)
                .setAppKey(EU_HPP_APP_KEY);
        config.setChannel(Channel.CardNotPresent);
        config.setServiceUrl("https://apis.sandbox.eu.globalpay.com/ucp");
        config.setEnableLogging(true);
        config.setRequestLogger(new RequestConsoleLogger());

        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setTransactionProcessingAccountName(ACCOUNT_NAME);
        config.setAccessTokenInfo(accessTokenInfo);

        ServicesContainer.configureService(config);
    }

    /**
     * Given I want to decrypt a Click to Pay payment token,
     * I can send this token to the /decrypt endpoint via the SDK,
     * and receive back a DEC_ID and PMT_ID.
     */
    @Test
    public void decryptClickToPayToken_ReturnsDecIdAndPmtId() throws ApiException {
        CreditCardData card = new CreditCardData();
        card.setToken(CTP_VISA_TOKEN);
        card.setMobileType(MobilePaymentMethodType.CLICK_TO_PAY);
        card.setDpaReference(DPA_REFERENCE);
        card.setCardType("visa");
        card.setCardHolderName("James Mason");

        Transaction response = card
                .decrypt("USD")
                .withCountry("US")
                .execute();

        assertNotNull(response);
        assertFalse(StringUtils.isNullOrEmpty(response.getTransactionId()),
                "Expected DEC_ID in transaction ID");
        assertFalse(StringUtils.isNullOrEmpty(response.getToken()),
                "Expected PMT_ID in token");
    }


    /**
     * End-to-end Click to Pay decrypt then authorize flow.
     * Step 1 – Decrypt the CTP token to get DEC_ID and PMT_ID.
     * Step 2 – Charge using the PMT_ID + DEC_ID.
     */
    @Test
    public void decryptThenChargeClickToPay_Success() throws ApiException {
        CreditCardData card = new CreditCardData();
        card.setToken(CTP_VISA_TOKEN);
        card.setMobileType(MobilePaymentMethodType.CLICK_TO_PAY);
        card.setDpaReference(DPA_REFERENCE);
        card.setCardHolderName("James Mason");
        card.setCardType("visa");

        // Step 1 – Decrypt
        Transaction decryptResponse = card
                .decrypt("EUR")
                .withCountry("GB")
                .execute();

        assertNotNull(decryptResponse);
        String decId = decryptResponse.getTransactionId();
        String pmtId = decryptResponse.getToken();
        assertFalse(StringUtils.isNullOrEmpty(decId), "DEC_ID must not be empty");
        assertFalse(StringUtils.isNullOrEmpty(pmtId), "PMT_ID must not be empty");

        // Step 2 – Charge
        CreditCardData tokenCard = new CreditCardData();
        tokenCard.setToken(pmtId);
        tokenCard.setMobileType(MobilePaymentMethodType.CLICK_TO_PAY);
        tokenCard.setDecryptId(decId);
        tokenCard.setDpaReference(DPA_REFERENCE);
        tokenCard.setCardHolderName("James Mason");

        ThreeDSecure threeDSecure = new ThreeDSecure();
        threeDSecure.setExemptStatus(ExemptStatus.LowValue);
        tokenCard.setThreeDSecure(threeDSecure);

        Transaction chargeResponse = tokenCard
                .charge(new BigDecimal("100"))
                .withCurrency("EUR")
                .execute();

        assertNotNull(chargeResponse);
        assertEquals(SUCCESS, chargeResponse.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), chargeResponse.getResponseMessage());
        assertFalse(StringUtils.isNullOrEmpty(chargeResponse.getTransactionId()));
    }

    /**
     * Given I want to create a single-use payment method without CVV (SINGLE_NO_CVN),
     * I can tokenize the card and receive a token back.
     */
    @Test
    public void tokenizeCardWithoutCvv_SingleUseNoCvn_ReturnsToken() throws ApiException {
        GpApiConfig config = new GpApiConfig()
                .setAppId(EU_APP_ID)
                .setAppKey(EU_APP_KEY);
        config.setChannel(Channel.CardNotPresent);
        config.setServiceUrl("https://apis.sandbox.eu.globalpay.com/ucp");
        config.setEnableLogging(true);
        config.setRequestLogger(new RequestConsoleLogger());

        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setTransactionProcessingAccountName(ACCOUNT_NAME);
        config.setAccessTokenInfo(accessTokenInfo);
        config.setPermissions(new String[]{"PMT_POST_Create_Single",
                "BIN_GET_Details",
                "INS_POST_Query",
                "TRN_POST_Initiate",
                "CCS_POST_DCC",
                "AUT_POST_Initiate",
                "AUT_POST_Check_Availability",
                "AUT_POST_Results"});

        ServicesContainer.configureService(config);

        CreditCardData card = new CreditCardData();
        card.setNumber("4263970000005262");
        card.setExpMonth(5);
        card.setExpYear(2030);
        card.setCvvPresent(CvvPresent.NO);

        Transaction response = card
                .tokenize(false, PaymentMethodUsageMode.SINGLE)
                .execute();

        assertNotNull(response);
        assertFalse(StringUtils.isNullOrEmpty(response.getToken()),
                "Expected a single-use token in the response");
    }

    @Test
    public void decryptClickToPayToken_WithWrongMobileType_ShouldFail() {
        CreditCardData card = new CreditCardData();
        card.setToken(CTP_VISA_TOKEN); // structurally valid CTP token
        card.setMobileType(MobilePaymentMethodType.APPLEPAY); // intentionally wrong for CTP token
        card.setDpaReference(DPA_REFERENCE);
        card.setCardType("visa");
        card.setCardHolderName("James Mason");

        GatewayException ex = assertThrows(GatewayException.class, () ->
                card.decrypt("USD")
                        .withCountry("US")
                        .execute()
        );

        assertNotNull(ex.getMessage());
        assertEquals("Status Code: 400 - provider contains unexpected data", ex.getMessage());
    }

    @Test
    public void decryptClickToPayToken_WithMismatchedDpaReference_ShouldFail() {
        CreditCardData card = new CreditCardData();
        card.setToken(CTP_VISA_TOKEN); // valid token format
        card.setMobileType(MobilePaymentMethodType.CLICK_TO_PAY);
        card.setDpaReference("00000000-0000-0000-0000-000000000000"); // valid UUID format, wrong value
        card.setCardType("visa");
        card.setCardHolderName("James Mason");

        GatewayException ex = assertThrows(GatewayException.class, () ->
                card.decrypt("USD")
                        .withCountry("US")
                        .execute()
        );

        assertNotNull(ex.getMessage());
        assertEquals("Status Code: 400 - Invalid token provided.", ex.getMessage());
    }

}

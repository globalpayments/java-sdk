package com.global.api.tests.ci.gpapi;

import com.global.api.entities.*;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.TransactionStatus;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.tests.utils.citesting.CiTestingHarness;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class GpApiTransactionsTests {

    private static final String APP_ID = "4gPqnGBkppGYvoE5UX9EWQlotTxGUDbs";
    private static final String APP_KEY = "FQyJA5VuEQfcji2M";
    private static final String SUCCESS = "SUCCESS";
    protected static final CiTestingHarness ciTestingHarness = new CiTestingHarness(
            "https://apis.sandbox.globalpay.com/ucp",
            CiTestingHarness.CacheMode.Locked,
            "GpApiTransactionsTests"
    );

    private static final int expMonth = ciTestingHarness.getCurrentTime().getMonthOfYear();
    private static final int expYear = ciTestingHarness.getCurrentTime().getYear() + 1;

    private final CreditCardData card;
    private final BigDecimal amount = new BigDecimal("2.02");
    private final String currency = "USD";

    public GpApiTransactionsTests() throws ApiException {
        card = new CreditCardData();
        card.setNumber("4263970000005262");
        card.setExpMonth(expMonth);
        card.setExpYear(expYear);
        card.setCvn("123");
        card.setCardPresent(true);
    }

    private void configureGpApiService() throws ApiException {
        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        config.setServiceUrl(ciTestingHarness.getTestingUrl());
        ciTestingHarness.attach(config);
    }

    private static GpApiConfig gpApiSetup(String appId, String appKey, Channel channel) {
        GpApiConfig gpApiConfig = new GpApiConfig()
                .setAppId(appId)
                .setAppKey(appKey);

        gpApiConfig.setChannel(channel);

        gpApiConfig.setChallengeNotificationUrl("https://ensi808o85za.x.pipedream.net/");
        gpApiConfig.setMethodNotificationUrl("https://ensi808o85za.x.pipedream.net/");
        gpApiConfig.setMerchantContactUrl("https://enp4qhvjseljg.x.pipedream.net/");

        gpApiConfig.setEnableLogging(true);
        gpApiConfig.setRequestLogger(new RequestConsoleLogger());

        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setTransactionProcessingAccountName("transaction_processing");
        accessTokenInfo.setRiskAssessmentAccountName("EOS_RiskAssessment");
        gpApiConfig.setAccessTokenInfo(accessTokenInfo);

        return gpApiConfig;
    }

    private void assertTransactionResponse(Transaction transaction, TransactionStatus transactionStatus) {
        assertNotNull(transaction);
        assertEquals("00", transaction.getResponseCode());
        assertEquals(transactionStatus.getValue(), transaction.getResponseMessage());
    }

    @Test
    public void postCapture() throws ApiException {
        ciTestingHarness.setFunction("GP-API|Transactions|POST Capture");
        configureGpApiService();

        Transaction transaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("postCapture_auth"))
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Preauthorized);

        Transaction capture =
                transaction
                        .capture(amount)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("postCapture_capture"))
                        .execute();
        assertTransactionResponse(capture, TransactionStatus.Captured);
    }


    @Test
    public void postCharge() throws ApiException {
        ciTestingHarness.setFunction("GP-API|Transactions|POST Create");
        configureGpApiService();

        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("postCreate"))
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Captured);
    }



}

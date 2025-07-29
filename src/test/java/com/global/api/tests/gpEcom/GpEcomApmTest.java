package com.global.api.tests.gpEcom;

import com.global.api.ServicesContainer;
import com.global.api.entities.BankList;
import com.global.api.entities.Transaction;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.exceptions.*;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.paymentMethods.AlternativePaymentMethod;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.serviceConfigs.GpEcomConfig;
import com.global.api.services.ReportingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.global.api.entities.enums.AlternativePaymentType.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class GpEcomApmTest extends BaseGpEComTest {

    static final BigDecimal amount = new BigDecimal(0.01);
    static final String currency = "EUR";
    static final String returnUrl = "https://www.example.com/returnUrl";
    static final String statusUpdateUrl = "https://www.example.com/statusUrl";
    static final String descriptor = "Test Transaction";
    static final String accountName = "James Mason";
    static final String chargeDescription = "New APM";

    @BeforeEach
    public void Init() throws ConfigurationException {
        GpEcomConfig config = gpEComSetup();
        ServicesContainer.configureService(config);
    }

    @Test
    public void testApmForCharge() throws ApiException {
        AlternativePaymentMethod paymentMethodDetails =
                new AlternativePaymentMethod()
                        .setAlternativePaymentMethodType(SOFORTUBERWEISUNG)
                        .setReturnUrl(returnUrl)
                        .setStatusUpdateUrl(statusUpdateUrl)
                        .setDescriptor(descriptor)
                        .setCountry("DE")
                        .setAccountHolderName(accountName);

        Transaction response =
                paymentMethodDetails
                        .charge(amount)
                        .withCurrency(currency)
                        .withDescription(chargeDescription)
                        .execute();

        assertNotNull(response);
        assertEquals("01", response.getResponseCode());
        assertNotNull(response.getAlternativePaymentResponse());
        assertNotNull(response.getAlternativePaymentResponse().getRedirectUrl());
        assertEquals("sofort", response.getAlternativePaymentResponse().getPaymentMethod());
        assertThat(response.getAlternativePaymentResponse().getPaymentPurpose(), containsString(descriptor));
        assertEquals(response.getAlternativePaymentResponse().getCountry(), paymentMethodDetails.getCountry());
        assertEquals(response.getAlternativePaymentResponse().getAccountHolderName(), paymentMethodDetails.getAccountHolderName());
    }

    @Test
    public void testApmWithoutAmount() throws ApiException {
        try {
            new AlternativePaymentMethod()
                    .charge()
                    .withCurrency(currency)
                    .execute();
        } catch (BuilderException ex) {
            assertEquals("amount cannot be null for this transaction type.", ex.getMessage());
        }
    }

    @Test
    public void testApmWithoutCurrency() throws ApiException {
        try {
            new AlternativePaymentMethod()
                    .charge(amount)
                    .execute();
        } catch (BuilderException ex) {
            assertEquals("currency cannot be null for this transaction type.", ex.getMessage());
        }
    }

    @Test
    public void testApmWithoutReturnUrl() throws ApiException {
        boolean errorFound = false;
        try {
            new AlternativePaymentMethod()
                    .setAlternativePaymentMethodType(SOFORTUBERWEISUNG)
                    .setStatusUpdateUrl(statusUpdateUrl)
                    .setDescriptor(descriptor)
                    .setCountry("DE")
                    .setAccountHolderName(accountName)
                    .charge(amount)
                    .withCurrency(currency)
                    .withDescription(chargeDescription)
                    .execute();
        } catch (BuilderException ex) {
            errorFound = true;
            assertEquals("returnUrl cannot be null for this transaction type.", ex.getMessage());
        } finally {
            assertTrue(errorFound);
        }
    }

    @Test
    public void testApmWithoutStatusUpdateUrl() throws ApiException {
        boolean errorFound = false;
        try {
            new AlternativePaymentMethod()
                    .setAlternativePaymentMethodType(SOFORTUBERWEISUNG)
                    .setReturnUrl(returnUrl)
                    .setDescriptor(descriptor)
                    .setCountry("DE")
                    .setAccountHolderName(accountName)
                    .charge(amount)
                    .withCurrency(currency)
                    .withDescription(chargeDescription)
                    .execute();
        } catch (BuilderException ex) {
            errorFound = true;
            assertEquals("statusUpdateUrl cannot be null for this transaction type.", ex.getMessage());
        } finally {
            assertTrue(errorFound);
        }
    }

    @Test
    public void testAPMRefundPendingTransaction() throws ApiException {
        AlternativePaymentMethod paymentMethod =
                new AlternativePaymentMethod()
                        .setAlternativePaymentMethodType(TESTPAY)
                        .setReturnUrl(returnUrl)
                        .setStatusUpdateUrl(statusUpdateUrl)
                        .setDescriptor(descriptor)
                        .setCountry("DE")
                        .setAccountHolderName(accountName);

        Transaction response =
                paymentMethod
                        .charge(amount)
                        .withCurrency(currency)
                        .withDescription(chargeDescription)
                        .execute();

        assertNotNull(response);
        assertEquals("01", response.getResponseCode());
        assertEquals("PENDING", response.getResponseMessage());
        assertNotNull(response.getTransactionReference().getOrderId());

        try {
            // send the settle request, we must specify the amount and currency
            response
                    .refund(amount)
                    .withCurrency(currency)
                    .withAlternativePaymentType(TESTPAY)
                    .execute();
        } catch (GatewayException ex) {
            assertEquals("FAILED", ex.getResponseText());
            assertEquals("508", ex.getResponseCode());
            assertEquals("Unexpected Gateway Response: 508 - FAILED", ex.getMessage());
        }
    }

    @Test
    public void testAPMPayByBankApp() throws ApiException {
        AlternativePaymentMethod paymentMethod =
                new AlternativePaymentMethod()
                        .setAlternativePaymentMethodType(PAYBYBANKAPP)
                        .setReturnUrl(returnUrl)
                        .setStatusUpdateUrl(statusUpdateUrl)
                        .setDescriptor(descriptor)
                        .setCountry("GB")
                        .setAccountHolderName(accountName);

        Transaction response =
                paymentMethod
                        .charge(amount)
                        .withCurrency("GBP")
                        .withDescription(chargeDescription)
                        .execute();

        assertNotNull(response);
        assertEquals("01", response.getResponseCode());
        assertNotNull(response.getAlternativePaymentResponse());
        assertNotNull(response.getAlternativePaymentResponse().getRedirectUrl());
        assertEquals("paybybankapp", response.getAlternativePaymentResponse().getPaymentMethod());
        assertThat(response.getAlternativePaymentResponse().getPaymentPurpose(), nullValue());
        assertEquals(response.getAlternativePaymentResponse().getCountry(), paymentMethod.getCountry());
        assertEquals(response.getAlternativePaymentResponse().getAccountHolderName(), paymentMethod.getAccountHolderName());
    }

    @Test
    public void testAPMPaypal() throws ApiException, InterruptedException {
        AlternativePaymentMethod paymentMethod =
                new AlternativePaymentMethod()
                        .setAlternativePaymentMethodType(PAYPAL)
                        .setReturnUrl("https://7b8e82a17ac00346e91e984f42a2a5fb.m.pipedream.net")
                        .setStatusUpdateUrl("https://7b8e82a17ac00346e91e984f42a2a5fb.m.pipedream.net")
                        .setCancelUrl("https://7b8e82a17ac00346e91e984f42a2a5fb.m.pipedream.net")
                        .setDescriptor(descriptor)
                        .setCountry("US")
                        .setAccountHolderName(accountName);

        Transaction transaction =
                paymentMethod
                        .charge(amount)
                        .withCurrency(currency)
                        .withDescription(chargeDescription)
                        .execute();

        assertNotNull(transaction);
        assertEquals("00", transaction.getResponseCode());
        assertNotNull(transaction.getAlternativePaymentResponse().getSessionToken());

        System.out.println("Open link in browser and confirm PAYPAL payment:");
        if (transaction.getAlternativePaymentResponse().getSessionToken() != null) {
            System.out.println("https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token=" + transaction.getAlternativePaymentResponse().getSessionToken());
        }

        Thread.sleep(30000);

        transaction.getAlternativePaymentResponse().setProviderReference("SMKGK7K2BLEUA");

        try {
            Transaction response =
                    transaction
                            .confirm(amount)
                            .withCurrency(currency)
                            .withAlternativePaymentType(PAYPAL)
                            .execute();

            assertNotNull(response);
            assertEquals("00", transaction.getResponseCode());
        } catch (GatewayException e) {
            assertEquals("Unexpected Gateway Response: 101 - Payment has not been authorized by the user.", e.getMessage());
        }
    }

    @Disabled // Getting Original transaction not found. Need to find a proper orderId without PENDING status
    @Test
    public void testApmForRefund() throws ApiException {
        // a settle request requires the original order id
        String orderId = "20180912050207-5b989dcfc9433";
        // and the payments reference (pasref) from the authorization response
        String paymentsReference = "15367285279651634";
        // and the auth code transaction response
        String authCode = "12345";

        // create the rebate transaction object
        Transaction transaction = Transaction.fromId(paymentsReference, orderId);

        // send the settle request, we must specify the amount and currency
        Transaction response =
                transaction
                        .refund(amount)
                        .withCurrency(currency)
                        .withAlternativePaymentType(TESTPAY)
                        .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // Sale for Blik APM
    @Test
    public void testBlikApmForSale() throws ApiException {
        GpApiBlikInitializationTest();
        AlternativePaymentMethod paymentMethodDetails =
                new AlternativePaymentMethod()
                        .setAlternativePaymentMethodType(BLIK)
                        .setReturnUrl(returnUrl)
                        .setStatusUpdateUrl(statusUpdateUrl)
                        .setDescriptor(descriptor)
                        .setCountry("PL")
                        .setAccountHolderName(accountName);

        Transaction response =
                paymentMethodDetails
                        .charge(amount)
                        .withCurrency("PLN")
                        .withDescription(chargeDescription)
                        .execute();

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertNotNull(response.getAlternativePaymentResponse());
        assertNotNull(response.getAlternativePaymentResponse().getRedirectUrl());
        assertEquals("BLIK",response.getAlternativePaymentResponse().getProviderName().toUpperCase());

    }

    // Sale for Blik APM without return url
    @Test
    public void testBlikApmForSaleWithoutReturnUrl() throws ApiException {
        GpApiBlikInitializationTest();
        boolean errorFound = false;
        try {
            new AlternativePaymentMethod()
                    .setAlternativePaymentMethodType(BLIK)
                    .setStatusUpdateUrl(statusUpdateUrl)
                    .setDescriptor(descriptor)
                    .setCountry("PL")
                    .setAccountHolderName(accountName)
                    .charge(amount)
                    .withCurrency("PLN")
                    .withDescription(chargeDescription)
                    .execute();
        } catch (BuilderException ex) {
            errorFound = true;
            assertEquals("returnUrl cannot be null for this transaction type.", ex.getMessage());
        } finally {
            assertTrue(errorFound);
        }

    }

    // Sale for Blik APM without status url
    @Test
    public void testBlikApmForSaleWithoutStatusUrl() throws ApiException {
        GpApiBlikInitializationTest();
        boolean errorFound = false;
        try {
            new AlternativePaymentMethod()
                    .setAlternativePaymentMethodType(BLIK)
                    .setReturnUrl(returnUrl)
                    .setDescriptor(descriptor)
                    .setCountry("PL")
                    .setAccountHolderName(accountName)
                    .charge(amount)
                    .withCurrency("PLN")
                    .withDescription(chargeDescription)
                    .execute();
        } catch (BuilderException ex) {
            errorFound = true;
            assertEquals("statusUpdateUrl cannot be null for this transaction type.", ex.getMessage());
        } finally {
            assertTrue(errorFound);
        }
    }


    // Refund for Blik APM first time
    @Test
    public void testBlikApmForRefund() throws ApiException {
        GpApiBlikInitializationTest();
        // For refund we have to run sale test and get Transaction ID from that response and paste here in transactionId.
        // Also go to redirect_url from response of sale and approve by entering the code.
        // After some time when status changed to "Captured" run the refund test.
        String transactionId = "TRN_L673EQlmzJ63SbmS7JDbiCoWVrDnPW_4a5a231010fa";

        // create the rebate transaction object
        Transaction transaction = Transaction.fromId(transactionId);

        TransactionSummary transactionDetails =
                ReportingService
                        .transactionDetail(transactionId)
                        .execute();
        transaction.setAlternativePaymentResponse(transactionDetails.getAlternativePaymentResponse());

        Transaction response =
                transaction
                        .refund(amount)
                        .withCurrency("PLN")
                        .withAlternativePaymentType(BLIK)
                        .execute();

        assertNotNull(response);
        assertEquals("BLIK",response.getTransactionReference().getAlternativePaymentResponse().getProviderName().toUpperCase());
        assertEquals("SUCCESS", response.getResponseCode());
    }

    // Run refund on same transactionId it will give response as "Declined"
    @Test
    public void testBlikApmForRefundSecondTime() throws ApiException {
        GpApiBlikInitializationTest();
        // Run Refund with same transaction Id given in first time blik apm refund
        String transactionId = "TRN_L673EQlmzJ63SbmS7JDbiCoWVrDnPW_4a5a231010fa";

        // create the rebate transaction object
        Transaction transaction = Transaction.fromId(transactionId);

        TransactionSummary transactionDetails =
                ReportingService
                        .transactionDetail(transactionId)
                        .execute();
        transaction.setAlternativePaymentResponse(transactionDetails.getAlternativePaymentResponse());

        Transaction response =
                transaction
                        .refund(amount)
                        .withCurrency("PLN")
                        .withAlternativePaymentType(BLIK)
                        .execute();

        assertNotNull(response);
        assertEquals("BLIK",response.getTransactionReference().getAlternativePaymentResponse().getProviderName().toUpperCase());
        assertEquals("DECLINED", response.getResponseCode());
    }

    public void GpApiBlikInitializationTest() throws ApiException {
        String APP_ID = "p2GgW0PntEUiUh4qXhJHPoDqj3G5GFGI";
        String APP_KEY = "lJk4Np5LoUEilFhH";
        GpApiConfig  gpApiConfig = new GpApiConfig()
                .setAppId(APP_ID)
                .setAppKey(APP_KEY);
        gpApiConfig.setChannel(Channel.CardNotPresent);
        gpApiConfig.setServiceUrl("https://apis-sit.globalpay.com/ucp");
        gpApiConfig.setEnableLogging(true);
        gpApiConfig.setRequestLogger(new RequestConsoleLogger());
        gpApiConfig.setCountry("PL");

        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setTransactionProcessingAccountName("GPECOM_BLIK_APM_Transaction_Processing");
        accessTokenInfo.setRiskAssessmentAccountName("EOS_RiskAssessment");
        gpApiConfig.setAccessTokenInfo(accessTokenInfo);
        ServicesContainer.configureService(gpApiConfig);

    }

    @Test
    public void testPayuApmForSale() throws ApiException {
        GpApiPayuInitializationTest();

        AlternativePaymentMethod paymentMethodDetails =
                new AlternativePaymentMethod()
                        .setAlternativePaymentMethodType(OB)
                        .setReturnUrl(returnUrl)
                        .setStatusUpdateUrl(statusUpdateUrl)
                        .setDescriptor(descriptor)
                        .setCountry("PL")
                        .setAccountHolderName(accountName)
                        .setBank(BankList.MBANK);

        Transaction response =
                paymentMethodDetails
                        .charge(amount)
                        .withCurrency("PLN")
                        .withDescription(chargeDescription)
                        .execute();

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertNotNull(response.getAlternativePaymentResponse());
        assertNotNull(response.getAlternativePaymentResponse().getRedirectUrl());
        assertEquals("BANK_PAYMENT",response.getAlternativePaymentResponse().getProviderName().toUpperCase());
    }

    @Test
    public void testPayuApmForSaleWithoutReturnUrl() throws ApiException {
        GpApiPayuInitializationTest();

        boolean errorFound = false;
        try {
            AlternativePaymentMethod paymentMethodDetails =
                    new AlternativePaymentMethod()
                            .setAlternativePaymentMethodType(OB)
                            .setStatusUpdateUrl(statusUpdateUrl)
                            .setDescriptor(descriptor)
                            .setCountry("PL")
                            .setAccountHolderName(accountName)
                            .setBank(BankList.MBANK);

            Transaction response =
                    paymentMethodDetails
                            .charge(amount)
                            .withCurrency("PLN")
                            .withDescription(chargeDescription)
                            .execute();
        } catch (BuilderException ex) {
            errorFound = true;
            assertEquals("returnUrl cannot be null for this transaction type.", ex.getMessage());
        } finally {
            assertTrue(errorFound);
        }

    }
    @Test
    public void testPayuApmForSaleWithoutStatusUrl() throws ApiException {
        GpApiPayuInitializationTest();
        boolean errorFound = false;
        try {
            AlternativePaymentMethod paymentMethodDetails =
                    new AlternativePaymentMethod()
                            .setAlternativePaymentMethodType(OB)
                            .setReturnUrl(returnUrl)
                            .setDescriptor(descriptor)
                            .setCountry("PL")
                            .setAccountHolderName(accountName)
                            .setBank(BankList.MBANK);

            Transaction response =
                    paymentMethodDetails
                            .charge(amount)
                            .withCurrency("PLN")
                            .withDescription(chargeDescription)
                            .execute();
        } catch (BuilderException ex) {
            errorFound = true;
            assertEquals("statusUpdateUrl cannot be null for this transaction type.", ex.getMessage());
        } finally {
            assertTrue(errorFound);
        }

    }

    public void GpApiPayuInitializationTest() throws ApiException {
        String APP_ID = "ZbFY1jAz6sqq0GAyIPZe1raLCC7cUlpD";
        String APP_KEY = "4NpIQJDCIDzfTKhA";

        GpApiConfig  gpApiConfig = new GpApiConfig()
                .setAppId(APP_ID)
                .setAppKey(APP_KEY);
        gpApiConfig.setChannel(Channel.CardNotPresent);

        gpApiConfig.setServiceUrl("https://apis.globalpay.com/ucp");

        gpApiConfig.setEnableLogging(true);
        gpApiConfig.setRequestLogger(new RequestConsoleLogger());
        gpApiConfig.setCountry("PL");

        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();

        accessTokenInfo.setTransactionProcessingAccountName("transaction_processing");
        accessTokenInfo.setRiskAssessmentAccountName("EOS_RiskAssessment");
        gpApiConfig.setAccessTokenInfo(accessTokenInfo);
        ServicesContainer.configureService(gpApiConfig);

    }
}

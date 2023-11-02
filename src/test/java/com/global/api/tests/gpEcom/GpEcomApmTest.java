package com.global.api.tests.gpEcom;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.paymentMethods.AlternativePaymentMethod;
import com.global.api.serviceConfigs.GpEcomConfig;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;

import static com.global.api.entities.enums.AlternativePaymentType.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GpEcomApmTest extends BaseGpEComTest {

    static final BigDecimal amount = new BigDecimal(10);
    static final String currency = "EUR";
    static final String returnUrl = "https://www.example.com/returnUrl";
    static final String statusUpdateUrl = "https://www.example.com/statusUrl";
    static final String descriptor = "Test Transaction";
    static final String accountName = "James Mason";
    static final String chargeDescription = "New APM";

    @Before
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
        } catch (GatewayException ex) {
            assertThat(ex.getMessage(), containsString("does not conform to the schema"));
        }
    }

    @Test
    public void testApmWithoutStatusUpdateUrl() throws ApiException {
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
        } catch (GatewayException ex) {
            assertThat(ex.getMessage(), containsString("does not conform to the schema"));
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

    @Ignore // Getting Original transaction not found. Need to find a proper orderId without PENDING status
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

}
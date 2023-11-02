package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.entities.reporting.TransactionSummaryPaged;
import com.global.api.paymentMethods.AlternativePaymentMethod;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.ReportingService;
import com.global.api.utils.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * ¡¡¡                  READ BEFORE RUNNING THIS TESTS SUITE                !!!
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * <p>
 * How to have a success running test. When you will run the test in the console it will be printed the
 * PayPal redirect url. You need to copy the link and open it in a browser, do the login wih your PayPal
 * credentials and authorize the payment in the PayPal form. You will be redirected to a blank page with a
 * printed message like this: { "success": true }. This has to be done within a 25 seconds timeframe.
 * In case you need more time update the sleep() to what you need.
 */
public class GpApiApmTest extends BaseGpApiTest {

    private AlternativePaymentMethod paymentMethod;
    private String currency;
    private Address shippingAddress = null;
    private Date startDate;

    @Before
    public void initialize() throws ConfigurationException {

        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        ServicesContainer.configureService(config);

        paymentMethod =
                new AlternativePaymentMethod()
                        .setAlternativePaymentMethodType(AlternativePaymentType.PAYPAL)
                        .setReturnUrl("https://7b8e82a17ac00346e91e984f42a2a5fb.m.pipedream.net")
                        .setStatusUpdateUrl("https://7b8e82a17ac00346e91e984f42a2a5fb.m.pipedream.net")
                        .setCancelUrl("https://7b8e82a17ac00346e91e984f42a2a5fb.m.pipedream.net")
                        .setDescriptor("Test Transaction")
                        .setCountry("GB")
                        .setAccountHolderName("James Mason");

        currency = "USD";
        startDate = new Date();

        shippingAddress =
                new Address()
                        .setStreetAddress1("Apartment 852")
                        .setStreetAddress2("Complex 741")
                        .setStreetAddress3("no")
                        .setCity("Chicago")
                        .setPostalCode("5001")
                        .setProvince("IL")
                        .setCountryCode("US");
    }

    @Test
    public void PayPalCharge_fullCycle() throws ApiException, InterruptedException {
        Transaction response = paymentMethod
                .charge(1.34)
                .withCurrency(currency)
                .withDescription("New APM")
                .execute();

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals("INITIATED", response.getResponseMessage());

        System.out.println("copy the link and open it in a browser, do the login wih your paypal credentials and authorize the payment in the paypal form. You will be redirected to a blank page with a printed message like this: { \"success\": true }. This has to be done within a 25 seconds timeframe.");
        if (response.getAlternativePaymentResponse() != null) {
            System.out.println(response.getAlternativePaymentResponse().getRedirectUrl());
        }

        Thread.sleep(25000);

        TransactionSummaryPaged responseFind =
                ReportingService
                        .findTransactionsPaged(1, 1)
                        .withTransactionId(response.getTransactionId())
                        .where(SearchCriteria.StartDate, startDate)
                        .execute();

        assertNotNull(responseFind);
        assertTrue(responseFind.getTotalRecordCount() > 0);

        TransactionSummary transactionSummary = responseFind.getResults().get(0);
        assertNotNull(transactionSummary.getAlternativePaymentResponse());
        assertEquals(AlternativePaymentType.PAYPAL.toString().toLowerCase(), transactionSummary.getAlternativePaymentResponse().getProviderName());

        if (transactionSummary.getTransactionStatus().equals("PENDING")) {
            assertNotNull(transactionSummary.getAlternativePaymentResponse().getProviderReference());

            Transaction transaction = Transaction.fromId(transactionSummary.getTransactionId(), null, PaymentMethodType.APM);
            transaction.setAlternativePaymentResponse(transactionSummary.getAlternativePaymentResponse());

            response =
                    transaction
                            .confirm()
                            .execute();

            assertNotNull(response);
            assertEquals("SUCCESS", response.getResponseCode());
            assertEquals("CAPTURED", response.getResponseMessage());
        } else assertEquals("INITIATED", transactionSummary.getTransactionStatus());
    }

    @Test
    public void PayPalCapture_fullCycle() throws ApiException, InterruptedException {
        Transaction response =
                paymentMethod
                        .authorize(1.34)
                        .withCurrency(currency)
                        .withDescription("New APM")
                        .execute();

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals("INITIATED", response.getResponseMessage());

        System.out.println("copy the link and open it in a browser, do the login wih your paypal credentials and authorize the payment in the paypal form. You will be redirected to a blank page with a printed message like this: { \"success\": true }. This has to be done within a 25 seconds timeframe.");
        if (response.getAlternativePaymentResponse() != null) {
            System.out.println(response.getAlternativePaymentResponse().getRedirectUrl());
        }

        Thread.sleep(25000);

        TransactionSummaryPaged responseFind =
                ReportingService
                        .findTransactionsPaged(1, 1)
                        .withTransactionId(response.getTransactionId())
                        .where(SearchCriteria.StartDate, startDate)
                        .execute();

        assertNotNull(responseFind);
        assertTrue(responseFind.getTotalRecordCount() > 0);

        TransactionSummary transactionSummary = responseFind.getResults().get(0);
        assertFalse(StringUtils.isNullOrEmpty(transactionSummary.getTransactionId()));
        assertNotNull(transactionSummary.getAlternativePaymentResponse());
        assertEquals(AlternativePaymentType.PAYPAL.toString().toLowerCase(), transactionSummary.getAlternativePaymentResponse().getProviderName());

        if (transactionSummary.getTransactionStatus().equals("PENDING")) {
            assertNotNull(transactionSummary.getAlternativePaymentResponse().getProviderReference());

            Transaction transaction = Transaction.fromId(transactionSummary.getTransactionId(), null, PaymentMethodType.APM);
            transaction.setAlternativePaymentResponse(transactionSummary.getAlternativePaymentResponse());

            response =
                    transaction
                            .confirm()
                            .execute();

            assertNotNull(response);
            assertEquals("SUCCESS", response.getResponseCode());
            assertEquals("PREAUTHORIZED", response.getResponseMessage());

            Transaction capture =
                    transaction
                            .capture()
                            .execute();

            assertNotNull(capture);
            assertEquals("SUCCESS", capture.getResponseCode());
            assertEquals("CAPTURED", capture.getResponseMessage());
        } else assertEquals("INITIATED", transactionSummary.getTransactionStatus());
    }

    @Test
    public void PayPalFullCycle_Refund() throws ApiException, InterruptedException {
        Transaction trn =
                paymentMethod
                        .charge(1.22)
                        .withCurrency(currency)
                        .withDescription("New APM")
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals("INITIATED", trn.getResponseMessage());

        System.out.println("copy the link and open it in a browser, do the login wih your paypal credentials and authorize the payment in the paypal form. You will be redirected to a blank page with a printed message like this: { \"success\": true }. This has to be done within a 25 seconds timeframe.");
        if (trn.getAlternativePaymentResponse() != null) {
            System.out.println(trn.getAlternativePaymentResponse().getRedirectUrl());
        }

        Thread.sleep(25000);

        TransactionSummaryPaged responseFind =
                ReportingService
                        .findTransactionsPaged(1, 1)
                        .withTransactionId(trn.getTransactionId())
                        .where(SearchCriteria.StartDate, startDate)
                        .execute();

        assertNotNull(responseFind);
        assertTrue(responseFind.getTotalRecordCount() > 0);

        TransactionSummary transactionSummary = responseFind.getResults().get(0);
        assertFalse(StringUtils.isNullOrEmpty(transactionSummary.getTransactionId()));
        assertNotNull(transactionSummary.getAlternativePaymentResponse());
        assertEquals(AlternativePaymentType.PAYPAL.toString().toLowerCase(), transactionSummary.getAlternativePaymentResponse().getProviderName());

        if (transactionSummary.getTransactionStatus().equals("PENDING")) {
            assertNotNull(transactionSummary.getAlternativePaymentResponse().getProviderReference());

            Transaction transaction = Transaction.fromId(transactionSummary.getTransactionId(), null, PaymentMethodType.APM);
            transaction.setAlternativePaymentResponse(transactionSummary.getAlternativePaymentResponse());

            Transaction response =
                    transaction
                            .confirm()
                            .execute();

            assertNotNull(response);
            assertEquals("SUCCESS", response.getResponseCode());
            assertEquals("CAPTURED", response.getResponseMessage());

            Transaction trnRefund =
                    transaction
                            .refund()
                            .withCurrency(currency)
                            .execute();

            assertNotNull(trnRefund);
            assertEquals("SUCCESS", trnRefund.getResponseCode());
            assertEquals("CAPTURED", trnRefund.getResponseMessage());
        } else assertEquals("INITIATED", transactionSummary.getTransactionStatus());
    }

    @Ignore
    @Test
    //Sandbox returning: Can't CAPTURE a Transaction that is already CAPTURED
    public void PayPalFullCycle_Reverse() throws ApiException, InterruptedException {
        Transaction trn =
                paymentMethod
                        .charge(1.22)
                        .withCurrency(currency)
                        .withDescription("New APM")
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals("INITIATED", trn.getResponseMessage());

        System.out.println("copy the link and open it in a browser, do the login wih your paypal credentials and authorize the payment in the paypal form. You will be redirected to a blank page with a printed message like this: { \"success\": true }. This has to be done within a 25 seconds timeframe.");
        System.out.println(trn.getAlternativePaymentResponse().getRedirectUrl());

        Thread.sleep(25000);

        TransactionSummaryPaged response =
                ReportingService
                        .findTransactionsPaged(1, 1)
                        .withTransactionId(trn.getTransactionId())
                        .where(SearchCriteria.StartDate, startDate)
                        .execute();

        assertNotNull(response);
        assertTrue(response.getTotalRecordCount() > 0);
        TransactionSummary transactionSummary = response.getResults().get(0);
        assertFalse(StringUtils.isNullOrEmpty(transactionSummary.getTransactionId()));
        assertNotNull(transactionSummary.getAlternativePaymentResponse());
        assertEquals(AlternativePaymentType.PAYPAL.toString().toLowerCase(), transactionSummary.getAlternativePaymentResponse().getProviderName());

        if (transactionSummary.getTransactionStatus().equals("PENDING")) {
            assertNotNull(transactionSummary.getAlternativePaymentResponse().getProviderReference());

            Transaction transaction = Transaction.fromId(transactionSummary.getTransactionId(), null, PaymentMethodType.APM);
            transaction.setAlternativePaymentResponse(transactionSummary.getAlternativePaymentResponse());

            Transaction responseTrn =
                    transaction
                            .confirm()
                            .execute();

            assertNotNull(responseTrn);
            assertEquals("SUCCESS", responseTrn.getResponseCode());
            assertEquals("CAPTURED", responseTrn.getResponseMessage());

            Transaction trnReverse =
                    responseTrn
                            .reverse()
                            .withCurrency(currency)
                            .execute();

            assertNotNull(trnReverse);
            assertEquals("SUCCESS", trnReverse.getResponseCode());
            assertEquals("REVERSED", trnReverse.getResponseMessage());
        } else assertEquals("INITIATED", transactionSummary.getTransactionStatus());
    }

    @Test
    public void PayPalMultiCapture_fullCycle() throws InterruptedException, ApiException {
        Transaction response =
                paymentMethod
                        .authorize(3)
                        .withCurrency(currency)
                        .withMultiCapture(true)
                        .withDescription("PayPal Multicapture")
                        .execute();

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals("INITIATED", response.getResponseMessage());

        System.out.println("copy the link and open it in a browser, do the login wih your paypal credentials and authorize the payment in the paypal form. You will be redirected to a blank page with a printed message like this: { \"success\": true }. This has to be done within a 25 seconds timeframe.");
        System.out.println(response.getAlternativePaymentResponse().getRedirectUrl());

        Thread.sleep(25000);

        TransactionSummaryPaged responseFind =
                ReportingService
                        .findTransactionsPaged(1, 1)
                        .withTransactionId(response.getTransactionId())
                        .where(SearchCriteria.StartDate, startDate)
                        .execute();

        assertNotNull(responseFind);
        assertTrue(responseFind.getTotalRecordCount() > 0);

        TransactionSummary transactionSummary = responseFind.getResults().get(0);
        assertFalse(StringUtils.isNullOrEmpty(transactionSummary.getTransactionId()));
        assertNotNull(transactionSummary.getAlternativePaymentResponse());
        assertEquals(AlternativePaymentType.PAYPAL.toString().toLowerCase(), transactionSummary.getAlternativePaymentResponse().getProviderName());

        if (transactionSummary.getTransactionStatus().equals("PENDING")) {
            assertNotNull(transactionSummary.getAlternativePaymentResponse().getProviderReference());

            Transaction transaction = Transaction.fromId(transactionSummary.getTransactionId(), null, PaymentMethodType.APM);
            transaction.setAlternativePaymentResponse(transactionSummary.getAlternativePaymentResponse());

            Transaction responseConf =
                    transaction
                            .confirm()
                            .execute();

            assertNotNull(responseConf);
            assertEquals("SUCCESS", responseConf.getResponseCode());
            assertEquals("PREAUTHORIZED", responseConf.getResponseMessage());

            Transaction capture =
                    transaction
                            .capture(1)
                            .execute();

            assertNotNull(capture);
            assertEquals("SUCCESS", capture.getResponseCode());
            assertEquals("CAPTURED", capture.getResponseMessage());

            Transaction capture2 =
                    transaction
                            .capture(2)
                            .execute();

            assertNotNull(capture2);
            assertEquals("SUCCESS", capture2.getResponseCode());
            assertEquals("CAPTURED", capture2.getResponseMessage());
        } else assertEquals("INITIATED", transactionSummary.getTransactionStatus());
    }

    @Test
    // unit_amount is actually the total amount for the item; waiting info about the shipping_discount
    public void PayPalChargeWithoutConfirm() throws ApiException {
        OrderDetails order = new OrderDetails();
        order.setInsuranceAmount(new BigDecimal(10));
        order.setHandlingAmount(new BigDecimal(2));
        order.setDescription("Order description");
        order.hasInsurance(true);

        ArrayList<Product> products = new ArrayList<>();
        products.add(
                new Product()
                        .setProductId("SKU251584")
                        .setProductName("Magazine Subscription")
                        .setDescription("Product description 1")
                        .setQuantity(1)
                        .setUnitPrice(new BigDecimal(7))
                        .setUnitCurrency(currency)
                        .setTaxAmount(new BigDecimal("0.5")));

        products.add(
                new Product()
                        .setProductId("SKU8884785")
                        .setProductName("Charger")
                        .setDescription("Product description 2")
                        .setQuantity(2)
                        .setUnitPrice(new BigDecimal(6))
                        .setUnitCurrency(currency)
                        .setTaxAmount(new BigDecimal("0.5")));

        Transaction response =
                paymentMethod
                        .charge(29)
                        .withCurrency(currency)
                        .withDescription("New APM Uplift")
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withCustomerId("REF123456789")
                        .withMiscProductData(products)
                        .withPhoneNumber("44", "124 445 556", PhoneNumberType.Work)
                        .withPhoneNumber("44", "124 444 333", PhoneNumberType.Home)
                        .withPhoneNumber("1", "258 3697 144", PhoneNumberType.Shipping)
                        .withOrderId("124214-214221")
                        .withShippingAmt(new BigDecimal(3))
                        //.withShippingDiscount(1)
                        .withOrderDetails(order)
                        .execute();

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals("INITIATED", response.getResponseMessage());
    }

    @Test
    public void Alipay() throws ApiException{
        AlternativePaymentMethod paymentMethod = new AlternativePaymentMethod()
                .setAlternativePaymentMethodType(AlternativePaymentType.ALIPAY)
                .setReturnUrl("https://example.com/returnUrl")
                .setStatusUpdateUrl("https://example.com/statusUrl")
                .setCountry("US")
                .setAccountHolderName("Jana Doe");

        Transaction response = paymentMethod
                .charge(19.99)
                .withCurrency("HKD")
                .withMerchantCategory(MerchantCategory.OTHER)
                .execute();

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(TransactionStatus.Initiated.toString().toUpperCase(),response.getResponseMessage());
        assertNotNull(response.getAlternativePaymentResponse().getRedirectUrl());
        assertEquals(AlternativePaymentType.ALIPAY.toString(),response.getAlternativePaymentResponse().getProviderName().toUpperCase());
    }

    @Test
    public void Alipay_MissingReturnUrl() throws ApiException {
        AlternativePaymentMethod paymentMethod = new AlternativePaymentMethod()
                .setAlternativePaymentMethodType(AlternativePaymentType.ALIPAY)
                .setStatusUpdateUrl("https://example.com/statusUrl")
                .setCountry("US")
                .setAccountHolderName("Jana Doe");

        boolean exceptionCaught = false;

        try {
            Transaction response = paymentMethod
                    .charge(19.99)
                    .withCurrency("HKD")
                    .withMerchantCategory(MerchantCategory.OTHER)
                    .execute();
        } catch (BuilderException exception) {
            exceptionCaught = true;
            assertEquals("returnUrl cannot be null for this transaction type.", exception.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void Alipay_MissingStatusUrl() throws ApiException {
        AlternativePaymentMethod paymentMethod = new AlternativePaymentMethod()
                .setAlternativePaymentMethodType(AlternativePaymentType.ALIPAY)
                .setReturnUrl("https://example.com/returnUrl")
                .setCountry("US")
                .setAccountHolderName("Jana Doe");

        boolean exceptionCaught = false;

        try {
            Transaction response = paymentMethod
                    .charge(19.99)
                    .withCurrency("HKD")
                    .withMerchantCategory(MerchantCategory.OTHER)
                    .execute();
        } catch (BuilderException exception) {
            exceptionCaught = true;
            assertEquals("statusUpdateUrl cannot be null for this transaction type.", exception.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void Alipay_MissingAccountHolderName() throws ApiException {
        AlternativePaymentMethod paymentMethod = new AlternativePaymentMethod()
                .setAlternativePaymentMethodType(AlternativePaymentType.ALIPAY)
                .setReturnUrl("https://example.com/returnUrl")
                .setStatusUpdateUrl("https://example.com/statusUrl")
                .setCountry("US");

        boolean exceptionCaught = false;

        try {
            Transaction response = paymentMethod
                    .charge(19.99)
                    .withCurrency("HKD")
                    .withMerchantCategory(MerchantCategory.OTHER)
                    .execute();
        } catch (BuilderException exception) {
            exceptionCaught = true;
            assertEquals("accountHolderName cannot be null for this transaction type.", exception.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void Alipay_MissingCurrency() throws ApiException {
        AlternativePaymentMethod paymentMethod = new AlternativePaymentMethod()
                .setAlternativePaymentMethodType(AlternativePaymentType.ALIPAY)
                .setStatusUpdateUrl("https://example.com/statusUrl")
                .setCountry("US")
                .setReturnUrl("https://example.com/returnUrl")
                .setAccountHolderName("Jana Doe");

        boolean exceptionCaught = false;

        try {
            Transaction response = paymentMethod
                    .charge(19.99)
                    .withMerchantCategory(MerchantCategory.OTHER)
                    .execute();
        } catch (BuilderException exception) {
            exceptionCaught = true;
            assertEquals("currency cannot be null for this transaction type.", exception.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void Alipay_MissingMerchantCategory() throws ApiException {
        AlternativePaymentMethod paymentMethod = new AlternativePaymentMethod()
                .setAlternativePaymentMethodType(AlternativePaymentType.ALIPAY)
                .setStatusUpdateUrl("https://example.com/statusUrl")
                .setReturnUrl("https://example.com/returnUrl")
                .setCountry("US")
                .setAccountHolderName("Jana Doe");

        boolean exceptionCaught = false;

        try {
            Transaction response = paymentMethod
                    .charge(19.99)
                    .withCurrency("HKD")
                    .execute();
        } catch (GatewayException exception) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields merchant_category", exception.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }
}
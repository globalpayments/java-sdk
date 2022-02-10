package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.paymentMethods.AlternativePaymentMethod;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.ReportingService;
import com.global.api.utils.StringUtils;
import lombok.var;
import org.joda.time.DateTime;
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
 *
 * How to have a success running test. When you will run the test in the console it will be printed the
 * Paypal redirect url. You need to copy the link and open it in a browser, do the login wih your Paypal
 * credentials and authorize the payment in the Paypal form. You will be redirected to a blank page with a
 * printed message like this: { "success": true }. This has to be done within a 25 seconds timeframe.
 * In case you need more time update the sleep() to what you need.
 */
public class GpApiApmTests extends BaseGpApiTest {

    private AlternativePaymentMethod paymentMethod;
    private String currency;
    private Address shippingAddress = null;

    @Before
    public void initialize() throws ConfigurationException {

        GpApiConfig config = new GpApiConfig();
        config.setAppId("Uyq6PzRbkorv2D4RQGlldEtunEeGNZll");
        config.setAppKey("QDsW1ETQKHX6Y4TA");
        config.setChannel(Channel.CardNotPresent.getValue());
        config.setEnableLogging(true);

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);

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
        var response = paymentMethod
                .charge(1.34)
                .withCurrency(currency)
                .withDescription("New APM")
                .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals("INITIATED", response.getResponseMessage());

        System.out.println("copy the link and open it in a browser, do the login wih your paypal credentials and authorize the payment in the paypal form. You will be redirected to a blank page with a printed message like this: { \"success\": true }. This has to be done within a 25 seconds timeframe.");
        if (response.getAlternativePaymentResponse() != null) {
            System.out.println(response.getAlternativePaymentResponse().getRedirectUrl());
        }

        Thread.sleep(25000);

        var startDate = new Date();

        var responseFind =
                ReportingService
                        .findTransactionsPaged(1, 1)
                        .withTransactionId(response.getTransactionId())
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, startDate)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(responseFind);
        assertTrue(responseFind.getTotalRecordCount() > 0);

        var transactionSummary = responseFind.getResults().get(0);
        assertTrue(transactionSummary.getAlternativePaymentResponse() instanceof AlternativePaymentResponse);
        assertEquals(AlternativePaymentType.PAYPAL.toString().toLowerCase(), transactionSummary.getAlternativePaymentResponse().getProviderName());
        assertEquals("PENDING", transactionSummary.getTransactionStatus());
        assertNotNull(transactionSummary.getAlternativePaymentResponse().getProviderReference());

        var transaction = Transaction.fromId(transactionSummary.getTransactionId(), null, PaymentMethodType.APM);
        transaction.setAlternativePaymentResponse(transactionSummary.getAlternativePaymentResponse());

        response =
                transaction
                        .confirm()
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals("CAPTURED", response.getResponseMessage());
    }

    @Test
    public void PayPalCapture_fullCycle() throws ApiException, InterruptedException {
        var response =
                paymentMethod
                        .authorize(1.34)
                        .withCurrency(currency)
                        .withDescription("New APM")
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals("INITIATED", response.getResponseMessage());

        System.out.println("copy the link and open it in a browser, do the login wih your paypal credentials and authorize the payment in the paypal form. You will be redirected to a blank page with a printed message like this: { \"success\": true }. This has to be done within a 25 seconds timeframe.");
        if (response.getAlternativePaymentResponse() != null) {
            System.out.println(response.getAlternativePaymentResponse().getRedirectUrl());
        }

        Thread.sleep(25000);

        var startDate = new Date();

        var responseFind =
                ReportingService
                        .findTransactionsPaged(1, 1)
                        .withTransactionId(response.getTransactionId())
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, startDate)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(responseFind);
        assertTrue(responseFind.getTotalRecordCount() > 0);

        var transactionSummary = responseFind.getResults().get(0);
        assertFalse(StringUtils.isNullOrEmpty(transactionSummary.getTransactionId()));
        assertTrue(transactionSummary.getAlternativePaymentResponse() instanceof AlternativePaymentResponse);
        assertEquals(AlternativePaymentType.PAYPAL.toString().toLowerCase(), transactionSummary.getAlternativePaymentResponse().getProviderName());
        assertEquals("PENDING", transactionSummary.getTransactionStatus());
        assertNotNull(transactionSummary.getAlternativePaymentResponse().getProviderReference());

        var transaction = Transaction.fromId(transactionSummary.getTransactionId(), null, PaymentMethodType.APM);
        transaction.setAlternativePaymentResponse(transactionSummary.getAlternativePaymentResponse());

        response =
                transaction
                        .confirm()
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals("PREAUTHORIZED", response.getResponseMessage());

        var capture =
                transaction
                        .capture()
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(capture);
        assertEquals("SUCCESS", capture.getResponseCode());
        assertEquals("CAPTURED", capture.getResponseMessage());
    }

    @Test
    public void PayPalFullCycle_Refund() throws ApiException, InterruptedException {
        var trn =
                paymentMethod
                        .charge(1.22)
                        .withCurrency(currency)
                        .withDescription("New APM")
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals("INITIATED", trn.getResponseMessage());

        System.out.println("copy the link and open it in a browser, do the login wih your paypal credentials and authorize the payment in the paypal form. You will be redirected to a blank page with a printed message like this: { \"success\": true }. This has to be done within a 25 seconds timeframe.");
        if (trn.getAlternativePaymentResponse() != null) {
            System.out.println(trn.getAlternativePaymentResponse().getRedirectUrl());
        }

        Thread.sleep(25000);

        var startDate = new Date();

        var responseFind =
                ReportingService
                        .findTransactionsPaged(1, 1)
                        .withTransactionId(trn.getTransactionId())
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, startDate)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(responseFind);
        assertTrue(responseFind.getTotalRecordCount() > 0);

        var transactionSummary = responseFind.getResults().get(0);
        assertFalse(StringUtils.isNullOrEmpty(transactionSummary.getTransactionId()));
        assertTrue(transactionSummary.getAlternativePaymentResponse() instanceof AlternativePaymentResponse);
        assertEquals(AlternativePaymentType.PAYPAL.toString().toLowerCase(), transactionSummary.getAlternativePaymentResponse().getProviderName());
        assertEquals("PENDING", transactionSummary.getTransactionStatus());
        assertNotNull(transactionSummary.getAlternativePaymentResponse().getProviderReference());

        var transaction = Transaction.fromId(transactionSummary.getTransactionId(), null, PaymentMethodType.APM);
        transaction.setAlternativePaymentResponse(transactionSummary.getAlternativePaymentResponse());

        var response =
                transaction
                        .confirm()
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals("CAPTURED", response.getResponseMessage());

        var trnRefund =
                transaction
                        .refund()
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(trnRefund);
        assertEquals("SUCCESS", trnRefund.getResponseCode());
        assertEquals("CAPTURED", trnRefund.getResponseMessage());
    }

    @Ignore
    @Test
    //Sandbox returning: Can't CAPTURE a Transaction that is already CAPTURED
    public void PayPalFullCycle_Reverse() throws ApiException, InterruptedException {
        var trn =
                paymentMethod
                        .charge(1.22)
                        .withCurrency(currency)
                        .withDescription("New APM")
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals("INITIATED", trn.getResponseMessage());

        System.out.println("copy the link and open it in a browser, do the login wih your paypal credentials and authorize the payment in the paypal form. You will be redirected to a blank page with a printed message like this: { \"success\": true }. This has to be done within a 25 seconds timeframe.");
        System.out.println(trn.getAlternativePaymentResponse().getRedirectUrl());

        Thread.sleep(25000);

        var startDate = DateTime.now();

        var response =
                ReportingService
                        .findTransactionsPaged(1, 1)
                        .withTransactionId(trn.getTransactionId())
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, startDate)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertTrue(response.getTotalRecordCount() > 0);
        var transactionSummary = response.getResults().get(0);
        assertFalse(StringUtils.isNullOrEmpty(transactionSummary.getTransactionId()));
        assertTrue(transactionSummary.getAlternativePaymentResponse() instanceof AlternativePaymentResponse);
        assertEquals(AlternativePaymentType.PAYPAL.toString().toLowerCase(), transactionSummary.getAlternativePaymentResponse().getProviderName());
        assertEquals("PENDING", transactionSummary.getTransactionStatus());
        assertNotNull(transactionSummary.getAlternativePaymentResponse().getProviderReference());

        var transaction = Transaction.fromId(transactionSummary.getTransactionId(), null,PaymentMethodType.APM);
        transaction.setAlternativePaymentResponse(transactionSummary.getAlternativePaymentResponse());

        var responsetrn =
                transaction
                        .confirm()
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(responsetrn);
        assertEquals("SUCCESS", responsetrn.getResponseCode());
        assertEquals("CAPTURED", responsetrn.getResponseMessage());

        var trnReverse =
                responsetrn
                        .reverse()
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(trnReverse);
        assertEquals("SUCCESS", trnReverse.getResponseCode());
        assertEquals("REVERSED", trnReverse.getResponseMessage());
    }

    @Test
    public void PayPalMultiCapture_fullCycle() throws InterruptedException, ApiException {
        var response =
                paymentMethod
                        .authorize(3)
                        .withCurrency(currency)
                        .withMultiCapture(true)
                        .withDescription("PayPal Multicapture")
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals("INITIATED", response.getResponseMessage());

        System.out.println("copy the link and open it in a browser, do the login wih your paypal credentials and authorize the payment in the paypal form. You will be redirected to a blank page with a printed message like this: { \"success\": true }. This has to be done within a 25 seconds timeframe.");
        if (response.getAlternativePaymentResponse() != null) {
            System.out.println(response.getAlternativePaymentResponse().getRedirectUrl());
        }

        Thread.sleep(25000);

        var startDate = new Date();

        var responseFind =
                ReportingService
                        .findTransactionsPaged(1, 1)
                        .withTransactionId(response.getTransactionId())
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, startDate)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(responseFind);
        assertTrue(responseFind.getTotalRecordCount() > 0);

        var transactionSummary = responseFind.getResults().get(0);
        assertFalse(StringUtils.isNullOrEmpty(transactionSummary.getTransactionId()));
        assertTrue(transactionSummary.getAlternativePaymentResponse() instanceof AlternativePaymentResponse);
        assertEquals(AlternativePaymentType.PAYPAL.toString().toLowerCase(), transactionSummary.getAlternativePaymentResponse().getProviderName());
        assertEquals("PENDING", transactionSummary.getTransactionStatus());
        assertNotNull(transactionSummary.getAlternativePaymentResponse().getProviderReference());

        var transaction = Transaction.fromId(transactionSummary.getTransactionId(), null, PaymentMethodType.APM);
        transaction.setAlternativePaymentResponse(transactionSummary.getAlternativePaymentResponse());

        var responseConf =
                transaction
                        .confirm()
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(responseConf);
        assertEquals("SUCCESS", responseConf.getResponseCode());
        assertEquals("PREAUTHORIZED", responseConf.getResponseMessage());

        var capture =
                transaction
                        .capture(1)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(capture);
        assertEquals("SUCCESS", capture.getResponseCode());
        assertEquals("CAPTURED", capture.getResponseMessage());

        var capture2 =
                transaction
                        .capture(2)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(capture2);
        assertEquals("SUCCESS", capture2.getResponseCode());
        assertEquals("CAPTURED", capture2.getResponseMessage());
    }

    @Test
    // unit_amount is actually the total amount for the item; waiting info about the shipping_discount
    public void PayPalChargeWithoutConfirm() throws ApiException {
        var order = new OrderDetails();
        order.setInsuranceAmount(new BigDecimal(10));
        order.setHandlingAmount(new BigDecimal(2));
        order.setDescription("Order description");
        order.hasInsurance(true);

        var products = new ArrayList<Product>();
        products.add(
                new Product()
                        .setProductId("SKU251584")
                        .setProductName("Magazine Subscription")
                        .setDescription("Product description 1")
                        .setQuantity(1)
                        .setUnitPrice(new BigDecimal(7))
                        .setUnitCurrency(currency)
                        .setTaxAmount(new BigDecimal(0.5)));

        products.add(
                new Product()
                        .setProductId("SKU8884785")
                        .setProductName("Charger")
                        .setDescription("Product description 2")
                        .setQuantity(2)
                        .setUnitPrice(new BigDecimal(6))
                        .setUnitCurrency(currency)
                        .setTaxAmount(new BigDecimal(0.5)));

        var response =
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
                .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals("INITIATED", response.getResponseMessage());
    }

}
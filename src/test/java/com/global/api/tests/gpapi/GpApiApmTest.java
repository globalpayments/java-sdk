package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.entities.reporting.TransactionSummaryPaged;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.paymentMethods.AlternativePaymentMethod;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.ReportingService;
import com.global.api.utils.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import static com.global.api.entities.enums.AlternativePaymentType.BLIK;
import static com.global.api.entities.enums.AlternativePaymentType.OB;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;



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
    static final BigDecimal amount = new BigDecimal(0.01);
    static final String returnUrl = "https://www.example.com/returnUrl";
    static final String statusUpdateUrl = "https://www.example.com/statusUrl";
    static final String descriptor = "Test Transaction";
    static final String accountName = "James Mason";
    static final String chargeDescription = "New APM";

    @BeforeEach
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

    @Disabled
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
                        .withCustomerId("PYR_b2d3b367fcf141dcbd03cd9ccfa60519")
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
    public void Alipay() throws ApiException {
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
        assertEquals(TransactionStatus.Initiated.toString().toUpperCase(), response.getResponseMessage());
        assertNotNull(response.getAlternativePaymentResponse().getRedirectUrl());
        assertEquals(AlternativePaymentType.ALIPAY.toString(), response.getAlternativePaymentResponse().getProviderName().toUpperCase());
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
            paymentMethod
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
            paymentMethod
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
            paymentMethod
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
            paymentMethod
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
            paymentMethod
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

package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.entities.reporting.TransactionSummaryPaged;
import com.global.api.paymentMethods.BNPL;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.ReportingService;
import com.global.api.utils.StringUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GpApiBNPLTest extends BaseGpApiTest {
    private BNPL paymentMethod;
    private final String currency = "USD";
    private final BigDecimal amount = new BigDecimal("550");
    private Address shippingAddress;
    private Address billingAddress;

    public GpApiBNPLTest() throws ApiException {
        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        ServicesContainer.configureService(config);
    }

    @Before
    public void testInitialize() {
        paymentMethod =
                new BNPL()
                        .setBNPLType(BNPLType.AFFIRM)
                        .setReturnUrl("https://7b8e82a17ac00346e91e984f42a2a5fb.m.pipedream.net")
                        .setStatusUpdateUrl("https://7b8e82a17ac00346e91e984f42a2a5fb.m.pipedream.net")
                        .setCancelUrl("https://7b8e82a17ac00346e91e984f42a2a5fb.m.pipedream.net");

        billingAddress = new Address();
        billingAddress.setStreetAddress1("10 Glenlake Pkwy NE");
        billingAddress.setStreetAddress2("no");
        billingAddress.setCity("Birmingham");
        billingAddress.setPostalCode("50001");
        billingAddress.setCountryCode("US");
        billingAddress.setState("IL");

        shippingAddress = new Address();
        shippingAddress.setStreetAddress1("Apartment 852");
        shippingAddress.setStreetAddress2("Complex 741");
        shippingAddress.setStreetAddress3("no");
        shippingAddress.setCity("Birmingham");
        shippingAddress.setPostalCode("50001");
        shippingAddress.setState("IL");
        shippingAddress.setCountryCode("US");
    }

    @Test
    public void BNPL_FullCycle() throws ApiException, InterruptedException {
        Customer customer = generateCustomerData();
        ArrayList<Product> products = generateProducts();

        Transaction response =
                paymentMethod
                        .authorize(amount)
                        .withCurrency(currency)
                        .withMiscProductData(products)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withAddress(billingAddress, AddressType.Billing)
                        .withPhoneNumber("1", "7708298000", PhoneNumberType.Shipping)
                        .withCustomerData(customer)
                        .withBNPLShippingMethod(BNPLShippingMethod.DELIVERY)
                        .execute();

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(TransactionStatus.Initiated.getValue().toUpperCase(), response.getResponseMessage());

        System.out.println(response.getBNPLResponse().getRedirectUrl());

        Thread.sleep(60000);

        TransactionSummary findTransactionByIdResponse =
                ReportingService
                        .transactionDetail(response.getTransactionId())
                        .execute();

        assertNotNull(findTransactionByIdResponse);

        if (findTransactionByIdResponse.getTransactionStatus().equals("PREAUTHORIZED")) {
            Transaction captured =
                    response
                            .capture()
                            .execute();

            assertNotNull(captured);
            assertEquals("SUCCESS", captured.getResponseCode());
            assertEquals(TransactionStatus.Captured.getValue(), captured.getResponseMessage());

            Transaction refund =
                    captured
                            .refund(amount)
                            .withCurrency(currency)
                            .execute();

            assertNotNull(refund);
            assertEquals("SUCCESS", refund.getResponseCode());
            assertEquals(TransactionStatus.Captured.getValue(), refund.getResponseMessage());
        } else assertEquals("INITIATED", findTransactionByIdResponse.getTransactionStatus());
    }

    @Test
    public void FullRefund() throws ApiException {
        TransactionSummaryPaged response =
                ReportingService
                        .findTransactionsPaged(1, 10)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Ascending)
                        .where(SearchCriteria.PaymentMethodName, PaymentMethodName.BNPL)
                        .and(SearchCriteria.TransactionStatus, TransactionStatus.Captured)
                        .and(SearchCriteria.PaymentType, PaymentType.Sale)
                        .execute();

        assertNotNull(response);
        assertTrue(!response.getResults().isEmpty());

        TransactionSummary trnSummary = response.getResults().get(new Random().nextInt(response.getResults().size()));
        Transaction trn = Transaction.fromId(trnSummary.getTransactionId(), trnSummary.getPaymentType());

        Transaction trnRefund =
                trn
                        .refund(StringUtils.toAmount(trnSummary.getAmount().toString()))
                        .withCurrency(trnSummary.getCurrency())
                        .execute();

        assertNotNull(trnRefund);
        assertEquals("SUCCESS", trnRefund.getResponseCode());
        assertEquals(TransactionStatus.Captured.toString().toUpperCase(), trnRefund.getResponseMessage());
    }

    @Test
    public void BNPL_PartialRefund() throws ApiException, InterruptedException {
        Customer customer = generateCustomerData();
        ArrayList<Product> products = generateProducts();

        Transaction transaction =
                paymentMethod
                        .authorize(amount)
                        .withCurrency(currency)
                        .withMiscProductData(products)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withAddress(billingAddress, AddressType.Billing)
                        .withPhoneNumber("41", "57774873", PhoneNumberType.Shipping)
                        .withCustomerData(customer)
                        .withBNPLShippingMethod(BNPLShippingMethod.DELIVERY)
                        .withOrderId("12365")
                        .execute();

        assertNotNull(transaction);
        assertEquals("SUCCESS", transaction.getResponseCode());
        assertEquals(TransactionStatus.Initiated.toString().toUpperCase(), transaction.getResponseMessage());
        assertNotNull(transaction.getBNPLResponse().getRedirectUrl());

        System.out.println(transaction.getBNPLResponse().getRedirectUrl());

        Thread.sleep(45000);

        TransactionSummary findTransactionByIdResponse =
                ReportingService
                        .transactionDetail(transaction.getTransactionId())
                        .execute();

        assertNotNull(findTransactionByIdResponse);

        if (findTransactionByIdResponse.getTransactionStatus().equals("PREAUTHORIZED")) {

            Transaction captureTrn =
                    transaction
                            .capture()
                            .execute();

            assertNotNull(captureTrn);
            assertEquals("SUCCESS", captureTrn.getResponseCode());
            assertEquals(TransactionStatus.Captured.toString().toUpperCase(), captureTrn.getResponseMessage());

            Thread.sleep(15000);

            Transaction trnRefund =
                    captureTrn
                            .refund(100)
                            .withCurrency(currency)
                            .execute();

            assertNotNull(trnRefund);
            assertEquals("SUCCESS", trnRefund.getResponseCode());
            assertEquals(TransactionStatus.Captured.toString().toUpperCase(), trnRefund.getResponseMessage());
        } else assertEquals("INITIATED", findTransactionByIdResponse.getTransactionStatus());
    }

    @Test
    public void BNPL_MultipleRefund() throws ApiException, InterruptedException {
        Customer customer = generateCustomerData();
        ArrayList<Product> products = generateProducts();

        Transaction transaction =
                paymentMethod
                        .authorize(amount)
                        .withCurrency(currency)
                        .withMiscProductData(products)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withAddress(billingAddress, AddressType.Billing)
                        .withPhoneNumber("41", "57774873", PhoneNumberType.Shipping)
                        .withCustomerData(customer)
                        .withBNPLShippingMethod(BNPLShippingMethod.DELIVERY)
                        .withOrderId("12365")
                        .execute();

        assertNotNull(transaction);
        assertEquals("SUCCESS", transaction.getResponseCode());
        assertEquals(TransactionStatus.Initiated.toString().toUpperCase(), transaction.getResponseMessage());
        assertNotNull(transaction.getBNPLResponse().getRedirectUrl());

        System.out.println(transaction.getBNPLResponse().getRedirectUrl());

        Thread.sleep(60000);

        TransactionSummary findTransactionByIdResponse =
                ReportingService
                        .transactionDetail(transaction.getTransactionId())
                        .execute();

        assertNotNull(findTransactionByIdResponse);

        if (findTransactionByIdResponse.getTransactionStatus().equals("PREAUTHORIZED")) {

            Transaction captureTrn =
                    transaction
                            .capture()
                            .execute();

            assertNotNull(captureTrn);
            assertEquals("SUCCESS", captureTrn.getResponseCode());
            assertEquals(TransactionStatus.Captured.toString().toUpperCase(), captureTrn.getResponseMessage());

            Thread.sleep(60000);

            Transaction trnRefund =
                    captureTrn
                            .refund(100)
                            .withCurrency(currency)
                            .execute();

            assertNotNull(trnRefund);
            assertEquals("SUCCESS", trnRefund.getResponseCode());
            assertEquals(TransactionStatus.Captured.toString().toUpperCase(), trnRefund.getResponseMessage());

            Transaction secondTrnRefund =
                    captureTrn
                            .refund(100)
                            .withCurrency(currency)
                            .execute();

            assertNotNull(secondTrnRefund);
            assertEquals("SUCCESS", secondTrnRefund.getResponseCode());
            assertEquals(TransactionStatus.Captured.toString().toUpperCase(), secondTrnRefund.getResponseMessage());
        } else assertEquals("INITIATED", findTransactionByIdResponse.getTransactionStatus());
    }

    @Test
    public void BNPL_Reverse() throws ApiException, InterruptedException {
        Customer customer = generateCustomerData();
        ArrayList<Product> products = generateProducts();

        Transaction transaction =
                paymentMethod
                        .authorize(amount)
                        .withCurrency(currency)
                        .withMiscProductData(products)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withAddress(billingAddress, AddressType.Billing)
                        .withPhoneNumber("41", "57774873", PhoneNumberType.Shipping)
                        .withCustomerData(customer)
                        .withBNPLShippingMethod(BNPLShippingMethod.DELIVERY)
                        .withOrderId("12365")
                        .execute();

        assertNotNull(transaction);
        assertEquals("SUCCESS", transaction.getResponseCode());
        assertEquals(TransactionStatus.Initiated.toString().toUpperCase(), transaction.getResponseMessage());
        assertNotNull(transaction.getBNPLResponse().getRedirectUrl());

        System.out.println(transaction.getBNPLResponse().getRedirectUrl());

        Thread.sleep(60000);

        TransactionSummary findTransactionByIdResponse =
                ReportingService
                        .transactionDetail(transaction.getTransactionId())
                        .execute();

        assertNotNull(findTransactionByIdResponse);

        if (findTransactionByIdResponse.getTransactionStatus().equals("PREAUTHORIZED")) {

            Transaction reverseTransactionResponse =
                    transaction
                            .reverse()
                            .execute();

            assertNotNull(reverseTransactionResponse);
            assertEquals("SUCCESS", reverseTransactionResponse.getResponseCode());
            assertEquals(TransactionStatus.Reversed.toString().toUpperCase(), reverseTransactionResponse.getResponseMessage());
        } else assertEquals("INITIATED", findTransactionByIdResponse.getTransactionStatus());
    }

    @Test
    public void BNPL_OnlyMandatory() throws ApiException {
        Customer customer = generateCustomerData();
        ArrayList<Product> products = generateProducts();

        Transaction transaction =
                paymentMethod
                        .authorize(amount)
                        .withCurrency(currency)
                        .withMiscProductData(products)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withAddress(billingAddress, AddressType.Billing)
                        .withCustomerData(customer)
                        .execute();

        assertNotNull(transaction);
        assertEquals("SUCCESS", transaction.getResponseCode());
        assertEquals(TransactionStatus.Initiated.toString().toUpperCase(), transaction.getResponseMessage());
        assertNotNull(transaction.getBNPLResponse().getRedirectUrl());
    }

    @Test
    public void BNPL_KlarnaProvider() throws ApiException, InterruptedException {
        paymentMethod.BNPLType = BNPLType.KLARNA;
        Customer customer = generateCustomerData();
        ArrayList<Product> products = generateProducts();

        Transaction transaction =
                paymentMethod
                        .authorize(amount)
                        .withCurrency(currency)
                        .withMiscProductData(products)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withAddress(billingAddress, AddressType.Billing)
                        .withCustomerData(customer)
                        .execute();

        assertNotNull(transaction);
        assertEquals("SUCCESS", transaction.getResponseCode());
        assertEquals(TransactionStatus.Initiated.getValue(), transaction.getResponseMessage());
        assertNotNull(transaction.getBNPLResponse().getRedirectUrl());

        System.out.println(transaction.getBNPLResponse().getRedirectUrl());

        Thread.sleep(45000);

        TransactionSummary findTransactionByIdResponse =
                ReportingService
                        .transactionDetail(transaction.getTransactionId())
                        .execute();

        assertNotNull(findTransactionByIdResponse);

        if (findTransactionByIdResponse.getTransactionStatus().equals("PREAUTHORIZED")) {

            Transaction captureTrn =
                    transaction
                            .capture()
                            .execute();

            assertNotNull(captureTrn);
            assertEquals("SUCCESS", captureTrn.getResponseCode());
            assertEquals(TransactionStatus.Captured.getValue(), captureTrn.getResponseMessage());
        } else assertEquals("INITIATED", findTransactionByIdResponse.getTransactionStatus());
    }

    @Test
    public void BNPL_ClearPayProvider() throws ApiException, InterruptedException {
        paymentMethod.BNPLType = BNPLType.CLEARPAY;
        Customer customer = generateCustomerData();
        ArrayList<Product> products = generateProducts();

        Transaction transaction =
                paymentMethod
                        .authorize(amount)
                        .withCurrency(currency)
                        .withMiscProductData(products)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withAddress(billingAddress, AddressType.Billing)
                        .withCustomerData(customer)
                        .execute();

        assertNotNull(transaction);
        assertEquals("SUCCESS", transaction.getResponseCode());
        assertEquals(TransactionStatus.Initiated.toString().toUpperCase(), transaction.getResponseMessage());
        assertNotNull(transaction.getBNPLResponse().getRedirectUrl());

        System.out.println(transaction.getBNPLResponse().getRedirectUrl());

        Thread.sleep(50000);

        TransactionSummary findTransactionByIdResponse =
                ReportingService
                        .transactionDetail(transaction.getTransactionId())
                        .execute();

        assertNotNull(findTransactionByIdResponse);

        if (findTransactionByIdResponse.getTransactionStatus().equals("PREAUTHORIZED")) {

            Transaction captureTrn =
                    transaction
                            .capture()
                            .execute();

            assertNotNull(captureTrn);
            assertEquals("SUCCESS", captureTrn.getResponseCode());
            assertEquals(TransactionStatus.Captured.toString().toUpperCase(), captureTrn.getResponseMessage());
        } else assertEquals("INITIATED", findTransactionByIdResponse.getTransactionStatus());
    }

    @Test
    public void BNPL_ClearPayProvider_PartialCapture() throws ApiException, InterruptedException {
        paymentMethod.BNPLType = BNPLType.CLEARPAY;
        Customer customer = generateCustomerData();
        ArrayList<Product> products = generateProducts();

        Transaction transaction =
                paymentMethod
                        .authorize(amount)
                        .withCurrency(currency)
                        .withMiscProductData(products)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withAddress(billingAddress, AddressType.Billing)
                        .withCustomerData(customer)
                        .execute();

        assertNotNull(transaction);
        assertEquals("SUCCESS", transaction.getResponseCode());
        assertEquals(TransactionStatus.Initiated.toString().toUpperCase(), transaction.getResponseMessage());
        assertNotNull(transaction.getBNPLResponse().getRedirectUrl());

        System.out.println(transaction.getBNPLResponse().getRedirectUrl());

        Thread.sleep(50000);

        TransactionSummary findTransactionByIdResponse =
                ReportingService
                        .transactionDetail(transaction.getTransactionId())
                        .execute();

        assertNotNull(findTransactionByIdResponse);

        if (findTransactionByIdResponse.getTransactionStatus().equals("PREAUTHORIZED")) {

            Transaction captureTrn =
                    transaction
                            .capture(100)
                            .execute();

            assertNotNull(captureTrn);
            assertEquals("SUCCESS", captureTrn.getResponseCode());
            assertEquals(TransactionStatus.Captured.toString().toUpperCase(), captureTrn.getResponseMessage());
        } else assertEquals("INITIATED", findTransactionByIdResponse.getTransactionStatus());
    }

    @Test
    public void BNPL_ClearPayProvider_MultipleCapture() throws ApiException, InterruptedException {
        paymentMethod.BNPLType = BNPLType.CLEARPAY;
        Customer customer = generateCustomerData();
        ArrayList<Product> products = generateProducts();

        Transaction transaction =
                paymentMethod
                        .authorize(amount)
                        .withCurrency(currency)
                        .withMiscProductData(products)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withAddress(billingAddress, AddressType.Billing)
                        .withCustomerData(customer)
                        .withMultiCapture(true)
                        .execute();

        assertNotNull(transaction);
        assertEquals("SUCCESS", transaction.getResponseCode());
        assertEquals(TransactionStatus.Initiated.toString().toUpperCase(), transaction.getResponseMessage());
        assertNotNull(transaction.getBNPLResponse().getRedirectUrl());

        System.out.println(transaction.getBNPLResponse().getRedirectUrl());

        Thread.sleep(45000);

        TransactionSummary findTransactionByIdResponse =
                ReportingService
                        .transactionDetail(transaction.getTransactionId())
                        .execute();

        assertNotNull(findTransactionByIdResponse);

        if (findTransactionByIdResponse.getTransactionStatus().equals("PREAUTHORIZED")) {

            Transaction captureTrn =
                    transaction
                            .capture(100)
                            .execute();

            assertNotNull(captureTrn);
            assertEquals("SUCCESS", captureTrn.getResponseCode());
            assertEquals(TransactionStatus.Captured.toString().toUpperCase(), captureTrn.getResponseMessage());

            Thread.sleep(25000);

            captureTrn =
                    transaction
                            .capture(100)
                            .execute();

            assertNotNull(captureTrn);
            assertEquals("SUCCESS", captureTrn.getResponseCode());
            assertEquals(TransactionStatus.Captured.toString().toUpperCase(), captureTrn.getResponseMessage());
        } else assertEquals("INITIATED", findTransactionByIdResponse.getTransactionStatus());
    }

    @Test
    public void BNPL_InvalidStatusForCapture_NoRedirect() throws ApiException {
        Customer customer = generateCustomerData();
        ArrayList<Product> products = generateProducts();

        Transaction transaction =
                paymentMethod
                        .authorize(amount)
                        .withCurrency(currency)
                        .withMiscProductData(products)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withAddress(billingAddress, AddressType.Billing)
                        .withPhoneNumber("41", "57774873", PhoneNumberType.Shipping)
                        .withCustomerData(customer)
                        .withBNPLShippingMethod(BNPLShippingMethod.DELIVERY)
                        .withOrderId("12365")
                        .execute();

        assertNotNull(transaction);
        assertEquals("SUCCESS", transaction.getResponseCode());
        assertEquals(TransactionStatus.Initiated.toString().toUpperCase(), transaction.getResponseMessage());
        assertNotNull(transaction.getBNPLResponse().getRedirectUrl());

        boolean exceptionCaught = false;
        try {
            transaction
                    .capture()
                    .execute();
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("40090", e.getResponseText());
            assertEquals("Status Code: 400 - id value is invalid. Please check the format and data provided is correct.", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void GetBNPLTransactionById() throws ApiException {
        String id = "TRN_o7PsaRAgOviqLCPHBaxDcqYO70oUhu";

        TransactionSummary trnInfo =
                ReportingService
                        .transactionDetail(id)
                        .execute();

        assertEquals(id, trnInfo.getTransactionId());
    }

    @Test
    public void GetBNPLTransactionById_RandomTransactionId() throws ApiException {
        String transactionId = UUID.randomUUID().toString();

        boolean exceptionCaught = false;
        try {
            ReportingService
                    .transactionDetail(transactionId)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 404 - Transactions " + transactionId + " not found at this /ucp/transactions/" + transactionId + "", ex.getMessage());
            assertEquals("40118", ex.getResponseText());
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void GetBNPLTransactionById_NullTransactionId() throws ApiException {
        boolean exceptionCaught = false;
        try {
            ReportingService
                    .transactionDetail(null)
                    .execute();
        } catch (BuilderException ex) {
            exceptionCaught = true;
            assertEquals("transactionId cannot be null for this transaction type.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void BNPL_MissingProducts() throws ApiException {
        Customer customer = generateCustomerData();

        boolean exceptionCaught = false;
        try {
            paymentMethod
                    .authorize(10)
                    .withCurrency(currency)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withAddress(billingAddress, AddressType.Billing)
                    .withPhoneNumber("1", "7708298000", PhoneNumberType.Shipping)
                    .withCustomerData(customer)
                    .withBNPLShippingMethod(BNPLShippingMethod.DELIVERY)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields: order.items.", ex.getMessage());
            assertEquals("40251", ex.getResponseText());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void BNPL_MissingBillingAddress() throws ApiException {
        Customer customer = generateCustomerData();
        ArrayList<Product> products = generateProducts();

        boolean exceptionCaught = false;
        try {
            paymentMethod
                    .authorize(amount)
                    .withCurrency(currency)
                    .withMiscProductData(products)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withPhoneNumber("1", "7708298000", PhoneNumberType.Shipping)
                    .withCustomerData(customer)
                    .withBNPLShippingMethod(BNPLShippingMethod.DELIVERY)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - One of the parameter is missing from the request body.", ex.getMessage());
            assertEquals("40297", ex.getResponseText());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void BNPL_MissingCustomerData() throws ApiException {
        ArrayList<Product> products = generateProducts();

        boolean exceptionCaught = false;
        try {
            paymentMethod
                    .authorize(amount)
                    .withCurrency(currency)
                    .withMiscProductData(products)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withAddress(billingAddress, AddressType.Billing)
                    .withPhoneNumber("1", "7708298000", PhoneNumberType.Shipping)
                    .withBNPLShippingMethod(BNPLShippingMethod.DELIVERY)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields: payer.email.", ex.getMessage());
            assertEquals("40251", ex.getResponseText());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void BNPL_MissingAmount() throws ApiException {
        Customer customer = generateCustomerData();
        ArrayList<Product> products = generateProducts();

        boolean exceptionCaught = false;
        try {
            paymentMethod
                    .authorize()
                    .withCurrency(currency)
                    .withMiscProductData(products)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withAddress(billingAddress, AddressType.Billing)
                    .withPhoneNumber("1", "7708298000", PhoneNumberType.Shipping)
                    .withCustomerData(customer)
                    .withBNPLShippingMethod(BNPLShippingMethod.DELIVERY)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields amount", ex.getMessage());
            assertEquals("40005", ex.getResponseText());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void BNPL_MissingCurrency() throws ApiException {
        Customer customer = generateCustomerData();
        ArrayList<Product> products = generateProducts();

        boolean exceptionCaught = false;
        try {
            paymentMethod
                    .authorize(amount)
                    .withMiscProductData(products)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withAddress(billingAddress, AddressType.Billing)
                    .withPhoneNumber("1", "7708298000", PhoneNumberType.Shipping)
                    .withCustomerData(customer)
                    .withBNPLShippingMethod(BNPLShippingMethod.DELIVERY)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields currency", ex.getMessage());
            assertEquals("40005", ex.getResponseText());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void BNPL_MissingCustomerEmail() throws ApiException {
        Customer customer = generateCustomerData();
        customer.setEmail(null);
        ArrayList<Product> products = generateProducts();

        boolean exceptionCaught = false;
        try {
            paymentMethod
                    .authorize(amount)
                    .withCurrency(currency)
                    .withMiscProductData(products)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withAddress(billingAddress, AddressType.Billing)
                    .withPhoneNumber("1", "7708298000", PhoneNumberType.Shipping)
                    .withCustomerData(customer)
                    .withBNPLShippingMethod(BNPLShippingMethod.DELIVERY)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields: payer.email.", ex.getMessage());
            assertEquals("40251", ex.getResponseText());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void BNPL_MissingCustomerPhoneNumber() throws ApiException {
        Customer customer = generateCustomerData();
        customer.setPhone(null);
        ArrayList<Product> products = generateProducts();

        boolean exceptionCaught = false;
        try {
            paymentMethod
                    .authorize(amount)
                    .withCurrency(currency)
                    .withMiscProductData(products)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withAddress(billingAddress, AddressType.Billing)
                    .withPhoneNumber("1", "7708298000", PhoneNumberType.Shipping)
                    .withCustomerData(customer)
                    .withBNPLShippingMethod(BNPLShippingMethod.DELIVERY)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields: payer.contact_phone.country_code.", ex.getMessage());
            assertEquals("40251", ex.getResponseText());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void BNPL_MissingProductId() throws ApiException {
        Customer customer = generateCustomerData();
        ArrayList<Product> products = generateProducts();
        products.get(0).setProductId(null);

        boolean exceptionCaught = false;
        try {
            paymentMethod
                    .authorize(amount)
                    .withCurrency(currency)
                    .withMiscProductData(products)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withAddress(billingAddress, AddressType.Billing)
                    .withPhoneNumber("1", "7708298000", PhoneNumberType.Shipping)
                    .withCustomerData(customer)
                    .withBNPLShippingMethod(BNPLShippingMethod.DELIVERY)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields: order.items[0].reference.", ex.getMessage());
            assertEquals("40251", ex.getResponseText());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void BNPL_MissingProductDescription() throws ApiException {
        Customer customer = generateCustomerData();
        ArrayList<Product> products = generateProducts();
        products.get(0).setDescription(null);

        boolean exceptionCaught = false;
        try {
            paymentMethod
                    .authorize(amount)
                    .withCurrency(currency)
                    .withMiscProductData(products)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withAddress(billingAddress, AddressType.Billing)
                    .withPhoneNumber("1", "7708298000", PhoneNumberType.Shipping)
                    .withCustomerData(customer)
                    .withBNPLShippingMethod(BNPLShippingMethod.DELIVERY)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields: order.items[0].description.", ex.getMessage());
            assertEquals("40251", ex.getResponseText());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void BNPL_ZeroProductQuantity() throws ApiException {
        Customer customer = generateCustomerData();
        ArrayList<Product> products = generateProducts();
        products.get(0).setQuantity(null);

        boolean exceptionCaught = false;
        try {
            paymentMethod
                    .authorize(amount)
                    .withCurrency(currency)
                    .withMiscProductData(products)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withAddress(billingAddress, AddressType.Billing)
                    .withPhoneNumber("1", "7708298000", PhoneNumberType.Shipping)
                    .withCustomerData(customer)
                    .withBNPLShippingMethod(BNPLShippingMethod.DELIVERY)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 502 - Processor System error", ex.getMessage());
            assertEquals("50143", ex.getResponseText());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void BNPL_MissingProductUrl() throws ApiException {
        Customer customer = generateCustomerData();
        ArrayList<Product> products = generateProducts();
        products.get(0).setUrl(null);

        boolean exceptionCaught = false;
        try {
            paymentMethod
                    .authorize(amount)
                    .withCurrency(currency)
                    .withMiscProductData(products)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withAddress(billingAddress, AddressType.Billing)
                    .withPhoneNumber("1", "7708298000", PhoneNumberType.Shipping)
                    .withCustomerData(customer)
                    .withBNPLShippingMethod(BNPLShippingMethod.DELIVERY)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields: order.items[0].product_url.", ex.getMessage());
            assertEquals("40251", ex.getResponseText());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void BNPL_MissingProductImageUrl() throws ApiException {
        Customer customer = generateCustomerData();
        ArrayList<Product> products = generateProducts();
        products.get(0).setImageUrl(null);

        boolean exceptionCaught = false;
        try {
            paymentMethod
                    .authorize(amount)
                    .withCurrency(currency)
                    .withMiscProductData(products)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withAddress(billingAddress, AddressType.Billing)
                    .withPhoneNumber("1", "7708298000", PhoneNumberType.Shipping)
                    .withCustomerData(customer)
                    .withBNPLShippingMethod(BNPLShippingMethod.DELIVERY)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields: order.items[0].product_image_url.", ex.getMessage());
            assertEquals("40251", ex.getResponseText());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    private Customer generateCustomerData() {
        Customer customer = new Customer();
        customer.setKey("12345678");
        //customer.setId("12345678");     // GenerationUtils.generateOrderId();
        customer.setFirstName("James");
        customer.setLastName("Mason");
        customer.setEmail("james.mason@example.com");

        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setCountryCode("1");
        phoneNumber.setNumber("7708298000");
        phoneNumber.setAreaCode(PhoneNumberType.Home.toString());

        customer.setPhone(phoneNumber);

        List<CustomerDocument> documents = new ArrayList<>();

        documents.add(
                new CustomerDocument()
                        .setReference("123456789")
                        .setIssuer("US")
                        .setType(CustomerDocumentType.PASSPORT));

        customer.setDocuments(documents);

        return customer;
    }

    private ArrayList<Product> generateProducts() {
        ArrayList<Product> products = new ArrayList<>();

        Product product =
                new Product()
                        .setProductId("92ebf294-f3ef-4aba-af30-6ebaf747de8f")
                        .setProductName("iPhone 13")
                        .setDescription("iPhone 13")
                        .setQuantity(1)
                        .setUnitPrice(new BigDecimal(550))
                        .setTaxAmount(new BigDecimal(1))
                        .setDiscountAmount(new BigDecimal(0))
                        .setTaxPercentage(new BigDecimal(0))
                        .setNetUnitAmount(new BigDecimal(550))
                        .setGiftCardCurrency(currency)
                        .setUrl("https://www.example.com/iphone.html")
                        .setImageUrl("https://www.example.com/iphone.png");

        products.add(product);

        return products;
    }

}
package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.paymentMethods.BNPL;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.ReportingService;
import com.global.api.utils.StringUtils;
import lombok.var;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.*;

public class GpApiBNPLTest extends BaseGpApiTest {
    private BNPL paymentMethod;
    private String currency;
    private Address shippingAddress;
    private Address billingAddress;

    public GpApiBNPLTest() throws ApiException {
        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId("uAGII1ChGyRk1CqzJBsOOGBTrDMMYjAp")
                .setAppKey("hgLnF6Fh7BIt3TDw")
                .setChannel(Channel.CardNotPresent.getValue());

        config.setEnableLogging(true);

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

        currency = "USD";

        // billing address
        billingAddress = new Address();
        billingAddress.setStreetAddress1("10 Glenlake Pkwy NE");
        billingAddress.setStreetAddress2("no");
        billingAddress.setCity("Birmingham");
        billingAddress.setPostalCode("50001");
        billingAddress.setCountryCode("US");
        billingAddress.setState("IL");

        // shipping address
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
        var customer = generateCustomerData();
        var products = generateProducts();

        var response =
                paymentMethod
                        .authorize(10)
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

        var captured =
                response
                        .capture()
                        .execute();

        assertNotNull(captured);
        assertEquals("SUCCESS", captured.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), captured.getResponseMessage());

        var refund =
                captured
                        .refund(5)
                        .withCurrency(currency)
                        .execute();

        assertNotNull(refund);
        assertEquals("SUCCESS", refund.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), refund.getResponseMessage());
    }

    @Test
    public void FullRefund() throws ApiException {
        var response =
                ReportingService
                        .findTransactionsPaged(1, 10)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Ascending)
                        .where(SearchCriteria.PaymentMethodName, PaymentMethodName.BNPL)
                        .and(SearchCriteria.TransactionStatus, TransactionStatus.Captured)
                        .and(SearchCriteria.PaymentType, PaymentType.Sale)
                        .execute();

        assertNotNull(response);
        assertTrue(response.getResults().size() > 0);

        var trnSummary = response.getResults().get(new Random().nextInt(response.getResults().size()));
        var trn = Transaction.fromId(trnSummary.getTransactionId(), trnSummary.getPaymentType());

        var trnRefund =
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
        var customer = generateCustomerData();
        var products = generateProducts();

        var transaction =
                paymentMethod
                        .authorize(550)
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

        var captureTrn =
                transaction
                        .capture()
                        .execute();

        assertNotNull(captureTrn);
        assertEquals("SUCCESS", captureTrn.getResponseCode());
        assertEquals(TransactionStatus.Captured.toString().toUpperCase(), captureTrn.getResponseMessage());

        Thread.sleep(15000);

        var trnRefund =
                captureTrn
                        .refund(100)
                        .withCurrency(currency)
                        .execute();

        assertNotNull(trnRefund);
        assertEquals("SUCCESS", trnRefund.getResponseCode());
        assertEquals(TransactionStatus.Captured.toString().toUpperCase(), trnRefund.getResponseMessage());
    }

    @Test
    public void BNPL_MultipleRefund() throws ApiException, InterruptedException {
        var customer = generateCustomerData();
        var products = generateProducts();

        var transaction =
                paymentMethod
                        .authorize(550)
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

        var captureTrn =
                transaction
                        .capture()
                        .execute();

        assertNotNull(captureTrn);
        assertEquals("SUCCESS", captureTrn.getResponseCode());
        assertEquals(TransactionStatus.Captured.toString().toUpperCase(), captureTrn.getResponseMessage());

        Thread.sleep(60000);

        var trnRefund =
                captureTrn
                        .refund(100)
                        .withCurrency(currency)
                        .execute();

        assertNotNull(trnRefund);
        assertEquals("SUCCESS", trnRefund.getResponseCode());
        assertEquals(TransactionStatus.Captured.toString().toUpperCase(), trnRefund.getResponseMessage());

        trnRefund =
                captureTrn
                        .refund(100)
                        .withCurrency(currency)
                        .execute();

        assertNotNull(trnRefund);
        assertEquals("SUCCESS", trnRefund.getResponseCode());
        assertEquals(TransactionStatus.Captured.toString().toUpperCase(), trnRefund.getResponseMessage());
    }

    @Test
    public void BNPL_Reverse() throws ApiException, InterruptedException {
        var customer = generateCustomerData();
        var products = generateProducts();

        var transaction =
                paymentMethod
                        .authorize(550)
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

        var captureTrn =
                transaction
                        .reverse()
                        .execute();

        assertNotNull(captureTrn);
        assertEquals("SUCCESS", captureTrn.getResponseCode());
        assertEquals(TransactionStatus.Reversed.toString().toUpperCase(), captureTrn.getResponseMessage());
    }

    @Test
    public void BNPL_OnlyMandatory() throws ApiException {
        var customer = generateCustomerData();
        var products = generateProducts();

        var transaction =
                paymentMethod
                        .authorize(550)
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
        var customer = generateCustomerData();
        var products = generateProducts();

        var transaction =
                paymentMethod
                        .authorize(550)
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

        var captureTrn=
                transaction
                        .capture()
                        .execute();

        assertNotNull(captureTrn);
        assertEquals("SUCCESS", captureTrn.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), captureTrn.getResponseMessage());
    }

    @Test
    public void BNPL_ClearPayProvider() throws ApiException {
        paymentMethod.BNPLType = BNPLType.CLEARPAY;
        var customer = generateCustomerData();
        var products = generateProducts();

        var transaction =
                paymentMethod
                        .authorize(550)
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
    public void BNPL_ClearPayProvider_PartialCapture() throws ApiException, InterruptedException {
        paymentMethod.BNPLType = BNPLType.CLEARPAY;
        var customer = generateCustomerData();
        var products = generateProducts();

        var transaction =
                paymentMethod
                        .authorize(550)
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

        var captureTrn =
                transaction
                        .capture(100)
                        .execute();

        assertNotNull(captureTrn);
        assertEquals("SUCCESS", captureTrn.getResponseCode());
        assertEquals(TransactionStatus.Captured.toString().toUpperCase(), captureTrn.getResponseMessage());
    }

    @Test
    public void BNPL_ClearPayProvider_MultipleCapture() throws ApiException, InterruptedException {
        paymentMethod.BNPLType = BNPLType.CLEARPAY;
        var customer = generateCustomerData();
        var products = generateProducts();

        var transaction =
                paymentMethod
                        .authorize(550)
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

        var captureTrn =
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
    }

    @Test
    public void BNPL_InvalidStatusForCapture_NoRedirect() throws ApiException {
        var customer = generateCustomerData();
        var products = generateProducts();

        var transaction =
                paymentMethod
                        .authorize(550)
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
        var id = "TRN_EryDeQRtqagH27G87DkSfZGL1kiE21";

        var trnInfo =
                ReportingService
                        .transactionDetail(id)
                        .execute();

        assertEquals(id, trnInfo.getTransactionId());
    }

    @Test
    public void GetBNPLTransactionById_RandomTransactionId() throws ApiException {
        var transactionId = UUID.randomUUID().toString();

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
        var customer = generateCustomerData();

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
        var customer = generateCustomerData();
        var products = generateProducts();

        boolean exceptionCaught = false;
        try {
            paymentMethod
                    .authorize(10)
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
        var products = generateProducts();

        boolean exceptionCaught = false;
        try {
            paymentMethod
                    .authorize(10)
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
        var customer = generateCustomerData();
        var products = generateProducts();

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
        var customer = generateCustomerData();
        var products = generateProducts();

        boolean exceptionCaught = false;
        try {
            paymentMethod
                    .authorize(10)
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
        var customer = generateCustomerData();
        customer.setEmail(null);
        var products = generateProducts();

        boolean exceptionCaught = false;
        try {
            paymentMethod
                    .authorize(10)
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
        var customer = generateCustomerData();
        customer.setPhone(null);
        var products = generateProducts();

        boolean exceptionCaught = false;
        try {
            paymentMethod
                    .authorize(10)
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
        var customer = generateCustomerData();
        var products = generateProducts();
        products.get(0).setProductId(null);

        boolean exceptionCaught = false;
        try {
            paymentMethod
                    .authorize(10)
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
        var customer = generateCustomerData();
        var products = generateProducts();
        products.get(0).setDescription(null);

        boolean exceptionCaught = false;
        try {
            paymentMethod
                    .authorize(10)
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
    public void BNPL_MissingProductQuantity() throws ApiException {
        var customer = generateCustomerData();
        var products = generateProducts();
        products.get(0).setQuantity(null);

        boolean exceptionCaught = false;
        try {
            paymentMethod
                    .authorize(10)
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
            assertEquals("Status Code: 400 - Request expects the following fields: order.items[0].quantity.", ex.getMessage());
            assertEquals("40251", ex.getResponseText());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void BNPL_MissingProductUrl() throws ApiException {
        var customer = generateCustomerData();
        var products = generateProducts();
        products.get(0).setUrl(null);

        boolean exceptionCaught = false;
        try {
            paymentMethod
                    .authorize(10)
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
        var customer = generateCustomerData();
        var products = generateProducts();
        products.get(0).setImageUrl(null);

        boolean exceptionCaught = false;
        try {
            paymentMethod
                    .authorize(10)
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
        var customer = new Customer();
        customer.setId("12345678");     // GenerationUtils.generateOrderId();
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

        var product =
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
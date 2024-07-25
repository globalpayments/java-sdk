package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.paymentMethods.BNPL;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.RecurringPaymentMethod;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.utils.GenerationUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

public class GpApiPayerTest extends BaseGpApiTest {

    private Customer newCustomer;
    private CreditCardData card;
    private Address billingAddress;
    private Address shippingAddress;

    @Before
    public void initialize() throws Exception {
        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        ServicesContainer.configureService(config);

        newCustomer = new Customer();
        newCustomer.setKey(GenerationUtils.generateOrderId());
        newCustomer.setFirstName("James");
        newCustomer.setLastName("Mason");

        card = new CreditCardData();
        card.setNumber("4263970000005262");
        card.setExpMonth(Integer.valueOf(expMonth));
        card.setExpYear(Integer.valueOf(expYear));
        card.setCvn("131");
        card.setCardHolderName("James Mason");

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

    @After
    public void removeConfig() throws ConfigurationException {
        ServicesContainer.removeConfig();
    }

    @Test
    public void createPayer() throws Exception {
        String tokenizedResponse = card.tokenize();
        card.setToken(tokenizedResponse);

        List<RecurringPaymentMethod> paymentMethods = new ArrayList<>();
        paymentMethods.add(newCustomer.addPaymentMethod(tokenizedResponse, card));

        newCustomer.setPaymentMethods(paymentMethods);

        CreditCardData card2 = new CreditCardData();
        card2.setNumber("4012001038488884");
        card2.setExpMonth(Integer.valueOf(expMonth));
        card2.setExpYear(Integer.valueOf(expYear));
        card2.setCvn("131");
        card2.setCardHolderName("James Mason");

        String tokenizedResponse2 = card2.tokenize();
        card2.setToken(tokenizedResponse2);
        Assert.assertNotNull(tokenizedResponse2);

        paymentMethods.add(newCustomer.addPaymentMethod(card2.getToken(), card2));
        newCustomer.setPaymentMethods(paymentMethods);
        Customer payer = newCustomer.create();
        Assert.assertNotNull(payer.getId());
        Assert.assertEquals(newCustomer.getFirstName(), payer.getFirstName());
        Assert.assertEquals(newCustomer.getLastName(), payer.getLastName());
        Assert.assertNotNull(payer.getPaymentMethods());
        for (RecurringPaymentMethod paymentMethod : payer.getPaymentMethods()) {
            String[] tokensArray = {card2.getToken(), card.getToken()};
            List<String> tokensList = Arrays.asList(tokensArray);
            Assert.assertTrue(tokensList.contains(paymentMethod.getId()));
        }
    }

    @Test
    public void createPayerWithoutPaymentMethods() throws ApiException {
        Customer payer = newCustomer.create();

        Assert.assertNotNull(payer.getId());
        Assert.assertEquals(newCustomer.getFirstName(), payer.getFirstName());
        Assert.assertEquals(newCustomer.getLastName(), payer.getLastName());
        Assert.assertNull(payer.getPaymentMethods());
    }

    @Test
    public void createPayerWithoutFirstName() throws ApiException {
        newCustomer = new Customer();
        newCustomer.setKey(GenerationUtils.generateOrderId());
        newCustomer.setLastName("Mason");

        boolean exceptionCaught = false;
        try {
            newCustomer.create();
        } catch (GatewayException e) {
            exceptionCaught = true;
            Assert.assertEquals("Status Code: 400 - Request expects the following fields: first_name", e.getMessage());
            Assert.assertEquals("40005", e.getResponseText());
        } finally {
            Assert.assertTrue(exceptionCaught);
        }
    }

    @Test
    public void createPayerWithoutLastName() throws ApiException {
        newCustomer = new Customer();
        newCustomer.setKey(GenerationUtils.generateOrderId());
        newCustomer.setFirstName("James");

        boolean exceptionCaught = false;
        try {
            newCustomer.create();
        } catch (GatewayException e) {
            exceptionCaught = true;
            Assert.assertEquals("Status Code: 400 - Request expects the following fields: last_name", e.getMessage());
            Assert.assertEquals("40005", e.getResponseText());
        } finally {
            Assert.assertTrue(exceptionCaught);
        }
    }

    @Test
    public void editPayer() throws ApiException {
        String key = "payer-123";
        String id = "PYR_df7aebe8e356430caf1a3f3b5a8eef71";
        newCustomer.setKey(key);
        newCustomer.setId(id);

        String tokenizeResponse = card.tokenize();
        Assert.assertNotNull(tokenizeResponse);

        card.setToken(tokenizeResponse);
        newCustomer.addPaymentMethod(tokenizeResponse, card);

        Customer payer = newCustomer.saveChanges();

        Assert.assertEquals(newCustomer.getKey(), payer.getKey());
        Assert.assertFalse(payer.getPaymentMethods().isEmpty());
        Assert.assertEquals(card.getToken(), payer.getPaymentMethods().get(0).getId());
    }

    @Test
    public void editPayerWithoutCustomerId() throws ApiException {
        String key = "payer-123";
        newCustomer.setKey(key);

        String tokenizeResponse = card.tokenize();
        Assert.assertNotNull(tokenizeResponse);

        card.setToken(tokenizeResponse);
        newCustomer.addPaymentMethod(tokenizeResponse, card);

        boolean exceptionCaught = false;
        try {
            newCustomer.saveChanges();
        } catch (ApiException e) {
            exceptionCaught = true;
            Assert.assertEquals("Status Code: 502 - Unable to process your request due to an error with a system down stream.", e.getCause().getMessage());
            Assert.assertEquals("50046", ((GatewayException) e.getCause()).getResponseText());
        } finally {
            Assert.assertTrue(exceptionCaught);
        }
    }

    @Test
    public void editPayerWithoutRandomId() throws ApiException {
        String key = "payer-123";
        String id = "PYR_" + UUID.randomUUID();
        newCustomer.setKey(key);
        newCustomer.setId(id);

        String tokenizeResponse = card.tokenize();
        Assert.assertNotNull(tokenizeResponse);

        card.setToken(tokenizeResponse);
        newCustomer.addPaymentMethod(tokenizeResponse, card);

        boolean exceptionCaught = false;
        try {
            newCustomer.saveChanges();
        } catch (ApiException e) {
            exceptionCaught = true;
            Assert.assertEquals("Status Code: 404 - Payer " + newCustomer.getId() + " not found at this location", e.getCause().getMessage());
            Assert.assertEquals("40008", ((GatewayException) e.getCause()).getResponseText());
        } finally {
            Assert.assertTrue(exceptionCaught);
        }
    }

    @Test
    public void bnplInitiateStep() throws ApiException {
        newCustomer.setEmail("james@example.com");
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setCountryCode("41");
        phoneNumber.setNumber("57774873");
        phoneNumber.setAreaCode(PhoneNumberType.Shipping.name());
        newCustomer.setPhone(phoneNumber);
        newCustomer.setKey("12345678");
        Customer payer = newCustomer.create();

        Product product = new Product();
        product.setProductId(GenerationUtils.generateOrderId());
        product.setProductName("iPhone 13");
        product.setDescription("iPhone 13");
        product.setQuantity(1);
        product.setUnitPrice(BigDecimal.valueOf(550));
        product.setNetUnitPrice(BigDecimal.valueOf(550));
        product.setTaxAmount(BigDecimal.valueOf(0));
        product.setDiscountAmount(BigDecimal.valueOf(0));
        product.setTaxPercentage(BigDecimal.valueOf(0));
        product.setUrl("https://www.example.com/iphone.html");
        product.setImageUrl("https://www.example.com/iphone.png");

        List<Product> products = Collections.singletonList(product);

        BNPL paymentMethod = new BNPL();
        paymentMethod.setBNPLType(BNPLType.AFFIRM);
        paymentMethod.setReturnUrl("https://7b8e82a17ac00346e91e984f42a2a5fb.m.pipedream.net");
        paymentMethod.setStatusUpdateUrl("https://7b8e82a17ac00346e91e984f42a2a5fb.m.pipedream.net");
        paymentMethod.setCancelUrl("https://7b8e82a17ac00346e91e984f42a2a5fb.m.pipedream.net");

        ArrayList<Product> newProducts = new ArrayList<>(products);

        Transaction transaction = paymentMethod.authorize(5.6)
                .withCurrency("USD")
                .withMiscProductData(newProducts)
                .withAddress(shippingAddress, AddressType.Shipping)
                .withAddress(billingAddress, AddressType.Billing)
                .withPhoneNumber("41", "57774873", PhoneNumberType.Shipping)
                .withCustomerData(payer)
                .withBNPLShippingMethod(BNPLShippingMethod.DELIVERY)
                .withOrderId("12365")
                .execute();

        Assert.assertNotNull(transaction);
        Assert.assertEquals("SUCCESS", transaction.getResponseCode());
        Assert.assertEquals(TransactionStatus.Initiated.toString().toUpperCase(), transaction.getResponseMessage());
        Assert.assertNotNull(transaction.getBNPLResponse().getRedirectUrl());
    }
}

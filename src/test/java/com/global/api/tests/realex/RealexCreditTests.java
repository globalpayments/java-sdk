package com.global.api.tests.realex;

import com.global.api.ServicesConfig;
import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.AddressType;
import com.global.api.entities.enums.RecurringSequence;
import com.global.api.entities.enums.RecurringType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.CreditCardData;
import org.junit.Test;

import java.math.BigDecimal;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

public class RealexCreditTests {
    private CreditCardData card;
    
    public RealexCreditTests() throws ApiException {
        ServicesConfig config = new ServicesConfig();
        config.setMerchantId("heartlandgpsandbox");
        config.setAccountId("api");
        config.setSharedSecret("secret");
        config.setRebatePassword("rebate");
        config.setRefundPassword("refund");
        config.setServiceUrl("https://api.sandbox.realexpayments.com/epage-remote.cgi");

        ServicesContainer.configure(config);

        card = new CreditCardData();
        card.setNumber("4111111111111111");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");
        card.setCardHolderName("Joe Smith");
    }

    @Test
    public void creditAuthorization() throws ApiException {
        Transaction authorization = card.authorize(new BigDecimal("14"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(authorization);
        assertEquals("00", authorization.getResponseCode());

        Transaction capture = authorization.capture(new BigDecimal("14"))
                .execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void creditSale() throws ApiException {
        Transaction response = card.charge(new BigDecimal("15"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSaleWithRecurring() throws ApiException {
        Transaction response = card.charge(new BigDecimal("15"))
                .withCurrency("USD")
                .withRecurringInfo(RecurringType.Fixed, RecurringSequence.First)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditRefund() throws ApiException {
        Transaction response = card.refund(new BigDecimal("16"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditRebate() throws ApiException {
        Transaction response = card.charge(new BigDecimal("17"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction rebate = response.refund(new BigDecimal("17"))
                .withCurrency("USD")
                .execute();
        assertNotNull(rebate);
        assertEquals("00", rebate.getResponseCode());
    }

    @Test
    public void creditVoid() throws ApiException {
        Transaction response = card.charge(new BigDecimal("15"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test
    public void creditVerify() throws ApiException {
        Transaction response = card.verify()
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    
    @Test
    public void creditFraudResponse() throws ApiException {
        Address billingAddress = new Address();
        billingAddress.setStreetAddress1("Flat 123");
        billingAddress.setStreetAddress2("House 456");
        billingAddress.setStreetAddress3("Cul-De-Sac");
        billingAddress.setCity("Halifax");
        billingAddress.setProvince("West Yorkshire");
        billingAddress.setState("Yorkshire and the Humber");
        billingAddress.setCountry("GB");
        billingAddress.setPostalCode("E77 4QJ");

        Address shippingAddress = new Address();
        shippingAddress.setStreetAddress1("House 456");
        shippingAddress.setStreetAddress2("987 The Street");
        shippingAddress.setStreetAddress3("Basement Flat");
        shippingAddress.setCity("Chicago");
        shippingAddress.setState("Illinois");
        shippingAddress.setProvince("Mid West");
        shippingAddress.setCountry("US");
        shippingAddress.setPostalCode("50001");

        Transaction fraudResponse = card.charge(new BigDecimal("199.99"))
                .withCurrency("EUR")
                .withAddress(billingAddress, AddressType.Billing)
                .withAddress(shippingAddress, AddressType.Shipping)
                .withProductId("SID9838383")
                .withClientTransactionId("Car Part HV")
                .withCustomerId("E8953893489")
                .withCustomerIpAddress("123.123.123.123")
                .execute();
        assertNotNull(fraudResponse);
        assertEquals("00", fraudResponse.getResponseCode());
    }

    @Test
    public void creditSale_GB_NoStreetAddress() throws ApiException {
        Address billingAddress = new Address();
        billingAddress.setCountry("GB");
        billingAddress.setPostalCode("E77 4QJ");

        Transaction response = card.charge(new BigDecimal("19.99"))
               .withCurrency("EUR")
               .withAddress(billingAddress, AddressType.Billing)
               .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSettleWithoutAmountCurrency() throws ApiException {
       Transaction response = card.authorize(new BigDecimal("99.99"))
               .withCurrency("EUR")
               .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        String orderId = response.getOrderId();
        String paymentsReference = response.getTransactionId();

        Transaction settle = Transaction.fromId(paymentsReference, orderId);

        Transaction responseSettle = settle.capture()
               .execute();
        assertNotNull(responseSettle);
        assertEquals(orderId, responseSettle.getOrderId());
        assertEquals("00", responseSettle.getResponseCode());
        assertEquals("000000", responseSettle.getAuthorizationCode());
    }
}
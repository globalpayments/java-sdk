package com.global.api.tests.realex;

import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.serviceConfigs.GatewayConfig;
import com.global.api.serviceConfigs.HostedPaymentConfig;
import com.global.api.services.HostedService;
import com.global.api.tests.JsonComparator;
import com.global.api.tests.realex.hpp.RealexHppClient;
import com.global.api.utils.JsonDoc;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class RealexHppRequestTests {
    private HostedService _service;
    private RealexHppClient _client;
    private Address billingAddress;
    private Address shippingAddress;

    public RealexHppRequestTests() throws ApiException {
        _client = new RealexHppClient("https://pay.sandbox.realexpayments.com/pay", "secret");

        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setLanguage("GB");
        hostedConfig.setResponseUrl("http://requestb.in/10q2bjb1");

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("heartlandgpsandbox");
        config.setAccountId("hpp");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://api.sandbox.realexpayments.com/epage-remote.cgi");
        config.setHostedPaymentConfig(hostedConfig);

        _service = new HostedService(config);

        // billing address
        billingAddress = new Address();
        billingAddress.setStreetAddress1("Flat 123");
        billingAddress.setStreetAddress2("House 456");
        billingAddress.setPostalCode("50001");
        billingAddress.setCountry("US");

        // shipping address
        shippingAddress = new Address();
        shippingAddress.setStreetAddress1("Flat 456");
        shippingAddress.setStreetAddress2("House 123");
        shippingAddress.setPostalCode("WB3 A21");
        shippingAddress.setCountry("GB");
    }

    @Test
    public void creditAuth() throws ApiException {
        Address address = new Address();
        address.setPostalCode("123|56");
        address.setCountry("IRELAND");
        
        String json = _service.authorize(new BigDecimal("1"))
                .withCurrency("EUR")
                .withCustomerId("123456")
                .withAddress(address).serialize();
        assertNotNull(json);

        String response = _client.sendRequest(json);
        Transaction parsedResponse = _service.parseResponse(response, true);
        assertNotNull(response);
        assertEquals("00", parsedResponse.getResponseCode());
    }

    @Test
    public void creditSale() throws ApiException {
        Address address = new Address();
        address.setPostalCode("123|56");
        address.setCountry("IRELAND");

        String json = _service.charge(new BigDecimal("1"))
                .withCurrency("EUR")
                .withCustomerId("123456")
                .withAddress(address).serialize();
        assertNotNull(json);

        String response = _client.sendRequest(json);
        Transaction parsedResponse = _service.parseResponse(response, true);
        assertNotNull(response);
        assertEquals("00", parsedResponse.getResponseCode());
    }

    @Test
    public void creditVerify() throws ApiException {
        Address address = new Address();
        address.setPostalCode("123|56");
        address.setCountry("IRELAND");

        String json = _service.verify()
                .withCurrency("EUR")
                .withCustomerId("123456")
                .withAddress(address).serialize();
        assertNotNull(json);

        String response = _client.sendRequest(json);
        Transaction parsedResponse = _service.parseResponse(response, true);
        assertNotNull(response);
        assertEquals("00", parsedResponse.getResponseCode());
    }

    @Test
    public void creditVerify_3DS() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setVersion(HppVersion.Version2);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("heartlandgpsandbox");
        config.setAccountId("3dsecure");
        config.setSharedSecret("secret");
        config.setHostedPaymentConfig(hostedConfig);

        HostedPaymentData testHostedPaymentData = new HostedPaymentData();
        testHostedPaymentData.setCustomerEmail("james.mason@example.com");
        testHostedPaymentData.setCustomerPhoneMobile("44|07123456789");
        testHostedPaymentData.setAddressesMatch(false);
        testHostedPaymentData.setCustomerCountry("GB");
        testHostedPaymentData.setCustomerFirstName("Jason");
        testHostedPaymentData.setCustomerLastName("Mason");
        testHostedPaymentData.setMerchantResponseUrl("http://requestb.in/10q2bjb1");
        testHostedPaymentData.setTransactionStatusUrl("http://requestb.in/10q2bjb1");

        _service = new HostedService(config, "3ds");

        String json = _service.verify(new BigDecimal("0"))
                .withCurrency("EUR")
                .withCustomerId("123456")
                .withAddress(billingAddress, AddressType.Billing)
                .withAddress(shippingAddress, AddressType.Shipping)
                .withHostedPaymentData(testHostedPaymentData)
                .serialize("3ds");
        assertNotNull(json);

        String response = _client.sendRequest(json);
        Transaction parsedResponse = _service.parseResponse(response, true, "3ds");
        assertNotNull(response);
        assertEquals("00", parsedResponse.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void authNoAmount() throws ApiException {
        _service.authorize(null).withCurrency("USD").serialize();
    }

    @Test(expected = BuilderException.class)
    public void authNoCurrency() throws ApiException {
        _service.authorize(new BigDecimal("10")).serialize();
    }

    @Test(expected = BuilderException.class)
    public void saleNoAmount() throws ApiException {
        _service.charge(null).withCurrency("USD").serialize();
    }

    @Test(expected = BuilderException.class)
    public void saleNoCurrency() throws ApiException {
        _service.charge(new BigDecimal("10")).serialize();
    }

    @Test(expected = BuilderException.class)
    public void verifyNoCurrency() throws ApiException {
        _service.verify().serialize();
    }

    @Test(expected = BuilderException.class)
    public void verifyWithAmount() throws ApiException {
        _service.verify().withAmount(new BigDecimal("10")).serialize();
    }

    @Test
    public void basicAuth() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);
        
        HostedService service = new HostedService(config);

        String hppJson = service.authorize(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .serialize();
        
        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"061609f85a8e0191dc7f487f8278e71898a2ee2d\", \"AUTO_SETTLE_FLAG\": \"0\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\"}";
        
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void basicCharge() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);

        HostedService service = new HostedService(config);

        String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .serialize();
        
        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"061609f85a8e0191dc7f487f8278e71898a2ee2d\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\"}";
        
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    // testing COMMENT1, CUST_NUM, PROD_ID, VAR_REF, HPP_LANG, CARD_PAYMENT_BUTTON
    public void basicHostedPaymentData() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setLanguage("EN");
        hostedConfig.setPaymentButtonText("Place Order");

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);
        
        HostedService service = new HostedService(config);

        HostedPaymentData testHostedPaymentData = new HostedPaymentData();
        testHostedPaymentData.setCustomerNumber("a028774f-beff-47bc-bd6e-ed7e04f5d758a028774f-btefa");
        testHostedPaymentData.setProductId("a0b38df5-b23c-4d82-88fe-2e9c47438972-b23c-4d82-88f");

       String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .withHostedPaymentData(testHostedPaymentData)
                .withDescription("Mobile Channel")
                .withClientTransactionId("My Legal Entity")
                .serialize();

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"061609f85a8e0191dc7f487f8278e71898a2ee2d\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"CUST_NUM\": \"a028774f-beff-47bc-bd6e-ed7e04f5d758a028774f-btefa\", \"PROD_ID\": \"a0b38df5-b23c-4d82-88fe-2e9c47438972-b23c-4d82-88f\", \"COMMENT1\": \"Mobile Channel\", \"HPP_LANG\": \"EN\", \"CARD_PAYMENT_BUTTON\": \"Place Order\", \"VAR_REF\": \"My Legal Entity\", \"PAYER_EXIST\": \"1\"}";

        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void storeCardNewCustomerNoRefs() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setCardStorageEnabled(true);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);

        HostedService service = new HostedService(config);

        HostedPaymentData testHostedPaymentData = new HostedPaymentData();
        testHostedPaymentData.setCustomerExists(false);
        testHostedPaymentData.setOfferToSaveCard(true);

       String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .withHostedPaymentData(testHostedPaymentData)
                .serialize();

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"7116c49826367c6513efdc0cc81e243b8095d78f\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"CARD_STORAGE_ENABLE\": \"1\", \"OFFER_SAVE_CARD\": \"1\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void storeCardNewCustomerJustPayerRef() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setCardStorageEnabled(true);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);

        HostedService service = new HostedService(config);

        HostedPaymentData testHostedPaymentData = new HostedPaymentData();
        testHostedPaymentData.setCustomerExists(false);
        testHostedPaymentData.setOfferToSaveCard(true);
        testHostedPaymentData.setCustomerKey("376a2598-412d-4805-9f47-c177d5605853");

       String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .withHostedPaymentData(testHostedPaymentData)
                .serialize();

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"4dcf4e5e2d43855fe31cdc097e985a895868563e\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"CARD_STORAGE_ENABLE\": \"1\", \"OFFER_SAVE_CARD\": \"1\", \"PAYER_REF\": \"376a2598-412d-4805-9f47-c177d5605853\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void storeCardNewCustomerJustPaymentRef() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setCardStorageEnabled(true);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);
        
        HostedService service = new HostedService(config);

        HostedPaymentData testHostedPaymentData = new HostedPaymentData();
        testHostedPaymentData.setCustomerExists(false);
        testHostedPaymentData.setOfferToSaveCard(true);
        testHostedPaymentData.setPaymentKey("ca46344d-4292-47dc-9ced-e8a42ce66977");

       String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .withHostedPaymentData(testHostedPaymentData)
                .serialize();

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"5fe76a45585d9793fd162ab8a3cd4a42991417df\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"CARD_STORAGE_ENABLE\": \"1\", \"OFFER_SAVE_CARD\": \"1\", \"PMT_REF\": \"ca46344d-4292-47dc-9ced-e8a42ce66977\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void storeCardNewCustomerAllSuppliedRefs() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setCardStorageEnabled(true);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);
        
        HostedService service = new HostedService(config);

        HostedPaymentData testHostedPaymentData = new HostedPaymentData();
        testHostedPaymentData.setCustomerExists(false);
        testHostedPaymentData.setOfferToSaveCard(true);
        testHostedPaymentData.setCustomerKey("376a2598-412d-4805-9f47-c177d5605853");
        testHostedPaymentData.setPaymentKey("ca46344d-4292-47dc-9ced-e8a42ce66977");

       String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .withHostedPaymentData(testHostedPaymentData)
                .serialize();

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"f0cf097fe769a6a5a6254eee631e51709ba34c90\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"CARD_STORAGE_ENABLE\": \"1\", \"OFFER_SAVE_CARD\": \"1\", \"PMT_REF\": \"ca46344d-4292-47dc-9ced-e8a42ce66977\", \"PAYER_REF\": \"376a2598-412d-4805-9f47-c177d5605853\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void storeCardAutoNewCustomerNoRefs() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig .setResponseUrl("https://www.example.com/response");
        hostedConfig .setVersion(HppVersion.Version2);
        hostedConfig .setCardStorageEnabled(true);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);
        
        HostedService service = new HostedService(config);

        HostedPaymentData testHostedPaymentData = new HostedPaymentData();
        testHostedPaymentData.setCustomerExists(false);
        testHostedPaymentData.setOfferToSaveCard(false);

       String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .withHostedPaymentData(testHostedPaymentData)
                .serialize();

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"7116c49826367c6513efdc0cc81e243b8095d78f\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"CARD_STORAGE_ENABLE\": \"1\", \"OFFER_SAVE_CARD\": \"0\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void storeCardReturnCustomerNoPaymentRef() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setCardStorageEnabled(true);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);
        
        HostedService service = new HostedService(config);

        HostedPaymentData testHostedPaymentData = new HostedPaymentData();
        testHostedPaymentData.setCustomerExists(true);
        testHostedPaymentData.setOfferToSaveCard(true);
        testHostedPaymentData.setCustomerKey("376a2598-412d-4805-9f47-c177d5605853");

       String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .withHostedPaymentData(testHostedPaymentData)
                .serialize();

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"4dcf4e5e2d43855fe31cdc097e985a895868563e\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"CARD_STORAGE_ENABLE\": \"1\", \"OFFER_SAVE_CARD\": \"1\", \"PAYER_EXIST\": \"1\", \"PAYER_REF\": \"376a2598-412d-4805-9f47-c177d5605853\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void storeCardReturnCustomerWithPaymentRef() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setCardStorageEnabled(true);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);
        
        HostedService service = new HostedService(config);

        HostedPaymentData testHostedPaymentData = new HostedPaymentData();
        testHostedPaymentData.setCustomerExists(true);
        testHostedPaymentData.setOfferToSaveCard(true);
        testHostedPaymentData.setPaymentKey("ca46344d-4292-47dc-9ced-e8a42ce66977");

       String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .withHostedPaymentData(testHostedPaymentData)
                .serialize();

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"5fe76a45585d9793fd162ab8a3cd4a42991417df\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"CARD_STORAGE_ENABLE\": \"1\", \"OFFER_SAVE_CARD\": \"1\", \"PAYER_EXIST\": \"1\", \"PMT_REF\": \"ca46344d-4292-47dc-9ced-e8a42ce66977\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void storeCardAutoReturnCustomerAllRefs() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setCardStorageEnabled(true);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);
        
        HostedService service = new HostedService(config);

        HostedPaymentData testHostedPaymentData = new HostedPaymentData();
        testHostedPaymentData.setCustomerExists(true);
        testHostedPaymentData.setOfferToSaveCard(false);
        testHostedPaymentData.setCustomerKey("376a2598-412d-4805-9f47-c177d5605853");
        testHostedPaymentData.setPaymentKey("ca46344d-4292-47dc-9ced-e8a42ce66977");

       String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .withHostedPaymentData(testHostedPaymentData)
                .serialize();

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"f0cf097fe769a6a5a6254eee631e51709ba34c90\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"CARD_STORAGE_ENABLE\": \"1\", \"OFFER_SAVE_CARD\": \"0\", \"PAYER_EXIST\": \"1\", \"PMT_REF\": \"ca46344d-4292-47dc-9ced-e8a42ce66977\", \"PAYER_REF\": \"376a2598-412d-4805-9f47-c177d5605853\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void displayStoredCardsOfferSaveNoPaymentRef() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setDisplaySavedCards(true);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);
        
        HostedService service = new HostedService(config);

        HostedPaymentData testHostedPaymentData = new HostedPaymentData();
        testHostedPaymentData.setCustomerExists(true);
        testHostedPaymentData.setOfferToSaveCard(true);
        testHostedPaymentData.setCustomerKey("376a2598-412d-4805-9f47-c177d5605853");

       String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .withHostedPaymentData(testHostedPaymentData)
                .serialize();

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"4dcf4e5e2d43855fe31cdc097e985a895868563e\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"HPP_SELECT_STORED_CARD\": \"376a2598-412d-4805-9f47-c177d5605853\", \"OFFER_SAVE_CARD\": \"1\", \"PAYER_EXIST\": \"1\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void displayStoredCardsOfferSaveWithPaymentRef() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setDisplaySavedCards(true);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);
        
        HostedService service = new HostedService(config);

        HostedPaymentData testHostedPaymentData = new HostedPaymentData();
        testHostedPaymentData.setCustomerExists(true);
        testHostedPaymentData.setOfferToSaveCard(true);
        testHostedPaymentData.setCustomerKey("376a2598-412d-4805-9f47-c177d5605853");
        testHostedPaymentData.setPaymentKey("ca46344d-4292-47dc-9ced-e8a42ce66977");

       String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .withHostedPaymentData(testHostedPaymentData)
                .serialize();

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"f0cf097fe769a6a5a6254eee631e51709ba34c90\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"HPP_SELECT_STORED_CARD\": \"376a2598-412d-4805-9f47-c177d5605853\", \"OFFER_SAVE_CARD\": \"1\", \"PAYER_EXIST\": \"1\", \"PMT_REF\": \"ca46344d-4292-47dc-9ced-e8a42ce66977\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void billingData() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);
        
        HostedService service = new HostedService(config);

        Address billingAddress = new Address();
        billingAddress.setCountry("US");
        billingAddress.setPostalCode("50001");

       String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .withAddress(billingAddress, AddressType.Billing)
                .serialize();

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"061609f85a8e0191dc7f487f8278e71898a2ee2d\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"BILLING_CODE\": \"50001\", \"BILLING_CO\": \"US\", \"HPP_BILLING_POSTALCODE\": \"50001\", \"HPP_BILLING_COUNTRY\":\"840\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void shippingData() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);

        HostedService service = new HostedService(config);

        Address shippingAddress = new Address();
        shippingAddress.setCountry("US");
        shippingAddress.setPostalCode("50001");

       String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .withAddress(shippingAddress, AddressType.Shipping)
                .serialize();

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"061609f85a8e0191dc7f487f8278e71898a2ee2d\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"SHIPPING_CODE\": \"50001\", \"SHIPPING_CO\": \"US\", \"HPP_SHIPPING_COUNTRY\":\"840\", \"HPP_SHIPPING_POSTALCODE\":\"50001\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void billingAndShippingData() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);

        HostedService service = new HostedService(config);

        Address billingAddress = new Address();
        billingAddress.setCountry("US");
        billingAddress.setPostalCode("50001");

        Address shippingAddress = new Address();
        shippingAddress.setCountry("GB");
        shippingAddress.setPostalCode("654|123");

       String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .withAddress(billingAddress, AddressType.Billing)
                .withAddress(shippingAddress, AddressType.Shipping)
                .serialize();

        String expectedJson = "{\"CURRENCY\":\"EUR\",\"HPP_SHIPPING_COUNTRY\":\"826\",\"SHIPPING_CODE\":\"654123\",\"MERCHANT_RESPONSE_URL\":\"https://www.example.com/response\",\"HPP_SHIPPING_POSTALCODE\":\"654|123\",\"HPP_BILLING_POSTALCODE\":\"50001\",\"HPP_BILLING_COUNTRY\":\"840\",\"AUTO_SETTLE_FLAG\":\"1\",\"BILLING_CODE\":\"50001\",\"ACCOUNT\":\"internet\",\"SHA1HASH\":\"061609f85a8e0191dc7f487f8278e71898a2ee2d\",\"ORDER_ID\":\"GTI5Yxb0SumL_TkDMCAxQA\",\"BILLING_CO\":\"US\",\"AMOUNT\":\"1999\",\"TIMESTAMP\":\"20170725154824\",\"HPP_VERSION\":\"2\",\"SHIPPING_CO\":\"GB\",\"MERCHANT_ID\":\"MerchantId\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void fraudFilterPassive() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setFraudFilterMode(FraudFilterMode.Passive);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);
        
        HostedService service = new HostedService(config);

       String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .serialize();

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"fff944d4da9a5dfd64d142448d5dcf6168b3b77f\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"HPP_FRAUDFILTER_MODE\": \"PASSIVE\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void fraudFilterOff() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setFraudFilterMode(FraudFilterMode.Off);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);
        
        HostedService service = new HostedService(config);

       String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .serialize();

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"2e98407a26f17dc8c7ed89df5cc69d17718bfeb2\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"HPP_FRAUDFILTER_MODE\": \"OFF\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void fraudFilterNone() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setFraudFilterMode(FraudFilterMode.None);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);

        HostedService service = new HostedService(config);

       String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .serialize();

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"061609f85a8e0191dc7f487f8278e71898a2ee2d\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void fraudFilterActive() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setFraudFilterMode(FraudFilterMode.Active);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);

        HostedService service = new HostedService(config);

        String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .serialize();

        String expectedJson = "{\"MERCHANT_ID\":\"MerchantId\",\"ACCOUNT\":\"internet\",\"ORDER_ID\":\"GTI5Yxb0SumL_TkDMCAxQA\",\"CURRENCY\":\"EUR\",\"AUTO_SETTLE_FLAG\":\"1\",\"SHA1HASH\":\"89ed337102ebe5992f022cf3194c4f28d6bcfa0e\",\"AMOUNT\":\"1999\",\"MERCHANT_RESPONSE_URL\":\"https://www.example.com/response\",\"TIMESTAMP\":\"20170725154824\",\"HPP_VERSION\":\"2\",\"HPP_FRAUDFILTER_MODE\":\"ACTIVE\"}";

        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void FraudManagementRequestWithRules() throws ApiException {
        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("myMerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://api.sandbox.realexpayments.com/epage-remote.cgi");

        HostedPaymentConfig hostedPaymentConfig = new HostedPaymentConfig();
        hostedPaymentConfig.setLanguage("GB");
        hostedPaymentConfig.setResponseUrl("https://www.example.com/response");
        hostedPaymentConfig.setVersion(HppVersion.Version2);
        hostedPaymentConfig.setFraudFilterMode(FraudFilterMode.Passive);

        config.setHostedPaymentConfig(hostedPaymentConfig);

        FraudRuleCollection rules = new FraudRuleCollection();
        String rule1 = "2603986b-3a17-410f-b05a-003f9d955a0f";
        String rule2 = "a7a0918d-20d7-444f-bf07-65f7d320be91";
        rules.addRule(rule1, FraudFilterMode.Active);
        rules.addRule(rule2, FraudFilterMode.Off);

        config.getHostedPaymentConfig().setFraudFilterRules(rules);

        HostedService service = new HostedService(config);
        RealexHppClient client = new RealexHppClient("https://pay.sandbox.realexpayments.com/pay","secret");

        // data to be passed to the HPP along with transaction level settings
        HostedPaymentData hostedPaymentData = new HostedPaymentData();
        hostedPaymentData.setCustomerNumber("E8953893489"); // display the save card tick box
        hostedPaymentData.setProductId("SID9838383"); // new customer

        //serialize the request
        String json = service
                .charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withAddress(billingAddress, AddressType.Billing)
                .withAddress(shippingAddress, AddressType.Shipping)
                .withClientTransactionId("Car Part HV") // varref
                .withCustomerIpAddress("123.123.123.123")
                .withHostedPaymentData(hostedPaymentData)
                .serialize();

        assertNotNull(json);

        //make API call
        String response = client.sendRequest(json);
        assertNotNull(response);

        Transaction parsedResponse = service.parseResponse(response, true);
        assertNotNull(parsedResponse);
        assertEquals("00", parsedResponse.getResponseCode());
        assertEquals(FraudFilterMode.Passive.getValue(), parsedResponse.getResponseValues().get("HPP_FRAUDFILTER_MODE"));
        assertEquals("PASS", parsedResponse.getResponseValues().get("HPP_FRAUDFILTER_RESULT"));
        assertEquals("PASS", parsedResponse.getResponseValues().get("HPP_FRAUDFILTER_RULE_" + rule1));
        assertEquals("NOT_EXECUTED", parsedResponse.getResponseValues().get("HPP_FRAUDFILTER_RULE_" + rule2));
    }

    @Test
    public void fraudWithFraudRules() throws ApiException {
        String ruleId = "853c1d37-6e9f-467e-9ffc-182210b40c6b";
        FraudFilterMode mode = FraudFilterMode.Off;
        FraudRuleCollection fraudRuleCollection = new FraudRuleCollection();
        fraudRuleCollection.addRule(ruleId, mode);

        HostedPaymentConfig hostedPaymentConfig = new HostedPaymentConfig();
        hostedPaymentConfig.setResponseUrl("https://www.example.com/response");
        hostedPaymentConfig.setFraudFilterMode(FraudFilterMode.Passive);
        hostedPaymentConfig.setFraudFilterRules(fraudRuleCollection);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("heartlandgpsandbox");
        config.setAccountId("hpp");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://api.sandbox.realexpayments.com/epage-remote.cgi");
        config.setHostedPaymentConfig(hostedPaymentConfig);

        HostedService service = new HostedService(config);

        String hppJson = service
                .charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .serialize();

        String response = _client.sendRequest(hppJson);
        Transaction parsedResponse = _service.parseResponse(response, true);
        assertNotNull(response);
        assertEquals("00", parsedResponse.getResponseCode());
    }

    @Test
    public void dynamicCurrencyConversionOn() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setDynamicCurrencyConversionEnabled(true);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);
        
        HostedService service = new HostedService(config);

       String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .serialize();

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"061609f85a8e0191dc7f487f8278e71898a2ee2d\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"DCC_ENABLE\": \"1\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void dynamicCurrencyConversionOff() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setDynamicCurrencyConversionEnabled(false);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);
        
        HostedService service = new HostedService(config);

       String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .serialize();

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"061609f85a8e0191dc7f487f8278e71898a2ee2d\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"DCC_ENABLE\": \"0\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void returnTssOn() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setRequestTransactionStabilityScore(true);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);
        
        HostedService service = new HostedService(config);

       String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .serialize();

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"061609f85a8e0191dc7f487f8278e71898a2ee2d\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"RETURN_TSS\": \"1\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void returnTssOff() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setRequestTransactionStabilityScore(false);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);
        
        HostedService service = new HostedService(config);

       String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .serialize();

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"061609f85a8e0191dc7f487f8278e71898a2ee2d\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"RETURN_TSS\": \"0\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void recurringInfo() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);
        
        HostedService service = new HostedService(config);

       String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withRecurringInfo(RecurringType.Fixed, RecurringSequence.First)
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .serialize();

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"061609f85a8e0191dc7f487f8278e71898a2ee2d\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"RECURRING_TYPE\": \"fixed\", \"RECURRING_SEQUENCE\": \"first\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));

        hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withRecurringInfo(RecurringType.Variable, RecurringSequence.Last)
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .serialize();

        expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"061609f85a8e0191dc7f487f8278e71898a2ee2d\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"RECURRING_TYPE\": \"variable\", \"RECURRING_SEQUENCE\": \"last\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));

        hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withRecurringInfo(RecurringType.Fixed, RecurringSequence.Subsequent)
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .serialize();

        expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"061609f85a8e0191dc7f487f8278e71898a2ee2d\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"RECURRING_TYPE\": \"fixed\", \"RECURRING_SEQUENCE\": \"subsequent\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void hashCheckAllInputs() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setCardStorageEnabled(true);
        hostedConfig.setFraudFilterMode(FraudFilterMode.Passive);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);
        
        HostedService service = new HostedService(config);

        HostedPaymentData testHostedPaymentData = new HostedPaymentData();
        testHostedPaymentData.setCustomerKey("376a2598-412d-4805-9f47-c177d5605853");
        testHostedPaymentData.setPaymentKey("ca46344d-4292-47dc-9ced-e8a42ce66977");

       String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .withHostedPaymentData(testHostedPaymentData)
                .serialize();

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"1384392a30abbd7a1993e33c308bf9a2bd354d48\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"CARD_STORAGE_ENABLE\": \"1\", \"PMT_REF\": \"ca46344d-4292-47dc-9ced-e8a42ce66977\", \"PAYER_REF\": \"376a2598-412d-4805-9f47-c177d5605853\", \"HPP_FRAUDFILTER_MODE\": \"PASSIVE\", \"PAYER_EXIST\": \"1\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void hashCheckNoPaymentRef() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setCardStorageEnabled(true);
        hostedConfig.setFraudFilterMode(FraudFilterMode.Passive);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);
        
        HostedService service = new HostedService(config);

        HostedPaymentData testHostedPaymentData = new HostedPaymentData();
        testHostedPaymentData.setCustomerKey("376a2598-412d-4805-9f47-c177d5605853");

       String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .withHostedPaymentData(testHostedPaymentData)
                .serialize();

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"c10b55c16276366ced59174cbab20a6eeeec16c9\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"CARD_STORAGE_ENABLE\": \"1\", \"PAYER_REF\": \"376a2598-412d-4805-9f47-c177d5605853\", \"HPP_FRAUDFILTER_MODE\": \"PASSIVE\", \"PAYER_EXIST\": \"1\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void hashCheckNoPayerRef() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setCardStorageEnabled(true);
        hostedConfig.setFraudFilterMode(FraudFilterMode.Passive);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);
        
        HostedService service = new HostedService(config);

        HostedPaymentData testHostedPaymentData = new HostedPaymentData();
        testHostedPaymentData.setPaymentKey("ca46344d-4292-47dc-9ced-e8a42ce66977");

       String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .withHostedPaymentData(testHostedPaymentData)
                .serialize();

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"73236b35e253215380a9bf2f7a1f11ac23204224\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"CARD_STORAGE_ENABLE\": \"1\", \"PMT_REF\": \"ca46344d-4292-47dc-9ced-e8a42ce66977\", \"HPP_FRAUDFILTER_MODE\": \"PASSIVE\", \"PAYER_EXIST\": \"1\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void hashCheckFraudFilterNone() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setCardStorageEnabled(true);
        hostedConfig.setFraudFilterMode(FraudFilterMode.None);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);
        
        HostedService service = new HostedService(config);

        HostedPaymentData testHostedPaymentData = new HostedPaymentData();
        testHostedPaymentData.setPaymentKey("ca46344d-4292-47dc-9ced-e8a42ce66977");

       String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .withHostedPaymentData(testHostedPaymentData)
                .serialize();

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"5fe76a45585d9793fd162ab8a3cd4a42991417df\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"CARD_STORAGE_ENABLE\": \"1\", \"PMT_REF\": \"ca46344d-4292-47dc-9ced-e8a42ce66977\", \"PAYER_EXIST\": \"1\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void allFieldsCheck() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setCardStorageEnabled(true);
        hostedConfig.setDisplaySavedCards(true);
        hostedConfig.setDynamicCurrencyConversionEnabled(true);
        hostedConfig.setFraudFilterMode(FraudFilterMode.Passive);
        hostedConfig.setLanguage("EN");
        hostedConfig.setPaymentButtonText("Place Order");
        hostedConfig.setRequestTransactionStabilityScore(true);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);
        
        HostedService service = new HostedService(config);

        Address billingAddress = new Address();
        billingAddress.setCountry("US");
        billingAddress.setPostalCode("50001");

        Address shippingAddress = new Address();
        shippingAddress.setCountry("GB");
        shippingAddress.setPostalCode("654");
        shippingAddress.setStreetAddress1("123");

        HostedPaymentData testHostedPaymentData = new HostedPaymentData();
        testHostedPaymentData.setCustomerExists(true);
        testHostedPaymentData.setCustomerKey("376a2598-412d-4805-9f47-c177d5605853");
        testHostedPaymentData.setPaymentKey("ca46344d-4292-47dc-9ced-e8a42ce66977");
        testHostedPaymentData.setCustomerNumber("a028774f-beff-47bc-bd6e-ed7e04f5d758a028774f-btefa");
        testHostedPaymentData.setProductId("a0b38df5-b23c-4d82-88fe-2e9c47438972-b23c-4d82-88f");
        testHostedPaymentData.setOfferToSaveCard(true);

       String hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .withAddress(billingAddress, AddressType.Billing)
                .withAddress(shippingAddress, AddressType.Shipping)
                .withHostedPaymentData(testHostedPaymentData)
                .serialize();

        String expectedJson = "{\"SHIPPING_CODE\":\"654|123\",\"MERCHANT_RESPONSE_URL\":\"https://www.example.com/response\",\"PMT_REF\":\"ca46344d-4292-47dc-9ced-e8a42ce66977\",\"HPP_FRAUDFILTER_MODE\":\"PASSIVE\",\"HPP_SHIPPING_STREET1\":\"123\",\"DCC_ENABLE\":\"1\",\"OFFER_SAVE_CARD\":\"1\",\"AUTO_SETTLE_FLAG\":\"1\",\"BILLING_CODE\":\"50001\",\"ACCOUNT\":\"internet\",\"RETURN_TSS\":\"1\",\"AMOUNT\":\"1999\",\"TIMESTAMP\":\"20170725154824\",\"PROD_ID\":\"a0b38df5-b23c-4d82-88fe-2e9c47438972-b23c-4d82-88f\",\"CURRENCY\":\"EUR\",\"HPP_SHIPPING_COUNTRY\":\"826\",\"HPP_SELECT_STORED_CARD\":\"376a2598-412d-4805-9f47-c177d5605853\",\"PAYER_EXIST\":\"1\",\"HPP_SHIPPING_POSTALCODE\":\"654\",\"HPP_BILLING_POSTALCODE\":\"50001\",\"HPP_BILLING_COUNTRY\":\"840\",\"HPP_LANG\":\"EN\",\"CARD_STORAGE_ENABLE\":\"1\",\"SHA1HASH\":\"1384392a30abbd7a1993e33c308bf9a2bd354d48\",\"ORDER_ID\":\"GTI5Yxb0SumL_TkDMCAxQA\",\"CUST_NUM\":\"a028774f-beff-47bc-bd6e-ed7e04f5d758a028774f-btefa\",\"BILLING_CO\":\"US\",\"CARD_PAYMENT_BUTTON\":\"Place Order\",\"HPP_VERSION\":\"2\",\"SHIPPING_CO\":\"GB\",\"MERCHANT_ID\":\"MerchantId\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
	public void basicChargeAlertnativePayment() throws ApiException {

        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
		hostedConfig.setVersion(HppVersion.Version2);

		GatewayConfig config = new GatewayConfig();
		config.setMerchantId("MerchantId");
		config.setAccountId("internet");
		config.setSharedSecret("secret");
		config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
		config.setHostedPaymentConfig(hostedConfig);

		HostedService service = new HostedService(config);

		HostedPaymentData testHostedPaymentData = new HostedPaymentData();
		testHostedPaymentData.setCustomerCountry("DE");
		testHostedPaymentData.setCustomerFirstName("James");
		testHostedPaymentData.setCustomerLastName("Mason");
		testHostedPaymentData.setMerchantResponseUrl("https://www.example.com/returnUrl");
		testHostedPaymentData.setTransactionStatusUrl("https://www.example.com/statusUrl");
		testHostedPaymentData.setPresetPaymentMethods(AlternativePaymentType.ASTROPAY_DIRECT, AlternativePaymentType.AURA, AlternativePaymentType.BALOTO_CASH, AlternativePaymentType.BANAMEX);

		String hppJson = service.charge(new BigDecimal("19.99"))
				.withCurrency("EUR")
				.withTimestamp("20170725154824")
				.withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
				.withHostedPaymentData(testHostedPaymentData)
				.serialize();

		String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"061609f85a8e0191dc7f487f8278e71898a2ee2d\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\",\"HPP_CUSTOMER_COUNTRY\": \"DE\",\"HPP_CUSTOMER_FIRSTNAME\": \"James\",\"HPP_CUSTOMER_LASTNAME\": \"Mason\",\"MERCHANT_RESPONSE_URL\": \"https://www.example.com/returnUrl\",\"HPP_TX_STATUS_URL\": \"https://www.example.com/statusUrl\",\"PM_METHODS\": \"astropaydirect|aura|baloto|banamex\", \"PAYER_EXIST\": \"1\"}";
		assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
	}

    @Test
    public void creditAuth_MultiAutoSettle() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);

        HostedService service = new HostedService(config);

        String hppJson = service.authorize(new BigDecimal("1"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .withMultiCapture(true)
                .serialize();

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"100\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"34160bd973fd1d8abb295a12c7c734ffd839d66a\", \"AUTO_SETTLE_FLAG\": \"MULTI\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\",\"HPP_VERSION\": \"2\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));

    }

    @Test
    public void creditAuth_AutoSettle() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);

        HostedService service = new HostedService(config);

        String hppJson = service.authorize(new BigDecimal("1"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .withMultiCapture(false)
                .serialize();

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"100\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"34160bd973fd1d8abb295a12c7c734ffd839d66a\", \"AUTO_SETTLE_FLAG\": \"0\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\",\"HPP_VERSION\": \"2\"}";
        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));

    }

    @Test
    public void hasSupplementaryDataWithOneValueSerialized() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);

        HostedService service = new HostedService(config);

        Address billingAddress = new Address();
        billingAddress.setCountry("US");
        billingAddress.setPostalCode("50001");

        Customer customer = new Customer();
        customer.setId("e193c21a-ce64-4820-b5b6-8f46715de931");
        customer.setFirstName("James");
        customer.setLastName("Mason");
        customer.setDateOfBirth("01011980");
        customer.setCustomerPassword("VerySecurePassword");
        customer.setEmail("text@example.com");
        customer.setDomainName("example.com");
        customer.setHomePhone("+35312345678");
        customer.setDeviceFingerPrint("devicefingerprint");

        HostedPaymentData testHostedPaymentData = new HostedPaymentData();
        testHostedPaymentData.setCustomerExists(true);
        testHostedPaymentData.setCustomerKey("376a2598-412d-4805-9f47-c177d5605853");
        testHostedPaymentData.setPaymentKey("ca46344d-4292-47dc-9ced-e8a42ce66977");
        testHostedPaymentData.setCustomerNumber("a028774f-beff-47bc-bd6e-ed7e04f5d758a028774f-btefa");
        testHostedPaymentData.setProductId("a0b38df5-b23c-4d82-88fe-2e9c47438972-b23c-4d82-88f");
        testHostedPaymentData.setOfferToSaveCard(true);

        String hppJson = service.authorize(new BigDecimal("1"))
                .withCurrency("EUR")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .withTimestamp("20170725154824")
                .withAddress(billingAddress, AddressType.Billing)
                .withCustomerData(customer)
                .withHostedPaymentData(testHostedPaymentData)
                .withCustomerId("123456")
                .withSupplementaryData("HPP_FRAUDFILTER_MODE", "ACTIVE")
                .serialize();

        String expectedJson = "{\"CURRENCY\":\"EUR\",\"PAYER_REF\":\"376a2598-412d-4805-9f47-c177d5605853\",\"MERCHANT_RESPONSE_URL\":\"https://www.example.com/response\",\"PAYER_EXIST\":\"1\",\"PMT_REF\":\"ca46344d-4292-47dc-9ced-e8a42ce66977\",\"HPP_BILLING_POSTALCODE\":\"50001\",\"HPP_BILLING_COUNTRY\":\"840\",\"HPP_FRAUDFILTER_MODE\":\"ACTIVE\",\"OFFER_SAVE_CARD\":\"1\",\"AUTO_SETTLE_FLAG\":\"0\",\"BILLING_CODE\":\"50001\",\"ACCOUNT\":\"internet\",\"SHA1HASH\":\"8cfb2201f43e4d8d07f77cab031a7d809876a639\",\"ORDER_ID\":\"GTI5Yxb0SumL_TkDMCAxQA\",\"CUST_NUM\":\"123456\",\"BILLING_CO\":\"US\",\"AMOUNT\":\"100\",\"TIMESTAMP\":\"20170725154824\",\"HPP_VERSION\":\"2\",\"PROD_ID\":\"a0b38df5-b23c-4d82-88fe-2e9c47438972-b23c-4d82-88f\",\"MERCHANT_ID\":\"MerchantId\", \"PAYER_EXIST\": \"1\"}";

        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void hasSupplementaryDataWithTwoValuesSerialized() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);

        HostedService service = new HostedService(config);

        Address billingAddress = new Address();
        billingAddress.setCountry("US");
        billingAddress.setPostalCode("50001");

        Customer customer = new Customer();
        customer.setId("e193c21a-ce64-4820-b5b6-8f46715de931");
        customer.setFirstName("James");
        customer.setLastName("Mason");
        customer.setDateOfBirth("01011980");
        customer.setCustomerPassword("VerySecurePassword");
        customer.setEmail("text@example.com");
        customer.setDomainName("example.com");
        customer.setHomePhone("+35312345678");
        customer.setDeviceFingerPrint("devicefingerprint");

        HostedPaymentData testHostedPaymentData = new HostedPaymentData();
        testHostedPaymentData.setCustomerExists(true);
        testHostedPaymentData.setCustomerKey("376a2598-412d-4805-9f47-c177d5605853");
        testHostedPaymentData.setPaymentKey("ca46344d-4292-47dc-9ced-e8a42ce66977");
        testHostedPaymentData.setCustomerNumber("a028774f-beff-47bc-bd6e-ed7e04f5d758a028774f-btefa");
        testHostedPaymentData.setProductId("a0b38df5-b23c-4d82-88fe-2e9c47438972-b23c-4d82-88f");
        testHostedPaymentData.setOfferToSaveCard(true);

        String hppJson = service.authorize(new BigDecimal("1"))
                .withCurrency("EUR")
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .withTimestamp("20170725154824")
                .withAddress(billingAddress, AddressType.Billing)
                .withCustomerData(customer)
                .withHostedPaymentData(testHostedPaymentData)
                .withCustomerId("123456")
                .withSupplementaryData("RANDOM_KEY", "RANDOM_VALUE1", "RANDOM_VALUE2")
                .serialize();

        String expectedJson = "{\"CURRENCY\":\"EUR\",\"PAYER_REF\":\"376a2598-412d-4805-9f47-c177d5605853\",\"RANDOM_KEY\":\"[RANDOM_VALUE1 ,RANDOM_VALUE2]\",\"MERCHANT_RESPONSE_URL\":\"https://www.example.com/response\",\"PMT_REF\":\"ca46344d-4292-47dc-9ced-e8a42ce66977\",\"HPP_BILLING_POSTALCODE\":\"50001\",\"HPP_BILLING_COUNTRY\":\"840\",\"OFFER_SAVE_CARD\":\"1\",\"AUTO_SETTLE_FLAG\":\"0\",\"BILLING_CODE\":\"50001\",\"ACCOUNT\":\"internet\",\"SHA1HASH\":\"8cfb2201f43e4d8d07f77cab031a7d809876a639\",\"ORDER_ID\":\"GTI5Yxb0SumL_TkDMCAxQA\",\"CUST_NUM\":\"123456\",\"BILLING_CO\":\"US\",\"AMOUNT\":\"100\",\"TIMESTAMP\":\"20170725154824\",\"HPP_VERSION\":\"2\",\"PROD_ID\":\"a0b38df5-b23c-4d82-88fe-2e9c47438972-b23c-4d82-88f\",\"MERCHANT_ID\":\"MerchantId\", \"PAYER_EXIST\": \"1\"}";
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void customHostedServiceConfigName() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setHostedPaymentConfig(hostedConfig);

        final String CONFIG_NAME = "customName";

        HostedService service = new HostedService(config, CONFIG_NAME);

        String hppJson =
                service
                        .authorize(new BigDecimal("19.99"))
                        .withCurrency("EUR")
                        .withTimestamp("20170725154824")
                        .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                        .serialize(CONFIG_NAME);

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"061609f85a8e0191dc7f487f8278e71898a2ee2d\", \"AUTO_SETTLE_FLAG\": \"0\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\"}";

        assertEquals(true, JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void testNetherlandsAntillesCountry() throws ApiException {
        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");

        HostedPaymentConfig hostedPaymentConfig = new HostedPaymentConfig();
        hostedPaymentConfig.setVersion(HppVersion.Version2);
        hostedPaymentConfig.setDynamicCurrencyConversionEnabled(true);

        config.setHostedPaymentConfig(hostedPaymentConfig);

        // Add 3D Secure 2 Mandatory and Recommended Fields
        HostedPaymentData hostedPaymentData = new HostedPaymentData();
        hostedPaymentData.setCustomerEmail("test@test.com");
        hostedPaymentData.setAddressesMatch(false);

        Address billingAddress = new Address();
        billingAddress.setStreetAddress1("Flat 123");
        billingAddress.setStreetAddress2("House 456");
        billingAddress.setStreetAddress3("Unit 4");
        billingAddress.setCity("Halifax");
        billingAddress.setPostalCode("W5 9HR");
        billingAddress.setCountry("AN");

        HostedService service = new HostedService(config);

        String hppJson =
                service
                        .charge(new BigDecimal("59.00"))
                        .withCurrency("EUR")
                        .withHostedPaymentData(hostedPaymentData)
                        .withAddress(billingAddress, AddressType.Billing)
                        .serialize();

        assertNotNull(hppJson);
        assertTrue(hppJson.contains("\"HPP_BILLING_COUNTRY\":\"530\""));
        assertTrue(hppJson.contains("\"BILLING_CO\":\"AN\""));
    }

    @Test
    /**
     * We can set multiple APMs/LPMs on $presetPaymentMethods, but our HppClient for testing will treat only the first
     * entry from the list as an example for our unit test, in this case will be "sofort"
     */
    public void testBasicChargeAlternativePayment() throws ApiException {
        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("heartlandgpsandbox");
        config.setAccountId("hpp");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setEnableLogging(true);

        HostedPaymentConfig hostedPaymentConfig = new HostedPaymentConfig();
        hostedPaymentConfig.setVersion(HppVersion.Version2);

        config.setHostedPaymentConfig(hostedPaymentConfig);

        HostedPaymentData hostedPaymentData = new HostedPaymentData();

        hostedPaymentData.setCustomerCountry("DE");
        hostedPaymentData.setCustomerFirstName("James");
        hostedPaymentData.setCustomerFirstName("Mason");
        hostedPaymentData.setMerchantResponseUrl("https://www.example.com/returnUrl");
        hostedPaymentData.setTransactionStatusUrl("https://www.example.com/statusUrl");

        AlternativePaymentType[] apmTypes = {
                AlternativePaymentType.SOFORTUBERWEISUNG,
                AlternativePaymentType.TESTPAY,
                AlternativePaymentType.PAYPAL,
                AlternativePaymentType.SEPA_DIRECTDEBIT_PPPRO_MANDATE_MODEL_A
        };

        hostedPaymentData.setPresetPaymentMethods(apmTypes);

        HostedService service = new HostedService(config);

        String json =
                service
                        .charge(new BigDecimal(10.01))
                        .withCurrency("EUR")
                        .withHostedPaymentData(hostedPaymentData)
                        .serialize();

        JsonDoc jsonResponse = JsonDoc.parse(json);

        assertEquals("sofort|testpay|paypal|sepapm", jsonResponse.getString("PM_METHODS"));
        assertEquals(hostedPaymentData.getCustomerFirstName(), jsonResponse.getString("HPP_CUSTOMER_FIRSTNAME"));
        assertEquals(hostedPaymentData.getCustomerLastName(), jsonResponse.getString("HPP_CUSTOMER_LASTNAME"));
        assertEquals(hostedPaymentData.getMerchantResponseUrl(), jsonResponse.getString("MERCHANT_RESPONSE_URL"));
        assertEquals(hostedPaymentData.getTransactionStatusUrl(), jsonResponse.getString("HPP_TX_STATUS_URL"));
        assertEquals(hostedPaymentData.getCustomerCountry(), jsonResponse.getString("HPP_CUSTOMER_COUNTRY"));

        RealexHppClient client = new RealexHppClient("https://pay.sandbox.realexpayments.com/pay","secret");
        String response = client.sendRequest(json);
        Transaction parsedResponse = service.parseResponse(response, true);

        assertNotNull(parsedResponse);
        // TODO: Getting 00 && [ test system ] Authorised
        assertEquals("01", parsedResponse.getResponseCode());
        assertEquals(TransactionStatus.Pending.getValue(), parsedResponse.getResponseMessage());
        assertEquals(AlternativePaymentType.SOFORTUBERWEISUNG.getValue(), parsedResponse.getResponseValues().get("PAYMENTMETHOD"));
        assertEquals(hostedPaymentData.getMerchantResponseUrl(), parsedResponse.getResponseValues().get("MERCHANT_RESPONSE_URL"));
    }

    @Test
    public void captureBillingAndShippingInformation() throws ApiException {
        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("MerchantId");
        config.setAccountId("internet");
        config.setRefundPassword("refund");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");

        HostedPaymentConfig hostedPaymentConfig = new HostedPaymentConfig();

        hostedPaymentConfig.setLanguage("GB");
        hostedPaymentConfig.setResponseUrl("https://www.example.com/response");
        hostedPaymentConfig.setVersion(HppVersion.Version2);

        config.setHostedPaymentConfig(hostedPaymentConfig);

        HostedService service = new HostedService(config);

        HostedPaymentData hostedPaymentData = new HostedPaymentData();
        hostedPaymentData.setAddressCapture(true);
        hostedPaymentData.setReturnAddress(false);

        String json =
                service
                        .charge(new BigDecimal(19.99))
                        .withCurrency("EUR")
                        .withAddress(billingAddress, AddressType.Billing)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withHostedPaymentData(hostedPaymentData)
                        .serialize();

        JsonDoc jsonResponse = JsonDoc.parse(json);

        assertTrue(jsonResponse.getBool("HPP_CAPTURE_ADDRESS"));
        assertFalse(jsonResponse.getBool("HPP_DO_NOT_RETURN_ADDRESS"));
    }

}
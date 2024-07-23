package com.global.api.tests.gpEcom;

import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.paymentMethods.BankPayment;
import com.global.api.serviceConfigs.GpEcomConfig;
import com.global.api.serviceConfigs.HostedPaymentConfig;
import com.global.api.services.HostedService;
import com.global.api.tests.JsonComparator;
import com.global.api.tests.gpEcom.hpp.GpEcomHppClient;
import com.global.api.utils.JsonDoc;
import lombok.var;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GpEcomHppRequestTest {
    private HostedService _service;
    private GpEcomHppClient _client;
    private Address billingAddress;
    private Address shippingAddress;

    public GpEcomHppRequestTest() throws ApiException {
        _client = new GpEcomHppClient("https://pay.sandbox.realexpayments.com/pay", "secret");

        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setLanguage("GB");
        hostedConfig.setResponseUrl("http://requestb.in/10q2bjb1");

        GpEcomConfig config = new GpEcomConfig();
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

        GpEcomConfig config = new GpEcomConfig();
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

    @Test
    public void test_CUST_NUM_HostedPaymentDataAndCustomerNumberAndCustomerId() throws ApiException {

        // prepare
        prepareConfig();

        String customerNumber = "a028774f-beff-47bc-bd6e-ed7e04f5d758a028774f-btefa";
        String customerId = "123456";

        HostedPaymentData testHostedPaymentData = buildHostedPaymentDataWithCustomerNumber(customerNumber);

        // act
        String json = serializeWithHostedPaymentDataAndCustomerId(testHostedPaymentData, customerId);

        // assert
        assert_CUST_NUM(json, customerNumber);
    }

    @Test
    public void test_CUST_NUM_HostedPaymentDataAndNoCustomerNumberAndCustomerId() throws ApiException {

        // prepare
        prepareConfig();

        String customerNumber = null;
        String customerId = "123456";

        HostedPaymentData testHostedPaymentData = buildHostedPaymentDataWithCustomerNumber(customerNumber);

        // act
        String json = serializeWithHostedPaymentDataAndCustomerId(testHostedPaymentData, customerId);

        // assert
        assert_CUST_NUM(json, null);
    }

    @Test
    public void test_CUST_NUM_HostedPaymentDataAndNoCustomerNumberAndNoCustomerId() throws ApiException {
        // prepare
        prepareConfig();

        String customerNumber = null;
        String customerId = null;

        HostedPaymentData testHostedPaymentData = buildHostedPaymentDataWithCustomerNumber(customerNumber);

        // act
        String json = serializeWithHostedPaymentDataAndCustomerId(testHostedPaymentData, customerId);

        // assert
        assert_CUST_NUM(json, null);
    }

    @Test
    public void test_CUST_NUM_NoHostedPaymentDataAndCustomerId() throws ApiException {
        // prepare
        prepareConfig();

        String customerId = "123456";

        HostedPaymentData testHostedPaymentData = null;

        // act
        String json = serializeWithHostedPaymentDataAndCustomerId(testHostedPaymentData, customerId);

        // assert
        assert_CUST_NUM(json, customerId);
    }

    @Test
    public void test_CUST_NUM_NoHostedPaymentDataAndNoCustomerId() throws ApiException {
        // prepare
        prepareConfig();

        String customerId = null;

        HostedPaymentData testHostedPaymentData = null;

        // act
        String json = serializeWithHostedPaymentDataAndCustomerId(testHostedPaymentData, customerId);

        // assert
        assert_CUST_NUM(json, null);
    }

    private void prepareConfig() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setVersion(HppVersion.Version2);

        GpEcomConfig config = new GpEcomConfig();
        config.setMerchantId("heartlandgpsandbox");
        config.setAccountId("3dsecure");
        config.setSharedSecret("secret");
        config.setHostedPaymentConfig(hostedConfig);

        _service = new HostedService(config, "3ds");
    }

    private HostedPaymentData buildHostedPaymentDataWithCustomerNumber(String customerNumber) {
        HostedPaymentData testHostedPaymentData = new HostedPaymentData();
        testHostedPaymentData.setCustomerEmail("james.mason@example.com");
        testHostedPaymentData.setCustomerPhoneMobile("44|07123456789");
        testHostedPaymentData.setAddressesMatch(false);
        testHostedPaymentData.setCustomerCountry("GB");
        testHostedPaymentData.setCustomerFirstName("Jason");
        testHostedPaymentData.setCustomerLastName("Mason");
        testHostedPaymentData.setMerchantResponseUrl("http://requestb.in/10q2bjb1");
        testHostedPaymentData.setTransactionStatusUrl("http://requestb.in/10q2bjb1");
        testHostedPaymentData.setCustomerNumber(customerNumber);
        return testHostedPaymentData;
    }

    private String serializeWithHostedPaymentDataAndCustomerId(HostedPaymentData testHostedPaymentData, String customerId) throws ApiException {
        String json = _service.verify(new BigDecimal("0"))
                .withCurrency("EUR")
                .withCustomerId(customerId)
                .withAddress(billingAddress, AddressType.Billing)
                .withAddress(shippingAddress, AddressType.Shipping)
                .withHostedPaymentData(testHostedPaymentData)
                .serialize("3ds");
        assertNotNull(json);
        return json;
    }

    private void assert_CUST_NUM(String json, String expected_CUST_NUM) {
        JsonDoc responseJsonDoc = JsonDoc.parse(json, null);
        String custNum = responseJsonDoc.getString("CUST_NUM");
        assertEquals(expected_CUST_NUM, custNum);
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

        GpEcomConfig config = new GpEcomConfig();
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

        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void basicCharge() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);

        GpEcomConfig config = new GpEcomConfig();
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

        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    // testing COMMENT1, CUST_NUM, PROD_ID, VAR_REF, HPP_LANG, CARD_PAYMENT_BUTTON
    public void basicHostedPaymentData() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setLanguage("EN");
        hostedConfig.setPaymentButtonText("Place Order");

        GpEcomConfig config = new GpEcomConfig();
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

        String expectedJson = "{\"MERCHANT_ID\":\"MerchantId\",\"ACCOUNT\":\"internet\",\"ORDER_ID\":\"GTI5Yxb0SumL_TkDMCAxQA\",\"AMOUNT\":\"1999\",\"CURRENCY\":\"EUR\",\"TIMESTAMP\":\"20170725154824\",\"AUTO_SETTLE_FLAG\":\"1\",\"COMMENT1\":\"Mobile Channel\",\"CUST_NUM\":\"a028774f-beff-47bc-bd6e-ed7e04f5d758a028774f-btefa\",\"PROD_ID\":\"a0b38df5-b23c-4d82-88fe-2e9c47438972-b23c-4d82-88f\",\"VAR_REF\":\"My Legal Entity\",\"HPP_LANG\":\"EN\",\"MERCHANT_RESPONSE_URL\":\"https://www.example.com/response\",\"CARD_PAYMENT_BUTTON\":\"Place Order\",\"HPP_VERSION\":\"2\",\"SHA1HASH\":\"061609f85a8e0191dc7f487f8278e71898a2ee2d\",\"HPP_DO_NOT_RETURN_ADDRESS\":\"FALSE\",\"PAYER_EXIST\":\"0\"}";

        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void storeCardNewCustomerNoRefs() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setCardStorageEnabled(true);

        GpEcomConfig config = new GpEcomConfig();
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

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"7116c49826367c6513efdc0cc81e243b8095d78f\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"CARD_STORAGE_ENABLE\": \"1\", \"OFFER_SAVE_CARD\": \"1\",\"HPP_DO_NOT_RETURN_ADDRESS\":\"FALSE\",\"PAYER_EXIST\":\"0\"}";
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void storeCardNewCustomerJustPayerRef() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setCardStorageEnabled(true);

        GpEcomConfig config = new GpEcomConfig();
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

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"4dcf4e5e2d43855fe31cdc097e985a895868563e\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"CARD_STORAGE_ENABLE\": \"1\", \"OFFER_SAVE_CARD\": \"1\", \"PAYER_REF\": \"376a2598-412d-4805-9f47-c177d5605853\",\"HPP_DO_NOT_RETURN_ADDRESS\":\"FALSE\",\"PAYER_EXIST\":\"0\"}";
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void storeCardNewCustomerJustPaymentRef() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setCardStorageEnabled(true);

        GpEcomConfig config = new GpEcomConfig();
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

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"5fe76a45585d9793fd162ab8a3cd4a42991417df\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"CARD_STORAGE_ENABLE\": \"1\", \"OFFER_SAVE_CARD\": \"1\", \"PMT_REF\": \"ca46344d-4292-47dc-9ced-e8a42ce66977\",\"HPP_DO_NOT_RETURN_ADDRESS\":\"FALSE\",\"PAYER_EXIST\":\"0\"}";
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void storeCardNewCustomerAllSuppliedRefs() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setCardStorageEnabled(true);

        GpEcomConfig config = new GpEcomConfig();
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

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"f0cf097fe769a6a5a6254eee631e51709ba34c90\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"CARD_STORAGE_ENABLE\": \"1\", \"OFFER_SAVE_CARD\": \"1\", \"PMT_REF\": \"ca46344d-4292-47dc-9ced-e8a42ce66977\", \"PAYER_REF\": \"376a2598-412d-4805-9f47-c177d5605853\",\"HPP_DO_NOT_RETURN_ADDRESS\":\"FALSE\",\"PAYER_EXIST\":\"0\"}";
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void storeCardAutoNewCustomerNoRefs() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setCardStorageEnabled(true);

        GpEcomConfig config = new GpEcomConfig();
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

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"7116c49826367c6513efdc0cc81e243b8095d78f\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"CARD_STORAGE_ENABLE\": \"1\", \"OFFER_SAVE_CARD\": \"0\",\"HPP_DO_NOT_RETURN_ADDRESS\":\"FALSE\",\"PAYER_EXIST\":\"0\"}";
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void storeCardReturnCustomerNoPaymentRef() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setCardStorageEnabled(true);

        GpEcomConfig config = new GpEcomConfig();
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

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"4dcf4e5e2d43855fe31cdc097e985a895868563e\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"CARD_STORAGE_ENABLE\": \"1\", \"OFFER_SAVE_CARD\": \"1\", \"PAYER_EXIST\": \"1\", \"PAYER_REF\": \"376a2598-412d-4805-9f47-c177d5605853\",\"HPP_DO_NOT_RETURN_ADDRESS\":\"FALSE\",\"PAYER_EXIST\":\"1\"}";
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void storeCardReturnCustomerWithPaymentRef() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setCardStorageEnabled(true);

        GpEcomConfig config = new GpEcomConfig();
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

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"5fe76a45585d9793fd162ab8a3cd4a42991417df\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"CARD_STORAGE_ENABLE\": \"1\", \"OFFER_SAVE_CARD\": \"1\", \"PAYER_EXIST\": \"1\", \"PMT_REF\": \"ca46344d-4292-47dc-9ced-e8a42ce66977\",\"HPP_DO_NOT_RETURN_ADDRESS\":\"FALSE\",\"PAYER_EXIST\":\"1\"}";
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void storeCardAutoReturnCustomerAllRefs() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setCardStorageEnabled(true);

        GpEcomConfig config = new GpEcomConfig();
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

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"f0cf097fe769a6a5a6254eee631e51709ba34c90\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"CARD_STORAGE_ENABLE\": \"1\", \"OFFER_SAVE_CARD\": \"0\", \"PAYER_EXIST\": \"1\", \"PMT_REF\": \"ca46344d-4292-47dc-9ced-e8a42ce66977\", \"PAYER_REF\": \"376a2598-412d-4805-9f47-c177d5605853\",\"HPP_DO_NOT_RETURN_ADDRESS\":\"FALSE\",\"PAYER_EXIST\":\"1\"}";
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void displayStoredCardsOfferSaveNoPaymentRef() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setDisplaySavedCards(true);

        GpEcomConfig config = new GpEcomConfig();
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

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"4dcf4e5e2d43855fe31cdc097e985a895868563e\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"HPP_SELECT_STORED_CARD\": \"376a2598-412d-4805-9f47-c177d5605853\", \"OFFER_SAVE_CARD\": \"1\", \"PAYER_EXIST\": \"1\",\"HPP_DO_NOT_RETURN_ADDRESS\":\"FALSE\"}";
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void displayStoredCardsOfferSaveWithPaymentRef() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setDisplaySavedCards(true);

        GpEcomConfig config = new GpEcomConfig();
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

        String expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"f0cf097fe769a6a5a6254eee631e51709ba34c90\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"HPP_SELECT_STORED_CARD\": \"376a2598-412d-4805-9f47-c177d5605853\", \"OFFER_SAVE_CARD\": \"1\", \"PAYER_EXIST\": \"1\", \"PMT_REF\": \"ca46344d-4292-47dc-9ced-e8a42ce66977\",\"HPP_DO_NOT_RETURN_ADDRESS\":\"FALSE\",\"PAYER_EXIST\":\"1\"}";
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void billingData() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);

        GpEcomConfig config = new GpEcomConfig();
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
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void shippingData() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);

        GpEcomConfig config = new GpEcomConfig();
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
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void billingAndShippingData() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);

        GpEcomConfig config = new GpEcomConfig();
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
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void fraudFilterPassive() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setFraudFilterMode(FraudFilterMode.Passive);

        GpEcomConfig config = new GpEcomConfig();
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
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void fraudFilterOff() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setFraudFilterMode(FraudFilterMode.Off);

        GpEcomConfig config = new GpEcomConfig();
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
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void fraudFilterNone() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setFraudFilterMode(FraudFilterMode.None);

        GpEcomConfig config = new GpEcomConfig();
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
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void fraudFilterActive() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setFraudFilterMode(FraudFilterMode.Active);

        GpEcomConfig config = new GpEcomConfig();
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

        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void fraudManagementRequestWithRules() throws ApiException {
        GpEcomConfig config = new GpEcomConfig();
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
        GpEcomHppClient client = new GpEcomHppClient("https://pay.sandbox.realexpayments.com/pay", "secret");

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

        GpEcomConfig config = new GpEcomConfig();
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

        GpEcomConfig config = new GpEcomConfig();
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
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void dynamicCurrencyConversionOff() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setDynamicCurrencyConversionEnabled(false);

        GpEcomConfig config = new GpEcomConfig();
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
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void returnTssOn() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setRequestTransactionStabilityScore(true);

        GpEcomConfig config = new GpEcomConfig();
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
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void returnTssOff() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setRequestTransactionStabilityScore(false);

        GpEcomConfig config = new GpEcomConfig();
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
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void recurringInfo() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);

        GpEcomConfig config = new GpEcomConfig();
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
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));

        hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withRecurringInfo(RecurringType.Variable, RecurringSequence.Last)
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .serialize();

        expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"061609f85a8e0191dc7f487f8278e71898a2ee2d\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"RECURRING_TYPE\": \"variable\", \"RECURRING_SEQUENCE\": \"last\"}";
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));

        hppJson = service.charge(new BigDecimal("19.99"))
                .withCurrency("EUR")
                .withTimestamp("20170725154824")
                .withRecurringInfo(RecurringType.Fixed, RecurringSequence.Subsequent)
                .withOrderId("GTI5Yxb0SumL_TkDMCAxQA")
                .serialize();

        expectedJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"CURRENCY\": \"EUR\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"061609f85a8e0191dc7f487f8278e71898a2ee2d\", \"AUTO_SETTLE_FLAG\": \"1\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"HPP_VERSION\": \"2\", \"RECURRING_TYPE\": \"fixed\", \"RECURRING_SEQUENCE\": \"subsequent\"}";
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void hashCheckAllInputs() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setCardStorageEnabled(true);
        hostedConfig.setFraudFilterMode(FraudFilterMode.Passive);

        GpEcomConfig config = new GpEcomConfig();
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

        String expectedJson = "{\"MERCHANT_ID\":\"MerchantId\",\"ACCOUNT\":\"internet\",\"ORDER_ID\":\"GTI5Yxb0SumL_TkDMCAxQA\",\"AMOUNT\":\"1999\",\"CURRENCY\":\"EUR\",\"TIMESTAMP\":\"20170725154824\",\"AUTO_SETTLE_FLAG\":\"1\",\"PAYER_REF\":\"376a2598-412d-4805-9f47-c177d5605853\",\"PMT_REF\":\"ca46344d-4292-47dc-9ced-e8a42ce66977\",\"MERCHANT_RESPONSE_URL\":\"https://www.example.com/response\",\"CARD_STORAGE_ENABLE\":\"1\",\"HPP_FRAUDFILTER_MODE\":\"PASSIVE\",\"HPP_VERSION\":\"2\",\"SHA1HASH\":\"1384392a30abbd7a1993e33c308bf9a2bd354d48\",\"HPP_DO_NOT_RETURN_ADDRESS\":\"FALSE\",\"PAYER_EXIST\":\"0\"}";
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void hashCheckNoPaymentRef() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setCardStorageEnabled(true);
        hostedConfig.setFraudFilterMode(FraudFilterMode.Passive);

        GpEcomConfig config = new GpEcomConfig();
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

        String expectedJson = "{\"MERCHANT_ID\":\"MerchantId\",\"ACCOUNT\":\"internet\",\"ORDER_ID\":\"GTI5Yxb0SumL_TkDMCAxQA\",\"AMOUNT\":\"1999\",\"CURRENCY\":\"EUR\",\"TIMESTAMP\":\"20170725154824\",\"AUTO_SETTLE_FLAG\":\"1\",\"PAYER_REF\":\"376a2598-412d-4805-9f47-c177d5605853\",\"MERCHANT_RESPONSE_URL\":\"https://www.example.com/response\",\"CARD_STORAGE_ENABLE\":\"1\",\"HPP_FRAUDFILTER_MODE\":\"PASSIVE\",\"HPP_VERSION\":\"2\",\"SHA1HASH\":\"c10b55c16276366ced59174cbab20a6eeeec16c9\",\"HPP_DO_NOT_RETURN_ADDRESS\":\"FALSE\",\"PAYER_EXIST\":\"0\"}";
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void hashCheckNoPayerRef() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setCardStorageEnabled(true);
        hostedConfig.setFraudFilterMode(FraudFilterMode.Passive);

        GpEcomConfig config = new GpEcomConfig();
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

        String expectedJson = "{\"MERCHANT_ID\":\"MerchantId\",\"ACCOUNT\":\"internet\",\"ORDER_ID\":\"GTI5Yxb0SumL_TkDMCAxQA\",\"AMOUNT\":\"1999\",\"CURRENCY\":\"EUR\",\"TIMESTAMP\":\"20170725154824\",\"AUTO_SETTLE_FLAG\":\"1\",\"PMT_REF\":\"ca46344d-4292-47dc-9ced-e8a42ce66977\",\"MERCHANT_RESPONSE_URL\":\"https://www.example.com/response\",\"CARD_STORAGE_ENABLE\":\"1\",\"HPP_FRAUDFILTER_MODE\":\"PASSIVE\",\"HPP_VERSION\":\"2\",\"SHA1HASH\":\"73236b35e253215380a9bf2f7a1f11ac23204224\",\"HPP_DO_NOT_RETURN_ADDRESS\":\"FALSE\",\"PAYER_EXIST\":\"0\"}";
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void hashCheckFraudFilterNone() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);
        hostedConfig.setCardStorageEnabled(true);
        hostedConfig.setFraudFilterMode(FraudFilterMode.None);

        GpEcomConfig config = new GpEcomConfig();
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

        String expectedJson = "{\"MERCHANT_ID\":\"MerchantId\",\"ACCOUNT\":\"internet\",\"ORDER_ID\":\"GTI5Yxb0SumL_TkDMCAxQA\",\"AMOUNT\":\"1999\",\"CURRENCY\":\"EUR\",\"TIMESTAMP\":\"20170725154824\",\"AUTO_SETTLE_FLAG\":\"1\",\"PMT_REF\":\"ca46344d-4292-47dc-9ced-e8a42ce66977\",\"MERCHANT_RESPONSE_URL\":\"https://www.example.com/response\",\"CARD_STORAGE_ENABLE\":\"1\",\"HPP_VERSION\":\"2\",\"SHA1HASH\":\"5fe76a45585d9793fd162ab8a3cd4a42991417df\",\"HPP_DO_NOT_RETURN_ADDRESS\":\"FALSE\",\"PAYER_EXIST\":\"0\"}";
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
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

        GpEcomConfig config = new GpEcomConfig();
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

        String expectedJson = "{\"SHIPPING_CODE\":\"654|123\",\"MERCHANT_RESPONSE_URL\":\"https://www.example.com/response\",\"PMT_REF\":\"ca46344d-4292-47dc-9ced-e8a42ce66977\",\"HPP_FRAUDFILTER_MODE\":\"PASSIVE\",\"HPP_SHIPPING_STREET1\":\"123\",\"DCC_ENABLE\":\"1\",\"OFFER_SAVE_CARD\":\"1\",\"AUTO_SETTLE_FLAG\":\"1\",\"BILLING_CODE\":\"50001\",\"ACCOUNT\":\"internet\",\"RETURN_TSS\":\"1\",\"AMOUNT\":\"1999\",\"TIMESTAMP\":\"20170725154824\",\"PROD_ID\":\"a0b38df5-b23c-4d82-88fe-2e9c47438972-b23c-4d82-88f\",\"CURRENCY\":\"EUR\",\"HPP_SHIPPING_COUNTRY\":\"826\",\"HPP_SELECT_STORED_CARD\":\"376a2598-412d-4805-9f47-c177d5605853\",\"PAYER_EXIST\":\"1\",\"HPP_SHIPPING_POSTALCODE\":\"654\",\"HPP_BILLING_POSTALCODE\":\"50001\",\"HPP_BILLING_COUNTRY\":\"840\",\"HPP_LANG\":\"EN\",\"CARD_STORAGE_ENABLE\":\"1\",\"SHA1HASH\":\"1384392a30abbd7a1993e33c308bf9a2bd354d48\",\"ORDER_ID\":\"GTI5Yxb0SumL_TkDMCAxQA\",\"CUST_NUM\":\"a028774f-beff-47bc-bd6e-ed7e04f5d758a028774f-btefa\",\"BILLING_CO\":\"US\",\"CARD_PAYMENT_BUTTON\":\"Place Order\",\"HPP_VERSION\":\"2\",\"SHIPPING_CO\":\"GB\",\"MERCHANT_ID\":\"MerchantId\",\"HPP_DO_NOT_RETURN_ADDRESS\":\"FALSE\",\"PAYER_EXIST\":\"1\"}";
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void basicChargeAlternativePayment() throws ApiException {

        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setVersion(HppVersion.Version2);

        GpEcomConfig config = new GpEcomConfig();
        config.setMerchantId("heartlandgpsandbox");
        config.setAccountId("hpp");
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

        String expectedJson = "{\"CURRENCY\":\"EUR\",\"HPP_CUSTOMER_LASTNAME\":\"Mason\",\"MERCHANT_RESPONSE_URL\":\"https://www.example.com/returnUrl\",\"HPP_TX_STATUS_URL\":\"https://www.example.com/statusUrl\",\"PM_METHODS\":\"astropaydirect|aura|baloto|banamex\",\"AUTO_SETTLE_FLAG\":\"1\",\"ACCOUNT\":\"hpp\",\"SHA1HASH\":\"647d071bdcb8d9da5f29688a787863a39dc51ef3\",\"ORDER_ID\":\"GTI5Yxb0SumL_TkDMCAxQA\",\"AMOUNT\":\"1999\",\"HPP_CUSTOMER_FIRSTNAME\":\"James\",\"TIMESTAMP\":\"20170725154824\",\"HPP_VERSION\":\"2\",\"HPP_CUSTOMER_COUNTRY\":\"DE\",\"MERCHANT_ID\":\"heartlandgpsandbox\",\"HPP_DO_NOT_RETURN_ADDRESS\":\"FALSE\",\"PAYER_EXIST\":\"0\", \"HPP_NAME\":\"James Mason\"}";
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void creditAuth_MultiAutoSettle() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);

        GpEcomConfig config = new GpEcomConfig();
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
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void creditAuth_AutoSettle() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);

        GpEcomConfig config = new GpEcomConfig();
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
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void hasSupplementaryDataWithOneValueSerialized() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);

        GpEcomConfig config = new GpEcomConfig();
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

        String expectedJson = "{\"CURRENCY\":\"EUR\",\"PAYER_REF\":\"376a2598-412d-4805-9f47-c177d5605853\",\"MERCHANT_RESPONSE_URL\":\"https://www.example.com/response\",\"PAYER_EXIST\":\"1\",\"PMT_REF\":\"ca46344d-4292-47dc-9ced-e8a42ce66977\",\"HPP_BILLING_POSTALCODE\":\"50001\",\"HPP_BILLING_COUNTRY\":\"840\",\"HPP_FRAUDFILTER_MODE\":\"ACTIVE\",\"OFFER_SAVE_CARD\":\"1\",\"AUTO_SETTLE_FLAG\":\"0\",\"BILLING_CODE\":\"50001\",\"ACCOUNT\":\"internet\",\"SHA1HASH\":\"8cfb2201f43e4d8d07f77cab031a7d809876a639\",\"ORDER_ID\":\"GTI5Yxb0SumL_TkDMCAxQA\",\"CUST_NUM\":\"a028774f-beff-47bc-bd6e-ed7e04f5d758a028774f-btefa\",\"BILLING_CO\":\"US\",\"AMOUNT\":\"100\",\"TIMESTAMP\":\"20170725154824\",\"HPP_VERSION\":\"2\",\"PROD_ID\":\"a0b38df5-b23c-4d82-88fe-2e9c47438972-b23c-4d82-88f\",\"MERCHANT_ID\":\"MerchantId\", \"PAYER_EXIST\": \"1\",\"HPP_DO_NOT_RETURN_ADDRESS\":\"FALSE\"}";

        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void hasSupplementaryDataWithTwoValuesSerialized() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);

        GpEcomConfig config = new GpEcomConfig();
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

        String expectedJson = "{\"CURRENCY\":\"EUR\",\"PAYER_REF\":\"376a2598-412d-4805-9f47-c177d5605853\",\"RANDOM_KEY\":\"[RANDOM_VALUE1 ,RANDOM_VALUE2]\",\"MERCHANT_RESPONSE_URL\":\"https://www.example.com/response\",\"PMT_REF\":\"ca46344d-4292-47dc-9ced-e8a42ce66977\",\"HPP_BILLING_POSTALCODE\":\"50001\",\"HPP_BILLING_COUNTRY\":\"840\",\"OFFER_SAVE_CARD\":\"1\",\"AUTO_SETTLE_FLAG\":\"0\",\"BILLING_CODE\":\"50001\",\"ACCOUNT\":\"internet\",\"SHA1HASH\":\"8cfb2201f43e4d8d07f77cab031a7d809876a639\",\"ORDER_ID\":\"GTI5Yxb0SumL_TkDMCAxQA\",\"CUST_NUM\":\"a028774f-beff-47bc-bd6e-ed7e04f5d758a028774f-btefa\",\"BILLING_CO\":\"US\",\"AMOUNT\":\"100\",\"TIMESTAMP\":\"20170725154824\",\"HPP_VERSION\":\"2\",\"PROD_ID\":\"a0b38df5-b23c-4d82-88fe-2e9c47438972-b23c-4d82-88f\",\"MERCHANT_ID\":\"MerchantId\", \"PAYER_EXIST\": \"1\",\"HPP_DO_NOT_RETURN_ADDRESS\":\"FALSE\"}";
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void customHostedServiceConfigName() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setResponseUrl("https://www.example.com/response");
        hostedConfig.setVersion(HppVersion.Version2);

        GpEcomConfig config = new GpEcomConfig();
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
        assertTrue(JsonComparator.areEqual(expectedJson, hppJson));
    }

    @Test
    public void testNetherlandsAntillesCountry() throws ApiException {
        GpEcomConfig config = new GpEcomConfig();
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
    public void cardBlockingPayment() throws ApiException {

        GpEcomConfig config = new GpEcomConfig();
        config.setMerchantId("heartlandgpsandbox");
        config.setAccountId("hpp");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");

        config.setHostedPaymentConfig(new HostedPaymentConfig());
        // TODO(mfranzoy) in dotnet we use version 1 but in have we only have version 2
        config.getHostedPaymentConfig().setVersion(HppVersion.Version2);

        GpEcomHppClient client = new GpEcomHppClient(config.getServiceUrl(), config.getSharedSecret());
        HostedService service = new HostedService(config);

        HostedPaymentData hostedPaymentData = new HostedPaymentData();
        hostedPaymentData.setCustomerCountry("DE");
        hostedPaymentData.setCustomerFirstName("James");
        hostedPaymentData.setCustomerLastName("Mason");
        hostedPaymentData.setMerchantResponseUrl("https://www.example.com/returnUrl");
        hostedPaymentData.setTransactionStatusUrl("https://www.example.com/statusUrl");
        BlockCardType[] blockCardTypes = new BlockCardType[]{ BlockCardType.COMMERCIAL_CREDIT, BlockCardType.COMMERCIAL_DEBIT};
        hostedPaymentData.setBlockCardTypes(blockCardTypes);

        String blockCardTypesToValidate = BlockCardType.COMMERCIAL_CREDIT.getValue() + "|" + BlockCardType.COMMERCIAL_DEBIT.getValue();

        String json = service.charge(new BigDecimal(10.01))
                .withCurrency("EUR")
                .withHostedPaymentData(hostedPaymentData)
                .serialize();

        String response = client.sendRequest(json);
        Transaction parsedResponse = service.parseResponse(response, true);
        String actualBlockCardType = parsedResponse.getResponseValues().get("BLOCK_CARD_TYPE");
        assertEquals(blockCardTypesToValidate, actualBlockCardType);

        assertEquals("00", parsedResponse.getResponseCode());
    }

    @Test
    public void cardBlockingPayment_AllCardTypes() throws ApiException {

        GpEcomConfig config = new GpEcomConfig();
        config.setMerchantId("heartlandgpsandbox");
        config.setAccountId("hpp");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");

        config.setHostedPaymentConfig(new HostedPaymentConfig());
        config.getHostedPaymentConfig().setVersion(HppVersion.Version2);

        GpEcomHppClient client = new GpEcomHppClient(config.getServiceUrl(), config.getSharedSecret());
        HostedService service = new HostedService(config);

        HostedPaymentData hostedPaymentData = new HostedPaymentData();
        hostedPaymentData.setCustomerCountry("DE");
        hostedPaymentData.setCustomerFirstName("James");
        hostedPaymentData.setCustomerLastName("Mason");
        hostedPaymentData.setMerchantResponseUrl("https://www.example.com/returnUrl");
        hostedPaymentData.setTransactionStatusUrl("https://www.example.com/statusUrl");

        BlockCardType[] blockCardTypes = {BlockCardType.CONSUMER_CREDIT,
                BlockCardType.CONSUMER_DEBIT,
                BlockCardType.COMMERCIAL_CREDIT,
                BlockCardType.COMMERCIAL_DEBIT};

        hostedPaymentData.setBlockCardTypes(blockCardTypes);

        String blockCardTypesToValidate = BlockCardType.CONSUMER_CREDIT.getValue()
                + "|" + BlockCardType.CONSUMER_DEBIT.getValue()
                + "|" +  BlockCardType.COMMERCIAL_CREDIT.getValue()
                + "|" +  BlockCardType.COMMERCIAL_DEBIT.getValue();


        String json = service.charge(new BigDecimal(10.01))
                .withCurrency("EUR")
                .withHostedPaymentData(hostedPaymentData)
                .serialize();

        boolean exceptionCaught = false;
        try {
            String response = client.sendRequest(json);
        } catch (Exception e) {
            exceptionCaught = true;
            assertEquals("Unexpected Gateway Response: 561 - All card types are blocked, invalid request", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    /**
     * We can set multiple APMs/LPMs on $presetPaymentMethods, but our HppClient for testing will treat only the first
     * entry from the list as an example for our unit test, in this case will be "sofort"
     */
    public void testBasicChargeAlternativePayment() throws ApiException {
        GpEcomConfig config = new GpEcomConfig();
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

        GpEcomHppClient client = new GpEcomHppClient("https://pay.sandbox.realexpayments.com/pay", "secret");
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
    public void openBankingInitiate() throws ApiException {
        var config = new GpEcomConfig();
        config.setMerchantId("openbankingsandbox");
        config.setSharedSecret("sharedsecret");
        config.setAccountId("internet");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setEnableBankPayment(true);
        config.setEnableLogging(true);

        var hostedPaymentConfig = new HostedPaymentConfig();
        hostedPaymentConfig.setVersion(HppVersion.Version2);

        config.setHostedPaymentConfig(hostedPaymentConfig);

        var hostedPaymentData = new HostedPaymentData();
        hostedPaymentData.setCustomerCountry("DE");
        hostedPaymentData.setCustomerFirstName("James");
        hostedPaymentData.setCustomerLastName("Mason");
        hostedPaymentData.setTransactionStatusUrl("https://www.example.com/statusUrl");
        hostedPaymentData.setMerchantResponseUrl("https://www.example.com/responseUrl");

        hostedPaymentData.setHostedPaymentMethods(new HostedPaymentMethods[]{HostedPaymentMethods.OB});

        var bankPayment = new BankPayment();
        bankPayment.setAccountNumber("12345678");
        bankPayment.setSortCode("406650");
        bankPayment.setAccountName("AccountName");

        var client = new GpEcomHppClient(config.getServiceUrl(), config.getSharedSecret(), ShaHashType.SHA1);
        var service = new HostedService(config);

        var json =
                service
                        .charge(new BigDecimal("10.99"))
                        .withCurrency("GBP")
                        .withPaymentMethod(bankPayment)
                        .withHostedPaymentData(hostedPaymentData)
                        .withRemittanceReference(RemittanceReferenceType.TEXT, "Nike Bounce Shoes")
                        .serialize();

        assertNotNull(json);
        var response = client.sendRequest(json);
        assertNotNull(response);

        var parsedResponse = service.parseResponse(response, true);
        assertEquals("PAYMENT_INITIATED", parsedResponse.getResponseMessage());
    }

    @Test
    public void captureBillingAndShippingInformation() throws ApiException {
        GpEcomConfig config = new GpEcomConfig();
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
        hostedPaymentData.setCaptureAddress(true);
        hostedPaymentData.setReturnAddress(false);
        hostedPaymentData.setRemoveShipping(true);

        String json =
                service
                        .charge(new BigDecimal(19.99))
                        .withCurrency("EUR")
                        .withAddress(billingAddress, AddressType.Billing)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withHostedPaymentData(hostedPaymentData)
                        .serialize();

        JsonDoc jsonResponse = JsonDoc.parse(json);

        assertEquals("TRUE", jsonResponse.getString("HPP_CAPTURE_ADDRESS"));
        assertEquals("FALSE", jsonResponse.getString("HPP_DO_NOT_RETURN_ADDRESS"));
        assertEquals("TRUE", jsonResponse.getString("HPP_REMOVE_SHIPPING"));
    }

    @Test
    public void threeDSExemption() throws ApiException {
        GpEcomConfig config = new GpEcomConfig();
        config.setMerchantId("heartlandgpsandbox");
        config.setAccountId("3dsecure");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://pay.sandbox.realexpayments.com/pay");
        config.setEnableLogging(true);

        HostedPaymentConfig hostedPaymentConfig = new HostedPaymentConfig();
        hostedPaymentConfig.setLanguage("GB");
        hostedPaymentConfig.setResponseUrl("https://www.example.com/response");
        hostedPaymentConfig.setVersion(HppVersion.Version2);

        config.setHostedPaymentConfig(hostedPaymentConfig);

        // data to be passed to the HPP along with transaction level settings
        HostedPaymentData hostedPaymentData = new HostedPaymentData();
        hostedPaymentData.setEnableExemptionOptimization(true);
        hostedPaymentData.setChallengeRequestIndicator(ChallengeRequest.NoChallengeRequested);

        var shippingAddress = new Address();
        shippingAddress.setStreetAddress1("Apartment 852");
        shippingAddress.setStreetAddress2("Complex 741");
        shippingAddress.setStreetAddress3("no");
        shippingAddress.setCity("Chicago");
        shippingAddress.setPostalCode("5001");
        shippingAddress.setState("IL");
        shippingAddress.setCountry("840");

        var billingAddress = new Address();

        billingAddress.setStreetAddress1("Flat 123");
        billingAddress.setStreetAddress2("House 456");
        billingAddress.setStreetAddress3("Cul-De-Sac");
        billingAddress.setCity("Halifax");
        billingAddress.setProvince("West Yorkshire");
        billingAddress.setState("Yorkshire and the Humber");
        billingAddress.setCountry("826");
        billingAddress.setPostalCode("E77 4QJ");

        HostedService service = new HostedService(config);

        //serialize the request
        var json =
                service
                        .charge(new BigDecimal("10.01"))
                        .withCurrency("EUR")
                        .withAddress(billingAddress, AddressType.Billing)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withHostedPaymentData(hostedPaymentData)
                        .serialize();

        assertNotNull(json);

        assertTrue(json.contains("\"HPP_ENABLE_EXEMPTION_OPTIMIZATION\":true"));
    }

    @Test
    public void testBasicChargeCardsAndAlternativePaymentMethods() throws ApiException {
        GpEcomConfig config = new GpEcomConfig();
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
        hostedPaymentData.setHostedPaymentMethods(new HostedPaymentMethods[]{HostedPaymentMethods.CARDS});

        HostedService service = new HostedService(config);

        String json =
                service
                        .charge(new BigDecimal(10.01))
                        .withCurrency("EUR")
                        .withHostedPaymentData(hostedPaymentData)
                        .serialize();

        JsonDoc jsonResponse = JsonDoc.parse(json);

        assertEquals("cards|sofort|testpay|paypal|sepapm", jsonResponse.getString("PM_METHODS"));
        assertEquals(hostedPaymentData.getCustomerFirstName(), jsonResponse.getString("HPP_CUSTOMER_FIRSTNAME"));
        assertEquals(hostedPaymentData.getCustomerLastName(), jsonResponse.getString("HPP_CUSTOMER_LASTNAME"));
        assertEquals(hostedPaymentData.getMerchantResponseUrl(), jsonResponse.getString("MERCHANT_RESPONSE_URL"));
        assertEquals(hostedPaymentData.getTransactionStatusUrl(), jsonResponse.getString("HPP_TX_STATUS_URL"));
        assertEquals(hostedPaymentData.getCustomerCountry(), jsonResponse.getString("HPP_CUSTOMER_COUNTRY"));

        GpEcomHppClient client = new GpEcomHppClient("https://pay.sandbox.realexpayments.com/pay", "secret");
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
    public void testPaymentsSerialization_nullArrays() throws ApiException {
        HostedPaymentMethods[] hostedPaymentMethods = null;

        AlternativePaymentType[] paymentTypesKey = null;

        String expected_PM_METHODS_serialization = null;

        paymentMethodsSerializationTestingHelper(expected_PM_METHODS_serialization, hostedPaymentMethods, paymentTypesKey);
    }

    @Test
    public void testPaymentsSerialization_emptyArrays() throws ApiException {
        HostedPaymentMethods[] hostedPaymentMethods = new HostedPaymentMethods[0];

        AlternativePaymentType[] paymentTypesKey = new AlternativePaymentType[0];

        String expected_PM_METHODS_serialization = null;

        paymentMethodsSerializationTestingHelper(expected_PM_METHODS_serialization, hostedPaymentMethods, paymentTypesKey);
    }

    @Test
    public void testPaymentsSerialization_IDEAL() throws ApiException {
        HostedPaymentMethods[] hostedPaymentMethods = null;

        AlternativePaymentType[] paymentTypesKey = new AlternativePaymentType[1];
        paymentTypesKey[0] = AlternativePaymentType.IDEAL;

        String expected_PM_METHODS_serialization = "ideal";

        paymentMethodsSerializationTestingHelper(expected_PM_METHODS_serialization, hostedPaymentMethods, paymentTypesKey);
    }

    @Test
    public void testPaymentsSerialization_IDEALandPAYPAL() throws ApiException {
        HostedPaymentMethods[] hostedPaymentMethods = null;

        AlternativePaymentType[] paymentTypesKey = new AlternativePaymentType[2];
        paymentTypesKey[0] = AlternativePaymentType.IDEAL;
        paymentTypesKey[1] = AlternativePaymentType.PAYPAL;

        String expected_PM_METHODS_serialization = "ideal|paypal";

        paymentMethodsSerializationTestingHelper(expected_PM_METHODS_serialization, hostedPaymentMethods, paymentTypesKey);
    }

    @Test
    public void testPaymentsSerialization_IDEALandPAYPALandSOFORTUBERWEISUNG() throws ApiException {
        HostedPaymentMethods[] hostedPaymentMethods = null;

        AlternativePaymentType[] paymentTypesKey = new AlternativePaymentType[3];
        paymentTypesKey[0] = AlternativePaymentType.IDEAL;
        paymentTypesKey[1] = AlternativePaymentType.PAYPAL;
        paymentTypesKey[2] = AlternativePaymentType.SOFORTUBERWEISUNG;

        String expected_PM_METHODS_serialization = "ideal|paypal|sofort";

        paymentMethodsSerializationTestingHelper(expected_PM_METHODS_serialization, hostedPaymentMethods, paymentTypesKey);
    }

    @Test
    public void testPaymentsSerialization_OB() throws ApiException {
        HostedPaymentMethods[] hostedPaymentMethods = new HostedPaymentMethods[1];
        hostedPaymentMethods[0] = HostedPaymentMethods.OB;

        AlternativePaymentType[] paymentTypesKey = null;

        String expected_PM_METHODS_serialization = "ob";

        paymentMethodsSerializationTestingHelper(expected_PM_METHODS_serialization, hostedPaymentMethods, paymentTypesKey);
    }

    @Test
    public void testPaymentsSerialization_OBandCARDS() throws ApiException {
        HostedPaymentMethods[] hostedPaymentMethods = new HostedPaymentMethods[2];
        hostedPaymentMethods[0] = HostedPaymentMethods.OB;
        hostedPaymentMethods[1] = HostedPaymentMethods.CARDS;

        AlternativePaymentType[] paymentTypesKey = null;

        String expected_PM_METHODS_serialization = "ob|cards";

        paymentMethodsSerializationTestingHelper(expected_PM_METHODS_serialization, hostedPaymentMethods, paymentTypesKey);
    }

    @Test
    public void testPaymentsSerialization_CARDSandIDEALandPAYPALandSOFORTUBERWEISUNG() throws ApiException {
        HostedPaymentMethods[] hostedPaymentMethods = new HostedPaymentMethods[1];
        hostedPaymentMethods[0] = HostedPaymentMethods.CARDS;

        AlternativePaymentType[] paymentTypesKey = new AlternativePaymentType[3];
        paymentTypesKey[0] = AlternativePaymentType.IDEAL;
        paymentTypesKey[1] = AlternativePaymentType.PAYPAL;
        paymentTypesKey[2] = AlternativePaymentType.SOFORTUBERWEISUNG;

        String expected_PM_METHODS_serialization = "cards|ideal|paypal|sofort";

        paymentMethodsSerializationTestingHelper(expected_PM_METHODS_serialization, hostedPaymentMethods, paymentTypesKey);
    }

    //endregion getPaymentValueList

    private void paymentMethodsSerializationTestingHelper(
            String expectedPayments,
            HostedPaymentMethods[] hostedPaymentMethods,
            AlternativePaymentType[] apmTypes) throws ApiException {

        GpEcomConfig config = new GpEcomConfig();
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

        hostedPaymentData.setPresetPaymentMethods(apmTypes);
        hostedPaymentData.setHostedPaymentMethods(hostedPaymentMethods);

        HostedService service = new HostedService(config);

        String json =
                service
                        .charge(new BigDecimal(10.01))
                        .withCurrency("EUR")
                        .withHostedPaymentData(hostedPaymentData)
                        .serialize();

        JsonDoc jsonResponse = JsonDoc.parse(json);

        assertEquals(expectedPayments, jsonResponse.getString("PM_METHODS"));
    }

}
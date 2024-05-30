package com.global.api.tests.gpEcom;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.RecurringPaymentMethod;
import com.global.api.serviceConfigs.GpEcomConfig;
import lombok.var;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.ArrayList;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GpEcomCreditTest extends BaseGpEComTest {
    private CreditCardData card;

    // Similar to ApiCaseTest.php file in the PHP-SDK
    public GpEcomCreditTest() throws ApiException {
        GpEcomConfig config = gpEComSetup();
        ServicesContainer.configureService(config);

        config.setAccountId("apidcc");
        ServicesContainer.configureService(config, "dcc");

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
    public void creditRefund_withClientTransactionId() throws ApiException {
        Transaction response = card.refund(new BigDecimal("16"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withClientTransactionId("123456789")
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
    public void creditSettle_WithoutAmountCurrency() throws ApiException {
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

    @Test
    public void creditAuthorization_WithMultiAutoSettle() throws ApiException {
        Transaction response = card.authorize(new BigDecimal("14"))
                .withCurrency("USD")
                .withMultiCapture(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void CreditAuthorization_WithDynamicDescriptor() throws ApiException {
        String dynamicDescriptor = "MyCompany LLC";

        Transaction authorization =
                card
                        .authorize(new BigDecimal("5"))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .withDynamicDescriptor(dynamicDescriptor)
                        .execute();

        assertNotNull(authorization);
        assertEquals("00", authorization.getResponseCode());

        Transaction capture =
                authorization
                        .capture(new BigDecimal("5"))
                        .withDynamicDescriptor(dynamicDescriptor)
                        .execute();

        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void dccRateLookup_Charge() throws ApiException {
        card.setNumber("4006097467207025");
        Transaction dccResponse = card.getDccRate(DccRateType.Sale, DccProcessor.Fexco)
                .withAmount(new BigDecimal("10.01"))
                .withCurrency("EUR")
                .execute("dcc");
        assertNotNull(dccResponse);
        assertEquals("00", dccResponse.getResponseCode());

        Transaction saleResponse = card.charge(new BigDecimal("10.01"))
                .withCurrency("EUR")
                .withDccRateData(dccResponse.getDccRateData())
                .withOrderId(dccResponse.getOrderId())
                .execute("dcc");
        assertNotNull(saleResponse);
        assertEquals("00", saleResponse.getResponseCode());
    }

    @Test
    public void dccRateLookup_Auth() throws ApiException {
        card.setNumber("4006097467207025");
        Transaction dccResponse = card.getDccRate(DccRateType.Sale, DccProcessor.Fexco)
                .withAmount(new BigDecimal("10.01"))
                .withCurrency("EUR")
                .execute("dcc");
        assertNotNull(dccResponse);
        assertEquals("00", dccResponse.getResponseCode());

        Transaction authResponse = card.authorize(new BigDecimal("10.01"))
                .withCurrency("EUR")
                .withOrderId(dccResponse.getOrderId())
                .withDccRateData(dccResponse.getDccRateData())
                .execute("dcc");
        assertNotNull(authResponse);
        assertEquals("00", authResponse.getResponseCode());
    }

    @Test(expected = ApiException.class)
    public void googlePay_InvalidToken() throws ApiException {
        String token = "{\"version\":\"EC_v1\",\"data\":\"dvMNzlcy6WNB\",\"header\":{\"ephemeralPublicKey\":\"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEWdNhNAHy9kO2Kol33kIh7k6wh6E\",\"transactionId\":\"fd88874954acdb299c285f95a3202ad1f330d3fd4ebc22a864398684198644c3\",\"publicKeyHash\":\"h7WnNVz2gmpTSkHqETOWsskFPLSj31e3sPTS2cBxgrk\"}}";

        card = new CreditCardData();
        card.setToken(token);
        card.setMobileType(MobilePaymentMethodType.GOOGLEPAY);

        card.charge(new BigDecimal("15"))
                .withCurrency("EUR")
                .execute();

    }

    @Test(expected = ApiException.class)
    public void googlePay_NoToken() throws ApiException {
        card = new CreditCardData();
        card.setMobileType(MobilePaymentMethodType.GOOGLEPAY);

        card.charge(new BigDecimal("15"))
                .withCurrency("EUR")
                .execute();

    }

    @Test(expected = ApiException.class)
    public void googlePay_Charge_NoAmount() throws ApiException {
        String token = "{\"version\":\"EC_v1\",\"data\":\"dvMNzlcy6WNB\",\"header\":{\"ephemeralPublicKey\":\"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEWdNhNAHy9kO2Kol33kIh7k6wh6E\",\"transactionId\":\"fd88874954acdb299c285f95a3202ad1f330d3fd4ebc22a864398684198644c3\",\"publicKeyHash\":\"h7WnNVz2gmpTSkHqETOWsskFPLSj31e3sPTS2cBxgrk\"}}";

        card = new CreditCardData();
        card.setToken(token);
        card.setMobileType(MobilePaymentMethodType.GOOGLEPAY);

        card.charge()
                .withCurrency("USD")
                .execute();

    }

    @Test(expected = ApiException.class)
    public void googlePay_Charge_NoCurrency() throws ApiException {
        String token = "{\"version\":\"EC_v1\",\"data\":\"dvMNzlcy6WNB\",\"header\":{\"ephemeralPublicKey\":\"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEWdNhNAHy9kO2Kol33kIh7k6wh6E\",\"transactionId\":\"fd88874954acdb299c285f95a3202ad1f330d3fd4ebc22a864398684198644c3\",\"publicKeyHash\":\"h7WnNVz2gmpTSkHqETOWsskFPLSj31e3sPTS2cBxgrk\"}}";

        card = new CreditCardData();
        card.setToken(token);
        card.setMobileType(MobilePaymentMethodType.GOOGLEPAY);

        card.charge(new BigDecimal(10))
                .execute();

    }

    @Test(expected = ApiException.class)
    public void applePay_InvalidToken() throws ApiException {
        String token = "{\"version\":\"EC_v1\",\"data\":\"dvMNzlcy6WNB\",\"header\":{\"ephemeralPublicKey\":\"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEWdNhNAHy9kO2Kol33kIh7k6wh6E\",\"transactionId\":\"fd88874954acdb299c285f95a3202ad1f330d3fd4ebc22a864398684198644c3\",\"publicKeyHash\":\"h7WnNVz2gmpTSkHqETOWsskFPLSj31e3sPTS2cBxgrk\"}}";

        card = new CreditCardData();
        card.setToken(token);
        card.setMobileType(MobilePaymentMethodType.APPLEPAY);

        card.charge().execute();

    }

    @Test(expected = ApiException.class)
    public void applePay_Charge_WithoutToken() throws ApiException {
        card = new CreditCardData();
        card.setMobileType(MobilePaymentMethodType.APPLEPAY);

        card.charge().execute();

    }

    @Test
    public void Credit_SupplementaryData() throws ApiException {
        Transaction authorize =
                card
                        .authorize(10)
                        .withCurrency("GBP")
                        .withSupplementaryData("taxInfo", "VATREF", "763637283332")
                        .withSupplementaryData("indentityInfo", "Passport", "PPS736353")
                        .withSupplementaryData("RANDOM_KEY1", "Passport", "PPS736353")
                        .execute();

        assertNotNull(authorize);
        assertEquals("00", authorize.getResponseCode());

        Transaction capture =
                authorize
                        .capture(10)
                        .withCurrency("GBP")
                        .withSupplementaryData("taxInfo", "VATREF", "763637283332")
                        .execute();

        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void fraudManagement_DecisionManager() throws ApiException {
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

        DecisionManager decisionManager = new DecisionManager();
        decisionManager.setBillToHostName("example.com");
        decisionManager.setBillToHttpBrowserCookiesAccepted(true);
        decisionManager.setBillToHttpBrowserEmail("jamesmason@example.com");
        decisionManager.setBillToHttpBrowserType("Mozilla");
        decisionManager.setBillToIpNetworkAddress("123.123.123.123");
        decisionManager.setBusinessRulesCoreThreshold("40");
        decisionManager.setBillToPersonalId("741258963");
        decisionManager.setInvoiceHeaderTenderType("consumer");
        decisionManager.setInvoiceHeaderIsGift(true);
        decisionManager.setDecisionManagerProfile("DemoProfile");
        decisionManager.setInvoiceHeaderReturnsAccepted(true);
        decisionManager.setItemHostHedge(Risk.HIGH);
        decisionManager.setItemNonsensicalHedge(Risk.HIGH);
        decisionManager.setItemObscenitiesHedge(Risk.HIGH);
        decisionManager.setItemPhoneHedge(Risk.HIGH);
        decisionManager.setItemTimeHedge(Risk.HIGH);
        decisionManager.setItemVelocityHedge(Risk.HIGH);

        var products = new ArrayList<Product>();
        products.add(
                new Product()
                        .setProductId("SKU251584")
                        .setProductName("Magazine Subscription")
                        .setQuantity(12)
                        .setUnitPrice(new BigDecimal(12))
                        .setGift(true)
                        .setType("subscription")
                        .setRisk("Low"));

        products.add(
                new Product()
                        .setProductId("SKU8884785")
                        .setProductName("Charger")
                        .setQuantity(10)
                        .setUnitPrice(new BigDecimal(12))
                        .setGift(false)
                        .setType("electronic_good")
                        .setRisk("High"));

        Transaction response = card.charge(new BigDecimal("199.99"))
                .withCurrency("EUR")
                .withAddress(billingAddress, AddressType.Billing)
                .withAddress(shippingAddress, AddressType.Shipping)
                .withDecisionManager(decisionManager)
                .withCustomerData(customer)
                .withMiscProductData(products)
                .withCustomData("fieldValue01", "fieldValue02", "fieldValue03", "fieldValue04")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void FraudManagementDataSubmissionWithRules() throws ApiException {
        final String rule1 = "853c1d37-6e9f-467e-9ffc-182210b40c6b";
        final String rule2 = "f9b93363-4f4e-4d31-b7a2-1f816f461ada";
        FraudRuleCollection rules = new FraudRuleCollection();
        rules.addRule(rule1, FraudFilterMode.Off);
        rules.addRule(rule2, FraudFilterMode.Active);

        // create the card object
        card = new CreditCardData();
        card.setNumber("4263970000005262");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("131");
        card.setCardHolderName("James Mason");

        // supply the customer's billing country and post code for avs checks
        Address billingAddress = new Address();
        billingAddress.setPostalCode("50001|Flat 123");
        billingAddress.setCountry("US");

        // supply the customer's shipping country and post code
        Address shippingAddress = new Address();
        shippingAddress.setPostalCode("654|123");
        shippingAddress.setCountry("FR");

        // create the delayed settle authorization
        Transaction response =
                card
                        .charge(new BigDecimal("10"))
                        .withCurrency("EUR")
                        .withAddress(billingAddress, AddressType.Billing)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withProductId("SID9838383") // prodid
                        .withClientTransactionId("Car Part HV") // varref
                        .withCustomerId("E8953893489") // custnum
                        .withCustomerIpAddress("123.123.123.123")
                        .withFraudFilter(FraudFilterMode.Passive, rules)
                        .execute();

        String responseCode = response.getResponseCode(); // 00 == Success
        String message = response.getResponseMessage(); // [ test system ] AUTHORISED
        // get the response details to save to the DB for future transaction management requests
        String orderId = response.getOrderId();
        String authCode = response.getAuthorizationCode();
        String paymentsReference = response.getTransactionId(); // pasref

        assertNotNull(response);
        assertEquals("00", responseCode);
        assertNotNull(response.getFraudResponse());
        assertEquals(FraudFilterMode.Passive, response.getFraudResponse().getMode());
        assertEquals("PASS", response.getFraudResponse().getResult());
        for (FraudResponse.Rule rule : response.getFraudResponse().getRules()) {
            switch (rule.getId()) {
                case rule1:
                    assertEquals("NOT_EXECUTED", rule.getAction());
                    assertEquals("Block Card Number", rule.getName());
                    break;
                case rule2:
                    assertEquals("PASS", rule.getAction());
                    assertEquals("Block Country", rule.getName());
            }
        }
    }

    @Test
    public void fraudManagement_Hold() throws ApiException {
        Transaction response = card.authorize(new BigDecimal("199.99"))
                .withCurrency("EUR")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction transaction = Transaction.fromId(response.getTransactionId(), response.getOrderId());

        Transaction holdResponse = transaction.hold()
                .withReasonCode(ReasonCode.Fraud)
                .execute();
        assertNotNull(holdResponse);
        assertEquals("00", holdResponse.getResponseCode());
    }

    @Test
    public void fraudManagement_Release() throws ApiException {
        Transaction response = card.authorize(new BigDecimal("199.99"))
                .withCurrency("EUR")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction transaction = Transaction.fromId(response.getTransactionId(), response.getOrderId());

        Transaction holdResponse = transaction.hold()
                .withReasonCode(ReasonCode.Fraud)
                .execute();
        assertNotNull(holdResponse);
        assertEquals("00", holdResponse.getResponseCode());

        Transaction releaseResponse = transaction.release()
                .withReasonCode(ReasonCode.FalsePositive)
                .execute();
        assertNotNull(releaseResponse);
        assertEquals("00", releaseResponse.getResponseCode());
    }

    @Test
    public void fraudManagement_Filter() throws ApiException {
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
                .withFraudFilter(FraudFilterMode.Passive)
                .execute();
        assertNotNull(fraudResponse);
        assertEquals("00", fraudResponse.getResponseCode());
    }

    @Test
    public void fraudManagement_FraudResponse() throws ApiException {
        Transaction response =
                card
                        .authorize(new BigDecimal("199.99"))
                        .withCurrency("EUR")
                        .execute();

        FraudResponse fraudResponse = response.getFraudResponse();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        assertEquals(FraudFilterMode.Active, fraudResponse.getMode());
        assertEquals("PASS", fraudResponse.getResult());

        FraudResponse.Rule rule0 = fraudResponse.getRules().get(0);

        assertEquals("Declines in one hour", rule0.getName());
        assertEquals("0e34d6fd-a50b-4282-93f0-509ecaf2ad19", rule0.getId());
        assertEquals("PASS", rule0.getAction());

        FraudResponse.Rule rule1 = fraudResponse.getRules().get(1);

        assertEquals("Block Card Number", rule1.getName());
        assertEquals("853c1d37-6e9f-467e-9ffc-182210b40c6b", rule1.getId());
        assertEquals("PASS", rule1.getAction());

        FraudResponse.Rule rule2 = fraudResponse.getRules().get(2);

        assertEquals("Block Country", rule2.getName());
        assertEquals("f9b93363-4f4e-4d31-b7a2-1f816f461ada", rule2.getId());
        assertEquals("PASS", rule2.getAction());
    }

//    TODO: At some point should make use of the proxy to send back mock responses we want to parse.
//    @Test
//    public void fraudManagement_FraudResponse_Mapping_01() throws ApiException {
//        String rawResponse = "<response timestamp=\"20210319120046\">\n" +
//                "  <merchantid>nakedwinesukdev</merchantid>\n" +
//                "  <account>ecom3ds</account>\n" +
//                "  <orderid>30931236</orderid>\n" +
//                "  <authcode>12345</authcode>\n" +
//                "  <result>00</result>\n" +
//                "  <cvnresult>U</cvnresult>\n" +
//                "  <avspostcoderesponse>U</avspostcoderesponse>\n" +
//                "  <avsaddressresponse>U</avsaddressresponse>\n" +
//                "  <batchid>-1</batchid>\n" +
//                "  <message>[ test system ] Authorised</message>\n" +
//                "  <pasref>16161552459779525</pasref>\n" +
//                "  <timetaken>0</timetaken>\n" +
//                "  <authtimetaken>0</authtimetaken>\n" +
//                "  <srd>9wbpTsF5Pe7K5Law</srd>\n" +
//                "  <cardissuer>\n" +
//                "    <bank></bank>\n" +
//                "    <country></country>\n" +
//                "    <countrycode></countrycode>\n" +
//                "    <region></region>\n" +
//                "  </cardissuer>\n" +
//                "  <fraudresponse mode=\"ACTIVE\">\n" +
//                "    <result>HOLD</result>\n" +
//                "    <rules>\n" +
//                "      <rule name=\"high risk customers\" id=\"2783b676-db7e-486d-901f-d9db7d977d87\">\n" +
//                "        <action>HOLD</action>\n" +
//                "      </rule>\n" +
//                "      <rule name=\"countryrisk\" id=\"aaddd3e5-8743-4332-888d-71f847af71a9\">\n" +
//                "        <action>PASS</action>\n" +
//                "      </rule>\n" +
//                "    </rules>\n" +
//                "  </fraudresponse>\n" +
//                "  <sha1hash>478c01f38a86cb8a06087c605a51653d3ebd0831</sha1hash>\n" +
//                "</response>";
//
//        Transaction response = new RealexConnector().mapResponse(rawResponse, null);
//
//        FraudResponse fraudResponse = response.getFraudResponse();
//
//        assertNotNull(response);
//        assertEquals("00", response.getResponseCode());
//
//        assertEquals(FraudFilterMode.Active, fraudResponse.getMode());
//        assertEquals("HOLD", fraudResponse.getResult());
//        for(FraudResponse.Rule rule : fraudResponse.getRules()) {
//            assertNotNull(rule.getName());
//            assertNotNull(rule.getId());
//            assertNotNull(rule.getAction());
//        }
//    }

    @Test
    public void supplementaryData() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("GBP")
                .withSupplementaryData("leg", "value1", "value2", "value3")
                .withSupplementaryData("leg", "value1", "value2", "value3")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction captureResponse = response.capture()
                .withSupplementaryData("leg", "value1", "value2", "value3")
                .withSupplementaryData("leg", "value1", "value2", "value3")
                .execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode());
    }

    @Test
    public void cardBlockingPaymentRequest() throws ApiException {
        BlockedCardType cardTypesBlocked = new BlockedCardType();
        cardTypesBlocked.setCommercialDebit(true);
        cardTypesBlocked.setConsumerDebit(true);

        Transaction authorization = card.authorize(14)
                .withCurrency("USD")
                .withBlockedCardType(cardTypesBlocked)
                .execute();

        assertNotNull(authorization);
        assertEquals("00", authorization.getResponseCode());
    }

    @Test
    public void storedCredential_Sale() throws ApiException {
        StoredCredential storedCredential = new StoredCredential();
        storedCredential.setType(StoredCredentialType.OneOff);
        storedCredential.setInitiator(StoredCredentialInitiator.CardHolder);
        storedCredential.setSequence(StoredCredentialSequence.First);

        Transaction response = card.charge(new BigDecimal("15"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withStoredCredential(storedCredential)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertNotNull(response.getSchemeId());
    }

    @Test
    public void storedCredential_OTB() throws ApiException {
        StoredCredential storedCredential = new StoredCredential();
        storedCredential.setType(StoredCredentialType.OneOff);
        storedCredential.setInitiator(StoredCredentialInitiator.CardHolder);
        storedCredential.setSequence(StoredCredentialSequence.First);

        Transaction response = card.verify()
                .withAllowDuplicates(true)
                .withStoredCredential(storedCredential)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertNotNull(response.getSchemeId());
    }

    @Test
    public void storedCredential_ReceiptIn() throws ApiException {
        RecurringPaymentMethod storedCard = new RecurringPaymentMethod("20190729-GlobalApi", "20190729-GlobalApi-Credit");

        StoredCredential storedCredential = new StoredCredential();
        storedCredential.setType(StoredCredentialType.Recurring);
        storedCredential.setInitiator(StoredCredentialInitiator.Merchant);
        storedCredential.setSequence(StoredCredentialSequence.Subsequent);
        storedCredential.setSchemeId("MMC0F00YE4000000715");

        Transaction response = storedCard.authorize(new BigDecimal("15.15"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withStoredCredential(storedCredential)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertNotNull(response.getSchemeId());
    }

    @Test
    public void storedCredential_ReceiptIn_OTB() throws ApiException {
        RecurringPaymentMethod storedCard = new RecurringPaymentMethod("20190729-GlobalApi", "20190729-GlobalApi-Credit");

        StoredCredential storedCredential = new StoredCredential();
        storedCredential.setType(StoredCredentialType.Recurring);
        storedCredential.setInitiator(StoredCredentialInitiator.Merchant);
        storedCredential.setSequence(StoredCredentialSequence.Subsequent);
        storedCredential.setSchemeId("MMC0F00YE4000000715");

        Transaction response = storedCard.verify()
                .withAllowDuplicates(true)
                .withStoredCredential(storedCredential)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertNotNull(response.getSchemeId());
    }

    @Test
    public void optionalFields() throws ApiException {
        Transaction response = card.authorize(new BigDecimal("14"))
                .withCurrency("USD")
                .withCustomerId("E8953893489")
                .withProductId("SID9838383")
                .withClientTransactionId("Car Part HV")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction captureResponse = response.capture()
                .withCustomerId("E8953893489")
                .withProductId("SID9838383")
                .withClientTransactionId("Car Part HV")
                .execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode());

        Transaction refundResponse = response.refund(new BigDecimal("14"))
                .withCustomerId("E8953893489")
                .withProductId("SID9838383")
                .withClientTransactionId("Car Part HV")
                .withCurrency("USD")
                .execute();
        assertNotNull(refundResponse);
        assertEquals("00", refundResponse.getResponseCode());
    }

    @Test
    public void creditChargeWithSurchargeAmount() throws ApiException {
        Transaction authorize = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withSurchargeAmount(new BigDecimal("0.4"), CreditDebitIndicator.Debit)
                .execute();

        assertNotNull(authorize);
        assertEquals("00", authorize.getResponseCode());
    }

    @Test
    public void creditCaptureWithSurchargeAmount() throws ApiException {
        Transaction authorize = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(authorize);
        assertEquals("00", authorize.getResponseCode());
        Transaction capture = authorize.capture(new BigDecimal(4))
                .withSurchargeAmount(new BigDecimal(0.16), CreditDebitIndicator.Debit)
                .execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void creditChargeWithExceededSurchargeAmount() throws ApiException {
        boolean exceptionCaught = false;

        try {
            card.charge(new BigDecimal(10))
                    .withCurrency("USD")
                    .withSurchargeAmount(new BigDecimal(5), CreditDebitIndicator.Debit)
                    .execute();
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("Unexpected Gateway Response: 508 - The surcharge amount is greater than 5% of the transaction amount", e.getMessage());
            assertEquals("508", e.getResponseCode());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

}

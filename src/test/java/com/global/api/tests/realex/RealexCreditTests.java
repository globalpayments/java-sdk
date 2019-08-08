package com.global.api.tests.realex;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Customer;
import com.global.api.entities.DccRateData;
import com.global.api.entities.DecisionManager;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.AddressType;
import com.global.api.entities.enums.DccProcessor;
import com.global.api.entities.enums.DccRateType;
import com.global.api.entities.enums.FraudFilterMode;
import com.global.api.entities.enums.MobilePaymentMethodType;
import com.global.api.entities.enums.ReasonCode;
import com.global.api.entities.enums.RecurringSequence;
import com.global.api.entities.enums.RecurringType;
import com.global.api.entities.enums.Risk;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GatewayConfig;
import org.junit.Test;

import java.math.BigDecimal;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

public class RealexCreditTests {
    private CreditCardData card;

    public RealexCreditTests() throws ApiException {
        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("heartlandgpsandbox");
        config.setAccountId("api");
        config.setSharedSecret("secret");
        config.setRebatePassword("rebate");
        config.setRefundPassword("refund");
        config.setServiceUrl("https://api.sandbox.realexpayments.com/epage-remote.cgi");
        config.setEnableLogging(true);

        ServicesContainer.configureService(config);

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
    public void creditRebateCapture() throws ApiException {
        Transaction response = card.authorize(new BigDecimal("17"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        
        Transaction capture = response.capture(new BigDecimal("14"))
                .execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());

        Transaction rebate = capture.refund(new BigDecimal("17"))
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
    public void creditAuthorization_WithMultiAutoSettle_WithEstNumTxn() throws ApiException {
        Transaction response = card.authorize(new BigDecimal("14"))
                .withEstimatedTransactions(3)
                .withCurrency("USD")
                .withMultiCapture(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    
    @Test
    public void creditMultisettle() throws ApiException {
        Transaction response = card.authorize(new BigDecimal("17"))
                .withEstimatedTransactions(3)
                .withCurrency("USD")
                .withMultiCapture(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction multicapture = response.multicapture(new BigDecimal("5"))
                .withCurrency("USD")
                .execute();
        assertNotNull(multicapture);
        assertEquals("00", multicapture.getResponseCode());
    }
    
    @Test
    public void creditSettleWithMultiSettleParam() throws ApiException {
        Transaction response = card.authorize(new BigDecimal("17"))
                .withEstimatedTransactions(3)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction singlecapture = response.capture(new BigDecimal("5"))
                .withCurrency("USD")
                .withEstimatedTransactions(3)
                .isFinal(true)
                .execute();
        assertNotNull(singlecapture);
        assertEquals("00", singlecapture.getResponseCode());
    }
    
    @Test
    public void creditMultisettle_WithEstNumTxn() throws ApiException {
        Transaction response = card.authorize(new BigDecimal("17"))
                .withCurrency("USD")
                .withMultiCapture(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction multicapture = response.multicapture(new BigDecimal("5"))
                .withEstimatedTransactions(3)
                .withCurrency("USD")
                .execute();
        assertNotNull(multicapture);
        assertEquals("00", multicapture.getResponseCode());
    }
    
    @Test
    public void creditMultisettle_WithFinalFlag() throws ApiException {
        Transaction response = card.authorize(new BigDecimal("17"))
                .withCurrency("USD")
                .withMultiCapture(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction multicapture = response.multicapture(new BigDecimal("5"))
                .isFinal(true)
                .withCurrency("USD")
                .execute();
        assertNotNull(multicapture);
        assertEquals("00", multicapture.getResponseCode());
    }
    
    @Test
    public void creditMultisettle_EqualEstNumTxn_EqualAmount() throws ApiException {
        Transaction response = card.authorize(new BigDecimal("15"))
                .withCurrency("USD")
                .withEstimatedTransactions(3)
                .withMultiCapture(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        
        Integer result = 0;
        Transaction multicapture = response.multicapture(new BigDecimal("5"))
                .withCurrency("USD")
                .execute();
        assertNotNull(multicapture);
        result += Integer.parseInt(multicapture.getResponseCode());
        //assertEquals("00", multicapture.getResponseCode());
        
        multicapture = response.multicapture(new BigDecimal("5"))
                .withCurrency("USD")
                .execute();
        assertNotNull(multicapture);
        result += Integer.parseInt(multicapture.getResponseCode());
        //assertEquals("00", multicapture02.getResponseCode());
        
        multicapture = response.multicapture(new BigDecimal("5"))
                .withCurrency("USD")
                .execute();
        assertNotNull(multicapture);
        result += Integer.parseInt(multicapture.getResponseCode());
        assertEquals("0", result.toString());
    }
    
    @Test(expected = ApiException.class)
    public void creditMultisettle_EqualEstNumTxn_AboveAmount() throws ApiException {
        Transaction response = card.authorize(new BigDecimal("15"))
                .withCurrency("USD")
                .withEstimatedTransactions(3)
                .withMultiCapture(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        
        Integer result = 0;
        Transaction multicapture;
        multicapture = response.multicapture(new BigDecimal("10"))
                .withCurrency("USD")
                .execute();
        assertNotNull(multicapture);
        result += Integer.parseInt(multicapture.getResponseCode());
        
        multicapture = response.multicapture(new BigDecimal("5"))
                .withCurrency("USD")
                .execute();
        assertNotNull(multicapture);
        result += Integer.parseInt(multicapture.getResponseCode());
        
        multicapture = response.multicapture(new BigDecimal("5"))
                .withCurrency("USD")
                .execute();
        assertNotNull(multicapture);
        result += Integer.parseInt(multicapture.getResponseCode());
        assertEquals("508", result.toString());
    }
    
    @Test
    public void creditMultisettle_EqualEstNumTxn_BelowAmount() throws ApiException {
        Transaction response = card.authorize(new BigDecimal("15"))
                .withCurrency("USD")
                .withEstimatedTransactions(3)
                .withMultiCapture(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        
        Integer result = 0;
        Transaction multicapture = response.multicapture(new BigDecimal("3"))
                .withCurrency("USD")
                .execute();
        assertNotNull(multicapture);
        result += Integer.parseInt(multicapture.getResponseCode());
        
        multicapture = response.multicapture(new BigDecimal("3"))
                .withCurrency("USD")
                .execute();
        assertNotNull(multicapture);
        result += Integer.parseInt(multicapture.getResponseCode());
        
        multicapture = response.multicapture(new BigDecimal("3"))
                .withCurrency("USD")
                .execute();
        assertNotNull(multicapture);
        result += Integer.parseInt(multicapture.getResponseCode());
        assertEquals("0", result.toString());
    }
    
    @Test(expected = ApiException.class)    // Sandbox does not check EstNumTxn. Production would generate a 508 response code
    public void creditMultisettle_ImplicitFinal_BelowAmount() throws ApiException {
        Transaction response = card.authorize(new BigDecimal("15"))
                .withCurrency("USD")
                .withEstimatedTransactions(3)
                .withMultiCapture(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        
        Integer result = 0;
        Transaction multicapture = response.multicapture(new BigDecimal("2"))
                .withCurrency("USD")
                .execute();
        assertNotNull(multicapture);
        result += Integer.parseInt(multicapture.getResponseCode());
        
        multicapture = response.multicapture(new BigDecimal("2"))
                .withCurrency("USD")
                .execute();
        assertNotNull(multicapture);
        result += Integer.parseInt(multicapture.getResponseCode());
        
        multicapture = response.multicapture(new BigDecimal("2"))
                .withCurrency("USD")
                .execute();
        assertNotNull(multicapture);
        result += Integer.parseInt(multicapture.getResponseCode());
        
        multicapture = response.multicapture(new BigDecimal("2"))
                .withCurrency("USD")
                .execute();
        assertNotNull(multicapture);
        result += Integer.parseInt(multicapture.getResponseCode());
        assertEquals("508", result.toString());
    }
    
    @Test(expected = ApiException.class)    // Sandbox does not check Final flag. Production would generate a 508 response code
    public void creditMultisettle_ExplicitFinal_BelowAmount() throws ApiException {
        Transaction response = card.authorize(new BigDecimal("15"))
                .withCurrency("USD")
                .withEstimatedTransactions(3)
                .withMultiCapture(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        
        Integer result = 0;
        Transaction multicapture = response.multicapture(new BigDecimal("2"))
                .withCurrency("USD")
                .execute();
        assertNotNull(multicapture);
        result += Integer.parseInt(multicapture.getResponseCode());
        
        multicapture = response.multicapture(new BigDecimal("2"))
                .withCurrency("USD")
                .execute();
        assertNotNull(multicapture);
        result += Integer.parseInt(multicapture.getResponseCode());
        
        multicapture = response.multicapture(new BigDecimal("2"))
                .withCurrency("USD")
                .isFinal(true)
                .execute();
        assertNotNull(multicapture);
        result += Integer.parseInt(multicapture.getResponseCode());
        
        multicapture = response.multicapture(new BigDecimal("2"))
                .withCurrency("USD")
                .execute();
        assertNotNull(multicapture);
        result += Integer.parseInt(multicapture.getResponseCode());
        assertEquals("508", result.toString());
    }

    @Test(expected = ApiException.class)
    public void creditDccRateLookup_ChargeNotEnabledAccount() throws ApiException {
        DccRateData dccRateData = card.getDccRate(DccRateType.Sale, new BigDecimal("10.01"), "EUR", DccProcessor.Fexco);
        card.charge(new BigDecimal("10.01"))
                .withCurrency("EUR")
                .withOrderId(dccRateData.getOredrId())
                .withDccRateData(dccRateData)
                .execute();

    }

    @Test(expected = ApiException.class)
    public void creditDccRateLookup_AuthNotEnabledAccount() throws ApiException {
        DccRateData dccRateData = card.getDccRate(DccRateType.Sale, new BigDecimal("10.01"), "EUR", DccProcessor.Fexco);
        card.authorize(new BigDecimal("10.01"))
                .withCurrency("EUR")
                .withOrderId(dccRateData.getOredrId())
                .withDccRateData(dccRateData)
                .execute();

    }

    @Test(expected = ApiException.class)
    public void creditChargeGooglePay_InvalidToken() throws ApiException {
        String token = "{\"version\":\"EC_v1\",\"data\":\"dvMNzlcy6WNB\",\"header\":{\"ephemeralPublicKey\":\"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEWdNhNAHy9kO2Kol33kIh7k6wh6E\",\"transactionId\":\"fd88874954acdb299c285f95a3202ad1f330d3fd4ebc22a864398684198644c3\",\"publicKeyHash\":\"h7WnNVz2gmpTSkHqETOWsskFPLSj31e3sPTS2cBxgrk\"}}";

        card = new CreditCardData();
        card.setToken(token);
        card.setMobileType(MobilePaymentMethodType.GOOGLEPAY);

        card.charge(new BigDecimal("15"))
                .withCurrency("EUR")
                .execute();

    }

    @Test(expected = ApiException.class)
    public void creditChargeGooglePay_WithoutToken() throws ApiException {
        card = new CreditCardData();
        card.setMobileType(MobilePaymentMethodType.GOOGLEPAY);

        card.charge(new BigDecimal("15"))
                .withCurrency("EUR")
                .execute();

    }

    @Test(expected = ApiException.class)
    public void creditChargeGooglePay_WithoutAmountCurrency() throws ApiException {
        String token = "{\"version\":\"EC_v1\",\"data\":\"dvMNzlcy6WNB\",\"header\":{\"ephemeralPublicKey\":\"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEWdNhNAHy9kO2Kol33kIh7k6wh6E\",\"transactionId\":\"fd88874954acdb299c285f95a3202ad1f330d3fd4ebc22a864398684198644c3\",\"publicKeyHash\":\"h7WnNVz2gmpTSkHqETOWsskFPLSj31e3sPTS2cBxgrk\"}}";

        card = new CreditCardData();
        card.setToken(token);
        card.setMobileType(MobilePaymentMethodType.GOOGLEPAY);

        card.charge().execute();

    }

    @Test(expected = ApiException.class)
    public void creditChargeApplePay_InvalidToken() throws ApiException {
        String token = "{\"version\":\"EC_v1\",\"data\":\"dvMNzlcy6WNB\",\"header\":{\"ephemeralPublicKey\":\"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEWdNhNAHy9kO2Kol33kIh7k6wh6E\",\"transactionId\":\"fd88874954acdb299c285f95a3202ad1f330d3fd4ebc22a864398684198644c3\",\"publicKeyHash\":\"h7WnNVz2gmpTSkHqETOWsskFPLSj31e3sPTS2cBxgrk\"}}";

        card = new CreditCardData();
        card.setToken(token);
        card.setMobileType(MobilePaymentMethodType.APPLEPAY);

        card.charge().execute();

    }

    @Test(expected = ApiException.class)
    public void creditChargeApplePay_WithoutToken() throws ApiException {
        card = new CreditCardData();
        card.setMobileType(MobilePaymentMethodType.APPLEPAY);

        card.charge().execute();

    }

    @Test
    public void creditAuthorizationWithSupplementaryData() throws ApiException {
        Transaction response = card.authorize(new BigDecimal("129.99"))
                .withCurrency("EUR")
                .withSupplementaryData("taxInfo", "VATREF", "763637283332")
                .withSupplementaryData("indentityInfo", "Passport", "PPS736353")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSaleWithSupplementaryData() throws ApiException {
        Transaction response = card.charge(new BigDecimal("129.99"))
                .withCurrency("EUR")
                .withSupplementaryData("taxInfo", "VATREF", "763637283332")
                .withSupplementaryData("indentityInfo", "Passport", "PPS736353")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void creditfraudManagementDecisionManager() throws ApiException {
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
        decisionManager.setBusinessRulessCoreThresHold("40");
        decisionManager.setBillToPersonalId("741258963");
        decisionManager.setInvoiceHeaderTenderType("consumer");
        decisionManager.setInvoiceHeaderIsGift(true);
        decisionManager.setDecisionManagerProfile("DemoProfile");
        decisionManager.setInvoiceHeaderReturnsAccepted(true);
        decisionManager.setItemHostHedge(Risk.HIGH);
        decisionManager.setItemNonsensicalHedge(Risk.HIGH);
        decisionManager.setItemObscenitiesHedge(Risk.HIGH);
        decisionManager.setItemPhoneFedge(Risk.HIGH);
        decisionManager.setItemTimeFedge(Risk.HIGH);
        decisionManager.setItemVelocityHedge(Risk.HIGH);

        Transaction response = card.charge(new BigDecimal("199.99"))
                .withCurrency("EUR")
                .withAddress(billingAddress, AddressType.Billing)
                .withAddress(shippingAddress, AddressType.Shipping)
                .withDecisionManager(decisionManager)
                .withCustomerData(customer)
                .withMiscProductData("SKU251584", "Magazine Subscription", "12", "1200", "true", "subscription", "Low")
                .withMiscProductData("SKU8884784", "Charger", "10", "1200", "false", "electronic_good", "High")
                .withCustomData("fieldValue01", "fieldValue02", "fieldValue03", "fieldValue04")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void creditFraudManagementHold() throws ApiException {
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
    public void creditFraudManagementRelease() throws ApiException {
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
    public void creditFraudFilterDataSubmission() throws ApiException {
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
    public void test_supplementaryData() throws ApiException {
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
}
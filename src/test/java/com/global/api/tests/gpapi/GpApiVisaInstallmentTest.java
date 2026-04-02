package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.paymentMethods.*;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.entities.enums.*;
import com.global.api.services.*;
import org.joda.time.DateTime;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Test class for Visa Installment features using GP API.
 * <p>
 * This class contains integration and negative test cases for:
 * <ul>
 *     <li>Credit sale transactions with Visa Installment program</li>
 *     <li>Querying and retrieving installment plans</li>
 *     <li>Hosted Payment Page (HPP) link creation with installments</li>
 *     <li>Validation of error scenarios (missing/invalid data)</li>
 *     <li>Regular transactions without installments</li>
 * </ul>
 * <p>
 * Uses JUnit 5 for testing.
 */
class GpApiVisaInstallmentTest {

    /**
     * Test Visa card data.
     */
    private CreditCardData visaCard;

    /**
     * Default address for transactions.
     */
    private Address address;

    /**
     * Default transaction currency.
     */
    private String currency = "USD";

    /**
     * Default transaction amount.
     */
    private BigDecimal amount = new BigDecimal("99.99");

    /**
     * Customer object for tests.
     */
    private Customer newCustomer;

    /**
     * Shipping address for HPP link tests.
     */
    private Address shippingAddress;

    /**
     * Billing address for HPP link tests.
     */
    private Address billingAddress;

    /**
     * Sets up test data and configures services before each test.
     */
    @BeforeEach
    public void setup() throws ConfigurationException {
        ServicesContainer.configureService(setUpConfig());
        ServicesContainer.configureService(setUpInstallmentConfig(), "installments");

        address = createAddress("123 Main St.", "Downtown", "NJ", "12345", "US", null);

        visaCard = createCreditCardData("4622943127052828", 12, 2025, "999", false, false);

        newCustomer = createCustomer("James", "Mason", "jamesmason@example.com", "en", "NEW");

        shippingAddress = createAddress("Apartment 852", "Chicago", "IL", "5001", "US", "840", "Complex 741", "no");
        billingAddress = createAddress("Apartment 852", "Chicago", null, "5001", "US", null, "Complex 741", "no");
    }

    /**
     * Creates an Address object with basic fields.
     */
    private Address createAddress(String street1, String city, String state, String postal, String country, String countryCode) {
        return createAddress(street1, city, state, postal, country, countryCode, null, null);
    }

    /**
     * Creates an Address object with all fields.
     */
    private Address createAddress(String street1, String city, String state, String postal, String country, String countryCode, String street2, String street3) {
        Address addr = new Address();
        addr.setStreetAddress1(street1);
        if (street2 != null) addr.setStreetAddress2(street2);
        if (street3 != null) addr.setStreetAddress3(street3);
        addr.setCity(city);
        if (state != null) addr.setState(state);
        addr.setPostalCode(postal);
        if (countryCode != null) addr.setCountryCode(countryCode);
        addr.setCountry(country);
        return addr;
    }

    /**
     * Creates a CreditCardData object.
     */
    private CreditCardData createCreditCardData(String number, int expMonth, int expYear, String cvn, boolean cardPresent, boolean readerPresent) {
        CreditCardData card = new CreditCardData();
        card.setNumber(number);
        card.setExpMonth(expMonth);
        card.setExpYear(expYear);
        card.setCvn(cvn);
        card.setCardPresent(cardPresent);
        card.setReaderPresent(readerPresent);
        return card;
    }

    /**
     * Creates a Customer object.
     */
    private Customer createCustomer(String firstName, String lastName, String email, String language, String status) {
        Customer customer = new Customer();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(email);
        customer.setLanguage(language);
        customer.setStatus(status);
        return customer;
    }

    /**
     * Sets up the main GP API configuration.
     */
    public GpApiConfig setUpConfig() {
        GpApiConfig config = new GpApiConfig();
        config.setAppId("hkjrcsGDhWiDt8GEhoDMKy3pzFz5R0Bo");
        config.setAppKey("cQOKHoAAvNIcEN8s");
        config.setChannel(Channel.CardNotPresent);
        config.setEnvironment(Environment.TEST);
        config.setServiceUrl("https://apis.sandbox.boipagateway.com/ucp");
        config.setCountry("GB");
        config.setEnableLogging(true);
        config.setRequestLogger(new RequestConsoleLogger());
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setTransactionProcessingAccountName("GPECOM_Installments_Processing");
        config.setAccessTokenInfo(accessTokenInfo);
        return config;
    }

    /**
     * Sets up the GP API configuration for installments.
     */
    public GpApiConfig setUpInstallmentConfig() {
        GpApiConfig config = new GpApiConfig();
        config.setAppId("hkjrcsGDhWiDt8GEhoDMKy3pzFz5R0Bo");
        config.setAppKey("cQOKHoAAvNIcEN8s");
        config.setChannel(Channel.CardNotPresent);
        config.setEnvironment(Environment.TEST);
        config.setCountry("GB");
        config.setEnableLogging(true);
        config.setRequestLogger(new RequestConsoleLogger());
        config.setServiceUrl("https://apis.sandbox.globalpay.com/ucp");
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setTransactionProcessingAccountName("GPECOM_Installments_Processing");
        config.setAccessTokenInfo(accessTokenInfo);
        return config;
    }

    /**
     * Creates an Installment query object for plan lookup.
     */
    public Installment createInstallmentQuery(BigDecimal amount, String currency) {
        Installment installment = new Installment();
        installment.setAccountName("GPECOM_Installments_Processing");
        installment.setChannel("CNP");
        installment.setAmount(amount);
        installment.setCurrency(currency);
        installment.setCountry("GB");
        installment.setProgram("VIS");
        installment.setReference("QUERY-" + UUID.randomUUID().toString());
        installment.setFundingMode(String.valueOf(FundingMode.CONSUMER_FUNDED));
        installment.setEligiblePlans(EligiblePlans.LIMITED);
        installment.setEntryMode("ECOM");
        installment.setCurrency("GBP");

        Terms terms = new Terms();
        terms.setMaxTimeUnitNumber("24");
        terms.setMaxAmount("1000");
        installment.setInstallmentTerms(terms);

        installment.setCreditCardData(createCreditCardData("4263970000005262", 12, 2027, null, false, false));

        return installment;
    }

    /**
     * Creates InstallmentData for use in transactions.
     */
    public InstallmentData createInstallmentData(String program, String reference) {
        InstallmentData data = new InstallmentData();
        data.setProgram(program);
        if (reference != null) {
            data.setReference(reference);
        }
        data.setCount("12");
        Terms terms = new Terms();
        terms.setLanguage("fre");
        terms.setVersion("2");
        data.setTerms(terms);
        return data;
    }

    /**
     * Tests a credit sale transaction with Visa Installment program.
     */
    @Test
    void testCreditSaleWithVisProgram() throws ApiException {
        Transaction response = visaCard.charge(new BigDecimal(1000))
                .withCurrency("GBP")
                .withAddress(address)
                .withInstallmentData(createInstallmentData("VIS", "109bbbf5-c027-de5e-50c1-153dafa9ac03"))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
        assertEquals("VIS", response.getInstallmentData().getProgram());
    }

    /**
     * Tests querying available installment plans.
     */
    @Test
    void testQueryInstallmentPlans() throws ApiException {
        Installment response = createInstallmentQuery(new BigDecimal("100"), "GBP").create("installments");
        assertNotNull(response);
        assertNotNull(response.getId());
        assertNotNull(response.getTerms());
        assertFalse(response.getTerms().isEmpty());
    }

    /**
     * Tests retrieving an installment by its ID.
     */
    @Test
    void testGetInstallmentWithInstallmentId() throws ApiException {
        Installment queryResponse = createInstallmentQuery(new BigDecimal("100"), "GBP").create("installments");
        assertNotNull(queryResponse);
        assertNotNull(queryResponse.getId());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }

        Installment installmentDetails = InstallmentService.get(queryResponse.getId(), "installments");
        assertNotNull(installmentDetails);
        assertNotNull(installmentDetails.getId());
        assertEquals(queryResponse.getId(), installmentDetails.getId());
    }

    /**
     * Tests retrieving a transaction with associated installment data.
     */
    @Test
    void testGetTransactionWithInstallment() throws ApiException {
        Transaction transactionResponse = visaCard.charge(new BigDecimal(1000))
                .withCurrency("GBP")
                .withAddress(address)
                .withInstallmentData(createInstallmentData("VIS", "109bbbf5-c027-de5e-50c1-153dafa9ac03"))
                .withAllowDuplicates(true)
                .execute();

        assertNotNull(transactionResponse);
        assertEquals("SUCCESS", transactionResponse.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transactionResponse.getResponseMessage());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {
        }

        TransactionSummary retrievedTransaction = ReportingService.transactionDetail(transactionResponse.getTransactionId())
                .execute();

        assertNotNull(retrievedTransaction);
        assertEquals(transactionResponse.getTransactionId(), retrievedTransaction.getTransactionId());
        assertNotNull(retrievedTransaction.getInstallmentData());
        assertNotNull(retrievedTransaction.getInstallmentData().getId());
        assertNotNull(retrievedTransaction.getInstallmentData().getProgram());
    }

    /**
     * Tests creating a Hosted Payment Page (HPP) link with installment data.
     */
    @Test
    void testCreateHPPLinkWithInstallments() throws ApiException {
        InstallmentData installmentData = createInstallmentData("VIS", null);
        installmentData.setFundingMode("CONSUMER_FUNDED");
        Terms terms = new Terms();
        terms.setMaxTimeUnitNumber("24");
        terms.setMaxAmount("200000");
        installmentData.setTerms(terms);

        PayByLinkData payByLink = new PayByLinkData();

        payByLink.setType(PayByLinkType.HOSTED_PAYMENT_PAGE);
        payByLink.setUsageMode(PaymentMethodUsageMode.SINGLE);
        payByLink.setAllowedPaymentMethods(new String[]{PaymentMethodName.Card.getValue(Target.GP_API), PaymentMethodName.BankPayment.getValue(Target.GP_API)});
        payByLink.setUsageLimit(1);
        payByLink.setName("Mobile");
        payByLink.isShippable(false);
        payByLink.setShippingAmount(new BigDecimal("1.23"));
        payByLink.setReturnUrl("https://www.example.com/returnUrl");
        payByLink.setStatusUpdateUrl("https://www.example.com/statusUrl");
        payByLink.setCancelUrl("https://www.example.com/cancelUrl");
        payByLink.setIsDccEnabled(false);
        payByLink.setPaymentMethodConfiguration(getPaymentMethodConfiguration());
        payByLink.setExpirationDate(DateTime.now().plusDays(10));
        payByLink.setInstallmentData(installmentData);

        newCustomer.setIsShippingAddressSameAsBilling(false);

        BigDecimal amount = new BigDecimal("2000.00");
        Transaction response =
                PayByLinkService
                        .create(payByLink, amount)
                        .withCurrency("USD")
                        .withClientTransactionId("TestOrder-123")
                        .withDescription("HPP_Links_Test")
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withAddress(billingAddress, AddressType.Billing)
                        .withCustomer(newCustomer)
                        .withPhoneNumber("99", "1801555999", PhoneNumberType.Shipping)
                        .withPayByLinkData(payByLink)
                        .execute("installments");

        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(PayByLinkStatus.ACTIVE.toString(), response.getResponseMessage());
        assertNotNull(response.getPayByLinkResponse().getUrl());
        assertNotNull(response.getPayByLinkResponse().getId());
    }

    /**
     * Tests error handling for missing card details in installment query.
     */
    @Test
    void testQueryInstallmentWithMissingCardDetails() {
        assertThrows(Exception.class, () -> {
            Installment installment = new Installment();
            installment.setAccountName("GPECOM_Installments_Processing");
            installment.setAmount(new BigDecimal("10000"));
            installment.setCurrency("GBP");
            installment.setProgram("VIS");
            // Missing card details - should fail
            installment.create("installments");
        });
    }

    /**
     * Tests error handling for retrieving a non-existent installment.
     */
    @Test
    void testGetNonExistentInstallment() {
        assertThrows(com.global.api.entities.exceptions.GatewayException.class, () -> {
            InstallmentService.get("NON_EXISTENT_ID_12345", "installments");
        });
    }

    /**
     * Tests error handling for missing terms in installment query.
     */
    @Test
    void testQueryInstallmentWithoutTerms() {
        assertThrows(Exception.class, () -> {
            Installment installment = new Installment();
            installment.setAccountName("GPECOM_Installments_Processing");
            installment.setChannel("CNP");
            installment.setAmount(new BigDecimal("10000"));
            installment.setCurrency("GBP");
            installment.setProgram("VIS");
            // Missing terms - should fail

            installment.setCreditCardData(createCreditCardData("4263970000005262", 12, 2027, null, false, false));

            installment.create("installments");
        });
    }

    /**
     * Tests error handling for zero amount transaction.
     */
    @Test
    void testTransactionWithZeroAmount() {
        assertThrows(com.global.api.entities.exceptions.GatewayException.class, () -> {
            visaCard.charge(BigDecimal.ZERO)
                    .withCurrency(currency)
                    .withAddress(address)
                    .withInstallmentData(createInstallmentData("VIS", null))
                    .execute();
        });
    }

    /**
     * Tests a regular transaction without installments.
     */
    @Test
    void testRegularTransactionWithoutInstallments() throws ApiException {
        Transaction response = visaCard.charge(amount)
                .withCurrency(currency)
                .withAddress(address)
                .execute();

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
        assertNull(response.getInstallmentData());
    }

    /**
     * Tests error handling for invalid currency in installment query.
     */
    @Test
    void testQueryInstallmentWithInvalidCurrency() {
        assertThrows(Exception.class, () -> {
            Installment installment = new Installment();
            installment.setAccountName("GPECOM_Installments_Processing");
            installment.setAmount(new BigDecimal("10000"));
            installment.setCurrency("INVALID_CURRENCY");
            installment.setProgram("VIS");
            installment.setCreditCardData(createCreditCardData("4263970000005262", 12, 2027, null, false, false));
            Terms terms = new Terms();
            terms.setMaxTimeUnitNumber("24");
            terms.setMaxAmount("1000");
            installment.setInstallmentTerms(terms);
            installment.create("installments");
        });
    }

    /**
     * Tests error handling for negative amount in installment creation.
     */
    @Test
    void testCreateInstallmentWithNegativeAmount() {
        assertThrows(Exception.class, () -> {
            Installment installment = new Installment();
            installment.setAccountName("GPECOM_Installments_Processing");
            installment.setAmount(new BigDecimal("-100"));
            installment.setCurrency("GBP");
            installment.setProgram("VIS");
            installment.setCreditCardData(createCreditCardData("4263970000005262", 12, 2027, null, false, false));
            Terms terms = new Terms();
            terms.setMaxTimeUnitNumber("24");
            terms.setMaxAmount("1000");
            installment.setInstallmentTerms(terms);
            installment.create("installments");
        });
    }

    /**
     * Tests error handling for null program in installment creation.
     */
    @Test
    void testCreateInstallmentWithNullProgram() {
        assertThrows(Exception.class, () -> {
            Installment installment = new Installment();
            installment.setAccountName("GPECOM_Installments_Processing");
            installment.setAmount(new BigDecimal("10000"));
            installment.setCurrency("GBP");
            // Program is null
            installment.setCreditCardData(createCreditCardData("4263970000005262", 12, 2027, null, false, false));
            Terms terms = new Terms();
            terms.setMaxTimeUnitNumber("24");
            terms.setMaxAmount("1000");
            installment.setInstallmentTerms(terms);
            installment.create("installments");
        });
    }

    /**
     * Tests error handling for expired card in installment creation.
     */
    @Test
    void testCreateInstallmentWithExpiredCard() {
        assertThrows(Exception.class, () -> {
            Installment installment = new Installment();
            installment.setAccountName("GPECOM_Installments_Processing");
            installment.setAmount(new BigDecimal("10000"));
            installment.setCurrency("GBP");
            installment.setProgram("VIS");
            installment.setCreditCardData(createCreditCardData("4263970000005262", 1, 2020, null, false, false));
            Terms terms = new Terms();
            terms.setMaxTimeUnitNumber("24");
            terms.setMaxAmount("1000");
            installment.setInstallmentTerms(terms);
            installment.create("installments");
        });
    }

    /**
     * Tests error handling for null installment ID in get operation.
     */
    @Test
    void testGetInstallmentWithInstallmentIdNull() {
        ApiException exception = assertThrows(ApiException.class, () -> {
            InstallmentService.get(null, "installments");
        });
        assertEquals("Installment id is mandatory and cannot be null", exception.getMessage());
    }

    /**
     * Returns a PaymentMethodConfiguration for HPP link creation.
     */
    private static PaymentMethodConfiguration getPaymentMethodConfiguration() {
        PaymentMethodConfiguration paymentMethodConfiguration = new PaymentMethodConfiguration();
        paymentMethodConfiguration.setStorageMode(StorageMode.OFF);
        paymentMethodConfiguration.setExemptStatus("LOW_VALUE");
        paymentMethodConfiguration.setIsBillingAddressRequired(false);
        paymentMethodConfiguration.setIsShippableAddressEnabled(true);
        paymentMethodConfiguration.setIsAddressOverrideAllowed(false);
        paymentMethodConfiguration.setChallengeRequestIndicator(ChallengeRequestIndicator.NoChallengeRequested);
        return paymentMethodConfiguration;
    }
}

package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.PhoneNumber;
import com.global.api.entities.Product;
import com.global.api.entities.User;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.payFac.BankAccountData;
import com.global.api.entities.payFac.PaymentStatistics;
import com.global.api.entities.payFac.Person;
import com.global.api.entities.payFac.UserPersonalData;
import com.global.api.entities.reporting.MerchantAccountSummaryPaged;
import com.global.api.entities.reporting.MerchantSummaryPaged;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.PayFacService;
import com.global.api.services.ReportingService;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class GpApiMerchantsOnboardTest extends BaseGpApiTest {

    private PayFacService payFacService;
    private CreditCardData card;

    @Before
    public void TestInitialize() throws ConfigurationException {
        payFacService = new PayFacService();

        GpApiConfig gpApiConfig = new GpApiConfig();
        gpApiConfig.setAppId(APP_ID_FOR_MERCHANT);
        gpApiConfig.setAppKey(APP_KEY_FOR_MERCHANT);
        gpApiConfig.setEnvironment(Environment.TEST);
        gpApiConfig.setChannel(Channel.CardNotPresent.getValue());
        gpApiConfig.setEnableLogging(true);

        ServicesContainer.configureService(gpApiConfig);

        card = new CreditCardData();
        card.setNumber("4263970000005262");
        card.setExpMonth(expMonth);
        card.setExpYear(expYear);
        card.setCvn("123");
        card.setCardPresent(true);
    }

    @Test
    public void BoardMerchant() throws ApiException {
        UserPersonalData merchantData = GetMerchantData();
        List<Product> productData = GetProductList();
        List<Person> persons = GetPersonList("");
        CreditCardData creditCardInformation = card;
        BankAccountData bankAccountInformation = GetBankAccountData();
        PaymentStatistics paymentStatistics = GetPaymentStatistics();

        User merchant =
                payFacService
                        .createMerchant()
                        .withUserPersonalData(merchantData)
                        .withDescription("Merchant Business Description")
                        .withProductData(productData)
                        .withCreditCardData(creditCardInformation, PaymentMethodFunction.PRIMARY_PAYOUT)
                        .withBankAccountData(bankAccountInformation, PaymentMethodFunction.SECONDARY_PAYOUT)
                        .withPersonsData(persons)
                        .withPaymentStatistics(paymentStatistics)
                        .execute();

        assertEquals("SUCCESS", merchant.getResponseCode());
        assertEquals(UserStatus.UNDER_REVIEW, merchant.getUserReference().getUserStatus());
        assertNotNull(merchant.getUserReference().getUserId());
    }

    @Test
    public void BoardMerchant_OnlyMandatory() throws ApiException {
        UserPersonalData merchantData = GetMerchantData();
        List<Product> productData = GetProductList();
        List<Person> persons = GetPersonList(null);
        PaymentStatistics paymentStatistics = GetPaymentStatistics();

        User merchant =
                payFacService
                        .createMerchant()
                        .withUserPersonalData(merchantData)
                        .withDescription("Merchant Business Description")
                        .withProductData(productData)
                        .withPersonsData(persons)
                        .withPaymentStatistics(paymentStatistics)
                        .execute();

        assertEquals("SUCCESS", merchant.getResponseCode());
        assertEquals(UserStatus.UNDER_REVIEW, merchant.getUserReference().getUserStatus());
        assertEquals(merchantData.getUserName(), merchant.getName());
        assertEquals("Merchant Boarding in progress", merchant.getStatusDescription());
        assertNotNull(merchant.getUserReference().getUserId());
    }

    @Test
    public void BoardMerchant_WithIdempotency() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();
        UserPersonalData merchantData = GetMerchantData();
        List<Product> productData = GetProductList();
        List<Person> persons = GetPersonList(null);
        PaymentStatistics paymentStatistics = GetPaymentStatistics();

        User merchant =
                payFacService
                        .createMerchant()
                        .withUserPersonalData(merchantData)
                        .withDescription("Merchant Business Description")
                        .withProductData(productData)
                        .withPersonsData(persons)
                        .withPaymentStatistics(paymentStatistics)
                        .withIdempotencyKey(idempotencyKey)
                        .execute();

        assertEquals("SUCCESS", merchant.getResponseCode());
        assertEquals(UserStatus.UNDER_REVIEW, merchant.getUserReference().getUserStatus());
        assertNotNull(merchant.getUserReference().getUserId());

        merchantData.setFirstName("James " + DateTime.now().toString("yyyyMMddmmss"));

        boolean exceptionCaught = false;
        try {
            payFacService
                    .createMerchant()
                    .withUserPersonalData(merchantData)
                    .withDescription("Merchant Business Description")
                    .withProductData(productData)
                    .withPersonsData(persons)
                    .withPaymentStatistics(paymentStatistics)
                    .withIdempotencyKey(idempotencyKey)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 409 - Idempotency Key seen before: id=" + merchant.getUserReference().getUserId() + ", status=" + merchant.getUserReference().getUserStatus(), ex.getMessage());
            assertEquals("DUPLICATE_ACTION", ex.getResponseCode());
            assertEquals("40039", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void BoardMerchant_DuplicateMerchantName() throws ApiException {
        UserPersonalData merchantData = GetMerchantData();
        List<Product> productData = GetProductList();
        List<Person> persons = GetPersonList(null);
        PaymentStatistics paymentStatistics = GetPaymentStatistics();

        User merchant =
                payFacService
                        .createMerchant()
                        .withUserPersonalData(merchantData)
                        .withDescription("Merchant Business Description")
                        .withProductData(productData)
                        .withPersonsData(persons)
                        .withPaymentStatistics(paymentStatistics)
                        .execute();

        assertEquals("SUCCESS", merchant.getResponseCode());
        assertEquals(UserStatus.UNDER_REVIEW, merchant.getUserReference().getUserStatus());
        assertNotNull(merchant.getUserReference().getUserId());

        boolean exceptionCaught = false;
        try {
            payFacService
                    .createMerchant()
                    .withUserPersonalData(merchantData)
                    .withDescription("Merchant Business Description")
                    .withProductData(productData)
                    .withPersonsData(persons)
                    .withPaymentStatistics(paymentStatistics)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Duplicate Merchant Name", ex.getMessage());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void BoardMerchant_WithoutMerchantData() {
        List<Product> productData = GetProductList();
        List<Person> persons = GetPersonList(null);
        PaymentStatistics paymentStatistics = GetPaymentStatistics();

        boolean exceptionCaught = false;
        try {
            payFacService
                    .createMerchant()
                    .withDescription("Merchant Business Description")
                    .withProductData(productData)
                    .withPersonsData(persons)
                    .withPaymentStatistics(paymentStatistics)
                    .execute();
        } catch (Exception ex) {
            exceptionCaught = true;
            assertEquals("userPersonalData cannot be null for this transaction type.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void BoardMerchant_WithoutUserName() {
        UserPersonalData merchantData = GetMerchantData();
        merchantData.setUserName(null);

        List<Product> productData = GetProductList();
        List<Person> persons = GetPersonList(null);
        PaymentStatistics paymentStatistics = GetPaymentStatistics();
        boolean exceptionCaught = false;

        try {
            payFacService
                    .createMerchant()
                    .withUserPersonalData(merchantData)
                    .withDescription("Merchant Business Description")
                    .withProductData(productData)
                    .withPersonsData(persons)
                    .withPaymentStatistics(paymentStatistics)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields name", ex.getMessage());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40005", ex.getResponseText());
        } catch (ApiException e) {
            throw new RuntimeException(e);
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void GetMerchantInfo() throws ApiException {
        String merchantId = "MER_98f60f1a397c4dd7b7167bda61520292";
        User merchant =
                payFacService
                        .getMerchantInfo(merchantId)
                        .execute();

        assertNotNull(merchant);
        assertEquals(merchantId, merchant.getUserReference().getUserId());
    }

    @Test
    public void GetMerchantInfo_RandomId() throws ApiException {
        String merchantId = "MER_" + UUID.randomUUID().toString().replace("-", "");

        boolean exceptionCaught = false;
        try {
            payFacService
                    .getMerchantInfo(merchantId)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Merchant configuration does not exist for the following combination: MMA_1595ca59906346beae43d92c24863430 , " + merchantId, ex.getMessage());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40041", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void GetMerchantInfo_InvalidId() throws ApiException {
        String merchantId = UUID.randomUUID().toString();

        boolean exceptionCaught = false;
        try {
            payFacService
                    .getMerchantInfo(merchantId)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 404 - Retrieve information about this transaction is not supported", ex.getMessage());
            assertEquals("INVALID_TRANSACTION_ACTION", ex.getResponseCode());
            assertEquals("40042", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void SearchMerchants() throws ApiException {
        MerchantSummaryPaged merchants =
                ReportingService
                        .findMerchants(1, 10)
                        .execute();

        assertTrue(merchants.getTotalRecordCount() > 0);
        assertTrue(merchants.getResults().size() <= 10);
    }

    @Test
    public void SearchMerchantAccounts() throws ApiException {
        MerchantSummaryPaged merchants =
                ReportingService
                        .findMerchants(1, 10)
                        .where(SearchCriteria.MerchantStatus, MerchantAccountStatus.ACTIVE)
                        .execute();

        assertTrue(merchants.getResults().size() > 0);
        assertTrue(merchants.getResults().size() <= 10);

        String merchantId = merchants.getResults().get(0).getId();

        GpApiConfig gpApiConfigForMerchantId = new GpApiConfig();

        gpApiConfigForMerchantId.setAppId(APP_ID_FOR_MERCHANT);
        gpApiConfigForMerchantId.setAppKey(APP_KEY_FOR_MERCHANT);
        gpApiConfigForMerchantId.setEnvironment(Environment.TEST);
        gpApiConfigForMerchantId.setChannel(Channel.CardNotPresent.getValue());
        gpApiConfigForMerchantId.setMerchantId(merchantId);
        gpApiConfigForMerchantId.setEnableLogging(true);

        ServicesContainer.configureService(gpApiConfigForMerchantId, "accounts");

        MerchantAccountSummaryPaged accounts =
                ReportingService
                        .findAccounts(1, 10)
                        .where(SearchCriteria.AccountStatus, MerchantAccountStatus.ACTIVE)
                        .execute("accounts");

        assertNotNull(accounts);
        assertTrue(accounts.getResults().size() > 0);
    }

    @Test
    public void EditMerchantApplicantInfo() throws ApiException {
        MerchantSummaryPaged merchants =
                ReportingService
                        .findMerchants(1, 1)
                        .execute();

        assertTrue(merchants.getTotalRecordCount() > 0);
        assertEquals(1, merchants.getResults().size());

        User merchant = User.fromId(merchants.getResults().get(0).getId(), UserType.MERCHANT);
        List<Person> persons = GetPersonList("Update");

        User response =
                merchant
                        .edit()
                        .withPersonsData(persons)
                        .withDescription("Update merchant payment processing")
                        .execute();

        assertEquals("PENDING", response.getResponseCode());
        assertEquals("Merchant Editing in progress", response.getStatusDescription());
        assertEquals(UserType.MERCHANT, response.getUserReference().getUserType());
        assertEquals(UserStatus.UNDER_REVIEW, response.getUserReference().getUserStatus());
    }

    @Test
    public void EditMerchantPaymentProcessing() throws ApiException {
        MerchantSummaryPaged merchants =
                ReportingService
                        .findMerchants(1, 1)
                        .execute();

        assertTrue(merchants.getTotalRecordCount() > 0);
        assertEquals(1, merchants.getResults().size());

        PaymentStatistics paymentStatistics = new PaymentStatistics();
        paymentStatistics.setTotalMonthlySalesAmount(new BigDecimal(1111));
        paymentStatistics.setHighestTicketSalesAmount(new BigDecimal(2222));

        User merchant = User.fromId(merchants.getResults().get(0).getId(), UserType.MERCHANT);

        User response =
                merchant
                        .edit()
                        .withPaymentStatistics(paymentStatistics)
                        .withDescription("Update merchant payment processing")
                        .execute();

        assertEquals("PENDING", response.getResponseCode());
        assertEquals("Merchant Editing in progress", response.getStatusDescription());
        assertEquals(UserType.MERCHANT, response.getUserReference().getUserType());
        assertEquals(UserStatus.UNDER_REVIEW, response.getUserReference().getUserStatus());
    }

    @Test
    public void EditMerchantBusinessInformation() throws ApiException {
        MerchantSummaryPaged merchants =
                ReportingService
                        .findMerchants(1, 1)
                        .execute();

        assertTrue(merchants.getTotalRecordCount() > 0);
        assertEquals(1, merchants.getResults().size());

        User merchant = User.fromId(merchants.getResults().get(0).getId(), UserType.MERCHANT);
        merchant.getUserReference().setUserStatus(UserStatus.ACTIVE);

        UserPersonalData merchantData =
                new UserPersonalData()
                        .setFirstName("Username")
                        .setDBA("Doing Business As")
                        .setWebsite("https://abcd.com")
                        .setTaxIdReference("987654321");

        Address businessAddress =
                new Address()
                        .setStreetAddress1("Apartment 852")
                        .setStreetAddress2("Complex 741")
                        .setStreetAddress3("Unit 4")
                        .setCity("Chicago")
                        .setState("IL")
                        .setPostalCode("50001")
                        .setCountryCode("840");

        merchantData.setUserAddress(businessAddress);

        User response =
                merchant
                        .edit()
                        .withUserPersonalData(merchantData)
                        //.withDescription("Sample Data for description")
                        .execute();

        assertEquals("PENDING", response.getResponseCode());
        assertEquals(UserStatus.UNDER_REVIEW, response.getUserReference().getUserStatus());
        assertEquals(merchants.getResults().get(0).getName(), response.getName());
    }

    @Test
    public void EditMerchant_RemoveMerchantFromPartner_FewArguments() throws ApiException {
        MerchantSummaryPaged merchants =
                ReportingService
                        .findMerchants(1, 1)
                        .execute();

        assertTrue(merchants.getTotalRecordCount() > 0);
        assertEquals(1, merchants.getResults().size());

        User merchant = User.fromId(merchants.getResults().get(0).getId(), UserType.MERCHANT);

        boolean exceptionCaught = false;
        try {
            merchant
                    .edit()
                    .withStatusChangeReason(StatusChangeReason.REMOVE_PARTNERSHIP)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Required field is missing.", ex.getMessage());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40241", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void EditMerchant_RemoveMerchantFromPartner_TooManyArguments() throws ApiException {
        MerchantSummaryPaged merchants =
                ReportingService
                        .findMerchants(1, 1)
                        .execute();

        assertTrue(merchants.getTotalRecordCount() > 0);
        assertEquals(1, merchants.getResults().size());

        User merchant = User.fromId(merchants.getResults().get(0).getId(), UserType.MERCHANT);

        boolean exceptionCaught = false;
        try {
            merchant
                    .edit()
                    .withUserPersonalData(GetMerchantData())
                    .withStatusChangeReason(StatusChangeReason.REMOVE_PARTNERSHIP)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Bad Request. The request has extra tags which are not required.", ex.getMessage());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40268", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void BoardMerchant_WithoutLegalName() {
        UserPersonalData merchantData = GetMerchantData();
        merchantData.setLegalName(null);

        List<Product> productData = GetProductList();
        List<Person> persons = GetPersonList(null);
        PaymentStatistics paymentStatistics = GetPaymentStatistics();
        boolean exceptionCaught = false;

        try {
            payFacService
                    .createMerchant()
                    .withUserPersonalData(merchantData)
                    .withDescription("Merchant Business Description")
                    .withProductData(productData)
                    .withPersonsData(persons)
                    .withPaymentStatistics(paymentStatistics)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields legal_name", ex.getMessage());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40005", ex.getResponseText());
        } catch (ApiException e) {
            throw new RuntimeException(e);
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void BoardMerchant_WithoutDba() {
        UserPersonalData merchantData = GetMerchantData();
        merchantData.setDBA(null);

        List<Product> productData = GetProductList();
        List<Person> persons = GetPersonList(null);
        PaymentStatistics paymentStatistics = GetPaymentStatistics();
        boolean exceptionCaught = false;

        try {
            payFacService
                    .createMerchant()
                    .withUserPersonalData(merchantData)
                    .withDescription("Merchant Business Description")
                    .withProductData(productData)
                    .withPersonsData(persons)
                    .withPaymentStatistics(paymentStatistics)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields dba", ex.getMessage());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40005", ex.getResponseText());
        } catch (ApiException e) {
            throw new RuntimeException(e);
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void BoardMerchant_WithoutWebsite() {
        UserPersonalData merchantData = GetMerchantData();
        merchantData.setWebsite(null);

        List<Product> productData = GetProductList();
        List<Person> persons = GetPersonList(null);
        PaymentStatistics paymentStatistics = GetPaymentStatistics();
        boolean exceptionCaught = false;

        try {
            payFacService
                    .createMerchant()
                    .withUserPersonalData(merchantData)
                    .withDescription("Merchant Business Description")
                    .withProductData(productData)
                    .withPersonsData(persons)
                    .withPaymentStatistics(paymentStatistics)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields website", ex.getMessage());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40005", ex.getResponseText());
        } catch (ApiException e) {
            throw new RuntimeException(e);
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void BoardMerchant_WithoutNotificationStatusUrl() {
        UserPersonalData merchantData = GetMerchantData();
        merchantData.setNotificationStatusUrl(null);

        List<Product> productData = GetProductList();
        List<Person> persons = GetPersonList(null);
        PaymentStatistics paymentStatistics = GetPaymentStatistics();
        boolean exceptionCaught = false;

        try {
            payFacService
                    .createMerchant()
                    .withUserPersonalData(merchantData)
                    .withDescription("Merchant Business Description")
                    .withProductData(productData)
                    .withPersonsData(persons)
                    .withPaymentStatistics(paymentStatistics)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields notifications.status_url", ex.getMessage());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40005", ex.getResponseText());
        } catch (ApiException e) {
            throw new RuntimeException(e);
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void BoardMerchant_WithoutPersons() {
        UserPersonalData merchantData = GetMerchantData();
        List<Product> productData = GetProductList();
        PaymentStatistics paymentStatistics = GetPaymentStatistics();
        boolean exceptionCaught = false;

        try {
            payFacService
                    .createMerchant()
                    .withUserPersonalData(merchantData)
                    .withDescription("Merchant Business Description")
                    .withProductData(productData)
                    .withPaymentStatistics(paymentStatistics)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields : email", ex.getMessage());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40005", ex.getResponseText());
        } catch (ApiException e) {
            throw new RuntimeException(e);
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void BoardMerchant_WithoutPaymentStatistics() {
        UserPersonalData merchantData = GetMerchantData();
        List<Product> productData = GetProductList();
        List<Person> persons = GetPersonList(null);
        boolean exceptionCaught = false;

        try {
            payFacService
                    .createMerchant()
                    .withUserPersonalData(merchantData)
                    .withDescription("Merchant Business Description")
                    .withProductData(productData)
                    .withPersonsData(persons)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields payment_processing_statistics.total_monthly_sales_amount", ex.getMessage());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40005", ex.getResponseText());
        } catch (ApiException e) {
            throw new RuntimeException(e);
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void BoardMerchant_WithoutTotalMonthlySalesAmount() {
        UserPersonalData merchantData = GetMerchantData();
        List<Product> productData = GetProductList();
        List<Person> persons = GetPersonList(null);
        PaymentStatistics paymentStatistics = new PaymentStatistics()
                .setAverageTicketSalesAmount(new BigDecimal(50000))
                .setHighestTicketSalesAmount(new BigDecimal(60000));

        boolean exceptionCaught = false;

        try {
            payFacService
                    .createMerchant()
                    .withUserPersonalData(merchantData)
                    .withDescription("Merchant Business Description")
                    .withProductData(productData)
                    .withPersonsData(persons)
                    .withPaymentStatistics(paymentStatistics)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields payment_processing_statistics.total_monthly_sales_amount", ex.getMessage());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40005", ex.getResponseText());
        } catch (ApiException e) {
            throw new RuntimeException(e);
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void BoardMerchant_WithoutAverageTicketSalesAmount() {
        UserPersonalData merchantData = GetMerchantData();
        List<Product> productData = GetProductList();
        List<Person> persons = GetPersonList(null);
        PaymentStatistics paymentStatistics = new PaymentStatistics()
                .setTotalMonthlySalesAmount(new BigDecimal(3000000))
                .setHighestTicketSalesAmount(new BigDecimal(60000));

        boolean exceptionCaught = false;

        try {
            payFacService
                    .createMerchant()
                    .withUserPersonalData(merchantData)
                    .withDescription("Merchant Business Description")
                    .withProductData(productData)
                    .withPersonsData(persons)
                    .withPaymentStatistics(paymentStatistics)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields payment_processing_statistics.average_ticket_sales_amount", ex.getMessage());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40005", ex.getResponseText());
        } catch (ApiException e) {
            throw new RuntimeException(e);
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void BoardMerchant_WithoutHighestTicketSalesAmount() {
        UserPersonalData merchantData = GetMerchantData();
        List<Product> productData = GetProductList();
        List<Person> persons = GetPersonList(null);
        PaymentStatistics paymentStatistics = new PaymentStatistics()
                .setTotalMonthlySalesAmount(new BigDecimal(3000000))
                .setAverageTicketSalesAmount(new BigDecimal(50000));

        boolean exceptionCaught = false;

        try {
            payFacService
                    .createMerchant()
                    .withUserPersonalData(merchantData)
                    .withDescription("Merchant Business Description")
                    .withProductData(productData)
                    .withPersonsData(persons)
                    .withPaymentStatistics(paymentStatistics)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields payment_processing_statistics.highest_ticket_sales_amount", ex.getMessage());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40005", ex.getResponseText());
        } catch (ApiException e) {
            throw new RuntimeException(e);
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void BoardMerchant_WithoutDescription() {
        UserPersonalData merchantData = GetMerchantData();
        List<Product> productData = GetProductList();
        List<Person> persons = GetPersonList(null);
        PaymentStatistics paymentStatistics = GetPaymentStatistics();

        boolean exceptionCaught = false;

        try {
            payFacService
                    .createMerchant()
                    .withUserPersonalData(merchantData)
                    .withProductData(productData)
                    .withPersonsData(persons)
                    .withPaymentStatistics(paymentStatistics)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields description", ex.getMessage());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40005", ex.getResponseText());
        } catch (ApiException e) {
            throw new RuntimeException(e);
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    private List<Person> GetPersonList(String type) {
        ArrayList<Person> persons = new ArrayList<>();

        Person person = new Person();

        person.setFunctions(PersonFunctions.APPLICANT);
        person.setFirstName("James " + type);
        person.setMiddleName("Mason " + type);
        person.setLastName("Doe " + " " + type);
        person.setEmail("uniqueemail@address.com");
        person.setDateOfBirth(DateTime.parse("1982-02-23").toString("yyyy-MM-dd"));
        person.setNationalIdReference("123456789");
        person.setJobTitle("CEO");
        person.setEquityPercentage("25");
        person.setAddress(new Address());
        person.getAddress().setStreetAddress1("1 Business Address");
        person.getAddress().setStreetAddress2("Suite 2");
        person.getAddress().setStreetAddress3("1234");
        person.getAddress().setCity("Atlanta");
        person.getAddress().setState("GA");
        person.getAddress().setPostalCode("30346");
        person.getAddress().setCountry("US");
        person.setHomePhone(
                new PhoneNumber()
                        .setCountryCode("01")
                        .setNumber("8008675309"));
        person.setWorkPhone(
                new PhoneNumber()
                        .setCountryCode("01")
                        .setNumber("8008675309"));

        persons.add(person);

        return persons;
    }

    private List<Product> GetProductList() {
        ArrayList<Product> productList = new ArrayList<>();

        productList.add(
                new Product()
                        .setProductId("PRO_TRA_CP-US-CARD-A920_SP"));

        productList.add(
                new Product()
                        .setProductId("PRO_TRA_CNP_US_BANK-TRANSFER_PP"));

        productList.add(
                new Product()
                        .setProductId("PRO_FMA_PUSH-FUNDS_PP"));

        productList.add(
                new Product()
                        .setProductId("PRO_TRA_CNP-US-CARD_PP"));

        return productList;
    }

    private PaymentStatistics GetPaymentStatistics() {
        return
                new PaymentStatistics()
                        .setTotalMonthlySalesAmount(new BigDecimal(3000000))
                        .setAverageTicketSalesAmount(new BigDecimal(50000))
                        .setHighestTicketSalesAmount(new BigDecimal(60000));
    }

    private BankAccountData GetBankAccountData() {
        return
                new BankAccountData()
                        .setAccountHolderName("Bank Account Holder Name")
                        .setAccountNumber("123456788")
                        .setAccountOwnershipType("Personal")
                        .setAccountType(AccountType.Savings.getValue(Target.DEFAULT))
                        .setRoutingNumber("102000076");
    }

    private UserPersonalData GetMerchantData() {
        UserPersonalData merchantData =
                new UserPersonalData()
                        .setUserName("CERT_Propay_" + DateTime.now().toString("yyyyMMddmmss"))
                        .setLegalName("Business Legal Name")
                        .setDBA("Doing Business As")
                        .setMerchantCategoryCode(5999)
                        .setWebsite("https://example.com/")
                        .setNotificationEmail("merchant@example.com")
                        .setCurrencyCode("USD")
                        .setTaxIdReference("123456789")
                        .setTier("test")
                        .setType(UserType.MERCHANT);

        Address businessAddress =
                new Address()
                        .setStreetAddress1("Apartment 852")
                        .setStreetAddress2("Complex 741")
                        .setStreetAddress3("Unit 4")
                        .setCity("Chicago")
                        .setState("IL")
                        .setPostalCode("50001")
                        .setCountryCode("840");

        merchantData.setUserAddress(businessAddress);

        Address shippingAddress =
                new Address()
                        .setStreetAddress1("Flat 456")
                        .setStreetAddress2("House 789")
                        .setStreetAddress3("Basement Flat")
                        .setCity("Halifax")
                        .setPostalCode("W5 9HR")
                        .setCountryCode("826");

        merchantData.setMailingAddress(shippingAddress);
        merchantData.setNotificationStatusUrl("https://www.example.com/notifications/status");

        return merchantData;
    }

}
package com.global.api.tests.gpEcom;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.RecurringPaymentMethod;
import com.global.api.serviceConfigs.GpEcomConfig;
import com.global.api.services.RecurringService;
import com.global.api.services.Secure3dService;
import com.global.api.utils.GenerationUtils;
import org.joda.time.DateTime;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GpEcomRecurringTest extends BaseGpEComTest {
    private Customer new_customer;
    private CreditCardData card;

    private String customerId() {
        return String.format("%s-GlobalApi", new SimpleDateFormat("yyyyMMddHH").format(new Date()));
    }

    private String paymentId(String type) {
        return String.format("%s-GlobalApi-%s", new SimpleDateFormat("yyyyMMddHH").format(new Date()), type);
    }

    public GpEcomRecurringTest() throws ApiException {
        GpEcomConfig config = gpEComSetup();
        config.setAccountId("3dsecure");
        config.setSharedSecret("secret");
        config.setChannel("ECOM");

        ServicesContainer.configureService(config);

        Address address = new Address();
        address.setStreetAddress1("Flat 123");
        address.setStreetAddress2("House 456");
        address.setStreetAddress3("The Cul-De-Sac");
        address.setCity("Halifax");
        address.setProvince("West Yorkshire");
        address.setPostalCode("W6 9HR");
        address.setCountry("United Kingdom");

        new_customer = new Customer();
        new_customer.setKey(customerId());
        new_customer.setTitle("Mr.");
        new_customer.setFirstName("James");
        new_customer.setLastName("Mason");
        new_customer.setCompany("Realex Payments");
        new_customer.setAddress(address);
        new_customer.setHomePhone("+35312345678");
        new_customer.setWorkPhone("+3531987654321");
        new_customer.setFax("+124546871258");
        new_customer.setMobilePhone("+25544778544");
        new_customer.setEmail("text@example.com");
        new_customer.setComments("Campaign Ref E7373G");

        card = new CreditCardData();
        card.setNumber("4263970000005262");
        card.setExpMonth(5);
        card.setExpYear(DateTime.now().getYear() + 2);
        card.setCardHolderName("James Mason");
    }

    @Test
    @Order(1)
    public void Test_001a_CreateCustomer() throws ApiException {
        try {
            Customer customer = new_customer.create();
            assertNotNull(customer);
        } catch (GatewayException exc) {
            // check for already created
            if (!exc.getResponseCode().equals("501"))
                throw exc;
        }
    }

    @Test
    @Order(2)
    public void Test_001b_CreatePaymentMethod() throws ApiException {
        try {
            RecurringPaymentMethod paymentMethod = new_customer.addPaymentMethod(paymentId("Credit"), card).create();
            assertNotNull(paymentMethod);
        } catch (GatewayException exc) {
            // check for already created
            if (!exc.getResponseCode().equals("520"))
                throw exc;
        }
    }

    @Test
    @Order(3)
    public void Test_001c_CreatePaymentMethodWithStoredCredential() throws ApiException {
        try {
            StoredCredential storedCredential = new StoredCredential();
            storedCredential.setSchemeId("YOUR_DESIRED_SCHEME_ID");

            RecurringPaymentMethod paymentMethod =
                    new_customer
                            .addPaymentMethod(paymentId("Credit") + UUID.randomUUID().toString().substring(0, 5), card, storedCredential)
                            .create();

            assertNotNull(paymentMethod);
        } catch (GatewayException exc) {
            // check for already created
            if (!exc.getResponseCode().equals("520"))
                throw exc;
        }
    }

    @Test
    @Order(4)
    public void Test_001d_CardStorage3DSFlow() throws ApiException {
        RecurringPaymentMethod paymentMethod = new RecurringPaymentMethod(customerId(), paymentId("Credit"));

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(paymentMethod)
                        .withCurrency("USD")
                        .withAmount(new BigDecimal(10))
                        .execute();

        assertNotNull(secureEcom);
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertNull(secureEcom.getEci());
        assertTrue(secureEcom.isEnrolled());
    }

    @Test
    @Order(5)
    public void Test_002a_EditCustomer() throws ApiException {
        Customer customer = new Customer();
        customer.setKey(customerId());
        customer.setFirstName("Perry");
        customer.saveChanges();
    }

    @Test
    @Order(6)
    public void Test_002b_EditPaymentMethod() throws ApiException {
        RecurringPaymentMethod paymentMethod = new RecurringPaymentMethod(customerId(), paymentId("Credit"));
        CreditCardData newCard = new CreditCardData();
        newCard.setNumber("5425230000004415");
        newCard.setExpMonth(10);
        newCard.setExpYear(2025);
        newCard.setCardHolderName("Philip Marlowe");

        paymentMethod.setPaymentMethod(newCard);
        paymentMethod.saveChanges();
    }

    @Test
    @Order(7)
    public void Test_002c_EditPaymentMethodWithStoredCredential() throws ApiException {
        RecurringPaymentMethod paymentMethod = new RecurringPaymentMethod(customerId(), paymentId("Credit"));
        CreditCardData newCard = new CreditCardData();
        newCard.setNumber("5425230000004415");
        newCard.setExpMonth(10);
        newCard.setExpYear(2025);
        newCard.setCardHolderName("Philip Marlowe");

        paymentMethod.setPaymentMethod(newCard);
        StoredCredential storedCredential = new StoredCredential();
        storedCredential.setSchemeId("YOUR_DESIRED_SCHEME_ID");
        paymentMethod.setStoredCredential(storedCredential);
        paymentMethod.saveChanges();
    }

    @Test
    @Order(8)
    public void Test_002c_EditPaymentMethodExpOnly() throws ApiException {
        RecurringPaymentMethod paymentMethod = new RecurringPaymentMethod(customerId(), paymentId("Credit"));
        CreditCardData card = new CreditCardData();
        card.setCardType("MC");
        card.setExpMonth(10);
        card.setExpYear(2025);
        card.setCardHolderName("Philip Marlowe");

        paymentMethod.setPaymentMethod(card);
        paymentMethod.saveChanges();
    }

    @Test
    @Order(9)
    public void Test_003_FindOnRealex() throws ApiException {
        assertThrows(UnsupportedTransactionException.class, () -> {
            Customer.find(customerId());
        });
    }

    @Test
    @Order(10)
    public void Test_004a_ChargeStoredCard() throws ApiException {
        checkCustomer();
        RecurringPaymentMethod paymentMethod = checkPaymentMethod();

        Transaction response = paymentMethod.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    @Order(11)
    public void Test_004b_VerifyStoredCard() throws ApiException {
        RecurringPaymentMethod paymentMethod = new RecurringPaymentMethod(customerId(), paymentId("Credit"));
        Transaction response = paymentMethod.verify()
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    @Order(12)
    public void Test_004c_RefundStoredCard() throws ApiException {
        RecurringPaymentMethod paymentMethod = new RecurringPaymentMethod(customerId(), paymentId("Credit"));
        Transaction response = paymentMethod.refund(new BigDecimal("10.01"))
                .withCurrency("USD")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    @Order(13)
    public void Test_005_RecurringPayment() throws ApiException {
        RecurringPaymentMethod paymentMethod = new RecurringPaymentMethod(customerId(), paymentId("Credit"));
        Transaction response = paymentMethod.charge(new BigDecimal("12"))
                .withRecurringInfo(RecurringType.Fixed, RecurringSequence.First)
                .withCurrency("USD")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    @Order(14)
    public void Test_006_DeletePaymentMethod() throws ApiException {
        RecurringPaymentMethod paymentMethod = new RecurringPaymentMethod(customerId(), paymentId("Credit"));
        paymentMethod.delete(false);
    }

    // Negative Test Cases
    @Test
    @Order(15)
    public void Test_007_EditPaymentMethod_Invalid_Name() {
        assertThrows(ApiException.class, () -> {
            RecurringPaymentMethod paymentMethod = new RecurringPaymentMethod(customerId(), paymentId("Credit"));
            CreditCardData newCard = new CreditCardData();
            newCard.setNumber("5425230000004415");
            newCard.setExpMonth(1000);
            newCard.setExpYear(2020);
            newCard.setCardHolderName(null);

            paymentMethod.setPaymentMethod(newCard);
            paymentMethod.saveChanges();
        });
    }

    @Test
    @Order(16)
    public void Test_008_EditPaymentMethod_Invalid_Card() {
        assertThrows(ApiException.class, () -> {
            RecurringPaymentMethod paymentMethod = new RecurringPaymentMethod(customerId(), paymentId("Credit"));
            CreditCardData newCard = new CreditCardData();
            newCard.setNumber("542523");
            newCard.setExpMonth(1000);
            newCard.setExpYear(2020);
            newCard.setCardHolderName("Philip Marlowe");

            paymentMethod.setPaymentMethod(newCard);
            paymentMethod.saveChanges();
        });
    }

    @Test
    @Order(17)
    public void Test_009_DccRateLookup_AuthNotEnabledAccount() {
        assertThrows(ApiException.class, () -> {
            RecurringPaymentMethod paymentMethod = new RecurringPaymentMethod(
                    "038cb8bc-0289-48cf-a5ad-8bfbe54e204a",
                    "fe1bb177-0a35-421c-9b0e-c7623712387c"
            );

            Transaction dccResponse = paymentMethod.getDccRate(DccRateType.Sale, DccProcessor.Fexco)
                    .withAmount(new BigDecimal("10.01"))
                    .withCurrency("EUR")
                    .execute();

            assertNotNull(dccResponse);
            assertEquals("00", dccResponse.getResponseCode());

            Transaction response = paymentMethod.authorize(new BigDecimal("10.01"))
                    .withCurrency("EUR")
                    .withOrderId(dccResponse.getOrderId())
                    .withDccRateData(dccResponse.getDccRateData())
                    .execute();

            assertNotNull(response);
            assertEquals("00", response.getResponseCode());
        });
    }

    @Test
    @Order(18)
    public void Test_010_DccRateLookup_ChargeNotEnabledAccount() {
        assertThrows(ApiException.class, () -> {
            RecurringPaymentMethod paymentMethod = new RecurringPaymentMethod(
                    "038cb8bc-0289-48cf-a5ad-8bfbe54e204a",
                    "fe1bb177-0a35-421c-9b0e-c7623712387c"
            );

            Transaction dccResponse = paymentMethod.getDccRate(DccRateType.Sale, DccProcessor.Fexco)
                    .withAmount(new BigDecimal("10.01"))
                    .withCurrency("EUR")
                    .execute();

            assertNotNull(dccResponse);
            assertEquals("00", dccResponse.getResponseCode());

            Transaction response = paymentMethod.charge(new BigDecimal("10.01"))
                    .withCurrency("EUR")
                    .withOrderId(dccResponse.getOrderId())
                    .withDccRateData(dccResponse.getDccRateData())
                    .execute();

            assertNotNull(response);
            assertEquals("00", response.getResponseCode());
        });
    }

    /**************** Payment Scheduler Test ****************/

    @Test
    @Order(19)
    public void CardStorageAddSchedule() throws ApiException {
        checkCustomer();
        RecurringPaymentMethod paymentMethod = checkPaymentMethod();
        String scheduleId = GenerationUtils.generateScheduleId();

        try {
            RecurringEntity<Schedule> response =
                    paymentMethod
                            .addSchedule(scheduleId)
                            .withStartDate(DateTime.now().toDate())
                            .withAmount(new BigDecimal("30.01"))
                            .withCurrency("USD")
                            .withFrequency(ScheduleFrequency.SemiAnnually)
                            .withReprocessingCount(1)
                            .withNumberOfPayments(12)
                            .withCustomerNumber("E8953893489")
                            .withOrderPrefix("gym")
                            .withName("Gym Membership")
                            .withDescription("Social Sign-Up")
                            .create();

            assertEquals("00", response.getResponseCode());
            assertEquals("Schedule created successfully", response.getResponseMessage());

            // the schedule id/key is not received in the response from the create request
            Schedule schedule = new Schedule();
            schedule.setKey(scheduleId);
            schedule = RecurringService.get(scheduleId, Schedule.class);

            assertEquals(scheduleId, schedule.getId());
            assertEquals(12, schedule.getNumberOfPayments().intValue());
        } catch (GatewayException exc) {
            if (!exc.getResponseCode().equals("501") && !exc.getResponseCode().equals("520")) {
                throw exc;
            }
        }
    }

    @Test
    @Order(20)
    public void CardStorageAddSchedule_AllScheduleFrequency() throws ApiException {
        checkCustomer();
        RecurringPaymentMethod paymentMethod = checkPaymentMethod();

        for (ScheduleFrequency frequency : ScheduleFrequency.values()) {
            if (frequency == ScheduleFrequency.BiWeekly || frequency == ScheduleFrequency.SemiMonthly)
                continue;

            String scheduleId = GenerationUtils.generateScheduleId();
            RecurringEntity<Schedule> response =
                    paymentMethod
                            .addSchedule(scheduleId)
                            .withStartDate(DateTime.now().toDate())
                            .withAmount(new BigDecimal("30.01"))
                            .withCurrency("USD")
                            .withFrequency(frequency)
                            .withReprocessingCount(1)
                            .withNumberOfPayments(12)
                            .withCustomerNumber("E8953893489")
                            .withOrderPrefix("gym")
                            .withName("Gym Membership")
                            .withDescription("Social Sign-Up")
                            .create();

            assertEquals("00", response.getResponseCode());
            assertEquals("Schedule created successfully", response.getResponseMessage());

            // the schedule id/key is not received in the response from the create request
            Schedule schedule = new Schedule();
            schedule.setKey(scheduleId);
            schedule = RecurringService.get(scheduleId, Schedule.class);

            assertEquals(scheduleId, schedule.getId());
            assertEquals(12, schedule.getNumberOfPayments().intValue());
        }
    }

    @Test
    @Order(21)
    public void CardStorageAddSchedule_WithIndefinitelyRun() throws ApiException {
        checkCustomer();
        RecurringPaymentMethod paymentMethod = checkPaymentMethod();
        String scheduleId = GenerationUtils.generateScheduleId();

        try {
            RecurringEntity<Schedule> response =
                    paymentMethod
                            .addSchedule(scheduleId)
                            .withStartDate(DateTime.now().toDate())
                            .withAmount(new BigDecimal("30.01"))
                            .withCurrency("USD")
                            .withFrequency(ScheduleFrequency.Quarterly)
                            .withReprocessingCount(1)
                            .withNumberOfPayments(-1)
                            .withCustomerNumber("E8953893489")
                            .withOrderPrefix("gym")
                            .withName("Gym Membership")
                            .withDescription("Social Sign-Up")
                            .create();

            assertEquals("00", response.getResponseCode());
            assertEquals("Schedule created successfully", response.getResponseMessage());

            // the schedule id/key is not received in the response from the create request
            Schedule schedule = RecurringService.get(scheduleId, Schedule.class);

            assertEquals(scheduleId, schedule.getId());
            assertEquals(-1, schedule.getNumberOfPayments().intValue());
        } catch (GatewayException exc) {
            if (!exc.getResponseCode().equals("501") && !exc.getResponseCode().equals("520")) {
                throw exc;
            }
        }
    }

    @Test
    @Order(22)
    public void CardStorageAddSchedule_With999Runs() throws ApiException {
        checkCustomer();
        RecurringPaymentMethod paymentMethod = checkPaymentMethod();
        String scheduleId = GenerationUtils.generateScheduleId();

        RecurringEntity<Schedule> response =
                paymentMethod
                        .addSchedule(scheduleId)
                        .withStartDate(DateTime.now().toDate())
                        .withAmount(new BigDecimal("30.01"))
                        .withCurrency("USD")
                        .withFrequency(ScheduleFrequency.Quarterly)
                        .withReprocessingCount(1)
                        .withNumberOfPayments(999)
                        .withCustomerNumber("E8953893489")
                        .withOrderPrefix("gym")
                        .withName("Gym Membership")
                        .withDescription("Social Sign-Up")
                        .create();

        assertEquals("00", response.getResponseCode());
        assertEquals("Schedule created successfully", response.getResponseMessage());

        Schedule schedule = new Schedule();
        schedule.setKey(scheduleId);

        schedule = RecurringService.get(schedule.getKey(), Schedule.class);

        assertEquals(scheduleId, schedule.getId());
        assertEquals(999, schedule.getNumberOfPayments().intValue());
    }

    @Test
    @Order(23)
    public void CardStorageAddSchedule_WithoutScheduleRef() throws ApiException {
        RecurringPaymentMethod paymentMethod = new_customer.addPaymentMethod(paymentId("Credit"), card);
        boolean exceptionCaught = false;

        try {
            paymentMethod
                    .addSchedule(null)
                    .withAmount(new BigDecimal("30.01"))
                    .withCurrency("USD")
                    .withFrequency(ScheduleFrequency.Quarterly)
                    .withReprocessingCount(1)
                    .withNumberOfPayments(12)
                    .withCustomerNumber("E8953893489")
                    .withOrderPrefix("gym")
                    .withName("Gym Membership")
                    .withDescription("Social Sign-Up")
                    .create();
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("502", e.getResponseCode());
            assertEquals("Unexpected Gateway Response: 502 - Mandatory Fields missing: [/request/scheduleref]. See Developers Guide", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(23)
    public void CardStorageAddSchedule_WithoutFrequency() throws ApiException {
        RecurringPaymentMethod paymentMethod = new_customer.addPaymentMethod(paymentId("Credit"), card);
        String scheduleId = GenerationUtils.generateScheduleId();
        boolean exceptionCaught = false;

        try {
            paymentMethod
                    .addSchedule(scheduleId)
                    .withAmount(new BigDecimal("30.01"))
                    .withCurrency("USD")
                    .withReprocessingCount(1)
                    .withNumberOfPayments(12)
                    .withCustomerNumber("E8953893489")
                    .withOrderPrefix("gym")
                    .withName("Gym Membership")
                    .withDescription("Social Sign-Up")
                    .create();
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("502", e.getResponseCode());
            assertEquals("Unexpected Gateway Response: 502 - Mandatory Fields missing: [/request/schedule]. See Developers Guide", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(24)
    public void CardStorageAddSchedule_WithoutCustomerRef() throws ApiException {
        RecurringPaymentMethod paymentMethod = new_customer.addPaymentMethod(paymentId("Credit"), card);
        String scheduleId = GenerationUtils.generateScheduleId();
        boolean exceptionCaught = false;

        paymentMethod.setCustomerKey(null);

        try {
            paymentMethod
                    .addSchedule(scheduleId)
                    .withAmount(new BigDecimal("30.01"))
                    .withCurrency("USD")
                    .withFrequency(ScheduleFrequency.Quarterly)
                    .withReprocessingCount(1)
                    .withNumberOfPayments(12)
                    .withCustomerNumber("E8953893489")
                    .withOrderPrefix("gym")
                    .withName("Gym Membership")
                    .withDescription("Social Sign-Up")
                    .create();
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("502", e.getResponseCode());
            assertEquals("Unexpected Gateway Response: 502 - Mandatory Fields missing: [/request/payerref]. See Developers Guide", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(25)
    public void CardStorageAddSchedule_WithoutPaymentMethod() throws ApiException {
        RecurringPaymentMethod paymentMethod = new_customer.addPaymentMethod(null, card);
        String scheduleId = GenerationUtils.generateScheduleId();
        boolean exceptionCaught = false;

        try {
            paymentMethod
                    .addSchedule(scheduleId)
                    .withAmount(new BigDecimal("30.01"))
                    .withCurrency("USD")
                    .withFrequency(ScheduleFrequency.Quarterly)
                    .withReprocessingCount(1)
                    .withNumberOfPayments(12)
                    .withCustomerNumber("E8953893489")
                    .withOrderPrefix("gym")
                    .withName("Gym Membership")
                    .withDescription("Social Sign-Up")
                    .create();
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("502", e.getResponseCode());
            assertEquals("Unexpected Gateway Response: 502 - Mandatory Fields missing: [/request/paymentmethod]. See Developers Guide", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(26)
    public void CardStorageAddSchedule_WithoutAmount() throws ApiException {
        RecurringPaymentMethod paymentMethod = new_customer.addPaymentMethod(paymentId("Credit"), card);
        String scheduleId = GenerationUtils.generateScheduleId();
        boolean exceptionCaught = false;

        try {
            paymentMethod
                    .addSchedule(scheduleId)
                    .withCurrency("USD")
                    .withFrequency(ScheduleFrequency.Quarterly)
                    .withReprocessingCount(1)
                    .withNumberOfPayments(12)
                    .withCustomerNumber("E8953893489")
                    .withOrderPrefix("gym")
                    .withName("Gym Membership")
                    .withDescription("Social Sign-Up")
                    .create();
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("502", e.getResponseCode());
            assertEquals("Unexpected Gateway Response: 502 - Mandatory Fields missing: [/request/amount/@currency]. See Developers Guide", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(27)
    public void CardStorageAddSchedule_WithoutCurrency() throws ApiException {
        RecurringPaymentMethod paymentMethod = new_customer.addPaymentMethod(paymentId("Credit"), card);
        String scheduleId = GenerationUtils.generateScheduleId();
        boolean exceptionCaught = false;

        try {
            paymentMethod
                    .addSchedule(scheduleId)
                    .withAmount(new BigDecimal("30.01"))
                    .withFrequency(ScheduleFrequency.Quarterly)
                    .withReprocessingCount(1)
                    .withNumberOfPayments(12)
                    .withCustomerNumber("E8953893489")
                    .withOrderPrefix("gym")
                    .withName("Gym Membership")
                    .withDescription("Social Sign-Up")
                    .create();
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("506", e.getResponseCode());
            assertTrue(e.getMessage().contains("currency=\"\"] ' does not conform to the schema"));
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(28)
    public void CardStorageAddSchedule_WithoutNumberOfPayments() throws ApiException {
        RecurringPaymentMethod paymentMethod = new_customer.addPaymentMethod(paymentId("Credit"), card);
        String scheduleId = GenerationUtils.generateScheduleId();
        boolean exceptionCaught = false;

        try {
            paymentMethod.addSchedule(scheduleId)
                    .withAmount(new BigDecimal("30.01"))
                    .withCurrency("USD")
                    .withFrequency(ScheduleFrequency.Quarterly)
                    .create();
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("502", e.getResponseCode());
            assertEquals("Unexpected Gateway Response: 502 - Mandatory Fields missing: [/request/numtimes]. See Developers Guide", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(29)
    public void CardStorageAddSchedule_WithNumberOfPaymentsInvalid() throws ApiException {
        checkCustomer();
        RecurringPaymentMethod paymentMethod = checkPaymentMethod();
        String scheduleId = GenerationUtils.generateScheduleId();
        boolean exceptionCaught = false;

        try {
            paymentMethod
                    .addSchedule(scheduleId)
                    .withStartDate(DateTime.now().toDate())
                    .withAmount(new BigDecimal("30.01"))
                    .withCurrency("USD")
                    .withFrequency(ScheduleFrequency.Quarterly)
                    .withReprocessingCount(1)
                    .withNumberOfPayments(1000)
                    .create();
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("535", e.getResponseCode());
            assertEquals("Unexpected Gateway Response: 535 - Invalid value, numtimes cannot be greater than 999.", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(30)
    public void CardStorageAddSchedule_WithNumberOfPaymentsZero() throws ApiException {
        checkCustomer();
        RecurringPaymentMethod paymentMethod = checkPaymentMethod();
        String scheduleId = GenerationUtils.generateScheduleId();
        boolean exceptionCaught = false;

        try {
            paymentMethod
                    .addSchedule(scheduleId)
                    .withStartDate(DateTime.now().toDate())
                    .withAmount(new BigDecimal("30.01"))
                    .withCurrency("USD")
                    .withFrequency(ScheduleFrequency.Quarterly)
                    .withReprocessingCount(1)
                    .withNumberOfPayments(0)
                    .create();
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("535", e.getResponseCode());
            assertEquals("Unexpected Gateway Response: 535 - Invalid value, numtimes cannot be greater than 999.", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(31)
    public void GetListOfPaymentSchedules() throws ApiException {
        checkCustomer();
        RecurringPaymentMethod paymentMethod = checkPaymentMethod();

        for (int i = 0; i < 3; i++) {
            String scheduleId = GenerationUtils.generateScheduleId();

            RecurringEntity<Schedule> response =
                    paymentMethod
                            .addSchedule(scheduleId)
                            .withStartDate(DateTime.now().toDate())
                            .withAmount(new BigDecimal("30.01"))
                            .withCurrency("USD")
                            .withFrequency(ScheduleFrequency.SemiAnnually)
                            .withReprocessingCount(1)
                            .withNumberOfPayments(12)
                            .withCustomerNumber("E8953893489")
                            .withOrderPrefix("gym")
                            .withName("Gym Membership")
                            .withDescription("Social Sign-Up")
                            .create();

            assertEquals("00", response.getResponseCode());
            assertEquals("Schedule created successfully", response.getResponseMessage());
        }

        RecurringCollection<Schedule> schedules =
                RecurringService
                        .search(new Schedule(), RecurringCollection.class)
                        .addSearchCriteria(SearchCriteria.PaymentMethodKey.toString(), paymentId("Credit"))
                        .addSearchCriteria(SearchCriteria.CustomerId.toString(), new_customer.getKey())
                        .execute();

        assertNotNull(schedules);

        for (Schedule schedule : schedules) {
            assertNotNull(schedule.getKey());
        }
    }

    @Test
    @Order(32)
    public void GetListOfPaymentSchedules_RandomCustomerIdDetails() throws ApiException {
        String customerId = UUID.randomUUID().toString();

        try {
            RecurringCollection<Schedule> schedules =
                    RecurringService
                            .search(new Schedule(), RecurringCollection.class)
                            .addSearchCriteria(SearchCriteria.PaymentMethodKey.toString(), paymentId("Credit"))
                            .addSearchCriteria(SearchCriteria.CustomerId.toString(), customerId)
                            .execute();

            assertNull(schedules);
        } catch (GatewayException e) {
            assertEquals("520", e.getResponseCode());
            assertEquals("Unexpected Gateway Response: 520 - This Payer Ref [" + customerId + "] does not exist", e.getMessage());
        }
    }

    @Test
    @Order(33)
    public void GetListOfPaymentSchedules_RandomPayment() throws ApiException {
        checkCustomer();
        RecurringPaymentMethod paymentMethod = checkPaymentMethod();
        String randomPaymentId = UUID.randomUUID().toString();

        try {
            RecurringCollection<Schedule> schedules =
                    RecurringService
                            .search(new Schedule(), RecurringCollection.class)
                            .addSearchCriteria(SearchCriteria.PaymentMethodKey.toString(), randomPaymentId)
                            .addSearchCriteria(SearchCriteria.CustomerId.toString(), paymentMethod.getCustomerKey())
                            .execute();

            assertNull(schedules);
        } catch (GatewayException e) {
            assertEquals("Unexpected Gateway Response: 501 - Card Ref and Payer combination does not exist", e.getMessage());
            assertEquals("501", e.getResponseCode());
        }
    }

    @Test
    @Order(34)
    public void GetListOfPaymentSchedules_EmptyList() throws ApiException {
        checkCustomer();
        RecurringPaymentMethod paymentMethod = checkPaymentMethod();

        try {
            RecurringCollection<Schedule> schedulesCheck =
                    RecurringService
                            .search(new Schedule(), RecurringCollection.class)
                            .addSearchCriteria(SearchCriteria.PaymentMethodKey.toString(), paymentMethod.getId())
                            .addSearchCriteria(SearchCriteria.CustomerId.toString(), paymentMethod.getCustomerKey())
                            .execute();

            if (!schedulesCheck.isEmpty()) {
                for (Schedule schedule : schedulesCheck) {
                    schedule.delete();
                }
            }

            RecurringCollection<Schedule> schedules =
                    RecurringService
                            .search(new Schedule(), RecurringCollection.class)
                            .addSearchCriteria(SearchCriteria.PaymentMethodKey.toString(), paymentMethod.getId())
                            .addSearchCriteria(SearchCriteria.CustomerId.toString(), paymentMethod.getCustomerKey())
                            .execute();

            assertNull(schedules);
        } catch (GatewayException e) {
            assertEquals("Unexpected Gateway Response: 508 - The Scheduled Payment does not exist.", e.getMessage());
            assertEquals("508", e.getResponseCode());
        }
    }

    @Test
    @Order(35)
    public void GetListOfPaymentSchedules_WithoutPayer() throws ApiException {
        try {
            RecurringCollection<Schedule> response =
                    RecurringService
                            .search(new Schedule(), RecurringCollection.class)
                            .addSearchCriteria(SearchCriteria.PaymentMethodKey.toString(), paymentId("Credit"))
                            .execute();

            assertNull(response);
        } catch (GatewayException e) {
            assertEquals("502", e.getResponseCode());
            assertEquals("Unexpected Gateway Response: 502 - Mandatory Fields missing: [/request/payerref]. See Developers Guide", e.getMessage());
        }
    }

    @Test
    @Order(36)
    public void GetListOfPaymentSchedules_WithoutPaymentMethod() throws ApiException {
        try {
            RecurringCollection<Schedule> response =
                    RecurringService
                            .search(new Schedule(), RecurringCollection.class)
                            .addSearchCriteria(SearchCriteria.CustomerId.toString(), customerId())
                            .execute();

            assertNull(response);
        } catch (GatewayException e) {
            assertEquals("502", e.getResponseCode());
            assertEquals("Unexpected Gateway Response: 502 - Mandatory Fields missing: [/request/paymentmethod]. See Developers Guide", e.getMessage());
        }
    }

    @Test
    @Order(37)
    public void DeleteSchedule() throws ApiException {
        checkCustomer();
        RecurringPaymentMethod paymentMethod = checkPaymentMethod();
        String scheduleId = GenerationUtils.generateScheduleId();

        try {
            RecurringEntity<Schedule> response =
                    paymentMethod
                            .addSchedule(scheduleId)
                            .withStartDate(DateTime.now().toDate())
                            .withAmount(new BigDecimal("30.01"))
                            .withCurrency("USD")
                            .withFrequency(ScheduleFrequency.SemiAnnually)
                            .withReprocessingCount(1)
                            .withNumberOfPayments(12)
                            .withCustomerNumber("E8953893489")
                            .withOrderPrefix("gym")
                            .withName("Gym Membership")
                            .withDescription("Social Sign-Up")
                            .create();

            assertEquals("00", response.getResponseCode());
            assertEquals("Schedule created successfully", response.getResponseMessage());
        } catch (GatewayException exc) {
            if (!exc.getResponseCode().equals("501") && !exc.getResponseCode().equals("520")) {
                throw exc;
            }
        }

        RecurringCollection<Schedule> schedules =
                RecurringService
                        .search(new Schedule(), RecurringCollection.class)
                        .addSearchCriteria(SearchCriteria.PaymentMethodKey.toString(), paymentMethod.getId())
                        .addSearchCriteria(SearchCriteria.CustomerId.toString(), paymentMethod.getCustomerKey())
                        .execute();

        assertNotNull(schedules);

        Schedule schedule = schedules.get(0);

        schedule.delete();

        assertEquals("00", schedule.getResponseCode());
        assertEquals("OK", schedule.getResponseMessage());
    }

    @Test
    @Order(38)
    public void Delete_RandomSchedule() {
        Schedule schedule = new Schedule();
        schedule.setKey(GenerationUtils.generateScheduleId());

        boolean exceptionCaught = false;
        try {
            schedule.delete();
        } catch (ApiException e) {
            exceptionCaught = true;
            assertEquals("Failed to delete payment method, see inner exception for more details.", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(39)
    public void GetPaymentScheduleById() throws ApiException {
        Schedule schedule = new Schedule();
        schedule.setKey("bopinslfouil39vfmkqg");

        schedule = RecurringService.get(schedule.getKey(), Schedule.class);

        assertNotNull(schedule);
        assertEquals(schedule.getId(), schedule.getKey());
        assertNotNull(schedule.getStartDate());
    }

    @Test
    @Order(40)
    public void GetPaymentScheduleById_RandomId() {
        Schedule schedule = new Schedule();
        schedule.setKey(GenerationUtils.generateScheduleId());

        try {
            RecurringEntity<Schedule> response = RecurringService.get(schedule.getKey(), Schedule.class);

            assertNotNull(response);
            assertNull(response.getKey());
        } catch (ApiException e) {
            assertEquals("Unexpected Gateway Response: 508 - The Scheduled Payment does not exist.", e.getMessage());
        }
    }

    @Test
    @Order(41)
    public void GetPaymentScheduleById_NullId() {
        try {
            RecurringEntity<Schedule> response = RecurringService.get(null, Schedule.class);

            assertNotNull(response);
            assertNull(response.getKey());
        } catch (ApiException e) {
            assertEquals("key cannot be null for this transaction type.", e.getMessage());
        }
    }

    private void checkCustomer() throws ApiException {
        try {
            Customer response = new_customer.create();

            assertNotNull(response);
        } catch (GatewayException exc) {
            if (!exc.getResponseCode().equals("501") && !exc.getResponseCode().equals("520")) {
                throw exc;
            }
        }
    }

    private RecurringPaymentMethod checkPaymentMethod() throws ApiException {
        RecurringPaymentMethod paymentMethod = new_customer.addPaymentMethod(paymentId("Credit"), card);

        try {
            RecurringPaymentMethod response = paymentMethod.create();
            assertNotNull(response);
        } catch (GatewayException exc) {
            if (!exc.getResponseCode().equals("501") && !exc.getResponseCode().equals("520")) {
                throw exc;
            }
        }
        return paymentMethod;
    }
}

package com.global.api.tests.certifications;

import com.global.api.ServicesConfig;
import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.AccountType;
import com.global.api.entities.enums.CheckType;
import com.global.api.entities.enums.ScheduleFrequency;
import com.global.api.entities.enums.SecCode;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.RecurringPaymentMethod;
import com.global.api.paymentMethods.eCheck;
import com.global.api.services.BatchService;
import com.global.api.utils.DateUtils;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RecurringCertification {
    private static Customer _customerPerson;
    private static Customer _customerBusiness;
    private static RecurringPaymentMethod _paymentMethodVisa;
    private static RecurringPaymentMethod _paymentMethodMasterCard;
    private static RecurringPaymentMethod _paymentMethodCheckPpd;
    private static RecurringPaymentMethod _paymentMethodCheckCcd;
    private static Schedule _scheduleVisa;
    private static Schedule _scheduleMasterCard;
    private static Schedule _scheduleCheckPpd;
    private static Schedule _scheduleCheckCcd;

    private static String todayDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
    private static String identifierBase = "%s-%s" + UUID.randomUUID().toString().substring(0, 10);


    private String getIdentifier(String identifier){
        String rvalue = String.format(identifierBase, todayDate, identifier);
        System.out.println(rvalue);
        return rvalue;
    }

    public RecurringCertification() throws ApiException {
        ServicesConfig config = new ServicesConfig();
        config.setSecretApiKey("skapi_cert_MTyMAQBiHVEAewvIzXVFcmUd2UcyBge_eCpaASUp0A");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");
        ServicesContainer.configure(config);
    }

    @Test @Ignore
    public void ecomm_000_CloseBatch() throws ApiException {
        try {
            BatchSummary response = BatchService.closeBatch();
            assertNotNull(response);
            System.out.println(String.format("Batch ID: %s", response.getBatchId()));
            System.out.println(String.format("Sequence Number: %s", response.getSequenceNumber()));
        }
        catch (GatewayException exc) {
            if (!exc.getResponseText().contains("Transaction was rejected because it requires a batch to be open."))
                fail(exc.getMessage());
        }
    }

    @Test
    public void Test_000_CleanUp() throws ApiException {
        // Remove Schedules
        try {
            List<Schedule> schResults = Schedule.findAll();
            for(Schedule schedule: schResults) {
                schedule.delete(true);
            }
        }
        catch(Exception e) { }

        // Remove Payment Methods
        try {
            List<RecurringPaymentMethod> pmResults = RecurringPaymentMethod.findAll();
            for(RecurringPaymentMethod pm: pmResults) {
                pm.delete(true);
            }
        }
        catch(Exception e) { }

        // Remove Customers
        try {
            List<Customer> custResults = Customer.findAll();
            for(Customer c: custResults) {
                c.delete(true);
            }
        }
        catch(Exception e) { }
    }

    // CUSTOMER SETUP

    @Test
    public void recurring_001_AddCustomerPerson() throws ApiException {
        Address address = new Address();
        address.setStreetAddress1("123 Main St");
        address.setCity("Dallas");
        address.setState("TX");
        address.setPostalCode("98765");
        address.setCountry("USA");
        
        Customer customer = new Customer();
        customer.setId(getIdentifier("Person"));
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setStatus("Active");
        customer.setEmail("john.doe@email.com");
        customer.setAddress(address);
        customer.setWorkPhone("5551112222");
        customer = customer.create();
        assertNotNull(customer);
        assertNotNull(customer.getKey());
        _customerPerson = customer;
    }

    @Test
    public void recurring_002_AddCustomerBusiness() throws ApiException {
        Address address = new Address();
        address.setStreetAddress1("987 Elm St");
        address.setCity("Princeton");
        address.setState("NJ");
        address.setPostalCode("12345");
        address.setCountry("USA");
        
        Customer customer = new Customer();
        customer.setId(getIdentifier("Business"));
        customer.setCompany("AcmeCo");
        customer.setStatus("Active");
        customer.setEmail("acme@email.com");
        customer.setAddress(address);
        customer.setWorkPhone("5551112222");
        customer = customer.create();
        assertNotNull(customer);
        assertNotNull(customer.getKey());
        _customerBusiness = customer;
    }

    // PAYMENT METHOD SETUP

    @Test
    public void recurring_003_AddPaymentCreditVisa() throws ApiException {
        if (_customerPerson == null)
            fail();
        
        CreditCardData card = new CreditCardData();
        card.setNumber("4012002000060016");
        card.setExpMonth(12);
        card.setExpYear(2025);

        RecurringPaymentMethod paymentMethod = _customerPerson.addPaymentMethod(getIdentifier("CreditV"), card).create();
        assertNotNull(paymentMethod);
        assertNotNull(paymentMethod.getKey());
        _paymentMethodVisa = paymentMethod;
    }

    @Test
    public void recurring_004_AddPaymentCreditMasterCard() throws ApiException {
        if (_customerPerson == null)
            fail();

        CreditCardData card = new CreditCardData();
        card.setNumber("5473500000000014");
        card.setExpMonth(12);
        card.setExpYear(2025);

        RecurringPaymentMethod paymentMethod = _customerPerson.addPaymentMethod(getIdentifier("CreditMC"), card).create();
        assertNotNull(paymentMethod);
        assertNotNull(paymentMethod.getKey());
        _paymentMethodMasterCard = paymentMethod;
    }

    @Test
    public void recurring_005_AddPaymentCheckPPD() throws ApiException {
        if (_customerPerson == null)
            fail();
        
        eCheck check = new eCheck();
        check.setAccountType(AccountType.Checking);
        check.setCheckType(CheckType.Personal);
        check.setSecCode(SecCode.Ppd);
        check.setRoutingNumber("490000018");
        check.setDriversLicenseNumber("7418529630");
        check.setDriversLicenseState("TX");
        check.setAccountNumber("24413815");
        check.setBirthYear(1989);

        RecurringPaymentMethod paymentMethod = _customerPerson.addPaymentMethod(getIdentifier("CheckPPD"), check).create();
        assertNotNull(paymentMethod);
        assertNotNull(paymentMethod.getKey());
        _paymentMethodCheckPpd = paymentMethod;
    }

    @Test
    public void recurring_006_AddPaymentCheckCCD() throws ApiException {
        if (_customerBusiness == null)
            fail();
        
        eCheck check = new eCheck();
        check.setAccountType(AccountType.Checking);
        check.setCheckType(CheckType.Business);
        check.setSecCode(SecCode.Ccd);
        check.setRoutingNumber("490000018");
        check.setDriversLicenseNumber("3692581470");
        check.setDriversLicenseState("TX");
        check.setAccountNumber("24413815");
        check.setBirthYear(1989);

        RecurringPaymentMethod paymentMethod = _customerBusiness.addPaymentMethod(getIdentifier("CheckCCD"), check).create();
        assertNotNull(paymentMethod);
        assertNotNull(paymentMethod.getKey());
        _paymentMethodCheckCcd = paymentMethod;
    }

    // PAYMENT SETUP - DECLINED

    @Test(expected = GatewayException.class)
    public void recurring_007_AddPaymentCheckPPD() throws ApiException {
        if (_customerPerson == null)
            fail();
        
        eCheck check = new eCheck();
        check.setAccountType(AccountType.Checking);
        check.setCheckType(CheckType.Personal);
        check.setSecCode(SecCode.Ppd);
        check.setRoutingNumber("490000018");
        check.setDriversLicenseNumber("7418529630");
        check.setDriversLicenseState("TX");
        check.setAccountNumber("24413815");
        check.setBirthYear(1989);

        _customerPerson.addPaymentMethod(getIdentifier("CheckPPD"), check).create();
    }

    // Recurring Billing using PayPlan - Managed Schedule

    @Test
    public void recurring_008_addScheduleCreditVisa() throws ApiException {
        if (_paymentMethodVisa == null)
            fail();

        Schedule schedule = _paymentMethodVisa.addSchedule(getIdentifier("CreditV"))
                .withStartDate(DateUtils.parse("02/01/2027"))
                .withAmount(new BigDecimal("30.01"))
                .withFrequency(ScheduleFrequency.Weekly)
                .withReprocessingCount(1)
                .withStatus("Active")
                .create();
        assertNotNull(schedule);
        assertNotNull(schedule.getKey());
        _scheduleVisa = schedule;
    }

    @Test
    public void recurring_009_addScheduleCreditMasterCard() throws ApiException {
        if (_paymentMethodMasterCard == null)
            fail();

        Schedule schedule = _paymentMethodMasterCard.addSchedule(getIdentifier("CreditMC"))
                .withStatus("Active")
                .withAmount(new BigDecimal("30.02"))
                .withStartDate(DateUtils.parse("02/01/2027"))
                .withFrequency(ScheduleFrequency.Weekly)
                .withEndDate(DateUtils.parse("04/01/2027"))
                .withReprocessingCount(2)
                .create();
        assertNotNull(schedule);
        assertNotNull(schedule.getKey());
        _scheduleMasterCard = schedule;
    }

    @Test
    public void recurring_010_addScheduleCheckPPD() throws ApiException {
        if (_paymentMethodCheckPpd == null)
            fail();

        Schedule schedule = _paymentMethodCheckPpd.addSchedule(getIdentifier("CheckPPD"))
                .withStatus("Active")
                .withAmount(new BigDecimal("30.03"))
                .withStartDate(DateUtils.parse("02/01/2027"))
                .withFrequency(ScheduleFrequency.Monthly)
                .withReprocessingCount(1)
                .withNumberOfPayments(2)
                .create();
        assertNotNull(schedule);
        assertNotNull(schedule.getKey());
        _scheduleCheckPpd = schedule;
    }

    @Test
    public void recurring_011_addScheduleCheckCCD() throws ApiException {
        if (_paymentMethodCheckCcd == null)
            fail();

        Schedule schedule = _paymentMethodCheckCcd.addSchedule(getIdentifier("CheckCCD"))
                .withStatus("Active")
                .withAmount(new BigDecimal("30.04"))
                .withStartDate(DateUtils.parse("02/01/2027"))
                .withFrequency(ScheduleFrequency.BiWeekly)
                .withReprocessingCount(1)
                .create();
        assertNotNull(schedule);
        assertNotNull(schedule.getKey());
        _scheduleCheckCcd = schedule;
    }

    @Test(expected = GatewayException.class)
    public void recurring_012_addScheduleCreditVisa() throws ApiException {
        if (_paymentMethodVisa == null)
            fail();

        Schedule schedule = _paymentMethodVisa.addSchedule(getIdentifier("CreditV"))
                .withStartDate(DateUtils.parse("02/01/2027"))
                .withAmount(new BigDecimal("30.01"))
                .withFrequency(ScheduleFrequency.Weekly)
                .withReprocessingCount(1)
                .withStatus("Active")
                .create();
    }

    @Test(expected = GatewayException.class)
    public void recurring_013_addScheduleCCheckPPD() throws ApiException {
        if (_paymentMethodCheckPpd == null)
            fail();

        Schedule schedule = _paymentMethodCheckPpd.addSchedule(getIdentifier("CheckPPD"))
                .withStatus("Active")
                .withAmount(new BigDecimal("30.03"))
                .withStartDate(DateUtils.parse("02/01/2027"))
                .withFrequency(ScheduleFrequency.Monthly)
                .withReprocessingCount(1)
                .withNumberOfPayments(2)
                .create();
    }

    // Recurring Billing using PayPlan - Managed Schedule

    @Test
    public void recurring_014_RecurringBillingVisa() throws ApiException {
        if (_paymentMethodVisa == null || _scheduleVisa == null)
            fail();

        Transaction response = _paymentMethodVisa.charge(new BigDecimal("20.01"))
                .withCurrency("USD")
                .withScheduleId(_scheduleVisa.getKey())
                .withOneTimePayment(false)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test @Ignore
    public void recurring_015_RecurringBillingMasterCard() throws ApiException {
        if (_paymentMethodMasterCard == null || _scheduleMasterCard == null)
            fail();

        Transaction response = _paymentMethodMasterCard.charge(new BigDecimal("20.02"))
                .withCurrency("USD")
                .withScheduleId(_scheduleVisa.getKey())
                .withOneTimePayment(false)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void recurring_016_RecurringBillingCheckPPD() throws ApiException {
        if (_paymentMethodCheckPpd == null || _scheduleCheckPpd == null)
            fail();

        Transaction response = _paymentMethodCheckPpd.charge(new BigDecimal("20.03"))
                .withCurrency("USD")
                .withScheduleId(_scheduleVisa.getKey())
                .withOneTimePayment(false)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void recurring_017_RecurringBillingCheckCCD() throws ApiException {
        if (_paymentMethodCheckCcd == null || _scheduleCheckCcd == null)
            fail();

        Transaction response = _paymentMethodCheckCcd.charge(new BigDecimal("20.04"))
                .withCurrency("USD")
                .withScheduleId(_scheduleVisa.getKey())
                .withOneTimePayment(false)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // One time bill payment

    @Test
    public void recurring_018_RecurringBillingVisa() throws ApiException {
        if (_paymentMethodVisa == null)
            fail();

        Transaction response = _paymentMethodVisa.charge(new BigDecimal("20.06"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void recurring_019_RecurringBillingMasterCard() throws ApiException {
        if (_paymentMethodMasterCard == null)
            fail();

        Transaction response = _paymentMethodMasterCard.charge(new BigDecimal("20.07"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void recurring_020_RecurringBillingCheckPPD() throws ApiException {
        if (_paymentMethodCheckPpd == null)
            fail();

        Transaction response = _paymentMethodCheckPpd.charge(new BigDecimal("20.08"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void recurring_021_RecurringBillingCheckCCD() throws ApiException {
        if (_paymentMethodCheckCcd == null)
            fail();

        Transaction response = _paymentMethodCheckCcd.charge(new BigDecimal("20.09"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // Onetime bill payment - declined

    @Test
    public void recurring_022_RecurringBillingVisa_Decline() throws ApiException {
        if (_paymentMethodVisa == null)
            fail();

        Transaction response = _paymentMethodVisa.charge(new BigDecimal("10.08"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("51", response.getResponseCode());
    }

    @Test
    public void recurring_023_RecurringBillingCheckPPD_Decline() throws ApiException {
        if (_paymentMethodCheckPpd == null)
            fail();

        Transaction response = _paymentMethodCheckPpd.charge(new BigDecimal("25.02"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("1", response.getResponseCode());
    }

    @Test @Ignore
    public void recurring_999_CloseBatch() throws ApiException {
        try {
            BatchSummary response = BatchService.closeBatch();
            assertNotNull(response);
            System.out.println(String.format("Batch ID: %s", response.getBatchId()));
            System.out.println(String.format("Sequence Number: %s", response.getSequenceNumber()));
        }
        catch (GatewayException exc) {
            if (!exc.getResponseText().contains("Transaction was rejected because it requires a batch to be open."))
                fail(exc.getMessage());
        }
    }
}

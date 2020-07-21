package com.global.api.tests.portico;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.RecurringPaymentMethod;
import com.global.api.paymentMethods.eCheck;
import com.global.api.serviceConfigs.GatewayConfig;
import com.global.api.utils.DateUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PorticoRecurringTests {
    private String customerId() {
        return String.format("%s-GlobalApi", new SimpleDateFormat("yyyyMMdd").format(new Date()));
    }
    private String paymentId(String type) {
        return String.format("%s-GlobalApi-%s", new SimpleDateFormat("yyyyMMdd").format(new Date()), type);
    }
    
    public PorticoRecurringTests() throws ApiException {
        GatewayConfig config = new GatewayConfig();
        config.setSecretApiKey("skapi_cert_MTyMAQBiHVEAewvIzXVFcmUd2UcyBge_eCpaASUp0A");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");
        config.setEnableLogging(true);
        ServicesContainer.configureService(config);
    }

    private void sleepOne() {
        try {
            Thread.sleep(5000);                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void Test_000_CleanUp() throws ApiException {
        // Remove Schedules
        try {
            List<Schedule> schResults = Schedule.findAll();
            for(Schedule schedule: schResults) {
                schedule.delete(true);
                sleepOne();
            }
        }
        catch(Exception e) {
            fail(e.getMessage());
        }

        // Remove Payment Methods
        try {
            List<RecurringPaymentMethod> pmResults = RecurringPaymentMethod.findAll();
            for(RecurringPaymentMethod pm: pmResults) {
                pm.delete(true);
                sleepOne();
            }
        }
        catch(Exception e) {
            fail(e.getMessage());
        }

        // Remove Customers
        try {
            List<Customer> custResults = Customer.findAll();
            for(Customer c: custResults) {
                c.delete(true);
                sleepOne();
            }
        }
        catch(Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Test_001a_CreateCustomer() throws ApiException {
        Customer cust = Customer.find(customerId());
        if (cust != null)
            fail("Customer already exists.");
        
        Address address = new Address();
        address.setStreetAddress1("987 Elm St");
        address.setCity("Princeton");
        address.setProvince("NJ");
        address.setPostalCode("12345");
        address.setCountry("USA");
        
        Customer customer = new Customer();
        customer.setId(customerId());
        customer.setStatus("Active");
        customer.setFirstName("Bill");
        customer.setLastName("Johnson");
        customer.setCompany("Heartland Payment Systems");
        customer.setAddress(address);
        customer.setHomePhone("9876543210");
        customer.setWorkPhone("9876543210");
        customer.setFax("9876543210");
        customer.setMobilePhone("9876543210");
        customer.setEmail("text@example.com");
        customer = customer.create();
        assertNotNull(customer);
        assertNotNull(customer.getKey());
    }

    @Test
    public void Test_001b_CreatePaymentMethod_Credit() throws ApiException {
        Customer customer = Customer.find(customerId());
        assertNotNull(customer);
        
        CreditCardData card = new CreditCardData();
        card.setNumber("4111111111111111");
        card.setExpMonth(12);
        card.setExpYear(2025);

        RecurringPaymentMethod payment = customer.addPaymentMethod(paymentId("Credit"), card).create();
        assertNotNull(payment);
        assertNotNull(payment.getKey());
    }

    @Test
    public void Test_001d_CreatePaymentMethod_CreditTrack() throws ApiException {
        Customer customer = Customer.find(customerId());
        assertNotNull(customer);

        CreditTrackData track = new CreditTrackData();
        track.setValue("%B4012110000000011^TEST CUSTOMER^250510148888000000000074800000?;");
        track.setEntryMethod(EntryMethod.Swipe);

        RecurringPaymentMethod payment = customer.addPaymentMethod(paymentId("Track"), track).create();
        assertNotNull(payment);
        assertNotNull(payment.getKey());
    }

    @Test
    public void Test_001g_CreatePaymentMethod_ACH() throws ApiException {
        Customer customer = Customer.find(customerId());
        assertNotNull(customer);

        eCheck check = new eCheck();
        check.setRoutingNumber("490000018");
        check.setAccountNumber("24413815");
        check.setCheckType(CheckType.Personal);
        check.setSecCode(SecCode.Ppd);
        check.setAccountType(AccountType.Checking);
        check.setDriversLicenseNumber("7418529630");
        check.setDriversLicenseState("TX");
        check.setBirthYear(1989);

        RecurringPaymentMethod payment = customer.addPaymentMethod(paymentId("ACH"), check).create();
        assertNotNull(payment);
        assertNotNull(payment.getKey());
    }

    @Test
    public void Test_001h_CreateSchedule_Credit() throws ApiException {
        RecurringPaymentMethod paymentMethod = RecurringPaymentMethod.find(paymentId("Credit"));
        assertNotNull(paymentMethod);

        Schedule schedule = paymentMethod.addSchedule(paymentId("Credit"))
                .withAmount(new BigDecimal("30.02"))
                .withCurrency("USD")
                .withStartDate(DateUtils.parse("02/01/2027"))
                .withFrequency(ScheduleFrequency.Weekly)
                .withStatus("Active")
                .withReprocessingCount(2)
                .withEndDate(DateUtils.parse("04/01/2027"))
                .create();
        assertNotNull(schedule);
        assertNotNull(schedule.getKey());
    }

    @Test
    public void Test_001i_CreateSchedule_ACH() throws ApiException {
        RecurringPaymentMethod paymentMethod = RecurringPaymentMethod.find(paymentId("ACH"));
        assertNotNull(paymentMethod);

        Schedule schedule = paymentMethod.addSchedule(paymentId("ACH"))
                .withAmount(new BigDecimal("11"))
                .withStartDate(DateUtils.addDays(new Date(), 7))
                .withFrequency(ScheduleFrequency.Monthly)
                .withStatus("Active")
                .create();
        assertNotNull(schedule);
        assertNotNull(schedule.getKey());
    }

    @Test
    public void Test_002a_FindCustomer() throws ApiException {
        Customer customer = Customer.find(customerId());
        assertNotNull(customer);
        assertNotNull(customer.getKey());
    }

    @Test
    public void Test_002b_FindPaymentMethod() throws ApiException {
        RecurringPaymentMethod paymentMethod = RecurringPaymentMethod.find(paymentId("Credit"));
        assertNotNull(paymentMethod);
    }

    @Test
    public void Test_002c_FindSchedule() throws ApiException {
        Schedule schedule = Schedule.find(paymentId("Credit"));
        assertNotNull(schedule);
    }

    @Test
    public void Test_003a_FindAllCustomers() throws ApiException {
        List<Customer> customers = Customer.findAll();
        assertNotNull(customers);
    }

    @Test
    public void Test_003b_FindAllPaymentMethods() throws ApiException {
        List<RecurringPaymentMethod> paymentMethods = RecurringPaymentMethod.findAll();
        assertNotNull(paymentMethods);
    }

    @Test
    public void Test_003c_FindAllSchedules() throws ApiException {
        List<Schedule> schedule = Schedule.findAll();
        assertNotNull(schedule);
    }

    @Test
    public void Test_004a_EditCustomer() throws ApiException {
        Customer customer = Customer.find(customerId());
        assertNotNull(customer);

        customer.setFirstName("Bob");
        customer.saveChanges();
    }

    @Test
    public void Test_004b_EditPaymentMethod() throws ApiException {
        RecurringPaymentMethod paymentMethod = RecurringPaymentMethod.find(paymentId("Credit"));
        assertNotNull(paymentMethod);

        paymentMethod.setPreferredPayment(false);
        paymentMethod.saveChanges();
    }

    @Test
    public void Test_004d_EditSchedule() throws ApiException {
        Schedule schedule = Schedule.find(paymentId("Credit"));
        assertNotNull(schedule);

        schedule.setStatus("Inactive");
        schedule.saveChanges();
    }

    @Test(expected = ApiException.class)
    public void Test_005a_EditCustomerBadData() throws ApiException {
        Customer customer = new Customer();
        customer.setKey("00000000");
        customer.saveChanges();
    }

    @Test(expected = ApiException.class)
    public void Test_005b_EditPaymentMethodBadData() throws ApiException {
        RecurringPaymentMethod paymentMethod = new RecurringPaymentMethod("000000", "000000");
        paymentMethod.saveChanges();
    }

    @Test
    public void Test_006a_GetCustomer() throws ApiException {
        Customer customer = Customer.find(customerId());
        assertNotNull(customer);

        customer = Customer.get(customer.getKey());
        assertNotNull(customer);
    }

    @Test
    public void Test_006b_GetPaymentMethod() throws ApiException {
        RecurringPaymentMethod paymentMethod = RecurringPaymentMethod.find(paymentId("Credit"));
        assertNotNull(paymentMethod);

        paymentMethod = RecurringPaymentMethod.get(paymentMethod.getKey());
        assertNotNull(paymentMethod);
    }

    @Test
    public void Test_006c_GetSchedule() throws ApiException {
        Schedule schedule = Schedule.find(paymentId("Credit"));
        assertNotNull(schedule);

        schedule = Schedule.get(schedule.getKey());
        assertNotNull(schedule);
    }

    @Test
    public void Test_007a_CreditCharge_OneTime() throws ApiException {
        RecurringPaymentMethod paymentMethod = RecurringPaymentMethod.find(paymentId("Credit"));
        assertNotNull(paymentMethod);

        Transaction response = paymentMethod.charge(new BigDecimal(9))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void Test_007b_CreditCharge_ScheduleId() throws ApiException {
        RecurringPaymentMethod paymentMethod = RecurringPaymentMethod.find(paymentId("Credit"));
        assertNotNull(paymentMethod);

        Schedule schedule = Schedule.find(paymentId("Credit"));
        assertNotNull(schedule);

        Transaction response = paymentMethod.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withScheduleId(schedule.getKey())
                .withOneTimePayment(false)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void Test_007b_CreditCharge_ScheduleIdWithCOF() throws ApiException {
        RecurringPaymentMethod paymentMethod = RecurringPaymentMethod.find(paymentId("Credit"));
        assertNotNull(paymentMethod);

        Schedule schedule = Schedule.find(paymentId("Credit"));
        assertNotNull(schedule);

        Transaction response = paymentMethod.charge(new BigDecimal("19"))
                .withCurrency("USD")
                .withScheduleId(schedule.getKey())
                .withAllowDuplicates(true)
                .withOneTimePayment(false)
                .withCardBrandStorage(StoredCredentialInitiator.CardHolder)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction nextResponse = paymentMethod.charge(new BigDecimal("15"))
                .withCurrency("USD")
                .withScheduleId(schedule.getKey())
                .withAllowDuplicates(true)
                .withOneTimePayment(false)
                .withCardBrandStorage(StoredCredentialInitiator.Merchant,response.getCardBrandTransactionId())
                .execute();
        assertNotNull(nextResponse);
        assertEquals("00", nextResponse.getResponseCode());
    }

    @Test
    public void Test_007c_ACHCharge_OneTime() throws ApiException {
        RecurringPaymentMethod paymentMethod = RecurringPaymentMethod.find(paymentId("ACH"));
        assertNotNull(paymentMethod);

        Transaction response = paymentMethod.charge(new BigDecimal("11"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void Test_007d_ACHCharge_ScheduleId() throws ApiException {
        RecurringPaymentMethod paymentMethod = RecurringPaymentMethod.find(paymentId("ACH"));
        assertNotNull(paymentMethod);

        Schedule schedule = Schedule.find(paymentId("ACH"));
        assertNotNull(schedule);

        Transaction response = paymentMethod.charge(new BigDecimal("12"))
                .withCurrency("USD")
                .withScheduleId(schedule.getKey())
                .withOneTimePayment(false)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void Test_007e_CreditCharge_Declined() throws ApiException {
        RecurringPaymentMethod paymentMethod = RecurringPaymentMethod.find(paymentId("Credit"));
        assertNotNull(paymentMethod);

        Transaction response = paymentMethod.charge(new BigDecimal("10.08"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("51", response.getResponseCode());
    }

    @Test
    public void Test_008a_DeleteSchedule_Credit() throws ApiException {
        Schedule schedule = Schedule.find(paymentId("Credit"));
        assertNotNull(schedule);
        schedule.delete();
        sleepOne();
    }

    @Test
    public void Test_008b_DeleteSchedule_ACH() throws ApiException {
        Schedule schedule = Schedule.find(paymentId("ACH"));
        assertNotNull(schedule);
        schedule.delete();
        sleepOne();
    }

    @Test
    public void Test_008c_DeletePaymentMethod_Credit() throws ApiException {
        RecurringPaymentMethod paymentMethod = RecurringPaymentMethod.find(paymentId("Credit"));
        assertNotNull(paymentMethod);
        paymentMethod.delete();
        sleepOne();
    }

    @Test
    public void Test_008d_DeletePaymentMethod_Track() throws ApiException {
        RecurringPaymentMethod paymentMethod = RecurringPaymentMethod.find(paymentId("Track"));
        assertNotNull(paymentMethod);
        paymentMethod.delete();
        sleepOne();
    }

    @Test
    public void Test_008e_DeletePaymentMethod_Ach() throws ApiException {
        RecurringPaymentMethod paymentMethod = RecurringPaymentMethod.find(paymentId("ACH"));
        assertNotNull(paymentMethod);
        paymentMethod.delete();
        sleepOne();
    }

    @Test
    public void Test_008f_DeleteCustomer() throws ApiException {
        Customer customer = Customer.find(customerId());
        assertNotNull(customer);
        customer.delete();
        sleepOne();
    }

    @Test
    public void Test_008g_CreditCharge_WithNewCryptoURL() throws ApiException {
        GatewayConfig config = new GatewayConfig();
        config.setSecretApiKey("skapi_cert_MTyMAQBiHVEAewvIzXVFcmUd2UcyBge_eCpaASUp0A");
        config.setServiceUrl("https://cert.api2-c.heartlandportico.com");
        ServicesContainer.configureService(config);

        RecurringPaymentMethod paymentMethod = RecurringPaymentMethod.find(paymentId("Credit"));
        assertNotNull(paymentMethod);

        Transaction response = paymentMethod.charge(new BigDecimal("10.08"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("51", response.getResponseCode());
    }
}

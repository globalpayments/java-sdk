package com.global.api.tests.portico;

import com.global.api.ServicesConfig;
import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.eCheck;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class PorticoAchTests {
    private eCheck check;
    private Address address;
    
    public PorticoAchTests() throws ApiException {
        ServicesConfig config = new ServicesConfig();
        config.setSecretApiKey("skapi_cert_MTyMAQBiHVEAewvIzXVFcmUd2UcyBge_eCpaASUp0A");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");

        ServicesContainer.configure(config);

        check = new eCheck();
        check.setAccountNumber("24413815");
        check.setRoutingNumber("490000018");
        check.setCheckType(CheckType.Personal);
        check.setSecCode(SecCode.Ppd);
        check.setAccountType(AccountType.Checking);
        check.setEntryMode(EntryMethod.Manual);
        check.setCheckHolderName("John Doe");
        check.setDriversLicenseNumber("09876543210");
        check.setDriversLicenseState("TX");
        check.setPhoneNumber("8003214567");
        check.setBirthYear(1997);
        check.setSsnLast4("4321");

        address = new Address();
        address.setStreetAddress1("123 Main St.");
        address.setCity("Downtown");
        address.setState("NJ");
        address.setPostalCode("12345");
    }

    @Test
    public void checkSale() throws ApiException {
        Transaction response = check.charge(new BigDecimal(11))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void checkVoidFromTransactionId() throws ApiException {
        Transaction response = check.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withAddress(address)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction voidResponse = Transaction.fromId(response.getTransactionId(), PaymentMethodType.ACH)
                .voidTransaction()
                .execute();
        assertNotNull(voidResponse);
        assertEquals("00", voidResponse.getResponseCode());
    }
}

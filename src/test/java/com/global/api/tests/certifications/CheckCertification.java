package com.global.api.tests.certifications;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.AccountType;
import com.global.api.entities.enums.CheckType;
import com.global.api.entities.enums.SecCode;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.eCheck;
import com.global.api.serviceConfigs.GatewayConfig;
import com.global.api.tests.testdata.TestChecks;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CheckCertification {
    private Address address;

    public CheckCertification() throws ApiException {
        GatewayConfig config = new GatewayConfig();
        config.setSecretApiKey("skapi_cert_MTyMAQBiHVEAewvIzXVFcmUd2UcyBge_eCpaASUp0A");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");

        ServicesContainer.configureService(config);

        address = new Address();
        address.setStreetAddress1("123 Main St.");
        address.setCity("Downtown");
        address.setState("NJ");
        address.setPostalCode("12345");
    }

    @Test
    public void checks_001ConsumerPersonalChecking() throws ApiException {
        eCheck check = TestChecks.certification(SecCode.Ppd, CheckType.Personal, AccountType.Checking);

        Transaction response = check.charge(new BigDecimal("11.00"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // test case 25
        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void checks_002ConsumerBusinessChecking() throws ApiException {
        eCheck check = TestChecks.certification(SecCode.Ppd, CheckType.Business, AccountType.Checking);
        Transaction response = check.charge(new BigDecimal("12.00"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void checks_003ConsumerPersonalSavings() throws ApiException {
        eCheck check = TestChecks.certification(SecCode.Ppd, CheckType.Personal, AccountType.Savings);
        Transaction response = check.charge(new BigDecimal("13.00"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void checks_004ConsumerBusinessSavings() throws ApiException {
        eCheck check = TestChecks.certification(SecCode.Ppd, CheckType.Business, AccountType.Savings);
        Transaction response = check.charge(new BigDecimal("14.00"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void checks_005CorporatePersonalChecking() throws ApiException {
        eCheck check = TestChecks.certification(SecCode.Ccd, CheckType.Personal, AccountType.Checking, "Heartland Pays");
        Transaction response = check.charge(new BigDecimal("15.00"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // test case 26
        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void checks_006CorporateBuisnessChecking() throws ApiException {
        eCheck check = TestChecks.certification(SecCode.Ccd, CheckType.Business, AccountType.Checking, "Heartland Pays");
        Transaction response = check.charge(new BigDecimal("16.00"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void checks_007CorporatePersonalSavings() throws ApiException {
        eCheck check = TestChecks.certification(SecCode.Ccd, CheckType.Personal, AccountType.Savings, "Heartland Pays");
        Transaction response = check.charge(new BigDecimal("17.00"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void checks_008CorporateBuisnessSavings() throws ApiException {
        eCheck check = TestChecks.certification(SecCode.Ccd, CheckType.Business, AccountType.Savings, "Heartland Pays");
        Transaction response = check.charge(new BigDecimal("18.00"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void checks_009EgoldPersonalChecking() throws ApiException {
        eCheck check = TestChecks.certification(SecCode.Pop, CheckType.Personal, AccountType.Checking);
        Transaction response = check.charge(new BigDecimal("11.00"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void checks_010EgoldBuisnessChecking() throws ApiException {
        eCheck check = TestChecks.certification(SecCode.Pop, CheckType.Business, AccountType.Checking);
        Transaction response = check.charge(new BigDecimal("12.00"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // test case 27
        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void checks_011EgoldPersonalSavings() throws ApiException {
        eCheck check = TestChecks.certification(SecCode.Pop, CheckType.Personal, AccountType.Savings);
        Transaction response = check.charge(new BigDecimal("13.00"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void checks_012EgoldBusinessSavings() throws ApiException {
        eCheck check = TestChecks.certification(SecCode.Pop, CheckType.Business, AccountType.Savings);
        Transaction response = check.charge(new BigDecimal("14.00"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void checks_013EsilverPersonalChecking() throws ApiException {
        eCheck check = TestChecks.certification(SecCode.Pop, CheckType.Personal, AccountType.Checking);
        Transaction response = check.charge(new BigDecimal("15.00"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void checks_014EsilverBuisnessChecking() throws ApiException {
        eCheck check = TestChecks.certification(SecCode.Pop, CheckType.Business, AccountType.Checking);
        Transaction response = check.charge(new BigDecimal("16.00"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // test case 28
        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void checks_015EsilverPersonalSavings() throws ApiException {
        eCheck check = TestChecks.certification(SecCode.Pop, CheckType.Personal, AccountType.Savings);
        Transaction response = check.charge(new BigDecimal("17.00"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void checks_016EsilverBuisnessSavings() throws ApiException {
        eCheck check = TestChecks.certification(SecCode.Pop, CheckType.Business, AccountType.Savings);
        Transaction response = check.charge(new BigDecimal("18.00"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    @Ignore
    public void checks_017EbronzePersonalChecking() throws ApiException {
        eCheck check = TestChecks.certification(SecCode.Ebronze, CheckType.Personal, AccountType.Checking);
        check.setCheckVerify(true);
        Transaction response = check.charge(new BigDecimal("19.00"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    @Ignore
    public void checks_018EbronzePersonalChecking() throws ApiException {
        eCheck check = TestChecks.certification(SecCode.Ebronze, CheckType.Personal, AccountType.Checking);
        check.setCheckVerify(true);
        Transaction response = check.charge(new BigDecimal("20.00"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    @Ignore
    public void checks_019EbronzePersonalChecking() throws ApiException {
        eCheck check = TestChecks.certification(SecCode.Ebronze, CheckType.Personal, AccountType.Savings);
        check.setCheckVerify(true);
        Transaction response = check.charge(new BigDecimal("21.00"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    @Ignore
    public void checks_020EbronzeBusinessSavings() throws ApiException {
        eCheck check = TestChecks.certification(SecCode.Ebronze, CheckType.Business, AccountType.Savings);
        check.setCheckVerify(true);
        Transaction response = check.charge(new BigDecimal("22.00"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void checks_021WebPersonalChecking() throws ApiException {
        eCheck check = TestChecks.certification(SecCode.Web, CheckType.Personal, AccountType.Checking);
        Transaction response = check.charge(new BigDecimal("23.00"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void checks_022WebBuisnessChecking() throws ApiException {
        eCheck check = TestChecks.certification(SecCode.Web, CheckType.Business, AccountType.Checking);
        Transaction response = check.charge(new BigDecimal("24.00"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void checks_023WebPersonalSavings() throws ApiException {
        eCheck check = TestChecks.certification(SecCode.Web, CheckType.Personal, AccountType.Savings);
        Transaction response = check.charge(new BigDecimal("25.00"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // test case 29
        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void checks_024WebBusinessSavings() throws ApiException {
        eCheck check = TestChecks.certification(SecCode.Web, CheckType.Business, AccountType.Savings);
        Transaction response = check.charge(new BigDecimal("5.00"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
}

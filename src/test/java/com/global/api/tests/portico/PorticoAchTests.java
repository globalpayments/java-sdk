package com.global.api.tests.portico;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.paymentMethods.eCheck;
import com.global.api.serviceConfigs.PorticoConfig;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class PorticoAchTests {
    private eCheck check;
    private Address address;
    private final String clientTxnID;
    private static final String TRANSACTION_EXCEPTION = "Either ClientTxnId or GatewayTxnId must be provided for this payment type.";
    
    public PorticoAchTests() throws ApiException {
        PorticoConfig config = new PorticoConfig();
        config.setSecretApiKey("skapi_cert_MTyMAQBiHVEAewvIzXVFcmUd2UcyBge_eCpaASUp0A");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");
        config.setEnableLogging(true);

        ServicesContainer.configureService(config);

        int randomID = new Random().nextInt(999999 - 10000)+10000;
        clientTxnID = Integer.toString(randomID);

        check = new eCheck();
        check.setAccountNumber("1357902468");
        check.setRoutingNumber("122000030");
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
        check.setCheckName("John Doe");

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
                .withClientTransactionId(clientTxnID)
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
    @Test
    public void checkQuery_withClientTxnId() throws ApiException {
        Transaction response = check.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withAddress(address)
                .withClientTransactionId(clientTxnID)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction response1 = check.checkQuery()
                .withCurrency("USD")
                .withClientTxnId(response.getClientTransactionId())
                .execute();
        assertNotNull(response1);
        assertEquals("00", response1.getResponseCode());
    }
    @Test
    public void checkQuery_WithGatewayTxnId() throws ApiException {
        Transaction response = check.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withAddress(address)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction response1 = check.checkQuery()
                .withGatewayTxnId(response.getTransactionId())
                .withCurrency("USD")
                .execute();
        assertNotNull(response1);
        assertEquals("00", response1.getResponseCode());
    }
    // Negative Test case
    // without Id
    // With Incorrect GatewayTxnId
    @Test
    public void checkQuery_WithoutId() {
        UnsupportedTransactionException exc = assertThrows(UnsupportedTransactionException.class,()-> {
               check.checkQuery()
                    .withCurrency("USD")
                    .execute();
        });
        assertEquals(TRANSACTION_EXCEPTION,exc.getMessage());
    }
    @Test
    public void checkQuery_randomId() {
        GatewayException exc = assertThrows(GatewayException.class,()-> {
            Transaction response = check.checkQuery()
                    .withGatewayTxnId("5342315375")
                    .withCurrency("USD")
                    .execute();
            assertNotNull(response);
        });
        assertEquals("3", exc.getResponseCode());
    }
    @Test
    public void checkNewCryptoURL() throws ApiException {
        PorticoConfig config = new PorticoConfig();
        config.setSecretApiKey("skapi_cert_MTyMAQBiHVEAewvIzXVFcmUd2UcyBge_eCpaASUp0A");
        config.setServiceUrl("https://cert.api2-c.heartlandportico.com");

        ServicesContainer.configureService(config);
        Transaction response = check.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withAddress(address)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }
}

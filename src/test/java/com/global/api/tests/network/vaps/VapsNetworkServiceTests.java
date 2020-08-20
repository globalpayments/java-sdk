package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.abstractions.IBatchProvider;
import com.global.api.network.enums.CardDataInputCapability;
import com.global.api.network.enums.TerminalOutputCapability;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.services.NetworkService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.TestCards;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VapsNetworkServiceTests {
    private IBatchProvider batchProvider;
    private ArrayList<String> acceptedCodes = new ArrayList<String>() {{
       add("500");
       add("501");
       add("580");
    }};

    public VapsNetworkServiceTests() throws ApiException {
        Address address = new Address();
        address.setName("My STORE");
        address.setStreetAddress1("1 MY STREET");
        address.setCity("MYTOWN");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        AcceptorConfig acceptorConfig = new AcceptorConfig();
        acceptorConfig.setAddress(address);

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactEmv_ContactlessMsd_MagStripe_KeyEntry);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);

        // hardware software config values
        acceptorConfig.setHardwareLevel("62");
        acceptorConfig.setSoftwareLevel("858.5.08");
        acceptorConfig.setOperatingSystemLevel("00");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);

        // gateway config
        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.7eleven.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns-c.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0044");
        config.setTerminalId("0000912197711");
        config.setUniqueDeviceId("0001");
        config.setMerchantType("5541");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());

        batchProvider = BatchProvider.getInstance();

        ServicesContainer.configureService(config);
    }

    @Test
    public void test_001_resubmitDataCollect() throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        Transaction response = track.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withBatchNumber(batchProvider.getBatchNumber(), batchProvider.getSequenceNumber())
                .execute();
        assertNotNull(response);
        assertNotNull(response.getTransactionToken());

        Transaction resubmit = NetworkService.resubmitDataCollect(response.getTransactionToken())
                .execute();
        assertNotNull(resubmit);
        assertEquals("000", resubmit.getResponseCode());
    }

    @Test
    public void test_002_resubmitBatchClose() throws ApiException {
        Transaction batchClose = BatchService.closeBatch(1, new BigDecimal(10), BigDecimal.ZERO)
                .withBatchNumber(batchProvider.getBatchNumber())
                .execute();
        assertNotNull(batchClose);
        assertNotNull(batchClose.getTransactionToken());
        assertTrue(acceptedCodes.contains(batchClose.getResponseCode()));

        Transaction resubmit = NetworkService.resubmitBatchClose(batchClose.getTransactionToken())
                .execute();
        assertNotNull(resubmit);
        assertTrue(acceptedCodes.contains(resubmit.getResponseCode()));
    }

    @Test
    public void test_003_resubmitDataCollect_forced() throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        Transaction response = track.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withBatchNumber(batchProvider.getBatchNumber(), batchProvider.getSequenceNumber())
                .execute();
        assertNotNull(response);
        assertNotNull(response.getTransactionToken());

        Transaction resubmit = NetworkService.resubmitDataCollect(response.getTransactionToken())
                .withForceToHost(true)
                .withBatchNumber(71, 32)
                .execute();
        assertNotNull(resubmit);
        assertEquals("000", resubmit.getResponseCode());
    }

    @Test
    public void test_004_resubmitBatchClose_forced() throws ApiException {
        Transaction batchClose = BatchService.closeBatch(1, new BigDecimal(10), BigDecimal.ZERO)
                .withBatchNumber(batchProvider.getBatchNumber())
                .withForceToHost(true)
                .execute();
        assertNotNull(batchClose);
        assertNotNull(batchClose.getTransactionToken());
        assertTrue(acceptedCodes.contains(batchClose.getResponseCode()));

        Transaction resubmit = NetworkService.resubmitBatchClose(batchClose.getTransactionToken())
                .execute();
        assertNotNull(resubmit);
        assertTrue(acceptedCodes.contains(resubmit.getResponseCode()));
    }
}

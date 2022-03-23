package com.global.api.tests.network.nts;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.entities.nts.NtsRequestToBalanceData;
import com.global.api.network.enums.CardDataInputCapability;
import com.global.api.network.enums.CardHolderAuthenticationCapability;
import com.global.api.network.enums.TerminalOutputCapability;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NtsRequestToBalanceTest {
    // gateway config
    NetworkGatewayConfig config;
    private NtsRequestMessageHeader ntsRequestMessageHeader;

    public NtsRequestToBalanceTest() throws ConfigurationException {
        Address address = new Address();
        address.setName("My STORE            ");
        address.setStreetAddress1("1 MY STREET       ");
        address.setCity("JEFFERSONVILLE  ");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        AcceptorConfig acceptorConfig = new AcceptorConfig();
        acceptorConfig.setAddress(address);
        ntsRequestMessageHeader = new NtsRequestMessageHeader();

        ntsRequestMessageHeader.setTerminalDestinationTag("478");
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.RequestToBalacnce);
        ntsRequestMessageHeader.setPriorMessageResponseTime(999);
        ntsRequestMessageHeader.setPriorMessageConnectTime(999);
        ntsRequestMessageHeader.setPriorMessageCode("08");

        // data code values
        // acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactlessMsd_KeyEntry);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.None);
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactlessMsd_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);

        //  acceptorConfig.setCardDataOutputCapability(CardDataOutputCapability.Unknown);
        // acceptorConfig.setPinCaptureCapability(PinCaptureCapability.Unknown);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);

        // gateway config
        // gateway config
        config = new NetworkGatewayConfig(Target.NTS);
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());
        config.setAcceptorConfig(acceptorConfig);

        // NTS Related configurations
        config.setBinTerminalId(" ");
        config.setBinTerminalType(" ");
        config.setInputCapabilityCode(CardDataInputCapability.ContactEmv_MagStripe);
        config.setTerminalId("21");
        config.setUnitNumber("00066654534");
        config.setSoftwareVersion("21");
        config.setLogicProcessFlag(LogicProcessFlag.Capable);
        config.setTerminalType(TerminalType.VerifoneRuby2Ci);
        
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);
    }

    @Test //working
    public void test_RequestToBalance_06() throws ApiException {
        NtsRequestToBalanceData data = new NtsRequestToBalanceData(1, new BigDecimal(1), "Version");

        Transaction batchClose = BatchService.closeBatch(BatchCloseType.Forced,
                        ntsRequestMessageHeader, 11, 0
                        , BigDecimal.ZERO, BigDecimal.ZERO, data)
                .execute();
        assertNotNull(batchClose);
        // check response
        assertEquals("00", batchClose.getResponseCode());

    }

    @Test //working
    public void test_RequestToBalance_16() throws ApiException {

        NtsRequestToBalanceData data = new NtsRequestToBalanceData(1, new BigDecimal(1), "Version");

        Transaction batchClose = BatchService.closeBatch(BatchCloseType.Forced,
                        ntsRequestMessageHeader, 11, 0
                        , BigDecimal.ZERO, BigDecimal.ZERO, data)
                .execute();
        assertNotNull(batchClose);
        // check response
        assertEquals("00", batchClose.getResponseCode());

    }

    @Test //working
    public void test_RequestToBalance_C6() throws ApiException {
        NtsRequestToBalanceData data = new NtsRequestToBalanceData(1, new BigDecimal(1), "Version");

        Transaction batchClose = BatchService.closeBatch(BatchCloseType.Forced,
                        ntsRequestMessageHeader, 11, 0
                        , BigDecimal.ZERO, BigDecimal.ZERO, data)
                .execute();
        assertNotNull(batchClose);
        // check response
        assertEquals("00", batchClose.getResponseCode());
    }

    @Test
    public void test_RequestToBalance_D6() throws ApiException {

        NtsRequestToBalanceData data = new NtsRequestToBalanceData(1, new BigDecimal(1), "Version");


        Transaction batchClose = BatchService.closeBatch(BatchCloseType.EndOfShift,
                        ntsRequestMessageHeader, 11, 0
                        , BigDecimal.ZERO, BigDecimal.ZERO, data)
                .execute();
        assertNotNull(batchClose);
        // check response
        assertEquals("00", batchClose.getResponseCode());

    }
}

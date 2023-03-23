package com.global.api.tests.network.nts;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.entities.nts.NtsUtilityMessageRequest;
import com.global.api.network.entities.nts.PriorMessageInfo;
import com.global.api.network.enums.CardDataInputCapability;
import com.global.api.network.enums.CardHolderAuthenticationCapability;
import com.global.api.network.enums.TerminalOutputCapability;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.NetworkService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NtsUtilityMessageTests {
    private DebitTrackData track;
    private NtsRequestMessageHeader ntsRequestMessageHeader;
    private PriorMessageInfo priorMessageInfo;
    // gateway config
    NetworkGatewayConfig config;

    public NtsUtilityMessageTests() throws ApiException {
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
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.UtilityMessage);
        priorMessageInfo=new PriorMessageInfo();
        priorMessageInfo.setPriorMessageResponseTime(999);
        priorMessageInfo.setPriorMessageConnectTime(999);
        priorMessageInfo.setPriorMessageCode("08");

        ntsRequestMessageHeader.setPriorMessageInfo(priorMessageInfo);

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
        ServicesContainer.configureService(config);

        config.setMerchantType("5541");
        ServicesContainer.configureService(config, "ICR");


        ServicesContainer.configureService(config, "timeout");
        EncryptionData encryptionData = new EncryptionData();
        encryptionData.setKsn("A504010005E0003C    ");
    }
    @Test //working
    public void test_Nts_Utility_Message_Success_00() throws ApiException {

        NtsUtilityMessageRequest ntsUtilityMessageRequest = new NtsUtilityMessageRequest();
        ntsUtilityMessageRequest.setUtilityType(001);
        ntsUtilityMessageRequest.setFullVendorSoftwareVersion("123123123123123123132112313213");
        ntsUtilityMessageRequest.setReserved("");

        Transaction response = NetworkService.sendUtilityMessage()
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsUtilityMessageRequest(ntsUtilityMessageRequest)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());
    }
    @Test //working
    public void test_Nts_Utility_Message_Failure_40() throws ApiException {

        NtsUtilityMessageRequest ntsUtilityMessageRequest = new NtsUtilityMessageRequest();
        ntsUtilityMessageRequest.setUtilityType(001);
        ntsUtilityMessageRequest.setFullVendorSoftwareVersion("123123123123123123132112313213");
        ntsUtilityMessageRequest.setReserved("");

        Transaction response = NetworkService.sendUtilityMessage()
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsUtilityMessageRequest(ntsUtilityMessageRequest)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test(expected = GatewayException .class) //working
    public void test_Nts_Utility_Message_Format_Error_70() throws ApiException {

        NtsUtilityMessageRequest ntsUtilityMessageRequest = new NtsUtilityMessageRequest();
        ntsUtilityMessageRequest.setUtilityType(002);
        ntsUtilityMessageRequest.setFullVendorSoftwareVersion("123123123123123123132112313213");
        ntsUtilityMessageRequest.setReserved("");

        Transaction response = NetworkService.sendUtilityMessage()
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsUtilityMessageRequest(ntsUtilityMessageRequest)
                .execute();

        // check response
        //assertEquals("00", response.getResponseCode());
    }
    @Test(expected = GatewayException.class) //working
    public void test_Nts_Utility_Message_Terminal_TimeOut_80() throws ApiException {
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net12");
        config.setPrimaryPort(1503112);
        config.setSecondaryEndpoint("test.txns-c.secureexchange.net2332");
        config.setSecondaryPort(150312312);
        config.setCompanyId("0045213");
        ServicesContainer.configureService(config);

        NtsUtilityMessageRequest ntsUtilityMessageRequest = new NtsUtilityMessageRequest();
        ntsUtilityMessageRequest.setUtilityType(001);
        ntsUtilityMessageRequest.setFullVendorSoftwareVersion("123123123123123123132112313213");
        ntsUtilityMessageRequest.setReserved("");

        Transaction response = NetworkService.sendUtilityMessage()
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsUtilityMessageRequest(ntsUtilityMessageRequest)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());
    }
    @Ignore
    @Test(expected = GatewayException.class) //working
    public void test_Nts_Utility_Message_Terminal_TimeOut_90() throws ApiException {

        NtsUtilityMessageRequest ntsUtilityMessageRequest = new NtsUtilityMessageRequest();
        ntsUtilityMessageRequest.setUtilityType(001);
        ntsUtilityMessageRequest.setFullVendorSoftwareVersion("123123123123123123132112313213");
        ntsUtilityMessageRequest.setReserved("");

        Transaction response = NetworkService.sendUtilityMessage()
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsUtilityMessageRequest(ntsUtilityMessageRequest)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());
    }
}

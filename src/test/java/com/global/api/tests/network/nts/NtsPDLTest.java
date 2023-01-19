package com.global.api.tests.network.nts;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.network.entities.NtsPDLData;
import com.global.api.network.entities.nts.NtsPDLResponseData;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.enums.CardDataInputCapability;
import com.global.api.network.enums.CardHolderAuthenticationCapability;
import com.global.api.network.enums.TerminalOutputCapability;
import com.global.api.network.enums.nts.PDLParameterType;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.NetworkService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NtsPDLTest {
    // gateway config
    NetworkGatewayConfig config;
    NtsRequestMessageHeader ntsRequestMessageHeader; //Main Request header class

   public NtsPDLTest() throws ConfigurationException {
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
       ntsRequestMessageHeader.setTerminalDestinationTag("510");
       ntsRequestMessageHeader.setPinIndicator(PinIndicator.NotPromptedPin);
       ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ParameterDataLoad);
       ntsRequestMessageHeader.setPriorMessageResponseTime(1);
       ntsRequestMessageHeader.setPriorMessageConnectTime(999);
       ntsRequestMessageHeader.setPriorMessageCode("01");


       acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.None);
       acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactlessMsd_KeyEntry);
       acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);

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
       config.setSoftwareVersion("21");
       config.setLogicProcessFlag(LogicProcessFlag.Capable);
       config.setTerminalType(TerminalType.VerifoneRuby2Ci);

       config.setCompanyId("009");
       config.setUnitNumber("00001234567");
       config.setTerminalId("01");

       config.setMerchantType("5541");
       ServicesContainer.configureService(config);
   }

    @Test
    public void test_single_PDL_fetch_001() throws ApiException {

        // PDL configurations.
        NtsPDLData ntsPDLData = new NtsPDLData();
        ntsPDLData.setParameterType(PDLParameterType.RequestAllParameter);
        ntsPDLData.setParameterVersion("020");
        ntsPDLData.setBlockSequenceNumber("00");

        Transaction response = NetworkService.fetchPDL(TransactionType.PDL)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_single_PDL_fetch_with_parameter_version_number_002() throws ApiException {

        // PDL configurations.
        NtsPDLData ntsPDLData = new NtsPDLData();
        ntsPDLData.setParameterType(PDLParameterType.RequestAllParameter);
        ntsPDLData.setParameterVersion("019");
        ntsPDLData.setBlockSequenceNumber("00");

        Transaction response = NetworkService.fetchPDL(TransactionType.PDL)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }


    @Test
    public void test_PDL_Expanded_response_003() throws ApiException {

        StringBuilder userDataResponse = new StringBuilder();

        // PDL configurations.
        NtsPDLData ntsPDLData = new NtsPDLData();
        ntsPDLData.setParameterType(PDLParameterType.RequestAllParameter);
        ntsPDLData.setParameterVersion("020");
        ntsPDLData.setBlockSequenceNumber("00");

        Transaction response = NetworkService.fetchPDL(TransactionType.PDL)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        NtsPDLResponseData obj = (NtsPDLResponseData) response.getNtsResponse().getNtsResponseMessage();
        String parameterType = obj.getNextParameterType();
        System.out.println("Parameter Type: " + obj.getParameterType());
        System.out.println("Network data length: " + obj.getNetworkDataLength());
        System.out.println("Next parameter sequence number: " + obj.getNextSequenceNumber());
        System.out.println("Parameter sequence number: " + obj.getParameterSequenceNumber());

        while (!parameterType.equals("00")) {
            String seq = obj.getNextSequenceNumber();
            ntsPDLData.setBlockSequenceNumber(seq);
            String parameterVersion = obj.getParameterVersion();
            ntsPDLData.setParameterVersion(parameterVersion);

            Transaction preResponse = NetworkService.fetchPDL(TransactionType.PDL)
                    .withPDLData(ntsPDLData)
                    .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                    .execute();

            obj = (NtsPDLResponseData) preResponse.getNtsResponse().getNtsResponseMessage();
            parameterType = obj.getNextParameterType();


            System.out.println("Next parameter sequence number: " + obj.getNextSequenceNumber());
            System.out.println("Parameter sequence number: " + obj.getParameterSequenceNumber());
            System.out.println("Parameter Type: " + obj.getParameterType());
            System.out.println("User data length: " + obj.getUserDataLength());
            if(obj.getUserData() != null) {
                userDataResponse.append(obj.getUserData());
            }

        }

        String seq = obj.getNextSequenceNumber();
        ntsPDLData.setBlockSequenceNumber(seq);
        String parameterVersion = obj.getParameterVersion();
        ntsPDLData.setParameterVersion(parameterVersion);
        ntsPDLData.setParameterType(PDLParameterType.ConfirmParameterDataReceived);

        Transaction preResponse = NetworkService.fetchPDL(TransactionType.PDL)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        System.out.println(userDataResponse.toString());

    }
}

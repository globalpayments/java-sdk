package com.global.api.tests.network.nts;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.entities.nts.PriorMessageInfo;
import com.global.api.network.enums.CardDataInputCapability;
import com.global.api.network.enums.CardHolderAuthenticationCapability;
import com.global.api.network.enums.TerminalOutputCapability;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.NetworkService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NtsRequestPendingMessagesTest {
    private NtsRequestMessageHeader ntsRequestMessageHeader;
    private PriorMessageInfo priorMessageInfo;
    NetworkGatewayConfig config;

    public NtsRequestPendingMessagesTest() throws ApiException {
        {
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
            priorMessageInfo=new PriorMessageInfo();
            priorMessageInfo.setPriorMessageResponseTime(999);
            priorMessageInfo.setPriorMessageConnectTime(999);
            priorMessageInfo.setPriorMessageCode("08");

            ntsRequestMessageHeader.setPriorMessageInfo(priorMessageInfo);

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
            config.setTerminalId("01");
            config.setUnitNumber("00001234567");
            config.setSoftwareVersion("21");
            config.setCompanyId("014");
            config.setLogicProcessFlag(LogicProcessFlag.Capable);
            config.setTerminalType(TerminalType.VerifoneRuby2Ci);

            ServicesContainer.configureService(config);

            config.setMerchantType("5541");
        }
    }


    @Test
    public void RequestPendingMessages_MailWaitingPDLWaiting() throws ApiException {

        Transaction response = NetworkService.sendRequestPendingMesssages()
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void RequestPendingMessages_NoMailNoPDLWaiting() throws ApiException {

        Transaction response = NetworkService.sendRequestPendingMesssages()
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
}




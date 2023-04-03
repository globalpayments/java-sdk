package com.global.api.tests.network.nts;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.entities.nts.PriorMessageInfo;
import com.global.api.network.enums.CardDataInputCapability;
import com.global.api.network.enums.CardHolderAuthenticationCapability;
import com.global.api.network.enums.OperatingEnvironment;
import com.global.api.network.enums.TerminalOutputCapability;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NtsVisaReadyLinkTest {
    // gateway config
    NetworkGatewayConfig config;
    String emvTagData = "4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F280208405F2A0208405F3401019F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000";
    private CreditTrackData track;
    private CreditCardData card;
    private NtsRequestMessageHeader ntsRequestMessageHeader;
    private PriorMessageInformation priorMessageInformation;

    public NtsVisaReadyLinkTest() throws ConfigurationException {
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
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.NotPromptedPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.PinDebit);

        priorMessageInformation =new PriorMessageInformation();
        priorMessageInformation.setResponseTime("999");
        priorMessageInformation.setConnectTime("999");
        priorMessageInformation.setMessageReasonCode("08");
        ntsRequestMessageHeader.setPriorMessageInformation(priorMessageInformation);

        // data code values
        // acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactlessMsd_KeyEntry);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.None);
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactlessMsd_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.Attended);

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
        config.setCompanyId("009");
        config.setUnitNumber("00010161983");
        config.setTerminalId("01");
        config.setSoftwareVersion("21");
        config.setLogicProcessFlag(LogicProcessFlag.Capable);
        config.setTerminalType(TerminalType.VerifoneRuby2Ci);

        ServicesContainer.configureService(config);

        // debit card
        track = new CreditTrackData();
        track.setValue("4111111111111111=1225");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setCardType("VisaReadyLink");

        card = new CreditCardData();
        card.setNumber("4111111111111111");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardType("VisaReadyLink");
    }
    @Test //working
    public void test_ReadyLink_load_01_Manual() throws ApiException {

        Transaction response = card.addValue(new BigDecimal(19))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test //working
    public void test_ReadyLink_load_01_manual() throws ApiException {

        Transaction response = card.addValue(new BigDecimal(20))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test //working
    public void test_ReadyLink_load_01() throws ApiException {

        Transaction response = track.addValue(new BigDecimal(600))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test //working
    public void test_ReadyLink_loadReversal_02() throws ApiException {

        Transaction response = track.addValue(new BigDecimal(750))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        response = response.reverse(new BigDecimal(750))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test //working
    public void test_ReadyLink_load_credit_03() throws ApiException {

        Transaction response = track.addValue(new BigDecimal(750))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.NotPromptedPin);


        Transaction dataCollectResponse = response.capture(new BigDecimal(750))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test //working
    public void test_ReadyLink_load_EMV_04() throws ApiException {

        Transaction response = track.addValue(new BigDecimal(142))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .withCardSequenceNumber("123")
                .withUniqueDeviceId("  14")
                .withModifier(TransactionModifier.OfflineDecline)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test //working
    public void test_ReadyLink_load_credit_EMV_05() throws ApiException {

        Transaction response = track.addValue(new BigDecimal(142))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .withCardSequenceNumber("123")
                .withUniqueDeviceId("  14")
                .withModifier(TransactionModifier.OfflineDecline)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.NotPromptedPin);


        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test //working
    public void test_ReadyLink_loadReversal_EMV_06() throws ApiException {

        Transaction response = track.addValue(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .withCardSequenceNumber("123")
                .withUniqueDeviceId("  14")
                .withModifier(TransactionModifier.OfflineDecline)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        response = response.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .withCardSequenceNumber("123")
                .withUniqueDeviceId("  14")
                .withModifier(TransactionModifier.OfflineDecline)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }
}

package com.global.api.tests.network.nts.certification_v24_1;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.network.entities.NtsProductData;
import com.global.api.network.entities.NtsTag16;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.entities.nts.NtsUtilityMessageRequest;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.NetworkService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.joda.time.DateTime;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NTS_TokenizationTests {
    private CreditCardData card;
    private NtsRequestMessageHeader header; //Main Request header class
    private NtsTag16 tag;
    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;
    private NtsProductData productData;
    private PriorMessageInformation priorMessageInformation;
    Integer Stan = Integer.parseInt(DateTime.now().toString("hhmmss"));
    public NTS_TokenizationTests() throws ConfigurationException {

        acceptorConfig = new AcceptorConfig();
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.Attended);

        // Address details.
        Address address = new Address();
        address.setName("My STORE            ");
        address.setStreetAddress1("1 MY STREET       ");
        address.setCity("JEFFERSONVILLE  ");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        acceptorConfig.setAddress(address);

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_ContactlessMsd_MagStripe_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.ByMerchant);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);
        // Setting operating environment
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.Attended);

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(false);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setCapableAmexRemainingBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setCapableVoid(true);
        acceptorConfig.setSupportsEmvPin(true);
        acceptorConfig.setMobileDevice(true);
        acceptorConfig.setPosActionCode(true);
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        //tokenization configuration
        acceptorConfig.setServiceType(ServiceType.GPN_API);
        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        acceptorConfig.setTokenizationType(TokenizationType.MerchantTokenization);
        acceptorConfig.setMerchantId("00009121977");

        header = new NtsRequestMessageHeader();
        header.setTerminalDestinationTag("999");
        header.setPinIndicator(PinIndicator.NotPromptedPin);
        header.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        priorMessageInformation =new PriorMessageInformation();
        priorMessageInformation.setResponseTime("1");
        priorMessageInformation.setConnectTime("999");
        priorMessageInformation.setMessageReasonCode("01");

        header.setPriorMessageInformation(priorMessageInformation);

        tag = new NtsTag16();
        tag.setPumpNumber(1);
        tag.setWorkstationId(1);
        tag.setServiceCode(ServiceCode.Self);
        tag.setSecurityData(SecurityData.NoAVSAndNoCVN);

        // gateway config
        config = new NetworkGatewayConfig(Target.NTS);
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns-e.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());
        config.setAcceptorConfig(acceptorConfig);

        // NTS Related configurations
        config.setBinTerminalId(" ");
        config.setBinTerminalType(" ");
        config.setInputCapabilityCode(CardDataInputCapability.ContactlessEmv_ContactEmv_MagStripe_KeyEntry);
        config.setTerminalId("01");
        config.setUnitNumber("00012378911");
        config.setSoftwareVersion("01");
        config.setCompanyId("009");
        config.setLogicProcessFlag(LogicProcessFlag.Capable);
        config.setTerminalType(TerminalType.VerifoneRuby2Ci);
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

    }


    @Test //working
    public void test_Nts_Utility_Message_credit_mastercard_tokenization() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.Tokenize);
        config.setAcceptorConfig(acceptorConfig);

        header.setNtsMessageCode(NtsMessageCode.UtilityMessage);

        NtsUtilityMessageRequest ntsUtilityMessageRequest = new NtsUtilityMessageRequest();
        ntsUtilityMessageRequest.setUtilityType(002);
        ntsUtilityMessageRequest.setReserved("");

        CreditCardData cardData = new CreditCardData();
        cardData.setNumber("5473500000000014");
        cardData.setExpMonth(12);
        cardData.setExpYear(2025);
        cardData.setTokenizationData("5473500000000014");

        ntsUtilityMessageRequest.setICardData(cardData);

        Transaction response = NetworkService.sendUtilityMessage()
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withNtsUtilityMessageRequest(ntsUtilityMessageRequest)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_reversal_without_track_mastercard() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        config.setAcceptorConfig(acceptorConfig);

        card = new CreditCardData();
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");
        card.setCardPresent(true);
        card.setReaderPresent(true);
        card.setCardType("MC");
        card.setTokenizationData("D7EB9013AD6E5962440994A6155221446250CC77A23ECD6DBAD5506337DB7C0E3A4BF39D97450709EE7D2C2FBB547E42ADC851E0B29ABC2DBB4EC4E07CC26526");

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withCvn("123")
                .execute();
        assertNotNull(response);

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .execute();
        assertEquals("00", voidResponse.getResponseCode());
    }

}

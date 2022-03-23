package com.global.api.tests.network.nts;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.network.entities.nts.NtsMailData;
import com.global.api.network.entities.nts.NtsMailResponse;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.enums.CardDataInputCapability;
import com.global.api.network.enums.CardHolderAuthenticationCapability;
import com.global.api.network.enums.TerminalOutputCapability;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.NetworkService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.utils.StringUtils;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NtsMailTest {
    private NtsRequestMessageHeader ntsRequestMessageHeader;
    // gateway config
    NetworkGatewayConfig config;
    public NtsMailTest() throws ApiException {
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
            ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.PinDebit);
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

            // with merchant type
            //  config.setMerchantType("5542");
            // ServicesContainer.configureService(config, "ICR");

            // forced timeoute
            //  config.setForceGatewayTimeout(true);
            ServicesContainer.configureService(config, "timeout");
        }
    }
    @Test //working
    public void test_Mail_Message_With_MessageText_Approved_00() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.Mail);

        NtsMailData ntsMailData = new NtsMailData();
        ntsMailData.setMailCount(0);
        ntsMailData.setMailIndicator(MailIndicatorType.SendingMailFromTheTerminal);
        ntsMailData.setMailMessageType(0);
        //ntsMailRequest.setMailMessageCode(MailMessageCodeType.TerminalConfigurationMail);
        ntsMailData.setMailMessageCode(MailMessageCodeType.TerminalConfigurationMail);
        ntsMailData.setMailTextLength(180);
        ntsMailData.setMailText(StringUtils.padLeft(" ", 180, '0'));

        Transaction response = NetworkService.sendMail(new BigDecimal(10))
                .withCurrency("USD")
                .withTransactiontype(TransactionType.Mail)
               // .withntsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsMailData(ntsMailData)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        NtsMailResponse mailResponse = (NtsMailResponse) response.getNtsResponse().getNtsResponseMessage();
//        assertEquals(MailIndicatorType.Unable_To_Send_Mail, mailResponse.getMailIndicator());

        assertEquals(NtsMessageCode.Mail, response.getNtsResponse().getNtsResponseMessageHeader().getNtsNetworkMessageHeader().getNtsMessageCode());
    }
    @Test //working
    public void test_Mail_Message_Without_MessageText_Approved_00() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.Mail);

        NtsMailData ntsMailData = new NtsMailData();
        ntsMailData.setMailCount(0);
       // ntsMailRequest.setMailIndicator(MailIndicatorType.Sending_Mail_From_The_Terminal);
        ntsMailData.setMailIndicator(MailIndicatorType.SendMail);
        ntsMailData.setMailMessageType(0);
        //ntsMailRequest.setMailMessageCode(MailMessageCodeType.TerminalConfigurationMail);
        ntsMailData.setMailMessageCode(MailMessageCodeType.FSC);
       // ntsMailRequest.setMailTextLength(63);
       // ntsMailRequest.setMailText("hi this is testing of mail transaction");

        Transaction response = NetworkService.sendMail(new BigDecimal(10))
                .withCurrency("USD")
                .withTransactiontype(TransactionType.Mail)
               // .withntsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsMailData(ntsMailData)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }
    @Test(expected = GatewayException.class) //workinge HOST RESPONSE AREA
    public void test_Mail_Message_Terminal_Format_Error_70() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.RequestToBalacnce);

        NtsMailData ntsMailData = new NtsMailData();
        ntsMailData.setMailCount(0);
        ntsMailData.setMailIndicator(MailIndicatorType.SendingMailFromTheTerminal);
        ntsMailData.setMailMessageType(0);
        ntsMailData.setMailMessageCode(MailMessageCodeType.TerminalConfigurationMail);
        ntsMailData.setMailTextLength(63);
        ntsMailData.setMailText("hi this is testing of mail transaction");

        Transaction response = NetworkService.sendMail(new BigDecimal(10))
                .withCurrency("USD")
                .withTransactiontype(TransactionType.Mail)
               // .withntsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsMailData(ntsMailData)
                .execute();

    }
}

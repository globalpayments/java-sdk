package com.global.api.tests.network.nts;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.entities.nts.POSSiteConfigurationData;
import com.global.api.network.entities.nts.PriorMessageInfo;
import com.global.api.network.enums.CardDataInputCapability;
import com.global.api.network.enums.CardHolderAuthenticationCapability;
import com.global.api.network.enums.TerminalOutputCapability;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.NetworkService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class NTSPOSConfigurationMessageTest {
    private NtsRequestMessageHeader ntsRequestMessageHeader;
    private PriorMessageInformation priorMessageInformation;
    // gateway config
    NetworkGatewayConfig config;
    public NTSPOSConfigurationMessageTest() throws ApiException {
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
            ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.PosSiteConfiguration);

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
            config.setSecondaryEndpoint("test.txns-e.secureexchange.net");
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
        }
    }

    @Test
    public void test_POS_site_configuration() throws ApiException {

        // Preparing for POS site configuration.
        POSSiteConfigurationData posSiteConfiguration = new POSSiteConfigurationData();
        posSiteConfiguration.setMessageVersion("1");
        posSiteConfiguration.setCompanyName("Test Company");
        posSiteConfiguration.setHeartlandCompanyId("045");
        posSiteConfiguration.setMerchantFranchiseName("Test Merchant");
        posSiteConfiguration.setMerchantIdUnitPlusTid("1245#dcdc");
        posSiteConfiguration.setMerchantAddressStreet("My Street");
        posSiteConfiguration.setMerchantAddressCity("My City");
        posSiteConfiguration.setMerchantAddressState("US");
        posSiteConfiguration.setMerchantAddressZip("345");
        posSiteConfiguration.setMerchantPhoneNumber("123-123-1234");
        posSiteConfiguration.setSiteBrand("My Site Brand");
        posSiteConfiguration.setPosSystemType("I");
        posSiteConfiguration.setMethodOfOperation("A");
        posSiteConfiguration.setPosVendor("VERIFONE");
        posSiteConfiguration.setPosProductNameOrModel("RUBY 2 C1");
        posSiteConfiguration.setHeartlandPosTerminalType("88");
        posSiteConfiguration.setHeartlandPosSoftwareVersion("1.1.1.1");
        posSiteConfiguration.setHeartlandTerminalSpecVersion("31");
        posSiteConfiguration.setPosHardwareVersion("0212");
        posSiteConfiguration.setPosSoftwareVersion("1.1.1");
        posSiteConfiguration.setPosOperatingSystem("V2");
        posSiteConfiguration.setMiddlewareVendor("Test Vendor");
        posSiteConfiguration.setMiddlewareProductNameOrModel("Model 1");
        posSiteConfiguration.setMiddlewareType("H");
        posSiteConfiguration.setMiddlewareSoftwareVersion("001");
        posSiteConfiguration.setReceiptPrinterType("I");
        posSiteConfiguration.setReceiptPrinterModel("Printer Model 1");
        posSiteConfiguration.setJournalPrinterType("I");
        posSiteConfiguration.setJournalPrinterModel("JournalPrinVer");
        posSiteConfiguration.setInsidePedMultiLaneDeviceType("I");
        posSiteConfiguration.setInsidePedMultiLaneDeviceVendor("PED Vendor");
        posSiteConfiguration.setInsidePedMultiLaneDeviceProductNameOrModel("PED Model 1");
        posSiteConfiguration.setPinEncryptionInside("S");
        posSiteConfiguration.setOutsidePedType("I");
        posSiteConfiguration.setOutsidePedVendor("PED Vendor");
        posSiteConfiguration.setOutsidePedProductNameOrModel("PED Model 1");
        posSiteConfiguration.setPinEncryptionOutside("S");
        posSiteConfiguration.setCheckReaderVendor("Check Vendor");
        posSiteConfiguration.setCheckReaderProductNameOrModel("Check Product 1");
        posSiteConfiguration.setInsideContactlessReaderType("I");
        posSiteConfiguration.setInsideContactlessReaderVendor("InsideVer");
        posSiteConfiguration.setInsideContactlessReaderProductNameOrModel("InsideModel 1");
        posSiteConfiguration.setOutsideContactlessReaderType("I");
        posSiteConfiguration.setOutsideContactlessReaderVendor("Reader Vendor 2");
        posSiteConfiguration.setOutsideContactlessReaderProductNameOrModel("Reader Model 2");
        posSiteConfiguration.setCommunicationMedia("D");
        posSiteConfiguration.setCommunicationProtocol("T");
        posSiteConfiguration.setInternetBroadbandUse("W");
        posSiteConfiguration.setDatawireAccess("A");
        posSiteConfiguration.setMicronodeModelNumber("Model 1");
        posSiteConfiguration.setMicronodeSoftwareVersion("0021");
        posSiteConfiguration.setModemRouterType("I");
        posSiteConfiguration.setModemRouterVendor("Modem Vendor 1");
        posSiteConfiguration.setModemRouterProductNameOrModel("Modem product 1");
        posSiteConfiguration.setModemPhoneNumber("123-123-1234");
        posSiteConfiguration.setHeartlandPrimaryDialNumberOrIpPort("123-123-1234");
        posSiteConfiguration.setHeartlandSecondaryDialNumberOrIpPort("123-123-1234");
        posSiteConfiguration.setDispenserInterfaceVendor("Interface v1");
        posSiteConfiguration.setDispenserInterfaceProductNameOrModel("dispenser m1");
        posSiteConfiguration.setDispenserInterfaceSoftwareVersion("0001");
        posSiteConfiguration.setDispenserVendor("Vendor 1");
        posSiteConfiguration.setDispenserProductNameOrModel("Model 1");
        posSiteConfiguration.setDispenserSoftwareVersion("0001");
        posSiteConfiguration.setDispenserQuantity("8");
        posSiteConfiguration.setNumberOfScannersPeripherals("2");
        posSiteConfiguration.setScanner1Vendor("vendor 1");
        posSiteConfiguration.setScanner1ProductNameOrModel("product 1");
        posSiteConfiguration.setScanner1SoftwareVersion("001");
        posSiteConfiguration.setPeripheral2Vendor("vendor 1");
        posSiteConfiguration.setPeripheral2ProductNameOrModel("product 1");
        posSiteConfiguration.setPeripheral2SoftwareVersion("001");
        posSiteConfiguration.setPeripheral3Vendor("vendor 1");
        posSiteConfiguration.setPeripheral3ProductNameOrModel("product 1");
        posSiteConfiguration.setPeripheral3SoftwareVersion("001");
        posSiteConfiguration.setPeripheral4Vendor("vendor 1");
        posSiteConfiguration.setPeripheral4ProductNameOrModel("product 1");
        posSiteConfiguration.setPeripheral4SoftwareVersion("001");
        posSiteConfiguration.setPeripheral5Vendor("vendor 1");
        posSiteConfiguration.setPeripheral5ProductNameOrModel("product 1");
        posSiteConfiguration.setPeripheral5SoftwareVersion("001");

        Transaction response = NetworkService.sendSiteConfiguration()
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withPOSSiteConfigData(posSiteConfiguration)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }
}

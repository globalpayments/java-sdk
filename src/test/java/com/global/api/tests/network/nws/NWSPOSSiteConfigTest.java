package com.global.api.tests.network.nws;


import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Target;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.nts.POSSiteConfigurationData;
import com.global.api.network.enums.*;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.NetworkService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NWSPOSSiteConfigTest {
    POSSiteConfigurationData posSiteConfiguration;
    public NWSPOSSiteConfigTest() throws ApiException{
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
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_ContactlessMsd_MagStripe_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.ByMerchant);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);
        acceptorConfig.setSupportsEmvPin(true);

        // gateway config
        NetworkGatewayConfig config = new NetworkGatewayConfig(Target.NWS);
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0044");
        config.setTerminalId("0000912197711");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());

        ServicesContainer.configureService(config);

        // Preparing for POS site configuration.
        posSiteConfiguration = new POSSiteConfigurationData(Target.NWS);
        posSiteConfiguration.setMessageVersion("001");
        posSiteConfiguration.setCompanyName("Test Company");
        posSiteConfiguration.setGlobalPaymentsCompanyId("0044");
        posSiteConfiguration.setMerchantFranchiseName("Test Merchant");
        posSiteConfiguration.setMerchantIdUnitPlusTid("0000912197711");
        posSiteConfiguration.setMerchantAddressStreet("My Street");
        posSiteConfiguration.setMerchantAddressCity("My City");
        posSiteConfiguration.setMerchantAddressState("US");
        posSiteConfiguration.setMerchantAddressZip("12345");
        posSiteConfiguration.setMerchantPhoneNumber("123-123-1234");
        posSiteConfiguration.setSiteBrand("My Site Brand");
        posSiteConfiguration.setPosSystemType("I");
        posSiteConfiguration.setMethodOfOperation("A");
        posSiteConfiguration.setPosVendor("VERIFONE");
        posSiteConfiguration.setPosProductNameOrModel("RUBY 2 C1");
        posSiteConfiguration.setGlobalPaymentsPosTerminalType("88");
        posSiteConfiguration.setGlobalPaymentsPosSoftwareVersion("1.1.1.1");
        posSiteConfiguration.setGlobalPaymentsTerminalSpecVersion("0212");
        posSiteConfiguration.setPosHardwareVersion("34");
        posSiteConfiguration.setPosSoftwareVersion("21205710");
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
        posSiteConfiguration.setGlobalPaymentsPrimaryDialNumberOrIpPort("123-123-1234");
        posSiteConfiguration.setGlobalPaymentsSecondaryDialNumberOrIpPort("123-123-1234");
        posSiteConfiguration.setDispenserInterfaceVendor("Interface v1");
        posSiteConfiguration.setDispenserInterfaceProductNameOrModel("dispenser m1");
        posSiteConfiguration.setDispenserInterfaceSoftwareVersion("0001");
        posSiteConfiguration.setDispenserVendor("Vendor 1");
        posSiteConfiguration.setDispenserProductNameOrModel("Model 1");
        posSiteConfiguration.setDispenserSoftwareVersion("0001");
        posSiteConfiguration.setDispenserQuantity("02");
        posSiteConfiguration.setNumberOfScannersPeripherals("01");
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

        // with merchant type
        config.setMerchantType("5542");
        ServicesContainer.configureService(config, "ICR");
    }

    @Test
    public void test_POS_Site_Config() throws ApiException {

        Transaction response = NetworkService.sendSiteConfiguration()
                .withCurrency("USD")
                .withPOSSiteConfigData(posSiteConfiguration)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("600", response.getResponseCode());
    }
}


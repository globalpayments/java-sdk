package com.global.api.tests.network.nts;

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
import com.global.api.network.entities.nts.NtsRequestToBalanceData;
import com.global.api.network.entities.nts.PriorMessageInfo;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.NtsTestCards;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NtsRequestToBalanceTest {
    // gateway config
    NetworkGatewayConfig config;
    private NtsRequestMessageHeader ntsRequestMessageHeader;
    private PriorMessageInformation priorMessageInformation;
    private BatchProvider batchProvider;
    private NtsTag16 tag;
    private NtsProductData productData;
    private CreditTrackData track;

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

        priorMessageInformation =new PriorMessageInformation();
        priorMessageInformation.setResponseTime("999");
        priorMessageInformation.setConnectTime("999");
        priorMessageInformation.setMessageReasonCode("08");
        ntsRequestMessageHeader.setPriorMessageInformation(priorMessageInformation);

        batchProvider = BatchProvider.getInstance();

        tag = new NtsTag16();
        tag.setPumpNumber(1);
        tag.setWorkstationId(1);
        tag.setServiceCode(ServiceCode.Self);
        tag.setSecurityData(SecurityData.NoAVSAndNoCVN);

        track = NtsTestCards.MasterCardTrack2(EntryMethod.Swipe);

        // data code values
        // acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactlessMsd_KeyEntry);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.None);
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactlessMsd_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.Attended);

        //  acceptorConfig.setCardDataOutputCapability(CardDataOutputCapability.Unknown);
        // acceptorConfig.setPinCaptureCapability(PinCaptureCapability.Unknown);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(false);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setCapableAmexRemainingBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setCapableVoid(true);
        acceptorConfig.setSupportsEmvPin(true);
        acceptorConfig.setMobileDevice(true);

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
    private NtsProductData getProductDataForNonFleetBankCards(IPaymentMethod method) throws ApiException {
        productData = new NtsProductData(ServiceLevel.FullServe, method);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addFuel(NtsProductCode.Diesel2, UnitOfMeasure.Gallons,20.24, 1.259);
        productData.addNonFuel(NtsProductCode.Batteries,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.CarWash,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.Dairy,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.Candy,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.Milk,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.OilChange,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal("32.33"),new BigDecimal(0));
        return productData;
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
                        ntsRequestMessageHeader, 1, 1
                        , new BigDecimal(10.11), new BigDecimal(11.11), data)
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

    @Test
    public void test_BatchCloseIssue_10161() throws ApiException {

        creditSale(10.11);
        creditSale(20.22);

        NtsRequestMessageHeader ntsRequestMessageHeader = new NtsRequestMessageHeader();
        ntsRequestMessageHeader.setTerminalDestinationTag("478");
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.RequestToBalacnce);
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.NotPromptedPin);

        priorMessageInformation =new PriorMessageInformation();
        priorMessageInformation.setResponseTime("999");
        priorMessageInformation.setConnectTime("999");
        priorMessageInformation.setMessageReasonCode("01");
        ntsRequestMessageHeader.setPriorMessageInformation(priorMessageInformation);

        NtsRequestToBalanceData data = new NtsRequestToBalanceData(batchProvider.getSequenceNumber(), new BigDecimal(1), "Version");
        Transaction batchClose = BatchService.closeBatch(BatchCloseType.EndOfShift,
                        ntsRequestMessageHeader, batchProvider.getBatchNumber(), 2
                        , new BigDecimal(30.33), BigDecimal.ZERO, data)
                .execute();
        assertNotNull(batchClose);

        //check batch summary
        assertNotNull(batchClose.getBatchSummary());

        // check response
        assertEquals("00", batchClose.getResponseCode());

    }

  // Credit Sale
    private Transaction creditSale(double amount) throws ApiException {
        NtsRequestMessageHeader ntsRequestMessageHeader = new NtsRequestMessageHeader();
        ntsRequestMessageHeader.setTerminalDestinationTag("478");
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.NotPromptedPin);

        priorMessageInformation =new PriorMessageInformation();
        priorMessageInformation.setResponseTime("999");
        priorMessageInformation.setConnectTime("999");
        priorMessageInformation.setMessageReasonCode("01");
        ntsRequestMessageHeader.setPriorMessageInformation(priorMessageInformation);


        productData = getProductDataForNonFleetBankCards(track);

        track = NtsTestCards.MasterCardTrack2(EntryMethod.Swipe);

        Transaction response = track.charge(new BigDecimal(amount))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        return response;
    }
}

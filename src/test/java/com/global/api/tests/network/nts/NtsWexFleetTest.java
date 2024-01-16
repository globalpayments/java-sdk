package com.global.api.tests.network.nts;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.entities.FleetData;
import com.global.api.network.entities.NtsProductData;
import com.global.api.network.entities.NtsTag16;
import com.global.api.network.entities.nts.PriorMessageInfo;
import com.global.api.network.enums.*;
import com.global.api.network.enums.nts.AvailableProductsCapability;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.NtsTestCards;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import java.math.BigDecimal;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NtsWexFleetTest {
    private CreditCardData card;
    private CreditTrackData track;
    private NtsRequestMessageHeader ntsRequestMessageHeader; //Main Request header class
    private NtsTag16 tag;
    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;
    String emvTagData = "4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000";
    private NtsProductData productData;
    private FleetData fleetData;
    private PriorMessageInformation priorMessageInformation;

    public NtsWexFleetTest() throws ConfigurationException {

        acceptorConfig = new AcceptorConfig();
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.Attended); //ICR


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

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setCapableAmexRemainingBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setCapableVoid(true);
        acceptorConfig.setSupportsEmvPin(true);
        acceptorConfig.setMobileDevice(true);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        ntsRequestMessageHeader = new NtsRequestMessageHeader();

        ntsRequestMessageHeader.setTerminalDestinationTag("510");
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.NotPromptedPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        priorMessageInformation =new PriorMessageInformation();
        priorMessageInformation.setResponseTime("1");
        priorMessageInformation.setConnectTime("999");
        priorMessageInformation.setMessageReasonCode("01");
        ntsRequestMessageHeader.setPriorMessageInformation(priorMessageInformation);

        fleetData=new FleetData();
        fleetData.setPurchaseDeviceSequenceNumber("12345");
        fleetData.setServicePrompt("06");
        fleetData.setVehicleNumber("123456");
        fleetData.setUserId("123456");
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("123456");
        fleetData.setDriversLicenseNumber("123456");
        fleetData.setEnteredData("123456");
        fleetData.setJobNumber("123456");
        fleetData.setDepartment("123456");
        fleetData.setOtherPromptCode("123456");


        tag = new NtsTag16();
        tag.setPumpNumber(1);
        tag.setWorkstationId(1);
        tag.setServiceCode(ServiceCode.Self);
        tag.setSecurityData(SecurityData.NoAVSAndNoCVN);


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

        // with merchant type
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        // Wex Card
        card = new CreditCardData();
        card.setNumber("6900460430001234566");
        card.setExpMonth(12);
        card.setExpYear(2025);

        track = new CreditTrackData();
        track.setValue(";6900460420006149231=21121012202100000?");
        track.setEntryMethod(EntryMethod.ContactlessEMV);


    }

    private NtsProductData getProductDataForNonFleetBankCards(IPaymentMethod method) throws ApiException {
        productData = new NtsProductData(ServiceLevel.FullServe, method);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addNonFuel(NtsProductCode.Batteries,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.CarWash,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.Dairy,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.Candy,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.Milk,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.OilChange,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.IceCream,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.setSalesTax(new BigDecimal(8));
        productData.add(new BigDecimal("32.33"),new BigDecimal(0));
        return productData;
    }

    @Test
    public void test_WexFleet_With_DataCollect_CreditAdjustment_03() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=25121012202100000?");
        track.setEntryMethod(EntryMethod.Swipe);


        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(dataCollectResponse);

        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_WexFleet_With_DataCollect_And_Sale_NonEmv_manual() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        card = new CreditCardData();
        card.setNumber("6900460430001234566");
        card.setExpMonth(12);
        card.setExpYear(2025);
        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);


        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(card))
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.


        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .withNtsProductData(getProductDataForNonFleetBankCards(card))
                .withFleetData(fleetData)
                .execute();
        assertNotNull(dataCollectResponse);

        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_WexFleet_With_DataCollect_And_Sale_NonEmv() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=21121012202100000?");
        track.setEntryMethod(EntryMethod.Swipe);

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .execute();
        assertNotNull(dataCollectResponse);

        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test //not working
    public void test_WexFleet_With_DataCollect_And_Sale_Emv() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=25121012202100000?");
        track.setEntryMethod(EntryMethod.ContactEMV);

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .withTagData(emvTagData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.


        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .withTagData(emvTagData)
                .execute();
        assertNotNull(dataCollectResponse);

        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_WexFleet_With_Sale_NonEmv() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=21121012202100000?");
        track.setEntryMethod(EntryMethod.Swipe);

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    @Test // Working
    public void test_WexFleet_AuthReversal_NonEmv() throws ApiException {

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=25121012202100000?");
        track.setEntryMethod(EntryMethod.Swipe);


        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons,new BigDecimal(0),new BigDecimal(0),new BigDecimal(10));
        productData.setSalesTax(new BigDecimal(9));

        fleetData.setServicePrompt("03");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .withCvn("123")
                .execute();
        assertNotNull(response);

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test // Working
    public void test_WexFleet_AuthReversal_Emv() throws ApiException {

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=25121012202100000?");
        track.setEntryMethod(EntryMethod.ContactEMV);

        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons,new BigDecimal(0),new BigDecimal(0),new BigDecimal(10));
        productData.setPurchaseType(PurchaseType.Fuel);
        productData.setSalesTax(new BigDecimal(9));

        fleetData.setServicePrompt("03");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .withTagData(emvTagData)
                .withCvn("123")
                .execute();
        assertNotNull(response);

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withTagData(emvTagData)
                .withFleetData(fleetData)
                .execute();
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test
    public void test_WexFleet_saleReversal_Emv() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        fleetData.setServicePrompt("06");

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=25121012202100000?");
        track.setEntryMethod(EntryMethod.ContactEMV);


        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .withTagData(emvTagData)
                .execute();
        assertNotNull(response);

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .execute();
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test // working
    public void test_WexFleet_With_Sale_NonEmv_Manual() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        card = new CreditCardData();
        card.setNumber("6900460430001234566");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");
        card.setCardPresent(true);
        card.setReaderPresent(true);
        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(card))
                .withFleetData(fleetData)
                //.withTagData(emvTagData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    @Test // working
    public void test_WexFleet_With_Sale_NonEmv_Track2() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=25121012203100000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1221");

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    @Test //working
    public void test_WexFleet_With_Sale_NonEmv_2Fuel_6Nonfuel() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=21121012203100000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1221");

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Cng, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addNonFuel(NtsProductCode.Batteries,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.CarWash,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.BeerOrAlc,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.AutoGlass,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.AutoAcces,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.BrakeSvc,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));

        fleetData.setServicePrompt("03");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    @Test //working
    public void test_WexFleet_With_Sale_NonEmv_2Fuel_3Nonfuel() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=21121012203100000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1221");

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Cng, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addNonFuel(NtsProductCode.Batteries,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.CarWash,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.BeerOrAlc,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));

        fleetData.setServicePrompt("03");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    @Test //working
    public void test_WexFleet_With_Sale_NonEmv_2Fuel_7Nonfuel() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=21121012203100000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1221");

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Cng, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addNonFuel(NtsProductCode.Batteries,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.CarWash,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.BeerOrAlc,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.AutoGlass,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.AutoAcces,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.BrakeSvc,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.EngineSvc,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));

        fleetData.setServicePrompt("03");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    @Test // working
    public void test_WexFleet_With_Sale_NonEmv_1Fuel_6Nonfuel() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=21121012203100000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1221");

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Cng, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addNonFuel(NtsProductCode.EngineSvc,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.Batteries,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.CarWash,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.BeerOrAlc,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.AutoGlass,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.AutoAcces,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.BrakeSvc,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));

        fleetData.setServicePrompt("03");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    @Test // working
    public void test_WexFleet_With_Sale_NonEmv_1Fuel_7Nonfuel() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=21121012203100000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1221");

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Cng, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addNonFuel(NtsProductCode.EngineSvc,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.Batteries,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.CarWash,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.BeerOrAlc,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.AutoGlass,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.AutoAcces,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.BrakeSvc,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));

        fleetData.setServicePrompt("03");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    @Test // working
    public void test_WexFleet_With_Sale_NonEmv_1Fuel_MoreThan7Nonfuel() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=21121012203100000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1221");

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Cng, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addNonFuel(NtsProductCode.EngineSvc,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.Batteries,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.CarWash,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.BeerOrAlc,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.AutoGlass,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.AutoAcces,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.BrakeSvc,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.CigTobaco,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));

        fleetData.setServicePrompt("03");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    @Test // working
    public void test_WexFleet_With_Sale_NonEmv_2Fuel_MoreThan7Nonfuel() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=21121012203100000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1221");

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Cng, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addNonFuel(NtsProductCode.EngineSvc,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.Batteries,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.CarWash,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.BeerOrAlc,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.AutoGlass,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.AutoAcces,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.BrakeSvc,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.CigTobaco,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));

        fleetData.setServicePrompt("03");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }
    @Test //working
    public void test_WexFleet_With_Sale_NonEmv_2Fuel_6Nonfuel_product_rollup() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        card = new CreditCardData();
        card.setNumber("6900460430001234566");
        card.setExpMonth(12);
        card.setExpYear(2026);
        card.setCvn("123");
        card.setCardPresent(true);
        card.setReaderPresent(true);
        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);


        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Cng, UnitOfMeasure.Gallons,1, 10);
        productData.addFuel(NtsProductCode.Cng, UnitOfMeasure.Gallons,1, 10);
        productData.addNonFuel(NtsProductCode.Batteries,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.CarWash,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.BeerOrAlc,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.AutoGlass,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.AutoAcces,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.BrakeSvc,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.Batteries,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.Milk,UnitOfMeasure.NoFuelPurchased,1,10.74);

        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));

        fleetData.setServicePrompt("03");

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    @Test
    public void test_WexFleet_With_Auth_And_DataCollect_NonEmv_manual_4fuelAnd1NonFuel_10241() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=25121012202100000?");
        track.setEntryMethod(EntryMethod.Swipe);

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Regular, UnitOfMeasure.Gallons,1, 2.399);
        productData.addFuel(NtsProductCode.Plus,UnitOfMeasure.Gallons,1,2.799);
        productData.addFuel(NtsProductCode.Premium,UnitOfMeasure.Gallons,1,3.299);
        productData.addFuel(NtsProductCode.Diesel2,UnitOfMeasure.Gallons,1,2.599);
        productData.addNonFuel(NtsProductCode.CarWash,UnitOfMeasure.NoFuelPurchased,1,15.34);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.setSalesTax(new BigDecimal(8));
        productData.add(new BigDecimal("32.33"),new BigDecimal(0));


        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCvn("123")
                .execute();

        assertNotNull(response);

        // Data-Collect request preparation.

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(dataCollectResponse);

        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_WexFleet_CreditAdjustment_Batch_SeqNo_Issue_10248() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        track = new CreditTrackData();
        track.setValue(";6900460430001234566=25121012202100000?");
        track.setEntryMethod(EntryMethod.Swipe);
        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);
        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    // Negative - Incorrect user data length scenario
    //purchase quantity for product 6&7 should be 1 as per spec
    @Test
    public void test_WexFleet_issue_10233_product_roll_up() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=21121012203100000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1221");

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addNonFuel(NtsProductCode.FoodSvc,UnitOfMeasure.NoFuelPurchased,2,2);
        productData.addNonFuel(NtsProductCode.OilChange,UnitOfMeasure.NoFuelPurchased,1,3);
        productData.addNonFuel(NtsProductCode.GenTobaco,UnitOfMeasure.NoFuelPurchased,2,4);
        productData.addNonFuel(NtsProductCode.Alcohol,UnitOfMeasure.NoFuelPurchased,1,5);
        productData.addNonFuel(NtsProductCode.SalesTax,UnitOfMeasure.NoFuelPurchased,2,2);
        productData.addNonFuel(NtsProductCode.PkgBevNa,UnitOfMeasure.NoFuelPurchased,12,2);
        productData.addNonFuel(NtsProductCode.Tires,UnitOfMeasure.NoFuelPurchased,12,1);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));

        fleetData.setServicePrompt("03");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }
    // Positive -correct user data length scenario
    @Test
    public void test_WexFleet_issue_10233_product_roll_up_positive_scenario() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=21121012203100000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1221");

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addNonFuel(NtsProductCode.FoodSvc,UnitOfMeasure.NoFuelPurchased,2,2);
        productData.addNonFuel(NtsProductCode.OilChange,UnitOfMeasure.NoFuelPurchased,1,3);
        productData.addNonFuel(NtsProductCode.GenTobaco,UnitOfMeasure.NoFuelPurchased,2,4);
        productData.addNonFuel(NtsProductCode.Alcohol,UnitOfMeasure.NoFuelPurchased,1,5);
        productData.addNonFuel(NtsProductCode.SalesTax,UnitOfMeasure.NoFuelPurchased,2,2);
        productData.addNonFuel(NtsProductCode.PkgBevNa,UnitOfMeasure.NoFuelPurchased,1,2);
        productData.addNonFuel(NtsProductCode.Tires,UnitOfMeasure.NoFuelPurchased,1,1);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));

        fleetData.setServicePrompt("03");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }
    //positive scenario
    @Test
    public void test_WexFleet_issue_10233_product_roll_up_measure_value_fixed() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=21121012203100000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1221");

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addNonFuel(NtsProductCode.FoodSvc,UnitOfMeasure.NoFuelPurchased,2,2);
        productData.addNonFuel(NtsProductCode.OilChange,UnitOfMeasure.NoFuelPurchased,1,3);
        productData.addNonFuel(NtsProductCode.GenTobaco,UnitOfMeasure.NoFuelPurchased,2,4);
        productData.addNonFuel(NtsProductCode.Alcohol,UnitOfMeasure.NoFuelPurchased,1,5);
        productData.addNonFuel(NtsProductCode.SalesTax,UnitOfMeasure.NoFuelPurchased,2,2);
        productData.addNonFuel(NtsProductCode.PkgBevNa,UnitOfMeasure.NoFuelPurchased,1,2);
        productData.addNonFuel(NtsProductCode.Tires,UnitOfMeasure.Units,1,1);
        productData.addNonFuel(NtsProductCode.PlusE10,UnitOfMeasure.Units,1,1);
        productData.addNonFuel(NtsProductCode.Premium,UnitOfMeasure.Units,1,1);

        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));

        fleetData.setServicePrompt("03");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    @Test
    public void test_WexFleet_issue_10233_product_roll_up_with_different_measure() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=21121012203100000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1221");

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addNonFuel(NtsProductCode.FoodSvc,UnitOfMeasure.NoFuelPurchased,2,2);
        productData.addNonFuel(NtsProductCode.OilChange,UnitOfMeasure.NoFuelPurchased,1,3);
        productData.addNonFuel(NtsProductCode.GenTobaco,UnitOfMeasure.NoFuelPurchased,2,4);
        productData.addNonFuel(NtsProductCode.Alcohol,UnitOfMeasure.NoFuelPurchased,1,5);
        productData.addNonFuel(NtsProductCode.SalesTax,UnitOfMeasure.NoFuelPurchased,2,2);
        productData.addNonFuel(NtsProductCode.PkgBevNa,UnitOfMeasure.NoFuelPurchased,1,2);
        productData.addNonFuel(NtsProductCode.Tires,UnitOfMeasure.NoFuelPurchased,1,1);
        productData.addNonFuel(NtsProductCode.PlusE10,UnitOfMeasure.Pounds,1,1);
        productData.addNonFuel(NtsProductCode.Premium,UnitOfMeasure.Quarts,1,1);

        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));

        fleetData.setServicePrompt("03");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    // Negative - Incorrect user data length scenario
    //purchase quantity for product 6&7 should be 1 as per spec
    @Test
    public void test_WexFleet_issue_10233_product_roll_up_rearranging_order() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=25121012203100000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1225");

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addNonFuel(NtsProductCode.FoodSvc,UnitOfMeasure.NoFuelPurchased,245,2);
        productData.addNonFuel(NtsProductCode.OilChange,UnitOfMeasure.NoFuelPurchased,1,3);
        productData.addNonFuel(NtsProductCode.GenTobaco,UnitOfMeasure.NoFuelPurchased,2,4);
        productData.addNonFuel(NtsProductCode.Alcohol,UnitOfMeasure.NoFuelPurchased,123,5);
        productData.addNonFuel(NtsProductCode.SalesTax,UnitOfMeasure.NoFuelPurchased,235,2);
        productData.addNonFuel(NtsProductCode.PkgBevNa,UnitOfMeasure.NoFuelPurchased,12,2);
        productData.addNonFuel(NtsProductCode.Tires,UnitOfMeasure.NoFuelPurchased,12,1);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));

        fleetData.setServicePrompt("03");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    //Positive Scenarios
    @Test
    public void test_WexFleet_issue_10233_product_roll_up_rearranging_order_02() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=21121012203100000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1221");

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addNonFuel(NtsProductCode.FoodSvc,UnitOfMeasure.NoFuelPurchased,2,2);
        productData.addNonFuel(NtsProductCode.OilChange,UnitOfMeasure.NoFuelPurchased,1,3);
        productData.addNonFuel(NtsProductCode.GenTobaco,UnitOfMeasure.NoFuelPurchased,2,4);
        productData.addNonFuel(NtsProductCode.Alcohol,UnitOfMeasure.NoFuelPurchased,1,5);
        productData.addNonFuel(NtsProductCode.SalesTax,UnitOfMeasure.NoFuelPurchased,2,2);
        productData.addNonFuel(NtsProductCode.PkgBevNa,UnitOfMeasure.NoFuelPurchased,213,2);
        productData.addNonFuel(NtsProductCode.Tires,UnitOfMeasure.NoFuelPurchased,12,1);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));

        fleetData.setServicePrompt("03");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    //purchase quantity length for product 6 & 7 more than 1
    @Test
    public void test_WexFleet_issue_10233_product_roll_up_rearranging_order_04() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=25121012203100000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1221");

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addNonFuel(NtsProductCode.FoodSvc,UnitOfMeasure.NoFuelPurchased,2,2);
        productData.addNonFuel(NtsProductCode.OilChange,UnitOfMeasure.NoFuelPurchased,1,3);
        productData.addNonFuel(NtsProductCode.GenTobaco,UnitOfMeasure.NoFuelPurchased,2,4);
        productData.addNonFuel(NtsProductCode.Alcohol,UnitOfMeasure.NoFuelPurchased,1,5);
        productData.addNonFuel(NtsProductCode.SalesTax,UnitOfMeasure.NoFuelPurchased,2,2);
        productData.addNonFuel(NtsProductCode.PkgBevNa,UnitOfMeasure.NoFuelPurchased,12,2);
        productData.addNonFuel(NtsProductCode.Tires,UnitOfMeasure.NoFuelPurchased,12,1);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));

        fleetData.setServicePrompt("03");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    //purchase quantity length for product 6 more than 1 & product 7 length is 1
    @Test
    public void test_WexFleet_issue_10233_product_roll_up_rearranging_order_05() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=25121012203100000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1221");

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addNonFuel(NtsProductCode.FoodSvc,UnitOfMeasure.NoFuelPurchased,2,2);
        productData.addNonFuel(NtsProductCode.OilChange,UnitOfMeasure.NoFuelPurchased,1,3);
        productData.addNonFuel(NtsProductCode.GenTobaco,UnitOfMeasure.NoFuelPurchased,2,4);
        productData.addNonFuel(NtsProductCode.Alcohol,UnitOfMeasure.NoFuelPurchased,1,5);
        productData.addNonFuel(NtsProductCode.SalesTax,UnitOfMeasure.NoFuelPurchased,2,2);
        productData.addNonFuel(NtsProductCode.PkgBevNa,UnitOfMeasure.NoFuelPurchased,12,2);
        productData.addNonFuel(NtsProductCode.Tires,UnitOfMeasure.NoFuelPurchased,1,1);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));

        fleetData.setServicePrompt("03");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    //purchase quantity length for product 6 is 1 & product 7 length more than 1
    @Test
    public void test_WexFleet_issue_10233_product_roll_up_rearranging_order_06() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=25121012203100000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1221");

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addNonFuel(NtsProductCode.FoodSvc,UnitOfMeasure.NoFuelPurchased,2,2);
        productData.addNonFuel(NtsProductCode.OilChange,UnitOfMeasure.NoFuelPurchased,1,3);
        productData.addNonFuel(NtsProductCode.GenTobaco,UnitOfMeasure.NoFuelPurchased,2,4);
        productData.addNonFuel(NtsProductCode.Alcohol,UnitOfMeasure.NoFuelPurchased,1,5);
        productData.addNonFuel(NtsProductCode.SalesTax,UnitOfMeasure.NoFuelPurchased,2,2);
        productData.addNonFuel(NtsProductCode.PkgBevNa,UnitOfMeasure.NoFuelPurchased,1,2);
        productData.addNonFuel(NtsProductCode.Tires,UnitOfMeasure.NoFuelPurchased,12,1);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));

        fleetData.setServicePrompt("03");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }



    @Test
    public void test_WexFleet_issue_10233_product_roll_up_rearranging_order_03() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=25121012203100000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1221");

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addNonFuel(NtsProductCode.FoodSvc,UnitOfMeasure.NoFuelPurchased,2,2);
        productData.addNonFuel(NtsProductCode.OilChange,UnitOfMeasure.NoFuelPurchased,133,3);
        productData.addNonFuel(NtsProductCode.GenTobaco,UnitOfMeasure.NoFuelPurchased,2,4);
        productData.addNonFuel(NtsProductCode.Alcohol,UnitOfMeasure.NoFuelPurchased,1,5);
        productData.addNonFuel(NtsProductCode.SalesTax,UnitOfMeasure.NoFuelPurchased,211,2);
        productData.addNonFuel(NtsProductCode.PkgBevNa,UnitOfMeasure.NoFuelPurchased,121,2);
        productData.addNonFuel(NtsProductCode.Tires,UnitOfMeasure.Units,133,1);
        productData.addNonFuel(NtsProductCode.PlusE10,UnitOfMeasure.Units,11,1);
        productData.addNonFuel(NtsProductCode.Premium,UnitOfMeasure.Units,11,1);

        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));

        fleetData.setServicePrompt("03");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }
    @Test // Working
    public void test_WexFleet_AuthCapture_Emv_issue_10267() throws ApiException {

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=25121012202100000?");
        track.setEntryMethod(EntryMethod.ContactEMV);

        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons,new BigDecimal(0),new BigDecimal(0),new BigDecimal(10));
        productData.setPurchaseType(PurchaseType.Fuel);
        productData.setSalesTax(new BigDecimal(9));

        fleetData.setServicePrompt("03");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withTagData("9F020600000000200082023900950500800080005F2A0208408407A00000076810109A032311089C01009F03060000000000009F090200969F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F1A0208409F1B04000000009F1E084B424944303037529F260819BECC51087985B99F2701809F330360D8C89F34031F00029F3501259F360200019F37042AC7EC229F40056000A050019F410400000001")
                .withNtsProductData(productData)
                .withCvn("123")
                .execute();
        assertNotNull(response);

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction capture = response.capture(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withTagData("9F020600000000200082023900950500800080005F2A0208408407A00000076810109A032311089C01009F03060000000000009F090200969F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F1A0208409F1B04000000009F1E084B424944303037529F260819BECC51087985B99F2701809F330360D8C89F34031F00029F3501259F360200019F37042AC7EC229F40056000A050019F410400000001")
                .withFleetData(fleetData)
                .execute();
        assertEquals("00", capture.getResponseCode());
    }


    @Test
    public void test_WexFleet_saleReversal_10269_Emv() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        fleetData.setServicePrompt("06");

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=25121012202100000?");
        track.setEntryMethod(EntryMethod.Swipe);


        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .execute();
        assertEquals("00", voidResponse.getResponseCode());
    }


    //Postive Scenario- 38 AuthResponse Code
    @Test
    public void test_WexFleet_saleReversal_nonEmv_10269_Authcode38()throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        fleetData.setServicePrompt("06");

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=25121012202100000?");
        track.setEntryMethod(EntryMethod.Swipe);


        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        Transaction transaction = Transaction.fromBuilder()
                .withPaymentMethod(track)
                .withAuthorizationCode("38")
                .withOriginalTransactionDate(response.getTransactionReference().getOriginalTransactionDate())
                .withTransactionTime(response.getTransactionReference().getOriginalTransactionTime())
                .withOriginalMessageCode("02")
                .withBatchNumber(response.getTransactionReference().getBatchNumber())
                .withSequenceNumber(response.getTransactionReference().getSequenceNumber())
                .build();


        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = transaction.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)

                .execute();
        assertEquals("00", voidResponse.getResponseCode());
    }

    //used only for code coverage scenario.
    @Test
    public void test_WexFleet_AuthTransactionSelfService_CodeCoverageScenario() throws ApiException {
        track = NtsTestCards.WexFleetTrack2(EntryMethod.ContactEMV);
        NtsProductData productData = new NtsProductData(ServiceLevel.SelfServe, track);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons, new BigDecimal(0), new BigDecimal(0), new BigDecimal(10));
        productData.setPurchaseType(PurchaseType.Fuel);
        productData.setSalesTax(new BigDecimal(9));

        fleetData.setServicePrompt("03");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withTagData(emvTagData)
                .withNtsProductData(productData)
                .withCvn("123")
                .execute();
        assertNotNull(response);
    }

    //used only for code coverage scenario.
    @Test
    public void test_WexFleet_AuthTransactionNoFuelPurchased_CodeCoverageScenario() throws ApiException {
        track = NtsTestCards.WexFleetTrack2(EntryMethod.ContactEMV);
        NtsProductData productData = new NtsProductData(ServiceLevel.NoFuelPurchased, track);
        productData.addFuel(NtsProductCode.Repairs, UnitOfMeasure.NoFuelPurchased, new BigDecimal(0), new BigDecimal(0), new BigDecimal(10));
        productData.setPurchaseType(PurchaseType.NonFuel);
        productData.setSalesTax(new BigDecimal(9));

        fleetData.setServicePrompt("03");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withTagData(emvTagData)
                .withNtsProductData(productData)
                .withCvn("123")
                .execute();
        assertNotNull(response);
    }

    // code coverage scenario only
    @Test
    public void test_WexFleet_AuthFallbackModifier_codeCoverage() throws ApiException {
        track = NtsTestCards.WexFleetTrack2(EntryMethod.Swipe);
        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons, new BigDecimal(2), new BigDecimal(10), new BigDecimal(20));
        productData.setSalesTax(new BigDecimal(9));

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .withCvn("123")
                .withModifier(TransactionModifier.Fallback)
                .execute();
        assertNotNull(response);
    }

    @Test
    public void test_WexFleet_saleNonFuelProductCodeOnly_codeCoverage() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        track = NtsTestCards.WexFleetTrack2(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addNonFuel(NtsProductCode.FoodSvc,UnitOfMeasure.NoFuelPurchased,2,2);
        productData.addNonFuel(NtsProductCode.OilChange,UnitOfMeasure.NoFuelPurchased,133,3);
        productData.addNonFuel(NtsProductCode.GenTobaco,UnitOfMeasure.NoFuelPurchased,2,4);
        productData.addNonFuel(NtsProductCode.Alcohol,UnitOfMeasure.NoFuelPurchased,1,5);
        productData.addNonFuel(NtsProductCode.SalesTax,UnitOfMeasure.NoFuelPurchased,211,2);
        productData.addNonFuel(NtsProductCode.PkgBevNa,UnitOfMeasure.NoFuelPurchased,121,2);
        productData.addNonFuel(NtsProductCode.Tires,UnitOfMeasure.Units,133,1);
        productData.addNonFuel(NtsProductCode.PlusE10,UnitOfMeasure.Units,11,1);
        productData.addNonFuel(NtsProductCode.Premium,UnitOfMeasure.Units,11,1);
        productData.setPurchaseType(PurchaseType.NonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));
      
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
      
    }

    //Scenario 1 : product 6 quantity length is 13.
    @Test
    public void test_WexFleet_issue_10233_product_roll_up_product6_double_digit01() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=25121012203100000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1225");

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addNonFuel(NtsProductCode.FoodSvc,UnitOfMeasure.NoFuelPurchased,12,2);
        productData.addNonFuel(NtsProductCode.OilChange,UnitOfMeasure.NoFuelPurchased,11,3);
        productData.addNonFuel(NtsProductCode.GenTobaco,UnitOfMeasure.NoFuelPurchased,12,4);
        productData.addNonFuel(NtsProductCode.Alcohol,UnitOfMeasure.NoFuelPurchased,11,5);
        productData.addNonFuel(NtsProductCode.SalesTax,UnitOfMeasure.NoFuelPurchased,12,2);
        productData.addNonFuel(NtsProductCode.PkgBevNa,UnitOfMeasure.NoFuelPurchased,13,2);
        productData.addNonFuel(NtsProductCode.Tires,UnitOfMeasure.NoFuelPurchased,1,1);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));

        fleetData.setServicePrompt("03");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    //Scenario 2 : product 6 quantity length is 13 and only have 6 products.
    @Test
    public void test_WexFleet_issue_10233_product_roll_up_product6_double_digit02() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=25121012203100000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1225");

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addNonFuel(NtsProductCode.FoodSvc,UnitOfMeasure.NoFuelPurchased,12,2);
        productData.addNonFuel(NtsProductCode.OilChange,UnitOfMeasure.NoFuelPurchased,11,3);
        productData.addNonFuel(NtsProductCode.GenTobaco,UnitOfMeasure.NoFuelPurchased,12,4);
        productData.addNonFuel(NtsProductCode.Alcohol,UnitOfMeasure.NoFuelPurchased,11,5);
        productData.addNonFuel(NtsProductCode.SalesTax,UnitOfMeasure.NoFuelPurchased,12,2);
        productData.addNonFuel(NtsProductCode.PkgBevNa,UnitOfMeasure.NoFuelPurchased,13,2);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));

        fleetData.setServicePrompt("03");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    //Scenario 3 : product 6 quantity length is more than 18 and only has 6 products.
    @Test
    public void test_WexFleet_issue_10233_product_roll_up_product6_double_digit03() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=25121012203100000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1225");

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addNonFuel(NtsProductCode.FoodSvc,UnitOfMeasure.NoFuelPurchased,12,2);
        productData.addNonFuel(NtsProductCode.OilChange,UnitOfMeasure.NoFuelPurchased,11,3);
        productData.addNonFuel(NtsProductCode.GenTobaco,UnitOfMeasure.NoFuelPurchased,12,4);
        productData.addNonFuel(NtsProductCode.Alcohol,UnitOfMeasure.NoFuelPurchased,11,5);
        productData.addNonFuel(NtsProductCode.SalesTax,UnitOfMeasure.NoFuelPurchased,12,2);
        productData.addNonFuel(NtsProductCode.PkgBevNa,UnitOfMeasure.NoFuelPurchased,21,2);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));

        fleetData.setServicePrompt("03");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    //Scenario 4 : product 6 quantity length is 1 and product 7 quantity length is 12
    @Test
    public void test_WexFleet_issue_10233_product_roll_up_product6_double_digit04() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=25121012203100000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1225");

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addNonFuel(NtsProductCode.FoodSvc,UnitOfMeasure.NoFuelPurchased,12,2);
        productData.addNonFuel(NtsProductCode.OilChange,UnitOfMeasure.NoFuelPurchased,11,3);
        productData.addNonFuel(NtsProductCode.GenTobaco,UnitOfMeasure.NoFuelPurchased,12,4);
        productData.addNonFuel(NtsProductCode.Alcohol,UnitOfMeasure.NoFuelPurchased,11,5);
        productData.addNonFuel(NtsProductCode.SalesTax,UnitOfMeasure.NoFuelPurchased,12,2);
        productData.addNonFuel(NtsProductCode.PkgBevNa,UnitOfMeasure.NoFuelPurchased,1,2);
        productData.addNonFuel(NtsProductCode.Tires,UnitOfMeasure.NoFuelPurchased,12,1);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));

        fleetData.setServicePrompt("03");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();
        assertNotNull(response);
    }

    @Test
    public void test_WexFleet_saleReversal_codeCoverage() throws ApiException {
        track = NtsTestCards.WexFleetTrack2(EntryMethod.Swipe);

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);
        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .execute();
        assertEquals("00", voidResponse.getResponseCode());
    }


    //Scenario 5 : product 6 quantity length is 1 and product 7 quantity length is 1.
    @Test
    public void test_WexFleet_issue_10233_product_roll_up_product6_double_digit05() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=25121012203100000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1225");

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addNonFuel(NtsProductCode.FoodSvc,UnitOfMeasure.NoFuelPurchased,12,2);
        productData.addNonFuel(NtsProductCode.OilChange,UnitOfMeasure.NoFuelPurchased,11,3);
        productData.addNonFuel(NtsProductCode.GenTobaco,UnitOfMeasure.NoFuelPurchased,12,4);
        productData.addNonFuel(NtsProductCode.Alcohol,UnitOfMeasure.NoFuelPurchased,11,5);
        productData.addNonFuel(NtsProductCode.SalesTax,UnitOfMeasure.NoFuelPurchased,12,2);
        productData.addNonFuel(NtsProductCode.PkgBevNa,UnitOfMeasure.NoFuelPurchased,1,2);
        productData.addNonFuel(NtsProductCode.Tires,UnitOfMeasure.NoFuelPurchased,1,1);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));

        fleetData.setServicePrompt("03");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

   @Test
    public void test_WexFleet_saleReversal_withTwoFuelCode() throws ApiException {
        track = NtsTestCards.WexFleetTrack2(EntryMethod.Swipe);
        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Diesel1,UnitOfMeasure.Liters,2,13);
        productData.addFuel(NtsProductCode.Cng,UnitOfMeasure.Kilograms,2,10);
        productData.addNonFuel(NtsProductCode.FoodSvc,UnitOfMeasure.NoFuelPurchased,2,2);
        productData.addNonFuel(NtsProductCode.OilChange,UnitOfMeasure.NoFuelPurchased,133,3);
        productData.addNonFuel(NtsProductCode.GenTobaco,UnitOfMeasure.NoFuelPurchased,2,4);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);
        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test
    public void test_WexFleet_saleReversal_withoutFuelCode() throws ApiException {
        track = NtsTestCards.WexFleetTrack2(EntryMethod.Swipe);
        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addNonFuel(NtsProductCode.FoodSvc,UnitOfMeasure.NoFuelPurchased,2,2);
        productData.addNonFuel(NtsProductCode.OilChange,UnitOfMeasure.NoFuelPurchased,133,3);
        productData.addNonFuel(NtsProductCode.GenTobaco,UnitOfMeasure.NoFuelPurchased,2,4);
        productData.addNonFuel(NtsProductCode.Alcohol,UnitOfMeasure.NoFuelPurchased,1,5);
        productData.addNonFuel(NtsProductCode.SalesTax,UnitOfMeasure.NoFuelPurchased,211,2);
        productData.addNonFuel(NtsProductCode.PkgBevNa,UnitOfMeasure.NoFuelPurchased,121,2);
        productData.addNonFuel(NtsProductCode.Tires,UnitOfMeasure.Units,133,1);
        productData.addNonFuel(NtsProductCode.PlusE10,UnitOfMeasure.Units,11,1);
        productData.addNonFuel(NtsProductCode.Premium,UnitOfMeasure.Units,11,1);
        productData.setPurchaseType(PurchaseType.NonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));

        FleetData fleetData=new FleetData();
        fleetData.setOdometerReading("123456");
        fleetData.setDepartment("123456");
        fleetData.setOtherPromptCode("123456");
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);
        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertEquals("00", voidResponse.getResponseCode());
    }

    //only 2 nonfuel product
    @Test
    public void test_WexFleet_issue_10233_product_roll_up_product6_double_digit01_neg01() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=25121012203100000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1225");

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addNonFuel(NtsProductCode.FoodSvc,UnitOfMeasure.NoFuelPurchased,12,2);
        productData.addNonFuel(NtsProductCode.OilChange,UnitOfMeasure.NoFuelPurchased,11,3);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));

        fleetData.setServicePrompt("03");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    // nonfuel product more than 7
    @Test
    public void test_WexFleet_issue_10233_product_roll_up_product6_double_digit01_neg02() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=25121012203100000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1225");

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addNonFuel(NtsProductCode.FoodSvc,UnitOfMeasure.NoFuelPurchased,12,2);
        productData.addNonFuel(NtsProductCode.OilChange,UnitOfMeasure.NoFuelPurchased,11,3);
        productData.addNonFuel(NtsProductCode.GenTobaco,UnitOfMeasure.NoFuelPurchased,12,4);
        productData.addNonFuel(NtsProductCode.Alcohol,UnitOfMeasure.NoFuelPurchased,11,5);
        productData.addNonFuel(NtsProductCode.SalesTax,UnitOfMeasure.NoFuelPurchased,12,2);
        productData.addNonFuel(NtsProductCode.PkgBevNa,UnitOfMeasure.NoFuelPurchased,10,1);
        productData.addNonFuel(NtsProductCode.Tires,UnitOfMeasure.NoFuelPurchased,10,1);
        productData.addNonFuel(NtsProductCode.CigTobaco,UnitOfMeasure.NoFuelPurchased,10,1);
        productData.addNonFuel(NtsProductCode.Candy,UnitOfMeasure.NoFuelPurchased,10,1);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33),new BigDecimal(5));
        productData.setSalesTax(new BigDecimal(8));

        fleetData.setServicePrompt("03");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    @Test // Working
    public void test_WexFleet_Auth_CreditAdjustment_Reversal_EMV_10270() throws ApiException {

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=25121012202100000?");
        track.setEntryMethod(EntryMethod.Swipe);


        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons,new BigDecimal(0),new BigDecimal(0),new BigDecimal(10));
        productData.setSalesTax(new BigDecimal(9));

        fleetData.setServicePrompt("03");

        Transaction transaction = Transaction.fromBuilder()
                .withPaymentMethod(track)
                .withDebitAuthorizer("00")
                .withApprovalCode("00")
                .withAuthorizationCode("00")
                .withOriginalTransactionDate("0112")
                .withTransactionTime("071423")
                .withOriginalMessageCode("03")
                .withBatchNumber(1)
                .withSequenceNumber(70)
                .build();

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = transaction.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertEquals("00", voidResponse.getResponseCode());
    }

}

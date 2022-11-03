package com.global.api.tests.network.nts;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.network.entities.FleetData;
import com.global.api.network.entities.NtsProductData;
import com.global.api.network.entities.NtsTag16;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NtsFleetCorTest {
    String emvTagData = "4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000";
    private CreditCardData card;
    private CreditTrackData track;
    private NtsRequestMessageHeader ntsRequestMessageHeader; //Main Request header class
    private NtsTag16 tag;
    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;
    private NtsProductData productData;
    private FleetData fleetData;

    public NtsFleetCorTest() throws ConfigurationException {

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
        acceptorConfig.setSupportsPartialApproval(false);
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
        ntsRequestMessageHeader.setPriorMessageResponseTime(1);
        ntsRequestMessageHeader.setPriorMessageConnectTime(999);
        ntsRequestMessageHeader.setPriorMessageCode("01");

        fleetData = new FleetData();
        fleetData.setOdometerReading("1234567");
        fleetData.setDriverId("123456789");
        fleetData.setServicePrompt("0");


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

        // FleetWide
        card = new CreditCardData();
        card.setNumber("70768512345200005");
        card.setExpMonth(12);
        card.setExpYear(2099);
    }

    /**---------------Test Cases for FleetCor------------******/
    /**
     * FleetCor Fuelman Authorization
     **/

    @Test
    public void test_Fuelman_authorization() throws ApiException {

        track = new CreditTrackData();
        track.setValue(";70764912345100040=4912?");
        track.setEntryMethod(EntryMethod.Swipe);

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5500");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    @Test //working
    public void test_FuelmanFleet_Sale_transaction_rollup() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        track = new CreditTrackData();
        track.setValue(";70764912345100040=4912?");
        track.setEntryMethod(EntryMethod.Swipe);

        fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5500");

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Regular, UnitOfMeasure.Gallons, 2, 15.20);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 20);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 1, 20);
        productData.addNonFuel(NtsProductCode.Milk, UnitOfMeasure.NoFuelPurchased, 1, 20);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 1, 20);
        productData.addNonFuel(NtsProductCode.AutoGlass, UnitOfMeasure.NoFuelPurchased, 1, 20);
        productData.addNonFuel(NtsProductCode.CigTobaco, UnitOfMeasure.NoFuelPurchased, 2, 20);
        productData.add(new BigDecimal(88), new BigDecimal(0));
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test //working
    public void test_FuelmanFleet_Sale_transaction_1Fuel3NonFuel() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        track = new CreditTrackData();
        track.setValue(";70764912345100040=4912?");
        track.setEntryMethod(EntryMethod.Swipe);

        fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5500");

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Cng, UnitOfMeasure.Gallons, 05.10, 15.20);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.BeerOrAlc, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.add(new BigDecimal(88), new BigDecimal(0));
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // 1Fuel and 4 Nonfuel
    @Test //working
    public void test_FuelmanFleet_Sale_1Fuel4NonFuel() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";70764912345100040=4912?");
        track.setEntryMethod(EntryMethod.Swipe);

        fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5500");
        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Cng, UnitOfMeasure.Gallons, 05.10, 15.20);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.Milk, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.add(new BigDecimal(88), new BigDecimal(0));
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test //working
    public void test_FuelmanFleet_Sale_allNonFuel() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        track = new CreditTrackData();
        track.setValue(";70764912345100040=4912?");
        track.setEntryMethod(EntryMethod.Swipe);

        fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5500");

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.Milk, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.add(new BigDecimal(88), new BigDecimal(0));
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test //working
    public void test_FuelmanFleet_With_DataCollect() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";70764912345100040=4912?");
        track.setEntryMethod(EntryMethod.Swipe);

        fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5500");

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Cng, UnitOfMeasure.Gallons, 05.10, 15.20);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.Milk, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.add(new BigDecimal(88), new BigDecimal(0));
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.


        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test // working
    public void test_Sale_FuelmanFleet_Credit_Adjustment() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";70764912345100040=4912?");
        track.setEntryMethod(EntryMethod.Swipe);

        fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5500");

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Cng, UnitOfMeasure.Gallons, 05.10, 15.20);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.Milk, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.add(new BigDecimal(88), new BigDecimal(0));
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.


        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    // Working
    @Test
    public void test_FuelmanFleet_AuthResponse() throws ApiException {

        track = new CreditTrackData();
        track.setValue(";70764912345100040=4912?");
        track.setEntryMethod(EntryMethod.Swipe);

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5500");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        System.out.println(response.getTransactionReference().getBankcardData().get(UserDataTag.ApprovedAmount));
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    @Test //working
    public void test_FuelmanFleet_SaleResponse() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";70764912345100040=4912?");
        track.setEntryMethod(EntryMethod.Swipe);

        fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5500");

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.Milk, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.add(new BigDecimal(88), new BigDecimal(0));
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCvn("123")
                .execute();

        System.out.println(response.getTransactionReference().getBankcardData().get(UserDataTag.ApprovedAmount));
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }


    // @Ignore
    @Test
    public void test_FuelmanFleet_authReversal() throws ApiException {

        track = new CreditTrackData();
        track.setValue(";70764912345100040=4912?");
        track.setEntryMethod(EntryMethod.Swipe);

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5500");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertEquals("00", voidResponse.getResponseCode());

    }

    //  @Ignore
    @Test
    public void test_FuelmanFleet_saleReversal() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";70764912345100040=4912?");
        track.setEntryMethod(EntryMethod.Swipe);

        fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5500");

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons, 05.10, 15.20);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.Milk, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.add(new BigDecimal(88), new BigDecimal(0));
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test //working
    public void test_FuelmanFleet_auth_001_Manual() throws ApiException {

        card = new CreditCardData();
        card.setNumber("70764912345100040");
        card.setExpMonth(12);
        card.setExpYear(2049);

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("55000");

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test //Working
    public void test_FuelmanFleet_Sale_transaction_manual() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        card = new CreditCardData();
        card.setNumber("70764912345100040");
        card.setExpMonth(12);
        card.setExpYear(2049);
        card.setCardPresent(true);
        card.setReaderPresent(true);

        fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5500");

        productData = new NtsProductData(ServiceLevel.FullServe, card);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons, 05.10, 15.20);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.Milk, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.add(new BigDecimal(88), new BigDecimal(0));
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    @Test//not working
    @Ignore
    public void test_FuelmanFleet_voiceAuthorization() throws ApiException {
        track = new CreditTrackData();
        track.setValue(";70764912345100040=4912?");
        track.setEntryMethod(EntryMethod.Swipe);

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("5560");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withModifier(TransactionModifier.ChipDecline) // Only for the offline decline transactions.
                .execute();

        assertNotNull(response);

        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.


        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    /**
     * FleetCor FleetWide Authorization
     **/

    @Test
    public void test_FleetWide_authorization() throws ApiException {

        track = new CreditTrackData();
        track.setValue(";70768512345200005=99120?");
        track.setEntryMethod(EntryMethod.Swipe);

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5500");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    //only Non fuel product Rollup
    @Test //working
    public void test_FleetWide_Sale_transaction_rollup() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";70768512345200005=99120?");
        track.setEntryMethod(EntryMethod.Swipe);

        fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5500");

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons, 05.10, 15.20);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.Milk, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.AutoGlass, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.CigTobaco, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.add(new BigDecimal(88), new BigDecimal(0));
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test //working
    public void test_FleetWide_Sale_transaction_1Fuel3NonFuel() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";70768512345200005=99120?");
        track.setEntryMethod(EntryMethod.Swipe);

        fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5500");

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Cng, UnitOfMeasure.Gallons, 05.10, 15.20);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.add(new BigDecimal(88), new BigDecimal(0));
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // 1Fuel and 4 Nonfuel
    @Test //working
    public void test_FleetWide_Sale_1Fuel4NonFuel() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        track = new CreditTrackData();
        track.setValue(";70768512345200005=99120?");
        track.setEntryMethod(EntryMethod.Swipe);

        fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5500");

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Cng, UnitOfMeasure.Gallons, 05.10, 15.20);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.Milk, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.add(new BigDecimal(88), new BigDecimal(0));
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test //working
    public void test_FleetWide_Sale_allNonFuel() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        track = new CreditTrackData();
        track.setValue(";70768512345200005=99120?");
        track.setEntryMethod(EntryMethod.Swipe);

        fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5500");

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.Milk, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.add(new BigDecimal(88), new BigDecimal(0));
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test //working
    public void test_FleetWide_With_DataCollect() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        track = new CreditTrackData();
        track.setValue(";70768512345200005=99120?");
        track.setEntryMethod(EntryMethod.Swipe);

        fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5500");

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Cng, UnitOfMeasure.Gallons, 05.10, 15.20);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.Milk, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.add(new BigDecimal(88), new BigDecimal(0));
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.


        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test // working
    public void test_Sale_FleetWide_Credit_Adjustment() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        track = new CreditTrackData();
        track.setValue(";70768512345200005=99120?");
        track.setEntryMethod(EntryMethod.Swipe);

        fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5500");

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons, 05.10, 15.20);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.Milk, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.add(new BigDecimal(88), new BigDecimal(0));
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    //Working
    @Test
    public void test_FleetWide_AuthResponse() throws ApiException {

        track = new CreditTrackData();
        track.setValue(";70768512345200005=99120?");
        track.setEntryMethod(EntryMethod.Swipe);

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5500");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    @Test //working
    public void test_FleetWide_SaleResponse() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        track = new CreditTrackData();
        track.setValue(";70768512345200005=99120?");
        track.setEntryMethod(EntryMethod.Swipe);

        fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5500");

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.Milk, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.add(new BigDecimal(88), new BigDecimal(0));
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCvn("123")
                .execute();

        System.out.println(response.getTransactionReference().getBankcardData().get(UserDataTag.ApprovedAmount));
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_FleetWide_authReversal() throws ApiException {

        track = new CreditTrackData();
        track.setValue(";70768512345200005=99120?");
        track.setEntryMethod(EntryMethod.Swipe);

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5500");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertEquals("00", voidResponse.getResponseCode());

    }

    @Test
    public void test_FleetWide_saleReversal() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        track = new CreditTrackData();
        track.setValue(";70768512345200005=99120?");
        track.setEntryMethod(EntryMethod.Swipe);

        fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5500");

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons, 05.10, 15.20);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.Milk, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.add(new BigDecimal(88), new BigDecimal(0));
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test //Working
    public void test_FleetWide_auth_001_Manual() throws ApiException, ApiException {

        card = new CreditCardData();
        card.setNumber("70768512345200005");
        card.setExpMonth(12);
        card.setExpYear(2099);

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5500");

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test //Working
    public void test_FleetWide_Sale_transaction_manual() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        card = new CreditCardData();
        card.setNumber("70768512345200005");
        card.setExpMonth(12);
        card.setExpYear(2099);

        fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5500");

        productData = new NtsProductData(ServiceLevel.FullServe, card);
        productData.addFuel(NtsProductCode.Cng, UnitOfMeasure.Gallons, 05.10, 15.20);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.Milk, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 10, 20);
        productData.add(new BigDecimal(88), new BigDecimal(0));
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test//not working
    @Ignore
    public void test_FleetWide_Voice_Authorization() throws ApiException {
        track = new CreditTrackData();
        track.setValue(";70768512345200005=99120?");
        track.setEntryMethod(EntryMethod.Swipe);


        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("5560");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withModifier(TransactionModifier.ChipDecline) // Only for the offline decline transactions.
                .execute();

        assertNotNull(response);
        assertEquals("12", response.getResponseCode());
    }
}

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
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NtsWexFleetIcrTest {
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

    public NtsWexFleetIcrTest() throws ConfigurationException {

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
        ServicesContainer.configureService(config,"ICR");

        // Wex Card
        card = new CreditCardData();
        card.setNumber("6900460430001234566");
        card.setExpMonth(12);
        card.setExpYear(2025);

        track = new CreditTrackData();
        track.setValue(";6900460420006149231=21121012202100000?");
        track.setEntryMethod(EntryMethod.ContactlessEMV);
        track.setExpiry("1225");

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
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"),new BigDecimal(0));
        return productData;
    }

    /**-----------------Wex Fleet-------------------------**/

    @Test //working
    public void test_WexFleet_auth_001_Manual() throws ApiException {
        card = new CreditCardData();
        card.setNumber("6900460420006149231");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("1234");
        card.setCardPresent(true);
        card.setReaderPresent(true);

        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe,card);
        productData.addFuel(NtsProductCode.Cng, UnitOfMeasure.Gallons,new BigDecimal(0),new BigDecimal(0),new BigDecimal(10));

        fleetData.setServicePrompt("03");

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute("ICR");

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test //working
    public void test_WexFleet_auth_002_Track2() throws  ApiException {

        track = new CreditTrackData();
        track.setValue(";6900460420006149231=21121012202100000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1225");


        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons,new BigDecimal(0),new BigDecimal(0),new BigDecimal(10));

        fleetData.setServicePrompt("03");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute("ICR");

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test// working
    public void test_WexFleet_auth_002_track2_EMV() throws ApiException {
        track = new CreditTrackData();
        track.setValue(";6900460420006149231=21121012202100000?");
        track.setEntryMethod(EntryMethod.ContactlessEMV);
        track.setExpiry("1225");


        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe,track);
        productData.addFuel(NtsProductCode.MiscFuel, UnitOfMeasure.Gallons,new BigDecimal(0),new BigDecimal(0),new BigDecimal(10));

        productData.setSalesTax(new BigDecimal(8));
        fleetData.setPurchaseDeviceSequenceNumber("12345");
        fleetData.setServicePrompt("01");

        emvTagData="4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F24032212315F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000";

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withTagData(emvTagData)
                .withCardSequenceNumber("101")
                .execute("ICR");

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_WexFleet_Sale_OutSide_Manual() throws  ApiException{
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        card = new CreditCardData();
        card.setNumber("6900460420006149231");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");
        card.setCardPresent(true);
        card.setReaderPresent(true);

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        FleetData fleetData=new FleetData();
        fleetData.setPurchaseDeviceSequenceNumber("12345");
        fleetData.setDriverId("123456");
        fleetData.setServicePrompt("01");

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(card))
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }
}

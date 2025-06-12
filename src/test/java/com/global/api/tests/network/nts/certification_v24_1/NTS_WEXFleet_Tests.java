package com.global.api.tests.network.nts.certification_v24_1;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.EcommerceInfo;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.network.entities.FleetData;
import com.global.api.network.entities.NtsProductData;
import com.global.api.network.entities.NtsTag16;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
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
public class NTS_WEXFleet_Tests {
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

    private static final String PRODUCT_DATA_REQUIRED = "Product Data is required for this transaction.";
    private static final String TAG_DATA_INVALID = "Please ensure that the Tag data is not empty or composed only of spaces.";

    public NTS_WEXFleet_Tests() throws ConfigurationException {

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
        config.setTerminalId("01");
        config.setUnitNumber("00012378911");
        config.setSoftwareVersion("01");
        config.setCompanyId("009");
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
        productData.addNonFuel(NtsProductCode.Dairy,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.setSalesTax(new BigDecimal(8));
        productData.add(new BigDecimal("32.33"),new BigDecimal(0));
        return productData;
    }

    @Test
    public void test43_auth_without_track_amount_expansion_e_commerce_Wex_DEVEXP1329() throws ApiException {
        acceptorConfig.setPosActionCode(true);
        acceptorConfig.setMobileDevice(false);

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withEcommerceInfo(new EcommerceInfo())
                .withNtsProductData(getProductDataForNonFleetBankCards(card))
                .withFleetData(fleetData)
                .withInvoiceNumber("1234567890123456789012345")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test44_sale_without_track_amount_expansion_e_commerce_Wex() throws ApiException {
        acceptorConfig.setPosActionCode(true);
        acceptorConfig.setMobileDevice(false);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.WexFleetTrack2(EntryMethod.ECommerce);

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
        productData.setDiscount(new BigDecimal(5));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withEcommerceInfo(new EcommerceInfo())
                .withOfflineAuthCode("")
                .withEcommerceData1("1234512345123451234512345123451234512345")
                .withEcommerceAuthIndicator("S")
                .withInvoiceNumber("1231212345123451234512345")
                .withNtsTag16(tag)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test67_sale_without_track_amount_expansion_e_commerce_Wex() throws ApiException {
        acceptorConfig.setPosActionCode(true);
        acceptorConfig.setMobileDevice(false);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.WexFleetTrack2(EntryMethod.ECommerce);

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
        productData.setDiscount(new BigDecimal(5));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withEcommerceInfo(new EcommerceInfo())
                .withOfflineAuthCode("")
                .withEcommerceData1("1234512345123451234512345123451234512345")
                .withEcommerceAuthIndicator("S")
                .withInvoiceNumber("1231212345123451234512345")
                .execute();

        assertNotNull(response);
        assertEquals("30", response.getResponseCode());
    }

}

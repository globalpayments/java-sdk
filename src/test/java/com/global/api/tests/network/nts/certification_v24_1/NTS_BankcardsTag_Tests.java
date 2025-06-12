package com.global.api.tests.network.nts.certification_v24_1;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.EcommerceInfo;
import com.global.api.entities.ThreeDSecure;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.gateways.events.IGatewayEvent;
import com.global.api.gateways.events.IGatewayEventHandler;
import com.global.api.network.entities.FleetData;
import com.global.api.network.entities.NtsProductData;
import com.global.api.network.entities.NtsTag16;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.enums.*;
import com.global.api.network.enums.nts.PurchaseRestrictionFlag;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.NtsTestCards;
import com.global.api.tests.testdata.TestCards;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NTS_BankcardsTag_Tests {
    private CreditCardData card;
    private CreditTrackData track;
    private NtsRequestMessageHeader header; //Main Request header class
    private NtsTag16 tag;
    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;
    private NtsProductData productData;
    private PriorMessageInformation priorMessageInformation;
    String emvTagData = "4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000";

    public NTS_BankcardsTag_Tests() throws ConfigurationException {

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
        acceptorConfig.setAccountFundingSourceOrTransactionLinkId(true);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");


        header = new NtsRequestMessageHeader();
        header.setTerminalDestinationTag("999");
        header.setPinIndicator(PinIndicator.NotPromptedPin);
        header.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        priorMessageInformation = new PriorMessageInformation();
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

        config.setGatewayEventHandler(new IGatewayEventHandler() {
            @Override
            public void eventRaised(IGatewayEvent event) {
            }
        });
        //ServicesContainer.configureService(config);

        // with merchant type
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        // VISA
//        card = TestCards.VisaManual(true, true);
//        track = TestCards.VisaSwipe();

        // VISA CORPORATE
//        card = TestCards.VisaCorporateManual(true, true);
//        cashCard = TestCards.VisaCorporateSwipe();

        // VISA PURCHASING
//        card = TestCards.VisaPurchasingManual(true, true);
//        cashCard = TestCards.VisaPurchasingSwipe();

        // MASTERCARD
        card = new CreditCardData();
        card = TestCards.MasterCardManual(true, true);

//        track = TestCards.MasterCardSwipe(EntryMethod.Swipe);
//        track.setPinBlock("78FBB9DAEEB14E5A");
        track = new CreditTrackData();
        track.setValue(";5473500000000014=12251019999888877776?");
        track.setEntryMethod(EntryMethod.Swipe);

        // MASTERCARD PURCHASING
//        card = TestCards.MasterCardPurchasingManual(true, true);
//        cashCard = TestCards.MasterCardPurchasingSwipe();

        // AMEX
//        card = TestCards.AmexManual(true, true);
//        cashCard = TestCards.AmexSwipe();

        // DISCOVER
//        card = TestCards.DiscoverManual(true, true);
//        cashCard = TestCards.DiscoverSwipe();

    }

    private NtsProductData getProductDataForNonFleetBankCards(IPaymentMethod method) throws ApiException {
        productData = new NtsProductData(ServiceLevel.FullServe, method);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons, 1.24, 2.899);
        productData.addFuel(NtsProductCode.Diesel2, UnitOfMeasure.Gallons, 2.24, 1.259);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 1.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 1, 1.74);
        productData.addNonFuel(NtsProductCode.Dairy, UnitOfMeasure.NoFuelPurchased, 1, 1.74);
        productData.addNonFuel(NtsProductCode.Candy, UnitOfMeasure.NoFuelPurchased, 1, 1.74);
        productData.addNonFuel(NtsProductCode.Milk, UnitOfMeasure.NoFuelPurchased, 1, 1.74);
        productData.addNonFuel(NtsProductCode.OilChange, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        return productData;
    }

    @Test
    public void test_Visa_Sale_tag2Flag12Y() throws ApiException {
        acceptorConfig.setAccountFundingSourceOrTransactionLinkId(true);
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        productData = getProductDataForNonFleetBankCards(track);
        track = NtsTestCards.VisaTrack1(EntryMethod.Swipe);
        Transaction response = track.charge(BigDecimal.valueOf(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withNtsProductData(productData)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_e_commerce_sale_without_track_MC() throws ApiException {
        card = TestCards.MasterCardManual();
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withEcommerceInfo(new EcommerceInfo())
                .withMcWalletId("100")
                .withMcSLI("210")
                .withEcommerceAuthIndicator("S")
                .withMerchantOrCustomerInitiatedFlag("C101")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test58_VisaFleetTwoPointO_auth_noNonFuel() throws ApiException {
        //flag 10 representing the VF 2.0 support
        acceptorConfig.setCapableVisaFleetTwoPointO(true);
        header.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        track = NtsTestCards.VisaFleetTwoPointO(EntryMethod.Swipe);
        FleetData fleetData = new FleetData();
        // fleetdata for tag 43 subtags
        fleetData.setTrailerNumber("12345");
        fleetData.setWorkOrderPoNumber("543210");
        fleetData.setEmployeeNumber("G12345");
        fleetData.setAdditionalPromptData1("567891");
        fleetData.setAdditionalPromptData2("198765");
        // fleetdata for tag 8
        fleetData.setOdometerReading("98765");
        fleetData.setVehicleNumber("56789");

        //product data for tag 9
        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons, 10.24, 2.899);
        productData.addFuel(NtsProductCode.Diesel2, UnitOfMeasure.Gallons, 20.24, 1.259);
        productData.setPurchaseType(PurchaseType.Fuel);
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetFuelAmount(new BigDecimal(10));

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .withPurchaseRestrictionFlag(PurchaseRestrictionFlag.NoRestriction)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test61_VisaFleetTwoPointO_datacollect_noNonFuel() throws ApiException {
        //flag 10 representing the VF 2.0 support
        acceptorConfig.setCapableVisaFleetTwoPointO(true);
        header.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        track = NtsTestCards.VisaFleetTwoPointO(EntryMethod.Swipe);
        FleetData fleetData = new FleetData();
        // fleetdata for tag 43 subtags
        fleetData.setTrailerNumber("12345");
        fleetData.setWorkOrderPoNumber("543210");
        fleetData.setEmployeeNumber("G12345");
        fleetData.setAdditionalPromptData1("567891");
        fleetData.setAdditionalPromptData2("198765");
        // fleetdata for tag 8
        fleetData.setOdometerReading("98765");
        fleetData.setVehicleNumber("56789");

        //product data for tag 9
        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons, 10.24, 2.899);
        productData.addFuel(NtsProductCode.Diesel2, UnitOfMeasure.Gallons, 20.24, 1.259);
        productData.setPurchaseType(PurchaseType.Fuel);
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetFuelAmount(new BigDecimal(10));

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .withPurchaseRestrictionFlag(PurchaseRestrictionFlag.NoRestriction)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test59_VisaFleetTwoPointO_auth_2NonFuel() throws ApiException {
        //flag 10 representing the VF 2.0 support
        acceptorConfig.setCapableVisaFleetTwoPointO(true);
        header.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        track = NtsTestCards.VisaFleetTwoPointO(EntryMethod.Swipe);
        FleetData fleetData = new FleetData();
        // fleetdata for tag 43 subtags
        fleetData.setTrailerNumber("12345");
        fleetData.setWorkOrderPoNumber("543210");
        fleetData.setEmployeeNumber("G12345");
        fleetData.setAdditionalPromptData1("567891");
        fleetData.setAdditionalPromptData2("198765");
        // fleetdata for tag 8
        fleetData.setOdometerReading("98765");
        fleetData.setVehicleNumber("56789");

        //product data for tag 9
        NtsProductData productData = new NtsProductData(ServiceLevel.NonFuelTransaction, track);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 09.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 2, 10.74);
        productData.setPurchaseType(PurchaseType.NonFuel);
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetNonFuelAmount(new BigDecimal(10));

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .withPurchaseRestrictionFlag(PurchaseRestrictionFlag.NoRestriction)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    @Test
    public void test62_VisaFleetTwoPointO_auth_7NonFuel() throws ApiException {
        //flag 10 representing the VF 2.0 support
        acceptorConfig.setCapableVisaFleetTwoPointO(true);
        header.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        track = NtsTestCards.VisaFleetTwoPointO(EntryMethod.Swipe);
        FleetData fleetData = new FleetData();
        // fleetdata for tag 43 subtags
        fleetData.setTrailerNumber("12345");
        fleetData.setWorkOrderPoNumber("543210");
        fleetData.setEmployeeNumber("G12345");
        fleetData.setAdditionalPromptData1("567891");
        fleetData.setAdditionalPromptData2("198765");
        // fleetdata for tag 8
        fleetData.setOdometerReading("98765");
        fleetData.setVehicleNumber("56789");

        //product data for tag 9
        NtsProductData productData = new NtsProductData(ServiceLevel.NonFuelTransaction, track);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 10);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 1, 10);
        productData.addNonFuel(NtsProductCode.Oil, UnitOfMeasure.NoFuelPurchased, 1, 10);
        productData.addNonFuel(NtsProductCode.Tires, UnitOfMeasure.NoFuelPurchased, 1, 10);
        productData.addNonFuel(NtsProductCode.EngineSvc, UnitOfMeasure.NoFuelPurchased, 1, 10);
        productData.addNonFuel(NtsProductCode.OilChange, UnitOfMeasure.NoFuelPurchased, 1, 10);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 1, 10);
        productData.setPurchaseType(PurchaseType.NonFuel);
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetNonFuelAmount(new BigDecimal(10));

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .withPurchaseRestrictionFlag(PurchaseRestrictionFlag.NoRestriction)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    @Test
    public void test_NTS_24point1_e_commerce_sale_without_track_MC() throws ApiException {
        config.setTerminalId("01");
        config.setUnitNumber("00012378911");
        config.setSoftwareVersion("01");
        config.setCompanyId("009");
        ServicesContainer.configureService(config);

        ThreeDSecure threeDSecure = new ThreeDSecure();
        threeDSecure.setAmount(new BigDecimal(10));
        threeDSecure.setCurrency("USD");
        threeDSecure.setOrderId("011");

        card = TestCards.MasterCardManual();
        card.setThreeDSecure(threeDSecure);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.Attended);
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        productData = getProductDataForNonFleetBankCards(card);

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withEcommerceInfo(new EcommerceInfo())
                .withMcWalletId("100")
                .withMcSLI("104")
                .withEcommerceAuthIndicator("S")
                .withMerchantOrCustomerInitiatedFlag("1234")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

}
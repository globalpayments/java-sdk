package com.global.api.tests.network.nts;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.EcommerceInfo;
import com.global.api.entities.StoredCredential;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.FleetData;
import com.global.api.network.entities.NtsProductData;
import com.global.api.network.entities.nts.NtsDataCollectRequest;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.entities.NtsTag16;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.TestCards;
import org.joda.time.DateTime;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import java.math.BigDecimal;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NtsFleetTest {
    String emvTagData = "4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000";
    private CreditCardData card;
    private CreditTrackData track;
    private NtsRequestMessageHeader ntsRequestMessageHeader; //Main Request header class
    private NtsTag16 tag;
    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;
    private NtsProductData productData;
    private FleetData fleetData;

    public NtsFleetTest() throws ApiException {

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
        //ServicesContainer.configureService(config);

        // with merchant type
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);
        ServicesContainer.configureService(config, "ICR");

        // MASTERCARD
        card = TestCards.MasterCardManual(true, true);

        track = new CreditTrackData();
        track.setValue(";5473500000000014=25121019999888877776?");
        track.setEntryMethod(EntryMethod.Swipe);

    }

    private NtsProductData getProductDataForNonFleetBankCards(IPaymentMethod method) throws ApiException {
        productData = new NtsProductData(ServiceLevel.FullServe, method);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons, 10.24, 1.259);
        productData.addFuel(NtsProductCode.Diesel2, UnitOfMeasure.Gallons, 20.24, 1.259);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Dairy, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Candy, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Milk, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.OilChange, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        return productData;
    }

    /**
     * ------------------Visa Fleet------------------------------
     **/
    @Test
    public void test_VisaFleet_authorization() throws ApiException {

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("1234567");
        fleetData.setDriverId("123456789");
        fleetData.setServicePrompt("0");

        track = new CreditTrackData();
        track.setValue("%B4484630000000126^VISA TEST CARD/GOOD^25121019206100000001?");
        track.setEntryMethod(EntryMethod.Swipe);


        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /**
     * Visa Fleet Auth EMV Transaction
     **/
    @Test
    public void test_VisaFleet_auth_track2_amount_expansion_emv() throws ApiException {

        track = new CreditTrackData();
        track.setValue(";4484630000000126=25121019206100000001?");
        track.setEntryMethod(EntryMethod.Swipe);
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withFleetData(fleetData)
                .withNtsTag16(tag)
                .withEmvMaxPinEntry("04")
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /**
     * Visa Fleet Auth EMV offline Transaction
     **/
    @Test
    @Ignore
    public void test_VisaFleet_auth_track2_amount_expansion_emv_offline_decline() throws ApiException {
        track = new CreditTrackData();
        track.setValue(";4484630000000126=25121019206100000001?");
        track.setEntryMethod(EntryMethod.ContactEMV);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withModifier(TransactionModifier.ChipDecline) // Only for the offline decline transactions.
                .withNtsTag16(tag)
                .withFleetData(fleetData)
                .withCvn("123")
                .withEmvMaxPinEntry("04")
                .execute();

        assertNotNull(response);
        assertEquals("12", response.getResponseCode());
    }

    /**
     * Visa Fleet Auth Ecommerce
     **/

    @Test
    public void test_VisaFleet_auth_e_commerce() throws ApiException {

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withEcommerceAuthIndicator("S")
                .withInvoiceNumber("1234567891234567891234567")
                .withFleetData(fleetData)
                .withCvn("123")
                .withEcommerceInfo(new EcommerceInfo())
                .withStoredCredential(new StoredCredential())
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test
    //Need to recheck for ecommerce data 1 and ecommerce data 1
    public void test_VisaFleet_auth_Secure_e_commerce() throws ApiException {

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withEcommerceAuthIndicator("S")
                .withInvoiceNumber("1234567891234567891234567")
                .withFleetData(fleetData)
                .withCvn("123")
                .withEcommerceInfo(new EcommerceInfo())
                .withStoredCredential(new StoredCredential())
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /**
     * Visa Fleet sale Transaction
     **/
    @Test
    public void test_VisaFleet_001_Sale_OnlyFuelItem() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = TestCards.VisaFleetSwipe();
        track.setValue(";4484630000000126=25121019206100000001?");
        track.setEntryMethod(EntryMethod.Swipe);


        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons, 10.24, 1.259);
        productData.setPurchaseType(PurchaseType.Fuel);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_VisaFleet_002_Sale_FuelAndNonFuelItem() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = TestCards.VisaFleetSwipe();
        track.setValue(";4484630000000126=25121019206100000001?");
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons, 10.24, 1.259);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_VisaFleet_003_Sale_OnlyNonFuelItem() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = TestCards.VisaFleetSwipe();
        track.setValue(";4484630000000126=25121019206100000001?");
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.setPurchaseType(PurchaseType.NonFuel);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_VisaFleet_004_Sale_1FuelAnd3NonFuel() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = TestCards.VisaFleetSwipe();
        track.setValue(";4484630000000126=25121019206100000001?");
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Cng, UnitOfMeasure.Gallons, 10.24, 1.259);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Wine, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.AutoGlass, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_VisaFleet_005_Sale_RollUp() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = TestCards.VisaFleetSwipe();
        track.setValue(";4484630000000126=25121019206100000001?");
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons, 10.24, 1.259);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.BeerOrAlc, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.AutoGlass, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.AcService, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.TobacoOth, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Tires, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

//        card=TestCards.VisaFleetManual(true,true);

        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /**
     * Visa Fleet Sales EMV offline
     **/

    @Test
    public void test_Visafleet_sales_emv_offline_without_track() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = TestCards.VisaFleetSwipe();
        track.setValue(";4484630000000126=25121019206100000001?");
        track.setEntryMethod(EntryMethod.Swipe);


        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0001")
                .withEmvMaxPinEntry("20")
                .withCardSequenceNumber("001")
                .withModifier(TransactionModifier.Offline)
                .withOfflineAuthCode("")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /**
     * Visa Fleet sale Ecommerce
     **/
    @Test
    @Ignore
    public void test_VisaFleet_sales_e_commerce_without_track() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = TestCards.VisaFleetSwipe();
        track.setValue(";4484630000000126=25121019206100000001?");
        track.setEntryMethod(EntryMethod.ECommerce);


        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0001")
                .withEmvMaxPinEntry("20")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .withEcommerceAuthIndicator("S")
                .withInvoiceNumber("1234567891234567891234567")
                .withModifier(TransactionModifier.Offline)
                .withOfflineAuthCode("")
                .withNtsTag16(tag)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test
    @Ignore
    public void test_VisaFleet_sales_Secure_e_commerce_without_track() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = TestCards.VisaFleetSwipe();
        track.setValue(";4484630000000126=25121019206100000001?");
        track.setEntryMethod(EntryMethod.SecureEcommerce);


        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0001")
                .withEmvMaxPinEntry("20")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .withEcommerceAuthIndicator("T")
                .withInvoiceNumber("1234567891234567891234567")
                .withModifier(TransactionModifier.Offline)
                .withOfflineAuthCode("")
                .withNtsTag16(tag)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_VisaFleet_001_credit_authorization_void() throws ApiException {

        track = new CreditTrackData();
        track.setValue("%B4484630000000126^VISA TEST CARD/GOOD^25121019206100000001?");
        track.setEntryMethod(EntryMethod.Swipe);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withFleetData(fleetData)
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();

        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test // working
    public void test_VisaFleet_002_credit_sales_void() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";4484630000000126=25121011000062111401?");
        track.setEntryMethod(EntryMethod.Swipe);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test // Working
    public void test_VisaFleet_AuthReversal_001_credit() throws ApiException {

        track = new CreditTrackData();
        track.setValue(";4484630000000126=25121011000062111401?");
        track.setEntryMethod(EntryMethod.Swipe);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withFleetData(fleetData)
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test // Working
    public void test_VisaFleet_saleReversal_001_credit() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        track = new CreditTrackData();
        track.setValue(";4484630000000126=25121011000062111401?");
        track.setEntryMethod(EntryMethod.Swipe);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test //working
    public void test_VisaFleet_001_With_DataCollect_02() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        track = new CreditTrackData();
        track.setValue(";4484630000000126=25121011000062111401?");
        track.setEntryMethod(EntryMethod.Swipe);


        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        NtsDataCollectRequest ntsDataCollectRequest = new NtsDataCollectRequest(NtsMessageCode.DataCollectOrSale, response, new BigDecimal(10));

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = track.charge(new BigDecimal(10))
                .withTransactiontype(TransactionType.DataCollect)
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsDataCollectRequest(ntsDataCollectRequest)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test //working
    public void test_VisaFleet_002_With_DataCollect_03() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";4484630000000126=25121011000062111401?");
        track.setEntryMethod(EntryMethod.Swipe);


        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        NtsDataCollectRequest ntsDataCollectRequest = new NtsDataCollectRequest(NtsMessageCode.DataCollectOrSale, response, new BigDecimal(10));

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction dataCollectResponse = track.charge(new BigDecimal(10))
                .withTransactiontype(TransactionType.DataCollect)
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsDataCollectRequest(ntsDataCollectRequest)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }


    /**---------------------------MasterCard Fleet-----------------------**/
    /**
     * Mastercard fleet Auth
     **/

    @Test
    public void test_MasterCardFleet_authorization() throws ApiException {
        card = TestCards.MasterCardFleetManual(true, true);

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withUniqueDeviceId("0102")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    /**
     * MasterCard Fleet Auth EMV
     **/

    @Test
    public void test_MastercardFleet_auth_track2_amount_expansion_emv() throws ApiException {

        track = new CreditTrackData();
        track.setValue(";5567300000000016=25121019999888877711?");
        track.setEntryMethod(EntryMethod.Swipe);
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withFleetData(fleetData)
                .withNtsTag16(tag)
                .withEmvMaxPinEntry("04")
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /**
     * MasterCard Fleet Auth Ecommerce
     **/

    //Need to check for 32 and 33 user tag
    @Test
    public void test_MastercardFleet_auth_e_commerce() throws ApiException {

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withEcommerceAuthIndicator("S")
                .withFleetData(fleetData)
                .withCvn("123")
                .withEcommerceInfo(new EcommerceInfo())
                .withStoredCredential(new StoredCredential())
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }


    /**
     * MastercardFleet sales transaction
     **/

    //Test1
    @Test
    public void test_MastercardFleet_sales_transaction_1Fueland4NonFuel() throws ApiException {

        card = TestCards.MasterCardFleetManual(true, true);

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, card);
        productData.addFuel(NtsProductCode.Cng, UnitOfMeasure.Gallons, new BigDecimal(10), new BigDecimal(10), new BigDecimal(10));
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 10.24);
        productData.addNonFuel(NtsProductCode.Grocery, UnitOfMeasure.NoFuelPurchased, 1, 10.24);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 1024);
        productData.addNonFuel(NtsProductCode.Milk, UnitOfMeasure.NoFuelPurchased, 2, 10.24);
        productData.add(new BigDecimal(9));
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "00", response.getResponseCode());

    }

    //Test2
    @Test
    public void test_MastercardFleet_sales_transaction_allNonFuel() throws ApiException {

        card = TestCards.MasterCardFleetManual(true, true);

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        NtsProductData productData = new NtsProductData(ServiceLevel.Other_NonFuel, card);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 1, 10.25);
        productData.addNonFuel(NtsProductCode.Grocery, UnitOfMeasure.NoFuelPurchased, 2, 10.24);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 10.24);
        productData.addNonFuel(NtsProductCode.Milk, UnitOfMeasure.NoFuelPurchased, 1, 10);
        productData.add(new BigDecimal(9));
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "00", response.getResponseCode());

    }

    //Track 3
    @Test
    public void test_MastercardFleet_sales_transaction_1Fueland3NonFuel() throws ApiException {

        card = TestCards.MasterCardFleetManual(true, true);

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        NtsProductData productData = new NtsProductData(ServiceLevel.SelfServe, card);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons, 1, 10.25);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 1, 10.48);
        productData.addNonFuel(NtsProductCode.Grocery, UnitOfMeasure.NoFuelPurchased, 1, 10.48);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 10.24);
        productData.add(new BigDecimal(9));
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "00", response.getResponseCode());

    }

    /**
     * MasterCard Fleet Sales EMV Offline
     **/
    @Test
    public void test_Mastercardfleet_sales_emv_offline_without_track() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        track = TestCards.MasterCardFleetSwipe();
        track.setValue(";5567300000000016=25121019999888877711?");
        track.setEntryMethod(EntryMethod.Swipe);


        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0001")
                .withEmvMaxPinEntry("20")
                .withCardSequenceNumber("001")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /**
     * MasterCard Fleet Sales Ecommerce
     **/

    //Need to check for 30,32 tag
    @Test
    @Ignore
    public void test_MasterCardFleet_sales_Secure_e_commerce_without_track() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0001")
                .withEmvMaxPinEntry("20")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .withEcommerceAuthIndicator("T")
                .withModifier(TransactionModifier.Offline)
                .withOfflineAuthCode("")
                .withNtsTag16(tag)
                .withEcommerceInfo(new EcommerceInfo())
                .withStoredCredential(new StoredCredential())
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    /**
     * Mastercard fleet Auth Void transaction
     **/

    @Test
    public void test_001_credit_authorization_track1_void_mcfleet() throws ApiException {
        track = new CreditTrackData();
        track.setValue("%B5567300000000016^MASTERCARD FLEET          ^2512101777766665555444433332111?");
        track.setEntryMethod(EntryMethod.Swipe);
        //

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5555");
        fleetData.setVehicleNumber("85214");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withFleetData(fleetData)
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .withTransactionDate(DateTime.now().toString("MMdd"))
                .withTransactionTime(DateTime.now().toString("hhmmss"))
                .execute();

        assertEquals("00", voidResponse.getResponseCode());
    }

    //track 2
    @Test
    public void test_001_credit_authorization_track2_void_mcfleet() throws ApiException {

        track = new CreditTrackData();
        track.setValue(";5567300000000016=25121019999888877711?");
        track.setEntryMethod(EntryMethod.Swipe);

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5555");
        fleetData.setVehicleNumber("85214");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withFleetData(fleetData)
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();

        assertEquals("00", voidResponse.getResponseCode());
    }

    /**
     * Mastercard fleet sales void transaction
     **/

    //Track2
    @Test
    public void test_001_credit_sales_track2_void_mcfleet() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        track = new CreditTrackData();
        track.setValue(";5567300000000016=25121019999888877711?");
        track.setEntryMethod(EntryMethod.Swipe);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withFleetData(fleetData)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withCvn("123")
                .execute();

        assertNotNull(response);
//        System.out.println(response.getNtsCreditSaleResponse().getHostResponseArea());
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }

    /**
     * Mastercard fleet Reversal Transaction.
     */
    /**
     * Mastercard fleet Auth Reversal Transaction.
     */

    @Test // Working
    public void test_001_credit_authorization_track1_reverse_mcfleet() throws ApiException {

        track = new CreditTrackData();
        track.setValue("%B5567300000000016^MASTERCARD FLEET          ^2512101777766665555444433332111?");
        track.setEntryMethod(EntryMethod.Swipe);

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5555");
        fleetData.setVehicleNumber("85214");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withFleetData(fleetData)
                .withCvn("123")
                .execute();

        assertNotNull(response);

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }

    //Track2
    @Test // Working
    public void test_001_credit_authorization_track2_reverse_mcfleet() throws ApiException {

        track = new CreditTrackData();
        track.setValue(";5567300000000016=25121019999888877711?");
        track.setEntryMethod(EntryMethod.Swipe);

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5555");
        fleetData.setVehicleNumber("85214");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withFleetData(fleetData)
                .withCvn("123")
                .execute();

        assertNotNull(response);

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }

    /**
     * Mastercard fleet Sales Reversal Transaction.
     */

    //track2
    @Test
    public void test_002_credit_sales_track2_reverse_mcfleet() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        track = new CreditTrackData();
        track.setValue(";5567300000000016=25121019999888877711?");
        track.setEntryMethod(EntryMethod.Swipe);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withFleetData(fleetData)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withCvn("123")
                .execute();

        assertNotNull(response);

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }

    /**
     * Mastercard fleet Data Collect Transaction
     */

    @Test //working
    public void test_mastercardfleet_DataCollect() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        track = new CreditTrackData();
        track.setValue(";5567300000000016=25121019999888877711?"); // sample test track 2 data.
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons, 1, 1.259);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        NtsDataCollectRequest ntsDataCollectRequest = new NtsDataCollectRequest(NtsMessageCode.DataCollectOrSale, response, new BigDecimal(10));

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = track.charge(new BigDecimal(10))
                .withTransactiontype(TransactionType.DataCollect)
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsDataCollectRequest(ntsDataCollectRequest)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }


    /**********************Test cases for Authorisation for voyager fleet card***************************/
    /**
     * Authorization Request Format without Track
     */

    @Test
    public void test_001_Voyager_Fleet_auth_without_track_amount_expansion() throws ApiException {
        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setVehicleNumber("123456");

        card = TestCards.VoyagerManual(true, true);
        card.setExpYear(2025);
        card.setExpMonth(9);
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // Authorization Request Format with Track 1 Data

    @Test
    public void test_002_Voyager_Fleet_auth_track1_amount_expansion() throws ApiException {
        track = new CreditTrackData();
        track.setValue("%07088869008250005031^VOYAGER TEST ACCT THREE  ^2512000000000000000?");
        track.setEntryMethod(EntryMethod.Swipe);

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("48000");
        fleetData.setVehicleNumber("123456");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // Authorization Request Format with Track 2 Data

    @Test
    public void test_003_Voyager_Fleet_auth_track2_amount_expansion() throws ApiException {
        track = new CreditTrackData();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.Swipe);

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setVehicleNumber("123456");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_004_Voyager_Fleet_auth_manual() throws ApiException {
        card = new CreditCardData();
        card.setNumber("7088869008250005031");
        card.setExpYear(2025);
        card.setExpMonth(12);

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setVehicleNumber("123456");
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /*-------------**********************Voyager Fleet Sales******************** ----------------------*/
    //Voyager Fleet sale for 2 fuel and 4 non fuel item Transaction

    @Test
    public void test_001_Voyager_Fleet_Sale_2FuelAnd4NonFuel() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = TestCards.VoyagerSwipe();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.Swipe);


        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Regular, UnitOfMeasure.Gallons, 10.34, 1.132);
        productData.addFuel(NtsProductCode.Plus, UnitOfMeasure.Gallons, 10.34, 1.132);
        productData.addNonFuel(NtsProductCode.Premium, UnitOfMeasure.NoFuelPurchased, 2, 10.74);
        productData.addNonFuel(NtsProductCode.Plus2, UnitOfMeasure.NoFuelPurchased, 2, 10.74);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setVehicleNumber("123456");

        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_002_Voyager_Fleet_Sale_OnlyFuelItems() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        track = TestCards.VoyagerSwipe();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Regular, UnitOfMeasure.Gallons, 10.34, 1.132);
        productData.addFuel(NtsProductCode.Plus, UnitOfMeasure.Gallons, 10.24, 1.259);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setVehicleNumber("123456");

        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_003_Voyager_Fleet_Sale_OnlyNonFuelItems() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        track = TestCards.VoyagerSwipe();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 2, 10.74);
        productData.addNonFuel(NtsProductCode.Plus2, UnitOfMeasure.NoFuelPurchased, 2, 10.74);
        productData.addNonFuel(NtsProductCode.Plus3, UnitOfMeasure.NoFuelPurchased, 2, 10.74);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 2, 10.74);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 2, 10.74);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setVehicleNumber("123456");

        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /*--  Voyager Fleet sale  for one fuel product only--*/
    @Test
    public void test_004_Voyager_Fleet_Sale_Only1FuelItem() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        track = TestCards.VoyagerSwipe();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Regular, UnitOfMeasure.Gallons, 10.34, 1.132);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setVehicleNumber("123456");

        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /*--  Voyager Fleet sale for one non fuel product only--*/
    @Test
    public void test_005_Voyager_Fleet_Sale_Only1NonFuelItem() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        track = TestCards.VoyagerSwipe();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setVehicleNumber("123456");

        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /*-- Voyager Fleet sale for 1 fuel 3 Non fuel product only--*/
    @Test
    public void test_006_Voyager_Fleet_Sale_1Fuel_3NonFuelItems() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        track = TestCards.VoyagerSwipe();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Regular, UnitOfMeasure.Gallons, new BigDecimal(10), new BigDecimal(10), new BigDecimal(10));
        productData.addNonFuel(NtsProductCode.Plus, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Premium, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus2, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setVehicleNumber("123456");

        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_007_Voyager_Fleet_Sale_RollUp() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        track = TestCards.VoyagerSwipe();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Plus, UnitOfMeasure.Gallons, 10.24, 1.259);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus3, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus2, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus3, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setVehicleNumber("123456");

        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    //manual

    @Test
    public void test_008_Voyager_Fleet_Sale_Only1NonFuelItem_manual() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        card = new CreditCardData();
        card.setNumber("7088869008250005031");
        card.setExpYear(2025);
        card.setExpMonth(12);

        productData = new NtsProductData(ServiceLevel.FullServe, card);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setVehicleNumber("123456");

        Transaction response = card.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_001_Voyager_Fleet_With_DataCollect() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Plus, UnitOfMeasure.Gallons, 10.24, 1.259);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus3, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus2, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus3, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setVehicleNumber("123456");

        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        NtsDataCollectRequest ntsDataCollectRequest = new NtsDataCollectRequest(NtsMessageCode.RetransmitForceCreditAdjustment, response, new BigDecimal(10));

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = track.charge(new BigDecimal(90.90))
                .withTransactiontype(TransactionType.DataCollect)
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsDataCollectRequest(ntsDataCollectRequest)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_002_Voyager_Fleet_With_DataCollect_manual() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        card = new CreditCardData();
        card.setNumber("7088869008250005031");
        card.setExpYear(2025);
        card.setExpMonth(12);

        productData = new NtsProductData(ServiceLevel.FullServe, card);
        productData.addFuel(NtsProductCode.Plus, UnitOfMeasure.Gallons, 10.24, 1.259);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus3, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus2, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus3, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setVehicleNumber("123456");


        Transaction response = card.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        NtsDataCollectRequest ntsDataCollectRequest = new NtsDataCollectRequest(NtsMessageCode.DataCollectOrSale, response, new BigDecimal(10));

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = card.charge(new BigDecimal(90.90))
                .withTransactiontype(TransactionType.DataCollect)
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsDataCollectRequest(ntsDataCollectRequest)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    /*---Reversal Transaction --*/

    @Test
    public void test_001_Voyager_Fleet_saleReversal_credit() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setVehicleNumber("123456");

        track = new CreditTrackData();
        track.setValue(";7088869008250005031=25120000000000000?");
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
                .execute();
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test
    public void test_002_Voyager_Fleet_saleReversal_credit() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setVehicleNumber("123456");

        card = new CreditCardData();
        card.setNumber("7088869008250005031");
        card.setExpYear(2025);
        card.setExpMonth(12);

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(card))
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test
    public void test_001_Voyager_Fleet_With_1Fuel4NonFuel_CreditAdjustment() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Plus, UnitOfMeasure.Gallons, 10.24, 1.259);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus3, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus2, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setVehicleNumber("123456");


        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        NtsDataCollectRequest ntsDataCollectRequest = new NtsDataCollectRequest(NtsMessageCode.CreditAdjustment, response, new BigDecimal(10));

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction dataCollectResponse = track.charge(new BigDecimal(90.90))
                .withTransactiontype(TransactionType.DataCollect)
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsDataCollectRequest(ntsDataCollectRequest)
                .withNtsProductData(productData)
                .withInvoiceNumber("101023")
                .execute();
        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_002_Voyager_Fleet_With_OnlyFuel_CreditAdjustment() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Plus, UnitOfMeasure.Gallons, 10.24, 1.259);
        productData.addFuel(NtsProductCode.Regular, UnitOfMeasure.Gallons, 10.34, 1.238);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setVehicleNumber("123456");

        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        NtsDataCollectRequest ntsDataCollectRequest = new NtsDataCollectRequest(NtsMessageCode.CreditAdjustment, response, new BigDecimal(10));

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction dataCollectResponse = track.charge(new BigDecimal(90.90))
                .withTransactiontype(TransactionType.DataCollect)
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsDataCollectRequest(ntsDataCollectRequest)
                .withNtsProductData(productData)
                .withInvoiceNumber("101023")
                .execute();
        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_003_Voyager_Fleet_With_OnlyNonFuel_CreditAdjustment() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus3, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus2, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus3, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setVehicleNumber("123456");


        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        NtsDataCollectRequest ntsDataCollectRequest = new NtsDataCollectRequest(NtsMessageCode.CreditAdjustment, response, new BigDecimal(10));

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction dataCollectResponse = track.charge(new BigDecimal(90.90))
                .withTransactiontype(TransactionType.DataCollect)
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsDataCollectRequest(ntsDataCollectRequest)
                .withNtsProductData(productData)
                .withInvoiceNumber("101023")
                .execute();
        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_004_Voyager_Fleet_With_Only1Fuel_5NonFuel_CreditAdjustment() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Plus, UnitOfMeasure.Gallons, 10.34, 1.238);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus3, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus2, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus3, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setVehicleNumber("123456");


        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        NtsDataCollectRequest ntsDataCollectRequest = new NtsDataCollectRequest(NtsMessageCode.CreditAdjustment, response, new BigDecimal(10));
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction dataCollectResponse = track.charge(new BigDecimal(90.90))
                .withTransactiontype(TransactionType.DataCollect)
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsDataCollectRequest(ntsDataCollectRequest)
                .withNtsProductData(productData)
                .withInvoiceNumber("101023")
                .execute();
        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    //Voice authorization
    @Test
    @Ignore
    public void test_VoyagerFleet_Voice_Authorization() throws ApiException {
        track = new CreditTrackData();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.Swipe);


        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setVehicleNumber("123456");
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
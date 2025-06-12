package com.global.api.tests.network.nts;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.EcommerceInfo;
import com.global.api.entities.StoredCredential;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.network.entities.FleetData;
import com.global.api.network.entities.NtsProductData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.entities.NtsTag16;
import com.global.api.network.entities.nts.PriorMessageInfo;
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
import org.joda.time.DateTime;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import java.math.BigDecimal;

import static org.junit.Assert.*;

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
    private PriorMessageInformation priorMessageInformation;

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
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setCapableAmexRemainingBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setCapableVoid(true);
        acceptorConfig.setSupportsEmvPin(true);
        acceptorConfig.setMobileDevice(false);

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
        config.setUnitNumber("00001234567");
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
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons, 10.24, 2.899);
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

        card = TestCards.MasterCardFleetManual(true,true);


        Transaction response = card.charge(new BigDecimal(10))
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

        Transaction transaction = Transaction.fromBuilder()
                .withAuthorizer(response.getTransactionReference().getAuthorizer())
                .withPaymentMethod(track)
                .withOriginalMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry.getValue())
                .withApprovalCode(response.getTransactionReference().getApprovalCode())
                .withAuthorizationCode(response.getAuthorizationCode())
                .withSystemTraceAuditNumber(response.getTransactionReference().getSystemTraceAuditNumber())
                .withTransactionTime(response.getTransactionReference().getOriginalTransactionTime())
                .withOriginalTransactionDate( response.getTransactionReference().getOriginalTransactionDate())
                .withVisaTransactionId(response.getTransactionReference().getVisaTransactionId())
                .build();

        Transaction voidResponse = transaction.voidTransaction(new BigDecimal(10))
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
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
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

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test //working
    public void test_VisaFleet_001_WithOutFleetData_DataCollect_02() throws ApiException {

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
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test //working
    public void test_VisaFleet_002_WithOutFleetData_DataCollect_03() throws ApiException {

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

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
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
                .execute("ICR");
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

        // Re-creating the transaction.
        Transaction transaction = Transaction.fromBuilder()
                .withAuthorizer(response.getTransactionReference().getAuthorizer())
                .withPaymentMethod(track)
                .withOriginalMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry.getValue())
                .withApprovalCode(response.getTransactionReference().getApprovalCode())
                .withAuthorizationCode(response.getAuthorizationCode())
                .withSystemTraceAuditNumber(response.getTransactionReference().getSystemTraceAuditNumber())
                .withTransactionTime(response.getTransactionReference().getOriginalTransactionTime())
                .withOriginalTransactionDate( response.getTransactionReference().getOriginalTransactionDate())
                .withBanknetRefId(response.getTransactionReference().getMastercardBanknetRefNo())
                .withSettlementDate(response.getTransactionReference().getMastercardBanknetSettlementDate())
                .build();


        Transaction voidResponse = transaction.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
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

        fleetData.setDriverId("123456");


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


        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test //working
    public void test_mastercardfleet_WithOutFleetData_DataCollect() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        fleetData.setDriverId("123456");

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


        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
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
        fleetData.setDriverId("123456");

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
        fleetData.setDriverId("123456");

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
        fleetData.setDriverId("123456");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_003_Voyager_Fleet_EMV_auth_track2_amount_expansion() throws ApiException {
        track = new CreditTrackData();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setPinBlock("78FBB9DAEEB14E5A");

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setDriverId("123456");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
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
        fleetData.setDriverId("123456");
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
        fleetData.setDriverId("123456");

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
    public void test_001_Voyager_Fleet_EMV_Sale_2FuelAnd4NonFuel() throws ApiException {
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
        fleetData.setDriverId("123456");

        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .withCardSequenceNumber("101")
                .withModifier(TransactionModifier.Fallback)
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
        fleetData.setDriverId("123456");

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
        fleetData.setDriverId("123456");

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
        fleetData.setDriverId("123456");

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
        fleetData.setDriverId("123456");

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
        fleetData.setDriverId("123456");

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
        fleetData.setDriverId("123456");

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
        fleetData.setDriverId("123456");

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
        fleetData.setDriverId("123456");

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


        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    @Test
    public void test_001_Voyager_Fleet_With_DataCollect_WithOut_FleetData() throws ApiException {

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


        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
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
        fleetData.setDriverId("123456");


        Transaction response = card.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.


        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_002_Voyager_Fleet_With_DataCollect_manual_WithOut_FleetData() throws ApiException {

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

        Transaction response = card.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.


        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
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
        fleetData.setDriverId("123456");

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
        fleetData.setDriverId("123456");

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
        fleetData.setDriverId("123456");


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


        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction dataCollectResponse = response.capture(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

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
        fleetData.setDriverId("123456");

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


        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction dataCollectResponse = response.capture(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

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
        fleetData.setDriverId("123456");


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


        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction dataCollectResponse = response.capture(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
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
        fleetData.setDriverId("123456");


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

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction dataCollectResponse = response.capture(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
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
        fleetData.setDriverId("12345");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withModifier(TransactionModifier.ChipDecline) // Only for the offline decline transactions.
                .execute();

        assertNotNull(response);
        assertEquals("12", response.getResponseCode());
    }

    @Test
    public void test_VisaFleet_refund_reversal_Issue_10226() throws ApiException {
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
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);
        Transaction refundResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .execute();
        assertNotNull(refundResponse);
        // check response
        assertEquals("00", refundResponse.getResponseCode());

        Transaction transaction = Transaction.fromBuilder()
                .withAuthorizer(refundResponse.getTransactionReference().getAuthorizer())
                .withPaymentMethod(track)
                .withDebitAuthorizer(refundResponse.getTransactionReference().getDebitAuthorizer())
                .withApprovalCode(refundResponse.getTransactionReference().getApprovalCode())
                .withAuthorizationCode(refundResponse.getAuthorizationCode())
                .withOriginalTransactionDate(refundResponse.getTransactionReference().getOriginalTransactionDate())
                .withTransactionTime(refundResponse.getTransactionReference().getOriginalTransactionTime())
                .withOriginalMessageCode("03")
                .withBatchNumber(refundResponse.getTransactionReference().getBatchNumber())
                .withSequenceNumber(refundResponse.getTransactionReference().getSequenceNumber())
                .build();

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);
        Transaction reverseResponse = transaction.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        // check response
        assertEquals("00", reverseResponse.getResponseCode());
    }

    @Test
    public void test_VoyagerFleet_Refund_Reversal_Issue_10226() throws ApiException {
        track = new CreditTrackData();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.Swipe);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
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

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);
        Transaction refundResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .execute();
        assertNotNull(refundResponse);
        // check response
        assertEquals("00", refundResponse.getResponseCode());

        Transaction transaction = Transaction.fromBuilder()
                .withAuthorizer(refundResponse.getTransactionReference().getAuthorizer())
                .withPaymentMethod(track)
                .withDebitAuthorizer(refundResponse.getTransactionReference().getDebitAuthorizer())
                .withApprovalCode(refundResponse.getTransactionReference().getApprovalCode())
                .withAuthorizationCode(refundResponse.getAuthorizationCode())
                .withOriginalTransactionDate(refundResponse.getTransactionReference().getOriginalTransactionDate())
                .withTransactionTime(refundResponse.getTransactionReference().getOriginalTransactionTime())
                .withOriginalMessageCode("03")
                .withBatchNumber(refundResponse.getTransactionReference().getBatchNumber())
                .withSequenceNumber(refundResponse.getTransactionReference().getSequenceNumber())
                .build();

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);
        Transaction reverseResponse = transaction.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        // check response
        assertEquals("00", reverseResponse.getResponseCode());

    }

    @Test
    public void test_MasterFleet_refund_reversal_Issue_10226() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        track = new CreditTrackData();
        track.setValue(";5567300000000016=25121019999888877711?");
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

        // refund request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);
        Transaction refundResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .execute();
        assertNotNull(refundResponse);
        // check response
        assertEquals("00", refundResponse.getResponseCode());

        Transaction transaction = Transaction.fromBuilder()
                .withAuthorizer(refundResponse.getTransactionReference().getAuthorizer())
                .withPaymentMethod(track)
                .withDebitAuthorizer(refundResponse.getTransactionReference().getDebitAuthorizer())
                .withApprovalCode(refundResponse.getTransactionReference().getApprovalCode())
                .withAuthorizationCode(refundResponse.getAuthorizationCode())
                .withOriginalTransactionDate(refundResponse.getTransactionReference().getOriginalTransactionDate())
                .withTransactionTime(refundResponse.getTransactionReference().getOriginalTransactionTime())
                .withOriginalMessageCode("03")
                .withBatchNumber(refundResponse.getTransactionReference().getBatchNumber())
                .withSequenceNumber(refundResponse.getTransactionReference().getSequenceNumber())
                .build();

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);
        Transaction reverseResponse = transaction.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        // check response
        assertEquals("00", reverseResponse.getResponseCode());
    }

    @Test // Working
    public void test_ApiExceptionIssue_Preauth_Reversal() throws ApiException {

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

    @Test
    public void test_VisaFleet_sale_UnitPriceValidation_10244() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = TestCards.VisaFleetSwipe();
        track.setValue(";4484630000000126=25121019206100000001?");
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Cng, UnitOfMeasure.Gallons, 10.24, 2.899);
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
    public void test_mastercardFleet_sale_UnitPrice_10244() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        fleetData.setDriverId("123456");
        track = new CreditTrackData();
        track.setValue(";5567300000000016=25121019999888877711?");
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons, 1, 2.899);
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
    }

    @Test
    public void test_masterCardFleet_DataCollect_UnitPriceValidations_10244() throws ApiException{
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

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_mastercardFleet_sale_No_Prompt_masterCardFleet_10263() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        fleetData.setDriverId("123456");
        track = new CreditTrackData();
        track.setValue(";5567300000000016=25121019999888877711?");
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons, 1, 2.899);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));
        productData.setProductCodeType(ProductCodeType.NoPromptMCFleet);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withFleetData(fleetData)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());

    }

    @Test
    public void test_Voyager_Fleet_CreditAdjustment_SpecUpdate23point1_UserDataLength_73_Validation() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        track = TestCards.VoyagerSwipe();

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
        fleetData.setDriverId("123456");

        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Credit Adjustment request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction dataCollectResponse = response.capture(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withInvoiceNumber("101023")
                .execute();
        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_MasterFleet_saleIncorrectFormat_CodeCoverageOnly() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        track = NtsTestCards.MasterFleetTrack2(EntryMethod.SecureEcommerce);
        track.setTrackNumber(TrackNumber.Unknown);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.ImperialGallons, 10.24, 2.899);
        productData.addFuel(NtsProductCode.Diesel2, UnitOfMeasure.Liters, 20.24, 1.259);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.Pounds, 1, 10.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Dairy, UnitOfMeasure.Kilograms, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Candy, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Milk, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.OilChange, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        GatewayException formatException = assertThrows(GatewayException.class,
                () -> track.charge(new BigDecimal(10))
                        .withCurrency("USD")
                        .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                        .withUniqueDeviceId("0102")
                        .withNtsProductData(productData)
                        .withFleetData(fleetData)
                        .withNtsTag16(tag)
                        .withCvn("123")
                        .withInvoiceNumber("1234")
                        .execute());
        assertEquals("Unexpected response from gateway: 70 FormatError", formatException.getMessage());
    }

    @Test
    public void test_003_VoyagerFleet_authServiceLevelSelfServe_() throws ApiException {
        track = NtsTestCards.VoyagerFleetTrack2(EntryMethod.Swipe);

        NtsProductData productData = new NtsProductData(ServiceLevel.SelfServe, track);
        productData.addFuel(NtsProductCode.Plus, UnitOfMeasure.CaseOrCarton, 10.34, 1.238);
        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setDriverId("123456");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_003_VoyagerFleet_SpecUpdate23point1_Auth_DataCollect_UserData_Validation() throws ApiException {
        track = TestCards.VoyagerSwipe();

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setDriverId("123456");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data Collect request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Plus, UnitOfMeasure.Gallons, 10.34, 1.238);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus3, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus2, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus3, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        Transaction dataCollectResponse = response.capture(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withInvoiceNumber("101023")
                .execute();
        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_VisaFleetTwoPointO_authorization() throws ApiException {
        //flag 10 representing the VF 2.0 support
        acceptorConfig.setCapableVisaFleetTwoPointO(true);

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
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 09.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 2, 10.74);
        productData.addNonFuel(NtsProductCode.Oil, UnitOfMeasure.NoFuelPurchased, 3, 11.74);
        productData.addNonFuel(NtsProductCode.Tires, UnitOfMeasure.NoFuelPurchased, 1, 12.74);
        productData.addNonFuel(NtsProductCode.EngineSvc, UnitOfMeasure.NoFuelPurchased, 2, 13.74);
        productData.addNonFuel(NtsProductCode.OilChange, UnitOfMeasure.NoFuelPurchased, 3, 14.74);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 2, 15.74);
        productData.addNonFuel(NtsProductCode.Repairs, UnitOfMeasure.NoFuelPurchased, 1, 16.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetFuelAmount(new BigDecimal(10));
        productData.setNetNonFuelAmount(new BigDecimal(10));

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withNtsProductData(productData)
                .withPurchaseRestrictionFlag(PurchaseRestrictionFlag.ChipBased)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_VisaFleetTwoPointO_authorization_with_fuelOnly() throws ApiException {
        //flag 10 representing the VF 2.0 support
        acceptorConfig.setCapableVisaFleetTwoPointO(true);

        track = NtsTestCards.VisaFleetTwoPointO(EntryMethod.Swipe);
        FleetData fleetData = new FleetData();
        // fleetdata for tag 43 subtags
        fleetData.setTrailerNumber("12345");
        fleetData.setWorkOrderPoNumber("543210");
        fleetData.setEmployeeNumber("G12345");
        fleetData.setAdditionalPromptData1("567891");
        // fleetdata for tag 8
        fleetData.setOdometerReading("98765");
        fleetData.setVehicleNumber("56789");

        //product data for tag 9
        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons, 10.24, 2.899);
        productData.setPurchaseType(PurchaseType.Fuel);
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetFuelAmount(new BigDecimal(10));

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withNtsProductData(productData)
                .withPurchaseRestrictionFlag(PurchaseRestrictionFlag.ChipBased)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_VisaFleetTwoPointO_Auth_DataCollect() throws ApiException {

        acceptorConfig.setCapableVisaFleetTwoPointO(true);

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

        track = NtsTestCards.VisaFleetTwoPointO(EntryMethod.Swipe);
       NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons, 10.24, 2.899);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 09.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 2, 10.74);
        productData.addNonFuel(NtsProductCode.Oil, UnitOfMeasure.NoFuelPurchased, 3, 11.74);
        productData.addNonFuel(NtsProductCode.Tires, UnitOfMeasure.NoFuelPurchased, 1, 12.74);
        productData.addNonFuel(NtsProductCode.EngineSvc, UnitOfMeasure.NoFuelPurchased, 2, 13.74);
        productData.addNonFuel(NtsProductCode.OilChange, UnitOfMeasure.NoFuelPurchased, 3, 14.74);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 2, 15.74);
        productData.addNonFuel(NtsProductCode.Repairs, UnitOfMeasure.NoFuelPurchased, 1, 16.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.setNonFuelProductCount("8");
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetFuelAmount(new BigDecimal(10));
        productData.setNetNonFuelAmount(new BigDecimal(10));

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        Transaction transaction = Transaction.fromBuilder()
                .withAuthorizer(AuthorizerCode.Interchange_Authorized)
                .withPaymentMethod(track)
                .withDebitAuthorizer("00")
                .withApprovalCode("      ")
                .withAuthorizationCode("01798A")
                .withOriginalTransactionDate("0118")
                .withTransactionTime("092701")
                .withOriginalMessageCode("01")
                .withBatchNumber(1)
                .withSequenceNumber(5)
                .build();

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_003_VoyagerFleet_authServiceLevelOther() throws ApiException {
        track = NtsTestCards.VoyagerFleetTrack2(EntryMethod.Swipe);

        NtsProductData productData = new NtsProductData(ServiceLevel.Other, track);
        productData.addFuel(NtsProductCode.Candy, UnitOfMeasure.Kilograms, 10.34, 1.238);
        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setDriverId("123456");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_7_VisaFleetTwoPointO_Auth_creditAdjustment() throws ApiException {

        acceptorConfig.setCapableVisaFleetTwoPointO(true);

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

        track = NtsTestCards.VisaFleetTwoPointO(EntryMethod.ContactEMV);
        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons, 10.24, 2.899);
        productData.addFuel(NtsProductCode.Diesel2, UnitOfMeasure.Gallons, 20.24, 1.259);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 09.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 2, 10.74);
        productData.addNonFuel(NtsProductCode.Oil, UnitOfMeasure.NoFuelPurchased, 3, 11.74);
        productData.addNonFuel(NtsProductCode.Tires, UnitOfMeasure.NoFuelPurchased, 1, 12.74);
        productData.addNonFuel(NtsProductCode.EngineSvc, UnitOfMeasure.NoFuelPurchased, 2, 13.74);
        productData.addNonFuel(NtsProductCode.OilChange, UnitOfMeasure.NoFuelPurchased, 3, 14.74);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 2, 15.74);
        productData.addNonFuel(NtsProductCode.Repairs, UnitOfMeasure.NoFuelPurchased, 1, 16.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.setNonFuelProductCount("8");
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetFuelAmount(new BigDecimal(10));
        productData.setNetNonFuelAmount(new BigDecimal(10));

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);
        Transaction transaction = Transaction.fromBuilder()
                .withAuthorizer(AuthorizerCode.Interchange_Authorized)
                .withPaymentMethod(track)
                .withDebitAuthorizer("00")
                .withApprovalCode("      ")
                .withAuthorizationCode("01800A")
                .withOriginalTransactionDate("0118")
                .withTransactionTime("093636")
                .withOriginalMessageCode("01")
                .withBatchNumber(1)
                .withSequenceNumber(5)
                .build();

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();

        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_003_VoyagerFleet_authServiceLevelUnknown() throws ApiException {
        track = NtsTestCards.VoyagerFleetTrack2(EntryMethod.Swipe);

        NtsProductData productData = new NtsProductData(ServiceLevel.Unknown, track);
        productData.addFuel(NtsProductCode.OilChange, UnitOfMeasure.Liters, 10.34, 1.238);
        productData.addFuel(NtsProductCode.Candy, UnitOfMeasure.Ounces, 10.34, 1.238);
        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setDriverId("123456");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_8_VisaFleetTwoPointO_authorization_with_OdometerAndGenericIDNo_Purchaseflagzero() throws ApiException {
        //flag 10 representing the VF 2.0 support
        acceptorConfig.setCapableVisaFleetTwoPointO(true);

        track = NtsTestCards.VisaFleetTwoPointO(EntryMethod.ContactEMV);
        FleetData fleetData = new FleetData();
        // fleetdata for tag 43 subtags
        fleetData.setTrailerNumber("12345");
        fleetData.setWorkOrderPoNumber("543210");
        fleetData.setEmployeeNumber("G12345");
        fleetData.setAdditionalPromptData1("567891");
        fleetData.setAdditionalPromptData2("198765");
        // fleetdata for tag 8
        fleetData.setOdometerReading("98765");
        fleetData.setGenericIdentificationNo("800781");
        fleetData.setServicePrompt("1");

        //product data for tag 9
        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons, 10.24, 2.899);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 09.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 2, 10.74);
        productData.addNonFuel(NtsProductCode.Oil, UnitOfMeasure.NoFuelPurchased, 3, 11.74);
        productData.addNonFuel(NtsProductCode.Tires, UnitOfMeasure.NoFuelPurchased, 1, 12.74);
        productData.addNonFuel(NtsProductCode.EngineSvc, UnitOfMeasure.NoFuelPurchased, 2, 13.74);
        productData.addNonFuel(NtsProductCode.OilChange, UnitOfMeasure.NoFuelPurchased, 3, 14.74);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 2, 15.74);
        productData.addNonFuel(NtsProductCode.Repairs, UnitOfMeasure.NoFuelPurchased, 1, 16.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.setNonFuelProductCount("8");
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetFuelAmount(new BigDecimal(10));
        productData.setNetNonFuelAmount(new BigDecimal(10));

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withPurchaseRestrictionFlag(PurchaseRestrictionFlag.NoRestriction)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void test_VisaFleetTwoPointO_authorization_with_5NF_products() throws ApiException {
        //flag 10 representing the VF 2.0 support
        acceptorConfig.setCapableVisaFleetTwoPointO(true);

        track = NtsTestCards.VisaFleetTwoPointO(EntryMethod.ContactEMV);
        FleetData fleetData = new FleetData();
        // fleetdata for tag 43 subtags
        fleetData.setTrailerNumber("12345");
        fleetData.setWorkOrderPoNumber("543210");
        fleetData.setEmployeeNumber("G12345");
        fleetData.setAdditionalPromptData1("567891");
        fleetData.setAdditionalPromptData2("198765");
        // fleetdata for tag 8
        fleetData.setOdometerReading("98765");
        fleetData.setGenericIdentificationNo("800781");
        fleetData.setServicePrompt("1");

        //product data for tag 9
        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 09.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 2, 10.74);
        productData.addNonFuel(NtsProductCode.Oil, UnitOfMeasure.NoFuelPurchased, 3, 11.74);
        productData.addNonFuel(NtsProductCode.Tires, UnitOfMeasure.NoFuelPurchased, 1, 12.74);
        productData.addNonFuel(NtsProductCode.EngineSvc, UnitOfMeasure.NoFuelPurchased, 2, 13.74);
        productData.setPurchaseType(PurchaseType.NonFuel);
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetFuelAmount(new BigDecimal(10));
        productData.setNetNonFuelAmount(new BigDecimal(10));

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withPurchaseRestrictionFlag(PurchaseRestrictionFlag.NoRestriction)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void test_VisaFleet_authorization_cert() throws ApiException {

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("1234567");
        fleetData.setDriverId("123456789");
        fleetData.setServicePrompt("0");

        track = NtsTestCards.VisaFleet(EntryMethod.Swipe);
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withUniqueDeviceId("0102")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_003_VoyagerFleet_authWithoutFleetData_codeCoverageOnly() throws ApiException {
        track = NtsTestCards.VoyagerFleetTrack2(EntryMethod.Swipe);

        NtsProductData productData = new NtsProductData(ServiceLevel.Unknown, track);
        productData.addFuel(NtsProductCode.OilChange, UnitOfMeasure.Liters, 10.34, 1.238);
        productData.addFuel(NtsProductCode.Candy, UnitOfMeasure.Ounces, 10.34, 1.238);
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_VisaFleet_Sale_MSR_cert() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

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
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_VisaFleet_Sale_MSR_1Fuel4Nonfuel_cert() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track.setValue(";4484630000000126=25121019206100000001?");
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Cng, UnitOfMeasure.Gallons, 10.24, 1.259);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Wine, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.AutoGlass, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.IceCream, UnitOfMeasure.NoFuelPurchased, 1, 5.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        Transaction response = track.charge(new BigDecimal(10.5))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0101")
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void test_VisaFleetTwoPointO_authorizationAndDataCollect_with_OdometerAndGenericIDNo() throws ApiException {
        //flag 10 representing the VF 2.0 support
        acceptorConfig.setCapableVisaFleetTwoPointO(true);

        track = NtsTestCards.VisaFleetTwoPointO(EntryMethod.ContactEMV);
        FleetData fleetData = new FleetData();
        // tag 43
        fleetData.setTrailerNumber("12345");
        fleetData.setWorkOrderPoNumber("543210");
        fleetData.setEmployeeNumber("G12345");
        fleetData.setAdditionalPromptData1("567891");
        fleetData.setAdditionalPromptData2("198765");

        //tag 8
        fleetData.setOdometerReading("98765");
        fleetData.setGenericIdentificationNo("800781");


        //product data for tag 9
        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons, 10.24, 2.899);
        productData.addFuel(NtsProductCode.Diesel2, UnitOfMeasure.Gallons, 20.24, 1.259);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 09.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 2, 10.74);
        productData.addNonFuel(NtsProductCode.Oil, UnitOfMeasure.NoFuelPurchased, 3, 11.74);
        productData.addNonFuel(NtsProductCode.Tires, UnitOfMeasure.NoFuelPurchased, 1, 12.74);
        productData.addNonFuel(NtsProductCode.EngineSvc, UnitOfMeasure.NoFuelPurchased, 2, 13.74);
        productData.addNonFuel(NtsProductCode.OilChange, UnitOfMeasure.NoFuelPurchased, 3, 14.74);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 2, 15.74);
        productData.addNonFuel(NtsProductCode.Repairs, UnitOfMeasure.NoFuelPurchased, 1, 16.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.setNonFuelProductCount("8");
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetFuelAmount(new BigDecimal(10));
        productData.setNetNonFuelAmount(new BigDecimal(10));

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withPurchaseRestrictionFlag(PurchaseRestrictionFlag.ChipBased)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

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
    public void test_VisaFleet_auth_WithGenericIdentificationNoOnly() throws ApiException {
        FleetData fleetData = new FleetData();
        fleetData.setGenericIdentificationNo("123456789");

        track = NtsTestCards.VisaFleetTrack1(EntryMethod.Swipe);
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

    @Test
    public void test_10_VisaFleetTwoPointO_authorization_with_OdometerAndDriverId() throws ApiException {
        //flag 10 representing the VF 2.0 support
        acceptorConfig.setCapableVisaFleetTwoPointO(true);

        track = NtsTestCards.VisaFleetTwoPointO(EntryMethod.ContactEMV);
        FleetData fleetData = new FleetData();
        // tag 43
        fleetData.setTrailerNumber("12345");
        fleetData.setWorkOrderPoNumber("543210");
        fleetData.setEmployeeNumber("G12345");
        fleetData.setAdditionalPromptData1("567891");
        fleetData.setAdditionalPromptData2("198765");
        //tag 8
        fleetData.setOdometerReading("98765");
        fleetData.setDriverId("800900");

        //product data for tag 9 only fuel item
        NtsProductData productData = new NtsProductData(ServiceLevel.NonFuelTransaction, track);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 09.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 2, 10.74);
        productData.addNonFuel(NtsProductCode.Oil, UnitOfMeasure.NoFuelPurchased, 3, 11.74);
        productData.addNonFuel(NtsProductCode.Tires, UnitOfMeasure.NoFuelPurchased, 1, 12.74);
        productData.addNonFuel(NtsProductCode.EngineSvc, UnitOfMeasure.NoFuelPurchased, 2, 13.74);
        productData.addNonFuel(NtsProductCode.OilChange, UnitOfMeasure.NoFuelPurchased, 3, 14.74);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 2, 15.74);
        productData.addNonFuel(NtsProductCode.Repairs, UnitOfMeasure.NoFuelPurchased, 1, 16.74);
        productData.addNonFuel(NtsProductCode.IceCream, UnitOfMeasure.NoFuelPurchased, 1, 10);
        productData.addNonFuel(NtsProductCode.Candy, UnitOfMeasure.NoFuelPurchased, 1, 20);

        productData.setPurchaseType(PurchaseType.NonFuel);
        productData.setNonFuelProductCount("8");
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetFuelAmount(new BigDecimal(10));
        productData.setNetNonFuelAmount(new BigDecimal(10));
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withNtsProductData(productData)
                .withPurchaseRestrictionFlag(PurchaseRestrictionFlag.HostBased)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_VisaFleet_auth_codeCoverageOnly() throws ApiException {
        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("123456789");

        track = NtsTestCards.VisaFleetTrack1(EntryMethod.Swipe);
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withPurchaseRestrictionFlag(PurchaseRestrictionFlag.HostBased)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_10_VisaFleet_DataCollect_withOdometerAndDriverId() throws ApiException {
        //flag 10 representing the VF 2.0 support
        acceptorConfig.setCapableVisaFleetTwoPointO(true);

        track = NtsTestCards.VisaFleetTwoPointO(EntryMethod.ContactEMV);
        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("98765");
        fleetData.setDriverId("800900");

        //product data for tag 9 only fuel item
        NtsProductData productData = new NtsProductData(ServiceLevel.NonFuelTransaction, track);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 09.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 2, 10.74);
        productData.addNonFuel(NtsProductCode.Oil, UnitOfMeasure.NoFuelPurchased, 3, 11.74);
        productData.addNonFuel(NtsProductCode.Tires, UnitOfMeasure.NoFuelPurchased, 1, 12.74);
        productData.addNonFuel(NtsProductCode.EngineSvc, UnitOfMeasure.NoFuelPurchased, 2, 13.74);
        productData.addNonFuel(NtsProductCode.OilChange, UnitOfMeasure.NoFuelPurchased, 3, 14.74);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 2, 15.74);
        productData.addNonFuel(NtsProductCode.Repairs, UnitOfMeasure.NoFuelPurchased, 1, 16.74);
        productData.addNonFuel(NtsProductCode.IceCream, UnitOfMeasure.NoFuelPurchased, 1, 10);
        productData.addNonFuel(NtsProductCode.Candy, UnitOfMeasure.NoFuelPurchased, 1, 20);
        productData.setPurchaseType(PurchaseType.NonFuel);
        productData.setNonFuelProductCount("8");
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetFuelAmount(new BigDecimal(10));
        productData.setNetNonFuelAmount(new BigDecimal(10));

        Transaction transaction = Transaction.fromBuilder()
                .withAuthorizer(AuthorizerCode.Interchange_Authorized)
                .withPaymentMethod(track)
                .withDebitAuthorizer("00")
                .withApprovalCode("      ")
                .withAuthorizationCode("01202A")
                .withOriginalTransactionDate("0122")
                .withTransactionTime("134408")
                .withOriginalMessageCode("01")
                .withBatchNumber(1)
                .withSequenceNumber(5)
                .build();

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_11_VisaFleetTwoPointO_authorizationAndDataCollect_with_OdometerOnly() throws ApiException {
        //flag 10 representing the VF 2.0 support
        acceptorConfig.setCapableVisaFleetTwoPointO(true);

        track = NtsTestCards.VisaFleetTwoPointO(EntryMethod.ContactEMV);
        FleetData fleetData = new FleetData();
        // tag 43
        fleetData.setTrailerNumber("12345");
        fleetData.setWorkOrderPoNumber("543210");
        fleetData.setEmployeeNumber("G12345");
        fleetData.setAdditionalPromptData1("567891");
        fleetData.setAdditionalPromptData2("198765");

        //tag 8
        fleetData.setOdometerReading("98765");


        //product data for tag 9 one fuel & 3 non fuel
        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons, 10.24, 2.899);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 09.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 2, 10.74);
        productData.addNonFuel(NtsProductCode.Candy, UnitOfMeasure.NoFuelPurchased, 1, 20);

        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.setNonFuelProductCount("3");
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetFuelAmount(new BigDecimal(10));
        productData.setNetNonFuelAmount(new BigDecimal(10));

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withPurchaseRestrictionFlag(PurchaseRestrictionFlag.BothChipAndHostBased)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

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
    public void test_MasterFleet_auth_withEmptyFleetData() throws ApiException {
        track = NtsTestCards.MasterFleetTrack2(EntryMethod.Swipe);
        FleetData fleetData = new FleetData();
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void test_VisaFleet_sale_WithoutSaleTax() throws ApiException {
        track = NtsTestCards.VisaFleetTrack1(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons, 10.24, 2.899);
        productData.addFuel(NtsProductCode.Diesel2, UnitOfMeasure.Gallons, 20.24, 1.259);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Dairy, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Candy, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Milk, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.OilChange, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
}

    @Test
    public void test_MasterFleet_sale_OtherNonFuel() throws ApiException {
        track = NtsTestCards.MasterFleetTrack2(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.Other_NonFuel, track);
        productData.addFuel(NtsProductCode.Cng,UnitOfMeasure.ImperialGallons,1,23,12);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Dairy, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Candy, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Milk, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.OilChange, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_007_Voyager_Fleet_Sale_RollUpAtNonFuel_codeCoverage() throws ApiException {
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
        productData.addNonFuel(NtsProductCode.Candy, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.EngineSvc, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.TransSvc, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.IceCream, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setDriverId("123456");

        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

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
    public void test_12_VisaFleetTwoPointO_authorizationAndDataCollect_withoutPromtAndZeroPurchaseflag() throws ApiException {
        //flag 10 representing the VF 2.0 support
        acceptorConfig.setCapableVisaFleetTwoPointO(true);

        track = NtsTestCards.VisaFleetTwoPointO(EntryMethod.ContactEMV);
        FleetData fleetData = new FleetData();
        //tag 8
        fleetData.setOdometerReading("98765");
        fleetData.setDriverId("800781");

        //product data for tag 9 one fuel & 3 non fuel
        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons, 10.24, 2.899);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 09.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 2, 10.74);
        productData.addNonFuel(NtsProductCode.Candy, UnitOfMeasure.NoFuelPurchased, 1, 20);

        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.setNonFuelProductCount("3");
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetFuelAmount(new BigDecimal(10));
        productData.setNetNonFuelAmount(new BigDecimal(10));

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withPurchaseRestrictionFlag(PurchaseRestrictionFlag.NoRestriction)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

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
    public void test_13_VisaFleetTwoPointO_authorizationAndDataCollect_withGenericIdentificationAndOnePurchaseflag() throws ApiException {
        //flag 10 representing the VF 2.0 support
        acceptorConfig.setCapableVisaFleetTwoPointO(true);

        track = NtsTestCards.VisaFleetTwoPointO(EntryMethod.ContactEMV);
        FleetData fleetData = new FleetData();
        fleetData.setTrailerNumber("12345");
        fleetData.setWorkOrderPoNumber("543210");
        fleetData.setEmployeeNumber("G12345");
        //tag 8
        fleetData.setGenericIdentificationNo("98765");

        //product data for tag 9 one fuel & 3 non fuel
        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons, 10.24, 2.899);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 09.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 2, 10.74);
        productData.addNonFuel(NtsProductCode.Candy, UnitOfMeasure.NoFuelPurchased, 1, 20);

        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.setNonFuelProductCount("3");
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetFuelAmount(new BigDecimal(10));
        productData.setNetNonFuelAmount(new BigDecimal(10));

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withPurchaseRestrictionFlag(PurchaseRestrictionFlag.ChipBased)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

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
    public void test_8_VisaFleetTwoPointO_DataCollect_withGenericIdAndOdometer() throws ApiException {

        acceptorConfig.setCapableVisaFleetTwoPointO(true);

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

        track = NtsTestCards.VisaFleetTwoPointO(EntryMethod.ContactEMV);
        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons, 10.24, 2.899);
        productData.addFuel(NtsProductCode.Diesel2, UnitOfMeasure.Gallons, 20.24, 1.259);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 09.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 2, 10.74);
        productData.addNonFuel(NtsProductCode.Oil, UnitOfMeasure.NoFuelPurchased, 3, 11.74);
        productData.addNonFuel(NtsProductCode.Tires, UnitOfMeasure.NoFuelPurchased, 1, 12.74);
        productData.addNonFuel(NtsProductCode.EngineSvc, UnitOfMeasure.NoFuelPurchased, 2, 13.74);
        productData.addNonFuel(NtsProductCode.OilChange, UnitOfMeasure.NoFuelPurchased, 3, 14.74);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 2, 15.74);
        productData.addNonFuel(NtsProductCode.Repairs, UnitOfMeasure.NoFuelPurchased, 1, 16.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.setNonFuelProductCount("8");
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetFuelAmount(new BigDecimal(10));
        productData.setNetNonFuelAmount(new BigDecimal(10));

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        Transaction transaction = Transaction.fromBuilder()
                .withAuthorizer(AuthorizerCode.Interchange_Authorized)
                .withPaymentMethod(track)
                .withDebitAuthorizer("00")
                .withApprovalCode("      ")
                .withAuthorizationCode("01804A")
                .withOriginalTransactionDate("0118")
                .withTransactionTime("095307")
                .withOriginalMessageCode("01")
                .withBatchNumber(1)
                .withSequenceNumber(5)
                .build();

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_9_VisaFleetTwoPointO_authorization_with_OdometerAndVehicleID_FuelOnly() throws ApiException {
        //flag 10 representing the VF 2.0 support
        acceptorConfig.setCapableVisaFleetTwoPointO(true);

        track = NtsTestCards.VisaFleetTwoPointO(EntryMethod.ContactEMV);
        FleetData fleetData = new FleetData();
        // fleetdata for tag 43 subtags
        fleetData.setTrailerNumber("12345");
        fleetData.setWorkOrderPoNumber("543210");
        fleetData.setEmployeeNumber("G12345");
        fleetData.setAdditionalPromptData1("567891");
        fleetData.setAdditionalPromptData2("198765");
        // fleetdata for tag 8
        fleetData.setOdometerReading("98765");
        fleetData.setVehicleNumber("800781");

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
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withPurchaseRestrictionFlag(PurchaseRestrictionFlag.ChipBased)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_9_VisaFleetTwoPointO_DataCollect_withVehicleIdAndOdometer_fuelProductOnly() throws ApiException {

        acceptorConfig.setCapableVisaFleetTwoPointO(true);

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

        track = NtsTestCards.VisaFleetTwoPointO(EntryMethod.ContactEMV);
        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons, 10.24, 2.899);
        productData.addFuel(NtsProductCode.Diesel2, UnitOfMeasure.Gallons, 20.24, 1.259);
        productData.setPurchaseType(PurchaseType.Fuel);
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetFuelAmount(new BigDecimal(10));

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        Transaction transaction = Transaction.fromBuilder()
                .withAuthorizer(AuthorizerCode.Interchange_Authorized)
                .withPaymentMethod(track)
                .withDebitAuthorizer("00")
                .withApprovalCode("      ")
                .withAuthorizationCode("01815A")
                .withOriginalTransactionDate("0118")
                .withTransactionTime("101745")
                .withOriginalMessageCode("01")
                .withBatchNumber(1)
                .withSequenceNumber(5)
                .build();

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    @Test
    public void test_5_VisaFleet_preauth() throws ApiException {
        track = NtsTestCards.VisaFleet(EntryMethod.Swipe);
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0104")
                .withFleetData(fleetData)
                .withNtsTag16(tag)
                .withCvn("456")
                .execute();
        assertNotNull(response);
    }

    @Test
    public void test_5_VisaFleet_Datacollect_withVisaFleetTwoPointO_capableTerminal() throws ApiException{
        acceptorConfig.setCapableVisaFleetTwoPointO(true);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        track = NtsTestCards.VisaFleet(EntryMethod.Swipe);
        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("98765");
        fleetData.setVehicleNumber("800781");

        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons, 10.24, 2.899);
        productData.addFuel(NtsProductCode.Diesel2, UnitOfMeasure.Gallons, 20.24, 1.259);
        productData.addNonFuel(NtsProductCode.OilChange, UnitOfMeasure.NoFuelPurchased, 3, 14.74);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 2, 15.74);
        productData.addNonFuel(NtsProductCode.Repairs, UnitOfMeasure.NoFuelPurchased, 1, 16.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetFuelAmount(new BigDecimal(10));
        productData.setNetNonFuelAmount(new BigDecimal(10));

        Transaction transaction = Transaction.fromBuilder()
                .withAuthorizer(AuthorizerCode.Interchange_Authorized)
                .withPaymentMethod(track)
                .withDebitAuthorizer("00")
                .withApprovalCode("      ")
                .withAuthorizationCode("03502A")
                .withOriginalTransactionDate("0201")
                .withTransactionTime("141443")
                .withOriginalMessageCode("01")
                .withBatchNumber(1)
                .withSequenceNumber(5)
                .build();

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_5_VisaFleet_preauth_withVisaFleet2_UserDataFormat() throws ApiException {
        acceptorConfig.setCapableVisaFleetTwoPointO(true);
        track = NtsTestCards.VisaFleet(EntryMethod.ContactEMV);

        FleetData fleetData = new FleetData();
        // fleetdata for tag 43 subtags
        fleetData.setTrailerNumber("12345");
        fleetData.setWorkOrderPoNumber("543210");
        fleetData.setEmployeeNumber("G12345");
        // fleetdata for tag 8
        fleetData.setOdometerReading("98765");
        fleetData.setVehicleNumber("800781");

        //product data for tag 9
        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons, 10.24, 2.899);
        productData.addFuel(NtsProductCode.Diesel2, UnitOfMeasure.Gallons, 20.24, 1.259);
        productData.addNonFuel(NtsProductCode.OilChange, UnitOfMeasure.NoFuelPurchased, 3, 14.74);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 2, 15.74);
        productData.addNonFuel(NtsProductCode.Repairs, UnitOfMeasure.NoFuelPurchased, 1, 16.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetFuelAmount(new BigDecimal(10));
        productData.setNetNonFuelAmount(new BigDecimal(10));

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0104")
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .withPurchaseRestrictionFlag(PurchaseRestrictionFlag.ChipBased)
                .withNtsTag16(tag)
                .withCvn("456")
                .execute();
        assertNotNull(response);
    }


    @Test
    public void test06_MastercardFleet_sales_transaction_1Fueland4NonFuel() throws ApiException {
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);
        card = TestCards.MasterCardFleetManual(true, true);

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, card);
        productData.addFuel(NtsProductCode.Cng, UnitOfMeasure.Gallons, new BigDecimal(10), new BigDecimal(10), new BigDecimal(10));
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 10.24);
        productData.addNonFuel(NtsProductCode.Grocery, UnitOfMeasure.NoFuelPurchased, 1, 10.24);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 1024);
        productData.add(new BigDecimal(9));
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "00", response.getResponseCode());

    }

    @Test
    public void test07_MastercardFleet_auth_transaction() throws ApiException {
        config.setMerchantType("5542");
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.UnattendedAfd);
        ServicesContainer.configureService(config);
        track.setValue(";5567300000000016=25121019999888877711?");
        track.setEntryMethod(EntryMethod.Swipe);
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "00", response.getResponseCode());

    }

    //V22.1 Certification Voyager Authorization Request Format with Track 2 Data
    @Test
    public void test09_Voyager_Fleet_auth_track2_amount_expansion() throws ApiException {
        track = new CreditTrackData();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.Swipe);

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setDriverId("123456");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test08_Voyager_Fleet_EMV_auth_track2_amount_expansion() throws ApiException {
        track = new CreditTrackData();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.ContactEMV);
        track.setPinBlock("78FBB9DAEEB14E5A");

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setDriverId("123456");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();
        
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }


    // Set the PDL timeout value using separate field withPDLTimeout.
    @Test
    public void test_VisaFleet_authorization_10329_PDLTimeout_1() throws ApiException {
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
                .withPDLTimeout(NTSCardTypes.VisaFleet.getTimeOut())
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test10_Voyager_Fleet_With_AuthEmv_DataCollect() throws ApiException {
        track = new CreditTrackData();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.ContactEMV);
        track.setPinBlock("78FBB9DAEEB14E5A");

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setDriverId("123456");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Plus, UnitOfMeasure.Gallons, 10.24, 1.259);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus3, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        // Data-Collect request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        Transaction dataCollectResponse = response.capture(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test11_Voyager_Fleet_With_DataCollect() throws ApiException {
        track = new CreditTrackData();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.Swipe);

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setDriverId("123456");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        // Data-Collect request preparation.

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Plus, UnitOfMeasure.Gallons, 10.24, 1.259);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus3, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus2, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus3, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());
    }


    @Test
    public void test12_v22_1cert_Voyager_Fleet_EMV_Sale_2FuelAnd4NonFuel() throws ApiException {
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
        fleetData.setDriverId("123456");
        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .withCardSequenceNumber("101")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }


    @Test
    public void test13_v22_1cert_Voyager_Fleet_With_1Fuel4NonFuel_CreditAdjustment() throws ApiException {
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
        fleetData.setDriverId("123456");
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
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);
        Transaction dataCollectResponse = response.capture(response.getTransactionReference().getOriginalApprovedAmount())
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .withNtsProductData(productData)
                .withInvoiceNumber("101023")
                .execute();
        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test14_v22_1cert_Voyager_Fleet_With_1Fuel4NonFuel_CreditAdjustment() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Plus, UnitOfMeasure.Gallons, 10.24, 1.259);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setDriverId("123456");
        Transaction response = track.charge(new BigDecimal(20))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);
        System.out.println(response.getTransactionReference().getOriginalApprovedAmount());
        Transaction dataCollectResponse = response.capture(5)
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .withNtsProductData(productData)
                .withInvoiceNumber("101023")
                .execute();
        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_VisaFleet_authorization_10329_PDLTimeout_2() throws ApiException {
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
                .withPDLTimeout(31)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void test_VisaFleet_authorization_Reversal_10329_PDLTimeout_1() throws ApiException {

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
                .withPDLTimeout(NTSCardTypes.VisaFleet.getTimeOut())
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);
        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withPDLTimeout(NTSCardTypes.VisaFleet.getTimeOut())
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }

    /**
      Voyager EMV Auth User Data
     */

    // Voyager Fleet with all the data ,such as pinBlock,ksn,card seq No,modifier etc
    @Test
    public void test_002_VoyagerFleet_AuthEMV_Positive() throws ApiException {
        track = TestCards.VoyagerSwipe(EntryMethod.ContactEMV);
        track.setPinBlock("78FBB9DAEEB14E5A   ");

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("48000");
        fleetData.setDriverId("123456");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .withCardSequenceNumber("123")
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // Voyager Fleet without all the fields except tagData from the User Data.
    @Test
    public void test_002_VoyagerFleet_AuthEMV_Negative() throws ApiException {
        track = TestCards.VoyagerSwipe();

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("48000");
        fleetData.setDriverId("123456");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_002_VoyagerFleet_AuthEMV_withOut_ModifierOnly() throws ApiException {
        track = TestCards.VoyagerSwipe();
        track.setPinBlock("78FBB9DAEEB14E5A   ");

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("48000");
        fleetData.setDriverId("123456");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .withCardSequenceNumber("123")
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_002_VoyagerFleet_AuthEMV_withOutPinBlock_KSN() throws ApiException {
        track = TestCards.VoyagerSwipe();

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("48000");
        fleetData.setDriverId("123456");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .withCardSequenceNumber("123")
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_002_VoyagerFleet_AuthEMV_withOut_cardSeqNo() throws ApiException {
        track = TestCards.VoyagerSwipe();
        track.setPinBlock("78FBB9DAEEB14E5A   ");

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("48000");
        fleetData.setDriverId("123456");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /**
     Voyager EMV Sale User Data
     */
    @Test
    public void test_002_VoyagerFleet_SaleEMV_Positive() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        track = TestCards.VoyagerSwipe(EntryMethod.ContactEMV);
        track.setPinBlock("78FBB9DAEEB14E5A   ");

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Regular, UnitOfMeasure.Gallons, 10.34, 1.132);
        productData.addFuel(NtsProductCode.Plus, UnitOfMeasure.Gallons, 10.24, 1.259);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setDriverId("123456");
        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .withTagData(emvTagData)
                .withCardSequenceNumber("123")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // Voyager Fleet without all the fields except tagData from the User Data.
    @Test
    public void test_002_VoyagerFleet_SaleEMV_Negative() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = TestCards.VoyagerSwipe();

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Regular, UnitOfMeasure.Gallons, 10.34, 1.132);
        productData.addFuel(NtsProductCode.Plus, UnitOfMeasure.Gallons, 10.24, 1.259);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setDriverId("123456");
        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .withTagData(emvTagData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_002_VoyagerFleet_SaleEMV_withOut_Modifier() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = TestCards.VoyagerSwipe();
        track.setPinBlock("78FBB9DAEEB14E5A   ");

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Regular, UnitOfMeasure.Gallons, 10.34, 1.132);
        productData.addFuel(NtsProductCode.Plus, UnitOfMeasure.Gallons, 10.24, 1.259);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setDriverId("123456");
        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .withTagData(emvTagData)
                .withCardSequenceNumber("123")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_002_VoyagerFleet_SaleEMV_withOut_PinBlock_And_KSN() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = TestCards.VoyagerSwipe();

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Regular, UnitOfMeasure.Gallons, 10.34, 1.132);
        productData.addFuel(NtsProductCode.Plus, UnitOfMeasure.Gallons, 10.24, 1.259);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setDriverId("123456");
        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .withTagData(emvTagData)
                .withModifier(TransactionModifier.Fallback)
                .withCardSequenceNumber("123")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_002_VoyagerFleet_SaleEMV_withOut_CardSeqNumber() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = TestCards.VoyagerSwipe();
        track.setPinBlock("78FBB9DAEEB14E5A   ");

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Regular, UnitOfMeasure.Gallons, 10.34, 1.132);
        productData.addFuel(NtsProductCode.Plus, UnitOfMeasure.Gallons, 10.24, 1.259);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setDriverId("123456");
        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .withTagData(emvTagData)
                .withModifier(TransactionModifier.Fallback)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_14_VoyagerFleet_authMSR() throws ApiException {
        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("48000");
        fleetData.setDriverId("123456");
        track = TestCards.VoyagerSwipe();
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_5_VoyagerFleet_Datacollect_() throws ApiException{
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        track = TestCards.VoyagerSwipe();

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("12345");
        fleetData.setDriverId("12345");
        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Plus, UnitOfMeasure.Gallons, 10.34, 1.238);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus3, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus2, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus3, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        Transaction transaction = Transaction.fromBuilder()
                .withAuthorizer(AuthorizerCode.Interchange_Authorized)
                .withPaymentMethod(track)
                .withDebitAuthorizer("00")
                .withApprovalCode("      ")
                .withAuthorizationCode("000616")
                .withOriginalTransactionDate("0129")
                .withTransactionTime("153044")
                .withOriginalMessageCode("01")
                .withBatchNumber(1)
                .withSequenceNumber(5)
                .build();

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    @Test
    public void test_16_VoyagerFleet_MSRCreditAdjustment_cert() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        track = TestCards.VoyagerSwipe();

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
        fleetData.setDriverId("123456");
        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // credit Adjustment request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);
        Transaction dataCollectResponse = response.capture(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withInvoiceNumber("101023")
                .execute();
        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    @Test
    public void test_16_VoyagerFleet_EMVSale_cert() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = TestCards.VoyagerSwipe();
        track.setPinBlock("78FBB9DAEEB14E5A   ");

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Regular, UnitOfMeasure.Gallons, 10.34, 1.132);
        productData.addFuel(NtsProductCode.Plus, UnitOfMeasure.Gallons, 10.24, 1.259);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setDriverId("123456");
        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .withTagData(emvTagData)
                .withCardSequenceNumber("123")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void test_5_VoyagerFleet_EMVCreditAdjustment_cert() throws ApiException{
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        track = TestCards.VoyagerSwipe();

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("12345");
        fleetData.setDriverId("12345");
        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Plus, UnitOfMeasure.Gallons, 10.34, 1.238);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus3, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus2, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus3, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        Transaction transaction = Transaction.fromBuilder()
                .withAuthorizer(AuthorizerCode.Interchange_Authorized)
                .withPaymentMethod(track)
                .withDebitAuthorizer("00")
                .withApprovalCode("      ")
                .withAuthorizationCode("000622")
                .withOriginalTransactionDate("0122")
                .withTransactionTime("150456")
                .withOriginalMessageCode("02")
                .withBatchNumber(1)
                .withSequenceNumber(5)
                .build();

        // credit Adjustment request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);
        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withInvoiceNumber("101023")
                .execute();
        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    @Test
    public void test_16_VoyagerFleet_MSRSale_cert() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        track = TestCards.VoyagerSwipe();

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
        fleetData.setDriverId("123456");
        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void test_002_VoyagerFleet_SaleEMV_cert() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        track = TestCards.VoyagerSwipe(EntryMethod.ContactEMV);
     //   track.setPinBlock("78FBB9DAEEB14E5A");

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Regular, UnitOfMeasure.Gallons, 10.34, 1.132);
        productData.addFuel(NtsProductCode.Plus, UnitOfMeasure.Gallons, 10.24, 1.259);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setDriverId("123456");
        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .withTagData(emvTagData)
                .withCardSequenceNumber("123")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void test_002_VoyagerFleet_SaleEMV_Fallback_cert() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        track = TestCards.VoyagerSwipe(EntryMethod.Swipe);
      //  track.setPinBlock("78FBB9DAEEB14E5A");

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Regular, UnitOfMeasure.Gallons, 10.34, 1.132);
        productData.addFuel(NtsProductCode.Plus, UnitOfMeasure.Gallons, 10.24, 1.259);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setDriverId("123456");
        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .withCardSequenceNumber("123")
                .withModifier(TransactionModifier.Fallback)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void test_002_VoyagerFleet_Auth_Fallback() throws ApiException {
        track = TestCards.VoyagerSwipe(EntryMethod.Swipe);
        track.setPinBlock("78FBB9DAEEB14E5A   ");

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("48000");
        fleetData.setDriverId("123456");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withCardSequenceNumber("123")
                .withFleetData(fleetData)
                .withModifier(TransactionModifier.Fallback)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void test_VisaFleetTwoPointO_auth_purchaseRestrictionFlag_noRestriction() throws ApiException {
        //flag 10 representing the VF 2.0 support
        acceptorConfig.setCapableVisaFleetTwoPointO(true);

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
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 09.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 2, 10.74);
        productData.addNonFuel(NtsProductCode.Oil, UnitOfMeasure.NoFuelPurchased, 3, 11.74);
        productData.addNonFuel(NtsProductCode.Tires, UnitOfMeasure.NoFuelPurchased, 1, 12.74);
        productData.addNonFuel(NtsProductCode.EngineSvc, UnitOfMeasure.NoFuelPurchased, 2, 13.74);
        productData.addNonFuel(NtsProductCode.OilChange, UnitOfMeasure.NoFuelPurchased, 3, 14.74);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 2, 15.74);
        productData.addNonFuel(NtsProductCode.Repairs, UnitOfMeasure.NoFuelPurchased, 1, 16.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetFuelAmount(new BigDecimal(10));
        productData.setNetNonFuelAmount(new BigDecimal(10));

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withNtsProductData(productData)
                .withPurchaseRestrictionFlag(PurchaseRestrictionFlag.NoRestriction)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_VisaFleetTwoPointO_auth_purchaseRestrictionFlag_chipBased() throws ApiException {
        //flag 10 representing the VF 2.0 support
        acceptorConfig.setCapableVisaFleetTwoPointO(true);

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
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 09.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 2, 10.74);
        productData.addNonFuel(NtsProductCode.Oil, UnitOfMeasure.NoFuelPurchased, 3, 11.74);
        productData.addNonFuel(NtsProductCode.Tires, UnitOfMeasure.NoFuelPurchased, 1, 12.74);
        productData.addNonFuel(NtsProductCode.EngineSvc, UnitOfMeasure.NoFuelPurchased, 2, 13.74);
        productData.addNonFuel(NtsProductCode.OilChange, UnitOfMeasure.NoFuelPurchased, 3, 14.74);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 2, 15.74);
        productData.addNonFuel(NtsProductCode.Repairs, UnitOfMeasure.NoFuelPurchased, 1, 16.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetFuelAmount(new BigDecimal(10));
        productData.setNetNonFuelAmount(new BigDecimal(10));

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withNtsProductData(productData)
                .withPurchaseRestrictionFlag(PurchaseRestrictionFlag.ChipBased)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void test_VisaFleetTwoPointO_auth_purchaseRestrictionFlag_hostBased() throws ApiException {
        //flag 10 representing the VF 2.0 support
        acceptorConfig.setCapableVisaFleetTwoPointO(true);

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
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 09.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 2, 10.74);
        productData.addNonFuel(NtsProductCode.Oil, UnitOfMeasure.NoFuelPurchased, 3, 11.74);
        productData.addNonFuel(NtsProductCode.Tires, UnitOfMeasure.NoFuelPurchased, 1, 12.74);
        productData.addNonFuel(NtsProductCode.EngineSvc, UnitOfMeasure.NoFuelPurchased, 2, 13.74);
        productData.addNonFuel(NtsProductCode.OilChange, UnitOfMeasure.NoFuelPurchased, 3, 14.74);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 2, 15.74);
        productData.addNonFuel(NtsProductCode.Repairs, UnitOfMeasure.NoFuelPurchased, 1, 16.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetFuelAmount(new BigDecimal(10));
        productData.setNetNonFuelAmount(new BigDecimal(10));

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withNtsProductData(productData)
                .withPurchaseRestrictionFlag(PurchaseRestrictionFlag.HostBased)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void test_VisaFleetTwoPointO_auth_purchaseRestrictionFlag_bothChipNHost() throws ApiException {
        //flag 10 representing the VF 2.0 support
        acceptorConfig.setCapableVisaFleetTwoPointO(true);

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
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 09.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 2, 10.74);
        productData.addNonFuel(NtsProductCode.Oil, UnitOfMeasure.NoFuelPurchased, 3, 11.74);
        productData.addNonFuel(NtsProductCode.Tires, UnitOfMeasure.NoFuelPurchased, 1, 12.74);
        productData.addNonFuel(NtsProductCode.EngineSvc, UnitOfMeasure.NoFuelPurchased, 2, 13.74);
        productData.addNonFuel(NtsProductCode.OilChange, UnitOfMeasure.NoFuelPurchased, 3, 14.74);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 2, 15.74);
        productData.addNonFuel(NtsProductCode.Repairs, UnitOfMeasure.NoFuelPurchased, 1, 16.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetFuelAmount(new BigDecimal(10));
        productData.setNetNonFuelAmount(new BigDecimal(10));

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withNtsProductData(productData)
                .withPurchaseRestrictionFlag(PurchaseRestrictionFlag.BothChipAndHostBased)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    // Negative scenario - Tag 2 flag 11 = N for this restriction should not applied.
    @Test
    public void test_VisaFleetTwoPointO_auth_purchaseRestrictionFlag_negative() throws ApiException {
        //flag 10 representing the VF 2.0 support
        acceptorConfig.setCapableVisaFleetTwoPointO(false);

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
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 09.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 2, 10.74);
        productData.addNonFuel(NtsProductCode.Oil, UnitOfMeasure.NoFuelPurchased, 3, 11.74);
        productData.addNonFuel(NtsProductCode.Tires, UnitOfMeasure.NoFuelPurchased, 1, 12.74);
        productData.addNonFuel(NtsProductCode.EngineSvc, UnitOfMeasure.NoFuelPurchased, 2, 13.74);
        productData.addNonFuel(NtsProductCode.OilChange, UnitOfMeasure.NoFuelPurchased, 3, 14.74);
        productData.addNonFuel(NtsProductCode.BrakeSvc, UnitOfMeasure.NoFuelPurchased, 2, 15.74);
        productData.addNonFuel(NtsProductCode.Repairs, UnitOfMeasure.NoFuelPurchased, 1, 16.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetFuelAmount(new BigDecimal(10));
        productData.setNetNonFuelAmount(new BigDecimal(10));

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withNtsProductData(productData)
                .withPurchaseRestrictionFlag(PurchaseRestrictionFlag.NoRestriction)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void test_DataCollect_Individual_CodeCoverageOnly() throws ApiException {
        track.setEntryMethod(null);
        Transaction transaction = Transaction.fromBuilder()
                .withPaymentMethod(track)
                .withApprovalCode("00")
                .withAuthorizationCode("00")
                .withOriginalTransactionDate("0727")
                .withTransactionTime("090540")
                .withOriginalMessageCode("01")
                .withBatchNumber(1)
                .withSequenceNumber(70)
                .withAuthorizer(AuthorizerCode.Interchange_Authorized)
                .build();

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .execute();
        assertNotNull(dataCollectResponse);

    }
    @Test
    public void test_masterCardFleet_DataCollect_fleetData_withoutValue_codeCoverage() throws ApiException{
        track = new CreditTrackData();
        track.setValue(";5567300000000016=25121019999888877711?");
        track.setEntryMethod(EntryMethod.Swipe);

        FleetData fleetData = new FleetData();
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withFleetData(fleetData)
                .withCvn("123")
                .execute();
        assertNotNull(response);

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
    @Test
    public void test_VisaFleetTwoPointO_authAndDataCollect_fleetData_withoutValue_codeCoverage() throws ApiException {
        //flag 10 representing the VF 2.0 support
        acceptorConfig.setCapableVisaFleetTwoPointO(true);

        track = NtsTestCards.VisaFleetTwoPointO(EntryMethod.ContactEMV);
        FleetData fleetData = new FleetData();

        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons, 10.24, 2.899);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 09.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 2, 10.74);
        productData.addNonFuel(NtsProductCode.Candy, UnitOfMeasure.NoFuelPurchased, 1, 20);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.setNonFuelProductCount("3");
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetFuelAmount(new BigDecimal(10));
        productData.setNetNonFuelAmount(new BigDecimal(10));

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withPurchaseRestrictionFlag(PurchaseRestrictionFlag.NoRestriction)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

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
    public void test_VisaFleetTwoPointO_auth_noFuelPurchased_codeCoverage() throws ApiException {
        acceptorConfig.setCapableVisaFleetTwoPointO(true);

        track = NtsTestCards.VisaFleetTwoPointO(EntryMethod.ContactEMV);
        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("98765");
        fleetData.setVehicleNumber("56789");

        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 09.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 2, 10.74);
        productData.addNonFuel(NtsProductCode.Candy, UnitOfMeasure.NoFuelPurchased, 1, 20);
        productData.setPurchaseType(PurchaseType.NonFuel);
        productData.setNonFuelProductCount("3");
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetFuelAmount(new BigDecimal(0));
        productData.setNetNonFuelAmount(new BigDecimal(10));

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withPurchaseRestrictionFlag(PurchaseRestrictionFlag.NoRestriction)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_VisaFleet_DataCollect_with0NonFuelProduct() throws ApiException {
        //flag 10 representing the VF 2.0 support
        acceptorConfig.setCapableVisaFleetTwoPointO(true);

        track = NtsTestCards.VisaFleetTwoPointO(EntryMethod.ContactEMV);
        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("98765");
        fleetData.setDriverId("800900");

        //product data for tag 9 only fuel item
        NtsProductData productData = new NtsProductData(ServiceLevel.NonFuelTransaction, track);
        productData.setPurchaseType(PurchaseType.NonFuel);
        productData.setNetNonFuelAmount(new BigDecimal(0));

        Transaction transaction = Transaction.fromBuilder()
                .withAuthorizer(AuthorizerCode.Interchange_Authorized)
                .withPaymentMethod(track)
                .withDebitAuthorizer("00")
                .withApprovalCode("      ")
                .withAuthorizationCode("01202A")
                .withOriginalTransactionDate("0122")
                .withTransactionTime("134408")
                .withOriginalMessageCode("01")
                .withBatchNumber(1)
                .withSequenceNumber(5)
                .build();

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_VisaFleetTwoPointO_authorizationAndDataCollect_with_0nonFuel() throws ApiException {
        //flag 10 representing the VF 2.0 support
        acceptorConfig.setCapableVisaFleetTwoPointO(true);

        track = NtsTestCards.VisaFleetTwoPointO(EntryMethod.ContactEMV);
        FleetData fleetData = new FleetData();
        // tag 43
        fleetData.setTrailerNumber("12345");
        fleetData.setWorkOrderPoNumber("543210");
        fleetData.setEmployeeNumber("G12345");
        fleetData.setAdditionalPromptData1("567891");
        fleetData.setAdditionalPromptData2("198765");

        //tag 8
        fleetData.setOdometerReading("98765");
        fleetData.setGenericIdentificationNo("800781");


        //product data for tag 9
        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons, 10.24, 2.899);
        productData.addFuel(NtsProductCode.Diesel2, UnitOfMeasure.Gallons, 20.24, 1.259);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        productData.setNetFuelAmount(new BigDecimal(10));
        productData.setNetNonFuelAmount(new BigDecimal(0));

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withPurchaseRestrictionFlag(PurchaseRestrictionFlag.ChipBased)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

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
    public void test_Voyager_Fleet_EMV_auth_track2_amount_expansion_10347() throws ApiException {
        track = new CreditTrackData();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.ContactEMV);

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setDriverId("3333");
        fleetData.setVehicleNumber("12345");
        fleetData.setIdNumber("5555");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    //negative scenario
    @Test
    public void test_003_Voyager_Fleet_EMV_auth_track2_amount_expansion_10347() throws ApiException {
        track = new CreditTrackData();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.ContactEMV);

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("");
        fleetData.setDriverId("");
        fleetData.setVehicleNumber("");
        fleetData.setIdNumber("");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_VoyagerFleetEMV_Datacollect_10348() throws ApiException{
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        track = TestCards.VoyagerSwipe();
        track.setEntryMethod(EntryMethod.ContactEMV);

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("12345");
        fleetData.setDriverId("12345");
        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Plus, UnitOfMeasure.Gallons, 10.34, 1.238);
        Transaction transaction = Transaction.fromBuilder()
                .withAuthorizer(AuthorizerCode.Interchange_Authorized)
                .withPaymentMethod(track)
                .withDebitAuthorizer("00")
                .withApprovalCode("      ")
                .withAuthorizationCode("000616")
                .withOriginalTransactionDate("0129")
                .withTransactionTime("153044")
                .withOriginalMessageCode("01")
                .withBatchNumber(1)
                .withSequenceNumber(5)
                .build();

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(dataCollectResponse);
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test //working
    public void test_VisaFleet_DataCollect_10360() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        track = new CreditTrackData();
        track.setValue(";4484630000000126=25121011000062111401?");
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons, 10.24, 2.899);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Dairy, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.setPurchaseType(PurchaseType.Fuel);
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));


        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

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

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    //-ve scenarios
    @Test //working
    public void test_VisaFleet_DataCollect_10360_1() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        track = new CreditTrackData();
        track.setValue(";4484630000000126=25121011000062111401?");
        track.setEntryMethod(EntryMethod.ContactEMV);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons, 10.24, 2.899);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Dairy, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.setPurchaseType(PurchaseType.NonFuel);
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));


        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .withNtsTag16(tag)
                .withTagData(emvTagData)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

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

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }



}
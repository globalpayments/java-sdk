package com.global.api.tests.network.nts;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.network.entities.NtsProductData;
import com.global.api.network.entities.NtsTag16;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.*;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.NtsTestCards;
import com.global.api.tests.testdata.TestCards;
import org.joda.time.DateTime;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Nts3DESCreditEncryptionTests {
    private CreditCardData card;
    private CreditCardData cardWithCvn;
    private CreditTrackData track;
    private CreditTrackData track2;
    private DebitTrackData debit;
    private NtsRequestMessageHeader header; //Main Request header class
    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;
    private NtsProductData productData;
    private NtsTag16 tag;
    private GiftCard giftCard;
    private PriorMessageInformation priorMessageInformation;
    Integer Stan = Integer.parseInt(DateTime.now().toString("hhmmss"));

    public Nts3DESCreditEncryptionTests() throws ConfigurationException {

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

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        acceptorConfig.setSupportedEncryptionType(EncryptionType.TDES);
        acceptorConfig.setServiceType(ServiceType.GPN_API);
        acceptorConfig.setOperationType(OperationType.Decrypt);

        header = new NtsRequestMessageHeader();
        header.setTerminalDestinationTag("510");
        header.setPinIndicator(PinIndicator.NotPromptedPin);
        header.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        priorMessageInformation =new PriorMessageInformation();
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
        config.setInputCapabilityCode(CardDataInputCapability.ContactEmv_MagStripe);
        config.setTerminalId("21");
        config.setUnitNumber("00001234567");
        config.setSoftwareVersion("01");
        config.setCompanyId("045");
        config.setLogicProcessFlag(LogicProcessFlag.Capable);
        config.setTerminalType(TerminalType.VerifoneRuby2Ci);
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        //2nd
        track = new CreditTrackData();
        track.setEntryMethod(EntryMethod.Swipe);
        track.setEncryptionData(EncryptionData.setKSNAndEncryptedData("EC7EB2F7BD67A2784F1AD9270EFFD90DD121B8653623911C6BC7B427F726A49F834CA051A6C1CC9CBB17910A1DBA209796BB6D08B8C374A2912AB018A679FA5A15B6FB3D21191BA5",
                "F000019990E00003"));
        track.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E17401487FC0B377F");
        track.setCardType("MC");
        track.setTrackNumber(TrackNumber.TrackOne);
        track.setExpiry("2512");

        track2 = new CreditTrackData();
        track2.setEntryMethod(EntryMethod.Swipe);
        track2.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3EC0C41AB0CCC3BCA6EF798140BEF7BB5A06F78222AFD7BA8E949CA21AAF26E33B97D7493E4D7C1A",
                "F000019990E00003"));
        track2.setCardType("MC");
        track2.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E17401487FC0B377F");
        track2.setExpiry("2512");
        track2.setEntryMethod(EntryMethod.Swipe);
        track2.setTrackNumber(TrackNumber.TrackTwo);

        // Mastercard
        card = new CreditCardData();
        card.setCardPresent(true);
        card.setReaderPresent(true);
        card.setEncryptionData(EncryptionData.setKSNAndEncryptedData("49AB0D7DF39F4EAA3ADEB107CCCC03D0",
                "F000019990E00003"));
        card.setCardType("Visa");
        card.setCvn("123");
        card.setExpMonth(12);
        card.setExpYear(2025);

        cardWithCvn = new CreditCardData();
        cardWithCvn.setCvn("103");
        cardWithCvn.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3A2067D00508DBE43E3342CC77B0575E",
                "F000019990E00003"));
        cardWithCvn.setCardType("MC");
        cardWithCvn.setCardPresent(false);
        cardWithCvn.setReaderPresent(false);

        // DEBIT
        debit = new DebitTrackData();
        debit.setPinBlock("62968D2481D231E1A504010024A00014");
        debit.setEncryptionData(EncryptionData.setKSNAndEncryptedData("E6699A44C3EE9E3AA75F9DF958C27469730C10D2929869F3704CC790CCB0AFDCDDE47F392E0D50E7",
                "3D3F820E00003"));
        debit.setCardType("PINDebitCard");
        debit.setPinBlock("62968D2481D231E1A504010024A00014");
        debit.setTrackNumber(TrackNumber.TrackTwo);
    }
    private NtsProductData getProductDataForNonFleetBankCards(IPaymentMethod method) throws ApiException {
        productData = new NtsProductData(ServiceLevel.FullServe, method);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons,1.24, 1.259);
        productData.addFuel(NtsProductCode.Diesel2, UnitOfMeasure.Gallons,2.24, 1.259);
        productData.addNonFuel(NtsProductCode.Batteries,UnitOfMeasure.NoFuelPurchased,1,1.74);
        productData.addNonFuel(NtsProductCode.CarWash,UnitOfMeasure.NoFuelPurchased,1,1.74);
        productData.addNonFuel(NtsProductCode.Dairy,UnitOfMeasure.NoFuelPurchased,1,1.74);
        productData.addNonFuel(NtsProductCode.Candy,UnitOfMeasure.NoFuelPurchased,1,1.74);
        productData.addNonFuel(NtsProductCode.Milk,UnitOfMeasure.NoFuelPurchased,1,1.74);
        productData.addNonFuel(NtsProductCode.OilChange,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal("32.33"),new BigDecimal(0));
        return productData;
    }

    //-----------------------------------------------Credit-------------------------------------------
    @Test
    public void test_001_sales_without_track() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        productData = getProductDataForNonFleetBankCards(card);

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withNtsProductData(productData)
                //.withCvn("123")
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void test_002_sales_with_track1() throws ApiException {
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        productData = getProductDataForNonFleetBankCards(track);

       // track = NtsTestCards.MasterCardTrack1(EntryMethod.Swipe);
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void test_003_sales_with_track2() throws ApiException {
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        productData = getProductDataForNonFleetBankCards(track2);

        Transaction response = track2.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_auth_004_card_authorization() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void test_auth_005_track1_authorization() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }
    @Test
    public void test_auth_006_track2_authorization() throws ApiException {
        Transaction response = track2.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    @Test //working
    public void test_007_auth_capture_card() throws ApiException {

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        header.setPinIndicator(PinIndicator.WithoutPin);
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(card))
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    @Test //working
    public void test_008_auth_capture_track() throws ApiException {

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        header.setPinIndicator(PinIndicator.WithoutPin);
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    @Test //working
    public void test_009_auth_capture_track2() throws ApiException {
        track2.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E");

        Transaction response = track2.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        header.setPinIndicator(PinIndicator.WithoutPin);
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track2))
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    @Test
    public void test_010_sales_credit_adjustment_with_card() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsProductData(getProductDataForNonFleetBankCards(card))
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);

        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        header.setPinIndicator(PinIndicator.WithoutPin);
        header.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(card))
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    @Test
    public void test_011_sales_credit_adjustment_with_track1() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);

        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        header.setPinIndicator(PinIndicator.WithoutPin);
        header.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    @Test
    public void test_012_sales_credit_adjustment_with_track2() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction response = track2.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsProductData(getProductDataForNonFleetBankCards(track2))
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);

        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        header.setPinIndicator(PinIndicator.WithoutPin);
        header.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    @Test
    public void test_013_credit_sales_void_mc_card() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);



        productData = getProductDataForNonFleetBankCards(card);

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withNtsProductData(productData)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }
    @Test
    public void test_014_credit_sales_void_mc_track1() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        productData = getProductDataForNonFleetBankCards(track);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withNtsProductData(productData)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }
    @Test
    public void test_015_credit_sales_void_mc_track2() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        productData = getProductDataForNonFleetBankCards(track2);

        Transaction response = track2.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withNtsProductData(productData)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }
    @Test // Working
    public void test_016_credit_sales_reverse_mc_card() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        productData = getProductDataForNonFleetBankCards(card);

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withNtsProductData(productData)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);


        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }
    @Test // Working
    public void test_017_credit_sales_reverse_mc_track1() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        productData = getProductDataForNonFleetBankCards(track);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withNtsProductData(productData)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);


        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }
    @Test // Working
    public void test_018_credit_sales_reverse_mc_track2() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        productData = getProductDataForNonFleetBankCards(track2);

        Transaction response = track2.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withNtsProductData(productData)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);


        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }
    @Test
    public void test_SVS_active_001() throws ApiException {
        giftCard = TestCards.SvsSwipe();
        Transaction response = giftCard.activate(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

    }
    @Test
    public void test_SVS_Pre_Authorization_008() throws ApiException {
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.Attended);
        giftCard = TestCards.SvsSwipe();
        Transaction response = giftCard.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

    }

    @Test //working
    public void test_15_certv221_auth_capture_track() throws ApiException {
        track.setCardType("MC");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
//                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test //working
    public void test_16_certv221_auth_capture_card() throws ApiException {
        track2.setCardType("MC");

        Transaction response = track2.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
//                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        header.setPinIndicator(PinIndicator.WithoutPin);
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track2.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E");
        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track2))
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
//                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_17_certv221_visa_sales_with_track2() throws ApiException {
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        productData = getProductDataForNonFleetBankCards(track2);

        Transaction response = track2.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_18_Auth_Approval_MC_CreditAdjustment() throws ApiException {
        track = NtsTestCards.MasterCardTrack1(EntryMethod.Swipe);
        track.setEncryptionData(EncryptionData.setKSNAndEncryptedData("EC7EB2F7BD67A2784F1AD9270EFFD90DD121B8653623911C6BC7B427F726A49F834CA051A6C1CC9CBB17910A1DBA209796BB6D08B8C374A2912AB018A679FA5A15B6FB3D21191BA5",
                "F000019990E00003"));
        track.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E");

        // Data-Collect request preparation.
        Transaction transaction = Transaction.fromNetwork(
                AuthorizerCode.Host_Authorized,
                "TYPE04",
                "08",
                "0208",
                "051226",
                track
        );

        header.setPinIndicator(PinIndicator.WithoutPin);
        header.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(productData)
                .withNtsRequestMessageHeader(header)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_18_sales_credit_adjustment_with_track2() throws ApiException {
        track2.setCardType("MC");

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        productData = getProductDataForNonFleetBankCards(track2);

        Transaction response = track2.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withCvn("123")
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        track2.setEncryptionData(EncryptionData.setKSNAndEncryptedData("49AB0D7DF39F4EAA3ADEB107CCCC03D0",
                "F000019990E00003"));
        header.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track2))
                .withNtsRequestMessageHeader(header)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test //working
    public void test_19_certv221_auth_capture_card() throws ApiException {
        track.setCardType("MC");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        header.setNtsMessageCode(NtsMessageCode.CombinedAuthorizationOrDataCollect);

        productData = getProductDataForNonFleetBankCards(track);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(header)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test //working
    public void test_20_certv221_discover_doubledatacollect_card() throws ApiException {
        track.setCardType("MC");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        header.setNtsMessageCode(NtsMessageCode.DoubleDataCollect);

        productData = getProductDataForNonFleetBankCards(track);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(header)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test21_credit_sales_void_mc_card() throws ApiException {


        track = NtsTestCards.MasterCardTrack1(EntryMethod.Swipe);
        track.setEncryptionData(EncryptionData.setKSNAndEncryptedData("EC7EB2F7BD67A2784F1AD9270EFFD90DD121B8653623911C6BC7B427F726A49F834CA051A6C1CC9CBB17910A1DBA209796BB6D08B8C374A2912AB018A679FA5A15B6FB3D21191BA5",
                "F000019990E00003"));

        TransactionReference transactionReference = new TransactionReference();
        transactionReference.setSystemTraceAuditNumber("1234");
        transactionReference.setMastercardBanknetRefNo("12322");
        transactionReference.setMastercardBanknetSettlementDate("02012024");

        Transaction transaction = Transaction.fromBuilder()
                .withPaymentMethod(track)
                .withDebitAuthorizer("00")
                .withApprovalCode("00")
                .withAuthorizationCode("00")
                .withOriginalTransactionDate("0202")
                .withTransactionTime("073148")
                .withOriginalMessageCode("02")
                .withBatchNumber(1)
                .withSequenceNumber(70)
                .withSystemTraceAuditNumber("123456")
                .withBanknetRefId("123456789")
                .withSettlementDate("0202")
                .build();


        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);
        Transaction voidResponse = transaction.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }


    @Test
    public void test23_credit_sales_reversal_mc_card() throws ApiException {

        track = NtsTestCards.VisaTrack1(EntryMethod.Swipe);
        track.setEncryptionData(EncryptionData.setKSNAndEncryptedData("EC7EB2F7BD67A2784F1AD9270EFFD90DD121B8653623911C6BC7B427F726A49F834CA051A6C1CC9CBB17910A1DBA209796BB6D08B8C374A2912AB018A679FA5A15B6FB3D21191BA5",
                "F000019990E00003"));

        Transaction transaction = Transaction.fromBuilder()
                .withPaymentMethod(track)
                .withDebitAuthorizer("00")
                .withApprovalCode("00")
                .withAuthorizationCode("38")
                .withOriginalTransactionDate("0202")
                .withTransactionTime("104959")
                .withOriginalMessageCode("02")
                .withBatchNumber(1)
                .withSequenceNumber(70)
                .withSystemTraceAuditNumber("123456")
                .withBanknetRefId("123456789")
                .withSettlementDate("0202")
                .build();


        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);
        Transaction voidResponse = transaction.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }


    @Test
    public void test22_credit_sales_void_mc_card() throws ApiException {
        track = NtsTestCards.MasterCardTrack1(EntryMethod.Swipe);
        track.setEncryptionData(EncryptionData.setKSNAndEncryptedData("EC7EB2F7BD67A2784F1AD9270EFFD90DD121B8653623911C6BC7B427F726A49F834CA051A6C1CC9CBB17910A1DBA209796BB6D08B8C374A2912AB018A679FA5A15B6FB3D21191BA5",
                "F000019990E00003"));
        track.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E");


        Transaction transaction = Transaction.fromBuilder()
                .withPaymentMethod(track)
                .withDebitAuthorizer("00")
                .withApprovalCode("00")
                .withAuthorizationCode("00")
                .withOriginalTransactionDate("0208")
                .withTransactionTime("052136")
                .withOriginalMessageCode("02")
                .withBatchNumber(1)
                .withSequenceNumber(70)
                .withSystemTraceAuditNumber("123456")
                .withBanknetRefId("123456789")
                .withSettlementDate("0208")
                .build();


        header.setNtsMessageCode(NtsMessageCode.ForceReversalOrForceVoid);
        Transaction voidResponse = transaction.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }


    @Test
    public void test24_credit_sales_reversal_mc_card() throws ApiException {

        track = NtsTestCards.MasterCardTrack1(EntryMethod.Swipe);


        track.setEncryptionData(EncryptionData.setKSNAndEncryptedData("EC7EB2F7BD67A2784F1AD9270EFFD90DD121B8653623911C6BC7B427F726A49F834CA051A6C1CC9CBB17910A1DBA209796BB6D08B8C374A2912AB018A679FA5A15B6FB3D21191BA5",
                "F000019990E00003"));
        track.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E");

        Transaction transaction = Transaction.fromBuilder()
                .withPaymentMethod(track)
                .withDebitAuthorizer("00")
                .withApprovalCode("00")
                .withAuthorizationCode("38")
                .withOriginalTransactionDate("0208")
                .withTransactionTime("062037")
                .withOriginalMessageCode("02")
                .withBatchNumber(1)
                .withSequenceNumber(70)
                .withSystemTraceAuditNumber("123456")
                .withBanknetRefId("123456789")
                .withSettlementDate("0208")
                .build();


        header.setNtsMessageCode(NtsMessageCode.ForceReversalOrForceVoid);
        Transaction voidResponse = transaction.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }

}
package com.global.api.tests.network.nts;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.EcommerceInfo;
import com.global.api.entities.StoredCredential;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.gateways.events.IGatewayEvent;
import com.global.api.gateways.events.IGatewayEventHandler;
import com.global.api.network.entities.NtsProductData;
import com.global.api.network.entities.NtsTag16;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.entities.nts.PriorMessageInfo;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.NetworkService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.NtsTestCards;
import com.global.api.tests.testdata.TestCards;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NtsCreditTest {
    private CreditCardData card;
    private CreditTrackData track;
    private NtsRequestMessageHeader header; //Main Request header class
    private NtsTag16 tag;
    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;
    private NtsProductData productData;
    private PriorMessageInfo priorMessageInfo;
    String emvTagData = "4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000";

    public NtsCreditTest() throws ConfigurationException {

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



        header = new NtsRequestMessageHeader();
        header.setTerminalDestinationTag("510");
        header.setPinIndicator(PinIndicator.NotPromptedPin);
        header.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        priorMessageInfo=new PriorMessageInfo();
        priorMessageInfo.setPriorMessageResponseTime(1);
        priorMessageInfo.setPriorMessageConnectTime(999);
        priorMessageInfo.setPriorMessageCode("01");

        header.setPriorMessageInfo(priorMessageInfo);

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
        config.setGatewayEventHandler(new IGatewayEventHandler() {
            @Override
            public void eventRaised(IGatewayEvent event) {
            }
        });
        //ServicesContainer.configureService(config);

        // with merchant type
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);
//        ServicesContainer.configureService(config, "ICR");

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

    /**
     * Implementation of Sale Request Format without Track Data
     * @throws ApiException
     */
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
                .withCvn("123")
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    /**
     * Implementation of Sale Request Format with Track 1 Data
     * @throws ApiException
     */
    @Test
    public void test_002_sales_with_track1() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        productData = getProductDataForNonFleetBankCards(track);

        track = NtsTestCards.MasterCardTrack1(EntryMethod.Swipe);

        Transaction response = track.charge(new BigDecimal(10))
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
    public void test_002_sales_with_track2() throws ApiException {
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        productData = getProductDataForNonFleetBankCards(track);

        track = NtsTestCards.MasterCardTrack2(EntryMethod.Swipe);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }


    /**
     * Implementation of Sale Request Format with Track 2 Data
     * @throws ApiException
     */
    @Test
    public void test_003_sales_with_track2_and_datacollect() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.MasterCardTrack2(EntryMethod.Swipe);

        productData = getProductDataForNonFleetBankCards(track);

        Transaction response = track.charge(new BigDecimal(90.90))
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

        Transaction transaction = Transaction.fromNetwork(
                response.getTransactionReference().getAuthorizer(),
                response.getTransactionReference().getApprovalCode(),
                response.getResponseCode(),
                response.getOriginalTransactionDate(),
                response.getOriginalTransactionTime(),
                track
        );

        header.setPinIndicator(PinIndicator.WithoutPin);
        header.setNtsMessageCode(NtsMessageCode.ForceCollectOrForceSale);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsProductData(productData)
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }


    /**
     * Implementation of EMV Sale Request Format with Track 2 Data,
     * Amount Expansion and User Data Expansion
     * @throws ApiException
     */
    @Test
    public void test_004_emv_with_track2() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.MasterCardTrack2(EntryMethod.Swipe);

        productData = getProductDataForNonFleetBankCards(track);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .withUniqueDeviceId("0001")
                .withNtsProductData(productData)
                .withEmvMaxPinEntry("20")
                .withPosSequenceNumber("001")  //only for the emv transaction
                .execute();
        assertNotNull(response);

        assertEquals("00", response.getResponseCode());
    }

    /**
     * Sale Offline Approved Advice with Track 2 Data
     * with Amount Expansion and User Data Expansion—EMV
     * @throws ApiException
     */
    @Test
    public void test_005_emv_offline_with_track2() throws ApiException {
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.MasterCardTrack2(EntryMethod.Swipe);

        productData = getProductDataForNonFleetBankCards(track);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0001")
                .withEmvMaxPinEntry("20")
                .withNtsProductData(productData)
                .withPosSequenceNumber("001")  //only for the emv transaction.
                .withModifier(TransactionModifier.Offline) // Only for the offline approved transactions.
                .withOfflineAuthCode("")
                .withNtsTag16(tag)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    /**
     * Sale Offline Approved Advice without Track
     * Data with Amount Expansion and User Data
     * Expansion—EMV
     * @throws ApiException
     */
    @Test
    @Ignore
    public void test_006_emv_offline_without_track() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        productData = getProductDataForNonFleetBankCards(card);


        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0001")
                .withEmvMaxPinEntry("20")
                .withNtsProductData(productData)
                .withPosSequenceNumber("001")  //only for the emv transaction.
                .withModifier(TransactionModifier.Offline) // Only for the offline approved transactions.
                .withOfflineAuthCode("")
                .withNtsTag16(tag)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    /**
     * Sale e-Commerce Request without Track Data
     * with Amount Expansion and User Data Expansion
     * @throws ApiException
     */
    @Test
    public void test_007_e_commerce_without_track() throws ApiException {
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        productData = getProductDataForNonFleetBankCards(card);

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0001")
                .withEmvMaxPinEntry("20")
                .withNtsProductData(productData)
                .withEcommerceInfo(new EcommerceInfo())
                .withStoredCredential(new StoredCredential())
                .withPosSequenceNumber("001")  //only for the emv transaction.
                .withModifier(TransactionModifier.Offline) // Only for the offline approved transactions.
                .withOfflineAuthCode("")
                .withNtsTag16(tag)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    /**
     * Authorization Request Format without Track
     * Data with Amount Expansion
     * @throws ApiException
     */
    @Test
    public void test_auth_001_without_track_amount_expansion() throws ApiException {
        card = TestCards.MasterCardManual(true, true);
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
//                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /**
     * Authorization Request Format with Track 1 Data
     * and Amount Expansion
     * @throws ApiException
     */
    @Test
    public void test_auth_002_track1_amount_expansion() throws ApiException {
        track = NtsTestCards.Visa2Track1(EntryMethod.Swipe);
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

    /**
     * Authorization Request Format with Track 2 Data
     * and Amount Expansion
     * @throws ApiException
     */
    @Test
    public void test_visa_auth_003_track2_amount_expansion() throws ApiException {

        track = NtsTestCards.VisaTrack2(EntryMethod.Swipe);

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

    /**
     * Authorization Request Format with Track 2
     * Data, Amount Expansion and User Data Expansion—
     * EMV
     * @throws ApiException
     */
    @Test
    public void test_mastercard_auth_004_track2_amount_expansion_emv() throws ApiException {
        track = NtsTestCards.MasterCardTrack2(EntryMethod.Swipe);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /**
     * Authorization Request Format with Track 2
     * Data, Amount Expansion and User Data Expansion—
     * EMV
     * @throws ApiException
     */
    @Test//not working
    @Ignore
    public void test_auth_005_track2_amount_expansion_emv_offline_decline() throws ApiException {

        track = NtsTestCards.MasterCardTrack2(EntryMethod.Swipe);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withModifier(TransactionModifier.ChipDecline) // Only for the offline decline transactions.
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);
       assertEquals("12", response.getResponseCode());
    }

    /**
     * Authorization Offline Decline Advice without
     * Track Data with Amount Expansion and User Data
     * Expansion—EMV
     * @throws ApiException
     */
    @Test//not woking
    @Ignore
    public void test_auth_006_without_track_amount_expansion_emv_offline_decline() throws ApiException {

        track = NtsTestCards.MasterCardTrack2(EntryMethod.Swipe);


        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withModifier(TransactionModifier.ChipDecline) // Only for the offline decline transactions.
                .withNtsTag16(tag)

                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("12", response.getResponseCode());
    }

    /**
     * Authorization e-Commerce without Track Data
     * with Amount Expansion and User Data Expansion
     * @throws ApiException
     */
    @Test
    @Ignore("This test case failling due to entry method is 'a'")
    public void test_auth_007_without_track_amount_expansion_e_commerce() throws ApiException {

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withEcommerceInfo(new EcommerceInfo())
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /**
     * Void Transactions
     */

    @Test
    public void test_001_credit_authorization_void_mc() throws ApiException {

        track = NtsTestCards.MasterCardTrack2(EntryMethod.Swipe);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);

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

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = transaction.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute();

        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test
    public void test_001_Velocity_authorization_void_mc() throws ApiException {

        track = NtsTestCards.MasterCardTrack2(EntryMethod.Swipe);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("16", response.getResponseCode());
        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute();

        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test
    public void test_001_credit_sales_void_mc() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        track = NtsTestCards.MasterCardTrack2(EntryMethod.Swipe);

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

    @Test // Not working
    @Ignore
    public void test_002_credit_authorization_void_emv_mc() throws ApiException {

        track = NtsTestCards.MasterCardTrack2(EntryMethod.Swipe);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withTagData(emvTagData)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .withCardSequenceNumber("123")
                .withEmvMaxPinEntry("04")
                .execute();

        assertNotNull(response);

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .withTagData(emvTagData)
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test // Not working
    @Ignore
    public void test_002_credit_sales_void_emv_mc() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.MasterCardTrack2(EntryMethod.Swipe);

        NtsTag16 tag = new NtsTag16();
        tag.setPumpNumber(1);
        tag.setWorkstationId(1);
        tag.setServiceCode(ServiceCode.Self);
        tag.setSecurityData(SecurityData.CVN);
        productData = getProductDataForNonFleetBankCards(track);


        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withTagData(emvTagData)
                .withNtsProductData(productData)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .withEmvMaxPinEntry("04")
                .execute();

        assertNotNull(response);

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);


        Transaction voidResponse = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .withTagData(emvTagData)
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test // Not working
    @Ignore
    public void test_002_credit_authorization_void_e_commerce_mc() throws ApiException {

       header.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        NtsTag16 tag = new NtsTag16();
        tag.setPumpNumber(1);
        tag.setWorkstationId(1);
        tag.setServiceCode(ServiceCode.Self);
        tag.setSecurityData(SecurityData.NoAVSAndNoCVN);

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withEcommerceInfo(new EcommerceInfo())
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .withEcommerceInfo(new EcommerceInfo())
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test // Not working
    @Ignore
    public void test_002_credit_sales_void_e_commerce_mc() throws ApiException {
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        productData = getProductDataForNonFleetBankCards(track);

        NtsTag16 tag = new NtsTag16();
        tag.setPumpNumber(1);
        tag.setWorkstationId(1);
        tag.setServiceCode(ServiceCode.Self);
        tag.setSecurityData(SecurityData.NoAVSAndNoCVN);

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0001")
                .withEmvMaxPinEntry("20")
                .withNtsProductData(productData)
                .withEcommerceInfo(new EcommerceInfo())
//                .withPosSequenceNumber("001")  //only for the emv transaction.
                .withModifier(TransactionModifier.Offline) // Only for the offline approved transactions.
                .withOfflineAuthCode("")
                .withNtsTag16(tag)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);


        Transaction voidResponse = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .withEcommerceInfo(new EcommerceInfo())
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }


    /**
     * Reversal Transaction.
     */


    @Test // Working
    public void test_001_credit_authorization_reverse_mc() throws ApiException {

        track = NtsTestCards.MasterCardTrack2(EntryMethod.Swipe);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
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
    public void test_002_credit_sales_reverse_mc() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.MasterCardTrack2(EntryMethod.Swipe);
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

    /*--------------------------------------Visa card------------------------------------------*/

    @Test //working
    public void test_visa_001_auth() throws ApiException {

        track = NtsTestCards.VisaTrack1(EntryMethod.Swipe);

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
    public void test_visa_auth_referral_processing() throws ApiException {

        track = NtsTestCards.VisaTrack1(EntryMethod.Swipe);

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
        Transaction transaction = Transaction.fromNetwork(
                AuthorizerCode.Voice_Authorized,
                "123456", // Set by the voice authorization
                response.getResponseCode(),
                response.getOriginalTransactionDate(),
                response.getOriginalTransactionTime(),
                track
        );

        header.setPinIndicator(PinIndicator.WithoutPin);
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
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
    public void test_visa_002_With_DataCollect() throws ApiException {

        // Updated Tag 16
        tag = new NtsTag16();
        tag.setPumpNumber(1);
        tag.setWorkstationId(1);
        tag.setServiceCode(ServiceCode.Self);
        tag.setSecurityData(SecurityData.NoAVSAndNoCVN);

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.VisaTrack2(EntryMethod.Swipe);

        // Updated Tag 09
        productData = getProductDataForNonFleetBankCards(track);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
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
                .withNtsProductData(productData)
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test //working
    public void test_visa_003_with_sales() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.VisaTrack1(EntryMethod.Swipe);
        productData = getProductDataForNonFleetBankCards(track);

        Transaction response = track.charge(BigDecimal.valueOf(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withNtsProductData(productData)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test // working
    public void test_004_credit_authorization_void_visa() throws ApiException {

        track = NtsTestCards.Visa2Track1(EntryMethod.Swipe);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

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
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test // working
    public void test_004_credit_sales_void_visa() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        //        track = TestCards.VisaSwipe(EntryMethod.Swipe);;
        track = NtsTestCards.Visa2Track1(EntryMethod.Swipe);
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

    @Test // Working
    public void test_003_credit_authorization_reverse_visa() throws ApiException {

        track = NtsTestCards.Visa2Track1(EntryMethod.Swipe);

        NtsTag16 tag = new NtsTag16();
        tag.setPumpNumber(1);
        tag.setWorkstationId(1);
        tag.setServiceCode(ServiceCode.Self);
        tag.setSecurityData(SecurityData.NoAVSAndNoCVN);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test // Working
    @Ignore
    public void test_004_credit_sales_reverse_visa() throws ApiException {

        track = NtsTestCards.Visa2Track1(EntryMethod.Swipe);


        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withCvn("123")
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);
        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test // Working
    public void test_004_credit_sales_reverse_visa_emv() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.Visa2Track1(EntryMethod.Swipe);
        productData = getProductDataForNonFleetBankCards(track);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withNtsProductData(productData)
                .withTagData(emvTagData)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .withEmvMaxPinEntry("04")
                .execute();

        assertNotNull(response);

        assertEquals("00", response.getResponseCode());

    }

    @Test // Working
    public void test_003_credit_authorization_reversal_emv_mc() throws ApiException {

        track = NtsTestCards.MasterCardTrack2(EntryMethod.Swipe);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withTagData(emvTagData)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .withCardSequenceNumber("123")
                .withEmvMaxPinEntry("04")
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
    public void test_004_credit_sales_reverse_emv_mc() throws ApiException {

         header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        track = NtsTestCards.MasterCardTrack2(EntryMethod.Swipe);

        NtsTag16 tag = new NtsTag16();
        tag.setPumpNumber(1);
        tag.setWorkstationId(1);
        tag.setServiceCode(ServiceCode.Self);
        tag.setSecurityData(SecurityData.CVN);

        productData = getProductDataForNonFleetBankCards(track);


        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withTagData(emvTagData)
                .withNtsProductData(productData)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .withEmvMaxPinEntry("04")
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
    @Ignore
    public void test_005_credit_authorization_reverse_e_commerce_mc() throws ApiException {

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withEcommerceInfo(new EcommerceInfo())
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .withEcommerceInfo(new EcommerceInfo())
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test // Working
    @Ignore
    public void test_006_credit_sales_reverse_e_commerce_mc() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        NtsTag16 tag = new NtsTag16();
        tag.setPumpNumber(1);
        tag.setWorkstationId(1);
        tag.setServiceCode(ServiceCode.Self);
        tag.setSecurityData(SecurityData.NoAVSAndNoCVN);

        productData = getProductDataForNonFleetBankCards(card);

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withNtsProductData(productData)
                .withEcommerceInfo(new EcommerceInfo())
//                .withStoredCredential(new StoredCredential())
                .withCvn("123")
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withEcommerceInfo(new EcommerceInfo())
//                .withStoredCredential(new StoredCredential())
                .withNtsRequestMessageHeader(header)
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }


   /**--------------******************Test Cases for Authorisation for Amex Credit Card---------*********/

    /**
     * Authorization Request Format without Track
     * Data with Amount Expansion
     * @throws ApiException
     */
    @Test
    public void test_Amex_auth_001_without_track_amount_expansion() throws ApiException {
        card = TestCards.AmexManual(true, true);
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withGoodsSold("1000")
//                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }


    @Test
    public void test_ICR_Auth_Approval_AmexCard_Credit() throws ApiException {
        track = new CreditTrackData();
        track.setValue("%B372700699251018^AMEX TEST CARD^2512990502700?"); // sample test track 1 data.
        track.setEntryMethod(EntryMethod.Swipe);

        Transaction authResponse = track.authorize(new BigDecimal(142))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute("ICR");
        assertNotNull(authResponse);

        // check response
        assertEquals("00", authResponse.getResponseCode());

        // Data-Collect request preparation.
        Transaction transaction = Transaction.fromNetwork(
                authResponse.getTransactionReference().getAuthorizer(),
                authResponse.getTransactionReference().getApprovalCode(),
                "08",
                authResponse.getOriginalTransactionDate(),
                authResponse.getOriginalTransactionTime(),
                track
        );

        header.setPinIndicator(PinIndicator.WithoutPin);
        header.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(productData)
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute("ICR");
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    /**
     * Authorization Request Format with Track 1 Data
     * and Amount Expansion
     * @throws ApiException
     */
    @Test
    public void test_Amex_auth_002_track1_amount_expansion() throws ApiException {
        track = NtsTestCards.AmexTrack1(EntryMethod.Swipe);
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withGoodsSold("1000")
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /**
     * Authorization Request Format with Track 2 Data
     * and Amount Expansion
     * @throws ApiException
     */
    @Test
    public void test_Amex_auth_003_track2_amount_expansion() throws ApiException {
        track = NtsTestCards.AmexTrack2(EntryMethod.Swipe);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withGoodsSold("1000")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /**
     * Authorization Request Format with Track 2
     * Data, Amount Expansion and User Data Expansion—
     * EMV
     * @throws ApiException
     */
    @Test
    public void test_Amex_auth_004_track2_amount_expansion_emv() throws ApiException {

        track = NtsTestCards.AmexTrack2(EntryMethod.Swipe);
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withGoodsSold("1000")
                .withNtsTag16(tag)
                .withEmvMaxPinEntry("04")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /**
     * Authorization Offline Decline Advice with Track
     * 2 Data, Amount Expansion and User Data
     * Expansion—EMV
     * @throws ApiException
     */
    @Test//not working
    @Ignore
    public void test_Amex_auth_005_track2_amount_expansion_emv_offline_decline() throws ApiException {
        track = NtsTestCards.AmexTrack2(EntryMethod.Swipe);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withModifier(TransactionModifier.ChipDecline) // Only for the offline decline transactions.
                .withNtsTag16(tag)
                .withGoodsSold("1000")
                .withCvn("123")
                .withEmvMaxPinEntry("04")
                .execute();

        assertNotNull(response);
        assertEquals("12", response.getResponseCode());
    }

    /**
     * Authorization Offline Decline Advice without
     * Track Data with Amount Expansion and User Data
     * Expansion—EMV
     * @throws ApiException
     */
    @Test//not working
    @Ignore
    public void test_Amex_auth_006_without_track_amount_expansion_emv_offline_decline() throws ApiException {
        track = NtsTestCards.AmexTrack2(EntryMethod.Swipe);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withModifier(TransactionModifier.ChipDecline) // Only for the offline decline transactions.
                .withNtsTag16(tag)
                .withGoodsSold("1000")
                .withEmvMaxPinEntry("04")
                .withCvn("123")
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("12", response.getResponseCode());
    }

    /**
     * Authorization e-Commerce without Track Data
     * with Amount Expansion and User Data Expansion
     * @throws ApiException
     */
    @Test
    @Ignore
    public void test_Amex_auth_007_without_track_amount_expansion_e_commerce() throws ApiException {
        card = TestCards.AmexManual();
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(header)
                .withEcommerceInfo(new EcommerceInfo())
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withGoodsSold("1000")
                .withCvn("123")
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }


/**----------------------Start Amex sales transaction -------------------*/


@Test
@Ignore
public void test_Amex_BalanceInquiry_without_track_amount_expansion() throws ApiException {

    card = TestCards.AmexManual(true, true);
    Transaction response = card.balanceInquiry()
            .withAmount(new BigDecimal("0"))
            .withCurrency("USD")
            .withNtsRequestMessageHeader(header)
            .withUniqueDeviceId("0102")
            .withServiceCode("111")
            .withGoodsSold("1000")
            .withNtsTag16(tag)
            .execute();

    assertNotNull(response);
    assertEquals("00", response.getResponseCode());
}

    @Test
    @Ignore
    public void test_Amex_BalanceInquiry_with_track2() throws ApiException {


        track = NtsTestCards.AmexTrack2(EntryMethod.Swipe);

        Transaction response = track.balanceInquiry()
                .withAmount(new BigDecimal("0"))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withServiceCode("111")
                .withGoodsSold("1000")
                .withNtsTag16(tag)
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }


    /**
     * Implementation of Sale Request Format without Track Data
     * @throws ApiException
     */
    @Test
    public void test_Amex_sales_001_without_track() throws ApiException {
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        card=TestCards.AmexManual();

        productData = getProductDataForNonFleetBankCards(card);

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withGoodsSold("1000")
                .withNtsProductData(productData)
                .withServiceCode("123")
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    /**
     * Implementation of Sale Request Format with Track 1 Data
     * @throws ApiException
     */
    @Test
    public void test_Amex_sales_002_with_track1() throws ApiException {
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.AmexTrack1(EntryMethod.Swipe);

        productData = getProductDataForNonFleetBankCards(track);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withGoodsSold("1000")
                .withNtsProductData(productData)
                .withCvn("123")
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    /**
     * Implementation of Sale Request Format with Track 2 Data
     * @throws ApiException
     */
    @Test
    public void test_Amex_sales_003_with_track2() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.AmexTrack2(EntryMethod.Swipe);

        productData = getProductDataForNonFleetBankCards(track);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withGoodsSold("1000")
                .withNtsProductData(productData)
                .withCvn("123")
                .execute();
        assertNotNull(response);

        assertEquals("00", response.getResponseCode());
    }


    /**
     * Implementation of EMV Sale Request Format with Track 2 Data,
     * Amount Expansion and User Data Expansion
     * @throws ApiException
     */
    @Test
    public void test_Amex_sales_004_emv_with_track2() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.AmexTrack2(EntryMethod.Swipe);

        productData = getProductDataForNonFleetBankCards(track);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .withNtsProductData(productData)
                .withUniqueDeviceId("0001")
                .withGoodsSold("1000")
                .withEmvMaxPinEntry("20")
                .withPosSequenceNumber("001")  //only for the emv transaction
                .execute();
        assertNotNull(response);

        assertEquals("00", response.getResponseCode());
    }

    /**
     * Sale Offline Approved Advice with Track 2 Data
     * with Amount Expansion and User Data Expansion—EMV
     * @throws ApiException
     */
    @Test
    public void test_Amex_sales_005_emv_offline_with_track2() throws ApiException {
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.AmexTrack2(EntryMethod.Swipe);

        productData = getProductDataForNonFleetBankCards(track);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0001")
                .withEmvMaxPinEntry("20")
                .withPosSequenceNumber("001")  //only for the emv transaction.
                .withModifier(TransactionModifier.Offline) // Only for the offline approved transactions.
                .withOfflineAuthCode("")
                .withNtsTag16(tag)
                .withGoodsSold("1000")
                .withNtsProductData(productData)
                .execute();
        assertNotNull(response);
        // check response
        assertEquals("00", response.getResponseCode());
    }

    /**
     * Sale Offline Approved Advice without Track
     * Data with Amount Expansion and User Data
     * Expansion—EMV
     * @throws ApiException
     */
    @Test
    @Ignore
    public void test_Amex_sales_006_emv_offline_without_track() throws ApiException {
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        productData = getProductDataForNonFleetBankCards(card);

        card=TestCards.AmexManual();

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0001")
                .withEmvMaxPinEntry("20")
                .withPosSequenceNumber("001")  //only for the emv transaction.
                .withModifier(TransactionModifier.Offline) // Only for the offline approved transactions.
                .withOfflineAuthCode("")
                .withGoodsSold("1000")
                .withNtsTag16(tag)
                .withNtsProductData(productData)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    /**
     * Sale e-Commerce Request without Track Data
     * with Amount Expansion and User Data Expansion
     * @throws ApiException
     */
    @Test
    @Ignore
    public void test_Amex_sales_007_e_commerce_without_track() throws ApiException {
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        card = TestCards.AmexManual();
        productData = getProductDataForNonFleetBankCards(card);

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0001")
                .withEmvMaxPinEntry("20")
                .withPosSequenceNumber("001")  //only for the emv transaction.
                .withModifier(TransactionModifier.Offline) // Only for the offline approved transactions.
                .withOfflineAuthCode("")
                .withEcommerceInfo(new EcommerceInfo())
                .withNtsTag16(tag)
                .withGoodsSold("1000")
                .withNtsProductData(productData)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    /* Test cases for DataCollect For Amex card*/

    @Test //working
    public void test_001_Amex_Purchase_With_DataCollect_02_With_UserData() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.AmexTrack1(EntryMethod.Swipe);

        Transaction chargeResponse = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        // check response
        assertEquals("00", chargeResponse.getResponseCode());

        // Data-Collect request preparation.
        header.setPinIndicator(PinIndicator.WithoutPin);
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = chargeResponse.capture(new BigDecimal(10))
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
    public void test_002_Amex_Purchase_With_DataCollect_03_With_UserData() throws ApiException {

        //track=TestCards.AmexSwipe(EntryMethod.Swipe);
        track=NtsTestCards.AmexTrack1(EntryMethod.Swipe);

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        productData = getProductDataForNonFleetBankCards(track);

        Transaction chargeResponse = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withNtsProductData(productData)
                .withCvn("123")
                .execute();

        // check response
        assertEquals("00", chargeResponse.getResponseCode());

        header.setPinIndicator(PinIndicator.WithoutPin);
        header.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction dataCollectResponse = chargeResponse.capture(new BigDecimal(10))
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
    public void test_003_Amex_Purchase_With_DataCollect_12_With_UserData() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.AmexTrack1(EntryMethod.Swipe);
        productData = getProductDataForNonFleetBankCards(track);

        Transaction chargeResponse = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withNtsProductData(productData)
                .withCvn("123")
                .execute();

        // check response
        assertEquals("00", chargeResponse.getResponseCode());

        // Data-Collect request preparation.
        header.setPinIndicator(PinIndicator.WithoutPin);
        header.setNtsMessageCode(NtsMessageCode.RetransmitDataCollect);

        Transaction dataCollectResponse = chargeResponse.capture(new BigDecimal(10))
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
    public void test_003_Amex_Purchase_With_DataCollect_13_With_UserData() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.AmexTrack1(EntryMethod.Swipe);

        Transaction chargeResponse = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withCvn("123")
                .execute();

        // check response
        assertEquals("00", chargeResponse.getResponseCode());


        header.setPinIndicator(PinIndicator.WithoutPin);
        header.setNtsMessageCode(NtsMessageCode.RetransmitCreditAdjustment);

        // Data-Collect request preparation.
        Transaction dataCollectResponse = chargeResponse.capture(new BigDecimal(10))
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
    public void test_003_Amex_Purchase_With_DataCollect_C2_With_UserData() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.AmexTrack1(EntryMethod.Swipe);

        Transaction chargeResponse = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        // check response
        assertEquals("00", chargeResponse.getResponseCode());

        header.setPinIndicator(PinIndicator.WithoutPin);
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        // Data-Collect request preparation.
        Transaction dataCollectResponse = chargeResponse.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00",dataCollectResponse.getResponseCode());
    }

    @Test //working
    public void test_003_Amex_Purchase_With_DataCollect_D2_With_UserData() throws ApiException {
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.AmexTrack1(EntryMethod.Swipe);

        Transaction chargeResponse = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        // check response
        assertEquals("00", chargeResponse.getResponseCode());

        // Data-Collect request preparation.
        header.setPinIndicator(PinIndicator.WithoutPin);
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = chargeResponse.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test // working
    public void test_Sale_Partial_Approval_AmexCard_Credit() throws ApiException
    {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.AmexTrack1(EntryMethod.Swipe);

        NtsTag16 tag = new NtsTag16();
        tag.setPumpNumber(1);
        tag.setWorkstationId(1);
        tag.setServiceCode(ServiceCode.Self);
        tag.setSecurityData(SecurityData.NoAVSAndNoCVN);

        Transaction response = track.charge(new BigDecimal(142))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withCvn("123")
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test
    @Ignore
    public void test_Partial_Approval_Balance_Inquiry_AmexCard_Credit() throws ApiException {

        track = NtsTestCards.AmexTrack1(EntryMethod.Swipe);

        Transaction response = track.balanceInquiry()
                .withNtsRequestMessageHeader(header)
                .withAmount(new BigDecimal(0))
                .withUniqueDeviceId("0001")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /** Amex Void Transaction **/
    @Test
    public void test_Amex_AuthVoid_001_credit() throws ApiException {

        track = NtsTestCards.AmexTrack2(EntryMethod.Swipe);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withGoodsSold("1000")
                .withCvn("123")
                .execute();

        assertNotNull(response);

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

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
                .build();



        Transaction voidResponse = transaction.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute();

        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test
    public void test_Amex_SalesVoid_002_credit() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.AmexTrack2(EntryMethod.Swipe);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
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


    /**
     * Amex Reversal Transaction.
     */


    @Test // Working
    public void test_Amex_AuthReversal_001_credit() throws ApiException {

        track = NtsTestCards.AmexTrack2(EntryMethod.Swipe);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .execute();
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test // Working
    public void test_Amex_saleReversal_001_credit() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.AmexTrack2(EntryMethod.Swipe);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .execute();
        assertEquals("00", voidResponse.getResponseCode());
    }
    /**---------------Discover Card Transaction-----------------------**/


    /**
     * Authorization Request Format without Track
     * Data with Amount Expansion
     * @throws ApiException
     */
    @Test
    public void test_Discover_auth_001_without_track_amount_expansion() throws ApiException {
        card = TestCards.DiscoverManual(true, true);
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

    /**
     * Authorization Request Format with Track 1 Data
     * and Amount Expansion
     * @throws ApiException
     */
    @Test
    public void test_Discover_auth_002_track1_amount_expansion() throws ApiException {
        track = TestCards.DiscoverSwipe(EntryMethod.Swipe);
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

    /**
     * Authorization Request Format with Track 2 Data
     * and Amount Expansion
     * @throws ApiException
     */
    @Test
    public void test_Discover_auth_003_track2_amount_expansion() throws ApiException {
        track = NtsTestCards.DiscoverTrack2(EntryMethod.Swipe);

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

    /**
     * Authorization Request Format with Track 2
     * Data, Amount Expansion and User Data Expansion—
     * EMV
     * @throws ApiException
     */
    @Test
    public void test_Discover_auth_004_track2_amount_expansion_emv() throws ApiException {

        track = NtsTestCards.DiscoverTrack2(EntryMethod.Swipe);
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withEmvMaxPinEntry("04")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /**
     * Authorization Offline Decline Advice with Track
     * 2 Data, Amount Expansion and User Data
     * Expansion—EMV
     * @throws ApiException
     */
    @Test//not working
    @Ignore
    public void test_Discover_auth_005_track2_amount_expansion_emv_offline_decline() throws ApiException {
        track = NtsTestCards.DiscoverTrack2(EntryMethod.Swipe);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withModifier(TransactionModifier.ChipDecline) // Only for the offline decline transactions.
                .withNtsTag16(tag)
                .withCvn("123")
                .withEmvMaxPinEntry("04")
                .execute();

        assertNotNull(response);
        assertEquals("12", response.getResponseCode());
    }

    /**
     * Authorization Offline Decline Advice without
     * Track Data with Amount Expansion and User Data
     * Expansion—EMV
     * @throws ApiException
     */
    @Test//not working
    @Ignore
    public void test_Discover_auth_006_without_track_amount_expansion_emv_offline_decline() throws ApiException {
        track = NtsTestCards.DiscoverTrack2(EntryMethod.Swipe);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withModifier(TransactionModifier.ChipDecline) // Only for the offline decline transactions.
                .withNtsTag16(tag)

                .withEmvMaxPinEntry("04")
                .withCvn("123")
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("12", response.getResponseCode());
    }

    /**
     * Authorization e-Commerce without Track Data
     * with Amount Expansion and User Data Expansion
     * @throws ApiException
     */
    @Test
    @Ignore
    public void test_Discover_auth_007_without_track_amount_expansion_e_commerce() throws ApiException {
        card = TestCards.DiscoverManual();

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withEcommerceAuthIndicator("S")
                .withEcommerceInfo(new EcommerceInfo())
                .withInvoiceNumber("123456712345671234567891")
                //.withEcommerceData2("123456789")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    /**
     * Implementation of Sale Request Format without Track Data
     * @throws ApiException
     */
    @Test
    public void test_Discover_sales_001_without_track() throws ApiException {
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        card=TestCards.DiscoverManual();

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withServiceCode("123")
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    /**
     * Implementation of Sale Request Format with Track 1 Data
     * @throws ApiException
     */
    @Test
    public void test_Discover_sales_002_with_track1() throws ApiException {
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.DiscoverTrack1(EntryMethod.Swipe);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    /**
     * Implementation of Sale Request Format with Track 2 Data
     * @throws ApiException
     */
    @Test
    public void test_Discover_sales_003_with_track2() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.DiscoverTrack2(EntryMethod.Swipe);

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


    /**
     * Implementation of EMV Sale Request Format with Track 2 Data,
     * Amount Expansion and User Data Expansion
     * @throws ApiException
     */
    @Test
    public void test_Discover_sales_004_emv_with_track2() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.DiscoverTrack2(EntryMethod.Swipe);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .withUniqueDeviceId("0001")
                .withEmvMaxPinEntry("20")
                .withPosSequenceNumber("001")  //only for the emv transaction
                .execute();
        assertNotNull(response);

        assertEquals("00", response.getResponseCode());
    }

    /** Discover Void Transaction **/

    @Test
    public void test_Discover_Void_001_credit_authorization() throws ApiException {

        track = NtsTestCards.DiscoverTrack2(EntryMethod.Swipe);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

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
                .withDiscoverNetworkRefId(response.getTransactionReference().getDiscoverNetworkRefId())
                .build();

        Transaction voidResponse = transaction.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute();

        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test
    @Ignore//doubt
    public void test_Discover_Void_002_track2_amount_expansion_emv() throws ApiException {
        track = NtsTestCards.DiscoverTrack2(EntryMethod.Swipe);

        Transaction response = track.authorize(new BigDecimal(142))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withEmvMaxPinEntry("04")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_Discover_SalesVoid_002_credit() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.DiscoverTrack2(EntryMethod.Swipe);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
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

    /**
     * Discover Reversal Transaction.
     */


    @Test // Working
    public void test_Discover_AuthReversal_001_credit() throws ApiException {

        track = NtsTestCards.DiscoverTrack2(EntryMethod.Swipe);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .execute();
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test // Working
    public void test_Discover_saleReversal_001_credit() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.DiscoverTrack2(EntryMethod.Swipe);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .execute();
        assertEquals("00", voidResponse.getResponseCode());
    }

    /**---------cash checkout-----------------**/
    @Test
    public void test_Discover_purchase_with_cashBack_Manual() throws ApiException {
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        card=TestCards.DiscoverManual();

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(3))
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_Discover_purchase_with_cashBack_Swipe() throws ApiException {
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.DiscoverTrack2(EntryMethod.Swipe);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(3))
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_NTS_ph1_Address_Issue() throws ApiException {
        track = NtsTestCards.AmexTrack1(EntryMethod.Swipe);
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withZipCode("12345")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_NTS_ph1_DateTimestamp_Issue() throws ApiException {
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        productData = getProductDataForNonFleetBankCards(track);

        track = NtsTestCards.MasterCardTrack1(EntryMethod.Swipe);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withNtsTag16(tag)
                .withTimestamp("221226221910")
                .withCvn("123")
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void test_DateTimeIssueNTS_10123() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        track = NtsTestCards.MasterCardTrack2(EntryMethod.Swipe);

        productData = getProductDataForNonFleetBankCards(track);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withNtsProductData(productData)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .withTimestamp("230308024510")
                .execute();

        assertNotNull(response);
        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .withTimestamp("230308024520")
                .withNtsTag16(tag)
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }
    @Test
    public void test_003_sales_with_track2_and_datacollect_resubmit() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.MasterCardTrack2(EntryMethod.Swipe);

        productData = getProductDataForNonFleetBankCards(track);

        Transaction response = track.charge(new BigDecimal(90.90))
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

        Transaction transaction = Transaction.fromNetwork(
                response.getTransactionReference().getAuthorizer(),
                response.getTransactionReference().getApprovalCode(),
                response.getResponseCode(),
                response.getOriginalTransactionDate(),
                response.getOriginalTransactionTime(),
                track
        );

        header.setPinIndicator(PinIndicator.WithoutPin);
        header.setNtsMessageCode(NtsMessageCode.ForceCollectOrForceSale);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsProductData(productData)
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());

        Transaction capture = NetworkService.resubmitDataCollect(dataCollectResponse.getTransactionToken())
                .execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }
}

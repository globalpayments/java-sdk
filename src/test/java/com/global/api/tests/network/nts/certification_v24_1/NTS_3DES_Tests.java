package com.global.api.tests.network.nts.certification_v24_1;

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
import org.joda.time.DateTime;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NTS_3DES_Tests {
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

    public NTS_3DES_Tests() throws ConfigurationException {

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
        config.setInputCapabilityCode(CardDataInputCapability.ContactEmv_MagStripe);
        config.setTerminalId("01");
        config.setUnitNumber("00012378911");
        config.setSoftwareVersion("01");
        config.setCompanyId("009");
        config.setLogicProcessFlag(LogicProcessFlag.Capable);
        config.setTerminalType(TerminalType.VerifoneRuby2Ci);
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        //2nd
        track = new CreditTrackData();
        track.setEntryMethod(EntryMethod.Swipe);
        track.setEncryptionData(EncryptionData.setKSNAndEncryptedData("EC7EB2F7BD67A2784F1AD9270EFFD90DD121B8653623911C6BC7B427F726A49F834CA051A6C1CC9CBB17910A1DBA209796BB6D08B8C374A2912AB018A679FA5A15B6FB3D21191BA5",
                "F000019990E00003"));
        track.setCardType("Amex");
        track.setTrackNumber(TrackNumber.TrackOne);

        track2 = new CreditTrackData();
        track2.setEntryMethod(EntryMethod.Swipe);
        track2.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3EC0C41AB0CCC3BCA6EF798140BEF7BB5A06F78222AFD7BA8E949CA21AAF26E33B97D7493E4D7C1A",
                "F000019990E00003"));
        track2.setCardType("MC");
        track2.setEncryptedPan("49AB0D7DF39F4EAA3ADEB107CCCC03D0");
        track2.setExpiry("2025");
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

        //prepaid card SVS account number
        giftCard = new GiftCard();
        giftCard.setCardType("StoredValue");
        giftCard.setEncryptionData(EncryptionData.setKSNAndEncryptedData("4FBB3A14C4D744F044CE5E56EAFEB19EC99A321B093237F", "F000019990E00003"));
        giftCard.setExpiry("1239");
    }

    private NtsProductData getProductDataForNonFleetBankCards(IPaymentMethod method) throws ApiException {
        productData = new NtsProductData(ServiceLevel.FullServe, method);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons, 1.24, 1.259);
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
    public void test_auth_005_visa_card_authorization() throws ApiException {
        card = new CreditCardData();
        card.setCardPresent(true);
        card.setReaderPresent(true);
        card.setEncryptionData(EncryptionData.setKSNAndEncryptedData("49AB0D7DF39F4EAA3ADEB107CCCC03D0",
                "F000019990E00003"));
        card.setCardType("Visa");
        card.setCvn("123");
        card.setExpMonth(12);
        card.setExpYear(2025);

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test //working
    public void test_006_3DES_visa_auth_capture_card() throws ApiException {
        card = new CreditCardData();
        card.setCardPresent(true);
        card.setReaderPresent(true);
        card.setEncryptionData(EncryptionData.setKSNAndEncryptedData("49AB0D7DF39F4EAA3ADEB107CCCC03D0",
                "F000019990E00003"));
        card.setCardType("Visa");
        card.setCvn("123");
        card.setExpMonth(12);
        card.setExpYear(2025);

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
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
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_007_3DES_MC_sales() throws ApiException {
        track2 = new CreditTrackData();
        track2.setEntryMethod(EntryMethod.Swipe);
        track2.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3EC0C41AB0CCC3BCA6EF798140BEF7BB5A06F78222AFD7BA8E949CA21AAF26E33B97D7493E4D7C1A",
                "F000019990E00003"));
        track2.setCardType("MC");
        track2.setEncryptedPan("49AB0D7DF39F4EAA3ADEB107CCCC03D0");
        track2.setExpiry("2025");
        track2.setEntryMethod(EntryMethod.Swipe);
        track2.setTrackNumber(TrackNumber.TrackTwo);

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

    }

    @Test
    public void test_008_discover_sales_credit_adjustment_with_card() throws ApiException {
        card = new CreditCardData();
        card.setCardPresent(true);
        card.setReaderPresent(true);
        card.setEncryptionData(EncryptionData.setKSNAndEncryptedData("49AB0D7DF39F4EAA3ADEB107CCCC03D0",
                "F000019990E00003"));
        card.setCardType("Discover");
        card.setCvn("123");
        card.setExpMonth(12);
        card.setExpYear(2025);

        Transaction transaction = Transaction.fromBuilder()
                .withPaymentMethod(card)
                .withDebitAuthorizer("00")
                .withApprovalCode("00")
                .withAuthorizationCode(" ")
                .withOriginalTransactionDate("0403")
                .withTransactionTime("135017")
                .withOriginalMessageCode("02")
                .withBatchNumber(1)
                .withSequenceNumber(25)
                .build();

        // Data-Collect request preparation.
        header.setPinIndicator(PinIndicator.WithoutPin);
        header.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(card))
                .withNtsRequestMessageHeader(header)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test //working
    public void test_009_3DES_Amex_auth_capture_card() throws ApiException {
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
        card = new CreditCardData();
        card.setCardPresent(true);
        card.setReaderPresent(true);
        card.setEncryptionData(EncryptionData.setKSNAndEncryptedData("49AB0D7DF39F4EAA3ADEB107CCCC03D0",
                "F000019990E00003"));
        card.setCardType("Discover");
        card.setCvn("123");
        card.setExpMonth(12);
        card.setExpYear(2025);

        Transaction transaction = Transaction.fromBuilder()
                .withPaymentMethod(card)
                .withAuthorizer(AuthorizerCode.Interchange_Authorized)
                .withApprovalCode("00")
                .withAuthorizationCode("38")
                .withOriginalTransactionDate("0404")
                .withTransactionTime("051211")
                .withOriginalMessageCode("01")
                .withBatchNumber(1)
                .withSequenceNumber(70)
                .withSystemTraceAuditNumber("123456")
                .withBanknetRefId("123456789")
                .withSettlementDate("0404")
                .build();


        // Data-Collect request preparation.
        header.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(card))
                .withNtsRequestMessageHeader(header)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test //working
    public void test_016_3DES_visa_auth_capture_card() throws ApiException {
        card = new CreditCardData();
        card.setCardPresent(true);
        card.setReaderPresent(true);
        card.setEncryptionData(EncryptionData.setKSNAndEncryptedData("49AB0D7DF39F4EAA3ADEB107CCCC03D0",
                "F000019990E00003"));
        card.setCardType("Visa");
        card.setCvn("123");
        card.setExpMonth(12);
        card.setExpYear(2025);

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        header.setPinIndicator(PinIndicator.WithoutPin);
        header.setNtsMessageCode(NtsMessageCode.DoubleDataCollect);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(card))
                .withNtsRequestMessageHeader(header)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test13_credit_sales_reversal_visa_card() throws ApiException {

        card = new CreditCardData();
        card.setCardPresent(true);
        card.setReaderPresent(true);
        card.setEncryptionData(EncryptionData.setKSNAndEncryptedData("49AB0D7DF39F4EAA3ADEB107CCCC03D0",
                "F000019990E00003"));
        card.setCardType("Visa");
        card.setCvn("123");
        card.setExpMonth(12);
        card.setExpYear(2025);

        Transaction transaction = Transaction.fromBuilder()
                .withPaymentMethod(card)
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
    public void test12_credit_sales_reversal_amex_card() throws ApiException {

        card = new CreditCardData();
        card.setCardPresent(true);
        card.setReaderPresent(true);
        card.setEncryptionData(EncryptionData.setKSNAndEncryptedData("49AB0D7DF39F4EAA3ADEB107CCCC03D0",
                "F000019990E00003"));
        card.setCardType("Amex");
        card.setCvn("123");
        card.setExpMonth(12);
        card.setExpYear(2025);

        Transaction transaction = Transaction.fromBuilder()
                .withPaymentMethod(card)
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
        Transaction voidResponse = transaction.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test
    public void test10_credit_doubledatacollect_discover_card() throws ApiException {


        card = new CreditCardData();
        card.setCardPresent(true);
        card.setReaderPresent(true);
        card.setEncryptionData(EncryptionData.setKSNAndEncryptedData("49AB0D7DF39F4EAA3ADEB107CCCC03D0",
                "F000019990E00003"));
        card.setCardType("Discover");
        card.setCvn("123");
        card.setExpMonth(12);
        card.setExpYear(2025);

        Transaction transaction = Transaction.fromBuilder()
                .withPaymentMethod(card)
                .withDebitAuthorizer("00")
                .withApprovalCode("00")
                .withAuthorizationCode("A")
                .withOriginalTransactionDate("0202")
                .withTransactionTime("104959")
                .withOriginalMessageCode("02")
                .withBatchNumber(1)
                .withSequenceNumber(70)
                .withSystemTraceAuditNumber("123456")
                .withBanknetRefId("123456789")
                .withSettlementDate("0202")
                .build();

        header.setNtsMessageCode(NtsMessageCode.DoubleDataCollect);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withNtsProductData(getProductDataForNonFleetBankCards(card))
                .withNtsRequestMessageHeader(header)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test23_credit_sales_reversal_mc_card() throws ApiException {

        track = NtsTestCards.VisaTrack1(EntryMethod.Swipe);
        track.setCardType("Discover");
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


        header.setNtsMessageCode(NtsMessageCode.CreditAdjustment);
        Transaction voidResponse = transaction.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test
    public void test23_credit_double_data_collect_discover_card() throws ApiException {

        track = NtsTestCards.VisaTrack1(EntryMethod.Swipe);
        track.setCardType("Discover");
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


        header.setNtsMessageCode(NtsMessageCode.DoubleDataCollect);
        Transaction voidResponse = transaction.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test//working
    public void test_SVS_pre_doubledatacolect_07() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DoubleDataCollect);

        Transaction transaction = Transaction.fromBuilder()
                .withPaymentMethod(giftCard)
                .withAuthorizer(AuthorizerCode.Interchange_Authorized)
                .withApprovalCode("00")
                .withAuthorizationCode("38")
                .withOriginalTransactionDate("0324")
                .withTransactionTime("104636")
                .withOriginalMessageCode("02")
                .withBatchNumber(1)
                .withSequenceNumber(70)
                .withSystemTraceAuditNumber("123456")
                .withBanknetRefId("123456789")
                .withSettlementDate("0202")
                .build();

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(productData)
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(Stan)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());

    }

    @Test
    public void ntscert24_1_SVS_active_cancellation_tc29() throws ApiException {
        giftCard = new GiftCard();
        giftCard.setEncryptionData(EncryptionData.setKtbAndKsn("D87A55F042D1DA9DAD3959DAAE8C3A423E27412D58669AA86993049F07662E478E75B439D9279790",
                "F000019990E00003"));
        giftCard.setEncryptedPan("49FE802FA87C5984BBE68AE7A3277200");
        giftCard.setTrackNumber(TrackNumber.TrackTwo);
        giftCard.setEntryMethod(EntryMethod.Swipe);
        giftCard.setCardType("StoredValue");
        giftCard.setExpiry("2501");

        Transaction transaction = Transaction.fromBuilder()
                .withPaymentMethod(giftCard)
                .withOriginalTransactionDate("0409")
                .withTransactionTime("045831")
                .build();
        transaction.getTransactionReference()
                .setOriginalTransactionTypeIndicator(TransactionTypeIndicator.CardActivation);

        header.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);
        Transaction response = transaction.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(32832)
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

    }

    @Test
    public void ntscert24_1_SVS_active_reversal_tc30() throws ApiException {
        giftCard = new GiftCard();
        giftCard.setEncryptionData(EncryptionData.setKtbAndKsn("D87A55F042D1DA9DAD3959DAAE8C3A423E27412D58669AA86993049F07662E478E75B439D9279790",
                "F000019990E00003"));
        giftCard.setEncryptedPan("49FE802FA87C5984BBE68AE7A3277200");
        giftCard.setTrackNumber(TrackNumber.TrackTwo);
        giftCard.setEntryMethod(EntryMethod.Swipe);
        giftCard.setCardType("StoredValue");
        giftCard.setExpiry("2501");

        Transaction transaction = Transaction.fromBuilder()
                .withPaymentMethod(giftCard)
                .withOriginalTransactionDate("0409")
                .withTransactionTime("045831")
                .build();
        transaction.getTransactionReference()
                .setOriginalTransactionTypeIndicator(TransactionTypeIndicator.CardActivation);

        header.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);
        Transaction response = transaction.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(32832)
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

    }

    @Test
    public void ntscert24_1_SVS_preauth_reversal_tc34() throws ApiException {
        giftCard = new GiftCard();
        giftCard.setEncryptionData(EncryptionData.setKtbAndKsn("D87A55F042D1DA9DAD3959DAAE8C3A423E27412D58669AA86993049F07662E478E75B439D9279790",
                "F000019990E00003"));
        giftCard.setEncryptedPan("49FE802FA87C5984BBE68AE7A3277200");
        giftCard.setTrackNumber(TrackNumber.TrackTwo);
        giftCard.setEntryMethod(EntryMethod.Swipe);
        giftCard.setCardType("StoredValue");
        giftCard.setExpiry("2501");

        Transaction transaction = Transaction.fromBuilder()
                .withPaymentMethod(giftCard)
                .withOriginalTransactionDate("0409")
                .withTransactionTime("050608")
                .build();
        transaction.getTransactionReference()
                .setOriginalTransactionTypeIndicator(TransactionTypeIndicator.PreAuthorization);

        header.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);
        Transaction response = transaction.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(33610)
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

    }

    @Test
    public void ntscert24_1_SVS_purchase_cancellation_tc36() throws ApiException {
        giftCard = new GiftCard();
        giftCard.setEncryptionData(EncryptionData.setKtbAndKsn("D87A55F042D1DA9DAD3959DAAE8C3A423E27412D58669AA86993049F07662E478E75B439D9279790",
                "F000019990E00003"));
        giftCard.setEncryptedPan("49FE802FA87C5984BBE68AE7A3277200");
        giftCard.setTrackNumber(TrackNumber.TrackTwo);
        giftCard.setEntryMethod(EntryMethod.Swipe);
        giftCard.setCardType("StoredValue");
        giftCard.setExpiry("2501");

        Transaction transaction = Transaction.fromBuilder()
                .withPaymentMethod(giftCard)
                .withOriginalTransactionDate("0409")
                .withTransactionTime("051546")
                .build();
        transaction.getTransactionReference()
                .setOriginalTransactionTypeIndicator(TransactionTypeIndicator.Purchase);

        header.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);
        Transaction response = transaction.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(34546)
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

    }

    @Test
    public void ntscert24_1_SVS_replenish_reversal_tc38() throws ApiException {
        giftCard = new GiftCard();
        giftCard.setEncryptionData(EncryptionData.setKtbAndKsn("D87A55F042D1DA9DAD3959DAAE8C3A423E27412D58669AA86993049F07662E478E75B439D9279790",
                "F000019990E00003"));
        giftCard.setEncryptedPan("49FE802FA87C5984BBE68AE7A3277200");
        giftCard.setTrackNumber(TrackNumber.TrackTwo);
        giftCard.setEntryMethod(EntryMethod.Swipe);
        giftCard.setCardType("StoredValue");
        giftCard.setExpiry("2501");

        Transaction transaction = Transaction.fromBuilder()
                .withPaymentMethod(giftCard)
                .withOriginalTransactionDate("0409")
                .withTransactionTime("033010")
                .build();
        transaction.getTransactionReference()
                .setOriginalTransactionTypeIndicator(TransactionTypeIndicator.RechargeCardBalance);

        header.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);
        Transaction response = transaction.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(40301)
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ntscert24_1_SVS_return_reversal_tc40() throws ApiException {
        giftCard = new GiftCard();
        giftCard.setEncryptionData(EncryptionData.setKtbAndKsn("D87A55F042D1DA9DAD3959DAAE8C3A423E27412D58669AA86993049F07662E478E75B439D9279790",
                "F000019990E00003"));
        giftCard.setEncryptedPan("49FE802FA87C5984BBE68AE7A3277200");
        giftCard.setTrackNumber(TrackNumber.TrackTwo);
        giftCard.setEntryMethod(EntryMethod.Swipe);
        giftCard.setCardType("StoredValue");
        giftCard.setExpiry("2501");

        Transaction transaction = Transaction.fromBuilder()
                .withPaymentMethod(giftCard)
                .withOriginalTransactionDate("0424")
                .withTransactionTime("012140")
                .build();
        transaction.getTransactionReference()
                .setOriginalTransactionTypeIndicator(TransactionTypeIndicator.Purchase);

        header.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);
        Transaction response = transaction.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(40811)
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }


    @Test
    public void test23_credit_double_data_collect_discover_card_debit() throws ApiException {
        debit = new DebitTrackData();
        debit.setEncryptionData(EncryptionData.setKSNAndEncryptedData("E6699A44C3EE9E3AA75F9DF958C27469730C10D2929869F3704CC790CCB0AFDCDDE47F392E0D50E7",
                "F000019990E00003"));
        debit.setCardType("PinDebit");
        debit.setPinBlock("62968D2481D231E1A504010024A00014");
        debit.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E");
        debit.setTrackNumber(TrackNumber.TrackTwo);
        debit.setValue(";6090001234567891=2112120000000000001? ");
        debit.setExpiry("2512");

        header.setNtsMessageCode(NtsMessageCode.PinDebit);

        Transaction transaction = Transaction.fromBuilder()
                .withPaymentMethod(debit)
                .withDebitAuthorizer("00")
                .withApprovalCode("00")
                .withAuthorizationCode("38")
                .withOriginalTransactionDate("0407")
                .withTransactionTime("081627")
                .withOriginalMessageCode("02")
                .withSystemTraceAuditNumber("123456")
                .build();


        Transaction voidResponse = transaction.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());


    }

}
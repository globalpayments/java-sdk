package com.global.api.tests.network.nws;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.enums.Target;
import com.global.api.entities.enums.TrackNumber;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.paymentMethods.GiftCard;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NWSEncryption3DESTests {
    private CreditCardData card;
    private CreditCardData cardWithCvn;
    private CreditTrackData track;
    private DebitTrackData debit;
    private GiftCard giftCard;
    private CreditCardData visaCard;


    private NetworkGatewayConfig config;

    public NWSEncryption3DESTests() throws ApiException {
        Address address = new Address();
        address.setName("My STORE");
        address.setStreetAddress1("1 MY STREET");
        address.setCity("MYTOWN");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        AcceptorConfig acceptorConfig = new AcceptorConfig();
        acceptorConfig.setAddress(address);

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_MagStripe_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);


        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);
        //DE 127
        acceptorConfig.setSupportedEncryptionType(EncryptionType.TDES);
        acceptorConfig.setServiceType(ServiceType.GPN_API);
        acceptorConfig.setOperationType(OperationType.Decrypt);

        // gateway config
        config = new NetworkGatewayConfig(Target.NWS);
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("SPSA");
        config.setTerminalId("NWSJAVA05");
        config.setUniqueDeviceId("0001");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());

        ServicesContainer.configureService(config);

        track = new CreditTrackData();
        track.setEntryMethod(EntryMethod.Swipe);
        track.setEncryptionData(EncryptionData.setKtbAndKsn("29086EC77231A1435ECAF4E5EA9BEC8CC8446DC92D5A99DDD9F1CE1BAC79D7115C1224B380C05DCBBA2FD0D7F18074392EC8D800863BD43EC0FDA7E43FA1C303C6C540F6297C76A4",
                "F000019990E00003"));
        track.setCardType("MC");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setTrackNumber(TrackNumber.TrackOne);

        visaCard = new CreditCardData();
        visaCard.setEncryptionData(EncryptionData.setKtbAndKsn("49AB0D7DF39F4EAA3ADEB107CCCC03D0",
                "F000019990E00003"));
        visaCard.setCardPresent(true);
        visaCard.setReaderPresent(true);
        visaCard.setCardType("Visa");

        // VISA
        card = new CreditCardData();
        card.setCardPresent(true);
        card.setReaderPresent(true);
        card.setEncryptionData(EncryptionData.setKtbAndKsn("E2C4A716EBE88B483F6A6117031AC93A",
                "3D3F820E00003"));
        card.setCardType("MC");

        cardWithCvn = new CreditCardData();
        cardWithCvn.setCvn("103");
        cardWithCvn.setEncryptionData(EncryptionData.setKtbAndKsn("E2C4A716EBE88B483F6A6117031AC93A",
                "3D3F820E00003"));
        cardWithCvn.setCardType("MC");
        cardWithCvn.setCardPresent(false);
        cardWithCvn.setReaderPresent(false);


        // DEBIT
        debit = new DebitTrackData();
        debit.setPinBlock("62968D2481D231E1A504010024A00014");
        debit.setEncryptionData(EncryptionData.setKtbAndKsn("3EC0C41AB0CCC3BCA6EF798140BEF7BB5A06F78222AFD7BA8E949CA21AAF26E3EB2A4334BE31534E",
                "F000019990E00003"));
        debit.setCardType("PINDebitCard");
        debit.setTrackNumber(TrackNumber.TrackTwo);

        //Gift Card
        giftCard = new GiftCard();
        giftCard.setEncryptionData(EncryptionData.setKtbAndKsn("14FBB3A14C4D744F044CE5E56EAFEB19D747F9E56F8D1A40CE7A0CA6C197608BC9DE3354F13943B3",
                    "F000019990E00003"));
        giftCard.setTrackNumber(TrackNumber.TrackTwo);
        giftCard.setEntryMethod(EntryMethod.Swipe);
        giftCard.setCardType("SV");
    }

    //-----------------------------------------------Credit-------------------------------------------
    @Test
    public void test_001_credit_manual_auth_cvn() throws ApiException {
        Transaction response = cardWithCvn.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_002_credit_manual_auth() throws ApiException {
        Transaction response = visaCard.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_002_credit_swipe_auth() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_003_credit_manual_sale() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // void the transaction test case #7
        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());
    }

    @Test
    public void test_003_credit_sale_void() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // void the transaction test case #7
        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());
    }

    @Test
    public void test_004_credit_manual_sale() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // void the transaction test case #8
        Transaction reverseResponse = response.reverse().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }

    @Test
    public void test_004_credit_sale_reversal() throws ApiException {
        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        response.setNtsData(ntsData);

        // void the transaction test case #8
        Transaction reverseResponse = response.reverse().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }

    @Test
    public void test_Credit_auth_capture() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // test_019
        Transaction captureResponse = response.capture()
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_credit_swipe_sale() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_credit_swipe_voice_capture() throws ApiException {
        Transaction auth = visaCard.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(auth);
        assertEquals(auth.getResponseMessage(), "000", auth.getResponseCode());

        Transaction transaction = Transaction.fromNetwork(
                auth.getAuthorizedAmount(),
                auth.getAuthorizationCode(),
                new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized),
                visaCard
        );

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }


    @Test
    public void test_005_credit_manual_refund_cvn() throws ApiException {
        Transaction response = cardWithCvn.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_006_credit_manual_refund() throws ApiException {
        Transaction response = card.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_credit_balance_inquiry() throws ApiException {
        Transaction response = track.balanceInquiry()
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("313000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_007_credit_swipe_auth() throws ApiException {
        CreditTrackData track=new CreditTrackData();
        track.setCardType("MC");
        track.setEncryptionData(EncryptionData.setKtbAndKsn("E6699A44C3EE9E3AA75F9DF958C27469730C10D2929869F3704CC790CCB0AFDCDDE47F392E0D50E7",
                "3D3F820E00003"));
        track.setTrackNumber(TrackNumber.TrackTwo);
        track.setEntryMethod(EntryMethod.Swipe);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }

    @Test
    public void test_009_credit_swipe_sale() throws ApiException {

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();

        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        String IRR_data=response.getIssuerData().get(CardIssuerEntryTag.RetrievalReferenceNumber);
        // reverse the transaction test case #40
        Transaction voidResponse = response.voidTransaction()
                .withIssuerData(CardIssuerEntryTag.RetrievalReferenceNumber,IRR_data)
                .withCustomerInitiated(true)
                .execute();
        assertNotNull(voidResponse);
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());

        // reverse the transaction test case #39
        Transaction reverseResponse = response.reverse().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }

    @Test
    public void test_010_credit_swipe_refund() throws ApiException {
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    @Test
    public void test_013_visa_encrypted_follow_on() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction recreated = Transaction.fromNetwork(
                response.getAuthorizedAmount(),
                response.getAuthorizationCode(),
                new NtsData(FallbackCode.Received_IssuerTimeout,AuthorizerCode.Terminal_Authorized),
                track,
                response.getMessageTypeIndicator(),
                response.getSystemTraceAuditNumber(),
                response.getOriginalTransactionTime(),
                response.getProcessingCode()
        );

        Transaction reversal = recreated.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);
        assertEquals("400", reversal.getResponseCode());
    }

    @Test
    public void test_014_visa_encrypted_refund() throws ApiException {
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_015_credit_swipe_void() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // reverse the transaction test case #40
//        Transaction voidResponse = response.voidTransaction()
//                .withCustomerInitiated(true)
//                .execute();
//        assertNotNull(voidResponse);
//        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());

        // reverse the transaction test case #39
        Transaction reverseResponse = response.reverse().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }

    //-----------------------------------Visa Credit-----------------------------------------------
    @Test
    public void test_001_visa_credit_manual_auth_cvn() throws ApiException {
        cardWithCvn.setCardType("Visa");
        Transaction response = cardWithCvn.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_002_visa_credit_manual_auth() throws ApiException {
        card.setCardType("Visa");
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_002_visa_credit_swipe_auth() throws ApiException {
        track.setCardType("Visa");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_visa_Credit_auth_capture() throws ApiException {
        track.setCardType("Visa");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // test_019
        Transaction captureResponse = response.capture()
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);
        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_visa_credit_swipe_sale() throws ApiException {
        track.setCardType("Visa");
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_visa_credit_swipe_voice_capture() throws ApiException {
        track.setCardType("Visa");
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized),
                track
        );

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_010_visa_credit_swipe_refund() throws ApiException {
        track.setCardType("Visa");
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_003_visa_credit_sale_void() throws ApiException {
        track.setCardType("Visa");
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // void the transaction test case #7
        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());
    }

    @Test
    public void test_004_visa_credit_sale_reversal() throws ApiException {
        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);

        track.setCardType("Visa");
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        response.setNtsData(ntsData);

        // void the transaction test case #8
        Transaction reverseResponse = response.reverse().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }

    @Test
    public void test_visa_credit_balance_inquiry() throws ApiException {
        track.setCardType("Visa");
        Transaction response = track.balanceInquiry()
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("313000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }


    //-----------------------------------VisaCorporate Credit-----------------------------------------------
    @Test
    public void test_001_visaCorporate_credit_manual_auth_cvn() throws ApiException {
        cardWithCvn.setCardType("VisaCorporate");
        Transaction response = cardWithCvn.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_002_visaCorporate_credit_manual_auth() throws ApiException {
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        card.setCardType("VisaCorporate");
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_002_visaCorporate_credit_swipe_auth() throws ApiException {
        track.setCardType("VisaCorporate");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_visaCorporate_Credit_auth_capture() throws ApiException {
        track.setCardType("VisaCorporate");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // test_019
        Transaction captureResponse = response.capture()
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);
        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_visaCorporate_credit_swipe_sale() throws ApiException {
        track.setCardType("VisaCorporate");
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_visaCorporate_credit_swipe_voice_capture() throws ApiException {
        track.setCardType("VisaCorporate");
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized),
                track
        );

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_010_visaCorporate_credit_swipe_refund() throws ApiException {
        track.setCardType("VisaCorporate");
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_003_visaCorporate_credit_sale_void() throws ApiException {
        track.setCardType("VisaCorporate");
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // void the transaction test case #7
        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());
    }

    @Test
    public void test_004_visaCorporate_credit_sale_reversal() throws ApiException {
        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);

        track.setCardType("VisaCorporate");
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        response.setNtsData(ntsData);

        // void the transaction test case #8
        Transaction reverseResponse = response.reverse().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }

    @Test
    public void test_visaCorporate_credit_balance_inquiry() throws ApiException {
        track.setCardType("VisaCorporate");
        Transaction response = track.balanceInquiry()
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("313000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }


    //-----------------------------------VisaPurchasing Credit-----------------------------------------------
    @Test
    public void test_001_visaPurchasing_credit_manual_auth_cvn() throws ApiException {
        cardWithCvn.setCardType("VisaPurchasing");
        Transaction response = cardWithCvn.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_002_visaPurchasing_credit_manual_auth() throws ApiException {
        card.setCardType("VisaPurchasing");
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_002_visaPurchasing_credit_swipe_auth() throws ApiException {
        track.setCardType("VisaPurchasing");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_visaPurchasing_Credit_auth_capture() throws ApiException {
        track.setCardType("VisaPurchasing");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // test_019
        Transaction captureResponse = response.capture()
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);
        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_visaPurchasing_credit_swipe_sale() throws ApiException {
        track.setCardType("VisaPurchasing");
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_visaPurchasing_credit_swipe_voice_capture() throws ApiException {
        track.setCardType("VisaPurchasing");
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized),
                track
        );

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_010_visaPurchasing_credit_swipe_refund() throws ApiException {
        track.setCardType("VisaPurchasing");
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_003_visaPurchasing_credit_sale_void() throws ApiException {
        track.setCardType("VisaPurchasing");
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // void the transaction test case #7
        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());
    }

    @Test
    public void test_004_visaPurchasing_credit_sale_reversal() throws ApiException {
        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);

        track.setCardType("VisaPurchasing");
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        response.setNtsData(ntsData);

        // void the transaction test case #8
        Transaction reverseResponse = response.reverse().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }

    @Test
    public void test_visaPurchasing_credit_balance_inquiry() throws ApiException {
        track.setCardType("VisaPurchasing");
        Transaction response = track.balanceInquiry()
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("313000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }

    //-----------------------------------MCPurchasing Credit-----------------------------------------------
    @Test
    public void test_001_mcPurchasing_credit_manual_auth_cvn() throws ApiException {
        cardWithCvn.setCardType("MCPurchasing");
        Transaction response = cardWithCvn.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_002_mcPurchasing_credit_manual_auth() throws ApiException {
        card.setCardType("MCPurchasing");
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_002_mcPurchasing_credit_swipe_auth() throws ApiException {
        track.setCardType("MCPurchasing");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_mcPurchasing_Credit_auth_capture() throws ApiException {
        track.setCardType("MCPurchasing");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // test_019
        Transaction captureResponse = response.capture()
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);
        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_mcPurchasing_credit_swipe_sale() throws ApiException {
        track.setCardType("MCPurchasing");
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_mcPurchasing_credit_swipe_voice_capture() throws ApiException {
        track.setCardType("MCPurchasing");
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized),
                track
        );

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_010_mcPurchasing_credit_swipe_refund() throws ApiException {
        track.setCardType("MCPurchasing");
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_003_mcPurchasing_credit_sale_void() throws ApiException {
        track.setCardType("MCPurchasing");
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // void the transaction test case #7
        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());
    }

    @Test
    public void test_004_mcPurchasing_credit_sale_reversal() throws ApiException {
        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);

        track.setCardType("MCPurchasing");
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        response.setNtsData(ntsData);

        // void the transaction test case #8
        Transaction reverseResponse = response.reverse().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }

    @Test
    public void test_mcPurchasing_credit_balance_inquiry() throws ApiException {
        track.setCardType("MCPurchasing");
        Transaction response = track.balanceInquiry()
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("313000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }

    //-----------------------------------Amex Credit-----------------------------------------------
    @Test
    public void test_001_Amex_credit_manual_auth_cvn() throws ApiException {
        cardWithCvn.setCardType("Amex");
        Transaction response = cardWithCvn.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_002_Amex_credit_manual_auth() throws ApiException {
        card.setCardType("Amex");
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_002_Amex_credit_swipe_auth() throws ApiException {
        track.setCardType("Amex");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_Amex_Credit_auth_capture() throws ApiException {
        track.setCardType("Amex");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // test_019
        Transaction captureResponse = response.capture()
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);
        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_Amex_credit_swipe_sale() throws ApiException {
        track.setCardType("Amex");
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_Amex_credit_swipe_voice_capture() throws ApiException {
        track.setCardType("Amex");
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized),
                track
        );

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_010_Amex_credit_swipe_refund() throws ApiException {
        track.setCardType("Amex");
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_003_Amex_credit_sale_void() throws ApiException {
        track.setCardType("Amex");
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // void the transaction test case #7
        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());
    }

    @Test
    public void test_004_Amex_credit_sale_reversal() throws ApiException {
        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);

        track.setCardType("Amex");
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        response.setNtsData(ntsData);

        // void the transaction test case #8
        Transaction reverseResponse = response.reverse().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }

    @Test
    public void test_Amex_credit_balance_inquiry() throws ApiException {
        track.setCardType("Amex");
        Transaction response = track.balanceInquiry()
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("313000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }

    //-----------------------------------Discover Credit-----------------------------------------------
    @Test
    public void test_001_Discover_credit_manual_auth_cvn() throws ApiException {
        cardWithCvn.setCardType("Discover");
        Transaction response = cardWithCvn.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_002_Discover_credit_manual_auth() throws ApiException {
        card.setCardType("Discover");
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_002_Discover_credit_swipe_auth() throws ApiException {
        track.setCardType("Discover");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_Discover_Credit_auth_capture() throws ApiException {
        track.setCardType("Discover");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // test_019
        Transaction captureResponse = response.capture()
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);
        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_Discover_credit_swipe_sale() throws ApiException {
        track.setCardType("Discover");
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_Discover_credit_swipe_voice_capture() throws ApiException {
        track.setCardType("Discover");
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized),
                track
        );

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_010_Discover_credit_swipe_refund() throws ApiException {
        track.setCardType("Discover");
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_003_Discover_credit_sale_void() throws ApiException {
        track.setCardType("Discover");
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // void the transaction test case #7
        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());
    }

    @Test
    public void test_004_Discover_credit_sale_reversal() throws ApiException {
        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);

        track.setCardType("Discover");
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        response.setNtsData(ntsData);

        // void the transaction test case #8
        Transaction reverseResponse = response.reverse().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }

    @Test
    public void test_Discover_credit_balance_inquiry() throws ApiException {
        track.setCardType("Discover");
        Transaction response = track.balanceInquiry()
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("313000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }


    //-----------------------------------PayPal Credit-----------------------------------------------
    @Test
    public void test_001_PayPal_credit_manual_auth_cvn() throws ApiException {
        cardWithCvn.setCardType("PayPal");
        Transaction response = cardWithCvn.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_002_PayPal_credit_manual_auth() throws ApiException {
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        card.setCardType("PayPal");
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test

    public void test_002_PayPal_credit_swipe_auth() throws ApiException {
        track.setCardType("PayPal");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_PayPal_Credit_auth_capture() throws ApiException {
        track.setCardType("PayPal");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // test_019
        Transaction captureResponse = response.capture()
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);
        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_PayPal_credit_swipe_sale() throws ApiException {
        track.setCardType("PayPal");
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_PayPal_credit_swipe_voice_capture() throws ApiException {
        track.setCardType("PayPal");
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized),
                track
        );

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_010_PayPal_credit_swipe_refund() throws ApiException {
        track.setCardType("PayPal");
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_003_PayPal_credit_sale_void() throws ApiException {
        track.setCardType("PayPal");
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // void the transaction test case #7
        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());
    }

    @Test
    public void test_004_PayPal_credit_sale_reversal() throws ApiException {
        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);

        track.setCardType("PayPal");
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        response.setNtsData(ntsData);

        // void the transaction test case #8
        Transaction reverseResponse = response.reverse().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }

    @Test
    public void test_PayPal_credit_balance_inquiry() throws ApiException {
        track.setCardType("PayPal");
        Transaction response = track.balanceInquiry()
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("313000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }

    //------------------------------------debit-----------------------------------------------------
    @Test
    public void test_001_debit_auth() throws ApiException {
        Transaction response = debit.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    @Test
    public void test_debit_auth_capture() throws ApiException {
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        Transaction response = debit.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
//        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction captureResponse = response.capture()
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_debit_swipe_voice_capture() throws ApiException {
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized),
                debit
        );

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("008000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_debit_sale_with_cashBack() throws ApiException {
        Transaction response = debit.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(3))
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("090800", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_008_debit_sale() throws ApiException {
        Transaction response = debit.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_014_debit_encrypted_refund() throws ApiException {
        Transaction response = debit.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_debit_balance_inquiry() throws ApiException {
        Transaction response = track.balanceInquiry()
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("303000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_015_debit_swipe_void() throws ApiException {
        Transaction response = debit.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // reverse the transaction test case #40
        Transaction voidResponse = response.voidTransaction()
                .withCustomerInitiated(true)
                .execute();
        assertNotNull(voidResponse);
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());

        // reverse the transaction test case #39
        Transaction reverseResponse = response.reverse().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }

    @Test
    public void test_013_debit_encrypted_follow_on() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction recreated = Transaction.fromNetwork(
                response.getAuthorizedAmount(),
                response.getAuthorizationCode(),
                new NtsData(FallbackCode.Received_IssuerTimeout,AuthorizerCode.Terminal_Authorized),
                track,
                response.getMessageTypeIndicator(),
                response.getSystemTraceAuditNumber(),
                response.getOriginalTransactionTime(),
                response.getProcessingCode()
        );

        Transaction reversal = recreated.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);
        assertEquals("400", reversal.getResponseCode());
    }

    @Test
    public void test_003_debit_sale_void() throws ApiException {
        Transaction response = debit.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        //assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // void the transaction test case #7
        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());
    }

    @Test
    public void test_004_debit_sale_reversal() throws ApiException {
        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);

        Transaction response = debit.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        //assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        response.setNtsData(ntsData);

        // void the transaction test case #8
        Transaction reverseResponse = response.reverse().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }

    //-----------------------------------------------GiftCard----------------------------------------
    @Test
    public void giftCard_activate() throws ApiException {
        Transaction response = giftCard.activate(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void giftCard_add_value() throws ApiException {
        Transaction response = giftCard.addValue(new BigDecimal(25.00))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_001_GiftCard_auth() throws ApiException {
        Transaction response = giftCard.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_003_giftCard_sale() throws ApiException {
        Transaction response = giftCard.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    public void test_003_giftCard_sale_cashback() throws ApiException {
        Transaction response = giftCard.charge(new BigDecimal(41.00))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(40.00))
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_003_giftCard_sale_void() throws ApiException {
        Transaction response = giftCard.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // void the transaction test case #7
        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());
    }

    @Test
    public void test_004_giftCard_sale_reversal() throws ApiException {
        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);

        Transaction response = giftCard.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        response.setNtsData(ntsData);

        // void the transaction test case #8
        Transaction reverseResponse = response.reverse().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }
    @Test
    public void test_GiftCard_auth_capture() throws ApiException {
        Transaction response = giftCard.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // test_019
        Transaction captureResponse = response.capture()
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_GiftCard_swipe_voice_capture() throws ApiException {
        Transaction auth = giftCard.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(auth);

        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                auth.getAuthorizationCode(),
                new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized),
                giftCard
        );

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("006000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_005_GiftCard_refund() throws ApiException {
        Transaction response = giftCard.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_GiftCard_balance_inquiry() throws ApiException {
        Transaction response = giftCard.balanceInquiry()
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("316000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_007_giftCard_swipe_auth() throws ApiException {

        Transaction response = giftCard.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }




}

package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.*;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.services.NetworkService;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.LinkedList;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VapsEncryption3DESTests {
    private CreditCardData card;
    private CreditCardData cardWithCvn;
    private CreditTrackData track;
    private DebitTrackData debit;
    private EBTTrackData cashTrack;
    private EBTCardData ebtCardData;
    private EBTTrackData foodCard;
    private GiftCard giftCard;
    private GiftCard globalPaymentsGiftCardSwipe;
    private GiftCard svs;
    private AcceptorConfig acceptorConfig ;
    private NetworkGatewayConfig config ;

    public VapsEncryption3DESTests() throws ApiException {
        Address address = new Address();
        address.setName("My STORE");
        address.setStreetAddress1("1 MY STREET");
        address.setCity("MYTOWN");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        acceptorConfig = new AcceptorConfig();
        acceptorConfig.setAddress(address);

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_MagStripe);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);

        // hardware software config values
        acceptorConfig.setHardwareLevel("S1");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);
        acceptorConfig.setSupportsEmvPin(true);
        //DE 127
        acceptorConfig.setSupportedEncryptionType(EncryptionType.TDES);
        acceptorConfig.setServiceType(ServiceType.GPN_API);
        acceptorConfig.setOperationType(OperationType.Decrypt);

        // gateway config
        config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns-e.secureexchange.net");
        config.setSecondaryPort(15031);
        //Used for HGC and SVS card testing
        config.setCompanyId("0009");
        config.setTerminalId("0001237891001");
//        config.setCompanyId("0044");
//        config.setTerminalId("0000912197711");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());
        config.setMerchantType("5541");

        ServicesContainer.configureService(config);

        track = new CreditTrackData();
        track.setEntryMethod(EntryMethod.Swipe);
        track.setEncryptionData(EncryptionData.setKSNAndEncryptedData("EC7EB2F7BD67A2784F1AD9270EFFD90DD121B8653623911C6BC7B427F726A49F834CA051A6C1CC9CBB17910A1DBA209796BB6D08B8C374A2912AB018A679FA5A0A0EDEADF349FED3",
                "F000019990E00003"));
        track.setCardType("MC");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setTrackNumber(TrackNumber.TrackOne);
        track.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E17401487FC0B377F");
        track.setExpiry("2510");

        // VISA
        card = new CreditCardData();
        card.setCardPresent(true);
        card.setReaderPresent(true);
        card.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3A2067D00508DBE43E3342CC77B0575E17401487FC0B377F",
                "F000019990E00003"));
        card.setCardType("MC");
        card.setExpYear(2024);
        card.setExpMonth(12);

        cardWithCvn = new CreditCardData();
        cardWithCvn.setCvn("103");
        cardWithCvn.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3A2067D00508DBE43E3342CC77B0575E17401487FC0B377F",
                "F000019990E00003"));
        cardWithCvn.setCardType("MC");
        cardWithCvn.setCardPresent(false);
        cardWithCvn.setReaderPresent(false);
        cardWithCvn.setExpMonth(10);
        cardWithCvn.setExpYear(2025);


        // DEBIT
        debit = new DebitTrackData();
        debit.setCardType("PINDebitCard");
        debit.setPinBlock("62968D2481D231E1A504010024A00014");
        debit.setTrackNumber(TrackNumber.TrackTwo);
        debit.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E");
        debit.setExpiry("2510");

        debit.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3EC0C41AB0CCC3BCA6EF798140BEF7BB5A06F78222AFD7BA8E949CA21AAF26E3EB2A4334BE31534E",
                "F000019990E00003"));

        cashTrack = new EBTTrackData(EbtCardType.CashBenefit);
        cashTrack.setEncryptionData(EncryptionData.setKSNAndEncryptedData("C540BE2B2666CDF89D1CCE48ED0ED682DB88A0AD0765136FA1966602F3A49D90","F000014151181825"));
        cashTrack.setPinBlock("62968D2481D231E1A504010024A00014");
        cashTrack.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E");
        cashTrack.setTrackNumber(TrackNumber.TrackTwo);
        cashTrack.setExpiry("2512");

        foodCard = new EBTTrackData(EbtCardType.FoodStamp);
        foodCard.setEncryptionData(EncryptionData.setKSNAndEncryptedData("C540BE2B2666CDF89D1CCE48ED0ED682DB88A0AD0765136FA1966602F3A49D90","F000014151181825"));
        foodCard.setPinBlock("62968D2481D231E1A504010024A00014");
        foodCard.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E");
        foodCard.setTrackNumber(TrackNumber.TrackTwo);

        //Gift card
        config.setNodeIdentification("VLK2");
        ServicesContainer.configureService(config, "ValueLink");

        // VALUE LINK
        giftCard = new GiftCard();
        giftCard.setEncryptionData(EncryptionData.setKtbAndKsn("B1ADDC4F8C73F54A6F774ACD6C9DB09DD1E7EAEDB044711EAA08DBC78887256B8CD1D14DB7C2CC8D",
                "F000016F870850EB"));
        giftCard.setEncryptedPan("6B246DF0FF1EE73152F148C5F0C992CC");
        giftCard.setTrackNumber(TrackNumber.TrackTwo);
        giftCard.setEntryMethod(EntryMethod.Swipe);
        giftCard.setExpiry("2501");
        giftCard.setCardType("ValueLink");

        ebtCardData = new EBTCardData();
        ebtCardData.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3A2067D00508DBE43E3342CC77B0575E17401487FC0B377F","F000019990E00003"));
        ebtCardData.setPinBlock("62968D2481D231E1A504010000600004");
        ebtCardData.setEbtCardType(EbtCardType.CashBenefit);
        ebtCardData.setExpYear(2024);
        ebtCardData.setExpMonth(10);

        globalPaymentsGiftCardSwipe = new GiftCard();
        globalPaymentsGiftCardSwipe.setEncryptionData(EncryptionData.setKSNAndEncryptedData("EC7EB2F7BD67A2784F1AD9270EFFD90DD121B8653623911C6BC7B427F726A49F834CA051A6C1CC9CBB17910A1DBA209796BB6D08B8C374A2912AB018A679FA5A0A0EDEADF349FED3",
                "F000019990E00003"));
        globalPaymentsGiftCardSwipe.setEncryptedPan("49FE802FA87C5984BBE68AE7A3277200");
        globalPaymentsGiftCardSwipe.setTrackNumber(TrackNumber.TrackTwo);
        globalPaymentsGiftCardSwipe.setEntryMethod(EntryMethod.Swipe);
        globalPaymentsGiftCardSwipe.setExpiry("2501");
        globalPaymentsGiftCardSwipe.setCardType("GlobalPaymentsGift");

        svs = new GiftCard();
        svs.setEncryptionData(EncryptionData.setKSNAndEncryptedData("EC7EB2F7BD67A2784F1AD9270EFFD90DD121B8653623911C6BC7B427F726A49F834CA051A6C1CC9CBB17910A1DBA209796BB6D08B8C374A2912AB018A679FA5A0A0EDEADF349FED3",
                "F000019990E00003"));
        svs.setEncryptedPan("49FE802FA87C5984BBE68AE7A3277200");
        svs.setTrackNumber(TrackNumber.TrackTwo);
        svs.setEntryMethod(EntryMethod.Swipe);
        svs.setExpiry("2501");
        svs.setCardType("StoredValue");
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
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_002_credit_manual_auth_mc_indicator() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withMasterCardIndicator(MasterCardCITMITIndicator.CARDHOLDER_INITIATED_SUBSCRIPTION)
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
    public void test_Credit_auth_capture() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // test_019
        Transaction captureResponse = response.capture()
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        assertNotNull(captureResponse);
        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_credit_swipe_sale() throws ApiException {
        acceptorConfig.setHardwareLevel("S3");
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_credit_swipe_forceSale() throws ApiException {
        acceptorConfig.setHardwareLevel("S3");
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction forceSale = NetworkService.forcedSale(response.getTransactionToken())
                .withForceToHost(true)
                .withPaymentMethod(track)
                .execute();
        assertNotNull(forceSale);
        assertEquals("000", forceSale.getResponseCode());
    }

    @Test
    public void test_005_credit_manual_refund_cvn() throws ApiException {
        Transaction response = cardWithCvn.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withOfflineAuthCode("105683")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_006_credit_manual_refund() throws ApiException {
        Transaction response = card.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withOfflineAuthCode("105683")
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
        assertEquals("303000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_007_credit_swipe_auth() throws ApiException {
        acceptorConfig.setHardwareLevel("S3");
        CreditTrackData track=new CreditTrackData();
        track.setCardType("MC");
        track.setEncryptionData(EncryptionData.setKSNAndEncryptedData("29086EC77231A1435ECAF4E5EA9BEC8CC8446DC92D5A99DDD9F1CE1BAC79D7115C1224B380C05DCBBA2FD0D7F18074392EC8D800863BD43EC0FDA7E43FA1C303C6C540F6297C76A4",
                "F000019990E00003"));
        track.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E17401487FC0B377F");
        track.setTrackNumber(TrackNumber.TrackOne);
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
                .withOfflineAuthCode("105683")
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
    public void test_014_visa_encrypted_Refund() throws ApiException {
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

    }

    @Test
    public void test_014_visa_encrypted_Refund_cardData() throws ApiException {
        Transaction response = card.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

    }

    @Test
    public void test_014_visa_encrypted_forceRefund_10297() throws ApiException {
        acceptorConfig.setHardwareLevel("S3");
        Transaction response1 = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withOfflineAuthCode("105683")
                .execute();
        assertNotNull(response1);

        Transaction response2 = response1.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withSystemTraceAuditNumber(Integer.parseInt(response1.getSystemTraceAuditNumber()))
                .execute();
        assertNotNull(response2);

        NtsData ntsData = new NtsData();
        response2.setNtsData(ntsData);
        Transaction response3 = response2.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withSystemTraceAuditNumber(Integer.parseInt(response2.getSystemTraceAuditNumber()))
                .execute();
        assertNotNull(response3);

        Transaction response = NetworkService.forcedRefund(response3.getTransactionToken())
                .withCurrency("USD")
                .withForceToHost(true)
                .execute();
        assertNotNull(response);

        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_014_visa_encrypted_forceRefund_debit_10297() throws ApiException {
        acceptorConfig.setHardwareLevel("S3");
        Transaction response1 = debit.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withOfflineAuthCode("105683")
                .execute();
        assertNotNull(response1);

        Transaction response2 = response1.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withSystemTraceAuditNumber(Integer.parseInt(response1.getSystemTraceAuditNumber()))
                .execute();
        assertNotNull(response2);

        Transaction response3 = response2.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withSystemTraceAuditNumber(Integer.parseInt(response2.getSystemTraceAuditNumber()))
                .execute();
        assertNotNull(response3);

        Transaction response = NetworkService.forcedRefund(response3.getTransactionToken())
                .withCurrency("USD")
                .withForceToHost(true)
                .withPaymentMethod(debit)
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
        Transaction voidResponse = response.voidTransaction()
                .withCustomerInitiated(false)
                .execute();
        assertNotNull(voidResponse);
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());

        // reverse the transaction test case #39
        Transaction reverseResponse = response.reverse().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
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
        Transaction response = debit.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // test_019
        Transaction captureResponse = response.capture()
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

//         check response
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
        assertEquals("003000", pmi.getProcessingCode());
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
    public void test_014_debit_encrypted_forceRefund() throws ApiException {
        acceptorConfig.setHardwareLevel("S3");
        Transaction response = debit.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction resubmit = NetworkService.forcedRefund(response.getTransactionToken())
                .withForceToHost(true)
                .withPaymentMethod(debit)
                .execute();
        assertNotNull(resubmit);
        assertEquals("000", resubmit.getResponseCode());
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
    public void test_001_refund_resubmit_DataCollectforce() throws ApiException {
//        Transaction response = track.authorize(new BigDecimal(10))
//                .withCurrency("USD")
//                .execute();
//        assertNotNull(response);
//        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withOfflineAuthCode("105683")
                .execute();
        assertNotNull(response);

        NtsData ntsData = new NtsData();
        response.setNtsData(ntsData);
//        // test_019
        Transaction captureResponse = response.capture()
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        assertNotNull(captureResponse);
        assertEquals("000", captureResponse.getResponseCode());

        Transaction resubmit = NetworkService.resubmitDataCollect(captureResponse.getTransactionToken())
                .withForceToHost(true)
                .execute();
        assertNotNull(resubmit);
        assertEquals("000", resubmit.getResponseCode());
    }

    @Test
    public void test_001_sale_refund() throws ApiException {
        Transaction response = debit.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction response1 = response.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response1);
    }

    @Test
    public void test_ebt_track_sale() throws ApiException {
        Transaction response = cashTrack.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("008100", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check result
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
  
    @Test
    public void test_ebt_track_balance_enquiry() throws ApiException {
        Transaction response = cashTrack.balanceInquiry()
          .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);

        // check result
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
  
    @Test
    public void test_ebt_swipe_foodStamp_voice_capture() throws ApiException {
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized, DebitAuthorizerCode.UnknownAuthorizer),
                foodCard,
                "1200",
                "001931",
                "181214024442"
        );

        Transaction response = transaction.capture()
                .withReferenceNumber("123456789012345")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("008000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());
        assertEquals("1378", pmi.getMessageReasonCode());

        // check result
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_008_debit_sale1() throws ApiException {
        Transaction response = debit.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
    }

//auth and capture

    @Test
    public void test_Credit_auth_capture01() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        Transaction captureResponse = response.capture()
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        assertNotNull(captureResponse);
        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_Credit_auth_capture02() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        Transaction captureResponse = response.capture()
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        assertNotNull(captureResponse);
        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_Credit_auth_capture03() throws ApiException {
        Transaction response = cardWithCvn.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        Transaction captureResponse = response.capture()
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        assertNotNull(captureResponse);
        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }
    @Test
    public void test_force_dataCollect() throws ApiException {

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        Transaction captureResponse = response.capture()
                .withCurrency("USD")
                .withForceToHost(true)
                .execute();
        assertNotNull(captureResponse);

        assertNotNull(captureResponse);
        assertEquals("000", captureResponse.getResponseCode());
    }
    @Test
    public void test_001_resubmitDataCollect_issue_10292() throws ApiException {
        acceptorConfig.setHardwareLevel("S3");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        Transaction captureResponse = response.capture()
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        assertNotNull(captureResponse);
        assertEquals("000", captureResponse.getResponseCode());

        Transaction resubmit = NetworkService.resubmitDataCollect(captureResponse.getTransactionToken())
                .withForceToHost(true)
                .execute();
        assertNotNull(resubmit);
        assertEquals("000", resubmit.getResponseCode());
    }
    @Test
    public void test_001_resubmitDataCollectForce_issue_10292() throws ApiException {
        acceptorConfig.setHardwareLevel("S3");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction captureResponse = response.capture()
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        assertNotNull(captureResponse);
        assertEquals("000", captureResponse.getResponseCode());

        Transaction resubmit = NetworkService.resubmitDataCollect(captureResponse.getTransactionToken())
                .withForceToHost(true)
                .execute();
        assertNotNull(resubmit);
        assertEquals("000", resubmit.getResponseCode());
    }


//-----------------------------------------------GiftCard----------------------------------------

    @Test
    public void test_001_GiftCard_auth() throws ApiException {
        Transaction response = giftCard.authorize(new BigDecimal(10),true)
                .withCurrency("USD")
                .execute("ValueLink");

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100",pmi.getMessageTransactionIndicator());
        assertEquals("006000",pmi.getProcessingCode());
        assertEquals("101",pmi.getFunctionCode());

        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_003_giftCard_sale() throws ApiException {
        Transaction response = giftCard.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200",pmi.getMessageTransactionIndicator());
        assertEquals("006000",pmi.getProcessingCode());
        assertEquals("200",pmi.getFunctionCode());

        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void giftCard_add_value() throws ApiException {
        Transaction response = giftCard.addValue(new BigDecimal(25.00))
                .withCurrency("USD")
                .execute("ValueLink");
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200",pmi.getMessageTransactionIndicator());
        assertEquals("210060",pmi.getProcessingCode());
        assertEquals("200",pmi.getFunctionCode());

        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void giftCard_return() throws ApiException {
        Transaction response = giftCard.refund(new BigDecimal(35.24))
                .withCurrency("USD")
                .withClerkId("41256")
                .execute("ValueLink");
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200",pmi.getMessageTransactionIndicator());
        assertEquals("200060",pmi.getProcessingCode());
        assertEquals("200",pmi.getFunctionCode());

        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_003_giftCard_sale_void() throws ApiException {
        Transaction response = giftCard.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // void the transaction test case #7
        Transaction voidResponse = response.voidTransaction()
                .execute("ValueLink");
        assertNotNull(voidResponse);
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());
    }

    @Test
    public void test_004_giftCard_sale_reversal() throws ApiException {
        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);

        Transaction response = giftCard.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        response.setNtsData(ntsData);

        // void the transaction test case #8
        Transaction reverseResponse = response.reverse().execute("ValueLink");
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }
    @Test
    public void test_GiftCard_auth_capture() throws ApiException {
        Transaction response = giftCard.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // test_019
        Transaction captureResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(captureResponse);

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_GiftCard_swipe_voice_capture() throws ApiException {
        Transaction preResponse = giftCard.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(preResponse);
        assertEquals(preResponse.getResponseMessage(), "000", preResponse.getResponseCode());

        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10), preResponse.getAuthorizationCode(),
                new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized),
                giftCard
        );

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");
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
                .execute("ValueLink");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_GiftCard_balance_inquiry() throws ApiException {
        Transaction response = giftCard.balanceInquiry()
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("316000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void value_link_Replenish_void() throws ApiException {

        Transaction response = giftCard.addValue(new BigDecimal(10.00))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction voidResponse = response.voidTransaction()
                .execute("ValueLink");
        assertNotNull(voidResponse);
        assertEquals("400", voidResponse.getResponseCode());
    }

    @Test
    public void test_005_GiftCard_void_return() throws ApiException {
        Transaction response = giftCard.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction voidResponse = response.voidTransaction()
                .execute("ValueLink");
        assertNotNull(voidResponse);
        assertEquals("400", voidResponse.getResponseCode());
    }

    @Test
    public void test_007_giftCard_refund_capture() throws ApiException {

        Transaction response = giftCard.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }

    //-------------------------------HGC------------------------------------
    @Test
    public void test_001_GiftCard_auth_HGC() throws ApiException {
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);
        config.setMerchantType("5542");
        ServicesContainer.configureService(config,"ICR");
        globalPaymentsGiftCardSwipe.setEncryptionData(EncryptionData.setKtbAndKsn("3A2067D00508DBE43E3342CC77B0575E04D9191B380C88036DD82D54C834DCB4",
                "F000019990E00003"));
        globalPaymentsGiftCardSwipe.setEncryptedPan("49FE802FA87C5984BBE68AE7A3277200");
        globalPaymentsGiftCardSwipe.setTrackNumber(TrackNumber.TrackTwo);
        globalPaymentsGiftCardSwipe.setEntryMethod(EntryMethod.Swipe);
        globalPaymentsGiftCardSwipe.setExpiry("2501");
        globalPaymentsGiftCardSwipe.setCardType("GlobalPaymentsGift");

        Transaction response = giftCard.authorize(new BigDecimal(10),true)
                .withCurrency("USD")
                .execute("ICR");

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100",pmi.getMessageTransactionIndicator());
        assertEquals("006000",pmi.getProcessingCode());
        assertEquals("101",pmi.getFunctionCode());

        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_003_giftCard_sale_HGC() throws ApiException {
        Transaction response = globalPaymentsGiftCardSwipe.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200",pmi.getMessageTransactionIndicator());
        assertEquals("006000",pmi.getProcessingCode());
        assertEquals("200",pmi.getFunctionCode());

        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void giftCard_activate_HGC() throws ApiException {
        globalPaymentsGiftCardSwipe.setEncryptionData(EncryptionData.setKtbAndKsn("3A2067D00508DBE43E3342CC77B0575E04D9191B380C88036DD82D54C834DCB4",
                "F000019990E00003"));
        globalPaymentsGiftCardSwipe.setEncryptedPan("49FE802FA87C5984BBE68AE7A3277200");
        globalPaymentsGiftCardSwipe.setTrackNumber(TrackNumber.TrackTwo);
        globalPaymentsGiftCardSwipe.setEntryMethod(EntryMethod.Swipe);
        globalPaymentsGiftCardSwipe.setExpiry("2501");
        globalPaymentsGiftCardSwipe.setCardType("GlobalPaymentsGift");

        Transaction response = globalPaymentsGiftCardSwipe.activate(new BigDecimal(25.00))
                .withCurrency("USD")
                .execute();
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200",pmi.getMessageTransactionIndicator());
        assertEquals("900060",pmi.getProcessingCode());
        assertEquals("200",pmi.getFunctionCode());

        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_GiftCard_preAuthCompletion_HGC() throws ApiException {
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);
        config.setMerchantType("5542");
        ServicesContainer.configureService(config,"ICR");
        Transaction response = globalPaymentsGiftCardSwipe.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ICR");
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100",pmi.getMessageTransactionIndicator());
        assertEquals("006000",pmi.getProcessingCode());
        assertEquals("100",pmi.getFunctionCode());
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction captureResponse = response.preAuthCompletion(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(captureResponse);
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220",pmi.getMessageTransactionIndicator());
        assertEquals("006000",pmi.getProcessingCode());
        assertEquals("201",pmi.getFunctionCode());
        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void giftCard_return_HGC() throws ApiException {
        Transaction response = globalPaymentsGiftCardSwipe.refund(new BigDecimal(35.24))
                .withCurrency("USD")
                .withClerkId("41256")
                .execute();
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200",pmi.getMessageTransactionIndicator());
        assertEquals("200060",pmi.getProcessingCode());
        assertEquals("200",pmi.getFunctionCode());

        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_003_giftCard_sale_void_HGC() throws ApiException {
        Transaction response = globalPaymentsGiftCardSwipe.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // void the transaction test case #7
        Transaction voidResponse = response.voidTransaction()
                .execute();
        assertNotNull(voidResponse);
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());
    }

    @Test
    public void test_004_giftCard_sale_reversal_HGC() throws ApiException {
        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);

        Transaction response = globalPaymentsGiftCardSwipe.charge(new BigDecimal(10))
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
    public void test_GiftCard_auth_capture_HGC() throws ApiException {
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);
        config.setMerchantType("5542");
        ServicesContainer.configureService(config,"ICR");
        Transaction response = globalPaymentsGiftCardSwipe.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // test_019
        Transaction captureResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(captureResponse);

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_GiftCard_swipe_voice_capture_HGC() throws ApiException {
        Transaction preResponse = globalPaymentsGiftCardSwipe.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(preResponse);
        assertEquals(preResponse.getResponseMessage(), "000", preResponse.getResponseCode());

        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10), preResponse.getAuthorizationCode(),
                new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized),
                globalPaymentsGiftCardSwipe
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
    public void test_005_refund_HGC() throws ApiException {
        globalPaymentsGiftCardSwipe.setEncryptionData(EncryptionData.setKtbAndKsn("3A2067D00508DBE43E3342CC77B0575E04D9191B380C88036DD82D54C834DCB4",
                "F000019990E00003"));
        globalPaymentsGiftCardSwipe.setEncryptedPan("49FE802FA87C5984BBE68AE7A3277200");
        globalPaymentsGiftCardSwipe.setTrackNumber(TrackNumber.TrackTwo);
        globalPaymentsGiftCardSwipe.setEntryMethod(EntryMethod.Swipe);
        globalPaymentsGiftCardSwipe.setExpiry("2501");
        globalPaymentsGiftCardSwipe.setCardType("GlobalPaymentsGift");

        Transaction trans = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Interchange_Authorized),
                globalPaymentsGiftCardSwipe
        );

        Transaction response = trans.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withIssuerData(CardIssuerEntryTag.RetrievalReferenceNumber,"12123")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_balance_inquiry_HGC() throws ApiException {
        globalPaymentsGiftCardSwipe.setEncryptionData(EncryptionData.setKtbAndKsn("3A2067D00508DBE43E3342CC77B0575E04D9191B380C88036DD82D54C834DCB4",
                "F000019990E00003"));
        globalPaymentsGiftCardSwipe.setEncryptedPan("49FE802FA87C5984BBE68AE7A3277200");
        globalPaymentsGiftCardSwipe.setTrackNumber(TrackNumber.TrackTwo);
        globalPaymentsGiftCardSwipe.setEntryMethod(EntryMethod.Swipe);
        globalPaymentsGiftCardSwipe.setExpiry("2501");
        globalPaymentsGiftCardSwipe.setCardType("GlobalPaymentsGift");
        Transaction response = globalPaymentsGiftCardSwipe.balanceInquiry()
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("316000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void replenish_void_HGC() throws ApiException {
        globalPaymentsGiftCardSwipe.setEncryptionData(EncryptionData.setKtbAndKsn("3A2067D00508DBE43E3342CC77B0575E04D9191B380C88036DD82D54C834DCB4",
                "F000019990E00003"));
        globalPaymentsGiftCardSwipe.setEncryptedPan("49FE802FA87C5984BBE68AE7A3277200");
        globalPaymentsGiftCardSwipe.setTrackNumber(TrackNumber.TrackTwo);
        globalPaymentsGiftCardSwipe.setEntryMethod(EntryMethod.Swipe);
        globalPaymentsGiftCardSwipe.setExpiry("2501");
        globalPaymentsGiftCardSwipe.setCardType("GlobalPaymentsGift");
        Transaction response = globalPaymentsGiftCardSwipe.addValue(new BigDecimal(10.00))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction voidResponse = response.voidTransaction()
                .execute();
        assertNotNull(voidResponse);
        assertEquals("400", voidResponse.getResponseCode());
    }

    @Test
    public void test_005_void_return_HGC() throws ApiException {
        Transaction response = globalPaymentsGiftCardSwipe.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction voidResponse = response.voidTransaction()
                .execute();
        assertNotNull(voidResponse);
        assertEquals("400", voidResponse.getResponseCode());
    }

    @Test
    public void test_007_refund_capture_HGC() throws ApiException {

        Transaction response = globalPaymentsGiftCardSwipe.refund(new BigDecimal(10))
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

//    -----------------------------------SVS----------------------------------------------

    @Test
    public void test_001_GiftCard_auth_SVS() throws ApiException {
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);
        config.setMerchantType("5542");
        ServicesContainer.configureService(config,"ICR");

        Transaction response = svs.authorize(new BigDecimal(10),true)
                .withCurrency("USD")
                .execute("ICR");

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100",pmi.getMessageTransactionIndicator());
        assertEquals("006000",pmi.getProcessingCode());
        assertEquals("101",pmi.getFunctionCode());

        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_003_giftCard_sale_SVS() throws ApiException {

        Transaction response = svs.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200",pmi.getMessageTransactionIndicator());
        assertEquals("006000",pmi.getProcessingCode());
        assertEquals("200",pmi.getFunctionCode());

        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void giftCard_activate_SVS() throws ApiException {
        Transaction response = svs.activate(new BigDecimal(10.00))
                .withCurrency("USD")
                .execute();
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200",pmi.getMessageTransactionIndicator());
        assertEquals("900060",pmi.getProcessingCode());
        assertEquals("200",pmi.getFunctionCode());

        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_005_refund_SVS() throws ApiException {
             Transaction trans = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Interchange_Authorized),
                svs
        );

        Transaction response = trans.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withIssuerData(CardIssuerEntryTag.RetrievalReferenceNumber,"12123")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_balance_inquiry_SVS() throws ApiException {
        Transaction response = svs.balanceInquiry()
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("316000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_auth_capture_SVS() throws ApiException {
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);
        config.setMerchantType("5542");
        ServicesContainer.configureService(config,"ICR");
        Transaction response = svs.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // test_019
        Transaction captureResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(captureResponse);

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    // ******************** EBT Track **************************
    @Test
    public void test_ebt_sale_CashTrack() throws ApiException {
        Transaction response = cashTrack.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("008100", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_sale_cash_back_ebtCardData() throws ApiException {
        Transaction response = ebtCardData.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(5))
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("098100", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check result
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_separate_data_collect_ebtCardData() throws ApiException {

        Transaction recreated = Transaction.fromNetwork(
                new BigDecimal(10),
                "135425",
                new NtsData(FallbackCode.Received_IssuerTimeout,AuthorizerCode.Terminal_Authorized),
                cashTrack,
                "1220",
                "000226",
                "240327022602",
                "008100"
        );

        // check response
//        assertEquals("000", recreated.getResponseCode());
        Transaction dataCollectResponse = recreated.capture()
                .withReferenceNumber("123456789012345")
                .execute();
        assertNotNull(dataCollectResponse);

        // check message data
        PriorMessageInformation pmi = dataCollectResponse.getMessageInformation();
        assertEquals("000",dataCollectResponse.getResponseCode());
        assertNotNull(pmi);
    }

    @Test
    public void test_014_ebt_encrypted_refund_ebtTrackData() throws ApiException {
        Transaction response = foodCard.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_ebt_balance_inquiry_ebtTrackData() throws ApiException {

        Transaction response = cashTrack.balanceInquiry(InquiryType.Cash)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("318100", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        // check result
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    public void test_ebt_benefit_withdrawal_ebtTrack() throws ApiException {

        Transaction response = cashTrack.benefitWithdrawal(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("018100", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check result
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    public void test_void_transaction_cashCardTrack() throws ApiException {
        Transaction response = cashTrack.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("008100", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
//        assertEquals("000", response.getResponseCode());

        Transaction voidResponse = response.voidTransaction()
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi1 = voidResponse.getMessageInformation();
        assertNotNull(pmi1);
        assertEquals("1420", pmi1.getMessageTransactionIndicator());
        assertEquals("008100", pmi1.getProcessingCode());
        assertEquals("441", pmi1.getFunctionCode());
        assertEquals("4351", pmi1.getMessageReasonCode());

        // check result
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());
    }

    @Test
    public void test_reversal_ebtCashCardTrack() throws ApiException {
        Transaction response = cashTrack.charge(new BigDecimal(13))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
//        assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse(new BigDecimal(13))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);
        assertEquals("400", reversal.getResponseCode());
    }
    @Test
    public void test_reversal_cashback_ebtCashCardTrackData() throws ApiException {
        Transaction response = cashTrack.charge(new BigDecimal(13))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(3))
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerTimeout,AuthorizerCode.Terminal_Authorized);
        response.setNtsData(ntsData);
        Transaction reversal = response.reverse(new BigDecimal(13))
                .withCurrency("USD")
                .withCashBackAmount(new BigDecimal(3))
                .execute();
        assertNotNull(reversal);
        assertEquals("400", reversal.getResponseCode());
    }

    // ************************ Ebt manual ******************

    @Test
    public void test_ebt_sale_cardCardCard() throws ApiException {
        Transaction response = ebtCardData.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("008100", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_sale_cash_back_ebtCashCard() throws ApiException {
        Transaction response = ebtCardData.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(5))
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("098100", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check result
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_separate_data_collect_ebtCashCard() throws ApiException {

        Transaction recreated = Transaction.fromNetwork(
                new BigDecimal(10),
                "135425",
                new NtsData(FallbackCode.Received_IssuerTimeout,AuthorizerCode.Terminal_Authorized),
                ebtCardData,
               "1220",
                "000226",
               "240327022602",
               "008100"
        );

        // check response
//        assertEquals("000", recreated.getResponseCode());
        Transaction dataCollectResponse = recreated.capture()
                .withReferenceNumber("123456789012345")
                .execute();
        assertNotNull(dataCollectResponse);

        // check message data
        PriorMessageInformation pmi = dataCollectResponse.getMessageInformation();
        assertEquals("000",dataCollectResponse.getResponseCode());
        assertNotNull(pmi);
    }
    @Test
    public void test_ebt_benefit_withdrawal_ebtCardData() throws ApiException {

        Transaction response = ebtCardData.benefitWithdrawal(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("018100", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check result
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    public void test_void_transaction_cardData() throws ApiException {
        Transaction response = ebtCardData.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("008100", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
//        assertEquals("000", response.getResponseCode());

        Transaction voidResponse = response.voidTransaction()
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi1 = voidResponse.getMessageInformation();
        assertNotNull(pmi1);
        assertEquals("1420", pmi1.getMessageTransactionIndicator());
        assertEquals("008000", pmi1.getProcessingCode());
        assertEquals("441", pmi1.getFunctionCode());
        assertEquals("4351", pmi1.getMessageReasonCode());

        // check result
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());
    }

    @Test
    public void test_reversal_ebtCardData() throws ApiException {
        Transaction response = ebtCardData.charge(new BigDecimal(13))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
//        assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse(new BigDecimal(13))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);
        assertEquals("400", reversal.getResponseCode());
    }
    @Test
    public void test_reversal_cashback_ebtCardData() throws ApiException {
        Transaction response = ebtCardData.charge(new BigDecimal(13))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(3))
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse(new BigDecimal(13))
                .withCurrency("USD")
                .withCashBackAmount(new BigDecimal(3))
                .execute();
        assertNotNull(reversal);
        assertEquals("400", reversal.getResponseCode());
    }

    @Test
    public void preauthCompl() throws ApiException {
        Transaction recreated = Transaction.fromNetwork(
                new BigDecimal(10),
                "135425",
                new NtsData(FallbackCode.Received_IssuerTimeout,AuthorizerCode.Terminal_Authorized),
                debit,
                "1220",
                "000073",
                "240327043953",
                "008100"
        );


        Transaction capture = recreated.preAuthCompletion(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
    }

    //-----------EBT---------------
    @Test
    public void test_ebt_track_sale1() throws ApiException {

        cashTrack = new EBTTrackData(EbtCardType.CashBenefit);
        cashTrack.setEncryptionData(EncryptionData.setKSNAndEncryptedData("C540BE2B2666CDF89D1CCE48ED0ED682DB88A0AD0765136FA1966602F3A49D90","F000014151181825"));
        cashTrack.setPinBlock("62968D2481D231E1A504010024A00014");
        cashTrack.setEncryptedPan("657DB3B704EB5E19C0D3BB31BC0F0964346EF3C8C55C021F");
        cashTrack.setTrackNumber(TrackNumber.TrackTwo);
        cashTrack.setExpiry("2412");

        Transaction response = cashTrack.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("008100", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check result
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_ebt_track_saleCashBack() throws ApiException {

        cashTrack = new EBTTrackData(EbtCardType.CashBenefit);
        cashTrack.setEncryptionData(EncryptionData.setKSNAndEncryptedData("C540BE2B2666CDF89D1CCE48ED0ED682DB88A0AD0765136FA1966602F3A49D90","F000014151181825"));
        cashTrack.setPinBlock("62968D2481D231E1A504010024A00014");
        cashTrack.setEncryptedPan("657DB3B704EB5E19C0D3BB31BC0F0964346EF3C8C55C021F");
        cashTrack.setExpiry("2412");
        cashTrack.setTrackNumber(TrackNumber.TrackTwo);

        Transaction response = cashTrack.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(3))
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("098100", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check result
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_ebt_track_withdrawal() throws ApiException {
        cashTrack = new EBTTrackData(EbtCardType.CashBenefit);
        cashTrack.setEncryptionData(EncryptionData.setKSNAndEncryptedData("C540BE2B2666CDF89D1CCE48ED0ED682DB88A0AD0765136FA1966602F3A49D90","F000014151181825"));
        cashTrack.setPinBlock("62968D2481D231E1A504010024A00014");
        cashTrack.setTrackNumber(TrackNumber.TrackTwo);

        Transaction response = cashTrack.benefitWithdrawal(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("018100", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check result
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_ebt_track_balance_enquiry1() throws ApiException {
        cashTrack = new EBTTrackData(EbtCardType.CashBenefit);
        cashTrack.setEncryptionData(EncryptionData.setKSNAndEncryptedData("C540BE2B2666CDF89D1CCE48ED0ED682DB88A0AD0765136FA1966602F3A49D90","F000014151181825"));
        cashTrack.setPinBlock("62968D2481D231E1A504010024A00014");
        cashTrack.setTrackNumber(TrackNumber.TrackTwo);

        Transaction response = cashTrack.balanceInquiry()
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);

        // check result
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_ebt_track_reversal() throws ApiException {
        cashTrack = new EBTTrackData(EbtCardType.CashBenefit);
        cashTrack.setEncryptionData(EncryptionData.setKSNAndEncryptedData("C540BE2B2666CDF89D1CCE48ED0ED682DB88A0AD0765136FA1966602F3A49D90", "F000014151181825"));
        cashTrack.setPinBlock("62968D2481D231E1A504010024A00014");
        cashTrack.setEncryptedPan("657DB3B704EB5E19C0D3BB31BC0F0964346EF3C8C55C021F");
        cashTrack.setTrackNumber(TrackNumber.TrackTwo);

        Transaction response = cashTrack.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();

        Transaction recreated = Transaction.fromNetwork(
                response.getAuthorizedAmount(),
                response.getAuthorizationCode(),
                new NtsData(FallbackCode.Received_IssuerTimeout,AuthorizerCode.Terminal_Authorized),
                cashTrack,
                response.getMessageTypeIndicator(),
                response.getSystemTraceAuditNumber(),
                response.getOriginalTransactionTime(),
                response.getProcessingCode()
        );

        Transaction reversal = recreated.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);

    }

    @Test
    public void test_ebt_track_void() throws ApiException {
        cashTrack = new EBTTrackData(EbtCardType.CashBenefit);
        cashTrack.setEncryptionData(EncryptionData.setKSNAndEncryptedData("C540BE2B2666CDF89D1CCE48ED0ED682DB88A0AD0765136FA1966602F3A49D90", "F000014151181825"));
        cashTrack.setPinBlock("62968D2481D231E1A504010024A00014");
        cashTrack.setEncryptedPan("657DB3B704EB5E19C0D3BB31BC0F0964346EF3C8C55C021F");
        cashTrack.setExpiry("2412");
        cashTrack.setTrackNumber(TrackNumber.TrackTwo);

        Transaction response = cashTrack.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();

        Transaction voidResponse = response.voidTransaction()
                .withCustomerInitiated(true)
                .execute();
        assertNotNull(voidResponse);

    }

    @Test
    public void test_ebt_swipe_foodStamp_voice_capture1() throws ApiException {
        foodCard = new EBTTrackData(EbtCardType.FoodStamp);
        foodCard.setEncryptionData(EncryptionData.setKSNAndEncryptedData("C540BE2B2666CDF89D1CCE48ED0ED682DB88A0AD0765136FA1966602F3A49D90","F000014151181825"));
        foodCard.setPinBlock("62968D2481D231E1A504010024A00014");
        foodCard.setEncryptedPan("657DB3B704EB5E19C0D3BB31BC0F0964346EF3C8C55C021F");
        foodCard.setExpiry("2412");

        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized, DebitAuthorizerCode.UnknownAuthorizer),
                foodCard,
                "1200",
                "001931",
                "181214024442"
        );

        Transaction response = transaction.capture()
                .withReferenceNumber("123456789012345")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("008000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());
        assertEquals("1378", pmi.getMessageReasonCode());

        // check result
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_ebt_foodstamp_track_sale() throws ApiException {

        foodCard = new EBTTrackData(EbtCardType.FoodStamp);
        foodCard.setEncryptionData(EncryptionData.setKSNAndEncryptedData("C540BE2B2666CDF89D1CCE48ED0ED682DB88A0AD0765136FA1966602F3A49D90","F000014151181825"));
        foodCard.setPinBlock("62968D2481D231E1A504010024A00014");
        foodCard.setEncryptedPan("4355567063338");
        foodCard.setExpiry("2412");
        foodCard.setTrackNumber(TrackNumber.TrackTwo);

        Transaction response = foodCard.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("008000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check result
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_ebt_foodStamp_track_balance_enquiry() throws ApiException {
        foodCard = new EBTTrackData(EbtCardType.FoodStamp);
        foodCard.setEncryptionData(EncryptionData.setKSNAndEncryptedData("C540BE2B2666CDF89D1CCE48ED0ED682DB88A0AD0765136FA1966602F3A49D90","F000014151181825"));
        foodCard.setPinBlock("62968D2481D231E1A504010024A00014");
//        foodCard.setEncryptedPan("4355567063338");
        foodCard.setTrackNumber(TrackNumber.TrackTwo);

        Transaction response = foodCard.balanceInquiry()
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);

        // check result
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_ebt_foodStamp_track_reversal() throws ApiException {
        foodCard = new EBTTrackData(EbtCardType.FoodStamp);
        foodCard.setEncryptionData(EncryptionData.setKSNAndEncryptedData("C540BE2B2666CDF89D1CCE48ED0ED682DB88A0AD0765136FA1966602F3A49D90","F000014151181825"));
        foodCard.setPinBlock("62968D2481D231E1A504010024A00014");
        foodCard.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E17401487FC0B377F");
        foodCard.setTrackNumber(TrackNumber.TrackTwo);

        Transaction response = foodCard.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();

        Transaction recreated = Transaction.fromNetwork(
                response.getAuthorizedAmount(),
                response.getAuthorizationCode(),
                new NtsData(FallbackCode.Received_IssuerTimeout,AuthorizerCode.Terminal_Authorized),
                foodCard,
                response.getMessageTypeIndicator(),
                response.getSystemTraceAuditNumber(),
                response.getOriginalTransactionTime(),
                response.getProcessingCode()
        );

        Transaction reversal = recreated.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);

    }

    @Test
    public void test_ebt_foodStamp_track_void() throws ApiException {
        foodCard = new EBTTrackData(EbtCardType.FoodStamp);
        foodCard.setEncryptionData(EncryptionData.setKSNAndEncryptedData("C540BE2B2666CDF89D1CCE48ED0ED682DB88A0AD0765136FA1966602F3A49D90","F000014151181825"));
        foodCard.setPinBlock("62968D2481D231E1A504010024A00014");
        foodCard.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E17401487FC0B377F");
        foodCard.setTrackNumber(TrackNumber.TrackTwo);

        Transaction response = foodCard.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();

        Transaction voidResponse = response.voidTransaction()
                .withCustomerInitiated(true)
                .execute();
        assertNotNull(voidResponse);

    }
    @Test
    public void test_debit_refund_retry() throws ApiException {
        debit = new DebitTrackData();
        debit.setCardType("PINDebitCard");
        debit.setPinBlock("62968D2481D231E1A504010024A00014");
        debit.setTrackNumber(TrackNumber.TrackTwo);
        debit.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E");
        debit.setExpiry("2412");

        debit.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3EC0C41AB0CCC3BCA6EF798140BEF7BB5A06F78222AFD7BA8E949CA21AAF26E3EB2A4334BE31534E",
                "F000019990E00003"));

        Transaction response = debit.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("000", response.getResponseCode());

        Transaction recreated = Transaction.fromBuilder()
                .withAmount(new BigDecimal(10))
                .withAuthorizedAmount(response.getAuthorizedAmount())
                .withSystemTraceAuditNumber(response.getSystemTraceAuditNumber())
                .withAuthorizationCode(response.getAuthorizationCode())
                .withPaymentMethod(debit)
                .withNtsData(response.getNtsData())
                .withPosDataCode(response.getPosDataCode())
                .withMessageTypeIndicator(response.getMessageTypeIndicator())
                .withProcessingCode(response.getProcessingCode())
                .withTransactionTime(response.getOriginalTransactionTime())
                .build();

        Transaction capture = recreated.preAuthCompletion(new BigDecimal(10))
                .withCurrency("USD")
                .execute();

        assertEquals("000", capture.getResponseCode());
    }
    @Test
    public void test_debit_pre_auth_retry() throws ApiException {
        debit = new DebitTrackData();
        debit.setCardType("PINDebitCard");
        debit.setPinBlock("62968D2481D231E1A504010024A00014");
        debit.setTrackNumber(TrackNumber.TrackTwo);
        debit.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E");
        debit.setExpiry("2412");

        debit.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3EC0C41AB0CCC3BCA6EF798140BEF7BB5A06F78222AFD7BA8E949CA21AAF26E3EB2A4334BE31534E",
                "F000019990E00003"));

        Transaction authResponse = debit.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(authResponse);

        Transaction captureResponse = authResponse.preAuthCompletion()
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        // check response
        assertEquals("000", captureResponse.getResponseCode());

        Transaction recreated = Transaction.fromBuilder()
                .withAmount(new BigDecimal(10))
                .withAuthorizedAmount(captureResponse.getAuthorizedAmount())
                .withSystemTraceAuditNumber(captureResponse.getSystemTraceAuditNumber())
                .withAuthorizationCode(captureResponse.getAuthorizationCode())
                .withPaymentMethod(debit)
                .withNtsData(captureResponse.getNtsData())
                .withPosDataCode(captureResponse.getPosDataCode())
                .withMessageTypeIndicator(captureResponse.getMessageTypeIndicator())
                .withProcessingCode(captureResponse.getProcessingCode())
                .withTransactionTime(captureResponse.getOriginalTransactionTime())
                .build();


        Transaction capture = recreated.preAuthCompletion(new BigDecimal(10))
                .withCurrency("USD")
                .execute();

        assertEquals("000", capture.getResponseCode());
    }
    @Test
    public void test_debit_sale_retry_issue_10295() throws ApiException {
        debit = new DebitTrackData();
        debit.setCardType("PINDebitCard");
        debit.setPinBlock("62968D2481D231E1A504010024A00014");
        debit.setTrackNumber(TrackNumber.TrackTwo);
        debit.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E");
        debit.setExpiry("2410");

        debit.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3EC0C41AB0CCC3BCA6EF798140BEF7BB5A06F78222AFD7BA8E949CA21AAF26E3EB2A4334BE31534E",
                "F000019990E00003"));

        Transaction response = debit.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());

        Transaction recreated = Transaction.fromBuilder()
                .withAmount(new BigDecimal(10))
                .withAuthorizedAmount(response.getAuthorizedAmount())
                .withSystemTraceAuditNumber(response.getSystemTraceAuditNumber())
                .withAuthorizationCode(response.getAuthorizationCode())
                .withPaymentMethod(debit)
                .withNtsData(response.getNtsData())
                .withPosDataCode(response.getPosDataCode())
                .withMessageTypeIndicator(response.getMessageTypeIndicator())
                .withProcessingCode(response.getProcessingCode())
                .withTransactionTime(response.getOriginalTransactionTime())
                .build();

        Transaction capture = recreated.preAuthCompletion(new BigDecimal(10))
                .withCurrency("USD")
                .execute();

        assertEquals("000", capture.getResponseCode());

    }

    @Test
    public void test_debit_sale_issue_10300() throws ApiException {
        debit = new DebitTrackData();
        debit.setCardType("PINDebitCard");
        debit.setPinBlock("62968D2481D231E1A504010024A00014");
        debit.setTrackNumber(TrackNumber.TrackTwo);
        debit.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E");
        debit.setExpiry("2412");

        debit.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3EC0C41AB0CCC3BCA6EF798140BEF7BB5A06F78222AFD7BA8E949CA21AAF26E3EB2A4334BE31534E",
                "F000019990E00003"));

        Transaction response = debit.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());


    }

    @Test
    public void test_debit_sale_retry_issue_10300() throws ApiException {
        debit = new DebitTrackData();
        debit.setCardType("PINDebitCard");
        debit.setPinBlock("62968D2481D231E1A504010024A00014");
        debit.setTrackNumber(TrackNumber.TrackTwo);
        debit.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E");
        debit.setExpiry("2412");

        debit.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3EC0C41AB0CCC3BCA6EF798140BEF7BB5A06F78222AFD7BA8E949CA21AAF26E3EB2A4334BE31534E",
                "F000019990E00003"));


        Transaction recreated = Transaction.fromBuilder()
                .withAmount(new BigDecimal(10))
                .withAuthorizedAmount(new BigDecimal(10))
                .withSystemTraceAuditNumber("009357")
                .withAuthorizationCode("86    ")
                .withPaymentMethod(debit)
                .withNtsData(new NtsData(FallbackCode.Received_IssuerTimeout,AuthorizerCode.Terminal_Authorized))
                .withPosDataCode("V10101B1014C")
                .withMessageTypeIndicator("1200")
                .withProcessingCode("000800")
                .withTransactionTime("240410023005")
                .build();

        Transaction capture = recreated.preAuthCompletion(new BigDecimal(10))
                .withCurrency("USD")
                .execute();

        assertEquals("000", capture.getResponseCode());
    }

    @Test
    public void test_10315_batchClose_retransmitDataCollect_withBatchSummary() throws ApiException {
        BatchProvider batchProvider = BatchProvider.getInstance();
        String configName = "default";
        acceptorConfig.setHardwareLevel("S3");

        Transaction creditSale = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(creditSale);
        assertEquals("000", creditSale.getResponseCode());
        assertNotNull(creditSale.getTransactionToken());
        assertTrue(TerminalUtilities.checkLRC(creditSale.getTransactionToken()));


        Transaction response = BatchService.closeBatch(1, new BigDecimal(10), null)
                .execute(configName);
        assertNotNull(response);
        assertNotNull(response.getBatchSummary());

        BatchSummary summary = response.getBatchSummary();
        assertNotNull(summary);
        assertNotNull(summary.getTransactionToken());


        LinkedList<String> tokens = new LinkedList<>();
        tokens.add(creditSale.getTransactionToken());

        BatchSummary newSummary = summary.resubmitTransactions(tokens, configName);
        assertNotNull(newSummary);

        assertTrue(newSummary.getResponseCode().equals("500") || newSummary.getResponseCode().equals("501"));

    }
    @Test
    public void test_10315_batchClose_retransmitDataCollect_withBatchSummary_debit() throws ApiException {
        BatchProvider batchProvider = BatchProvider.getInstance();
        String configName = "default";
        acceptorConfig.setHardwareLevel("S3");

        Transaction creditSale = debit.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(creditSale);
        assertEquals("000", creditSale.getResponseCode());
        assertNotNull(creditSale.getTransactionToken());
        assertTrue(TerminalUtilities.checkLRC(creditSale.getTransactionToken()));


        Transaction response = BatchService.closeBatch(1, new BigDecimal(10), null)
                .execute(configName);
        assertNotNull(response);
        assertNotNull(response.getBatchSummary());

        BatchSummary summary = response.getBatchSummary();
        assertNotNull(summary);
        assertNotNull(summary.getTransactionToken());


        LinkedList<String> tokens = new LinkedList<>();
        tokens.add(creditSale.getTransactionToken());

        BatchSummary newSummary = summary.resubmitTransactions(tokens, configName);
        assertNotNull(newSummary);

        assertTrue(newSummary.getResponseCode().equals("500") || newSummary.getResponseCode().equals("501"));

    }

    @Test
    public void test_10315_batchClose_retransmitDataCollect_withBatchSummary_EBT() throws ApiException {
        BatchProvider batchProvider = BatchProvider.getInstance();
        String configName = "default";
        acceptorConfig.setHardwareLevel("S3");

        Transaction creditSale = ebtCardData.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(creditSale);
        assertEquals("000", creditSale.getResponseCode());
        assertNotNull(creditSale.getTransactionToken());
        assertTrue(TerminalUtilities.checkLRC(creditSale.getTransactionToken()));


        Transaction response = BatchService.closeBatch(1, new BigDecimal(10), null)
                .execute(configName);
        assertNotNull(response);
        assertNotNull(response.getBatchSummary());

        BatchSummary summary = response.getBatchSummary();
        assertNotNull(summary);
        assertNotNull(summary.getTransactionToken());


        LinkedList<String> tokens = new LinkedList<>();
        tokens.add(creditSale.getTransactionToken());

        BatchSummary newSummary = summary.resubmitTransactions(tokens, configName);
        assertNotNull(newSummary);

        assertTrue(newSummary.getResponseCode().equals("500") || newSummary.getResponseCode().equals("501"));

    }

}

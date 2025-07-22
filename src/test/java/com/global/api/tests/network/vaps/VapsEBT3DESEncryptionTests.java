package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.DebitAuthorizerCode;
import com.global.api.entities.enums.EbtCardType;
import com.global.api.entities.enums.InquiryType;
import com.global.api.entities.enums.TrackNumber;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.*;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.LinkedList;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

public class VapsEBT3DESEncryptionTests {
    private EBTTrackData cashTrack;
    private EBTCardData ebtCardData;
    private EBTTrackData foodCard;
    private AcceptorConfig acceptorConfig;

    public VapsEBT3DESEncryptionTests() throws ApiException {
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
        acceptorConfig.setHardwareLevel("S3");
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
        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns-e.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0044");
        config.setTerminalId("0007998855611");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());
        config.setMerchantType("5541");

        ServicesContainer.configureService(config);

        cashTrack = new EBTTrackData(EbtCardType.CashBenefit);
        cashTrack.setEncryptionData(EncryptionData.setKSNAndEncryptedData("C540BE2B2666CDF89D1CCE48ED0ED682DB88A0AD0765136FA1966602F3A49D90", "F000014151181825"));
        cashTrack.setPinBlock("62968D2481D231E1A504010024A00014");
        cashTrack.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E");
        cashTrack.setTrackNumber(TrackNumber.TrackTwo);
        cashTrack.setExpiry("2512");

        foodCard = new EBTTrackData(EbtCardType.FoodStamp);
        foodCard.setEncryptionData(EncryptionData.setKSNAndEncryptedData("C540BE2B2666CDF89D1CCE48ED0ED682DB88A0AD0765136FA1966602F3A49D90", "F000014151181825"));
        foodCard.setPinBlock("62968D2481D231E1A504010024A00014");
        foodCard.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E");
        foodCard.setTrackNumber(TrackNumber.TrackTwo);

        ebtCardData = new EBTCardData();
        ebtCardData.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3A2067D00508DBE43E3342CC77B0575E17401487FC0B377F", "F000019990E00003"));
        ebtCardData.setPinBlock("62968D2481D231E1A504010000600004");
        ebtCardData.setEbtCardType(EbtCardType.CashBenefit);
        ebtCardData.setExpYear(2024);
        ebtCardData.setExpMonth(10);
    }

    @Test
    public void test_ebt_track_saleCashBack() throws ApiException {
        Transaction response = foodCard.charge(new BigDecimal(10))
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
    public void test_ebt_track_balance_enquiry1() throws ApiException {
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
    public void test_014_ebt_encrypted_refund_ebtTrackData() throws ApiException {
        Transaction response = foodCard.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_ebt_foodStamp_track_reversal() throws ApiException {
        foodCard = new EBTTrackData(EbtCardType.FoodStamp);
        foodCard.setEncryptionData(EncryptionData.setKSNAndEncryptedData("C540BE2B2666CDF89D1CCE48ED0ED682DB88A0AD0765136FA1966602F3A49D90", "F000014151181825"));
        foodCard.setPinBlock("62968D2481D231E1A504010024A00014");
        foodCard.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E");
        foodCard.setTrackNumber(TrackNumber.TrackTwo);

        Transaction response = foodCard.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction recreated = Transaction.fromNetwork(
                response.getAuthorizedAmount(),
                response.getAuthorizationCode(),
                new NtsData(FallbackCode.Received_IssuerTimeout, AuthorizerCode.Terminal_Authorized),
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
        assertEquals("000", reversal.getResponseCode());

    }

    @Test
    public void test_ebt_foodStamp_track_void() throws ApiException {
        foodCard = new EBTTrackData(EbtCardType.FoodStamp);
        foodCard.setEncryptionData(EncryptionData.setKSNAndEncryptedData("C540BE2B2666CDF89D1CCE48ED0ED682DB88A0AD0765136FA1966602F3A49D90", "F000014151181825"));
        foodCard.setPinBlock("62968D2481D231E1A504010024A00014");
        foodCard.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E");
        foodCard.setTrackNumber(TrackNumber.TrackTwo);

        Transaction response = foodCard.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction voidResponse = response.voidTransaction()
                .withCustomerInitiated(true)
                .execute();
        assertNotNull(voidResponse);
        assertEquals("000", voidResponse.getResponseCode());

    }

    @Test
    public void test_ebt_swipe_foodStamp_voice_capture1() throws ApiException {
        foodCard = new EBTTrackData(EbtCardType.FoodStamp);
        foodCard.setEncryptionData(EncryptionData.setKSNAndEncryptedData("C540BE2B2666CDF89D1CCE48ED0ED682DB88A0AD0765136FA1966602F3A49D90", "F000014151181825"));
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
        assertEquals("000", response.getResponseCode());

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
        assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse(new BigDecimal(13))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);
        assertEquals("400", reversal.getResponseCode());
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
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction recreated = Transaction.fromNetwork(
                response.getAuthorizedAmount(),
                response.getAuthorizationCode(),
                new NtsData(FallbackCode.Received_IssuerTimeout, AuthorizerCode.Terminal_Authorized),
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
        assertEquals(reversal.getResponseMessage(), "000", reversal.getResponseCode());
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
        assertEquals("000", response.getResponseCode());

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

    @Test //Negative scenerio with data
    public void test_sale_with_InvalidData() throws ApiException {
        cashTrack.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3A2067D00508DBE43E3342CC77B0575E17401487FC0B377F",
                "F000019990E003"));
        Transaction response = cashTrack.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertTrue(("111").matches(response.getResponseCode()));
    }
}

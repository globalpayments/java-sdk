package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayTimeoutException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.network.abstractions.IStanProvider;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.EBTCardData;
import com.global.api.paymentMethods.EBTTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.services.NetworkService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VapsEbtTests {
    private EBTTrackData cashCard;
    private EBTTrackData foodCard;
    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;

    public VapsEbtTests() throws ApiException {
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
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_MagStripe_KeyEntry);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);
        acceptorConfig.setSupportsEmvPin(true);
        acceptorConfig.setSupportWexAdditionalProducts(true);

        // gateway config
        config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0044");
//        config.setTerminalId("0000912197711");
        config.setTerminalId("0007998855611");
        config.setUniqueDeviceId("0001");
        config.setMerchantType("5541");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());

        ServicesContainer.configureService(config);

        // cash card
        cashCard = new EBTTrackData(EbtCardType.CashBenefit);
        cashCard.setValue("4355567063338=2012101HJNw/ewskBgnZqkL");
        cashCard.setPinBlock("62968D2481D231E1A504010024A00014");
        cashCard.setEncryptionData(EncryptionData.version2("/wECAQEEAoFGAgEH4gcOTDT6jRZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0m+/d4SO9TEshhRGUUQzVBrBvP/Os1qFx+6zdQp1ejjUCoDmzoUMbil9UG73zBxxTOy25f3Px0p8joyCh8PEWhADz1BkROJT3q6JnocQE49yYBHuFK0obm5kqUcYPfTY09vPOpmN+wp45gJY9PhkJF5XvPsMlcxX4/JhtCshegz4AYrcU/sFnI+nDwhy295BdOkVN1rn00jwCbRcE900kj3UsFfyc", "2"));
        cashCard.setEncryptedPan("4355567063338");

        // cash card
        foodCard = new EBTTrackData(EbtCardType.FoodStamp);
        foodCard.setValue("4355567063338=2012101HJNw/ewskBgnZqkL");
        foodCard.setPinBlock("62968D2481D231E1A504010024A00014");
        foodCard.setEncryptionData(EncryptionData.version2("/wECAQEEAoFGAgEH4gcOTDT6jRZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0m+/d4SO9TEshhRGUUQzVBrBvP/Os1qFx+6zdQp1ejjUCoDmzoUMbil9UG73zBxxTOy25f3Px0p8joyCh8PEWhADz1BkROJT3q6JnocQE49yYBHuFK0obm5kqUcYPfTY09vPOpmN+wp45gJY9PhkJF5XvPsMlcxX4/JhtCshegz4AYrcU/sFnI+nDwhy295BdOkVN1rn00jwCbRcE900kj3UsFfyc", "2"));
    }

    @Test
    public void test_000_batch_close() throws ApiException {
        BatchSummary summary = BatchService.closeBatch();
        assertNotNull(summary);
        assertTrue(summary.isBalanced());
    }

    @Test
    public void test_213_manual_sale() throws ApiException {
        EBTCardData ebtCard = new EBTCardData(EbtCardType.CashBenefit);
        ebtCard.setNumber("4012002000060016");
        ebtCard.setExpMonth(12);
        ebtCard.setExpYear(2025);
        ebtCard.setPinBlock("32539F50C245A6A93D123412324000AA");

        Transaction response = ebtCard.charge(new BigDecimal(110))
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

    @Test(expected = BuilderException.class)
    public void test_214_manual_balance_inquiry() throws ApiException {
        EBTCardData ebtCard = new EBTCardData(EbtCardType.CashBenefit);
        ebtCard.setNumber("4012002000060016");
        ebtCard.setExpMonth(12);
        ebtCard.setExpYear(2025);
        ebtCard.setPinBlock("32539F50C245A6A93D123412324000AA");

        ebtCard.balanceInquiry(InquiryType.Cash)
                .withUniqueDeviceId("0001")
                .execute();
    }

    @Test(expected = UnsupportedTransactionException.class)
    public void test_215_swipe_authorization() throws ApiException {
        cashCard.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
    }

    @Test
    public void test_216_swipe_balance_inquiry() throws ApiException {
        EBTTrackData trackData = new EBTTrackData(EbtCardType.CashBenefit);
        trackData.setValue(";9840000921111111123=491200000000?");
        trackData.setPinBlock("62968D2481D231E1A504010024A00014");

        Transaction response = trackData.balanceInquiry(InquiryType.Cash)
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
    public void test_217_swipe_sale() throws ApiException {
        EBTTrackData trackData = new EBTTrackData(EbtCardType.CashBenefit);
        trackData.setValue(";9840000921111111149=491200000000?");
        trackData.setPinBlock("62968D2481D231E1A504010024A00014");

        Transaction response = trackData.charge(new BigDecimal(10))
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
    public void test_03_swipe_sale_TransactionFee() throws ApiException {
        Transaction response = cashCard.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withFee(FeeType.TransactionFee, new BigDecimal(1))
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
    public void test_219_swipe_sale_cash_back() throws ApiException {
        Transaction response = cashCard.charge(new BigDecimal(10))
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
    public void test_219_swipe_sale_capture_DE46_56() throws ApiException {
        Transaction response = cashCard.charge(new BigDecimal(10))
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

        Transaction dataCollectResponse = response.capture()
                .withReferenceNumber("123456789012345")
                .execute();
        assertNotNull(dataCollectResponse);

        // check message data
        pmi = dataCollectResponse.getMessageInformation();
        assertEquals("000",dataCollectResponse.getResponseMessage());
        assertNotNull(pmi);

    }

    @Test
    public void test_219_swipe_sale_capture_1221() throws ApiException {
        Transaction response = cashCard.charge(new BigDecimal(10))
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

        Transaction dataCollectResponse = NetworkService.resubmitDataCollect(response.getTransactionToken())
//                .withReferenceNumber("123456789012345")
                .withForceToHost(true)
                .execute();
        assertNotNull(dataCollectResponse);
        assertEquals("000",dataCollectResponse.getResponseMessage());

        // check message data
        pmi = dataCollectResponse.getMessageInformation();
        assertNotNull(pmi);

    }

    @Test
    public void test_220_swipe_benefit_withdrawal() throws ApiException {
        Transaction response = cashCard.benefitWithdrawal(new BigDecimal(10))
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

    @Test(expected = UnsupportedTransactionException.class)
    public void test_221_swipe_refund() throws ApiException {
        cashCard.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
    }

    @Test
    public void test_222_swipe_void() throws ApiException {
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(),
                cashCard,
                "1200",
                "000791",
                "181126125809"
        );

        Transaction response = transaction.voidTransaction()
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("008100", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4351", pmi.getMessageReasonCode());

        // check result
        assertEquals(response.getResponseMessage(), "400", response.getResponseCode());
    }

    @Test(expected = GatewayTimeoutException.class) @Ignore
    public void test_223_swipe_reversal() throws ApiException {
        cashCard.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withSimulatedHostErrors(Host.Primary, HostError.Timeout)
                .execute();
        Assert.fail("Did not timeout.");
    }

    @Test
    public void test_224_swipe_reversal_cashBack() throws ApiException {
        Transaction response = cashCard.charge(new BigDecimal(13))
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
    public void test_225_manual_foodStamp_sale() throws ApiException {
        EBTCardData ebtCard = new EBTCardData(EbtCardType.FoodStamp);
        ebtCard.setNumber("4012002000060016");
        ebtCard.setExpMonth(12);
        ebtCard.setExpYear(2025);
        ebtCard.setPinBlock("32539F50C245A6A93D123412324000AA");

        Transaction response = ebtCard.charge(new BigDecimal(110))
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

    @Test(expected = BuilderException.class)
    public void test_226_manual_foodStamp_balance() throws ApiException {
        EBTCardData ebtCard = new EBTCardData(EbtCardType.FoodStamp);
        ebtCard.setNumber("4012002000060016");
        ebtCard.setExpMonth(12);
        ebtCard.setExpYear(2025);
        ebtCard.setPinBlock("32539F50C245A6A93D123412324000AA");

        ebtCard.balanceInquiry(InquiryType.Foodstamp)
                .withUniqueDeviceId("0001")
                .execute();
    }

    @Test(expected = UnsupportedTransactionException.class)
    public void test_227_swipe_foodStamp_authorization() throws ApiException {
        foodCard.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
    }

    @Test
    public void test_228_swipe_foodStamp_balance() throws ApiException {
        Transaction response = foodCard.balanceInquiry(InquiryType.Foodstamp)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("318000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        // check result
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_229_swipe_foodStamp_sale() throws ApiException {
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

    @Test(expected = BuilderException.class)
    public void test_230_swipe_foodStamp_sale_cashBack() throws ApiException {
        foodCard.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(5))
                .execute();
    }

    @Test
    public void test_231_swipe_foodStamp_return() throws ApiException {
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(),
                foodCard,
                "1200",
                "000791",
                "181126125809"
        );

        Transaction response = transaction.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("200080", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check result
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_232_swipe_foodStamp_voice_capture() throws ApiException {
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
    public void test_233_swipe_foodStamp_void() throws ApiException {
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(),
                foodCard,
                "1200",
                "000791",
                "181126125809"
        );

        Transaction response = transaction.voidTransaction()
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("008000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4351", pmi.getMessageReasonCode());

        // check result
        assertEquals(response.getResponseMessage(), "400", response.getResponseCode());
    }

    @Test(expected = GatewayTimeoutException.class) @Ignore
    public void test_234_swipe_foodStamp_reverse_sale() throws ApiException {
        foodCard.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withSimulatedHostErrors(Host.Primary, HostError.Timeout)
                .execute();
        Assert.fail("Did not timeout.");
    }

    @Test
    public void test_235_swipe_foodStanp_refund_by_card() throws ApiException {
        Transaction response = foodCard.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("200080", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check result
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    public void test_sale_address_null_for_code_coverage_only() throws ApiException {

        acceptorConfig.setAddress(null);
        config.setAcceptorConfig(acceptorConfig);

        BuilderException builderException = assertThrows(BuilderException.class,()->{
            cashCard.charge(new BigDecimal(10))
                    .withCurrency("USD")
                    .execute();
        });
        assertEquals("The address in the acceptor config cannot be null for PIN based transactions.", builderException.getMessage());

    }

    @Test
    public void test_resubmit_token_null_code_coverage() throws ApiException {

        UnsupportedTransactionException unsupportedTransactionException = assertThrows(UnsupportedTransactionException.class, ()->{
            Transaction response = cashCard.authorize(new BigDecimal(10))
                    .withCurrency("USD")
                    .execute();
            assertNotNull(response);
        });
        assertEquals("Authorizations are not allowed for EBT cards.", unsupportedTransactionException.getMessage());
    }

    @Test
    public void test_sale_internal_datacollect() throws ApiException {
        EBTTrackData trackData = new EBTTrackData(EbtCardType.CashBenefit);
        trackData.setValue(";9840000921111111149=491200000000?");
        trackData.setPinBlock("62968D2481D231E1A504010024A00014");

        IStanProvider stanGenerator = StanGenerator.getInstance();
        int stan = stanGenerator.generateStan();

        Transaction response = trackData.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withSystemTraceAuditNumber(stan,stan)
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
    public void test_EBT_internal_Capture() throws ApiException {
        EBTCardData ebtCard = new EBTCardData(EbtCardType.FoodStamp);
        ebtCard.setNumber("4012002000060016");
        ebtCard.setExpMonth(12);
        ebtCard.setExpYear(2025);
        ebtCard.setPinBlock("32539F50C245A6A93D123412324000AA");
        IStanProvider stanGenerator = StanGenerator.getInstance();
        int stan = stanGenerator.generateStan();

        Transaction response = ebtCard.charge(new BigDecimal(40))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("000", response.getResponseCode());

        Transaction capture = response.capture(new BigDecimal(10))
                .withSystemTraceAuditNumber(stan,stan)
                .execute();
        assertNotNull(capture);


        // check response
        assertEquals("000", capture.getResponseCode());
    }
}

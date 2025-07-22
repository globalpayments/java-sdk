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
import com.global.api.network.abstractions.IBatchProvider;
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
import org.joda.time.DateTime;
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
    private EBTCardData foodCardData;
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
        acceptorConfig.setSupportWexAvailableProducts(true);

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
//        cashCard.setEncryptionData(EncryptionData.version2("/wECAQEEAoFGAgEH4gcOTDT6jRZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0m+/d4SO9TEshhRGUUQzVBrBvP/Os1qFx+6zdQp1ejjUCoDmzoUMbil9UG73zBxxTOy25f3Px0p8joyCh8PEWhADz1BkROJT3q6JnocQE49yYBHuFK0obm5kqUcYPfTY09vPOpmN+wp45gJY9PhkJF5XvPsMlcxX4/JhtCshegz4AYrcU/sFnI+nDwhy295BdOkVN1rn00jwCbRcE900kj3UsFfyc", "2"));
//        cashCard.setEncryptedPan("4355567063338");

        // cash card
        foodCard = new EBTTrackData(EbtCardType.FoodStamp);
        foodCard.setValue("4355567063338=2012101HJNw/ewskBgnZqkL");
        foodCard.setPinBlock("62968D2481D231E1A504010024A00014");
//        foodCard.setEncryptionData(EncryptionData.version2("/wECAQEEAoFGAgEH4gcOTDT6jRZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0m+/d4SO9TEshhRGUUQzVBrBvP/Os1qFx+6zdQp1ejjUCoDmzoUMbil9UG73zBxxTOy25f3Px0p8joyCh8PEWhADz1BkROJT3q6JnocQE49yYBHuFK0obm5kqUcYPfTY09vPOpmN+wp45gJY9PhkJF5XvPsMlcxX4/JhtCshegz4AYrcU/sFnI+nDwhy295BdOkVN1rn00jwCbRcE900kj3UsFfyc", "2"));

        foodCardData = new EBTCardData(EbtCardType.FoodStamp);
        foodCardData.setNumber("4012002000060016");
        foodCardData.setExpMonth(12);
        foodCardData.setExpYear(2025);
        foodCardData.setReaderPresent(true);
        foodCardData.setCardPresent(true);
        foodCardData.setPinBlock("32539F50C245A6A93D123412324000AA");
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
                .withFee(FeeType.TransactionFee, new BigDecimal(1))
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

        Transaction dataCollectResponse = response.preAuthCompletion()
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
    public void test_EBT_CashBenefits_balance_inquiry() throws ApiException {
        EBTTrackData trackData = new EBTTrackData(EbtCardType.CashBenefit);
        trackData.setValue(";9840000921111111123=491200000000?");
        trackData.setPinBlock("AFEC374574FC90623D010000116001EE");

        Transaction response = trackData.balanceInquiry(InquiryType.Cash)
                .withCurrency("USD")
                .withFee(FeeType.TransactionFee,new BigDecimal("1.1"))
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("318100", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());
    }
    @Test
    public void test_EBT_CashBenefits_sale() throws ApiException {
        EBTTrackData trackData = new EBTTrackData(EbtCardType.CashBenefit);
        trackData.setValue(";9840000921111111149=491200000000?");
        trackData.setPinBlock("AFEC374574FC90623D010000116001EE");

        Transaction response = trackData.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withFee(FeeType.TransactionFee,new BigDecimal("1.1"))
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("008100", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());
    }
    @Test
    public void test_EBT_CashBenefits_void() throws ApiException {
        EBTTrackData trackData = new EBTTrackData(EbtCardType.CashBenefit);
        trackData.setValue(";9840000921111111149=491200000000?");
        trackData.setPinBlock("AFEC374574FC90623D010000116001EE");

        Transaction response = trackData.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withFee(FeeType.TransactionFee,new BigDecimal("1.1"))
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("008100", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        Transaction voidResponse = response.voidTransaction(new BigDecimal("10"))
                .execute();
        assertNotNull(voidResponse);
    }

    @Test
    public void test_EBT_FeeAmount_Support_CashBenefitsOnly_Exception() throws UnsupportedOperationException {
        EBTTrackData trackData = new EBTTrackData(EbtCardType.FoodStamp);
        trackData.setValue(";9840000921111111149=491200000000?");
        trackData.setPinBlock("AFEC374574FC90623D010000116001EE");

        UnsupportedOperationException foodStampNotSupportedException = assertThrows(UnsupportedOperationException.class, ()-> {
            trackData.charge(new BigDecimal("10"))
                    .withCurrency("USD")
                    .withFee(FeeType.TransactionFee, new BigDecimal("1.1"))
                    .execute();
        });
        assertTrue(foodStampNotSupportedException.getMessage().equals("For EBT,Fee Amount supported for Cash Benefits card only"));

    }
    @Test
    public void test_sale_DataCollect_TransactionFee() throws ApiException {
        Transaction response = cashCard.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withFee(FeeType.TransactionFee, new BigDecimal("1"))
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

        // check the dataCollect
        Transaction dataCollect = response.getPreAuthCompletion();

        // check message data
        pmi = dataCollect.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("008100", pmi.getProcessingCode());
        assertEquals("1379", pmi.getMessageReasonCode());
        assertEquals("201", pmi.getFunctionCode());
    }
    @Test
    public void test_codeCoverage_cashcard_refund_by_card() throws ApiException {
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
    public void test_card_type_null_for_code_coverage_only() throws ApiException {
        EBTTrackData trackData = new EBTTrackData();
        trackData.setValue(";9840000921111111149=491200000000?");
        trackData.setPinBlock("AFEC374574FC90623D010000116001EE");

        BuilderException builderException = assertThrows(BuilderException.class,()->{
            trackData.charge(new BigDecimal("10"))
                    .withCurrency("USD")
                    .withFee(FeeType.TransactionFee,new BigDecimal("1.1"))
                    .execute();
        });
        assertEquals("The card type must be specified for EBT transactions.", builderException.getMessage());

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
    public void test_EBT_Capture() throws ApiException {
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

        Transaction capture = response.preAuthCompletion(new BigDecimal(10))
                .execute();
        assertNotNull(capture);


        // check response
        assertEquals("000", capture.getResponseCode());
    }

    @Test
    public void test_EBT_sale_capture_foodCard() throws ApiException {
        IStanProvider stan = StanGenerator.getInstance();
        IBatchProvider batch = BatchProvider.getInstance();
        String date = DateTime.now().toString("yyMMddhhmmss");

        Transaction saleResponse = foodCard.charge(new BigDecimal("10")) //Amount : DE 4
                .withCurrency("USD")
                .withSystemTraceAuditNumber(stan.generateStan()) //STAN : DE 11
                .withBatchNumber(batch.getBatchNumber(), batch.getSequenceNumber()) // DE 48-4
                .withTimestamp(date) // DE 12
                .withUniqueDeviceId("1001")
                .execute();
        assertNotNull(saleResponse);

        PriorMessageInformation pmi = saleResponse.getMessageInformation();

        Transaction rebuild = Transaction.fromBuilder()
                .withAmount(new BigDecimal("10")) //original transaction amount
                .withAuthorizationCode(saleResponse.getAuthorizationCode()) // auth code from sales response DE 38 M
                .withNtsData(saleResponse.getNtsData())
                .withPaymentMethod(foodCard) //Original Payment method DE 48-11 M
                .withMessageTypeIndicator(pmi.getMessageTransactionIndicator()) // original MTI DE 56 M
                .withSystemTraceAuditNumber(pmi.getSystemTraceAuditNumber()) // Original STAN DE 56 M
                .withTransactionTime(saleResponse.getOriginalTransactionTime()) // original transaction time DE 56 M
                .withProcessingCode(saleResponse.getProcessingCode()) //original Processing code DE 3 M
                .build();

        Transaction capture = rebuild.preAuthCompletion()
                .withSystemTraceAuditNumber(stan.generateStan()) //DE 11
                .withTimestamp(date)    //DE 12 - M
                .withPriorMessageInformation(pmi) //DE 48-39
                .withBatchNumber(saleResponse.getTransactionReference().getBatchNumber(),
                        saleResponse.getTransactionReference().getSequenceNumber())
                .execute();
        assertNotNull(capture);
        assertEquals("000", capture.getResponseCode());
    }

    //Food Benefit only
    @Test
    public void test_ebt_refund() throws ApiException {
        IStanProvider stan = StanGenerator.getInstance();
        IBatchProvider batch = BatchProvider.getInstance();
        String date = DateTime.now().toString("yyMMddhhmmss");

         Transaction refundResponse = foodCard.refund(new BigDecimal("10"))
                .withCurrency("USD")
                .withSystemTraceAuditNumber(stan.generateStan())
                .withTimestamp(date)
                .withUniqueDeviceId("2402")
                .withBatchNumber(batch.getBatchNumber(), batch.getSequenceNumber())
                .execute();
        assertNotNull(refundResponse);

        // check response
        assertEquals("000", refundResponse.getResponseCode());
        PriorMessageInformation pmi = refundResponse.getMessageInformation();

        Transaction recreated = Transaction.fromBuilder()
                .withAmount(new BigDecimal("10")) //original amount
                .withAuthorizationCode(refundResponse.getAuthorizationCode()) // auth code from response DE 38 M
                .withNtsData(refundResponse.getNtsData())
                .withPaymentMethod(foodCard) //Original Payment method DE 48-11 M
                .withMessageTypeIndicator(pmi.getMessageTransactionIndicator()) // original MTI DE 56 M
                .withSystemTraceAuditNumber(pmi.getSystemTraceAuditNumber()) // Original STAN DE 56 M
                .withTransactionTime(refundResponse.getOriginalTransactionTime()) // original transaction time DE 56 M
                .withProcessingCode(refundResponse.getProcessingCode()) //original Processing code DE 3 M
                .build();

        Transaction capture = recreated.preAuthCompletion()
                .withSystemTraceAuditNumber(stan.generateStan())
                .withPriorMessageInformation(pmi) //DE 48-39
                .withTimestamp(date) //DE 12 - M
                .withBatchNumber(refundResponse.getTransactionReference().getBatchNumber(),
                        refundResponse.getTransactionReference().getSequenceNumber())
                .execute();
        assertNotNull(capture);
    }

    //CashBenefit only
    @Test
    public void test_ebt_withdrawal_capture_cashCard() throws ApiException {
        IStanProvider stan = StanGenerator.getInstance();
        IBatchProvider batch = BatchProvider.getInstance();
        String date = DateTime.now().toString("yyMMddhhmmss");

        Transaction response = cashCard.benefitWithdrawal(new BigDecimal(10))
                .withCurrency("USD")
                .withSystemTraceAuditNumber(stan.generateStan())
                .withTimestamp(date)
                .withBatchNumber(batch.getBatchNumber(),batch.getSequenceNumber())
                .withUniqueDeviceId("2402")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        PriorMessageInformation pmi = response.getMessageInformation();

        Transaction authResponse = Transaction.fromBuilder()
                .withAmount(new BigDecimal("10")) //Original Amount
                .withAuthorizationCode(response.getAuthorizationCode()) // Auth Code from response
                .withNtsData(response.getNtsData())
                .withPaymentMethod(cashCard) //Original Payment method
                .withMessageTypeIndicator(pmi.getMessageTransactionIndicator()) //Original MTI DE 56 M
                .withSystemTraceAuditNumber(pmi.getSystemTraceAuditNumber()) //Original Stan DE 56 M
                .withTransactionTime(response.getOriginalTransactionTime()) //Original Transaction Time DE 56 M
                .withProcessingCode(response.getProcessingCode()) //original Processing code DE 3 M
                .build();

        Transaction capture = authResponse.preAuthCompletion()
                .withSystemTraceAuditNumber(stan.generateStan())
                .withTimestamp(date)
                .withBatchNumber(response.getTransactionReference().getBatchNumber(), response.getTransactionReference().getSequenceNumber())
                .withPriorMessageInformation(pmi) //DE 48-39 M
                .execute();
        assertNotNull(capture);
        assertEquals("000", capture.getResponseCode());
    }

        @Test
        public void test_ebt_voice_capture_sale() throws ApiException {
            IStanProvider stan = StanGenerator.getInstance();
            IBatchProvider batch = BatchProvider.getInstance();
            String date = DateTime.now().toString("yyMMddhhmmss");

            EBTCardData ebtCard = new EBTCardData(EbtCardType.FoodStamp);
            ebtCard.setNumber("4012002000060016");
            ebtCard.setExpMonth(12);
            ebtCard.setExpYear(2025);
            ebtCard.setReaderPresent(true);
            ebtCard.setCardPresent(true);
            ebtCard.setPinBlock("32539F50C245A6A93D123412324000AA");

            Transaction saleResponse = ebtCard.charge(new BigDecimal("10")) //Amount : DE 4
                    .withCurrency("USD")
                    .withSystemTraceAuditNumber(stan.generateStan()) //STAN : DE 11
                    .withBatchNumber(batch.getBatchNumber(), batch.getSequenceNumber()) // DE 48-4
                    .withTimestamp(date) // DE 12
                    .withUniqueDeviceId("0001") //DE 62
                    .execute();
            assertNotNull(saleResponse);

            PriorMessageInformation pmi = saleResponse.getMessageInformation();

            Transaction rebuild = Transaction.fromBuilder()
                    .withAmount(new BigDecimal("10")) //original transaction amount
                    .withAuthorizationCode(saleResponse.getAuthorizationCode()) // auth code from sales response DE 38 M
                    .withAuthorizedAmount(saleResponse.getAuthorizedAmount()) //Approved Amount
                    .withNtsData(new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized, DebitAuthorizerCode.UnknownAuthorizer))
                    .withPaymentMethod(ebtCard) //Original Payment method DE 48-11 M
                    .withMessageTypeIndicator(pmi.getMessageTransactionIndicator()) // original MTI DE 56 M
                    .withSystemTraceAuditNumber(pmi.getSystemTraceAuditNumber()) // Original STAN DE 56 M
                    .withTransactionTime(saleResponse.getOriginalTransactionTime()) // original transaction time DE 56 M
                    .withProcessingCode(saleResponse.getProcessingCode()) //original Processing code DE 3 M
                    .build();

        Transaction response = rebuild.capture()
                .withSystemTraceAuditNumber(stan.generateStan())
                .withTimestamp(date)
                .withReferenceNumber("123456789012345")
                .withBatchNumber(saleResponse.getTransactionReference().getBatchNumber(),saleResponse.getTransactionReference().getSequenceNumber())
                .withPriorMessageInformation(pmi)
                .execute();
        assertNotNull(response);

            pmi = response.getMessageInformation();
        // check message data
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("008000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());
        assertEquals("1378", pmi.getMessageReasonCode());

        // check result
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_ebt_voice_capture_return() throws ApiException {
        IStanProvider stan = StanGenerator.getInstance();
        IBatchProvider batch = BatchProvider.getInstance();
        String date = DateTime.now().toString("yyMMddhhmmss");
        EBTCardData foodCard = new EBTCardData(EbtCardType.FoodStamp);
        foodCard.setNumber("4012002000060016");
        foodCard.setExpMonth(12);
        foodCard.setExpYear(2025);
        foodCard.setReaderPresent(true);
        foodCard.setCardPresent(true);
        foodCard.setPinBlock("32539F50C245A6A93D123412324000AA");

        Transaction refundResponse = foodCard.refund(new BigDecimal("10"))
                .withCurrency("USD")
                .withSystemTraceAuditNumber(stan.generateStan())
                .withTimestamp(date)
                .withUniqueDeviceId("2402")
                .withBatchNumber(batch.getBatchNumber(), batch.getSequenceNumber())
                .execute();
        assertNotNull(refundResponse);

        PriorMessageInformation pmi = refundResponse.getMessageInformation();

        Transaction rebuild = Transaction.fromBuilder()
                .withAmount(new BigDecimal("10")) //original transaction amount
                .withAuthorizationCode(refundResponse.getAuthorizationCode()) // auth code from sales response DE 38 M
                .withAuthorizedAmount(refundResponse.getAuthorizedAmount()) //Approved Amount
                .withNtsData(new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized, DebitAuthorizerCode.UnknownAuthorizer))
                .withPaymentMethod(foodCard) //Original Payment method DE 48-11 M
                .withMessageTypeIndicator(pmi.getMessageTransactionIndicator()) // original MTI DE 56 M
                .withSystemTraceAuditNumber(pmi.getSystemTraceAuditNumber()) // Original STAN DE 56 M
                .withTransactionTime(refundResponse.getOriginalTransactionTime()) // original transaction time DE 56 M
                .withProcessingCode(refundResponse.getProcessingCode()) //original Processing code DE 3 M
                .build();

        Transaction response = rebuild.capture()
                .withSystemTraceAuditNumber(stan.generateStan())
                .withTimestamp(date)
                .withReferenceNumber("123456789012345")
                .withBatchNumber(refundResponse.getTransactionReference().getBatchNumber(),refundResponse.getTransactionReference().getSequenceNumber())
                .withPriorMessageInformation(pmi)
                .execute();
        assertNotNull(response);

        pmi = response.getMessageInformation();
        // check message data
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("200080", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());
        assertEquals("1378", pmi.getMessageReasonCode());

        // check result
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_EBT_sale_capture_CashBenefit() throws ApiException {
        IStanProvider stan = StanGenerator.getInstance();
        IBatchProvider batch = BatchProvider.getInstance();
        String date = DateTime.now().toString("yyMMddhhmmss");

        Transaction saleResponse = cashCard.charge(new BigDecimal("10")) //Amount : DE 4
                .withCurrency("USD")
                .withSystemTraceAuditNumber(stan.generateStan()) //STAN : DE 11
                .withBatchNumber(batch.getBatchNumber(), batch.getSequenceNumber()) // DE 48-4
                .withTimestamp(date) // DE 12
                .withUniqueDeviceId("1001")
                .execute();
        assertNotNull(saleResponse);

        PriorMessageInformation pmi = saleResponse.getMessageInformation();

        Transaction rebuild = Transaction.fromBuilder()
                .withAmount(new BigDecimal("10")) //original transaction amount
                .withAuthorizationCode(saleResponse.getAuthorizationCode()) // auth code from sales response DE 38 M
                .withNtsData(saleResponse.getNtsData()) //DE 62-NTS Need to check, as we are receiving this parameter missing
                .withPaymentMethod(cashCard) //Original Payment method DE 48-11 M
                .withMessageTypeIndicator(pmi.getMessageTransactionIndicator()) // original MTI DE 56 M
                .withSystemTraceAuditNumber(pmi.getSystemTraceAuditNumber()) // Original STAN DE 56 M
                .withTransactionTime(saleResponse.getOriginalTransactionTime()) // original transaction time DE 56 M
                .withProcessingCode(saleResponse.getProcessingCode()) //original Processing code DE 3 M
                .build();

        Transaction capture = rebuild.preAuthCompletion()
                .withSystemTraceAuditNumber(stan.generateStan()) //DE 11
                .withTimestamp(date)    //DE 12 - M
                .withPriorMessageInformation(pmi) //DE 48-39
                .withBatchNumber(saleResponse.getTransactionReference().getBatchNumber(),
                        saleResponse.getTransactionReference().getSequenceNumber())
                .execute();
        assertNotNull(capture);
        assertEquals("000", capture.getResponseCode());
    }

    @Test
    public void test_EBT_sale_with_cashback_capture_CashBenefit() throws ApiException {
        IStanProvider stan = StanGenerator.getInstance();
        IBatchProvider batch = BatchProvider.getInstance();
        String date = DateTime.now().toString("yyMMddhhmmss");

        Transaction saleResponse = cashCard.charge(new BigDecimal("10")) //Amount : DE 4
                .withCurrency("USD")
                .withSystemTraceAuditNumber(stan.generateStan()) //STAN : DE 11
                .withBatchNumber(batch.getBatchNumber(), batch.getSequenceNumber()) // DE 48-4
                .withTimestamp(date) // DE 12
                .withCashBack(new BigDecimal("5"))
                .withUniqueDeviceId("1001")
                .execute();
        assertNotNull(saleResponse);

        PriorMessageInformation pmi = saleResponse.getMessageInformation();

        Transaction rebuild = Transaction.fromBuilder()
                .withAmount(new BigDecimal("10")) //original transaction amount
                .withAuthorizationCode(saleResponse.getAuthorizationCode()) // auth code from sales response DE 38 M
                .withNtsData(saleResponse.getNtsData())
                .withPaymentMethod(cashCard) //Original Payment method DE 48-11 M
                .withMessageTypeIndicator(pmi.getMessageTransactionIndicator()) // original MTI DE 56 M
                .withSystemTraceAuditNumber(pmi.getSystemTraceAuditNumber()) // Original STAN DE 56 M
                .withTransactionTime(saleResponse.getOriginalTransactionTime()) // original transaction time DE 56 M
                .withProcessingCode(saleResponse.getProcessingCode()) //original Processing code DE 3 M
                .build();

        Transaction capture = rebuild.preAuthCompletion()
                .withSystemTraceAuditNumber(stan.generateStan()) //DE 11
                .withTimestamp(date)    //DE 12 - M
                .withPriorMessageInformation(pmi) //DE 48-39
                .withBatchNumber(saleResponse.getTransactionReference().getBatchNumber(),
                        saleResponse.getTransactionReference().getSequenceNumber())
                .execute();
        assertNotNull(capture);
        assertEquals("000", capture.getResponseCode());
    }

    @Test
    public void test_ebt_voice_capture_sale_Retransmit() throws ApiException {
        IStanProvider stan = StanGenerator.getInstance();
        IBatchProvider batch = BatchProvider.getInstance();
        String date = DateTime.now().toString("yyMMddhhmmss");

        Transaction saleResponse = foodCardData.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withSystemTraceAuditNumber(stan.generateStan())
                .withTimestamp(date)
                .withUniqueDeviceId("2402")
                .withBatchNumber(batch.getBatchNumber(), batch.getSequenceNumber())
                .execute();
        assertNotNull(saleResponse);

        PriorMessageInformation pmi = saleResponse.getMessageInformation();

        Transaction rebuild = Transaction.fromBuilder()
                .withAmount(new BigDecimal("10")) //original transaction amount
                .withAuthorizationCode(saleResponse.getAuthorizationCode()) // auth code from sales response DE 38 M
                .withAuthorizedAmount(saleResponse.getAuthorizedAmount()) //Approved Amount
                .withNtsData(new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized, DebitAuthorizerCode.UnknownAuthorizer))
                .withPaymentMethod(foodCardData) //Original Payment method DE 48-11 M
                .withMessageTypeIndicator(pmi.getMessageTransactionIndicator()) // original MTI DE 56 M
                .withSystemTraceAuditNumber(pmi.getSystemTraceAuditNumber()) // Original STAN DE 56 M
                .withTransactionTime(saleResponse.getOriginalTransactionTime()) // original transaction time DE 56 M
                .withProcessingCode(saleResponse.getProcessingCode()) //original Processing code DE 3 M
                .build();

        Transaction response = rebuild.capture()
                .withSystemTraceAuditNumber(stan.generateStan())
                .withTimestamp(date)
                .withReferenceNumber("123456789012345")
                .withBatchNumber(saleResponse.getTransactionReference().getBatchNumber(),saleResponse.getTransactionReference().getSequenceNumber())
                .withPriorMessageInformation(pmi)
                .execute();
        assertNotNull(response);

        pmi = response.getMessageInformation();
        // check message data
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("008000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());
        assertEquals("1378", pmi.getMessageReasonCode());

        // check result
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction resubmitDataCollect = NetworkService.resubmitDataCollect(response.getTransactionToken())
                .withTimestamp(date)
                .execute();
        assertNotNull(resubmitDataCollect);
    }

    @Test
    public void test_ebt_voice_capture_Retransmit() throws ApiException {
        IStanProvider stan = StanGenerator.getInstance();
        IBatchProvider batch = BatchProvider.getInstance();
        String date = DateTime.now().toString("yyMMddhhmmss");

        Transaction refundResponse = foodCardData.refund(new BigDecimal("10"))
                .withCurrency("USD")
                .withSystemTraceAuditNumber(stan.generateStan())
                .withTimestamp(date)
                .withUniqueDeviceId("2402")
                .withBatchNumber(batch.getBatchNumber(), batch.getSequenceNumber())
                .execute();
        assertNotNull(refundResponse);

        PriorMessageInformation pmi = refundResponse.getMessageInformation();

        Transaction rebuild = Transaction.fromBuilder()
                .withAmount(new BigDecimal("10")) //original transaction amount
                .withAuthorizationCode(refundResponse.getAuthorizationCode()) // auth code from sales response DE 38 M
                .withAuthorizedAmount(refundResponse.getAuthorizedAmount()) //Approved Amount
                .withNtsData(new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized, DebitAuthorizerCode.UnknownAuthorizer))
                .withPaymentMethod(foodCardData) //Original Payment method DE 48-11 M
                .withMessageTypeIndicator(pmi.getMessageTransactionIndicator()) // original MTI DE 56 M
                .withSystemTraceAuditNumber(pmi.getSystemTraceAuditNumber()) // Original STAN DE 56 M
                .withTransactionTime(refundResponse.getOriginalTransactionTime()) // original transaction time DE 56 M
                .withProcessingCode(refundResponse.getProcessingCode()) //original Processing code DE 3 M
                .build();

        Transaction response = rebuild.capture()
                .withSystemTraceAuditNumber(stan.generateStan())
                .withTimestamp(date)
                .withReferenceNumber("123456789012345")
                .withBatchNumber(refundResponse.getTransactionReference().getBatchNumber(),refundResponse.getTransactionReference().getSequenceNumber())
                .withPriorMessageInformation(pmi)
                .execute();
        assertNotNull(response);

        pmi = response.getMessageInformation();
        // check message data
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("008000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());
        assertEquals("1378", pmi.getMessageReasonCode());

        // check result
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction resubmitDataCollect = NetworkService.resubmitDataCollect(response.getTransactionToken())
                .withTimestamp(date)
                .execute();
        assertNotNull(resubmitDataCollect);
    }

    @Test
    public void test_EBT_sale_capture_CashBenefit_Retransmit() throws ApiException {
        IStanProvider stan = StanGenerator.getInstance();
        IBatchProvider batch = BatchProvider.getInstance();
        String date = DateTime.now().toString("yyMMddhhmmss");

        Transaction saleResponse = cashCard.charge(new BigDecimal("10")) //Amount : DE 4
                .withCurrency("USD")
                .withSystemTraceAuditNumber(stan.generateStan()) //STAN : DE 11
                .withBatchNumber(batch.getBatchNumber(), batch.getSequenceNumber()) // DE 48-4
                .withTimestamp(date) // DE 12
                .withUniqueDeviceId("1001")
                .execute();
        assertNotNull(saleResponse);

        PriorMessageInformation pmi = saleResponse.getMessageInformation();

        Transaction rebuild = Transaction.fromBuilder()
                .withAmount(new BigDecimal("10")) //original transaction amount
                .withAuthorizationCode(saleResponse.getAuthorizationCode()) // auth code from sales response DE 38 M
                .withNtsData(saleResponse.getNtsData()) //DE 62-NTS Need to check, as we are receiving this parameter missing
                .withPaymentMethod(cashCard) //Original Payment method DE 48-11 M
                .withMessageTypeIndicator(pmi.getMessageTransactionIndicator()) // original MTI DE 56 M
                .withSystemTraceAuditNumber(pmi.getSystemTraceAuditNumber()) // Original STAN DE 56 M
                .withTransactionTime(saleResponse.getOriginalTransactionTime()) // original transaction time DE 56 M
                .withProcessingCode(saleResponse.getProcessingCode()) //original Processing code DE 3 M
                .build();

        Transaction capture = rebuild.preAuthCompletion()
                .withSystemTraceAuditNumber(stan.generateStan()) //DE 11
                .withTimestamp(date)    //DE 12 - M
                .withPriorMessageInformation(pmi) //DE 48-39
                .withBatchNumber(saleResponse.getTransactionReference().getBatchNumber(),
                        saleResponse.getTransactionReference().getSequenceNumber())
                .execute();
        assertNotNull(capture);
        assertEquals("000", capture.getResponseCode());

        Transaction resubmitDataCollect = NetworkService.resubmitDataCollect(capture.getTransactionToken())
                .withTimestamp(date)
                .execute();
        assertNotNull(resubmitDataCollect);
    }

    @Test
    public void test_EBT_sale_capture_FoodStamp_Retransmit() throws ApiException {
        IStanProvider stan = StanGenerator.getInstance();
        IBatchProvider batch = BatchProvider.getInstance();
        String date = DateTime.now().toString("yyMMddhhmmss");

        Transaction saleResponse = foodCard.charge(new BigDecimal("10")) //Amount : DE 4
                .withCurrency("USD")
                .withSystemTraceAuditNumber(stan.generateStan()) //STAN : DE 11
                .withBatchNumber(batch.getBatchNumber(), batch.getSequenceNumber()) // DE 48-4
                .withTimestamp(date) // DE 12
                .withUniqueDeviceId("1001")
                .execute();
        assertNotNull(saleResponse);

        PriorMessageInformation pmi = saleResponse.getMessageInformation();

        Transaction rebuild = Transaction.fromBuilder()
                .withAmount(new BigDecimal("10")) //original transaction amount
                .withAuthorizationCode(saleResponse.getAuthorizationCode()) // auth code from sales response DE 38 M
                .withNtsData(saleResponse.getNtsData()) //DE 62-NTS Need to check, as we are receiving this parameter missing
                .withPaymentMethod(foodCard) //Original Payment method DE 48-11 M
                .withMessageTypeIndicator(pmi.getMessageTransactionIndicator()) // original MTI DE 56 M
                .withSystemTraceAuditNumber(pmi.getSystemTraceAuditNumber()) // Original STAN DE 56 M
                .withTransactionTime(saleResponse.getOriginalTransactionTime()) // original transaction time DE 56 M
                .withProcessingCode(saleResponse.getProcessingCode()) //original Processing code DE 3 M
                .build();

        Transaction capture = rebuild.preAuthCompletion()
                .withSystemTraceAuditNumber(stan.generateStan()) //DE 11
                .withTimestamp(date)    //DE 12 - M
                .withPriorMessageInformation(pmi) //DE 48-39
                .withBatchNumber(saleResponse.getTransactionReference().getBatchNumber(),
                        saleResponse.getTransactionReference().getSequenceNumber())
                .execute();
        assertNotNull(capture);
        assertEquals("000", capture.getResponseCode());

        Transaction resubmitDataCollect = NetworkService.resubmitDataCollect(capture.getTransactionToken())
                .withTimestamp(date)
                .execute();
        assertNotNull(resubmitDataCollect);
    }
}

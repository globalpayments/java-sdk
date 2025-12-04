package com.global.api.tests.network.vaps.Certification;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.EbtCardType;
import com.global.api.entities.enums.EmvChipCondition;
import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.enums.InquiryType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.EBTCardData;
import com.global.api.paymentMethods.EBTTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VapsEBTEMVTestCert {
    private EBTTrackData cashCard;
    private EBTTrackData foodCard;
    private EBTCardData foodCardData;
    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;

    private String emvData = "4F07A000000004101050104D415354455243415244204445424954820218008407A00000000410108E120000000000000000420102055E0342031F00950580000080009A031901099B0268009C01405F24032212315F25031711015F2A0208405F300202015F3401119F01060000000000019F02060000000006009F03060000000000009F0607A00000000410109F0702FFC09F090200029F0D05B0509C88009F0E0500000000009F0F05B0709C98009F10120110A00003220000000000000000000000FF9F12104D6173746572636172642044656269749F160F3132333435363738393031323334359F1A0208409F1C0831313232333334349F1E0831323334353637389F21030647199F26084233C50A9D5D7FA29F2701809F330360F0C89F34035E03009F3501219F360201259F3704FF4CA1CD9F3901059F4005F000A0B0019F4104000000809F4E0D54657374204D65726368616E74";
    public VapsEBTEMVTestCert() throws ApiException {
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
        acceptorConfig.setCardDataInputMode(DE22_CardDataInputMode.ContactlessEmv);

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
        config.setTerminalId("0007998855611");
        config.setUniqueDeviceId("0001");
        config.setMerchantType("5542");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());

        ServicesContainer.configureService(config);

        // cash card
        cashCard = new EBTTrackData(EbtCardType.CashBenefit);
        cashCard.setValue("4355567063338=2012101HJNw/ewskBgnZqkL");
        cashCard.setPinBlock("62968D2481D231E1A504010024A00014");

        // cash card
        foodCard = new EBTTrackData(EbtCardType.FoodStamp);
        foodCard.setValue("4355567063338=2012101HJNw/ewskBgnZqkL");
        foodCard.setPinBlock("62968D2481D231E1A504010024A00014");

        foodCardData = new EBTCardData(EbtCardType.FoodStamp);
        foodCardData.setNumber("4012002000060016");
        foodCardData.setExpMonth(12);
        foodCardData.setExpYear(2025);
        foodCardData.setReaderPresent(true);
        foodCardData.setCardPresent(true);
        foodCardData.setPinBlock("32539F50C245A6A93D123412324000AA");
    }

    @Test
    public void test_EBT_EMV_balance_inquiry() throws ApiException {
        EBTTrackData trackData = new EBTTrackData(EbtCardType.CashBenefit);
        trackData.setValue(";9840000921111111123=491200000000?");
        trackData.setPinBlock("32539F50C245A6A93D123412324000AA");
        trackData.setEntryMethod(EntryMethod.ContactEMV);

        Transaction response = trackData.balanceInquiry(InquiryType.Cash)
                .withFee(FeeType.TransactionFee, new BigDecimal(1))
                .withCurrency("USD")
                .withTagData(emvData)
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
    public void test_EBT_EMV_sale() throws ApiException {
        EBTTrackData cashCard = new EBTTrackData(EbtCardType.CashBenefit);
        cashCard.setValue(";9840000921111111149=491200000000?");
        cashCard.setPinBlock("32539F50C245A6A93D123412324000AA");
        cashCard.setEntryMethod(EntryMethod.ContactEMV);

        Transaction response = cashCard.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withFee(FeeType.TransactionFee, new BigDecimal(1))
                .withTagData("4F07A000000004101050104D415354455243415244204445424954820218008407A00000000410108E120000000000000000420102055E0342031F00950580000080009A031901099B0268009C01405F24032212315F25031711015F2A0208405F300202015F3401119F01060000000000019F02060000000006009F03060000000000009F0607A00000000410109F0702FFC09F090200029F0D05B0509C88009F0E0500000000009F0F05B0709C98009F10120110A00003220000000000000000000000FF9F12104D6173746572636172642044656269749F160F3132333435363738393031323334359F1A0208409F1C0831313232333334349F1E0831323334353637389F21030647199F26084233C50A9D5D7FA29F2701809F330360F0C89F34035E03009F3501219F360201259F3704FF4CA1CD9F3901059F4005F000A0B0019F4104000000809F4E0D54657374204D65726368616E74")
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
    public void test_EBT_EMV_sale_cash_back() throws ApiException {
        EBTTrackData cashCard = new EBTTrackData(EbtCardType.CashBenefit);
        cashCard.setValue(";9840000921111111149=491200000000?");
        cashCard.setPinBlock("32539F50C245A6A93D123412324000AA");
        cashCard.setEntryMethod(EntryMethod.ContactEMV);
        Transaction response = cashCard.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(5))
                .withTagData(emvData)
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
    public void test_EBT_EMV_benefit_withdrawal() throws ApiException {
        EBTTrackData cashCard = new EBTTrackData(EbtCardType.CashBenefit);
        cashCard.setValue(";9840000921111111149=491200000000?");
        cashCard.setPinBlock("32539F50C245A6A93D123412324000AA");
        cashCard.setEntryMethod(EntryMethod.ContactEMV);
        Transaction response = cashCard.benefitWithdrawal(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvData)
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
    public void test_foodStanp_refund_by_card() throws ApiException {
        EBTTrackData foodCard = new EBTTrackData(EbtCardType.FoodStamp);
        foodCard.setValue(";9840000921111111149=491200000000?");
        foodCard.setPinBlock("32539F50C245A6A93D123412324000AA");
        foodCard.setEntryMethod(EntryMethod.ContactEMV);
        Transaction response = foodCard.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvData)
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
    /**
     *  EBT EMV Fallback to Track 2 functionality.
     * This section is intended for handling scenarios where a chip card
     * transaction falls back to using the magnetic stripe due to chip failure.
     */
    @Test
    public void test_Fallback_balance_inquiry() throws ApiException {
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactEmv_MagStripe);
        acceptorConfig.setCardDataInputMode(DE22_CardDataInputMode.MagStripe_Fallback);

        EBTTrackData trackData = new EBTTrackData(EbtCardType.CashBenefit);
        trackData.setValue(";9840000921111111123=491200000000?");
        trackData.setPinBlock("62968D2481D231E1A504010024A00014");

        Transaction response = trackData.balanceInquiry(InquiryType.Cash)
                .withFee(FeeType.TransactionFee, new BigDecimal(1))
                .withChipCondition(EmvChipCondition.ChipFailPreviousFail)
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
    public void test_217_Fallback_sale() throws ApiException {
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactEmv_MagStripe);

        EBTTrackData trackData = new EBTTrackData(EbtCardType.CashBenefit);
        trackData.setValue(";9840000921111111149=491200000000?");
        trackData.setPinBlock("62968D2481D231E1A504010024A00014");

        Transaction response = trackData.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withChipCondition(EmvChipCondition.ChipFailPreviousFail)
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
    public void test_fallback_sale_TransactionFee() throws ApiException {
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactEmv_MagStripe);
        acceptorConfig.setCardDataInputMode(DE22_CardDataInputMode.MagStripe_Fallback);

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
    public void test_Fallback_benefit_withdrawal() throws ApiException {
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactEmv_MagStripe);

        Transaction response = cashCard.benefitWithdrawal(new BigDecimal(10))
                .withCurrency("USD")
                .withChipCondition(EmvChipCondition.ChipFailPreviousFail)
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
    public void test_Fallback_foodStamp_balance_inquiry() throws ApiException {
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactEmv);

        EBTTrackData trackData = new EBTTrackData(EbtCardType.FoodStamp);
        trackData.setValue(";9840000921111111123=491200000000?");
        trackData.setPinBlock("62968D2481D231E1A504010024A00014");

        Transaction response = trackData.balanceInquiry(InquiryType.Foodstamp)
                .withChipCondition(EmvChipCondition.ChipFailPreviousFail)
                .withCurrency("USD")
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
    public void test_217_Fallback_FoodStamp_sale() throws ApiException {
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactEmv_MagStripe);
        acceptorConfig.setCardDataInputMode(DE22_CardDataInputMode.MagStripe_Fallback);

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
    public void test_Fallback_foodStamp_refund() throws ApiException {
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactEmv_ContactlessMsd);

        Transaction response = foodCard.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withChipCondition(EmvChipCondition.ChipFailPreviousFail)
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
    public void test_swipe_ebt_decline_advice() throws ApiException {
        EBTTrackData trackData = new EBTTrackData(EbtCardType.CashBenefit);
        trackData.setValue(";9840000921111111149=491200000000?");
        trackData.setPinBlock("62968D2481D231E1A504010024A00014");
        trackData.setEntryMethod(EntryMethod.ContactEMV);

        Transaction response = trackData.offlineDecline(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData("4F07A0000000041010500A4D617374657243617264820238008407A00000000410108E0A00000000000000001F00950500008080009A031901099B02E8009C01405F24032212315F25030401015F2A0208405F300202015F3401009F01060000000000019F02060000000006009F03060000000000009F0607A00000000410109F0702FF009F090200029F0D05B8508000009F0E0500000000009F0F05B8708098009F10120110A0800F22000065C800000000000000FF9F120A4D6173746572436172649F160F3132333435363738393031323334359F1A0208409F1C0831313232333334349F1E0831323334353637389F21030710109F26080631450565A30B759F2701809F330360F0C89F34033F00019F3501219F360200049F3704C6B1A04F9F3901059F4005F000A0B0019F4104000000869F4C0865C862608A23945A9F4E0D54657374204D65726368616E74")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1120", pmi.getMessageTransactionIndicator());
        assertEquals("008100", pmi.getProcessingCode());
        assertEquals("190", pmi.getFunctionCode());

        // check result
        assertEquals(response.getResponseMessage(), "900", response.getResponseCode());
    }

    @Test
    public void test_manual_advice() throws ApiException {
        EBTCardData ebtCard = new EBTCardData(EbtCardType.CashBenefit);
        ebtCard.setNumber("4012002000060016");
        ebtCard.setExpMonth(12);
        ebtCard.setExpYear(2025);
        ebtCard.setPinBlock("32539F50C245A6A93D123412324000AA");

        Transaction response = ebtCard.offlineDecline(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData("4F07A0000000041010500A4D617374657243617264820238008407A00000000410108E0A00000000000000001F00950500008080009A031901099B02E8009C01405F24032212315F25030401015F2A0208405F300202015F3401009F01060000000000019F02060000000006009F03060000000000009F0607A00000000410109F0702FF009F090200029F0D05B8508000009F0E0500000000009F0F05B8708098009F10120110A0800F22000065C800000000000000FF9F120A4D6173746572436172649F160F3132333435363738393031323334359F1A0208409F1C0831313232333334349F1E0831323334353637389F21030710109F26080631450565A30B759F2701809F330360F0C89F34033F00019F3501219F360200049F3704C6B1A04F9F3901059F4005F000A0B0019F4104000000869F4C0865C862608A23945A9F4E0D54657374204D65726368616E74")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1120", pmi.getMessageTransactionIndicator());
        assertEquals("008100", pmi.getProcessingCode());
        assertEquals("190", pmi.getFunctionCode());

        // check result
        assertEquals(response.getResponseMessage(), "900", response.getResponseCode());
    }


}

package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayTimeoutException;
import com.global.api.network.abstractions.IBatchProvider;
import com.global.api.network.abstractions.IStanProvider;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.services.NetworkService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.TestCards;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VapsCreditTests {
    private CreditCardData card;
    private CreditTrackData track;
    private AcceptorConfig acceptorConfig;

    public VapsCreditTests() throws ApiException {
        Address address = new Address();
        address.setName("My STORE");
        address.setStreetAddress1("1 MY STREET");
        address.setCity("MYTOWN");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setType(AddressType.Shipping);
        address.setCountry("USA");

        acceptorConfig = new AcceptorConfig();
        acceptorConfig.setAddress(address);

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_MagStripe_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.ByMerchant);
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
        acceptorConfig.setSupportsEmvPin(true);
        acceptorConfig.setSupportWexAdditionalProducts(true);
        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.Fuel);


        // gateway config
        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0044");
        config.setTerminalId("0007998855611");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());

        ServicesContainer.configureService(config);

        // with merchant type
        config.setMerchantType("5542");
        ServicesContainer.configureService(config, "ICR");

        // VISA
        card = TestCards.VisaManual(true, true);
        track = TestCards.VisaSwipe();

        // VISA CORPORATE
//        card = TestCards.VisaCorporateManual(true, true);
//        cashCard = TestCards.VisaCorporateSwipe();

        // VISA PURCHASING
//        card = TestCards.VisaPurchasingManual(true, true);
//        cashCard = TestCards.VisaPurchasingSwipe();

        // MASTERCARD
//        card = TestCards.MasterCardManual(true, true);
//        track = TestCards.MasterCardSwipe();

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

    @Test
    public void test_000_batch_close() throws ApiException {
        BatchSummary summary = BatchService.closeBatch();
        assertNotNull(summary);
        assertTrue(summary.isBalanced());
    }

    /*
    001 - Check Services
    002 - Check Services
    */

    @Test
    public void test_003_manual_authorization() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(10.456),true)
                .withCurrency("USD")
                .withFee(FeeType.TransactionFee,new BigDecimal(1))
                .withMasterCardIndicator(MasterCardCITMITIndicator.CARDHOLDER_INITIATED_CREDENTIAL_ON_FILE)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_004_manual_sale() throws ApiException {
        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_005_swipe_verify() throws ApiException {
        Transaction response = track.verify().execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("313000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_006_swipe_authorization() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        Transaction recreated = Transaction.fromNetwork(
                response.getAuthorizedAmount(),
                response.getAuthorizationCode(),
                response.getNtsData(),
                track,
                response.getMessageTypeIndicator(),
                response.getSystemTraceAuditNumber(),
                response.getOriginalTransactionTime(),
                response.getProcessingCode()
        );

        // check response
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        ManagementBuilder builder = recreated.voidTransaction()
                .withForceToHost(true);

        for(CardIssuerEntryTag key: response.getIssuerData().keySet()) {
            builder.withIssuerData(key, response.getIssuerData().get(key));
        }

        Transaction voidResponse = builder
                .execute();
        assertNotNull(voidResponse);
        assertEquals("400", voidResponse.getResponseCode());
    }

    @Test
    public void test_007_swipe_sale() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_007_swipe_sale_mc_Indicator() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withMasterCardIndicator(MasterCardCITMITIndicator.CARDHOLDER_INITIATED_SUBSCRIPTION)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_008_swipe_refund_mc_Indicator() throws ApiException {
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withMasterCardIndicator(MasterCardCITMITIndicator.MERCHANT_INITIATED_DELAYED_CHARGE)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("200030", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_008_swipe_refund() throws ApiException {
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("200030", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_008_swipe_forceRefund_10297() throws ApiException {
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("200030", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());

        Transaction res = NetworkService.forcedRefund(response.getTransactionToken())
                .withCurrency("USD")
                .execute();
        // check response
        assertEquals("000", res.getResponseCode());

    }

    @Test
    public void test_009_swipe_stand_in_capture() throws ApiException {
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.Received_IssuerTimeout, AuthorizerCode.Terminal_Authorized),
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
    public void test_010_swipe_voice_capture() throws ApiException {
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized),
                track
        );

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withTerminalError(true)
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
    public void test_011_swipe_void() throws ApiException {
        Transaction sale = track.charge(new BigDecimal(12))
                .withCurrency("USD")
                .execute();
        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());

        Transaction response = sale.voidTransaction()
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4351", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }

    @Test
    public void test_011_swipe_void_mc_Indicator() throws ApiException {
        Transaction sale = track.charge(new BigDecimal(12))
                .withCurrency("USD")
                .withMasterCardIndicator(MasterCardCITMITIndicator.MERCHANT_INITIATED_DELAYED_CHARGE)
                .execute();
        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());

        Transaction response = sale.voidTransaction()
                .withMasterCardIndicator(MasterCardCITMITIndicator.MERCHANT_INITIATED_DELAYED_CHARGE)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4351", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }

    @Test
    public void test_011_swipe_void_forced() throws ApiException {
        Transaction sale = track.charge(new BigDecimal(12))
                .withCurrency("USD")
                .execute();
        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());

        String NM_1 = sale.getIssuerData().get(CardIssuerEntryTag.NTS_MastercardBankNet_ReferenceNumber);
        String NM_2 = sale.getIssuerData().get(CardIssuerEntryTag.NTS_MastercardBankNet_SettlementDate);

        assertNotNull(NM_1);
        assertNotNull(NM_2);
        assertNotNull(sale.getReferenceNumber());

        Transaction response = sale.voidTransaction()
                .withForceToHost(true)
                .withIssuerData(CardIssuerEntryTag.NTS_MastercardBankNet_ReferenceNumber, NM_1)
                .withIssuerData(CardIssuerEntryTag.NTS_MastercardBankNet_SettlementDate, NM_2)
                .withReferenceNumber(sale.getReferenceNumber())
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4356", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }

    @Test
    public void test_011_forced_swipe_void_full() throws ApiException {
        Transaction sale = track.authorize(new BigDecimal(12))
                .withCurrency("USD")
                .execute();
        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());

        ManagementBuilder builder = sale.voidTransaction()
                .withForceToHost(true);

        HashMap<CardIssuerEntryTag, String> issuerData = sale.getIssuerData();
        for(CardIssuerEntryTag key: issuerData.keySet()) {
            builder.withIssuerData(key, issuerData.get(key));
        }

        Transaction response = builder.execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4356", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }

    @Test
    public void test_011_forced_swipe_void_partial() throws ApiException {
        Transaction sale = track.charge(new BigDecimal(12))
                .withCurrency("USD")
                .execute();
        assertNotNull(sale);

        //temporarly set partial approval to true
        sale.getTransactionReference().setPartialApproval(true);
        sale.setResponseCode("002");

        assertEquals("002", sale.getResponseCode());

        ManagementBuilder builder = sale.voidTransaction(new BigDecimal(6), true)
                .withForceToHost(true);

        HashMap<CardIssuerEntryTag, String> issuerData = sale.getIssuerData();
        for(CardIssuerEntryTag key: issuerData.keySet()) {
            builder.withIssuerData(key, issuerData.get(key));
        }

        Transaction response = builder.execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4355", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }

    @Test
    public void test_012_swipe_partial_void() throws ApiException {
        Transaction sale = track.charge(new BigDecimal(142))
                .withCurrency("USD")
                .execute();
        assertNotNull(sale);
        //assertEquals("002", sale.getResponseCode());

        Transaction response = sale.voidTransaction()
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4351", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }

    @Test(expected = GatewayTimeoutException.class)
    public void test_014_swipe_reverse_sale() throws ApiException {
        track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withSimulatedHostErrors(Host.Primary, HostError.Timeout)
                .withSimulatedHostErrors(Host.Secondary, HostError.Timeout)
                .execute();
    }

    @Test
    public void test_016_ICR_authorization() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // test_017
        Transaction captureResponse = response.capture(new BigDecimal(12))
                .withCurrency("USD")
                .withTerminalError(true)
                .execute("ICR");
        assertNotNull(captureResponse);

        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("1376", pmi.getMessageReasonCode());
        assertEquals("202", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_018_ICR_partial_authorization() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // test_019
        Transaction captureResponse = response.capture(response.getAuthorizedAmount())
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(captureResponse);

        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_020_ICR_auth_reversal() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        //assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse(new BigDecimal(1))
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(reversal);

        // check message data
        pmi = reversal.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4351", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", reversal.getResponseCode());
    }

    @Test
    public void test_020b_ICR_auth_reversal_forced() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        //assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse(new BigDecimal(1))
                .withForceToHost(true)
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(reversal);

        // check message data
        pmi = reversal.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("1381", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", reversal.getResponseCode());
    }

    @Test
    public void test_021_EMV_sale() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setValue("5413330089010434=22122019882803290000");

        Transaction response = track.charge(new BigDecimal(6))
                .withCurrency("USD")
                .withTagData("4F07A0000000041010500A4D617374657243617264820238008407A00000000410108E0A00000000000000001F00950500008080009A031901099B02E8009C01405F24032212315F25030401015F2A0208405F300202015F3401009F01060000000000019F02060000000006009F03060000000000009F0607A00000000410109F0702FF009F090200029F0D05B8508000009F0E0500000000009F0F05B8708098009F10120110A0800F22000065C800000000000000FF9F120A4D6173746572436172649F160F3132333435363738393031323334359F1A0208409F1C0831313232333334349F1E0831323334353637389F21030710109F26080631450565A30B759F2701809F330360F0C89F34033F00019F3501219F360200049F3704C6B1A04F9F3901059F4005F000A0B0019F4104000000869F4C0865C862608A23945A9F4E0D54657374204D65726368616E74")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_022_EMV_authorization() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setValue("5413330089010434=22122019882803290000");

        Transaction response = track.authorize(new BigDecimal(6))
                .withCurrency("USD")
                .withTagData("4F07A0000000041010500A4D617374657243617264820238008407A00000000410108E0A00000000000000001F00950500008080009A031901099B02E8009C01405F24032212315F25030401015F2A0208405F300202015F3401009F01060000000000019F02060000000006009F03060000000000009F0607A00000000410109F0702FF009F090200029F0D05B8508000009F0E0500000000009F0F05B8708098009F10120110A0800F22000065C800000000000000FF9F120A4D6173746572436172649F160F3132333435363738393031323334359F1A0208409F1C0831313232333334349F1E0831323334353637389F21030710109F26080631450565A30B759F2701809F330360F0C89F34033F00019F3501219F360200049F3704C6B1A04F9F3901059F4005F000A0B0019F4104000000869F4C0865C862608A23945A9F4E0D54657374204D65726368616E74")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
        assertNotNull(response.getPosDataCode());

        Transaction rebuilt = Transaction.fromBuilder()
                // All other attributes here
                .withPosDataCode(response.getPosDataCode())
                .build();

        Transaction capture = rebuilt.capture()
                .execute();
        assertNotNull(capture);
        assertEquals("000", response.getResponseCode());
        assertEquals(response.getPosDataCode(), capture.getPosDataCode());
    }

    @Test
    public void test_023_EMV_02() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setValue("5413330089010434=1812201088280329");

        Transaction response = track.charge(new BigDecimal(6))
                .withCurrency("USD")
                .withTagData("4F07A0000000041010500A4D617374657243617264820258008407A00000000410108E0A00000000000000000100950542400080009A031901199B02E8009C01005F24031812315F25030401015F2A0208405F300202015F3401009F01060000000000019F02060000000003009F03060000000000009F0607A00000000410109F0702FF009F090200029F0D05FC50A000009F0E0500000000009F0F05F870A498009F10120210A5000F040000000000000000000000FF9F120A4D6173746572436172649F160F3132333435363738393031323334359F1A0208409F1C0831313232333334349F1E0831323334353637389F21030622599F26080AB5BD8D4719AEEA9F2701809F330360F0C89F34030100029F3501219F360200059F3704DADCC7CB9F3901059F4005F000A0B0019F4104000000989F4E0D54657374204D65726368616E74")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction reversal = response.reverse().execute();
        assertNotNull(reversal);
        assertEquals(reversal.getResponseMessage(), "400", reversal.getResponseCode());
    }

    @Test
    public void test_024_EMV_03() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setValue("5413330089099130=221220114835949000");

        Transaction response = track.charge(new BigDecimal(6))
                .withCurrency("USD")
                .withTagData("4F07A000000004101050104D415354455243415244204445424954820218008407A00000000410108E120000000000000000420102055E0342031F00950580000080009A031901099B0268009C01405F24032212315F25031711015F2A0208405F300202015F3401119F01060000000000019F02060000000006009F03060000000000009F0607A00000000410109F0702FFC09F090200029F0D05B0509C88009F0E0500000000009F0F05B0709C98009F10120110A00003220000000000000000000000FF9F12104D6173746572636172642044656269749F160F3132333435363738393031323334359F1A0208409F1C0831313232333334349F1E0831323334353637389F21030647199F26084233C50A9D5D7FA29F2701809F330360F0C89F34035E03009F3501219F360201259F3704FF4CA1CD9F3901059F4005F000A0B0019F4104000000809F4E0D54657374204D65726368616E74")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals(voidResponse.getResponseMessage(), "400", voidResponse.getResponseCode());
    }

    @Test
    public void test_025_EMV_04() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setValue("5413330089010434=22122019882803290000");

        Transaction response = track.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withTagData("4F07A0000000041010500A4D61737465724361726457135413330089010434D22122019882803290000F5A085413330089010434820238008407A00000000410108E0A00000000000000001F00950500008080009A031901099B02E8009C01405F201A546573742F4361726420313020202020202020202020202020205F24032212315F25030401015F2A0208405F300202015F3401009F01060000000000019F02060000000006009F03060000000000009F0607A00000000410109F0702FF009F090200029F0D05B8508000009F0E0500000000009F0F05B8708098009F10120110A0800F22000065C800000000000000FF9F120A4D6173746572436172649F160F3132333435363738393031323334359F1A0208409F1C0831313232333334349F1E0831323334353637389F21030710109F26080631450565A30B759F2701809F330360F0C89F34033F00019F3501219F360200049F3704C6B1A04F9F3901059F4005F000A0B0019F4104000000869F4C0865C862608A23945A9F4E0D54657374204D65726368616E74")
                .execute("ICR");
        assertNotNull(response);

        //temporarly set partial approval to true
        response.getTransactionReference().setPartialApproval(true);
        response.setResponseCode("002");

        assertEquals(response.getResponseMessage(), "002", response.getResponseCode());

        Transaction capture = response.capture(response.getAuthorizedAmount())
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }

    @Test
    public void test_025_EMV_credit_online_pin() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setValue("5413330089010434=22122019882803290000");
        track.setPinBlock("62968D2481D231E1A504010024A00014");

        Transaction response = track.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withTagData("4F07A0000000041010500A4D61737465724361726457135413330089010434D22122019882803290000F5A085413330089010434820238008407A00000000410108E0A00000000000000001F00950500008080009A031901099B02E8009C01405F201A546573742F4361726420313020202020202020202020202020205F24032212315F25030401015F2A0208405F300202015F3401009F01060000000000019F02060000000006009F03060000000000009F0607A00000000410109F0702FF009F090200029F0D05B8508000009F0E0500000000009F0F05B8708098009F10120110A0800F22000065C800000000000000FF9F120A4D6173746572436172649F160F3132333435363738393031323334359F1A0208409F1C0831313232333334349F1E0831323334353637389F21030710109F26080631450565A30B759F2701809F330360F0C89F34033F00019F3501219F360200049F3704C6B1A04F9F3901059F4005F000A0B0019F4104000000869F4C0865C862608A23945A9F4E0D54657374204D65726368616E74")
                .execute("ICR");
        assertNotNull(response);

        //temporarly set partial approval to true
        response.getTransactionReference().setPartialApproval(true);
        response.setResponseCode("002");

        assertEquals(response.getResponseMessage(), "002", response.getResponseCode());

        Transaction capture = response.capture(response.getAuthorizedAmount())
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }

    @Test
    public void test_026_balance_inquiry() throws ApiException {
        Transaction response = track.balanceInquiry()
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("303000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_027_emv_fallback_refund_by_card() throws ApiException {
        Transaction response = track.refund(new BigDecimal("10"))
                .withCurrency("USD")
                .withChipCondition(EmvChipCondition.ChipFailPreviousSuccess)
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_028_emv_fallback_refund_by_txn() throws ApiException {
        Transaction response = track.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withChipCondition(EmvChipCondition.ChipFailPreviousSuccess)
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        TransactionReference reference = response.getTransactionReference();
        assertNotNull(reference);
        assertNotNull(reference.getOriginalEmvChipCondition());

        Transaction refund = response.refund()
                .execute();
        assertNotNull(refund);
        assertEquals("000", refund.getResponseCode());
    }

    @Test
    public void test_029_emv_fallback_refund_from_network() throws ApiException {
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(1),
                "TYPE04",
                new NtsData(FallbackCode.Received_IssuerTimeout, AuthorizerCode.Host_Authorized),
                track
        );

        Transaction refund = transaction.refund()
                .withChipCondition(EmvChipCondition.ChipFailPreviousSuccess)
                .execute();
        assertNotNull(refund);
        assertEquals("000", refund.getResponseCode());
    }

    @Test
    public void test_030_ready_link() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setValue(";4009081122223335=21121010000012345678?");
        track.setCardType("VisaReadyLink");

        Transaction response = track.addValue(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_030_ready_link_reversal() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setValue(";4009081122223335=21121010000012345678?");
        track.setCardType("VisaReadyLink");
        Transaction response = null;

        response = track.addValue(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .execute();

        assertNotNull(reversal);
        PriorMessageInformation pmi = reversal.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("600008", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4021", pmi.getMessageReasonCode());

        assertNotNull(reversal);
        assertEquals("000", reversal.getResponseCode());
    }

    @Test
    public void test_030_ready_link_force_reversal() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setValue(";4009081122223335=21121010000012345678?");
        track.setCardType("VisaReadyLink");
        Transaction response = null;

        response = track.addValue(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);
        response.setNtsData(ntsData);
        Transaction reversal = response.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);
        PriorMessageInformation pmi = reversal.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("600008", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4021", pmi.getMessageReasonCode());
        assertEquals("400", reversal.getResponseCode());
    }

    @Test
    public void test_031_pudding() throws ApiException {
        Transaction trans = Transaction.fromBuilder()
                .withAmount(new BigDecimal(10))
                .withMessageTypeIndicator("1200")
                .withSystemTraceAuditNumber("1234567")
                .withTransactionTime("NOT EVEN A DATE")
                .build();

        Transaction response = trans.capture()
                .execute();
    }

    @Test
    public void test_033_ready_link_Data_Collect() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setValue(";4009081122223335=21121010000012345678?");
        track.setCardType("VisaReadyLink");

        Transaction response = track.addValue(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        NtsData ntsData = new NtsData(FallbackCode.None,AuthorizerCode.Interchange_Authorized);
        response.setNtsData(ntsData);

        Transaction dataCollectResponse = response.preAuthCompletion(new BigDecimal(10))
                .withCurrency("USD")
                .withForceToHost(true)
                .execute();
        assertNotNull(dataCollectResponse);
        // check response
        assertEquals("000", dataCollectResponse.getResponseCode());
    }
    @Test
    public void test_034_ready_link_reversal_DE56() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setValue(";4009081122223335=21121010000012345678?");
        track.setCardType("VisaReadyLink");

        Transaction response = track.addValue(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Host_Authorized);
        response.setNtsData(ntsData);

        Transaction reversal = response.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);
        PriorMessageInformation pmi = reversal.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("400", reversal.getResponseCode());
    }

    @Test
    public void test_034_ready_link_reversal_force_DE56() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setValue(";4009081122223335=21121010000012345678?");
        track.setCardType("VisaReadyLink");

        Transaction response = track.addValue(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .withForceToHost(true)
                .execute();
        assertNotNull(reversal);
        PriorMessageInformation pmi = reversal.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("400", pmi.getFunctionCode());
    }

    @Test
    public void test_032_ICR_authorization_Discover() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction captureResponse = response.capture(new BigDecimal(12))
                .withCurrency("USD")
                .withTerminalError(true)
                .execute("ICR");
        assertNotNull(captureResponse);

        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("1376", pmi.getMessageReasonCode());
        assertEquals("202", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_001_DE49_swipe_partial_void() throws ApiException {
        Transaction sale = track.charge(new BigDecimal(142))
                .withCurrency("CAD")
                .execute();
        assertNotNull(sale);

        //temporarly set partial approval to true
        sale.getTransactionReference().setPartialApproval(true);
        sale.setResponseCode("002");

        assertEquals("002", sale.getResponseCode());


        Transaction response = sale.voidTransaction()
                .withCurrency("CAD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4351", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }

    @Test
    public void test_swipe_refund() throws ApiException {
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData("4F07A0000000041010500A4D617374657243617264820238008407A00000000410108E0A00000000000000001F00950500008080009A031901099B02E8009C01405F24032212315F25030401015F2A0208405F300202015F3401009F01060000000000019F02060000000006009F03060000000000009F0607A00000000410109F0702FF009F090200029F0D05B8508000009F0E0500000000009F0F05B8708098009F10120110A0800F22000065C800000000000000FF9F120A4D6173746572436172649F160F3132333435363738393031323334359F1A0208409F1C0831313232333334349F1E0831323334353637389F21030710109F26080631450565A30B759F2701809F330360F0C89F34033F00019F3501219F360200049F3704C6B1A04F9F3901059F4005F000A0B0019F4104000000869F4C0865C862608A23945A9F4E0D54657374204D65726368616E74")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("200030", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    //invalid transaction readylink doesn't support data_collect
    @Ignore
    @Test
    public void test_034_ready_link_data_collect_DE56_1221() throws ApiException {
        IStanProvider stan = StanGenerator.getInstance();
        IBatchProvider batch = BatchProvider.getInstance();
        CreditTrackData track = new CreditTrackData();
        track.setValue(";4009081122223335=21121010000012345678?");
        track.setCardType("VisaReadyLink");


        Transaction response = track.addValue(new BigDecimal(10))
                .withCurrency("USD")
                .execute();

        assertNotNull(response);
        assertNotNull(response.getTransactionToken());
        assertEquals("000", response.getResponseCode());

        Transaction capture = NetworkService.resubmitDataCollect(response.getTransactionToken(),true)
                .withForceToHost(true)
                .execute();

        assertNotNull(capture);
        PriorMessageInformation pmi = capture.getMessageInformation();
        assertEquals("1221", pmi.getMessageTransactionIndicator());
        assertEquals("1381", pmi.getMessageReasonCode());
        assertEquals("201", pmi.getFunctionCode());

        assertEquals("000",capture.getResponseCode());

    }

    @Test
    public void test_manual_sale() throws ApiException {

        card = TestCards.VisaManual(true,true);

        Transaction sale = card.charge(new BigDecimal(142))
                .withCurrency("CAD")
                .execute();
        assertNotNull(sale);

        assertEquals("000", sale.getResponseCode());

        Transaction response= NetworkService.resubmitDataCollect(sale.getTransactionToken())
                .execute();

        assertNotNull(response);
        assertEquals(response.getResponseCode(),"000");
    }

    @Test
    public void test_auth_capture_for_entryMethod_proximity() throws ApiException {
        track= TestCards.VisaSwipe();
        track.setEntryMethod(EntryMethod.Proximity);

        Transaction response = track.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // test_017
        Transaction captureResponse = response.reverse(new BigDecimal(12))
                .withCurrency("USD")
                .withCashBackAmount(new BigDecimal(10))
                .execute();
        assertNotNull(captureResponse);

        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("1376", pmi.getMessageReasonCode());
        assertEquals("202", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_manual_sale_resubmit_dataCollect() throws ApiException {
        NtsData ntsData = new NtsData();
        card = TestCards.VisaManual(true,true);

        Transaction sale = card.charge(new BigDecimal(142))
                .withCurrency("CAD")
                .execute();
        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());

        sale.setNtsData(ntsData);

        Transaction captureResponse = sale.capture(new BigDecimal(12))
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);
        assertEquals(captureResponse.getResponseCode(),"000");

        captureResponse.setNtsData(ntsData);

        Transaction response= NetworkService.resubmitDataCollect(captureResponse.getTransactionToken())
                .execute();

        assertNotNull(response);
        assertEquals(response.getResponseCode(),"000");
    }
    @Test
    public void test_manual_sale_force_resubmit_dataCollect() throws ApiException {
        NtsData ntsData = new NtsData();
        card = TestCards.VisaManual(true,true);

        Transaction sale = card.charge(new BigDecimal(142))
                .withCurrency("CAD")
                .execute();
        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());

        sale.setNtsData(ntsData);

        Transaction captureResponse = sale.capture(new BigDecimal(12))
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);
        assertEquals(captureResponse.getResponseCode(),"000");

        captureResponse.setNtsData(ntsData);

        Transaction response= NetworkService.resubmitDataCollect(captureResponse.getTransactionToken())
                .execute();

        assertNotNull(response);
        assertEquals(response.getResponseCode(),"000");
    }

    @Test
    public void test_exception_handling_code_coverage() throws ApiException {
        NtsData ntsData = new NtsData();
        card = TestCards.VisaManual(true,true);

        Transaction sale = card.charge(new BigDecimal(142))
                .withCurrency("USD")
                .withSimulatedHostErrors(Host.Primary, HostError.Timeout)
                .withSimulatedHostErrors(Host.Secondary, HostError.Timeout)
                .execute();
        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());

        sale.setNtsData(ntsData);

    }
    @Test
    public void test_manual_sale_resubmit_dataCollect_code_coverage() throws ApiException {
        NtsData ntsData = new NtsData();
        card = TestCards.VisaManual(true,true);

        Transaction sale = card.charge(new BigDecimal(142))
                .withCurrency("CAD")
                .execute();
        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());

        sale.setNtsData(ntsData);

        Transaction captureResponse = sale.capture(new BigDecimal(12))
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);
        assertEquals(captureResponse.getResponseCode(),"000");

        captureResponse.setNtsData(ntsData);

        Transaction response= NetworkService.forcedSale(captureResponse.getTransactionToken())
                .execute();

        assertNotNull(response);
        assertEquals(response.getResponseCode(),"000");
    }
    @Test
    public void test_proximity_entry_method_with_tag_data() throws ApiException {
        track = TestCards.VisaSwipe();
        track.setEntryMethod(EntryMethod.Proximity);

        Transaction sale = track.charge(new BigDecimal(142))
                .withCurrency("USD")
                .withTagData("4F07A0000000041010500A4D617374657243617264820238008407A00000000410108E0A00000000000000001F00950500008080009A031901099B02E8009C01405F24032212315F25030401015F2A0208405F300202015F3401009F01060000000000019F02060000000006009F03060000000000009F0607A00000000410109F0702FF009F090200029F0D05B8508000009F0E0500000000009F0F05B8708098009F10120110A0800F22000065C800000000000000FF9F120A4D6173746572436172649F160F3132333435363738393031323334359F1A0208409F1C0831313232333334349F1E0831323334353637389F21030710109F26080631450565A30B759F2701809F330360F0C89F34033F00019F3501219F360200049F3704C6B1A04F9F3901059F4005F000A0B0019F4104000000869F4C0865C862608A23945A9F4E0D54657374204D65726368616E74")
                .execute();

        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());
    }
    @Test
    public void test_contactLessMsd_entry_method_emv_for_code_coverage() throws ApiException {
        track = TestCards.VisaSwipe();
        track.setEntryMethod(EntryMethod.Proximity);

        Transaction sale = track.charge(new BigDecimal(142))
                .withCurrency("USD")
                .withTagData("4F07A0000000041010500A4D617374657243617264820238008407A00000000410108E0A00000000000000001F00950500008080009A031901099B02E8009C01405F24032212315F25030401015F2A0208405F300202015F3401009F01060000000000019F02060000000006009F03060000000000009F0607A00000000410109F0702FF009F090200029F0D05B8508000009F0E0500000000009F0F05B8708098009F10120110A0800F22000065C800000000000000FF9F120A4D6173746572436172649F160F3132333435363738393031323334359F1A0208409F1C0831313232333334349F1E0831323334353637389F21030710109F26080631450565A30B759F2701809F330360F0C89F34033F00019F3501219F360200049F3704C6B1A04F9F3901919F4005F000A0B0019F4104000000869F4C0865C862608A23945A9F4E0D54657374204D65726368616E74")
                .execute();

        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());
    }

    @Test
    public void test_gateway_timeout_exception_code_coverage() throws ApiException {
        String token = "ASU5MDQ1ICAgIDA0MCBXIDk5OTAxMlAwMjg4MDAwOTMyOTIzOTkyMTAxMDMxMjA2NDYwMDE5OTkwMTcwMjAwNTQ3MzUwMDAwMDAwMDAxNCAgIDEyMjUxMDU2ODMgMDAwMTAwMDAyMDAwMTAzMTIwNjQ2MTIwODFFMTcwMDEwMTAxMDMyNDEyMDY0NjEwMDAwMDAwMDAwMDAwMDA5OTAwMDAwMDAwMzQ4MDAwMDAwNjQxMTEyMDE3NDAwMDAxMDAwMDAwMDAxNzQxMDIwMTc0MDAwMDEwMDAwMDAwMDE3NDQ2MDAxNzQwMDAwMTAwMDAwMDAwMTc0NDAwMDAwMDAwMDAzMDAwMDAwMDE0MjIwMDAzMjMzMDAwMDAwMDAwMDAwMDAwMDA=";
        Transaction response = NetworkService.resubmitDataCollect(token)
                .execute();

        assertNotNull(response);
        assertEquals("000",response.getResponseCode());
    }
    @Test
    public void test_resubmit_token_null_code_coverage() throws ApiException {

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        BuilderException builderException = assertThrows(BuilderException.class, ()->{
            NetworkService.forcedRefund(response.getTransactionToken())
                    .withForceToHost(true)
                    .execute();
        });
        assertEquals("The transaction token cannot be null for resubmitted transactions.", builderException.getMessage());
    }
    @Test
    public void test_resubmit_with_currency() throws ApiException {
        NtsData ntsData = new NtsData();
        card = TestCards.VisaManual(true,true);

        Transaction sale = card.charge(new BigDecimal(142))
                .withCurrency("USD")
                .execute();
        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());

        sale.setNtsData(ntsData);

        Transaction captureResponse = sale.capture(new BigDecimal(12))
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);
        assertEquals(captureResponse.getResponseCode(),"000");

        captureResponse.setNtsData(ntsData);

        Transaction response= NetworkService.resubmitDataCollect(captureResponse.getTransactionToken())
                .withForceToHost(true)
                .withCurrency("CAD")
                .execute();

        assertNotNull(response);
        assertEquals(response.getResponseCode(),"000");
    }
    @Test
    public void test_manual_force_capture_issue_10292() throws ApiException {
        NtsData ntsData = new NtsData();
        card = TestCards.VisaManual(true,true);

        Transaction auth = card.authorize(new BigDecimal(142))
                .withCurrency("CAD")
                .execute();
        assertNotNull(auth);
        assertEquals("000", auth.getResponseCode());

        auth.setNtsData(ntsData);

        Transaction captureResponse = auth.capture(new BigDecimal(12))
                .withCurrency("USD")
                .withForceToHost(true)
                .execute();
        assertNotNull(captureResponse);
        assertEquals(captureResponse.getResponseCode(),"000");

        captureResponse.setNtsData(ntsData);

    }
    @Test
    public void test_manual_authorize_resubmit_issue_10292() throws ApiException {
        NtsData ntsData = new NtsData();
        card = TestCards.VisaManual(true,true);

        Transaction auth = card.authorize(new BigDecimal(142))
                .withCurrency("CAD")
                .execute();
        assertNotNull(auth);
        assertEquals("000", auth.getResponseCode());

        auth.setNtsData(ntsData);

        Transaction captureResponse = auth.capture(new BigDecimal(12))
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);
        assertEquals(captureResponse.getResponseCode(),"000");

        captureResponse.setNtsData(ntsData);

        Transaction response= NetworkService.resubmitDataCollect(captureResponse.getTransactionToken())
                .execute();

        assertNotNull(response);
        assertEquals(response.getResponseCode(),"000");
    }

    @Test
    public void test_manual_authorize_force_resubmit_issue_10292() throws ApiException {
        NtsData ntsData = new NtsData();
        card = TestCards.VisaManual(true,true);

        Transaction sale = card.authorize(new BigDecimal(142))
                .withCurrency("CAD")
                .execute();
        assertNotNull(sale);
        assertEquals("002", sale.getResponseCode());

        sale.setNtsData(ntsData);

        Transaction captureResponse = sale.capture(new BigDecimal(12))
                .withCurrency("USD")
                .withForceToHost(true)
                .execute();
        assertNotNull(captureResponse);
        assertEquals(captureResponse.getResponseCode(),"000");

        captureResponse.setNtsData(ntsData);

        Transaction response= NetworkService.resubmitDataCollect(captureResponse.getTransactionToken())
                .withForceToHost(true)
                .execute();

        assertNotNull(response);
        assertEquals(response.getResponseCode(),"000");
    }


}

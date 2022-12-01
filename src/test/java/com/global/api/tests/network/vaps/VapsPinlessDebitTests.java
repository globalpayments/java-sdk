package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.AddressType;
import com.global.api.entities.enums.EmvChipCondition;
import com.global.api.entities.enums.Host;
import com.global.api.entities.enums.HostError;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayTimeoutException;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
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
public class VapsPinlessDebitTests {
    private CreditCardData card;
    private CreditTrackData trackVisa;
    private CreditTrackData trackMC;
    private CreditTrackData trackDC;
    private Address address;

    public VapsPinlessDebitTests() throws ApiException {
        address = new Address();
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
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.ByMerchant);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);
        //acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended); //ICR

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
        acceptorConfig.setPinlessDebit(true);

        // gateway config
        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0044");
        config.setTerminalId("0000912197711");
        config.setUniqueDeviceId("0001");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());
        // with merchant type
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);
        ServicesContainer.configureService(config, "ICR");

        // VISA
        trackVisa = new CreditTrackData();
        trackVisa.setValue("4761739001010135=24121011955904500001");

        // MASTERCARD PURCHASING
        trackMC = new CreditTrackData();
        trackMC.setValue("5413330089099015=2512120042960326");

        // DISCOVER

        trackDC = new CreditTrackData();
        trackDC.setValue("6011973700000005=23121011000091500000");
    }

    @Test
    public void test_000_batch_close() throws ApiException {
        BatchSummary summary = BatchService.closeBatch();
        assertNotNull(summary);
        assertTrue(summary.isBalanced());
    }
    @Test
    public void test_001_authorization_prepay_visa_45() throws ApiException {
        Transaction response = trackVisa.authorize(new BigDecimal(45), true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());
        assertEquals("000", response.getResponseCode());

        Transaction captureResponse = response.capture(new BigDecimal(45))
                .withCurrency("USD")
                .withTerminalError(true)
                .execute();
        assertNotNull(captureResponse);

        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("1376", pmi.getMessageReasonCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());

    }
    @Test
    public void test_002_sale25_visa() throws ApiException {
        Transaction response = trackVisa.charge(new BigDecimal(25))
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
    public void test_003_sale55_visa() throws ApiException {

        Transaction response = trackVisa.charge(new BigDecimal(55))
                .withCurrency("USD").withAddress(address , AddressType.Billing)
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
    public void test_004_void_full_approval_visa() throws ApiException {
        Transaction sale = trackVisa.charge(new BigDecimal(25))
                .withCurrency("USD")
                .execute();
        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());

       String NV_1 = sale.getIssuerData().get(CardIssuerEntryTag.VisaTransactionId);
        assertNotNull(NV_1);
        assertNotNull(sale.getReferenceNumber());

        Transaction response = sale.voidTransaction()
                .withCustomerInitiated(true)
                .withIssuerData(CardIssuerEntryTag.VisaTransactionId, NV_1)
                .withReferenceNumber(sale.getReferenceNumber())
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4352", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }

    @Test
    public void test_004_reverse() throws ApiException {
        Transaction response = trackVisa.charge(new BigDecimal(10))
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

        Transaction reversal = response.reverse(new BigDecimal(10))
                .withCashBackAmount(new BigDecimal(3))
                .withTimestamp(response.getTimestamp())
                .withTerminalError(true)
                .withUniqueDeviceId("123456789")
                .execute();
        assertNotNull(reversal);
        assertEquals("400", reversal.getResponseCode());
    }

    @Test
    public void test_005_void_partial_approval_visa() throws ApiException {
        Transaction sale = trackVisa.charge(new BigDecimal(110))
                .withCurrency("USD")
                .execute();
        assertNotNull(sale);
        assertEquals("002", sale.getResponseCode());

        String NV_1 = sale.getIssuerData().get(CardIssuerEntryTag.VisaTransactionId);
        assertNotNull(NV_1);

        Transaction response = sale.voidTransaction()
                .withReferenceNumber(sale.getReferenceNumber())
                .withIssuerData(CardIssuerEntryTag.VisaTransactionId, NV_1)
                .withCustomerInitiated(true)
                .withPartialApproval(true)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4353", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }
    @Test
    public void test_006_authorization_prepay_visa_55() throws ApiException {

        Transaction response = trackVisa.authorize(new BigDecimal(55), true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());
        assertEquals("000", response.getResponseCode());

        Transaction captureResponse = response.capture(new BigDecimal(55))
                .withCurrency("USD")
                .withTerminalError(true)
                .execute();
        assertNotNull(captureResponse);

        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("1376", pmi.getMessageReasonCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());

    }
    @Test
    public void test_007_ICR_authorization_visa() throws ApiException {
        Transaction response = trackVisa.authorize(new BigDecimal(100), true)
                .withCurrency("USD")
                .withAddress(address, AddressType.Billing)
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
        //  response.getNtsData();
        // test_017
        Transaction captureResponse = response.capture(new BigDecimal(100))
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
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }
    @Test
    public void test_008_EMV_sale_visa25() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setValue("4761739001010135=24122011955904500001");

        Transaction response = track.charge(new BigDecimal(25))
                .withCurrency("USD")
                .withTagData("4F07A0000000980840500A4D617374657243617264820238008407A00000009808408E0A00000000000000001F00950500008080009A031901099B02E8009C01405F24032212315F25030401015F2A0208405F300202015F3401009F01060000000000019F02060000000006009F03060000000000009F0607A00000009808409F0702FF009F090200029F0D05B8508000009F0E0500000000009F0F05B8708098009F10120110A0800F22000065C800000000000000FF9F120A4D6173746572436172649F160F3132333435363738393031323334359F1A0208409F1C0831313232333334349F1E0831323334353637389F21030710109F26080631450565A30B759F2701809F330360F0C89F34031F02009F3501219F360200049F3704C6B1A04F9F3901059F4005F000A0B0019F4104000000869F4C0865C862608A23945A9F4E0D54657374204D65726368616E74")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    public void test_009_EMV_04_ICR_visa() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setValue("4761739001010135=24121011955904500001");

        Transaction response = track.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withTagData("4F07A0000000980840500A4D617374657243617264820238008407A00000009808408E0A00000000000000001F00950500008080009A031901099B02E8009C01405F24032212315F25030401015F2A0208405F300202015F3401009F01060000000000019F02060000000006009F03060000000000009F0607A00000000410109F0702FF009F090200029F0D05B8508000009F0E0500000000009F0F05B8708098009F10120110A0800F22000065C800000000000000FF9F120A4D6173746572436172649F160F3132333435363738393031323334359F1A0208409F1C0831313232333334349F1E0831323334353637389F21030710109F26080631450565A30B759F2701809F330360F0C89F34033F00019F3501219F360200049F3704C6B1A04F9F3901059F4005F000A0B0019F4104000000869F4C0865C862608A23945A9F4E0D54657374204D65726368616E74")
                .execute("ICR");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction capture = response.capture(response.getAuthorizedAmount())
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    @Test
    public void test_010_authorization_prepay_MC_45() throws ApiException {

        Transaction response = trackMC.authorize(new BigDecimal(45), true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());
        assertEquals("000", response.getResponseCode());

        Transaction captureResponse = response.capture(new BigDecimal(45))
                .withCurrency("USD")
                .withTerminalError(true)
                .execute();
        assertNotNull(captureResponse);

        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("1376", pmi.getMessageReasonCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());

    }
    @Test
    public void test_011_sale25_MC() throws ApiException {
        Transaction response = trackMC.charge(new BigDecimal(25))
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
    public void test_012_sale55_MC() throws ApiException {
        Transaction response = trackMC.charge(new BigDecimal(55))
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
    public void test_013_void_full_approval_MC() throws ApiException {
        Transaction sale = trackMC.charge(new BigDecimal(25))
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
                .withCustomerInitiated(true)
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
        assertEquals("4352", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }
    @Test
    public void test_014_void_partial_approval_MC() throws ApiException {
        Transaction voidTransaction = trackMC.charge(new BigDecimal(110))
                .withCurrency("USD")
                .execute();
        assertNotNull(voidTransaction);
        assertEquals("002", voidTransaction.getResponseCode());

        String NM_1 = voidTransaction.getIssuerData().get(CardIssuerEntryTag.NTS_MastercardBankNet_ReferenceNumber);
        String NM_2 = voidTransaction.getIssuerData().get(CardIssuerEntryTag.NTS_MastercardBankNet_SettlementDate);

        assertNotNull(NM_1);
        assertNotNull(NM_2);
        assertNotNull(voidTransaction.getReferenceNumber());

        Transaction response = voidTransaction.voidTransaction()
                .withReferenceNumber(voidTransaction.getReferenceNumber())
                .withIssuerData(CardIssuerEntryTag.NTS_MastercardBankNet_ReferenceNumber, NM_1)
                .withIssuerData(CardIssuerEntryTag.NTS_MastercardBankNet_SettlementDate, NM_2)
                .withCustomerInitiated(true)
                .withPartialApproval(true)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4353", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }
    @Test
    public void test_015_authorization_prepay_MC_55() throws ApiException {

        Transaction response = trackMC.authorize(new BigDecimal(55), true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());
        assertEquals("000", response.getResponseCode());

        Transaction captureResponse = response.capture(new BigDecimal(55))
                .withCurrency("USD")
                .withTerminalError(true)
                .execute();
        assertNotNull(captureResponse);

        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("1376", pmi.getMessageReasonCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());

    }
    @Test
    public void test_016_ICR_authorization_MC() throws ApiException {
        Transaction response = trackMC.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withAddress(address, AddressType.Billing)
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
        //  response.getNtsData();
        // test_017
        Transaction captureResponse = response.capture(new BigDecimal(1))
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
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }
    @Test
    @Ignore
    public void test_017_EMV_sale_MC25() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setValue("5413330089099015=2512120042960326");

        Transaction response = track.charge(new BigDecimal(25))
                .withCurrency("USD")
                .withTagData("4F07A0000000042203500A4D617374657243617264820238008407A00000000422038E0A00000000000000001F00950500008080009A031901099B02E8009C01405F24032212315F25030401015F2A0208405F300202015F3401009F01060000000000019F02060000000006009F03060000000000009F0607A00000000422039F0702FF009F090200029F0D05B8508000009F0E0500000000009F0F05B8708098009F10120110A0800F22000065C800000000000000FF9F120A4D6173746572436172649F160F3132333435363738393031323334359F1A0208409F1C0831313232333334349F1E0831323334353637389F21030710109F26080631450565A30B759F2701809F330360F0C89F34031F02009F3501219F360200049F3704C6B1A04F9F3901059F4005F000A0B0019F4104000000869F4C0865C862608A23945A9F4E0D54657374204D65726368616E74")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "100", response.getResponseCode());
    }
    @Test
    public void test_018_authorization_prepay_DC_45() throws ApiException {
        Transaction response = trackDC.authorize(new BigDecimal(45), true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());
        assertEquals("000", response.getResponseCode());

        Transaction captureResponse = response.capture(new BigDecimal(45))
                .withCurrency("USD")
                .withTerminalError(true)
                .execute();
        assertNotNull(captureResponse);

        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("1376", pmi.getMessageReasonCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }
    @Test
    public void test_019_sale25_DC() throws ApiException {
        Transaction response = trackDC.charge(new BigDecimal(25))
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
    public void test_020_sale55_DC() throws ApiException {
        Transaction response = trackDC.charge(new BigDecimal(55))
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
    public void test_021_void_full_approval_DC() throws ApiException {
        Transaction response = trackDC.charge(new BigDecimal(25))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        String ND_2 = response.getIssuerData().get(CardIssuerEntryTag.DiscoverNetworkReferenceId);

        assertNotNull(ND_2);
        assertNotNull(response.getReferenceNumber());

        Transaction voidResponse = response.voidTransaction()
                .withCustomerInitiated(true)
                .withIssuerData(CardIssuerEntryTag.DiscoverNetworkReferenceId, ND_2)
                .withReferenceNumber(response.getReferenceNumber())
                .execute();

        // check message data
        PriorMessageInformation pmi = voidResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4352", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", voidResponse.getResponseCode());
    }
    @Test
    public void test_022_void_partial_approval_DC() throws ApiException {
        Transaction voidTransaction = trackDC.charge(new BigDecimal(110))
                .withCurrency("USD")
                .execute();
        assertNotNull(voidTransaction);
        assertEquals("002", voidTransaction.getResponseCode());

        String ND_2 = voidTransaction.getIssuerData().get(CardIssuerEntryTag.DiscoverNetworkReferenceId);

        assertNotNull(ND_2);
        assertNotNull(voidTransaction.getReferenceNumber());

        Transaction response = voidTransaction.voidTransaction()
                .withReferenceNumber(voidTransaction.getReferenceNumber())
                .withIssuerData(CardIssuerEntryTag.DiscoverNetworkReferenceId, ND_2)
                .withCustomerInitiated(true)
                .withPartialApproval(true)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4353", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }
    @Test
    public void test_023_authorization_prepay_DC_55() throws ApiException {
        Transaction response = trackDC.authorize(new BigDecimal(55), true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());
        assertEquals("000", response.getResponseCode());

        Transaction captureResponse = response.capture(new BigDecimal(55))
                .withCurrency("USD")
                .withTerminalError(true)
                .execute();
        assertNotNull(captureResponse);

        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("1376", pmi.getMessageReasonCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }
    @Test
    public void test_024_ICR_auth_reversal_DC() throws ApiException {
        Transaction response = trackDC.authorize(new BigDecimal(100), true)
                .withCurrency("USD")
                .withAddress(address, AddressType.Billing)
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
        //  response.getNtsData();
        // test_017
        Transaction captureResponse = response.capture(new BigDecimal(100))
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
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }
    @Test
    public void test_025_EMV_sale_DC25() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setValue("6011973700000005=23121011000091500000");

        Transaction response = track.charge(new BigDecimal(25))
                .withCurrency("USD")
                .withTagData("4F07A0000001524010500A4D617374657243617264820238008407A00000015240108E0A00000000000000001F00950500008080009A031901099B02E8009C01405F24032212315F25030401015F2A0208405F300202015F3401009F01060000000000019F02060000000006009F03060000000000009F0607A00000015240109F0702FF009F090200029F0D05B8508000009F0E0500000000009F0F05B8708098009F10120110A0800F22000065C800000000000000FF9F120A4D6173746572436172649F160F3132333435363738393031323334359F1A0208409F1C0831313232333334349F1E0831323334353637389F21030710109F26080631450565A30B759F2701809F330360F0C89F34031F02009F3501219F360200049F3704C6B1A04F9F3901059F4005F000A0B0019F4104000000869F4C0865C862608A23945A9F4E0D54657374204D65726368616E74")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_157_reverse_sale_cashBack() throws ApiException {
        Transaction response = trackVisa.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(3))
                .execute();
        assertNotNull(response);
        //assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse()
                .withCurrency("USD")
                .withCashBackAmount(new BigDecimal(3))
                .execute();
        assertNotNull(reversal);
    }
}

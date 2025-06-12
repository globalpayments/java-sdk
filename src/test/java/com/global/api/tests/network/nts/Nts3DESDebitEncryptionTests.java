package com.global.api.tests.network.nts;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.NtsProductData;
import com.global.api.network.entities.NtsTag16;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.ProductData;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class Nts3DESDebitEncryptionTests {
    private DebitTrackData debit;
    private NtsRequestMessageHeader ntsRequestMessageHeader;
    private PriorMessageInformation priorMessageInformation;
    AcceptorConfig acceptorConfig;
    // gateway config
    NetworkGatewayConfig config;
    private NtsTag16 tag;
    private NtsProductData productData;
    public Nts3DESDebitEncryptionTests() throws ApiException {
        Address address = new Address();
        address.setName("My STORE            ");
        address.setStreetAddress1("1 MY STREET       ");
        address.setCity("JEFFERSONVILLE  ");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        acceptorConfig = new AcceptorConfig();
        acceptorConfig.setAddress(address);
        ntsRequestMessageHeader = new NtsRequestMessageHeader();

        ntsRequestMessageHeader.setTerminalDestinationTag("478");
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.PinDebit);

        priorMessageInformation = new PriorMessageInformation();
        priorMessageInformation.setResponseTime("999");
        priorMessageInformation.setConnectTime("999");
        priorMessageInformation.setMessageReasonCode("08");
        ntsRequestMessageHeader.setPriorMessageInformation(priorMessageInformation);

        // data code values
        // acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactlessMsd_KeyEntry);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.None);
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactlessMsd_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.Attended);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);

        acceptorConfig.setSupportedEncryptionType(EncryptionType.TDES);
        acceptorConfig.setServiceType(ServiceType.GPN_API);
        acceptorConfig.setOperationType(OperationType.Decrypt);

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
        config.setTerminalId("21");
        config.setUnitNumber("00001234567");
        config.setSoftwareVersion("01");
        config.setCompanyId("045");
        config.setLogicProcessFlag(LogicProcessFlag.Capable);
        config.setTerminalType(TerminalType.VerifoneRuby2Ci);

        ServicesContainer.configureService(config);
        ServicesContainer.configureService(config, "ICR");

        tag = new NtsTag16();
        tag.setPumpNumber(1);
        tag.setWorkstationId(1);
        tag.setServiceCode(ServiceCode.Self);
        tag.setSecurityData(SecurityData.NoAVSAndNoCVN);

        // DEBIT
        debit = new DebitTrackData();
        debit.setEncryptionData(EncryptionData.setKSNAndEncryptedData("E6699A44C3EE9E3AA75F9DF958C27469730C10D2929869F3704CC790CCB0AFDCDDE47F392E0D50E7",
                "F000019990E00003"));
        debit.setCardType("PinDebit");
        debit.setPinBlock("62968D2481D231E1A504010024A00014");
        debit.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E");
        debit.setTrackNumber(TrackNumber.TrackTwo);
        debit.setExpiry("2512");

    }
    @Test
    public void test_001_sales_without_track() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.PinDebit);

        Transaction response = debit.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test//working
    public void test_PinDebit_pre_authorization_06() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.PinDebit);

        Transaction response = debit.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(response);

        assertEquals("00", response.getResponseCode());
    }

    @Test//working
    public void test_PinDebit_pre_authorization_Completion_07() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.PinDebit);

        Transaction response = debit.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withAmountEstimated(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction dataCollectResponse = response.preAuthCompletion(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(productData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSettlementAmount(new BigDecimal(10))
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());

    }
    //Debit Pre-Auth Cancel
    @Test//working
    public void test_PinDebit_pre_authorization_Cancel_08() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.PinDebit);

        Transaction response = debit.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        Transaction dataCollectResponse = response.voidTransaction(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(productData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
//    Debit Purchase Reversal
    @Test//working
    public void test_PinDebit_purchase_reversal_13() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.PinDebit);

        Transaction response = debit.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
            assertNotNull(response);
            assertEquals("00", response.getResponseCode());

        Transaction dataCollectResponse = response.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(productData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    //    Debit Purchase with Cash Back Reversal
    @Test// working
    public void test_purchase_with_cashBack_reversal_14() throws ApiException {
        Transaction reversalResponse = debit.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(3))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(reversalResponse);

        assertEquals("00", reversalResponse.getResponseCode());

        Transaction reversal = reversalResponse.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSettlementAmount(new BigDecimal(3))
                .withCurrency("USD")
                .execute();

        assertEquals("00", reversal.getResponseCode());
    }

    // A Purchase Return Reversal message is used when a time-out or a HOST RESPONSE CODE 80 is received on a Purchase Return (05).
    @Test
    public void test_purchase_refund_reversal_15() throws ApiException {
        Transaction refundResponse = debit.refund(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withCurrency("USD")
                .execute();
        assertNotNull(refundResponse);
        assertEquals("00", refundResponse.getResponseCode());

        Transaction reversalResponse = refundResponse.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withCurrency("USD")
                .execute();
        assertNotNull(reversalResponse);

        assertEquals("00", reversalResponse.getResponseCode());
    }

    @Test
    public void test_purchase_refund_05() throws ApiException {

        Transaction refundResponse = debit.refund(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withCurrency("USD")
                .execute();
        assertNotNull(refundResponse);
        assertEquals("00", refundResponse.getResponseCode());

    }
}

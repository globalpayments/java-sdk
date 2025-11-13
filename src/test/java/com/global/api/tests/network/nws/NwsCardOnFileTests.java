package com.global.api.tests.network.nws;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.BatchCloseType;
import com.global.api.entities.enums.Target;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.*;
import com.global.api.network.enums.*;
import com.global.api.network.enums.gnap.CardHolderPresentIndicator;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.TestCards;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.ProductData;
import java.math.BigDecimal;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NwsCardOnFileTests  {
    private CreditCardData card;
    private CreditTrackData track;
    private ProductData productData;
    private FleetData fleetData;
    private NetworkGatewayConfig config;


    public NwsCardOnFileTests() throws ApiException {
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
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.Manual);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.None);
        acceptorConfig.setCardCaptureCapability(false);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended_Mobile);
        acceptorConfig.setCardDataInputMode(DE22_CardDataInputMode.CredentialOnFile);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        acceptorConfig.setCardDataOutputCapability(CardDataOutputCapability.None);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Display);
        acceptorConfig.setPinCaptureCapability(PinCaptureCapability.None);



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

        // gateway config
        config = new NetworkGatewayConfig(Target.NWS);
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns-e.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("SPSA");
        config.setTerminalId("NWSJAVA05");
        config.setUniqueDeviceId("0001");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());
        config.setMerchantType("5541");
        //for cash advanced transaction
        //  config.setMerchantType("6010");

        ServicesContainer.configureService(config);

        card = new CreditCardData();
        card.setNumber("5473500000000014");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardPresent(false);
        card.setReaderPresent(false);

        fleetData = new FleetData();
        fleetData.setServicePrompt("0");

        productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.GlobalPayments);
        productData.add(ProductCode.Unleaded_Gas, UnitOfMeasure.Gallons, 1, 10);
    }

    @Test
    public void test_003_manual_authorization_card_on_file() throws ApiException {
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        fleetData.setDriverId("373395");
        fleetData.setVehicleNumber("46591");

        Transaction response = card.authorize(new BigDecimal(10),true)
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
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_016_ICR_authorization_card_on_file() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("01", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(10), true)
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
        Transaction captureResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
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
    public void test_011_swipe_void_card_on_file() throws ApiException {
        Transaction sale = card.charge(new BigDecimal(12))
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
    public void test_000_batch_close_card_on_file() throws ApiException {
        Transaction response = BatchService.closeBatch(BatchCloseType.EndOfShift)
                .execute();
        assertNotNull(response);
        assertNotNull(response.getBatchSummary());
        assertTrue(response.getBatchSummary().isBalanced());
    }

    @Test
    public void test_swipe_Auth_cards() throws ApiException {

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        Transaction preResponse = card.authorize(new BigDecimal(30))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .execute();
        assertNotNull(preResponse);

        // check message data
        PriorMessageInformation pmi = preResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());

        // check response
        assertEquals("000", preResponse.getResponseCode());

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("05", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));
        productData.add("45", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
    }
}



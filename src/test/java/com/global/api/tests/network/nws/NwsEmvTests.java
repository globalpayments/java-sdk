package com.global.api.tests.network.nws;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.enums.Target;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.*;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.TestCards;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NwsEmvTests {
    private CreditCardData card;
    private CreditTrackData track;
    private NetworkGatewayConfig config;
    private FleetData fleetData;
    private ProductData productData;

    String emvTagData = "4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000";


    public NwsEmvTests() throws ApiException {
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
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.ICC);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(false);
        acceptorConfig.setSupportsShutOffAmount(false);
        acceptorConfig.setSupportsReturnBalance(false);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(false);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(false);
        acceptorConfig.setSupportsEmvPin(false);

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

        ServicesContainer.configureService(config);

        fleetData = new FleetData();
        fleetData.setServicePrompt("0");

        productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Heartland);
        productData.add(ProductCode.Unleaded_Gas, UnitOfMeasure.Gallons, 1, 10);


        // VISA
        card = TestCards.VisaManual(true, true);
        track = TestCards.VisaSwipe();

        //UnionPay
        track = TestCards.UnionPaySwipe();
    }
    @Test
    public void test_EMV_authorization_opt() throws ApiException {
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        DebitTrackData debit = new DebitTrackData();
        debit.setValue(";6090001234567891=2112120000000000001?");
        debit.setPinBlock("62968D2481D231E1A504010024A00014");
        track.setEntryMethod(EntryMethod.Swipe);

        Transaction response = debit.authorize(new BigDecimal(6))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_Authorization_wex_otp() throws ApiException {
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        fleetData.setDriverId("373395");
        fleetData.setVehicleNumber("46561");
        fleetData.setServicePrompt("0");

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460430001234566=24121014656100000");

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withTagData(emvTagData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_mastercard_fleet_swipe_Authorize_cards() throws ApiException {
        config.setMerchantType("5542");
        ServicesContainer.configureService(config, "ICR");

        fleetData.setOdometerReading("987654");
        fleetData.setDriverId("001001");

        track = new CreditTrackData();
        track = TestCards.MasterCardFleetSwipe();

        Transaction preResponse = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withTagData(emvTagData)
                .execute("ICR");
        assertNotNull(preResponse);

        // check message data
        PriorMessageInformation pmi = preResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());

        // check response
        assertEquals("000", preResponse.getResponseCode());

    }
    @Test
    public void test_debit_pre_authorization_opt() throws ApiException {
        DebitTrackData debit = new DebitTrackData();
        debit.setValue(";6090001234567891=2112120000000000001?");
        debit.setPinBlock("62968D2481D231E1A504010024A00014");

        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        Transaction response = debit.authorize(new BigDecimal(1))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_EMV_authorization_opt_capture() throws ApiException {
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        CreditTrackData track = new CreditTrackData();
        track.setValue("5413330089010434=22122019882803290000");

        Transaction response = track.authorize(new BigDecimal(1))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .execute();
        assertNotNull(response);
       // assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction capture = response.capture(response.getAuthorizedAmount())
                .withCurrency("USD")
                .withTagData(emvTagData)
                .execute("ICR");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }
    @Test
    public void test_Authorization_wex_otp_capture() throws ApiException {
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        fleetData.setDriverId("373395");
        fleetData.setVehicleNumber("46561");
        fleetData.setServicePrompt("0");

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460430001234566=24121014656100000");

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withTagData(emvTagData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());

        // check response
       // assertEquals("000", response.getResponseCode());

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("05", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));
       // productData.add("45", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(30))
                .withCurrency("USD")
                .withProductData(productData)
                .withTagData(emvTagData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(capture);

        // check message data
        pmi = capture.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("202", pmi.getFunctionCode());

        // check response
        assertEquals("000", capture.getResponseCode());
    }

    @Test
    public void test_mastercard_fleet_swipe_authCapture() throws ApiException {
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        track = new CreditTrackData();
        track = TestCards.MasterCardFleetSwipe();

        Transaction preRresponse = track.authorize(new BigDecimal(30))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withTagData(emvTagData)
                .execute();
        assertNotNull(preRresponse);

        // check message data
        PriorMessageInformation pmi = preRresponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());

        // check response
        //assertEquals("000", preRresponse.getResponseCode());

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("05", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));
        productData.add("45", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = preRresponse.capture(new BigDecimal(30))
                .withCurrency("USD")
                .withProductData(productData)
                .withTagData(emvTagData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        // check message data
        pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }


    @Test
    public void test_debit_pre_authorization_opt_capture() throws ApiException {
        DebitTrackData debit = new DebitTrackData();
        debit.setValue(";6090001234567891=2112120000000000001?");
        debit.setPinBlock("62968D2481D231E1A504010024A00014");

        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        Transaction response = debit.authorize(new BigDecimal(1))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        //response.getTransactionReference().setAuthCode("00479A");


        // check response
       // assertEquals("000", response.getResponseCode());

        // test case 160
        Transaction capture = response.capture(response.getAuthorizedAmount())
                .withTagData(emvTagData)
                .execute();
        assertNotNull(capture);

        // check message data
        pmi = capture.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());
        assertEquals("1376", pmi.getMessageReasonCode());

        // check response
        assertEquals("000", capture.getResponseCode());
    }
    @Test
    public void test_swipe_sale_offline_pin() throws ApiException {
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        track = new CreditTrackData();
        track = TestCards.MasterCardSwipe();

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
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
    public void test_wex_sale_IPT() throws ApiException {
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        fleetData.setDriverId("373395");
        fleetData.setVehicleNumber("46561");
        fleetData.setServicePrompt("0");

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460430001234566=24121014656100000");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("102", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(30))
                .withCurrency("USD")
                .withProductData(productData)
                .withTagData(emvTagData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_visa_fleet_sale_IPT() throws ApiException {
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("01", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        track = new CreditTrackData();
        track = TestCards.VisaFleetSwipe();

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(emvTagData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_discover_auth_reversal_IPT() throws ApiException {
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        track = new CreditTrackData();
        track = TestCards.DiscoverSwipe();

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());

//         check response
        assertEquals("000", response.getResponseCode());

        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);
        response.setNtsData(ntsData);

        Transaction reversal = response.reverse(new BigDecimal(1))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .execute();
        assertNotNull(reversal);

        // check message data
        pmi = reversal.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4021", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", reversal.getResponseCode());
    }

    @Test
    public void test_debit_return_IPT() throws ApiException {
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        DebitTrackData debit = new DebitTrackData();
        debit.setValue(";6090001234567891=2112120000000000001?");
        debit.setPinBlock("62968D2481D231E1A504010024A00014");

        Transaction response = debit.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("200008", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_Authorization_wex_otp_codeCoverage() throws ApiException {
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        fleetData.setDriverId("373395");
        fleetData.setVehicleNumber("46561");
        fleetData.setServicePrompt("0");

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460430001234566=24121014656100000");

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withTagData("4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901919F4005F000F0A0019F410400000000")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_discover_auth_reversal_IPT_codeCoverage() throws ApiException {
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        track = new CreditTrackData();
        track = TestCards.DiscoverSwipe();

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(emvTagData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());

//         check response
        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);
        response.setNtsData(ntsData);

        Transaction reversal = response.reverse(new BigDecimal(1))
                .withCurrency("USD")
                .withTagData("4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901919F4005F000F0A0019F410400000000")
                .execute();
        assertNotNull(reversal);

        // check message data
        pmi = reversal.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4021", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", reversal.getResponseCode());
    }

    @Test
    public void test_discover_auth_reversal_IPT_codeCoverage01() throws ApiException {
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        track = new CreditTrackData();
        track = TestCards.DiscoverSwipe();
        track.setEntryMethod(EntryMethod.Proximity);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(null)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());

//         check response
        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);
        response.setNtsData(ntsData);

        Transaction reversal = response.reverse(new BigDecimal(1))
                .withCurrency("USD")
                .withTagData(null)
                .execute();
        assertNotNull(reversal);

        // check message data
        pmi = reversal.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4021", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", reversal.getResponseCode());
    }


}

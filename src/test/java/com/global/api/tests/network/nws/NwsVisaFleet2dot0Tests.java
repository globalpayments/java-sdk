package com.global.api.tests.network.nws;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Target;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.FleetData;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.ProductData;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import java.math.BigDecimal;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NwsVisaFleet2dot0Tests {
    private CreditTrackData card;
    private String visaTagData = "4F07A0000000031010820239008407A00000000310107005123456789057124485580000080017D311220115886224023F5F201A546573742F4361726420313020202020202020202020202020205A0844855800000800175F24032212315F280208405F3401008C0F1234567890123451234567890123458D13123745524364726335524364726325374552438E0C00000000000000001F0000009F0702FF009F080200019F0D05F470C498009F0E0500000000009F0F05F470C498008F01019001119F4604123456789F4701239F5801129F5901019F6804123456789F6C0212349F6E04D8E0000082010112820201128203013482040112";
    AcceptorConfig acceptorConfig;
    NetworkGatewayConfig config;


    public NwsVisaFleet2dot0Tests() throws ApiException {
        acceptorConfig = new AcceptorConfig();

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_MagStripe_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.ByMerchant);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);
        acceptorConfig.setSupportedEncryptionType(EncryptionType.TEP2);
        acceptorConfig.setSupportsEmvPin(true);

        //DE48-33 POS CONFIGURATION message
        acceptorConfig.setTimezone("S");
        acceptorConfig.setSupportsPartialApproval(false);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsCashAtCheckout(true);
        acceptorConfig.setMobileDevice(false);
        acceptorConfig.setSupportWexAvailableProducts(true);
        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.FuelAndNonFuel);
        acceptorConfig.setSupportTerminalPurchaseRestriction(PurchaseRestrictionCapability.HOSTBASEDPRODUCTRESTRICTION);
        acceptorConfig.setVisaFleet2(true);

        //DE48-34 message configuration values
        acceptorConfig.setPerformDateCheck(true);
        acceptorConfig.setEchoSettlementData(true);
        acceptorConfig.setIncludeLoyaltyData(false);


        // gateway config
        config = new NetworkGatewayConfig(Target.NWS);
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("SPSA");
        config.setTerminalId("NWSJAVA05");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());
        config.setUniqueDeviceId("2001");
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        // Visa Fleet 2.0
        card = new CreditTrackData();
        card.setValue(";4485580000080017=311220115886224023?");
    }

    @Test
    public void test_VisaFleet_emv_Auth_1200_1() throws ApiException {

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        ProductData productData = new ProductData(ServiceLevel.SelfServe,ProductCodeSet.IssuerSpecific,ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("01", UnitOfMeasure.Gallons, new BigDecimal("15.9999"), new BigDecimal("21.9999"));
        productData.addNonFuel("79", UnitOfMeasure.Units, new BigDecimal("100"), new BigDecimal("30"), new BigDecimal("30"));
        productData.addNonFuel("01", UnitOfMeasure.Units, new BigDecimal("145.67"), new BigDecimal("67.009"), new BigDecimal("30"));

        Transaction response = card.authorize(new BigDecimal(13.062))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000000031010820239008407A00000000310107005123456789057124485580000080017D311220115886224023F5F201A546573742F4361726420313020202020202020202020202020205A0844855800000800175F24032212315F280208405F3401008C0F1234567890123451234567890123458D13123745524364726335524364726325374552438E0C00000000000000001F0000009F0702FF009F080200019F0D05F470C498009F0E0500000000009F0F05F470C498008F01019001119F4604123456789F4701239F5801129F5901019F6804123456789F6C0212349F6E04D8E0000082010112820201128203013482040112")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    public void test_VisaFleet_emv_Auth_capture() throws ApiException {

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        ProductData productData = new ProductData(ServiceLevel.SelfServe,ProductCodeSet.IssuerSpecific,ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("01", UnitOfMeasure.Gallons, new BigDecimal("15.9999"), new BigDecimal("21.9999"));
        productData.addNonFuel("79", UnitOfMeasure.Units, new BigDecimal("100"), new BigDecimal("30"), new BigDecimal("30"));
        productData.addNonFuel("01", UnitOfMeasure.Units, new BigDecimal("145.67"), new BigDecimal("67.009"), new BigDecimal("30"));

        Transaction response = card.authorize(new BigDecimal(13.062))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
       // assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
        TransactionReference  reference = response.getTransactionReference();
        reference.setNtsData(new NtsData());
        reference.setAuthCode("578198");
        response.setTransactionReference(reference);

        Transaction captureResponse = response.capture(new BigDecimal(45))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .withForceToHost(true)
                .execute();
        assertNotNull(captureResponse);
        // check response
        assertEquals("400", captureResponse.getResponseCode());
    }
    @Test
    public void test_swipe_voice_capture() throws ApiException {
        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        ProductData productData = new ProductData(ServiceLevel.SelfServe,ProductCodeSet.IssuerSpecific,ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("01", UnitOfMeasure.Gallons, new BigDecimal("15.9999"), new BigDecimal("21.9999"));
        productData.addNonFuel("79", UnitOfMeasure.Units, new BigDecimal("100"), new BigDecimal("30"), new BigDecimal("30"));
        productData.addNonFuel("01", UnitOfMeasure.Units, new BigDecimal("145.67"), new BigDecimal("67.009"), new BigDecimal("30"));

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000000031010820239008407A00000000310107005123456789057124485580000080017D311220115886224023F5F201A546573742F4361726420313020202020202020202020202020205A0844855800000800175F24032212315F280208405F3401008C0F1234567890123451234567890123458D13123745524364726335524364726325374552438E0C00000000000000001F0000009F0702FF009F080200019F0D05F470C498009F0E0500000000009F0F05F470C498008F01019001119F4604123456789F4701239F5801129F5901019F6804123456789F6C0212349F6E04D8E0000082010112820201128203013482040112")
                .execute();
        assertNotNull(response);
       // assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "578198",
                new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized),
                card
        );

        Transaction captureResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(captureResponse);
        // check message data
        PriorMessageInformation pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_VisaFleet_emv_refund_credit_adjustment() throws ApiException {

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("123456");
        fleetData.setEnteredData("123456");

        ProductData productData = new ProductData(ServiceLevel.SelfServe,ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.add("01", UnitOfMeasure.Gallons, new BigDecimal("10.72"), new BigDecimal(4), new BigDecimal(50));

        Transaction response = card.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_VisaFleet_emv_Auth_void() throws ApiException {

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        ProductData productData = new ProductData(ServiceLevel.SelfServe,ProductCodeSet.IssuerSpecific,ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("01", UnitOfMeasure.Gallons, new BigDecimal("15.9999"), new BigDecimal("21.9999"));
        productData.addNonFuel("79", UnitOfMeasure.Units, new BigDecimal("100"), new BigDecimal("30"), new BigDecimal("30"));
        productData.addNonFuel("01", UnitOfMeasure.Units, new BigDecimal("145.67"), new BigDecimal("67.009"), new BigDecimal("30"));

        Transaction response = card.authorize(new BigDecimal(13.062))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        // assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
        TransactionReference  reference = response.getTransactionReference();
        reference.setNtsData(new NtsData());
        reference.setAuthCode("578198");
        response.setTransactionReference(reference);

        Transaction captureResponse = response.voidTransaction(response.getAuthorizedAmount())
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);
        // check response
        assertEquals("400", captureResponse.getResponseCode());
    }
    @Test
    public void test_VisaFleet_emv_Auth_reversal() throws ApiException {
        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        ProductData productData = new ProductData(ServiceLevel.SelfServe,ProductCodeSet.IssuerSpecific,ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("01", UnitOfMeasure.Gallons, new BigDecimal("15.9999"), new BigDecimal("21.9999"));
        productData.addNonFuel("79", UnitOfMeasure.Units, new BigDecimal("100"), new BigDecimal("30"), new BigDecimal("30"));
        productData.addNonFuel("01", UnitOfMeasure.Units, new BigDecimal("145.67"), new BigDecimal("67.009"), new BigDecimal("30"));

        Transaction response = card.authorize(new BigDecimal(13.062))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        // assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
        TransactionReference  reference = response.getTransactionReference();
        reference.setNtsData(ntsData);
        reference.setAuthCode("578198");
        response.setTransactionReference(reference);



        Transaction captureResponse = response.reverse()
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);
        // check response
        assertEquals("400", captureResponse.getResponseCode());
    }

    @Test
    public void test_VisaFleet_emv_1200_Product_10() throws ApiException {

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("123456");
        fleetData.setEnteredData("123456");

        ProductData productData = new ProductData(ServiceLevel.FullServe,ProductCodeSet.Conexxus_3_Digit, ProductDataFormat.VISAFLEET2Dot0);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal("10.72"), new BigDecimal(4), new BigDecimal(50));
        productData.add("079", UnitOfMeasure.CaseOrCarton, new BigDecimal("10"), new BigDecimal(34), new BigDecimal(10));
        productData.add("067", UnitOfMeasure.OtherOrUnknown, new BigDecimal("10.720"), new BigDecimal(34664), new BigDecimal(50));
        productData.add("084", UnitOfMeasure.Liters, new BigDecimal("10.720"), new BigDecimal(34664), new BigDecimal(50));
        productData.add("080", UnitOfMeasure.Quarts, new BigDecimal(10), new BigDecimal(21), new BigDecimal(10));
        productData.add("062", UnitOfMeasure.OtherOrUnknown, new BigDecimal(10), new BigDecimal(21), new BigDecimal(10));
        productData.add("071", UnitOfMeasure.ImperialGallons, new BigDecimal(10), new BigDecimal(21), new BigDecimal(10));
        productData.add("073", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(5), new BigDecimal(10));
        productData.add("064", UnitOfMeasure.Pounds, new BigDecimal(1), new BigDecimal(5), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000000031010820239008407A00000000310107005123456789057124485580000080017D311220115886224023F5F201A546573742F4361726420313020202020202020202020202020205A0844855800000800175F24032212315F280208405F3401008C0F1234567890123451234567890123458D13123745524364726335524364726325374552438E0C00000000000000001F0000009F0702FF009F080200019F0D05F470C498009F0E0500000000009F0F05F470C498008F01019001119F4604123456789F4701239F5801129F5901019F6804123456789F6C0212349F6E04D8E0000082010112820201128203013482040112")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    public void test_VisaFleet_emv_1200_7() throws ApiException {

        FleetData fleetData = new FleetData();
        fleetData.setVehicleNumber("123456");
        fleetData.setOdometerReading("123456");
        fleetData.setTrailerReferHours("123456");
        fleetData.setUnitNumber("123456");

        ProductData productData = new ProductData(ServiceLevel.Other_NonFuel,ProductCodeSet.Conexxus_3_Digit,ProductDataFormat.VISAFLEET2Dot0);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal("10.720"), new BigDecimal(34664), new BigDecimal(50));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000000031010820239008407A00000000310107005123456789057124485580000080017D311220115886224023F5F201A546573742F4361726420313020202020202020202020202020205A0844855800000800175F24032212315F280208405F3401008C0F1234567890123451234567890123458D13123745524364726335524364726325374552438E0C00000000000000001F0000009F0702FF009F080200019F0D05F470C498009F0E0500000000009F0F05F470C498008F01019001119F4604123456789F4701239F5801129F5901019F6804123456789F6C0212349F6E04D8E0000082010112820201128203013482040112")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    @Test
    public void test_1220_1() throws ApiException {

        card.setValue(";4485580000080017=311220115886224023?");

        ProductData productData = new ProductData(ServiceLevel.Other_NonFuel,ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("01", UnitOfMeasure.Gallons, new BigDecimal("10.120"), new BigDecimal("5.1182"), new BigDecimal("5.121"));
        productData.addNonFuel("79", UnitOfMeasure.Units, new BigDecimal("100"), new BigDecimal("30"), new BigDecimal("30"));
        productData.addNonFuel("DM", UnitOfMeasure.Quarts, new BigDecimal("200"),new BigDecimal("30") ,new BigDecimal("20"));
        productData.addNonFuel("B9", UnitOfMeasure.Units, new BigDecimal("2"), new BigDecimal("30"),new BigDecimal("80"));
        productData.addNonFuel("CG", UnitOfMeasure.Units, new BigDecimal("40"), new BigDecimal("30"),new BigDecimal("80"));

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("1234");
        fleetData.setDriverId("1234");

        Transaction response = card.authorize(new BigDecimal("10"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withServiceCode("101")
                .withTagData("4F07A0000000031010820239008407A00000000310107005123456789057124485580000080017D311220115886224023F5F201A546573742F4361726420313020202020202020202020202020205A0844855800000800175F24032212315F280208405F3401008C0F1234567890123451234567890123458D13123745524364726335524364726325374552438E0C00000000000000001F0000009F0702FF009F080200019F0D05F470C498009F0E0500000000009F0F05F470C498008F01019001119F4604123456789F4701239F5801129F5901019F6804123456789F6C0212349F6E04D8E0000082010112820201128203013482040112")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit,ProductDataFormat.VISAFLEET2Dot0);
        productData.add("400", UnitOfMeasure.Units, new BigDecimal(0001), new BigDecimal(21000), new BigDecimal(10));

        Transaction captureResponse = response.capture(new BigDecimal(45))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000000031010820239008407A00000000310107005123456789057124485580000080017D311220115886224023F5F201A546573742F4361726420313020202020202020202020202020205A0844855800000800175F24032212315F280208405F3401008C0F1234567890123451234567890123458D13123745524364726335524364726325374552438E0C00000000000000001F0000009F0702FF009F080200019F0D05F470C498009F0E0500000000009F0F05F470C498008F01019001119F4604123456789F4701239F5801129F5901019F6804123456789F6C0212349F6E04D8E0000082010112820201128203013482040112")
                .execute();
        assertNotNull(captureResponse);
        // check response
        assertEquals("400", captureResponse.getResponseCode());
    }

    @Test
    public void test_VisaFleet_Capture_1220_2() throws ApiException {

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("123450");
        fleetData.setDriverId("123450");

        ProductData productData = new ProductData(ServiceLevel.Other_NonFuel, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.add("10", UnitOfMeasure.Units, new BigDecimal(0001), new BigDecimal(10), new BigDecimal(10));
//        productData.add("084", UnitOfMeasure.Liters, new BigDecimal("10.720"), new BigDecimal(34664), new BigDecimal(50));
//        productData.add("080", UnitOfMeasure.Quarts, new BigDecimal(10), new BigDecimal(21), new BigDecimal(10));
//        productData.add("062", UnitOfMeasure.OtherOrUnknown, new BigDecimal(10), new BigDecimal(21), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000000031010820239008407A00000000310107005123456789057124485580000080017D311220115886224023F5F201A546573742F4361726420313020202020202020202020202020205A0844855800000800175F24032212315F280208405F3401008C0F1234567890123451234567890123458D13123745524364726335524364726325374552438E0C00000000000000001F0000009F0702FF009F080200019F0D05F470C498009F0E0500000000009F0F05F470C498008F01019001119F4604123456789F4701239F5801129F5901019F6804123456789F6C0212349F6E04D8E0000082010112820201128203013482040112")
                .execute();
        assertNotNull(response);
       // assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.add("10", UnitOfMeasure.Units, new BigDecimal(0001), new BigDecimal(21000), new BigDecimal(10));
        response.setNtsData(new NtsData());
        response.getTransactionReference().setNtsData(new NtsData());

        Transaction captureResponse = response.capture(new BigDecimal(45))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000000031010820239008407A00000000310107005123456789057124485580000080017D311220115886224023F5F201A546573742F4361726420313020202020202020202020202020205A0844855800000800175F24032212315F280208405F3401008C0F1234567890123451234567890123458D13123745524364726335524364726325374552438E0C00000000000000001F0000009F0702FF009F080200019F0D05F470C498009F0E0500000000009F0F05F470C498008F01019001119F4604123456789F4701239F5801129F5901019F6804123456789F6C0212349F6E04D8E0000082010112820201128203013482040112")
                .execute();
        assertNotNull(captureResponse);
        // check response
        assertEquals("400", captureResponse.getResponseCode());
    }

    @Test
    public void test_Auth_capture_1220_4() throws ApiException {

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("23235");
        fleetData.setDriverId("3887");
        fleetData.setJobNumber("50");
        fleetData.setServicePrompt("S");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("101", UnitOfMeasure.Units, new BigDecimal(0001), new BigDecimal(21000), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("110", UnitOfMeasure.Units, new BigDecimal(0001), new BigDecimal(21000), new BigDecimal(10));

        Transaction captureResponse = response.capture(new BigDecimal(45))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("04F7A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000")
                .execute("outside");
        assertNotNull(captureResponse);
        // check response
        assertEquals("400", captureResponse.getResponseCode());
    }

    //PreAuthorization
    @Test
    public void test_VisaFleet_swipe_preAuthorizaiton_Completion() throws ApiException {

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.SelfServe,ProductCodeSet.IssuerSpecific,ProductDataFormat.VISAFLEET2Dot0);
        productData.add("51",UnitOfMeasure.Kilograms,new BigDecimal(10),new BigDecimal(10),new BigDecimal(100));
        productData.add("06",UnitOfMeasure.Gallons,new BigDecimal(5),new BigDecimal(10),new BigDecimal(50));
        productData.add("16",UnitOfMeasure.Pounds,new BigDecimal(2),new BigDecimal(20),new BigDecimal(40));
        productData.add("63",UnitOfMeasure.Liters,new BigDecimal(5),new BigDecimal(5),new BigDecimal(25));
        productData.add("32",UnitOfMeasure.Gallons,new BigDecimal(5),new BigDecimal(2),new BigDecimal(10));


        Transaction preRresponse = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
                .execute();
        assertNotNull(preRresponse);

        // check message data
        PriorMessageInformation pmi = preRresponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());

        // check response
        assertEquals("000", preRresponse.getResponseCode());

        Transaction response = preRresponse.preAuthCompletion(new BigDecimal(20))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        // check message data
        pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("202", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_025_EMV_04() throws ApiException {
        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.SelfServe,ProductCodeSet.IssuerSpecific,ProductDataFormat.VISAFLEET2Dot0);
        productData.add("51",UnitOfMeasure.Kilograms,new BigDecimal(10),new BigDecimal(10),new BigDecimal(100));
        productData.add("06",UnitOfMeasure.Gallons,new BigDecimal(5),new BigDecimal(10),new BigDecimal(50));
        productData.add("16",UnitOfMeasure.Pounds,new BigDecimal(2),new BigDecimal(20),new BigDecimal(40));
        productData.add("63",UnitOfMeasure.Liters,new BigDecimal(5),new BigDecimal(5),new BigDecimal(25));
        productData.add("32",UnitOfMeasure.Gallons,new BigDecimal(5),new BigDecimal(2),new BigDecimal(10));


        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000000041010500A4D61737465724361726457135413330089010434D22122019882803290000F5A085413330089010434820238008407A00000000410108E0A00000000000000001F00950500008080009A031901099B02E8009C01405F201A546573742F4361726420313020202020202020202020202020205F24032212315F25030401015F2A0208405F300202015F3401009F01060000000000019F02060000000006009F03060000000000009F0607A00000000410109F0702FF009F090200029F0D05B8508000009F0E0500000000009F0F05B8708098009F10120110A0800F22000065C800000000000000FF9F120A4D6173746572436172649F160F3132333435363738393031323334359F1A0208409F1C0831313232333334349F1E0831323334353637389F21030710109F26080631450565A30B759F2701809F330360F0C89F34033F00019F3501219F360200049F3704C6B1A04F9F3901059F4005F000A0B0019F4104000000869F4C0865C862608A23945A9F4E0D54657374204D65726368616E74")
                .execute("ICR");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction capture = response.capture(response.getAuthorizedAmount())
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("ICR");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }

    @Test
    public void test_VisaFleet_emv_Auth_Capture() throws ApiException {

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        ProductData productData = new ProductData(ServiceLevel.SelfServe,ProductCodeSet.IssuerSpecific,ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("01", UnitOfMeasure.Gallons, new BigDecimal("15.9999"), new BigDecimal("21.9999"));
        productData.addNonFuel("79", UnitOfMeasure.Units, new BigDecimal("100"), new BigDecimal("30"), new BigDecimal("30"));
        productData.addNonFuel("01", UnitOfMeasure.Units, new BigDecimal("145.67"), new BigDecimal("67.009"), new BigDecimal("30"));

        Transaction response = card.authorize(new BigDecimal(13.062))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000000031010820239008407A00000000310107005123456789057124485580000080017D311220115886224023F5F201A546573742F4361726420313020202020202020202020202020205A0844855800000800175F24032212315F280208405F3401008C0F1234567890123451234567890123458D13123745524364726335524364726325374552438E0C00000000000000001F0000009F0702FF009F080200019F0D05F470C498009F0E0500000000009F0F05F470C498008F01019001119F4604123456789F4701239F5801129F5901019F6804123456789F6C0212349F6E04D8E0000082010112820201128203013482040112")
                .execute();
        assertNotNull(response);
      //  assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction captureResponse = response.capture(new BigDecimal(23.56))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(captureResponse);
        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }
    @Test
    public void test_VisaFleet_emv_Auth_fuel() throws ApiException {

        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.Fuel);
        config.setAcceptorConfig(acceptorConfig);
        ServicesContainer.configureService(config);


        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        ProductData productData = new ProductData(ServiceLevel.SelfServe,ProductCodeSet.IssuerSpecific,ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("01", UnitOfMeasure.Gallons, new BigDecimal("15.9999"), new BigDecimal("21.9999"));

        Transaction response = card.authorize(new BigDecimal(13.062))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000000031010820239008407A00000000310107005123456789057124485580000080017D311220115886224023F5F201A546573742F4361726420313020202020202020202020202020205A0844855800000800175F24032212315F280208405F3401008C0F1234567890123451234567890123458D13123745524364726335524364726325374552438E0C00000000000000001F0000009F0702FF009F080200019F0D05F470C498009F0E0500000000009F0F05F470C498008F01019001119F4604123456789F4701239F5801129F5901019F6804123456789F6C0212349F6E04D8E0000082010112820201128203013482040112")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    public void test_VisaFleet_emv_Auth_nonFuel() throws ApiException {

        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.Fuel);
        config.setAcceptorConfig(acceptorConfig);
        ServicesContainer.configureService(config);


        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        ProductData productData = new ProductData(ServiceLevel.SelfServe,ProductCodeSet.IssuerSpecific,ProductDataFormat.VISAFLEET2Dot0);
        productData.addNonFuel("79", UnitOfMeasure.Units, new BigDecimal("100"), new BigDecimal("30"), new BigDecimal("30"));
        productData.addNonFuel("01", UnitOfMeasure.Units, new BigDecimal("145.67"), new BigDecimal("67.009"), new BigDecimal("30"));

        Transaction response = card.authorize(new BigDecimal(13.062))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000000031010820239008407A00000000310107005123456789057124485580000080017D311220115886224023F5F201A546573742F4361726420313020202020202020202020202020205A0844855800000800175F24032212315F280208405F3401008C0F1234567890123451234567890123458D13123745524364726335524364726325374552438E0C00000000000000001F0000009F0702FF009F080200019F0D05F470C498009F0E0500000000009F0F05F470C498008F01019001119F4604123456789F4701239F5801129F5901019F6804123456789F6C0212349F6E04D8E0000082010112820201128203013482040112")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

    }

    @Test
    public void test_VisaFleet_emv_Auth_Capture_nonFuel() throws ApiException {

        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.Fuel);
        config.setAcceptorConfig(acceptorConfig);
        ServicesContainer.configureService(config);


        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        ProductData productData = new ProductData(ServiceLevel.SelfServe,ProductCodeSet.IssuerSpecific,ProductDataFormat.VISAFLEET2Dot0);
        productData.addNonFuel("79", UnitOfMeasure.Units, new BigDecimal("100"), new BigDecimal("30"), new BigDecimal("30"));
        productData.addNonFuel("01", UnitOfMeasure.Units, new BigDecimal("145.67"), new BigDecimal("67.009"), new BigDecimal("30"));

        Transaction response = card.authorize(new BigDecimal(13.062))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000000031010820239008407A00000000310107005123456789057124485580000080017D311220115886224023F5F201A546573742F4361726420313020202020202020202020202020205A0844855800000800175F24032212315F280208405F3401008C0F1234567890123451234567890123458D13123745524364726335524364726325374552438E0C00000000000000001F0000009F0702FF009F080200019F0D05F470C498009F0E0500000000009F0F05F470C498008F01019001119F4604123456789F4701239F5801129F5901019F6804123456789F6C0212349F6E04D8E0000082010112820201128203013482040112")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction captureResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(captureResponse);
        // check message data
        PriorMessageInformation pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }
    @Test
    public void test_VisaFleet_emv_Auth_Capture_Fuel() throws ApiException {

        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.Fuel);
        config.setAcceptorConfig(acceptorConfig);
        ServicesContainer.configureService(config);


        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        ProductData productData = new ProductData(ServiceLevel.SelfServe,ProductCodeSet.IssuerSpecific,ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("01", UnitOfMeasure.Gallons, new BigDecimal("15.9999"), new BigDecimal("21.9999"));

        Transaction response = card.authorize(new BigDecimal(13.062))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000000031010820239008407A00000000310107005123456789057124485580000080017D311220115886224023F5F201A546573742F4361726420313020202020202020202020202020205A0844855800000800175F24032212315F280208405F3401008C0F1234567890123451234567890123458D13123745524364726335524364726325374552438E0C00000000000000001F0000009F0702FF009F080200019F0D05F470C498009F0E0500000000009F0F05F470C498008F01019001119F4604123456789F4701239F5801129F5901019F6804123456789F6C0212349F6E04D8E0000082010112820201128203013482040112")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction captureResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(captureResponse);
        // check message data
        PriorMessageInformation pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }




}

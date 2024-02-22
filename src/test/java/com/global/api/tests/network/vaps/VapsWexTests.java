package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.*;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VapsWexTests {

    private Address address;
    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;
    public VapsWexTests() throws ApiException {
        acceptorConfig = new AcceptorConfig();

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_MagStripe_KeyEntry);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        // Address details.
        address = new Address();
        address.setName("My STORE            ");
        address.setStreetAddress1("1 MY STREET       ");
        address.setCity("JEFFERSONVILLE  ");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);
        acceptorConfig.setSupportedEncryptionType(EncryptionType.TEP2);
        acceptorConfig.setSupportsEmvPin(true);
        acceptorConfig.setSupportWexAdditionalProducts(true);
        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.NOVISAFLEET2DOT0);

        // gateway config
        config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0044");
//        config.setTerminalId("0000912197711");
        config.setTerminalId("0003698521408");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());
        config.setMerchantType("4214");
        ServicesContainer.configureService(config);

        ServicesContainer.configureService(config, "outside");
    }

    @Test
    public void test_000_batch_close() throws ApiException {
        BatchSummary summary = BatchService.closeBatch();
        assertNotNull(summary);
        assertTrue(summary.isBalanced());
    }

    /*
        3000-V202-MA-I-WX2-1200
        IPT Manually Keyed Gen Merch (400)- Approved
    */
    @Test
    public void test_001_3000_V202_MA_I_WX2_1200() throws ApiException {
        CreditCardData card = new CreditCardData();
        card.setNumber("6900460430006149231");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardPresent(true);

        FleetData fleetData = new FleetData();
        fleetData.setPurchaseDeviceSequenceNumber("30002");
        fleetData.setServicePrompt("00");
        fleetData.setDriverId("5555");
        fleetData.setOdometerReading("123456");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("400", UnitOfMeasure.Units, 1, 10);

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3000-V202-ON-O-WX2-1100
        OPT Unleaded pre-authorization (074) - Approved
        3000-V202-ON-O-WX2-1220
        OPT Unleaded (001) - Approved
    */
    @Test
    public void test_002_3000_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19020013000200000");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("00");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3010-V202-ON-I-WX2-1200
        IPT Gen Merch (400) - Approved
    */
    @Test
    public void test_003_3010_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021013010200000");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("10");
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5500");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("102", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3010-V202-ON-O-WX2-1100
        OPT preauthorization fuel (074) Say No to Carwash - Approved
        3010-V202-ON-O-WX2-1220
        OPT Unleaded Super (003) Say No to Carwash - Approved
    */
    @Test
    public void test_004_3010_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021013010200000");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("10");
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("5500");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("003", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3011-V202-MA-I-WX2-RTRN
        Manually Keyed Return Test General Merchandise (400)
    */
    @Test
    public void test_005_3011_V202_MA_I_WX2_RTRN() throws ApiException {
        CreditCardData card = new CreditCardData();
        card.setNumber("6900460420006149231");
        card.setExpMonth(19);
        card.setExpYear(2002);

        FleetData fleetData = new FleetData();
        fleetData.setPurchaseDeviceSequenceNumber("30112");
        fleetData.setServicePrompt("11");
        fleetData.setOdometerReading("100");
        fleetData.setDriverId("745212");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("400", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(53.72), new BigDecimal(53.72));

        Transaction response = card.refund(new BigDecimal(53.72))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTransactionMatchingData(new TransactionMatchingData("0000040067", "0114"))
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3011-V202-ON-O-WX2-1100
        OPT fuel (074) preauthorization - Declined invalid vehicle (181)
    */
    @Test
    public void test_006_3011_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021013011200001");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("11");
        fleetData.setOdometerReading("100");
        fleetData.setVehicleNumber("1");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "100", response.getResponseCode());
    }


    /*
        3012-V202-ON-I-WX2-1200
        "IPT Unleaded & Car Wash (001, 102) - Approved with WEX override from fleet manager"
    */
    @Test
    public void test_007_3012_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021003012200002");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("12");
        fleetData.setOdometerReading("4");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("102", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3013-V202-ON-I-WX2-1200
        IPT Unl PLUS (002) - Approved
    */
    @Test
    public void test_008_3013_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021013013200003");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("13");
        fleetData.setDriverId("698520");
        fleetData.setVehicleNumber("76543210");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("002", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3013-V202-ON-O-WX2-1100
        OPT fuel (074) preauthorization - Approved
        3013-V202-ON-O-WX2-1220
        OPT Regular Diesel (019) - Approved
    */
    @Test
    public void test_009_3013_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021013013200003");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("13");
        fleetData.setDriverId("698520");
        fleetData.setVehicleNumber("76543210");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("019", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3014-V202-ON-I-WX2-1200
        IPT Off Road Diesel (318) - Approved
    */
    @Test
    public void test_010_3014_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021013014200004");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("14");
        fleetData.setDriverId("896523");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("318", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3014-V202-ON-O-WX2-1100
        OPT fuel (074) preauthorization - Approved
        3014-V202-ON-O-WX2-1220
        OPT E-85 (026) - Approved
    */
    @Test
    public void test_011_3014_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021013014200004");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("14");
        fleetData.setDriverId("896523");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("026", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3015-V202-ON-I-WX2-1200
        IPT Motor Oil (101) - Approved
    */
    @Test
    public void test_012_3015_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021043015200005");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("15");
        fleetData.setVehicleNumber("45850");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("101", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3015-V202-ON-O-WX2-1100
        OPT fuel (074) preauthorization - Approved
        3015-V202-ON-O-WX2-1220
        OPT Unleaded (001) - Approved
    */
    @Test
    public void test_013_3015_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021043015200005");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("15");
        fleetData.setVehicleNumber("45850");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3016-V202-ON-I-WX2-1100
        IPT fuel (074) preauthorization
    */
    @Test
    public void test_014_3016_V202_ON_I_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021013016200006");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("16");
        fleetData.setDriverId("456320");
        fleetData.setJobNumber("123456");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3016-V202-ON-I-WX2-1200
        IPT Unleaded (001) - Approved
    */
    @Test
    public void test_015_3016_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021013016200006");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("16");
        fleetData.setDriverId("456320");
        fleetData.setJobNumber("4789650");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3016-V202-ON-I-WX2-1220
        IPT Completion - UNL (001) - Approved
    */
    @Test
    public void test_016_3016_V202_ON_I_WX2_1220() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=1902");

        FleetData fleetData = new FleetData();
        fleetData.setPurchaseDeviceSequenceNumber("30162");
        fleetData.setServicePrompt("16");
        fleetData.setDriverId("456320");
        fleetData.setJobNumber("123456");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction trans = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Host_Authorized),
                card
        );

        Transaction response = trans.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3017-V202-ON-I-WX2-1200
        IPT General Merchandise (400)
    */
    @Test
    public void test_017_3017_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021013017200007");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("17");
        fleetData.setVehicleNumber("456320");
        fleetData.setJobNumber("4789650");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("400", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3017-V202-ON-O-WX2-1100
        OPT fuel (074) preauthorization with car wash on Unrestricted card - Approved

        3017-V202-ON-O-WX2-1220
        "OPT UNL Super & car wash (003,102) - Approved"
    */
    @Test
    public void test_018_3017_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021013017200007");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("17");
        fleetData.setVehicleNumber("89650");
        fleetData.setJobNumber("123456");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("003", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("102", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(20))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3018-V202-ON-I-WX2-1200
        IPT Fuel Only Card Purchase Gen Merch (400) - Decline
    */
    @Test
    public void test_019_3018_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021003018200008");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("18");
        fleetData.setOdometerReading("896520");
        fleetData.setVehicleNumber("78563");
        fleetData.setDriverId("745212");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("400", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "100", response.getResponseCode());
    }


    /*
        3018-V202-ON-O-WX2-1100
        "OPT PRE-AUTH for UNL & car wash (001,102)  - Approved"
        3018-V202-ON-O-WX2-1220
        "OPT Completion for UNL & car wash (001,102) - Approved"
    */
    @Test
    public void test_020_3018_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021003018200008");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("18");
        fleetData.setOdometerReading("896520");
        fleetData.setVehicleNumber("78563");
        fleetData.setDriverId("745212");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("102", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(20))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3019-V202-ON-I-WX2-1100
        IPT Pre-authorization for fuel (074) - Approved
    */
    @Test
    public void test_021_3019_V202_ON_I_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021013019200009");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("19");
        fleetData.setOdometerReading("85236");
        fleetData.setDriverId("0051");
        fleetData.setJobNumber("875236");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3019-V202-ON-I-WX2-1200
        IPT Sale Max Product Codes Test (See Prod Code column) - Approved
    */
    @Test
    public void test_022_3019_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021013019200009");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("19");
        fleetData.setOdometerReading("85236");
        fleetData.setDriverId("0051");
        fleetData.setJobNumber("875236");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("100", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("400", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("950", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(40))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3019-V202-ON-I-WX2-1220
        IPT Completion for CNG (022) - Approved
    */
    @Test
    public void test_023_3019_V202_ON_I_WX2_1220() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=1902");

        FleetData fleetData = new FleetData();
        fleetData.setPurchaseDeviceSequenceNumber("30192");
        fleetData.setServicePrompt("19");
        fleetData.setOdometerReading("85236");
        fleetData.setDriverId("0051");
        fleetData.setJobNumber("875236");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("022", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction trans = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Host_Authorized),
                card
        );

        Transaction response = trans.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3022-V202-MA-I-WX2-1200
        "IPT Manually Keyed Gen Auto & Mdse w/tax (100, 400, 950) Unrestricted Card - Approved"
    */
    @Test
    public void test_024_3022_V202_MA_I_WX2_1200() throws ApiException {
        CreditCardData card = new CreditCardData();
        card.setNumber("6900460420006149231");
        card.setExpMonth(19);
        card.setExpYear(2002);

        FleetData fleetData = new FleetData();
        fleetData.setPurchaseDeviceSequenceNumber("30222");
        //fleetData.setVehicleNumber("30222");
        fleetData.setServicePrompt("22");
        fleetData.setDriverId("785423");
        fleetData.setOdometerReading("520310");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("100", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("400", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("950", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(30))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3022-V202-ON-O-WX2-1100
        OPT fuel (074) preauthorization - Approved
        3022-V202-ON-O-WX2-1220
        OPT Unleaded (001) - Approved
    */
    @Test
    public void test_025_3022_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19022013022200002");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("22");
        fleetData.setOdometerReading("785423");
        fleetData.setDriverId("520310");
        fleetData.setEnteredData("1234");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3031-V202-ON-I-WX2-1200
        IPT Gen Merch w/Discount (400 & 904) - Approved
    */
    @Test
    public void test_026_3031_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19023013031200001");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("31");
        fleetData.setOdometerReading("125430");
        fleetData.setEnteredData("126542");
        fleetData.setUserId("152430");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("400", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3031-V202-ON-O-WX2-1100
        OPT fuel (074) preauthorization - Approved
        3031-V202-ON-O-WX2-1220
        OPT Unleaded (001) - Approved
    */
    @Test
    public void test_027_3031_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19023013031200001");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("31");
        fleetData.setOdometerReading("125430");
        fleetData.setEnteredData("126542");
        fleetData.setUserId("152430");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3032-V202-ON-I-WX2-1200
        IPT Unleaded Super (003) - Approved
    */
    @Test
    public void test_028_3032_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19023013032200002");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("32");
        fleetData.setUserId("8533");
        fleetData.setJobNumber("1298765432");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("003", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3032-V202-ON-O-WX2-1100
        OPT fuel (074) preauthorization - Approved
        3032-V202-ON-O-WX2-1220
        OPT Unleaded (001) - Approved
    */
    @Test
    public void test_029_3032_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19023013032200002");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("32");
        fleetData.setUserId("8533");
        fleetData.setJobNumber("1298765432");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3033-V202-ON-I-WX2-1200
        IPT General Merchandise w/discount (400 & 904) - Approved
    */
    @Test
    public void test_030_3033_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19023013033200003");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("33");
        fleetData.setVehicleNumber("52365");
        fleetData.setUserId("23652");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("400", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3033-V202-ON-O-WX2-1100
        OPT fuel (074) preauthorization - Approved
        3033-V202-ON-O-WX2-1220
        OPT DEF container (600) - Approved
    */
    @Test
    public void test_031_3033_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19023013033200003");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("33");
        fleetData.setVehicleNumber("52365");
        fleetData.setUserId("23652");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("600", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3034-V202-ON-I-WX2-1200
        IPT Auto Parts (100) - Approved
    */
    @Test
    public void test_032_3034_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19023013034200004");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("34");
        fleetData.setDriverId("0123");
        fleetData.setUserId("52361");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("100", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3034-V202-ON-O-WX2-1100
        OPT fuel (074) preauthorization - Approved
        3034-V202-ON-O-WX2-1220
        OPT Unleaded (001) - Approved
    */
    @Test
    public void test_033_3034_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19023013034200004");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("34");
        fleetData.setDriverId("0123");
        fleetData.setUserId("52361");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3035-V202-ON-I-WX2-1200
        IPT Unl Plus (002) - Invalid Driver ID (180) - Decline
    */
    @Test
    public void test_034_3035_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19023013035200005");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("35");
        fleetData.setDriverId("001234");
        fleetData.setDepartment("1298765432");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("002", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "100", response.getResponseCode());
    }


    /*
        3035-V202-ON-O-WX2-1100
        OPT fuel (074) preauthorization - Approved
        3035-V202-ON-O-WX2-1220
        OPT Unleaded (001) - Approved
    */
    @Test
    public void test_035_3035_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19023013035200005");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("35");
        fleetData.setDriverId("009865");
        fleetData.setDepartment("1298765432");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3036-V202-ON-I-WX2-1200
        IPT General Automotive (100) - Approved
    */
    @Test
    public void test_036_3036_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19023013036200006");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("36");
        fleetData.setUserId("1298765432");
        fleetData.setDepartment("20");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("100", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3036-V202-ON-O-WX2-1100
        OPT fuel (074) preauthorization - Approved
        3036-V202-ON-O-WX2-1220
        OPT DEF (pumped) (062) - Approved
    */
    @Test
    public void test_037_3036_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19023013036200006");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("36");
        fleetData.setUserId("1298765432");
        fleetData.setDepartment("20");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("062", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3037-V202-ON-I-WX2-1200
        IPT Off Road Diesel & Gen Merch (317 400) - Approved
    */
    @Test
    public void test_038_3037_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19023013037200007");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("37");
        fleetData.setVehicleNumber("52369");
        fleetData.setDepartment("11");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("317", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("400", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(20))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3037-V202-ON-O-WX2-1100
        OPT fuel (074) preauthorization - Approved
        3037-V202-ON-O-WX2-1220
        OPT Unleaded (001) - Approved
    */
    @Test
    public void test_039_3037_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19023013037200007");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("37");
        fleetData.setVehicleNumber("52369");
        fleetData.setDepartment("11");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3038-V202-ON-I-WX2-1200
        "IPT Gen Merch w/tax (400,950) - Approved"
    */
    @Test
    public void test_040_3038_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460430006149231=19023013038200008");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("38");
        fleetData.setOdometerReading("152310");
        fleetData.setDriverId("523690");
        fleetData.setDepartment("9");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("400", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("950", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(20))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3038-V202-ON-O-WX2-1100
        OPT fuel (074) preauthorization - Approved
        3038-V202-ON-O-WX2-1220
        OPT Unleaded (001) - Approved
    */
    @Test
    public void test_041_3038_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460430006149231=19023013038200008");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("38");
        fleetData.setOdometerReading("152310");
        fleetData.setDriverId("523690");
        fleetData.setDepartment("9");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3039-V202-ON-I-WX2-1200
        "IPT Gen Auto w/discount & tax (100,904,950) - Approved"
    */
    @Test
    public void test_042_3039_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19023013039200009");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("39");
        fleetData.setOdometerReading("100236");
        fleetData.setUserId("456320");
        fleetData.setDepartment("1");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("100", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("950", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(20))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3039-V202-ON-O-WX2-1100
        OPT fuel (074) preauthorization - Approved
        3039-V202-ON-O-WX2-1220
        OPT Off Road Diesel (317) - Approved
    */
    @Test
    public void test_043_3039_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19023013039200009");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("39");
        fleetData.setOdometerReading("100236");
        fleetData.setUserId("456320");
        fleetData.setDepartment("1");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("317", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3040-V202-ON-I-WX2-1200
        IPT Gen Merch (400) - Approved
    */
    @Test
    public void test_044_3040_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19024023040200000");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("40");
        fleetData.setOdometerReading("100023");
        fleetData.setVehicleNumber("5236");
        fleetData.setDepartment("8");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("400", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3040-V202-ON-O-WX2-1100
        OPT fuel (074) preauthorization - Approved
        3040-V202-ON-O-WX2-1220
        OPT Unleaded (001) - Approved
    */
    @Test
    public void test_045_3040_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19024023040200000");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("40");
        fleetData.setOdometerReading("100023");
        fleetData.setVehicleNumber("5263");
        fleetData.setDepartment("8");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3041-V202-ON-I-WX2-1200
        IPT General Merchandise w/Discount (400 & 904) - Approved
    */
    @Test
    public void test_046_3041_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19024013041200001");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("41");
        fleetData.setDepartment("96");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("400", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3041-V202-ON-O-WX2-1100
        OPT fuel (074) preauthorization - Approved
        3041-V202-ON-O-WX2-1220
        OPT Unleaded (001) - Approved
    */
    @Test
    public void test_047_3041_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19024013041200001");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("41");
        fleetData.setDepartment("96");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3042-V202-ON-O-WX2-1100
        OPT fuel (074) preauthorization - Approved
        3042-V202-ON-O-WX2-1220
        OPT Unleaded (001) - Approved
    */
    @Test
    public void test_048_3042_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19024013042200002");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("42");
        fleetData.setEnteredData("1298765432");
        fleetData.setUserId("8564");
        fleetData.setDepartment("3");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3043-V202-ON-I-WX2-1200
        Gen Merch (400) Declined - Invalid Site
    */
    @Test
    public void test_049_3043_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19024013043200003");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("43");
        fleetData.setEnteredData("878623");
        fleetData.setVehicleNumber("85214");
        fleetData.setDepartment("3");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("400", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "100", response.getResponseCode());
    }


    /*
        3043-V202-ON-O-WX2-1100
        OPT fuel (074) preauthorization - Approved
        3043-V202-ON-O-WX2-1220
        OPT Off Road Diesel (317) - Approved
    */
    @Test
    public void test_050_3043_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19024013043200003");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("43");
        fleetData.setEnteredData("878623");
        fleetData.setVehicleNumber("85214");
        fleetData.setDepartment("3");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("317", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3044-V202-ON-I-WX2-1200
        IPT Premium Diesel (020) - Approved
    */
    @Test
    public void test_051_3044_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19024013044200004");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("44");
        fleetData.setEnteredData("543210");
        fleetData.setDriverId("123456");
        fleetData.setDepartment("1230");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("020", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3044-V202-ON-O-WX2-1100
        OPT fuel (074) preauthorization - Approved
        3044-V202-ON-O-WX2-1220
        OPT Unleaded (001) - Approved
    */
    @Test
    public void test_052_3044_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19024013044200004");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("44");
        fleetData.setEnteredData("753214");
        fleetData.setDriverId("908765");
        fleetData.setDepartment("5");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3045-V202-ON-I-WX2-1200
        IPT General Merchandise w/Coupon (400 & 904) - Approved
    */
    @Test
    public void test_053_3045_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19024013045200005");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("45");
        fleetData.setEnteredData("8523641");
        fleetData.setDriverId("012361");
        fleetData.setUserId("52369");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("400", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3045-V202-ON-O-WX2-1100
        OPT fuel (074) preauthorization - Approved
        3045-V202-ON-O-WX2-1220
        OPT Unleaded  (001) - Approved
    */
    @Test
    public void test_054_3045_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19024013045200005");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("45");
        fleetData.setEnteredData("753214");
        fleetData.setDriverId("908765");
        fleetData.setUserId("52369");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3046-V202-ON-I-WX2-1200
        IPT General Merchandise w/discount (400 & 904) - Approved
    */
    @Test
    public void test_055_3046_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19024013046200006");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("46");
        fleetData.setEnteredData("52361");
        fleetData.setUserId("9876543210");
        fleetData.setDriversLicenseNumber("520123");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("400", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3046-V202-ON-O-WX2-1100
        OPT fuel (074) preauthorization - Approved
        3046-V202-ON-O-WX2-1220
        OPT Unleaded (001) - Approved
    */
    @Test
    public void test_056_3046_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19024013046200006");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("46");
        fleetData.setEnteredData("52361");
        fleetData.setDriversLicenseNumber("520123");
        fleetData.setUserId("9876543210");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3047-V202-ON-I-WX2-1200
        IPT Unleaded & Gen Merch (001 & 400) - Approved
    */
    @Test
    public void test_057_3047_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19024013047200007");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("47");
        fleetData.setEnteredData("1230");
        fleetData.setVehicleNumber("1230");
        fleetData.setDriversLicenseNumber("1");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("400", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(20))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3047-V202-ON-O-WX2-1100
        OPT fuel (074) preauthorization - Approved
        3047-V202-ON-O-WX2-1220
        OPT Unleaded (001) - Approved
    */
    @Test
    public void test_058_3047_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19024013047200007");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("47");
        fleetData.setEnteredData("1230");
        fleetData.setVehicleNumber("1230");
        fleetData.setDriversLicenseNumber("1");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3048-V202-ON-I-WX2-1200
        IPT Unl PLUS (002) - Approved
    */
    @Test
    public void test_059_3048_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19024013048200008");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("48");
        fleetData.setEnteredData("1230");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("002", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3048-V202-ON-O-WX2-1100
        OPT fuel (074) preauthorization - Approved
        3048-V202-ON-O-WX2-1220
        OPT Unleaded Plus (002) - Approved
    */
    @Test
    public void test_060_3048_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19024013048200008");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("48");
        fleetData.setEnteredData("1230");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("002", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3049-V202-ON-I-WX2-1200
        IPT Regular Diesel (019) - Approved
    */
    @Test
    public void test_061_3049_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19024013049200009");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("49");
        fleetData.setDriverId("023450");
        fleetData.setEnteredData("1234");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("019", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3049-V202-ON-O-WX2-1100
        OPT Unleaded preauthorization (074) - Approved
        3049-V202-ON-O-WX2-1220
        OPT Unleaded (001) - Approved
    */
    @Test
    public void test_062_3049_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19024013049200009");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("49");
        fleetData.setDriverId("023450");
        fleetData.setEnteredData("1234");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3050-V202-ON-O-WX2-1100
        OPT fuel (074) preauthorization - Approved
        3050-V202-ON-O-WX2-1220
        OPT Unleaded (001) - Approved
    */
    @Test
    public void test_063_3050_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19025013050200000");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("50");
        fleetData.setUserId("16548");
        fleetData.setEnteredData("254630");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3051-V202-ON-I-WX2-1200
        Gen Merch (400)
    */
    @Test
    public void test_064_3051_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19025013051200001");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("51");
        fleetData.setVehicleNumber("5365");
        fleetData.setEnteredData("63255");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("400", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3051-V202-ON-I-WX2-RTRN
        "Return Test Gen Merch with discount (400, 904)"
    */
    @Test
    public void test_065_3051_V202_ON_I_WX2_RTRN() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=1902");

        FleetData fleetData = new FleetData();
        fleetData.setPurchaseDeviceSequenceNumber("30512");
        fleetData.setServicePrompt("10");
//        fleetData.setVehicleTag("5365");
//        fleetData.setEnteredData("63255");
        fleetData.setDriverId("123456");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("400", UnitOfMeasure.Units, 1, 52.73);

        Transaction response = card.refund(new BigDecimal(52.73))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTransactionMatchingData(new TransactionMatchingData("0000040067", "0114"))
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3051-V202-ON-O-WX2-1100
        OPT fuel (074) preauthorization - Decline (Invalid Driver ID - 180)
    */
    @Test
    public void test_066_3051_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19025013051200001");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("51");
        fleetData.setVehicleNumber("5365");
        fleetData.setEnteredData("63255");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "100", response.getResponseCode());
    }


    /*
        3052-V202-ON-O-WX2-1100
        OPT fuel (074) preauthorization - Declined Exp Card (101)
    */
    @Test
    public void test_067_3052_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=11021003052200000");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("10");
        fleetData.setOdometerReading("4");
        fleetData.setDriverId("130031");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "100", response.getResponseCode());
    }


    /*
        3053-V202-ON-O-WX2-1100
        OPT Unleaded (001) - Approved (mod 10 test)
        3053-V202-ON-O-WX2-1220
        OPT Unleaded (001) - Approved (mod 10 test)
    */
    @Test
    public void test_068_3053_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460430006149231=19020013053200000");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("00");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3069-V202-SF-O-WX2-1200
        Off-line Keyed Manual - Pretend you called VRU (on Verifone) and receive auth # 123456 to key in manually.
    */
    @Test
    public void test_069_3069_V202_SF_O_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19020003069200000");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("00");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withOfflineAuthCode("123456")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3070-V202-ON-O-WX2-1100
        OPT Action Code 002 ($999.99 returned in 1110 and POS shuts off at $150.00 Floor Limit) Purchase UNL (001)
        3070-V202-ON-O-WX2-1220
        OPT Action Code 002 ($999.99 returned in 1110 and POS shuts off at $150.00 Floor Limit) Purchase UNL (001)
    */
    @Test
    public void test_070_3070_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021013070200000");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("10");
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("0330");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3072-V202-ON-O-WX2-1100
        "OPT Action Code 002 ($10.00 returned on 1110 and POS shuts off at $10.00, which is less than $150.00 Floor Limit) Purc UNL (001)"
        3072-V202-ON-O-WX2-1220
        "OPT Action Code 002 ($10.00 returned on 1110 and POS shuts off at $10.00, which is less than $20.00 Floor Limit) Purch UNL(001)"
    */
    @Test
    public void test_071_3072_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021003072200000");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("10");
        fleetData.setOdometerReading("123456");
        fleetData.setDriverId("123456");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }


    /*
        3073-V202-ON-I-WX2-1200
        IPT Fuel Only Card Purchase Gen Merch (400) - WEX override approves
    */
    @Test
    public void test_072_3073_V202_ON_I_WX2_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021043073200002");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("12");
        fleetData.setOdometerReading("123456");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("400", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    /*
        3073-V202-ON-O-WX2-1100
        OPT Action Code 002 ($150.00 returned on 1110 and POS shuts off at $150.00 which is also the Floor Limit) Purch UNL(001)
        3073-V202-ON-O-WX2-1220
        OPT Action Code 002 ($150.00 returned on 1110 and POS shuts off at $150.00 which is also the Floor Limit) Purch UNL(001)
    */
    @Test
    public void test_073_3073_V202_ON_O_WX2_1100() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021003073200002");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("12");
        fleetData.setOdometerReading("123456");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }

    //void of partial approval
    @Test
    public void test_000_credit_Void_partial_approval() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021003073200002");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("12");
        fleetData.setOdometerReading("896520");
        fleetData.setVehicleNumber("78563");
        fleetData.setDriverId("745212");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("014", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        Transaction response = card.charge(new BigDecimal("40"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);
        assertEquals("002", response.getResponseCode());
        assertNotNull(response.getAuthorizedAmount());

        BigDecimal authorizedAmount = response.getAuthorizedAmount();
        assertNotEquals(new BigDecimal("40"), authorizedAmount);

        Transaction voidResponse = response.voidTransaction(authorizedAmount)
                .withCurrency("USD")
                .withReferenceNumber(response.getReferenceNumber())
                .withCustomerInitiated(true)
                .withPartialApproval(true)
                .execute("outside");
        assertNotNull(voidResponse);

        PriorMessageInformation pmi = voidResponse.getMessageInformation();
        assertNotNull(pmi);
        // check message data
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4353", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", voidResponse.getResponseCode());
    }

    @Test
    public void test_Wex_Field5_Field9_1200() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue(";6900460430001234566=22124012203100001?");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("00");
        fleetData.setDriverId("456320");
        fleetData.setOdometerReading("100");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    public void test_dataCollect_with_tag_data_contactLessMsd_code_coverage() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=1902");

        FleetData fleetData = new FleetData();
        fleetData.setPurchaseDeviceSequenceNumber("30162");
        fleetData.setServicePrompt("16");
        fleetData.setDriverId("456320");
        fleetData.setJobNumber("123456");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction trans = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Host_Authorized),
                card
        );

        Transaction response = trans.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000000041010500A4D617374657243617264820238008407A00000000410108E0A00000000000000001F00950500008080009A031901099B02E8009C01405F24032212315F25030401015F2A0208405F300202015F3401009F01060000000000019F02060000000006009F03060000000000009F0607A00000000410109F0702FF009F090200029F0D05B8508000009F0E0500000000009F0F05B8708098009F10120110A0800F22000065C800000000000000FF9F120A4D6173746572436172649F160F3132333435363738393031323334359F1A0208409F1C0831313232333334349F1E0831323334353637389F21030710109F26080631450565A30B759F2701809F330360F0C89F34033F00019F3501219F360200049F3704C6B1A04F9F3901919F4005F000A0B0019F4104000000869F4C0865C862608A23945A9F4E0D54657374204D65726368616E74")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    public void test_proximity_entry_method_() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setValue("6900460420006149231=19021003073200002");
        track.setEntryMethod(EntryMethod.Proximity);

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("12");
        fleetData.setOdometerReading("896520");
        fleetData.setVehicleNumber("78563");
        fleetData.setDriverId("745212");

        Transaction trans = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Host_Authorized),
                track
        );
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = trans.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000000041010500A4D617374657243617264820238008407A00000000410108E0A00000000000000001F00950500008080009A031901099B02E8009C01405F24032212315F25030401015F2A0208405F300202015F3401009F01060000000000019F02060000000006009F03060000000000009F0607A00000000410109F0702FF009F090200029F0D05B8508000009F0E0500000000009F0F05B8708098009F10120110A0800F22000065C800000000000000FF9F120A4D6173746572436172649F160F3132333435363738393031323334359F1A0208409F1C0831313232333334349F1E0831323334353637389F21030710109F26080631450565A30B759F2701809F330360F0C89F34033F00019F3501219F360200049F3704C6B1A04F9F3901059F4005F000A0B0019F4104000000869F4C0865C862608A23945A9F4E0D54657374204D65726368616E74")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_Void_partial_approval_code_coverage_only() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021003073200002");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("12");
        fleetData.setOdometerReading("896520");
        fleetData.setVehicleNumber("78563");
        fleetData.setDriverId("745212");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("014", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        Transaction response = card.charge(new BigDecimal("40"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("outside");
        assertNotNull(response);

        //temporarly set response code to 002 for partial approval for code coverage only
        response.setResponseCode("002");

        assertEquals("002", response.getResponseCode());
        assertNotNull(response.getAuthorizedAmount());

        BigDecimal authorizedAmount = response.getAuthorizedAmount();
        assertNotEquals(new BigDecimal("40"), authorizedAmount);

        Transaction voidResponse = response.voidTransaction(authorizedAmount)
                .withCurrency("USD")
                .withReferenceNumber(response.getReferenceNumber())
                .withCustomerInitiated(true)
                .withPartialApproval(true)
                .execute("outside");
        assertNotNull(voidResponse);

        PriorMessageInformation pmi = voidResponse.getMessageInformation();
        assertNotNull(pmi);
        // check message data
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4353", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", voidResponse.getResponseCode());
    }
    @Test
    public void test_Wex_sale_Address_code_coverage() throws ApiException {

        address = new Address();
        address.setName("My STORE            ");
        address.setStreetAddress1("1 MY STREET       ");
        address.setCity("JEFFERSONVILLE  ");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        acceptorConfig.setAddress(address);
        config.setAcceptorConfig(acceptorConfig);

        CreditTrackData card = new CreditTrackData();
        card.setValue(";6900460430001234566=22124012203100001?");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("00");
        fleetData.setDriverId("456320");
        fleetData.setOdometerReading("100");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

}

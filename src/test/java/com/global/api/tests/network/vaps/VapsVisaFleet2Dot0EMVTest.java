package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
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
public class VapsVisaFleet2Dot0EMVTest {
    private CreditTrackData card;
    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;
    private String tagData = "4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F4104000000009F0A06000210840000";
    private String visaTagData = "4F07A0000000031010820239008407A00000000310107005123456789057124485580000080017D311220115886224023F5F201A546573742F4361726420313020202020202020202020202020205A0844855800000800175F24032212315F280208405F3401008C0F1234567890123451234567890123458D13123745524364726335524364726325374552438E0C00000000000000001F0000009F0702FF009F080200019F0D05F470C498009F0E0500000000009F0F05F470C498008F01019001119F4604123456789F4701239F5801129F5901019F6804123456789F6C0212349F6E04D8E0000082010112820201128203013482040112";

    public VapsVisaFleet2Dot0EMVTest() throws ApiException {
        acceptorConfig = new AcceptorConfig();


        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_MagStripe_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.ByMerchant);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);

        // hardware software config values
        acceptorConfig.setHardwareLevel("S1");
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
        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.Fuel);
        acceptorConfig.setSupportTerminalPurchaseRestriction(PurchaseRestrictionCapability.CHIPBASEDPRODUCTRESTRICTION);
        acceptorConfig.setVisaFleet2(true);

        //DE48-34 message configuration values
        acceptorConfig.setPerformDateCheck(true);
        acceptorConfig.setEchoSettlementData(true);
        acceptorConfig.setIncludeLoyaltyData(false);


        // gateway config
        config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setUniqueDeviceId("2001");
        config.setCompanyId("0044");
        config.setTerminalId("0007998855611");
//        config.setTerminalId("0000912197711");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());
        config.setMerchantType("5541");

        ServicesContainer.configureService(config);

        card = new CreditTrackData();
        card.setValue(";4485580000080017=311220115886224023?");
    }

    @Test
    public void test_1100_authorization_06_DE63() throws ApiException {

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("01", UnitOfMeasure.Gallons, new BigDecimal("15.1134"), new BigDecimal("2.1010"));

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        Transaction response = card.authorize(new BigDecimal("31.75"), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_1100_authorization_VehicleID_DE48_8() throws ApiException {

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("01", UnitOfMeasure.Gallons, new BigDecimal("15.1134"), new BigDecimal("2.1010"));

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setVehicleNumber("111222");
        fleetData.setIdNumber("987654");
        fleetData.setOdometerReading("221123");

        Transaction response = card.authorize(new BigDecimal("13.90"), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_1100_authorization_DE54() throws ApiException {

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("01", UnitOfMeasure.Gallons, new BigDecimal("15.1134"), new BigDecimal("2.1010"));

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        Transaction response = card.authorize(new BigDecimal("31.75"), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_1100_authorization_DE54_GrossFuelPrice() throws ApiException {

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("01", UnitOfMeasure.Gallons, new BigDecimal("15.1134"), new BigDecimal("2.1010"));
        productData.addSaleTax(new BigDecimal("10.11"));

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        Transaction response = card.authorize(new BigDecimal("13.90"), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_1100_authorization_DE54_GrossFuelPrice_F_NF() throws ApiException {
        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.FuelAndNonFuel);
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("01", UnitOfMeasure.Gallons, new BigDecimal("15.1134"), new BigDecimal("2.1010"));
        productData.addNonFuel("DN", UnitOfMeasure.Units, new BigDecimal("11.0091"), new BigDecimal("21"));
        productData.addNonFuel("79", UnitOfMeasure.Quarts, new BigDecimal("11.1101"), new BigDecimal("31"));

        productData.addSaleTax(new BigDecimal("10.11"));

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setVehicleNumber("111222");
        fleetData.setOdometerReading("221123");

        Transaction response = card.authorize(new BigDecimal("13.90"), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_VisaFleet_emv_Auth_1200_1() throws ApiException {

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("01", UnitOfMeasure.Gallons, new BigDecimal("15.1134"), new BigDecimal("2.1010"));

        Transaction response = card.authorize(new BigDecimal(31.75), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000000031010820239008407A00000000310107005123456789057124485580000080017D311220115886224023F5F201A546573742F4361726420313020202020202020202020202020205A0844855800000800175F24032212315F280208405F3401008C0F1234567890123451234567890123458D13123745524364726335524364726325374552438E0C00000000000000001F0000009F0702FF009F080200019F0D05F470C498009F0E0500000000009F0F05F470C498008F01019001119F4604123456789F4701239F5801129F5901019F6804123456789F6C0212349F6E04D8E0000082010112820201128203013482040112")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_1100_authorization_029_1F2NF() throws ApiException {

        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.Fuel);

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("01", UnitOfMeasure.Gallons, new BigDecimal("15.1134"), new BigDecimal("2.1010"));
        productData.addNonFuel("79", UnitOfMeasure.Quarts, new BigDecimal("11.0091"), new BigDecimal("31"), new BigDecimal("20.0901"));
        productData.addNonFuel("DN", UnitOfMeasure.Units, new BigDecimal("11.0091"), new BigDecimal("21"), new BigDecimal("10.0011"));


        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        Transaction response = card.authorize(new BigDecimal("31.75"), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test //Attempt to run with 2 Fuel product
    public void test_1100_authorization_029_2F2NF() throws ApiException {

        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.Fuel);

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("01", UnitOfMeasure.Gallons, new BigDecimal("15.1134"), new BigDecimal("2.1010"));
        productData.addFuel("05", UnitOfMeasure.Quarts, new BigDecimal("11.0091"), new BigDecimal("31"));
        productData.addNonFuel("DN", UnitOfMeasure.Units, new BigDecimal("11.1121"), new BigDecimal("21"));


        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        Transaction response = card.authorize(new BigDecimal("31.75"), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test //Attempt to run more than 8 NonFuel product
    public void test_1100_authorization_029_8NF() throws ApiException {

        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.FuelAndNonFuel);

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("01", UnitOfMeasure.Gallons, new BigDecimal("15.1134"), new BigDecimal("2.1010"));
        productData.addNonFuel("79", UnitOfMeasure.Quarts, new BigDecimal("10"), new BigDecimal("31"), new BigDecimal("20"));
        productData.addNonFuel("DN", UnitOfMeasure.Units, new BigDecimal("10"), new BigDecimal("21"), new BigDecimal("10"));
        productData.addNonFuel(ProductCode.UNLEADED_PLUS_ETHANOL, UnitOfMeasure.Units, new BigDecimal("0002"), new BigDecimal("0.66"), new BigDecimal("1.32"));
        productData.addNonFuel(ProductCode.SUPER_UNLEADED_ETHANOL, UnitOfMeasure.Units, new BigDecimal("0002"), new BigDecimal("0.66"), new BigDecimal("1.32"));
        productData.addNonFuel(ProductCode.BIODIESEL, UnitOfMeasure.Units, new BigDecimal("0002"), new BigDecimal("0.66"), new BigDecimal("1.32"));
        productData.addNonFuel(ProductCode.Car_Wash, UnitOfMeasure.Units, new BigDecimal("0001"), new BigDecimal("0.66"), new BigDecimal("1.32"));
        productData.addNonFuel(ProductCode.Brake_Service, UnitOfMeasure.Units, new BigDecimal("0001"), new BigDecimal("0.66"), new BigDecimal("1.32"));
        productData.addNonFuel(ProductCode.Tires, UnitOfMeasure.Units, new BigDecimal("1"), new BigDecimal("12.74"), new BigDecimal("12.74"));
        productData.addNonFuel(ProductCode.Filters, UnitOfMeasure.Units, new BigDecimal("1"), new BigDecimal("12.74"), new BigDecimal("12.74"));


        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        Transaction response = card.authorize(new BigDecimal("31.75"), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_45_authorization_FuelNonFuel_DE54_43() throws ApiException {
        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.FuelAndNonFuel);

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("02", UnitOfMeasure.Gallons, new BigDecimal("50.0001"), new BigDecimal("10.0110"), new BigDecimal("500.55"));
        productData.addNonFuel("79", UnitOfMeasure.Quarts, new BigDecimal("10.1231"), new BigDecimal("31"), new BigDecimal("20"));
        productData.addNonFuel("DM", UnitOfMeasure.Units, new BigDecimal("10.0011"), new BigDecimal("21"), new BigDecimal("10"));

        productData.addSaleTax(new BigDecimal("11.0"));

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setVehicleNumber("221123");

        Transaction response = card.authorize(new BigDecimal("500.55"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_45_authorization_NonFuel_DE54_43_discount() throws ApiException {
        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.NonFuel);

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addNonFuel("79", UnitOfMeasure.Quarts, new BigDecimal("10.1211"), new BigDecimal("31"), new BigDecimal("20"));
        productData.addNonFuel("DM", UnitOfMeasure.Units, new BigDecimal("10.1120"), new BigDecimal("21"), new BigDecimal("10"));

        productData.addSaleTax(new BigDecimal("11.0"));
        productData.addDiscount(new BigDecimal("12.22"));

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setVehicleNumber("221123");

        Transaction response = card.authorize(new BigDecimal("500.55"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    //2./49.validate DE 48-33.7 and DE 48-33.8
    @Test
    public void test_1220_authorization_PRC() throws ApiException {

        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.Fuel);
        acceptorConfig.setSupportTerminalPurchaseRestriction(PurchaseRestrictionCapability.BOTHCHIPANDHOSTBASEDPRODUCTRESTRICTION);

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("01", UnitOfMeasure.Gallons, new BigDecimal("7.1870"), new BigDecimal("2.4321"), new BigDecimal("17.48"));

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        Transaction response = card.authorize(new BigDecimal(17.48), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_33_authorization_AFD_DE48_8e() throws ApiException {

        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.Fuel);
        acceptorConfig.setSupportTerminalPurchaseRestriction(PurchaseRestrictionCapability.HOSTBASEDPRODUCTRESTRICTION);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);


        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("03", UnitOfMeasure.Gallons, new BigDecimal("11.1010"), new BigDecimal("2.2341"), new BigDecimal("24.80"));

        FleetData fleetData = new FleetData();
        fleetData.setAdditionalPromptData1("001");//d
        fleetData.setAdditionalPromptData2("002");//e
        fleetData.setEmployeeNumber("1212");//f

        Transaction response = card.authorize(new BigDecimal("24.80"), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_33_authorization_AFD_DE48_8s() throws ApiException {

        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.Fuel);
        acceptorConfig.setSupportTerminalPurchaseRestriction(PurchaseRestrictionCapability.HOSTBASEDPRODUCTRESTRICTION);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);


        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("03", UnitOfMeasure.Gallons, new BigDecimal("11.1010"), new BigDecimal("2.2341"), new BigDecimal("24.80"));

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("00");//s

        Transaction response = card.authorize(new BigDecimal("24.80"), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_33_authorization_AFD_DE48_8f() throws ApiException {

        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.Fuel);
        acceptorConfig.setSupportTerminalPurchaseRestriction(PurchaseRestrictionCapability.HOSTBASEDPRODUCTRESTRICTION);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);


        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("03", UnitOfMeasure.Gallons, new BigDecimal("11.1010"), new BigDecimal("2.2341"), new BigDecimal("24.80"));

        FleetData fleetData = new FleetData();
        fleetData.setEmployeeNumber("1212");//f

        Transaction response = card.authorize(new BigDecimal("24.80"), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_33_authorization_AFD_DE48_8b() throws ApiException {

        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.Fuel);
        acceptorConfig.setSupportTerminalPurchaseRestriction(PurchaseRestrictionCapability.HOSTBASEDPRODUCTRESTRICTION);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);


        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("03", UnitOfMeasure.Gallons, new BigDecimal("11.1010"), new BigDecimal("2.2341"), new BigDecimal("24.80"));

        FleetData fleetData = new FleetData();
        fleetData.setTrailerNumber("1223");//b

        Transaction response = card.authorize(new BigDecimal("24.80"), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_33_authorization_AFD_DE48_8_4() throws ApiException {

        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.Fuel);
        acceptorConfig.setSupportTerminalPurchaseRestriction(PurchaseRestrictionCapability.HOSTBASEDPRODUCTRESTRICTION);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);


        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("03", UnitOfMeasure.Gallons, new BigDecimal("11.1010"), new BigDecimal("2.2341"), new BigDecimal("24.80"));

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("221123"); //4

        Transaction response = card.authorize(new BigDecimal("24.80"), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_24_authorization_capture_discount() throws ApiException {

        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.Fuel);
        acceptorConfig.setSupportTerminalPurchaseRestriction(PurchaseRestrictionCapability.CHIPBASEDPRODUCTRESTRICTION);

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("03", UnitOfMeasure.Gallons, new BigDecimal("11.1011"), new BigDecimal("2.1221"));

        productData.addSaleTax(new BigDecimal("20"));
        productData.addDiscount(new BigDecimal("11.11"));

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        Transaction response = card.authorize(new BigDecimal("23.56"), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction captureResponse = response.preAuthCompletion(new BigDecimal("23.56"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(captureResponse);
        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_1220_authorization_capture_38_DE48_8_b() throws ApiException {

        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.Fuel);
        acceptorConfig.setSupportTerminalPurchaseRestriction(PurchaseRestrictionCapability.BOTHCHIPANDHOSTBASEDPRODUCTRESTRICTION);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("02", UnitOfMeasure.Gallons, new BigDecimal("11.1011"), new BigDecimal("2.1221"), new BigDecimal("23.56"));

        FleetData fleetData = new FleetData();
        fleetData.setTrailerNumber("1223");//b

        Transaction response = card.authorize(new BigDecimal(23.56), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        response.setNtsData(new NtsData());
        TransactionReference transactionReference = response.getTransactionReference();
        transactionReference.setNtsData(new NtsData());
        transactionReference.setAuthCode(response.getAuthorizationCode());
        response.setTransactionReference(transactionReference);


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
    public void test_swipe_voice_capture() throws ApiException {
        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
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
                response.getAuthorizationCode(),
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
    public void test_1220_authorization_capture_38_DE48_8() throws ApiException {

        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.Fuel);
        acceptorConfig.setSupportTerminalPurchaseRestriction(PurchaseRestrictionCapability.BOTHCHIPANDHOSTBASEDPRODUCTRESTRICTION);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("02", UnitOfMeasure.Gallons, new BigDecimal("11.1011"), new BigDecimal("2.1221"), new BigDecimal("23.56"));

        FleetData fleetData = new FleetData();
        fleetData.setWorkOrderPoNumber("123456");//8

        Transaction response = card.authorize(new BigDecimal(23.56), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

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
    public void test_1220_authorization_capture_38_DE48_8_S() throws ApiException {

        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.Fuel);
        acceptorConfig.setSupportTerminalPurchaseRestriction(PurchaseRestrictionCapability.BOTHCHIPANDHOSTBASEDPRODUCTRESTRICTION);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("02", UnitOfMeasure.Gallons, new BigDecimal("11.1011"), new BigDecimal("2.1221"), new BigDecimal("23.56"));

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("00");//s

        Transaction response = card.authorize(new BigDecimal(23.56), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("400", UnitOfMeasure.Units, new BigDecimal(0001), new BigDecimal(21000), new BigDecimal(10));

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
    public void test_VisaFleet_Capture_1220_2() throws ApiException {

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("123450");
        fleetData.setDriverId("123450");

        ProductData productData = new ProductData(ServiceLevel.Other_NonFuel, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("10", UnitOfMeasure.Units, new BigDecimal(0001), new BigDecimal(10), new BigDecimal(10));
        productData.addNonFuel("084", UnitOfMeasure.Liters, new BigDecimal("10.720"), new BigDecimal(34664), new BigDecimal(50));
        productData.addNonFuel("080", UnitOfMeasure.Quarts, new BigDecimal(10), new BigDecimal(21), new BigDecimal(10));
        productData.addNonFuel("062", UnitOfMeasure.OtherOrUnknown, new BigDecimal(10), new BigDecimal(21), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("100", UnitOfMeasure.Units, new BigDecimal(0001), new BigDecimal(21000), new BigDecimal(10));

        Transaction captureResponse = response.capture(new BigDecimal(45))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(captureResponse);
        // check response
        assertEquals("400", captureResponse.getResponseCode());
    }

    //reversal
    @Test
    public void test_020_auth_reversal() throws ApiException {

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("02", UnitOfMeasure.Gallons, new BigDecimal("15.1234"), new BigDecimal("2.0110"));
        productData.addNonFuel("79", UnitOfMeasure.Quarts, new BigDecimal("10"), new BigDecimal("31"), new BigDecimal("20"));
        productData.addNonFuel("DM", UnitOfMeasure.Units, new BigDecimal("10"), new BigDecimal("21"), new BigDecimal("10"));

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        Transaction response = card.authorize(new BigDecimal("30.41"), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withReferenceNumber("0000")
                .withTagData(visaTagData)
                .execute();
        assertNotNull(reversal);

        // check message data
        pmi = reversal.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4351", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", reversal.getResponseCode());
    }

    @Test
    public void test_Auth_capture_1220_4() throws ApiException {

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("23235");
        fleetData.setDriverId("3887");
        fleetData.setJobNumber("50");
        fleetData.setServicePrompt("S");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel(ProductCode.Regular_Leaded, UnitOfMeasure.Units, new BigDecimal(0001), new BigDecimal(21000), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction captureResponse = response.capture(new BigDecimal(45))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
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

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("51", UnitOfMeasure.Kilograms, 10, 10);
        productData.addNonFuel("06", UnitOfMeasure.Gallons, 5, 10);
        productData.addNonFuel("16", UnitOfMeasure.Pounds, 2, 20);
        productData.addNonFuel("63", UnitOfMeasure.Liters, 5, 5);
        productData.addNonFuel("32", UnitOfMeasure.Gallons, 5, 2);


        Transaction preRresponse = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
                .withTagData(visaTagData)
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

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("51", UnitOfMeasure.Kilograms, new BigDecimal(10), new BigDecimal(10), new BigDecimal(100));
        productData.addNonFuel("06", UnitOfMeasure.Gallons, new BigDecimal(5), new BigDecimal(10), new BigDecimal(50));
        productData.addNonFuel("16", UnitOfMeasure.Pounds, new BigDecimal(2), new BigDecimal(20), new BigDecimal(40));
        productData.addNonFuel("63", UnitOfMeasure.Liters, new BigDecimal(5), new BigDecimal(5), new BigDecimal(25));
        productData.addNonFuel("32", UnitOfMeasure.Gallons, new BigDecimal(5), new BigDecimal(2), new BigDecimal(10));


        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction capture = response.capture(response.getAuthorizedAmount())
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }

    //Attempt to run an sale -ve scenerio
    @Test
    public void test_VisaFleet_emv_sale() throws ApiException {

        FleetData fleetData = new FleetData();
        fleetData.setVehicleNumber("123456");
        fleetData.setOdometerReading("123456");
        fleetData.setTrailerReferHours("123456");
        fleetData.setUnitNumber("123456");

        ProductData productData = new ProductData(ServiceLevel.Other_NonFuel, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("01", UnitOfMeasure.Gallons, new BigDecimal("10.720"), new BigDecimal("34664"), new BigDecimal("50"));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_visaFleet_auth_customerDataTag_DE48_8() throws ApiException {
        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.Fuel);
        acceptorConfig.setSupportTerminalPurchaseRestriction(PurchaseRestrictionCapability.HOSTBASEDPRODUCTRESTRICTION);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("03", UnitOfMeasure.Gallons, new BigDecimal("11.1010"), new BigDecimal("2.2341"), new BigDecimal("24.80"));

        FleetData fleetData = new FleetData();
        fleetData.setAdditionalPromptData1("001");//d
        fleetData.setAdditionalPromptData2("002");//e
        fleetData.setEmployeeNumber("EM002");//f

        Transaction response = card.authorize(new BigDecimal("24.80"), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_visaFleet_preAuth_Completion_customerDataTag_DE48_8() throws ApiException {

        FleetData fleetData = new FleetData();
        fleetData.setAdditionalPromptData1("001");//d
        fleetData.setAdditionalPromptData2("002");//e
        fleetData.setEmployeeNumber("EM002");//f

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("51", UnitOfMeasure.Kilograms, 10, 10);
        productData.addNonFuel("06", UnitOfMeasure.Gallons, 5, 10);
        productData.addNonFuel("16", UnitOfMeasure.Pounds, 2, 20);
        productData.addNonFuel("63", UnitOfMeasure.Liters, 5, 5);
        productData.addNonFuel("32", UnitOfMeasure.Gallons, 5, 2);


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
    public void test_24_authorization_productCodes() throws ApiException {

        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.Fuel);
        acceptorConfig.setSupportTerminalPurchaseRestriction(PurchaseRestrictionCapability.CHIPBASEDPRODUCTRESTRICTION);

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("32", UnitOfMeasure.Gallons, new BigDecimal("11.1011"), new BigDecimal("2.1221"));
        productData.addNonFuel("NZ", UnitOfMeasure.Gallons, 5, 10);
        productData.addNonFuel("ZC", UnitOfMeasure.Pounds, 2, 20);

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        Transaction response = card.authorize(new BigDecimal("23.56"), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test //Tested with different product Codes
    public void test_24_authorization_capture_productCodes() throws ApiException {

        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.Fuel);
        acceptorConfig.setSupportTerminalPurchaseRestriction(PurchaseRestrictionCapability.CHIPBASEDPRODUCTRESTRICTION);

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("44", UnitOfMeasure.Gallons, new BigDecimal("11.1011"), new BigDecimal("2.1221"));
        productData.addSaleTax(new BigDecimal("20"));
        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        Transaction response = card.authorize(new BigDecimal("23.56"), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction captureResponse = response.preAuthCompletion(new BigDecimal("23.56"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(captureResponse);
        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test //Combine product more than 8 NonFuel product
    public void test_1100_authorization_combine_NF() throws ApiException {

        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.NonFuel);

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addNonFuel("79", UnitOfMeasure.Quarts, new BigDecimal("10.0000"), new BigDecimal("100"), new BigDecimal("100"));
        productData.addNonFuel("DN", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("200"), new BigDecimal("200"));
        productData.addNonFuel(ProductCode.UNLEADED_PLUS_ETHANOL, UnitOfMeasure.Units, new BigDecimal("2.0000"), new BigDecimal("300"), new BigDecimal("300"));
        productData.addNonFuel(ProductCode.SUPER_UNLEADED_ETHANOL, UnitOfMeasure.Units, new BigDecimal("2.0000"), new BigDecimal("400"), new BigDecimal("400"));
        productData.addNonFuel(ProductCode.BIODIESEL, UnitOfMeasure.Units, new BigDecimal("3.0000"), new BigDecimal("500"), new BigDecimal("500"));
        productData.addNonFuel(ProductCode.Car_Wash, UnitOfMeasure.Units, new BigDecimal("3.0000"), new BigDecimal("500"), new BigDecimal("500"));
        productData.addNonFuel(ProductCode.Brake_Service, UnitOfMeasure.Units, new BigDecimal("4.0000"), new BigDecimal("200"), new BigDecimal("200"));
        productData.addNonFuel(ProductCode.Tires, UnitOfMeasure.Units, new BigDecimal("4.0000"), new BigDecimal("200"), new BigDecimal("200"));
        productData.addNonFuel(ProductCode.Filters, UnitOfMeasure.Units, new BigDecimal("5.5000"), new BigDecimal("20.50"), new BigDecimal("20.50"));


        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        Transaction response = card.authorize(new BigDecimal("31.75"), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test //Combine nonFuel product more than 8 NonFuel product
    public void test_1100_authorization_combine_F_NF() throws ApiException {

        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.FuelAndNonFuel);

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("01", UnitOfMeasure.Gallons, new BigDecimal("15.1134"), new BigDecimal("2.1010"));
        productData.addNonFuel("79", UnitOfMeasure.Quarts, new BigDecimal("10.0000"), new BigDecimal("100"), new BigDecimal("100"));
        productData.addNonFuel("DN", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("200"), new BigDecimal("200"));
        productData.addNonFuel(ProductCode.UNLEADED_PLUS_ETHANOL, UnitOfMeasure.Units, new BigDecimal("2.0000"), new BigDecimal("300"), new BigDecimal("300"));
        productData.addNonFuel(ProductCode.SUPER_UNLEADED_ETHANOL, UnitOfMeasure.Units, new BigDecimal("2.0000"), new BigDecimal("400"), new BigDecimal("400"));
        productData.addNonFuel(ProductCode.BIODIESEL, UnitOfMeasure.Units, new BigDecimal("3.0000"), new BigDecimal("500"), new BigDecimal("500"));
        productData.addNonFuel(ProductCode.Car_Wash, UnitOfMeasure.Units, new BigDecimal("3.0000"), new BigDecimal("500"), new BigDecimal("500"));
        productData.addNonFuel(ProductCode.Brake_Service, UnitOfMeasure.Units, new BigDecimal("4.0000"), new BigDecimal("200"), new BigDecimal("200"));
        productData.addNonFuel(ProductCode.Tires, UnitOfMeasure.Units, new BigDecimal("4.0000"), new BigDecimal("200"), new BigDecimal("200"));
        productData.addNonFuel(ProductCode.Filters, UnitOfMeasure.Units, new BigDecimal("5.5000"), new BigDecimal("20.50"), new BigDecimal("20.50"));


        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        Transaction response = card.authorize(new BigDecimal("31.75"), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test //-ve scenario
    public void test_1100_authorization_combine_NF1() throws ApiException {

        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.NonFuel);

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addNonFuel("79", UnitOfMeasure.Quarts, new BigDecimal("10.0000"), new BigDecimal("100"), new BigDecimal("100"));
        productData.addNonFuel("DN", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("200"), new BigDecimal("200"));
        productData.addNonFuel(ProductCode.UNLEADED_PLUS_ETHANOL, UnitOfMeasure.Units, new BigDecimal("2.0000"), new BigDecimal("300"), new BigDecimal("300"));
        productData.addNonFuel(ProductCode.SUPER_UNLEADED_ETHANOL, UnitOfMeasure.Units, new BigDecimal("2.0000"), new BigDecimal("400"), new BigDecimal("400"));
        productData.addNonFuel(ProductCode.BIODIESEL, UnitOfMeasure.Units, new BigDecimal("3.0000"), new BigDecimal("500"), new BigDecimal("500"));
        productData.addNonFuel(ProductCode.Car_Wash, UnitOfMeasure.Units, new BigDecimal("3.0000"), new BigDecimal("500"), new BigDecimal("500"));
        productData.addNonFuel(ProductCode.Brake_Service, UnitOfMeasure.Units, new BigDecimal("4.0000"), new BigDecimal("200"), new BigDecimal("200"));
        productData.addNonFuel(ProductCode.Tires, UnitOfMeasure.Units, new BigDecimal("4.0000"), new BigDecimal("200"), new BigDecimal("200"));


        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        Transaction response = card.authorize(new BigDecimal("31.75"), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test //-ve scenario
    public void test_1100_authorization_combine_NF2() throws ApiException {

        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.Fuel);

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("01", UnitOfMeasure.Gallons, new BigDecimal("15.1134"), new BigDecimal("2.1010"));

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        Transaction response = card.authorize(new BigDecimal("31.75"), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

}

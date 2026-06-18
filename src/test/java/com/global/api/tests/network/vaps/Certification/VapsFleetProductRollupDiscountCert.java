package com.global.api.tests.network.vaps.Certification;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.FleetData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.ProductData;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.TestCards;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VapsFleetProductRollupDiscountCert {
    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;
    private CreditTrackData track;
    private final String visaTagData = "4F07A0000000031010820239008407A00000000310107005123456789057124485580000080017D311220115886224023F5F201A546573742F4361726420313020202020202020202020202020205A0844855800000800175F24032212315F280208405F3401008C0F1234567890123451234567890123458D13123745524364726335524364726325374552438E0C00000000000000001F0000009F0702FF009F080200019F0D05F470C498009F0E0500000000009F0F05F470C498008F01019001119F4604123456789F4701239F5801129F5901019F6804123456789F6C0212349F6E04D8E0000082010112820201128203013482040112";

    public VapsFleetProductRollupDiscountCert() throws ApiException {
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
        config.setTerminalId("0006182025511");
//        config.setTerminalId("0000912197711");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());
        config.setMerchantType("5541");

        ServicesContainer.configureService(config);

        track = new CreditTrackData();
        track.setValue(";4485580000080017=311220115886224023?");
    }

    @Test
    public void test_VF2_TC02_visaFleetTwoPoint0_auth_moreThan8NonFuel_withFuel_withoutDiscount_rolledUp() throws ApiException {
        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.FuelAndNonFuel);

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("01", UnitOfMeasure.Gallons, new BigDecimal("10.0000"), new BigDecimal("3.0000"), new BigDecimal("30.0000"));

        // Add 10 non-fuel entries without discount
        productData.addNonFuel("60", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("25.00"), new BigDecimal("25.0000"));
        productData.addNonFuel("61", UnitOfMeasure.Units, new BigDecimal("2.0000"), new BigDecimal("10.00"), new BigDecimal("20.0000"));
        productData.addNonFuel("62", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("15.00"), new BigDecimal("15.0000"));
        productData.addNonFuel("63", UnitOfMeasure.Units, new BigDecimal("2.0000"), new BigDecimal("5.00"), new BigDecimal("10.0000"));
        productData.addNonFuel("64", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("5.00"), new BigDecimal("5.0000"));
        productData.addNonFuel("65", UnitOfMeasure.Units, new BigDecimal("2.0000"), new BigDecimal("2.00"), new BigDecimal("4.0000"));
        productData.addNonFuel("66", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("3.00"), new BigDecimal("3.0000"));
        productData.addNonFuel("67", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("2.00"), new BigDecimal("2.0000"));
        productData.addNonFuel("68", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("1.50"), new BigDecimal("1.5000"));
        productData.addNonFuel("69", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("1.00"), new BigDecimal("1.0000"));

        productData.addSaleTax(new BigDecimal("2.40"));

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        Transaction response = track.authorize(new BigDecimal("118.90"),true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_VF2_TC03_visaFleetTwoPoint0_auth_moreThan8NonFuel_withFuel_withDiscount_rolledUp() throws ApiException {
        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.FuelAndNonFuel);

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("01", UnitOfMeasure.Gallons, new BigDecimal("10.0000"), new BigDecimal("3.0000"), new BigDecimal("30.0000"));

        // Add 10 non-fuel entries including discount
        productData.addNonFuel("60", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("25.00"), new BigDecimal("25.0000"));
        productData.addNonFuel("61", UnitOfMeasure.Units, new BigDecimal("2.0000"), new BigDecimal("10.00"), new BigDecimal("20.0000"));
        productData.addNonFuel("62", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("15.00"), new BigDecimal("15.0000"));
        productData.addNonFuel("63", UnitOfMeasure.Units, new BigDecimal("2.0000"), new BigDecimal("5.00"), new BigDecimal("10.0000"));
        productData.addNonFuel("64", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("5.00"), new BigDecimal("5.0000"));
        productData.addNonFuel("65", UnitOfMeasure.Units, new BigDecimal("2.0000"), new BigDecimal("2.00"), new BigDecimal("4.0000"));
        productData.addNonFuel("66", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("3.00"), new BigDecimal("3.0000"));
        productData.addNonFuel("67", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("2.00"), new BigDecimal("2.0000"));
        productData.addNonFuel("68", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("1.50"), new BigDecimal("1.5000"));
        productData.addNonFuel("68", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("1.00"), new BigDecimal("1.0000"));

        // Discount should NOT be rolled up into ZC
        productData.addNonFuel(ProductCode.DISCOUNT_CODE1, UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("3.00"), new BigDecimal("3.0000"));

        productData.addSaleTax(new BigDecimal("2.40"));

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        Transaction response = track.authorize(new BigDecimal("115.90"),true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    public void test_VF2_TC04_visaFleetTwoPoint0_auth_moreThan8NonFuel_withFuel_withDiscount_coupon_rolledUp() throws ApiException {
        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.FuelAndNonFuel);

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("01", UnitOfMeasure.Gallons, new BigDecimal("10.0000"), new BigDecimal("3.0000"), new BigDecimal("30.0000"));

        // Add 10 non-fuel entries including discount
        productData.addNonFuel("60", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("25.00"), new BigDecimal("25.0000"));
        productData.addNonFuel("61", UnitOfMeasure.Units, new BigDecimal("2.0000"), new BigDecimal("10.00"), new BigDecimal("20.0000"));
        productData.addNonFuel("62", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("15.00"), new BigDecimal("15.0000"));
        productData.addNonFuel("63", UnitOfMeasure.Units, new BigDecimal("2.0000"), new BigDecimal("5.00"), new BigDecimal("10.0000"));
        productData.addNonFuel("64", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("5.00"), new BigDecimal("5.0000"));
        productData.addNonFuel("65", UnitOfMeasure.Units, new BigDecimal("2.0000"), new BigDecimal("2.00"), new BigDecimal("4.0000"));
        productData.addNonFuel("66", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("3.00"), new BigDecimal("3.0000"));
        productData.addNonFuel("67", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("2.00"), new BigDecimal("2.0000"));
        productData.addNonFuel("68", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("1.50"), new BigDecimal("1.5000"));

        // Discount should NOT be rolled up into ZC
        productData.addNonFuel(ProductCode.DISCOUNT_CODE1, UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("3.00"), new BigDecimal("3.0000"));
        productData.addNonFuel(ProductCode.DISCOUNT_CODE1, UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("1.00"), new BigDecimal("1.0000"));

        productData.addSaleTax(new BigDecimal("2.40"));

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        Transaction response = track.authorize(new BigDecimal("114.90"),true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    public void test_VF2_TC05_visaFleetTwoPoint0_auth_lessThan8NonFuel_withFuel_withDiscount_noRollup() throws ApiException {
        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.FuelAndNonFuel);

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("01", UnitOfMeasure.Gallons, new BigDecimal("10.0000"), new BigDecimal("3.0000"), new BigDecimal("30.0000"));

        // Add 3 non-fuel entries including discount
        productData.addNonFuel("60", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("20.00"), new BigDecimal("20.0000"));
        productData.addNonFuel("61", UnitOfMeasure.Units, new BigDecimal("2.0000"), new BigDecimal("5.00"), new BigDecimal("10.0000"));

        // Discount should NOT be rolled up into ZC
        productData.addNonFuel(ProductCode.DISCOUNT_CODE1, UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("5.00"), new BigDecimal("5.0000"));

        productData.addSaleTax(new BigDecimal("2.75"));

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        Transaction response = track.authorize(new BigDecimal("57.75"),true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_VYG_TC01_voyager_noRollup() throws ApiException {
        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.NonFuel);
        acceptorConfig.setVisaFleet2(false);
        config.setAcceptorConfig(acceptorConfig);
        CreditTrackData track = TestCards.VoyagerSwipe();
        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("02");
        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.GlobalPayments, ProductDataFormat.GlobalPaymentsStandardFormat);
        productData.addNonFuel("61", UnitOfMeasure.Units, new BigDecimal("2"), new BigDecimal("10"), new BigDecimal("20"));
        productData.addNonFuel("62", UnitOfMeasure.Units, new BigDecimal("2"), new BigDecimal("5"), new BigDecimal("10"));
        productData.addNonFuel("35", UnitOfMeasure.Units, new BigDecimal("1"), new BigDecimal("3"), new BigDecimal("3"));

        productData.addSaleTax(new BigDecimal("2.10"));

        Transaction response = track.authorize(new BigDecimal("29.10"),true)
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
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
    }

    @Test
    public void test_VYG_TC02_voyager_noRollup() throws ApiException {
        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.NonFuel);
        acceptorConfig.setVisaFleet2(false);
        config.setAcceptorConfig(acceptorConfig);
        CreditTrackData track = TestCards.VoyagerSwipe();
        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("02");
        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.GlobalPayments, ProductDataFormat.GlobalPaymentsStandardFormat);
        productData.addNonFuel("61", UnitOfMeasure.Units, new BigDecimal("2"), new BigDecimal("20"), new BigDecimal("40"));
        productData.addNonFuel("62", UnitOfMeasure.Units, new BigDecimal("1"), new BigDecimal("15"), new BigDecimal("15"));
        productData.addNonFuel("35", UnitOfMeasure.Units, new BigDecimal("1"), new BigDecimal("3"), new BigDecimal("5"));

        productData.addSaleTax(new BigDecimal("3.0"));

        Transaction response = track.authorize(new BigDecimal("53.00"),true)
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
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
    }

    @Test
    public void test_MCF_TC02_1Fuel_5NonFuel_products_withoutRollup() throws ApiException {
        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.FuelAndNonFuel);
        acceptorConfig.setVisaFleet2(false);
        config.setAcceptorConfig(acceptorConfig);
        track = TestCards.MasterCardFleetSwipe();

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("02");
        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.GlobalPayments, ProductDataFormat.GlobalPaymentsStandardFormat);
        productData.addFuel("04", UnitOfMeasure.Gallons, new BigDecimal("1"), new BigDecimal("30"), new BigDecimal("30"));
        productData.addNonFuel(ProductCode.Tires, UnitOfMeasure.OtherOrUnknown, new BigDecimal("1"), new BigDecimal("25"), new BigDecimal("25"));
        productData.addNonFuel(ProductCode.Oil_Change, UnitOfMeasure.OtherOrUnknown, new BigDecimal("2"), new BigDecimal("10"), new BigDecimal("20"));
        productData.addNonFuel(ProductCode.Batteries, UnitOfMeasure.OtherOrUnknown, new BigDecimal("1"), new BigDecimal("15"), new BigDecimal("15"));
        productData.addNonFuel(ProductCode.Wipers, UnitOfMeasure.OtherOrUnknown, new BigDecimal("2"), new BigDecimal("5"), new BigDecimal("10"));
        productData.addNonFuel(ProductCode.Brake_Service, UnitOfMeasure.OtherOrUnknown, new BigDecimal("1"), new BigDecimal("1"), new BigDecimal("5"));

        productData.addSaleTax(new BigDecimal("2.00"));

        Transaction response = track.authorize(new BigDecimal("107"),true)
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
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

    }
    @Test
    public void test_MCF_TC03_1Fuel_7NonFuel_products_withRollup() throws ApiException {
        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.FuelAndNonFuel);
        acceptorConfig.setVisaFleet2(false);
        config.setAcceptorConfig(acceptorConfig);
        track = TestCards.MasterCardFleetSwipe();

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("02");
        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.GlobalPayments, ProductDataFormat.GlobalPaymentsStandardFormat);
        productData.addFuel("04", UnitOfMeasure.Gallons, new BigDecimal("1"), new BigDecimal("30"), new BigDecimal("30"));
        productData.addNonFuel(ProductCode.Tires, UnitOfMeasure.OtherOrUnknown, new BigDecimal("1"), new BigDecimal("25"), new BigDecimal("25"));
        productData.addNonFuel(ProductCode.Oil_Change, UnitOfMeasure.OtherOrUnknown, new BigDecimal("2"), new BigDecimal("10"), new BigDecimal("20"));
        productData.addNonFuel(ProductCode.Batteries, UnitOfMeasure.OtherOrUnknown, new BigDecimal("1"), new BigDecimal("15"), new BigDecimal("15"));
        productData.addNonFuel(ProductCode.Wipers, UnitOfMeasure.OtherOrUnknown, new BigDecimal("2"), new BigDecimal("5"), new BigDecimal("10"));
        productData.addNonFuel(ProductCode.Brake_Service, UnitOfMeasure.OtherOrUnknown, new BigDecimal("1"), new BigDecimal("1"), new BigDecimal("5"));
        productData.addNonFuel(ProductCode.Car_Wash, UnitOfMeasure.OtherOrUnknown, new BigDecimal("1"), new BigDecimal("1"), new BigDecimal("3"));
        productData.addNonFuel(ProductCode.Filters, UnitOfMeasure.OtherOrUnknown, new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("2"));

        productData.addSaleTax(new BigDecimal("2.00"));

        Transaction response = track.authorize(new BigDecimal("112"),true)
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
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
    }

    @Test
    public void test_MCF_TC04_1Fuel_7NonFuel_products_withRollup() throws ApiException {
        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.FuelAndNonFuel);
        acceptorConfig.setVisaFleet2(false);
        config.setAcceptorConfig(acceptorConfig);
        track = TestCards.MasterCardFleetSwipe();

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("02");
        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.GlobalPayments, ProductDataFormat.GlobalPaymentsStandardFormat);
        productData.addFuel("04", UnitOfMeasure.Gallons, new BigDecimal("1"), new BigDecimal("30"), new BigDecimal("30"));
        productData.addNonFuel(ProductCode.Tires, UnitOfMeasure.OtherOrUnknown, new BigDecimal("1"), new BigDecimal("25"), new BigDecimal("25"));
        productData.addNonFuel(ProductCode.Oil_Change, UnitOfMeasure.OtherOrUnknown, new BigDecimal("2"), new BigDecimal("10"), new BigDecimal("20"));
        productData.addNonFuel(ProductCode.Batteries, UnitOfMeasure.OtherOrUnknown, new BigDecimal("1"), new BigDecimal("15"), new BigDecimal("15"));
        productData.addNonFuel(ProductCode.Wipers, UnitOfMeasure.OtherOrUnknown, new BigDecimal("2"), new BigDecimal("5"), new BigDecimal("10"));
        productData.addNonFuel(ProductCode.Brake_Service, UnitOfMeasure.OtherOrUnknown, new BigDecimal("1"), new BigDecimal("1"), new BigDecimal("5"));
        productData.addNonFuel(ProductCode.Car_Wash, UnitOfMeasure.OtherOrUnknown, new BigDecimal("1"), new BigDecimal("1"), new BigDecimal("3"));
        productData.addNonFuel(ProductCode.Filters, UnitOfMeasure.OtherOrUnknown, new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("2"));

        productData.addSaleTax(new BigDecimal("2.00"));

        Transaction response = track.authorize(new BigDecimal("112"),true)
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
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
    }

    @Test
    public void test_COM_TC01_VF2_withDiscount_rolledUp() throws ApiException {
        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.FuelAndNonFuel);

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.VISAFLEET2Dot0);
        productData.addFuel("01", UnitOfMeasure.Gallons, new BigDecimal("10.0000"), new BigDecimal("3.0000"), new BigDecimal("30.0000"));

        // Add 10 non-fuel entries including discount
        productData.addNonFuel("60", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("25.00"), new BigDecimal("25.0000"));
        productData.addNonFuel("61", UnitOfMeasure.Units, new BigDecimal("2.0000"), new BigDecimal("10.00"), new BigDecimal("20.0000"));
        productData.addNonFuel("62", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("15.00"), new BigDecimal("15.0000"));
        productData.addNonFuel("63", UnitOfMeasure.Units, new BigDecimal("2.0000"), new BigDecimal("5.00"), new BigDecimal("10.0000"));
        productData.addNonFuel("64", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("5.00"), new BigDecimal("5.0000"));
        productData.addNonFuel("65", UnitOfMeasure.Units, new BigDecimal("2.0000"), new BigDecimal("2.00"), new BigDecimal("4.0000"));
        productData.addNonFuel("66", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("3.00"), new BigDecimal("3.0000"));
        productData.addNonFuel("67", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("2.00"), new BigDecimal("2.0000"));
        productData.addNonFuel("68", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("1.50"), new BigDecimal("1.5000"));
        productData.addNonFuel("68", UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("1.00"), new BigDecimal("1.0000"));

        // Discount should NOT be rolled up into ZC
        productData.addNonFuel(ProductCode.DISCOUNT_CODE2, UnitOfMeasure.Units, new BigDecimal("1.0000"), new BigDecimal("3.00"), new BigDecimal("3.0000"));

        productData.addSaleTax(new BigDecimal("2.40"));

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("221123");

        Transaction response = track.authorize(new BigDecimal("115.90"),true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData(visaTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    public void test_COM_TC01_VYG_withDiscount_Rollup() throws ApiException {
        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.NonFuel);
        acceptorConfig.setVisaFleet2(false);
        config.setAcceptorConfig(acceptorConfig);
        CreditTrackData track = TestCards.VoyagerSwipe();
        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("02");
        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.GlobalPayments, ProductDataFormat.GlobalPaymentsStandardFormat);
        productData.addFuel("04", UnitOfMeasure.Gallons, new BigDecimal("1"), new BigDecimal("30"), new BigDecimal("30"));
        productData.addNonFuel("61", UnitOfMeasure.Units, new BigDecimal("1"), new BigDecimal("25"), new BigDecimal("25"));
        productData.addNonFuel("62", UnitOfMeasure.Units, new BigDecimal("2"), new BigDecimal("10"), new BigDecimal("20"));
        productData.addNonFuel("63", UnitOfMeasure.Units, new BigDecimal("3"), new BigDecimal("5"), new BigDecimal("15"));
        productData.addNonFuel("64", UnitOfMeasure.Units, new BigDecimal("2"), new BigDecimal("5"), new BigDecimal("10"));
        productData.addNonFuel("65", UnitOfMeasure.Units, new BigDecimal("1"), new BigDecimal("5"), new BigDecimal("5"));
        productData.addNonFuel("35", UnitOfMeasure.Units, new BigDecimal("1"), new BigDecimal("3"), new BigDecimal("3"));

        Transaction response = track.authorize(new BigDecimal("102.00"),true)
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
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
    }
    @Test
    public void test_FC_TC01_() throws ApiException {
        acceptorConfig.setVisaFleet2(false);
        config.setAcceptorConfig(acceptorConfig);
        config.setCompanyId("0009");
        config.setTerminalId("0001126198301");
        ServicesContainer.configureService(config);

        CreditTrackData track = TestCards.FleetWide();
        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("11411");
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("001", UnitOfMeasure.Gallons, new BigDecimal("10.000"), new BigDecimal("1.000"), new BigDecimal("10.000"));
        productData.addNonFuel("062", UnitOfMeasure.Units, new BigDecimal("3.000"), new BigDecimal("3.000"), new BigDecimal("9.000"));
        productData.addNonFuel("063", UnitOfMeasure.Quarts, new BigDecimal("2.000"), new BigDecimal("4.000"), new BigDecimal("8.000"));
        productData.addNonFuel("064", UnitOfMeasure.Units, new BigDecimal("1.000"), new BigDecimal("7.000"), new BigDecimal("7.000"));
        productData.addNonFuel("065", UnitOfMeasure.Units, new BigDecimal("2.000"), new BigDecimal("3.000"), new BigDecimal("6.000"));
        productData.addNonFuel(FleetCorConexxusProductCode.DISCOUNT_1.getValue(), UnitOfMeasure.OtherOrUnknown, new BigDecimal("1.000"), new BigDecimal("2.000"), new BigDecimal("2.000"));

        Transaction response = track.charge(new BigDecimal("38.00"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_COM_TC01_FC() throws ApiException {
        acceptorConfig.setVisaFleet2(false);
        config.setAcceptorConfig(acceptorConfig);
        config.setCompanyId("0009");
        config.setTerminalId("0001126198301");
        ServicesContainer.configureService(config);

        CreditTrackData track = TestCards.FleetWide();
        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("11411");
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("001", UnitOfMeasure.Gallons, new BigDecimal("10.000"), new BigDecimal("1.000"), new BigDecimal("10.000"));
        productData.addNonFuel("062", UnitOfMeasure.Units, new BigDecimal("3.000"), new BigDecimal("3.000"), new BigDecimal("9.000"));
        productData.addNonFuel("063", UnitOfMeasure.Quarts, new BigDecimal("2.000"), new BigDecimal("4.000"), new BigDecimal("8.000"));
        productData.addNonFuel("064", UnitOfMeasure.Units, new BigDecimal("1.000"), new BigDecimal("7.000"), new BigDecimal("7.000"));
        productData.addNonFuel("065", UnitOfMeasure.Units, new BigDecimal("2.000"), new BigDecimal("3.000"), new BigDecimal("6.000"));
        productData.addNonFuel(FleetCorConexxusProductCode.DISCOUNT_1.getValue(), UnitOfMeasure.OtherOrUnknown, new BigDecimal("1.000"), new BigDecimal("2.000"), new BigDecimal("2.000"));

        Transaction response = track.charge(new BigDecimal("38.00"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());
        assertEquals("000", response.getResponseCode());
    }

}

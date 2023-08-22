package com.global.api.tests.certifications;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.TaxType;
import com.global.api.entities.enums.TransactionModifier;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.GiftCard;
import com.global.api.serviceConfigs.PorticoConfig;
import com.global.api.services.BatchService;
import com.global.api.tests.testdata.TestCards;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EcomCertification {
    private boolean useTokens = false;

    private String visa_token;
    private String mastercard_token;
    private String discover_token;
    private String amex_token;
    private CommercialData commercialData;
    private String clientTxnID;

    public EcomCertification() throws ApiException {
        PorticoConfig config = new PorticoConfig();
        config.setSecretApiKey("skapi_cert_MTyMAQBiHVEAewvIzXVFcmUd2UcyBge_eCpaASUp0A");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");

        ServicesContainer.configureService(config);

        commercialData = new CommercialData(TaxType.SalesTax,TransactionModifier.Level_III) ;
        commercialData.setPoNumber("9876543210");
        commercialData.setTaxAmount(new BigDecimal(10));
        commercialData.setDestinationPostalCode("85212");
        commercialData.setDestinationCountryCode("USA");
        commercialData.setOriginPostalCode("22193");
        commercialData.setSummaryCommodityCode("SSC");
        commercialData.setCustomerReferenceId("UVATREF162");
        commercialData.setOrderDate(DateTime.now());
        commercialData.setFreightAmount(new BigDecimal(10));
        commercialData.setDutyAmount(new BigDecimal(10));

        AdditionalTaxDetails ad = new AdditionalTaxDetails();
        ad.setTaxAmount(new BigDecimal(10));
        ad.setTaxRate(new BigDecimal(10));

        commercialData.setAdditionalTaxDetails(ad);
        CommercialLineItem commercialLineItem = new CommercialLineItem();

        commercialLineItem.setDescription("PRODUCT 1 NOTES");
        commercialLineItem.setProductCode("PRDCD1");
        commercialLineItem.setUnitCost(new BigDecimal(0.01));
        commercialLineItem.setQuantity(new BigDecimal(1));
        commercialLineItem.setUnitOfMeasure("METER");
        commercialLineItem.setTotalAmount(new BigDecimal(10));

        DiscountDetails discountDetails = new DiscountDetails();
        discountDetails.setDiscountAmount(new BigDecimal(1));
        commercialLineItem.setDiscountDetails(discountDetails);

        commercialData.AddLineItems(commercialLineItem);
    }

    @Test
    public void ecomm_000_CloseBatch() throws ApiException {
        try {
            BatchSummary response = BatchService.closeBatch();
            assertNotNull(response);
            System.out.println(String.format("Batch ID: %s", response.getBatchId()));
            System.out.println(String.format("Sequence Number: %s", response.getSequenceNumber()));
        }
        catch (GatewayException exc) {
            if (!exc.getResponseText().contains("Transaction was rejected because it requires a batch to be open."))
                Assert.fail(exc.getMessage());
        }
    }

    @Test
    public void ecomm_001_verify_visa() throws ApiException {
        CreditCardData card = TestCards.VisaManual();

        Transaction response = card.verify()
                .withRequestMultiUseToken(useTokens)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ecomm_002_verify_master_card() throws ApiException {
        CreditCardData card = TestCards.MasterCardManual();

        Transaction response = card.verify()
                .withRequestMultiUseToken(useTokens)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ecomm_003_verify_discover() throws ApiException {
        CreditCardData card = TestCards.DiscoverManual();

        Transaction response = card.verify()
            .withAddress(new Address("75024"))
            .withRequestMultiUseToken(useTokens)
            .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // StreetAddress Verification

    @Test
    public void ecomm_004_verify_amex() throws ApiException {
        CreditCardData card = TestCards.AmexManual();

        Transaction response = card.verify()
            .withAddress(new Address("75024"))
            //.withRequestMultiUseToken(useTokens)
            .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // Balance Inquiry (for Prepaid Card)

    @Test @Ignore
    public void ecomm_005_balance_inquiry_visa() throws ApiException {
        CreditCardData card = TestCards.VisaManual();
        card.setCvn(null);

        Transaction response = card.balanceInquiry().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // CREDIT SALE (For Multi-Use Token Only)

    @Test
    public void ecomm_006_charge_visa_token() throws ApiException {
        Address address = new Address("6860 Dallas Pkwy", "75024");

        CreditCardData card = TestCards.VisaManual();

        Transaction response = card.charge(new BigDecimal("13.01"))
                .withCurrency("USD")
                .withAddress(address)
                .withRequestMultiUseToken(true)
                .withAllowDuplicates(true)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        visa_token = response.getToken();
    }

    @Test
    public void ecomm_007_charge_master_card_token() throws ApiException {
        Address address = new Address("6860", "75024");

        CreditCardData card = TestCards.MasterCardManual();

        Transaction response = card.charge(new BigDecimal("13.02"))
                .withCurrency("USD")
                .withAddress(address)
                .withRequestMultiUseToken(true)
                .withAllowDuplicates(true)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        mastercard_token = response.getToken();
    }

    @Test
    public void ecomm_008_charge_discover_token() throws ApiException {
        Address address = new Address("6860", "750241234");
        CreditCardData card = TestCards.DiscoverManual();

        Transaction response = card.charge(new BigDecimal("13.03"))
                .withCurrency("USD")
                .withAddress(address)
                .withRequestMultiUseToken(true)
                .withAllowDuplicates(true)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        discover_token = response.getToken();
    }

    @Test
    public void ecomm_009_charge_amex_token() throws ApiException {
        Address address = new Address("6860 Dallas Pkwy", "75024");

        CreditCardData card = TestCards.AmexManual();

        Transaction response = card.charge(new BigDecimal("13.04"))
                .withCurrency("USD")
                .withAddress(address)
                .withRequestMultiUseToken(true)
                .withAllowDuplicates(true)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        amex_token = response.getToken();
    }

    // CREDIT SALE

    @Test
    public void ecomm_010_charge_visa() throws ApiException {
        Address address = new Address("6860 Dallas Pkwy", "75024");
        CreditCardData card = useTokens ? new CreditCardData(visa_token) : TestCards.VisaManual();
        Transaction response = card.charge(new BigDecimal("17.01"))
                .withCurrency("USD")
                .withAddress(address)
                .withInvoiceNumber("123456")
                .withAllowDuplicates(true)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // test case 35
        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test
    public void ecomm_011_charge_master_card() throws ApiException {
        Address address = new Address("6860", "75024");
        CreditCardData card = useTokens ? new CreditCardData(mastercard_token) : TestCards.MasterCardManual();

        Transaction chargeResponse = card.charge(new BigDecimal("17.02"))
                .withCurrency("USD")
                .withAddress(address)
                .withInvoiceNumber("123456")
                .withAllowDuplicates(true)
                .execute();

        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
    }

    @Test
    public void ecomm_012_charge_discover() throws ApiException {
        Address address = new Address("6860", "750241234");
        CreditCardData card = useTokens ? new CreditCardData(discover_token) : TestCards.DiscoverManual();

        Transaction chargeResponse = card.charge(new BigDecimal("17.03"))
                .withCurrency("USD")
                .withAddress(address)
                .withInvoiceNumber("123456")
                .withAllowDuplicates(true)
                .execute();

        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
    }

    @Test
    public void ecomm_013_charge_amex() throws ApiException {
        Address address = new Address("6860 Dallas Pkwy", "75024");
        CreditCardData card = useTokens ? new CreditCardData(amex_token) : TestCards.AmexManual();

        Transaction chargeResponse = card.charge(new BigDecimal("17.04"))
                .withCurrency("USD")
                .withAddress(address)
                .withInvoiceNumber("123456")
                .withAllowDuplicates(true)
                .execute();

        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
    }

    @Test
    public void ecomm_014_charge_jcb() throws ApiException {
        Address address = new Address("6860 Dallas Pkwy", "750241234");

        CreditCardData card = TestCards.JcbManual();

        Transaction chargeResponse = card.charge(new BigDecimal("17.05"))
                .withCurrency("USD")
                .withAddress(address)
                .withInvoiceNumber("123456")
                .withAllowDuplicates(true)
                .execute();

        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
    }

    // AUTHORIZATION

    @Test
    public void ecomm_015_authorization_visa() throws ApiException {
        Address address = new Address("6860 Dallas Pkwy", "75024");

        CreditCardData card = TestCards.VisaManual();

        Transaction authResponse = card.authorize(new BigDecimal("17.06"))
                .withCurrency("USD")
                .withAddress(address)
                .withInvoiceNumber("123456")
                .withAllowDuplicates(true)
                .execute();

        assertNotNull(authResponse);
        assertEquals("00", authResponse.getResponseCode());

        // test 015b Capture/AddToBatch
        Transaction captureResponse = authResponse.capture().execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode());
    }

    @Test
    public void ecomm_016_authorization_master_card() throws ApiException {
        Address address = new Address("6860 Dallas Pkwy", "750241234");

        CreditCardData card = TestCards.MasterCardManual();

        Transaction authResponse = card.authorize(new BigDecimal("17.07"))
                .withCurrency("USD")
                .withAddress(address)
                .withInvoiceNumber("123456")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(authResponse);
        assertEquals("00", authResponse.getResponseCode());

        // test 016b Capture/AddToBatch
        Transaction captureResponse = authResponse.capture().execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode());
    }

    @Test
    public void ecomm_017_authorization_discover() throws ApiException {
        Address address = new Address("6860", "75024");

        CreditCardData card = TestCards.DiscoverManual();
        
        Transaction authResponse = card.authorize(new BigDecimal("17.07"))
                .withCurrency("USD")
                .withAddress(address)
                .withInvoiceNumber("123456")
                .execute();
        assertNotNull(authResponse);
        assertEquals("00", authResponse.getResponseCode());
    }

    // PARTIALLY - APPROVED SALE

    @Test
    public void ecomm_018_partial_approval_visa() throws ApiException {
        Address address = new Address("6860", "75024");

        CreditCardData card = TestCards.VisaManual();
        Transaction response = card.charge(new BigDecimal("130"))
                .withCurrency("USD")
                .withAddress(address)
                .withInvoiceNumber("123456")
                .withAllowPartialAuth(true)
                .execute();

        assertNotNull(response);
        assertEquals("10", response.getResponseCode());
        assertEquals(new BigDecimal("110.00"), response.getAuthorizedAmount());
    }

    @Test
    public void ecomm_019_partial_approval_discover() throws ApiException {
        Address address = new Address("6860", "75024");

        CreditCardData card = TestCards.DiscoverManual();

        Transaction response = card.charge(new BigDecimal("145"))
                .withCurrency("USD")
                .withAddress(address)
                .withInvoiceNumber("123456")
                .withAllowPartialAuth(true)
                .execute();

        assertNotNull(response);
        assertEquals("10", response.getResponseCode());
        assertEquals(new BigDecimal("65.00"), response.getAuthorizedAmount());
    }

    @Test
    public void ecomm_020_partial_approval_master_card() throws ApiException {
        Address address = new Address("6860", "75024");

        CreditCardData card = TestCards.MasterCardManual();
        Transaction chargeResponse = card.charge(new BigDecimal("155"))
                .withCurrency("USD")
                .withAddress(address)
                .withInvoiceNumber("123456")
                .withAllowPartialAuth(true)
                .execute();

        assertNotNull(chargeResponse);
        assertEquals("10", chargeResponse.getResponseCode());
        assertEquals(new BigDecimal("100.00"), chargeResponse.getAuthorizedAmount());

        // test case 36
        Transaction voidResponse = chargeResponse.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals("00", voidResponse.getResponseCode());
    }

    // LEVEL II CORPORATE PURCHASE CARD

    @Test
    public void ecomm_021_level_ii_response_b() throws ApiException {
        Address address = new Address("6860 Dallas Pkwy", "750241234");

        CreditCardData card = TestCards.VisaManual();
        Transaction chargeResponse = card.charge(new BigDecimal("112.34"))
                .withCurrency("USD")
                .withAddress(address)
                .withAllowDuplicates(true)
                .withCommercialRequest(true)
                .execute();
        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
        assertEquals("B", chargeResponse.getCommercialIndicator());

        Transaction cpcResponse = chargeResponse.edit()
                .withCommercialData(commercialData)
                .execute();

        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    public void ecomm_022_level_ii_response_b() throws ApiException {
        Address address = new Address("6860", "750241234");

        CreditCardData card = TestCards.VisaManual();
        Transaction chargeResponse = card.charge(new BigDecimal("112.34"))
                .withCurrency("USD")
                .withAddress(address)
                .withAllowDuplicates(true)
                .withCommercialRequest(true)
                .execute();
        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
        assertEquals("B", chargeResponse.getCommercialIndicator());

        Transaction cpcResponse = chargeResponse.edit()
                .withCommercialData(commercialData)
                .execute();
        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    public void ecomm_023_level_ii_response_r() throws ApiException {
        Address address = new Address("6860", "75024");

        CreditCardData card = TestCards.VisaManual();
        Transaction chargeResponse = card.charge(new BigDecimal("123.45"))
                .withCurrency("USD")
                .withAddress(address)
                .withCommercialRequest(true)
                .execute();

        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
        assertEquals("R", chargeResponse.getCommercialIndicator());

        Transaction cpcResponse = chargeResponse.edit()
                .withCommercialData(commercialData)
                .execute();

        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    public void ecomm_024_level_ii_response_s() throws ApiException {
        Address address = new Address("6860", "75024");

        CreditCardData card = TestCards.VisaManual();
        Transaction chargeResponse = card.charge(new BigDecimal("134.56"))
                .withCurrency("USD")
                .withAddress(address)
                .withCommercialRequest(true)
                .execute();

        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
        assertEquals("S", chargeResponse.getCommercialIndicator());

        //var cpcData = new HpsCpcData { CardHolderPoNumber = "9876543210", TaxType = taxTypeType.SalesTax, TaxAmount = 1.00m };

        Transaction cpcResponse = chargeResponse.edit()
                .withCommercialData(commercialData)
                .execute();

        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    public void ecomm_025_level_ii_response_s() throws ApiException {
        Address address = new Address("6860", "75024");

        CreditCardData card = TestCards.MasterCardManual();
        Transaction chargeResponse = card.charge(new BigDecimal("111.06"))
                .withCurrency("USD")
                .withAddress(address)
                .withCommercialRequest(true)
                .execute();

        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
        assertEquals("S", chargeResponse.getCommercialIndicator());

        Transaction cpcResponse = chargeResponse.edit()
                .withCommercialData(commercialData)
                .execute();
        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    public void ecomm_026_level_ii_response_s() throws ApiException {
        Address address = new Address("6860", "75024");

        CreditCardData card = TestCards.MasterCardManual();
        Transaction chargeResponse = card.charge(new BigDecimal("111.07"))
                .withCurrency("USD")
                .withAddress(address)
                .withCommercialRequest(true)
                .execute();

        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
        assertEquals("S", chargeResponse.getCommercialIndicator());

        Transaction cpcResponse = chargeResponse.edit()
                .withCommercialData(commercialData)
                .execute();

        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    public void ecomm_027_level_ii_response_s() throws ApiException {
        Address address = new Address("6860", "75024");

        CreditCardData card = TestCards.MasterCardManual();
        Transaction chargeResponse = card.charge(new BigDecimal("111.08"))
                .withCurrency("USD")
                .withAddress(address)
                .withCommercialRequest(true)
                .execute();

        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
        assertEquals("S", chargeResponse.getCommercialIndicator());

        //var cpcData = new HpsCpcData { CardHolderPoNumber = "9876543210", TaxAmount = 1.00m, TaxType = taxTypeType.SalesTax };

        Transaction cpcResponse = chargeResponse.edit()
//                PONumber, TaxType, TaxAmount
                .withCommercialData(commercialData)
                .execute();

        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    public void ecomm_028_level_ii_response_s() throws ApiException {
        Address address = new Address("6860", "75024");

        CreditCardData card = TestCards.MasterCardManual();
        Transaction chargeResponse = card.charge(new BigDecimal("111.09"))
                .withCurrency("USD")
                .withAddress(address)
                .withCommercialRequest(true)
                .execute();

        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
        assertEquals("S", chargeResponse.getCommercialIndicator());

        Transaction cpcResponse = chargeResponse.edit()
                .withCommercialData(commercialData)
                .execute();
        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    public void ecomm_029_level_ii_no_response() throws ApiException {
        Address address = new Address("6860", "75024");

        CreditCardData card = TestCards.AmexManual();
        Transaction chargeResponse = card.charge(new BigDecimal("111.10"))
                .withCurrency("USD")
                .withAddress(address)
                .withCommercialRequest(true)
                .execute();

        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
        assertEquals("0", chargeResponse.getCommercialIndicator());

        Transaction cpcResponse = chargeResponse.edit()
//               PO Number, TaxType
                .withCommercialData(commercialData)
                .execute();
        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    public void ecomm_030_level_ii_no_response() throws ApiException {
        Address address = new Address("6860", "750241234");

        CreditCardData card = TestCards.AmexManual();
        Transaction chargeResponse = card.charge(new BigDecimal("111.11"))
                .withCurrency("USD")
                .withAddress(address)
                .withCommercialRequest(true)
                .execute();
        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
        assertEquals("0", chargeResponse.getCommercialIndicator());

        Transaction cpcResponse = chargeResponse.edit()
                //TaxType, TaxAmount
                .withCommercialData(commercialData)
                .execute();

        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    public void ecomm_031_level_ii_no_response() throws ApiException {
        Address address = new Address("6860", "750241234");

        CreditCardData card = TestCards.AmexManual();
        Transaction chargeResponse = card.charge(new BigDecimal("111.12"))
                .withCurrency("USD")
                .withAddress(address)
                .withCommercialRequest(true)
                .execute();

        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
        assertEquals("0", chargeResponse.getCommercialIndicator());

        Transaction cpcResponse = chargeResponse.edit()
                .withCommercialData(commercialData)
                .execute();

        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    public void ecomm_032_level_ii_no_response() throws ApiException {
        Address address = new Address("6860", "75024");

        CreditCardData card = TestCards.AmexManual();
        Transaction chargeResponse = card.charge(new BigDecimal("111.13"))
                .withCurrency("USD")
                .withAddress(address)
                .withCommercialRequest(true)
                .execute();

        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
        assertEquals("0", chargeResponse.getCommercialIndicator());

        Transaction cpcResponse = chargeResponse.edit()
                .withCommercialData(commercialData)
                .execute();

        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    //level 3
    @Test
    public void ecomm_025_level_iii_response_s_MC() throws ApiException {
        Address address = new Address("6860", "75024");

        int randomID = new Random().nextInt(999999 - 10000)+10000;
        clientTxnID = Integer.toString(randomID);

        CreditCardData card = TestCards.MasterCardManual();
        Transaction chargeResponse = card.charge(new BigDecimal("111.06"))
                .withCurrency("USD")
                .withAddress(address)
                .withCommercialRequest(true)
                .withClientTransactionId(clientTxnID)
                .execute();

        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
        assertEquals("S", chargeResponse.getCommercialIndicator());

        Transaction cpcResponse = chargeResponse.edit()
                .withCommercialData(commercialData)
                .execute();
        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
        assertEquals(clientTxnID, cpcResponse.getClientTransactionId());
    }

    @Test
    public void ecomm_025_level_iii_response_s_Visa() throws ApiException {
        Address address = new Address("6860", "75024");

        commercialData = new CommercialData(TaxType.SalesTax,TransactionModifier.Level_III) ;
        commercialData.setPoNumber("9876543210");
        commercialData.setTaxAmount(new BigDecimal(10));
        commercialData.setDestinationPostalCode("85212");
        commercialData.setDestinationCountryCode("USA");
        commercialData.setOriginPostalCode("22193");
        commercialData.setSummaryCommodityCode("SSC");
        commercialData.setCustomerReferenceId("UVATREF162");
        commercialData.setOrderDate(DateTime.now());
        commercialData.setFreightAmount(new BigDecimal(10));
        commercialData.setDutyAmount(new BigDecimal(10));

        AdditionalTaxDetails ad = new AdditionalTaxDetails();
        ad.setTaxAmount(new BigDecimal(10));
        ad.setTaxRate(new BigDecimal(10));

        commercialData.setAdditionalTaxDetails(ad);
        CommercialLineItem commercialLineItem = new CommercialLineItem();

        commercialLineItem.setDescription("PRODUCT 1 NOTES");
        commercialLineItem.setProductCode("PRDCD1");
        commercialLineItem.setQuantity(new BigDecimal(1));

        commercialData.AddLineItems(commercialLineItem);

        CreditCardData card = TestCards.VisaManual();
        Transaction chargeResponse = card.charge(new BigDecimal("134.56"))
                .withCurrency("USD")
                .withAddress(address)
                .withCommercialRequest(true)
                .execute();

        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
        assertEquals("S", chargeResponse.getCommercialIndicator());

        Transaction cpcResponse = chargeResponse.edit()
                .withCommercialData(commercialData)
                .execute();

        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }


    // PRIOR / VOICE AUTHORIZATION

    @Test
    public void ecomm_033_offline_sale() throws ApiException {
        CreditCardData card = TestCards.VisaManual();
        Transaction response = card.charge(new BigDecimal("17.10"))
                .withCurrency("USD")
                .withOfflineAuthCode("654321")
                .withInvoiceNumber("123456")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ecomm_033_offline_authorization() throws ApiException {
        CreditCardData card = TestCards.VisaManual();
        Transaction response = card.authorize(new BigDecimal("17.10"))
                .withCurrency("USD")
                .withOfflineAuthCode("654321")
                .withInvoiceNumber("123456")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // RETURN

    @Test
    public void ecomm_034_offline_credit_return() throws ApiException {
        CreditCardData card = TestCards.MasterCardManual();
        Transaction response = card.refund(new BigDecimal("15.15"))
                .withCurrency("USD")
                .withInvoiceNumber("123456")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // ONLINE VOID / REVERSAL

    // HMS GIFT - REWARDS

    // ACTIVATE

    @Test
    public void ecomm_042_activate_gift_1() throws ApiException {
        GiftCard giftCard1 = new GiftCard();
		giftCard1.setNumber("5022440000000000098");
        Transaction response = giftCard1.activate(new BigDecimal("6.00")).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ecomm_043_activate_gift_2() throws ApiException {
        GiftCard giftCard2 = new GiftCard();
		giftCard2.setNumber("5022440000000000007");
        Transaction response = giftCard2.activate(new BigDecimal("7.00")).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // LOAD / ADD VALUE

    @Test
    public void ecomm_044_add_value_gift_1() throws ApiException {
        GiftCard giftCard1 = new GiftCard();
		giftCard1.setNumber("5022440000000000098");
        Transaction response = giftCard1.activate(new BigDecimal("8.00")).execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ecomm_045_add_value_gift_2() throws ApiException {
        GiftCard giftCard2 = new GiftCard();
		giftCard2.setNumber("5022440000000000007");
        Transaction response = giftCard2.activate(new BigDecimal("8.00")).execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // BALANCE INQUIRY

    @Test
    public void ecomm_046_balance_inquiry_gift_1() throws ApiException {
        GiftCard giftCard1 = new GiftCard();
		giftCard1.setNumber("5022440000000000098");
        Transaction response = giftCard1.balanceInquiry().execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals(new BigDecimal("10.00"), response.getBalanceAmount());
    }

    @Test
    public void ecomm_047_balance_inquiry_gift_2() throws ApiException {
        GiftCard giftCard2 = new GiftCard();
		giftCard2.setNumber("5022440000000000007");
        Transaction response = giftCard2.balanceInquiry().execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals(new BigDecimal("10.00"), response.getBalanceAmount());
    }

    // REPLACE / TRANSFER

    @Test
    public void ecomm_048_replace_gift_1() throws ApiException {
        GiftCard giftCard1 = new GiftCard();
		giftCard1.setNumber("5022440000000000098");
        GiftCard giftCard2 = new GiftCard();
		giftCard2.setNumber("5022440000000000007");
        Transaction response = giftCard1.replaceWith(giftCard2).execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals(new BigDecimal("10.00"), response.getBalanceAmount());
    }

    @Test
    public void ecomm_049_replace_gift_2() throws ApiException {
        GiftCard giftCard1 = new GiftCard();
		giftCard1.setNumber("5022440000000000098");
        GiftCard giftCard2 = new GiftCard();
		giftCard2.setNumber("5022440000000000007");
        Transaction response = giftCard2.replaceWith(giftCard1).execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals(new BigDecimal("10.00"), response.getBalanceAmount());
    }

    // SALE / REDEEM

    @Test
    public void ecomm_050_sale_gift_1() throws ApiException {
        GiftCard giftCard1 = new GiftCard();
		giftCard1.setNumber("5022440000000000098");
        Transaction response = giftCard1.charge(new BigDecimal("1.0"))
                .withCurrency("USD")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ecomm_051_sale_gift_2() throws ApiException {
        GiftCard giftCard2 = new GiftCard();
		giftCard2.setNumber("5022440000000000007");
        Transaction response = giftCard2.charge(new BigDecimal("2.0"))
                .withCurrency("USD")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ecomm_052_sale_gift_1_void() throws ApiException {
        GiftCard giftCard1 = new GiftCard();
		giftCard1.setNumber("5022440000000000098");

        Transaction saleResponse = giftCard1.charge(new BigDecimal("3.0"))
                .withCurrency("USD")
                .execute();
        assertNotNull(saleResponse);
        assertEquals("00", saleResponse.getResponseCode());

        // test case 54
        Transaction voidResponse = saleResponse.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test
    public void ecomm_053_sale_gift_2_reversal() throws ApiException {
        GiftCard giftCard2 = new GiftCard();
		giftCard2.setNumber("5022440000000000007");

        Transaction saleResponse = giftCard2.charge(new BigDecimal("4.0"))
                .withCurrency("USD")
                .execute();
        assertNotNull(saleResponse);
        assertEquals("00", saleResponse.getResponseCode());

        // test case 55
        Transaction reverseResponse = saleResponse.reverse(new BigDecimal("4.0")).execute();
        assertNotNull(reverseResponse);
        assertEquals("00", reverseResponse.getResponseCode());
    }

    @Test
    public void ecomm_056_reversal_gift_2() throws ApiException {
        GiftCard giftCard2 = new GiftCard();
		giftCard2.setNumber("5022440000000000007");

        Transaction response = giftCard2.reverse(new BigDecimal("2.0")).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // DEACTIVATE

    @Test
    public void ecomm_057_deactivate_gift_1() throws ApiException {
        GiftCard giftCard1 = new GiftCard();
		giftCard1.setNumber("5022440000000000098");

        Transaction response = giftCard1.deactivate().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // RECEIPTS MESSAGING

    // ecomm_058_receipts_messaging: print and scan receipt for test 51

    // BALANCE INQUIRY

    @Test
    public void ecomm_059_balance_inquiry_rewards_1() throws ApiException {
        GiftCard giftCard1 = new GiftCard();
		giftCard1.setNumber("5022440000000000098");
        Transaction response = giftCard1.balanceInquiry().execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals(new BigDecimal("0"), response.getPointsBalanceAmount());
    }

    @Test
    public void ecomm_060_balance_inquiry_rewards_2() throws ApiException {
        GiftCard giftCard2 = new GiftCard();
		giftCard2.setNumber("5022440000000000007");
        Transaction response = giftCard2.balanceInquiry().execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals(new BigDecimal("0"), response.getPointsBalanceAmount());
    }

    // ALIAS

    @Test
    public void ecomm_061_create_alias_gift_1() throws ApiException {
        GiftCard card = GiftCard.create("9725550100");
        assertNotNull(card);
    }

    @Test
    public void ecomm_062_create_alias_gift_2() throws ApiException {
        GiftCard card = GiftCard.create("9725550100");
        assertNotNull(card);
    }

    @Test
    public void ecomm_063_add_alias_gift_1() throws ApiException {
        GiftCard giftCard1 = new GiftCard();
		giftCard1.setNumber("5022440000000000098");
        Transaction response = giftCard1.addAlias("2145550199").execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ecomm_064_add_alias_gift_2() throws ApiException {
        GiftCard giftCard2 = new GiftCard();
		giftCard2.setNumber("5022440000000000007");

        Transaction response = giftCard2.addAlias("2145550199").execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ecomm_065_delete_alias_gift_1() throws ApiException {
        GiftCard giftCard1 = new GiftCard();
		giftCard1.setNumber("5022440000000000098");

        Transaction response = giftCard1.removeAlias("2145550199").execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // SALE / REDEEM

    @Test
    public void ecomm_066_redeem_points_gift_1() throws ApiException {
        GiftCard giftCard1 = new GiftCard();
		giftCard1.setNumber("5022440000000000098");

        Transaction response = giftCard1.charge(new BigDecimal("100"))
                .withCurrency("POINTS")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ecomm_067_redeem_points_gift_2() throws ApiException {
        GiftCard giftCard2 = new GiftCard();
		giftCard2.setNumber("5022440000000000007");

        Transaction response = giftCard2.charge(new BigDecimal("200"))
                .withCurrency("POINTS")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ecomm_068_redeem_points_gift_2() throws ApiException {
        GiftCard giftCard = new GiftCard();
        giftCard.setAlias("9725550100");

        Transaction response = giftCard.charge(new BigDecimal("300.00"))
                .withCurrency("POINTS")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // REWARDS

    @Test
    public void ecomm_069_rewards_gift_1() throws ApiException {
        GiftCard giftCard1 = new GiftCard();
        giftCard1.setNumber("5022440000000000098");

        Transaction response = giftCard1.rewards(new BigDecimal("10.00")).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ecomm_070_rewards_gift_2() throws ApiException {
        GiftCard giftCard2 = new GiftCard();
		giftCard2.setNumber("5022440000000000007");

        Transaction response = giftCard2.rewards(new BigDecimal("11.00")).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // REPLACE / TRANSFER

    @Test
    public void ecomm_071_replace_gift_1() throws ApiException {
        GiftCard giftCard1 = new GiftCard();
		giftCard1.setNumber("5022440000000000098");
        GiftCard giftCard2 = new GiftCard();
		giftCard2.setNumber("5022440000000000007");

        Transaction response = giftCard1.replaceWith(giftCard2).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ecomm_072_replace_gift_2() throws ApiException {
        GiftCard giftCard1 = new GiftCard();
		giftCard1.setNumber("5022440000000000098");
        GiftCard giftCard2 = new GiftCard();
		giftCard2.setNumber("5022440000000000007");

        Transaction response = giftCard2.replaceWith(giftCard1).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // DEACTIVATE

    @Test
    public void ecomm_073_deactivate_gift_1() throws ApiException {
        GiftCard giftCard1 = new GiftCard();
		giftCard1.setNumber("5022440000000000098");

        Transaction response = giftCard1.deactivate().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ecomm_074_deactivate_gift_2() throws ApiException {
        GiftCard giftCard2 = new GiftCard();
		giftCard2.setNumber("5022440000000000007");

        Transaction response = giftCard2.deactivate().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // RECEIPTS MESSAGING

    // ecomm_075_receipts_messaging: print and scan receipt for test 51

    // CLOSE BATCH

    @Test
    public void ecomm_999_CloseBatch() throws ApiException {
        try {
            BatchSummary response = BatchService.closeBatch();
            assertNotNull(response);
            System.out.println(String.format("Batch ID: %s", response.getBatchId()));
            System.out.println(String.format("Sequence Number: %s", response.getSequenceNumber()));
        }
        catch (GatewayException exc) {
            if (exc.getResponseText().contains("Transaction was rejected because it requires a batch to be open."))
                Assert.fail(exc.getMessage());
        }
    }
}

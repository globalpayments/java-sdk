package com.global.api.tests.portico;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.PorticoConfig;
import com.global.api.services.ReportingService;
import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.Assert.*;

public class PorticoCreditTests {
	private CreditCardData card;
	private CreditTrackData track;
    private String clientTxnID;
    private CommercialData commercialData;

	public PorticoCreditTests() throws ApiException {
        PorticoConfig config = new PorticoConfig();
		config.setSecretApiKey("skapi_cert_MTeSAQAfG1UA9qQDrzl-kz4toXvARyieptFwSKP24w");
		config.setServiceUrl("https://cert.api2.heartlandportico.com");
		config.setDeveloperId("002914");
		config.setVersionNumber("3026");
		config.setEnableLogging(true);

		ServicesContainer.configureService(config);

		card = new CreditCardData();
		card.setNumber("4111111111111111");
		card.setExpMonth(12);
		card.setExpYear(2025);
		card.setCvn("123");

		track = new CreditTrackData();
		track.setValue("<E1050711%B4012001000000016^VI TEST CREDIT^251200000000000000000000?|LO04K0WFOmdkDz0um+GwUkILL8ZZOP6Zc4rCpZ9+kg2T3JBT4AEOilWTI|+++++++Dbbn04ekG|11;4012001000000016=25120000000000000000?|1u2F/aEhbdoPixyAPGyIDv3gBfF|+++++++Dbbn04ekG|00|||/wECAQECAoFGAgEH2wYcShV78RZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0PX50qfj4dt0lu9oFBESQQNkpoxEVpCW3ZKmoIV3T93zphPS3XKP4+DiVlM8VIOOmAuRrpzxNi0TN/DWXWSjUC8m/PI2dACGdl/hVJ/imfqIs68wYDnp8j0ZfgvM26MlnDbTVRrSx68Nzj2QAgpBCHcaBb/FZm9T7pfMr2Mlh2YcAt6gGG1i2bJgiEJn8IiSDX5M2ybzqRT86PCbKle/XCTwFFe1X|>;");
		track.setEncryptionData(EncryptionData.version1());

        int randomID = new Random().nextInt(999999 - 10000)+10000;
        clientTxnID = Integer.toString(randomID);

        commercialData = new CommercialData(TaxType.SalesTax, TransactionModifier.Level_III) ;
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
	public void creditAuthorization() throws ApiException {
		Transaction response = card.authorize(new BigDecimal(14)).withCurrency("USD").withAllowDuplicates(true)
				.execute();
		assertNotNull(response);
		assertEquals("00", response.getResponseCode());

		Transaction capture = response.capture(new BigDecimal(16)).withGratuity(new BigDecimal(2)).execute();
		assertNotNull(capture);
		assertEquals("00", capture.getResponseCode());
	}

	@Test
	public void creditAuthWithConvenienceAmt() throws ApiException {
		Transaction response = card.authorize(new BigDecimal(14)).withCurrency("USD").withAllowDuplicates(true)
				.withConvenienceAmt(new BigDecimal(2)).execute();
		assertNotNull(response);
		assertEquals("00", response.getResponseCode());

		TransactionSummary report = ReportingService.transactionDetail(response.getTransactionId()).execute();
		assertNotNull(report);
		assertEquals(new BigDecimal("2.00"), report.getConvenienceAmount());
	}

	@Test
	public void creditAuthWithShippingAmt() throws ApiException {
		Transaction response = card.authorize(new BigDecimal(14)).withCurrency("USD").withAllowDuplicates(true)
				.withShippingAmt(new BigDecimal(2)).execute();
		assertNotNull(response);
		assertEquals("00", response.getResponseCode());

		TransactionSummary report = ReportingService.transactionDetail(response.getTransactionId()).execute();
		assertNotNull(report);
		assertEquals(new BigDecimal("2.00"), report.getShippingAmount());
	}

	@Test
	public void creditSale() throws ApiException {
        Transaction response = card.charge(new BigDecimal(15))
                .withCurrency("USD")
                .withClientTransactionId(clientTxnID)
                .withUniqueDeviceId("5678")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals(clientTxnID, response.getClientTransactionId());
	}
    @Test
    public void creditSaleWithCardHolderLanguage() throws ApiException {
        Transaction response = card.charge(new BigDecimal(15))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withCardHolderLanguage("en-US")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
	
	@Test
	public void creditTestWithNewCryptoURL() throws ApiException {
        PorticoConfig config = new PorticoConfig();
		config.setSecretApiKey("skapi_cert_MTeSAQAfG1UA9qQDrzl-kz4toXvARyieptFwSKP24w");
		config.setServiceUrl("https://cert.api2-c.heartlandportico.com");

		ServicesContainer.configureService(config);

		card = new CreditCardData();
		card.setNumber("4111111111111111");
		card.setExpMonth(12);
		card.setExpYear(2025);
		card.setCvn("123");

		Transaction response = card.authorize(new BigDecimal(14)).withCurrency("USD").withAllowDuplicates(true)
		        .execute();
		assertNotNull(response);
		assertEquals("00", response.getResponseCode());
	}

	@Test
	public void creditTokenization() throws ApiException {
        PorticoConfig config = new PorticoConfig();
		config.setSecretApiKey("skapi_cert_MTyMAQBiHVEAewvIzXVFcmUd2UcyBge_eCpaASUp0A");
		config.setServiceUrl("https://cert.api2-c.heartlandportico.com");
		config.setEnableLogging(true);

		ServicesContainer.configureService(config, "tokenConfig");

		String response = card.tokenize();
		assertNotNull(response);
	}
	@Test
	public void creditTokenizationWithVerify() throws ApiException {
        PorticoConfig config = new PorticoConfig();
	    config.setSecretApiKey("skapi_cert_MTyMAQBiHVEAewvIzXVFcmUd2UcyBge_eCpaASUp0A");
	    config.setServiceUrl("https://cert.api2-c.heartlandportico.com");
	    config.setEnableLogging(true);

	    ServicesContainer.configureService(config, "tokenConfig");

	    String response = card.tokenize(true, "tokenConfig");
	    assertNotNull(response);
	}
	@Test
	public void creditTokenizationWithoutVerify() throws ApiException {
        PorticoConfig config = new PorticoConfig();
		config.setSecretApiKey("skapi_cert_MTyMAQBiHVEAewvIzXVFcmUd2UcyBge_eCpaASUp0A");
		config.setServiceUrl("https://cert.api2-c.heartlandportico.com");
		config.setEnableLogging(true);
		
		ServicesContainer.configureService(config, "tokenConfig");

		String response = card.tokenize(false, "tokenConfig");
		assertNotNull(response);
	}

    @Test
    public void creditSaleWithConvenienceAmt() throws ApiException {
        Transaction response = card.charge(new BigDecimal(14))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withConvenienceAmt(new BigDecimal(2))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        TransactionSummary report = ReportingService.transactionDetail(response.getTransactionId()).execute();
        assertNotNull(report);
        assertEquals(new BigDecimal("2.00"), report.getConvenienceAmount());
    }

    @Test
    public void creditSaleWithShippingAmt() throws ApiException {
        Transaction response = card.charge(new BigDecimal(14))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withShippingAmt(new BigDecimal(2))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        TransactionSummary report = ReportingService.transactionDetail(response.getTransactionId()).execute();
        assertNotNull(report);
        assertEquals(new BigDecimal("2.00"), report.getShippingAmount());
    }

    @Test
    public void creditOfflineAuth() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(16))
                .withCurrency("USD")
                .withOfflineAuthCode("12345")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditOfflineSale() throws ApiException {
        Transaction response = card.charge(new BigDecimal(17))
                .withCurrency("USD")
                .withOfflineAuthCode("12345")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditRefund() throws ApiException {
        String invoiceNumber = Long.toString(System.currentTimeMillis());
        Transaction response = card.refund(new BigDecimal(16))
                .withCurrency("USD")
                .withInvoiceNumber(invoiceNumber)
                .withCustomerId("Customer7766")
                .withDescription("This is a good description")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        TransactionSummary report = ReportingService.transactionDetail(response.getTransactionId())
                .execute();
        assertEquals(invoiceNumber, report.getInvoiceNumber());
        assertEquals("Customer7766", report.getCustomerId());
        assertEquals("This is a good description", report.getDescription());
    }

    @Test
    public void creditReverse() throws ApiException {
        Transaction response = card.charge(new BigDecimal(18))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction reversal = card.reverse(new BigDecimal(18))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);
        assertEquals("00", reversal.getResponseCode());

        TransactionSummary report = ReportingService.transactionDetail(response.getTransactionId())
                .execute();
        assertEquals("R", report.getStatus());
    }

    @Test
    public void creditPartialReverse() throws ApiException {
        Transaction response = card.charge(new BigDecimal("18.00"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction reversal = Transaction.fromId(response.getTransactionId())
                .reverse(new BigDecimal("18.00"))
                .withAuthAmount(new BigDecimal("10.00"))
                .execute();
        assertNotNull(reversal);
        assertEquals("00", reversal.getResponseCode());

        TransactionSummary report = ReportingService.transactionDetail(response.getTransactionId())
                .execute();
        TransactionSummary report2 = ReportingService.transactionDetail(reversal.getTransactionId())
                .execute();
        assertEquals("A", report.getStatus());
        assertEquals(new BigDecimal("10.00"), report2.getAuthorizedAmount());
    }

    @Test
    public void creditVerify() throws ApiException {
        Transaction response = card.verify()
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSwipeAuthorization() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(14))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction capture = response.capture(new BigDecimal(16))
                .withGratuity(new BigDecimal(2))
                .execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void creditSwipeSale() throws ApiException {
        Transaction response = track.charge(new BigDecimal(15))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withClientTransactionId(clientTxnID)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals(clientTxnID, response.getClientTransactionId());
    }

    @Test
    public void creditSwipeOfflineAuth() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(16))
                .withCurrency("USD")
                .withOfflineAuthCode("12345")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSwipeOfflineSale() throws ApiException {
        Transaction response = track.charge(new BigDecimal(17))
                .withCurrency("USD")
                .withOfflineAuthCode("12345")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test @Ignore
    public void creditSwipeAddValue() throws ApiException {
        Transaction response = track.addValue(new BigDecimal(16))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSwipeBalanceInquiry() throws ApiException {
        Transaction response = track.balanceInquiry().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSwipeRefund() throws ApiException {
        Transaction response = track.refund(new BigDecimal(16))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withClientTransactionId(clientTxnID)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals(clientTxnID, response.getClientTransactionId());
    }

    @Test
    public void creditSwipeReverse() throws ApiException {
        Transaction response = track.charge(new BigDecimal(19))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withClientTransactionId(clientTxnID)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction reverseResponse = track.reverse(new BigDecimal(19))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(reverseResponse);
        assertEquals("00", reverseResponse.getResponseCode());
        assertEquals(clientTxnID, response.getClientTransactionId());
    }

    @Test
    public void creditSwipeVerify() throws ApiException {
        Transaction response = card.verify()
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditVoidFromTransactionId() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction voidResponse = Transaction.fromId(response.getTransactionId())
                .voidTransaction()
                .execute();
        assertNotNull(voidResponse);
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test
    public void creditReversalFromClientTransactionId() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withClientTransactionId(clientTxnID)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction voidResponse = Transaction.fromClientTransactionId(clientTxnID)
                .reverse(new BigDecimal(10))
                .execute();
        assertNotNull(voidResponse);
        assertEquals("00", voidResponse.getResponseCode());
        assertEquals(clientTxnID,response.getClientTransactionId());
    }

    @Test
    public void creditTestManualWithOneName() throws ApiException {
        PorticoConfig config = new PorticoConfig();
        config.setSecretApiKey("skapi_cert_MTeSAQAfG1UA9qQDrzl-kz4toXvARyieptFwSKP24w");
        config.setServiceUrl("https://cert.api2-c.heartlandportico.com");
        config.setEnableLogging(true);
        ServicesContainer.configureService(config);

        Address address = new Address();
        address.setPostalCode("75024");

        card = new CreditCardData();
        card.setNumber("4111111111111111");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");
        card.setCardHolderName("John");

        Transaction response = card.authorize(new BigDecimal(14))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditTestManualWithTwoNames() throws ApiException {
        PorticoConfig config = new PorticoConfig();
        config.setSecretApiKey("skapi_cert_MTeSAQAfG1UA9qQDrzl-kz4toXvARyieptFwSKP24w");
        config.setServiceUrl("https://cert.api2-c.heartlandportico.com");

        ServicesContainer.configureService(config);

        Address address = new Address();
        address.setPostalCode("75024");

        card = new CreditCardData();
        card.setNumber("4111111111111111");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");
        card.setCardHolderName("John Doe");

        Transaction response = card.authorize(new BigDecimal(14))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditTestManualWithThreeNames() throws ApiException {
        PorticoConfig config = new PorticoConfig();
        config.setSecretApiKey("skapi_cert_MTeSAQAfG1UA9qQDrzl-kz4toXvARyieptFwSKP24w");
        config.setServiceUrl("https://cert.api2-c.heartlandportico.com");

        ServicesContainer.configureService(config);

        Address address = new Address();
        address.setPostalCode("75024");

        card = new CreditCardData();
        card.setNumber("4111111111111111");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");
        card.setCardHolderName("John Doe Smith");

        Transaction response = card.authorize(new BigDecimal(14))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSaleWithTDESDukptEMV() throws ApiException {
        PorticoConfig config = new PorticoConfig();
        config.setSecretApiKey("skapi_cert_MTeSAQAfG1UA9qQDrzl-kz4toXvARyieptFwSKP24w");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");

        ServicesContainer.configureService(config);

        CreditTrackData card = new CreditTrackData();
        card.setValue("oDA60Hw+9/K2wx+DA3Xn/q+8AZzl2ojR");
        EncryptionData encryptionData = new EncryptionData();
        encryptionData.setVersion("05");
        encryptionData.setTrackNumber("2");
        encryptionData.setKsn("//89P4EAAEAACA==");
        card.setEncryptionData(encryptionData);

        Transaction response = card.charge(new BigDecimal(14))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withTagData("9F1A0208409C0150950500000088009F0702FF009F03060000000000009F2701809F3901059F0D05B850AC88009F350121500B56697361204372656469745F3401019F0802008C9F120B56697361204372656469749F0E0500000000009F360200759F40057E0000A0019F0902008C9F0F05B870BC98009F370425D254AC5F280208409F33036028C882023C004F07A00000000310109F4104000000899F0607A00000000310105F2A0208409A031911229F02060000000001009F2608D4EC434B9C1CBB358407A00000000310109F100706010A03A088069B02E8009F34031E0300")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    
    @Test
    public void creditSaleWithTDESDukptEMV_postAuthChipDecline() throws ApiException {
        PorticoConfig config = new PorticoConfig();
        config.setSecretApiKey("skapi_cert_MTeSAQAfG1UA9qQDrzl-kz4toXvARyieptFwSKP24w");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");
        config.setEnableLogging(true);

        ServicesContainer.configureService(config);

        CreditTrackData card = new CreditTrackData();
        card.setValue("oDA60Hw+9/K2wx+DA3Xn/q+8AZzl2ojR");
        EncryptionData encryptionData = new EncryptionData();
        encryptionData.setVersion("05");
        encryptionData.setTrackNumber("2");
        encryptionData.setKsn("//89P4EAAEAACA==");
        card.setEncryptionData(encryptionData);

        Transaction response = card.charge(new BigDecimal(14))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withTagData("9F1A0208409C0150950500000088009F0702FF009F03060000000000009F2701809F3901059F0D05B850AC88009F350121500B56697361204372656469745F3401019F0802008C9F120B56697361204372656469749F0E0500000000009F360200759F40057E0000A0019F0902008C9F0F05B870BC98009F370425D254AC5F280208409F33036028C882023C004F07A00000000310109F4104000000899F0607A00000000310105F2A0208409A031911229F02060000000001009F2608D4EC434B9C1CBB358407A00000000310109F100706010A03A088069B02E8009F34031E0300")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction reversalResponse = response.reverse(new BigDecimal(14))
                .withTagData(response.getEmvIssuerResponse())
                .execute();
        assertNotNull(reversalResponse);
        assertEquals("00", reversalResponse.getResponseCode());
    }

    @Test
    public void reportDetail_withNonexistentGatewayTxnId_throwsException() throws ApiException {
        try {
            TransactionSummary response = ReportingService.transactionDetail("0").execute();
        } catch (GatewayException ex) {
            assertEquals("10", ex.getResponseCode());
        }
    }

    @Test
    public void findTransactions_withNonexistentClientTxnId_returnsEmptyResultList() throws ApiException {
        TransactionSummaryList response = ReportingService.findTransactions()
                .where(SearchCriteria.ClientTransactionId, "12345")
                .execute();

        assertNotNull(response);
        assertEquals(0, response.size());
    }

    @Test
    public void creditSaleWithCOF() throws ApiException {
        Transaction response = card.charge(new BigDecimal(15))
                .withCurrency("USD")
                .withCardBrandStorage(StoredCredentialInitiator.CardHolder)
                .withRequestMultiUseToken(true)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertNotNull(response.getCardBrandTransactionId());

        Transaction nextResponse = card.charge(new BigDecimal(15))
                .withCurrency("USD")
                .withCardBrandStorage(StoredCredentialInitiator.Merchant,response.getCardBrandTransactionId())
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(nextResponse);
        assertEquals("00", nextResponse.getResponseCode());
    }

    @Test
    public void creditVerifyWithCOF() throws ApiException {
        Transaction response = card.verify()
                .withAllowDuplicates(true)
                .withRequestMultiUseToken(true)
                .withCardBrandStorage(StoredCredentialInitiator.CardHolder)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertNotNull(response.getCardBrandTransactionId());

        Transaction nextResponse = card.verify()
                .withAllowDuplicates(true)
                .withCardBrandStorage(StoredCredentialInitiator.Merchant,response.getCardBrandTransactionId())
                .execute();
        assertNotNull(nextResponse);
        assertEquals("00", nextResponse.getResponseCode());
    }

    @Test
    public void creditAuthorizationWithCOF() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(14))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withRequestMultiUseToken(true)
                .withCardBrandStorage(StoredCredentialInitiator.Merchant)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction nextResponse = card.authorize(new BigDecimal(14))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withCardBrandStorage(StoredCredentialInitiator.CardHolder, response.getCardBrandTransactionId())
                .execute();
        assertNotNull(nextResponse);
        assertEquals("00", nextResponse.getResponseCode());

        Transaction capture = nextResponse.capture(new BigDecimal(16))
                .withGratuity(new BigDecimal(2))
                .execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void creditSaleAdditionalDuplicateData() throws ApiException {
        Transaction response = card.charge(new BigDecimal(14))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withRequestMultiUseToken(true)
                .withCardBrandStorage(StoredCredentialInitiator.CardHolder)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction nextResponse = card.charge(new BigDecimal(14))
                .withCurrency("USD")
                .withAllowDuplicates(false)
                .withCardBrandStorage(StoredCredentialInitiator.CardHolder,response.getCardBrandTransactionId())
                .execute();
        assertNotNull(nextResponse);
        assertNotNull(nextResponse.getAdditionalDuplicateData());
    }
    @Test
    public void creditSale_Pinblock() throws ApiException {
	    track.setPinBlock("abcjhvcjbvhjxbvjxh");
        Transaction response = track.charge(new BigDecimal(15))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withChipCondition(EmvChipCondition.ChipFailPreviousSuccess)
                //.withTagData("9F1A0208409C0150950500000088009F0702FF009F03060000000000009F2701809F3901059F0D05B850AC88009F350121500B56697361204372656469745F3401019F0802008C9F120B56697361204372656469749F0E0500000000009F360200759F40057E0000A0019F0902008C9F0F05B870BC98009F370425D254AC5F280208409F33036028C882023C004F07A00000000310109F4104000000899F0607A00000000310105F2A0208409A031911229F02060000000001009F2608D4EC434B9C1CBB358407A00000000310109F100706010A03A088069B02E8009F34031E0300")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSale_withSAFIndicator_And_SAFOrigDT_01() throws ApiException {
        Transaction response = card.charge(new BigDecimal(15))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withSAFIndicator(true)
                .withSAFOrigDT(DateTime.now().toString())
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSale_withSAFIndicator_And_SAFOrigDT_02() throws ApiException {
        Transaction response = card.charge(new BigDecimal(15))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withSAFIndicator(false)
                .withSAFOrigDT(DateTime.now().toString())
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSale_withoutSAFIndicator03() throws ApiException {
        Transaction response = card.charge(new BigDecimal(15))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditAuthorization_withSAFIndicator_And_SAFOrigDT_01() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(14)).withCurrency("USD").withAllowDuplicates(true)
                .withSAFIndicator(true)
                .withSAFOrigDT(DateTime.now().toString())
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction capture = response.capture(new BigDecimal(16)).withGratuity(new BigDecimal(2))
                .withSAFIndicator(true)
                .withSAFOrigDT(DateTime.now().toString())
                .execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void creditAuthorization_withSAFIndicator_And_SAFOrigDT_02() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(14)).withCurrency("USD").withAllowDuplicates(true)
                .withSAFIndicator(false)
                .withSAFOrigDT(DateTime.now().toString())
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction capture = response.capture(new BigDecimal(16)).withGratuity(new BigDecimal(2))
                .withSAFIndicator(false)
                .withSAFOrigDT(DateTime.now().toString())
                .execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void creditAuthorization_withoutSAFIndicator03() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(14))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction capture = response.capture(new BigDecimal(16)).withGratuity(new BigDecimal(2)).execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void creditSale_ClerkId() throws ApiException {
        Transaction response = card.charge(new BigDecimal(15))
                .withCurrency("USD")
                .withClientTransactionId(clientTxnID)
                .withAllowDuplicates(true)
                .withClerkId("CL_1001")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void CreditCapture_ClerkId() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(14)).withCurrency("USD").withAllowDuplicates(true)
                .withClerkId("0998_ID")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction capture = response.capture(new BigDecimal(16))
                .withGratuity(new BigDecimal(2))
                .withClerkId("C3008_ID")
                .execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }
    @Test
    public void ClerkId_Negative() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(14))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withClerkId("C3008")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        BuilderException exc = assertThrows(BuilderException.class,()-> {
       response.capture(new BigDecimal(16))
                .withGratuity(new BigDecimal(2))
                .withClerkId("C3008_ID0983634567ndhgfds45678908765432wqsdcvn 87723")
                .execute();

        });
        assertEquals("length should not be more than 50 digits",exc.getMessage());
  }
    @Test
    public void creditAuthorizationWithCOF_categoryIndicator() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(14))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withCardBrandStorage(StoredCredentialInitiator.Merchant)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction nextResponse = card.authorize(new BigDecimal(14))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withCardBrandStorage(StoredCredentialInitiator.CardHolder, response.getCardBrandTransactionId(),"07")
                .execute();
        assertNotNull(nextResponse);
        assertEquals("00", nextResponse.getResponseCode());

        Transaction capture = nextResponse.capture(new BigDecimal(16))
                .withGratuity(new BigDecimal(2))
                .execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void credit_AddtoBatch() throws ApiException {
        PorticoConfig config = new PorticoConfig();
        config.setSecretApiKey("skapi_cert_MTeSAQAfG1UA9qQDrzl-kz4toXvARyieptFwSKP24w");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");
        config.setEnableLogging(true);

        ServicesContainer.configureService(config);

        Transaction response = track.authorize(new BigDecimal(14))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withTagData("9F1A0208409C0150950500000088009F0702FF009F03060000000000009F2701809F3901059F0D05B850AC88009F350121500B56697361204372656469745F3401019F0802008C9F120B56697361204372656469749F0E0500000000009F360200759F40057E0000A0019F0902008C9F0F05B870BC98009F370425D254AC5F280208409F33036028C882023C004F07A00000000310109F4104000000899F0607A00000000310105F2A0208409A031911229F02060000000001009F2608D4EC434B9C1CBB358407A00000000310109F100706010A03A088069B02E8009F34031E0300")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction captureResponse = response.capture(new BigDecimal(14))
                .withTagData(response.getEmvIssuerResponse())
                .withChipCondition(EmvChipCondition.ChipFailPreviousSuccess)
                .execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode());
    }
}


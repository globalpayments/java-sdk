package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.BrowserData;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.FraudService;
import lombok.var;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GpApiRiskAssessmentTest extends BaseGpApiTest {

    private CreditCardData card;
    private Address shippingAddress;
    private BrowserData browserData;

    private final static String Currency = "GBP";
    private final static BigDecimal Amount = new BigDecimal(10.01);

    public GpApiRiskAssessmentTest() throws ConfigurationException {
        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId(APP_ID)
                .setAppKey(APP_KEY)
                .setCountry("GB");

        config.setChallengeNotificationUrl("https://ensi808o85za.x.pipedream.net/");
        config.setMethodNotificationUrl("https://ensi808o85za.x.pipedream.net/");
        config.setMerchantContactUrl("https://enp4qhvjseljg.x.pipedream.net/");

        config.setEnableLogging(true);

        ServicesContainer.configureService(config);
    }

    @Before
    public void testInitialize() throws ApiException {
        // Create card data
        card = new CreditCardData();
        card.setExpMonth(expMonth);
        card.setExpYear(expYear);
        card.setCardHolderName("John Smith");
        card.setNumber("4012001038488884");

        // Shipping address
        shippingAddress =
                new Address()
                        .setStreetAddress1("Apartment 852")
                        .setStreetAddress2("Complex 741")
                        .setStreetAddress3("no")
                        .setCity("Chicago")
                        .setPostalCode("5001")
                        .setState("IL")
                        .setCountryCode("840");

        // Browser data
        browserData =
                new BrowserData()
                        .setAcceptHeader("text/html,application/xhtml+xml,application/xml;q=9,image/webp,img/apng,*/*;q=0.8")
                        .setColorDepth(ColorDepth.TwentyFourBit)
                        .setIpAddress("123.123.123.123")
                        .setJavaEnabled(true)
                        .setLanguage("en")
                        .setChallengeWindowSize(ChallengeWindowSize.Windowed_600x400)
                        .setTimezone("0")
                        .setUserAgent("Mozilla/5.0 (Windows NT 6.1; Win64, x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36");
    }

    @Test
    public void TransactionRiskAnalysisBasicOption() throws ApiException {
        var response =
                FraudService
                        .RiskAssess(card)
                        .WithAmount(Amount)
                        .WithCurrency(Currency)
                        .WithAuthenticationSource(AuthenticationSource.Browser)
                        .WithBrowserData(browserData)
                        .execute();

        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(RiskAssessmentStatus.ACCEPTED, response.getStatus());
    }

    @Test
    public void RiskAssessment() throws ApiException {
        var idempotencyKey = UUID.randomUUID().toString();

        var response =
                FraudService
                        .RiskAssess(card)
                        .WithAmount(Amount)
                        .WithCurrency(Currency)
                        .WithAuthenticationSource(AuthenticationSource.Browser)
                        .WithOrderCreateDate(DateTime.now())
                        .WithReferenceNumber("my_EOS_risk_assessment")
                        .WithAddressMatchIndicator(false)
                        .WithAddress(shippingAddress, AddressType.Shipping)
                        .WithGiftCardAmount(new BigDecimal(2))
                        .WithGiftCardCount(1)
                        .WithGiftCardCurrency(Currency)
                        .WithDeliveryEmail("james.mason@example.com")
                        .WithDeliveryTimeFrame(DeliveryTimeFrame.SameDay)
                        .WithShippingMethod(ShippingMethod.VerifiedAddress)
                        .WithShippingNameMatchesCardHolderName(false)
                        .WithPreOrderIndicator(PreOrderIndicator.FutureAvailability)
                        .WithPreOrderAvailabilityDate(DateTime.parse("2019-04-18"))
                        .WithReorderIndicator(ReorderIndicator.Reorder)
                        .WithOrderTransactionType(OrderTransactionType.GoodsAndServicesPurchase)
                        .WithCustomerAccountId("6dcb24f5-74a0-4da3-98da-4f0aa0e88db3")
                        .WithAccountAgeIndicator(AgeIndicator.LessThanThirtyDays)
                        .WithAccountCreateDate(DateTime.parse("2019-01-10"))
                        .WithAccountChangeDate(DateTime.parse("2019-01-28"))
                        .WithAccountChangeIndicator(AgeIndicator.ThisTransaction)
                        .WithPasswordChangeDate(DateTime.parse("2019-01-15"))
                        .WithPasswordChangeIndicator(AgeIndicator.LessThanThirtyDays)
                        .WithPhoneNumber("44", "123456789", PhoneNumberType.Home)
                        .WithPhoneNumber("44", "1801555888", PhoneNumberType.Work)
                        .WithPaymentAccountCreateDate(DateTime.now())
                        .WithPaymentAccountAgeIndicator(AgeIndicator.LessThanThirtyDays)
                        .WithPreviousSuspiciousActivity(false)
                        .WithNumberOfPurchasesInLastSixMonths(3)
                        .WithNumberOfTransactionsInLast24Hours(1)
                        .WithNumberOfTransactionsInLastYear(5)
                        .WithNumberOfAddCardAttemptsInLast24Hours(1)
                        .WithShippingAddressCreateDate(DateTime.now())
                        .WithShippingAddressUsageIndicator(AgeIndicator.ThisTransaction)
                        .WithPriorAuthenticationMethod(PriorAuthenticationMethod.FrictionlessAuthentication)
                        .WithPriorAuthenticationTransactionId(UUID.randomUUID().toString())
                        .WithPriorAuthenticationTimestamp(DateTime.parse("2022-10-10T16:41:33.333Z"))
                        .WithPriorAuthenticationData("secret123")
                        .WithMaxNumberOfInstallments(5)
                        .WithRecurringAuthorizationFrequency(25)
                        .WithRecurringAuthorizationExpiryDate(DateTime.now())
                        .WithCustomerAuthenticationData("secret123")
                        .WithCustomerAuthenticationTimestamp(DateTime.parse("2022-10-10T16:41:33"))
                        .WithCustomerAuthenticationMethod(CustomerAuthenticationMethod.MerchantSystem)
                        .WithBrowserData(browserData)
                        .WithIdempotencyKey(idempotencyKey)
                        .execute();

        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(RiskAssessmentStatus.ACCEPTED, response.getStatus());
    }

    @Test
    public void TransactionRiskAnalysis_AllSources() throws ApiException {
        var source = new ArrayList<AuthenticationSource>();
        source.add(AuthenticationSource.Browser);
        source.add(AuthenticationSource.MerchantInitiated);
        source.add(AuthenticationSource.MobileSDK);

        for (var item : source){

            var response = FraudService.RiskAssess(card)
                    .WithAmount(Amount)
                    .WithCurrency(Currency)
                    .WithAuthenticationSource(item)
                    .WithBrowserData(browserData)
                    .execute();

            assertEquals("SUCCESS", response.getResponseCode());
            assertEquals(RiskAssessmentStatus.ACCEPTED, response.getStatus());
            assertTrue(response.getId().startsWith(("RAS_")));
        }
    }

    @Test
    public void TransactionRiskAnalysis_AllDeliveryTimeFrames() throws ApiException {
        for (DeliveryTimeFrame value : DeliveryTimeFrame.values()) {

            var response =
                    FraudService
                            .RiskAssess(card)
                            .WithAmount(Amount)
                            .WithCurrency(Currency)
                            .WithAuthenticationSource(AuthenticationSource.Browser)
                            .WithBrowserData(browserData)
                            .WithDeliveryTimeFrame(value)
                            .execute();

            assertEquals("SUCCESS", response.getResponseCode());
            assertEquals(RiskAssessmentStatus.ACCEPTED, response.getStatus());
            assertTrue(response.getId().startsWith("RAS_"));
        }
    }

    @Test
    public void TransactionRiskAnalysis_AllShippingMethods() throws ApiException {
        for (ShippingMethod value : ShippingMethod.values()) {
            var response =
                    FraudService
                            .RiskAssess(card)
                            .WithAmount(Amount)
                            .WithCurrency(Currency)
                            .WithAuthenticationSource(AuthenticationSource.Browser)
                            .WithBrowserData(browserData)
                            .WithShippingMethod(value)
                            .execute();

            assertEquals("SUCCESS", response.getResponseCode());
            assertEquals(RiskAssessmentStatus.ACCEPTED, response.getStatus());
            assertTrue(response.getId().startsWith("RAS_"));
        }
    }

    @Test
    public void TransactionRiskAnalysis_AllOrderTransactionTypes() throws ApiException {
        for (OrderTransactionType value : OrderTransactionType.values()) {
            var response =
                    FraudService
                            .RiskAssess(card)
                            .WithAmount(Amount)
                            .WithCurrency(Currency)
                            .WithAuthenticationSource(AuthenticationSource.Browser)
                            .WithBrowserData(browserData)
                            .WithOrderTransactionType(value)
                            .execute();

            assertEquals("SUCCESS", response.getResponseCode());
            assertEquals(RiskAssessmentStatus.ACCEPTED, response.getStatus());
            assertTrue(response.getId().startsWith("RAS_"));
        }
    }

    @Test
    public void TransactionRiskAnalysis_AllPriorAuthenticationMethods() throws ApiException {
        for (PriorAuthenticationMethod value : PriorAuthenticationMethod.values()) {
            var response =
                    FraudService
                            .RiskAssess(card)
                            .WithAmount(Amount)
                            .WithCurrency(Currency)
                            .WithAuthenticationSource(AuthenticationSource.Browser)
                            .WithBrowserData(browserData)
                            .WithPriorAuthenticationMethod(value)
                            .execute();

            assertEquals("SUCCESS", response.getResponseCode());
            assertEquals(RiskAssessmentStatus.ACCEPTED, response.getStatus());
            assertTrue(response.getId().startsWith("RAS_"));
        }
    }

    @Test
    public void TransactionRiskAnalysis_AllCustomerAuthenticationMethods() throws ApiException {
        for (CustomerAuthenticationMethod value : CustomerAuthenticationMethod.values()) {
            var response =
                    FraudService
                            .RiskAssess(card)
                            .WithAmount(Amount)
                            .WithCurrency(Currency)
                            .WithAuthenticationSource(AuthenticationSource.Browser)
                            .WithBrowserData(browserData)
                            .WithCustomerAuthenticationMethod(value)
                            .execute();

            assertEquals("SUCCESS", response.getResponseCode());
            assertEquals(RiskAssessmentStatus.ACCEPTED, response.getStatus());
            assertEquals("Apply Exemption", response.getResponseMessage());
            assertTrue(response.getId().startsWith("RAS_"));

        }
    }

    @Test
    public void TransactionRiskAnalysis_MissingAmount() throws ApiException {
        var errorFound = false;

        try {
            FraudService
                    .RiskAssess(card)
                    .WithCurrency(Currency)
                    .WithAuthenticationSource(AuthenticationSource.Browser)
                    .WithBrowserData(browserData)
                    .execute();
        } catch (GatewayException e) {
            errorFound = true;
            assertEquals("Status Code: 400 - Request expects the following field order.amount", e.getMessage());
            assertEquals("40005", e.getResponseText());
        } finally {
            assertTrue(errorFound);
        }
    }

    @Test
    public void TransactionRiskAnalysis_MissingCurrency() {
        var errorFound = false;

        try {
            FraudService
                    .RiskAssess(card)
                    .WithAmount(Amount)
                    .WithAuthenticationSource(AuthenticationSource.Browser)
                    .WithBrowserData(browserData)
                    .execute();
        } catch (GatewayException e) {
            errorFound = true;
            assertEquals("Status Code: 400 - Request expects the following field order.currency", e.getMessage());
            assertEquals("40005", e.getResponseText());
        } catch (ApiException e) {
            throw new RuntimeException(e);
        } finally {
            assertTrue(errorFound);
        }
    }

    @Test
    public void TransactionRiskAnalysis_MissingSource() throws ApiException {
        var errorFound = false;

        try {
            FraudService
                    .RiskAssess(card)
                    .WithAmount(Amount)
                    .WithCurrency(Currency)
                    .WithBrowserData(browserData)
                    .execute();
        } catch (GatewayException e) {
            errorFound = true;
            assertEquals("Status Code: 400 - Request expects the following field source", e.getMessage());
            assertEquals("40005", e.getResponseText());
        } finally {
            assertTrue(errorFound);
        }
    }

    @Test
    public void TransactionRiskAnalysis_MissingBrowserData() throws ApiException {
        var errorFound = false;

        try {
            FraudService.RiskAssess(card)
                    .WithAmount(Amount)
                    .WithCurrency(Currency)
                    .WithAuthenticationSource(AuthenticationSource.Browser)
                    .execute();
        } catch (GatewayException e) {
            errorFound = true;
            assertEquals("Status Code: 400 - Request expects the following field browser_data.accept_header", e.getMessage());
            assertEquals("40005", e.getResponseText());
        }finally {
            assertTrue(errorFound);
        }
    }

    @Test
    public void TransactionRiskAnalysis_MissingCard() throws ApiException {
        var errorFound = false;
        try {
            FraudService.RiskAssess(new CreditCardData())
                    .WithAmount(Amount)
                    .WithCurrency(Currency)
                    .WithAuthenticationSource(AuthenticationSource.Browser)
                    .WithBrowserData(browserData)
                    .execute();
        } catch (GatewayException e) {
            errorFound = true;
            assertEquals("Status Code: 400 - Request contains unexpected data payment_method.card.brand", e.getMessage());
            assertEquals("40006", e.getResponseText());
        } finally {
            assertTrue(errorFound);
        }
    }

}
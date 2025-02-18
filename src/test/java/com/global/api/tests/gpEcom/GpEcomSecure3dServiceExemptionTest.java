package com.global.api.tests.gpEcom;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.BrowserData;
import com.global.api.entities.ThreeDSecure;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.RecurringPaymentMethod;
import com.global.api.serviceConfigs.GpEcomConfig;
import com.global.api.services.Secure3dService;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

public class GpEcomSecure3dServiceExemptionTest extends BaseGpEComTest {

    private final CreditCardData card;
    private final RecurringPaymentMethod stored;
    private final Address shippingAddress;
    private final Address billingAddress;
    private final BrowserData browserData;

    public GpEcomSecure3dServiceExemptionTest() throws ApiException {
        GpEcomConfig config = gpEComSetup();
        config.setMerchantId("myMerchantId");
        config.setAccountId("ecomeos");

        ServicesContainer.configureService(config);

        // create card data
        card = new CreditCardData();
        card.setNumber("4263970000005262");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardHolderName("John Smith");

        // stored card
        stored = new RecurringPaymentMethod(
                "20190809-Realex",
                "20190809-Realex-Credit"
        );

        // shipping address
        shippingAddress = new Address();
        shippingAddress.setStreetAddress1("Apartment 852");
        shippingAddress.setStreetAddress2("Complex 741");
        shippingAddress.setStreetAddress3("no");
        shippingAddress.setCity("Chicago");
        shippingAddress.setPostalCode("5001");
        shippingAddress.setState("IL");
        shippingAddress.setCountryCode("840");

        // billing address
        billingAddress = new Address();
        billingAddress.setStreetAddress1("Flat 456");
        billingAddress.setStreetAddress2("House 789");
        billingAddress.setStreetAddress3("no");
        billingAddress.setCity("Halifax");
        billingAddress.setPostalCode("W5 9HR");
        billingAddress.setCountryCode("826");

        // browser data
        browserData = new BrowserData();
        browserData.setAcceptHeader("text/html,application/xhtml+xml,application/xml;q=9,image/webp,img/apng,*/*;q=0.8");
        browserData.setColorDepth(ColorDepth.TwentyFourBit);
        browserData.setIpAddress("123.123.123.123");
        browserData.setJavaEnabled(true);
        browserData.setLanguage("en");
        browserData.setScreenHeight(1080);
        browserData.setScreenWidth(1920);
        browserData.setChallengeWindowSize(ChallengeWindowSize.Windowed_600x400);
        browserData.setTimezone("0");
        browserData.setUserAgent("Mozilla/5.0 (Windows NT 6.1; Win64, x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36");
    }

    // 'APPLY_EXEMPTION' - Amount is less than or equal to 250 EUR (or converted equivalent)
    // The 3D Secure Service will populate the outbound authentication message with the appropriate exemption flag.
    @Test
    public void fullCycle_v2_EOS_ApplyExemption() throws ApiException {
        // check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .execute(Secure3dVersion.TWO);

        assertNotNull(secureEcom);

        if (secureEcom.isEnrolled()) {
            assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());

            // initiate authentication
            ThreeDSecure initAuth =
                    Secure3dService
                            .initiateAuthentication(card, secureEcom)
                            .withAmount(new BigDecimal("10.01"))
                            .withCurrency("EUR")
                            .withOrderCreateDate(DateTime.now())
                            .withAddress(billingAddress, AddressType.Billing)
                            .withAddress(shippingAddress, AddressType.Shipping)
                            .withBrowserData(browserData)
                            .withEnableExemptionOptimization(true)
                            .withMethodUrlCompletion(MethodUrlCompletion.Yes)
                            .execute();

            assertNotNull(initAuth);
            assertEquals(ExemptReason.APPLY_EXEMPTION.name(), initAuth.getExemptReason());
            assertEquals(ExemptStatus.TransactionRiskAnalysis, initAuth.getExemptStatus());

            // get authentication data
            secureEcom =
                    Secure3dService
                            .getAuthenticationData()
                            .withServerTransactionId(initAuth.getServerTransactionId())
                            .execute(Secure3dVersion.TWO);

            card.setThreeDSecure(secureEcom);

            if ("AUTHENTICATION_SUCCESSFUL".equals(secureEcom.getStatus())) {
                Transaction response =
                        card
                                .charge(new BigDecimal("10.01"))
                                .withCurrency("EUR")
                                .execute();

                assertNotNull(response);
                assertEquals("00", response.getResponseCode());
            } else {
                fail("Signature verification Assert.Failed.");
            }
        } else {
            fail("Card not enrolled.");
        }
    }

    // 'CONTINUE' - Amount is above 250 EUR and less than or equal to 500 EUR (or converted equivalent)
    // The 3D Secure Service will populate the outbound authentication as normal.
    @Test
    public void fullCycle_v2_EOS_Continue() throws ApiException {
        // check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .execute(Secure3dVersion.TWO);

        assertNotNull(secureEcom);

        if (secureEcom.isEnrolled()) {
            assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());

            // initiate authentication
            ThreeDSecure initAuth =
                    Secure3dService
                            .initiateAuthentication(card, secureEcom)
                            .withAmount(new BigDecimal("300"))
                            .withCurrency("EUR")
                            .withOrderCreateDate(DateTime.now())
                            .withAddress(billingAddress, AddressType.Billing)
                            .withAddress(shippingAddress, AddressType.Shipping)
                            .withBrowserData(browserData)
                            .withEnableExemptionOptimization(true)
                            .withMethodUrlCompletion(MethodUrlCompletion.Yes)
                            .execute();

            assertNotNull(initAuth);
            assertEquals(ExemptReason.CONTINUE.name(), initAuth.getExemptReason());
            assertNull(initAuth.getExemptStatus());

            // get authentication data
            secureEcom =
                    Secure3dService
                            .getAuthenticationData()
                            .withServerTransactionId(initAuth.getServerTransactionId())
                            .execute(Secure3dVersion.TWO);

            card.setThreeDSecure(secureEcom);

            if ("AUTHENTICATION_SUCCESSFUL".equals(secureEcom.getStatus())) {
                Transaction response =
                        card
                                .charge(new BigDecimal("10.01"))
                                .withCurrency("EUR")
                                .execute();

                assertNotNull(response);
                assertEquals("00", response.getResponseCode());
            } else {
                fail("Signature verification Assert.Failed.");
            }
        } else {
            fail("Card not enrolled.");
        }
    }

    // 'FORCE_SECURE' - Amount is above 500 EUR and less than or equal to 750 EUR (or converted equivalent)
    // The 3D Secure Service will populate the outbound authentication message indicating a challenge is mandated.
    // This will always force a challenge to be applied, regardless of test card used.
    @Test
    public void fullCycle_v2_EOS_ForceSecure() throws ApiException {
        // check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .execute(Secure3dVersion.TWO);

        assertNotNull(secureEcom);

        if (secureEcom.isEnrolled()) {
            assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());

            // initiate authentication
            ThreeDSecure initAuth =
                    Secure3dService
                            .initiateAuthentication(card, secureEcom)
                            .withAmount(new BigDecimal("550"))
                            .withCurrency("EUR")
                            .withOrderCreateDate(DateTime.now())
                            .withAddress(billingAddress, AddressType.Billing)
                            .withAddress(shippingAddress, AddressType.Shipping)
                            .withBrowserData(browserData)
                            .withEnableExemptionOptimization(true)
                            .withMethodUrlCompletion(MethodUrlCompletion.Yes)
                            .execute();

            assertNotNull(initAuth);
            assertEquals("CHALLENGE_REQUIRED", initAuth.getStatus());
            assertEquals(ExemptReason.FORCE_SECURE.name(), initAuth.getExemptReason());
            assertNull(initAuth.getExemptStatus());

            // get authentication data
            secureEcom =
                    Secure3dService
                            .getAuthenticationData()
                            .withServerTransactionId(initAuth.getServerTransactionId())
                            .execute(Secure3dVersion.TWO);

            assertNotNull(secureEcom);
            assertEquals("CHALLENGE_REQUIRED", secureEcom.getStatus());
        } else {
            fail("Card not enrolled.");
        }
    }

    // 'BLOCK' - Amount is above 750 EUR (or converted equivalent)
    // The transaction will be blocked, and a 202 Accepted response will be returned.
    @Test
    public void fullCycle_v2_EOS_Block() throws ApiException {
        // check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .execute(Secure3dVersion.TWO);

        assertNotNull(secureEcom);

        if (secureEcom.isEnrolled()) {
            assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());

            try {
                // initiate authentication
                Secure3dService
                        .initiateAuthentication(card, secureEcom)
                        .withAmount(new BigDecimal("800"))
                        .withCurrency("EUR")
                        .withOrderCreateDate(DateTime.now())
                        .withAddress(billingAddress, AddressType.Billing)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withBrowserData(browserData)
                        .withEnableExemptionOptimization(true)
                        .execute();
            } catch (GatewayException ex) {
                String message = ex.getMessage().replace("\n", "").replace("\r", "").replace("\"", "'");
                assertEquals("Status code: 202 - {  'eos_reason' : 'Blocked by Transaction Risk Analysis.'}", message);
            }
        } else {
            fail("Card not enrolled.");
        }
    }

}

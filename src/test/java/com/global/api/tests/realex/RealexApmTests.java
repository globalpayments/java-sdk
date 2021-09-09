package com.global.api.tests.realex;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.AlternativePaymentType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.paymentMethods.AlternatePaymentMethod;
import com.global.api.serviceConfigs.GatewayConfig;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RealexApmTests {

    @Before
    public void Init() throws ConfigurationException {
        GatewayConfig config = new GatewayConfig();

        config.setMerchantId("heartlandgpsandbox");
        config.setAccountId("hpp");
        config.setSharedSecret("secret");
        config.setRebatePassword("rebate");
        config.setRefundPassword("refund");
        config.setServiceUrl("https://api.sandbox.realexpayments.com/epage-remote.cgi");
        config.setEnableLogging(true);

        ServicesContainer.configureService(config);
    }

    @Test
    public void TestAlternativePaymentMethodForCharge() throws ApiException {
        AlternatePaymentMethod paymentMethodDetails =
                new AlternatePaymentMethod()
                        .setAlternativePaymentMethodType(AlternativePaymentType.TESTPAY)
                        .setReturnUrl("https://www.example.com/returnUrl")
                        .setStatusUpdateUrl("https://www.example.com/statusUrl")
                        .setDescriptor("Test Transaction")
                        .setCountry("DE")
                        .setAccountHolderName("James Mason");

        Transaction response =
                paymentMethodDetails
                        .charge(new BigDecimal("15"))
                        .withCurrency("EUR")
                        .execute();

        assertNotNull(response);
        assertNotNull(response.getAlternativePaymentResponse().getAccountHolderName());
        assertNotNull(response.getAlternativePaymentResponse().getCountry());
        assertNotNull(response.getAlternativePaymentResponse().getPaymentPurpose());
        assertNotNull(response.getAlternativePaymentResponse().getRedirectUrl());
        assertEquals("01", response.getResponseCode());
    }

}
package com.global.api.tests.realex;

import com.global.api.HostedPaymentConfig;
import com.global.api.ServicesConfig;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.services.HostedService;
import com.global.api.tests.realex.hpp.RealexHppClient;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RealexHppTests {
    private HostedService _service;
    private RealexHppClient _client;
    private Address address;

    public RealexHppTests() throws ApiException {
        _client = new RealexHppClient("https://pay.sandbox.realexpayments.com/pay", "secret");

        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setLanguage("GB");
        hostedConfig.setResponseUrl("http://requestb.in/10q2bjb1");
        
        ServicesConfig config = new ServicesConfig();
        config.setMerchantId("heartlandgpsandbox");
        config.setAccountId("hpp");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://api.sandbox.realexpayments.com/epage-remote.cgi");
        config.setHostedPaymentConfig(hostedConfig);
        
        _service = new HostedService(config);
        
        address = new Address();
        address.setPostalCode("123|56");
        address.setCountry("IRELAND");
    }

    @Test
    public void CreditAuth() throws ApiException {
        String json = _service.authorize(new BigDecimal("1"))
                .withCurrency("EUR")
                .withCustomerId("123456")
                .withAddress(address)
                .serialize();
        assertNotNull(json);

        String response = _client.sendRequest(json);
        Transaction parsedResponse = _service.parseResponse(response);
        assertNotNull(response);
        assertEquals("00", parsedResponse.getResponseCode());
    }

    @Test
    public void CreditSale() throws ApiException {
        String json = _service.charge(new BigDecimal("1"))
                .withCurrency("EUR")
                .withCustomerId("123456")
                .withAddress(address)
                .serialize();
        assertNotNull(json);

        String response = _client.sendRequest(json);
        Transaction parsedResponse = _service.parseResponse(response);
        assertNotNull(response);
        assertEquals("00", parsedResponse.getResponseCode());
    }

    @Test
    public void CreditVerify() throws ApiException {
        String json = _service.verify()
                .withCurrency("EUR")
                .withCustomerId("123456")
                .withAddress(address)
                .serialize();
        assertNotNull(json);

        String response = _client.sendRequest(json);
        Transaction parsedResponse = _service.parseResponse(response);
        assertNotNull(response);
        assertEquals("00", parsedResponse.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void AuthNoAmount() throws ApiException {
        _service.authorize(null).withCurrency("USD").serialize();
    }

    @Test(expected = BuilderException.class)
    public void AuthNoCurrency() throws ApiException {
        _service.authorize(new BigDecimal("10")).serialize();
    }

    @Test(expected = BuilderException.class)
    public void SaleNoAmount() throws ApiException {
        _service.charge(null).withCurrency("USD").serialize();
    }

    @Test(expected = BuilderException.class)
    public void SaleNoCurrency() throws ApiException {
        _service.charge(new BigDecimal("10")).serialize();
    }

    @Test(expected = BuilderException.class)
    public void VerifyNoCurrency() throws ApiException {
        _service.verify().serialize();
    }

    @Test(expected = BuilderException.class)
    public void VerifyWithAmount() throws ApiException {
        _service.verify().withAmount(new BigDecimal("10")).serialize();
    }
}

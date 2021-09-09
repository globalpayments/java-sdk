package com.global.api.tests.realex;

import com.global.api.serviceConfigs.GatewayConfig;
import com.global.api.serviceConfigs.HostedPaymentConfig;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.services.HostedService;
import com.global.api.utils.JsonDoc;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RealexHppResponseTests {
    private HostedService _service;

    public RealexHppResponseTests() throws ApiException {
        HostedPaymentConfig hostedConfig = new HostedPaymentConfig();
        hostedConfig.setLanguage("GB");
        hostedConfig.setResponseUrl("http://requestb.in/10q2bjb1");

        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("heartlandgpsandbox");
        config.setAccountId("hpp");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://api.sandbox.realexpayments.com/epage-remote.cgi");
        config.setHostedPaymentConfig(hostedConfig);

        _service = new HostedService(config);
    }

    @Test
    public void basicResponse() throws ApiException {
        String responseJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"843680654f377bfa845387fdbace35acc9d95778\", \"RESULT\": \"00\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"AUTHCODE\": \"12345\", \"CARD_PAYMENT_BUTTON\": \"Place Order\", \"AVSADDRESSRESULT\": \"M\", \"AVSPOSTCODERESULT\": \"M\", \"BATCHID\": \"445196\", \"MESSAGE\": \"[ test system ] Authorised\", \"PASREF\": \"15011597872195765\", \"CVNRESULT\": \"M\"}";
        Transaction response = _service.parseResponse(responseJson, false);

        assertEquals("12345", response.getAuthorizationCode());
        assertEquals(new BigDecimal("1999"), response.getAuthorizedAmount());
        assertEquals("M", response.getAvsResponseCode());
        assertEquals("GTI5Yxb0SumL_TkDMCAxQA", response.getOrderId());
        assertEquals("00", response.getResponseCode());
        assertEquals("[ test system ] Authorised", response.getResponseMessage());
        assertEquals("15011597872195765", response.getTransactionId());
        assertEquals("M", response.getCvnResponseCode());
    }

    @Test
    public void standardResponse() throws ApiException {
        String responseJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"843680654f377bfa845387fdbace35acc9d95778\", \"RESULT\": \"00\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"AUTHCODE\": \"12345\", \"SHIPPING_CODE\": \"654|123\", \"SHIPPING_CO\": \"GB\", \"BILLING_CODE\": \"50001\", \"BILLING_CO\": \"US\", \"CARD_PAYMENT_BUTTON\": \"Place Order\", \"AVSADDRESSRESULT\": \"M\", \"AVSPOSTCODERESULT\": \"M\", \"BATCHID\": \"445196\", \"DCC_ENABLE\": \"1\", \"HPP_FRAUDFILTER_MODE\": \"PASSIVE\", \"HPP_LANG\": \"EN\", \"MESSAGE\": \"[ test system ] Authorised\", \"PASREF\": \"15011597872195765\", \"CVNRESULT\": \"M\", \"HPP_FRAUDFILTER_RESULT\": \"PASS\", \"COMMENT1\": \"Mobile Channel\", \"COMMENT2\": \"Down Payment\", \"ECI\": \"5\", \"XID\": \"vJ9NXpFueXsAqeb4iAbJJbe+66s=\", \"CAVV\": \"AAACBUGDZYYYIgGFGYNlAAAAAAA=\", \"CARDDIGITS\": \"424242xxxx4242\", \"CARDTYPE\": \"VISA\", \"EXPDATE\": \"1025\", \"CHNAME\": \"James Mason\"}";
        Transaction response = _service.parseResponse(responseJson, false);

        assertEquals("12345", response.getAuthorizationCode());
        assertEquals(new BigDecimal("1999"), response.getAuthorizedAmount());
        assertEquals("M", response.getAvsResponseCode());
        assertEquals("GTI5Yxb0SumL_TkDMCAxQA", response.getOrderId());
        assertEquals("00", response.getResponseCode());
        assertEquals("[ test system ] Authorised", response.getResponseMessage());
        assertEquals("15011597872195765", response.getTransactionId());
        assertEquals("M", response.getCvnResponseCode());
    }

    @Test(expected = ApiException.class)
    public void incorrectHashAuthCode() throws ApiException {
        String responseJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"841680654f377bfa845387fdbace35acc9d95778\", \"RESULT\": \"00\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"AUTHCODE\": \"54321\", \"CARD_PAYMENT_BUTTON\": \"Place Order\", \"AVSADDRESSRESULT\": \"M\", \"AVSPOSTCODERESULT\": \"M\", \"BATCHID\": \"445196\", \"MESSAGE\": \"[ test system ] Authorised\", \"PASREF\": \"15011597872195765\", \"CVNRESULT\": \"M\"}";
        _service.parseResponse(responseJson, false);
    }

    @Test(expected = ApiException.class)
    public void incorrectSharedSecret() throws ApiException {
        String responseJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"841680654f377bfa845387fdbace35acc9d95778\", \"RESULT\": \"00\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"AUTHCODE\": \"12345\", \"CARD_PAYMENT_BUTTON\": \"Place Order\", \"AVSADDRESSRESULT\": \"M\", \"AVSPOSTCODERESULT\": \"M\", \"BATCHID\": \"445196\", \"MESSAGE\": \"[ test system ] Authorised\", \"PASREF\": \"15011597872195765\", \"CVNRESULT\": \"M\"}";
        _service.parseResponse(responseJson, false);
    }

    @Test
    public void fraudCheckBlock() throws ApiException {
        String responseJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"7a2fff26deac60f470b5de22c63151f530a22805\", \"RESULT\": \"107\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"AUTHCODE\": \"\", \"SHIPPING_CODE\": \"654|123\", \"SHIPPING_CO\": \"GB\", \"BILLING_CODE\": \"50001\", \"BILLING_CO\": \"US\", \"CARD_PAYMENT_BUTTON\": \"Place Order\", \"AVSADDRESSRESULT\": \"U\", \"AVSPOSTCODERESULT\": \"U\", \"BATCHID\": \"-1\", \"MESSAGE\": \"Fails Fraud Checks\", \"PASREF\": \"15016893697197771\", \"CVNRESULT\": \"U\", \"HPP_FRAUDFILTER_RESULT\": \"BLOCK\", \"HPP_FRAUDFILTER_RULE_56257838-4590-4227-b946-11e061fb15fe\": \"BLOCK\", \"HPP_FRAUDFILTER_RULE_NAME\": \"Cardholder Name Check\"}";
        Transaction response = _service.parseResponse(responseJson, false);

        assertEquals("107", response.getResponseCode());
        assertEquals("Fails Fraud Checks", response.getResponseMessage());
        assertEquals("", response.getAuthorizationCode());
        assertEquals(new BigDecimal("1999"), response.getAuthorizedAmount());
        assertEquals("U", response.getAvsResponseCode());
        assertEquals("GTI5Yxb0SumL_TkDMCAxQA", response.getOrderId());
        assertEquals("15016893697197771", response.getTransactionId());
        assertEquals("U", response.getCvnResponseCode());
    }

    @Test
    public void declinedTransaction() throws ApiException {
        String responseJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"41a73ae4f563c60a0da840af36f078fde1beb4e0\", \"RESULT\": \"101\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"AUTHCODE\": \"\", \"CARD_PAYMENT_BUTTON\": \"Place Order\", \"AVSADDRESSRESULT\": \"M\", \"AVSPOSTCODERESULT\": \"M\", \"BATCHID\": \"-1\", \"MESSAGE\": \"[ test system ] DECLINED\", \"PASREF\": \"15016900517792053\", \"CVNRESULT\": \"N\"}";
        Transaction response = _service.parseResponse(responseJson, false);

        assertEquals("101", response.getResponseCode());
        assertEquals("[ test system ] DECLINED", response.getResponseMessage());
        assertEquals("", response.getAuthorizationCode());
        assertEquals(new BigDecimal("1999"), response.getAuthorizedAmount());
        assertEquals("M", response.getAvsResponseCode());
        assertEquals("GTI5Yxb0SumL_TkDMCAxQA", response.getOrderId());
        assertEquals("15016900517792053", response.getTransactionId());
        assertEquals("N", response.getCvnResponseCode());
    }

    @Test
    public void referralBTransaction() throws ApiException {
        String responseJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"1de7ac3d4128719fe1d7a9217b9a1cce02e2b1c9\", \"RESULT\": \"102\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"AUTHCODE\": \"\", \"CARD_PAYMENT_BUTTON\": \"Place Order\", \"AVSADDRESSRESULT\": \"M\", \"AVSPOSTCODERESULT\": \"M\", \"BATCHID\": \"-1\", \"MESSAGE\": \"[ test system ] REFERRAL B\", \"PASREF\": \"15017567624469248\", \"CVNRESULT\": \"M\"}";
        Transaction response = _service.parseResponse(responseJson, false);

        assertEquals("102", response.getResponseCode());
        assertEquals("[ test system ] REFERRAL B", response.getResponseMessage());
        assertEquals("", response.getAuthorizationCode());
        assertEquals(new BigDecimal("1999"), response.getAuthorizedAmount());
        assertEquals("GTI5Yxb0SumL_TkDMCAxQA", response.getOrderId());
        assertEquals("15017567624469248", response.getTransactionId());
    }

    @Test
    public void referralATransaction() throws ApiException {
        String responseJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"f6ef9e8d2c463ae94e07009954dc83527125bb7e\", \"RESULT\": \"103\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"AUTHCODE\": \"\", \"CARD_PAYMENT_BUTTON\": \"Place Order\", \"AVSADDRESSRESULT\": \"M\", \"AVSPOSTCODERESULT\": \"M\", \"BATCHID\": \"-1\", \"MESSAGE\": \"[ test system ] REFERRAL A\", \"PASREF\": \"15017567624469248\", \"CVNRESULT\": \"M\"}";
        Transaction response = _service.parseResponse(responseJson, false);

        assertEquals("103", response.getResponseCode());
        assertEquals("[ test system ] REFERRAL A", response.getResponseMessage());
        assertEquals("", response.getAuthorizationCode());
        assertEquals(new BigDecimal("1999"), response.getAuthorizedAmount());
        assertEquals("GTI5Yxb0SumL_TkDMCAxQA", response.getOrderId());
        assertEquals("15017567624469248", response.getTransactionId());
    }

    @Test(expected = ApiException.class)
    public void incorrectResponseHash() throws ApiException {
        String responseJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"841680654f377bfa845387fdbace35acc9d95778\", \"RESULT\": \"00\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"AUTHCODE\": \"12345\", \"CARD_PAYMENT_BUTTON\": \"Place Order\", \"AVSADDRESSRESULT\": \"M\", \"AVSPOSTCODERESULT\": \"M\", \"BATCHID\": \"445196\", \"MESSAGE\": \"[ test system ] Authorised\", \"PASREF\": \"15011597872195765\", \"CVNRESULT\": \"M\"}";
        _service.parseResponse(responseJson, false);
    }

    @Test(expected = ApiException.class)
    public void incorrectResultCode() throws ApiException {
        String responseJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"841680654f377bfa845387fdbace35acc9d95778\", \"RESULT\": \"101\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"AUTHCODE\": \"54321\", \"CARD_PAYMENT_BUTTON\": \"Place Order\", \"AVSADDRESSRESULT\": \"M\", \"AVSPOSTCODERESULT\": \"M\", \"BATCHID\": \"445196\", \"MESSAGE\": \"[ test system ] Authorised\", \"PASREF\": \"15011597872195765\", \"CVNRESULT\": \"M\"}";
        _service.parseResponse(responseJson, false);
    }

    @Test(expected = ApiException.class)
    public void incorrectMerchantId() throws ApiException {
        String responseJson = "{ \"MERCHANT_ID\": \"Merchant Id\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"841680654f377bfa845387fdbace35acc9d95778\", \"RESULT\": \"00\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"AUTHCODE\": \"54321\", \"CARD_PAYMENT_BUTTON\": \"Place Order\", \"AVSADDRESSRESULT\": \"M\", \"AVSPOSTCODERESULT\": \"M\", \"BATCHID\": \"445196\", \"MESSAGE\": \"[ test system ] Authorised\", \"PASREF\": \"15011597872195765\", \"CVNRESULT\": \"M\"}";
        _service.parseResponse(responseJson, false);
    }

    @Test(expected = ApiException.class)
    public void incorrectTimestamp() throws ApiException {
        String responseJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"TIMESTAMP\": \"20170803151925\", \"SHA1HASH\": \"841680654f377bfa845387fdbace35acc9d95778\", \"RESULT\": \"00\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"AUTHCODE\": \"54321\", \"CARD_PAYMENT_BUTTON\": \"Place Order\", \"AVSADDRESSRESULT\": \"M\", \"AVSPOSTCODERESULT\": \"M\", \"BATCHID\": \"445196\", \"MESSAGE\": \"[ test system ] Authorised\", \"PASREF\": \"15011597872195765\", \"CVNRESULT\": \"M\"}";
        _service.parseResponse(responseJson, false);
    }

    @Test(expected = ApiException.class)
    public void incorrectOrderId() throws ApiException {
        String responseJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"N6qsk4kYRZihmPrTXWYS6g\", \"AMOUNT\": \"1999\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"841680654f377bfa845387fdbace35acc9d95778\", \"RESULT\": \"00\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"AUTHCODE\": \"54321\", \"CARD_PAYMENT_BUTTON\": \"Place Order\", \"AVSADDRESSRESULT\": \"M\", \"AVSPOSTCODERESULT\": \"M\", \"BATCHID\": \"445196\", \"MESSAGE\": \"[ test system ] Authorised\", \"PASREF\": \"15011597872195765\", \"CVNRESULT\": \"M\"}";
        _service.parseResponse(responseJson, false);
    }

    @Test(expected = ApiException.class)
    public void incorrectMessage() throws ApiException {
        String responseJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"841680654f377bfa845387fdbace35acc9d95778\", \"RESULT\": \"00\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"AUTHCODE\": \"54321\", \"CARD_PAYMENT_BUTTON\": \"Place Order\", \"AVSADDRESSRESULT\": \"M\", \"AVSPOSTCODERESULT\": \"M\", \"BATCHID\": \"445196\", \"MESSAGE\": \"[ test system ] DECLINED\", \"PASREF\": \"15011597872195765\", \"CVNRESULT\": \"M\"}";
        _service.parseResponse(responseJson, false);
    }

    @Test(expected = ApiException.class)
    public void incorrectPasref() throws ApiException {
        String responseJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"841680654f377bfa845387fdbace35acc9d95778\", \"RESULT\": \"00\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"AUTHCODE\": \"54321\", \"CARD_PAYMENT_BUTTON\": \"Place Order\", \"AVSADDRESSRESULT\": \"M\", \"AVSPOSTCODERESULT\": \"M\", \"BATCHID\": \"445196\", \"MESSAGE\": \"[ test system ] Authorised\", \"PASREF\": \"15011596872195765\", \"CVNRESULT\": \"M\"}";
        _service.parseResponse(responseJson, false);
    }

    @Test
    public void basicEncodedResponse() throws ApiException {
        String responseJson = "{ \"MERCHANT_ID\": \"TWVyY2hhbnRJZA==\", \"ACCOUNT\": \"aW50ZXJuZXQ=\", \"ORDER_ID\": \"R1RJNVl4YjBTdW1MX1RrRE1DQXhRQQ==\", \"AMOUNT\": \"MTk5OQ==\", \"TIMESTAMP\": \"MjAxNzA3MjUxNTQ4MjQ=\", \"SHA1HASH\": \"ODQzNjgwNjU0ZjM3N2JmYTg0NTM4N2ZkYmFjZTM1YWNjOWQ5NTc3OA==\", \"RESULT\": \"MDA=\",  \"MERCHANT_RESPONSE_URL\": \"aHR0cHM6Ly93d3cuZXhhbXBsZS5jb20vcmVzcG9uc2U=\", \"AUTHCODE\": \"MTIzNDU=\", \"CARD_PAYMENT_BUTTON\": \"UGxhY2UgT3JkZXI=\", \"AVSADDRESSRESULT\": \"TQ==\", \"AVSPOSTCODERESULT\": \"TQ==\", \"BATCHID\": \"NDQ1MTk2\", \"MESSAGE\": \"WyB0ZXN0IHN5c3RlbSBdIEF1dGhvcmlzZWQ=\", \"PASREF\": \"MTUwMTE1OTc4NzIxOTU3NjU=\", \"CVNRESULT\": \"TQ==\"}";
        Transaction response = _service.parseResponse(responseJson, true);

        assertEquals("12345", response.getAuthorizationCode());
        assertEquals(new BigDecimal("1999"), response.getAuthorizedAmount());
        assertEquals("M", response.getAvsResponseCode());
        assertEquals("GTI5Yxb0SumL_TkDMCAxQA", response.getOrderId());
        assertEquals("00", response.getResponseCode());
        assertEquals("[ test system ] Authorised", response.getResponseMessage());
        assertEquals("15011597872195765", response.getTransactionId());
        assertEquals("M", response.getCvnResponseCode());
    }

    @Test(expected = ApiException.class)
    public void incorrectEncodedResponse() throws ApiException {
        String responseJson = "{ \"MERCHANT_ID\": \"TWVyY2hhbnRJZA==\", \"ACCOUNT\": \"aW50ZXJuZXQ=\", \"ORDER_ID\": \"R1RJNVl4YjBTdW1MX1RrRE1DQXhRQQ==\", \"AMOUNT\": \"MTk5OQ==\", \"TIMESTAMP\": \"MjAxNzA3MjUxNTQ4MjQ=\", \"SHA1HASH\": \"ODQzNjgwNjU0ZjM3N2JmYTg0NTM4N2ZkYmFjZTM1YWNjOWQ5NTc3OA==\", \"RESULT\": \"MDA=\",  \"MERCHANT_RESPONSE_URL\": \"aHR0cHM6Ly93d3cuZXhhbXBsZS5jb20vcmVzcG9uc2U=\", \"AUTHCODE\": \"MTIzNDU=\", \"CARD_PAYMENT_BUTTON\": \"UGxhY2UgT3JkZXI=\", \"AVSADDRESSRESULT\": \"TQ==\", \"AVSPOSTCODERESULT\": \"TQ==\", \"BATCHID\": \"NDQ1MTk2\", \"MESSAGE\": \"WyB0ZXN0IHN5c3RlbSBdIEF1dGhvcmlzZWQ=\", \"PASREF\": \"MTUwMTE1OTc4NzIxOTU3NjU=\", \"CVNRESULT\": \"TQ==\"}";
        _service.parseResponse(responseJson, false);
    }

    @Test(expected = ApiException.class)
    public void incorrectNonEncodedResponse() throws ApiException {
        String responseJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"843680654f377bfa845387fdbace35acc9d95778\", \"RESULT\": \"00\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"AUTHCODE\": \"12345\", \"CARD_PAYMENT_BUTTON\": \"Place Order\", \"AVSADDRESSRESULT\": \"M\", \"AVSPOSTCODERESULT\": \"M\", \"BATCHID\": \"445196\", \"MESSAGE\": \"[ test system ] Authorised\", \"PASREF\": \"15011597872195765\", \"CVNRESULT\": \"M\"}";
        _service.parseResponse(responseJson, true);
    }

    @Test
    public void verifyResponseValues() throws ApiException {
        String responseJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"843680654f377bfa845387fdbace35acc9d95778\", \"RESULT\": \"00\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"AUTHCODE\": \"12345\", \"CARD_PAYMENT_BUTTON\": \"Place Order\", \"AVSADDRESSRESULT\": \"M\", \"AVSPOSTCODERESULT\": \"M\", \"BATCHID\": \"445196\", \"MESSAGE\": \"[ test system ] Authorised\", \"PASREF\": \"15011597872195765\", \"CVNRESULT\": \"M\"}";
        Transaction response = _service.parseResponse(responseJson, false);

        JsonDoc doc = JsonDoc.parse(responseJson);
        assertNotNull(response.getResponseValues());
        assertEquals(doc.getKeys().size(), response.getResponseValues().size());
        for(String key: doc.getKeys()) {
            String strValue = doc.getString(key);
            assertEquals(strValue, response.getResponseValues().get(key));
        }
    }

    @Test
    public void standardResponseWithTimestamp() throws ApiException {
        String responseJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"843680654f377bfa845387fdbace35acc9d95778\", \"RESULT\": \"00\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"AUTHCODE\": \"12345\", \"SHIPPING_CODE\": \"654|123\", \"SHIPPING_CO\": \"GB\", \"BILLING_CODE\": \"50001\", \"BILLING_CO\": \"US\", \"CARD_PAYMENT_BUTTON\": \"Place Order\", \"AVSADDRESSRESULT\": \"M\", \"AVSPOSTCODERESULT\": \"M\", \"BATCHID\": \"445196\", \"DCC_ENABLE\": \"1\", \"HPP_FRAUDFILTER_MODE\": \"PASSIVE\", \"HPP_LANG\": \"EN\", \"MESSAGE\": \"[ test system ] Authorised\", \"PASREF\": \"15011597872195765\", \"CVNRESULT\": \"M\", \"HPP_FRAUDFILTER_RESULT\": \"PASS\", \"COMMENT1\": \"Mobile Channel\", \"COMMENT2\": \"Down Payment\", \"ECI\": \"5\", \"XID\": \"vJ9NXpFueXsAqeb4iAbJJbe+66s=\", \"CAVV\": \"AAACBUGDZYYYIgGFGYNlAAAAAAA=\", \"CARDDIGITS\": \"424242xxxx4242\", \"CARDTYPE\": \"VISA\", \"EXPDATE\": \"1025\", \"CHNAME\": \"James Mason\"}";
        Transaction response = _service.parseResponse(responseJson, false);

        assertEquals("12345", response.getAuthorizationCode());
        assertEquals(new BigDecimal("1999"), response.getAuthorizedAmount());
        assertEquals("M", response.getAvsResponseCode());
        assertEquals("GTI5Yxb0SumL_TkDMCAxQA", response.getOrderId());
        assertEquals("00", response.getResponseCode());
        assertEquals("[ test system ] Authorised", response.getResponseMessage());
        assertEquals("15011597872195765", response.getTransactionId());
        assertEquals("M", response.getCvnResponseCode());
        assertEquals("20170725154824", response.getTimestamp());
    }

    @Test
    public void standardResponseWithMultiAutoSettle() throws ApiException {
        String responseJson = "{ \"MERCHANT_ID\": \"MerchantId\", \"ACCOUNT\": \"internet\", \"ORDER_ID\": \"GTI5Yxb0SumL_TkDMCAxQA\", \"AMOUNT\": \"1999\", \"TIMESTAMP\": \"20170725154824\", \"SHA1HASH\": \"843680654f377bfa845387fdbace35acc9d95778\", \"RESULT\": \"00\",  \"MERCHANT_RESPONSE_URL\": \"https://www.example.com/response\", \"AUTHCODE\": \"12345\", \"SHIPPING_CODE\": \"654|123\", \"SHIPPING_CO\": \"GB\", \"BILLING_CODE\": \"50001\", \"BILLING_CO\": \"US\", \"CARD_PAYMENT_BUTTON\": \"Place Order\", \"AVSADDRESSRESULT\": \"M\", \"AVSPOSTCODERESULT\": \"M\", \"BATCHID\": \"445196\", \"DCC_ENABLE\": \"1\", \"HPP_FRAUDFILTER_MODE\": \"PASSIVE\", \"HPP_LANG\": \"EN\", \"MESSAGE\": \"[ test system ] Authorised\", \"PASREF\": \"15011597872195765\", \"CVNRESULT\": \"M\", \"HPP_FRAUDFILTER_RESULT\": \"PASS\", \"COMMENT1\": \"Mobile Channel\", \"COMMENT2\": \"Down Payment\", \"ECI\": \"5\", \"XID\": \"vJ9NXpFueXsAqeb4iAbJJbe+66s=\", \"CAVV\": \"AAACBUGDZYYYIgGFGYNlAAAAAAA=\", \"CARDDIGITS\": \"424242xxxx4242\", \"CARDTYPE\": \"VISA\", \"EXPDATE\": \"1025\", \"CHNAME\": \"James Mason\", \"AUTO_SETTLE_FLAG\": \"MULTI\"}";
        Transaction response = _service.parseResponse(responseJson, false);

        assertEquals("12345", response.getAuthorizationCode());
        assertEquals("MULTI", response.getAutoSettleFlag());
        assertEquals(new BigDecimal("1999"), response.getAuthorizedAmount());
        assertEquals("M", response.getAvsResponseCode());
        assertEquals("GTI5Yxb0SumL_TkDMCAxQA", response.getOrderId());
        assertEquals("00", response.getResponseCode());
        assertEquals("[ test system ] Authorised", response.getResponseMessage());
        assertEquals("15011597872195765", response.getTransactionId());
        assertEquals("M", response.getCvnResponseCode());
        assertEquals("20170725154824", response.getTimestamp());
    }
}

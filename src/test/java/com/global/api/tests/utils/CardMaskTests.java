package com.global.api.tests.utils;

import com.global.api.utils.StringUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class CardMaskTests {

    @Test
    public void MaskRealexRequest() {
        String realexRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><request timestamp=\"20180523030836\" type=\"auth\"><merchantid>heartlandgpsandbox</merchantid><account>api</account><orderid>7C-8YKN9QYSIK-BxVs2eXw</orderid><amount currency=\"USD\">1500</amount><card><number>4111111111111111</number><expdate>1225</expdate><chname>Joe Smith</chname><type>VISA</type><cvn><number>123</number><presind>1</presind></cvn></card><sha1hash>2cbfa21733e52c28f0772f1b85b2e6478cfad7bc</sha1hash><autosettle flag=\"1\"/></request>";
        String result = StringUtils.mask(realexRequest);
        assertTrue(result.contains("411111XXXXXX1111"));
    }

    @Test
    public void MaskPorticoManualRequest() {
        String porticoManualRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><soap:Body><PosRequest xmlns=\"http://Hps.Exchange.PosGateway\"><Ver1.0><Header><SecretAPIKey>skapi_cert_MTeSAQAfG1UA9qQDrzl-kz4toXvARyieptFwSKP24w</SecretAPIKey><PosReqDT>2018-05-23T03:27:24.526-04:00</PosReqDT></Header><Transaction><CreditSale><Block1><AllowDup>Y</AllowDup><AllowPartialAuth>N</AllowPartialAuth><Amt>15</Amt><CardData><ManualEntry><CardNbr>4111111111111111</CardNbr><ExpMonth>12</ExpMonth><ExpYear>2025</ExpYear><CVV2>123</CVV2><ReaderPresent>N</ReaderPresent><DE22_CardPresence>N</DE22_CardPresence></ManualEntry><TokenRequest>N</TokenRequest></CardData></Block1></CreditSale></Transaction></Ver1.0></PosRequest></soap:Body></soap:Envelope>";
        String result = StringUtils.mask(porticoManualRequest);
        assertTrue(result.contains("411111XXXXXX1111"));
    }

    @Test
    public void MaskPorticoSwipedRequest() {
        String porticoSwipedRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><soap:Body><PosRequest xmlns=\"http://Hps.Exchange.PosGateway\"><Ver1.0><Header><SecretAPIKey>skapi_cert_MTeSAQAfG1UA9qQDrzl-kz4toXvARyieptFwSKP24w</SecretAPIKey><PosReqDT>2018-05-23T03:39:06.575-04:00</PosReqDT></Header><Transaction><CreditSale><Block1><AllowDup>Y</AllowDup><AllowPartialAuth>N</AllowPartialAuth><Amt>15</Amt><CardData><TrackData method=\"swipe\">&lt;E1050711%B4012000000000016^VI TEST CREDIT^251200000000000000000000?|LO04K0WFOmdkDz0um+GwUkILL8ZZOP6Zc4rCpZ9+kg2T3JBT4AEOilWTI|+++++++Dbbn04ekG|11;4012000000000016=25120000000000000000?|1u2F/aEhbdoPixyAPGyIDv3gBfF|+++++++Dbbn04ekG|00|||/wECAQECAoFGAgEH2wYcShV78RZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0PX50qfj4dt0lu9oFBESQQNkpoxEVpCW3ZKmoIV3T93zphPS3XKP4+DiVlM8VIOOmAuRrpzxNi0TN/DWXWSjUC8m/PI2dACGdl/hVJ/imfqIs68wYDnp8j0ZfgvM26MlnDbTVRrSx68Nzj2QAgpBCHcaBb/FZm9T7pfMr2Mlh2YcAt6gGG1i2bJgiEJn8IiSDX5M2ybzqRT86PCbKle/XCTwFFe1X|&gt;;</TrackData><EncryptionData><Version>01</Version></EncryptionData><TokenRequest>N</TokenRequest></CardData></Block1></CreditSale></Transaction></Ver1.0></PosRequest></soap:Body></soap:Envelope>";
        String result = StringUtils.mask(porticoSwipedRequest);
        assertTrue(result.contains("%B401200XXXXXX0016^VI TEST CREDIT^251200000000000000000000?"));
        assertTrue(result.contains(";401200XXXXXX0016=25120000000000000000?"));
    }

    @Test
    public void MaskPayPlanRequest() {
        String payplanRequest = "{\"preferredPayment\":false,\"customerKey\":\"77133\",\"stateProvince\":\"NJ\",\"card\":{\"expMon\":12,\"expYear\":2025,\"number\":\"4111110000001111\"},\"paymentMethodIdentifier\":\"20180523-GlobalApi-Credit\",\"zipPostalCode\":\"12345\",\"addressLine1\":\"987 Elm St\",\"nameOnAccount\":\"Bill Johnson\",\"city\":\"Princeton\",\"country\":\"USA\"}";
        String result = StringUtils.mask(payplanRequest);
        assertTrue(result.contains("411111XXXXXX1111"));
    }

    @Test
    public void Test_CardMask_Visa() {
        String cardNumber = "4263970000005262";
        String expected = "426397XXXXXX5262";
        String result = StringUtils.mask(cardNumber);
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void Test_CardMask_Master() {
        String cardNumber = "5425 2300 0000 4415";
        String expected = "542523XXXXXX4415";
        String result = StringUtils.mask(cardNumber);
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void Test_CardMask_Amex() {
        String cardNumber = "3741-0100-000-0608";
        String expected = "374101XXXXX0608";
        String result = StringUtils.mask(cardNumber);
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void Test_CardMask_Discover() {
        String cardNumber = "6011 0000 0000 0087";
        String expected = "601100XXXXXX0087";
        String result = StringUtils.mask(cardNumber);
        assertNotNull(result);
        assertEquals(expected, result);
    }
}

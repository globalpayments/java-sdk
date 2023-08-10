package com.global.api.tests.utils;

import com.global.api.entities.exceptions.GatewayException;
import com.global.api.gateways.GpApiConnector;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

public class ParseDateTimeTests {


    @Test
    public void testNullValue() throws GatewayException {
        DateTime result = GpApiConnector.parseGpApiDateTime(null);
        Assert.assertNull(result);
    }

    @Test
    public void testNonValidValue() {
        try {
            GpApiConnector.parseGpApiDateTime("non-valid-date");
            Assert.fail("It should throw an Exception");
        } catch (Exception ex) {
            // fine
            Assert.assertTrue(ex instanceof GatewayException);
            Assert.assertEquals("DateTime format is not supported.", ex.getMessage());
            Assert.assertTrue(ex.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testNonValidValueForOldPattern_9() throws GatewayException {
        try {
            // old wrong pattern 9: // "yyyy-mm-dd"
            GpApiConnector.parseGpApiDateTime("2023-59-01");
            Assert.fail("It should throw an Exception");
        } catch (Exception ex) {
            // fine
            Assert.assertTrue(ex instanceof GatewayException);
            Assert.assertEquals("DateTime format is not supported.", ex.getMessage());
            Assert.assertTrue(ex.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testForDateTimePattern() throws GatewayException {
        // "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        DateTime parsedResult = GpApiConnector.parseGpApiDateTime("2023-05-01T16:51:12.345Z");
        Assert.assertEquals(new DateTime(2023, 5, 1, 16, 51, 12, 345), parsedResult);
    }

    @Test
    public void testForDateTimePattern_2() throws GatewayException {
        // "yyyy-MM-dd'T'HH:mm:ss.SSS"
        DateTime parsedResult = GpApiConnector.parseGpApiDateTime("2023-05-01T16:51:12.345");
        Assert.assertEquals(new DateTime(2023, 5, 1, 16, 51, 12, 345), parsedResult);
    }

    @Test
    public void testForDateTimePattern_3() throws GatewayException {
        // "yyyy-MM-dd'T'HH:mm:ss'Z'"
        DateTime parsedResult = GpApiConnector.parseGpApiDateTime("2023-05-01T16:51:12Z");
        Assert.assertEquals(new DateTime(2023, 5, 1, 16, 51, 12), parsedResult);
    }

    @Test
    public void testForDateTimePattern_4() throws GatewayException {
        // "yyyy-MM-dd'T'HH:mm:ss"
        DateTime parsedResult = GpApiConnector.parseGpApiDateTime("2023-05-01T16:51:12");
        Assert.assertEquals(new DateTime(2023, 5, 1, 16, 51, 12), parsedResult);
    }

    @Test
    public void testForDateTimePattern_5() throws GatewayException {
        // "yyyy-MM-dd'T'HH:mm"
        DateTime parsedResult = GpApiConnector.parseGpApiDateTime("2023-05-01T16:51");
        Assert.assertEquals(new DateTime(2023, 5, 1, 16, 51), parsedResult);
    }

    @Test
    public void testForDateTimePattern_6() throws GatewayException {
        // "yyyy-MM-dd"
        DateTime parsedResult = GpApiConnector.parseGpApiDateTime("2023-05-01");
        Assert.assertEquals(new DateTime(2023, 5, 1, 0, 0), parsedResult);
    }

    @Test
    public void testForDateTimePattern_7() throws GatewayException {
        // "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'"
        DateTime parsedResult = GpApiConnector.parseGpApiDateTime("2023-05-01T16:51:12.123456789Z");
        Assert.assertEquals(new DateTime(2023, 5, 1, 16, 51, 12, 123), parsedResult);
    }

    @Test
    public void testForDateTimePattern_8() throws GatewayException {
        // "yyyy-MM-dd'T'HH:mm:ss+SS:SS"
        DateTime parsedResult = GpApiConnector.parseGpApiDateTime("2023-05-01T16:51:12+34:56");
        Assert.assertEquals(new DateTime(2023, 5, 1, 16, 51, 12, 560), parsedResult);
    }

    @Test
    public void testForDateTimePattern_10() throws GatewayException {
        // "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS"
        DateTime parsedResult = GpApiConnector.parseGpApiDateTime("2023-07-20T18:22:49.0710761");
        Assert.assertEquals(new DateTime(2023, 7, 20, 18, 22, 49, 71), parsedResult);
    }

}

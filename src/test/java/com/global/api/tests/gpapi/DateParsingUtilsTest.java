package com.global.api.tests.gpapi;

import com.global.api.entities.exceptions.GatewayException;
import com.global.api.gateways.DateParsingUtils;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DateParsingUtilsTest extends BaseGpApiTest {

    @Test
    public void testNullValue() throws GatewayException {
        DateTime result = DateParsingUtils.parseDateTime(null);
        assertNull(result);
    }

    @Test
    public void testNonValidValue() {
        try {
            DateParsingUtils.parseDateTime("non-valid-date");
           fail("It should throw an Exception");
        } catch (Exception ex) {
            // fine
           assertTrue(ex instanceof GatewayException);
           assertEquals("DateTime format is not supported.", ex.getMessage());
           assertTrue(ex.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testNonValidValueForOldPattern_9() {
        try {
            // old wrong pattern 9: // "yyyy-mm-dd"
            DateParsingUtils.parseDateTime("2023-59-01");
           fail("It should throw an Exception");
        } catch (Exception ex) {
            // fine
            assertTrue(ex instanceof GatewayException);
            assertEquals("DateTime format is not supported.", ex.getMessage());
            assertTrue(ex.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testForDateTimePattern() throws GatewayException {
        // "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        DateTime parsedResult = DateParsingUtils.parseDateTime("2023-05-01T16:51:12.345Z");
        assertEquals(new DateTime(2023, 5, 1, 16, 51, 12, 345), parsedResult);
    }

    @Test
    public void testForDateTimePattern_2() throws GatewayException {
        // "yyyy-MM-dd'T'HH:mm:ss.SSS"
        DateTime parsedResult = DateParsingUtils.parseDateTime("2023-05-01T16:51:12.345");
        assertEquals(new DateTime(2023, 5, 1, 16, 51, 12, 345), parsedResult);
    }

    @Test
    public void testForDateTimePattern_3() throws GatewayException {
        // "yyyy-MM-dd'T'HH:mm:ss'Z'"
        DateTime parsedResult = DateParsingUtils.parseDateTime("2023-05-01T16:51:12Z");
        assertEquals(new DateTime(2023, 5, 1, 16, 51, 12), parsedResult);
    }

    @Test
    public void testForDateTimePattern_4() throws GatewayException {
        // "yyyy-MM-dd'T'HH:mm:ss"
        DateTime parsedResult = DateParsingUtils.parseDateTime("2023-05-01T16:51:12");
        assertEquals(new DateTime(2023, 5, 1, 16, 51, 12), parsedResult);
    }

    @Test
    public void testForDateTimePattern_5() throws GatewayException {
        // "yyyy-MM-dd'T'HH:mm"
        DateTime parsedResult = DateParsingUtils.parseDateTime("2023-05-01T16:51");
        assertEquals(new DateTime(2023, 5, 1, 16, 51), parsedResult);
    }

    @Test
    public void testForDateTimePattern_6() throws GatewayException {
        // "yyyy-MM-dd"
        DateTime parsedResult = DateParsingUtils.parseDateTime("2023-05-01");
        assertEquals(new DateTime(2023, 5, 1, 0, 0), parsedResult);
    }

    @Test
    public void testForDateTimePattern_7() throws GatewayException {
        // "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'"
        DateTime parsedResult = DateParsingUtils.parseDateTime("2023-05-01T16:51:12.123456789Z");
        assertEquals(new DateTime(2023, 5, 1, 16, 51, 12, 123), parsedResult);
    }

    @Test
    public void testForDateTimePattern_8() throws GatewayException {
        // "yyyy-MM-dd'T'HH:mm:ss+SS:SS"
        DateTime parsedResult = DateParsingUtils.parseDateTime("2023-05-01T16:51:12+34:56");
        assertEquals(new DateTime(2023, 5, 1, 16, 51, 12, 560), parsedResult);
    }

    @Test
    public void testForDateTimePattern_10() throws GatewayException {
        // "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS"
        DateTime parsedResult = DateParsingUtils.parseDateTime("2023-07-20T18:22:49.0710761");
        assertEquals(new DateTime(2023, 7, 20, 18, 22, 49, 71), parsedResult);
    }

}

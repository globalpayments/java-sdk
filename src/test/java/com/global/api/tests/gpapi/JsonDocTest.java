package com.global.api.tests.gpapi;

import com.global.api.utils.JsonDoc;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JsonDocTest {

    @Test
    public void testGetString_withNullValue() {

        String aValue = null;
        JsonDoc data = new JsonDoc();
        data.set("lala", aValue, true);

        assertThrows(NullPointerException.class, () -> {
            data.getString("lala");
        });
    }

    @Test
    public void testGetString_withAValue() {

        String aValue = "a value";
        JsonDoc data = new JsonDoc();
        data.set("lala", aValue);

        String value = data.getString("lala");
        assertEquals(aValue, value);
    }

    @Test
    public void testGetStringOrNull_withNullValue() {

        String aValue = null;
        JsonDoc data = new JsonDoc();
        data.set("lala", aValue);

        String value = data.getStringOrNull("lala");
        assertNull(value);
    }

    @Test
    public void testGetStringOrNull_withAValue() {

        String aValue = "a value";
        JsonDoc data = new JsonDoc();
        data.set("lala", aValue);

        String value = data.getStringOrNull("lala");
        assertEquals(aValue, value);
    }

    @Test
    public void testIsJson_withNullValue() {

        String aValue = null;

        boolean result = JsonDoc.isJson(aValue);
        assertFalse(result);
    }

    @Test
    public void testIsJson_withEmptyValue() {

        String aValue = "";

        boolean result = JsonDoc.isJson(aValue);
        assertFalse(result);
    }

    @Test
    public void testIsJson_withEmptyObject() {

        String aValue = "{}";

        boolean result = JsonDoc.isJson(aValue);
        assertTrue(result);
    }

    @Test
    public void testIsJson_withObject() {

        String aValue = "{\"a\": \"b\"}";

        boolean result = JsonDoc.isJson(aValue);
        assertTrue(result);
    }

    @Test
    public void testIsJson_withTest() {

        String aValue = "lala";

        boolean result = JsonDoc.isJson(aValue);
        assertFalse(result);
    }

}

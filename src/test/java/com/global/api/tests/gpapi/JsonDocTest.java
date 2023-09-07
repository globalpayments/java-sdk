package com.global.api.tests.gpapi;

import com.global.api.utils.JsonDoc;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JsonDocTest {

    @Test(expected = NullPointerException.class)
    public void testGetString_withNullValue() {

        String aValue = null;
        JsonDoc data = new JsonDoc();
        data.set("lala", aValue, true);

        data.getString("lala");
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

}

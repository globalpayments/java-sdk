package com.global.api.tests.utils;

import com.global.api.utils.StringUtils;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class StringUtilsTest {

    @Test
    public void toNumericNull() {

        BigDecimal amount = null;
        String result = StringUtils.toNumeric(amount);

        assertEquals("", result);
    }

    @Test
    public void toNumericZero() {

        BigDecimal amount = BigDecimal.ZERO;
        String result = StringUtils.toNumeric(amount);

        assertEquals("000", result);
    }

    @Test
    public void toNumericWithOneDecimal() {

        BigDecimal amount = new BigDecimal(123.5);
        String result = StringUtils.toNumeric(amount);

        assertEquals("12350", result);
    }

    @Test
    public void toNumericWithTwoDecimals() {

        BigDecimal amount = new BigDecimal(123.51);
        String result = StringUtils.toNumeric(amount);

        assertEquals("12351", result);
    }

    @Test
    public void toNumericWithThreeDecimals_roundsDown() {

        BigDecimal amount = new BigDecimal(123.514);
        String result = StringUtils.toNumeric(amount);

        assertEquals("12351", result);
    }

    @Test
    public void toNumericWithThreeDecimals_roundsUp() {

        BigDecimal amount = new BigDecimal(123.515);
        String result = StringUtils.toNumeric(amount);

        assertEquals("12352", result);
    }

}

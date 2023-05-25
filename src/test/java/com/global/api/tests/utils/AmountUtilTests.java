package com.global.api.tests.utils;

import com.global.api.utils.AmountUtils;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertTrue;

public class AmountUtilTests {
    @Test
    public void test_amount_equals_different_scale() {
        assertTrue(AmountUtils.areEqual(new BigDecimal(15.6), new BigDecimal(15.60)));
    }
}

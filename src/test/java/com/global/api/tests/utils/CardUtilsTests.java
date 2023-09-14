package com.global.api.tests.utils;

import org.junit.Assert;
import org.junit.Test;

public class CardUtilsTests {
    @Test
    public void test_valid_regex_pattern_for_MC() {

        String regex = "^(?:5[1-8]|222[1-9]|22[3-9][0-9]|2[3-6][0-9]{2}|27[01][0-9]|2720)";

        Assert.assertTrue("Should match 51","51".matches(regex));
        Assert.assertTrue("Should match 2221","2221".matches(regex));
        Assert.assertTrue("Should match 2245","2245".matches(regex));
        Assert.assertTrue("Should match 2720","2720".matches(regex));

        Assert.assertFalse("Should not match 50","50".matches(regex));
        Assert.assertFalse("Should not match 2220","2220".matches(regex));
        Assert.assertFalse("Should not match 9999","9999".matches(regex));
    }
}

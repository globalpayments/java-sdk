package com.global.api.tests.gpEcom.certification;

import com.global.api.tests.JsonComparator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JsonComparatorTest {
    private String KNOWN_GOOD_JSON = "{ \"MERCHANT_ID\": \"TWVyY2hhbnRJZA==\", \"ACCOUNT\": \"aW50ZXJuZXQ=\", \"ORDER_ID\": \"R1RJNVl4YjBTdW1MX1RrRE1DQXhRQQ==\", \"AMOUNT\": \"MTk5OQ==\", \"CURRENCY\": \"RVVS\", \"TIMESTAMP\": \"MjAxNzA3MTMxNTUzNDM=\", \"SHA1HASH\": \"NjhlMTgyZDIzNTg1ZTJlNDNlMDIwODFhNTA1ODYyM2Y2ODg2MjQyZQ==\", \"AUTO_SETTLE_FLAG\": \"MA==\", \"SHIPPING_CODE\": \"NjU0fDEyMw==\", \"SHIPPING_CO\": \"R0I=\", \"BILLING_CODE\": \"OTg3fDY1NA==\", \"BILLING_CO\": \"SUU=\", \"CUST_NUM\": \"Q1JNUkVGMTIzNDU2Nzg5\", \"PROD_ID\": \"U0tVMTIzNDU2Nzg5\", \"HPP_LANG\": \"RU4=\", \"CARD_PAYMENT_BUTTON\": \"Q29tcGxldGUgUGF5bWVudA==\"}";

    @Test
    public void compareSameStrings() {
        assertTrue(JsonComparator.areEqual(KNOWN_GOOD_JSON, KNOWN_GOOD_JSON));
    }

    @Test
    public void compareWithMissingField() {
        String testString = "{ \"ACCOUNT\": \"aW50ZXJuZXQ=\", \"ORDER_ID\": \"R1RJNVl4YjBTdW1MX1RrRE1DQXhRQQ==\", \"AMOUNT\": \"MTk5OQ==\", \"CURRENCY\": \"RVVS\", \"TIMESTAMP\": \"MjAxNzA3MTMxNTUzNDM=\", \"SHA1HASH\": \"NjhlMTgyZDIzNTg1ZTJlNDNlMDIwODFhNTA1ODYyM2Y2ODg2MjQyZQ==\", \"AUTO_SETTLE_FLAG\": \"MA==\", \"SHIPPING_CODE\": \"NjU0fDEyMw==\", \"SHIPPING_CO\": \"R0I=\", \"BILLING_CODE\": \"OTg3fDY1NA==\", \"BILLING_CO\": \"SUU=\", \"CUST_NUM\": \"Q1JNUkVGMTIzNDU2Nzg5\", \"PROD_ID\": \"U0tVMTIzNDU2Nzg5\", \"HPP_LANG\": \"RU4=\", \"CARD_PAYMENT_BUTTON\": \"Q29tcGxldGUgUGF5bWVudA==\"}";
        assertFalse(JsonComparator.areEqual(KNOWN_GOOD_JSON, testString));
    }

    @Test
    public void compareWithDifferentValue() {
        String testString = "{ \"MERCHANT_ID\": \"merchant_id\", \"ACCOUNT\": \"aW50ZXJuZXQ=\", \"ORDER_ID\": \"R1RJNVl4YjBTdW1MX1RrRE1DQXhRQQ==\", \"AMOUNT\": \"MTk5OQ==\", \"CURRENCY\": \"RVVS\", \"TIMESTAMP\": \"MjAxNzA3MTMxNTUzNDM=\", \"SHA1HASH\": \"NjhlMTgyZDIzNTg1ZTJlNDNlMDIwODFhNTA1ODYyM2Y2ODg2MjQyZQ==\", \"AUTO_SETTLE_FLAG\": \"MA==\", \"SHIPPING_CODE\": \"NjU0fDEyMw==\", \"SHIPPING_CO\": \"R0I=\", \"BILLING_CODE\": \"OTg3fDY1NA==\", \"BILLING_CO\": \"SUU=\", \"CUST_NUM\": \"Q1JNUkVGMTIzNDU2Nzg5\", \"PROD_ID\": \"U0tVMTIzNDU2Nzg5\", \"HPP_LANG\": \"RU4=\", \"CARD_PAYMENT_BUTTON\": \"Q29tcGxldGUgUGF5bWVudA==\"}";
        assertFalse(JsonComparator.areEqual(KNOWN_GOOD_JSON, testString));
    }

    @Test
    public void compareWithExtraData() {
        String testString = "{ \"NEW_FIELD\": \"new_field_data\", \"MERCHANT_ID\": \"TWVyY2hhbnRJZA==\", \"ACCOUNT\": \"aW50ZXJuZXQ=\", \"ORDER_ID\": \"R1RJNVl4YjBTdW1MX1RrRE1DQXhRQQ==\", \"AMOUNT\": \"MTk5OQ==\", \"CURRENCY\": \"RVVS\", \"TIMESTAMP\": \"MjAxNzA3MTMxNTUzNDM=\", \"SHA1HASH\": \"NjhlMTgyZDIzNTg1ZTJlNDNlMDIwODFhNTA1ODYyM2Y2ODg2MjQyZQ==\", \"AUTO_SETTLE_FLAG\": \"MA==\", \"SHIPPING_CODE\": \"NjU0fDEyMw==\", \"SHIPPING_CO\": \"R0I=\", \"BILLING_CODE\": \"OTg3fDY1NA==\", \"BILLING_CO\": \"SUU=\", \"CUST_NUM\": \"Q1JNUkVGMTIzNDU2Nzg5\", \"PROD_ID\": \"U0tVMTIzNDU2Nzg5\", \"HPP_LANG\": \"RU4=\", \"CARD_PAYMENT_BUTTON\": \"Q29tcGxldGUgUGF5bWVudA==\"}";
        assertFalse(JsonComparator.areEqual(KNOWN_GOOD_JSON, testString));
    }
}

package com.global.api.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
/**
 * Utility class for resolving decimal exponents for ISO\-4217 currency codes.
 *
 * <p>Contains an immutable in\-memory mapping of currency code to exponent and
 * provides a single lookup method with a default exponent of `2` when the
 * currency code is not present.</p>
 */
public final class CurrencyExponentUtils {

    public static final Map<String, Integer> CURRENCY_EXPONENT_MAP;

    static {
        Map<String, Integer> map = new HashMap<>();
        map.put("AED", 2); map.put("AUD", 2); map.put("BDT", 2);
        map.put("BHD", 3); map.put("BND", 2); map.put("BRL", 2);
        map.put("CAD", 2); map.put("CHF", 2); map.put("CLP", 2);
        map.put("CNY", 2); map.put("DKK", 2); map.put("EGP", 2);
        map.put("EUR", 2); map.put("GBP", 2); map.put("HKD", 2);
        map.put("IDR", 2); map.put("ILS", 2); map.put("INR", 2);
        map.put("ISK", 0); map.put("JPY", 2); map.put("KRW", 0);
        map.put("KWD", 3); map.put("LKR", 2); map.put("MOP", 2);
        map.put("MUR", 2); map.put("MVR", 2); map.put("MXN", 2);
        map.put("MYR", 2); map.put("NOK", 2); map.put("NZD", 2);
        map.put("OMR", 3); map.put("PGK", 2); map.put("PHP", 2);
        map.put("PKR", 2); map.put("QAR", 2); map.put("RUB", 2);
        map.put("SAR", 2); map.put("SEK", 2); map.put("SGD", 2);
        map.put("THB", 2); map.put("TRY", 2); map.put("TWD", 2);
        map.put("USD", 2); map.put("VEF", 2); map.put("VND", 0);
        map.put("ZAR", 2);
        CURRENCY_EXPONENT_MAP = Collections.unmodifiableMap(map);
    }

    private CurrencyExponentUtils() {
    }

    /**
     * Returns the decimal exponent for the given ISO currency code.
     *
     * @param currencyCode the 3\-letter currency code \(for example, `USD`\)
     * @return the configured exponent for the currency, or `2` if the currency is not found
     */
    public static int getExponent(String currencyCode) {
        return CURRENCY_EXPONENT_MAP.getOrDefault(currencyCode, 2);
    }
}

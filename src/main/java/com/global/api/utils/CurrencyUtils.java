package com.global.api.utils;

import java.util.HashMap;
import java.util.Map;

public class CurrencyUtils {

    private static Map<String, String> currencyCodeMapByCurrency;

    static {
        currencyCodeMapByCurrency = new HashMap<>();
        currencyCodeMapByCurrency.put("USD", "840");
        currencyCodeMapByCurrency.put("CAD", "124");
        currencyCodeMapByCurrency.put("AUD", "036");
        currencyCodeMapByCurrency.put("NZD", "554");
        currencyCodeMapByCurrency.put("CHF", "756");
        currencyCodeMapByCurrency.put("EUR", "978");
        currencyCodeMapByCurrency.put("GBP", "826");
        currencyCodeMapByCurrency.put("BHD", "048");
        currencyCodeMapByCurrency.put("BRL", "986");
        currencyCodeMapByCurrency.put("DKK", "208");
        currencyCodeMapByCurrency.put("EGP", "818");
        currencyCodeMapByCurrency.put("HKD", "344");
        currencyCodeMapByCurrency.put("INR", "356");
        currencyCodeMapByCurrency.put("JPY", "392");
        currencyCodeMapByCurrency.put("KRW", "410");
        currencyCodeMapByCurrency.put("KWD", "414");
        currencyCodeMapByCurrency.put("LBP", "422");
        currencyCodeMapByCurrency.put("MYR", "458");
        currencyCodeMapByCurrency.put("ILS", "376");
        currencyCodeMapByCurrency.put("TRY", "949");
        currencyCodeMapByCurrency.put("NGN", "566");
        currencyCodeMapByCurrency.put("NOK", "578");
        currencyCodeMapByCurrency.put("PLN", "985");
        currencyCodeMapByCurrency.put("QAR", "634");
        currencyCodeMapByCurrency.put("RUB", "643");
        currencyCodeMapByCurrency.put("SAR", "682");
        currencyCodeMapByCurrency.put("SGD", "702");
        currencyCodeMapByCurrency.put("ZAR", "710");
        currencyCodeMapByCurrency.put("SEK", "752");
        currencyCodeMapByCurrency.put("THB", "764");
        currencyCodeMapByCurrency.put("UAH", "980");
        currencyCodeMapByCurrency.put("AED", "784");
        currencyCodeMapByCurrency.put("CNY", "156");
    }

    public static String getCurrencyByCode(String currency) {
        if(currency == null)
            return null;

        if(currencyCodeMapByCurrency.containsKey(currency))
            return currencyCodeMapByCurrency.get(currency);
        else {
            throw new UnsupportedOperationException("Provided invalid currency.");
        }
    }
}

package com.global.api.utils;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.enums.IStringConstant;

import java.math.BigDecimal;
import java.text.NumberFormat;

public class StringUtils {
    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().equals("");
    }

    public static String padLeft(String input, int totalLength, char paddingCharacter) {
        String rvalue = input;
        while(rvalue.length() < totalLength)
            rvalue = paddingCharacter + rvalue;
        return rvalue;
    }

    public static BigDecimal toAmount(String str) {
        if(isNullOrEmpty(str))
            return null;

        BigDecimal amount = new BigDecimal(str);
        return amount.divide(new BigDecimal(100));
    }

    public static String toNumeric(BigDecimal amount) {
        if(amount == null)
            return "";

        NumberFormat fmt = NumberFormat.getCurrencyInstance();
        String currency = fmt.format(amount);
        return currency.replaceAll("[^0-9]", "");
    }

    public static String join(String separator, String[] fields) {
        String rvalue = "";
        for(String field: fields) {
            if(field == null)
                field = "";
            rvalue += field + separator;
        }
        //return trimEnd(rvalue, separator);
        return rvalue.substring(0, rvalue.length() - separator.length());
    }

    public static String trimEnd(String str) {
        return trimEnd(str, " ");
    }
    public static String trimEnd(String str, String trimString) {
        String rvalue = str;
        while(rvalue.endsWith(trimString)) {
            int trimLength = trimString.length();
            rvalue = rvalue.substring(0, rvalue.length() - trimLength);
        }
        return rvalue;
    }
    public static String trimEnd(String str, ControlCodes code) {
        // Strip the nulls off
        str = str.replaceAll("null", "");
        String trimChar = (char)code.getByte() + "";

        return trimEnd(str, trimChar);
    }

    public static String toInititalCase(IStringConstant value) {
        String initialValue = value.getValue();
        return initialValue.substring(0, 1).toUpperCase() + initialValue.substring(1).toLowerCase();
    }
}

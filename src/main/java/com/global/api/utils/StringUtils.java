package com.global.api.utils;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.enums.IStringConstant;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static String join(String separator, Object[] fields) {
        String rvalue = "";
        for(Object field: fields) {
            if(field == null)
                field = "";
            rvalue += field.toString() + separator;
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

	public static String mask(String value) {
		String masked = null;
		Pattern regex = Pattern.compile("\\b(?:4[ -]*(?:\\d[ -]*){11}(?:(?:\\d[ -]*){3})?\\d|"
				+ "(?:5[ -]*[1-5](?:[ -]*\\d){2}|(?:2[ -]*){3}[1-9]|(?:2[ -]*){2}[3-9][ -]*"
				+ "\\d|2[ -]*[3-6](?:[ -]*\\d){2}|2[ -]*7[ -]*[01][ -]*\\d|2[ -]*7[ -]*2[ -]*0)(?:[ -]*"
				+ "\\d){12}|3[ -]*[47](?:[ -]*\\d){13}|3[ -]*(?:0[ -]*[0-5]|[68][ -]*\\d)(?:[ -]*"
				+ "\\d){11}|6[ -]*(?:0[ -]*1[ -]*1|5[ -]*\\d[ -]*\\d)(?:[ -]*"
				+ "\\d){12}|(?:2[ -]*1[ -]*3[ -]*1|1[ -]*8[ -]*0[ -]*0|3[ -]*5(?:[ -]*"
				+ "\\d){3})(?:[ -]*\\d){11})\\b");

		Matcher regexMatcher = regex.matcher(value);
		if (regexMatcher.find()) {
			String card = regexMatcher.group();
			String strippedCard = card.replaceAll("[ -]+", "");
			String subSectionOfCard = strippedCard.substring(6, strippedCard.length() - 4);
			String prefix = strippedCard.substring(0, 6);
			String middle = padLeft("X", subSectionOfCard.length(), 'X');
			String suffix = strippedCard.substring(strippedCard.length() - 4, strippedCard.length());
			String maskedCard = prefix + middle + suffix;
			masked = value.replace(card, maskedCard);
		} else {
			masked = value;
		}
		return masked;
	}
}

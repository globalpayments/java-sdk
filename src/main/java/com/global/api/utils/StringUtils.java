package com.global.api.utils;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.enums.IStringConstant;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().equals("");
    }

    public static String padLeft(Object input, int totalLength, char paddingCharacter) {
        if(input == null) {
            input = "";
        }
        return padLeft(input.toString(), totalLength, paddingCharacter);
    }
    public static String padLeft(String input, int totalLength, char paddingCharacter) {
        String rvalue = input;
        if(rvalue == null) {
            rvalue = "";
        }

        while(rvalue.length() < totalLength) {
            rvalue = paddingCharacter + rvalue;
        }
        return rvalue;
    }

    public static String padRight(String input, int totalLength, char paddingCharacter) {
        String rvalue = input;
        if(rvalue == null) {
            rvalue = "";
        }

        while(rvalue.length() < totalLength) {
            rvalue = rvalue + paddingCharacter;
        }
        return rvalue;
    }

    public static BigDecimal toAmount(String str) {
        if(isNullOrEmpty(str))
            return null;

        BigDecimal amount = new BigDecimal(str);
        return amount.divide(new BigDecimal(100));
    }
    public static BigDecimal toFractionalAmount(String str) {
        if(isNullOrEmpty(str)) {
            return null;
        }

        int numDecimals = Integer.parseInt(str.substring(0, 1));
        int shiftValue = Integer.parseInt(StringUtils.padRight("1", numDecimals + 1, '0'));

        BigDecimal qty = new BigDecimal(str.substring(1)).setScale(numDecimals);
        return qty.divide(new BigDecimal(shiftValue));
    }

    public static String toNumeric(BigDecimal amount) {
        if(amount == null) {
            return "";
        }
        else if (amount.toString().equals("0")) {
            return "000";
        }

        NumberFormat fmt = NumberFormat.getCurrencyInstance();
        String currency = fmt.format(amount);
        return trimStart(currency.replaceAll("[^0-9]", ""), "0");
    }
    public static String toNumeric(BigDecimal amount, int length) {
        String rvalue = toNumeric(amount);
        return padLeft(rvalue, length, '0');
    }
    public static String toFractionalNumeric(BigDecimal amount) {
        if(amount == null) {
            return "";
        }

        int numberPlaces = amount.scale();
        String rvalue = trimStart(amount.toString().replaceAll("[^0-9]", ""), "0");
        return numberPlaces + rvalue;
    }

    public static String join(String separator, Object[] fields) {
        String rvalue = "";
        for(Object field: fields) {
            if(field == null) {
                field = "";
            }
            rvalue = rvalue.concat(field.toString() + separator);
        }
        return rvalue.substring(0, rvalue.length() - separator.length());
    }

    public static String trim(String str) {
        String rvalue = trimEnd(str);
        return trimStart(rvalue);
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
    public static String trimEnd(String str, String... trimChars) {
        String rvalue = str;
        for(String trimChar: trimChars) {
            rvalue = trimEnd(rvalue, trimChar);
        }
        return rvalue;
    }
    public static String trimEnd(String str, ControlCodes code) {
        // Strip the nulls off
        str = str.replaceAll("null", "");
        String trimChar = (char)code.getByte() + "";

        return trimEnd(str, trimChar);
    }

    public static String trimStart(String str) {
        return trimStart(str, " ");
    }
    public static String trimStart(String str, String trimString) {
        String rvalue = str;
        while(rvalue.startsWith(trimString)) {
            int trimLength = trimString.length();
            rvalue = rvalue.substring(trimLength);
        }
        return rvalue;
    }
    public static String trimStart(String str, String... trimChars) {
        String rvalue = str;
        for(String trimChar: trimChars) {
            rvalue = trimStart(rvalue, trimChar);
        }
        return rvalue;
    }

    public static String toLVar(String str) {
        String length = padLeft(str.length() + "", 1, '0');
        return length + str;
    }
    public static String toLLVar(String str) {
        String length = padLeft(str.length() + "", 2, '0');
        return length + str;
    }
    public static String toLLLVar(String str) {
        String length = padLeft(str.length() + "", 3, '0');
        return length + str;
    }

    public static String toInitialCase(IStringConstant value) {
        String initialValue = value.getValue();
        return initialValue.substring(0, 1).toUpperCase() + initialValue.substring(1).toLowerCase();
    }

    public static byte[] bytesFromHex(String hexString) {
        String s = hexString.toLowerCase();

        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }
    public static String hexFromBytes(byte[] buffer) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[buffer.length * 2];
        for ( int j = 0; j < buffer.length; j++ ) {
            int v = buffer[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
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

    public static String extractDigits(String str) {
        return StringUtils.isNullOrEmpty(str) ? str : str.replaceAll("[^0-9]", "");
    }

}

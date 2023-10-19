package com.global.api.utils.masking;


import com.global.api.utils.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class MaskValueUtil {

    public static Map<String, String> hideValues(ElementToMask... elementToMasks) {
        return Arrays
                .stream(elementToMasks)
                .distinct()
                .filter(entry -> validateValue(entry.getValue()))
                .map(MaskValueUtil::hideValueInternal)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Map.Entry<String, String> hideValueInternal(ElementToMask elementToMask) {
        String hiddenValue = disguise(
                elementToMask.getValue(),
                elementToMask.getUnmaskedLastChars(),
                elementToMask.getUnmaskedFirstChars(),
                elementToMask.getMaskSymbol()
        );
        return new AbstractMap.SimpleEntry<>(elementToMask.getKey(), hiddenValue);
    }

    private static String disguise(String value, int unmaskedLastChars, int unmaskedFirstChars, char maskSymbol) {
        if (unmaskedLastChars >= value.length()) {
            unmaskedLastChars = 0;
        }
        if (unmaskedLastChars > (value.length() / 2)) {
            unmaskedLastChars = (int) Math.round((unmaskedLastChars / 2.0));
        }
        if (unmaskedLastChars < 0) {
            int positiveUnmaskedLastChars = unmaskedLastChars * -1;
            String unmasked = value.substring(value.length() - positiveUnmaskedLastChars, positiveUnmaskedLastChars);
            return StringUtils.padLeft(unmasked, unmaskedLastChars, maskSymbol);
        }

        String unmaskedFirstData = value.substring(0, unmaskedFirstChars);
        String unmaskedLastData = value.substring(value.length() - unmaskedLastChars);

        return unmaskedFirstData + StringUtils.padLeft("", value.length() - (unmaskedLastChars + unmaskedFirstChars), maskSymbol) + unmaskedLastData;
    }

    private static boolean validateValue(String value) {
        return !StringUtils.isNullOrEmpty(value);
    }

}
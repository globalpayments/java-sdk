package com.global.api.gateways;

import com.global.api.entities.exceptions.GatewayException;
import com.global.api.utils.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;

public class DateParsingUtils {
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";// Standard expected GP API DateTime format
    public static final String DATE_TIME_PATTERN_2 = "yyyy-MM-dd'T'HH:mm:ss.SSS";   // Slightly different GP API DateTime format
    public static final String DATE_TIME_PATTERN_3 = "yyyy-MM-dd'T'HH:mm:ss'Z'";    // Another slightly different GP API DateTime format. Appears in Paypal.
    public static final String DATE_TIME_PATTERN_4 = "yyyy-MM-dd'T'HH:mm:ss";       // Another slightly different GP API DateTime format
    public static final String DATE_TIME_PATTERN_5 = "yyyy-MM-dd'T'HH:mm";          // Another slightly different GP API DateTime format
    public static final String DATE_TIME_PATTERN_6 = DATE_PATTERN;                  // Another slightly different GP API DateTime format
    public static final String DATE_TIME_PATTERN_7 = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'"; // Another slightly different GP API DateTime format
    public static final String DATE_TIME_PATTERN_8 = "yyyy-MM-dd'T'HH:mm:ss+SS:SS";     // Another slightly different GP API DateTime format
    public static final String DATE_TIME_PATTERN_10 = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS"; // 2023-07-20T18:22:49.0710761 Another slightly different GP API DateTime format


    public static final SimpleDateFormat DATE_SDF = new SimpleDateFormat(DATE_PATTERN);

    public static final DateTimeFormatter DATE_TIME_DTF = DateTimeFormat.forPattern(DATE_TIME_PATTERN);
    public static final DateTimeFormatter DATE_TIME_DTF_2 = DateTimeFormat.forPattern(DATE_TIME_PATTERN_2);
    public static final DateTimeFormatter DATE_TIME_DTF_3 = DateTimeFormat.forPattern(DATE_TIME_PATTERN_3);
    public static final DateTimeFormatter DATE_TIME_DTF_4 = DateTimeFormat.forPattern(DATE_TIME_PATTERN_4);
    public static final DateTimeFormatter DATE_TIME_DTF_5 = DateTimeFormat.forPattern(DATE_TIME_PATTERN_5);
    public static final DateTimeFormatter DATE_TIME_DTF_6 = DateTimeFormat.forPattern(DATE_TIME_PATTERN_6);
    public static final DateTimeFormatter DATE_TIME_DTF_7 = DateTimeFormat.forPattern(DATE_TIME_PATTERN_7);
    public static final DateTimeFormatter DATE_TIME_DTF_8 = DateTimeFormat.forPattern(DATE_TIME_PATTERN_8);
    public static final DateTimeFormatter DATE_TIME_DTF_10 = DateTimeFormat.forPattern(DATE_TIME_PATTERN_10);

    private static final DateTimeFormatter parserArray[] = {
            DATE_TIME_DTF,
            DATE_TIME_DTF_2,
            DATE_TIME_DTF_3,
            DATE_TIME_DTF_4,
            DATE_TIME_DTF_5,
            DATE_TIME_DTF_6,
            DATE_TIME_DTF_7,
            DATE_TIME_DTF_8,
            DATE_TIME_DTF_10
    };

    public static DateTime parseDateTime(String dateValue) throws GatewayException {

        if (StringUtils.isNullOrEmpty(dateValue)) {
            return null;
        }

        Exception lastExceptionIfAny = null;

        for (DateTimeFormatter actualParser : parserArray) {
            try {
                return actualParser.parseDateTime(dateValue);
            } catch (IllegalArgumentException ex) {
                lastExceptionIfAny = ex;
            }
        }
        throw new GatewayException("DateTime format is not supported.", lastExceptionIfAny);
    }

}
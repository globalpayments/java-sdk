package com.global.api.utils;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    public static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }

    public static Date parse(String date) {
        return parse(date, "MM/dd/yyyy");
    }

    public static Date parse(String date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.parse(date, new ParsePosition(0));
    }

    public static String toString(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public static boolean isBeforeOrEquals(Date date1, Date date2) {
        return date1.before(date2) || date1.equals(date2);
    }

    public static boolean isAfterOrEquals(Date date1, Date date2) {
        return date1.after(date2) || date1.equals(date2);
    }

}
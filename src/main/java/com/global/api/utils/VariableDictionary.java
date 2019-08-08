package com.global.api.utils;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class VariableDictionary extends HashMap<String, String> {
    public BigDecimal getAmount(String key) {
        return getAmount(key, null);
    }
    public BigDecimal getAmount(String key, BigDecimal defaultValue) {
        if(this.containsKey(key)) {
            return StringUtils.toAmount(this.get(key));
        }
        return defaultValue;
    }

    public Boolean getBoolean(String key) {
        if(this.containsKey(key)) {
            return Boolean.parseBoolean(this.get(key));
        }
        return null;
    }

    public DateTime getDateTime(String key) {
        return getDateTime(key, null);
    }
    public DateTime getDateTime(String key, DateTime defaultValue) {
        if(this.containsKey(key)) {
            return DateTime.parse(this.get(key));
        }
        return defaultValue;
    }

    public Integer getInt(String key) {
        return getInt(key, null);
    }
    public Integer getInt(String key, Integer defaultValue) {
        if(this.containsKey(key)) {
            return Integer.parseInt(this.get(key));
        }
        return defaultValue;
    }

    public String getString(String key) {
        return getString(key, null);
    }
    public String getString(String key, String defaultValue) {
        if(this.containsKey(key)) {
            return this.get(key);
        }
        return defaultValue;
    }
}

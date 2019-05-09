package com.global.api.utils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class VariableDictionary extends HashMap<String, String> {
    public BigDecimal getAmount(String key) {
        if(this.containsKey(key)) {
            return StringUtils.toAmount(this.get(key));
        }
        return null;
    }

    public Boolean getBoolean(String key) {
        if(this.containsKey(key)) {
            return Boolean.parseBoolean(this.get(key));
        }
        return null;
    }

    public Date getDate(String key) {
        return getDate(key, "yyyyMMddHHmmss");
    }
    public Date getDate(String key, String format) {
        SimpleDateFormat sfd = new SimpleDateFormat(format);
        if(this.containsKey(key)) {
            try {
                return sfd.parse(this.get(key));
            }
            catch(ParseException exc) {
                return null;
            }
        }
        return null;
    }

    public Integer getInt(String key) {
        if(this.containsKey(key)) {
            return Integer.parseInt(this.get(key));
        }
        return null;
    }

    public String getString(String key) {
        if(this.containsKey(key)) {
            return this.get(key);
        }
        return null;
    }
}

package com.global.api.utils;

import com.global.api.entities.exceptions.ApiException;
import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;

public class Base64Encoder implements IRequestEncoder {
    public String encode(Object value) {
        if(value == null) return null;
        try {
            byte[] encoded = Base64.encodeBase64(value.toString().getBytes("UTF-8"));
            return new String(encoded);
        }
        catch(UnsupportedEncodingException e) { return value.toString(); }
    }

    public String decode(Object value) {
        if(value == null) return null;
        try {
            byte[] decoded = Base64.decodeBase64(value.toString().getBytes("UTF-8"));
            return new String(decoded);
        }
        catch(UnsupportedEncodingException e) { return value.toString(); }
    }
}

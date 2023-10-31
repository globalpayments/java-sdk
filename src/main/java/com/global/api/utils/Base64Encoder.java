package com.global.api.utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Encoder implements IRequestEncoder {
    public String encode(Object value) {
        if(value == null) return null;
        byte[] encoded = Base64.getEncoder().encode(value.toString().getBytes(StandardCharsets.UTF_8));
        return new String(encoded);
    }

    public String decode(Object value) {
        if(value == null) return null;
        try {
            byte[] decoded = Base64.getDecoder().decode(value.toString().getBytes(StandardCharsets.UTF_8));
            return new String(decoded);
        }catch (IllegalArgumentException exception) {
            //Decoding failed, return the old object
            return value.toString();
        }
    }
}

package com.global.api.tests;

import com.global.api.entities.MerchantDataEncoder;
import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;

public class TestEncoder extends MerchantDataEncoder {
    public String encode(String input) {
        String encoded = String.format("%s.%s", input, "secret");
        try {
            return Base64.encodeBase64String(encoded.getBytes("UTF-8"));
        }
        catch(UnsupportedEncodingException e) {
            return input;
        }
    }

    public String decode(String input) {
        String[] decoded = new String(Base64.decodeBase64(input)).split("\\.");
        return decoded[0];
    }
}

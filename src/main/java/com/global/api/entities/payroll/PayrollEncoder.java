package com.global.api.entities.payroll;

import com.global.api.utils.IRequestEncoder;
import com.global.api.utils.ValueConverter;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

public class PayrollEncoder implements IRequestEncoder {
    private String username;
    private String apiKey;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public ValueConverter<String> getEncoder() {
        return new ValueConverter<String>() {
            @Override
            public String call(String value) throws Exception {
                return encode(value);
            }
        };
    }

    public ValueConverter<String> getDecoder() {
        return new ValueConverter<String>() {
            @Override
            public String call(String value) throws Exception {
                return decode(value);
            }
        };
    }

    public PayrollEncoder() {
        this(null,null);
    }
    public PayrollEncoder(String username, String apiKey) {
        this.username = username;
        this.apiKey = apiKey;
    }

    public String encode(Object value) {
        if (value == null)
            return null;

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            PBEKeySpec pbeKeySpec = new PBEKeySpec(apiKey.toCharArray(), username.getBytes("UTF-8"), 1000, 384);
            Key secretKey = factory.generateSecret(pbeKeySpec);
            byte[] key = new byte[32];
            byte[] iv = new byte[16];
            System.arraycopy(secretKey.getEncoded(), 0, key, 0, 32);
            System.arraycopy(secretKey.getEncoded(), 32, iv, 0, 16);

            SecretKeySpec secret = new SecretKeySpec(key, "AES");
            AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secret, ivSpec);
            byte[] results = cipher.doFinal(value.toString().getBytes("UTF-8"));
            return Base64.encodeBase64String(results);
        }
        catch(Exception exc) {
            return value.toString();
        }
    }

    public String decode(Object value) {
        if (value == null)
            return null;

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            PBEKeySpec pbeKeySpec = new PBEKeySpec(apiKey.toCharArray(), username.getBytes("UTF-8"), 1000, 384);
            Key secretKey = factory.generateSecret(pbeKeySpec);
            byte[] key = new byte[32];
            byte[] iv = new byte[16];
            System.arraycopy(secretKey.getEncoded(), 0, key, 0, 32);
            System.arraycopy(secretKey.getEncoded(), 32, iv, 0, 16);

            SecretKeySpec secret = new SecretKeySpec(key, "AES");
            AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secret, ivSpec);
            byte[] decode = Base64.decodeBase64(value.toString());
            byte[] results = cipher.doFinal(decode);
            return new String(results);
        }
        catch(Exception exc) {
            return value.toString();
        }
    }
}

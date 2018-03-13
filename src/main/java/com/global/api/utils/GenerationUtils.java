package com.global.api.utils;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Utils for the auto-generation of fields, for example the SHA1 hash.
 *
 * @author thomasduffy
 */
public class GenerationUtils {
    /**
     * Generates a one-pass SHA1 hash
     *
     * @param toHash The value to hash
     * @return String
     */
    static public String generateHash(String toHash) {
        if(toHash == null)
            return null;

        return DigestUtils.sha1Hex(toHash);
    }

    /**
     * Each message sent to Realex should have a hash, attached. For a message using the remote
     * interface this is generated using the This is generated from the TIMESTAMP, MERCHANT_ID,
     * ORDER_ID, AMOUNT, and CURRENCY fields concatenated together with "." in between each field.
     * This confirms the message comes
     * from the client and
     * Generate a hash, required for all messages sent to IPS to prove it was not tampered with.
     *
     * Hashing takes a string as input, and produce a fixed size number (160 bits for SHA-1 which
     * this implementation uses). This number is a hash of the input, and a small change in the
     * input results in a substantial change in the output. The functions are thought to be secure
     * in the sense that it requires an enormous amount of computing power and time to find a string
     * that hashes to the same value. In others words, there's no way to decrypt a secure hash.
     * Given the larger key size, this implementation uses SHA-1 which we prefer that you, but Realex
     * has retained compatibilty with MD5 hashing for compatibility with older systems.
     *
     * To construct the hash for the remote interface follow this procedure:
     *
     * Form a string by concatenating the above fields with a period ('.') in the following order
     *
     * (TIMESTAMP.MERCHANT_ID.ORDER_ID.AMOUNT.CURRENCY)
     *
     * Like so (where a field is empty an empty string "" is used):
     *
     * (20120926112654.thestore.ORD453-11.29900.EUR)
     *
     * Get the hash of this string (SHA-1 shown below).
     *
     * (b3d51ca21db725f9c7f13f8aca9e0e2ec2f32502)
     *
     * Create a new string by concatenating this string and your shared secret using a period.
     *
     * (b3d51ca21db725f9c7f13f8aca9e0e2ec2f32502.mysecret )
     *
     * Get the hash of this value. This is the value that you send to Realex Payments.
     *
     * (3c3cac74f2b783598b99af6e43246529346d95d1)
     *
     * This method takes the pre-built string of concatenated fields and the secret and returns the
     * SHA-1 hash to be placed in the request sent to Realex.
     *
     * @param toHash The value to hash
     * @param secret The shared secret to use in the second pass
     * @return the hash as a hex string
     */
    static public String generateHash(String toHash, String secret) {
        if(toHash == null)
            return null;

        //first pass hashes the String of required fields
        String toHashFirstPass = DigestUtils.sha1Hex(toHash);

        //second pass takes the first hash, adds the secret and hashes again
        String toHashSecondPass = new StringBuilder().append(toHashFirstPass).append(".").append(secret).toString();
        return DigestUtils.sha1Hex(toHashSecondPass);
    }

    static public String generateHash(String secret, String... fields) {
        String toHash = StringUtils.join(".", fields);
        return generateHash(toHash, secret);
    }

    /**
     * Generate the current datetimestamp in the string formaat (YYYYMMDDHHSS) required in a
     * request to Realex.
     *
     * @return current timestamp in YYYYMMDDHHSS format
     */
    static public String generateTimestamp() {
        return generateTimestamp(null);
    }
    static public String generateTimestamp(String timestamp) {
        if(timestamp != null)
            return timestamp;
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    /**
     * Order Id for a initial request should be unique per client ID. This method generates a unique
     * order Id using the Java UUID class and then convert it to base64 to shorten the length to 22
     * characters. Order Id for a subsequent request (void, rebate, settle ect.) should use the
     * order Id of the initial request.
     *
     * * the order ID uses the Java UUID (universally unique identifier) so in theory it may not
     * be unique but the odds of this are extremely remote (see
     * <a href="http://en.wikipedia.org/wiki/Universally_unique_identifier#Random_UUID_probability_of_duplicates">http://en.wikipedia.org/wiki/Universally_unique_identifier#Random_UUID_probability_of_duplicates</a>)
     *
     * @return orderId as a String
     */
    static public String generateOrderId() {
        return generateOrderId(null);
    }
    static public String generateOrderId(String orderId) {
        if(orderId != null)
            return orderId;

        UUID uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);

        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return Base64.encodeBase64URLSafeString(bb.array());
    }

    static public String generateRecurringKey() {
        return generateRecurringKey(null);
    }
    static public String generateRecurringKey(String key) {
        if(key != null)
            return key;

        UUID uuid = UUID.randomUUID();
        return uuid.toString().toLowerCase();
    }
}

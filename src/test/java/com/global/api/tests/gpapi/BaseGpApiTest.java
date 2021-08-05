package com.global.api.tests.gpapi;

import org.junit.Ignore;

public class BaseGpApiTest {

    // ================================================================================
    // Latest Credentials
    // ================================================================================
    static final String APP_ID = "yDkdruxQ7hUjm8p76SaeBVAUnahESP5P";
    static final String APP_KEY = "o8C8CYrgXNELI46x";
    // ================================================================================

    // ================================================================================
    // Credentials For Batch Actions
    // ================================================================================
    static final String APP_ID_FOR_BATCH = "P3LRVjtGRGxWQQJDE345mSkEh2KfdAyg";
    static final String APP_KEY_FOR_BATCH = "ockJr6pv6KFoGiZA";
    // ================================================================================

    static final String GP_API_CONFIG_NAME = "GP_API_CONFIG";
    static final String GP_API_CONFIG_NAME_CARD_PRESENT = "GP_API_CONFIG_CARD_PRESENT";

    static final String SUCCESS = "SUCCESS";
    static final String VERIFIED = "VERIFIED";
    static final String CLOSED = "CLOSED";

    @Ignore // Avoid this class to be considered as a Test class by JUnit
    public enum GpApi3DSTestCards {

        CARDHOLDER_NOT_ENROLLED_V1("4917000000000087"),
        CARDHOLDER_ENROLLED_V1("4012001037141112"),

        CARD_AUTH_SUCCESSFUL_V2_1("4263970000005262"),
        CARD_AUTH_SUCCESSFUL_NO_METHOD_URL_V2_1("4222000006724235"),
        CARD_AUTH_ATTEMPTED_BUT_NOT_SUCCESSFUL_V2_1("4012001037167778"),
        CARD_AUTH_FAILED_V2_1("4012001037461114"),
        CARD_AUTH_ISSUER_REJECTED_V2_1("4012001038443335"),
        CARD_AUTH_COULD_NOT_BE_PREFORMED_V2_1("4012001037484447"),
        CARD_CHALLENGE_REQUIRED_V2_1("4012001038488884"),

        CARD_AUTH_SUCCESSFUL_V2_2("4222000006285344"),
        CARD_AUTH_SUCCESSFUL_NO_METHOD_URL_V2_2("4222000009719489"),
        CARD_AUTH_ATTEMPTED_BUT_NOT_SUCCESSFUL_V2_2("4222000005218627"),
        CARD_AUTH_FAILED_V2_2("4222000002144131"),
        CARD_AUTH_ISSUER_REJECTED_V2_2("4222000007275799"),
        CARD_AUTH_COULD_NOT_BE_PREFORMED_V2_2("4222000008880910"),
        CARD_CHALLENGE_REQUIRED_V2_2("4222000001227408");

        String cardNumber;

        GpApi3DSTestCards(String cardNumber) {
            this.cardNumber = cardNumber;
        }

    }
}

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
    static final String DECLINED = "DECLINED";
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

        final String cardNumber;

        GpApi3DSTestCards(String cardNumber) {
            this.cardNumber = cardNumber;
        }
    }

    @Ignore // Avoid this class to be considered as a Test class by JUnit
    public enum AvsCheckTestCards {

        AVS_MasterCard_1("5167308114614170"),
        AVS_MasterCard_2("5167300431085507"),
        AVS_MasterCard_3("5167302774869323"),
        AVS_MasterCard_4("5167308372298823"),
        AVS_MasterCard_5("5167304376624205"),
        AVS_MasterCard_6("5167305286858551"),
        AVS_MasterCard_7("5167302027021516"),
        AVS_MasterCard_8("5167300747736579"),
        AVS_MasterCard_9("5167300575629722"),
        AVS_MasterCard_10("5167305971795258"),
        AVS_MasterCard_11("5167306563324952"),
        AVS_MasterCard_12("5167305301636255"),
        AVS_MasterCard_13("5167308925965308"),
        AVS_MasterCard_14("5167308304373025"),
        AVS_Visa_1("4259917010910078"),
        AVS_Visa_2("4259916460134858"),
        AVS_Visa_3("4259910583250337"),
        AVS_Visa_4("4259913676419876"),
        AVS_Visa_5("4259915858684813"),
        AVS_Visa_6("4259919754924299"),
        AVS_Visa_7("4259917979151326"),
        AVS_Visa_8("4259915339571613"),
        AVS_Visa_9("4259911964632119"),
        AVS_Visa_10("4259912262216019"),
        AVS_Visa_11("4259912836281333"),
        AVS_Visa_12("4259914539794240"),
        AVS_Visa_13("4259913732291939"),
        AVS_Visa_14("4259913622000341");

        final String avsCardNumber;

        AvsCheckTestCards(String cardNumber) {
            this.avsCardNumber = cardNumber;
        }
    }

}
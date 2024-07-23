package com.global.api.tests.gpapi;

import com.global.api.entities.enums.Channel;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.logging.RequestFileLogger;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.utils.DateUtils;
import lombok.SneakyThrows;
import org.joda.time.DateTime;
import org.junit.Ignore;

import java.util.Date;

public class BaseGpApiTest {

    // ================================================================================
    // Latest Credentials
    // ================================================================================
    public static final String APP_ID = "4gPqnGBkppGYvoE5UX9EWQlotTxGUDbs";
    public static final String APP_KEY = "FQyJA5VuEQfcji2M";
    static final String APP_ID_FOR_MERCHANT = "A1feRdMmEB6m0Y1aQ65H0bDi9ZeAEB2t";
    static final String APP_KEY_FOR_MERCHANT = "5jPt1OpB6LLitgi7";
    // ================================================================================
    static final String GP_API_CONFIG_NAME = "GP_API_CONFIG";
    static final String SUCCESS = "SUCCESS";
    static final String DECLINED = "DECLINED";
    static final String VERIFIED = "VERIFIED";
    static final String CLOSED = "CLOSED";

    static final int expMonth = DateTime.now().getMonthOfYear();
    static final int expYear = DateTime.now().getYear() + 1;

    static final Date startDate = DateUtils.atStartOfDay(DateUtils.addDays(new Date(), -30));
    static final Date endDate = DateUtils.atEndOfDay(new Date());

    public static GpApiConfig gpApiSetup(String APP_ID, String APP_KEY, Channel channel) {
        GpApiConfig gpApiConfig = new GpApiConfig()
                .setAppId(APP_ID)
                .setAppKey(APP_KEY);

        gpApiConfig.setChannel(channel);

        gpApiConfig.setChallengeNotificationUrl("https://ensi808o85za.x.pipedream.net/");
        gpApiConfig.setMethodNotificationUrl("https://ensi808o85za.x.pipedream.net/");
        gpApiConfig.setMerchantContactUrl("https://enp4qhvjseljg.x.pipedream.net/");

        gpApiConfig.setEnableLogging(true);
        gpApiConfig.setRequestLogger(new RequestConsoleLogger());

        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setTransactionProcessingAccountName("transaction_processing");
        accessTokenInfo.setRiskAssessmentAccountName("EOS_RiskAssessment");
        gpApiConfig.setAccessTokenInfo(accessTokenInfo);
//        gpApiConfig.setRequestLogger(new RequestFileLogger("C:\\temp\\GpApiCreditCardNotPresentTests.txt"));

        //DO NOT DELETE - usage example for some settings
//        HashMap<String, String> dynamicHeaders = new HashMap<String, String>() {{
//            put("x-gp-platform", "prestashop;version=1.7.2");
//            put("x-gp-extension", "coccinet;version=2.4.1");
//        }};
//
//        gpApiConfig.setDynamicHeaders(dynamicHeaders);

        return gpApiConfig;
    }

    @Ignore // Avoid this class to be considered as a Test class by JUnit
    public enum GpApi3DSTestCards {

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

    @SneakyThrows
    protected void waitForGpApiReplication() {
        Thread.sleep(2000);
    }

}
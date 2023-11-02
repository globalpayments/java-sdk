package com.global.api.tests.gpEcom;

import com.global.api.entities.enums.Secure3dVersion;
import com.global.api.serviceConfigs.GpEcomConfig;

public class BaseGpEComTest {

    public static GpEcomConfig gpEComSetup() {
        GpEcomConfig config = new GpEcomConfig();
        config.setMerchantId("heartlandgpsandbox");
        config.setAccountId("api");
        config.setSharedSecret("secret");
        config.setRebatePassword("rebate");
        config.setRefundPassword("refund");
        config.setServiceUrl("https://api.sandbox.realexpayments.com/epage-remote.cgi");
        config.setMethodNotificationUrl("https://www.example.com/methodNotificationUrl");
        config.setChallengeNotificationUrl("https://www.example.com/challengeNotificationUrl");
        config.setSecure3dVersion(Secure3dVersion.ANY);
        config.setEnableLogging(true);

        return config;
    }
}

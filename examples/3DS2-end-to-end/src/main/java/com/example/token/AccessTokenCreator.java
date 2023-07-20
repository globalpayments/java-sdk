/*
 * this sample code is not specific to the Global Payments SDK and is intended as a simple example and
 * should not be treated as Production-ready code. You'll need to add your own message parsing and
 * security in line with your application or website
 */
package com.example.token;

import com.example.applicationparameters.ApplicationParameters;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.Environment;
import com.global.api.entities.enums.IntervalToExpire;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.GpApiService;

public class AccessTokenCreator {

    private static AccessTokenCreator INSTANCE = null;

    private String myAccessToken;

    private AccessTokenCreator() {

        System.out.println();
        System.out.println("*** 01 *** AccessTokenCreator");
        System.out.println();

        GpApiConfig config = new GpApiConfig();
        config.setAppId(ApplicationParameters.getAppId());
        config.setAppKey(ApplicationParameters.getAppKey());
        config.setChannel(Channel.CardNotPresent);
        config.setEnvironment(Environment.TEST);
        // optional to request a subset of permissions
        config.setPermissions(new String[]{"PMT_POST_Create_Single"});
        config.setIntervalToExpire(IntervalToExpire.TEN_MINUTES);
        try {
            AccessTokenInfo accessTokenInfo = GpApiService.generateTransactionKey(config);
            myAccessToken = accessTokenInfo.getAccessToken();
        } catch (ApiException e) {
            // TODO: Add your error handling
            e.printStackTrace();
            throw new RuntimeException("just fail fast in this example");
        }
    }

    public static AccessTokenCreator getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new AccessTokenCreator();
        }
        return INSTANCE;
    }

    public String getAccessToken() {
        return myAccessToken;
    }
}

package com.global.api.tests.gpapi;

import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.GpApiService;
import org.junit.Test;

import static com.global.api.entities.enums.IntervalToExpire.FIVE_MINUTES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GpApiAuthenticationTests {

    private final String APP_ID = "OWTP5ptQZKGj7EnvPt3uqO844XDBt8Oj";
    private final String APP_KEY = "qM31FmlFiyXRHGYh";

    @Test
    public void GenerateAccessTokenManual() throws GatewayException {
        GpApiConfig config =
                new GpApiConfig()
                        .setAppId(APP_ID)
                        .setAppKey(APP_KEY);

        config.setEnableLogging(true);

        AccessTokenInfo info = GpApiService.generateTransactionKey(config);

        assertNotNull(info);
        assertNotNull(info.getToken());
        assertNotNull(info.getDataAccountName());
        assertNotNull(info.getDisputeManagementAccountName());
        assertNotNull(info.getTokenizationAccountName());
        assertNotNull(info.getTransactionProcessingAccountName());
    }

    @Test
    public void createAccessTokenWithSpecific_SecondsToExpire() throws GatewayException {
        GpApiConfig config =
                new GpApiConfig()
                        .setAppId(APP_ID)
                        .setAppKey(APP_KEY)
                        .setSecondsToExpire(60);    // 60 is the minimum supported value

        config.setEnableLogging(true);

        AccessTokenInfo info = GpApiService.generateTransactionKey(config);

        assertNotNull(info);
        assertNotNull(info.getToken());
        assertNotNull(info.getDataAccountName());
        assertNotNull(info.getDisputeManagementAccountName());
        assertNotNull(info.getTokenizationAccountName());
        assertNotNull(info.getTransactionProcessingAccountName());
    }

    @Test
    public void createAccessTokenWithSpecific_IntervalToExpire() throws GatewayException {
        GpApiConfig config =
                new GpApiConfig()
                        .setAppId(APP_ID)
                        .setAppKey(APP_KEY)
                        .setIntervalToExpire(FIVE_MINUTES);

        config.setEnableLogging(true);

        AccessTokenInfo info = GpApiService.generateTransactionKey(config);

        assertNotNull(info);
        assertNotNull(info.getToken());
        assertNotNull(info.getDataAccountName());
        assertNotNull(info.getDisputeManagementAccountName());
        assertNotNull(info.getTokenizationAccountName());
        assertNotNull(info.getTransactionProcessingAccountName());
    }

    @Test
    public void createAccessTokenWithSpecific_SecondsToExpireAndIntervalToExpire() throws GatewayException {
        GpApiConfig config =
                new GpApiConfig()
                        .setAppId(APP_ID)
                        .setAppKey(APP_KEY)
                        .setSecondsToExpire(60)    // 60 is the minimum supported value
                        .setIntervalToExpire(FIVE_MINUTES);

        config.setEnableLogging(true);

        AccessTokenInfo info = GpApiService.generateTransactionKey(config);

        assertNotNull(info);
        assertNotNull(info.getToken());
        assertNotNull(info.getDataAccountName());
        assertNotNull(info.getDisputeManagementAccountName());
        assertNotNull(info.getTokenizationAccountName());
        assertNotNull(info.getTransactionProcessingAccountName());
    }

    @Test
    public void GenerateAccessTokenWrongAppId() {
        try {
            GpApiConfig config =
                    new GpApiConfig()
                            .setAppId(APP_ID + "a")
                            .setAppKey(APP_KEY);

            config.setEnableLogging(true);

            GpApiService.generateTransactionKey(config);
        } catch (GatewayException ex) {
            assertEquals("40004", ex.getResponseText());
            assertEquals("ACTION_NOT_AUTHORIZED", ex.getResponseCode());
            assertEquals("Status Code: 403 - Credentials not recognized to create access token.", ex.getMessage());
        }
    }

    @Test
    public void GenerateAccessTokenWrongAppKey() {
        try {
            GpApiConfig config =
                    new GpApiConfig()
                            .setAppId(APP_ID)
                            .setAppKey(APP_KEY + "a");

            config.setEnableLogging(true);

            GpApiService.generateTransactionKey(config);
        } catch (GatewayException ex) {
            assertEquals("40004", ex.getResponseText());
            assertEquals("ACTION_NOT_AUTHORIZED", ex.getResponseCode());
            assertEquals("Status Code: 403 - Credentials not recognized to create access token.", ex.getMessage());
        }
    }

}
package com.global.api.tests.gpapi;

import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.services.GpApiService;
import org.junit.Test;

import static com.global.api.entities.enums.Environment.TEST;
import static com.global.api.entities.enums.IntervalToExpire.FIVE_MINUTES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GpApiAuthenticationTests {

    private final String APP_ID = "OWTP5ptQZKGj7EnvPt3uqO844XDBt8Oj";
    private final String APP_KEY = "qM31FmlFiyXRHGYh";

    @Test
    public void GenerateAccessTokenManual() throws GatewayException {

        AccessTokenInfo info = GpApiService.generateTransactionKey(TEST, APP_ID, APP_KEY);

        assertNotNull(info);
        assertNotNull(info.getToken());
        assertNotNull(info.getDataAccountName());
        assertNotNull(info.getDisputeManagementAccountName());
        assertNotNull(info.getTokenizationAccountName());
        assertNotNull(info.getTransactionProcessingAccountName());
    }

    @Test
    public void createAccessTokenWithSpecific_SecondsToExpire() throws GatewayException {
        // 60 is the minimum supported value
        AccessTokenInfo info = GpApiService.generateTransactionKey(TEST, APP_ID, APP_KEY, 60);

        assertNotNull(info);
        assertNotNull(info.getToken());
        assertNotNull(info.getDataAccountName());
        assertNotNull(info.getDisputeManagementAccountName());
        assertNotNull(info.getTokenizationAccountName());
        assertNotNull(info.getTransactionProcessingAccountName());
    }

    @Test
    public void createAccessTokenWithSpecific_IntervalToExpire() throws GatewayException {
        AccessTokenInfo info = GpApiService.generateTransactionKey(TEST, APP_ID, APP_KEY, FIVE_MINUTES);

        assertNotNull(info);
        assertNotNull(info.getToken());
        assertNotNull(info.getDataAccountName());
        assertNotNull(info.getDisputeManagementAccountName());
        assertNotNull(info.getTokenizationAccountName());
        assertNotNull(info.getTransactionProcessingAccountName());
    }

    @Test
    public void createAccessTokenWithSpecific_SecondsToExpireAndIntervalToExpire() throws GatewayException {
        AccessTokenInfo info = GpApiService.generateTransactionKey(TEST, APP_ID, APP_KEY, 60, FIVE_MINUTES);

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
            GpApiService.generateTransactionKey(TEST, APP_ID + "a", APP_KEY);
        } catch (GatewayException ex) {
            assertEquals("40004", ex.getResponseText());
            assertEquals("ACTION_NOT_AUTHORIZED", ex.getResponseCode());
            assertEquals("Status Code: 403 - Credentials not recognized to create access token.", ex.getMessage());
        }
    }

    @Test
    public void GenerateAccessTokenWrongAppKey() {
        try {
            GpApiService.generateTransactionKey(TEST, APP_ID, APP_KEY + "a");
        } catch (GatewayException ex) {
            assertEquals("40004", ex.getResponseText());
            assertEquals("ACTION_NOT_AUTHORIZED", ex.getResponseCode());
            assertEquals("Status Code: 403 - Credentials not recognized to create access token.", ex.getMessage());
        }
    }

}
package com.global.api.services;

import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.entities.enums.Environment;
import com.global.api.entities.enums.ServiceEndpoints;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.GpApiTokenResponse;
import com.global.api.gateways.GpApiConnector;
import com.global.api.entities.enums.IntervalToExpire;
import com.global.api.serviceConfigs.GpApiConfig;

public class GpApiService {

    public static AccessTokenInfo generateTransactionKey(Environment environment, String appId, String appKey) throws GatewayException {
        return generateTransactionKey(environment, appId, appKey, 0, null);
    }

    public static AccessTokenInfo generateTransactionKey(Environment environment, String appId, String appKey, int secondsToExpire) throws GatewayException {
        return generateTransactionKey(environment, appId, appKey, secondsToExpire, null);
    }

    public static AccessTokenInfo generateTransactionKey(Environment environment, String appId, String appKey, IntervalToExpire intervalToExpire) throws GatewayException {
        return generateTransactionKey(environment, appId, appKey, 0, intervalToExpire);
    }

    public static AccessTokenInfo generateTransactionKey(Environment environment, String appId, String appKey, int secondsToExpire, IntervalToExpire intervalToExpire) throws GatewayException {
        GpApiConfig gpApiConfig = new GpApiConfig();
        gpApiConfig.setAppId(appId);
        gpApiConfig.setAppKey(appKey);

        if(secondsToExpire != 0) {
            gpApiConfig.setSecondsToExpire(secondsToExpire);
        }

        if(intervalToExpire != null) {
            gpApiConfig.setIntervalToExpire(intervalToExpire);
        }

        gpApiConfig.setTimeout(1000);

        GpApiConnector connector = new GpApiConnector(gpApiConfig);
        connector.setServiceUrl(environment.equals(Environment.PRODUCTION) ? ServiceEndpoints.GP_API_PRODUCTION.getValue() : ServiceEndpoints.GP_API_TEST.getValue());

        // TODO: Disable Logging when Production ready
        connector.setEnableLogging(true);

        GpApiTokenResponse data = connector.getAccessToken();

        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();

        accessTokenInfo.setToken(data.getToken());
        accessTokenInfo.setDataAccountName(data.getDataAccountName());
        accessTokenInfo.setDisputeManagementAccountName(data.getDisputeManagementAccountName());
        accessTokenInfo.setTokenizationAccountName(data.getTokenizationAccountName());
        accessTokenInfo.setTransactionProcessingAccountName(data.getTransactionProcessingAccountName());

        return accessTokenInfo;
    }
}
package com.global.api.services;

import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.GpApiTokenResponse;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.gateways.GpApiConnector;
import com.global.api.serviceConfigs.GpApiConfig;

public class GpApiService {

    public static AccessTokenInfo generateTransactionKey(GpApiConfig gpApiConfig) throws GatewayException {
        GpApiConnector connector = new GpApiConnector(gpApiConfig);

        GpApiTokenResponse data = connector.getAccessToken();

        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();

        accessTokenInfo.setAccessToken(data.getToken());
        accessTokenInfo.setDataAccountName(data.getDataAccountName());
        accessTokenInfo.setDisputeManagementAccountName(data.getDisputeManagementAccountName());
        accessTokenInfo.setTokenizationAccountName(data.getTokenizationAccountName());
        accessTokenInfo.setTransactionProcessingAccountName(data.getTransactionProcessingAccountName());

        return accessTokenInfo;
    }
}
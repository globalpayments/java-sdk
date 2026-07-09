package com.global.api.services;

import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.GpApiSessionInfo;
import com.global.api.entities.gpApi.GpApiTokenResponse;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.gateways.GpApiConnector;
import com.global.api.serviceConfigs.GpApiConfig;

public class GpApiService {

    public static AccessTokenInfo generateTransactionKey(GpApiConfig gpApiConfig) throws GatewayException {
        if(gpApiConfig.getAccessTokenProvider() == null) {
            gpApiConfig.setAccessTokenProvider(new GpApiSessionInfo());
        }

        GpApiConnector connector = new GpApiConnector(gpApiConfig);

        GpApiTokenResponse data = connector.getAccessToken();

        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();

        accessTokenInfo.setAccessToken(data.getToken());
        accessTokenInfo.setType(data.getType());
        accessTokenInfo.setMerchantId(data.getMerchantId());
        accessTokenInfo.setMerchantName(data.getMerchantName());
        accessTokenInfo.setAppId(data.getAppId());
        accessTokenInfo.setAppName(data.getAppName());
        accessTokenInfo.setTimeCreated(data.getTimeCreated());
        accessTokenInfo.setSecondsToExpire(data.getSecondsToExpire());
        accessTokenInfo.setIntervalToExpire(data.getIntervalToExpire());
        accessTokenInfo.setEmail(data.getEmail());
        accessTokenInfo.setAccounts(data.getAccounts());
        accessTokenInfo.setDataAccountName(data.getDataAccountName());
        accessTokenInfo.setDisputeManagementAccountName(data.getDisputeManagementAccountName());
        accessTokenInfo.setTokenizationAccountName(data.getTokenizationAccountName());
        accessTokenInfo.setTransactionProcessingAccountName(data.getTransactionProcessingAccountName());
        accessTokenInfo.setRiskAssessmentAccountName(data.getRiskAssessmentAccountName());
        accessTokenInfo.setMerchantManagementAccountName(data.getMerchantManagementAccountName());
        accessTokenInfo.setDataAccountID(data.getDataAccountID());
        accessTokenInfo.setDisputeManagementAccountID(data.getDisputeManagementAccountID());
        accessTokenInfo.setTokenizationAccountID(data.getTokenizationAccountID());
        accessTokenInfo.setTransactionProcessingAccountID(data.getTransactionProcessingAccountID());
        accessTokenInfo.setRiskAssessmentAccountID(data.getRiskAssessmentAccountID());
        accessTokenInfo.setMerchantManagementAccountID(data.getMerchantManagementAccountID());
        accessTokenInfo.setFileProcessingAccountID(data.getFileProcessingAccountID());
        accessTokenInfo.setFileProcessingAccountName(data.getFileProcessingAccountName());

        return accessTokenInfo;
    }
}
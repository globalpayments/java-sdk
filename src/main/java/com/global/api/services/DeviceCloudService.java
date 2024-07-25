package com.global.api.services;

import com.global.api.ServicesContainer;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.diamond.responses.DiamondCloudResponse;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;

public class DeviceCloudService {

    private ConnectionConfig Config;

    public DeviceCloudService(ConnectionConfig config) throws ConfigurationException {
        this.Config = config;
        ServicesContainer.configureService(config);
    }

    public TerminalResponse parseResponse(String response) throws ApiException {
        if (StringUtils.isNullOrEmpty(response)) {
            throw new ApiException("Enable to parse : empty response");
        }
        if (!JsonDoc.isJson(response)) {
            throw new ApiException("Unexpected response format!");
        }

        switch (this.Config.getConnectionMode()) {
            case DIAMOND_CLOUD:
                return new DiamondCloudResponse(response);
            default:
                throw new UnsupportedTransactionException("The selected gateway does not support this response type!");
        }
    }
}

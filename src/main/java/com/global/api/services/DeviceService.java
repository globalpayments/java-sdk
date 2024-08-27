package com.global.api.services;

import com.global.api.ServicesContainer;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.abstractions.IDeviceInterface;

public class DeviceService {
    public static IDeviceInterface create(ConnectionConfig config) throws ApiException {
        return create(config, "default");
    }
    public static IDeviceInterface create(ConnectionConfig config, String configName) throws ApiException {
        ServicesContainer.configureService(config, configName);
        if (config.getGatewayConfig() != null) {
            ServicesContainer.configureService(config.getGatewayConfig(), "_upa_passthrough");
        }
        return ServicesContainer.getInstance().getDeviceInterface(configName);
    }
}

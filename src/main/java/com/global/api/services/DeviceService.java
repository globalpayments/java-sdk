package com.global.api.services;

import com.global.api.ServicesConfig;
import com.global.api.ServicesContainer;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.terminals.abstractions.IDeviceInterface;

public class DeviceService {
    public static IDeviceInterface create(ServicesConfig config) throws ApiException {
        return create(config, "default");
    }
    public static IDeviceInterface create(ServicesConfig config, String configName) throws ApiException {
        ServicesContainer.configure(config, configName);
        return ServicesContainer.getInstance().getDeviceInterface(configName);
    }
}

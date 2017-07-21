package com.global.api.services;

import com.global.api.ServicesConfig;
import com.global.api.ServicesContainer;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.terminals.abstractions.IDeviceInterface;

public class DeviceService {
    public static IDeviceInterface create(ServicesConfig config) throws ApiException {
        ServicesContainer.configure(config);
        return ServicesContainer.getInstance().getDeviceInterface();
    }
}

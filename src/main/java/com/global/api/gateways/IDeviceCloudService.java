package com.global.api.gateways;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.utils.JsonDoc;

public interface IDeviceCloudService {

    String processPassThrough(JsonDoc request) throws ApiException;
}

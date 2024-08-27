package com.global.api.entities;


import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.gpApi.GpApiRequest;
import com.global.api.gateways.GpApiConnector;

public interface IRequestBuilder<T> {

    GpApiRequest buildRequest(T builder, GpApiConnector gateway) throws ApiException;

}

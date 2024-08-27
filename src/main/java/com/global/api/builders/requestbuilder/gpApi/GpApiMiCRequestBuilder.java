package com.global.api.builders.requestbuilder.gpApi;

import com.global.api.entities.IRequestBuilder;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.gpApi.GpApiRequest;
import com.global.api.gateways.GpApiConnector;
import com.global.api.serviceConfigs.GpApiConfig;

public class GpApiMiCRequestBuilder implements IRequestBuilder<String> {

    @Override
    public GpApiRequest buildRequest(String builder, GpApiConnector gateway) throws ApiException {

        return (GpApiRequest)
                new GpApiRequest()
                        .setVerb(GpApiRequest.HttpMethod.Post)
                        .setEndpoint(GpApiRequest.DEVICE_ENDPOINT)
                        .setRequestBody(builder);
    }

}
package com.global.api.builders.requestbuilder.gpApi;

import com.global.api.builders.FileProcessingBuilder;
import com.global.api.entities.IRequestBuilder;
import com.global.api.entities.gpApi.GpApiRequest;
import com.global.api.gateways.GpApiConnector;
import com.global.api.utils.JsonDoc;
import lombok.var;

public class GpApiFileProcessingRequestBuilder implements IRequestBuilder<FileProcessingBuilder> {

    public GpApiRequest buildRequest(FileProcessingBuilder builder, GpApiConnector gateway) {
        switch (builder.getFileProcessingActionType()) {
            case CREATE_UPLOAD_URL:
                var data = new JsonDoc()
                        .set("merchant_id", gateway.getGpApiConfig().getMerchantId())
                        .set("account_id", gateway.getGpApiConfig().getAccessTokenInfo().getFileProcessingAccountID());

                var notifications = new JsonDoc()
                        .set("status_url", gateway.getGpApiConfig().getStatusUrl());

                if (notifications.getKeys().size() > 0) {
                    data.set("notifications", notifications);
                }

                return (GpApiRequest)
                        new GpApiRequest()
                                .setVerb(GpApiRequest.HttpMethod.Post)
                                .setEndpoint(GpApiRequest.FILE_PROCESSING)
                                .setRequestBody(data.toString());

            case GET_DETAILS:
                return (GpApiRequest)
                        new GpApiRequest()
                                .setVerb(GpApiRequest.HttpMethod.Get)
                                .setEndpoint(GpApiRequest.FILE_PROCESSING + "/" + builder.getResourceId());

            default:
                return null;
        }
    }
}

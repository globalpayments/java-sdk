package com.global.api.services;

import com.global.api.builders.FileProcessingBuilder;
import com.global.api.entities.FileProcessor;
import com.global.api.entities.enums.FileProcessingActionType;
import com.global.api.entities.exceptions.ApiException;

public class FileProcessingService {
    public FileProcessor initiate() throws ApiException {
        return new FileProcessingBuilder(FileProcessingActionType.CREATE_UPLOAD_URL)
                .execute();
    }

    public FileProcessor getDetails(String resourceId) throws ApiException {
        return new FileProcessingBuilder(FileProcessingActionType.GET_DETAILS)
                .withResourceId(resourceId)
                .execute();
    }
}

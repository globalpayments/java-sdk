package com.global.api.gateways;

import com.global.api.builders.FileProcessingBuilder;
import com.global.api.entities.FileProcessor;
import com.global.api.entities.exceptions.ApiException;

public interface IFileProcessingService {
    FileProcessor processFileUpload(FileProcessingBuilder builder) throws ApiException;

}

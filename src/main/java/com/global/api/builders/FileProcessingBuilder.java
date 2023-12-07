package com.global.api.builders;

import com.global.api.ServicesContainer;
import com.global.api.entities.FileProcessor;
import com.global.api.entities.enums.FileProcessingActionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.gateways.IFileProcessingService;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileProcessingBuilder extends BaseBuilder<FileProcessor> {

    private String resourceId;

    private FileProcessingActionType fileProcessingActionType;

    public FileProcessingBuilder(FileProcessingActionType actionType) {
        this.fileProcessingActionType = actionType;
    }

    public FileProcessor execute() throws ApiException {
        return this.execute("default");
    }

    @Override
    public FileProcessor execute(String configName) throws ApiException {
        super.execute(configName);
        IFileProcessingService client = ServicesContainer.getInstance().getFileProcessingClient(configName);
        return client.processFileUpload(this);
    }

    public FileProcessingBuilder withResourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    @Override
    public void setupValidations() {
        this.validations.of(FileProcessingActionType.GET_DETAILS)
                .check("resourceId").isNotNull();
    }

}

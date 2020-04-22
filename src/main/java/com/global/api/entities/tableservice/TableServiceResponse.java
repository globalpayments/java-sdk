package com.global.api.entities.tableservice;

import com.global.api.ServicesContainer;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.gateways.TableServiceConnector;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.MultipartForm;
import com.global.api.utils.StringUtils;

public class TableServiceResponse extends BaseTableServiceResponse {
    protected String configName = "default";
    protected String expectedAction;

    public TableServiceResponse(String json, String configName) throws ApiException {
        super(json);
        this.configName = configName;
    }

    protected void mapResponse(JsonDoc response) throws ApiException {
        if(!StringUtils.isNullOrEmpty(expectedAction) && !action.equals(expectedAction))
            throw new MessageException(String.format("Unexpected message type received. %s", action));
    }

    protected <T extends TableServiceResponse> T sendRequest(Class<T> clazz, String endpoint, MultipartForm formData) throws ApiException {
        TableServiceConnector connector = ServicesContainer.getInstance().getTableService(configName);
        if(!connector.isConfigured() && !endpoint.equals("user/login"))
            throw new ConfigurationException("Table service has not been configured properly. Please ensure you have logged in first.");

        try {
            String response = connector.call(endpoint, formData);
            return clazz.getDeclaredConstructor(String.class, String.class).newInstance(response, configName);
        }
        catch(Exception exc) {
            throw new ApiException(exc.getMessage(), exc);
        }
    }
}

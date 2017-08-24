package com.global.api.entities.tableservice;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;

import java.util.List;

public abstract class BaseTableServiceResponse {
    private List<String> messageIds;

    protected String responseCode;
    protected String responseText;
    protected String action;

    public String getResponseCode() {
        return responseCode;
    }
    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }
    public String getResponseText() {
        return responseText;
    }
    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }

    public BaseTableServiceResponse(String json) throws ApiException {
        if(StringUtils.isNullOrEmpty(json))
            return;

        JsonDoc response = JsonDoc.parse(json);

        responseCode = normalizeResponse(response.getString("code"));
        responseText = response.getString("codeMsg");
        action = response.getString("action");

        if(!responseCode.equals("00"))
            throw new MessageException(responseText);

        if(response.has("data")) {
            JsonDoc data = response.get("data");
            if(data.has("row")) {
                JsonDoc row = data.get("row");
                mapResponse(row == null ? data : row);
            }
        }
    }

    protected abstract void mapResponse(JsonDoc response) throws ApiException;

    protected String normalizeResponse(String responseCode) {
        if(responseCode.equals("01"))
            return "00";
        return responseCode;
    }
}

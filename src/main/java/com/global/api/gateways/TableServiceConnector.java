package com.global.api.gateways;

import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.tableservice.BumpStatusCollection;
import com.global.api.utils.MultipartForm;
import com.global.api.utils.StringUtils;

public class TableServiceConnector extends Gateway {
    private String locationId;
    private String securityToken;
    private String sessionId;
    private BumpStatusCollection bumpStatusCollection;

    public boolean isConfigured() {
        return !StringUtils.isNullOrEmpty(locationId)
                && !StringUtils.isNullOrEmpty(securityToken)
                && !StringUtils.isNullOrEmpty(sessionId);
    }
    public String getLocationId() {
        return locationId;
    }
    public String getSecurityToken() {
        return securityToken;
    }
    public String getSessionId() {
        return sessionId;
    }
    public BumpStatusCollection getBumpStatusCollection() {
        return bumpStatusCollection;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }
    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    public void setBumpStatusCollection(BumpStatusCollection bumpStatusCollection) {
        this.bumpStatusCollection = bumpStatusCollection;
    }

    public TableServiceConnector() {
        super("multipart/form-data;");
    }

    public String call(String endpoint, MultipartForm content) throws GatewayException {
        try {
            content.set("locID", locationId);
            content.set("token", securityToken);
            content.set("sessionID", sessionId);

            GatewayResponse response = sendRequest(endpoint, content.getContent());
            if(response.getStatusCode() != 200) {
                // TODO: put some error handling here
            }
            return response.getRawResponse();
        }
        catch(GatewayException exc) { throw exc; }
        catch(Exception exc) {
            throw new GatewayException(exc.getMessage(), exc);
        }
    }
}

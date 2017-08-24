package com.global.api.entities.tableservice;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.utils.JsonDoc;

public class LoginResponse extends TableServiceResponse {
    private String locationId;
    private String token;
    private String tableStatus;

    public String getLocationId() {
        return locationId;
    }
    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getSessionId() {
        return "10101";
    }
    public String getTableStatus() {
        return tableStatus;
    }
    public void setTableStatus(String tableStatus) {
        this.tableStatus = tableStatus;
    }

    public LoginResponse(String json) throws ApiException {
        super(json);
        expectedAction = "login";
    }

    protected void mapResponse(JsonDoc response) throws ApiException {
        super.mapResponse(response);

        locationId = response.getString("locID");
        token = response.getString("token");
        tableStatus = response.getString("tableStatus");
    }
}

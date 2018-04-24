package com.global.api.services;

import com.global.api.ServicesContainer;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.tableservice.*;
import com.global.api.gateways.TableServiceConnector;
import com.global.api.serviceConfigs.TableServiceConfig;
import com.global.api.utils.MultipartForm;
import com.global.api.utils.StringUtils;

import java.lang.reflect.Constructor;
import java.util.Date;

public class TableService {
    private String configName = "default";

    public String[] getBumpStatuses() throws ApiException {
        return ServicesContainer.getInstance().getTableService(configName).getBumpStatusCollection().getKeys();
    }

    public TableService(TableServiceConfig config) throws ApiException {
        this(config, "default");
    }
    public TableService(TableServiceConfig config, String configName) throws ApiException {
        this.configName = configName;
        ServicesContainer.configureService(config, configName);
    }

    private <T extends TableServiceResponse> T sendRequest(Class<T> clazz, String endpoint, MultipartForm formData) throws ApiException {
        TableServiceConnector connector = ServicesContainer.getInstance().getTableService(configName);
        if(!connector.isConfigured() && !endpoint.equals("user/login"))
            throw new ConfigurationException("Reservation service has not been configured properly. Please ensure you have logged in first.");

        String response = connector.call(endpoint, formData);
        try {
            Constructor<T> instance = clazz.getConstructor(String.class, String.class);
            return instance.newInstance(response, configName);
        }
        catch(Exception exc) {
            throw new ApiException(exc.getMessage(), exc);
        }
    }

    public LoginResponse login(String username, String password) throws ApiException {
        MultipartForm content = new MultipartForm()
                .set("username", username)
                .set("password", password);

        LoginResponse response = sendRequest(LoginResponse.class, "user/login", content);

        // configure the connector
        TableServiceConnector connector = ServicesContainer.getInstance().getTableService(configName);
        connector.setLocationId(response.getLocationId());
        connector.setSecurityToken(response.getToken());
        connector.setSessionId(response.getSessionId());
        connector.setBumpStatusCollection(new BumpStatusCollection(response.getTableStatus()));

        return response;
    }

    public Ticket assignCheck(int tableNumber, int checkId) throws ApiException {
        return assignCheck(tableNumber, checkId, null);
    }
    public Ticket assignCheck(int tableNumber, int checkId, Date startTime) throws ApiException {
        MultipartForm content = new MultipartForm()
                .set("tableNumber", tableNumber)
                .set("checkID", checkId)
                .set("startTime", startTime == null ? new Date() : startTime);

        Ticket response = sendRequest(Ticket.class, "pos/assignCheck", content);
        response.setTableNumber(tableNumber);
        return response;
    }

    public Ticket queryTableStatus(int tableNumber) throws ApiException {
        MultipartForm content = new MultipartForm()
                .set("tableNumber", tableNumber);

        Ticket response = sendRequest(Ticket.class, "pos/tableStatus", content);
        response.setTableNumber(tableNumber);
        return response;
    }
    public Ticket queryCheckStatus(int checkId) throws ApiException {
        MultipartForm content = new MultipartForm()
                .set("checkID", checkId);

        return sendRequest(Ticket.class, "pos/checkStatus", content);
    }

    public ServerListResponse getServerList() throws ApiException {
        return sendRequest(ServerListResponse.class, "pos/getServerList", new MultipartForm());
    }

    public ServerListResponse updateServerList(String... serverList) throws ApiException {
        String _serverList = StringUtils.join(",", serverList);

        MultipartForm content = new MultipartForm()
                .set("serverList", _serverList);

        sendRequest(TableServiceResponse.class, "pos/updateServerList", content);
        return getServerList();
    }

    public ServerAssignmentResponse getServerAssignments() throws ApiException {
        return sendRequest(ServerAssignmentResponse.class, "pos/getServerAssignment", new MultipartForm());
    }
    public ServerAssignmentResponse getServerAssignments(String serverName) throws ApiException {
        MultipartForm content = new MultipartForm()
                .set("serverName", serverName);

        return sendRequest(ServerAssignmentResponse.class, "pos/getServerAssignment", content);
    }
    public ServerAssignmentResponse getServerAssignments(int tableNumber) throws ApiException {
        MultipartForm content = new MultipartForm()
                .set("tableNumber", tableNumber);

        return sendRequest(ServerAssignmentResponse.class, "pos/getServerAssignment", content);
    }

    public ServerAssignmentResponse assignShift(ShiftAssignments shiftData) throws ApiException {
        MultipartForm content = new MultipartForm()
                .set("shiftData", shiftData.toString());

        sendRequest(TableServiceResponse.class, "pos/assignShift", content);
        return getServerAssignments();
    }
}
